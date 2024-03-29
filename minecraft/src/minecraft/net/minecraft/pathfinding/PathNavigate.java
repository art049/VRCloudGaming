package net.minecraft.pathfinding;

import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;

public abstract class PathNavigate {
   private static int updatePathDelay = 20;
   protected EntityLiving theEntity;
   protected World worldObj;
   @Nullable
   protected Path currentPath;
   protected double speed;
   private final IAttributeInstance pathSearchRange;
   private int totalTicks;
   private int ticksAtLastPos;
   private Vec3d lastPosCheck = Vec3d.ZERO;
   private Vec3d timeoutCachedNode = Vec3d.ZERO;
   private long timeoutTimer = 0L;
   private long lastTimeoutCheck = 0L;
   private double timeoutLimit;
   private float maxDistanceToWaypoint = 0.5F;
   private boolean tryUpdatePath;
   private long lastTimeUpdated;
   protected NodeProcessor nodeProcessor;
   private BlockPos targetPos;
   private final PathFinder pathFinder;

   public PathNavigate(EntityLiving entitylivingIn, World worldIn) {
      this.theEntity = entitylivingIn;
      this.worldObj = worldIn;
      this.pathSearchRange = entitylivingIn.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE);
      this.pathFinder = this.getPathFinder();
   }

   protected abstract PathFinder getPathFinder();

   public void setSpeed(double speedIn) {
      this.speed = speedIn;
   }

   public float getPathSearchRange() {
      return (float)this.pathSearchRange.getAttributeValue();
   }

   public boolean canUpdatePathOnTimeout() {
      return this.tryUpdatePath;
   }

   public void updatePath() {
      if(this.worldObj.getTotalWorldTime() - this.lastTimeUpdated > (long)updatePathDelay) {
         if(this.targetPos != null) {
            this.currentPath = null;
            this.currentPath = this.getPathToPos(this.targetPos);
            this.lastTimeUpdated = this.worldObj.getTotalWorldTime();
            this.tryUpdatePath = false;
         }
      } else {
         this.tryUpdatePath = true;
      }
   }

   @Nullable
   public final Path getPathToXYZ(double x, double y, double z) {
      return this.getPathToPos(new BlockPos(MathHelper.floor_double(x), (int)y, MathHelper.floor_double(z)));
   }

   @Nullable
   public Path getPathToPos(BlockPos pos) {
      if(!this.canNavigate()) {
         return null;
      } else if(this.currentPath != null && !this.currentPath.isFinished() && pos.equals(this.targetPos)) {
         return this.currentPath;
      } else {
         this.targetPos = pos;
         float f = this.getPathSearchRange();
         this.worldObj.theProfiler.startSection("pathfind");
         BlockPos blockpos = new BlockPos(this.theEntity);
         int i = (int)(f + 8.0F);
         ChunkCache chunkcache = new ChunkCache(this.worldObj, blockpos.add(-i, -i, -i), blockpos.add(i, i, i), 0);
         Path path = this.pathFinder.findPath(chunkcache, this.theEntity, this.targetPos, f);
         this.worldObj.theProfiler.endSection();
         return path;
      }
   }

   @Nullable
   public Path getPathToEntityLiving(Entity entityIn) {
      if(!this.canNavigate()) {
         return null;
      } else {
         BlockPos blockpos = new BlockPos(entityIn);
         if(this.currentPath != null && !this.currentPath.isFinished() && blockpos.equals(this.targetPos)) {
            return this.currentPath;
         } else {
            this.targetPos = blockpos;
            float f = this.getPathSearchRange();
            this.worldObj.theProfiler.startSection("pathfind");
            BlockPos blockpos1 = (new BlockPos(this.theEntity)).up();
            int i = (int)(f + 16.0F);
            ChunkCache chunkcache = new ChunkCache(this.worldObj, blockpos1.add(-i, -i, -i), blockpos1.add(i, i, i), 0);
            Path path = this.pathFinder.findPath(chunkcache, this.theEntity, entityIn, f);
            this.worldObj.theProfiler.endSection();
            return path;
         }
      }
   }

   public boolean tryMoveToXYZ(double x, double y, double z, double speedIn) {
      Path path = this.getPathToXYZ((double)MathHelper.floor_double(x), (double)((int)y), (double)MathHelper.floor_double(z));
      return this.setPath(path, speedIn);
   }

   public boolean tryMoveToEntityLiving(Entity entityIn, double speedIn) {
      Path path = this.getPathToEntityLiving(entityIn);
      return path != null?this.setPath(path, speedIn):false;
   }

   public boolean setPath(@Nullable Path pathentityIn, double speedIn) {
      if(pathentityIn == null) {
         this.currentPath = null;
         return false;
      } else {
         if(!pathentityIn.isSamePath(this.currentPath)) {
            this.currentPath = pathentityIn;
         }

         this.removeSunnyPath();
         if(this.currentPath.getCurrentPathLength() == 0) {
            return false;
         } else {
            this.speed = speedIn;
            Vec3d vec3d = this.getEntityPosition();
            this.ticksAtLastPos = this.totalTicks;
            this.lastPosCheck = vec3d;
            return true;
         }
      }
   }

   @Nullable
   public Path getPath() {
      return this.currentPath;
   }

   public void onUpdateNavigation() {
      ++this.totalTicks;
      if(this.tryUpdatePath) {
         this.updatePath();
      }

      if(!this.noPath()) {
         if(this.canNavigate()) {
            this.pathFollow();
         } else if(this.currentPath != null && this.currentPath.getCurrentPathIndex() < this.currentPath.getCurrentPathLength()) {
            Vec3d vec3d = this.getEntityPosition();
            Vec3d vec3d1 = this.currentPath.getVectorFromIndex(this.theEntity, this.currentPath.getCurrentPathIndex());
            if(vec3d.yCoord > vec3d1.yCoord && !this.theEntity.onGround && MathHelper.floor_double(vec3d.xCoord) == MathHelper.floor_double(vec3d1.xCoord) && MathHelper.floor_double(vec3d.zCoord) == MathHelper.floor_double(vec3d1.zCoord)) {
               this.currentPath.setCurrentPathIndex(this.currentPath.getCurrentPathIndex() + 1);
            }
         }

         if(!this.noPath()) {
            Vec3d vec3d2 = this.currentPath.getPosition(this.theEntity);
            if(vec3d2 != null) {
               BlockPos blockpos = (new BlockPos(vec3d2)).down();
               AxisAlignedBB axisalignedbb = this.worldObj.getBlockState(blockpos).getBoundingBox(this.worldObj, blockpos);
               vec3d2 = vec3d2.subtract(0.0D, 1.0D - axisalignedbb.maxY, 0.0D);
               this.theEntity.getMoveHelper().setMoveTo(vec3d2.xCoord, vec3d2.yCoord, vec3d2.zCoord, this.speed);
            }
         }
      }
   }

   protected void pathFollow() {
      Vec3d vec3d = this.getEntityPosition();
      int i = this.currentPath.getCurrentPathLength();

      for(int j = this.currentPath.getCurrentPathIndex(); j < this.currentPath.getCurrentPathLength(); ++j) {
         if((double)this.currentPath.getPathPointFromIndex(j).yCoord != Math.floor(vec3d.yCoord)) {
            i = j;
            break;
         }
      }

      this.maxDistanceToWaypoint = this.theEntity.width > 0.75F?this.theEntity.width / 2.0F:0.75F - this.theEntity.width / 2.0F;
      Vec3d vec3d1 = this.currentPath.getCurrentPos();
      if(MathHelper.abs((float)(this.theEntity.posX - (vec3d1.xCoord + 0.5D))) < this.maxDistanceToWaypoint && MathHelper.abs((float)(this.theEntity.posZ - (vec3d1.zCoord + 0.5D))) < this.maxDistanceToWaypoint) {
         this.currentPath.setCurrentPathIndex(this.currentPath.getCurrentPathIndex() + 1);
      }

      int k = MathHelper.ceiling_float_int(this.theEntity.width);
      int l = MathHelper.ceiling_float_int(this.theEntity.height);
      int i1 = k;

      for(int j1 = i - 1; j1 >= this.currentPath.getCurrentPathIndex(); --j1) {
         if(this.isDirectPathBetweenPoints(vec3d, this.currentPath.getVectorFromIndex(this.theEntity, j1), k, l, i1)) {
            this.currentPath.setCurrentPathIndex(j1);
            break;
         }
      }

      this.checkForStuck(vec3d);
   }

   protected void checkForStuck(Vec3d positionVec3) {
      if(this.totalTicks - this.ticksAtLastPos > 100) {
         if(positionVec3.squareDistanceTo(this.lastPosCheck) < 2.25D) {
            this.clearPathEntity();
         }

         this.ticksAtLastPos = this.totalTicks;
         this.lastPosCheck = positionVec3;
      }

      if(this.currentPath != null && !this.currentPath.isFinished()) {
         Vec3d vec3d = this.currentPath.getCurrentPos();
         if(!vec3d.equals(this.timeoutCachedNode)) {
            this.timeoutCachedNode = vec3d;
            double d0 = positionVec3.distanceTo(this.timeoutCachedNode);
            this.timeoutLimit = this.theEntity.getAIMoveSpeed() > 0.0F?d0 / (double)this.theEntity.getAIMoveSpeed() * 1000.0D:0.0D;
         } else {
            this.timeoutTimer += System.currentTimeMillis() - this.lastTimeoutCheck;
         }

         if(this.timeoutLimit > 0.0D && (double)this.timeoutTimer > this.timeoutLimit * 3.0D) {
            this.timeoutCachedNode = Vec3d.ZERO;
            this.timeoutTimer = 0L;
            this.timeoutLimit = 0.0D;
            this.clearPathEntity();
         }

         this.lastTimeoutCheck = System.currentTimeMillis();
      }
   }

   public boolean noPath() {
      return this.currentPath == null || this.currentPath.isFinished();
   }

   public void clearPathEntity() {
      this.currentPath = null;
   }

   protected abstract Vec3d getEntityPosition();

   protected abstract boolean canNavigate();

   protected boolean isInLiquid() {
      return this.theEntity.isInWater() || this.theEntity.isInLava();
   }

   protected void removeSunnyPath() {
   }

   protected abstract boolean isDirectPathBetweenPoints(Vec3d posVec31, Vec3d posVec32, int sizeX, int sizeY, int sizeZ);

   public boolean canEntityStandOnPos(BlockPos pos) {
      return this.worldObj.getBlockState(pos.down()).isFullBlock();
   }

   public NodeProcessor func_189566_q() {
      return this.nodeProcessor;
   }
}
