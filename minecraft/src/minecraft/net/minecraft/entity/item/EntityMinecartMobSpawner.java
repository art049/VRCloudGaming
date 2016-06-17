package net.minecraft.entity.item;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EntityMinecartMobSpawner extends EntityMinecart {
   private final MobSpawnerBaseLogic mobSpawnerLogic = new MobSpawnerBaseLogic() {
      public void broadcastEvent(int id) {
         EntityMinecartMobSpawner.this.worldObj.setEntityState(EntityMinecartMobSpawner.this, (byte)id);
      }

      public World getSpawnerWorld() {
         return EntityMinecartMobSpawner.this.worldObj;
      }

      public BlockPos getSpawnerPosition() {
         return new BlockPos(EntityMinecartMobSpawner.this);
      }
   };

   public EntityMinecartMobSpawner(World worldIn) {
      super(worldIn);
   }

   public EntityMinecartMobSpawner(World worldIn, double x, double y, double z) {
      super(worldIn, x, y, z);
   }

   public EntityMinecart.Type getType() {
      return EntityMinecart.Type.SPAWNER;
   }

   public IBlockState getDefaultDisplayTile() {
      return Blocks.MOB_SPAWNER.getDefaultState();
   }

   protected void readEntityFromNBT(NBTTagCompound compound) {
      super.readEntityFromNBT(compound);
      this.mobSpawnerLogic.readFromNBT(compound);
   }

   protected void writeEntityToNBT(NBTTagCompound compound) {
      super.writeEntityToNBT(compound);
      this.mobSpawnerLogic.func_189530_b(compound);
   }

   public void handleStatusUpdate(byte id) {
      this.mobSpawnerLogic.setDelayToMin(id);
   }

   public void onUpdate() {
      super.onUpdate();
      this.mobSpawnerLogic.updateSpawner();
   }
}
