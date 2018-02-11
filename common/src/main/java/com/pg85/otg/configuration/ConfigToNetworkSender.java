package com.pg85.otg.configuration;

import com.pg85.otg.LocalBiome;
import com.pg85.otg.configuration.standard.WorldStandardValues;

import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Writes the relevant settings of a configuration file to a network stream.
 *
 * <p>Relevant settings include things like grass color, water color and biome
 * names: basically everything that is needed to display a world properly on the
 * client. Irrelevant settings include things like biome size, ore distribution,
 * etc.: the client doesn't need to generate chunks on its own.</p>
 */
public final class ConfigToNetworkSender
{

    /**
     * Sends the relevant settings in the {@link ConfigProvider} to the given
     * network stream.
     * @param configProvider All the settings of a world.
     * @param stream         Stream to write to.
     * @throws IOException If an IO error occurs.
     */
    public static void writeConfigsToStream(ConfigProvider configProvider, DataOutput stream, boolean isSinglePlayer) throws IOException
    {
        WorldConfig worldConfig = configProvider.getWorldConfig();
        LocalBiome[] biomes = configProvider.getBiomeArray();

        // General information
        ConfigFile.writeStringToStream(stream, worldConfig.getName());

        stream.writeInt(worldConfig.WorldFog);
        stream.writeInt(worldConfig.WorldNightFog);

        // TODO: Probably only a few of these settings are really needed on the client

        stream.writeInt(worldConfig.waterLevelMax);

        ConfigFile.writeStringToStream(stream, worldConfig.commandBlockOutput); // Whether command blocks should notify admins when they perform commands
        ConfigFile.writeStringToStream(stream, worldConfig.disableElytraMovementCheck); // Whether the server should skip checking player speed when the player is wearing elytra. Often helps with jittering due to lag in multiplayer, but may also be used to travel unfairly long distances in survival mode (cheating).
        ConfigFile.writeStringToStream(stream, worldConfig.doDaylightCycle); // Whether the day-night cycle and moon phases progress
        ConfigFile.writeStringToStream(stream, worldConfig.doEntityDrops); // Whether entities that are not mobs should have drops
        ConfigFile.writeStringToStream(stream, worldConfig.doFireTick); // Whether fire should spread and naturally extinguish
        //public String doLimitedCrafting; // Whether players should only be able to craft recipes that they've unlocked first
        ConfigFile.writeStringToStream(stream, worldConfig.doMobLoot); // Whether mobs should drop items
        ConfigFile.writeStringToStream(stream, worldConfig.doMobSpawning); // Whether mobs should naturally spawn. Does not affect monster spawners.
        ConfigFile.writeStringToStream(stream, worldConfig.doTileDrops); // Whether blocks should have drops
        ConfigFile.writeStringToStream(stream, worldConfig.doWeatherCycle); // Whether the weather will change
        //public String gameLoopFunction = "true"; // The function to run every game tick // TODO: Implement for 1.12
        ConfigFile.writeStringToStream(stream, worldConfig.keepInventory); // Whether the player should keep items in their inventory after death
        ConfigFile.writeStringToStream(stream, worldConfig.logAdminCommands); // Whether to log admin commands to server log
        //public String maxCommandChainLength = "65536"; // Determines the number at which the chain command block acts as a "chain". // TODO: Implement for 1.12
        ConfigFile.writeStringToStream(stream, worldConfig.maxEntityCramming); // The maximum number of other pushable entities a mob or player can push, before taking 3 doublehearts suffocation damage per half-second. Setting to 0 disables the rule. Damage affects survival-mode or adventure-mode players, and all mobs but bats. Pushable entities include non-spectator-mode players, any mob except bats, as well as boats and minecarts.
        ConfigFile.writeStringToStream(stream, worldConfig.mobGriefing); // Whether creepers, zombies, endermen, ghasts, withers, ender dragons, rabbits, sheep, and villagers should be able to change blocks and whether villagers, zombies, skeletons, and zombie pigmen can pick up items
        ConfigFile.writeStringToStream(stream, worldConfig.naturalRegeneration); // Whether the player can regenerate health naturally if their hunger is full enough (doesn't affect external healing, such as golden apples, the Regeneration effect, etc.)
        ConfigFile.writeStringToStream(stream, worldConfig.randomTickSpeed); // How often a random block tick occurs (such as plant growth, leaf decay, etc.) per chunk section per game tick. 0 will disable random ticks, higher numbers will increase random ticks
        ConfigFile.writeStringToStream(stream, worldConfig.reducedDebugInfo); // Whether the debug screen shows all or reduced information; and whether the effects of F3+B (entity hitboxes) and F3+G (chunk boundaries) are shown.
        ConfigFile.writeStringToStream(stream, worldConfig.sendCommandFeedback); // Whether the feedback from commands executed by a player should show up in chat. Also affects the default behavior of whether command blocks store their output text
        ConfigFile.writeStringToStream(stream, worldConfig.showDeathMessages); // Whether death messages are put into chat when a player dies. Also affects whether a message is sent to the pet's owner when the pet dies.
        ConfigFile.writeStringToStream(stream, worldConfig.spawnRadius); // The number of blocks outward from the world spawn coordinates that a player will spawn in when first joining a server or when dying without a spawnpoint.
        ConfigFile.writeStringToStream(stream, worldConfig.spectatorsGenerateChunks); // Whether players in spectator mode can generate chunks

        // World provider settings for worlds used as dimensions with Forge : TODO: Apply to overworld too?

        ConfigFile.writeStringToStream(stream, worldConfig.welcomeMessage); // A message to display to the user when they transfer to this dimension.
        ConfigFile.writeStringToStream(stream, worldConfig.departMessage); // A Message to display to the user when they transfer out of this dimension.
    	//public boolean isHellWorld = false; // DoesWaterVaporize sets this
        stream.writeBoolean(worldConfig.hasSkyLight); // A boolean that tells if a world does not have a sky. Used in calculating weather and skylight. Also affects GetActualHeight(), hasNoSky = true worlds are seen as 128 height worlds, which affects nether portal placement/detection.
        stream.writeBoolean(worldConfig.isSurfaceWorld); // Returns 'true' if in the "main surface world", but 'false' if in the Nether or End dimensions. Affects: Clock, Compass, sky/cloud rendering, allowed to sleep here, zombie pigmen spawning in portal frames.
    	//public boolean canCoordinateBeSpawn; // Will check if the x, z position specified is alright to be set as the map spawn point
        stream.writeBoolean(worldConfig.canRespawnHere); // True if the player can respawn in this dimension (true = overworld, false = nether).

        stream.writeBoolean(worldConfig.doesWaterVaporize); // True for nether, any water that is placed vaporises.

        stream.writeBoolean(worldConfig.doesXZShowFog); // Returns true if the given X,Z coordinate should show environmental fog. True for Nether.

        stream.writeBoolean(worldConfig.useCustomFogColor);
        stream.writeDouble(worldConfig.fogColorRed);
        stream.writeDouble(worldConfig.fogColorGreen);
        stream.writeDouble(worldConfig.fogColorBlue);

    	stream.writeBoolean(worldConfig.isSkyColored); // Is set to false for End (black sky?)

    	//public int averageGroundlevel; // Affects spawn point location and village spawning. Should be equal to sea level + 1(?)

    	//public int horizonHeight; // Returns horizon height for use in rendering the sky. Should be equal to sea level(?)

    	stream.writeInt(worldConfig.cloudHeight);

    	stream.writeBoolean(worldConfig.canDoLightning);

    	stream.writeBoolean(worldConfig.canDoRainSnowIce);

        //public boolean canMineBlock; // If set to false players are unable to mine blocks

    	stream.writeBoolean(worldConfig.isNightWorld); // Sky is always moon and stars but light levels are same as day, used for Cartographer

    	stream.writeDouble(worldConfig.voidFogYFactor); // A double value representing the Y value relative to the top of the map at which void fog is at its maximum. The default factor of 0.03125 relative to 256, for example, means the void fog will be at its maximum at (256*0.03125), or 8.

        stream.writeBoolean(worldConfig.shouldMapSpin); // Determine if the cursor on the map should 'spin' when rendered, like it does for the player in the nether.

        stream.writeBoolean(worldConfig.canDropChunk); // Called to determine if the chunk at the given chunk coordinates within the provider's world can be dropped. Used in WorldProviderSurface to prevent spawn chunks from being unloaded.

        stream.writeInt(worldConfig.respawnDimension); // Dimension that players respawn in when dying in this dimension, defaults to 0, only applies when canRespawnHere = false.

        stream.writeInt(worldConfig.movementFactor); // The dimension's movement factor. Whenever a player or entity changes dimension from world A to world B, their coordinates are multiplied by worldA.provider.getMovementFactor() / worldB.provider.getMovementFactor(). Example: Overworld factor is 1, nether factor is 8. Traveling from overworld to nether multiplies coordinates by 1/8.

        ConfigFile.writeStringToStream(stream, worldConfig.itemsToAddOnJoinDimension); // Similar to the /give command, gives players items when they enter a dimension/world.

        ConfigFile.writeStringToStream(stream, worldConfig.itemsToRemoveOnJoinDimension); // The opposite of the /give command, removes items from players inventories when they enter a dimension/world.

        ConfigFile.writeStringToStream(stream, worldConfig.itemsToAddOnLeaveDimension); // Similar to the /give command, gives players items when they leave a dimension/world.

		ConfigFile.writeStringToStream(stream, worldConfig.itemsToRemoveOnLeaveDimension); // The opposite of the /give command, removes items from players inventories when they leave a dimension/world.

		ConfigFile.writeStringToStream(stream, worldConfig.itemsToAddOnRespawn); // Similar to the /give command, gives players items when they respawn in a dimension/world.

		stream.writeBoolean(worldConfig.spawnPointSet); // Set this to true to set the server spawn point to SpawnPointX, SpawnPointY, SpawnPointZ

		stream.writeInt(worldConfig.spawnPointX); // Use this with SpawnPointSet: true to set a spawn coordinate.

		stream.writeInt(worldConfig.spawnPointY); // "Use this with SpawnPointSet: true to set a spawn coordinate.

		stream.writeInt(worldConfig.spawnPointZ); // "Use this with SpawnPointSet: true to set a spawn coordinate.

		stream.writeBoolean(worldConfig.playersCanBreakBlocks);  // When set to false players cannot break blocks in this world. Defaults to: true

		stream.writeBoolean(worldConfig.explosionsCanBreakBlocks);  // When set to false explosions cannot break blocks in this world. Defaults to: true

		stream.writeBoolean(worldConfig.playersCanPlaceBlocks); // When set to false players cannot place blocks in this world. Defaults to: true

        // Fetch all non-virtual biomes
        Collection<LocalBiome> nonVirtualBiomes = new ArrayList<LocalBiome>();
        Collection<LocalBiome> nonVirtualCustomBiomes = new ArrayList<LocalBiome>();
        for (LocalBiome biome : biomes)
        {
            if (biome == null)
                continue;

            if (!biome.getIds().isVirtual())
            {
                nonVirtualBiomes.add(biome);
                if (biome.isCustom())
                {
                    nonVirtualCustomBiomes.add(biome);
                }
            }
        }

        // Write them to the stream
        stream.writeInt(nonVirtualCustomBiomes.size());
        for (LocalBiome biome : nonVirtualCustomBiomes)
        {
            ConfigFile.writeStringToStream(stream, biome.getName());
            stream.writeInt(biome.getIds().getSavedId());
        }

        // BiomeConfigs
        stream.writeInt(nonVirtualBiomes.size());
        for (LocalBiome biome : nonVirtualBiomes)
        {
            if (biome == null)
            {
            	throw new RuntimeException("Whatever it is you're trying to do, we didn't write any code for it (sorry). Please contact Team OTG about this crash.");
            }
            stream.writeInt(biome.getIds().getSavedId());
            biome.getBiomeConfig().writeToStream(stream, isSinglePlayer);
        }
    }
}
