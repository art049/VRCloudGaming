package net.minecraft.util.datafix;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.datafix.fixes.ArmorStandSilent;
import net.minecraft.util.datafix.fixes.BookPagesStrictJSON;
import net.minecraft.util.datafix.fixes.EntityArmorAndHeld;
import net.minecraft.util.datafix.fixes.EntityHealth;
import net.minecraft.util.datafix.fixes.HorseSaddle;
import net.minecraft.util.datafix.fixes.ItemIntIDToString;
import net.minecraft.util.datafix.fixes.MinecartEntityTypes;
import net.minecraft.util.datafix.fixes.PaintingDirection;
import net.minecraft.util.datafix.fixes.PotionItems;
import net.minecraft.util.datafix.fixes.RedundantChanceTags;
import net.minecraft.util.datafix.fixes.RidingToPassengers;
import net.minecraft.util.datafix.fixes.SignStrictJSON;
import net.minecraft.util.datafix.fixes.SpawnEggNames;
import net.minecraft.util.datafix.fixes.SpawnerEntityTypes;
import net.minecraft.util.datafix.fixes.StringToUUID;
import net.minecraft.util.datafix.walkers.BlockEntityTag;
import net.minecraft.util.datafix.walkers.EntityTag;
import net.minecraft.util.datafix.walkers.ItemStackData;
import net.minecraft.util.datafix.walkers.ItemStackDataLists;

public class DataFixesManager
{
    private static void registerFixes(DataFixer fixer)
    {
        fixer.registerFix(FixTypes.ENTITY, new EntityArmorAndHeld());
        fixer.registerFix(FixTypes.BLOCK_ENTITY, new SignStrictJSON());
        fixer.registerFix(FixTypes.ITEM_INSTANCE, new ItemIntIDToString());
        fixer.registerFix(FixTypes.ITEM_INSTANCE, new PotionItems());
        fixer.registerFix(FixTypes.ITEM_INSTANCE, new SpawnEggNames());
        fixer.registerFix(FixTypes.ENTITY, new MinecartEntityTypes());
        fixer.registerFix(FixTypes.BLOCK_ENTITY, new SpawnerEntityTypes());
        fixer.registerFix(FixTypes.ENTITY, new StringToUUID());
        fixer.registerFix(FixTypes.ENTITY, new EntityHealth());
        fixer.registerFix(FixTypes.ENTITY, new HorseSaddle());
        fixer.registerFix(FixTypes.ENTITY, new PaintingDirection());
        fixer.registerFix(FixTypes.ENTITY, new RedundantChanceTags());
        fixer.registerFix(FixTypes.ENTITY, new RidingToPassengers());
        fixer.registerFix(FixTypes.ENTITY, new ArmorStandSilent());
        fixer.registerFix(FixTypes.ITEM_INSTANCE, new BookPagesStrictJSON());
    }

    public static DataFixer createFixer()
    {
        DataFixer datafixer = new DataFixer(184);
        datafixer.registerWalker(FixTypes.LEVEL, new IDataWalker()
        {
            public NBTTagCompound process(IDataFixer fixer, NBTTagCompound compound, int versionIn)
            {
                if (compound.hasKey("Player", 10))
                {
                    compound.setTag("Player", fixer.process(FixTypes.PLAYER, compound.getCompoundTag("Player"), versionIn));
                }

                return compound;
            }
        });
        datafixer.registerWalker(FixTypes.PLAYER, new IDataWalker()
        {
            public NBTTagCompound process(IDataFixer fixer, NBTTagCompound compound, int versionIn)
            {
                DataFixesManager.processInventory(fixer, compound, versionIn, "Inventory");
                DataFixesManager.processInventory(fixer, compound, versionIn, "EnderItems");
                return compound;
            }
        });
        datafixer.registerWalker(FixTypes.CHUNK, new IDataWalker()
        {
            public NBTTagCompound process(IDataFixer fixer, NBTTagCompound compound, int versionIn)
            {
                if (compound.hasKey("Level", 10))
                {
                    NBTTagCompound nbttagcompound = compound.getCompoundTag("Level");

                    if (nbttagcompound.hasKey("Entities", 9))
                    {
                        NBTTagList nbttaglist = nbttagcompound.getTagList("Entities", 10);

                        for (int i = 0; i < nbttaglist.tagCount(); ++i)
                        {
                            nbttaglist.set(i, fixer.process(FixTypes.ENTITY, (NBTTagCompound)nbttaglist.get(i), versionIn));
                        }
                    }

                    if (nbttagcompound.hasKey("TileEntities", 9))
                    {
                        NBTTagList nbttaglist1 = nbttagcompound.getTagList("TileEntities", 10);

                        for (int j = 0; j < nbttaglist1.tagCount(); ++j)
                        {
                            nbttaglist1.set(j, fixer.process(FixTypes.BLOCK_ENTITY, (NBTTagCompound)nbttaglist1.get(j), versionIn));
                        }
                    }
                }

                return compound;
            }
        });
        datafixer.registerWalker(FixTypes.ENTITY, new ItemStackData("Item", new String[] {"Item"}));
        datafixer.registerWalker(FixTypes.ENTITY, new ItemStackData("ThrownPotion", new String[] {"Potion"}));
        datafixer.registerWalker(FixTypes.ENTITY, new ItemStackData("ItemFrame", new String[] {"Item"}));
        datafixer.registerWalker(FixTypes.ENTITY, new ItemStackData("FireworksRocketEntity", new String[] {"FireworksItem"}));
        datafixer.registerWalker(FixTypes.ENTITY, new ItemStackData("TippedArrow", new String[] {"Item"}));
        datafixer.registerWalker(FixTypes.ENTITY, new ItemStackDataLists("MinecartChest", new String[] {"Items"}));
        datafixer.registerWalker(FixTypes.ENTITY, new ItemStackDataLists("MinecartHopper", new String[] {"Items"}));
        datafixer.registerWalker(FixTypes.ENTITY, new ItemStackDataLists("Enderman", new String[] {"ArmorItems", "HandItems"}));
        datafixer.registerWalker(FixTypes.ENTITY, new ItemStackDataLists("ArmorStand", new String[] {"ArmorItems", "HandItems"}));
        datafixer.registerWalker(FixTypes.ENTITY, new ItemStackDataLists("Bat", new String[] {"ArmorItems", "HandItems"}));
        datafixer.registerWalker(FixTypes.ENTITY, new ItemStackDataLists("Blaze", new String[] {"ArmorItems", "HandItems"}));
        datafixer.registerWalker(FixTypes.ENTITY, new ItemStackDataLists("CaveSpider", new String[] {"ArmorItems", "HandItems"}));
        datafixer.registerWalker(FixTypes.ENTITY, new ItemStackDataLists("Chicken", new String[] {"ArmorItems", "HandItems"}));
        datafixer.registerWalker(FixTypes.ENTITY, new ItemStackDataLists("Cow", new String[] {"ArmorItems", "HandItems"}));
        datafixer.registerWalker(FixTypes.ENTITY, new ItemStackDataLists("Creeper", new String[] {"ArmorItems", "HandItems"}));
        datafixer.registerWalker(FixTypes.ENTITY, new ItemStackDataLists("EnderDragon", new String[] {"ArmorItems", "HandItems"}));
        datafixer.registerWalker(FixTypes.ENTITY, new ItemStackDataLists("Endermite", new String[] {"ArmorItems", "HandItems"}));
        datafixer.registerWalker(FixTypes.ENTITY, new ItemStackDataLists("Ghast", new String[] {"ArmorItems", "HandItems"}));
        datafixer.registerWalker(FixTypes.ENTITY, new ItemStackDataLists("Giant", new String[] {"ArmorItems", "HandItems"}));
        datafixer.registerWalker(FixTypes.ENTITY, new ItemStackDataLists("Guardian", new String[] {"ArmorItems", "HandItems"}));
        datafixer.registerWalker(FixTypes.ENTITY, new ItemStackDataLists("LavaSlime", new String[] {"ArmorItems", "HandItems"}));
        datafixer.registerWalker(FixTypes.ENTITY, new ItemStackDataLists("Mob", new String[] {"ArmorItems", "HandItems"}));
        datafixer.registerWalker(FixTypes.ENTITY, new ItemStackDataLists("Monster", new String[] {"ArmorItems", "HandItems"}));
        datafixer.registerWalker(FixTypes.ENTITY, new ItemStackDataLists("MushroomCow", new String[] {"ArmorItems", "HandItems"}));
        datafixer.registerWalker(FixTypes.ENTITY, new ItemStackDataLists("Ozelot", new String[] {"ArmorItems", "HandItems"}));
        datafixer.registerWalker(FixTypes.ENTITY, new ItemStackDataLists("Pig", new String[] {"ArmorItems", "HandItems"}));
        datafixer.registerWalker(FixTypes.ENTITY, new ItemStackDataLists("PigZombie", new String[] {"ArmorItems", "HandItems"}));
        datafixer.registerWalker(FixTypes.ENTITY, new ItemStackDataLists("Rabbit", new String[] {"ArmorItems", "HandItems"}));
        datafixer.registerWalker(FixTypes.ENTITY, new ItemStackDataLists("Sheep", new String[] {"ArmorItems", "HandItems"}));
        datafixer.registerWalker(FixTypes.ENTITY, new ItemStackDataLists("Shulker", new String[] {"ArmorItems", "HandItems"}));
        datafixer.registerWalker(FixTypes.ENTITY, new ItemStackDataLists("Silverfish", new String[] {"ArmorItems", "HandItems"}));
        datafixer.registerWalker(FixTypes.ENTITY, new ItemStackDataLists("Skeleton", new String[] {"ArmorItems", "HandItems"}));
        datafixer.registerWalker(FixTypes.ENTITY, new ItemStackDataLists("Slime", new String[] {"ArmorItems", "HandItems"}));
        datafixer.registerWalker(FixTypes.ENTITY, new ItemStackDataLists("SnowMan", new String[] {"ArmorItems", "HandItems"}));
        datafixer.registerWalker(FixTypes.ENTITY, new ItemStackDataLists("Spider", new String[] {"ArmorItems", "HandItems"}));
        datafixer.registerWalker(FixTypes.ENTITY, new ItemStackDataLists("Squid", new String[] {"ArmorItems", "HandItems"}));
        datafixer.registerWalker(FixTypes.ENTITY, new ItemStackDataLists("VillagerGolem", new String[] {"ArmorItems", "HandItems"}));
        datafixer.registerWalker(FixTypes.ENTITY, new ItemStackDataLists("Witch", new String[] {"ArmorItems", "HandItems"}));
        datafixer.registerWalker(FixTypes.ENTITY, new ItemStackDataLists("WitherBoss", new String[] {"ArmorItems", "HandItems"}));
        datafixer.registerWalker(FixTypes.ENTITY, new ItemStackDataLists("Wolf", new String[] {"ArmorItems", "HandItems"}));
        datafixer.registerWalker(FixTypes.ENTITY, new ItemStackDataLists("Zombie", new String[] {"ArmorItems", "HandItems"}));
        datafixer.registerWalker(FixTypes.ENTITY, new ItemStackDataLists("EntityHorse", new String[] {"ArmorItems", "HandItems", "Items"}));
        datafixer.registerWalker(FixTypes.ENTITY, new ItemStackData("EntityHorse", new String[] {"ArmorItem", "SaddleItem"}));
        datafixer.registerWalker(FixTypes.ENTITY, new ItemStackDataLists("Villager", new String[] {"ArmorItems", "HandItems", "Inventory"}));
        datafixer.registerWalker(FixTypes.ENTITY, new IDataWalker()
        {
            public NBTTagCompound process(IDataFixer fixer, NBTTagCompound compound, int versionIn)
            {
                if ("Villager".equals(compound.getString("id")) && compound.hasKey("Offers", 10))
                {
                    NBTTagCompound nbttagcompound = compound.getCompoundTag("Offers");

                    if (nbttagcompound.hasKey("Recipes", 9))
                    {
                        NBTTagList nbttaglist = nbttagcompound.getTagList("Recipes", 10);

                        for (int i = 0; i < nbttaglist.tagCount(); ++i)
                        {
                            NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
                            DataFixesManager.processItemStack(fixer, nbttagcompound1, versionIn, "buy");
                            DataFixesManager.processItemStack(fixer, nbttagcompound1, versionIn, "buyB");
                            DataFixesManager.processItemStack(fixer, nbttagcompound1, versionIn, "sell");
                            nbttaglist.set(i, nbttagcompound1);
                        }
                    }
                }

                return compound;
            }
        });
        datafixer.registerWalker(FixTypes.ENTITY, new IDataWalker()
        {
            public NBTTagCompound process(IDataFixer fixer, NBTTagCompound compound, int versionIn)
            {
                if ("MinecartSpawner".equals(compound.getString("id")))
                {
                    compound.setString("id", "MobSpawner");
                    fixer.process(FixTypes.BLOCK_ENTITY, compound, versionIn);
                    compound.setString("id", "MinecartSpawner");
                }

                return compound;
            }
        });
        datafixer.registerWalker(FixTypes.ENTITY, new IDataWalker()
        {
            public NBTTagCompound process(IDataFixer fixer, NBTTagCompound compound, int versionIn)
            {
                if ("MinecartCommandBlock".equals(compound.getString("id")))
                {
                    compound.setString("id", "Control");
                    fixer.process(FixTypes.BLOCK_ENTITY, compound, versionIn);
                    compound.setString("id", "MinecartCommandBlock");
                }

                return compound;
            }
        });
        datafixer.registerWalker(FixTypes.BLOCK_ENTITY, new ItemStackDataLists("Furnace", new String[] {"Items"}));
        datafixer.registerWalker(FixTypes.BLOCK_ENTITY, new ItemStackDataLists("Chest", new String[] {"Items"}));
        datafixer.registerWalker(FixTypes.BLOCK_ENTITY, new ItemStackDataLists("Trap", new String[] {"Items"}));
        datafixer.registerWalker(FixTypes.BLOCK_ENTITY, new ItemStackDataLists("Dropper", new String[] {"Items"}));
        datafixer.registerWalker(FixTypes.BLOCK_ENTITY, new ItemStackDataLists("Cauldron", new String[] {"Items"}));
        datafixer.registerWalker(FixTypes.BLOCK_ENTITY, new ItemStackDataLists("Hopper", new String[] {"Items"}));
        datafixer.registerWalker(FixTypes.BLOCK_ENTITY, new ItemStackData("RecordPlayer", new String[] {"RecordItem"}));
        datafixer.registerWalker(FixTypes.BLOCK_ENTITY, new IDataWalker()
        {
            public NBTTagCompound process(IDataFixer fixer, NBTTagCompound compound, int versionIn)
            {
                if ("MobSpawner".equals(compound.getString("id")))
                {
                    if (compound.hasKey("SpawnPotentials", 9))
                    {
                        NBTTagList nbttaglist = compound.getTagList("SpawnPotentials", 10);

                        for (int i = 0; i < nbttaglist.tagCount(); ++i)
                        {
                            NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
                            nbttagcompound.setTag("Entity", fixer.process(FixTypes.ENTITY, nbttagcompound.getCompoundTag("Entity"), versionIn));
                        }
                    }

                    compound.setTag("SpawnData", fixer.process(FixTypes.ENTITY, compound.getCompoundTag("SpawnData"), versionIn));
                }

                return compound;
            }
        });
        datafixer.registerWalker(FixTypes.ITEM_INSTANCE, new BlockEntityTag());
        datafixer.registerWalker(FixTypes.ITEM_INSTANCE, new EntityTag());
        registerFixes(datafixer);
        return datafixer;
    }

    public static NBTTagCompound processItemStack(IDataFixer fixer, NBTTagCompound compound, int version, String key)
    {
        if (compound.hasKey(key, 10))
        {
            compound.setTag(key, fixer.process(FixTypes.ITEM_INSTANCE, compound.getCompoundTag(key), version));
        }

        return compound;
    }

    public static NBTTagCompound processInventory(IDataFixer fixer, NBTTagCompound compound, int version, String key)
    {
        if (compound.hasKey(key, 9))
        {
            NBTTagList nbttaglist = compound.getTagList(key, 10);

            for (int i = 0; i < nbttaglist.tagCount(); ++i)
            {
                nbttaglist.set(i, fixer.process(FixTypes.ITEM_INSTANCE, nbttaglist.getCompoundTagAt(i), version));
            }
        }

        return compound;
    }
}
