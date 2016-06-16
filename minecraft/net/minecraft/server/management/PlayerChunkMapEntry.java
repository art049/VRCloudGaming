package net.minecraft.server.management;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.network.play.server.SPacketMultiBlockChange;
import net.minecraft.network.play.server.SPacketUnloadChunk;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PlayerChunkMapEntry
{
    private static final Logger LOGGER = LogManager.getLogger();
    private final PlayerChunkMap field_187282_b;
    private final List<EntityPlayerMP> players = Lists.<EntityPlayerMP>newArrayList();
    private final ChunkPos pos;
    private final short[] changedBlocks = new short[64];
    @Nullable
    private Chunk chunk;
    private int changes;
    private int changedSectionFilter;
    private long lastUpdateInhabitedTime;
    private boolean sentToPlayers;

    public PlayerChunkMapEntry(PlayerChunkMap p_i1518_1_, int chunkX, int chunkZ)
    {
        this.field_187282_b = p_i1518_1_;
        this.pos = new ChunkPos(chunkX, chunkZ);
        this.chunk = p_i1518_1_.getWorldServer().getChunkProvider().loadChunk(chunkX, chunkZ);
    }

    public ChunkPos getPos()
    {
        return this.pos;
    }

    public void addPlayer(EntityPlayerMP player)
    {
        if (this.players.contains(player))
        {
            LOGGER.debug("Failed to add player. {} already is in chunk {}, {}", new Object[] {player, Integer.valueOf(this.pos.chunkXPos), Integer.valueOf(this.pos.chunkZPos)});
        }
        else
        {
            if (this.players.isEmpty())
            {
                this.lastUpdateInhabitedTime = this.field_187282_b.getWorldServer().getTotalWorldTime();
            }

            this.players.add(player);

            if (this.sentToPlayers)
            {
                this.sendNearbySpecialEntities(player);
            }
        }
    }

    public void removePlayer(EntityPlayerMP player)
    {
        if (this.players.contains(player))
        {
            if (this.sentToPlayers)
            {
                player.connection.sendPacket(new SPacketUnloadChunk(this.pos.chunkXPos, this.pos.chunkZPos));
            }

            this.players.remove(player);

            if (this.players.isEmpty())
            {
                this.field_187282_b.removeEntry(this);
            }
        }
    }

    /**
     * Provide the chunk at the player's location. Can fail, returning false, if the player is a spectator floating
     * outside of any pre-existing chunks, and the server is not configured to allow chunk generation for spectators.
     */
    public boolean providePlayerChunk(boolean canGenerate)
    {
        if (this.chunk != null)
        {
            return true;
        }
        else
        {
            if (canGenerate)
            {
                this.chunk = this.field_187282_b.getWorldServer().getChunkProvider().provideChunk(this.pos.chunkXPos, this.pos.chunkZPos);
            }
            else
            {
                this.chunk = this.field_187282_b.getWorldServer().getChunkProvider().loadChunk(this.pos.chunkXPos, this.pos.chunkZPos);
            }

            return this.chunk != null;
        }
    }

    public boolean sentToPlayers()
    {
        if (this.sentToPlayers)
        {
            return true;
        }
        else if (this.chunk == null)
        {
            return false;
        }
        else if (!this.chunk.isPopulated())
        {
            return false;
        }
        else
        {
            this.changes = 0;
            this.changedSectionFilter = 0;
            this.sentToPlayers = true;
            SPacketChunkData spacketchunkdata = new SPacketChunkData(this.chunk, 65535);

            for (EntityPlayerMP entityplayermp : this.players)
            {
                entityplayermp.connection.sendPacket(spacketchunkdata);
                this.field_187282_b.getWorldServer().getEntityTracker().sendLeashedEntitiesInChunk(entityplayermp, this.chunk);
            }

            return true;
        }
    }

    /**
     * Send packets to player for:
     *  - nearby tile entities
     *  - nearby entities that are leashed
     *  - nearby entities with
     */
    public void sendNearbySpecialEntities(EntityPlayerMP player)
    {
        if (this.sentToPlayers)
        {
            player.connection.sendPacket(new SPacketChunkData(this.chunk, 65535));
            this.field_187282_b.getWorldServer().getEntityTracker().sendLeashedEntitiesInChunk(player, this.chunk);
        }
    }

    public void updateChunkInhabitedTime()
    {
        if (this.chunk != null)
        {
            this.chunk.setInhabitedTime(this.chunk.getInhabitedTime() + this.field_187282_b.getWorldServer().getTotalWorldTime() - this.lastUpdateInhabitedTime);
        }

        this.lastUpdateInhabitedTime = this.field_187282_b.getWorldServer().getTotalWorldTime();
    }

    public void blockChanged(int x, int y, int z)
    {
        if (this.sentToPlayers)
        {
            if (this.changes == 0)
            {
                this.field_187282_b.addEntry(this);
            }

            this.changedSectionFilter |= 1 << (y >> 4);

            if (this.changes < 64)
            {
                short short1 = (short)(x << 12 | z << 8 | y);

                for (int i = 0; i < this.changes; ++i)
                {
                    if (this.changedBlocks[i] == short1)
                    {
                        return;
                    }
                }

                this.changedBlocks[this.changes++] = short1;
            }
        }
    }

    public void sendPacket(Packet<?> packetIn)
    {
        if (this.sentToPlayers)
        {
            for (int i = 0; i < this.players.size(); ++i)
            {
                ((EntityPlayerMP)this.players.get(i)).connection.sendPacket(packetIn);
            }
        }
    }

    public void update()
    {
        if (this.sentToPlayers && this.chunk != null)
        {
            if (this.changes != 0)
            {
                if (this.changes == 1)
                {
                    int i = (this.changedBlocks[0] >> 12 & 15) + this.pos.chunkXPos * 16;
                    int j = this.changedBlocks[0] & 255;
                    int k = (this.changedBlocks[0] >> 8 & 15) + this.pos.chunkZPos * 16;
                    BlockPos blockpos = new BlockPos(i, j, k);
                    this.sendPacket(new SPacketBlockChange(this.field_187282_b.getWorldServer(), blockpos));

                    if (this.field_187282_b.getWorldServer().getBlockState(blockpos).getBlock().hasTileEntity())
                    {
                        this.sendBlockEntity(this.field_187282_b.getWorldServer().getTileEntity(blockpos));
                    }
                }
                else if (this.changes == 64)
                {
                    this.sendPacket(new SPacketChunkData(this.chunk, this.changedSectionFilter));
                }
                else
                {
                    this.sendPacket(new SPacketMultiBlockChange(this.changes, this.changedBlocks, this.chunk));

                    for (int l = 0; l < this.changes; ++l)
                    {
                        int i1 = (this.changedBlocks[l] >> 12 & 15) + this.pos.chunkXPos * 16;
                        int j1 = this.changedBlocks[l] & 255;
                        int k1 = (this.changedBlocks[l] >> 8 & 15) + this.pos.chunkZPos * 16;
                        BlockPos blockpos1 = new BlockPos(i1, j1, k1);

                        if (this.field_187282_b.getWorldServer().getBlockState(blockpos1).getBlock().hasTileEntity())
                        {
                            this.sendBlockEntity(this.field_187282_b.getWorldServer().getTileEntity(blockpos1));
                        }
                    }
                }

                this.changes = 0;
                this.changedSectionFilter = 0;
            }
        }
    }

    private void sendBlockEntity(@Nullable TileEntity be)
    {
        if (be != null)
        {
            SPacketUpdateTileEntity spacketupdatetileentity = be.func_189518_D_();

            if (spacketupdatetileentity != null)
            {
                this.sendPacket(spacketupdatetileentity);
            }
        }
    }

    public boolean containsPlayer(EntityPlayerMP player)
    {
        return this.players.contains(player);
    }

    public boolean hasPlayerMatching(Predicate<EntityPlayerMP> predicate)
    {
        return Iterables.tryFind(this.players, predicate).isPresent();
    }

    public boolean hasPlayerMatchingInRange(double range, Predicate<EntityPlayerMP> predicate)
    {
        int i = 0;

        for (int j = this.players.size(); i < j; ++i)
        {
            EntityPlayerMP entityplayermp = (EntityPlayerMP)this.players.get(i);

            if (predicate.apply(entityplayermp) && this.pos.getDistanceSq(entityplayermp) < range * range)
            {
                return true;
            }
        }

        return false;
    }

    public boolean isSentToPlayers()
    {
        return this.sentToPlayers;
    }

    @Nullable
    public Chunk getChunk()
    {
        return this.chunk;
    }

    public double getClosestPlayerDistance()
    {
        double d0 = Double.MAX_VALUE;

        for (EntityPlayerMP entityplayermp : this.players)
        {
            double d1 = this.pos.getDistanceSq(entityplayermp);

            if (d1 < d0)
            {
                d0 = d1;
            }
        }

        return d0;
    }
}
