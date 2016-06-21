package net.minecraft.stats;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IJsonSerializable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;

public class Achievement extends StatBase {
   public final int displayColumn;
   public final int displayRow;
   public final Achievement parentAchievement;
   private final String achievementDescription;
   private IStatStringFormat statStringFormatter;
   public final ItemStack theItemStack;
   private boolean isSpecial;

   public Achievement(String statIdIn, String unlocalizedName, int column, int row, Item itemIn, Achievement parent) {
      this(statIdIn, unlocalizedName, column, row, new ItemStack(itemIn), parent);
   }

   public Achievement(String statIdIn, String unlocalizedName, int column, int row, Block blockIn, Achievement parent) {
      this(statIdIn, unlocalizedName, column, row, new ItemStack(blockIn), parent);
   }

   public Achievement(String statIdIn, String unlocalizedName, int column, int row, ItemStack stack, Achievement parent) {
      super(statIdIn, new TextComponentTranslation("achievement." + unlocalizedName, new Object[0]));
      this.theItemStack = stack;
      this.achievementDescription = "achievement." + unlocalizedName + ".desc";
      this.displayColumn = column;
      this.displayRow = row;
      if(column < AchievementList.minDisplayColumn) {
         AchievementList.minDisplayColumn = column;
      }

      if(row < AchievementList.minDisplayRow) {
         AchievementList.minDisplayRow = row;
      }

      if(column > AchievementList.maxDisplayColumn) {
         AchievementList.maxDisplayColumn = column;
      }

      if(row > AchievementList.maxDisplayRow) {
         AchievementList.maxDisplayRow = row;
      }

      this.parentAchievement = parent;
   }

   public Achievement initIndependentStat() {
      this.isIndependent = true;
      return this;
   }

   public Achievement setSpecial() {
      this.isSpecial = true;
      return this;
   }

   public Achievement registerStat() {
      super.registerStat();
      AchievementList.ACHIEVEMENTS.add(this);
      return this;
   }

   public boolean isAchievement() {
      return true;
   }

   public ITextComponent getStatName() {
      ITextComponent itextcomponent = super.getStatName();
      itextcomponent.getStyle().setColor(this.getSpecial()?TextFormatting.DARK_PURPLE:TextFormatting.GREEN);
      return itextcomponent;
   }

   public Achievement setSerializableClazz(Class<? extends IJsonSerializable> clazz) {
      return (Achievement)super.setSerializableClazz(clazz);
   }

   public String getDescription() {
      return this.statStringFormatter != null?this.statStringFormatter.formatString(I18n.translateToLocal(this.achievementDescription)):I18n.translateToLocal(this.achievementDescription);
   }

   public Achievement setStatStringFormatter(IStatStringFormat statStringFormatterIn) {
      this.statStringFormatter = statStringFormatterIn;
      return this;
   }

   public boolean getSpecial() {
      return this.isSpecial;
   }
}
