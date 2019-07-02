package com.pg85.otg.bukkit;

import com.pg85.otg.OTG;
import com.pg85.otg.bukkit.commands.OTGCommandExecutor;
import com.pg85.otg.bukkit.events.OTGListener;
import com.pg85.otg.bukkit.generator.BukkitVanillaBiomeGenerator;
import com.pg85.otg.bukkit.generator.OTGChunkGenerator;
import com.pg85.otg.bukkit.generator.structures.RareBuildingStart;
import com.pg85.otg.bukkit.generator.structures.VillageStart;
import com.pg85.otg.bukkit.metrics.BukkitMetricsHelper;
import com.pg85.otg.configuration.dimensions.DimensionConfig;
import com.pg85.otg.configuration.dimensions.DimensionsConfig;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.generator.biome.VanillaBiomeGenerator;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.network.ServerConfigProvider;
import com.pg85.otg.util.minecraftTypes.StructureNames;
import net.minecraft.server.v1_12_R1.WorldGenFactory;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;

public class OTGPlugin extends JavaPlugin
{
    private OTGListener listener;
    public OTGCommandExecutor commandExecutor;

    /*
     * Debug setting. Set it to true to make Open Terrain Generator try to disable
     * itself. However, terrain generators aren't cleaned up properly by
     * Bukkit, so this won't really work until that bug is fixed.
     */
    private boolean cleanupOnDisable = false;

    public final HashMap<String, BukkitWorld> worlds = new HashMap<String, BukkitWorld>();
    private final HashMap<String, BukkitWorld> notInitedWorlds = new HashMap<String, BukkitWorld>();

    @Override
    public void onDisable()
    {
        if (cleanupOnDisable)
        {
            // Cleanup worlds
            for (BukkitWorld world : worlds.values())
            {
                world.disable();
            }
            worlds.clear();

            OTG.stopEngine();
        }
    }

    @Override
    public void onEnable()
    {
        OTG.setEngine(new BukkitEngine(this));
        if (!Bukkit.getWorlds().isEmpty() && !cleanupOnDisable)
        {
            // Reload "handling"
            // (worlds are already loaded and TC didn't clean up itself)
            OTG.log(LogMarker.FATAL, Arrays.asList(
                    "The server was just /reloaded! Open Terrain Generator has problems handling this, ",
                    "as old parts from before the reload have not been cleaned up. ",
                    "Unexpected things may happen! Please restart the server! ",
                    "In the future, instead of /reloading, please restart the server, ",
                    "or reload a plugin using it's built-in command (like /otg reload), ",
                    "or use a plugin managing plugin that can reload one plugin at a time."));
            setEnabled(false);
        } else {
            // Register vanilla generator
            OTG.getBiomeModeManager().register(VanillaBiomeGenerator.GENERATOR_NAME, BukkitVanillaBiomeGenerator.class);

            // Register structures
            try
            {
                Method registerStructure = WorldGenFactory.class.getDeclaredMethod("b", Class.class, String.class);
                registerStructure.setAccessible(true);
                registerStructure.invoke(null, RareBuildingStart.class, StructureNames.RARE_BUILDING);
                registerStructure.invoke(null, VillageStart.class, StructureNames.VILLAGE);
            } catch (Exception e) {
                OTG.log(LogMarker.FATAL, "Failed to register structures:");
                OTG.printStackTrace(LogMarker.FATAL, e);
            }

            // Start the engine
            this.commandExecutor = new OTGCommandExecutor(this);
            this.listener = new OTGListener(this);
            Bukkit.getMessenger().registerOutgoingPluginChannel(this, PluginStandardValues.ChannelName);

            OTG.log(LogMarker.DEBUG, "Global objects loaded, waiting for worlds to load");

            // Start metrics
            new BukkitMetricsHelper(this);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        return this.commandExecutor.onCommand(sender, command, label, args);
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id)
    {
        if (worldName.isEmpty())
        {
            OTG.log(LogMarker.WARN, "Ignoring empty world name. Is some generator plugin checking if \"OpenTerrainGenerator\" is a valid world name?");
            return new OTGChunkGenerator(this);
        }

        // Check if not already enabled
        BukkitWorld world = worlds.get(worldName);
        if (world != null)
        {
            OTG.log(LogMarker.DEBUG, "Already enabled for ''{}''", (Object) worldName);
            return world.getChunkGenerator();
        }

        OTG.log(LogMarker.DEBUG, "Starting to enable world ''{}''...", (Object) worldName);

		// This is a vanilla overworld, a new OTG world or a legacy OTG world without a dimensionconfig
	    if(OTG.getDimensionsConfig() == null)
	    {
	    	DimensionsConfig dimsConfig = new DimensionsConfig();
			dimsConfig.Overworld = new DimensionConfig();
			OTG.setDimensionsConfig(dimsConfig);
		}
        
        // Create BukkitWorld instance
        BukkitWorld localWorld = new BukkitWorld(worldName);

        // Load settings
        File baseFolder = getWorldSettingsFolder(worldName);
        
        // Check if world exists
	    File worldSaveDir = new File(".\\" + worldName + "\\");
	    OTG.IsNewWorldBeingCreated = !new File(worldSaveDir, "/region").exists();
        
        ServerConfigProvider configs = new ServerConfigProvider(baseFolder, localWorld, worldSaveDir);
        localWorld.setSettings(configs);
        
        OTG.IsNewWorldBeingCreated = false;

        // Add the world to the to-do list
        this.notInitedWorlds.put(worldName, localWorld);

        // Get the right chunk generator
        OTGChunkGenerator generator = null;
        switch (configs.getWorldConfig().modeTerrain)
        {
            case Normal:
            case TerrainTest:
            case OldGenerator:
            case NotGenerate:
                generator = new OTGChunkGenerator(this);
                break;
            case Default:
                break;
        }

        // Set and return the generator
        localWorld.setChunkGenerator(generator);
        return generator;
    }

    private File getWorldSettingsFolder(String worldName)
    {
        File baseFolder = new File(this.getDataFolder(), PluginStandardValues.PresetsDirectoryName + File.separator + worldName);
        if (!baseFolder.exists())
        {
            if (!baseFolder.mkdirs())
            {
                OTG.log(LogMarker.FATAL, "Can't create folder ", baseFolder.getName());
            }
        }
        return baseFolder;
    }

    public void onWorldInit(World world)
    {
        if (this.notInitedWorlds.containsKey(world.getName()))
        {
            // Remove the world from the to-do list
            BukkitWorld bukkitWorld = this.notInitedWorlds.remove(world.getName());

            // Enable and register the world
            bukkitWorld.enable(world);
            this.worlds.put(world.getName(), bukkitWorld);

            // Show message
            OTG.log(LogMarker.INFO, "World {} is now enabled!", (Object) bukkitWorld.getName());
        }
    }

    public void onWorldUnload(World world)
    {
        if (this.notInitedWorlds.containsKey(world.getName()))
        {
            // Remove the world from the to-do list
            this.notInitedWorlds.remove(world.getName());
        }
        if (this.worlds.containsKey(world.getName()))
        {
            // Disable and Remove the world from enabled list
            this.worlds.get(world.getName()).disable();
            this.worlds.remove(world.getName());
        }
        // Show message
        OTG.log(LogMarker.INFO, "World {} is now unloaded!", (Object) world.getName());
    }
}
