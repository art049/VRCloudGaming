package net.minecraft.entity.passive;

import com.google.common.base.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.ai.EntityAISit;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.management.PreYggdrasilConverter;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

public abstract class EntityTameable extends EntityAnimal implements IEntityOwnable {
   protected static final DataParameter<Byte> TAMED = EntityDataManager.<Byte>createKey(EntityTameable.class, DataSerializers.BYTE);
   protected static final DataParameter<Optional<UUID>> OWNER_UNIQUE_ID = EntityDataManager.<Optional<UUID>>createKey(EntityTameable.class, DataSerializers.OPTIONAL_UNIQUE_ID);
   protected EntityAISit aiSit;

   public EntityTameable(World worldIn) {
      super(worldIn);
      this.setupTamedAI();
   }

   protected void entityInit() {
      super.entityInit();
      this.dataManager.register(TAMED, Byte.valueOf((byte)0));
      this.dataManager.register(OWNER_UNIQUE_ID, Optional.<UUID>absent());
   }

   public void writeEntityToNBT(NBTTagCompound compound) {
      super.writeEntityToNBT(compound);
      if(this.getOwnerId() == null) {
         compound.setString("OwnerUUID", "");
      } else {
         compound.setString("OwnerUUID", this.getOwnerId().toString());
      }

      compound.setBoolean("Sitting", this.isSitting());
   }

   public void readEntityFromNBT(NBTTagCompound compound) {
      super.readEntityFromNBT(compound);
      String s = "";
      if(compound.hasKey("OwnerUUID", 8)) {
         s = compound.getString("OwnerUUID");
      } else {
         String s1 = compound.getString("Owner");
         s = PreYggdrasilConverter.convertMobOwnerIfNeeded(this.getServer(), s1);
      }

      if(!s.isEmpty()) {
         try {
            this.setOwnerId(UUID.fromString(s));
            this.setTamed(true);
         } catch (Throwable var4) {
            this.setTamed(false);
         }
      }

      if(this.aiSit != null) {
         this.aiSit.setSitting(compound.getBoolean("Sitting"));
      }

      this.setSitting(compound.getBoolean("Sitting"));
   }

   public boolean canBeLeashedTo(EntityPlayer player) {
      return this.isTamed() && this.isOwner(player);
   }

   protected void playTameEffect(boolean play) {
      EnumParticleTypes enumparticletypes = EnumParticleTypes.HEART;
      if(!play) {
         enumparticletypes = EnumParticleTypes.SMOKE_NORMAL;
      }

      for(int i = 0; i < 7; ++i) {
         double d0 = this.rand.nextGaussian() * 0.02D;
         double d1 = this.rand.nextGaussian() * 0.02D;
         double d2 = this.rand.nextGaussian() * 0.02D;
         this.worldObj.spawnParticle(enumparticletypes, this.posX + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, this.posY + 0.5D + (double)(this.rand.nextFloat() * this.height), this.posZ + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, d0, d1, d2, new int[0]);
      }
   }

   public void handleStatusUpdate(byte id) {
      if(id == 7) {
         this.playTameEffect(true);
      } else if(id == 6) {
         this.playTameEffect(false);
      } else {
         super.handleStatusUpdate(id);
      }
   }

   public boolean isTamed() {
      return (((Byte)this.dataManager.get(TAMED)).byteValue() & 4) != 0;
   }

   public void setTamed(boolean tamed) {
      byte b0 = ((Byte)this.dataManager.get(TAMED)).byteValue();
      if(tamed) {
         this.dataManager.set(TAMED, Byte.valueOf((byte)(b0 | 4)));
      } else {
         this.dataManager.set(TAMED, Byte.valueOf((byte)(b0 & -5)));
      }

      this.setupTamedAI();
   }

   protected void setupTamedAI() {
   }

   public boolean isSitting() {
      return (((Byte)this.dataManager.get(TAMED)).byteValue() & 1) != 0;
   }

   public void setSitting(boolean sitting) {
      byte b0 = ((Byte)this.dataManager.get(TAMED)).byteValue();
      if(sitting) {
         this.dataManager.set(TAMED, Byte.valueOf((byte)(b0 | 1)));
      } else {
         this.dataManager.set(TAMED, Byte.valueOf((byte)(b0 & -2)));
      }
   }

   @Nullable
   public UUID getOwnerId() {
      return (UUID)((Optional)this.dataManager.get(OWNER_UNIQUE_ID)).orNull();
   }

   public void setOwnerId(@Nullable UUID p_184754_1_) {
      this.dataManager.set(OWNER_UNIQUE_ID, Optional.fromNullable(p_184754_1_));
   }

   @Nullable
   public EntityLivingBase getOwner() {
      try {
         UUID uuid = this.getOwnerId();
         return uuid == null?null:this.worldObj.getPlayerEntityByUUID(uuid);
      } catch (IllegalArgumentException var2) {
         return null;
      }
   }

   public boolean isOwner(EntityLivingBase entityIn) {
      return entityIn == this.getOwner();
   }

   public EntityAISit getAISit() {
      return this.aiSit;
   }

   public boolean shouldAttackEntity(EntityLivingBase p_142018_1_, EntityLivingBase p_142018_2_) {
      return true;
   }

   public Team getTeam() {
      if(this.isTamed()) {
         EntityLivingBase entitylivingbase = this.getOwner();
         if(entitylivingbase != null) {
            return entitylivingbase.getTeam();
         }
      }

      return super.getTeam();
   }

   public boolean isOnSameTeam(Entity entityIn) {
      if(this.isTamed()) {
         EntityLivingBase entitylivingbase = this.getOwner();
         if(entityIn == entitylivingbase) {
            return true;
         }

         if(entitylivingbase != null) {
            return entitylivingbase.isOnSameTeam(entityIn);
         }
      }

      return super.isOnSameTeam(entityIn);
   }

   public void onDeath(DamageSource cause) {
      if(!this.worldObj.isRemote && this.worldObj.getGameRules().getBoolean("showDeathMessages") && this.getOwner() instanceof EntityPlayerMP) {
         this.getOwner().addChatMessage(this.getCombatTracker().getDeathMessage());
      }

      super.onDeath(cause);
   }
}
