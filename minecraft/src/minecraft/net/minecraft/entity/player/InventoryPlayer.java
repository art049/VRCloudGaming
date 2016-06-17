package net.minecraft.entity.player;

import java.util.Arrays;
import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ICrashReportDetail;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ReportedException;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

public class InventoryPlayer implements IInventory {
   public final ItemStack[] mainInventory = new ItemStack[36];
   public final ItemStack[] armorInventory = new ItemStack[4];
   public final ItemStack[] offHandInventory = new ItemStack[1];
   private final ItemStack[][] allInventories;
   public int currentItem;
   public EntityPlayer player;
   private ItemStack itemStack;
   public boolean inventoryChanged;

   public InventoryPlayer(EntityPlayer playerIn) {
      this.allInventories = new ItemStack[][]{this.mainInventory, this.armorInventory, this.offHandInventory};
      this.player = playerIn;
   }

   @Nullable
   public ItemStack getCurrentItem() {
      return isHotbar(this.currentItem)?this.mainInventory[this.currentItem]:null;
   }

   public static int getHotbarSize() {
      return 9;
   }

   private boolean canMergeStacks(@Nullable ItemStack stack1, ItemStack stack2) {
      return stack1 != null && this.stackEqualExact(stack1, stack2) && stack1.isStackable() && stack1.stackSize < stack1.getMaxStackSize() && stack1.stackSize < this.getInventoryStackLimit();
   }

   private boolean stackEqualExact(ItemStack stack1, ItemStack stack2) {
      return stack1.getItem() == stack2.getItem() && (!stack1.getHasSubtypes() || stack1.getMetadata() == stack2.getMetadata()) && ItemStack.areItemStackTagsEqual(stack1, stack2);
   }

   public int getFirstEmptyStack() {
      for(int i = 0; i < this.mainInventory.length; ++i) {
         if(this.mainInventory[i] == null) {
            return i;
         }
      }

      return -1;
   }

   public void setPickedItemStack(ItemStack stack) {
      int i = this.getSlotFor(stack);
      if(isHotbar(i)) {
         this.currentItem = i;
      } else {
         if(i == -1) {
            this.currentItem = this.getBestHotbarSlot();
            if(this.mainInventory[this.currentItem] != null) {
               int j = this.getFirstEmptyStack();
               if(j != -1) {
                  this.mainInventory[j] = this.mainInventory[this.currentItem];
               }
            }

            this.mainInventory[this.currentItem] = stack;
         } else {
            this.pickItem(i);
         }
      }
   }

   public void pickItem(int index) {
      this.currentItem = this.getBestHotbarSlot();
      ItemStack itemstack = this.mainInventory[this.currentItem];
      this.mainInventory[this.currentItem] = this.mainInventory[index];
      this.mainInventory[index] = itemstack;
   }

   public static boolean isHotbar(int index) {
      return index >= 0 && index < 9;
   }

   public int getSlotFor(ItemStack stack) {
      for(int i = 0; i < this.mainInventory.length; ++i) {
         if(this.mainInventory[i] != null && this.stackEqualExact(stack, this.mainInventory[i])) {
            return i;
         }
      }

      return -1;
   }

   public int getBestHotbarSlot() {
      for(int i = 0; i < 9; ++i) {
         int j = (this.currentItem + i) % 9;
         if(this.mainInventory[j] == null) {
            return j;
         }
      }

      for(int k = 0; k < 9; ++k) {
         int l = (this.currentItem + k) % 9;
         if(!this.mainInventory[l].isItemEnchanted()) {
            return l;
         }
      }

      return this.currentItem;
   }

   public void changeCurrentItem(int direction) {
      if(direction > 0) {
         direction = 1;
      }

      if(direction < 0) {
         direction = -1;
      }

      for(this.currentItem -= direction; this.currentItem < 0; this.currentItem += 9) {
         ;
      }

      while(this.currentItem >= 9) {
         this.currentItem -= 9;
      }
   }

   public int clearMatchingItems(@Nullable Item itemIn, int metadataIn, int removeCount, @Nullable NBTTagCompound itemNBT) {
      int i = 0;

      for(int j = 0; j < this.getSizeInventory(); ++j) {
         ItemStack itemstack = this.getStackInSlot(j);
         if(itemstack != null && (itemIn == null || itemstack.getItem() == itemIn) && (metadataIn <= -1 || itemstack.getMetadata() == metadataIn) && (itemNBT == null || NBTUtil.areNBTEquals(itemNBT, itemstack.getTagCompound(), true))) {
            int k = removeCount <= 0?itemstack.stackSize:Math.min(removeCount - i, itemstack.stackSize);
            i += k;
            if(removeCount != 0) {
               itemstack.stackSize -= k;
               if(itemstack.stackSize == 0) {
                  this.setInventorySlotContents(j, (ItemStack)null);
               }

               if(removeCount > 0 && i >= removeCount) {
                  return i;
               }
            }
         }
      }

      if(this.itemStack != null) {
         if(itemIn != null && this.itemStack.getItem() != itemIn) {
            return i;
         }

         if(metadataIn > -1 && this.itemStack.getMetadata() != metadataIn) {
            return i;
         }

         if(itemNBT != null && !NBTUtil.areNBTEquals(itemNBT, this.itemStack.getTagCompound(), true)) {
            return i;
         }

         int l = removeCount <= 0?this.itemStack.stackSize:Math.min(removeCount - i, this.itemStack.stackSize);
         i += l;
         if(removeCount != 0) {
            this.itemStack.stackSize -= l;
            if(this.itemStack.stackSize == 0) {
               this.itemStack = null;
            }

            if(removeCount > 0 && i >= removeCount) {
               return i;
            }
         }
      }

      return i;
   }

   private int storePartialItemStack(ItemStack itemStackIn) {
      Item item = itemStackIn.getItem();
      int i = itemStackIn.stackSize;
      int j = this.storeItemStack(itemStackIn);
      if(j == -1) {
         j = this.getFirstEmptyStack();
      }

      if(j == -1) {
         return i;
      } else {
         ItemStack itemstack = this.getStackInSlot(j);
         if(itemstack == null) {
            itemstack = new ItemStack(item, 0, itemStackIn.getMetadata());
            if(itemStackIn.hasTagCompound()) {
               itemstack.setTagCompound((NBTTagCompound)itemStackIn.getTagCompound().copy());
            }

            this.setInventorySlotContents(j, itemstack);
         }

         int k = i;
         if(i > itemstack.getMaxStackSize() - itemstack.stackSize) {
            k = itemstack.getMaxStackSize() - itemstack.stackSize;
         }

         if(k > this.getInventoryStackLimit() - itemstack.stackSize) {
            k = this.getInventoryStackLimit() - itemstack.stackSize;
         }

         if(k == 0) {
            return i;
         } else {
            i = i - k;
            itemstack.stackSize += k;
            itemstack.animationsToGo = 5;
            return i;
         }
      }
   }

   private int storeItemStack(ItemStack itemStackIn) {
      if(this.canMergeStacks(this.getStackInSlot(this.currentItem), itemStackIn)) {
         return this.currentItem;
      } else if(this.canMergeStacks(this.getStackInSlot(40), itemStackIn)) {
         return 40;
      } else {
         for(int i = 0; i < this.mainInventory.length; ++i) {
            if(this.canMergeStacks(this.mainInventory[i], itemStackIn)) {
               return i;
            }
         }

         return -1;
      }
   }

   public void decrementAnimations() {
      for(int i = 0; i < this.allInventories.length; ++i) {
         ItemStack[] aitemstack = this.allInventories[i];

         for(int j = 0; j < aitemstack.length; ++j) {
            if(aitemstack[j] != null) {
               aitemstack[j].updateAnimation(this.player.worldObj, this.player, j, this.currentItem == j);
            }
         }
      }
   }

   public boolean addItemStackToInventory(@Nullable final ItemStack itemStackIn) {
      if(itemStackIn != null && itemStackIn.stackSize != 0 && itemStackIn.getItem() != null) {
         try {
            if(itemStackIn.isItemDamaged()) {
               int j = this.getFirstEmptyStack();
               if(j >= 0) {
                  this.mainInventory[j] = ItemStack.copyItemStack(itemStackIn);
                  this.mainInventory[j].animationsToGo = 5;
                  itemStackIn.stackSize = 0;
                  return true;
               } else if(this.player.capabilities.isCreativeMode) {
                  itemStackIn.stackSize = 0;
                  return true;
               } else {
                  return false;
               }
            } else {
               int i;
               while(true) {
                  i = itemStackIn.stackSize;
                  itemStackIn.stackSize = this.storePartialItemStack(itemStackIn);
                  if(itemStackIn.stackSize <= 0 || itemStackIn.stackSize >= i) {
                     break;
                  }
               }

               if(itemStackIn.stackSize == i && this.player.capabilities.isCreativeMode) {
                  itemStackIn.stackSize = 0;
                  return true;
               } else {
                  return itemStackIn.stackSize < i;
               }
            }
         } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Adding item to inventory");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Item being added");
            crashreportcategory.addCrashSection("Item ID", Integer.valueOf(Item.getIdFromItem(itemStackIn.getItem())));
            crashreportcategory.addCrashSection("Item data", Integer.valueOf(itemStackIn.getMetadata()));
            crashreportcategory.func_189529_a("Item name", new ICrashReportDetail<String>() {
               public String call() throws Exception {
                  return itemStackIn.getDisplayName();
               }
            });
            throw new ReportedException(crashreport);
         }
      } else {
         return false;
      }
   }

   @Nullable
   public ItemStack decrStackSize(int index, int count) {
      ItemStack[] aitemstack = null;

      for(ItemStack[] aitemstack1 : this.allInventories) {
         if(index < aitemstack1.length) {
            aitemstack = aitemstack1;
            break;
         }

         index -= aitemstack1.length;
      }

      return aitemstack != null && aitemstack[index] != null?ItemStackHelper.getAndSplit(aitemstack, index, count):null;
   }

   public void deleteStack(ItemStack stack) {
      for(ItemStack[] aitemstack : this.allInventories) {
         for(int i = 0; i < aitemstack.length; ++i) {
            if(aitemstack[i] == stack) {
               aitemstack[i] = null;
               break;
            }
         }
      }
   }

   @Nullable
   public ItemStack removeStackFromSlot(int index) {
      ItemStack[] aitemstack = null;

      for(ItemStack[] aitemstack1 : this.allInventories) {
         if(index < aitemstack1.length) {
            aitemstack = aitemstack1;
            break;
         }

         index -= aitemstack1.length;
      }

      if(aitemstack != null && aitemstack[index] != null) {
         ItemStack itemstack = aitemstack[index];
         aitemstack[index] = null;
         return itemstack;
      } else {
         return null;
      }
   }

   public void setInventorySlotContents(int index, @Nullable ItemStack stack) {
      ItemStack[] aitemstack = null;

      for(ItemStack[] aitemstack1 : this.allInventories) {
         if(index < aitemstack1.length) {
            aitemstack = aitemstack1;
            break;
         }

         index -= aitemstack1.length;
      }

      if(aitemstack != null) {
         aitemstack[index] = stack;
      }
   }

   public float getStrVsBlock(IBlockState state) {
      float f = 1.0F;
      if(this.mainInventory[this.currentItem] != null) {
         f *= this.mainInventory[this.currentItem].getStrVsBlock(state);
      }

      return f;
   }

   public NBTTagList writeToNBT(NBTTagList nbtTagListIn) {
      for(int i = 0; i < this.mainInventory.length; ++i) {
         if(this.mainInventory[i] != null) {
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            nbttagcompound.setByte("Slot", (byte)i);
            this.mainInventory[i].writeToNBT(nbttagcompound);
            nbtTagListIn.appendTag(nbttagcompound);
         }
      }

      for(int j = 0; j < this.armorInventory.length; ++j) {
         if(this.armorInventory[j] != null) {
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();
            nbttagcompound1.setByte("Slot", (byte)(j + 100));
            this.armorInventory[j].writeToNBT(nbttagcompound1);
            nbtTagListIn.appendTag(nbttagcompound1);
         }
      }

      for(int k = 0; k < this.offHandInventory.length; ++k) {
         if(this.offHandInventory[k] != null) {
            NBTTagCompound nbttagcompound2 = new NBTTagCompound();
            nbttagcompound2.setByte("Slot", (byte)(k + 150));
            this.offHandInventory[k].writeToNBT(nbttagcompound2);
            nbtTagListIn.appendTag(nbttagcompound2);
         }
      }

      return nbtTagListIn;
   }

   public void readFromNBT(NBTTagList nbtTagListIn) {
      Arrays.fill(this.mainInventory, (Object)null);
      Arrays.fill(this.armorInventory, (Object)null);
      Arrays.fill(this.offHandInventory, (Object)null);

      for(int i = 0; i < nbtTagListIn.tagCount(); ++i) {
         NBTTagCompound nbttagcompound = nbtTagListIn.getCompoundTagAt(i);
         int j = nbttagcompound.getByte("Slot") & 255;
         ItemStack itemstack = ItemStack.loadItemStackFromNBT(nbttagcompound);
         if(itemstack != null) {
            if(j >= 0 && j < this.mainInventory.length) {
               this.mainInventory[j] = itemstack;
            } else if(j >= 100 && j < this.armorInventory.length + 100) {
               this.armorInventory[j - 100] = itemstack;
            } else if(j >= 150 && j < this.offHandInventory.length + 150) {
               this.offHandInventory[j - 150] = itemstack;
            }
         }
      }
   }

   public int getSizeInventory() {
      return this.mainInventory.length + this.armorInventory.length + this.offHandInventory.length;
   }

   @Nullable
   public ItemStack getStackInSlot(int index) {
      ItemStack[] aitemstack = null;

      for(ItemStack[] aitemstack1 : this.allInventories) {
         if(index < aitemstack1.length) {
            aitemstack = aitemstack1;
            break;
         }

         index -= aitemstack1.length;
      }

      return aitemstack == null?null:aitemstack[index];
   }

   public String getName() {
      return "container.inventory";
   }

   public boolean hasCustomName() {
      return false;
   }

   public ITextComponent getDisplayName() {
      return (ITextComponent)(this.hasCustomName()?new TextComponentString(this.getName()):new TextComponentTranslation(this.getName(), new Object[0]));
   }

   public int getInventoryStackLimit() {
      return 64;
   }

   public boolean canHarvestBlock(IBlockState state) {
      if(state.getMaterial().isToolNotRequired()) {
         return true;
      } else {
         ItemStack itemstack = this.getStackInSlot(this.currentItem);
         return itemstack != null?itemstack.canHarvestBlock(state):false;
      }
   }

   public ItemStack armorItemInSlot(int slotIn) {
      return this.armorInventory[slotIn];
   }

   public void damageArmor(float damage) {
      damage = damage / 4.0F;
      if(damage < 1.0F) {
         damage = 1.0F;
      }

      for(int i = 0; i < this.armorInventory.length; ++i) {
         if(this.armorInventory[i] != null && this.armorInventory[i].getItem() instanceof ItemArmor) {
            this.armorInventory[i].damageItem((int)damage, this.player);
            if(this.armorInventory[i].stackSize == 0) {
               this.armorInventory[i] = null;
            }
         }
      }
   }

   public void dropAllItems() {
      for(ItemStack[] aitemstack : this.allInventories) {
         for(int i = 0; i < aitemstack.length; ++i) {
            if(aitemstack[i] != null) {
               this.player.dropItem(aitemstack[i], true, false);
               aitemstack[i] = null;
            }
         }
      }
   }

   public void markDirty() {
      this.inventoryChanged = true;
   }

   public void setItemStack(@Nullable ItemStack itemStackIn) {
      this.itemStack = itemStackIn;
   }

   @Nullable
   public ItemStack getItemStack() {
      return this.itemStack;
   }

   public boolean isUseableByPlayer(EntityPlayer player) {
      return this.player.isDead?false:player.getDistanceSqToEntity(this.player) <= 64.0D;
   }

   public boolean hasItemStack(ItemStack itemStackIn) {
      for(ItemStack[] aitemstack : this.allInventories) {
         for(int i = 0; i < aitemstack.length; ++i) {
            if(aitemstack[i] != null && aitemstack[i].isItemEqual(itemStackIn)) {
               return true;
            }
         }
      }

      return false;
   }

   public void openInventory(EntityPlayer player) {
   }

   public void closeInventory(EntityPlayer player) {
   }

   public boolean isItemValidForSlot(int index, ItemStack stack) {
      return true;
   }

   public void copyInventory(InventoryPlayer playerInventory) {
      for(int i = 0; i < this.getSizeInventory(); ++i) {
         this.setInventorySlotContents(i, playerInventory.getStackInSlot(i));
      }

      this.currentItem = playerInventory.currentItem;
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
      for(ItemStack[] aitemstack : this.allInventories) {
         for(int i = 0; i < aitemstack.length; ++i) {
            aitemstack[i] = null;
         }
      }
   }
}
