package net.minecraft.entity.passive;

import com.google.common.base.Predicate;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIFollowOwner;
import net.minecraft.entity.ai.EntityAILeapAtTarget;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAIOcelotAttack;
import net.minecraft.entity.ai.EntityAIOcelotSit;
import net.minecraft.entity.ai.EntityAISit;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITargetNonTamed;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

public class EntityOcelot extends EntityTameable {
   private static final DataParameter<Integer> OCELOT_VARIANT = EntityDataManager.<Integer>createKey(EntityOcelot.class, DataSerializers.VARINT);
   private EntityAIAvoidEntity<EntityPlayer> avoidEntity;
   private EntityAITempt aiTempt;

   public EntityOcelot(World worldIn) {
      super(worldIn);
      this.setSize(0.6F, 0.7F);
   }

   protected void initEntityAI() {
      this.tasks.addTask(1, new EntityAISwimming(this));
      this.tasks.addTask(2, this.aiSit = new EntityAISit(this));
      this.tasks.addTask(3, this.aiTempt = new EntityAITempt(this, 0.6D, Items.FISH, true));
      this.tasks.addTask(5, new EntityAIFollowOwner(this, 1.0D, 10.0F, 5.0F));
      this.tasks.addTask(6, new EntityAIOcelotSit(this, 0.8D));
      this.tasks.addTask(7, new EntityAILeapAtTarget(this, 0.3F));
      this.tasks.addTask(8, new EntityAIOcelotAttack(this));
      this.tasks.addTask(9, new EntityAIMate(this, 0.8D));
      this.tasks.addTask(10, new EntityAIWander(this, 0.8D));
      this.tasks.addTask(11, new EntityAIWatchClosest(this, EntityPlayer.class, 10.0F));
      this.targetTasks.addTask(1, new EntityAITargetNonTamed(this, EntityChicken.class, false, (Predicate)null));
   }

   protected void entityInit() {
      super.entityInit();
      this.dataManager.register(OCELOT_VARIANT, Integer.valueOf(0));
   }

   public void updateAITasks() {
      if(this.getMoveHelper().isUpdating()) {
         double d0 = this.getMoveHelper().getSpeed();
         if(d0 == 0.6D) {
            this.setSneaking(true);
            this.setSprinting(false);
         } else if(d0 == 1.33D) {
            this.setSneaking(false);
            this.setSprinting(true);
         } else {
            this.setSneaking(false);
            this.setSprinting(false);
         }
      } else {
         this.setSneaking(false);
         this.setSprinting(false);
      }
   }

   protected boolean canDespawn() {
      return !this.isTamed() && this.ticksExisted > 2400;
   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
      this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.30000001192092896D);
   }

   public void fall(float distance, float damageMultiplier) {
   }

   public void writeEntityToNBT(NBTTagCompound compound) {
      super.writeEntityToNBT(compound);
      compound.setInteger("CatType", this.getTameSkin());
   }

   public void readEntityFromNBT(NBTTagCompound compound) {
      super.readEntityFromNBT(compound);
      this.setTameSkin(compound.getInteger("CatType"));
   }

   @Nullable
   protected SoundEvent getAmbientSound() {
      return this.isTamed()?(this.isInLove()?SoundEvents.ENTITY_CAT_PURR:(this.rand.nextInt(4) == 0?SoundEvents.ENTITY_CAT_PURREOW:SoundEvents.ENTITY_CAT_AMBIENT)):null;
   }

   protected SoundEvent getHurtSound() {
      return SoundEvents.ENTITY_CAT_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_CAT_DEATH;
   }

   protected float getSoundVolume() {
      return 0.4F;
   }

   public boolean attackEntityAsMob(Entity entityIn) {
      return entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), 3.0F);
   }

   public boolean attackEntityFrom(DamageSource source, float amount) {
      if(this.isEntityInvulnerable(source)) {
         return false;
      } else {
         if(this.aiSit != null) {
            this.aiSit.setSitting(false);
         }

         return super.attackEntityFrom(source, amount);
      }
   }

   @Nullable
   protected ResourceLocation getLootTable() {
      return LootTableList.ENTITIES_OCELOT;
   }

   public boolean processInteract(EntityPlayer player, EnumHand hand, @Nullable ItemStack stack) {
      if(this.isTamed()) {
         if(this.isOwner(player) && !this.worldObj.isRemote && !this.isBreedingItem(stack)) {
            this.aiSit.setSitting(!this.isSitting());
         }
      } else if((this.aiTempt == null || this.aiTempt.isRunning()) && stack != null && stack.getItem() == Items.FISH && player.getDistanceSqToEntity(this) < 9.0D) {
         if(!player.capabilities.isCreativeMode) {
            --stack.stackSize;
         }

         if(!this.worldObj.isRemote) {
            if(this.rand.nextInt(3) == 0) {
               this.setTamed(true);
               this.setTameSkin(1 + this.worldObj.rand.nextInt(3));
               this.setOwnerId(player.getUniqueID());
               this.playTameEffect(true);
               this.aiSit.setSitting(true);
               this.worldObj.setEntityState(this, (byte)7);
            } else {
               this.playTameEffect(false);
               this.worldObj.setEntityState(this, (byte)6);
            }
         }

         return true;
      }

      return super.processInteract(player, hand, stack);
   }

   public EntityOcelot createChild(EntityAgeable ageable) {
      EntityOcelot entityocelot = new EntityOcelot(this.worldObj);
      if(this.isTamed()) {
         entityocelot.setOwnerId(this.getOwnerId());
         entityocelot.setTamed(true);
         entityocelot.setTameSkin(this.getTameSkin());
      }

      return entityocelot;
   }

   public boolean isBreedingItem(@Nullable ItemStack stack) {
      return stack != null && stack.getItem() == Items.FISH;
   }

   public boolean canMateWith(EntityAnimal otherAnimal) {
      if(otherAnimal == this) {
         return false;
      } else if(!this.isTamed()) {
         return false;
      } else if(!(otherAnimal instanceof EntityOcelot)) {
         return false;
      } else {
         EntityOcelot entityocelot = (EntityOcelot)otherAnimal;
         return !entityocelot.isTamed()?false:this.isInLove() && entityocelot.isInLove();
      }
   }

   public int getTameSkin() {
      return ((Integer)this.dataManager.get(OCELOT_VARIANT)).intValue();
   }

   public void setTameSkin(int skinId) {
      this.dataManager.set(OCELOT_VARIANT, Integer.valueOf(skinId));
   }

   public boolean getCanSpawnHere() {
      return this.worldObj.rand.nextInt(3) != 0;
   }

   public boolean isNotColliding() {
      if(this.worldObj.checkNoEntityCollision(this.getEntityBoundingBox(), this) && this.worldObj.getCollisionBoxes(this, this.getEntityBoundingBox()).isEmpty() && !this.worldObj.containsAnyLiquid(this.getEntityBoundingBox())) {
         BlockPos blockpos = new BlockPos(this.posX, this.getEntityBoundingBox().minY, this.posZ);
         if(blockpos.getY() < this.worldObj.getSeaLevel()) {
            return false;
         }

         IBlockState iblockstate = this.worldObj.getBlockState(blockpos.down());
         Block block = iblockstate.getBlock();
         if(block == Blocks.GRASS || iblockstate.getMaterial() == Material.LEAVES) {
            return true;
         }
      }

      return false;
   }

   public String getName() {
      return this.hasCustomName()?this.getCustomNameTag():(this.isTamed()?I18n.translateToLocal("entity.Cat.name"):super.getName());
   }

   public void setTamed(boolean tamed) {
      super.setTamed(tamed);
   }

   protected void setupTamedAI() {
      if(this.avoidEntity == null) {
         this.avoidEntity = new EntityAIAvoidEntity(this, EntityPlayer.class, 16.0F, 0.8D, 1.33D);
      }

      this.tasks.removeTask(this.avoidEntity);
      if(!this.isTamed()) {
         this.tasks.addTask(4, this.avoidEntity);
      }
   }

   @Nullable
   public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
      livingdata = super.onInitialSpawn(difficulty, livingdata);
      if(this.worldObj.rand.nextInt(7) == 0) {
         for(int i = 0; i < 2; ++i) {
            EntityOcelot entityocelot = new EntityOcelot(this.worldObj);
            entityocelot.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, 0.0F);
            entityocelot.setGrowingAge(-24000);
            this.worldObj.spawnEntityInWorld(entityocelot);
         }
      }

      return livingdata;
   }
}
