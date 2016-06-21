package net.minecraft.inventory;

import java.util.List;
import net.minecraft.item.ItemStack;

public interface IContainerListener {
   void updateCraftingInventory(Container containerToSend, List<ItemStack> itemsList);

   void sendSlotContents(Container containerToSend, int slotInd, ItemStack stack);

   void sendProgressBarUpdate(Container containerIn, int varToUpdate, int newValue);

   void sendAllWindowProperties(Container containerIn, IInventory inventory);
}
