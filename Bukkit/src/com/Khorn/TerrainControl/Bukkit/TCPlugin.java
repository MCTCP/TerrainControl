package com.Khorn.TerrainControl.Bukkit;

import java.io.*;
import java.util.HashMap;
import java.util.UUID;

import com.Khorn.TerrainControl.Bukkit.BiomeManager.BiomeManager;
import com.Khorn.TerrainControl.Bukkit.BiomeManager.BiomeManagerOld;
import com.Khorn.TerrainControl.Bukkit.Commands.TCCommandExecutor;
import com.Khorn.TerrainControl.Configuration.TCDefaultValues;
import com.Khorn.TerrainControl.Configuration.WorldConfig;
import net.minecraft.server.BiomeBase;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

public class TCPlugin extends JavaPlugin
{
    private final HashMap<String, BukkitWorld> NotInitedWorlds = new HashMap<String, BukkitWorld>();

    @SuppressWarnings("UnusedDeclaration")
    private TCListener listener;
    private final HashMap<String, TCPlayer> sessions = new HashMap<String, TCPlayer>();

    public final HashMap<UUID, BukkitWorld> worlds = new HashMap<UUID, BukkitWorld>();

    public void onDisable()
    {
        System.out.println(getDescription().getFullName() + " can`t be disabled");
    }

    public void onEnable()
    {
        BiomeManagerOld.GenBiomeDiagram();

        if (this.getCommand("tc") != null)
            this.getCommand("tc").setExecutor(new TCCommandExecutor(this));
        this.getCommand("terraincontrol").setExecutor(new TCCommandExecutor(this));
        this.listener = new TCListener(this);

        Bukkit.getMessenger().registerIncomingPluginChannel(this, TCDefaultValues.ChannelName.stringValue(),this.listener);
        Bukkit.getMessenger().registerOutgoingPluginChannel(this,TCDefaultValues.ChannelName.stringValue());
        

        System.out.println(getDescription().getFullName() + " is now enabled");
    }

    public TCPlayer GetPlayer(Player bukkitPlayer)
    {
        TCPlayer player;
        synchronized (this.sessions)
        {
            if (this.sessions.containsKey(bukkitPlayer.getName()))
                return this.sessions.get(bukkitPlayer.getName());
            player = new TCPlayer(bukkitPlayer);
            this.sessions.put(bukkitPlayer.getName(), player);
        }
        return player;
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id)
    {
        if (worldName.trim().equals(""))
        {
            System.out.println("TerrainControl: world name is empty string !!");
            return null;
        }

        for (BukkitWorld world : worlds.values())
        {
            if (world.getName().equals(worldName))
            {
                System.out.println("TerrainControl: enabled for '" + worldName + "'");
                return world.getChunkGenerator();
            }
        }

        TCChunkGenerator prov = null;
        BukkitWorld world = new BukkitWorld(worldName);
        WorldConfig conf = this.CreateSettings(worldName, world);

        switch (conf.ModeTerrain)
        {
            case Normal:
            case TerrainTest:
            case OldGenerator:
            case NotGenerate:
                prov = new TCChunkGenerator(this);
                break;
            case Default:
                break;
        }

        world.setChunkGenerator(prov);

        System.out.println("TerrainControl: mode " + conf.ModeTerrain.name() + " enabled for '" + worldName + "'");
        return prov;
    }

    public WorldConfig CreateSettings(String worldName, BukkitWorld bukkitWorld)
    {
        File baseFolder = new File(this.getDataFolder(), "worlds" + System.getProperty("file.separator") + worldName);

        if (!baseFolder.exists())
        {
            System.out.println("TerrainControl: settings does not exist, creating defaults");

            if (!baseFolder.mkdirs())
                System.out.println("TerrainControl: cant create folder " + baseFolder.getName());
        }
        // Get for init BiomeMapping
        CraftBlock.biomeBaseToBiome(BiomeBase.OCEAN);
        WorldConfig worldConfig;
        if (bukkitWorld == null)
        {
            bukkitWorld = new BukkitWorld(worldName);
            worldConfig = new WorldConfig(baseFolder, bukkitWorld, true);

        }
        else
        {
            worldConfig = new WorldConfig(baseFolder, bukkitWorld, false);
            bukkitWorld.setSettings(worldConfig);
            this.NotInitedWorlds.put(worldName, bukkitWorld);
        }

        System.out.println("TerrainControl: settings for '" + worldName + "' loaded");
        return worldConfig;
    }


    public void WorldInit(World world)
    {
        if (this.NotInitedWorlds.containsKey(world.getName()))
        {

            BukkitWorld bukkitWorld = this.NotInitedWorlds.remove(world.getName());

            net.minecraft.server.World workWorld = ((CraftWorld) world).getHandle();

            bukkitWorld.Init(workWorld);

            switch (bukkitWorld.getSettings().ModeBiome)
            {
                case Normal:
                    BiomeManager manager = new BiomeManager(bukkitWorld);
                    workWorld.worldProvider.c = manager;
                    bukkitWorld.setBiomeManager(manager);
                    break;
                case OldGenerator:
                    BiomeManagerOld managerOld = new BiomeManagerOld(bukkitWorld);
                    workWorld.worldProvider.c = managerOld;
                    bukkitWorld.setOldBiomeManager(managerOld);
                    break;
                case Default:
                    break;
            }

            this.worlds.put(workWorld.getUUID(),bukkitWorld);

            System.out.println("TerrainControl: world initialized with seed is " + workWorld.getSeed());

        }
    }
}