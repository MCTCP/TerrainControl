package com.khorn.terraincontrol.bukkit;

import com.khorn.terraincontrol.biomegenerators.BiomeGenerator;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.TerrainControlEngine;
import com.khorn.terraincontrol.bukkit.commands.TCCommandExecutor;
import com.khorn.terraincontrol.configuration.TCDefaultValues;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.customobjects.BODefaultValues;
import com.khorn.terraincontrol.util.StringHelper;
import net.minecraft.server.v1_4_6.BiomeBase;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_4_6.CraftWorld;
import org.bukkit.craftbukkit.v1_4_6.block.CraftBlock;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TCPlugin extends JavaPlugin implements TerrainControlEngine
{
    private final HashMap<String, BukkitWorld> NotInitedWorlds = new HashMap<String, BukkitWorld>();

    public TCListener listener;
    public TCCommandExecutor commandExecutor;

    public final HashMap<UUID, BukkitWorld> worlds = new HashMap<UUID, BukkitWorld>();

    public void onDisable()
    {
        TerrainControl.log("Can not be disabled.");
        TerrainControl.stopEngine();
    }

    public void onEnable()
    {
        // Start the engine
        TerrainControl.startEngine(this);

        this.commandExecutor = new TCCommandExecutor(this);

        this.listener = new TCListener(this);

        Bukkit.getMessenger().registerOutgoingPluginChannel(this, TCDefaultValues.ChannelName.stringValue());

        TerrainControl.log("Enabled");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        return this.commandExecutor.onCommand(sender, command, label, args);
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id)
    {
        if (worldName.trim().equals(""))
        {
            TerrainControl.log("world name is empty string !!");
            return null;
        }

        for (BukkitWorld world : worlds.values())
        {
            if (world.getName().equals(worldName))
            {
                TerrainControl.log("enabled for '" + worldName + "'");
                return world.getChunkGenerator();
            }
        }

        TCChunkGenerator generator = null;
        BukkitWorld world = new BukkitWorld(worldName);
        WorldConfig conf = this.CreateSettings(worldName, world);

        switch (conf.ModeTerrain)
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

        world.setChunkGenerator(generator);

        TerrainControl.log("mode " + conf.ModeTerrain.name() + " enabled for '" + worldName + "'");
        return generator;
    }

    public WorldConfig CreateSettings(String worldName, BukkitWorld bukkitWorld)
    {
        File baseFolder = new File(this.getDataFolder(), "worlds" + System.getProperty("file.separator") + worldName);

        TerrainControl.log("Loading settings for " + worldName);

        if (!baseFolder.exists())
        {
            TerrainControl.log("settings does not exist, creating defaults");

            if (!baseFolder.mkdirs())
                TerrainControl.log("cant create folder " + baseFolder.getName());
        }
        // Get for init BiomeMapping
        CraftBlock.biomeBaseToBiome(BiomeBase.OCEAN);
        WorldConfig worldConfig;
        if (bukkitWorld == null)
        {
            bukkitWorld = new BukkitWorld(worldName);
            worldConfig = new WorldConfig(baseFolder, bukkitWorld, true);

        } else
        {
            worldConfig = new WorldConfig(baseFolder, bukkitWorld, false);
            bukkitWorld.setSettings(worldConfig);
            this.NotInitedWorlds.put(worldName, bukkitWorld);
        }

        return worldConfig;
    }

    public void onWorldInit(World world)
    {
        if (this.NotInitedWorlds.containsKey(world.getName()))
        {
            BukkitWorld bukkitWorld = this.NotInitedWorlds.remove(world.getName());

            net.minecraft.server.v1_4_6.World workWorld = ((CraftWorld) world).getHandle();

            bukkitWorld.Init(workWorld);

            Class<? extends BiomeGenerator> biomeModeClass = bukkitWorld.getSettings().biomeMode;
            if (biomeModeClass != TerrainControl.getBiomeModeManager().VANILLA)
            {
                TCWorldChunkManager worldChunkManager = new TCWorldChunkManager(bukkitWorld);
                workWorld.worldProvider.d = worldChunkManager;

                BiomeGenerator biomeManager = TerrainControl.getBiomeModeManager().create(biomeModeClass, bukkitWorld, new BiomeCacheWrapper(worldChunkManager));
                worldChunkManager.setBiomeManager(biomeManager);
                bukkitWorld.setBiomeManager(biomeManager);
            }

            this.worlds.put(workWorld.getDataManager().getUUID(), bukkitWorld);

            TerrainControl.log("World initialized; seed is " + workWorld.getSeed());
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