package com.pg85.otg.configuration;

import com.pg85.otg.BiomeIds;
import com.pg85.otg.LocalBiome;
import com.pg85.otg.LocalWorld;
import com.pg85.otg.configuration.io.SettingsMap;
import com.pg85.otg.configuration.io.SimpleSettingsMap;
import com.pg85.otg.configuration.standard.BiomeStandardValues;
import com.pg85.otg.configuration.standard.StandardBiomeTemplate;
import com.pg85.otg.configuration.standard.WorldStandardValues;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;

/**
 * Holds the WorldConfig and all BiomeConfigs.
 *
 * <p>Note: this is an internal class that is pending a rename. For backwards
 * compatibility it is still here as a public class with this name.
 */
public final class ClientConfigProvider implements ConfigProvider
{
    private WorldConfig worldConfig;

    /**
     * Holds all biome configs. Generation Id => BiomeConfig
     * <p>
     * Must be simple array for fast access. Warning: some ids may contain
     * null values, always check.
     */
    private LocalBiome[] biomes;

    public ClientConfigProvider(DataInputStream stream, LocalWorld world, boolean isSinglePlayer) throws IOException
    {
        // Create WorldConfig
        SettingsMap worldSettingsReader = new SimpleSettingsMap(world.getName(), false);
        worldSettingsReader.putSetting(WorldStandardValues.WORLD_FOG, stream.readInt());
        worldSettingsReader.putSetting(WorldStandardValues.WORLD_NIGHT_FOG, stream.readInt());

        // TODO: Probably not all of these are required on the client
        worldSettingsReader.putSetting(WorldStandardValues.WATER_LEVEL_MAX, stream.readInt());

        worldSettingsReader.putSetting(WorldStandardValues.commandBlockOutput, Boolean.parseBoolean(ConfigFile.readStringFromStream(stream))); // "true"; // Whether command blocks should notify admins when they perform commands
        worldSettingsReader.putSetting(WorldStandardValues.disableElytraMovementCheck, Boolean.parseBoolean(ConfigFile.readStringFromStream(stream))); // "false"; // Whether the server should skip checking player speed when the player is wearing elytra. Often helps with jittering due to lag in multiplayer, but may also be used to travel unfairly long distances in survival mode (cheating).
        worldSettingsReader.putSetting(WorldStandardValues.doDaylightCycle, Boolean.parseBoolean(ConfigFile.readStringFromStream(stream))); // "true"; // Whether the day-night cycle and moon phases progress
        worldSettingsReader.putSetting(WorldStandardValues.doEntityDrops, Boolean.parseBoolean(ConfigFile.readStringFromStream(stream))); // "true"; // Whether entities that are not mobs should have drops
        worldSettingsReader.putSetting(WorldStandardValues.doFireTick, Boolean.parseBoolean(ConfigFile.readStringFromStream(stream))); // "true"; // Whether fire should spread and naturally extinguish
        //this.doLimitedCrafting = reader.getSetting(WorldStandardValues.doLimitedCrafting).toString(); // "false"; // Whether players should only be able to craft recipes that they've unlocked first // TODO: Implement for 1.12
        worldSettingsReader.putSetting(WorldStandardValues.doMobLoot, Boolean.parseBoolean(ConfigFile.readStringFromStream(stream))); // "true"; // Whether mobs should drop items
        worldSettingsReader.putSetting(WorldStandardValues.doMobSpawning, Boolean.parseBoolean(ConfigFile.readStringFromStream(stream))); // "true"; // Whether mobs should naturally spawn. Does not affect monster spawners.
        worldSettingsReader.putSetting(WorldStandardValues.doTileDrops, Boolean.parseBoolean(ConfigFile.readStringFromStream(stream))); // "true"; // Whether blocks should have drops
        worldSettingsReader.putSetting(WorldStandardValues.doWeatherCycle, Boolean.parseBoolean(ConfigFile.readStringFromStream(stream))); // "true"; // Whether the weather will change
        //public String gameLoopFunction = "true"; // The function to run every game tick // TODO: Implement for 1.12
        worldSettingsReader.putSetting(WorldStandardValues.keepInventory, Boolean.parseBoolean(ConfigFile.readStringFromStream(stream))); // "false"; // Whether the player should keep items in their inventory after death
        worldSettingsReader.putSetting(WorldStandardValues.logAdminCommands, Boolean.parseBoolean(ConfigFile.readStringFromStream(stream))); // "true"; // Whether to log admin commands to server log
        //public String maxCommandChainLength = "65536"; // Determines the number at which the chain command block acts as a "chain". // TODO: Implement for 1.12
        worldSettingsReader.putSetting(WorldStandardValues.maxEntityCramming, Integer.parseInt(ConfigFile.readStringFromStream(stream))); // "24"; // The maximum number of other pushable entities a mob or player can push, before taking 3 doublehearts suffocation damage per half-second. Setting to 0 disables the rule. Damage affects survival-mode or adventure-mode players, and all mobs but bats. Pushable entities include non-spectator-mode players, any mob except bats, as well as boats and minecarts.
        worldSettingsReader.putSetting(WorldStandardValues.mobGriefing, Boolean.parseBoolean(ConfigFile.readStringFromStream(stream))); // "true"; // Whether creepers, zombies, endermen, ghasts, withers, ender dragons, rabbits, sheep, and villagers should be able to change blocks and whether villagers, zombies, skeletons, and zombie pigmen can pick up items
        worldSettingsReader.putSetting(WorldStandardValues.naturalRegeneration, Boolean.parseBoolean(ConfigFile.readStringFromStream(stream))); // "true"; // Whether the player can regenerate health naturally if their hunger is full enough (doesn't affect external healing, such as golden apples, the Regeneration effect, etc.)
        worldSettingsReader.putSetting(WorldStandardValues.randomTickSpeed, Integer.parseInt(ConfigFile.readStringFromStream(stream))); // "3"; // How often a random block tick occurs (such as plant growth, leaf decay, etc.) per chunk section per game tick. 0 will disable random ticks, higher numbers will increase random ticks
        worldSettingsReader.putSetting(WorldStandardValues.reducedDebugInfo, Boolean.parseBoolean(ConfigFile.readStringFromStream(stream))); // "false"; // Whether the debug screen shows all or reduced information; and whether the effects of F3+B (entity hitboxes) and F3+G (chunk boundaries) are shown.
        worldSettingsReader.putSetting(WorldStandardValues.sendCommandFeedback, Boolean.parseBoolean(ConfigFile.readStringFromStream(stream))); // "true"; // Whether the feedback from commands executed by a player should show up in chat. Also affects the default behavior of whether command blocks store their output text
        worldSettingsReader.putSetting(WorldStandardValues.showDeathMessages, Boolean.parseBoolean(ConfigFile.readStringFromStream(stream))); // "true"; // Whether death messages are put into chat when a player dies. Also affects whether a message is sent to the pet's owner when the pet dies.
        worldSettingsReader.putSetting(WorldStandardValues.spawnRadius, Integer.parseInt(ConfigFile.readStringFromStream(stream))); // "10"; // The number of blocks outward from the world spawn coordinates that a player will spawn in when first joining a server or when dying without a spawnpoint.
        worldSettingsReader.putSetting(WorldStandardValues.spectatorsGenerateChunks, Boolean.parseBoolean(ConfigFile.readStringFromStream(stream))); // "true"; // Whether players in spectator mode can generate chunks

        // World provider settings for worlds used as dimensions with Forge : TODO: Apply to overworld too?

        worldSettingsReader.putSetting(WorldStandardValues.welcomeMessage, ConfigFile.readStringFromStream(stream)); // ""; // A message to display to the user when they transfer to this dimension.
        worldSettingsReader.putSetting(WorldStandardValues.departMessage, ConfigFile.readStringFromStream(stream)); // A Message to display to the user when they transfer out of this dimension.
        //public boolean isHellWorld = false; // DoesWaterVaporize sets this
        worldSettingsReader.putSetting(WorldStandardValues.hasSkyLight, stream.readBoolean()); // false; // A boolean that tells if a world does not have a sky. Used in calculating weather and skylight. Also affects GetActualHeight(), hasNoSky = true worlds are seen as 128 height worlds, which affects nether portal placement/detection.
        worldSettingsReader.putSetting(WorldStandardValues.isSurfaceWorld, stream.readBoolean()); // true; // Returns 'true' if in the "main surface world", but 'false' if in the Nether or End dimensions. Affects: Clock, Compass, sky/cloud rendering, allowed to sleep here, zombie pigmen spawning in portal frames.
        //this.canCoordinateBeSpawn = reader.getSetting(WorldStandardValues.canCoordinateBeSpawn); // false; // Will check if the x, z position specified is alright to be set as the map spawn point
        worldSettingsReader.putSetting(WorldStandardValues.canRespawnHere, stream.readBoolean()); // true; // True if the player can respawn in this dimension (true = overworld, false = nether).

        worldSettingsReader.putSetting(WorldStandardValues.doesWaterVaporize, stream.readBoolean()); // false; // True for nether, any water that is placed vaporises.

        worldSettingsReader.putSetting(WorldStandardValues.doesXZShowFog, stream.readBoolean()); // false; // Returns true if the given X,Z coordinate should show environmental fog. True for Nether.

        worldSettingsReader.putSetting(WorldStandardValues.useCustomFogColor, stream.readBoolean()); // false
        worldSettingsReader.putSetting(WorldStandardValues.fogColorRed, stream.readDouble()); // 0.20000000298023224D;
        worldSettingsReader.putSetting(WorldStandardValues.fogColorGreen, stream.readDouble()); // 0.029999999329447746D;
        worldSettingsReader.putSetting(WorldStandardValues.fogColorBlue, stream.readDouble()); // 0.029999999329447746D;

        worldSettingsReader.putSetting(WorldStandardValues.isSkyColored, stream.readBoolean()); // true; // Is set to false for End (black sky?)

        //this.averageGroundlevel = reader.getSetting(WorldStandardValues.averageGroundlevel); // 0; // Affects spawn point location and village spawning. Should be equal to sea level + 1(?)

        //this.horizonHeight = reader.getSetting(WorldStandardValues.horizonHeight); // 0; // Returns horizon height for use in rendering the sky. Should be equal to sea level(?)

        worldSettingsReader.putSetting(WorldStandardValues.cloudHeight, stream.readInt()); // 0;

        worldSettingsReader.putSetting(WorldStandardValues.canDoLightning, stream.readBoolean()); // false;

        worldSettingsReader.putSetting(WorldStandardValues.canDoRainSnowIce, stream.readBoolean()); // false;

        //this.canMineBlock = reader.getSetting(WorldStandardValues.canMineBlock); // false; // If set to false players are unable to mine blocks

        worldSettingsReader.putSetting(WorldStandardValues.isNightWorld, stream.readBoolean()); // false;

        worldSettingsReader.putSetting(WorldStandardValues.voidFogYFactor, stream.readDouble()); // 0.03125D; // A double value representing the Y value relative to the top of the map at which void fog is at its maximum. The default factor of 0.03125 relative to 256, for example, means the void fog will be at its maximum at (256*0.03125), or 8.

        worldSettingsReader.putSetting(WorldStandardValues.gravityFactor, stream.readDouble()); // 0.08D; Affects entities jumping and falling

        worldSettingsReader.putSetting(WorldStandardValues.shouldMapSpin, stream.readBoolean()); // false; // Determine if the cursor on the map should 'spin' when rendered, like it does for the player in the nether.

        worldSettingsReader.putSetting(WorldStandardValues.canDropChunk, stream.readBoolean()); // true; // // Determine if the chunk at the given chunk coordinates within the provider's world can be dropped. Used in WorldProviderSurface to prevent spawn chunks from being unloaded.

        worldSettingsReader.putSetting(WorldStandardValues.RESPAWN_DIMENSION, stream.readInt()); // 0 // Dimension that players respawn in when dying in this dimension, defaults to 0, only applies when canRespawnHere = false.

        worldSettingsReader.putSetting(WorldStandardValues.MOVEMENT_FACTOR, stream.readInt()); // The dimension's movement factor. Whenever a player or entity changes dimension from world A to world B, their coordinates are multiplied by worldA.provider.getMovementFactor() / worldB.provider.getMovementFactor(). Example: Overworld factor is 1, nether factor is 8. Traveling from overworld to nether multiplies coordinates by 1/8.

        worldSettingsReader.putSetting(WorldStandardValues.ITEMS_TO_ADD_ON_JOIN_DIMENSION, ConfigFile.readStringFromStream(stream)); // Similar to the /give command, gives players items when they enter a dimension/world.

        worldSettingsReader.putSetting(WorldStandardValues.ITEMS_TO_REMOVE_ON_JOIN_DIMENSION, ConfigFile.readStringFromStream(stream)); // The opposite of the /give command, removes items from players inventories when they enter a dimension/world.

        worldSettingsReader.putSetting(WorldStandardValues.ITEMS_TO_ADD_ON_LEAVE_DIMENSION, ConfigFile.readStringFromStream(stream)); // Similar to the /give command, gives players items when they leave a dimension/world.

        worldSettingsReader.putSetting(WorldStandardValues.ITEMS_TO_REMOVE_ON_LEAVE_DIMENSION, ConfigFile.readStringFromStream(stream)); // The opposite of the /give command, removes items from players inventories when they leave a dimension/world.

        worldSettingsReader.putSetting(WorldStandardValues.ITEMS_TO_ADD_ON_RESPAWN, ConfigFile.readStringFromStream(stream)); // Similar to the /give command, gives players items when they respawn in a dimension/world.

        worldSettingsReader.putSetting(WorldStandardValues.SPAWN_POINT_SET, stream.readBoolean());

        worldSettingsReader.putSetting(WorldStandardValues.SPAWN_POINT_X, stream.readInt());

        worldSettingsReader.putSetting(WorldStandardValues.SPAWN_POINT_Y, stream.readInt());

        worldSettingsReader.putSetting(WorldStandardValues.SPAWN_POINT_Z, stream.readInt());

        worldSettingsReader.putSetting(WorldStandardValues.PLAYERS_CAN_BREAK_BLOCKS, stream.readBoolean());

        worldSettingsReader.putSetting(WorldStandardValues.EXPLOSIONS_CAN_BREAK_BLOCKS, stream.readBoolean());

        worldSettingsReader.putSetting(WorldStandardValues.PLAYERS_CAN_PLACE_BLOCKS, stream.readBoolean());

        worldConfig = new WorldConfig(new File("."), worldSettingsReader, world);

        // Custom biomes + ids
        int count = stream.readInt();
        while (count-- > 0)
        {
            String biomeName = ConfigFile.readStringFromStream(stream);
            int id = stream.readInt();
            worldConfig.customBiomeGenerationIds.put(biomeName, id);
        }

        // BiomeConfigs
        StandardBiomeTemplate defaultSettings = new StandardBiomeTemplate(worldConfig.worldHeightCap);
        biomes = new LocalBiome[world.getMaxBiomesCount()];

        count = stream.readInt();
        while (count-- > 0)
        {
            int id = stream.readInt();
            String biomeName = ConfigFile.readStringFromStream(stream);
            SettingsMap biomeReader = new SimpleSettingsMap(biomeName, false);
            biomeReader.putSetting(BiomeStandardValues.BIOME_TEMPERATURE, stream.readFloat());
            biomeReader.putSetting(BiomeStandardValues.BIOME_WETNESS, stream.readFloat());
            biomeReader.putSetting(BiomeStandardValues.SKY_COLOR, stream.readInt());
            biomeReader.putSetting(BiomeStandardValues.WATER_COLOR, stream.readInt());
            biomeReader.putSetting(BiomeStandardValues.GRASS_COLOR, stream.readInt());
            biomeReader.putSetting(BiomeStandardValues.GRASS_COLOR_IS_MULTIPLIER, stream.readBoolean());
            biomeReader.putSetting(BiomeStandardValues.FOLIAGE_COLOR, stream.readInt());
            biomeReader.putSetting(BiomeStandardValues.FOLIAGE_COLOR_IS_MULTIPLIER, stream.readBoolean());

            // TODO: Are these really necessary? <-- Maybe only for Forge SP? <-- In ClientNetworkEventListener for Forge SP packet is ignored so this doesn't actually do anything??

        	String biomeDictId = ConfigFile.readStringFromStream(stream);
        	biomeReader.putSetting(BiomeStandardValues.BIOME_DICT_ID, biomeDictId); // <-- This might be used even in MP by client mods?

            BiomeLoadInstruction instruction = new BiomeLoadInstruction(biomeName, id, defaultSettings);
            BiomeConfig config = new BiomeConfig(instruction, null, biomeReader, worldConfig);

            LocalBiome biome = world.createBiomeFor(config, new BiomeIds(id), this);
            biomes[id] = biome;
        }
    }

    @Override
    public WorldConfig getWorldConfig()
    {
        return worldConfig;
    }

    @Override
    public LocalBiome getBiomeByIdOrNull(int id)
    {
        if (id < 0 || id > biomes.length)
        {
            return null;
        }
        return biomes[id];
    }

    @Override
    public void reload()
    {
        // Does nothing on client world
    }

    @Override
    public LocalBiome[] getBiomeArray()
    {
        return this.biomes;
    }
}
