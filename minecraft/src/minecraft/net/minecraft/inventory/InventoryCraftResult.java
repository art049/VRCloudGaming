package net.minecraft.inventory;

import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

public class InventoryCraftResult implements IInventory {
   private ItemStack[] stackResult = new ItemStack[1];

   public int getSizeInventory() {
      return 1;
   }

   @Nullable
   public ItemStack getStackInSlot(int index) {
      return this.stackResult[0];
   }

   public String getName() {
      return "Result";
   }

   public boolean hasCustomName() {
      return false;
   }

   public ITextComponent getDisplayName() {
      return (ITextComponent)(this.hasCustomName()?new TextComponentString(this.getName()):new TextComponentTranslation(this.getName(), new Object[0]));
   }

   @Nullable
   public ItemStack decrStackSize(int index, int count) {
      return ItemStackHelper.getAndRemove(this.stackResult, 0);
   }

   @Nullable
   public ItemStack removeStackFromSlot(int index) {
      return ItemStackHelper.getAndRemove(this.stackResult, 0);
   }

   public void setInventorySlotContents(int index, @Nullable ItemStack stack) {
      this.stackResult[0] = stack;
   }

   public int getInventoryStackLimit() {
      return 64;
   }

   public void markDirty() {
   }

   public boolean isUseableByPlayer(EntityPlayer player) {
      return true;
   }

   public void openInventory(EntityPlayer player) {
   }

   public void closeInventory(EntityPlayer player) {
   }

   public boolean isItemValidForSlot(int index, ItemStack stack) {
      return true;
   }

   public int getField(int id) {
      return 0;
   }

   public void setField(int id, int value) {
   }

   public int getFieldCount() {
      return 0;
   }

   public void clear() {
      for(int i = 0; i < this.stackResult.length; ++i) {
         this.stackResult[i] = null;
      }
   }
}
