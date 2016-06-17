package net.minecraft.item;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemSeedFood extends ItemFood {
   private Block crops;
   private Block soilId;

   public ItemSeedFood(int healAmount, float saturation, Block crops, Block soil) {
      super(healAmount, saturation, false);
      this.crops = crops;
      this.soilId = soil;
   }

   public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
      if(facing == EnumFacing.UP && playerIn.canPlayerEdit(pos.offset(facing), facing, stack) && worldIn.getBlockState(pos).getBlock() == this.soilId && worldIn.isAirBlock(pos.up())) {
         worldIn.setBlockState(pos.up(), this.crops.getDefaultState(), 11);
         --stack.stackSize;
         return EnumActionResult.SUCCESS;
      } else {
         return EnumActionResult.FAIL;
      }
   }
}
