package com.Khorn.PTMBukkit;

import java.io.File;
import java.util.HashSet;

import org.bukkit.Chunk;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;



public class PTMPlugin extends JavaPlugin
{

    public final static String FILE_SEPARATOR = System.getProperty("file.separator");


    public void onDisable()
    {
        System.out.println(getDescription().getFullName() + " is now disabled");
    }

    public void onEnable()
    {


        File pluginDir = new File("plugins" + FILE_SEPARATOR + "PhoenixTerrainMod" + FILE_SEPARATOR);
        if(!pluginDir.exists())
            pluginDir.mkdir();
        System.out.println(getDescription().getFullName() + " is now enabled");



    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id)
    {
        File worldDir = new File("plugins" + FILE_SEPARATOR + "PhoenixTerrainMod"+ FILE_SEPARATOR + worldName + FILE_SEPARATOR);
        if (!worldDir.exists())
             worldDir.mkdir();
        Settings worker = new Settings("plugins" + FILE_SEPARATOR + "PhoenixTerrainMod"+ FILE_SEPARATOR + worldName + FILE_SEPARATOR,this);

        return new ChunkProviderPTM(worker);


    }
}

