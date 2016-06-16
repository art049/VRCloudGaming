package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WorldGenDeadBush extends WorldGenerator
{
    public boolean generate(World worldIn, Random rand, BlockPos position)
    {
        IBlockState iblockstate;

        while (((iblockstate = worldIn.getBlockState(position)).getMaterial() == Material.AIR || iblockstate.getMaterial() == Material.LEAVES) && position.getY() > 0)
        {
            position = position.down();
        }

        for (int i = 0; i < 4; ++i)
        {
            BlockPos blockpos = position.add(rand.nextInt(8) - rand.nextInt(8), rand.nextInt(4) - rand.nextInt(4), rand.nextInt(8) - rand.nextInt(8));

            if (worldIn.isAirBlock(blockpos) && Blocks.DEADBUSH.canBlockStay(worldIn, blockpos, Blocks.DEADBUSH.getDefaultState()))
            {
                worldIn.setBlockState(blockpos, Blocks.DEADBUSH.getDefaultState(), 2);
            }
        }

        return true;
    }
}
