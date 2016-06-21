package net.minecraft.entity.boss.dragon.phase;

import javax.annotation.Nullable;
import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public interface IPhase {
   boolean getIsStationary();

   void doClientRenderEffects();

   void doLocalUpdate();

   void onCrystalDestroyed(EntityEnderCrystal crystal, BlockPos pos, DamageSource dmgSrc, EntityPlayer plyr);

   void initPhase();

   void removeAreaEffect();

   float getMaxRiseOrFall();

   float getYawFactor();

   PhaseList<? extends IPhase> getPhaseList();

   @Nullable
   Vec3d getTargetLocation();

   float getAdjustedDamage(EntityDragonPart pt, DamageSource src, float damage);
}
