package net.minecraft.tileentity;

import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class TileEntityChest extends TileEntityLockableLoot implements ITickable, IInventory {
   private ItemStack[] chestContents = new ItemStack[27];
   public boolean adjacentChestChecked;
   public TileEntityChest adjacentChestZNeg;
   public TileEntityChest adjacentChestXPos;
   public TileEntityChest adjacentChestXNeg;
   public TileEntityChest adjacentChestZPos;
   public float lidAngle;
   public float prevLidAngle;
   public int numPlayersUsing;
   private int ticksSinceSync;
   private BlockChest.Type cachedChestType;
   private String customName;

   public TileEntityChest() {
   }

   public TileEntityChest(BlockChest.Type typeIn) {
      this.cachedChestType = typeIn;
   }

   public int getSizeInventory() {
      return 27;
   }

   @Nullable
   public ItemStack getStackInSlot(int index) {
      this.fillWithLoot((EntityPlayer)null);
      return this.chestContents[index];
   }

   @Nullable
   public ItemStack decrStackSize(int index, int count) {
      this.fillWithLoot((EntityPlayer)null);
      ItemStack itemstack = ItemStackHelper.getAndSplit(this.chestContents, index, count);
      if(itemstack != null) {
         this.markDirty();
      }

      return itemstack;
   }

   @Nullable
   public ItemStack removeStackFromSlot(int index) {
      this.fillWithLoot((EntityPlayer)null);
      return ItemStackHelper.getAndRemove(this.chestContents, index);
   }

   public void setInventorySlotContents(int index, @Nullable ItemStack stack) {
      this.fillWithLoot((EntityPlayer)null);
      this.chestContents[index] = stack;
      if(stack != null && stack.stackSize > this.getInventoryStackLimit()) {
         stack.stackSize = this.getInventoryStackLimit();
      }

      this.markDirty();
   }

   public String getName() {
      return this.hasCustomName()?this.customName:"container.chest";
   }

   public boolean hasCustomName() {
      return this.customName != null && !this.customName.isEmpty();
   }

   public void setCustomName(String name) {
      this.customName = name;
   }

   public void readFromNBT(NBTTagCompound compound) {
      super.readFromNBT(compound);
      this.chestContents = new ItemStack[this.getSizeInventory()];
      if(compound.hasKey("CustomName", 8)) {
         this.customName = compound.getString("CustomName");
      }

      if(!this.checkLootAndRead(compound)) {
         NBTTagList nbttaglist = compound.getTagList("Items", 10);

         for(int i = 0; i < nbttaglist.tagCount(); ++i) {
            NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
            int j = nbttagcompound.getByte("Slot") & 255;
            if(j >= 0 && j < this.chestContents.length) {
               this.chestContents[j] = ItemStack.loadItemStackFromNBT(nbttagcompound);
            }
         }
      }
   }

   public NBTTagCompound func_189515_b(NBTTagCompound p_189515_1_) {
      super.func_189515_b(p_189515_1_);
      if(!this.checkLootAndWrite(p_189515_1_)) {
         NBTTagList nbttaglist = new NBTTagList();

         for(int i = 0; i < this.chestContents.length; ++i) {
            if(this.chestContents[i] != null) {
               NBTTagCompound nbttagcompound = new NBTTagCompound();
               nbttagcompound.setByte("Slot", (byte)i);
               this.chestContents[i].writeToNBT(nbttagcompound);
               nbttaglist.appendTag(nbttagcompound);
            }
         }

         p_189515_1_.setTag("Items", nbttaglist);
      }

      if(this.hasCustomName()) {
         p_189515_1_.setString("CustomName", this.customName);
      }

      return p_189515_1_;
   }

   public int getInventoryStackLimit() {
      return 64;
   }

   public boolean isUseableByPlayer(EntityPlayer player) {
      return this.worldObj.getTileEntity(this.pos) != this?false:player.getDistanceSq((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
   }

   public void updateContainingBlockInfo() {
      super.updateContainingBlockInfo();
      this.adjacentChestChecked = false;
   }

   @SuppressWarnings("incomplete-switch")
   private void setNeighbor(TileEntityChest chestTe, EnumFacing side) {
      if(chestTe.isInvalid()) {
         this.adjacentChestChecked = false;
      } else if(this.adjacentChestChecked) {
         switch(side) {
         case NORTH:
            if(this.adjacentChestZNeg != chestTe) {
               this.adjacentChestChecked = false;
            }
            break;
         case SOUTH:
            if(this.adjacentChestZPos != chestTe) {
               this.adjacentChestChecked = false;
            }
            break;
         case EAST:
            if(this.adjacentChestXPos != chestTe) {
               this.adjacentChestChecked = false;
            }
            break;
         case WEST:
            if(this.adjacentChestXNeg != chestTe) {
               this.adjacentChestChecked = false;
            }
         }
      }
   }

   public void checkForAdjacentChests() {
      if(!this.adjacentChestChecked) {
         this.adjacentChestChecked = true;
         this.adjacentChestXNeg = this.getAdjacentChest(EnumFacing.WEST);
         this.adjacentChestXPos = this.getAdjacentChest(EnumFacing.EAST);
         this.adjacentChestZNeg = this.getAdjacentChest(EnumFacing.NORTH);
         this.adjacentChestZPos = this.getAdjacentChest(EnumFacing.SOUTH);
      }
   }

   @Nullable
   protected TileEntityChest getAdjacentChest(EnumFacing side) {
      BlockPos blockpos = this.pos.offset(side);
      if(this.isChestAt(blockpos)) {
         TileEntity tileentity = this.worldObj.getTileEntity(blockpos);
         if(tileentity instanceof TileEntityChest) {
            TileEntityChest tileentitychest = (TileEntityChest)tileentity;
            tileentitychest.setNeighbor(this, side.getOpposite());
            return tileentitychest;
         }
      }

      return null;
   }

   private boolean isChestAt(BlockPos posIn) {
      if(this.worldObj == null) {
         return false;
      } else {
         Block block = this.worldObj.getBlockState(posIn).getBlock();
         return block instanceof BlockChest && ((BlockChest)block).chestType == this.getChestType();
      }
   }

   public void update() {
      this.checkForAdjacentChests();
      int i = this.pos.getX();
      int j = this.pos.getY();
      int k = this.pos.getZ();
      ++this.ticksSinceSync;
      if(!this.worldObj.isRemote && this.numPlayersUsing != 0 && (this.ticksSinceSync + i + j + k) % 200 == 0) {
         this.numPlayersUsing = 0;
         float f = 5.0F;

         for(EntityPlayer entityplayer : this.worldObj.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB((double)((float)i - f), (double)((float)j - f), (double)((float)k - f), (double)((float)(i + 1) + f), (double)((float)(j + 1) + f), (double)((float)(k + 1) + f)))) {
            if(entityplayer.openContainer instanceof ContainerChest) {
               IInventory iinventory = ((ContainerChest)entityplayer.openContainer).getLowerChestInventory();
               if(iinventory == this || iinventory instanceof InventoryLargeChest && ((InventoryLargeChest)iinventory).isPartOfLargeChest(this)) {
                  ++this.numPlayersUsing;
               }
            }
         }
      }

      this.prevLidAngle = this.lidAngle;
      float f1 = 0.1F;
      if(this.numPlayersUsing > 0 && this.lidAngle == 0.0F && this.adjacentChestZNeg == null && this.adjacentChestXNeg == null) {
         double d1 = (double)i + 0.5D;
         double d2 = (double)k + 0.5D;
         if(this.adjacentChestZPos != null) {
            d2 += 0.5D;
         }

         if(this.adjacentChestXPos != null) {
            d1 += 0.5D;
         }

         this.worldObj.playSound((EntityPlayer)null, d1, (double)j + 0.5D, d2, SoundEvents.BLOCK_CHEST_OPEN, SoundCategory.BLOCKS, 0.5F, this.worldObj.rand.nextFloat() * 0.1F + 0.9F);
      }

      if(this.numPlayersUsing == 0 && this.lidAngle > 0.0F || this.numPlayersUsing > 0 && this.lidAngle < 1.0F) {
         float f2 = this.lidAngle;
         if(this.numPlayersUsing > 0) {
            this.lidAngle += f1;
         } else {
            this.lidAngle -= f1;
         }

         if(this.lidAngle > 1.0F) {
            this.lidAngle = 1.0F;
         }

         float f3 = 0.5F;
         if(this.lidAngle < f3 && f2 >= f3 && this.adjacentChestZNeg == null && this.adjacentChestXNeg == null) {
            double d3 = (double)i + 0.5D;
            double d0 = (double)k + 0.5D;
            if(this.adjacentChestZPos != null) {
               d0 += 0.5D;
            }

            if(this.adjacentChestXPos != null) {
               d3 += 0.5D;
            }

            this.worldObj.playSound((EntityPlayer)null, d3, (double)j + 0.5D, d0, SoundEvents.BLOCK_CHEST_CLOSE, SoundCategory.BLOCKS, 0.5F, this.worldObj.rand.nextFloat() * 0.1F + 0.9F);
         }

         if(this.lidAngle < 0.0F) {
            this.lidAngle = 0.0F;
         }
      }
   }

   public boolean receiveClientEvent(int id, int type) {
      if(id == 1) {
         this.numPlayersUsing = type;
         return true;
      } else {
         return super.receiveClientEvent(id, type);
      }
   }

   public void openInventory(EntityPlayer player) {
      if(!player.isSpectator()) {
         if(this.numPlayersUsing < 0) {
            this.numPlayersUsing = 0;
         }

         ++this.numPlayersUsing;
         this.worldObj.addBlockEvent(this.pos, this.getBlockType(), 1, this.numPlayersUsing);
         this.worldObj.notifyNeighborsOfStateChange(this.pos, this.getBlockType());
         this.worldObj.notifyNeighborsOfStateChange(this.pos.down(), this.getBlockType());
      }
   }

   public void closeInventory(EntityPlayer player) {
      if(!player.isSpectator() && this.getBlockType() instanceof BlockChest) {
         --this.numPlayersUsing;
         this.worldObj.addBlockEvent(this.pos, this.getBlockType(), 1, this.numPlayersUsing);
         this.worldObj.notifyNeighborsOfStateChange(this.pos, this.getBlockType());
         this.worldObj.notifyNeighborsOfStateChange(this.pos.down(), this.getBlockType());
      }
   }

   public boolean isItemValidForSlot(int index, ItemStack stack) {
      return true;
   }

   public void invalidate() {
      super.invalidate();
      this.updateContainingBlockInfo();
      this.checkForAdjacentChests();
   }

   public BlockChest.Type getChestType() {
      if(this.cachedChestType == null) {
         if(this.worldObj == null || !(this.getBlockType() instanceof BlockChest)) {
            return BlockChest.Type.BASIC;
         }

         this.cachedChestType = ((BlockChest)this.getBlockType()).chestType;
      }

      return this.cachedChestType;
   }

   public String getGuiID() {
      return "minecraft:chest";
   }

   public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
      this.fillWithLoot(playerIn);
      return new ContainerChest(playerInventory, this, playerIn);
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

      for(int i = 0; i < this.chestContents.length; ++i) {
         this.chestContents[i] = null;
      }
   }
}
