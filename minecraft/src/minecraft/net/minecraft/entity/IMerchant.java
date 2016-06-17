package net.minecraft.entity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;

public interface IMerchant {
   void setCustomer(EntityPlayer player);

   EntityPlayer getCustomer();

   MerchantRecipeList getRecipes(EntityPlayer player);

   void setRecipes(MerchantRecipeList recipeList);

   void useRecipe(MerchantRecipe recipe);

   void verifySellingItem(ItemStack stack);

   ITextComponent getDisplayName();
}
