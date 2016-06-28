package net.minecraft.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

public class ItemPotion extends Item {
   public ItemPotion() {
      this.setMaxStackSize(1);
      this.setCreativeTab(CreativeTabs.BREWING);
   }

   @Nullable
   public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving) {
      EntityPlayer entityplayer = entityLiving instanceof EntityPlayer?(EntityPlayer)entityLiving:null;
      if(entityplayer == null || !entityplayer.capabilities.isCreativeMode) {
         --stack.stackSize;
      }

      if(!worldIn.isRemote) {
         for(PotionEffect potioneffect : PotionUtils.getEffectsFromStack(stack)) {
            entityLiving.addPotionEffect(new PotionEffect(potioneffect));
         }
      }

      if(entityplayer != null) {
         entityplayer.addStat(StatList.getObjectUseStats(this));
      }

      if(entityplayer == null || !entityplayer.capabilities.isCreativeMode) {
         if(stack.stackSize <= 0) {
            return new ItemStack(Items.GLASS_BOTTLE);
         }

         if(entityplayer != null) {
            entityplayer.inventory.addItemStackToInventory(new ItemStack(Items.GLASS_BOTTLE));
         }
      }

      return stack;
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

   public String getItemStackDisplayName(ItemStack stack) {
      return I18n.translateToLocal(PotionUtils.getPotionFromItem(stack).getNamePrefixed("potion.effect."));
   }

   public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
      PotionUtils.addPotionTooltip(stack, tooltip, 1.0F);
   }

   public boolean hasEffect(ItemStack stack) {
      return !PotionUtils.getEffectsFromStack(stack).isEmpty();
   }

   public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
      for(PotionType potiontype : PotionType.REGISTRY) {
         subItems.add(PotionUtils.addPotionToItemStack(new ItemStack(itemIn), potiontype));
      }
   }
}
