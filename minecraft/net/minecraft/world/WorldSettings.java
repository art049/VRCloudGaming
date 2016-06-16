package net.minecraft.world;

import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.world.storage.WorldInfo;

public final class WorldSettings
{
    /** The seed for the map. */
    private final long seed;

    /** The EnumGameType. */
    private final WorldSettings.GameType theGameType;

    /**
     * Switch for the map features. 'true' for enabled, 'false' for disabled.
     */
    private final boolean mapFeaturesEnabled;

    /** True if hardcore mode is enabled */
    private final boolean hardcoreEnabled;
    private final WorldType terrainType;

    /** True if Commands (cheats) are allowed. */
    private boolean commandsAllowed;

    /** True if the Bonus Chest is enabled. */
    private boolean bonusChestEnabled;
    private String generatorOptions;

    public WorldSettings(long seedIn, WorldSettings.GameType gameType, boolean enableMapFeatures, boolean hardcoreMode, WorldType worldTypeIn)
    {
        this.generatorOptions = "";
        this.seed = seedIn;
        this.theGameType = gameType;
        this.mapFeaturesEnabled = enableMapFeatures;
        this.hardcoreEnabled = hardcoreMode;
        this.terrainType = worldTypeIn;
    }

    public WorldSettings(WorldInfo info)
    {
        this(info.getSeed(), info.getGameType(), info.isMapFeaturesEnabled(), info.isHardcoreModeEnabled(), info.getTerrainType());
    }

    /**
     * Enables the bonus chest.
     */
    public WorldSettings enableBonusChest()
    {
        this.bonusChestEnabled = true;
        return this;
    }

    /**
     * Enables Commands (cheats).
     */
    public WorldSettings enableCommands()
    {
        this.commandsAllowed = true;
        return this;
    }

    public WorldSettings setGeneratorOptions(String options)
    {
        this.generatorOptions = options;
        return this;
    }

    /**
     * Returns true if the Bonus Chest is enabled.
     */
    public boolean isBonusChestEnabled()
    {
        return this.bonusChestEnabled;
    }

    /**
     * Returns the seed for the world.
     */
    public long getSeed()
    {
        return this.seed;
    }

    /**
     * Gets the game type.
     */
    public WorldSettings.GameType getGameType()
    {
        return this.theGameType;
    }

    /**
     * Returns true if hardcore mode is enabled, otherwise false
     */
    public boolean getHardcoreEnabled()
    {
        return this.hardcoreEnabled;
    }

    /**
     * Get whether the map features (e.g. strongholds) generation is enabled or disabled.
     */
    public boolean isMapFeaturesEnabled()
    {
        return this.mapFeaturesEnabled;
    }

    public WorldType getTerrainType()
    {
        return this.terrainType;
    }

    /**
     * Returns true if Commands (cheats) are allowed.
     */
    public boolean areCommandsAllowed()
    {
        return this.commandsAllowed;
    }

    /**
     * Gets the GameType by ID
     */
    public static WorldSettings.GameType getGameTypeById(int id)
    {
        return WorldSettings.GameType.getByID(id);
    }

    public String getGeneratorOptions()
    {
        return this.generatorOptions;
    }

    public static enum GameType
    {
        NOT_SET(-1, "", ""),
        SURVIVAL(0, "survival", "s"),
        CREATIVE(1, "creative", "c"),
        ADVENTURE(2, "adventure", "a"),
        SPECTATOR(3, "spectator", "sp");

        int id;
        String name;
        String shortName;

        private GameType(int idIn, String nameIn, String shortNameIn)
        {
            this.id = idIn;
            this.name = nameIn;
            this.shortName = shortNameIn;
        }

        public int getID()
        {
            return this.id;
        }

        public String getName()
        {
            return this.name;
        }

        public void configurePlayerCapabilities(PlayerCapabilities capabilities)
        {
            if (this == CREATIVE)
            {
                capabilities.allowFlying = true;
                capabilities.isCreativeMode = true;
                capabilities.disableDamage = true;
            }
            else if (this == SPECTATOR)
            {
                capabilities.allowFlying = true;
                capabilities.isCreativeMode = false;
                capabilities.disableDamage = true;
                capabilities.isFlying = true;
            }
            else
            {
                capabilities.allowFlying = false;
                capabilities.isCreativeMode = false;
                capabilities.disableDamage = false;
                capabilities.isFlying = false;
            }

            capabilities.allowEdit = !this.isAdventure();
        }

        public boolean isAdventure()
        {
            return this == ADVENTURE || this == SPECTATOR;
        }

        public boolean isCreative()
        {
            return this == CREATIVE;
        }

        public boolean isSurvivalOrAdventure()
        {
            return this == SURVIVAL || this == ADVENTURE;
        }

        public static WorldSettings.GameType getByID(int idIn)
        {
            return parseGameTypeWithDefault(idIn, SURVIVAL);
        }

        public static WorldSettings.GameType parseGameTypeWithDefault(int targetId, WorldSettings.GameType fallback)
        {
            for (WorldSettings.GameType worldsettings$gametype : values())
            {
                if (worldsettings$gametype.id == targetId)
                {
                    return worldsettings$gametype;
                }
            }

            return fallback;
        }

        public static WorldSettings.GameType getByName(String gamemodeName)
        {
            return parseGameTypeWithDefault(gamemodeName, SURVIVAL);
        }

        public static WorldSettings.GameType parseGameTypeWithDefault(String targetName, WorldSettings.GameType fallback)
        {
            for (WorldSettings.GameType worldsettings$gametype : values())
            {
                if (worldsettings$gametype.name.equals(targetName) || worldsettings$gametype.shortName.equals(targetName))
                {
                    return worldsettings$gametype;
                }
            }

            return fallback;
        }
    }
}
