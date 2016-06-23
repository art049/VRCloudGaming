package net.minecraft.entity.projectile;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.init.MobEffects;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class EntityWitherSkull extends EntityFireball {
   private static final DataParameter<Boolean> INVULNERABLE = EntityDataManager.<Boolean>createKey(EntityWitherSkull.class, DataSerializers.BOOLEAN);

   public EntityWitherSkull(World worldIn) {
      super(worldIn);
      this.setSize(0.3125F, 0.3125F);
   }

   public EntityWitherSkull(World worldIn, EntityLivingBase shooter, double accelX, double accelY, double accelZ) {
      super(worldIn, shooter, accelX, accelY, accelZ);
      this.setSize(0.3125F, 0.3125F);
   }

   protected float getMotionFactor() {
      return this.isInvulnerable()?0.73F:super.getMotionFactor();
   }

   public EntityWitherSkull(World worldIn, double x, double y, double z, double accelX, double accelY, double accelZ) {
      super(worldIn, x, y, z, accelX, accelY, accelZ);
      this.setSize(0.3125F, 0.3125F);
   }

   public boolean isBurning() {
      return false;
   }

   public float getExplosionResistance(Explosion explosionIn, World worldIn, BlockPos pos, IBlockState blockStateIn) {
      float f = super.getExplosionResistance(explosionIn, worldIn, pos, blockStateIn);
      Block block = blockStateIn.getBlock();
      if(this.isInvulnerable() && EntityWither.canDestroyBlock(block)) {
         f = Math.min(0.8F, f);
      }

      return f;
   }

   protected void onImpact(RayTraceResult result) {
      if(!this.worldObj.isRemote) {
         if(result.entityHit != null) {
            if(this.shootingEntity != null) {
               if(result.entityHit.attackEntityFrom(DamageSource.causeMobDamage(this.shootingEntity), 8.0F)) {
                  if(!result.entityHit.isEntityAlive()) {
                     this.shootingEntity.heal(5.0F);
                  } else {
                     this.applyEnchantments(this.shootingEntity, result.entityHit);
                  }
               }
            } else {
               result.entityHit.attackEntityFrom(DamageSource.magic, 5.0F);
            }

            if(result.entityHit instanceof EntityLivingBase) {
               int i = 0;
               if(this.worldObj.getDifficulty() == EnumDifficulty.NORMAL) {
                  i = 10;
               } else if(this.worldObj.getDifficulty() == EnumDifficulty.HARD) {
                  i = 40;
               }

               if(i > 0) {
                  ((EntityLivingBase)result.entityHit).addPotionEffect(new PotionEffect(MobEffects.WITHER, 20 * i, 1));
               }
            }
         }

         this.worldObj.newExplosion(this, this.posX, this.posY, this.posZ, 1.0F, false, this.worldObj.getGameRules().getBoolean("mobGriefing"));
         this.setDead();
      }
   }

   public boolean canBeCollidedWith() {
      return false;
   }

   public boolean attackEntityFrom(DamageSource source, float amount) {
      return false;
   }

   protected void entityInit() {
      this.dataManager.register(INVULNERABLE, Boolean.valueOf(false));
   }

   public boolean isInvulnerable() {
      return ((Boolean)this.dataManager.get(INVULNERABLE)).booleanValue();
   }

   public void setInvulnerable(boolean invulnerable) {
      this.dataManager.set(INVULNERABLE, Boolean.valueOf(invulnerable));
   }

   protected boolean isFireballFiery() {
      return false;
   }
}