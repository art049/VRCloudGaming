package net.minecraft.entity.ai;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;

public abstract class EntityAIDoorInteract extends EntityAIBase {
   protected EntityLiving theEntity;
   protected BlockPos doorPosition = BlockPos.ORIGIN;
   protected BlockDoor doorBlock;
   boolean hasStoppedDoorInteraction;
   float entityPositionX;
   float entityPositionZ;

   public EntityAIDoorInteract(EntityLiving entityIn) {
      this.theEntity = entityIn;
      if(!(entityIn.getNavigator() instanceof PathNavigateGround)) {
         throw new IllegalArgumentException("Unsupported mob type for DoorInteractGoal");
      }
   }

   public boolean shouldExecute() {
      if(!this.theEntity.isCollidedHorizontally) {
         return false;
      } else {
         PathNavigateGround pathnavigateground = (PathNavigateGround)this.theEntity.getNavigator();
         Path path = pathnavigateground.getPath();
         if(path != null && !path.isFinished() && pathnavigateground.getEnterDoors()) {
            for(int i = 0; i < Math.min(path.getCurrentPathIndex() + 2, path.getCurrentPathLength()); ++i) {
               PathPoint pathpoint = path.getPathPointFromIndex(i);
               this.doorPosition = new BlockPos(pathpoint.xCoord, pathpoint.yCoord + 1, pathpoint.zCoord);
               if(this.theEntity.getDistanceSq((double)this.doorPosition.getX(), this.theEntity.posY, (double)this.doorPosition.getZ()) <= 2.25D) {
                  this.doorBlock = this.getBlockDoor(this.doorPosition);
                  if(this.doorBlock != null) {
                     return true;
                  }
               }
            }

            this.doorPosition = (new BlockPos(this.theEntity)).up();
            this.doorBlock = this.getBlockDoor(this.doorPosition);
            return this.doorBlock != null;
         } else {
            return false;
         }
      }
   }

   public boolean continueExecuting() {
      return !this.hasStoppedDoorInteraction;
   }

   public void startExecuting() {
      this.hasStoppedDoorInteraction = false;
      this.entityPositionX = (float)((double)((float)this.doorPosition.getX() + 0.5F) - this.theEntity.posX);
      this.entityPositionZ = (float)((double)((float)this.doorPosition.getZ() + 0.5F) - this.theEntity.posZ);
   }

   public void updateTask() {
      float f = (float)((double)((float)this.doorPosition.getX() + 0.5F) - this.theEntity.posX);
      float f1 = (float)((double)((float)this.doorPosition.getZ() + 0.5F) - this.theEntity.posZ);
      float f2 = this.entityPositionX * f + this.entityPositionZ * f1;
      if(f2 < 0.0F) {
         this.hasStoppedDoorInteraction = true;
      }
   }

   private BlockDoor getBlockDoor(BlockPos pos) {
      IBlockState iblockstate = this.theEntity.worldObj.getBlockState(pos);
      Block block = iblockstate.getBlock();
      return block instanceof BlockDoor && iblockstate.getMaterial() == Material.WOOD?(BlockDoor)block:null;
   }
}
