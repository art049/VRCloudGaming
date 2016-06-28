package net.minecraft.util;

import net.minecraft.util.registry.RegistryNamespaced;

public class SoundEvent {
   public static final RegistryNamespaced<ResourceLocation, SoundEvent> REGISTRY = new RegistryNamespaced();
   private final ResourceLocation soundName;
   private static int soundEventId = 0;

   public SoundEvent(ResourceLocation soundNameIn) {
      this.soundName = soundNameIn;
   }

   public ResourceLocation getSoundName() {
      return this.soundName;
   }

   public static void registerSounds() {
      registerSound("ambient.cave");
      registerSound("block.anvil.break");
      registerSound("block.anvil.destroy");
      registerSound("block.anvil.fall");
      registerSound("block.anvil.hit");
      registerSound("block.anvil.land");
      registerSound("block.anvil.place");
      registerSound("block.anvil.step");
      registerSound("block.anvil.use");
      registerSound("block.brewing_stand.brew");
      registerSound("block.chest.close");
      registerSound("block.chest.locked");
      registerSound("block.chest.open");
      registerSound("block.chorus_flower.death");
      registerSound("block.chorus_flower.grow");
      registerSound("block.cloth.break");
      registerSound("block.cloth.fall");
      registerSound("block.cloth.hit");
      registerSound("block.cloth.place");
      registerSound("block.cloth.step");
      registerSound("block.comparator.click");
      registerSound("block.dispenser.dispense");
      registerSound("block.dispenser.fail");
      registerSound("block.dispenser.launch");
      registerSound("block.end_gateway.spawn");
      registerSound("block.enderchest.close");
      registerSound("block.enderchest.open");
      registerSound("block.fence_gate.close");
      registerSound("block.fence_gate.open");
      registerSound("block.fire.ambient");
      registerSound("block.fire.extinguish");
      registerSound("block.furnace.fire_crackle");
      registerSound("block.glass.break");
      registerSound("block.glass.fall");
      registerSound("block.glass.hit");
      registerSound("block.glass.place");
      registerSound("block.glass.step");
      registerSound("block.grass.break");
      registerSound("block.grass.fall");
      registerSound("block.grass.hit");
      registerSound("block.grass.place");
      registerSound("block.grass.step");
      registerSound("block.gravel.break");
      registerSound("block.gravel.fall");
      registerSound("block.gravel.hit");
      registerSound("block.gravel.place");
      registerSound("block.gravel.step");
      registerSound("block.iron_door.close");
      registerSound("block.iron_door.open");
      registerSound("block.iron_trapdoor.close");
      registerSound("block.iron_trapdoor.open");
      registerSound("block.ladder.break");
      registerSound("block.ladder.fall");
      registerSound("block.ladder.hit");
      registerSound("block.ladder.place");
      registerSound("block.ladder.step");
      registerSound("block.lava.ambient");
      registerSound("block.lava.extinguish");
      registerSound("block.lava.pop");
      registerSound("block.lever.click");
      registerSound("block.metal.break");
      registerSound("block.metal.fall");
      registerSound("block.metal.hit");
      registerSound("block.metal.place");
      registerSound("block.metal.step");
      registerSound("block.metal_pressureplate.click_off");
      registerSound("block.metal_pressureplate.click_on");
      registerSound("block.note.basedrum");
      registerSound("block.note.bass");
      registerSound("block.note.harp");
      registerSound("block.note.hat");
      registerSound("block.note.pling");
      registerSound("block.note.snare");
      registerSound("block.piston.contract");
      registerSound("block.piston.extend");
      registerSound("block.portal.ambient");
      registerSound("block.portal.travel");
      registerSound("block.portal.trigger");
      registerSound("block.redstone_torch.burnout");
      registerSound("block.sand.break");
      registerSound("block.sand.fall");
      registerSound("block.sand.hit");
      registerSound("block.sand.place");
      registerSound("block.sand.step");
      registerSound("block.slime.break");
      registerSound("block.slime.fall");
      registerSound("block.slime.hit");
      registerSound("block.slime.place");
      registerSound("block.slime.step");
      registerSound("block.snow.break");
      registerSound("block.snow.fall");
      registerSound("block.snow.hit");
      registerSound("block.snow.place");
      registerSound("block.snow.step");
      registerSound("block.stone.break");
      registerSound("block.stone.fall");
      registerSound("block.stone.hit");
      registerSound("block.stone.place");
      registerSound("block.stone.step");
      registerSound("block.stone_button.click_off");
      registerSound("block.stone_button.click_on");
      registerSound("block.stone_pressureplate.click_off");
      registerSound("block.stone_pressureplate.click_on");
      registerSound("block.tripwire.attach");
      registerSound("block.tripwire.click_off");
      registerSound("block.tripwire.click_on");
      registerSound("block.tripwire.detach");
      registerSound("block.water.ambient");
      registerSound("block.waterlily.place");
      registerSound("block.wood.break");
      registerSound("block.wood.fall");
      registerSound("block.wood.hit");
      registerSound("block.wood.place");
      registerSound("block.wood.step");
      registerSound("block.wood_button.click_off");
      registerSound("block.wood_button.click_on");
      registerSound("block.wood_pressureplate.click_off");
      registerSound("block.wood_pressureplate.click_on");
      registerSound("block.wooden_door.close");
      registerSound("block.wooden_door.open");
      registerSound("block.wooden_trapdoor.close");
      registerSound("block.wooden_trapdoor.open");
      registerSound("enchant.thorns.hit");
      registerSound("entity.armorstand.break");
      registerSound("entity.armorstand.fall");
      registerSound("entity.armorstand.hit");
      registerSound("entity.armorstand.place");
      registerSound("entity.arrow.hit");
      registerSound("entity.arrow.hit_player");
      registerSound("entity.arrow.shoot");
      registerSound("entity.bat.ambient");
      registerSound("entity.bat.death");
      registerSound("entity.bat.hurt");
      registerSound("entity.bat.loop");
      registerSound("entity.bat.takeoff");
      registerSound("entity.blaze.ambient");
      registerSound("entity.blaze.burn");
      registerSound("entity.blaze.death");
      registerSound("entity.blaze.hurt");
      registerSound("entity.blaze.shoot");
      registerSound("entity.bobber.splash");
      registerSound("entity.bobber.throw");
      registerSound("entity.cat.ambient");
      registerSound("entity.cat.death");
      registerSound("entity.cat.hiss");
      registerSound("entity.cat.hurt");
      registerSound("entity.cat.purr");
      registerSound("entity.cat.purreow");
      registerSound("entity.chicken.ambient");
      registerSound("entity.chicken.death");
      registerSound("entity.chicken.egg");
      registerSound("entity.chicken.hurt");
      registerSound("entity.chicken.step");
      registerSound("entity.cow.ambient");
      registerSound("entity.cow.death");
      registerSound("entity.cow.hurt");
      registerSound("entity.cow.milk");
      registerSound("entity.cow.step");
      registerSound("entity.creeper.death");
      registerSound("entity.creeper.hurt");
      registerSound("entity.creeper.primed");
      registerSound("entity.donkey.ambient");
      registerSound("entity.donkey.angry");
      registerSound("entity.donkey.chest");
      registerSound("entity.donkey.death");
      registerSound("entity.donkey.hurt");
      registerSound("entity.egg.throw");
      registerSound("entity.elder_guardian.ambient");
      registerSound("entity.elder_guardian.ambient_land");
      registerSound("entity.elder_guardian.curse");
      registerSound("entity.elder_guardian.death");
      registerSound("entity.elder_guardian.death_land");
      registerSound("entity.elder_guardian.hurt");
      registerSound("entity.elder_guardian.hurt_land");
      registerSound("entity.enderdragon.ambient");
      registerSound("entity.enderdragon.death");
      registerSound("entity.enderdragon.flap");
      registerSound("entity.enderdragon.growl");
      registerSound("entity.enderdragon.hurt");
      registerSound("entity.enderdragon.shoot");
      registerSound("entity.enderdragon_fireball.explode");
      registerSound("entity.endereye.launch");
      registerSound("entity.endermen.ambient");
      registerSound("entity.endermen.death");
      registerSound("entity.endermen.hurt");
      registerSound("entity.endermen.scream");
      registerSound("entity.endermen.stare");
      registerSound("entity.endermen.teleport");
      registerSound("entity.endermite.ambient");
      registerSound("entity.endermite.death");
      registerSound("entity.endermite.hurt");
      registerSound("entity.endermite.step");
      registerSound("entity.enderpearl.throw");
      registerSound("entity.experience_bottle.throw");
      registerSound("entity.experience_orb.pickup");
      registerSound("entity.experience_orb.touch");
      registerSound("entity.firework.blast");
      registerSound("entity.firework.blast_far");
      registerSound("entity.firework.large_blast");
      registerSound("entity.firework.large_blast_far");
      registerSound("entity.firework.launch");
      registerSound("entity.firework.shoot");
      registerSound("entity.firework.twinkle");
      registerSound("entity.firework.twinkle_far");
      registerSound("entity.generic.big_fall");
      registerSound("entity.generic.burn");
      registerSound("entity.generic.death");
      registerSound("entity.generic.drink");
      registerSound("entity.generic.eat");
      registerSound("entity.generic.explode");
      registerSound("entity.generic.extinguish_fire");
      registerSound("entity.generic.hurt");
      registerSound("entity.generic.small_fall");
      registerSound("entity.generic.splash");
      registerSound("entity.generic.swim");
      registerSound("entity.ghast.ambient");
      registerSound("entity.ghast.death");
      registerSound("entity.ghast.hurt");
      registerSound("entity.ghast.scream");
      registerSound("entity.ghast.shoot");
      registerSound("entity.ghast.warn");
      registerSound("entity.guardian.ambient");
      registerSound("entity.guardian.ambient_land");
      registerSound("entity.guardian.attack");
      registerSound("entity.guardian.death");
      registerSound("entity.guardian.death_land");
      registerSound("entity.guardian.flop");
      registerSound("entity.guardian.hurt");
      registerSound("entity.guardian.hurt_land");
      registerSound("entity.horse.ambient");
      registerSound("entity.horse.angry");
      registerSound("entity.horse.armor");
      registerSound("entity.horse.breathe");
      registerSound("entity.horse.death");
      registerSound("entity.horse.eat");
      registerSound("entity.horse.gallop");
      registerSound("entity.horse.hurt");
      registerSound("entity.horse.jump");
      registerSound("entity.horse.land");
      registerSound("entity.horse.saddle");
      registerSound("entity.horse.step");
      registerSound("entity.horse.step_wood");
      registerSound("entity.hostile.big_fall");
      registerSound("entity.hostile.death");
      registerSound("entity.hostile.hurt");
      registerSound("entity.hostile.small_fall");
      registerSound("entity.hostile.splash");
      registerSound("entity.hostile.swim");
      registerSound("entity.irongolem.attack");
      registerSound("entity.irongolem.death");
      registerSound("entity.irongolem.hurt");
      registerSound("entity.irongolem.step");
      registerSound("entity.item.break");
      registerSound("entity.item.pickup");
      registerSound("entity.itemframe.add_item");
      registerSound("entity.itemframe.break");
      registerSound("entity.itemframe.place");
      registerSound("entity.itemframe.remove_item");
      registerSound("entity.itemframe.rotate_item");
      registerSound("entity.leashknot.break");
      registerSound("entity.leashknot.place");
      registerSound("entity.lightning.impact");
      registerSound("entity.lightning.thunder");
      registerSound("entity.lingeringpotion.throw");
      registerSound("entity.magmacube.death");
      registerSound("entity.magmacube.hurt");
      registerSound("entity.magmacube.jump");
      registerSound("entity.magmacube.squish");
      registerSound("entity.minecart.inside");
      registerSound("entity.minecart.riding");
      registerSound("entity.mooshroom.shear");
      registerSound("entity.mule.ambient");
      registerSound("entity.mule.death");
      registerSound("entity.mule.hurt");
      registerSound("entity.painting.break");
      registerSound("entity.painting.place");
      registerSound("entity.pig.ambient");
      registerSound("entity.pig.death");
      registerSound("entity.pig.hurt");
      registerSound("entity.pig.saddle");
      registerSound("entity.pig.step");
      registerSound("entity.player.attack.crit");
      registerSound("entity.player.attack.knockback");
      registerSound("entity.player.attack.nodamage");
      registerSound("entity.player.attack.strong");
      registerSound("entity.player.attack.sweep");
      registerSound("entity.player.attack.weak");
      registerSound("entity.player.big_fall");
      registerSound("entity.player.breath");
      registerSound("entity.player.burp");
      registerSound("entity.player.death");
      registerSound("entity.player.hurt");
      registerSound("entity.player.levelup");
      registerSound("entity.player.small_fall");
      registerSound("entity.player.splash");
      registerSound("entity.player.swim");
      registerSound("entity.rabbit.ambient");
      registerSound("entity.rabbit.attack");
      registerSound("entity.rabbit.death");
      registerSound("entity.rabbit.hurt");
      registerSound("entity.rabbit.jump");
      registerSound("entity.sheep.ambient");
      registerSound("entity.sheep.death");
      registerSound("entity.sheep.hurt");
      registerSound("entity.sheep.shear");
      registerSound("entity.sheep.step");
      registerSound("entity.shulker.ambient");
      registerSound("entity.shulker.close");
      registerSound("entity.shulker.death");
      registerSound("entity.shulker.hurt");
      registerSound("entity.shulker.hurt_closed");
      registerSound("entity.shulker.open");
      registerSound("entity.shulker.shoot");
      registerSound("entity.shulker.teleport");
      registerSound("entity.shulker_bullet.hit");
      registerSound("entity.shulker_bullet.hurt");
      registerSound("entity.silverfish.ambient");
      registerSound("entity.silverfish.death");
      registerSound("entity.silverfish.hurt");
      registerSound("entity.silverfish.step");
      registerSound("entity.skeleton.ambient");
      registerSound("entity.skeleton.death");
      registerSound("entity.skeleton.hurt");
      registerSound("entity.skeleton.shoot");
      registerSound("entity.skeleton.step");
      registerSound("entity.skeleton_horse.ambient");
      registerSound("entity.skeleton_horse.death");
      registerSound("entity.skeleton_horse.hurt");
      registerSound("entity.slime.attack");
      registerSound("entity.slime.death");
      registerSound("entity.slime.hurt");
      registerSound("entity.slime.jump");
      registerSound("entity.slime.squish");
      registerSound("entity.small_magmacube.death");
      registerSound("entity.small_magmacube.hurt");
      registerSound("entity.small_magmacube.squish");
      registerSound("entity.small_slime.death");
      registerSound("entity.small_slime.hurt");
      registerSound("entity.small_slime.jump");
      registerSound("entity.small_slime.squish");
      registerSound("entity.snowball.throw");
      registerSound("entity.snowman.ambient");
      registerSound("entity.snowman.death");
      registerSound("entity.snowman.hurt");
      registerSound("entity.snowman.shoot");
      registerSound("entity.spider.ambient");
      registerSound("entity.spider.death");
      registerSound("entity.spider.hurt");
      registerSound("entity.spider.step");
      registerSound("entity.splash_potion.break");
      registerSound("entity.splash_potion.throw");
      registerSound("entity.squid.ambient");
      registerSound("entity.squid.death");
      registerSound("entity.squid.hurt");
      registerSound("entity.tnt.primed");
      registerSound("entity.villager.ambient");
      registerSound("entity.villager.death");
      registerSound("entity.villager.hurt");
      registerSound("entity.villager.no");
      registerSound("entity.villager.trading");
      registerSound("entity.villager.yes");
      registerSound("entity.witch.ambient");
      registerSound("entity.witch.death");
      registerSound("entity.witch.drink");
      registerSound("entity.witch.hurt");
      registerSound("entity.witch.throw");
      registerSound("entity.wither.ambient");
      registerSound("entity.wither.break_block");
      registerSound("entity.wither.death");
      registerSound("entity.wither.hurt");
      registerSound("entity.wither.shoot");
      registerSound("entity.wither.spawn");
      registerSound("entity.wolf.ambient");
      registerSound("entity.wolf.death");
      registerSound("entity.wolf.growl");
      registerSound("entity.wolf.howl");
      registerSound("entity.wolf.hurt");
      registerSound("entity.wolf.pant");
      registerSound("entity.wolf.shake");
      registerSound("entity.wolf.step");
      registerSound("entity.wolf.whine");
      registerSound("entity.zombie.ambient");
      registerSound("entity.zombie.attack_door_wood");
      registerSound("entity.zombie.attack_iron_door");
      registerSound("entity.zombie.break_door_wood");
      registerSound("entity.zombie.death");
      registerSound("entity.zombie.hurt");
      registerSound("entity.zombie.infect");
      registerSound("entity.zombie.step");
      registerSound("entity.zombie_horse.ambient");
      registerSound("entity.zombie_horse.death");
      registerSound("entity.zombie_horse.hurt");
      registerSound("entity.zombie_pig.ambient");
      registerSound("entity.zombie_pig.angry");
      registerSound("entity.zombie_pig.death");
      registerSound("entity.zombie_pig.hurt");
      registerSound("entity.zombie_villager.ambient");
      registerSound("entity.zombie_villager.converted");
      registerSound("entity.zombie_villager.cure");
      registerSound("entity.zombie_villager.death");
      registerSound("entity.zombie_villager.hurt");
      registerSound("entity.zombie_villager.step");
      registerSound("item.armor.equip_chain");
      registerSound("item.armor.equip_diamond");
      registerSound("item.armor.equip_generic");
      registerSound("item.armor.equip_gold");
      registerSound("item.armor.equip_iron");
      registerSound("item.armor.equip_leather");
      registerSound("item.bottle.fill");
      registerSound("item.bottle.fill_dragonbreath");
      registerSound("item.bucket.empty");
      registerSound("item.bucket.empty_lava");
      registerSound("item.bucket.fill");
      registerSound("item.bucket.fill_lava");
      registerSound("item.chorus_fruit.teleport");
      registerSound("item.elytra.flying");
      registerSound("item.firecharge.use");
      registerSound("item.flintandsteel.use");
      registerSound("item.hoe.till");
      registerSound("item.shield.block");
      registerSound("item.shield.break");
      registerSound("item.shovel.flatten");
      registerSound("music.creative");
      registerSound("music.credits");
      registerSound("music.dragon");
      registerSound("music.end");
      registerSound("music.game");
      registerSound("music.menu");
      registerSound("music.nether");
      registerSound("record.11");
      registerSound("record.13");
      registerSound("record.blocks");
      registerSound("record.cat");
      registerSound("record.chirp");
      registerSound("record.far");
      registerSound("record.mall");
      registerSound("record.mellohi");
      registerSound("record.stal");
      registerSound("record.strad");
      registerSound("record.wait");
      registerSound("record.ward");
      registerSound("ui.button.click");
      registerSound("weather.rain");
      registerSound("weather.rain.above");
   }

   private static void registerSound(String soundNameIn) {
      ResourceLocation resourcelocation = new ResourceLocation(soundNameIn);
      REGISTRY.register(soundEventId++, resourcelocation, new SoundEvent(resourcelocation));
   }
}
