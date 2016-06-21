package net.minecraft.world;

import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

public interface IBlockAccess {
   @Nullable
   TileEntity getTileEntity(BlockPos pos);

   int getCombinedLight(BlockPos pos, int lightValue);

   IBlockState getBlockState(BlockPos pos);

   boolean isAirBlock(BlockPos pos);

   Biome getBiomeGenForCoords(BlockPos pos);

   boolean extendedLevelsInChunkCache();

   int getStrongPower(BlockPos pos, EnumFacing direction);

   WorldType getWorldType();
}
