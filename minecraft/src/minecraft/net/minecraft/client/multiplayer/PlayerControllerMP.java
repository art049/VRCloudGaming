package net.minecraft.client.multiplayer;

import io.netty.buffer.Unpooled;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCommandBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketClickWindow;
import net.minecraft.network.play.client.CPacketCreativeInventoryAction;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.network.play.client.CPacketEnchantItem;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.stats.StatisticsManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;

public class PlayerControllerMP {
   private final Minecraft mc;
   private final NetHandlerPlayClient connection;
   private BlockPos currentBlock = new BlockPos(-1, -1, -1);
   private ItemStack currentItemHittingBlock;
   private float curBlockDamageMP;
   private float stepSoundTickCounter;
   private int blockHitDelay;
   private boolean isHittingBlock;
   private WorldSettings.GameType currentGameType = WorldSettings.GameType.SURVIVAL;
   private int currentPlayerItem;

   public PlayerControllerMP(Minecraft mcIn, NetHandlerPlayClient netHandler) {
      this.mc = mcIn;
      this.connection = netHandler;
   }

   public static void clickBlockCreative(Minecraft mcIn, PlayerControllerMP playerController, BlockPos pos, EnumFacing facing) {
      if(!mcIn.theWorld.extinguishFire(mcIn.thePlayer, pos, facing)) {
         playerController.onPlayerDestroyBlock(pos);
      }
   }

   public void setPlayerCapabilities(EntityPlayer player) {
      this.currentGameType.configurePlayerCapabilities(player.capabilities);
   }

   public boolean isSpectator() {
      return this.currentGameType == WorldSettings.GameType.SPECTATOR;
   }

   public void setGameType(WorldSettings.GameType type) {
      this.currentGameType = type;
      this.currentGameType.configurePlayerCapabilities(this.mc.thePlayer.capabilities);
   }

   public void flipPlayer(EntityPlayer playerIn) {
      playerIn.rotationYaw = -180.0F;
   }

   public boolean shouldDrawHUD() {
      return this.currentGameType.isSurvivalOrAdventure();
   }

   public boolean onPlayerDestroyBlock(BlockPos pos) {
      if(this.currentGameType.isAdventure()) {
         if(this.currentGameType == WorldSettings.GameType.SPECTATOR) {
            return false;
         }

         if(!this.mc.thePlayer.isAllowEdit()) {
            ItemStack itemstack = this.mc.thePlayer.getHeldItemMainhand();
            if(itemstack == null) {
               return false;
            }

            if(!itemstack.canDestroy(this.mc.theWorld.getBlockState(pos).getBlock())) {
               return false;
            }
         }
      }

      if(this.currentGameType.isCreative() && this.mc.thePlayer.getHeldItemMainhand() != null && this.mc.thePlayer.getHeldItemMainhand().getItem() instanceof ItemSword) {
         return false;
      } else {
         World world = this.mc.theWorld;
         IBlockState iblockstate = world.getBlockState(pos);
         Block block = iblockstate.getBlock();
         if(block instanceof BlockCommandBlock && !this.mc.thePlayer.canCommandSenderUseCommand(2, "")) {
            return false;
         } else if(iblockstate.getMaterial() == Material.AIR) {
            return false;
         } else {
            world.playEvent(2001, pos, Block.getStateId(iblockstate));
            block.onBlockHarvested(world, pos, iblockstate, this.mc.thePlayer);
            boolean flag = world.setBlockState(pos, Blocks.AIR.getDefaultState(), 11);
            if(flag) {
               block.onBlockDestroyedByPlayer(world, pos, iblockstate);
            }

            this.currentBlock = new BlockPos(this.currentBlock.getX(), -1, this.currentBlock.getZ());
            if(!this.currentGameType.isCreative()) {
               ItemStack itemstack1 = this.mc.thePlayer.getHeldItemMainhand();
               if(itemstack1 != null) {
                  itemstack1.onBlockDestroyed(world, iblockstate, pos, this.mc.thePlayer);
                  if(itemstack1.stackSize == 0) {
                     this.mc.thePlayer.setHeldItem(EnumHand.MAIN_HAND, (ItemStack)null);
                  }
               }
            }

            return flag;
         }
      }
   }

   public boolean clickBlock(BlockPos loc, EnumFacing face) {
      if(this.currentGameType.isAdventure()) {
         if(this.currentGameType == WorldSettings.GameType.SPECTATOR) {
            return false;
         }

         if(!this.mc.thePlayer.isAllowEdit()) {
            ItemStack itemstack = this.mc.thePlayer.getHeldItemMainhand();
            if(itemstack == null) {
               return false;
            }

            if(!itemstack.canDestroy(this.mc.theWorld.getBlockState(loc).getBlock())) {
               return false;
            }
         }
      }

      if(!this.mc.theWorld.getWorldBorder().contains(loc)) {
         return false;
      } else {
         if(this.currentGameType.isCreative()) {
            this.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, loc, face));
            clickBlockCreative(this.mc, this, loc, face);
            this.blockHitDelay = 5;
         } else if(!this.isHittingBlock || !this.isHittingPosition(loc)) {
            if(this.isHittingBlock) {
               this.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, this.currentBlock, face));
            }

            this.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, loc, face));
            IBlockState iblockstate = this.mc.theWorld.getBlockState(loc);
            boolean flag = iblockstate.getMaterial() != Material.AIR;
            if(flag && this.curBlockDamageMP == 0.0F) {
               iblockstate.getBlock().onBlockClicked(this.mc.theWorld, loc, this.mc.thePlayer);
            }

            if(flag && iblockstate.getPlayerRelativeBlockHardness(this.mc.thePlayer, this.mc.thePlayer.worldObj, loc) >= 1.0F) {
               this.onPlayerDestroyBlock(loc);
            } else {
               this.isHittingBlock = true;
               this.currentBlock = loc;
               this.currentItemHittingBlock = this.mc.thePlayer.getHeldItemMainhand();
               this.curBlockDamageMP = 0.0F;
               this.stepSoundTickCounter = 0.0F;
               this.mc.theWorld.sendBlockBreakProgress(this.mc.thePlayer.getEntityId(), this.currentBlock, (int)(this.curBlockDamageMP * 10.0F) - 1);
            }
         }

         return true;
      }
   }

   public void resetBlockRemoving() {
      if(this.isHittingBlock) {
         this.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, this.currentBlock, EnumFacing.DOWN));
         this.isHittingBlock = false;
         this.curBlockDamageMP = 0.0F;
         this.mc.theWorld.sendBlockBreakProgress(this.mc.thePlayer.getEntityId(), this.currentBlock, -1);
         this.mc.thePlayer.resetCooldown();
      }
   }

   public boolean onPlayerDamageBlock(BlockPos posBlock, EnumFacing directionFacing) {
      this.syncCurrentPlayItem();
      if(this.blockHitDelay > 0) {
         --this.blockHitDelay;
         return true;
      } else if(this.currentGameType.isCreative() && this.mc.theWorld.getWorldBorder().contains(posBlock)) {
         this.blockHitDelay = 5;
         this.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, posBlock, directionFacing));
         clickBlockCreative(this.mc, this, posBlock, directionFacing);
         return true;
      } else if(this.isHittingPosition(posBlock)) {
         IBlockState iblockstate = this.mc.theWorld.getBlockState(posBlock);
         Block block = iblockstate.getBlock();
         if(iblockstate.getMaterial() == Material.AIR) {
            this.isHittingBlock = false;
            return false;
         } else {
            this.curBlockDamageMP += iblockstate.getPlayerRelativeBlockHardness(this.mc.thePlayer, this.mc.thePlayer.worldObj, posBlock);
            if(this.stepSoundTickCounter % 4.0F == 0.0F) {
               SoundType soundtype = block.getSoundType();
               this.mc.getSoundHandler().playSound(new PositionedSoundRecord(soundtype.getHitSound(), SoundCategory.NEUTRAL, (soundtype.getVolume() + 1.0F) / 8.0F, soundtype.getPitch() * 0.5F, posBlock));
            }

            ++this.stepSoundTickCounter;
            if(this.curBlockDamageMP >= 1.0F) {
               this.isHittingBlock = false;
               this.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, posBlock, directionFacing));
               this.onPlayerDestroyBlock(posBlock);
               this.curBlockDamageMP = 0.0F;
               this.stepSoundTickCounter = 0.0F;
               this.blockHitDelay = 5;
            }

            this.mc.theWorld.sendBlockBreakProgress(this.mc.thePlayer.getEntityId(), this.currentBlock, (int)(this.curBlockDamageMP * 10.0F) - 1);
            return true;
         }
      } else {
         return this.clickBlock(posBlock, directionFacing);
      }
   }

   public float getBlockReachDistance() {
      return this.currentGameType.isCreative()?5.0F:4.5F;
   }

   public void updateController() {
      this.syncCurrentPlayItem();
      if(this.connection.getNetworkManager().isChannelOpen()) {
         this.connection.getNetworkManager().processReceivedPackets();
      } else {
         this.connection.getNetworkManager().checkDisconnected();
      }
   }

   private boolean isHittingPosition(BlockPos pos) {
      ItemStack itemstack = this.mc.thePlayer.getHeldItemMainhand();
      boolean flag = this.currentItemHittingBlock == null && itemstack == null;
      if(this.currentItemHittingBlock != null && itemstack != null) {
         flag = itemstack.getItem() == this.currentItemHittingBlock.getItem() && ItemStack.areItemStackTagsEqual(itemstack, this.currentItemHittingBlock) && (itemstack.isItemStackDamageable() || itemstack.getMetadata() == this.currentItemHittingBlock.getMetadata());
      }

      return pos.equals(this.currentBlock) && flag;
   }

   private void syncCurrentPlayItem() {
      int i = this.mc.thePlayer.inventory.currentItem;
      if(i != this.currentPlayerItem) {
         this.currentPlayerItem = i;
         this.connection.sendPacket(new CPacketHeldItemChange(this.currentPlayerItem));
      }
   }

   public EnumActionResult processRightClickBlock(EntityPlayerSP player, WorldClient worldIn, @Nullable ItemStack stack, BlockPos pos, EnumFacing facing, Vec3d vec, EnumHand hand) {
      this.syncCurrentPlayItem();
      float f = (float)(vec.xCoord - (double)pos.getX());
      float f1 = (float)(vec.yCoord - (double)pos.getY());
      float f2 = (float)(vec.zCoord - (double)pos.getZ());
      boolean flag = false;
      if(!this.mc.theWorld.getWorldBorder().contains(pos)) {
         return EnumActionResult.FAIL;
      } else {
         if(this.currentGameType != WorldSettings.GameType.SPECTATOR) {
            IBlockState iblockstate = worldIn.getBlockState(pos);
            if((!player.isSneaking() || player.getHeldItemMainhand() == null && player.getHeldItemOffhand() == null) && iblockstate.getBlock().onBlockActivated(worldIn, pos, iblockstate, player, hand, stack, facing, f, f1, f2)) {
               flag = true;
            }

            if(!flag && stack != null && stack.getItem() instanceof ItemBlock) {
               ItemBlock itemblock = (ItemBlock)stack.getItem();
               if(!itemblock.canPlaceBlockOnSide(worldIn, pos, facing, player, stack)) {
                  return EnumActionResult.FAIL;
               }
            }
         }

         this.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, facing, hand, f, f1, f2));
         if(!flag && this.currentGameType != WorldSettings.GameType.SPECTATOR) {
            if(stack == null) {
               return EnumActionResult.PASS;
            } else if(player.getCooldownTracker().hasCooldown(stack.getItem())) {
               return EnumActionResult.PASS;
            } else if(stack.getItem() instanceof ItemBlock && ((ItemBlock)stack.getItem()).getBlock() instanceof BlockCommandBlock && !player.canCommandSenderUseCommand(2, "")) {
               return EnumActionResult.FAIL;
            } else if(this.currentGameType.isCreative()) {
               int i = stack.getMetadata();
               int j = stack.stackSize;
               EnumActionResult enumactionresult = stack.onItemUse(player, worldIn, pos, hand, facing, f, f1, f2);
               stack.setItemDamage(i);
               stack.stackSize = j;
               return enumactionresult;
            } else {
               return stack.onItemUse(player, worldIn, pos, hand, facing, f, f1, f2);
            }
         } else {
            return EnumActionResult.SUCCESS;
         }
      }
   }

   public EnumActionResult processRightClick(EntityPlayer player, World worldIn, ItemStack stack, EnumHand hand) {
      if(this.currentGameType == WorldSettings.GameType.SPECTATOR) {
         return EnumActionResult.PASS;
      } else {
         this.syncCurrentPlayItem();
         this.connection.sendPacket(new CPacketPlayerTryUseItem(hand));
         if(player.getCooldownTracker().hasCooldown(stack.getItem())) {
            return EnumActionResult.PASS;
         } else {
            int i = stack.stackSize;
            ActionResult<ItemStack> actionresult = stack.useItemRightClick(worldIn, player, hand);
            ItemStack itemstack = (ItemStack)actionresult.getResult();
            if(itemstack != stack || itemstack.stackSize != i) {
               player.setHeldItem(hand, itemstack);
               if(itemstack.stackSize == 0) {
                  player.setHeldItem(hand, (ItemStack)null);
               }
            }

            return actionresult.getType();
         }
      }
   }

   public EntityPlayerSP createClientPlayer(World worldIn, StatisticsManager statWriter) {
      return new EntityPlayerSP(this.mc, worldIn, this.connection, statWriter);
   }

   public void attackEntity(EntityPlayer playerIn, Entity targetEntity) {
      this.syncCurrentPlayItem();
      this.connection.sendPacket(new CPacketUseEntity(targetEntity));
      if(this.currentGameType != WorldSettings.GameType.SPECTATOR) {
         playerIn.attackTargetEntityWithCurrentItem(targetEntity);
         playerIn.resetCooldown();
      }
   }

   public EnumActionResult interactWithEntity(EntityPlayer player, Entity target, @Nullable ItemStack heldItem, EnumHand hand) {
      this.syncCurrentPlayItem();
      this.connection.sendPacket(new CPacketUseEntity(target, hand));
      return this.currentGameType == WorldSettings.GameType.SPECTATOR?EnumActionResult.PASS:player.interact(target, heldItem, hand);
   }

   public EnumActionResult interactWithEntity(EntityPlayer player, Entity target, RayTraceResult raytrace, @Nullable ItemStack heldItem, EnumHand hand) {
      this.syncCurrentPlayItem();
      Vec3d vec3d = new Vec3d(raytrace.hitVec.xCoord - target.posX, raytrace.hitVec.yCoord - target.posY, raytrace.hitVec.zCoord - target.posZ);
      this.connection.sendPacket(new CPacketUseEntity(target, hand, vec3d));
      return this.currentGameType == WorldSettings.GameType.SPECTATOR?EnumActionResult.PASS:target.applyPlayerInteraction(player, vec3d, heldItem, hand);
   }

   public ItemStack windowClick(int windowId, int slotId, int mouseButton, ClickType type, EntityPlayer player) {
      short short1 = player.openContainer.getNextTransactionID(player.inventory);
      ItemStack itemstack = player.openContainer.slotClick(slotId, mouseButton, type, player);
      this.connection.sendPacket(new CPacketClickWindow(windowId, slotId, mouseButton, type, itemstack, short1));
      return itemstack;
   }

   public void sendEnchantPacket(int windowID, int button) {
      this.connection.sendPacket(new CPacketEnchantItem(windowID, button));
   }

   public void sendSlotPacket(ItemStack itemStackIn, int slotId) {
      if(this.currentGameType.isCreative()) {
         this.connection.sendPacket(new CPacketCreativeInventoryAction(slotId, itemStackIn));
      }
   }

   public void sendPacketDropItem(ItemStack itemStackIn) {
      if(this.currentGameType.isCreative() && itemStackIn != null) {
         this.connection.sendPacket(new CPacketCreativeInventoryAction(-1, itemStackIn));
      }
   }

   public void onStoppedUsingItem(EntityPlayer playerIn) {
      this.syncCurrentPlayItem();
      this.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
      playerIn.stopActiveHand();
   }

   public boolean gameIsSurvivalOrAdventure() {
      return this.currentGameType.isSurvivalOrAdventure();
   }

   public boolean isNotCreative() {
      return !this.currentGameType.isCreative();
   }

   public boolean isInCreativeMode() {
      return this.currentGameType.isCreative();
   }

   public boolean extendedReach() {
      return this.currentGameType.isCreative();
   }

   public boolean isRidingHorse() {
      return this.mc.thePlayer.isRiding() && this.mc.thePlayer.getRidingEntity() instanceof EntityHorse;
   }

   public boolean isSpectatorMode() {
      return this.currentGameType == WorldSettings.GameType.SPECTATOR;
   }

   public WorldSettings.GameType getCurrentGameType() {
      return this.currentGameType;
   }

   public boolean getIsHittingBlock() {
      return this.isHittingBlock;
   }

   public void pickItem(int index) {
      this.connection.sendPacket(new CPacketCustomPayload("MC|PickItem", (new PacketBuffer(Unpooled.buffer())).writeVarIntToBuffer(index)));
   }
}