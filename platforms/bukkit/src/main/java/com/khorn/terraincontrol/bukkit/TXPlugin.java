package com.khorn.terraincontrol.bukkit;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.bukkit.commands.TXCommandExecutor;
import com.khorn.terraincontrol.bukkit.events.TCListener;
import com.khorn.terraincontrol.bukkit.generator.BukkitVanillaBiomeGenerator;
import com.khorn.terraincontrol.bukkit.generator.TXChunkGenerator;
import com.khorn.terraincontrol.bukkit.generator.structures.TXRareBuildingGen.RareBuildingStart;
import com.khorn.terraincontrol.bukkit.generator.structures.TXVillageGen.VillageStart;
import com.khorn.terraincontrol.bukkit.metrics.BukkitMetricsHelper;
import com.khorn.terraincontrol.configuration.ServerConfigProvider;
import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;
import com.khorn.terraincontrol.generator.biome.VanillaBiomeGenerator;
import com.khorn.terraincontrol.logging.LogMarker;
import com.khorn.terraincontrol.util.minecraftTypes.StructureNames;
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

public class TXPlugin extends JavaPlugin
{

    public TCListener listener;
    public TXCommandExecutor commandExecutor;

    /*
     * Debug setting. Set it to true to make Terrain Control try to disable
     * itself. However, terrain generators aren't cleaned up properly by
     * Bukkit, so this won't really work until that bug is fixed.
     */
    public boolean cleanupOnDisable = false;

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

            TerrainControl.stopEngine();
        }
    }

    @Override
    public void onEnable()
    {

        TerrainControl.setEngine(new BukkitEngine(this));
        if (!Bukkit.getWorlds().isEmpty() && !cleanupOnDisable)
        {
            // Reload "handling"
            // (worlds are already loaded and TC didn't clean up itself)
            TerrainControl.log(LogMarker.FATAL, Arrays.asList(
                    "The server was just /reloaded! Terrain Control has problems handling this, ",
                    "as old parts from before the reload have not been cleaned up. ",
                    "Unexpected things may happen! Please restart the server! ",
                    "In the future, instead of /reloading, please restart the server, ",
                    "or reload a plugin using it's built-in command (like /tc reload), ",
                    "or use a plugin managing plugin that can reload one plugin at a time."));
            setEnabled(false);
        } else
        {
            // Register vanilla generator
            TerrainControl.getBiomeModeManager().register(VanillaBiomeGenerator.GENERATOR_NAME, BukkitVanillaBiomeGenerator.class);

            // Register structures
            try
            {
                Method registerStructure = WorldGenFactory.class.getDeclaredMethod("b", Class.class, String.class);
                registerStructure.setAccessible(true);
                registerStructure.invoke(null, RareBuildingStart.class, StructureNames.RARE_BUILDING);
                registerStructure.invoke(null, VillageStart.class, StructureNames.VILLAGE);
            } catch (Exception e)
            {
                TerrainControl.log(LogMarker.FATAL, "Failed to register structures:");
                TerrainControl.printStackTrace(LogMarker.FATAL, e);
            }

            // Start the engine
            this.commandExecutor = new TXCommandExecutor(this);
            this.listener = new TCListener(this);
            Bukkit.getMessenger().registerOutgoingPluginChannel(this, PluginStandardValues.ChannelName);

            TerrainControl.log(LogMarker.INFO, "Global objects loaded, waiting for worlds to load");

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
            TerrainControl.log(LogMarker.DEBUG, "Ignoring empty world name. Is some generator plugin checking if \"TerrainControl\" is a valid world name?");
            return new TXChunkGenerator(this);
        }

        // Check if not already enabled
        BukkitWorld world = worlds.get(worldName);
        if (world != null)
        {
            TerrainControl.log(LogMarker.DEBUG, "Already enabled for ''{}''", (Object) worldName);
            return world.getChunkGenerator();
        }

        TerrainControl.log(LogMarker.INFO, "Starting to enable world ''{}''...", (Object) worldName);

        // Create BukkitWorld instance
        BukkitWorld localWorld = new BukkitWorld(worldName);

        // Load settings
        File baseFolder = getWorldSettingsFolder(worldName);
        ServerConfigProvider configs = new ServerConfigProvider(baseFolder, localWorld);
        localWorld.setSettings(configs);

        // Add the world to the to-do list
        this.notInitedWorlds.put(worldName, localWorld);

        // Get the right chunk generator
        TXChunkGenerator generator = null;
        switch (configs.getWorldConfig().ModeTerrain)
        {
            case Normal:
            case TerrainTest:
            case OldGenerator:
            case NotGenerate:
                generator = new TXChunkGenerator(this);
                break;
            case Default:
                break;
        }

        // Set and return the generator
        localWorld.setChunkGenerator(generator);
        return generator;
    }

    public File getWorldSettingsFolder(String worldName)
    {
        File baseFolder = new File(this.getDataFolder(), "worlds" + File.separator + worldName);
        if (!baseFolder.exists())
        {
            if (!baseFolder.mkdirs())
                TerrainControl.log(LogMarker.FATAL, "Can't create folder ", baseFolder.getName());
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
            TerrainControl.log(LogMarker.INFO, "World {} is now enabled!", (Object) bukkitWorld.getName());
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
        TerrainControl.log(LogMarker.INFO, "World {} is now unloaded!", (Object) world.getName());
    }

}
