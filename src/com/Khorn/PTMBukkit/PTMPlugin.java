package com.Khorn.PTMBukkit;

import java.io.File;
import java.util.HashMap;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;


public class PTMPlugin extends JavaPlugin
{

    private final HashMap<String, ChunkProviderPTM> loadedWorlds = new HashMap<String, ChunkProviderPTM>();
    public final HashMap<String,Settings> worldsSettings = new HashMap<String, Settings>();


    public void onDisable()
    {
        System.out.println(getDescription().getFullName() + " is now disabled");
    }

    public void onEnable()
    {
        this.getCommand("ptm").setExecutor(new PTMCommand(this));

        System.out.println(getDescription().getFullName() + " is now enabled");

    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id)
    {
        if (loadedWorlds.containsKey(worldName))
        {
            System.out.println("PhoenixTerrainMod: enabled for '" + worldName + "'");
            return loadedWorlds.get(worldName);
        }




        ChunkProviderPTM prov = new ChunkProviderPTM(this.GetSettings(worldName));

        loadedWorlds.put(worldName, prov);

        System.out.println("PhoenixTerrainMod: enabled for '" + worldName + "'");
        return prov;


    }

    public Settings GetSettings(String worldName)
    {
       File baseFolder = new File(this.getDataFolder(), "worlds/" + worldName);
        if (!baseFolder.exists())
        {
            if (!baseFolder.mkdirs())
                System.out.println("PhoenixTerrainMod: error create directory, working with defaults");

        }

        Settings worker = new Settings(baseFolder, this);

        worldsSettings.put(worldName,worker);

        System.out.println("PhoenixTerrainMod: settings for '" + worldName + "' loaded");
        return  worker;
    }

}

