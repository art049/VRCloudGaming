package net.minecraft.entity.ai;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityTameable;

public class EntityAIOwnerHurtTarget extends EntityAITarget {
   EntityTameable theEntityTameable;
   EntityLivingBase theTarget;
   private int timestamp;

   public EntityAIOwnerHurtTarget(EntityTameable theEntityTameableIn) {
      super(theEntityTameableIn, false);
      this.theEntityTameable = theEntityTameableIn;
      this.setMutexBits(1);
   }

   public boolean shouldExecute() {
      if(!this.theEntityTameable.isTamed()) {
         return false;
      } else {
         EntityLivingBase entitylivingbase = this.theEntityTameable.getOwner();
         if(entitylivingbase == null) {
            return false;
         } else {
            this.theTarget = entitylivingbase.getLastAttacker();
            int i = entitylivingbase.getLastAttackerTime();
            return i != this.timestamp && this.isSuitableTarget(this.theTarget, false) && this.theEntityTameable.shouldAttackEntity(this.theTarget, entitylivingbase);
         }
      }
   }

   public void startExecuting() {
      this.taskOwner.setAttackTarget(this.theTarget);
      EntityLivingBase entitylivingbase = this.theEntityTameable.getOwner();
      if(entitylivingbase != null) {
         this.timestamp = entitylivingbase.getLastAttackerTime();
      }

      super.startExecuting();
   }
}
