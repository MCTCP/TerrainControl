package com.Khorn.PTMBukkit;

import java.io.File;
import java.util.HashMap;

import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;



public class PTMPlugin extends JavaPlugin
{

    private static HashMap<String,ChunkProviderPTM> loadedWorlds = new HashMap<String,ChunkProviderPTM>();


    public void onDisable()
    {
        System.out.println(getDescription().getFullName() + " is now disabled");
    }

    public void onEnable()
    {

        System.out.println(getDescription().getFullName() + " is now enabled");

    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id)
    {
        if(loadedWorlds.containsKey(worldName))
        {
            System.out.println("PhoenixTerrainMod: enabled for world '" + worldName + "'");
            return loadedWorlds.get(worldName);
        }


        File baseFolder = new File(this.getDataFolder(), "worlds/" + worldName);
        if(!baseFolder.exists())
        {
            if(!baseFolder.mkdirs())
                System.out.println("PhoenixTerrainMod: error create directory, working with defaults");

        }

        Settings worker = new Settings(baseFolder,this);
        ChunkProviderPTM prov = new ChunkProviderPTM(worker);

        loadedWorlds.put(worldName,prov);

        System.out.println("PhoenixTerrainMod: loaded and enabled for world '" + worldName + "'");
        return prov;


    }
}

