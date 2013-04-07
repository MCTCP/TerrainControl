package com.khorn.terraincontrol.bukkit;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.TerrainControlEngine;
import com.khorn.terraincontrol.bukkit.commands.TCCommandExecutor;
import com.khorn.terraincontrol.configuration.TCDefaultValues;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.customobjects.BODefaultValues;
import com.khorn.terraincontrol.util.StringHelper;
import net.minecraft.server.v1_5_R2.BiomeBase;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_5_R2.block.CraftBlock;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TCPlugin extends JavaPlugin implements TerrainControlEngine
{
    private final HashMap<String, BukkitWorld> notInitedWorlds = new HashMap<String, BukkitWorld>();

    public TCListener listener;
    public TCCommandExecutor commandExecutor;

    // Debug setting. Set it to true to make Terrain Control try to disable
    // itself. However, terrain generators aren't cleaned up properly by Bukkit,
    // so this won't really work until that bug is fixed.
    public boolean cleanupOnDisable = false;

    public final HashMap<UUID, BukkitWorld> worlds = new HashMap<UUID, BukkitWorld>();

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
        if (Bukkit.getWorlds().size() != 0 && !cleanupOnDisable)
        {
            // Reload "handling"
            // (worlds are already loaded and TC didn't clean up itself)
            log(Level.SEVERE, "The server was just /reloaded! Terrain Control has problems handling this,");
            log(Level.SEVERE, "as old parts from before the reload have not been cleaned up.");
            log(Level.SEVERE, "Unexpected things may happen! Please restart the server!");
            log(Level.SEVERE, "In the future, instead of /reloading, please restart the server,");
            log(Level.SEVERE, "or reload a plugin using it's built-in command (like /tc reload),");
            log(Level.SEVERE, "or use a plugin managing plugin that can reload one plugin at a time.");
            setEnabled(false);
        } else
        {
            // Start the engine
            TerrainControl.startEngine(this);
            this.commandExecutor = new TCCommandExecutor(this);
            this.listener = new TCListener(this);
            Bukkit.getMessenger().registerOutgoingPluginChannel(this, TCDefaultValues.ChannelName.stringValue());

            TerrainControl.log("Global objects loaded, waiting for worlds to load");
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
        if(worldName.equals("")) {
            TerrainControl.log("Ignoring empty world name. Is some generator plugin checking if \"TerrainControl\" is a valid world name?");
            return new TCChunkGenerator(this);
        }
        if(worldName.equals("test")) {
            TerrainControl.log("Ignoring world with the name \"test\". This is not a valid world name,");
            TerrainControl.log("as it's used by Multiverse to check if \"TerrainControl\" a valid generator name.");
            TerrainControl.log("So if you were just using /mv create, don't worry about this message.");
            return new TCChunkGenerator(this);
        }

        // Check if not already enabled
        for (BukkitWorld world : worlds.values())
        {
            if (world.getName().equals(worldName))
            {
                TerrainControl.log("Already enabled for '" + worldName + "'");
                return world.getChunkGenerator();
            }
        }

        TerrainControl.log("Starting to enable world '" + worldName + "'...");

        // Create BukkitWorld instance
        BukkitWorld localWorld = new BukkitWorld(worldName);

        // Hack to initialize CraftBukkit's biome mappings
        // This is really needed. Try for yourself if you don't believe it,
        // you will get a java.lang.IllegalArgumentException when adding biomes
        CraftBlock.biomeBaseToBiome(BiomeBase.OCEAN);
        
        // Load settings
        File baseFolder = getWorldSettingsFolder(worldName);
        WorldConfig worldConfig = new WorldConfig(baseFolder, localWorld, false);
        localWorld.setSettings(worldConfig);

        // Add the world to the to-do list
        this.notInitedWorlds.put(worldName, localWorld);

        // Get the right chunk generator
        TCChunkGenerator generator = null;
        switch (worldConfig.ModeTerrain)
        {
            case Normal:
            case TerrainTest:
            case OldGenerator:
            case NotGenerate:
                generator = new TCChunkGenerator(this);
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
            TerrainControl.log("settings does not exist, creating defaults");

            if (!baseFolder.mkdirs())
                TerrainControl.log(Level.SEVERE, "cant create folder " + baseFolder.getName());
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
            this.worlds.put(world.getUID(), bukkitWorld);

            // Show message
            TerrainControl.log("World " + bukkitWorld.getName() + " is now enabled!");
        }
    }

    @Override
    public void log(Level level, String... msg)
    {
        Logger.getLogger("Minecraft").log(level, "[TerrainControl] " + StringHelper.join(msg, " "));
    }

    @Override
    public LocalWorld getWorld(String name)
    {
        World world = Bukkit.getWorld(name);
        if (world == null)
        {
            // World not loaded
            return null;
        }
        return this.worlds.get(world.getUID());
    }

    @Override
    public File getGlobalObjectsDirectory()
    {
        return new File(this.getDataFolder(), BODefaultValues.BO_GlobalDirectoryName.stringValue());
    }
}