package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityStructure;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockStructure extends BlockContainer {
   public static final PropertyEnum<TileEntityStructure.Mode> MODE = PropertyEnum.<TileEntityStructure.Mode>create("mode", TileEntityStructure.Mode.class);

   public BlockStructure() {
      super(Material.IRON, MapColor.SILVER);
      this.setDefaultState(this.blockState.getBaseState());
   }

   public TileEntity createNewTileEntity(World worldIn, int meta) {
      return new TileEntityStructure();
   }

   public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
      return false;
   }

   public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
   }

   @Nullable
   public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
      return null;
   }

   public int quantityDropped(Random random) {
      return 0;
   }

   public EnumBlockRenderType getRenderType(IBlockState state) {
      return EnumBlockRenderType.MODEL;
   }

   public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
      return this.getDefaultState().withProperty(MODE, TileEntityStructure.Mode.DATA);
   }

   public IBlockState getStateFromMeta(int meta) {
      return this.getDefaultState().withProperty(MODE, TileEntityStructure.Mode.getById(meta));
   }

   public int getMetaFromState(IBlockState state) {
      return ((TileEntityStructure.Mode)state.getValue(MODE)).getModeId();
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{MODE});
   }
}
