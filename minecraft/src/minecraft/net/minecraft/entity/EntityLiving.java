package net.minecraft.entity;

import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.EntityJumpHelper;
import net.minecraft.entity.ai.EntityLookHelper;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.ai.EntitySenses;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPacketEntityAttach;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.stats.AchievementList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;

public abstract class EntityLiving extends EntityLivingBase {
   private static final DataParameter<Byte> AI_FLAGS = EntityDataManager.<Byte>createKey(EntityLiving.class, DataSerializers.BYTE);
   public int livingSoundTime;
   protected int experienceValue;
   private EntityLookHelper lookHelper;
   protected EntityMoveHelper moveHelper;
   protected EntityJumpHelper jumpHelper;
   private EntityBodyHelper bodyHelper;
   protected PathNavigate navigator;
   protected final EntityAITasks tasks;
   protected final EntityAITasks targetTasks;
   private EntityLivingBase attackTarget;
   private EntitySenses senses;
   private ItemStack[] inventoryHands = new ItemStack[2];
   protected float[] inventoryHandsDropChances = new float[2];
   private ItemStack[] inventoryArmor = new ItemStack[4];
   protected float[] inventoryArmorDropChances = new float[4];
   private boolean canPickUpLoot;
   private boolean persistenceRequired;
   private Map<PathNodeType, Float> mapPathPriority = Maps.newEnumMap(PathNodeType.class);
   private ResourceLocation deathLootTable;
   private long deathLootTableSeed;
   private boolean isLeashed;
   private Entity leashedToEntity;
   private NBTTagCompound leashNBTTag;

   public EntityLiving(World worldIn) {
      super(worldIn);
      this.tasks = new EntityAITasks(worldIn != null && worldIn.theProfiler != null?worldIn.theProfiler:null);
      this.targetTasks = new EntityAITasks(worldIn != null && worldIn.theProfiler != null?worldIn.theProfiler:null);
      this.lookHelper = new EntityLookHelper(this);
      this.moveHelper = new EntityMoveHelper(this);
      this.jumpHelper = new EntityJumpHelper(this);
      this.bodyHelper = this.createBodyHelper();
      this.navigator = this.getNewNavigator(worldIn);
      this.senses = new EntitySenses(this);

      for(int i = 0; i < this.inventoryArmorDropChances.length; ++i) {
         this.inventoryArmorDropChances[i] = 0.085F;
      }

      for(int j = 0; j < this.inventoryHandsDropChances.length; ++j) {
         this.inventoryHandsDropChances[j] = 0.085F;
      }

      if(worldIn != null && !worldIn.isRemote) {
         this.initEntityAI();
      }
   }

   protected void initEntityAI() {
   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getAttributeMap().registerAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(16.0D);
   }

   protected PathNavigate getNewNavigator(World worldIn) {
      return new PathNavigateGround(this, worldIn);
   }

   public float getPathPriority(PathNodeType nodeType) {
      Float f = (Float)this.mapPathPriority.get(nodeType);
      return f == null?nodeType.getPriority():f.floatValue();
   }

   public void setPathPriority(PathNodeType nodeType, float priority) {
      this.mapPathPriority.put(nodeType, Float.valueOf(priority));
   }

   protected EntityBodyHelper createBodyHelper() {
      return new EntityBodyHelper(this);
   }

   public EntityLookHelper getLookHelper() {
      return this.lookHelper;
   }

   public EntityMoveHelper getMoveHelper() {
      return this.moveHelper;
   }

   public EntityJumpHelper getJumpHelper() {
      return this.jumpHelper;
   }

   public PathNavigate getNavigator() {
      return this.navigator;
   }

   public EntitySenses getEntitySenses() {
      return this.senses;
   }

   @Nullable
   public EntityLivingBase getAttackTarget() {
      return this.attackTarget;
   }

   public void setAttackTarget(@Nullable EntityLivingBase entitylivingbaseIn) {
      this.attackTarget = entitylivingbaseIn;
   }

   public boolean canAttackClass(Class<? extends EntityLivingBase> cls) {
      return cls != EntityGhast.class;
   }

   public void eatGrassBonus() {
   }

   protected void entityInit() {
      super.entityInit();
      this.dataManager.register(AI_FLAGS, Byte.valueOf((byte)0));
   }

   public int getTalkInterval() {
      return 80;
   }

   public void playLivingSound() {
      SoundEvent soundevent = this.getAmbientSound();
      if(soundevent != null) {
         this.playSound(soundevent, this.getSoundVolume(), this.getSoundPitch());
      }
   }

   public void onEntityUpdate() {
      super.onEntityUpdate();
      this.worldObj.theProfiler.startSection("mobBaseTick");
      if(this.isEntityAlive() && this.rand.nextInt(1000) < this.livingSoundTime++) {
         this.applyEntityAI();
         this.playLivingSound();
      }

      this.worldObj.theProfiler.endSection();
   }

   protected void playHurtSound(DamageSource source) {
      this.applyEntityAI();
      super.playHurtSound(source);
   }

   private void applyEntityAI() {
      this.livingSoundTime = -this.getTalkInterval();
   }

   protected int getExperiencePoints(EntityPlayer player) {
      if(this.experienceValue > 0) {
         int i = this.experienceValue;

         for(int j = 0; j < this.inventoryArmor.length; ++j) {
            if(this.inventoryArmor[j] != null && this.inventoryArmorDropChances[j] <= 1.0F) {
               i += 1 + this.rand.nextInt(3);
            }
         }

         for(int k = 0; k < this.inventoryHands.length; ++k) {
            if(this.inventoryHands[k] != null && this.inventoryHandsDropChances[k] <= 1.0F) {
               i += 1 + this.rand.nextInt(3);
            }
         }

         return i;
      } else {
         return this.experienceValue;
      }
   }

   public void spawnExplosionParticle() {
      if(this.worldObj.isRemote) {
         for(int i = 0; i < 20; ++i) {
            double d0 = this.rand.nextGaussian() * 0.02D;
            double d1 = this.rand.nextGaussian() * 0.02D;
            double d2 = this.rand.nextGaussian() * 0.02D;
            double d3 = 10.0D;
            this.worldObj.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, this.posX + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width - d0 * d3, this.posY + (double)(this.rand.nextFloat() * this.height) - d1 * d3, this.posZ + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width - d2 * d3, d0, d1, d2, new int[0]);
         }
      } else {
         this.worldObj.setEntityState(this, (byte)20);
      }
   }

   public void handleStatusUpdate(byte id) {
      if(id == 20) {
         this.spawnExplosionParticle();
      } else {
         super.handleStatusUpdate(id);
      }
   }

   public void onUpdate() {
      super.onUpdate();
      if(!this.worldObj.isRemote) {
         this.updateLeashedState();
         if(this.ticksExisted % 5 == 0) {
            boolean flag = !(this.getControllingPassenger() instanceof EntityLiving);
            boolean flag1 = !(this.getRidingEntity() instanceof EntityBoat);
            this.tasks.setControlFlag(5, flag && flag1);
            this.tasks.setControlFlag(2, flag);
         }
      }
   }

   protected float updateDistance(float p_110146_1_, float p_110146_2_) {
      this.bodyHelper.updateRenderAngles();
      return p_110146_2_;
   }

   @Nullable
   protected SoundEvent getAmbientSound() {
      return null;
   }

   @Nullable
   protected Item getDropItem() {
      return null;
   }

   protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
      Item item = this.getDropItem();
      if(item != null) {
         int i = this.rand.nextInt(3);
         if(lootingModifier > 0) {
            i += this.rand.nextInt(lootingModifier + 1);
         }

         for(int j = 0; j < i; ++j) {
            this.dropItem(item, 1);
         }
      }
   }

   public void writeEntityToNBT(NBTTagCompound compound) {
      super.writeEntityToNBT(compound);
      compound.setBoolean("CanPickUpLoot", this.canPickUpLoot());
      compound.setBoolean("PersistenceRequired", this.persistenceRequired);
      NBTTagList nbttaglist = new NBTTagList();

      for(int i = 0; i < this.inventoryArmor.length; ++i) {
         NBTTagCompound nbttagcompound = new NBTTagCompound();
         if(this.inventoryArmor[i] != null) {
            this.inventoryArmor[i].writeToNBT(nbttagcompound);
         }

         nbttaglist.appendTag(nbttagcompound);
      }

      compound.setTag("ArmorItems", nbttaglist);
      NBTTagList nbttaglist1 = new NBTTagList();

      for(int k = 0; k < this.inventoryHands.length; ++k) {
         NBTTagCompound nbttagcompound1 = new NBTTagCompound();
         if(this.inventoryHands[k] != null) {
            this.inventoryHands[k].writeToNBT(nbttagcompound1);
         }

         nbttaglist1.appendTag(nbttagcompound1);
      }

      compound.setTag("HandItems", nbttaglist1);
      NBTTagList nbttaglist2 = new NBTTagList();

      for(int l = 0; l < this.inventoryArmorDropChances.length; ++l) {
         nbttaglist2.appendTag(new NBTTagFloat(this.inventoryArmorDropChances[l]));
      }

      compound.setTag("ArmorDropChances", nbttaglist2);
      NBTTagList nbttaglist3 = new NBTTagList();

      for(int j = 0; j < this.inventoryHandsDropChances.length; ++j) {
         nbttaglist3.appendTag(new NBTTagFloat(this.inventoryHandsDropChances[j]));
      }

      compound.setTag("HandDropChances", nbttaglist3);
      compound.setBoolean("Leashed", this.isLeashed);
      if(this.leashedToEntity != null) {
         NBTTagCompound nbttagcompound2 = new NBTTagCompound();
         if(this.leashedToEntity instanceof EntityLivingBase) {
            UUID uuid = this.leashedToEntity.getUniqueID();
            nbttagcompound2.setUniqueId("UUID", uuid);
         } else if(this.leashedToEntity instanceof EntityHanging) {
            BlockPos blockpos = ((EntityHanging)this.leashedToEntity).getHangingPosition();
            nbttagcompound2.setInteger("X", blockpos.getX());
            nbttagcompound2.setInteger("Y", blockpos.getY());
            nbttagcompound2.setInteger("Z", blockpos.getZ());
         }

         compound.setTag("Leash", nbttagcompound2);
      }

      compound.setBoolean("LeftHanded", this.isLeftHanded());
      if(this.deathLootTable != null) {
         compound.setString("DeathLootTable", this.deathLootTable.toString());
         if(this.deathLootTableSeed != 0L) {
            compound.setLong("DeathLootTableSeed", this.deathLootTableSeed);
         }
      }

      if(this.isAIDisabled()) {
         compound.setBoolean("NoAI", this.isAIDisabled());
      }
   }

   public void readEntityFromNBT(NBTTagCompound compound) {
      super.readEntityFromNBT(compound);
      if(compound.hasKey("CanPickUpLoot", 1)) {
         this.setCanPickUpLoot(compound.getBoolean("CanPickUpLoot"));
      }

      this.persistenceRequired = compound.getBoolean("PersistenceRequired");
      if(compound.hasKey("ArmorItems", 9)) {
         NBTTagList nbttaglist = compound.getTagList("ArmorItems", 10);

         for(int i = 0; i < this.inventoryArmor.length; ++i) {
            this.inventoryArmor[i] = ItemStack.loadItemStackFromNBT(nbttaglist.getCompoundTagAt(i));
         }
      }

      if(compound.hasKey("HandItems", 9)) {
         NBTTagList nbttaglist1 = compound.getTagList("HandItems", 10);

         for(int j = 0; j < this.inventoryHands.length; ++j) {
            this.inventoryHands[j] = ItemStack.loadItemStackFromNBT(nbttaglist1.getCompoundTagAt(j));
         }
      }

      if(compound.hasKey("ArmorDropChances", 9)) {
         NBTTagList nbttaglist2 = compound.getTagList("ArmorDropChances", 5);

         for(int k = 0; k < nbttaglist2.tagCount(); ++k) {
            this.inventoryArmorDropChances[k] = nbttaglist2.getFloatAt(k);
         }
      }

      if(compound.hasKey("HandDropChances", 9)) {
         NBTTagList nbttaglist3 = compound.getTagList("HandDropChances", 5);

         for(int l = 0; l < nbttaglist3.tagCount(); ++l) {
            this.inventoryHandsDropChances[l] = nbttaglist3.getFloatAt(l);
         }
      }

      this.isLeashed = compound.getBoolean("Leashed");
      if(this.isLeashed && compound.hasKey("Leash", 10)) {
         this.leashNBTTag = compound.getCompoundTag("Leash");
      }

      this.setLeftHanded(compound.getBoolean("LeftHanded"));
      if(compound.hasKey("DeathLootTable", 8)) {
         this.deathLootTable = new ResourceLocation(compound.getString("DeathLootTable"));
         this.deathLootTableSeed = compound.getLong("DeathLootTableSeed");
      }

      this.setNoAI(compound.getBoolean("NoAI"));
   }

   @Nullable
   protected ResourceLocation getLootTable() {
      return null;
   }

   protected void dropLoot(boolean wasRecentlyHit, int lootingModifier, DamageSource source) {
      ResourceLocation resourcelocation = this.deathLootTable;
      if(resourcelocation == null) {
         resourcelocation = this.getLootTable();
      }

      if(resourcelocation != null) {
         LootTable loottable = this.worldObj.getLootTableManager().getLootTableFromLocation(resourcelocation);
         this.deathLootTable = null;
         LootContext.Builder lootcontext$builder = (new LootContext.Builder((WorldServer)this.worldObj)).withLootedEntity(this).withDamageSource(source);
         if(wasRecentlyHit && this.attackingPlayer != null) {
            lootcontext$builder = lootcontext$builder.withPlayer(this.attackingPlayer).withLuck(this.attackingPlayer.getLuck());
         }

         for(ItemStack itemstack : loottable.generateLootForPools(this.deathLootTableSeed == 0L?this.rand:new Random(this.deathLootTableSeed), lootcontext$builder.build())) {
            this.entityDropItem(itemstack, 0.0F);
         }

         this.dropEquipment(wasRecentlyHit, lootingModifier);
      } else {
         super.dropLoot(wasRecentlyHit, lootingModifier, source);
      }
   }

   public void setMoveForward(float amount) {
      this.moveForward = amount;
   }

   public void setMoveStrafing(float amount) {
      this.moveStrafing = amount;
   }

   public void setAIMoveSpeed(float speedIn) {
      super.setAIMoveSpeed(speedIn);
      this.setMoveForward(speedIn);
   }

   public void onLivingUpdate() {
      super.onLivingUpdate();
      this.worldObj.theProfiler.startSection("looting");
      if(!this.worldObj.isRemote && this.canPickUpLoot() && !this.dead && this.worldObj.getGameRules().getBoolean("mobGriefing")) {
         for(EntityItem entityitem : this.worldObj.getEntitiesWithinAABB(EntityItem.class, this.getEntityBoundingBox().expand(1.0D, 0.0D, 1.0D))) {
            if(!entityitem.isDead && entityitem.getEntityItem() != null && !entityitem.cannotPickup()) {
               this.updateEquipmentIfNeeded(entityitem);
            }
         }
      }

      this.worldObj.theProfiler.endSection();
   }

   protected void updateEquipmentIfNeeded(EntityItem itemEntity) {
      ItemStack itemstack = itemEntity.getEntityItem();
      EntityEquipmentSlot entityequipmentslot = getSlotForItemStack(itemstack);
      boolean flag = true;
      ItemStack itemstack1 = this.getItemStackFromSlot(entityequipmentslot);
      if(itemstack1 != null) {
         if(entityequipmentslot.getSlotType() == EntityEquipmentSlot.Type.HAND) {
            if(itemstack.getItem() instanceof ItemSword && !(itemstack1.getItem() instanceof ItemSword)) {
               flag = true;
            } else if(itemstack.getItem() instanceof ItemSword && itemstack1.getItem() instanceof ItemSword) {
               ItemSword itemsword = (ItemSword)itemstack.getItem();
               ItemSword itemsword1 = (ItemSword)itemstack1.getItem();
               if(itemsword.getDamageVsEntity() == itemsword1.getDamageVsEntity()) {
                  flag = itemstack.getMetadata() > itemstack1.getMetadata() || itemstack.hasTagCompound() && !itemstack1.hasTagCompound();
               } else {
                  flag = itemsword.getDamageVsEntity() > itemsword1.getDamageVsEntity();
               }
            } else if(itemstack.getItem() instanceof ItemBow && itemstack1.getItem() instanceof ItemBow) {
               flag = itemstack.hasTagCompound() && !itemstack1.hasTagCompound();
            } else {
               flag = false;
            }
         } else if(itemstack.getItem() instanceof ItemArmor && !(itemstack1.getItem() instanceof ItemArmor)) {
            flag = true;
         } else if(itemstack.getItem() instanceof ItemArmor && itemstack1.getItem() instanceof ItemArmor) {
            ItemArmor itemarmor = (ItemArmor)itemstack.getItem();
            ItemArmor itemarmor1 = (ItemArmor)itemstack1.getItem();
            if(itemarmor.damageReduceAmount == itemarmor1.damageReduceAmount) {
               flag = itemstack.getMetadata() > itemstack1.getMetadata() || itemstack.hasTagCompound() && !itemstack1.hasTagCompound();
            } else {
               flag = itemarmor.damageReduceAmount > itemarmor1.damageReduceAmount;
            }
         } else {
            flag = false;
         }
      }

      if(flag && this.canEquipItem(itemstack)) {
         double d0;
         switch(entityequipmentslot.getSlotType()) {
         case HAND:
            d0 = (double)this.inventoryHandsDropChances[entityequipmentslot.getIndex()];
            break;
         case ARMOR:
            d0 = (double)this.inventoryArmorDropChances[entityequipmentslot.getIndex()];
            break;
         default:
            d0 = 0.0D;
         }

         if(itemstack1 != null && (double)(this.rand.nextFloat() - 0.1F) < d0) {
            this.entityDropItem(itemstack1, 0.0F);
         }

         if(itemstack.getItem() == Items.DIAMOND && itemEntity.getThrower() != null) {
            EntityPlayer entityplayer = this.worldObj.getPlayerEntityByName(itemEntity.getThrower());
            if(entityplayer != null) {
               entityplayer.addStat(AchievementList.DIAMONDS_TO_YOU);
            }
         }

         this.setItemStackToSlot(entityequipmentslot, itemstack);
         switch(entityequipmentslot.getSlotType()) {
         case HAND:
            this.inventoryHandsDropChances[entityequipmentslot.getIndex()] = 2.0F;
            break;
         case ARMOR:
            this.inventoryArmorDropChances[entityequipmentslot.getIndex()] = 2.0F;
         }

         this.persistenceRequired = true;
         this.onItemPickup(itemEntity, 1);
         itemEntity.setDead();
      }
   }

   protected boolean canEquipItem(ItemStack stack) {
      return true;
   }

   protected boolean canDespawn() {
      return true;
   }

   protected void despawnEntity() {
      if(this.persistenceRequired) {
         this.entityAge = 0;
      } else {
         Entity entity = this.worldObj.getClosestPlayerToEntity(this, -1.0D);
         if(entity != null) {
            double d0 = entity.posX - this.posX;
            double d1 = entity.posY - this.posY;
            double d2 = entity.posZ - this.posZ;
            double d3 = d0 * d0 + d1 * d1 + d2 * d2;
            if(this.canDespawn() && d3 > 16384.0D) {
               this.setDead();
            }

            if(this.entityAge > 600 && this.rand.nextInt(800) == 0 && d3 > 1024.0D && this.canDespawn()) {
               this.setDead();
            } else if(d3 < 1024.0D) {
               this.entityAge = 0;
            }
         }
      }
   }

   protected final void updateEntityActionState() {
      ++this.entityAge;
      this.worldObj.theProfiler.startSection("checkDespawn");
      this.despawnEntity();
      this.worldObj.theProfiler.endSection();
      this.worldObj.theProfiler.startSection("sensing");
      this.senses.clearSensingCache();
      this.worldObj.theProfiler.endSection();
      this.worldObj.theProfiler.startSection("targetSelector");
      this.targetTasks.onUpdateTasks();
      this.worldObj.theProfiler.endSection();
      this.worldObj.theProfiler.startSection("goalSelector");
      this.tasks.onUpdateTasks();
      this.worldObj.theProfiler.endSection();
      this.worldObj.theProfiler.startSection("navigation");
      this.navigator.onUpdateNavigation();
      this.worldObj.theProfiler.endSection();
      this.worldObj.theProfiler.startSection("mob tick");
      this.updateAITasks();
      this.worldObj.theProfiler.endSection();
      if(this.isRiding() && this.getRidingEntity() instanceof EntityLiving) {
         EntityLiving entityliving = (EntityLiving)this.getRidingEntity();
         entityliving.getNavigator().setPath(this.getNavigator().getPath(), 1.5D);
         entityliving.getMoveHelper().read(this.getMoveHelper());
      }

      this.worldObj.theProfiler.startSection("controls");
      this.worldObj.theProfiler.startSection("move");
      this.moveHelper.onUpdateMoveHelper();
      this.worldObj.theProfiler.endStartSection("look");
      this.lookHelper.onUpdateLook();
      this.worldObj.theProfiler.endStartSection("jump");
      this.jumpHelper.doJump();
      this.worldObj.theProfiler.endSection();
      this.worldObj.theProfiler.endSection();
   }

   protected void updateAITasks() {
   }

   public int getVerticalFaceSpeed() {
      return 40;
   }

   public int getHorizontalFaceSpeed() {
      return 10;
   }

   public void faceEntity(Entity entityIn, float maxYawIncrease, float maxPitchIncrease) {
      double d0 = entityIn.posX - this.posX;
      double d2 = entityIn.posZ - this.posZ;
      double d1;
      if(entityIn instanceof EntityLivingBase) {
         EntityLivingBase entitylivingbase = (EntityLivingBase)entityIn;
         d1 = entitylivingbase.posY + (double)entitylivingbase.getEyeHeight() - (this.posY + (double)this.getEyeHeight());
      } else {
         d1 = (entityIn.getEntityBoundingBox().minY + entityIn.getEntityBoundingBox().maxY) / 2.0D - (this.posY + (double)this.getEyeHeight());
      }

      double d3 = (double)MathHelper.sqrt_double(d0 * d0 + d2 * d2);
      float f = (float)(MathHelper.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
      float f1 = (float)(-(MathHelper.atan2(d1, d3) * (180D / Math.PI)));
      this.rotationPitch = this.updateRotation(this.rotationPitch, f1, maxPitchIncrease);
      this.rotationYaw = this.updateRotation(this.rotationYaw, f, maxYawIncrease);
   }

   private float updateRotation(float angle, float targetAngle, float maxIncrease) {
      float f = MathHelper.wrapDegrees(targetAngle - angle);
      if(f > maxIncrease) {
         f = maxIncrease;
      }

      if(f < -maxIncrease) {
         f = -maxIncrease;
      }

      return angle + f;
   }

   public boolean getCanSpawnHere() {
      return true;
   }

   public boolean isNotColliding() {
      return !this.worldObj.containsAnyLiquid(this.getEntityBoundingBox()) && this.worldObj.getCollisionBoxes(this, this.getEntityBoundingBox()).isEmpty() && this.worldObj.checkNoEntityCollision(this.getEntityBoundingBox(), this);
   }

   public float getRenderSizeModifier() {
      return 1.0F;
   }

   public int getMaxSpawnedInChunk() {
      return 4;
   }

   public int getMaxFallHeight() {
      if(this.getAttackTarget() == null) {
         return 3;
      } else {
         int i = (int)(this.getHealth() - this.getMaxHealth() * 0.33F);
         i = i - (3 - this.worldObj.getDifficulty().getDifficultyId()) * 4;
         if(i < 0) {
            i = 0;
         }

         return i + 3;
      }
   }

   public Iterable<ItemStack> getHeldEquipment() {
      return Arrays.<ItemStack>asList(this.inventoryHands);
   }

   public Iterable<ItemStack> getArmorInventoryList() {
      return Arrays.<ItemStack>asList(this.inventoryArmor);
   }

   @Nullable
   public ItemStack getItemStackFromSlot(EntityEquipmentSlot slotIn) {
      ItemStack itemstack = null;
      switch(slotIn.getSlotType()) {
      case HAND:
         itemstack = this.inventoryHands[slotIn.getIndex()];
         break;
      case ARMOR:
         itemstack = this.inventoryArmor[slotIn.getIndex()];
      }

      return itemstack;
   }

   public void setItemStackToSlot(EntityEquipmentSlot slotIn, @Nullable ItemStack stack) {
      switch(slotIn.getSlotType()) {
      case HAND:
         this.inventoryHands[slotIn.getIndex()] = stack;
         break;
      case ARMOR:
         this.inventoryArmor[slotIn.getIndex()] = stack;
      }
   }

   protected void dropEquipment(boolean wasRecentlyHit, int lootingModifier) {
      for(EntityEquipmentSlot entityequipmentslot : EntityEquipmentSlot.values()) {
         ItemStack itemstack = this.getItemStackFromSlot(entityequipmentslot);
         double d0;
         switch(entityequipmentslot.getSlotType()) {
         case HAND:
            d0 = (double)this.inventoryHandsDropChances[entityequipmentslot.getIndex()];
            break;
         case ARMOR:
            d0 = (double)this.inventoryArmorDropChances[entityequipmentslot.getIndex()];
            break;
         default:
            d0 = 0.0D;
         }

         boolean flag = d0 > 1.0D;
         if(itemstack != null && (wasRecentlyHit || flag) && (double)(this.rand.nextFloat() - (float)lootingModifier * 0.01F) < d0) {
            if(!flag && itemstack.isItemStackDamageable()) {
               int i = Math.max(itemstack.getMaxDamage() - 25, 1);
               int j = itemstack.getMaxDamage() - this.rand.nextInt(this.rand.nextInt(i) + 1);
               if(j > i) {
                  j = i;
               }

               if(j < 1) {
                  j = 1;
               }

               itemstack.setItemDamage(j);
            }

            this.entityDropItem(itemstack, 0.0F);
         }
      }
   }

   protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficulty) {
      if(this.rand.nextFloat() < 0.15F * difficulty.getClampedAdditionalDifficulty()) {
         int i = this.rand.nextInt(2);
         float f = this.worldObj.getDifficulty() == EnumDifficulty.HARD?0.1F:0.25F;
         if(this.rand.nextFloat() < 0.095F) {
            ++i;
         }

         if(this.rand.nextFloat() < 0.095F) {
            ++i;
         }

         if(this.rand.nextFloat() < 0.095F) {
            ++i;
         }

         boolean flag = true;

         for(EntityEquipmentSlot entityequipmentslot : EntityEquipmentSlot.values()) {
            if(entityequipmentslot.getSlotType() == EntityEquipmentSlot.Type.ARMOR) {
               ItemStack itemstack = this.getItemStackFromSlot(entityequipmentslot);
               if(!flag && this.rand.nextFloat() < f) {
                  break;
               }

               flag = false;
               if(itemstack == null) {
                  Item item = getArmorByChance(entityequipmentslot, i);
                  if(item != null) {
                     this.setItemStackToSlot(entityequipmentslot, new ItemStack(item));
                  }
               }
            }
         }
      }
   }

   public static EntityEquipmentSlot getSlotForItemStack(ItemStack stack) {
      return stack.getItem() != Item.getItemFromBlock(Blocks.PUMPKIN) && stack.getItem() != Items.SKULL?(stack.getItem() == Items.ELYTRA?EntityEquipmentSlot.CHEST:(stack.getItem() instanceof ItemArmor?((ItemArmor)stack.getItem()).armorType:(stack.getItem() == Items.ELYTRA?EntityEquipmentSlot.CHEST:EntityEquipmentSlot.MAINHAND))):EntityEquipmentSlot.HEAD;
   }

   public static Item getArmorByChance(EntityEquipmentSlot slotIn, int chance) {
      switch(slotIn) {
      case HEAD:
         if(chance == 0) {
            return Items.LEATHER_HELMET;
         } else if(chance == 1) {
            return Items.GOLDEN_HELMET;
         } else if(chance == 2) {
            return Items.CHAINMAIL_HELMET;
         } else if(chance == 3) {
            return Items.IRON_HELMET;
         } else if(chance == 4) {
            return Items.DIAMOND_HELMET;
         }
      case CHEST:
         if(chance == 0) {
            return Items.LEATHER_CHESTPLATE;
         } else if(chance == 1) {
            return Items.GOLDEN_CHESTPLATE;
         } else if(chance == 2) {
            return Items.CHAINMAIL_CHESTPLATE;
         } else if(chance == 3) {
            return Items.IRON_CHESTPLATE;
         } else if(chance == 4) {
            return Items.DIAMOND_CHESTPLATE;
         }
      case LEGS:
         if(chance == 0) {
            return Items.LEATHER_LEGGINGS;
         } else if(chance == 1) {
            return Items.GOLDEN_LEGGINGS;
         } else if(chance == 2) {
            return Items.CHAINMAIL_LEGGINGS;
         } else if(chance == 3) {
            return Items.IRON_LEGGINGS;
         } else if(chance == 4) {
            return Items.DIAMOND_LEGGINGS;
         }
      case FEET:
         if(chance == 0) {
            return Items.LEATHER_BOOTS;
         } else if(chance == 1) {
            return Items.GOLDEN_BOOTS;
         } else if(chance == 2) {
            return Items.CHAINMAIL_BOOTS;
         } else if(chance == 3) {
            return Items.IRON_BOOTS;
         } else if(chance == 4) {
            return Items.DIAMOND_BOOTS;
         }
      default:
         return null;
      }
   }

   protected void setEnchantmentBasedOnDifficulty(DifficultyInstance difficulty) {
      float f = difficulty.getClampedAdditionalDifficulty();
      if(this.getHeldItemMainhand() != null && this.rand.nextFloat() < 0.25F * f) {
         EnchantmentHelper.addRandomEnchantment(this.rand, this.getHeldItemMainhand(), (int)(5.0F + f * (float)this.rand.nextInt(18)), false);
      }

      for(EntityEquipmentSlot entityequipmentslot : EntityEquipmentSlot.values()) {
         if(entityequipmentslot.getSlotType() == EntityEquipmentSlot.Type.ARMOR) {
            ItemStack itemstack = this.getItemStackFromSlot(entityequipmentslot);
            if(itemstack != null && this.rand.nextFloat() < 0.5F * f) {
               EnchantmentHelper.addRandomEnchantment(this.rand, itemstack, (int)(5.0F + f * (float)this.rand.nextInt(18)), false);
            }
         }
      }
   }

   @Nullable
   public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
      this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).applyModifier(new AttributeModifier("Random spawn bonus", this.rand.nextGaussian() * 0.05D, 1));
      if(this.rand.nextFloat() < 0.05F) {
         this.setLeftHanded(true);
      } else {
         this.setLeftHanded(false);
      }

      return livingdata;
   }

   public boolean canBeSteered() {
      return false;
   }

   public void enablePersistence() {
      this.persistenceRequired = true;
   }

   public void setDropChance(EntityEquipmentSlot slotIn, float chance) {
      switch(slotIn.getSlotType()) {
      case HAND:
         this.inventoryHandsDropChances[slotIn.getIndex()] = chance;
         break;
      case ARMOR:
         this.inventoryArmorDropChances[slotIn.getIndex()] = chance;
      }
   }

   public boolean canPickUpLoot() {
      return this.canPickUpLoot;
   }

   public void setCanPickUpLoot(boolean canPickup) {
      this.canPickUpLoot = canPickup;
   }

   public boolean isNoDespawnRequired() {
      return this.persistenceRequired;
   }

   public final boolean processInitialInteract(EntityPlayer player, @Nullable ItemStack stack, EnumHand hand) {
      if(this.getLeashed() && this.getLeashedToEntity() == player) {
         this.clearLeashed(true, !player.capabilities.isCreativeMode);
         return true;
      } else if(stack != null && stack.getItem() == Items.LEAD && this.canBeLeashedTo(player)) {
         this.setLeashedToEntity(player, true);
         --stack.stackSize;
         return true;
      } else {
         return this.processInteract(player, hand, stack)?true:super.processInitialInteract(player, stack, hand);
      }
   }

   protected boolean processInteract(EntityPlayer player, EnumHand hand, @Nullable ItemStack stack) {
      return false;
   }

   protected void updateLeashedState() {
      if(this.leashNBTTag != null) {
         this.recreateLeash();
      }

      if(this.isLeashed) {
         if(!this.isEntityAlive()) {
            this.clearLeashed(true, true);
         }

         if(this.leashedToEntity == null || this.leashedToEntity.isDead) {
            this.clearLeashed(true, true);
         }
      }
   }

   public void clearLeashed(boolean sendPacket, boolean dropLead) {
      if(this.isLeashed) {
         this.isLeashed = false;
         this.leashedToEntity = null;
         if(!this.worldObj.isRemote && dropLead) {
            this.dropItem(Items.LEAD, 1);
         }

         if(!this.worldObj.isRemote && sendPacket && this.worldObj instanceof WorldServer) {
            ((WorldServer)this.worldObj).getEntityTracker().sendToAllTrackingEntity(this, new SPacketEntityAttach(this, (Entity)null));
         }
      }
   }

   public boolean canBeLeashedTo(EntityPlayer player) {
      return !this.getLeashed() && !(this instanceof IMob);
   }

   public boolean getLeashed() {
      return this.isLeashed;
   }

   public Entity getLeashedToEntity() {
      return this.leashedToEntity;
   }

   public void setLeashedToEntity(Entity entityIn, boolean sendAttachNotification) {
      this.isLeashed = true;
      this.leashedToEntity = entityIn;
      if(!this.worldObj.isRemote && sendAttachNotification && this.worldObj instanceof WorldServer) {
         ((WorldServer)this.worldObj).getEntityTracker().sendToAllTrackingEntity(this, new SPacketEntityAttach(this, this.leashedToEntity));
      }

      if(this.isRiding()) {
         this.dismountRidingEntity();
      }
   }

   public boolean startRiding(Entity entityIn, boolean force) {
      boolean flag = super.startRiding(entityIn, force);
      if(flag && this.getLeashed()) {
         this.clearLeashed(true, true);
      }

      return flag;
   }

   private void recreateLeash() {
      if(this.isLeashed && this.leashNBTTag != null) {
         if(this.leashNBTTag.hasUniqueId("UUID")) {
            UUID uuid = this.leashNBTTag.getUniqueId("UUID");

            for(EntityLivingBase entitylivingbase : this.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().expandXyz(10.0D))) {
               if(entitylivingbase.getUniqueID().equals(uuid)) {
                  this.leashedToEntity = entitylivingbase;
                  break;
               }
            }
         } else if(this.leashNBTTag.hasKey("X", 99) && this.leashNBTTag.hasKey("Y", 99) && this.leashNBTTag.hasKey("Z", 99)) {
            BlockPos blockpos = new BlockPos(this.leashNBTTag.getInteger("X"), this.leashNBTTag.getInteger("Y"), this.leashNBTTag.getInteger("Z"));
            EntityLeashKnot entityleashknot = EntityLeashKnot.getKnotForPosition(this.worldObj, blockpos);
            if(entityleashknot == null) {
               entityleashknot = EntityLeashKnot.createKnot(this.worldObj, blockpos);
            }

            this.leashedToEntity = entityleashknot;
         } else {
            this.clearLeashed(false, true);
         }
      }

      this.leashNBTTag = null;
   }

   public boolean replaceItemInInventory(int inventorySlot, @Nullable ItemStack itemStackIn) {
      EntityEquipmentSlot entityequipmentslot;
      if(inventorySlot == 98) {
         entityequipmentslot = EntityEquipmentSlot.MAINHAND;
      } else if(inventorySlot == 99) {
         entityequipmentslot = EntityEquipmentSlot.OFFHAND;
      } else if(inventorySlot == 100 + EntityEquipmentSlot.HEAD.getIndex()) {
         entityequipmentslot = EntityEquipmentSlot.HEAD;
      } else if(inventorySlot == 100 + EntityEquipmentSlot.CHEST.getIndex()) {
         entityequipmentslot = EntityEquipmentSlot.CHEST;
      } else if(inventorySlot == 100 + EntityEquipmentSlot.LEGS.getIndex()) {
         entityequipmentslot = EntityEquipmentSlot.LEGS;
      } else {
         if(inventorySlot != 100 + EntityEquipmentSlot.FEET.getIndex()) {
            return false;
         }

         entityequipmentslot = EntityEquipmentSlot.FEET;
      }

      if(itemStackIn != null && !isItemStackInSlot(entityequipmentslot, itemStackIn) && entityequipmentslot != EntityEquipmentSlot.HEAD) {
         return false;
      } else {
         this.setItemStackToSlot(entityequipmentslot, itemStackIn);
         return true;
      }
   }

   public static boolean isItemStackInSlot(EntityEquipmentSlot slotIn, ItemStack stack) {
      EntityEquipmentSlot entityequipmentslot = getSlotForItemStack(stack);
      return entityequipmentslot == slotIn || entityequipmentslot == EntityEquipmentSlot.MAINHAND && slotIn == EntityEquipmentSlot.OFFHAND;
   }

   public boolean isServerWorld() {
      return super.isServerWorld() && !this.isAIDisabled();
   }

   public void setNoAI(boolean disable) {
      byte b0 = ((Byte)this.dataManager.get(AI_FLAGS)).byteValue();
      this.dataManager.set(AI_FLAGS, Byte.valueOf(disable?(byte)(b0 | 1):(byte)(b0 & -2)));
   }

   public void setLeftHanded(boolean disable) {
      byte b0 = ((Byte)this.dataManager.get(AI_FLAGS)).byteValue();
      this.dataManager.set(AI_FLAGS, Byte.valueOf(disable?(byte)(b0 | 2):(byte)(b0 & -3)));
   }

   public boolean isAIDisabled() {
      return (((Byte)this.dataManager.get(AI_FLAGS)).byteValue() & 1) != 0;
   }

   public boolean isLeftHanded() {
      return (((Byte)this.dataManager.get(AI_FLAGS)).byteValue() & 2) != 0;
   }

   public EnumHandSide getPrimaryHand() {
      return this.isLeftHanded()?EnumHandSide.LEFT:EnumHandSide.RIGHT;
   }

   public static enum SpawnPlacementType {
      ON_GROUND,
      IN_AIR,
      IN_WATER;
   }
}
