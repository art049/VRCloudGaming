package net.minecraft.inventory;

import javax.annotation.Nullable;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.village.MerchantRecipe;

public class SlotMerchantResult extends Slot {
   private final InventoryMerchant theMerchantInventory;
   private EntityPlayer thePlayer;
   private int removeCount;
   private final IMerchant theMerchant;

   public SlotMerchantResult(EntityPlayer player, IMerchant merchant, InventoryMerchant merchantInventory, int slotIndex, int xPosition, int yPosition) {
      super(merchantInventory, slotIndex, xPosition, yPosition);
      this.thePlayer = player;
      this.theMerchant = merchant;
      this.theMerchantInventory = merchantInventory;
   }

   public boolean isItemValid(@Nullable ItemStack stack) {
      return false;
   }

   public ItemStack decrStackSize(int amount) {
      if(this.getHasStack()) {
         this.removeCount += Math.min(amount, this.getStack().stackSize);
      }

      return super.decrStackSize(amount);
   }

   protected void onCrafting(ItemStack stack, int amount) {
      this.removeCount += amount;
      this.onCrafting(stack);
   }

   protected void onCrafting(ItemStack stack) {
      stack.onCrafting(this.thePlayer.worldObj, this.thePlayer, this.removeCount);
      this.removeCount = 0;
   }

   public void onPickupFromSlot(EntityPlayer playerIn, ItemStack stack) {
      this.onCrafting(stack);
      MerchantRecipe merchantrecipe = this.theMerchantInventory.getCurrentRecipe();
      if(merchantrecipe != null) {
         ItemStack itemstack = this.theMerchantInventory.getStackInSlot(0);
         ItemStack itemstack1 = this.theMerchantInventory.getStackInSlot(1);
         if(this.doTrade(merchantrecipe, itemstack, itemstack1) || this.doTrade(merchantrecipe, itemstack1, itemstack)) {
            this.theMerchant.useRecipe(merchantrecipe);
            playerIn.addStat(StatList.TRADED_WITH_VILLAGER);
            if(itemstack != null && itemstack.stackSize <= 0) {
               itemstack = null;
            }

            if(itemstack1 != null && itemstack1.stackSize <= 0) {
               itemstack1 = null;
            }

            this.theMerchantInventory.setInventorySlotContents(0, itemstack);
            this.theMerchantInventory.setInventorySlotContents(1, itemstack1);
         }
      }
   }

   private boolean doTrade(MerchantRecipe trade, ItemStack firstItem, ItemStack secondItem) {
      ItemStack itemstack = trade.getItemToBuy();
      ItemStack itemstack1 = trade.getSecondItemToBuy();
      if(firstItem != null && firstItem.getItem() == itemstack.getItem() && firstItem.stackSize >= itemstack.stackSize) {
         if(itemstack1 != null && secondItem != null && itemstack1.getItem() == secondItem.getItem() && secondItem.stackSize >= itemstack1.stackSize) {
            firstItem.stackSize -= itemstack.stackSize;
            secondItem.stackSize -= itemstack1.stackSize;
            return true;
         }

         if(itemstack1 == null && secondItem == null) {
            firstItem.stackSize -= itemstack.stackSize;
            return true;
         }
      }

      return false;
   }
}
