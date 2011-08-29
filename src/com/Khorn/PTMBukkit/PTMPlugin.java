package com.Khorn.PTMBukkit;

import java.io.*;
import java.util.HashMap;

import com.Khorn.PTMBukkit.Generator.BiomeManagerPTM;
import com.Khorn.PTMBukkit.Generator.ChunkProviderPTM;
import com.Khorn.PTMBukkit.Listeners.PTMBlockListener;
import com.Khorn.PTMBukkit.Listeners.PTMPlayerListener;
import com.Khorn.PTMBukkit.Listeners.PTMWorldListener;
import com.Khorn.PTMBukkit.Util.FileSystemManager;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.event.Event;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;


public class PTMPlugin extends JavaPlugin
{

    public final HashMap<String, Settings> worldsSettings = new HashMap<String, Settings>();
    private final PTMBlockListener blockListener = new PTMBlockListener(this);
    private final PTMWorldListener worldListener = new PTMWorldListener(this);
    private final PTMPlayerListener playerListener = new PTMPlayerListener(this);


    public void onDisable()
    {
        System.out.println(getDescription().getFullName() + " is now disabled");
    }

    public void onEnable()
    {
        this.getCommand("ptm").setExecutor(new PTMCommand(this));
        this.RegisterEvents();

        System.out.println(getDescription().getFullName() + " is now enabled");

    }

    private void RegisterEvents()
    {
        PluginManager pm = this.getServer().getPluginManager();
        pm.registerEvent(Event.Type.BLOCK_PLACE, blockListener, Event.Priority.Monitor, this);

        pm.registerEvent(Event.Type.WORLD_INIT, worldListener, Event.Priority.High, this);

        pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Event.Priority.Normal, this);


    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id)
    {
        if (worldsSettings.containsKey(worldName))
        {
            System.out.println("PhoenixTerrainMod: enabled for '" + worldName + "'");
            return worldsSettings.get(worldName).ChunkProvider;
        }


        ChunkProviderPTM prov = new ChunkProviderPTM(this.GetSettings(worldName));


        System.out.println("PhoenixTerrainMod: enabled for '" + worldName + "'");
        return prov;


    }

    public Settings GetSettings(String worldName)
    {
        File baseFolder = new File(this.getDataFolder(), "worlds/" + worldName);
        if (!baseFolder.exists())
        {
            File oldFolder = new File(this.getDataFolder(), worldName);
            if (oldFolder.exists() && oldFolder.isDirectory())
            {
                try
                {
                    FileSystemManager.copyFolder(oldFolder, baseFolder);
                    System.out.println("PhoenixTerrainMod: config files copied to new folder");
                    FileSystemManager.deleteFile(oldFolder);
                } catch (IOException e)
                {
                    System.out.println("PhoenixTerrainMod: error copying old directory, working with defaults");
                }


            } else if (!baseFolder.mkdirs())
                System.out.println("PhoenixTerrainMod: error create directory, working with defaults");

        }

        Settings worker = new Settings(baseFolder, this);

        worldsSettings.put(worldName, worker);

        System.out.println("PhoenixTerrainMod: settings for '" + worldName + "' loaded");
        return worker;
    }


    public void WorldInit(World world)
    {
        if (this.worldsSettings.containsKey(world.getName()))
        {
            Settings worldSetting = this.worldsSettings.get(world.getName());
            if (worldSetting.isInit)
                return;

            net.minecraft.server.World workWorld = ((CraftWorld) world).getHandle();

            workWorld.worldProvider.b = new BiomeManagerPTM(workWorld, worldSetting);

            worldSetting.objectSpawner.Init(workWorld);
            worldSetting.ChunkProvider.Init(world);
            worldSetting.isInit = true;

            System.out.println("PhoenixTerrainMod: world seed is " + workWorld.getSeed());

        }
    }


    /*
   public void DebugLog(String str)
   {
       File f = new File(SettingsDir, "Debug.log");
       try
       {
           FileWriter writer = new FileWriter(f,true);


           writer.write(DateFormat.getTimeInstance().format(new Date())+ ":"+ str + System.getProperty("line.separator"));


           writer.close();

       } catch (IOException e)
       {
           e.printStackTrace();

       }
   } */

}

