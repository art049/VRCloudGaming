package net.minecraft.item;

import javax.annotation.Nullable;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class ItemBucketMilk extends Item {
   public ItemBucketMilk() {
      this.setMaxStackSize(1);
      this.setCreativeTab(CreativeTabs.MISC);
   }

   @Nullable
   public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving) {
      if(entityLiving instanceof EntityPlayer && !((EntityPlayer)entityLiving).capabilities.isCreativeMode) {
         --stack.stackSize;
      }

      if(!worldIn.isRemote) {
         entityLiving.clearActivePotions();
      }

      if(entityLiving instanceof EntityPlayer) {
         ((EntityPlayer)entityLiving).addStat(StatList.getObjectUseStats(this));
      }

      return stack.stackSize <= 0?new ItemStack(Items.BUCKET):stack;
   }

   public int getMaxItemUseDuration(ItemStack stack) {
      return 32;
   }

   public EnumAction getItemUseAction(ItemStack stack) {
      return EnumAction.DRINK;
   }

   public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand) {
      playerIn.setActiveHand(hand);
      return new ActionResult(EnumActionResult.SUCCESS, itemStackIn);
   }
}
