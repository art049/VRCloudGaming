package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityPiston;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockPistonMoving extends BlockContainer {
   public static final PropertyDirection FACING = BlockPistonExtension.FACING;
   public static final PropertyEnum<BlockPistonExtension.EnumPistonType> TYPE = BlockPistonExtension.TYPE;

   public BlockPistonMoving() {
      super(Material.PISTON);
      this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(TYPE, BlockPistonExtension.EnumPistonType.DEFAULT));
      this.setHardness(-1.0F);
   }

   public TileEntity createNewTileEntity(World worldIn, int meta) {
      return null;
   }

   public static TileEntity createTilePiston(IBlockState blockStateIn, EnumFacing facingIn, boolean extendingIn, boolean shouldHeadBeRenderedIn) {
      return new TileEntityPiston(blockStateIn, facingIn, extendingIn, shouldHeadBeRenderedIn);
   }

   public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
      TileEntity tileentity = worldIn.getTileEntity(pos);
      if(tileentity instanceof TileEntityPiston) {
         ((TileEntityPiston)tileentity).clearPistonTileEntity();
      } else {
         super.breakBlock(worldIn, pos, state);
      }
   }

   public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
      return false;
   }

   public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side) {
      return false;
   }

   public void onBlockDestroyedByPlayer(World worldIn, BlockPos pos, IBlockState state) {
      BlockPos blockpos = pos.offset(((EnumFacing)state.getValue(FACING)).getOpposite());
      IBlockState iblockstate = worldIn.getBlockState(blockpos);
      if(iblockstate.getBlock() instanceof BlockPistonBase && ((Boolean)iblockstate.getValue(BlockPistonBase.EXTENDED)).booleanValue()) {
         worldIn.setBlockToAir(blockpos);
      }
   }

   public boolean isOpaqueCube(IBlockState state) {
      return false;
   }

   public boolean isFullCube(IBlockState state) {
      return false;
   }

   public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
      if(!worldIn.isRemote && worldIn.getTileEntity(pos) == null) {
         worldIn.setBlockToAir(pos);
         return true;
      } else {
         return false;
      }
   }

   @Nullable
   public Item getItemDropped(IBlockState state, Random rand, int fortune) {
      return null;
   }

   public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune) {
      if(!worldIn.isRemote) {
         TileEntityPiston tileentitypiston = this.getTilePistonAt(worldIn, pos);
         if(tileentitypiston != null) {
            IBlockState iblockstate = tileentitypiston.getPistonState();
            iblockstate.getBlock().dropBlockAsItem(worldIn, pos, iblockstate, 0);
         }
      }
   }

   public RayTraceResult collisionRayTrace(IBlockState blockState, World worldIn, BlockPos pos, Vec3d start, Vec3d end) {
      return null;
   }

   public void func_189540_a(IBlockState p_189540_1_, World p_189540_2_, BlockPos p_189540_3_, Block p_189540_4_) {
      if(!p_189540_2_.isRemote) {
         p_189540_2_.getTileEntity(p_189540_3_);
      }
   }

   @Nullable
   public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, World worldIn, BlockPos pos) {
      TileEntityPiston tileentitypiston = this.getTilePistonAt(worldIn, pos);
      return tileentitypiston == null?null:tileentitypiston.getAABB(worldIn, pos);
   }

   public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
      TileEntityPiston tileentitypiston = this.getTilePistonAt(source, pos);
      return tileentitypiston != null?tileentitypiston.getAABB(source, pos):FULL_BLOCK_AABB;
   }

   @Nullable
   private TileEntityPiston getTilePistonAt(IBlockAccess iBlockAccessIn, BlockPos blockPosIn) {
      TileEntity tileentity = iBlockAccessIn.getTileEntity(blockPosIn);
      return tileentity instanceof TileEntityPiston?(TileEntityPiston)tileentity:null;
   }

   @Nullable
   public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
      return null;
   }

   public IBlockState getStateFromMeta(int meta) {
      return this.getDefaultState().withProperty(FACING, BlockPistonExtension.getFacing(meta)).withProperty(TYPE, (meta & 8) > 0?BlockPistonExtension.EnumPistonType.STICKY:BlockPistonExtension.EnumPistonType.DEFAULT);
   }

   public IBlockState withRotation(IBlockState state, Rotation rot) {
      return state.withProperty(FACING, rot.rotate((EnumFacing)state.getValue(FACING)));
   }

   public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
      return state.withRotation(mirrorIn.toRotation((EnumFacing)state.getValue(FACING)));
   }

   public int getMetaFromState(IBlockState state) {
      int i = 0;
      i = i | ((EnumFacing)state.getValue(FACING)).getIndex();
      if(state.getValue(TYPE) == BlockPistonExtension.EnumPistonType.STICKY) {
         i |= 8;
      }

      return i;
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{FACING, TYPE});
   }
}
