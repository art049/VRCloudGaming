package net.minecraft.pathfinding;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;

public class PathWorldListener implements IWorldEventListener {
   private final List<PathNavigate> field_189519_a = Lists.<PathNavigate>newArrayList();

   public void notifyBlockUpdate(World worldIn, BlockPos pos, IBlockState oldState, IBlockState newState, int flags) {
      if(this.didBlockChange(worldIn, pos, oldState, newState)) {
         int i = 0;

         for(int j = this.field_189519_a.size(); i < j; ++i) {
            PathNavigate pathnavigate = (PathNavigate)this.field_189519_a.get(i);
            if(pathnavigate != null && !pathnavigate.canUpdatePathOnTimeout()) {
               Path path = pathnavigate.getPath();
               if(path != null && !path.isFinished() && path.getCurrentPathLength() != 0) {
                  PathPoint pathpoint = pathnavigate.currentPath.getFinalPathPoint();
                  double d0 = pos.distanceSq(((double)pathpoint.xCoord + pathnavigate.theEntity.posX) / 2.0D, ((double)pathpoint.yCoord + pathnavigate.theEntity.posY) / 2.0D, ((double)pathpoint.zCoord + pathnavigate.theEntity.posZ) / 2.0D);
                  int k = (path.getCurrentPathLength() - path.getCurrentPathIndex()) * (path.getCurrentPathLength() - path.getCurrentPathIndex());
                  if(d0 < (double)k) {
                     pathnavigate.updatePath();
                  }
               }
            }
         }
      }
   }

   protected boolean didBlockChange(World worldIn, BlockPos pos, IBlockState oldState, IBlockState newState) {
      AxisAlignedBB axisalignedbb = oldState.getCollisionBoundingBox(worldIn, pos);
      AxisAlignedBB axisalignedbb1 = newState.getCollisionBoundingBox(worldIn, pos);
      return axisalignedbb != axisalignedbb1 && (axisalignedbb == null || !axisalignedbb.equals(axisalignedbb1));
   }

   public void notifyLightSet(BlockPos pos) {
   }

   public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {
   }

   public void playSoundToAllNearExcept(@Nullable EntityPlayer player, SoundEvent soundIn, SoundCategory category, double x, double y, double z, float volume, float pitch) {
   }

   public void spawnParticle(int particleID, boolean ignoreRange, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... parameters) {
   }

   public void onEntityAdded(Entity entityIn) {
      if(entityIn instanceof EntityLiving) {
         this.field_189519_a.add(((EntityLiving)entityIn).getNavigator());
      }
   }

   public void onEntityRemoved(Entity entityIn) {
      if(entityIn instanceof EntityLiving) {
         this.field_189519_a.remove(((EntityLiving)entityIn).getNavigator());
      }
   }

   public void playRecord(SoundEvent soundIn, BlockPos pos) {
   }

   public void broadcastSound(int soundID, BlockPos pos, int data) {
   }

   public void playEvent(EntityPlayer player, int type, BlockPos blockPosIn, int data) {
   }

   public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {
   }
}
