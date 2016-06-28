package net.minecraft.inventory;

import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IWorldNameable;

public interface IInventory extends IWorldNameable {
   int getSizeInventory();

   @Nullable
   ItemStack getStackInSlot(int index);

   @Nullable
   ItemStack decrStackSize(int index, int count);

   @Nullable
   ItemStack removeStackFromSlot(int index);

   void setInventorySlotContents(int index, ItemStack stack);

   int getInventoryStackLimit();

   void markDirty();

   boolean isUseableByPlayer(EntityPlayer player);

   void openInventory(EntityPlayer player);

   void closeInventory(EntityPlayer player);

   boolean isItemValidForSlot(int index, ItemStack stack);

   int getField(int id);

   void setField(int id, int value);

   int getFieldCount();

   void clear();
}
