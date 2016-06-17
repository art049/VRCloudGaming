package net.minecraft.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EntityAIAttackMelee extends EntityAIBase {
   World worldObj;
   protected EntityCreature attacker;
   int attackTick;
   double speedTowardsTarget;
   boolean longMemory;
   Path entityPathEntity;
   private int delayCounter;
   private double targetX;
   private double targetY;
   private double targetZ;
   protected final int attackInterval = 20;

   public EntityAIAttackMelee(EntityCreature creature, double speedIn, boolean useLongMemory) {
      this.attacker = creature;
      this.worldObj = creature.worldObj;
      this.speedTowardsTarget = speedIn;
      this.longMemory = useLongMemory;
      this.setMutexBits(3);
   }

   public boolean shouldExecute() {
      EntityLivingBase entitylivingbase = this.attacker.getAttackTarget();
      if(entitylivingbase == null) {
         return false;
      } else if(!entitylivingbase.isEntityAlive()) {
         return false;
      } else {
         this.entityPathEntity = this.attacker.getNavigator().getPathToEntityLiving(entitylivingbase);
         return this.entityPathEntity != null;
      }
   }

   public boolean continueExecuting() {
      EntityLivingBase entitylivingbase = this.attacker.getAttackTarget();
      return entitylivingbase == null?false:(!entitylivingbase.isEntityAlive()?false:(!this.longMemory?!this.attacker.getNavigator().noPath():(!this.attacker.isWithinHomeDistanceFromPosition(new BlockPos(entitylivingbase))?false:!(entitylivingbase instanceof EntityPlayer) || !((EntityPlayer)entitylivingbase).isSpectator() && !((EntityPlayer)entitylivingbase).isCreative())));
   }

   public void startExecuting() {
      this.attacker.getNavigator().setPath(this.entityPathEntity, this.speedTowardsTarget);
      this.delayCounter = 0;
   }

   public void resetTask() {
      EntityLivingBase entitylivingbase = this.attacker.getAttackTarget();
      if(entitylivingbase instanceof EntityPlayer && (((EntityPlayer)entitylivingbase).isSpectator() || ((EntityPlayer)entitylivingbase).isCreative())) {
         this.attacker.setAttackTarget((EntityLivingBase)null);
      }

      this.attacker.getNavigator().clearPathEntity();
   }

   public void updateTask() {
      EntityLivingBase entitylivingbase = this.attacker.getAttackTarget();
      this.attacker.getLookHelper().setLookPositionWithEntity(entitylivingbase, 30.0F, 30.0F);
      double d0 = this.attacker.getDistanceSq(entitylivingbase.posX, entitylivingbase.getEntityBoundingBox().minY, entitylivingbase.posZ);
      double d1 = this.getAttackReachSqr(entitylivingbase);
      --this.delayCounter;
      if((this.longMemory || this.attacker.getEntitySenses().canSee(entitylivingbase)) && this.delayCounter <= 0 && (this.targetX == 0.0D && this.targetY == 0.0D && this.targetZ == 0.0D || entitylivingbase.getDistanceSq(this.targetX, this.targetY, this.targetZ) >= 1.0D || this.attacker.getRNG().nextFloat() < 0.05F)) {
         this.targetX = entitylivingbase.posX;
         this.targetY = entitylivingbase.getEntityBoundingBox().minY;
         this.targetZ = entitylivingbase.posZ;
         this.delayCounter = 4 + this.attacker.getRNG().nextInt(7);
         if(d0 > 1024.0D) {
            this.delayCounter += 10;
         } else if(d0 > 256.0D) {
            this.delayCounter += 5;
         }

         if(!this.attacker.getNavigator().tryMoveToEntityLiving(entitylivingbase, this.speedTowardsTarget)) {
            this.delayCounter += 15;
         }
      }

      this.attackTick = Math.max(this.attackTick - 1, 0);
      if(d0 <= d1 && this.attackTick <= 0) {
         this.attackTick = 20;
         this.attacker.swingArm(EnumHand.MAIN_HAND);
         this.attacker.attackEntityAsMob(entitylivingbase);
      }
   }

   protected double getAttackReachSqr(EntityLivingBase attackTarget) {
      return (double)(this.attacker.width * 2.0F * this.attacker.width * 2.0F + attackTarget.width);
   }
}
