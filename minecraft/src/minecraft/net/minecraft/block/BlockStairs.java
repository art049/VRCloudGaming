package net.minecraft.block;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockStairs extends Block {
   public static final PropertyDirection FACING = BlockHorizontal.FACING;
   public static final PropertyEnum<BlockStairs.EnumHalf> HALF = PropertyEnum.<BlockStairs.EnumHalf>create("half", BlockStairs.EnumHalf.class);
   public static final PropertyEnum<BlockStairs.EnumShape> SHAPE = PropertyEnum.<BlockStairs.EnumShape>create("shape", BlockStairs.EnumShape.class);
   protected static final AxisAlignedBB AABB_SLAB_TOP = new AxisAlignedBB(0.0D, 0.5D, 0.0D, 1.0D, 1.0D, 1.0D);
   protected static final AxisAlignedBB AABB_QTR_TOP_WEST = new AxisAlignedBB(0.0D, 0.5D, 0.0D, 0.5D, 1.0D, 1.0D);
   protected static final AxisAlignedBB AABB_QTR_TOP_EAST = new AxisAlignedBB(0.5D, 0.5D, 0.0D, 1.0D, 1.0D, 1.0D);
   protected static final AxisAlignedBB AABB_QTR_TOP_NORTH = new AxisAlignedBB(0.0D, 0.5D, 0.0D, 1.0D, 1.0D, 0.5D);
   protected static final AxisAlignedBB AABB_QTR_TOP_SOUTH = new AxisAlignedBB(0.0D, 0.5D, 0.5D, 1.0D, 1.0D, 1.0D);
   protected static final AxisAlignedBB AABB_OCT_TOP_NW = new AxisAlignedBB(0.0D, 0.5D, 0.0D, 0.5D, 1.0D, 0.5D);
   protected static final AxisAlignedBB AABB_OCT_TOP_NE = new AxisAlignedBB(0.5D, 0.5D, 0.0D, 1.0D, 1.0D, 0.5D);
   protected static final AxisAlignedBB AABB_OCT_TOP_SW = new AxisAlignedBB(0.0D, 0.5D, 0.5D, 0.5D, 1.0D, 1.0D);
   protected static final AxisAlignedBB AABB_OCT_TOP_SE = new AxisAlignedBB(0.5D, 0.5D, 0.5D, 1.0D, 1.0D, 1.0D);
   protected static final AxisAlignedBB AABB_SLAB_BOTTOM = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D);
   protected static final AxisAlignedBB AABB_QTR_BOT_WEST = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.5D, 0.5D, 1.0D);
   protected static final AxisAlignedBB AABB_QTR_BOT_EAST = new AxisAlignedBB(0.5D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D);
   protected static final AxisAlignedBB AABB_QTR_BOT_NORTH = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 0.5D);
   protected static final AxisAlignedBB AABB_QTR_BOT_SOUTH = new AxisAlignedBB(0.0D, 0.0D, 0.5D, 1.0D, 0.5D, 1.0D);
   protected static final AxisAlignedBB AABB_OCT_BOT_NW = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.5D, 0.5D, 0.5D);
   protected static final AxisAlignedBB AABB_OCT_BOT_NE = new AxisAlignedBB(0.5D, 0.0D, 0.0D, 1.0D, 0.5D, 0.5D);
   protected static final AxisAlignedBB AABB_OCT_BOT_SW = new AxisAlignedBB(0.0D, 0.0D, 0.5D, 0.5D, 0.5D, 1.0D);
   protected static final AxisAlignedBB AABB_OCT_BOT_SE = new AxisAlignedBB(0.5D, 0.0D, 0.5D, 1.0D, 0.5D, 1.0D);
   private final Block modelBlock;
   private final IBlockState modelState;

   protected BlockStairs(IBlockState modelState) {
      super(modelState.getBlock().blockMaterial);
      this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(HALF, BlockStairs.EnumHalf.BOTTOM).withProperty(SHAPE, BlockStairs.EnumShape.STRAIGHT));
      this.modelBlock = modelState.getBlock();
      this.modelState = modelState;
      this.setHardness(this.modelBlock.blockHardness);
      this.setResistance(this.modelBlock.blockResistance / 3.0F);
      this.setSoundType(this.modelBlock.blockSoundType);
      this.setLightOpacity(255);
      this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
   }

   public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn) {
      state = this.getActualState(state, worldIn, pos);

      for(AxisAlignedBB axisalignedbb : getCollisionBoxList(state)) {
         addCollisionBoxToList(pos, entityBox, collidingBoxes, axisalignedbb);
      }
   }

   private static List<AxisAlignedBB> getCollisionBoxList(IBlockState bstate) {
      List<AxisAlignedBB> list = Lists.<AxisAlignedBB>newArrayList();
      boolean flag = bstate.getValue(HALF) == BlockStairs.EnumHalf.TOP;
      list.add(flag?AABB_SLAB_TOP:AABB_SLAB_BOTTOM);
      BlockStairs.EnumShape blockstairs$enumshape = (BlockStairs.EnumShape)bstate.getValue(SHAPE);
      if(blockstairs$enumshape == BlockStairs.EnumShape.STRAIGHT || blockstairs$enumshape == BlockStairs.EnumShape.INNER_LEFT || blockstairs$enumshape == BlockStairs.EnumShape.INNER_RIGHT) {
         list.add(getCollQuarterBlock(bstate));
      }

      if(blockstairs$enumshape != BlockStairs.EnumShape.STRAIGHT) {
         list.add(getCollEighthBlock(bstate));
      }

      return list;
   }

   private static AxisAlignedBB getCollQuarterBlock(IBlockState bstate) {
      boolean flag = bstate.getValue(HALF) == BlockStairs.EnumHalf.TOP;
      switch((EnumFacing)bstate.getValue(FACING)) {
      case NORTH:
      default:
         return flag?AABB_QTR_BOT_NORTH:AABB_QTR_TOP_NORTH;
      case SOUTH:
         return flag?AABB_QTR_BOT_SOUTH:AABB_QTR_TOP_SOUTH;
      case WEST:
         return flag?AABB_QTR_BOT_WEST:AABB_QTR_TOP_WEST;
      case EAST:
         return flag?AABB_QTR_BOT_EAST:AABB_QTR_TOP_EAST;
      }
   }

   private static AxisAlignedBB getCollEighthBlock(IBlockState bstate) {
      EnumFacing enumfacing = (EnumFacing)bstate.getValue(FACING);
      EnumFacing enumfacing1;
      switch((BlockStairs.EnumShape)bstate.getValue(SHAPE)) {
      case OUTER_LEFT:
      default:
         enumfacing1 = enumfacing;
         break;
      case OUTER_RIGHT:
         enumfacing1 = enumfacing.rotateY();
         break;
      case INNER_RIGHT:
         enumfacing1 = enumfacing.getOpposite();
         break;
      case INNER_LEFT:
         enumfacing1 = enumfacing.rotateYCCW();
      }

      boolean flag = bstate.getValue(HALF) == BlockStairs.EnumHalf.TOP;
      switch(enumfacing1) {
      case NORTH:
      default:
         return flag?AABB_OCT_BOT_NW:AABB_OCT_TOP_NW;
      case SOUTH:
         return flag?AABB_OCT_BOT_SE:AABB_OCT_TOP_SE;
      case WEST:
         return flag?AABB_OCT_BOT_SW:AABB_OCT_TOP_SW;
      case EAST:
         return flag?AABB_OCT_BOT_NE:AABB_OCT_TOP_NE;
      }
   }

   public boolean isOpaqueCube(IBlockState state) {
      return false;
   }

   public boolean isFullCube(IBlockState state) {
      return false;
   }

   public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
      this.modelBlock.randomDisplayTick(stateIn, worldIn, pos, rand);
   }

   public void onBlockClicked(World worldIn, BlockPos pos, EntityPlayer playerIn) {
      this.modelBlock.onBlockClicked(worldIn, pos, playerIn);
   }

   public void onBlockDestroyedByPlayer(World worldIn, BlockPos pos, IBlockState state) {
      this.modelBlock.onBlockDestroyedByPlayer(worldIn, pos, state);
   }

   public int getPackedLightmapCoords(IBlockState state, IBlockAccess source, BlockPos pos) {
      return this.modelState.getPackedLightmapCoords(source, pos);
   }

   public float getExplosionResistance(Entity exploder) {
      return this.modelBlock.getExplosionResistance(exploder);
   }

   public BlockRenderLayer getBlockLayer() {
      return this.modelBlock.getBlockLayer();
   }

   public int tickRate(World worldIn) {
      return this.modelBlock.tickRate(worldIn);
   }

   public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World worldIn, BlockPos pos) {
      return this.modelState.getSelectedBoundingBox(worldIn, pos);
   }

   public Vec3d modifyAcceleration(World worldIn, BlockPos pos, Entity entityIn, Vec3d motion) {
      return this.modelBlock.modifyAcceleration(worldIn, pos, entityIn, motion);
   }

   public boolean isCollidable() {
      return this.modelBlock.isCollidable();
   }

   public boolean canCollideCheck(IBlockState state, boolean hitIfLiquid) {
      return this.modelBlock.canCollideCheck(state, hitIfLiquid);
   }

   public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
      return this.modelBlock.canPlaceBlockAt(worldIn, pos);
   }

   public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
      this.modelState.func_189546_a(worldIn, pos, Blocks.AIR);
      this.modelBlock.onBlockAdded(worldIn, pos, this.modelState);
   }

   public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
      this.modelBlock.breakBlock(worldIn, pos, this.modelState);
   }

   public void onEntityWalk(World worldIn, BlockPos pos, Entity entityIn) {
      this.modelBlock.onEntityWalk(worldIn, pos, entityIn);
   }

   public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
      this.modelBlock.updateTick(worldIn, pos, state, rand);
   }

   public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
      return this.modelBlock.onBlockActivated(worldIn, pos, this.modelState, playerIn, hand, heldItem, EnumFacing.DOWN, 0.0F, 0.0F, 0.0F);
   }

   public void onBlockDestroyedByExplosion(World worldIn, BlockPos pos, Explosion explosionIn) {
      this.modelBlock.onBlockDestroyedByExplosion(worldIn, pos, explosionIn);
   }

   public boolean isFullyOpaque(IBlockState state) {
      return state.getValue(HALF) == BlockStairs.EnumHalf.TOP;
   }

   public MapColor getMapColor(IBlockState state) {
      return this.modelBlock.getMapColor(this.modelState);
   }

   public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
      IBlockState iblockstate = super.onBlockPlaced(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer);
      iblockstate = iblockstate.withProperty(FACING, placer.getHorizontalFacing()).withProperty(SHAPE, BlockStairs.EnumShape.STRAIGHT);
      return facing != EnumFacing.DOWN && (facing == EnumFacing.UP || (double)hitY <= 0.5D)?iblockstate.withProperty(HALF, BlockStairs.EnumHalf.BOTTOM):iblockstate.withProperty(HALF, BlockStairs.EnumHalf.TOP);
   }

   @Nullable
   public RayTraceResult collisionRayTrace(IBlockState blockState, World worldIn, BlockPos pos, Vec3d start, Vec3d end) {
      List<RayTraceResult> list = Lists.<RayTraceResult>newArrayList();

      for(AxisAlignedBB axisalignedbb : getCollisionBoxList(this.getActualState(blockState, worldIn, pos))) {
         list.add(this.rayTrace(pos, start, end, axisalignedbb));
      }

      RayTraceResult raytraceresult1 = null;
      double d1 = 0.0D;

      for(RayTraceResult raytraceresult : list) {
         if(raytraceresult != null) {
            double d0 = raytraceresult.hitVec.squareDistanceTo(end);
            if(d0 > d1) {
               raytraceresult1 = raytraceresult;
               d1 = d0;
            }
         }
      }

      return raytraceresult1;
   }

   public IBlockState getStateFromMeta(int meta) {
      IBlockState iblockstate = this.getDefaultState().withProperty(HALF, (meta & 4) > 0?BlockStairs.EnumHalf.TOP:BlockStairs.EnumHalf.BOTTOM);
      iblockstate = iblockstate.withProperty(FACING, EnumFacing.getFront(5 - (meta & 3)));
      return iblockstate;
   }

   public int getMetaFromState(IBlockState state) {
      int i = 0;
      if(state.getValue(HALF) == BlockStairs.EnumHalf.TOP) {
         i |= 4;
      }

      i = i | 5 - ((EnumFacing)state.getValue(FACING)).getIndex();
      return i;
   }

   public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
      return state.withProperty(SHAPE, getStairsShape(state, worldIn, pos));
   }

   private static BlockStairs.EnumShape getStairsShape(IBlockState p_185706_0_, IBlockAccess p_185706_1_, BlockPos p_185706_2_) {
      EnumFacing enumfacing = (EnumFacing)p_185706_0_.getValue(FACING);
      IBlockState iblockstate = p_185706_1_.getBlockState(p_185706_2_.offset(enumfacing));
      if(isBlockStairs(iblockstate) && p_185706_0_.getValue(HALF) == iblockstate.getValue(HALF)) {
         EnumFacing enumfacing1 = (EnumFacing)iblockstate.getValue(FACING);
         if(enumfacing1.getAxis() != ((EnumFacing)p_185706_0_.getValue(FACING)).getAxis() && isDifferentStairs(p_185706_0_, p_185706_1_, p_185706_2_, enumfacing1.getOpposite())) {
            if(enumfacing1 == enumfacing.rotateYCCW()) {
               return BlockStairs.EnumShape.OUTER_LEFT;
            }

            return BlockStairs.EnumShape.OUTER_RIGHT;
         }
      }

      IBlockState iblockstate1 = p_185706_1_.getBlockState(p_185706_2_.offset(enumfacing.getOpposite()));
      if(isBlockStairs(iblockstate1) && p_185706_0_.getValue(HALF) == iblockstate1.getValue(HALF)) {
         EnumFacing enumfacing2 = (EnumFacing)iblockstate1.getValue(FACING);
         if(enumfacing2.getAxis() != ((EnumFacing)p_185706_0_.getValue(FACING)).getAxis() && isDifferentStairs(p_185706_0_, p_185706_1_, p_185706_2_, enumfacing2)) {
            if(enumfacing2 == enumfacing.rotateYCCW()) {
               return BlockStairs.EnumShape.INNER_LEFT;
            }

            return BlockStairs.EnumShape.INNER_RIGHT;
         }
      }

      return BlockStairs.EnumShape.STRAIGHT;
   }

   private static boolean isDifferentStairs(IBlockState p_185704_0_, IBlockAccess p_185704_1_, BlockPos p_185704_2_, EnumFacing p_185704_3_) {
      IBlockState iblockstate = p_185704_1_.getBlockState(p_185704_2_.offset(p_185704_3_));
      return !isBlockStairs(iblockstate) || iblockstate.getValue(FACING) != p_185704_0_.getValue(FACING) || iblockstate.getValue(HALF) != p_185704_0_.getValue(HALF);
   }

   public static boolean isBlockStairs(IBlockState state) {
      return state.getBlock() instanceof BlockStairs;
   }

   public IBlockState withRotation(IBlockState state, Rotation rot) {
      return state.withProperty(FACING, rot.rotate((EnumFacing)state.getValue(FACING)));
   }

   @SuppressWarnings("incomplete-switch")
   public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
      EnumFacing enumfacing = (EnumFacing)state.getValue(FACING);
      BlockStairs.EnumShape blockstairs$enumshape = (BlockStairs.EnumShape)state.getValue(SHAPE);
      switch(mirrorIn) {
      case LEFT_RIGHT:
         if(enumfacing.getAxis() == EnumFacing.Axis.Z) {
            switch(blockstairs$enumshape) {
            case OUTER_LEFT:
               return state.withRotation(Rotation.CLOCKWISE_180).withProperty(SHAPE, BlockStairs.EnumShape.OUTER_RIGHT);
            case OUTER_RIGHT:
               return state.withRotation(Rotation.CLOCKWISE_180).withProperty(SHAPE, BlockStairs.EnumShape.OUTER_LEFT);
            case INNER_RIGHT:
               return state.withRotation(Rotation.CLOCKWISE_180).withProperty(SHAPE, BlockStairs.EnumShape.INNER_LEFT);
            case INNER_LEFT:
               return state.withRotation(Rotation.CLOCKWISE_180).withProperty(SHAPE, BlockStairs.EnumShape.INNER_RIGHT);
            default:
               return state.withRotation(Rotation.CLOCKWISE_180);
            }
         }
         break;
      case FRONT_BACK:
         if(enumfacing.getAxis() == EnumFacing.Axis.X) {
            switch(blockstairs$enumshape) {
            case OUTER_LEFT:
               return state.withRotation(Rotation.CLOCKWISE_180).withProperty(SHAPE, BlockStairs.EnumShape.OUTER_RIGHT);
            case OUTER_RIGHT:
               return state.withRotation(Rotation.CLOCKWISE_180).withProperty(SHAPE, BlockStairs.EnumShape.OUTER_LEFT);
            case INNER_RIGHT:
               return state.withRotation(Rotation.CLOCKWISE_180).withProperty(SHAPE, BlockStairs.EnumShape.INNER_RIGHT);
            case INNER_LEFT:
               return state.withRotation(Rotation.CLOCKWISE_180).withProperty(SHAPE, BlockStairs.EnumShape.INNER_LEFT);
            case STRAIGHT:
               return state.withRotation(Rotation.CLOCKWISE_180);
            }
         }
      }

      return super.withMirror(state, mirrorIn);
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{FACING, HALF, SHAPE});
   }

   public static enum EnumHalf implements IStringSerializable {
      TOP("top"),
      BOTTOM("bottom");

      private final String name;

      private EnumHalf(String name) {
         this.name = name;
      }

      public String toString() {
         return this.name;
      }

      public String getName() {
         return this.name;
      }
   }

   public static enum EnumShape implements IStringSerializable {
      STRAIGHT("straight"),
      INNER_LEFT("inner_left"),
      INNER_RIGHT("inner_right"),
      OUTER_LEFT("outer_left"),
      OUTER_RIGHT("outer_right");

      private final String name;

      private EnumShape(String name) {
         this.name = name;
      }

      public String toString() {
         return this.name;
      }

      public String getName() {
         return this.name;
      }
   }
}
