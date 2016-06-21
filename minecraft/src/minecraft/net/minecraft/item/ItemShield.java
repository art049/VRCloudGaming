package net.minecraft.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

public class ItemShield extends Item {
   public ItemShield() {
      this.maxStackSize = 1;
      this.setCreativeTab(CreativeTabs.COMBAT);
      this.setMaxDamage(336);
      this.addPropertyOverride(new ResourceLocation("blocking"), new IItemPropertyGetter() {
         public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn) {
            return entityIn != null && entityIn.isHandActive() && entityIn.getActiveItemStack() == stack?1.0F:0.0F;
         }
      });
   }

   public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
      return super.onItemUse(stack, playerIn, worldIn, pos, hand, facing, hitX, hitY, hitZ);
   }

   public String getItemStackDisplayName(ItemStack stack) {
      if(stack.getSubCompound("BlockEntityTag", false) != null) {
         String s = "item.shield.";
         EnumDyeColor enumdyecolor = ItemBanner.getBaseColor(stack);
         s = s + enumdyecolor.getUnlocalizedName() + ".name";
         return I18n.translateToLocal(s);
      } else {
         return I18n.translateToLocal("item.shield.name");
      }
   }

   public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
      ItemBanner.appendHoverTextFromTileEntityTag(stack, tooltip);
   }

   public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
      ItemStack itemstack = new ItemStack(itemIn, 1, 0);
      subItems.add(itemstack);
   }

   public CreativeTabs getCreativeTab() {
      return CreativeTabs.COMBAT;
   }

   public EnumAction getItemUseAction(ItemStack stack) {
      return EnumAction.BLOCK;
   }

   public int getMaxItemUseDuration(ItemStack stack) {
      return 72000;
   }

   public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand) {
      playerIn.setActiveHand(hand);
      return new ActionResult(EnumActionResult.SUCCESS, itemStackIn);
   }

   public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
      return repair.getItem() == Item.getItemFromBlock(Blocks.PLANKS)?true:super.getIsRepairable(toRepair, repair);
   }
}
