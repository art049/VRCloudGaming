package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.CommandBlockBaseLogic;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockCommandBlock extends BlockContainer {
   public static final PropertyDirection FACING = BlockDirectional.FACING;
   public static final PropertyBool CONDITIONAL = PropertyBool.create("conditional");

   public BlockCommandBlock(MapColor color) {
      super(Material.IRON, color);
      this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(CONDITIONAL, Boolean.valueOf(false)));
   }

   public TileEntity createNewTileEntity(World worldIn, int meta) {
      TileEntityCommandBlock tileentitycommandblock = new TileEntityCommandBlock();
      tileentitycommandblock.setAuto(this == Blocks.CHAIN_COMMAND_BLOCK);
      return tileentitycommandblock;
   }

   public void func_189540_a(IBlockState p_189540_1_, World p_189540_2_, BlockPos p_189540_3_, Block p_189540_4_) {
      if(!p_189540_2_.isRemote) {
         TileEntity tileentity = p_189540_2_.getTileEntity(p_189540_3_);
         if(tileentity instanceof TileEntityCommandBlock) {
            TileEntityCommandBlock tileentitycommandblock = (TileEntityCommandBlock)tileentity;
            boolean flag = p_189540_2_.isBlockPowered(p_189540_3_);
            boolean flag1 = tileentitycommandblock.isPowered();
            boolean flag2 = tileentitycommandblock.isAuto();
            if(flag && !flag1) {
               tileentitycommandblock.setPowered(true);
               if(tileentitycommandblock.getMode() != TileEntityCommandBlock.Mode.SEQUENCE && !flag2) {
                  boolean flag3 = !tileentitycommandblock.isConditional() || this.isNextToSuccessfulCommandBlock(p_189540_2_, p_189540_3_, p_189540_1_);
                  tileentitycommandblock.setConditionMet(flag3);
                  p_189540_2_.scheduleUpdate(p_189540_3_, this, this.tickRate(p_189540_2_));
                  if(flag3) {
                     this.propagateUpdate(p_189540_2_, p_189540_3_);
                  }
               }
            } else if(!flag && flag1) {
               tileentitycommandblock.setPowered(false);
            }
         }
      }
   }

   public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
      if(!worldIn.isRemote) {
         TileEntity tileentity = worldIn.getTileEntity(pos);
         if(tileentity instanceof TileEntityCommandBlock) {
            TileEntityCommandBlock tileentitycommandblock = (TileEntityCommandBlock)tileentity;
            CommandBlockBaseLogic commandblockbaselogic = tileentitycommandblock.getCommandBlockLogic();
            boolean flag = !StringUtils.isNullOrEmpty(commandblockbaselogic.getCommand());
            TileEntityCommandBlock.Mode tileentitycommandblock$mode = tileentitycommandblock.getMode();
            boolean flag1 = !tileentitycommandblock.isConditional() || this.isNextToSuccessfulCommandBlock(worldIn, pos, state);
            boolean flag2 = tileentitycommandblock.isConditionMet();
            boolean flag3 = false;
            if(tileentitycommandblock$mode != TileEntityCommandBlock.Mode.SEQUENCE && flag2 && flag) {
               commandblockbaselogic.trigger(worldIn);
               flag3 = true;
            }

            if(tileentitycommandblock.isPowered() || tileentitycommandblock.isAuto()) {
               if(tileentitycommandblock$mode == TileEntityCommandBlock.Mode.SEQUENCE && flag1 && flag) {
                  commandblockbaselogic.trigger(worldIn);
                  flag3 = true;
               }

               if(tileentitycommandblock$mode == TileEntityCommandBlock.Mode.AUTO) {
                  worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
                  if(flag1) {
                     this.propagateUpdate(worldIn, pos);
                  }
               }
            }

            if(!flag3) {
               commandblockbaselogic.setSuccessCount(0);
            }

            tileentitycommandblock.setConditionMet(flag1);
            worldIn.updateComparatorOutputLevel(pos, this);
         }
      }
   }

   public boolean isNextToSuccessfulCommandBlock(World worldIn, BlockPos pos, IBlockState state) {
      EnumFacing enumfacing = (EnumFacing)state.getValue(FACING);
      TileEntity tileentity = worldIn.getTileEntity(pos.offset(enumfacing.getOpposite()));
      return tileentity instanceof TileEntityCommandBlock && ((TileEntityCommandBlock)tileentity).getCommandBlockLogic().getSuccessCount() > 0;
   }

   public int tickRate(World worldIn) {
      return 1;
   }

   public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
      TileEntity tileentity = worldIn.getTileEntity(pos);
      if(tileentity instanceof TileEntityCommandBlock) {
         if(!playerIn.capabilities.isCreativeMode) {
            return false;
         } else {
            playerIn.displayGuiCommandBlock((TileEntityCommandBlock)tileentity);
            return true;
         }
      } else {
         return false;
      }
   }

   public boolean hasComparatorInputOverride(IBlockState state) {
      return true;
   }

   public int getComparatorInputOverride(IBlockState blockState, World worldIn, BlockPos pos) {
      TileEntity tileentity = worldIn.getTileEntity(pos);
      return tileentity instanceof TileEntityCommandBlock?((TileEntityCommandBlock)tileentity).getCommandBlockLogic().getSuccessCount():0;
   }

   public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
      TileEntity tileentity = worldIn.getTileEntity(pos);
      if(tileentity instanceof TileEntityCommandBlock) {
         TileEntityCommandBlock tileentitycommandblock = (TileEntityCommandBlock)tileentity;
         CommandBlockBaseLogic commandblockbaselogic = tileentitycommandblock.getCommandBlockLogic();
         if(stack.hasDisplayName()) {
            commandblockbaselogic.setName(stack.getDisplayName());
         }

         if(!worldIn.isRemote) {
            NBTTagCompound nbttagcompound = stack.getTagCompound();
            if(nbttagcompound == null || !nbttagcompound.hasKey("BlockEntityTag", 10)) {
               commandblockbaselogic.setTrackOutput(worldIn.getGameRules().getBoolean("sendCommandFeedback"));
               tileentitycommandblock.setAuto(this == Blocks.CHAIN_COMMAND_BLOCK);
            }

            if(tileentitycommandblock.getMode() == TileEntityCommandBlock.Mode.SEQUENCE) {
               boolean flag = worldIn.isBlockPowered(pos);
               tileentitycommandblock.setPowered(flag);
            }
         }
      }
   }

   public int quantityDropped(Random random) {
      return 0;
   }

   public EnumBlockRenderType getRenderType(IBlockState state) {
      return EnumBlockRenderType.MODEL;
   }

   public IBlockState getStateFromMeta(int meta) {
      return this.getDefaultState().withProperty(FACING, EnumFacing.getFront(meta & 7)).withProperty(CONDITIONAL, Boolean.valueOf((meta & 8) != 0));
   }

   public int getMetaFromState(IBlockState state) {
      return ((EnumFacing)state.getValue(FACING)).getIndex() | (((Boolean)state.getValue(CONDITIONAL)).booleanValue()?8:0);
   }

   public IBlockState withRotation(IBlockState state, Rotation rot) {
      return state.withProperty(FACING, rot.rotate((EnumFacing)state.getValue(FACING)));
   }

   public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
      return state.withRotation(mirrorIn.toRotation((EnumFacing)state.getValue(FACING)));
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{FACING, CONDITIONAL});
   }

   public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
      return this.getDefaultState().withProperty(FACING, BlockPistonBase.getFacingFromEntity(pos, placer)).withProperty(CONDITIONAL, Boolean.valueOf(false));
   }

   public void propagateUpdate(World worldIn, BlockPos pos) {
      IBlockState iblockstate = worldIn.getBlockState(pos);
      if(iblockstate.getBlock() == Blocks.COMMAND_BLOCK || iblockstate.getBlock() == Blocks.REPEATING_COMMAND_BLOCK) {
         BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos(pos);
         blockpos$mutableblockpos.func_189536_c((EnumFacing)iblockstate.getValue(FACING));

         for(TileEntity tileentity = worldIn.getTileEntity(blockpos$mutableblockpos); tileentity instanceof TileEntityCommandBlock; tileentity = worldIn.getTileEntity(blockpos$mutableblockpos)) {
            TileEntityCommandBlock tileentitycommandblock = (TileEntityCommandBlock)tileentity;
            if(tileentitycommandblock.getMode() != TileEntityCommandBlock.Mode.SEQUENCE) {
               break;
            }

            IBlockState iblockstate1 = worldIn.getBlockState(blockpos$mutableblockpos);
            Block block = iblockstate1.getBlock();
            if(block != Blocks.CHAIN_COMMAND_BLOCK || worldIn.isUpdateScheduled(blockpos$mutableblockpos, block)) {
               break;
            }

            worldIn.scheduleUpdate(new BlockPos(blockpos$mutableblockpos), block, this.tickRate(worldIn));
            blockpos$mutableblockpos.func_189536_c((EnumFacing)iblockstate1.getValue(FACING));
         }
      }
   }
}
