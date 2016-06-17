package net.minecraft.tileentity;

import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFurnace;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.SlotFurnaceFuel;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.MathHelper;

public class TileEntityFurnace extends TileEntityLockable implements ITickable, ISidedInventory {
   private static final int[] SLOTS_TOP = new int[]{0};
   private static final int[] SLOTS_BOTTOM = new int[]{2, 1};
   private static final int[] SLOTS_SIDES = new int[]{1};
   private ItemStack[] furnaceItemStacks = new ItemStack[3];
   private int furnaceBurnTime;
   private int currentItemBurnTime;
   private int cookTime;
   private int totalCookTime;
   private String furnaceCustomName;

   public int getSizeInventory() {
      return this.furnaceItemStacks.length;
   }

   @Nullable
   public ItemStack getStackInSlot(int index) {
      return this.furnaceItemStacks[index];
   }

   @Nullable
   public ItemStack decrStackSize(int index, int count) {
      return ItemStackHelper.getAndSplit(this.furnaceItemStacks, index, count);
   }

   @Nullable
   public ItemStack removeStackFromSlot(int index) {
      return ItemStackHelper.getAndRemove(this.furnaceItemStacks, index);
   }

   public void setInventorySlotContents(int index, @Nullable ItemStack stack) {
      boolean flag = stack != null && stack.isItemEqual(this.furnaceItemStacks[index]) && ItemStack.areItemStackTagsEqual(stack, this.furnaceItemStacks[index]);
      this.furnaceItemStacks[index] = stack;
      if(stack != null && stack.stackSize > this.getInventoryStackLimit()) {
         stack.stackSize = this.getInventoryStackLimit();
      }

      if(index == 0 && !flag) {
         this.totalCookTime = this.getCookTime(stack);
         this.cookTime = 0;
         this.markDirty();
      }
   }

   public String getName() {
      return this.hasCustomName()?this.furnaceCustomName:"container.furnace";
   }

   public boolean hasCustomName() {
      return this.furnaceCustomName != null && !this.furnaceCustomName.isEmpty();
   }

   public void setCustomInventoryName(String p_145951_1_) {
      this.furnaceCustomName = p_145951_1_;
   }

   public void readFromNBT(NBTTagCompound compound) {
      super.readFromNBT(compound);
      NBTTagList nbttaglist = compound.getTagList("Items", 10);
      this.furnaceItemStacks = new ItemStack[this.getSizeInventory()];

      for(int i = 0; i < nbttaglist.tagCount(); ++i) {
         NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
         int j = nbttagcompound.getByte("Slot");
         if(j >= 0 && j < this.furnaceItemStacks.length) {
            this.furnaceItemStacks[j] = ItemStack.loadItemStackFromNBT(nbttagcompound);
         }
      }

      this.furnaceBurnTime = compound.getShort("BurnTime");
      this.cookTime = compound.getShort("CookTime");
      this.totalCookTime = compound.getShort("CookTimeTotal");
      this.currentItemBurnTime = getItemBurnTime(this.furnaceItemStacks[1]);
      if(compound.hasKey("CustomName", 8)) {
         this.furnaceCustomName = compound.getString("CustomName");
      }
   }

   public NBTTagCompound func_189515_b(NBTTagCompound p_189515_1_) {
      super.func_189515_b(p_189515_1_);
      p_189515_1_.setShort("BurnTime", (short)this.furnaceBurnTime);
      p_189515_1_.setShort("CookTime", (short)this.cookTime);
      p_189515_1_.setShort("CookTimeTotal", (short)this.totalCookTime);
      NBTTagList nbttaglist = new NBTTagList();

      for(int i = 0; i < this.furnaceItemStacks.length; ++i) {
         if(this.furnaceItemStacks[i] != null) {
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            nbttagcompound.setByte("Slot", (byte)i);
            this.furnaceItemStacks[i].writeToNBT(nbttagcompound);
            nbttaglist.appendTag(nbttagcompound);
         }
      }

      p_189515_1_.setTag("Items", nbttaglist);
      if(this.hasCustomName()) {
         p_189515_1_.setString("CustomName", this.furnaceCustomName);
      }

      return p_189515_1_;
   }

   public int getInventoryStackLimit() {
      return 64;
   }

   public boolean isBurning() {
      return this.furnaceBurnTime > 0;
   }

   public static boolean isBurning(IInventory inventory) {
      return inventory.getField(0) > 0;
   }

   public void update() {
      boolean flag = this.isBurning();
      boolean flag1 = false;
      if(this.isBurning()) {
         --this.furnaceBurnTime;
      }

      if(!this.worldObj.isRemote) {
         if(this.isBurning() || this.furnaceItemStacks[1] != null && this.furnaceItemStacks[0] != null) {
            if(!this.isBurning() && this.canSmelt()) {
               this.currentItemBurnTime = this.furnaceBurnTime = getItemBurnTime(this.furnaceItemStacks[1]);
               if(this.isBurning()) {
                  flag1 = true;
                  if(this.furnaceItemStacks[1] != null) {
                     --this.furnaceItemStacks[1].stackSize;
                     if(this.furnaceItemStacks[1].stackSize == 0) {
                        Item item = this.furnaceItemStacks[1].getItem().getContainerItem();
                        this.furnaceItemStacks[1] = item != null?new ItemStack(item):null;
                     }
                  }
               }
            }

            if(this.isBurning() && this.canSmelt()) {
               ++this.cookTime;
               if(this.cookTime == this.totalCookTime) {
                  this.cookTime = 0;
                  this.totalCookTime = this.getCookTime(this.furnaceItemStacks[0]);
                  this.smeltItem();
                  flag1 = true;
               }
            } else {
               this.cookTime = 0;
            }
         } else if(!this.isBurning() && this.cookTime > 0) {
            this.cookTime = MathHelper.clamp_int(this.cookTime - 2, 0, this.totalCookTime);
         }

         if(flag != this.isBurning()) {
            flag1 = true;
            BlockFurnace.setState(this.isBurning(), this.worldObj, this.pos);
         }
      }

      if(flag1) {
         this.markDirty();
      }
   }

   public int getCookTime(@Nullable ItemStack stack) {
      return 200;
   }

   private boolean canSmelt() {
      if(this.furnaceItemStacks[0] == null) {
         return false;
      } else {
         ItemStack itemstack = FurnaceRecipes.instance().getSmeltingResult(this.furnaceItemStacks[0]);
         return itemstack == null?false:(this.furnaceItemStacks[2] == null?true:(!this.furnaceItemStacks[2].isItemEqual(itemstack)?false:(this.furnaceItemStacks[2].stackSize < this.getInventoryStackLimit() && this.furnaceItemStacks[2].stackSize < this.furnaceItemStacks[2].getMaxStackSize()?true:this.furnaceItemStacks[2].stackSize < itemstack.getMaxStackSize())));
      }
   }

   public void smeltItem() {
      if(this.canSmelt()) {
         ItemStack itemstack = FurnaceRecipes.instance().getSmeltingResult(this.furnaceItemStacks[0]);
         if(this.furnaceItemStacks[2] == null) {
            this.furnaceItemStacks[2] = itemstack.copy();
         } else if(this.furnaceItemStacks[2].getItem() == itemstack.getItem()) {
            ++this.furnaceItemStacks[2].stackSize;
         }

         if(this.furnaceItemStacks[0].getItem() == Item.getItemFromBlock(Blocks.SPONGE) && this.furnaceItemStacks[0].getMetadata() == 1 && this.furnaceItemStacks[1] != null && this.furnaceItemStacks[1].getItem() == Items.BUCKET) {
            this.furnaceItemStacks[1] = new ItemStack(Items.WATER_BUCKET);
         }

         --this.furnaceItemStacks[0].stackSize;
         if(this.furnaceItemStacks[0].stackSize <= 0) {
            this.furnaceItemStacks[0] = null;
         }
      }
   }

   public static int getItemBurnTime(ItemStack stack) {
      if(stack == null) {
         return 0;
      } else {
         Item item = stack.getItem();
         if(item instanceof ItemBlock && Block.getBlockFromItem(item) != Blocks.AIR) {
            Block block = Block.getBlockFromItem(item);
            if(block == Blocks.WOODEN_SLAB) {
               return 150;
            }

            if(block.getDefaultState().getMaterial() == Material.WOOD) {
               return 300;
            }

            if(block == Blocks.COAL_BLOCK) {
               return 16000;
            }
         }

         return item instanceof ItemTool && ((ItemTool)item).getToolMaterialName().equals("WOOD")?200:(item instanceof ItemSword && ((ItemSword)item).getToolMaterialName().equals("WOOD")?200:(item instanceof ItemHoe && ((ItemHoe)item).getMaterialName().equals("WOOD")?200:(item == Items.STICK?100:(item == Items.COAL?1600:(item == Items.LAVA_BUCKET?20000:(item == Item.getItemFromBlock(Blocks.SAPLING)?100:(item == Items.BLAZE_ROD?2400:0)))))));
      }
   }

   public static boolean isItemFuel(ItemStack stack) {
      return getItemBurnTime(stack) > 0;
   }

   public boolean isUseableByPlayer(EntityPlayer player) {
      return this.worldObj.getTileEntity(this.pos) != this?false:player.getDistanceSq((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
   }

   public void openInventory(EntityPlayer player) {
   }

   public void closeInventory(EntityPlayer player) {
   }

   public boolean isItemValidForSlot(int index, ItemStack stack) {
      if(index == 2) {
         return false;
      } else if(index != 1) {
         return true;
      } else {
         ItemStack itemstack = this.furnaceItemStacks[1];
         return isItemFuel(stack) || SlotFurnaceFuel.isBucket(stack) && (itemstack == null || itemstack.getItem() != Items.BUCKET);
      }
   }

   public int[] getSlotsForFace(EnumFacing side) {
      return side == EnumFacing.DOWN?SLOTS_BOTTOM:(side == EnumFacing.UP?SLOTS_TOP:SLOTS_SIDES);
   }

   public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
      return this.isItemValidForSlot(index, itemStackIn);
   }

   public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
      if(direction == EnumFacing.DOWN && index == 1) {
         Item item = stack.getItem();
         if(item != Items.WATER_BUCKET && item != Items.BUCKET) {
            return false;
         }
      }

      return true;
   }

   public String getGuiID() {
      return "minecraft:furnace";
   }

   public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
      return new ContainerFurnace(playerInventory, this);
   }

   public int getField(int id) {
      switch(id) {
      case 0:
         return this.furnaceBurnTime;
      case 1:
         return this.currentItemBurnTime;
      case 2:
         return this.cookTime;
      case 3:
         return this.totalCookTime;
      default:
         return 0;
      }
   }

   public void setField(int id, int value) {
      switch(id) {
      case 0:
         this.furnaceBurnTime = value;
         break;
      case 1:
         this.currentItemBurnTime = value;
         break;
      case 2:
         this.cookTime = value;
         break;
      case 3:
         this.totalCookTime = value;
      }
   }

   public int getFieldCount() {
      return 4;
   }

   public void clear() {
      for(int i = 0; i < this.furnaceItemStacks.length; ++i) {
         this.furnaceItemStacks[i] = null;
      }
   }
}
