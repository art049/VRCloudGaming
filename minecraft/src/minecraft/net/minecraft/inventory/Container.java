package net.minecraft.inventory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.MathHelper;

public abstract class Container {
   public List<ItemStack> inventoryItemStacks = Lists.<ItemStack>newArrayList();
   public List<Slot> inventorySlots = Lists.<Slot>newArrayList();
   public int windowId;
   private short transactionID;
   private int dragMode = -1;
   private int dragEvent;
   private final Set<Slot> dragSlots = Sets.<Slot>newHashSet();
   protected List<IContainerListener> listeners = Lists.<IContainerListener>newArrayList();
   private Set<EntityPlayer> playerList = Sets.<EntityPlayer>newHashSet();

   protected Slot addSlotToContainer(Slot slotIn) {
      slotIn.slotNumber = this.inventorySlots.size();
      this.inventorySlots.add(slotIn);
      this.inventoryItemStacks.add((ItemStack)null);
      return slotIn;
   }

   public void addListener(IContainerListener listener) {
      if(this.listeners.contains(listener)) {
         throw new IllegalArgumentException("Listener already listening");
      } else {
         this.listeners.add(listener);
         listener.updateCraftingInventory(this, this.getInventory());
         this.detectAndSendChanges();
      }
   }

   public void removeListener(IContainerListener listener) {
      this.listeners.remove(listener);
   }

   public List<ItemStack> getInventory() {
      List<ItemStack> list = Lists.<ItemStack>newArrayList();

      for(int i = 0; i < this.inventorySlots.size(); ++i) {
         list.add(((Slot)this.inventorySlots.get(i)).getStack());
      }

      return list;
   }

   public void detectAndSendChanges() {
      for(int i = 0; i < this.inventorySlots.size(); ++i) {
         ItemStack itemstack = ((Slot)this.inventorySlots.get(i)).getStack();
         ItemStack itemstack1 = (ItemStack)this.inventoryItemStacks.get(i);
         if(!ItemStack.areItemStacksEqual(itemstack1, itemstack)) {
            itemstack1 = itemstack == null?null:itemstack.copy();
            this.inventoryItemStacks.set(i, itemstack1);

            for(int j = 0; j < this.listeners.size(); ++j) {
               ((IContainerListener)this.listeners.get(j)).sendSlotContents(this, i, itemstack1);
            }
         }
      }
   }

   public boolean enchantItem(EntityPlayer playerIn, int id) {
      return false;
   }

   @Nullable
   public Slot getSlotFromInventory(IInventory inv, int slotIn) {
      for(int i = 0; i < this.inventorySlots.size(); ++i) {
         Slot slot = (Slot)this.inventorySlots.get(i);
         if(slot.isHere(inv, slotIn)) {
            return slot;
         }
      }

      return null;
   }

   public Slot getSlot(int slotId) {
      return (Slot)this.inventorySlots.get(slotId);
   }

   @Nullable
   public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
      Slot slot = (Slot)this.inventorySlots.get(index);
      return slot != null?slot.getStack():null;
   }

   @Nullable
   public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player) {
      ItemStack itemstack = null;
      InventoryPlayer inventoryplayer = player.inventory;
      if(clickTypeIn == ClickType.QUICK_CRAFT) {
         int i = this.dragEvent;
         this.dragEvent = getDragEvent(dragType);
         if((i != 1 || this.dragEvent != 2) && i != this.dragEvent) {
            this.resetDrag();
         } else if(inventoryplayer.getItemStack() == null) {
            this.resetDrag();
         } else if(this.dragEvent == 0) {
            this.dragMode = extractDragMode(dragType);
            if(isValidDragMode(this.dragMode, player)) {
               this.dragEvent = 1;
               this.dragSlots.clear();
            } else {
               this.resetDrag();
            }
         } else if(this.dragEvent == 1) {
            Slot slot = (Slot)this.inventorySlots.get(slotId);
            if(slot != null && canAddItemToSlot(slot, inventoryplayer.getItemStack(), true) && slot.isItemValid(inventoryplayer.getItemStack()) && inventoryplayer.getItemStack().stackSize > this.dragSlots.size() && this.canDragIntoSlot(slot)) {
               this.dragSlots.add(slot);
            }
         } else if(this.dragEvent == 2) {
            if(!this.dragSlots.isEmpty()) {
               ItemStack itemstack3 = inventoryplayer.getItemStack().copy();
               int j = inventoryplayer.getItemStack().stackSize;

               for(Slot slot1 : this.dragSlots) {
                  if(slot1 != null && canAddItemToSlot(slot1, inventoryplayer.getItemStack(), true) && slot1.isItemValid(inventoryplayer.getItemStack()) && inventoryplayer.getItemStack().stackSize >= this.dragSlots.size() && this.canDragIntoSlot(slot1)) {
                     ItemStack itemstack1 = itemstack3.copy();
                     int k = slot1.getHasStack()?slot1.getStack().stackSize:0;
                     computeStackSize(this.dragSlots, this.dragMode, itemstack1, k);
                     if(itemstack1.stackSize > itemstack1.getMaxStackSize()) {
                        itemstack1.stackSize = itemstack1.getMaxStackSize();
                     }

                     if(itemstack1.stackSize > slot1.getItemStackLimit(itemstack1)) {
                        itemstack1.stackSize = slot1.getItemStackLimit(itemstack1);
                     }

                     j -= itemstack1.stackSize - k;
                     slot1.putStack(itemstack1);
                  }
               }

               itemstack3.stackSize = j;
               if(itemstack3.stackSize <= 0) {
                  itemstack3 = null;
               }

               inventoryplayer.setItemStack(itemstack3);
            }

            this.resetDrag();
         } else {
            this.resetDrag();
         }
      } else if(this.dragEvent != 0) {
         this.resetDrag();
      } else if((clickTypeIn == ClickType.PICKUP || clickTypeIn == ClickType.QUICK_MOVE) && (dragType == 0 || dragType == 1)) {
         if(slotId == -999) {
            if(inventoryplayer.getItemStack() != null) {
               if(dragType == 0) {
                  player.dropItem(inventoryplayer.getItemStack(), true);
                  inventoryplayer.setItemStack((ItemStack)null);
               }

               if(dragType == 1) {
                  player.dropItem(inventoryplayer.getItemStack().splitStack(1), true);
                  if(inventoryplayer.getItemStack().stackSize == 0) {
                     inventoryplayer.setItemStack((ItemStack)null);
                  }
               }
            }
         } else if(clickTypeIn == ClickType.QUICK_MOVE) {
            if(slotId < 0) {
               return null;
            }

            Slot slot6 = (Slot)this.inventorySlots.get(slotId);
            if(slot6 != null && slot6.canTakeStack(player)) {
               ItemStack itemstack8 = slot6.getStack();
               if(itemstack8 != null && itemstack8.stackSize <= 0) {
                  itemstack = itemstack8.copy();
                  slot6.putStack((ItemStack)null);
               }

               ItemStack itemstack11 = this.transferStackInSlot(player, slotId);
               if(itemstack11 != null) {
                  Item item = itemstack11.getItem();
                  itemstack = itemstack11.copy();
                  if(slot6.getStack() != null && slot6.getStack().getItem() == item) {
                     this.retrySlotClick(slotId, dragType, true, player);
                  }
               }
            }
         } else {
            if(slotId < 0) {
               return null;
            }

            Slot slot7 = (Slot)this.inventorySlots.get(slotId);
            if(slot7 != null) {
               ItemStack itemstack9 = slot7.getStack();
               ItemStack itemstack12 = inventoryplayer.getItemStack();
               if(itemstack9 != null) {
                  itemstack = itemstack9.copy();
               }

               if(itemstack9 == null) {
                  if(itemstack12 != null && slot7.isItemValid(itemstack12)) {
                     int l2 = dragType == 0?itemstack12.stackSize:1;
                     if(l2 > slot7.getItemStackLimit(itemstack12)) {
                        l2 = slot7.getItemStackLimit(itemstack12);
                     }

                     slot7.putStack(itemstack12.splitStack(l2));
                     if(itemstack12.stackSize == 0) {
                        inventoryplayer.setItemStack((ItemStack)null);
                     }
                  }
               } else if(slot7.canTakeStack(player)) {
                  if(itemstack12 == null) {
                     if(itemstack9.stackSize > 0) {
                        int k2 = dragType == 0?itemstack9.stackSize:(itemstack9.stackSize + 1) / 2;
                        inventoryplayer.setItemStack(slot7.decrStackSize(k2));
                        if(itemstack9.stackSize <= 0) {
                           slot7.putStack((ItemStack)null);
                        }

                        slot7.onPickupFromSlot(player, inventoryplayer.getItemStack());
                     } else {
                        slot7.putStack((ItemStack)null);
                        inventoryplayer.setItemStack((ItemStack)null);
                     }
                  } else if(slot7.isItemValid(itemstack12)) {
                     if(itemstack9.getItem() == itemstack12.getItem() && itemstack9.getMetadata() == itemstack12.getMetadata() && ItemStack.areItemStackTagsEqual(itemstack9, itemstack12)) {
                        int j2 = dragType == 0?itemstack12.stackSize:1;
                        if(j2 > slot7.getItemStackLimit(itemstack12) - itemstack9.stackSize) {
                           j2 = slot7.getItemStackLimit(itemstack12) - itemstack9.stackSize;
                        }

                        if(j2 > itemstack12.getMaxStackSize() - itemstack9.stackSize) {
                           j2 = itemstack12.getMaxStackSize() - itemstack9.stackSize;
                        }

                        itemstack12.splitStack(j2);
                        if(itemstack12.stackSize == 0) {
                           inventoryplayer.setItemStack((ItemStack)null);
                        }

                        itemstack9.stackSize += j2;
                     } else if(itemstack12.stackSize <= slot7.getItemStackLimit(itemstack12)) {
                        slot7.putStack(itemstack12);
                        inventoryplayer.setItemStack(itemstack9);
                     }
                  } else if(itemstack9.getItem() == itemstack12.getItem() && itemstack12.getMaxStackSize() > 1 && (!itemstack9.getHasSubtypes() || itemstack9.getMetadata() == itemstack12.getMetadata()) && ItemStack.areItemStackTagsEqual(itemstack9, itemstack12)) {
                     int i2 = itemstack9.stackSize;
                     if(i2 > 0 && i2 + itemstack12.stackSize <= itemstack12.getMaxStackSize()) {
                        itemstack12.stackSize += i2;
                        itemstack9 = slot7.decrStackSize(i2);
                        if(itemstack9.stackSize == 0) {
                           slot7.putStack((ItemStack)null);
                        }

                        slot7.onPickupFromSlot(player, inventoryplayer.getItemStack());
                     }
                  }
               }

               slot7.onSlotChanged();
            }
         }
      } else if(clickTypeIn == ClickType.SWAP && dragType >= 0 && dragType < 9) {
         Slot slot5 = (Slot)this.inventorySlots.get(slotId);
         ItemStack itemstack7 = inventoryplayer.getStackInSlot(dragType);
         if(itemstack7 != null && itemstack7.stackSize <= 0) {
            itemstack7 = null;
            inventoryplayer.setInventorySlotContents(dragType, (ItemStack)null);
         }

         ItemStack itemstack10 = slot5.getStack();
         if(itemstack7 != null || itemstack10 != null) {
            if(itemstack7 == null) {
               if(slot5.canTakeStack(player)) {
                  inventoryplayer.setInventorySlotContents(dragType, itemstack10);
                  slot5.putStack((ItemStack)null);
                  slot5.onPickupFromSlot(player, itemstack10);
               }
            } else if(itemstack10 == null) {
               if(slot5.isItemValid(itemstack7)) {
                  int k1 = slot5.getItemStackLimit(itemstack7);
                  if(itemstack7.stackSize > k1) {
                     slot5.putStack(itemstack7.splitStack(k1));
                  } else {
                     slot5.putStack(itemstack7);
                     inventoryplayer.setInventorySlotContents(dragType, (ItemStack)null);
                  }
               }
            } else if(slot5.canTakeStack(player) && slot5.isItemValid(itemstack7)) {
               int l1 = slot5.getItemStackLimit(itemstack7);
               if(itemstack7.stackSize > l1) {
                  slot5.putStack(itemstack7.splitStack(l1));
                  slot5.onPickupFromSlot(player, itemstack10);
                  if(!inventoryplayer.addItemStackToInventory(itemstack10)) {
                     player.dropItem(itemstack10, true);
                  }
               } else {
                  slot5.putStack(itemstack7);
                  inventoryplayer.setInventorySlotContents(dragType, itemstack10);
                  slot5.onPickupFromSlot(player, itemstack10);
               }
            }
         }
      } else if(clickTypeIn == ClickType.CLONE && player.capabilities.isCreativeMode && inventoryplayer.getItemStack() == null && slotId >= 0) {
         Slot slot4 = (Slot)this.inventorySlots.get(slotId);
         if(slot4 != null && slot4.getHasStack()) {
            if(slot4.getStack().stackSize > 0) {
               ItemStack itemstack6 = slot4.getStack().copy();
               itemstack6.stackSize = itemstack6.getMaxStackSize();
               inventoryplayer.setItemStack(itemstack6);
            } else {
               slot4.putStack((ItemStack)null);
            }
         }
      } else if(clickTypeIn == ClickType.THROW && inventoryplayer.getItemStack() == null && slotId >= 0) {
         Slot slot3 = (Slot)this.inventorySlots.get(slotId);
         if(slot3 != null && slot3.getHasStack() && slot3.canTakeStack(player)) {
            ItemStack itemstack5 = slot3.decrStackSize(dragType == 0?1:slot3.getStack().stackSize);
            slot3.onPickupFromSlot(player, itemstack5);
            player.dropItem(itemstack5, true);
         }
      } else if(clickTypeIn == ClickType.PICKUP_ALL && slotId >= 0) {
         Slot slot2 = (Slot)this.inventorySlots.get(slotId);
         ItemStack itemstack4 = inventoryplayer.getItemStack();
         if(itemstack4 != null && (slot2 == null || !slot2.getHasStack() || !slot2.canTakeStack(player))) {
            int i1 = dragType == 0?0:this.inventorySlots.size() - 1;
            int j1 = dragType == 0?1:-1;

            for(int i3 = 0; i3 < 2; ++i3) {
               for(int j3 = i1; j3 >= 0 && j3 < this.inventorySlots.size() && itemstack4.stackSize < itemstack4.getMaxStackSize(); j3 += j1) {
                  Slot slot8 = (Slot)this.inventorySlots.get(j3);
                  if(slot8.getHasStack() && canAddItemToSlot(slot8, itemstack4, true) && slot8.canTakeStack(player) && this.canMergeSlot(itemstack4, slot8) && (i3 != 0 || slot8.getStack().stackSize != slot8.getStack().getMaxStackSize())) {
                     int l = Math.min(itemstack4.getMaxStackSize() - itemstack4.stackSize, slot8.getStack().stackSize);
                     ItemStack itemstack2 = slot8.decrStackSize(l);
                     itemstack4.stackSize += l;
                     if(itemstack2.stackSize <= 0) {
                        slot8.putStack((ItemStack)null);
                     }

                     slot8.onPickupFromSlot(player, itemstack2);
                  }
               }
            }
         }

         this.detectAndSendChanges();
      }

      return itemstack;
   }

   public boolean canMergeSlot(ItemStack stack, Slot slotIn) {
      return true;
   }

   protected void retrySlotClick(int slotId, int clickedButton, boolean mode, EntityPlayer playerIn) {
      this.slotClick(slotId, clickedButton, ClickType.QUICK_MOVE, playerIn);
   }

   public void onContainerClosed(EntityPlayer playerIn) {
      InventoryPlayer inventoryplayer = playerIn.inventory;
      if(inventoryplayer.getItemStack() != null) {
         playerIn.dropItem(inventoryplayer.getItemStack(), false);
         inventoryplayer.setItemStack((ItemStack)null);
      }
   }

   public void onCraftMatrixChanged(IInventory inventoryIn) {
      this.detectAndSendChanges();
   }

   public void putStackInSlot(int slotID, ItemStack stack) {
      this.getSlot(slotID).putStack(stack);
   }

   public void putStacksInSlots(ItemStack[] stack) {
      for(int i = 0; i < stack.length; ++i) {
         this.getSlot(i).putStack(stack[i]);
      }
   }

   public void updateProgressBar(int id, int data) {
   }

   public short getNextTransactionID(InventoryPlayer invPlayer) {
      ++this.transactionID;
      return this.transactionID;
   }

   public boolean getCanCraft(EntityPlayer player) {
      return !this.playerList.contains(player);
   }

   public void setCanCraft(EntityPlayer player, boolean canCraft) {
      if(canCraft) {
         this.playerList.remove(player);
      } else {
         this.playerList.add(player);
      }
   }

   public abstract boolean canInteractWith(EntityPlayer playerIn);

   protected boolean mergeItemStack(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
      boolean flag = false;
      int i = startIndex;
      if(reverseDirection) {
         i = endIndex - 1;
      }

      if(stack.isStackable()) {
         while(stack.stackSize > 0 && (!reverseDirection && i < endIndex || reverseDirection && i >= startIndex)) {
            Slot slot = (Slot)this.inventorySlots.get(i);
            ItemStack itemstack = slot.getStack();
            if(itemstack != null && areItemStacksEqual(stack, itemstack)) {
               int j = itemstack.stackSize + stack.stackSize;
               if(j <= stack.getMaxStackSize()) {
                  stack.stackSize = 0;
                  itemstack.stackSize = j;
                  slot.onSlotChanged();
                  flag = true;
               } else if(itemstack.stackSize < stack.getMaxStackSize()) {
                  stack.stackSize -= stack.getMaxStackSize() - itemstack.stackSize;
                  itemstack.stackSize = stack.getMaxStackSize();
                  slot.onSlotChanged();
                  flag = true;
               }
            }

            if(reverseDirection) {
               --i;
            } else {
               ++i;
            }
         }
      }

      if(stack.stackSize > 0) {
         if(reverseDirection) {
            i = endIndex - 1;
         } else {
            i = startIndex;
         }

         while(!reverseDirection && i < endIndex || reverseDirection && i >= startIndex) {
            Slot slot1 = (Slot)this.inventorySlots.get(i);
            ItemStack itemstack1 = slot1.getStack();
            if(itemstack1 == null) {
               slot1.putStack(stack.copy());
               slot1.onSlotChanged();
               stack.stackSize = 0;
               flag = true;
               break;
            }

            if(reverseDirection) {
               --i;
            } else {
               ++i;
            }
         }
      }

      return flag;
   }

   private static boolean areItemStacksEqual(ItemStack stackA, ItemStack stackB) {
      return stackB.getItem() == stackA.getItem() && (!stackA.getHasSubtypes() || stackA.getMetadata() == stackB.getMetadata()) && ItemStack.areItemStackTagsEqual(stackA, stackB);
   }

   public static int extractDragMode(int eventButton) {
      return eventButton >> 2 & 3;
   }

   public static int getDragEvent(int clickedButton) {
      return clickedButton & 3;
   }

   public static int getQuickcraftMask(int p_94534_0_, int p_94534_1_) {
      return p_94534_0_ & 3 | (p_94534_1_ & 3) << 2;
   }

   public static boolean isValidDragMode(int dragModeIn, EntityPlayer player) {
      return dragModeIn == 0?true:(dragModeIn == 1?true:dragModeIn == 2 && player.capabilities.isCreativeMode);
   }

   protected void resetDrag() {
      this.dragEvent = 0;
      this.dragSlots.clear();
   }

   public static boolean canAddItemToSlot(Slot slotIn, ItemStack stack, boolean stackSizeMatters) {
      boolean flag = slotIn == null || !slotIn.getHasStack();
      if(slotIn != null && slotIn.getHasStack() && stack != null && stack.isItemEqual(slotIn.getStack()) && ItemStack.areItemStackTagsEqual(slotIn.getStack(), stack)) {
         flag |= slotIn.getStack().stackSize + (stackSizeMatters?0:stack.stackSize) <= stack.getMaxStackSize();
      }

      return flag;
   }

   public static void computeStackSize(Set<Slot> dragSlotsIn, int dragModeIn, ItemStack stack, int slotStackSize) {
      switch(dragModeIn) {
      case 0:
         stack.stackSize = MathHelper.floor_float((float)stack.stackSize / (float)dragSlotsIn.size());
         break;
      case 1:
         stack.stackSize = 1;
         break;
      case 2:
         stack.stackSize = stack.getItem().getItemStackLimit();
      }

      stack.stackSize += slotStackSize;
   }

   public boolean canDragIntoSlot(Slot slotIn) {
      return true;
   }

   public static int calcRedstone(@Nullable TileEntity te) {
      return te instanceof IInventory?calcRedstoneFromInventory((IInventory)te):0;
   }

   public static int calcRedstoneFromInventory(@Nullable IInventory inv) {
      if(inv == null) {
         return 0;
      } else {
         int i = 0;
         float f = 0.0F;

         for(int j = 0; j < inv.getSizeInventory(); ++j) {
            ItemStack itemstack = inv.getStackInSlot(j);
            if(itemstack != null) {
               f += (float)itemstack.stackSize / (float)Math.min(inv.getInventoryStackLimit(), itemstack.getMaxStackSize());
               ++i;
            }
         }

         f = f / (float)inv.getSizeInventory();
         return MathHelper.floor_float(f * 14.0F) + (i > 0?1:0);
      }
   }
}
