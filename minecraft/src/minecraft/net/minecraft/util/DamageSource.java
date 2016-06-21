package net.minecraft.util;

import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.Explosion;

public class DamageSource {
   public static DamageSource inFire = (new DamageSource("inFire")).setFireDamage();
   public static DamageSource lightningBolt = new DamageSource("lightningBolt");
   public static DamageSource onFire = (new DamageSource("onFire")).setDamageBypassesArmor().setFireDamage();
   public static DamageSource lava = (new DamageSource("lava")).setFireDamage();
   public static DamageSource inWall = (new DamageSource("inWall")).setDamageBypassesArmor();
   public static DamageSource drown = (new DamageSource("drown")).setDamageBypassesArmor();
   public static DamageSource starve = (new DamageSource("starve")).setDamageBypassesArmor().setDamageIsAbsolute();
   public static DamageSource cactus = new DamageSource("cactus");
   public static DamageSource fall = (new DamageSource("fall")).setDamageBypassesArmor();
   public static DamageSource flyIntoWall = (new DamageSource("flyIntoWall")).setDamageBypassesArmor();
   public static DamageSource outOfWorld = (new DamageSource("outOfWorld")).setDamageBypassesArmor().setDamageAllowedInCreativeMode();
   public static DamageSource generic = (new DamageSource("generic")).setDamageBypassesArmor();
   public static DamageSource magic = (new DamageSource("magic")).setDamageBypassesArmor().setMagicDamage();
   public static DamageSource wither = (new DamageSource("wither")).setDamageBypassesArmor();
   public static DamageSource anvil = new DamageSource("anvil");
   public static DamageSource fallingBlock = new DamageSource("fallingBlock");
   public static DamageSource dragonBreath = (new DamageSource("dragonBreath")).setDamageBypassesArmor();
   private boolean isUnblockable;
   private boolean isDamageAllowedInCreativeMode;
   private boolean damageIsAbsolute;
   private float hungerDamage = 0.3F;
   private boolean fireDamage;
   private boolean projectile;
   private boolean difficultyScaled;
   private boolean magicDamage;
   private boolean explosion;
   public String damageType;

   public static DamageSource causeMobDamage(EntityLivingBase mob) {
      return new EntityDamageSource("mob", mob);
   }

   public static DamageSource causeIndirectDamage(Entity source, EntityLivingBase indirectEntityIn) {
      return new EntityDamageSourceIndirect("mob", source, indirectEntityIn);
   }

   public static DamageSource causePlayerDamage(EntityPlayer player) {
      return new EntityDamageSource("player", player);
   }

   public static DamageSource causeArrowDamage(EntityArrow arrow, @Nullable Entity indirectEntityIn) {
      return (new EntityDamageSourceIndirect("arrow", arrow, indirectEntityIn)).setProjectile();
   }

   public static DamageSource causeFireballDamage(EntityFireball fireball, @Nullable Entity indirectEntityIn) {
      return indirectEntityIn == null?(new EntityDamageSourceIndirect("onFire", fireball, fireball)).setFireDamage().setProjectile():(new EntityDamageSourceIndirect("fireball", fireball, indirectEntityIn)).setFireDamage().setProjectile();
   }

   public static DamageSource causeThrownDamage(Entity source, @Nullable Entity indirectEntityIn) {
      return (new EntityDamageSourceIndirect("thrown", source, indirectEntityIn)).setProjectile();
   }

   public static DamageSource causeIndirectMagicDamage(Entity source, @Nullable Entity indirectEntityIn) {
      return (new EntityDamageSourceIndirect("indirectMagic", source, indirectEntityIn)).setDamageBypassesArmor().setMagicDamage();
   }

   public static DamageSource causeThornsDamage(Entity source) {
      return (new EntityDamageSource("thorns", source)).setIsThornsDamage().setMagicDamage();
   }

   public static DamageSource causeExplosionDamage(@Nullable Explosion explosionIn) {
      return explosionIn != null && explosionIn.getExplosivePlacedBy() != null?(new EntityDamageSource("explosion.player", explosionIn.getExplosivePlacedBy())).setDifficultyScaled().setExplosion():(new DamageSource("explosion")).setDifficultyScaled().setExplosion();
   }

   public static DamageSource causeExplosionDamage(@Nullable EntityLivingBase entityLivingBaseIn) {
      return entityLivingBaseIn != null?(new EntityDamageSource("explosion.player", entityLivingBaseIn)).setDifficultyScaled().setExplosion():(new DamageSource("explosion")).setDifficultyScaled().setExplosion();
   }

   public boolean isProjectile() {
      return this.projectile;
   }

   public DamageSource setProjectile() {
      this.projectile = true;
      return this;
   }

   public boolean isExplosion() {
      return this.explosion;
   }

   public DamageSource setExplosion() {
      this.explosion = true;
      return this;
   }

   public boolean isUnblockable() {
      return this.isUnblockable;
   }

   public float getHungerDamage() {
      return this.hungerDamage;
   }

   public boolean canHarmInCreative() {
      return this.isDamageAllowedInCreativeMode;
   }

   public boolean isDamageAbsolute() {
      return this.damageIsAbsolute;
   }

   protected DamageSource(String damageTypeIn) {
      this.damageType = damageTypeIn;
   }

   @Nullable
   public Entity getSourceOfDamage() {
      return this.getEntity();
   }

   @Nullable
   public Entity getEntity() {
      return null;
   }

   protected DamageSource setDamageBypassesArmor() {
      this.isUnblockable = true;
      this.hungerDamage = 0.0F;
      return this;
   }

   protected DamageSource setDamageAllowedInCreativeMode() {
      this.isDamageAllowedInCreativeMode = true;
      return this;
   }

   protected DamageSource setDamageIsAbsolute() {
      this.damageIsAbsolute = true;
      this.hungerDamage = 0.0F;
      return this;
   }

   protected DamageSource setFireDamage() {
      this.fireDamage = true;
      return this;
   }

   public ITextComponent getDeathMessage(EntityLivingBase entityLivingBaseIn) {
      EntityLivingBase entitylivingbase = entityLivingBaseIn.getAttackingEntity();
      String s = "death.attack." + this.damageType;
      String s1 = s + ".player";
      return entitylivingbase != null && I18n.canTranslate(s1)?new TextComponentTranslation(s1, new Object[]{entityLivingBaseIn.getDisplayName(), entitylivingbase.getDisplayName()}):new TextComponentTranslation(s, new Object[]{entityLivingBaseIn.getDisplayName()});
   }

   public boolean isFireDamage() {
      return this.fireDamage;
   }

   public String getDamageType() {
      return this.damageType;
   }

   public DamageSource setDifficultyScaled() {
      this.difficultyScaled = true;
      return this;
   }

   public boolean isDifficultyScaled() {
      return this.difficultyScaled;
   }

   public boolean isMagicDamage() {
      return this.magicDamage;
   }

   public DamageSource setMagicDamage() {
      this.magicDamage = true;
      return this;
   }

   public boolean isCreativePlayer() {
      Entity entity = this.getEntity();
      return entity instanceof EntityPlayer && ((EntityPlayer)entity).capabilities.isCreativeMode;
   }

   @Nullable
   public Vec3d getDamageLocation() {
      return null;
   }
}
