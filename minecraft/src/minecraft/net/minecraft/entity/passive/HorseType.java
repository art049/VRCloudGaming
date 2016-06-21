package net.minecraft.entity.passive;

import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.storage.loot.LootTableList;

public enum HorseType {
   HORSE("horse", "horse_white", SoundEvents.ENTITY_HORSE_AMBIENT, SoundEvents.ENTITY_HORSE_HURT, SoundEvents.ENTITY_HORSE_DEATH, LootTableList.ENTITIES_HORSE),
   DONKEY("donkey", "donkey", SoundEvents.ENTITY_DONKEY_AMBIENT, SoundEvents.ENTITY_DONKEY_HURT, SoundEvents.ENTITY_DONKEY_DEATH, LootTableList.ENTITIES_HORSE),
   MULE("mule", "mule", SoundEvents.ENTITY_MULE_AMBIENT, SoundEvents.ENTITY_MULE_HURT, SoundEvents.ENTITY_MULE_DEATH, LootTableList.ENTITIES_HORSE),
   ZOMBIE("zombiehorse", "horse_zombie", SoundEvents.ENTITY_ZOMBIE_HORSE_AMBIENT, SoundEvents.ENTITY_ZOMBIE_HORSE_HURT, SoundEvents.ENTITY_ZOMBIE_HORSE_DEATH, LootTableList.ENTITIES_ZOMBIE_HORSE),
   SKELETON("skeletonhorse", "horse_skeleton", SoundEvents.ENTITY_SKELETON_HORSE_AMBIENT, SoundEvents.ENTITY_SKELETON_HORSE_HURT, SoundEvents.ENTITY_SKELETON_HORSE_DEATH, LootTableList.ENTITIES_SKELETON_HORSE);

   private final TextComponentTranslation name;
   private final ResourceLocation texture;
   private final SoundEvent hurtSound;
   private final SoundEvent ambientSound;
   private final SoundEvent deathSound;
   private ResourceLocation lootTable;

   private HorseType(String p_i46798_3_, String textureName, SoundEvent ambientSound, SoundEvent hurtSoundIn, SoundEvent deathSoundIn, ResourceLocation lootTableIn) {
      this.lootTable = lootTableIn;
      this.name = new TextComponentTranslation("entity." + p_i46798_3_ + ".name", new Object[0]);
      this.texture = new ResourceLocation("textures/entity/horse/" + textureName + ".png");
      this.hurtSound = hurtSoundIn;
      this.ambientSound = ambientSound;
      this.deathSound = deathSoundIn;
   }

   public SoundEvent getAmbientSound() {
      return this.ambientSound;
   }

   public SoundEvent getHurtSound() {
      return this.hurtSound;
   }

   public SoundEvent getDeathSound() {
      return this.deathSound;
   }

   public TextComponentTranslation getDefaultName() {
      return this.name;
   }

   public ResourceLocation getTexture() {
      return this.texture;
   }

   public boolean canBeChested() {
      return this == DONKEY || this == MULE;
   }

   public boolean hasMuleEars() {
      return this == DONKEY || this == MULE;
   }

   public boolean isUndead() {
      return this == ZOMBIE || this == SKELETON;
   }

   public boolean canMate() {
      return !this.isUndead() && this != MULE;
   }

   public boolean isHorse() {
      return this == HORSE;
   }

   public int getOrdinal() {
      return this.ordinal();
   }

   public static HorseType getArmorType(int armorID) {
      return values()[armorID];
   }

   public ResourceLocation getLootTable() {
      return this.lootTable;
   }
}
