package net.minecraft.stats;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IJsonSerializable;
import net.minecraft.util.TupleIntJsonSerializable;

public class StatisticsManager {
   protected final Map<StatBase, TupleIntJsonSerializable> statsData = Maps.<StatBase, TupleIntJsonSerializable>newConcurrentMap();

   public boolean hasAchievementUnlocked(Achievement achievementIn) {
      return this.readStat(achievementIn) > 0;
   }

   public boolean canUnlockAchievement(Achievement achievementIn) {
      return achievementIn.parentAchievement == null || this.hasAchievementUnlocked(achievementIn.parentAchievement);
   }

   public int countRequirementsUntilAvailable(Achievement achievementIn) {
      if(this.hasAchievementUnlocked(achievementIn)) {
         return 0;
      } else {
         int i = 0;

         for(Achievement achievement = achievementIn.parentAchievement; achievement != null && !this.hasAchievementUnlocked(achievement); ++i) {
            achievement = achievement.parentAchievement;
         }

         return i;
      }
   }

   public void increaseStat(EntityPlayer player, StatBase stat, int amount) {
      if(!stat.isAchievement() || this.canUnlockAchievement((Achievement)stat)) {
         this.unlockAchievement(player, stat, this.readStat(stat) + amount);
      }
   }

   public void unlockAchievement(EntityPlayer playerIn, StatBase statIn, int p_150873_3_) {
      TupleIntJsonSerializable tupleintjsonserializable = (TupleIntJsonSerializable)this.statsData.get(statIn);
      if(tupleintjsonserializable == null) {
         tupleintjsonserializable = new TupleIntJsonSerializable();
         this.statsData.put(statIn, tupleintjsonserializable);
      }

      tupleintjsonserializable.setIntegerValue(p_150873_3_);
   }

   public int readStat(StatBase stat) {
      TupleIntJsonSerializable tupleintjsonserializable = (TupleIntJsonSerializable)this.statsData.get(stat);
      return tupleintjsonserializable == null?0:tupleintjsonserializable.getIntegerValue();
   }

   public <T extends IJsonSerializable> T getProgress(StatBase p_150870_1_) {
      TupleIntJsonSerializable tupleintjsonserializable = (TupleIntJsonSerializable)this.statsData.get(p_150870_1_);
      return (T)(tupleintjsonserializable != null?tupleintjsonserializable.getJsonSerializableValue():null);
   }

   public <T extends IJsonSerializable> T setProgress(StatBase p_150872_1_, T p_150872_2_) {
      TupleIntJsonSerializable tupleintjsonserializable = (TupleIntJsonSerializable)this.statsData.get(p_150872_1_);
      if(tupleintjsonserializable == null) {
         tupleintjsonserializable = new TupleIntJsonSerializable();
         this.statsData.put(p_150872_1_, tupleintjsonserializable);
      }

      tupleintjsonserializable.setJsonSerializableValue(p_150872_2_);
      return (T)p_150872_2_;
   }
}
