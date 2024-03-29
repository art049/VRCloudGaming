package net.minecraft.world.chunk;

import javax.annotation.Nullable;

public interface IChunkProvider {
   @Nullable
   Chunk getLoadedChunk(int x, int z);

   Chunk provideChunk(int x, int z);

   boolean unloadQueuedChunks();

   String makeString();
}
