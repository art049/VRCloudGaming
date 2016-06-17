package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockCake extends Block {
   public static final PropertyInteger BITES = PropertyInteger.create("bites", 0, 6);
   protected static final AxisAlignedBB[] CAKE_AABB = new AxisAlignedBB[]{new AxisAlignedBB(0.0625D, 0.0D, 0.0625D, 0.9375D, 0.5D, 0.9375D), new AxisAlignedBB(0.1875D, 0.0D, 0.0625D, 0.9375D, 0.5D, 0.9375D), new AxisAlignedBB(0.3125D, 0.0D, 0.0625D, 0.9375D, 0.5D, 0.9375D), new AxisAlignedBB(0.4375D, 0.0D, 0.0625D, 0.9375D, 0.5D, 0.9375D), new AxisAlignedBB(0.5625D, 0.0D, 0.0625D, 0.9375D, 0.5D, 0.9375D), new AxisAlignedBB(0.6875D, 0.0D, 0.0625D, 0.9375D, 0.5D, 0.9375D), new AxisAlignedBB(0.8125D, 0.0D, 0.0625D, 0.9375D, 0.5D, 0.9375D)};

   protected BlockCake() {
      super(Material.CAKE);
      this.setDefaultState(this.blockState.getBaseState().withProperty(BITES, Integer.valueOf(0)));
      this.setTickRandomly(true);
   }

   public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
      return CAKE_AABB[((Integer)state.getValue(BITES)).intValue()];
   }

   public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World worldIn, BlockPos pos) {
      return state.getCollisionBoundingBox(worldIn, pos);
   }

   public boolean isFullCube(IBlockState state) {
      return false;
   }

   public boolean isOpaqueCube(IBlockState state) {
      return false;
   }

   public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
      this.eatCake(worldIn, pos, state, playerIn);
      return true;
   }

   private void eatCake(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
      if(player.canEat(false)) {
         player.addStat(StatList.CAKE_SLICES_EATEN);
         player.getFoodStats().addStats(2, 0.1F);
         int i = ((Integer)state.getValue(BITES)).intValue();
         if(i < 6) {
            worldIn.setBlockState(pos, state.withProperty(BITES, Integer.valueOf(i + 1)), 3);
         } else {
            worldIn.setBlockToAir(pos);
         }
      }
   }

   public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
      return super.canPlaceBlockAt(worldIn, pos)?this.canBlockStay(worldIn, pos):false;
   }

   public void func_189540_a(IBlockState p_189540_1_, World p_189540_2_, BlockPos p_189540_3_, Block p_189540_4_) {
      if(!this.canBlockStay(p_189540_2_, p_189540_3_)) {
         p_189540_2_.setBlockToAir(p_189540_3_);
      }
   }

   private boolean canBlockStay(World worldIn, BlockPos pos) {
      return worldIn.getBlockState(pos.down()).getMaterial().isSolid();
   }

   public int quantityDropped(Random random) {
      return 0;
   }

   @Nullable
   public Item getItemDropped(IBlockState state, Random rand, int fortune) {
      return null;
   }

   public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
      return new ItemStack(Items.CAKE);
   }

   public BlockRenderLayer getBlockLayer() {
      return BlockRenderLayer.CUTOUT;
   }

   public IBlockState getStateFromMeta(int meta) {
      return this.getDefaultState().withProperty(BITES, Integer.valueOf(meta));
   }

   public int getMetaFromState(IBlockState state) {
      return ((Integer)state.getValue(BITES)).intValue();
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{BITES});
   }

   public int getComparatorInputOverride(IBlockState blockState, World worldIn, BlockPos pos) {
      return (7 - ((Integer)blockState.getValue(BITES)).intValue()) * 2;
   }

   public boolean hasComparatorInputOverride(IBlockState state) {
      return true;
   }
}
