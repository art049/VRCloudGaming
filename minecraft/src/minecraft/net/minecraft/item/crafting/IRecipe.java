package net.minecraft.item.crafting;

import javax.annotation.Nullable;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface IRecipe {
   boolean matches(InventoryCrafting inv, World worldIn);

   @Nullable
   ItemStack getCraftingResult(InventoryCrafting inv);

   int getRecipeSize();

   @Nullable
   ItemStack getRecipeOutput();

   ItemStack[] getRemainingItems(InventoryCrafting inv);
}
