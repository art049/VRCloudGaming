package net.minecraft.tileentity;

import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.WeightedSpawnerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TileEntityMobSpawner extends TileEntity implements ITickable
{
    private final MobSpawnerBaseLogic spawnerLogic = new MobSpawnerBaseLogic()
    {
        public void broadcastEvent(int id)
        {
            TileEntityMobSpawner.this.worldObj.addBlockEvent(TileEntityMobSpawner.this.pos, Blocks.MOB_SPAWNER, id, 0);
        }
        public World getSpawnerWorld()
        {
            return TileEntityMobSpawner.this.worldObj;
        }
        public BlockPos getSpawnerPosition()
        {
            return TileEntityMobSpawner.this.pos;
        }
        public void setNextSpawnData(WeightedSpawnerEntity p_184993_1_)
        {
            super.setNextSpawnData(p_184993_1_);

            if (this.getSpawnerWorld() != null)
            {
                IBlockState iblockstate = this.getSpawnerWorld().getBlockState(this.getSpawnerPosition());
                this.getSpawnerWorld().notifyBlockUpdate(TileEntityMobSpawner.this.pos, iblockstate, iblockstate, 4);
            }
        }
    };

    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        this.spawnerLogic.readFromNBT(compound);
    }

    public NBTTagCompound func_189515_b(NBTTagCompound p_189515_1_)
    {
        super.func_189515_b(p_189515_1_);
        this.spawnerLogic.func_189530_b(p_189515_1_);
        return p_189515_1_;
    }

    /**
     * Like the old updateEntity(), except more generic.
     */
    public void update()
    {
        this.spawnerLogic.updateSpawner();
    }

    @Nullable
    public SPacketUpdateTileEntity func_189518_D_()
    {
        return new SPacketUpdateTileEntity(this.pos, 1, this.func_189517_E_());
    }

    public NBTTagCompound func_189517_E_()
    {
        NBTTagCompound nbttagcompound = this.func_189515_b(new NBTTagCompound());
        nbttagcompound.removeTag("SpawnPotentials");
        return nbttagcompound;
    }

    public boolean receiveClientEvent(int id, int type)
    {
        return this.spawnerLogic.setDelayToMin(id) ? true : super.receiveClientEvent(id, type);
    }

    public boolean onlyOpsCanSetNbt()
    {
        return true;
    }

    public MobSpawnerBaseLogic getSpawnerBaseLogic()
    {
        return this.spawnerLogic;
    }
}
