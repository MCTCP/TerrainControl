package com.khorn.terraincontrol.bukkit;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.TerrainControlEngine;
import com.khorn.terraincontrol.bukkit.commands.TCCommandExecutor;
import com.khorn.terraincontrol.bukkit.util.BukkitMetricsHelper;
import com.khorn.terraincontrol.configuration.TCDefaultValues;
import com.khorn.terraincontrol.configuration.TCLogManager;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.customobjects.BODefaultValues;
import com.khorn.terraincontrol.util.StringHelper;
import net.minecraft.server.v1_6_R2.BiomeBase;
import net.minecraft.server.v1_6_R2.Block;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_6_R2.block.CraftBlock;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class TCPlugin extends JavaPlugin implements TerrainControlEngine
{
    public TCListener listener;
    public TCCommandExecutor commandExecutor;

    /* Debug setting. Set it to true to make Terrain Control try to disable
     * itself. However, terrain generators aren't cleaned up properly by
     * Bukkit, so this won't really work until that bug is fixed.
     */
    public boolean cleanupOnDisable = false;

    public final HashMap<UUID, BukkitWorld> worlds = new HashMap<UUID, BukkitWorld>();
    
    private final HashMap<String, BukkitWorld> notInitedWorlds = new HashMap<String, BukkitWorld>();
    private Logger logger;

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

        TerrainControl.setEngine(this);
        logger = TCLogManager.getLogger(this);

        if (Bukkit.getWorlds().size() != 0 && !cleanupOnDisable)
        {
            // Reload "handling"
            // (worlds are already loaded and TC didn't clean up itself)
            TerrainControl.log(Level.SEVERE,
                               "The server was just /reloaded! Terrain Control has problems handling this, ",
                               "as old parts from before the reload have not been cleaned up. ",
                               "Unexpected things may happen! Please restart the server! ",
                               "In the future, instead of /reloading, please restart the server, ",
                               "or reload a plugin using it's built-in command (like /tc reload), ",
                               "or use a plugin managing plugin that can reload one plugin at a time.");
            setEnabled(false);
        } else
        {
            if (Bukkit.getVersion().contains("MCPC-Plus"))
            {
                // We're on MCPC+, so enable the extra block ids.
                TerrainControl.supportedBlockIds = 4095;
                this.log(Level.INFO, "MCPC+ detected, enabling extended block id support.");
            }

            // Start the engine
            TerrainControl.startEngine();
            this.commandExecutor = new TCCommandExecutor(this);
            this.listener = new TCListener(this);
            Bukkit.getMessenger().registerOutgoingPluginChannel(this, TCDefaultValues.ChannelName.stringValue());

            TerrainControl.log(Level.INFO, "Global objects loaded, waiting for worlds to load");

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
        if (worldName.equals(""))
        {
            TerrainControl.log(Level.CONFIG, "Ignoring empty world name. Is some generator plugin checking if \"TerrainControl\" is a valid world name?");
            return new TCChunkGenerator(this);
        }
        if (worldName.equals("test"))
        {
            TerrainControl.log(Level.CONFIG,
                               "Ignoring world with the name \"test\". This is not a valid world name, ",
                               "as it's used by Multiverse to check if \"TerrainControl\" a valid generator name. ",
                               "So if you were just using /mv create, don't worry about this message.");
            return new TCChunkGenerator(this);
        }

        // Check if not already enabled
        for (BukkitWorld world : worlds.values())
        {
            if (world.getName().equals(worldName))
            {
                TerrainControl.log(Level.CONFIG, "Already enabled for ''{0}''", worldName);
                return world.getChunkGenerator();
            }
        }

        TerrainControl.log(Level.INFO, "Starting to enable world ''{0}''...", worldName);

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
            TerrainControl.log(Level.SEVERE, "TC was not allowed to create folder {0}", baseFolder.getName());

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
            TerrainControl.log(Level.INFO, "World {0} is now enabled!", bukkitWorld.getName());
        }
    }

    @Override
    public void logIfLevel(Level ifLevel, String... messages)
    {
        if (logger.getLevel().intValue() == ifLevel.intValue())
        {
            this.log(ifLevel, messages);
        }
    }

    @Override
    public void logIfLevel(Level ifLevel, String messages, Object[] params)
    {
        if (logger.getLevel().intValue() == ifLevel.intValue())
        {
            this.log(ifLevel, messages, params);
        }
    }

    @Override
    public void logIfLevel(Level min, Level max, String... messages)
    {
        if (logger.getLevel().intValue() <= max.intValue() && logger.getLevel().intValue() >= min.intValue())
        {
            this.log(max, messages);
        }
    }

    @Override
    public void logIfLevel(Level min, Level max, String messages, Object[] params)
    {
        if (logger.getLevel().intValue() <= max.intValue() && logger.getLevel().intValue() >= min.intValue())
        {
            this.log(max, messages);
        }
    }
    
    @Override
    public void log(Level level, String... messages)
    {
        this.log(level, "{0}", new Object[]
        {
            StringHelper.join(messages, " ")
        });
    }

    @Override
    public void log(Level level, String message, Object param)
    {
        LogRecord lr = new LogRecord(level, message);
        lr.setMessage(TCLogManager.formatter.format(lr));
        lr.setParameters(new Object[]
        {
            param
        });
        if (logger == null)
        {
            logger = TCLogManager.getLogger();
        }
        logger.log(lr);
    }

    @Override
    public void log(Level level, String message, Object[] params)
    {
        LogRecord lr = new LogRecord(level, message);
        lr.setMessage(TCLogManager.formatter.format(lr));
        lr.setParameters(params);
        if (logger == null)
        {
            logger = TCLogManager.getLogger();
        }
        logger.log(lr);
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
    public File getTCDataFolder()
    {
        return this.getDataFolder();
    }

    @Override
    public File getGlobalObjectsDirectory()
    {
        return new File(this.getTCDataFolder(), BODefaultValues.BO_GlobalDirectoryName.stringValue());
    }

    @Override
    public boolean isValidBlockId(int id)
    {
        if (id == 0)
        {
            // Air is a special case
            return true;
        }
        if (id < 0 || id > TerrainControl.supportedBlockIds)
        {
            return false;
        }
        if (Block.byId[id] == null)
        {
            return false;
        }
        return true;
    }

}