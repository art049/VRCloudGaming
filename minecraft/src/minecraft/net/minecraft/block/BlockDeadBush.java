package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockDeadBush extends BlockBush {
   protected static final AxisAlignedBB DEAD_BUSH_AABB = new AxisAlignedBB(0.09999999403953552D, 0.0D, 0.09999999403953552D, 0.8999999761581421D, 0.800000011920929D, 0.8999999761581421D);

   protected BlockDeadBush() {
      super(Material.VINE);
   }

   public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
      return DEAD_BUSH_AABB;
   }

   public MapColor getMapColor(IBlockState state) {
      return MapColor.WOOD;
   }

   protected boolean canSustainBush(IBlockState state) {
      return state.getBlock() == Blocks.SAND || state.getBlock() == Blocks.HARDENED_CLAY || state.getBlock() == Blocks.STAINED_HARDENED_CLAY || state.getBlock() == Blocks.DIRT;
   }

   public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) {
      return true;
   }

   public int quantityDropped(Random random) {
      return random.nextInt(3);
   }

   @Nullable
   public Item getItemDropped(IBlockState state, Random rand, int fortune) {
      return Items.STICK;
   }

   public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, @Nullable ItemStack stack) {
      if(!worldIn.isRemote && stack != null && stack.getItem() == Items.SHEARS) {
         player.addStat(StatList.getBlockStats(this));
         spawnAsEntity(worldIn, pos, new ItemStack(Blocks.DEADBUSH, 1, 0));
      } else {
         super.harvestBlock(worldIn, player, pos, state, te, stack);
      }
   }
}
