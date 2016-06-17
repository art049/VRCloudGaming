package net.minecraft.tileentity;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockHopper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerHopper;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class TileEntityHopper extends TileEntityLockableLoot implements IHopper, ITickable {
   private ItemStack[] inventory = new ItemStack[5];
   private String customName;
   private int transferCooldown = -1;

   public void readFromNBT(NBTTagCompound compound) {
      super.readFromNBT(compound);
      this.inventory = new ItemStack[this.getSizeInventory()];
      if(compound.hasKey("CustomName", 8)) {
         this.customName = compound.getString("CustomName");
      }

      this.transferCooldown = compound.getInteger("TransferCooldown");
      if(!this.checkLootAndRead(compound)) {
         NBTTagList nbttaglist = compound.getTagList("Items", 10);

         for(int i = 0; i < nbttaglist.tagCount(); ++i) {
            NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
            int j = nbttagcompound.getByte("Slot");
            if(j >= 0 && j < this.inventory.length) {
               this.inventory[j] = ItemStack.loadItemStackFromNBT(nbttagcompound);
            }
         }
      }
   }

   public NBTTagCompound func_189515_b(NBTTagCompound p_189515_1_) {
      super.func_189515_b(p_189515_1_);
      if(!this.checkLootAndWrite(p_189515_1_)) {
         NBTTagList nbttaglist = new NBTTagList();

         for(int i = 0; i < this.inventory.length; ++i) {
            if(this.inventory[i] != null) {
               NBTTagCompound nbttagcompound = new NBTTagCompound();
               nbttagcompound.setByte("Slot", (byte)i);
               this.inventory[i].writeToNBT(nbttagcompound);
               nbttaglist.appendTag(nbttagcompound);
            }
         }

         p_189515_1_.setTag("Items", nbttaglist);
      }

      p_189515_1_.setInteger("TransferCooldown", this.transferCooldown);
      if(this.hasCustomName()) {
         p_189515_1_.setString("CustomName", this.customName);
      }

      return p_189515_1_;
   }

   public int getSizeInventory() {
      return this.inventory.length;
   }

   @Nullable
   public ItemStack getStackInSlot(int index) {
      this.fillWithLoot((EntityPlayer)null);
      return this.inventory[index];
   }

   @Nullable
   public ItemStack decrStackSize(int index, int count) {
      this.fillWithLoot((EntityPlayer)null);
      return ItemStackHelper.getAndSplit(this.inventory, index, count);
   }

   @Nullable
   public ItemStack removeStackFromSlot(int index) {
      this.fillWithLoot((EntityPlayer)null);
      return ItemStackHelper.getAndRemove(this.inventory, index);
   }

   public void setInventorySlotContents(int index, @Nullable ItemStack stack) {
      this.fillWithLoot((EntityPlayer)null);
      this.inventory[index] = stack;
      if(stack != null && stack.stackSize > this.getInventoryStackLimit()) {
         stack.stackSize = this.getInventoryStackLimit();
      }
   }

   public String getName() {
      return this.hasCustomName()?this.customName:"container.hopper";
   }

   public boolean hasCustomName() {
      return this.customName != null && !this.customName.isEmpty();
   }

   public void setCustomName(String customNameIn) {
      this.customName = customNameIn;
   }

   public int getInventoryStackLimit() {
      return 64;
   }

   public boolean isUseableByPlayer(EntityPlayer player) {
      return this.worldObj.getTileEntity(this.pos) != this?false:player.getDistanceSq((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
   }

   public void openInventory(EntityPlayer player) {
   }

   public void closeInventory(EntityPlayer player) {
   }

   public boolean isItemValidForSlot(int index, ItemStack stack) {
      return true;
   }

   public void update() {
      if(this.worldObj != null && !this.worldObj.isRemote) {
         --this.transferCooldown;
         if(!this.isOnTransferCooldown()) {
            this.setTransferCooldown(0);
            this.updateHopper();
         }
      }
   }

   public boolean updateHopper() {
      if(this.worldObj != null && !this.worldObj.isRemote) {
         if(!this.isOnTransferCooldown() && BlockHopper.isEnabled(this.getBlockMetadata())) {
            boolean flag = false;
            if(!this.isEmpty()) {
               flag = this.transferItemsOut();
            }

            if(!this.isFull()) {
               flag = captureDroppedItems(this) || flag;
            }

            if(flag) {
               this.setTransferCooldown(8);
               this.markDirty();
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private boolean isEmpty() {
      for(ItemStack itemstack : this.inventory) {
         if(itemstack != null) {
            return false;
         }
      }

      return true;
   }

   private boolean isFull() {
      for(ItemStack itemstack : this.inventory) {
         if(itemstack == null || itemstack.stackSize != itemstack.getMaxStackSize()) {
            return false;
         }
      }

      return true;
   }

   private boolean transferItemsOut() {
      IInventory iinventory = this.getInventoryForHopperTransfer();
      if(iinventory == null) {
         return false;
      } else {
         EnumFacing enumfacing = BlockHopper.getFacing(this.getBlockMetadata()).getOpposite();
         if(this.isInventoryFull(iinventory, enumfacing)) {
            return false;
         } else {
            for(int i = 0; i < this.getSizeInventory(); ++i) {
               if(this.getStackInSlot(i) != null) {
                  ItemStack itemstack = this.getStackInSlot(i).copy();
                  ItemStack itemstack1 = putStackInInventoryAllSlots(iinventory, this.decrStackSize(i, 1), enumfacing);
                  if(itemstack1 == null || itemstack1.stackSize == 0) {
                     iinventory.markDirty();
                     return true;
                  }

                  this.setInventorySlotContents(i, itemstack);
               }
            }

            return false;
         }
      }
   }

   private boolean isInventoryFull(IInventory inventoryIn, EnumFacing side) {
      if(inventoryIn instanceof ISidedInventory) {
         ISidedInventory isidedinventory = (ISidedInventory)inventoryIn;
         int[] aint = isidedinventory.getSlotsForFace(side);

         for(int k = 0; k < aint.length; ++k) {
            ItemStack itemstack1 = isidedinventory.getStackInSlot(aint[k]);
            if(itemstack1 == null || itemstack1.stackSize != itemstack1.getMaxStackSize()) {
               return false;
            }
         }
      } else {
         int i = inventoryIn.getSizeInventory();

         for(int j = 0; j < i; ++j) {
            ItemStack itemstack = inventoryIn.getStackInSlot(j);
            if(itemstack == null || itemstack.stackSize != itemstack.getMaxStackSize()) {
               return false;
            }
         }
      }

      return true;
   }

   private static boolean isInventoryEmpty(IInventory inventoryIn, EnumFacing side) {
      if(inventoryIn instanceof ISidedInventory) {
         ISidedInventory isidedinventory = (ISidedInventory)inventoryIn;
         int[] aint = isidedinventory.getSlotsForFace(side);

         for(int i = 0; i < aint.length; ++i) {
            if(isidedinventory.getStackInSlot(aint[i]) != null) {
               return false;
            }
         }
      } else {
         int j = inventoryIn.getSizeInventory();

         for(int k = 0; k < j; ++k) {
            if(inventoryIn.getStackInSlot(k) != null) {
               return false;
            }
         }
      }

      return true;
   }

   public static boolean captureDroppedItems(IHopper hopper) {
      IInventory iinventory = getHopperInventory(hopper);
      if(iinventory != null) {
         EnumFacing enumfacing = EnumFacing.DOWN;
         if(isInventoryEmpty(iinventory, enumfacing)) {
            return false;
         }

         if(iinventory instanceof ISidedInventory) {
            ISidedInventory isidedinventory = (ISidedInventory)iinventory;
            int[] aint = isidedinventory.getSlotsForFace(enumfacing);

            for(int i = 0; i < aint.length; ++i) {
               if(pullItemFromSlot(hopper, iinventory, aint[i], enumfacing)) {
                  return true;
               }
            }
         } else {
            int j = iinventory.getSizeInventory();

            for(int k = 0; k < j; ++k) {
               if(pullItemFromSlot(hopper, iinventory, k, enumfacing)) {
                  return true;
               }
            }
         }
      } else {
         for(EntityItem entityitem : getCaptureItems(hopper.getWorld(), hopper.getXPos(), hopper.getYPos(), hopper.getZPos())) {
            if(putDropInInventoryAllSlots(hopper, entityitem)) {
               return true;
            }
         }
      }

      return false;
   }

   private static boolean pullItemFromSlot(IHopper hopper, IInventory inventoryIn, int index, EnumFacing direction) {
      ItemStack itemstack = inventoryIn.getStackInSlot(index);
      if(itemstack != null && canExtractItemFromSlot(inventoryIn, itemstack, index, direction)) {
         ItemStack itemstack1 = itemstack.copy();
         ItemStack itemstack2 = putStackInInventoryAllSlots(hopper, inventoryIn.decrStackSize(index, 1), (EnumFacing)null);
         if(itemstack2 == null || itemstack2.stackSize == 0) {
            inventoryIn.markDirty();
            return true;
         }

         inventoryIn.setInventorySlotContents(index, itemstack1);
      }

      return false;
   }

   public static boolean putDropInInventoryAllSlots(IInventory p_145898_0_, EntityItem itemIn) {
      boolean flag = false;
      if(itemIn == null) {
         return false;
      } else {
         ItemStack itemstack = itemIn.getEntityItem().copy();
         ItemStack itemstack1 = putStackInInventoryAllSlots(p_145898_0_, itemstack, (EnumFacing)null);
         if(itemstack1 != null && itemstack1.stackSize != 0) {
            itemIn.setEntityItemStack(itemstack1);
         } else {
            flag = true;
            itemIn.setDead();
         }

         return flag;
      }
   }

   public static ItemStack putStackInInventoryAllSlots(IInventory inventoryIn, ItemStack stack, @Nullable EnumFacing side) {
      if(inventoryIn instanceof ISidedInventory && side != null) {
         ISidedInventory isidedinventory = (ISidedInventory)inventoryIn;
         int[] aint = isidedinventory.getSlotsForFace(side);

         for(int k = 0; k < aint.length && stack != null && stack.stackSize > 0; ++k) {
            stack = insertStack(inventoryIn, stack, aint[k], side);
         }
      } else {
         int i = inventoryIn.getSizeInventory();

         for(int j = 0; j < i && stack != null && stack.stackSize > 0; ++j) {
            stack = insertStack(inventoryIn, stack, j, side);
         }
      }

      if(stack != null && stack.stackSize == 0) {
         stack = null;
      }

      return stack;
   }

   private static boolean canInsertItemInSlot(IInventory inventoryIn, ItemStack stack, int index, EnumFacing side) {
      return !inventoryIn.isItemValidForSlot(index, stack)?false:!(inventoryIn instanceof ISidedInventory) || ((ISidedInventory)inventoryIn).canInsertItem(index, stack, side);
   }

   private static boolean canExtractItemFromSlot(IInventory inventoryIn, ItemStack stack, int index, EnumFacing side) {
      return !(inventoryIn instanceof ISidedInventory) || ((ISidedInventory)inventoryIn).canExtractItem(index, stack, side);
   }

   private static ItemStack insertStack(IInventory inventoryIn, ItemStack stack, int index, EnumFacing side) {
      ItemStack itemstack = inventoryIn.getStackInSlot(index);
      if(canInsertItemInSlot(inventoryIn, stack, index, side)) {
         boolean flag = false;
         if(itemstack == null) {
            inventoryIn.setInventorySlotContents(index, stack);
            stack = null;
            flag = true;
         } else if(canCombine(itemstack, stack)) {
            int i = stack.getMaxStackSize() - itemstack.stackSize;
            int j = Math.min(stack.stackSize, i);
            stack.stackSize -= j;
            itemstack.stackSize += j;
            flag = j > 0;
         }

         if(flag) {
            if(inventoryIn instanceof TileEntityHopper) {
               TileEntityHopper tileentityhopper = (TileEntityHopper)inventoryIn;
               if(tileentityhopper.mayTransfer()) {
                  tileentityhopper.setTransferCooldown(8);
               }

               inventoryIn.markDirty();
            }

            inventoryIn.markDirty();
         }
      }

      return stack;
   }

   private IInventory getInventoryForHopperTransfer() {
      EnumFacing enumfacing = BlockHopper.getFacing(this.getBlockMetadata());
      return getInventoryAtPosition(this.getWorld(), this.getXPos() + (double)enumfacing.getFrontOffsetX(), this.getYPos() + (double)enumfacing.getFrontOffsetY(), this.getZPos() + (double)enumfacing.getFrontOffsetZ());
   }

   public static IInventory getHopperInventory(IHopper hopper) {
      return getInventoryAtPosition(hopper.getWorld(), hopper.getXPos(), hopper.getYPos() + 1.0D, hopper.getZPos());
   }

   public static List<EntityItem> getCaptureItems(World worldIn, double p_184292_1_, double p_184292_3_, double p_184292_5_) {
      return worldIn.<EntityItem>getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(p_184292_1_ - 0.5D, p_184292_3_, p_184292_5_ - 0.5D, p_184292_1_ + 0.5D, p_184292_3_ + 1.5D, p_184292_5_ + 0.5D), EntitySelectors.IS_ALIVE);
   }

   public static IInventory getInventoryAtPosition(World worldIn, double x, double y, double z) {
      IInventory iinventory = null;
      int i = MathHelper.floor_double(x);
      int j = MathHelper.floor_double(y);
      int k = MathHelper.floor_double(z);
      BlockPos blockpos = new BlockPos(i, j, k);
      Block block = worldIn.getBlockState(blockpos).getBlock();
      if(block.hasTileEntity()) {
         TileEntity tileentity = worldIn.getTileEntity(blockpos);
         if(tileentity instanceof IInventory) {
            iinventory = (IInventory)tileentity;
            if(iinventory instanceof TileEntityChest && block instanceof BlockChest) {
               iinventory = ((BlockChest)block).func_189418_a(worldIn, blockpos, true);
            }
         }
      }

      if(iinventory == null) {
         List<Entity> list = worldIn.getEntitiesInAABBexcluding((Entity)null, new AxisAlignedBB(x - 0.5D, y - 0.5D, z - 0.5D, x + 0.5D, y + 0.5D, z + 0.5D), EntitySelectors.HAS_INVENTORY);
         if(!list.isEmpty()) {
            iinventory = (IInventory)list.get(worldIn.rand.nextInt(list.size()));
         }
      }

      return iinventory;
   }

   private static boolean canCombine(ItemStack stack1, ItemStack stack2) {
      return stack1.getItem() != stack2.getItem()?false:(stack1.getMetadata() != stack2.getMetadata()?false:(stack1.stackSize > stack1.getMaxStackSize()?false:ItemStack.areItemStackTagsEqual(stack1, stack2)));
   }

   public double getXPos() {
      return (double)this.pos.getX() + 0.5D;
   }

   public double getYPos() {
      return (double)this.pos.getY() + 0.5D;
   }

   public double getZPos() {
      return (double)this.pos.getZ() + 0.5D;
   }

   public void setTransferCooldown(int ticks) {
      this.transferCooldown = ticks;
   }

   public boolean isOnTransferCooldown() {
      return this.transferCooldown > 0;
   }

   public boolean mayTransfer() {
      return this.transferCooldown <= 1;
   }

   public String getGuiID() {
      return "minecraft:hopper";
   }

   public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
      this.fillWithLoot(playerIn);
      return new ContainerHopper(playerInventory, this, playerIn);
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
      this.fillWithLoot((EntityPlayer)null);

      for(int i = 0; i < this.inventory.length; ++i) {
         this.inventory[i] = null;
      }
   }
}
