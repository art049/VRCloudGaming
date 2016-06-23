package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockReed extends Block {
   public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 15);
   protected static final AxisAlignedBB REED_AABB = new AxisAlignedBB(0.125D, 0.0D, 0.125D, 0.875D, 1.0D, 0.875D);

   protected BlockReed() {
      super(Material.PLANTS);
      this.setDefaultState(this.blockState.getBaseState().withProperty(AGE, Integer.valueOf(0)));
      this.setTickRandomly(true);
   }

   public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
      return REED_AABB;
   }

   public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
      if(worldIn.getBlockState(pos.down()).getBlock() == Blocks.REEDS || this.checkForDrop(worldIn, pos, state)) {
         if(worldIn.isAirBlock(pos.up())) {
            int i;
            for(i = 1; worldIn.getBlockState(pos.down(i)).getBlock() == this; ++i) {
               ;
            }

            if(i < 3) {
               int j = ((Integer)state.getValue(AGE)).intValue();
               if(j == 15) {
                  worldIn.setBlockState(pos.up(), this.getDefaultState());
                  worldIn.setBlockState(pos, state.withProperty(AGE, Integer.valueOf(0)), 4);
               } else {
                  worldIn.setBlockState(pos, state.withProperty(AGE, Integer.valueOf(j + 1)), 4);
               }
            }
         }
      }
   }

   public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
      Block block = worldIn.getBlockState(pos.down()).getBlock();
      if(block == this) {
         return true;
      } else if(block != Blocks.GRASS && block != Blocks.DIRT && block != Blocks.SAND) {
         return false;
      } else {
         BlockPos blockpos = pos.down();

         for(EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
            IBlockState iblockstate = worldIn.getBlockState(blockpos.offset(enumfacing));
            if(iblockstate.getMaterial() == Material.WATER || iblockstate.getBlock() == Blocks.FROSTED_ICE) {
               return true;
            }
         }

         return false;
      }
   }

   public void func_189540_a(IBlockState p_189540_1_, World p_189540_2_, BlockPos p_189540_3_, Block p_189540_4_) {
      this.checkForDrop(p_189540_2_, p_189540_3_, p_189540_1_);
   }

   protected final boolean checkForDrop(World worldIn, BlockPos pos, IBlockState state) {
      if(this.canBlockStay(worldIn, pos)) {
         return true;
      } else {
         this.dropBlockAsItem(worldIn, pos, state, 0);
         worldIn.setBlockToAir(pos);
         return false;
      }
   }

   public boolean canBlockStay(World worldIn, BlockPos pos) {
      return this.canPlaceBlockAt(worldIn, pos);
   }

   @Nullable
   public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, World worldIn, BlockPos pos) {
      return NULL_AABB;
   }

   @Nullable
   public Item getItemDropped(IBlockState state, Random rand, int fortune) {
      return Items.REEDS;
   }

   public boolean isOpaqueCube(IBlockState state) {
      return false;
   }

   public boolean isFullCube(IBlockState state) {
      return false;
   }

   public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
      return new ItemStack(Items.REEDS);
   }

   public BlockRenderLayer getBlockLayer() {
      return BlockRenderLayer.CUTOUT;
   }

   public IBlockState getStateFromMeta(int meta) {
      return this.getDefaultState().withProperty(AGE, Integer.valueOf(meta));
   }

   public int getMetaFromState(IBlockState state) {
      return ((Integer)state.getValue(AGE)).intValue();
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{AGE});
   }
}