package net.minecraft.entity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryMerchant;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;

public class NpcMerchant implements IMerchant {
   private InventoryMerchant theMerchantInventory;
   private EntityPlayer customer;
   private MerchantRecipeList recipeList;
   private ITextComponent name;

   public NpcMerchant(EntityPlayer customerIn, ITextComponent nameIn) {
      this.customer = customerIn;
      this.name = nameIn;
      this.theMerchantInventory = new InventoryMerchant(customerIn, this);
   }

   public EntityPlayer getCustomer() {
      return this.customer;
   }

   public void setCustomer(EntityPlayer player) {
   }

   public MerchantRecipeList getRecipes(EntityPlayer player) {
      return this.recipeList;
   }

   public void setRecipes(MerchantRecipeList recipeList) {
      this.recipeList = recipeList;
   }

   public void useRecipe(MerchantRecipe recipe) {
      recipe.incrementToolUses();
   }

   public void verifySellingItem(ItemStack stack) {
   }

   public ITextComponent getDisplayName() {
      return (ITextComponent)(this.name != null?this.name:new TextComponentTranslation("entity.Villager.name", new Object[0]));
   }
}
