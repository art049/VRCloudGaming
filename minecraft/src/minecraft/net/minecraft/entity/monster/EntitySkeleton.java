package net.minecraft.entity.monster;

import java.util.Calendar;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIAttackRangedBow;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIFleeSun;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIRestrictSun;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.AchievementList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderHell;
import net.minecraft.world.storage.loot.LootTableList;

public class EntitySkeleton extends EntityMob implements IRangedAttackMob {
   private static final DataParameter<Integer> SKELETON_VARIANT = EntityDataManager.<Integer>createKey(EntitySkeleton.class, DataSerializers.VARINT);
   private static final DataParameter<Boolean> SWINGING_ARMS = EntityDataManager.<Boolean>createKey(EntitySkeleton.class, DataSerializers.BOOLEAN);
   private final EntityAIAttackRangedBow aiArrowAttack = new EntityAIAttackRangedBow(this, 1.0D, 20, 15.0F);
   private final EntityAIAttackMelee aiAttackOnCollide = new EntityAIAttackMelee(this, 1.2D, false) {
      public void resetTask() {
         super.resetTask();
         EntitySkeleton.this.setSwingingArms(false);
      }

      public void startExecuting() {
         super.startExecuting();
         EntitySkeleton.this.setSwingingArms(true);
      }
   };

   public EntitySkeleton(World worldIn) {
      super(worldIn);
      this.setCombatTask();
   }

   protected void initEntityAI() {
      this.tasks.addTask(1, new EntityAISwimming(this));
      this.tasks.addTask(2, new EntityAIRestrictSun(this));
      this.tasks.addTask(3, new EntityAIFleeSun(this, 1.0D));
      this.tasks.addTask(3, new EntityAIAvoidEntity(this, EntityWolf.class, 6.0F, 1.0D, 1.2D));
      this.tasks.addTask(5, new EntityAIWander(this, 1.0D));
      this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
      this.tasks.addTask(6, new EntityAILookIdle(this));
      this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false, new Class[0]));
      this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, true));
      this.targetTasks.addTask(3, new EntityAINearestAttackableTarget(this, EntityIronGolem.class, true));
   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
   }

   protected void entityInit() {
      super.entityInit();
      this.dataManager.register(SKELETON_VARIANT, Integer.valueOf(0));
      this.dataManager.register(SWINGING_ARMS, Boolean.valueOf(false));
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENTITY_SKELETON_AMBIENT;
   }

   protected SoundEvent getHurtSound() {
      return SoundEvents.ENTITY_SKELETON_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_SKELETON_DEATH;
   }

   protected void playStepSound(BlockPos pos, Block blockIn) {
      this.playSound(SoundEvents.ENTITY_SKELETON_STEP, 0.15F, 1.0F);
   }

   public boolean attackEntityAsMob(Entity entityIn) {
      if(super.attackEntityAsMob(entityIn)) {
         if(this.getSkeletonType() == 1 && entityIn instanceof EntityLivingBase) {
            ((EntityLivingBase)entityIn).addPotionEffect(new PotionEffect(MobEffects.WITHER, 200));
         }

         return true;
      } else {
         return false;
      }
   }

   public EnumCreatureAttribute getCreatureAttribute() {
      return EnumCreatureAttribute.UNDEAD;
   }

   public void onLivingUpdate() {
      if(this.worldObj.isDaytime() && !this.worldObj.isRemote) {
         float f = this.getBrightness(1.0F);
         BlockPos blockpos = this.getRidingEntity() instanceof EntityBoat?(new BlockPos(this.posX, (double)Math.round(this.posY), this.posZ)).up():new BlockPos(this.posX, (double)Math.round(this.posY), this.posZ);
         if(f > 0.5F && this.rand.nextFloat() * 30.0F < (f - 0.4F) * 2.0F && this.worldObj.canSeeSky(blockpos)) {
            boolean flag = true;
            ItemStack itemstack = this.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
            if(itemstack != null) {
               if(itemstack.isItemStackDamageable()) {
                  itemstack.setItemDamage(itemstack.getItemDamage() + this.rand.nextInt(2));
                  if(itemstack.getItemDamage() >= itemstack.getMaxDamage()) {
                     this.renderBrokenItemStack(itemstack);
                     this.setItemStackToSlot(EntityEquipmentSlot.HEAD, (ItemStack)null);
                  }
               }

               flag = false;
            }

            if(flag) {
               this.setFire(8);
            }
         }
      }

      if(this.worldObj.isRemote) {
         this.updateSize(this.getSkeletonType());
      }

      super.onLivingUpdate();
   }

   public void updateRidden() {
      super.updateRidden();
      if(this.getRidingEntity() instanceof EntityCreature) {
         EntityCreature entitycreature = (EntityCreature)this.getRidingEntity();
         this.renderYawOffset = entitycreature.renderYawOffset;
      }
   }

   public void onDeath(DamageSource cause) {
      super.onDeath(cause);
      if(cause.getSourceOfDamage() instanceof EntityArrow && cause.getEntity() instanceof EntityPlayer) {
         EntityPlayer entityplayer = (EntityPlayer)cause.getEntity();
         double d0 = entityplayer.posX - this.posX;
         double d1 = entityplayer.posZ - this.posZ;
         if(d0 * d0 + d1 * d1 >= 2500.0D) {
            entityplayer.addStat(AchievementList.SNIPE_SKELETON);
         }
      } else if(cause.getEntity() instanceof EntityCreeper && ((EntityCreeper)cause.getEntity()).getPowered() && ((EntityCreeper)cause.getEntity()).isAIEnabled()) {
         ((EntityCreeper)cause.getEntity()).incrementDroppedSkulls();
         this.entityDropItem(new ItemStack(Items.SKULL, 1, this.getSkeletonType() == 1?1:0), 0.0F);
      }
   }

   @Nullable
   protected ResourceLocation getLootTable() {
      return this.getSkeletonType() == 1?LootTableList.ENTITIES_WITHER_SKELETON:LootTableList.ENTITIES_SKELETON;
   }

   protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficulty) {
      super.setEquipmentBasedOnDifficulty(difficulty);
      this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
   }

   @Nullable
   public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
      livingdata = super.onInitialSpawn(difficulty, livingdata);
      if(this.worldObj.provider instanceof WorldProviderHell && this.getRNG().nextInt(5) > 0) {
         this.tasks.addTask(4, this.aiAttackOnCollide);
         this.setSkeletonType(1);
         this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
         this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4.0D);
      } else {
         this.tasks.addTask(4, this.aiArrowAttack);
         this.setEquipmentBasedOnDifficulty(difficulty);
         this.setEnchantmentBasedOnDifficulty(difficulty);
      }

      this.setCanPickUpLoot(this.rand.nextFloat() < 0.55F * difficulty.getClampedAdditionalDifficulty());
      if(this.getItemStackFromSlot(EntityEquipmentSlot.HEAD) == null) {
         Calendar calendar = this.worldObj.getCurrentDate();
         if(calendar.get(2) + 1 == 10 && calendar.get(5) == 31 && this.rand.nextFloat() < 0.25F) {
            this.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(this.rand.nextFloat() < 0.1F?Blocks.LIT_PUMPKIN:Blocks.PUMPKIN));
            this.inventoryArmorDropChances[EntityEquipmentSlot.HEAD.getIndex()] = 0.0F;
         }
      }

      return livingdata;
   }

   public void setCombatTask() {
      if(this.worldObj != null && !this.worldObj.isRemote) {
         this.tasks.removeTask(this.aiAttackOnCollide);
         this.tasks.removeTask(this.aiArrowAttack);
         ItemStack itemstack = this.getHeldItemMainhand();
         if(itemstack != null && itemstack.getItem() == Items.BOW) {
            int i = 20;
            if(this.worldObj.getDifficulty() != EnumDifficulty.HARD) {
               i = 40;
            }

            this.aiArrowAttack.func_189428_b(i);
            this.tasks.addTask(4, this.aiArrowAttack);
         } else {
            this.tasks.addTask(4, this.aiAttackOnCollide);
         }
      }
   }

   public void attackEntityWithRangedAttack(EntityLivingBase target, float p_82196_2_) {
      EntityArrow entityarrow = new EntityTippedArrow(this.worldObj, this);
      double d0 = target.posX - this.posX;
      double d1 = target.getEntityBoundingBox().minY + (double)(target.height / 3.0F) - entityarrow.posY;
      double d2 = target.posZ - this.posZ;
      double d3 = (double)MathHelper.sqrt_double(d0 * d0 + d2 * d2);
      entityarrow.setThrowableHeading(d0, d1 + d3 * 0.20000000298023224D, d2, 1.6F, (float)(14 - this.worldObj.getDifficulty().getDifficultyId() * 4));
      int i = EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.POWER, this);
      int j = EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.PUNCH, this);
      entityarrow.setDamage((double)(p_82196_2_ * 2.0F) + this.rand.nextGaussian() * 0.25D + (double)((float)this.worldObj.getDifficulty().getDifficultyId() * 0.11F));
      if(i > 0) {
         entityarrow.setDamage(entityarrow.getDamage() + (double)i * 0.5D + 0.5D);
      }

      if(j > 0) {
         entityarrow.setKnockbackStrength(j);
      }

      if(EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.FLAME, this) > 0 || this.getSkeletonType() == 1) {
         entityarrow.setFire(100);
      }

      this.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.getRNG().nextFloat() * 0.4F + 0.8F));
      this.worldObj.spawnEntityInWorld(entityarrow);
   }

   public int getSkeletonType() {
      return ((Integer)this.dataManager.get(SKELETON_VARIANT)).intValue();
   }

   public void setSkeletonType(int p_82201_1_) {
      this.dataManager.set(SKELETON_VARIANT, Integer.valueOf(p_82201_1_));
      this.isImmuneToFire = p_82201_1_ == 1;
      this.updateSize(p_82201_1_);
   }

   private void updateSize(int p_184726_1_) {
      if(p_184726_1_ == 1) {
         this.setSize(0.7F, 2.4F);
      } else {
         this.setSize(0.6F, 1.99F);
      }
   }

   public void readEntityFromNBT(NBTTagCompound compound) {
      super.readEntityFromNBT(compound);
      if(compound.hasKey("SkeletonType", 99)) {
         int i = compound.getByte("SkeletonType");
         this.setSkeletonType(i);
      }

      this.setCombatTask();
   }

   public void writeEntityToNBT(NBTTagCompound compound) {
      super.writeEntityToNBT(compound);
      compound.setByte("SkeletonType", (byte)this.getSkeletonType());
   }

   public void setItemStackToSlot(EntityEquipmentSlot slotIn, @Nullable ItemStack stack) {
      super.setItemStackToSlot(slotIn, stack);
      if(!this.worldObj.isRemote && slotIn == EntityEquipmentSlot.MAINHAND) {
         this.setCombatTask();
      }
   }

   public float getEyeHeight() {
      return this.getSkeletonType() == 1?2.1F:1.74F;
   }

   public double getYOffset() {
      return -0.35D;
   }

   public boolean isSwingingArms() {
      return ((Boolean)this.dataManager.get(SWINGING_ARMS)).booleanValue();
   }

   public void setSwingingArms(boolean swingingArms) {
      this.dataManager.set(SWINGING_ARMS, Boolean.valueOf(swingingArms));
   }
}
