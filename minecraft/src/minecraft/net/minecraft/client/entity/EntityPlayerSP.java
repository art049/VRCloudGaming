package net.minecraft.client.entity;

import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ElytraSound;
import net.minecraft.client.audio.MovingSoundMinecartRiding;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiCommandBlock;
import net.minecraft.client.gui.GuiEnchantment;
import net.minecraft.client.gui.GuiHopper;
import net.minecraft.client.gui.GuiMerchant;
import net.minecraft.client.gui.GuiRepair;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenBook;
import net.minecraft.client.gui.inventory.GuiBeacon;
import net.minecraft.client.gui.inventory.GuiBrewingStand;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.client.gui.inventory.GuiDispenser;
import net.minecraft.client.gui.inventory.GuiEditCommandBlockMinecart;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.client.gui.inventory.GuiFurnace;
import net.minecraft.client.gui.inventory.GuiScreenHorseInventory;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IJumpingMount;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.client.CPacketClientStatus;
import net.minecraft.network.play.client.CPacketCloseWindow;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketInput;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerAbilities;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketVehicleMove;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatisticsManager;
import net.minecraft.tileentity.CommandBlockBaseLogic;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MovementInput;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.World;

public class EntityPlayerSP extends AbstractClientPlayer {
   public final NetHandlerPlayClient connection;
   private final StatisticsManager statWriter;
   private int permissionLevel = 0;
   private double lastReportedPosX;
   private double lastReportedPosY;
   private double lastReportedPosZ;
   private float lastReportedYaw;
   private float lastReportedPitch;
   private boolean prevOnGround;
   private boolean serverSneakState;
   private boolean serverSprintState;
   private int positionUpdateTicks;
   private boolean hasValidHealth;
   private String serverBrand;
   public MovementInput movementInput;
   protected Minecraft mc;
   protected int sprintToggleTimer;
   public int sprintingTicksLeft;
   public float renderArmYaw;
   public float renderArmPitch;
   public float prevRenderArmYaw;
   public float prevRenderArmPitch;
   private int horseJumpPowerCounter;
   private float horseJumpPower;
   public float timeInPortal;
   public float prevTimeInPortal;
   private boolean handActive;
   private EnumHand activeHand;
   private boolean rowingBoat;

   public EntityPlayerSP(Minecraft mcIn, World worldIn, NetHandlerPlayClient netHandler, StatisticsManager statFile) {
      super(worldIn, netHandler.getGameProfile());
      this.connection = netHandler;
      this.statWriter = statFile;
      this.mc = mcIn;
      this.dimension = 0;
   }

   public boolean attackEntityFrom(DamageSource source, float amount) {
      return false;
   }

   public void heal(float healAmount) {
   }

   public boolean startRiding(Entity entityIn, boolean force) {
      if(!super.startRiding(entityIn, force)) {
         return false;
      } else {
         if(entityIn instanceof EntityMinecart) {
            this.mc.getSoundHandler().playSound(new MovingSoundMinecartRiding(this, (EntityMinecart)entityIn));
         }

         if(entityIn instanceof EntityBoat) {
            this.prevRotationYaw = entityIn.rotationYaw;
            this.rotationYaw = entityIn.rotationYaw;
            this.setRotationYawHead(entityIn.rotationYaw);
         }

         return true;
      }
   }

   public void dismountRidingEntity() {
      super.dismountRidingEntity();
      this.rowingBoat = false;
   }

   public void onUpdate() {
      if(this.worldObj.isBlockLoaded(new BlockPos(this.posX, 0.0D, this.posZ))) {
         super.onUpdate();
         if(this.isRiding()) {
            this.connection.sendPacket(new CPacketPlayer.Rotation(this.rotationYaw, this.rotationPitch, this.onGround));
            this.connection.sendPacket(new CPacketInput(this.moveStrafing, this.moveForward, this.movementInput.jump, this.movementInput.sneak));
            Entity entity = this.getLowestRidingEntity();
            if(entity != this && entity.canPassengerSteer()) {
               this.connection.sendPacket(new CPacketVehicleMove(entity));
            }
         } else {
            this.onUpdateWalkingPlayer();
         }
      }
   }

   public void onUpdateWalkingPlayer() {
      boolean flag = this.isSprinting();
      if(flag != this.serverSprintState) {
         if(flag) {
            this.connection.sendPacket(new CPacketEntityAction(this, CPacketEntityAction.Action.START_SPRINTING));
         } else {
            this.connection.sendPacket(new CPacketEntityAction(this, CPacketEntityAction.Action.STOP_SPRINTING));
         }

         this.serverSprintState = flag;
      }

      boolean flag1 = this.isSneaking();
      if(flag1 != this.serverSneakState) {
         if(flag1) {
            this.connection.sendPacket(new CPacketEntityAction(this, CPacketEntityAction.Action.START_SNEAKING));
         } else {
            this.connection.sendPacket(new CPacketEntityAction(this, CPacketEntityAction.Action.STOP_SNEAKING));
         }

         this.serverSneakState = flag1;
      }

      if(this.isCurrentViewEntity()) {
         AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
         double d0 = this.posX - this.lastReportedPosX;
         double d1 = axisalignedbb.minY - this.lastReportedPosY;
         double d2 = this.posZ - this.lastReportedPosZ;
         double d3 = (double)(this.rotationYaw - this.lastReportedYaw);
         double d4 = (double)(this.rotationPitch - this.lastReportedPitch);
         ++this.positionUpdateTicks;
         boolean flag2 = d0 * d0 + d1 * d1 + d2 * d2 > 9.0E-4D || this.positionUpdateTicks >= 20;
         boolean flag3 = d3 != 0.0D || d4 != 0.0D;
         if(this.isRiding()) {
            this.connection.sendPacket(new CPacketPlayer.PositionRotation(this.motionX, -999.0D, this.motionZ, this.rotationYaw, this.rotationPitch, this.onGround));
            flag2 = false;
         } else if(flag2 && flag3) {
            this.connection.sendPacket(new CPacketPlayer.PositionRotation(this.posX, axisalignedbb.minY, this.posZ, this.rotationYaw, this.rotationPitch, this.onGround));
         } else if(flag2) {
            this.connection.sendPacket(new CPacketPlayer.Position(this.posX, axisalignedbb.minY, this.posZ, this.onGround));
         } else if(flag3) {
            this.connection.sendPacket(new CPacketPlayer.Rotation(this.rotationYaw, this.rotationPitch, this.onGround));
         } else if(this.prevOnGround != this.onGround) {
            this.connection.sendPacket(new CPacketPlayer(this.onGround));
         }

         if(flag2) {
            this.lastReportedPosX = this.posX;
            this.lastReportedPosY = axisalignedbb.minY;
            this.lastReportedPosZ = this.posZ;
            this.positionUpdateTicks = 0;
         }

         if(flag3) {
            this.lastReportedYaw = this.rotationYaw;
            this.lastReportedPitch = this.rotationPitch;
         }

         this.prevOnGround = this.onGround;
      }
   }

   @Nullable
   public EntityItem dropItem(boolean dropAll) {
      CPacketPlayerDigging.Action cpacketplayerdigging$action = dropAll?CPacketPlayerDigging.Action.DROP_ALL_ITEMS:CPacketPlayerDigging.Action.DROP_ITEM;
      this.connection.sendPacket(new CPacketPlayerDigging(cpacketplayerdigging$action, BlockPos.ORIGIN, EnumFacing.DOWN));
      return null;
   }

   @Nullable
   protected ItemStack dropItemAndGetStack(EntityItem p_184816_1_) {
      return null;
   }

   public void sendChatMessage(String message) {
      this.connection.sendPacket(new CPacketChatMessage(message));
   }

   public void swingArm(EnumHand hand) {
      super.swingArm(hand);
      this.connection.sendPacket(new CPacketAnimation(hand));
   }

   public void respawnPlayer() {
      this.connection.sendPacket(new CPacketClientStatus(CPacketClientStatus.State.PERFORM_RESPAWN));
   }

   protected void damageEntity(DamageSource damageSrc, float damageAmount) {
      if(!this.isEntityInvulnerable(damageSrc)) {
         this.setHealth(this.getHealth() - damageAmount);
      }
   }

   public void closeScreen() {
      this.connection.sendPacket(new CPacketCloseWindow(this.openContainer.windowId));
      this.closeScreenAndDropStack();
   }

   public void closeScreenAndDropStack() {
      this.inventory.setItemStack((ItemStack)null);
      super.closeScreen();
      this.mc.displayGuiScreen((GuiScreen)null);
   }

   public void setPlayerSPHealth(float health) {
      if(this.hasValidHealth) {
         float f = this.getHealth() - health;
         if(f <= 0.0F) {
            this.setHealth(health);
            if(f < 0.0F) {
               this.hurtResistantTime = this.maxHurtResistantTime / 2;
            }
         } else {
            this.lastDamage = f;
            this.setHealth(this.getHealth());
            this.hurtResistantTime = this.maxHurtResistantTime;
            this.damageEntity(DamageSource.generic, f);
            this.hurtTime = this.maxHurtTime = 10;
         }
      } else {
         this.setHealth(health);
         this.hasValidHealth = true;
      }
   }

   public void addStat(StatBase stat, int amount) {
      if(stat != null) {
         if(stat.isIndependent) {
            super.addStat(stat, amount);
         }
      }
   }

   public void sendPlayerAbilities() {
      this.connection.sendPacket(new CPacketPlayerAbilities(this.capabilities));
   }

   public boolean isUser() {
      return true;
   }

   protected void sendHorseJump() {
      this.connection.sendPacket(new CPacketEntityAction(this, CPacketEntityAction.Action.START_RIDING_JUMP, MathHelper.floor_float(this.getHorseJumpPower() * 100.0F)));
   }

   public void sendHorseInventory() {
      this.connection.sendPacket(new CPacketEntityAction(this, CPacketEntityAction.Action.OPEN_INVENTORY));
   }

   public void setServerBrand(String brand) {
      this.serverBrand = brand;
   }

   public String getServerBrand() {
      return this.serverBrand;
   }

   public StatisticsManager getStatFileWriter() {
      return this.statWriter;
   }

   public int getPermissionLevel() {
      return this.permissionLevel;
   }

   public void setPermissionLevel(int p_184839_1_) {
      this.permissionLevel = p_184839_1_;
   }

   public void addChatComponentMessage(ITextComponent chatComponent) {
      this.mc.ingameGUI.getChatGUI().printChatMessage(chatComponent);
   }

   protected boolean pushOutOfBlocks(double x, double y, double z) {
      if(this.noClip) {
         return false;
      } else {
         BlockPos blockpos = new BlockPos(x, y, z);
         double d0 = x - (double)blockpos.getX();
         double d1 = z - (double)blockpos.getZ();
         if(!this.isOpenBlockSpace(blockpos)) {
            int i = -1;
            double d2 = 9999.0D;
            if(this.isOpenBlockSpace(blockpos.west()) && d0 < d2) {
               d2 = d0;
               i = 0;
            }

            if(this.isOpenBlockSpace(blockpos.east()) && 1.0D - d0 < d2) {
               d2 = 1.0D - d0;
               i = 1;
            }

            if(this.isOpenBlockSpace(blockpos.north()) && d1 < d2) {
               d2 = d1;
               i = 4;
            }

            if(this.isOpenBlockSpace(blockpos.south()) && 1.0D - d1 < d2) {
               d2 = 1.0D - d1;
               i = 5;
            }

            float f = 0.1F;
            if(i == 0) {
               this.motionX = (double)(-f);
            }

            if(i == 1) {
               this.motionX = (double)f;
            }

            if(i == 4) {
               this.motionZ = (double)(-f);
            }

            if(i == 5) {
               this.motionZ = (double)f;
            }
         }

         return false;
      }
   }

   private boolean isOpenBlockSpace(BlockPos pos) {
      return !this.worldObj.getBlockState(pos).isNormalCube() && !this.worldObj.getBlockState(pos.up()).isNormalCube();
   }

   public void setSprinting(boolean sprinting) {
      super.setSprinting(sprinting);
      this.sprintingTicksLeft = 0;
   }

   public void setXPStats(float currentXP, int maxXP, int level) {
      this.experience = currentXP;
      this.experienceTotal = maxXP;
      this.experienceLevel = level;
   }

   public void addChatMessage(ITextComponent component) {
      this.mc.ingameGUI.getChatGUI().printChatMessage(component);
   }

   public boolean canCommandSenderUseCommand(int permLevel, String commandName) {
      return permLevel <= this.getPermissionLevel();
   }

   public void handleStatusUpdate(byte id) {
      if(id >= 24 && id <= 28) {
         this.setPermissionLevel(id - 24);
      } else {
         super.handleStatusUpdate(id);
      }
   }

   public BlockPos getPosition() {
      return new BlockPos(this.posX + 0.5D, this.posY + 0.5D, this.posZ + 0.5D);
   }

   public void playSound(SoundEvent soundIn, float volume, float pitch) {
      this.worldObj.playSound(this.posX, this.posY, this.posZ, soundIn, this.getSoundCategory(), volume, pitch, false);
   }

   public boolean isServerWorld() {
      return true;
   }

   public void setActiveHand(EnumHand hand) {
      ItemStack itemstack = this.getHeldItem(hand);
      if(itemstack != null && !this.isHandActive()) {
         super.setActiveHand(hand);
         this.handActive = true;
         this.activeHand = hand;
      }
   }

   public boolean isHandActive() {
      return this.handActive;
   }

   public void resetActiveHand() {
      super.resetActiveHand();
      this.handActive = false;
   }

   public EnumHand getActiveHand() {
      return this.activeHand;
   }

   public void notifyDataManagerChange(DataParameter<?> key) {
      super.notifyDataManagerChange(key);
      if(HAND_STATES.equals(key)) {
         boolean flag = (((Byte)this.dataManager.get(HAND_STATES)).byteValue() & 1) > 0;
         EnumHand enumhand = (((Byte)this.dataManager.get(HAND_STATES)).byteValue() & 2) > 0?EnumHand.OFF_HAND:EnumHand.MAIN_HAND;
         if(flag && !this.handActive) {
            this.setActiveHand(enumhand);
         } else if(!flag && this.handActive) {
            this.resetActiveHand();
         }
      }
   }

   public boolean isRidingHorse() {
      Entity entity = this.getRidingEntity();
      return this.isRiding() && entity instanceof IJumpingMount && ((IJumpingMount)entity).canJump();
   }

   public float getHorseJumpPower() {
      return this.horseJumpPower;
   }

   public void openEditSign(TileEntitySign signTile) {
      this.mc.displayGuiScreen(new GuiEditSign(signTile));
   }

   public void displayGuiEditCommandCart(CommandBlockBaseLogic p_184809_1_) {
      if(this.canCommandSenderUseCommand(2, "")) {
         this.mc.displayGuiScreen(new GuiEditCommandBlockMinecart(p_184809_1_));
      }
   }

   public void displayGuiCommandBlock(TileEntityCommandBlock p_184824_1_) {
      if(this.canCommandSenderUseCommand(2, "")) {
         this.mc.displayGuiScreen(new GuiCommandBlock(p_184824_1_));
      }
   }

   public void openBook(ItemStack stack, EnumHand hand) {
      Item item = stack.getItem();
      if(item == Items.WRITABLE_BOOK) {
         this.mc.displayGuiScreen(new GuiScreenBook(this, stack, true));
      }
   }

   public void displayGUIChest(IInventory chestInventory) {
      String s = chestInventory instanceof IInteractionObject?((IInteractionObject)chestInventory).getGuiID():"minecraft:container";
      if("minecraft:chest".equals(s)) {
         this.mc.displayGuiScreen(new GuiChest(this.inventory, chestInventory));
      } else if("minecraft:hopper".equals(s)) {
         this.mc.displayGuiScreen(new GuiHopper(this.inventory, chestInventory));
      } else if("minecraft:furnace".equals(s)) {
         this.mc.displayGuiScreen(new GuiFurnace(this.inventory, chestInventory));
      } else if("minecraft:brewing_stand".equals(s)) {
         this.mc.displayGuiScreen(new GuiBrewingStand(this.inventory, chestInventory));
      } else if("minecraft:beacon".equals(s)) {
         this.mc.displayGuiScreen(new GuiBeacon(this.inventory, chestInventory));
      } else if(!"minecraft:dispenser".equals(s) && !"minecraft:dropper".equals(s)) {
         this.mc.displayGuiScreen(new GuiChest(this.inventory, chestInventory));
      } else {
         this.mc.displayGuiScreen(new GuiDispenser(this.inventory, chestInventory));
      }
   }

   public void openGuiHorseInventory(EntityHorse horse, IInventory inventoryIn) {
      this.mc.displayGuiScreen(new GuiScreenHorseInventory(this.inventory, inventoryIn, horse));
   }

   public void displayGui(IInteractionObject guiOwner) {
      String s = guiOwner.getGuiID();
      if("minecraft:crafting_table".equals(s)) {
         this.mc.displayGuiScreen(new GuiCrafting(this.inventory, this.worldObj));
      } else if("minecraft:enchanting_table".equals(s)) {
         this.mc.displayGuiScreen(new GuiEnchantment(this.inventory, this.worldObj, guiOwner));
      } else if("minecraft:anvil".equals(s)) {
         this.mc.displayGuiScreen(new GuiRepair(this.inventory, this.worldObj));
      }
   }

   public void displayVillagerTradeGui(IMerchant villager) {
      this.mc.displayGuiScreen(new GuiMerchant(this.inventory, villager, this.worldObj));
   }

   public void onCriticalHit(Entity entityHit) {
      this.mc.effectRenderer.emitParticleAtEntity(entityHit, EnumParticleTypes.CRIT);
   }

   public void onEnchantmentCritical(Entity entityHit) {
      this.mc.effectRenderer.emitParticleAtEntity(entityHit, EnumParticleTypes.CRIT_MAGIC);
   }

   public boolean isSneaking() {
      boolean flag = this.movementInput != null?this.movementInput.sneak:false;
      return flag && !this.sleeping;
   }

   public void updateEntityActionState() {
      super.updateEntityActionState();
      if(this.isCurrentViewEntity()) {
         this.moveStrafing = this.movementInput.moveStrafe;
         this.moveForward = this.movementInput.moveForward;
         this.isJumping = this.movementInput.jump;
         this.prevRenderArmYaw = this.renderArmYaw;
         this.prevRenderArmPitch = this.renderArmPitch;
         this.renderArmPitch = (float)((double)this.renderArmPitch + (double)(this.rotationPitch - this.renderArmPitch) * 0.5D);
         this.renderArmYaw = (float)((double)this.renderArmYaw + (double)(this.rotationYaw - this.renderArmYaw) * 0.5D);
      }
   }

   protected boolean isCurrentViewEntity() {
      return this.mc.getRenderViewEntity() == this;
   }

   public void onLivingUpdate() {
      ++this.sprintingTicksLeft;
      if(this.sprintToggleTimer > 0) {
         --this.sprintToggleTimer;
      }

      this.prevTimeInPortal = this.timeInPortal;
      if(this.inPortal) {
         if(this.mc.currentScreen != null && !this.mc.currentScreen.doesGuiPauseGame()) {
            this.mc.displayGuiScreen((GuiScreen)null);
         }

         if(this.timeInPortal == 0.0F) {
            this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.BLOCK_PORTAL_TRIGGER, this.rand.nextFloat() * 0.4F + 0.8F));
         }

         this.timeInPortal += 0.0125F;
         if(this.timeInPortal >= 1.0F) {
            this.timeInPortal = 1.0F;
         }

         this.inPortal = false;
      } else if(this.isPotionActive(MobEffects.NAUSEA) && this.getActivePotionEffect(MobEffects.NAUSEA).getDuration() > 60) {
         this.timeInPortal += 0.006666667F;
         if(this.timeInPortal > 1.0F) {
            this.timeInPortal = 1.0F;
         }
      } else {
         if(this.timeInPortal > 0.0F) {
            this.timeInPortal -= 0.05F;
         }

         if(this.timeInPortal < 0.0F) {
            this.timeInPortal = 0.0F;
         }
      }

      if(this.timeUntilPortal > 0) {
         --this.timeUntilPortal;
      }

      boolean flag = this.movementInput.jump;
      boolean flag1 = this.movementInput.sneak;
      float f = 0.8F;
      boolean flag2 = this.movementInput.moveForward >= f;
      this.movementInput.updatePlayerMoveState();
      if(this.isHandActive() && !this.isRiding()) {
         this.movementInput.moveStrafe *= 0.2F;
         this.movementInput.moveForward *= 0.2F;
         this.sprintToggleTimer = 0;
      }

      AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
      this.pushOutOfBlocks(this.posX - (double)this.width * 0.35D, axisalignedbb.minY + 0.5D, this.posZ + (double)this.width * 0.35D);
      this.pushOutOfBlocks(this.posX - (double)this.width * 0.35D, axisalignedbb.minY + 0.5D, this.posZ - (double)this.width * 0.35D);
      this.pushOutOfBlocks(this.posX + (double)this.width * 0.35D, axisalignedbb.minY + 0.5D, this.posZ - (double)this.width * 0.35D);
      this.pushOutOfBlocks(this.posX + (double)this.width * 0.35D, axisalignedbb.minY + 0.5D, this.posZ + (double)this.width * 0.35D);
      boolean flag3 = (float)this.getFoodStats().getFoodLevel() > 6.0F || this.capabilities.allowFlying;
      if(this.onGround && !flag1 && !flag2 && this.movementInput.moveForward >= f && !this.isSprinting() && flag3 && !this.isHandActive() && !this.isPotionActive(MobEffects.BLINDNESS)) {
         if(this.sprintToggleTimer <= 0 && !this.mc.gameSettings.keyBindSprint.isKeyDown()) {
            this.sprintToggleTimer = 7;
         } else {
            this.setSprinting(true);
         }
      }

      if(!this.isSprinting() && this.movementInput.moveForward >= f && flag3 && !this.isHandActive() && !this.isPotionActive(MobEffects.BLINDNESS) && this.mc.gameSettings.keyBindSprint.isKeyDown()) {
         this.setSprinting(true);
      }

      if(this.isSprinting() && (this.movementInput.moveForward < f || this.isCollidedHorizontally || !flag3)) {
         this.setSprinting(false);
      }

      if(this.capabilities.allowFlying) {
         if(this.mc.playerController.isSpectatorMode()) {
            if(!this.capabilities.isFlying) {
               this.capabilities.isFlying = true;
               this.sendPlayerAbilities();
            }
         } else if(!flag && this.movementInput.jump) {
            if(this.flyToggleTimer == 0) {
               this.flyToggleTimer = 7;
            } else {
               this.capabilities.isFlying = !this.capabilities.isFlying;
               this.sendPlayerAbilities();
               this.flyToggleTimer = 0;
            }
         }
      }

      if(this.movementInput.jump && !flag && !this.onGround && this.motionY < 0.0D && !this.isElytraFlying() && !this.capabilities.isFlying) {
         ItemStack itemstack = this.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
         if(itemstack != null && itemstack.getItem() == Items.ELYTRA && ItemElytra.isBroken(itemstack)) {
            this.connection.sendPacket(new CPacketEntityAction(this, CPacketEntityAction.Action.START_FALL_FLYING));
            this.mc.getSoundHandler().playSound(new ElytraSound(this));
         }
      }

      if(this.capabilities.isFlying && this.isCurrentViewEntity()) {
         if(this.movementInput.sneak) {
            this.movementInput.moveStrafe = (float)((double)this.movementInput.moveStrafe / 0.3D);
            this.movementInput.moveForward = (float)((double)this.movementInput.moveForward / 0.3D);
            this.motionY -= (double)(this.capabilities.getFlySpeed() * 3.0F);
         }

         if(this.movementInput.jump) {
            this.motionY += (double)(this.capabilities.getFlySpeed() * 3.0F);
         }
      }

      if(this.isRidingHorse()) {
         IJumpingMount ijumpingmount = (IJumpingMount)this.getRidingEntity();
         if(this.horseJumpPowerCounter < 0) {
            ++this.horseJumpPowerCounter;
            if(this.horseJumpPowerCounter == 0) {
               this.horseJumpPower = 0.0F;
            }
         }

         if(flag && !this.movementInput.jump) {
            this.horseJumpPowerCounter = -10;
            ijumpingmount.setJumpPower(MathHelper.floor_float(this.getHorseJumpPower() * 100.0F));
            this.sendHorseJump();
         } else if(!flag && this.movementInput.jump) {
            this.horseJumpPowerCounter = 0;
            this.horseJumpPower = 0.0F;
         } else if(flag) {
            ++this.horseJumpPowerCounter;
            if(this.horseJumpPowerCounter < 10) {
               this.horseJumpPower = (float)this.horseJumpPowerCounter * 0.1F;
            } else {
               this.horseJumpPower = 0.8F + 2.0F / (float)(this.horseJumpPowerCounter - 9) * 0.1F;
            }
         }
      } else {
         this.horseJumpPower = 0.0F;
      }

      super.onLivingUpdate();
      if(this.onGround && this.capabilities.isFlying && !this.mc.playerController.isSpectatorMode()) {
         this.capabilities.isFlying = false;
         this.sendPlayerAbilities();
      }
   }

   public void updateRidden() {
      super.updateRidden();
      this.rowingBoat = false;
      if(this.getRidingEntity() instanceof EntityBoat) {
         EntityBoat entityboat = (EntityBoat)this.getRidingEntity();
         entityboat.updateInputs(this.movementInput.leftKeyDown, this.movementInput.rightKeyDown, this.movementInput.forwardKeyDown, this.movementInput.backKeyDown);
         this.rowingBoat |= this.movementInput.leftKeyDown || this.movementInput.rightKeyDown || this.movementInput.forwardKeyDown || this.movementInput.backKeyDown;
      }
   }

   public boolean isRowingBoat() {
      return this.rowingBoat;
   }

   @Nullable
   public PotionEffect removeActivePotionEffect(@Nullable Potion potioneffectin) {
      if(potioneffectin == MobEffects.NAUSEA) {
         this.prevTimeInPortal = 0.0F;
         this.timeInPortal = 0.0F;
      }

      return super.removeActivePotionEffect(potioneffectin);
   }
}
