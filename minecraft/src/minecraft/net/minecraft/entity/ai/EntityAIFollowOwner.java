package net.minecraft.entity.ai;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class EntityAIFollowOwner extends EntityAIBase {
   private EntityTameable thePet;
   private EntityLivingBase theOwner;
   World theWorld;
   private double followSpeed;
   private PathNavigate petPathfinder;
   private int timeToRecalcPath;
   float maxDist;
   float minDist;
   private float oldWaterCost;

   public EntityAIFollowOwner(EntityTameable thePetIn, double followSpeedIn, float minDistIn, float maxDistIn) {
      this.thePet = thePetIn;
      this.theWorld = thePetIn.worldObj;
      this.followSpeed = followSpeedIn;
      this.petPathfinder = thePetIn.getNavigator();
      this.minDist = minDistIn;
      this.maxDist = maxDistIn;
      this.setMutexBits(3);
      if(!(thePetIn.getNavigator() instanceof PathNavigateGround)) {
         throw new IllegalArgumentException("Unsupported mob type for FollowOwnerGoal");
      }
   }

   public boolean shouldExecute() {
      EntityLivingBase entitylivingbase = this.thePet.getOwner();
      if(entitylivingbase == null) {
         return false;
      } else if(entitylivingbase instanceof EntityPlayer && ((EntityPlayer)entitylivingbase).isSpectator()) {
         return false;
      } else if(this.thePet.isSitting()) {
         return false;
      } else if(this.thePet.getDistanceSqToEntity(entitylivingbase) < (double)(this.minDist * this.minDist)) {
         return false;
      } else {
         this.theOwner = entitylivingbase;
         return true;
      }
   }

   public boolean continueExecuting() {
      return !this.petPathfinder.noPath() && this.thePet.getDistanceSqToEntity(this.theOwner) > (double)(this.maxDist * this.maxDist) && !this.thePet.isSitting();
   }

   public void startExecuting() {
      this.timeToRecalcPath = 0;
      this.oldWaterCost = this.thePet.getPathPriority(PathNodeType.WATER);
      this.thePet.setPathPriority(PathNodeType.WATER, 0.0F);
   }

   public void resetTask() {
      this.theOwner = null;
      this.petPathfinder.clearPathEntity();
      this.thePet.setPathPriority(PathNodeType.WATER, this.oldWaterCost);
   }

   private boolean isEmptyBlock(BlockPos pos) {
      IBlockState iblockstate = this.theWorld.getBlockState(pos);
      Block block = iblockstate.getBlock();
      return block == Blocks.AIR?true:!iblockstate.isFullCube();
   }

   public void updateTask() {
      this.thePet.getLookHelper().setLookPositionWithEntity(this.theOwner, 10.0F, (float)this.thePet.getVerticalFaceSpeed());
      if(!this.thePet.isSitting()) {
         if(--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = 10;
            if(!this.petPathfinder.tryMoveToEntityLiving(this.theOwner, this.followSpeed)) {
               if(!this.thePet.getLeashed()) {
                  if(this.thePet.getDistanceSqToEntity(this.theOwner) >= 144.0D) {
                     int i = MathHelper.floor_double(this.theOwner.posX) - 2;
                     int j = MathHelper.floor_double(this.theOwner.posZ) - 2;
                     int k = MathHelper.floor_double(this.theOwner.getEntityBoundingBox().minY);

                     for(int l = 0; l <= 4; ++l) {
                        for(int i1 = 0; i1 <= 4; ++i1) {
                           if((l < 1 || i1 < 1 || l > 3 || i1 > 3) && this.theWorld.getBlockState(new BlockPos(i + l, k - 1, j + i1)).isFullyOpaque() && this.isEmptyBlock(new BlockPos(i + l, k, j + i1)) && this.isEmptyBlock(new BlockPos(i + l, k + 1, j + i1))) {
                              this.thePet.setLocationAndAngles((double)((float)(i + l) + 0.5F), (double)k, (double)((float)(j + i1) + 0.5F), this.thePet.rotationYaw, this.thePet.rotationPitch);
                              this.petPathfinder.clearPathEntity();
                              return;
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }
}
