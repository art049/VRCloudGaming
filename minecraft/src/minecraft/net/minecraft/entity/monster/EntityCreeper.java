package net.minecraft.entity.monster;

import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAICreeperSwell;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

public class EntityCreeper extends EntityMob {
   private static final DataParameter<Integer> STATE = EntityDataManager.<Integer>createKey(EntityCreeper.class, DataSerializers.VARINT);
   private static final DataParameter<Boolean> POWERED = EntityDataManager.<Boolean>createKey(EntityCreeper.class, DataSerializers.BOOLEAN);
   private static final DataParameter<Boolean> IGNITED = EntityDataManager.<Boolean>createKey(EntityCreeper.class, DataSerializers.BOOLEAN);
   private int lastActiveTime;
   private int timeSinceIgnited;
   private int fuseTime = 30;
   private int explosionRadius = 3;
   private int droppedSkulls = 0;

   public EntityCreeper(World worldIn) {
      super(worldIn);
      this.setSize(0.6F, 1.7F);
   }

   protected void initEntityAI() {
      this.tasks.addTask(1, new EntityAISwimming(this));
      this.tasks.addTask(2, new EntityAICreeperSwell(this));
      this.tasks.addTask(3, new EntityAIAvoidEntity(this, EntityOcelot.class, 6.0F, 1.0D, 1.2D));
      this.tasks.addTask(4, new EntityAIAttackMelee(this, 1.0D, false));
      this.tasks.addTask(5, new EntityAIWander(this, 0.8D));
      this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
      this.tasks.addTask(6, new EntityAILookIdle(this));
      this.targetTasks.addTask(1, new EntityAINearestAttackableTarget(this, EntityPlayer.class, true));
      this.targetTasks.addTask(2, new EntityAIHurtByTarget(this, false, new Class[0]));
   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
   }

   public int getMaxFallHeight() {
      return this.getAttackTarget() == null?3:3 + (int)(this.getHealth() - 1.0F);
   }

   public void fall(float distance, float damageMultiplier) {
      super.fall(distance, damageMultiplier);
      this.timeSinceIgnited = (int)((float)this.timeSinceIgnited + distance * 1.5F);
      if(this.timeSinceIgnited > this.fuseTime - 5) {
         this.timeSinceIgnited = this.fuseTime - 5;
      }
   }

   protected void entityInit() {
      super.entityInit();
      this.dataManager.register(STATE, Integer.valueOf(-1));
      this.dataManager.register(POWERED, Boolean.valueOf(false));
      this.dataManager.register(IGNITED, Boolean.valueOf(false));
   }

   public void writeEntityToNBT(NBTTagCompound compound) {
      super.writeEntityToNBT(compound);
      if(((Boolean)this.dataManager.get(POWERED)).booleanValue()) {
         compound.setBoolean("powered", true);
      }

      compound.setShort("Fuse", (short)this.fuseTime);
      compound.setByte("ExplosionRadius", (byte)this.explosionRadius);
      compound.setBoolean("ignited", this.hasIgnited());
   }

   public void readEntityFromNBT(NBTTagCompound compound) {
      super.readEntityFromNBT(compound);
      this.dataManager.set(POWERED, Boolean.valueOf(compound.getBoolean("powered")));
      if(compound.hasKey("Fuse", 99)) {
         this.fuseTime = compound.getShort("Fuse");
      }

      if(compound.hasKey("ExplosionRadius", 99)) {
         this.explosionRadius = compound.getByte("ExplosionRadius");
      }

      if(compound.getBoolean("ignited")) {
         this.ignite();
      }
   }

   public void onUpdate() {
      if(this.isEntityAlive()) {
         this.lastActiveTime = this.timeSinceIgnited;
         if(this.hasIgnited()) {
            this.setCreeperState(1);
         }

         int i = this.getCreeperState();
         if(i > 0 && this.timeSinceIgnited == 0) {
            this.playSound(SoundEvents.ENTITY_CREEPER_PRIMED, 1.0F, 0.5F);
         }

         this.timeSinceIgnited += i;
         if(this.timeSinceIgnited < 0) {
            this.timeSinceIgnited = 0;
         }

         if(this.timeSinceIgnited >= this.fuseTime) {
            this.timeSinceIgnited = this.fuseTime;
            this.explode();
         }
      }

      super.onUpdate();
   }

   protected SoundEvent getHurtSound() {
      return SoundEvents.ENTITY_CREEPER_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_CREEPER_DEATH;
   }

   public void onDeath(DamageSource cause) {
      super.onDeath(cause);
      if(this.worldObj.getGameRules().getBoolean("doMobLoot")) {
         if(cause.getEntity() instanceof EntitySkeleton) {
            int i = Item.getIdFromItem(Items.RECORD_13);
            int j = Item.getIdFromItem(Items.RECORD_WAIT);
            int k = i + this.rand.nextInt(j - i + 1);
            this.dropItem(Item.getItemById(k), 1);
         } else if(cause.getEntity() instanceof EntityCreeper && cause.getEntity() != this && ((EntityCreeper)cause.getEntity()).getPowered() && ((EntityCreeper)cause.getEntity()).isAIEnabled()) {
            ((EntityCreeper)cause.getEntity()).incrementDroppedSkulls();
            this.entityDropItem(new ItemStack(Items.SKULL, 1, 4), 0.0F);
         }
      }
   }

   public boolean attackEntityAsMob(Entity entityIn) {
      return true;
   }

   public boolean getPowered() {
      return ((Boolean)this.dataManager.get(POWERED)).booleanValue();
   }

   public float getCreeperFlashIntensity(float p_70831_1_) {
      return ((float)this.lastActiveTime + (float)(this.timeSinceIgnited - this.lastActiveTime) * p_70831_1_) / (float)(this.fuseTime - 2);
   }

   @Nullable
   protected ResourceLocation getLootTable() {
      return LootTableList.ENTITIES_CREEPER;
   }

   public int getCreeperState() {
      return ((Integer)this.dataManager.get(STATE)).intValue();
   }

   public void setCreeperState(int state) {
      this.dataManager.set(STATE, Integer.valueOf(state));
   }

   public void onStruckByLightning(EntityLightningBolt lightningBolt) {
      super.onStruckByLightning(lightningBolt);
      this.dataManager.set(POWERED, Boolean.valueOf(true));
   }

   protected boolean processInteract(EntityPlayer player, EnumHand hand, @Nullable ItemStack stack) {
      if(stack != null && stack.getItem() == Items.FLINT_AND_STEEL) {
         this.worldObj.playSound(player, this.posX, this.posY, this.posZ, SoundEvents.ITEM_FLINTANDSTEEL_USE, this.getSoundCategory(), 1.0F, this.rand.nextFloat() * 0.4F + 0.8F);
         player.swingArm(hand);
         if(!this.worldObj.isRemote) {
            this.ignite();
            stack.damageItem(1, player);
            return true;
         }
      }

      return super.processInteract(player, hand, stack);
   }

   private void explode() {
      if(!this.worldObj.isRemote) {
         boolean flag = this.worldObj.getGameRules().getBoolean("mobGriefing");
         float f = this.getPowered()?2.0F:1.0F;
         this.dead = true;
         this.worldObj.createExplosion(this, this.posX, this.posY, this.posZ, (float)this.explosionRadius * f, flag);
         this.setDead();
      }
   }

   public boolean hasIgnited() {
      return ((Boolean)this.dataManager.get(IGNITED)).booleanValue();
   }

   public void ignite() {
      this.dataManager.set(IGNITED, Boolean.valueOf(true));
   }

   public boolean isAIEnabled() {
      return this.droppedSkulls < 1 && this.worldObj.getGameRules().getBoolean("doMobLoot");
   }

   public void incrementDroppedSkulls() {
      ++this.droppedSkulls;
   }
}
