package com.Khorn.TerrainControl;

import java.io.*;
import java.util.HashMap;

import com.Khorn.TerrainControl.BiomeManager.BiomeManager;
import com.Khorn.TerrainControl.BiomeManager.BiomeManagerOld;
import com.Khorn.TerrainControl.Commands.TCCommandExecutor;
import com.Khorn.TerrainControl.Configuration.WorldConfig;
import com.Khorn.TerrainControl.Generator.ChunkProviderTC;
import com.Khorn.TerrainControl.Generator.ChunkProviderTest;
import com.Khorn.TerrainControl.Listeners.TCBlockListener;
import com.Khorn.TerrainControl.Listeners.TCPlayerListener;
import com.Khorn.TerrainControl.Listeners.TCWorldListener;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;


public class TCPlugin extends JavaPlugin
{

    public final HashMap<String, WorldConfig> worldsSettings = new HashMap<String, WorldConfig>();
    private final TCBlockListener blockListener = new TCBlockListener(this);
    private final TCWorldListener worldListener = new TCWorldListener(this);
    private final TCPlayerListener playerListener = new TCPlayerListener(this);
    private final HashMap<String, TCPlayer> sessions = new HashMap<String, TCPlayer>();


    public void onDisable()
    {
        System.out.println(getDescription().getFullName() + " is now disabled");
    }

    public void onEnable()
    {
        BiomeManagerOld.GenBiomeDiagram();

        this.getCommand("tc").setExecutor(new TCCommandExecutor(this));
        this.RegisterEvents();

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
        if (worldsSettings.containsKey(worldName))
        {
            System.out.println("TerrainControl: enabled for '" + worldName + "'");
            return worldsSettings.get(worldName).ChunkProvider;
        }


        ChunkGenerator prov = null;
        WorldConfig conf = this.GetSettings(worldName, false);
        if (conf.Mode != WorldConfig.GenMode.OnlyBiome)
        {

            if (conf.Mode == WorldConfig.GenMode.Normal || conf.Mode == WorldConfig.GenMode.TerrainTest)
                prov = new ChunkProviderTC(conf);
            else
                prov = new ChunkProviderTest(conf);
        }

        System.out.println("TerrainControl: mode " + conf.Mode.name() + " enabled for '" + worldName + "'");
        return prov;


    }

    public WorldConfig GetSettings(String worldName, boolean onlyCheck)
    {
        File baseFolder = new File(this.getDataFolder(), "worlds" + System.getProperty("file.separator") + worldName);

        if (!baseFolder.exists())
        {
            System.out.println("TerrainControl: settings does not exist, creating defaults");

            if(!baseFolder.mkdirs())
                System.out.println("TerrainControl: cant create folder " + baseFolder.getName());
        }


        WorldConfig worker = new WorldConfig(baseFolder, this, worldName);

        if (!onlyCheck)
            worldsSettings.put(worldName, worker);

        System.out.println("TerrainControl: settings for '" + worldName + "' loaded");
        return worker;
    }


    public void WorldInit(World world)
    {
        if (this.worldsSettings.containsKey(world.getName()))
        {
            WorldConfig worldSetting = this.worldsSettings.get(world.getName());
            if (worldSetting.isInit)
                return;

            net.minecraft.server.World workWorld = ((CraftWorld) world).getHandle();

            if (worldSetting.oldBiomeGenerator)
                workWorld.worldProvider.b = new BiomeManagerOld(workWorld, worldSetting);
            else
                workWorld.worldProvider.b = new BiomeManager(workWorld, worldSetting);

            switch (worldSetting.Mode)
            {

                case Normal:
                {
                    worldSetting.objectSpawner.Init(workWorld);
                    worldSetting.ChunkProvider.Init(world);
                    workWorld.worldProvider.e = true;
                    break;
                }
                case TerrainTest:
                {
                    worldSetting.ChunkProvider.Init(world);
                    break;
                }
                case OnlyBiome:
                    break;
                case NotGenerate:
                    break;
            }


            worldSetting.isInit = true;

            System.out.println("TerrainControl: world seed is " + workWorld.getSeed());

        }
    }

    private void RegisterEvents()
    {
        PluginManager pm = this.getServer().getPluginManager();
        pm.registerEvent(Event.Type.BLOCK_PLACE, blockListener, Event.Priority.Monitor, this);

        pm.registerEvent(Event.Type.WORLD_INIT, worldListener, Event.Priority.High, this);

        pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Event.Priority.Normal, this);


    }

}
//TODO Fix lighting

