package net.minecraft.client.multiplayer;

import com.google.common.base.Objects;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import javax.annotation.Nullable;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.chunk.IChunkProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChunkProviderClient implements IChunkProvider {
   private static final Logger LOGGER = LogManager.getLogger();
   private final Chunk blankChunk;
   private final Long2ObjectMap<Chunk> chunkMapping = new Long2ObjectOpenHashMap<Chunk>(8192) {
      protected void rehash(int p_rehash_1_) {
         if(p_rehash_1_ > this.key.length) {
            super.rehash(p_rehash_1_);
         }
      }
   };
   private final World worldObj;

   public ChunkProviderClient(World worldIn) {
      this.blankChunk = new EmptyChunk(worldIn, 0, 0);
      this.worldObj = worldIn;
   }

   public void unloadChunk(int x, int z) {
      Chunk chunk = this.provideChunk(x, z);
      if(!chunk.isEmpty()) {
         chunk.onChunkUnload();
      }

      this.chunkMapping.remove(ChunkPos.chunkXZ2Int(x, z));
   }

   @Nullable
   public Chunk getLoadedChunk(int x, int z) {
      return (Chunk)this.chunkMapping.get(ChunkPos.chunkXZ2Int(x, z));
   }

   public Chunk loadChunk(int chunkX, int chunkZ) {
      Chunk chunk = new Chunk(this.worldObj, chunkX, chunkZ);
      this.chunkMapping.put(ChunkPos.chunkXZ2Int(chunkX, chunkZ), chunk);
      chunk.setChunkLoaded(true);
      return chunk;
   }

   public Chunk provideChunk(int x, int z) {
      return (Chunk)Objects.firstNonNull(this.getLoadedChunk(x, z), this.blankChunk);
   }

   public boolean unloadQueuedChunks() {
      long i = System.currentTimeMillis();

      for(Chunk chunk : this.chunkMapping.values()) {
         chunk.onTick(System.currentTimeMillis() - i > 5L);
      }

      if(System.currentTimeMillis() - i > 100L) {
         LOGGER.info("Warning: Clientside chunk ticking took {} ms", new Object[]{Long.valueOf(System.currentTimeMillis() - i)});
      }

      return false;
   }

   public String makeString() {
      return "MultiplayerChunkCache: " + this.chunkMapping.size() + ", " + this.chunkMapping.size();
   }
}
