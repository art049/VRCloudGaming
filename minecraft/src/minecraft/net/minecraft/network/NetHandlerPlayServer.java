package net.minecraft.network;

import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;
import com.google.common.util.concurrent.Futures;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import net.minecraft.block.BlockCommandBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ICrashReportDetail;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IJumpingMount;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecartCommandBlock;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerBeacon;
import net.minecraft.inventory.ContainerMerchant;
import net.minecraft.inventory.ContainerRepair;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemWritableBook;
import net.minecraft.item.ItemWrittenBook;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.client.CPacketClickWindow;
import net.minecraft.network.play.client.CPacketClientSettings;
import net.minecraft.network.play.client.CPacketClientStatus;
import net.minecraft.network.play.client.CPacketCloseWindow;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketConfirmTransaction;
import net.minecraft.network.play.client.CPacketCreativeInventoryAction;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.network.play.client.CPacketEnchantItem;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketInput;
import net.minecraft.network.play.client.CPacketKeepAlive;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerAbilities;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketResourcePackStatus;
import net.minecraft.network.play.client.CPacketSpectate;
import net.minecraft.network.play.client.CPacketSteerBoat;
import net.minecraft.network.play.client.CPacketTabComplete;
import net.minecraft.network.play.client.CPacketUpdateSign;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.client.CPacketVehicleMove;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.network.play.server.SPacketConfirmTransaction;
import net.minecraft.network.play.server.SPacketDisconnect;
import net.minecraft.network.play.server.SPacketHeldItemChange;
import net.minecraft.network.play.server.SPacketKeepAlive;
import net.minecraft.network.play.server.SPacketMoveVehicle;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.network.play.server.SPacketRespawn;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.network.play.server.SPacketTabComplete;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.AchievementList;
import net.minecraft.tileentity.CommandBlockBaseLogic;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.tileentity.TileEntityStructure;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.IntHashMap;
import net.minecraft.util.Mirror;
import net.minecraft.util.ReportedException;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NetHandlerPlayServer implements INetHandlerPlayServer, ITickable {
   private static final Logger LOGGER = LogManager.getLogger();
   public final NetworkManager netManager;
   private final MinecraftServer serverController;
   public EntityPlayerMP playerEntity;
   private int networkTickCount;
   private int keepAliveId;
   private long lastPingTime;
   private long lastSentPingPacket;
   private int chatSpamThresholdCount;
   private int itemDropThreshold;
   private final IntHashMap<Short> pendingTransactions = new IntHashMap();
   private double firstGoodX;
   private double firstGoodY;
   private double firstGoodZ;
   private double lastGoodX;
   private double lastGoodY;
   private double lastGoodZ;
   private Entity lowestRiddenEnt;
   private double lowestRiddenX;
   private double lowestRiddenY;
   private double lowestRiddenZ;
   private double lowestRiddenX1;
   private double lowestRiddenY1;
   private double lowestRiddenZ1;
   private Vec3d targetPos;
   private int teleportId;
   private int lastPositionUpdate;
   private boolean floating;
   private int floatingTickCount;
   private boolean vehicleFloating;
   private int vehicleFloatingTickCount;
   private int movePacketCounter;
   private int lastMovePacketCounter;

   public NetHandlerPlayServer(MinecraftServer server, NetworkManager networkManagerIn, EntityPlayerMP playerIn) {
      this.serverController = server;
      this.netManager = networkManagerIn;
      networkManagerIn.setNetHandler(this);
      this.playerEntity = playerIn;
      playerIn.connection = this;
   }

   public void update() {
      this.captureCurrentPosition();
      this.playerEntity.onUpdateEntity();
      this.playerEntity.setPositionAndRotation(this.firstGoodX, this.firstGoodY, this.firstGoodZ, this.playerEntity.rotationYaw, this.playerEntity.rotationPitch);
      ++this.networkTickCount;
      this.lastMovePacketCounter = this.movePacketCounter;
      if(this.floating) {
         if(++this.floatingTickCount > 80) {
            LOGGER.warn(this.playerEntity.getName() + " was kicked for floating too long!");
            this.kickPlayerFromServer("Flying is not enabled on this server");
            return;
         }
      } else {
         this.floating = false;
         this.floatingTickCount = 0;
      }

      this.lowestRiddenEnt = this.playerEntity.getLowestRidingEntity();
      if(this.lowestRiddenEnt != this.playerEntity && this.lowestRiddenEnt.getControllingPassenger() == this.playerEntity) {
         this.lowestRiddenX = this.lowestRiddenEnt.posX;
         this.lowestRiddenY = this.lowestRiddenEnt.posY;
         this.lowestRiddenZ = this.lowestRiddenEnt.posZ;
         this.lowestRiddenX1 = this.lowestRiddenEnt.posX;
         this.lowestRiddenY1 = this.lowestRiddenEnt.posY;
         this.lowestRiddenZ1 = this.lowestRiddenEnt.posZ;
         if(this.vehicleFloating && this.playerEntity.getLowestRidingEntity().getControllingPassenger() == this.playerEntity) {
            if(++this.vehicleFloatingTickCount > 80) {
               LOGGER.warn(this.playerEntity.getName() + " was kicked for floating a vehicle too long!");
               this.kickPlayerFromServer("Flying is not enabled on this server");
               return;
            }
         } else {
            this.vehicleFloating = false;
            this.vehicleFloatingTickCount = 0;
         }
      } else {
         this.lowestRiddenEnt = null;
         this.vehicleFloating = false;
         this.vehicleFloatingTickCount = 0;
      }

      this.serverController.theProfiler.startSection("keepAlive");
      if((long)this.networkTickCount - this.lastSentPingPacket > 40L) {
         this.lastSentPingPacket = (long)this.networkTickCount;
         this.lastPingTime = this.currentTimeMillis();
         this.keepAliveId = (int)this.lastPingTime;
         this.sendPacket(new SPacketKeepAlive(this.keepAliveId));
      }

      this.serverController.theProfiler.endSection();
      if(this.chatSpamThresholdCount > 0) {
         --this.chatSpamThresholdCount;
      }

      if(this.itemDropThreshold > 0) {
         --this.itemDropThreshold;
      }

      if(this.playerEntity.getLastActiveTime() > 0L && this.serverController.getMaxPlayerIdleMinutes() > 0 && MinecraftServer.getCurrentTimeMillis() - this.playerEntity.getLastActiveTime() > (long)(this.serverController.getMaxPlayerIdleMinutes() * 1000 * 60)) {
         this.kickPlayerFromServer("You have been idle for too long!");
      }
   }

   private void captureCurrentPosition() {
      this.firstGoodX = this.playerEntity.posX;
      this.firstGoodY = this.playerEntity.posY;
      this.firstGoodZ = this.playerEntity.posZ;
      this.lastGoodX = this.playerEntity.posX;
      this.lastGoodY = this.playerEntity.posY;
      this.lastGoodZ = this.playerEntity.posZ;
   }

   public NetworkManager getNetworkManager() {
      return this.netManager;
   }

   public void kickPlayerFromServer(String reason) {
      final TextComponentString textcomponentstring = new TextComponentString(reason);
      this.netManager.sendPacket(new SPacketDisconnect(textcomponentstring), new GenericFutureListener<Future<? super Void>>() {
         public void operationComplete(Future<? super Void> p_operationComplete_1_) throws Exception {
            NetHandlerPlayServer.this.netManager.closeChannel(textcomponentstring);
         }
      }, new GenericFutureListener[0]);
      this.netManager.disableAutoRead();
      Futures.getUnchecked(this.serverController.addScheduledTask(new Runnable() {
         public void run() {
            NetHandlerPlayServer.this.netManager.checkDisconnected();
         }
      }));
   }

   public void processInput(CPacketInput packetIn) {
      PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerWorld());
      this.playerEntity.setEntityActionState(packetIn.getStrafeSpeed(), packetIn.getForwardSpeed(), packetIn.isJumping(), packetIn.isSneaking());
   }

   private static boolean isMovePlayerPacketInvalid(CPacketPlayer packetIn) {
      return Doubles.isFinite(packetIn.getX(0.0D)) && Doubles.isFinite(packetIn.getY(0.0D)) && Doubles.isFinite(packetIn.getZ(0.0D)) && Floats.isFinite(packetIn.getPitch(0.0F)) && Floats.isFinite(packetIn.getYaw(0.0F))?false:Math.abs(packetIn.getX(0.0D)) <= 3.0E7D && Math.abs(packetIn.getX(0.0D)) <= 3.0E7D;
   }

   private static boolean isMoveVehiclePacketInvalid(CPacketVehicleMove packetIn) {
      return !Doubles.isFinite(packetIn.getX()) || !Doubles.isFinite(packetIn.getY()) || !Doubles.isFinite(packetIn.getZ()) || !Floats.isFinite(packetIn.getPitch()) || !Floats.isFinite(packetIn.getYaw());
   }

   public void processVehicleMove(CPacketVehicleMove packetIn) {
      PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerWorld());
      if(isMoveVehiclePacketInvalid(packetIn)) {
         this.kickPlayerFromServer("Invalid move vehicle packet received");
      } else {
         Entity entity = this.playerEntity.getLowestRidingEntity();
         if(entity != this.playerEntity && entity.getControllingPassenger() == this.playerEntity && entity == this.lowestRiddenEnt) {
            WorldServer worldserver = this.playerEntity.getServerWorld();
            double d0 = entity.posX;
            double d1 = entity.posY;
            double d2 = entity.posZ;
            double d3 = packetIn.getX();
            double d4 = packetIn.getY();
            double d5 = packetIn.getZ();
            float f = packetIn.getYaw();
            float f1 = packetIn.getPitch();
            double d6 = d3 - this.lowestRiddenX;
            double d7 = d4 - this.lowestRiddenY;
            double d8 = d5 - this.lowestRiddenZ;
            double d9 = entity.motionX * entity.motionX + entity.motionY * entity.motionY + entity.motionZ * entity.motionZ;
            double d10 = d6 * d6 + d7 * d7 + d8 * d8;
            if(d10 - d9 > 100.0D && (!this.serverController.isSinglePlayer() || !this.serverController.getServerOwner().equals(entity.getName()))) {
               LOGGER.warn(entity.getName() + " (vehicle of " + this.playerEntity.getName() + ") moved too quickly! " + d6 + "," + d7 + "," + d8);
               this.netManager.sendPacket(new SPacketMoveVehicle(entity));
               return;
            }

            boolean flag = worldserver.getCollisionBoxes(entity, entity.getEntityBoundingBox().contract(0.0625D)).isEmpty();
            d6 = d3 - this.lowestRiddenX1;
            d7 = d4 - this.lowestRiddenY1 - 1.0E-6D;
            d8 = d5 - this.lowestRiddenZ1;
            entity.moveEntity(d6, d7, d8);
            double d11 = d7;
            d6 = d3 - entity.posX;
            d7 = d4 - entity.posY;
            if(d7 > -0.5D || d7 < 0.5D) {
               d7 = 0.0D;
            }

            d8 = d5 - entity.posZ;
            d10 = d6 * d6 + d7 * d7 + d8 * d8;
            boolean flag1 = false;
            if(d10 > 0.0625D) {
               flag1 = true;
               LOGGER.warn(entity.getName() + " moved wrongly!");
            }

            entity.setPositionAndRotation(d3, d4, d5, f, f1);
            boolean flag2 = worldserver.getCollisionBoxes(entity, entity.getEntityBoundingBox().contract(0.0625D)).isEmpty();
            if(flag && (flag1 || !flag2)) {
               entity.setPositionAndRotation(d0, d1, d2, f, f1);
               this.netManager.sendPacket(new SPacketMoveVehicle(entity));
               return;
            }

            this.serverController.getPlayerList().serverUpdateMountedMovingPlayer(this.playerEntity);
            this.playerEntity.addMovementStat(this.playerEntity.posX - d0, this.playerEntity.posY - d1, this.playerEntity.posZ - d2);
            this.vehicleFloating = d11 >= -0.03125D && !this.serverController.isFlightAllowed() && !worldserver.checkBlockCollision(entity.getEntityBoundingBox().expandXyz(0.0625D).addCoord(0.0D, -0.55D, 0.0D));
            this.lowestRiddenX1 = entity.posX;
            this.lowestRiddenY1 = entity.posY;
            this.lowestRiddenZ1 = entity.posZ;
         }
      }
   }

   public void processConfirmTeleport(CPacketConfirmTeleport packetIn) {
      PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerWorld());
      if(packetIn.getTeleportId() == this.teleportId) {
         this.playerEntity.setPositionAndRotation(this.targetPos.xCoord, this.targetPos.yCoord, this.targetPos.zCoord, this.playerEntity.rotationYaw, this.playerEntity.rotationPitch);
         if(this.playerEntity.isInvulnerableDimensionChange()) {
            this.lastGoodX = this.targetPos.xCoord;
            this.lastGoodY = this.targetPos.yCoord;
            this.lastGoodZ = this.targetPos.zCoord;
            this.playerEntity.clearInvulnerableDimensionChange();
         }

         this.targetPos = null;
      }
   }

   public void processPlayer(CPacketPlayer packetIn) {
      PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerWorld());
      if(isMovePlayerPacketInvalid(packetIn)) {
         this.kickPlayerFromServer("Invalid move player packet received");
      } else {
         WorldServer worldserver = this.serverController.worldServerForDimension(this.playerEntity.dimension);
         if(!this.playerEntity.playerConqueredTheEnd) {
            if(this.networkTickCount == 0) {
               this.captureCurrentPosition();
            }

            if(this.targetPos != null) {
               if(this.networkTickCount - this.lastPositionUpdate > 20) {
                  this.lastPositionUpdate = this.networkTickCount;
                  this.setPlayerLocation(this.targetPos.xCoord, this.targetPos.yCoord, this.targetPos.zCoord, this.playerEntity.rotationYaw, this.playerEntity.rotationPitch);
               }
            } else {
               this.lastPositionUpdate = this.networkTickCount;
               if(this.playerEntity.isRiding()) {
                  this.playerEntity.setPositionAndRotation(this.playerEntity.posX, this.playerEntity.posY, this.playerEntity.posZ, packetIn.getYaw(this.playerEntity.rotationYaw), packetIn.getPitch(this.playerEntity.rotationPitch));
                  this.serverController.getPlayerList().serverUpdateMountedMovingPlayer(this.playerEntity);
               } else {
                  double d0 = this.playerEntity.posX;
                  double d1 = this.playerEntity.posY;
                  double d2 = this.playerEntity.posZ;
                  double d3 = this.playerEntity.posY;
                  double d4 = packetIn.getX(this.playerEntity.posX);
                  double d5 = packetIn.getY(this.playerEntity.posY);
                  double d6 = packetIn.getZ(this.playerEntity.posZ);
                  float f = packetIn.getYaw(this.playerEntity.rotationYaw);
                  float f1 = packetIn.getPitch(this.playerEntity.rotationPitch);
                  double d7 = d4 - this.firstGoodX;
                  double d8 = d5 - this.firstGoodY;
                  double d9 = d6 - this.firstGoodZ;
                  double d10 = this.playerEntity.motionX * this.playerEntity.motionX + this.playerEntity.motionY * this.playerEntity.motionY + this.playerEntity.motionZ * this.playerEntity.motionZ;
                  double d11 = d7 * d7 + d8 * d8 + d9 * d9;
                  ++this.movePacketCounter;
                  int i = this.movePacketCounter - this.lastMovePacketCounter;
                  if(i > 5) {
                     LOGGER.debug(this.playerEntity.getName() + " is sending move packets too frequently (" + i + " packets since last tick)");
                     i = 1;
                  }

                  if(!this.playerEntity.isInvulnerableDimensionChange() && (!this.playerEntity.getServerWorld().getGameRules().getBoolean("disableElytraMovementCheck") || !this.playerEntity.isElytraFlying())) {
                     float f2 = this.playerEntity.isElytraFlying()?300.0F:100.0F;
                     if(d11 - d10 > (double)(f2 * (float)i) && (!this.serverController.isSinglePlayer() || !this.serverController.getServerOwner().equals(this.playerEntity.getName()))) {
                        LOGGER.warn(this.playerEntity.getName() + " moved too quickly! " + d7 + "," + d8 + "," + d9);
                        this.setPlayerLocation(this.playerEntity.posX, this.playerEntity.posY, this.playerEntity.posZ, this.playerEntity.rotationYaw, this.playerEntity.rotationPitch);
                        return;
                     }
                  }

                  boolean flag2 = worldserver.getCollisionBoxes(this.playerEntity, this.playerEntity.getEntityBoundingBox().contract(0.0625D)).isEmpty();
                  d7 = d4 - this.lastGoodX;
                  d8 = d5 - this.lastGoodY;
                  d9 = d6 - this.lastGoodZ;
                  if(this.playerEntity.onGround && !packetIn.isOnGround() && d8 > 0.0D) {
                     this.playerEntity.jump();
                  }

                  this.playerEntity.moveEntity(d7, d8, d9);
                  this.playerEntity.onGround = packetIn.isOnGround();
                  double d12 = d8;
                  d7 = d4 - this.playerEntity.posX;
                  d8 = d5 - this.playerEntity.posY;
                  if(d8 > -0.5D || d8 < 0.5D) {
                     d8 = 0.0D;
                  }

                  d9 = d6 - this.playerEntity.posZ;
                  d11 = d7 * d7 + d8 * d8 + d9 * d9;
                  boolean flag = false;
                  if(!this.playerEntity.isInvulnerableDimensionChange() && d11 > 0.0625D && !this.playerEntity.isPlayerSleeping() && !this.playerEntity.interactionManager.isCreative() && this.playerEntity.interactionManager.getGameType() != WorldSettings.GameType.SPECTATOR) {
                     flag = true;
                     LOGGER.warn(this.playerEntity.getName() + " moved wrongly!");
                  }

                  this.playerEntity.setPositionAndRotation(d4, d5, d6, f, f1);
                  this.playerEntity.addMovementStat(this.playerEntity.posX - d0, this.playerEntity.posY - d1, this.playerEntity.posZ - d2);
                  if(!this.playerEntity.noClip && !this.playerEntity.isPlayerSleeping()) {
                     boolean flag1 = worldserver.getCollisionBoxes(this.playerEntity, this.playerEntity.getEntityBoundingBox().contract(0.0625D)).isEmpty();
                     if(flag2 && (flag || !flag1)) {
                        this.setPlayerLocation(d0, d1, d2, f, f1);
                        return;
                     }
                  }

                  this.floating = d12 >= -0.03125D;
                  this.floating &= !this.serverController.isFlightAllowed() && !this.playerEntity.capabilities.allowFlying;
                  this.floating &= !this.playerEntity.isPotionActive(MobEffects.LEVITATION) && !this.playerEntity.isElytraFlying() && !worldserver.checkBlockCollision(this.playerEntity.getEntityBoundingBox().expandXyz(0.0625D).addCoord(0.0D, -0.55D, 0.0D));
                  this.playerEntity.onGround = packetIn.isOnGround();
                  this.serverController.getPlayerList().serverUpdateMountedMovingPlayer(this.playerEntity);
                  this.playerEntity.handleFalling(this.playerEntity.posY - d3, packetIn.isOnGround());
                  this.lastGoodX = this.playerEntity.posX;
                  this.lastGoodY = this.playerEntity.posY;
                  this.lastGoodZ = this.playerEntity.posZ;
               }
            }
         }
      }
   }

   public void setPlayerLocation(double x, double y, double z, float yaw, float pitch) {
      this.setPlayerLocation(x, y, z, yaw, pitch, Collections.<SPacketPlayerPosLook.EnumFlags>emptySet());
   }

   public void setPlayerLocation(double x, double y, double z, float yaw, float pitch, Set<SPacketPlayerPosLook.EnumFlags> relativeSet) {
      double d0 = relativeSet.contains(SPacketPlayerPosLook.EnumFlags.X)?this.playerEntity.posX:0.0D;
      double d1 = relativeSet.contains(SPacketPlayerPosLook.EnumFlags.Y)?this.playerEntity.posY:0.0D;
      double d2 = relativeSet.contains(SPacketPlayerPosLook.EnumFlags.Z)?this.playerEntity.posZ:0.0D;
      this.targetPos = new Vec3d(x + d0, y + d1, z + d2);
      float f = yaw;
      float f1 = pitch;
      if(relativeSet.contains(SPacketPlayerPosLook.EnumFlags.Y_ROT)) {
         f = yaw + this.playerEntity.rotationYaw;
      }

      if(relativeSet.contains(SPacketPlayerPosLook.EnumFlags.X_ROT)) {
         f1 = pitch + this.playerEntity.rotationPitch;
      }

      if(++this.teleportId == Integer.MAX_VALUE) {
         this.teleportId = 0;
      }

      this.lastPositionUpdate = this.networkTickCount;
      this.playerEntity.setPositionAndRotation(this.targetPos.xCoord, this.targetPos.yCoord, this.targetPos.zCoord, f, f1);
      this.playerEntity.connection.sendPacket(new SPacketPlayerPosLook(x, y, z, yaw, pitch, relativeSet, this.teleportId));
   }

   public void processPlayerDigging(CPacketPlayerDigging packetIn) {
      PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerWorld());
      WorldServer worldserver = this.serverController.worldServerForDimension(this.playerEntity.dimension);
      BlockPos blockpos = packetIn.getPosition();
      this.playerEntity.markPlayerActive();
      switch(packetIn.getAction()) {
      case SWAP_HELD_ITEMS:
         if(!this.playerEntity.isSpectator()) {
            ItemStack itemstack1 = this.playerEntity.getHeldItem(EnumHand.OFF_HAND);
            this.playerEntity.setHeldItem(EnumHand.OFF_HAND, this.playerEntity.getHeldItem(EnumHand.MAIN_HAND));
            this.playerEntity.setHeldItem(EnumHand.MAIN_HAND, itemstack1);
         }

         return;
      case DROP_ITEM:
         if(!this.playerEntity.isSpectator()) {
            this.playerEntity.dropItem(false);
         }

         return;
      case DROP_ALL_ITEMS:
         if(!this.playerEntity.isSpectator()) {
            this.playerEntity.dropItem(true);
         }

         return;
      case RELEASE_USE_ITEM:
         this.playerEntity.stopActiveHand();
         ItemStack itemstack = this.playerEntity.getHeldItemMainhand();
         if(itemstack != null && itemstack.stackSize == 0) {
            this.playerEntity.setHeldItem(EnumHand.MAIN_HAND, (ItemStack)null);
         }

         return;
      case START_DESTROY_BLOCK:
      case ABORT_DESTROY_BLOCK:
      case STOP_DESTROY_BLOCK:
         double d0 = this.playerEntity.posX - ((double)blockpos.getX() + 0.5D);
         double d1 = this.playerEntity.posY - ((double)blockpos.getY() + 0.5D) + 1.5D;
         double d2 = this.playerEntity.posZ - ((double)blockpos.getZ() + 0.5D);
         double d3 = d0 * d0 + d1 * d1 + d2 * d2;
         if(d3 > 36.0D) {
            return;
         } else if(blockpos.getY() >= this.serverController.getBuildLimit()) {
            return;
         } else {
            if(packetIn.getAction() == CPacketPlayerDigging.Action.START_DESTROY_BLOCK) {
               if(!this.serverController.isBlockProtected(worldserver, blockpos, this.playerEntity) && worldserver.getWorldBorder().contains(blockpos)) {
                  this.playerEntity.interactionManager.onBlockClicked(blockpos, packetIn.getFacing());
               } else {
                  this.playerEntity.connection.sendPacket(new SPacketBlockChange(worldserver, blockpos));
               }
            } else {
               if(packetIn.getAction() == CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK) {
                  this.playerEntity.interactionManager.blockRemoving(blockpos);
               } else if(packetIn.getAction() == CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK) {
                  this.playerEntity.interactionManager.cancelDestroyingBlock();
               }

               if(worldserver.getBlockState(blockpos).getMaterial() != Material.AIR) {
                  this.playerEntity.connection.sendPacket(new SPacketBlockChange(worldserver, blockpos));
               }
            }

            return;
         }
      default:
         throw new IllegalArgumentException("Invalid player action");
      }
   }

   public void processRightClickBlock(CPacketPlayerTryUseItemOnBlock packetIn) {
      PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerWorld());
      WorldServer worldserver = this.serverController.worldServerForDimension(this.playerEntity.dimension);
      EnumHand enumhand = packetIn.getHand();
      ItemStack itemstack = this.playerEntity.getHeldItem(enumhand);
      BlockPos blockpos = packetIn.getPos();
      EnumFacing enumfacing = packetIn.getDirection();
      this.playerEntity.markPlayerActive();
      if(blockpos.getY() < this.serverController.getBuildLimit() - 1 || enumfacing != EnumFacing.UP && blockpos.getY() < this.serverController.getBuildLimit()) {
         if(this.targetPos == null && this.playerEntity.getDistanceSq((double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 0.5D, (double)blockpos.getZ() + 0.5D) < 64.0D && !this.serverController.isBlockProtected(worldserver, blockpos, this.playerEntity) && worldserver.getWorldBorder().contains(blockpos)) {
            this.playerEntity.interactionManager.processRightClickBlock(this.playerEntity, worldserver, itemstack, enumhand, blockpos, enumfacing, packetIn.getFacingX(), packetIn.getFacingY(), packetIn.getFacingZ());
         }
      } else {
         TextComponentTranslation textcomponenttranslation = new TextComponentTranslation("build.tooHigh", new Object[]{Integer.valueOf(this.serverController.getBuildLimit())});
         textcomponenttranslation.getStyle().setColor(TextFormatting.RED);
         this.playerEntity.connection.sendPacket(new SPacketChat(textcomponenttranslation));
      }

      this.playerEntity.connection.sendPacket(new SPacketBlockChange(worldserver, blockpos));
      this.playerEntity.connection.sendPacket(new SPacketBlockChange(worldserver, blockpos.offset(enumfacing)));
      itemstack = this.playerEntity.getHeldItem(enumhand);
      if(itemstack != null && itemstack.stackSize == 0) {
         this.playerEntity.setHeldItem(enumhand, (ItemStack)null);
         itemstack = null;
      }
   }

   public void processPlayerBlockPlacement(CPacketPlayerTryUseItem packetIn) {
      PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerWorld());
      WorldServer worldserver = this.serverController.worldServerForDimension(this.playerEntity.dimension);
      EnumHand enumhand = packetIn.getHand();
      ItemStack itemstack = this.playerEntity.getHeldItem(enumhand);
      this.playerEntity.markPlayerActive();
      if(itemstack != null) {
         this.playerEntity.interactionManager.processRightClick(this.playerEntity, worldserver, itemstack, enumhand);
         itemstack = this.playerEntity.getHeldItem(enumhand);
         if(itemstack != null && itemstack.stackSize == 0) {
            this.playerEntity.setHeldItem(enumhand, (ItemStack)null);
            itemstack = null;
         }
      }
   }

   public void handleSpectate(CPacketSpectate packetIn) {
      PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerWorld());
      if(this.playerEntity.isSpectator()) {
         Entity entity = null;

         for(WorldServer worldserver : this.serverController.worldServers) {
            if(worldserver != null) {
               entity = packetIn.getEntity(worldserver);
               if(entity != null) {
                  break;
               }
            }
         }

         if(entity != null) {
            this.playerEntity.setSpectatingEntity(this.playerEntity);
            this.playerEntity.dismountRidingEntity();
            if(entity.worldObj != this.playerEntity.worldObj) {
               WorldServer worldserver1 = this.playerEntity.getServerWorld();
               WorldServer worldserver2 = (WorldServer)entity.worldObj;
               this.playerEntity.dimension = entity.dimension;
               this.sendPacket(new SPacketRespawn(this.playerEntity.dimension, worldserver1.getDifficulty(), worldserver1.getWorldInfo().getTerrainType(), this.playerEntity.interactionManager.getGameType()));
               this.serverController.getPlayerList().updatePermissionLevel(this.playerEntity);
               worldserver1.removeEntityDangerously(this.playerEntity);
               this.playerEntity.isDead = false;
               this.playerEntity.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);
               if(this.playerEntity.isEntityAlive()) {
                  worldserver1.updateEntityWithOptionalForce(this.playerEntity, false);
                  worldserver2.spawnEntityInWorld(this.playerEntity);
                  worldserver2.updateEntityWithOptionalForce(this.playerEntity, false);
               }

               this.playerEntity.setWorld(worldserver2);
               this.serverController.getPlayerList().preparePlayer(this.playerEntity, worldserver1);
               this.playerEntity.setPositionAndUpdate(entity.posX, entity.posY, entity.posZ);
               this.playerEntity.interactionManager.setWorld(worldserver2);
               this.serverController.getPlayerList().updateTimeAndWeatherForPlayer(this.playerEntity, worldserver2);
               this.serverController.getPlayerList().syncPlayerInventory(this.playerEntity);
            } else {
               this.playerEntity.setPositionAndUpdate(entity.posX, entity.posY, entity.posZ);
            }
         }
      }
   }

   public void handleResourcePackStatus(CPacketResourcePackStatus packetIn) {
   }

   public void processSteerBoat(CPacketSteerBoat packetIn) {
      PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerWorld());
      Entity entity = this.playerEntity.getRidingEntity();
      if(entity instanceof EntityBoat) {
         ((EntityBoat)entity).setPaddleState(packetIn.getLeft(), packetIn.getRight());
      }
   }

   public void onDisconnect(ITextComponent reason) {
      LOGGER.info(this.playerEntity.getName() + " lost connection: " + reason);
      this.serverController.refreshStatusNextTick();
      TextComponentTranslation textcomponenttranslation = new TextComponentTranslation("multiplayer.player.left", new Object[]{this.playerEntity.getDisplayName()});
      textcomponenttranslation.getStyle().setColor(TextFormatting.YELLOW);
      this.serverController.getPlayerList().sendChatMsg(textcomponenttranslation);
      this.playerEntity.mountEntityAndWakeUp();
      this.serverController.getPlayerList().playerLoggedOut(this.playerEntity);
      if(this.serverController.isSinglePlayer() && this.playerEntity.getName().equals(this.serverController.getServerOwner())) {
         LOGGER.info("Stopping singleplayer server as player logged out");
         this.serverController.initiateShutdown();
      }
   }

   public void sendPacket(final Packet<?> packetIn) {
      if(packetIn instanceof SPacketChat) {
         SPacketChat spacketchat = (SPacketChat)packetIn;
         EntityPlayer.EnumChatVisibility entityplayer$enumchatvisibility = this.playerEntity.getChatVisibility();
         if(entityplayer$enumchatvisibility == EntityPlayer.EnumChatVisibility.HIDDEN) {
            return;
         }

         if(entityplayer$enumchatvisibility == EntityPlayer.EnumChatVisibility.SYSTEM && !spacketchat.isSystem()) {
            return;
         }
      }

      try {
         this.netManager.sendPacket(packetIn);
      } catch (Throwable throwable) {
         CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Sending packet");
         CrashReportCategory crashreportcategory = crashreport.makeCategory("Packet being sent");
         crashreportcategory.func_189529_a("Packet class", new ICrashReportDetail<String>() {
            public String call() throws Exception {
               return packetIn.getClass().getCanonicalName();
            }
         });
         throw new ReportedException(crashreport);
      }
   }

   public void processHeldItemChange(CPacketHeldItemChange packetIn) {
      PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerWorld());
      if(packetIn.getSlotId() >= 0 && packetIn.getSlotId() < InventoryPlayer.getHotbarSize()) {
         this.playerEntity.inventory.currentItem = packetIn.getSlotId();
         this.playerEntity.markPlayerActive();
      } else {
         LOGGER.warn(this.playerEntity.getName() + " tried to set an invalid carried item");
      }
   }

   public void processChatMessage(CPacketChatMessage packetIn) {
      PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerWorld());
      if(this.playerEntity.getChatVisibility() == EntityPlayer.EnumChatVisibility.HIDDEN) {
         TextComponentTranslation textcomponenttranslation = new TextComponentTranslation("chat.cannotSend", new Object[0]);
         textcomponenttranslation.getStyle().setColor(TextFormatting.RED);
         this.sendPacket(new SPacketChat(textcomponenttranslation));
      } else {
         this.playerEntity.markPlayerActive();
         String s = packetIn.getMessage();
         s = StringUtils.normalizeSpace(s);

         for(int i = 0; i < s.length(); ++i) {
            if(!ChatAllowedCharacters.isAllowedCharacter(s.charAt(i))) {
               this.kickPlayerFromServer("Illegal characters in chat");
               return;
            }
         }

         if(s.startsWith("/")) {
            this.handleSlashCommand(s);
         } else {
            ITextComponent itextcomponent = new TextComponentTranslation("chat.type.text", new Object[]{this.playerEntity.getDisplayName(), s});
            this.serverController.getPlayerList().sendChatMsgImpl(itextcomponent, false);
         }

         this.chatSpamThresholdCount += 20;
         if(this.chatSpamThresholdCount > 200 && !this.serverController.getPlayerList().canSendCommands(this.playerEntity.getGameProfile())) {
            this.kickPlayerFromServer("disconnect.spam");
         }
      }
   }

   private void handleSlashCommand(String command) {
      this.serverController.getCommandManager().executeCommand(this.playerEntity, command);
   }

   public void handleAnimation(CPacketAnimation packetIn) {
      PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerWorld());
      this.playerEntity.markPlayerActive();
      this.playerEntity.swingArm(packetIn.getHand());
   }

   public void processEntityAction(CPacketEntityAction packetIn) {
      PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerWorld());
      this.playerEntity.markPlayerActive();
      switch(packetIn.getAction()) {
      case START_SNEAKING:
         this.playerEntity.setSneaking(true);
         break;
      case STOP_SNEAKING:
         this.playerEntity.setSneaking(false);
         break;
      case START_SPRINTING:
         this.playerEntity.setSprinting(true);
         break;
      case STOP_SPRINTING:
         this.playerEntity.setSprinting(false);
         break;
      case STOP_SLEEPING:
         this.playerEntity.wakeUpPlayer(false, true, true);
         this.targetPos = new Vec3d(this.playerEntity.posX, this.playerEntity.posY, this.playerEntity.posZ);
         break;
      case START_RIDING_JUMP:
         if(this.playerEntity.getRidingEntity() instanceof IJumpingMount) {
            IJumpingMount ijumpingmount1 = (IJumpingMount)this.playerEntity.getRidingEntity();
            int i = packetIn.getAuxData();
            if(ijumpingmount1.canJump() && i > 0) {
               ijumpingmount1.handleStartJump(i);
            }
         }
         break;
      case STOP_RIDING_JUMP:
         if(this.playerEntity.getRidingEntity() instanceof IJumpingMount) {
            IJumpingMount ijumpingmount = (IJumpingMount)this.playerEntity.getRidingEntity();
            ijumpingmount.handleStopJump();
         }
         break;
      case OPEN_INVENTORY:
         if(this.playerEntity.getRidingEntity() instanceof EntityHorse) {
            ((EntityHorse)this.playerEntity.getRidingEntity()).openGUI(this.playerEntity);
         }
         break;
      case START_FALL_FLYING:
         if(!this.playerEntity.onGround && this.playerEntity.motionY < 0.0D && !this.playerEntity.isElytraFlying() && !this.playerEntity.isInWater()) {
            ItemStack itemstack = this.playerEntity.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
            if(itemstack != null && itemstack.getItem() == Items.ELYTRA && ItemElytra.isBroken(itemstack)) {
               this.playerEntity.setElytraFlying();
            }
         } else {
            this.playerEntity.clearElytraFlying();
         }
         break;
      default:
         throw new IllegalArgumentException("Invalid client command!");
      }
   }

   public void processUseEntity(CPacketUseEntity packetIn) {
      PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerWorld());
      WorldServer worldserver = this.serverController.worldServerForDimension(this.playerEntity.dimension);
      Entity entity = packetIn.getEntityFromWorld(worldserver);
      this.playerEntity.markPlayerActive();
      if(entity != null) {
         boolean flag = this.playerEntity.canEntityBeSeen(entity);
         double d0 = 36.0D;
         if(!flag) {
            d0 = 9.0D;
         }

         if(this.playerEntity.getDistanceSqToEntity(entity) < d0) {
            if(packetIn.getAction() == CPacketUseEntity.Action.INTERACT) {
               EnumHand enumhand = packetIn.getHand();
               ItemStack itemstack = this.playerEntity.getHeldItem(enumhand);
               this.playerEntity.interact(entity, itemstack, enumhand);
            } else if(packetIn.getAction() == CPacketUseEntity.Action.INTERACT_AT) {
               EnumHand enumhand1 = packetIn.getHand();
               ItemStack itemstack1 = this.playerEntity.getHeldItem(enumhand1);
               entity.applyPlayerInteraction(this.playerEntity, packetIn.getHitVec(), itemstack1, enumhand1);
            } else if(packetIn.getAction() == CPacketUseEntity.Action.ATTACK) {
               if(entity instanceof EntityItem || entity instanceof EntityXPOrb || entity instanceof EntityArrow || entity == this.playerEntity) {
                  this.kickPlayerFromServer("Attempting to attack an invalid entity");
                  this.serverController.logWarning("Player " + this.playerEntity.getName() + " tried to attack an invalid entity");
                  return;
               }

               this.playerEntity.attackTargetEntityWithCurrentItem(entity);
            }
         }
      }
   }

   public void processClientStatus(CPacketClientStatus packetIn) {
      PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerWorld());
      this.playerEntity.markPlayerActive();
      CPacketClientStatus.State cpacketclientstatus$state = packetIn.getStatus();
      switch(cpacketclientstatus$state) {
      case PERFORM_RESPAWN:
         if(this.playerEntity.playerConqueredTheEnd) {
            this.playerEntity.playerConqueredTheEnd = false;
            this.playerEntity = this.serverController.getPlayerList().recreatePlayerEntity(this.playerEntity, 0, true);
         } else {
            if(this.playerEntity.getHealth() > 0.0F) {
               return;
            }

            this.playerEntity = this.serverController.getPlayerList().recreatePlayerEntity(this.playerEntity, 0, false);
            if(this.serverController.isHardcore()) {
               this.playerEntity.setGameType(WorldSettings.GameType.SPECTATOR);
               this.playerEntity.getServerWorld().getGameRules().setOrCreateGameRule("spectatorsGenerateChunks", "false");
            }
         }
         break;
      case REQUEST_STATS:
         this.playerEntity.getStatFile().sendStats(this.playerEntity);
         break;
      case OPEN_INVENTORY_ACHIEVEMENT:
         this.playerEntity.addStat(AchievementList.OPEN_INVENTORY);
      }
   }

   public void processCloseWindow(CPacketCloseWindow packetIn) {
      PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerWorld());
      this.playerEntity.closeContainer();
   }

   public void processClickWindow(CPacketClickWindow packetIn) {
      PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerWorld());
      this.playerEntity.markPlayerActive();
      if(this.playerEntity.openContainer.windowId == packetIn.getWindowId() && this.playerEntity.openContainer.getCanCraft(this.playerEntity)) {
         if(this.playerEntity.isSpectator()) {
            List<ItemStack> list = Lists.<ItemStack>newArrayList();

            for(int i = 0; i < this.playerEntity.openContainer.inventorySlots.size(); ++i) {
               list.add(((Slot)this.playerEntity.openContainer.inventorySlots.get(i)).getStack());
            }

            this.playerEntity.updateCraftingInventory(this.playerEntity.openContainer, list);
         } else {
            ItemStack itemstack2 = this.playerEntity.openContainer.slotClick(packetIn.getSlotId(), packetIn.getUsedButton(), packetIn.getClickType(), this.playerEntity);
            if(ItemStack.areItemStacksEqual(packetIn.getClickedItem(), itemstack2)) {
               this.playerEntity.connection.sendPacket(new SPacketConfirmTransaction(packetIn.getWindowId(), packetIn.getActionNumber(), true));
               this.playerEntity.isChangingQuantityOnly = true;
               this.playerEntity.openContainer.detectAndSendChanges();
               this.playerEntity.updateHeldItem();
               this.playerEntity.isChangingQuantityOnly = false;
            } else {
               this.pendingTransactions.addKey(this.playerEntity.openContainer.windowId, Short.valueOf(packetIn.getActionNumber()));
               this.playerEntity.connection.sendPacket(new SPacketConfirmTransaction(packetIn.getWindowId(), packetIn.getActionNumber(), false));
               this.playerEntity.openContainer.setCanCraft(this.playerEntity, false);
               List<ItemStack> list1 = Lists.<ItemStack>newArrayList();

               for(int j = 0; j < this.playerEntity.openContainer.inventorySlots.size(); ++j) {
                  ItemStack itemstack = ((Slot)this.playerEntity.openContainer.inventorySlots.get(j)).getStack();
                  ItemStack itemstack1 = itemstack != null && itemstack.stackSize > 0?itemstack:null;
                  list1.add(itemstack1);
               }

               this.playerEntity.updateCraftingInventory(this.playerEntity.openContainer, list1);
            }
         }
      }
   }

   public void processEnchantItem(CPacketEnchantItem packetIn) {
      PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerWorld());
      this.playerEntity.markPlayerActive();
      if(this.playerEntity.openContainer.windowId == packetIn.getWindowId() && this.playerEntity.openContainer.getCanCraft(this.playerEntity) && !this.playerEntity.isSpectator()) {
         this.playerEntity.openContainer.enchantItem(this.playerEntity, packetIn.getButton());
         this.playerEntity.openContainer.detectAndSendChanges();
      }
   }

   public void processCreativeInventoryAction(CPacketCreativeInventoryAction packetIn) {
      PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerWorld());
      if(this.playerEntity.interactionManager.isCreative()) {
         boolean flag = packetIn.getSlotId() < 0;
         ItemStack itemstack = packetIn.getStack();
         if(itemstack != null && itemstack.hasTagCompound() && itemstack.getTagCompound().hasKey("BlockEntityTag", 10)) {
            NBTTagCompound nbttagcompound = itemstack.getTagCompound().getCompoundTag("BlockEntityTag");
            if(nbttagcompound.hasKey("x") && nbttagcompound.hasKey("y") && nbttagcompound.hasKey("z")) {
               BlockPos blockpos = new BlockPos(nbttagcompound.getInteger("x"), nbttagcompound.getInteger("y"), nbttagcompound.getInteger("z"));
               TileEntity tileentity = this.playerEntity.worldObj.getTileEntity(blockpos);
               if(tileentity != null) {
                  NBTTagCompound nbttagcompound1 = tileentity.func_189515_b(new NBTTagCompound());
                  nbttagcompound1.removeTag("x");
                  nbttagcompound1.removeTag("y");
                  nbttagcompound1.removeTag("z");
                  itemstack.setTagInfo("BlockEntityTag", nbttagcompound1);
               }
            }
         }

         boolean flag1 = packetIn.getSlotId() >= 1 && packetIn.getSlotId() <= 45;
         boolean flag2 = itemstack == null || itemstack.getItem() != null;
         boolean flag3 = itemstack == null || itemstack.getMetadata() >= 0 && itemstack.stackSize <= 64 && itemstack.stackSize > 0;
         if(flag1 && flag2 && flag3) {
            if(itemstack == null) {
               this.playerEntity.inventoryContainer.putStackInSlot(packetIn.getSlotId(), (ItemStack)null);
            } else {
               this.playerEntity.inventoryContainer.putStackInSlot(packetIn.getSlotId(), itemstack);
            }

            this.playerEntity.inventoryContainer.setCanCraft(this.playerEntity, true);
         } else if(flag && flag2 && flag3 && this.itemDropThreshold < 200) {
            this.itemDropThreshold += 20;
            EntityItem entityitem = this.playerEntity.dropItem(itemstack, true);
            if(entityitem != null) {
               entityitem.setAgeToCreativeDespawnTime();
            }
         }
      }
   }

   public void processConfirmTransaction(CPacketConfirmTransaction packetIn) {
      PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerWorld());
      Short oshort = (Short)this.pendingTransactions.lookup(this.playerEntity.openContainer.windowId);
      if(oshort != null && packetIn.getUid() == oshort.shortValue() && this.playerEntity.openContainer.windowId == packetIn.getWindowId() && !this.playerEntity.openContainer.getCanCraft(this.playerEntity) && !this.playerEntity.isSpectator()) {
         this.playerEntity.openContainer.setCanCraft(this.playerEntity, true);
      }
   }

   public void processUpdateSign(CPacketUpdateSign packetIn) {
      PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerWorld());
      this.playerEntity.markPlayerActive();
      WorldServer worldserver = this.serverController.worldServerForDimension(this.playerEntity.dimension);
      BlockPos blockpos = packetIn.getPosition();
      if(worldserver.isBlockLoaded(blockpos)) {
         IBlockState iblockstate = worldserver.getBlockState(blockpos);
         TileEntity tileentity = worldserver.getTileEntity(blockpos);
         if(!(tileentity instanceof TileEntitySign)) {
            return;
         }

         TileEntitySign tileentitysign = (TileEntitySign)tileentity;
         if(!tileentitysign.getIsEditable() || tileentitysign.getPlayer() != this.playerEntity) {
            this.serverController.logWarning("Player " + this.playerEntity.getName() + " just tried to change non-editable sign");
            return;
         }

         String[] astring = packetIn.getLines();

         for(int i = 0; i < astring.length; ++i) {
            tileentitysign.signText[i] = new TextComponentString(TextFormatting.getTextWithoutFormattingCodes(astring[i]));
         }

         tileentitysign.markDirty();
         worldserver.notifyBlockUpdate(blockpos, iblockstate, iblockstate, 3);
      }
   }

   public void processKeepAlive(CPacketKeepAlive packetIn) {
      if(packetIn.getKey() == this.keepAliveId) {
         int i = (int)(this.currentTimeMillis() - this.lastPingTime);
         this.playerEntity.ping = (this.playerEntity.ping * 3 + i) / 4;
      }
   }

   private long currentTimeMillis() {
      return System.nanoTime() / 1000000L;
   }

   public void processPlayerAbilities(CPacketPlayerAbilities packetIn) {
      PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerWorld());
      this.playerEntity.capabilities.isFlying = packetIn.isFlying() && this.playerEntity.capabilities.allowFlying;
   }

   public void processTabComplete(CPacketTabComplete packetIn) {
      PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerWorld());
      List<String> list = Lists.<String>newArrayList();

      for(String s : this.serverController.getTabCompletions(this.playerEntity, packetIn.getMessage(), packetIn.getTargetBlock(), packetIn.hasTargetBlock())) {
         list.add(s);
      }

      this.playerEntity.connection.sendPacket(new SPacketTabComplete((String[])list.toArray(new String[list.size()])));
   }

   public void processClientSettings(CPacketClientSettings packetIn) {
      PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerWorld());
      this.playerEntity.handleClientSettings(packetIn);
   }

   public void processCustomPayload(CPacketCustomPayload packetIn) {
      PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerWorld());
      String s = packetIn.getChannelName();
      if("MC|BEdit".equals(s)) {
         PacketBuffer packetbuffer6 = new PacketBuffer(Unpooled.wrappedBuffer((ByteBuf)packetIn.getBufferData()));

         try {
            ItemStack itemstack1 = packetbuffer6.readItemStackFromBuffer();
            if(itemstack1 != null) {
               if(!ItemWritableBook.isNBTValid(itemstack1.getTagCompound())) {
                  throw new IOException("Invalid book tag!");
               }

               ItemStack itemstack3 = this.playerEntity.getHeldItemMainhand();
               if(itemstack3 == null) {
                  return;
               }

               if(itemstack1.getItem() == Items.WRITABLE_BOOK && itemstack1.getItem() == itemstack3.getItem()) {
                  itemstack3.setTagInfo("pages", itemstack1.getTagCompound().getTagList("pages", 8));
               }

               return;
            }
         } catch (Exception exception6) {
            LOGGER.error((String)"Couldn\'t handle book info", (Throwable)exception6);
            return;
         } finally {
            packetbuffer6.release();
         }

         return;
      } else if("MC|BSign".equals(s)) {
         PacketBuffer packetbuffer5 = new PacketBuffer(Unpooled.wrappedBuffer((ByteBuf)packetIn.getBufferData()));

         try {
            ItemStack itemstack = packetbuffer5.readItemStackFromBuffer();
            if(itemstack != null) {
               if(!ItemWrittenBook.validBookTagContents(itemstack.getTagCompound())) {
                  throw new IOException("Invalid book tag!");
               }

               ItemStack itemstack2 = this.playerEntity.getHeldItemMainhand();
               if(itemstack2 == null) {
                  return;
               }

               if(itemstack.getItem() == Items.WRITABLE_BOOK && itemstack2.getItem() == Items.WRITABLE_BOOK) {
                  itemstack2.setTagInfo("author", new NBTTagString(this.playerEntity.getName()));
                  itemstack2.setTagInfo("title", new NBTTagString(itemstack.getTagCompound().getString("title")));
                  NBTTagList nbttaglist = itemstack.getTagCompound().getTagList("pages", 8);

                  for(int j1 = 0; j1 < nbttaglist.tagCount(); ++j1) {
                     String s4 = nbttaglist.getStringTagAt(j1);
                     ITextComponent itextcomponent = new TextComponentString(s4);
                     s4 = ITextComponent.Serializer.componentToJson(itextcomponent);
                     nbttaglist.set(j1, new NBTTagString(s4));
                  }

                  itemstack2.setTagInfo("pages", nbttaglist);
                  itemstack2.setItem(Items.WRITTEN_BOOK);
               }

               return;
            }
         } catch (Exception exception7) {
            LOGGER.error((String)"Couldn\'t sign book", (Throwable)exception7);
            return;
         } finally {
            packetbuffer5.release();
         }

         return;
      } else if("MC|TrSel".equals(s)) {
         try {
            int i = packetIn.getBufferData().readInt();
            Container container = this.playerEntity.openContainer;
            if(container instanceof ContainerMerchant) {
               ((ContainerMerchant)container).setCurrentRecipeIndex(i);
            }
         } catch (Exception exception5) {
            LOGGER.error((String)"Couldn\'t select trade", (Throwable)exception5);
         }
      } else if("MC|AdvCmd".equals(s)) {
         if(!this.serverController.isCommandBlockEnabled()) {
            this.playerEntity.addChatMessage(new TextComponentTranslation("advMode.notEnabled", new Object[0]));
            return;
         }

         if(!this.playerEntity.canCommandSenderUseCommand(2, "") || !this.playerEntity.capabilities.isCreativeMode) {
            this.playerEntity.addChatMessage(new TextComponentTranslation("advMode.notAllowed", new Object[0]));
            return;
         }

         PacketBuffer packetbuffer = packetIn.getBufferData();

         try {
            int j = packetbuffer.readByte();
            CommandBlockBaseLogic commandblockbaselogic = null;
            if(j == 0) {
               TileEntity tileentity = this.playerEntity.worldObj.getTileEntity(new BlockPos(packetbuffer.readInt(), packetbuffer.readInt(), packetbuffer.readInt()));
               if(tileentity instanceof TileEntityCommandBlock) {
                  commandblockbaselogic = ((TileEntityCommandBlock)tileentity).getCommandBlockLogic();
               }
            } else if(j == 1) {
               Entity entity = this.playerEntity.worldObj.getEntityByID(packetbuffer.readInt());
               if(entity instanceof EntityMinecartCommandBlock) {
                  commandblockbaselogic = ((EntityMinecartCommandBlock)entity).getCommandBlockLogic();
               }
            }

            String s3 = packetbuffer.readStringFromBuffer(packetbuffer.readableBytes());
            boolean flag = packetbuffer.readBoolean();
            if(commandblockbaselogic != null) {
               commandblockbaselogic.setCommand(s3);
               commandblockbaselogic.setTrackOutput(flag);
               if(!flag) {
                  commandblockbaselogic.setLastOutput((ITextComponent)null);
               }

               commandblockbaselogic.updateCommand();
               this.playerEntity.addChatMessage(new TextComponentTranslation("advMode.setCommand.success", new Object[]{s3}));
            }
         } catch (Exception exception4) {
            LOGGER.error((String)"Couldn\'t set command block", (Throwable)exception4);
         } finally {
            packetbuffer.release();
         }
      } else if("MC|AutoCmd".equals(s)) {
         if(!this.serverController.isCommandBlockEnabled()) {
            this.playerEntity.addChatMessage(new TextComponentTranslation("advMode.notEnabled", new Object[0]));
            return;
         }

         if(!this.playerEntity.canCommandSenderUseCommand(2, "") || !this.playerEntity.capabilities.isCreativeMode) {
            this.playerEntity.addChatMessage(new TextComponentTranslation("advMode.notAllowed", new Object[0]));
            return;
         }

         PacketBuffer packetbuffer1 = packetIn.getBufferData();

         try {
            CommandBlockBaseLogic commandblockbaselogic1 = null;
            TileEntityCommandBlock tileentitycommandblock = null;
            BlockPos blockpos1 = new BlockPos(packetbuffer1.readInt(), packetbuffer1.readInt(), packetbuffer1.readInt());
            TileEntity tileentity2 = this.playerEntity.worldObj.getTileEntity(blockpos1);
            if(tileentity2 instanceof TileEntityCommandBlock) {
               tileentitycommandblock = (TileEntityCommandBlock)tileentity2;
               commandblockbaselogic1 = tileentitycommandblock.getCommandBlockLogic();
            }

            String s1 = packetbuffer1.readStringFromBuffer(packetbuffer1.readableBytes());
            boolean flag1 = packetbuffer1.readBoolean();
            TileEntityCommandBlock.Mode tileentitycommandblock$mode = TileEntityCommandBlock.Mode.valueOf(packetbuffer1.readStringFromBuffer(16));
            boolean flag2 = packetbuffer1.readBoolean();
            boolean flag3 = packetbuffer1.readBoolean();
            if(commandblockbaselogic1 != null) {
               EnumFacing enumfacing = (EnumFacing)this.playerEntity.worldObj.getBlockState(blockpos1).getValue(BlockCommandBlock.FACING);
               switch(tileentitycommandblock$mode) {
               case SEQUENCE:
                  IBlockState iblockstate3 = Blocks.CHAIN_COMMAND_BLOCK.getDefaultState();
                  this.playerEntity.worldObj.setBlockState(blockpos1, iblockstate3.withProperty(BlockCommandBlock.FACING, enumfacing).withProperty(BlockCommandBlock.CONDITIONAL, Boolean.valueOf(flag2)), 2);
                  break;
               case AUTO:
                  IBlockState iblockstate2 = Blocks.REPEATING_COMMAND_BLOCK.getDefaultState();
                  this.playerEntity.worldObj.setBlockState(blockpos1, iblockstate2.withProperty(BlockCommandBlock.FACING, enumfacing).withProperty(BlockCommandBlock.CONDITIONAL, Boolean.valueOf(flag2)), 2);
                  break;
               case REDSTONE:
                  IBlockState lvt_14_1_ = Blocks.COMMAND_BLOCK.getDefaultState();
                  this.playerEntity.worldObj.setBlockState(blockpos1, lvt_14_1_.withProperty(BlockCommandBlock.FACING, enumfacing).withProperty(BlockCommandBlock.CONDITIONAL, Boolean.valueOf(flag2)), 2);
               }

               tileentity2.validate();
               this.playerEntity.worldObj.setTileEntity(blockpos1, tileentity2);
               commandblockbaselogic1.setCommand(s1);
               commandblockbaselogic1.setTrackOutput(flag1);
               if(!flag1) {
                  commandblockbaselogic1.setLastOutput((ITextComponent)null);
               }

               tileentitycommandblock.setAuto(flag3);
               commandblockbaselogic1.updateCommand();
               if(!net.minecraft.util.StringUtils.isNullOrEmpty(s1)) {
                  this.playerEntity.addChatMessage(new TextComponentTranslation("advMode.setCommand.success", new Object[]{s1}));
               }
            }
         } catch (Exception exception3) {
            LOGGER.error((String)"Couldn\'t set command block", (Throwable)exception3);
         } finally {
            packetbuffer1.release();
         }
      } else if("MC|Beacon".equals(s)) {
         if(this.playerEntity.openContainer instanceof ContainerBeacon) {
            try {
               PacketBuffer packetbuffer2 = packetIn.getBufferData();
               int k = packetbuffer2.readInt();
               int i1 = packetbuffer2.readInt();
               ContainerBeacon containerbeacon = (ContainerBeacon)this.playerEntity.openContainer;
               Slot slot = containerbeacon.getSlot(0);
               if(slot.getHasStack()) {
                  slot.decrStackSize(1);
                  IInventory iinventory = containerbeacon.getTileEntity();
                  iinventory.setField(1, k);
                  iinventory.setField(2, i1);
                  iinventory.markDirty();
               }
            } catch (Exception exception2) {
               LOGGER.error((String)"Couldn\'t set beacon", (Throwable)exception2);
            }
         }
      } else if("MC|ItemName".equals(s)) {
         if(this.playerEntity.openContainer instanceof ContainerRepair) {
            ContainerRepair containerrepair = (ContainerRepair)this.playerEntity.openContainer;
            if(packetIn.getBufferData() != null && packetIn.getBufferData().readableBytes() >= 1) {
               String s2 = ChatAllowedCharacters.filterAllowedCharacters(packetIn.getBufferData().readStringFromBuffer(32767));
               if(s2.length() <= 30) {
                  containerrepair.updateItemName(s2);
               }
            } else {
               containerrepair.updateItemName("");
            }
         }
      } else if("MC|Struct".equals(s)) {
         PacketBuffer packetbuffer3 = packetIn.getBufferData();

         try {
            if(this.playerEntity.canCommandSenderUseCommand(4, "") && this.playerEntity.capabilities.isCreativeMode) {
               BlockPos blockpos = new BlockPos(packetbuffer3.readInt(), packetbuffer3.readInt(), packetbuffer3.readInt());
               IBlockState iblockstate1 = this.playerEntity.worldObj.getBlockState(blockpos);
               TileEntity tileentity1 = this.playerEntity.worldObj.getTileEntity(blockpos);
               if(tileentity1 instanceof TileEntityStructure) {
                  TileEntityStructure tileentitystructure = (TileEntityStructure)tileentity1;
                  int k1 = packetbuffer3.readByte();
                  String s5 = packetbuffer3.readStringFromBuffer(32);
                  tileentitystructure.setMode(TileEntityStructure.Mode.valueOf(s5));
                  tileentitystructure.setName(packetbuffer3.readStringFromBuffer(64));
                  tileentitystructure.setPosition(new BlockPos(packetbuffer3.readInt(), packetbuffer3.readInt(), packetbuffer3.readInt()));
                  tileentitystructure.setSize(new BlockPos(packetbuffer3.readInt(), packetbuffer3.readInt(), packetbuffer3.readInt()));
                  String s6 = packetbuffer3.readStringFromBuffer(32);
                  tileentitystructure.setMirror(Mirror.valueOf(s6));
                  String s7 = packetbuffer3.readStringFromBuffer(32);
                  tileentitystructure.setRotation(Rotation.valueOf(s7));
                  tileentitystructure.setMetadata(packetbuffer3.readStringFromBuffer(128));
                  tileentitystructure.setIgnoresEntities(packetbuffer3.readBoolean());
                  if(k1 == 2) {
                     if(tileentitystructure.save()) {
                        this.playerEntity.addChatComponentMessage(new TextComponentString("Structure saved"));
                     } else {
                        this.playerEntity.addChatComponentMessage(new TextComponentString("Structure NOT saved"));
                     }
                  } else if(k1 == 3) {
                     if(tileentitystructure.load()) {
                        this.playerEntity.addChatComponentMessage(new TextComponentString("Structure loaded"));
                     } else {
                        this.playerEntity.addChatComponentMessage(new TextComponentString("Structure prepared"));
                     }
                  } else if(k1 == 4 && tileentitystructure.detectSize()) {
                     this.playerEntity.addChatComponentMessage(new TextComponentString("Size detected"));
                  }

                  tileentitystructure.markDirty();
                  this.playerEntity.worldObj.notifyBlockUpdate(blockpos, iblockstate1, iblockstate1, 3);
               }
            }
         } catch (Exception exception1) {
            LOGGER.error((String)"Couldn\'t set structure block", (Throwable)exception1);
         } finally {
            packetbuffer3.release();
         }
      } else if("MC|PickItem".equals(s)) {
         PacketBuffer packetbuffer4 = packetIn.getBufferData();

         try {
            int l = packetbuffer4.readVarIntFromBuffer();
            this.playerEntity.inventory.pickItem(l);
            this.playerEntity.connection.sendPacket(new SPacketSetSlot(-2, this.playerEntity.inventory.currentItem, this.playerEntity.inventory.getStackInSlot(this.playerEntity.inventory.currentItem)));
            this.playerEntity.connection.sendPacket(new SPacketSetSlot(-2, l, this.playerEntity.inventory.getStackInSlot(l)));
            this.playerEntity.connection.sendPacket(new SPacketHeldItemChange(this.playerEntity.inventory.currentItem));
         } catch (Exception exception) {
            LOGGER.error((String)"Couldn\'t pick item", (Throwable)exception);
         } finally {
            packetbuffer4.release();
         }
      }
   }
}
