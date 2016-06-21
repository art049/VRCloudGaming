package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;

public class BlockEmptyDrops extends Block {
   public BlockEmptyDrops(Material materialIn) {
      super(materialIn);
   }

   public int quantityDropped(Random random) {
      return 0;
   }

   @Nullable
   public Item getItemDropped(IBlockState state, Random rand, int fortune) {
      return null;
   }
}
