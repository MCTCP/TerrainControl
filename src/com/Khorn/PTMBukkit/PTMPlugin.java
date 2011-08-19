package com.Khorn.PTMBukkit;

import java.io.*;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import sun.management.FileSystem;

import javax.jnlp.FileSaveService;
import javax.naming.spi.DirectoryManager;


public class PTMPlugin extends JavaPlugin
{

    private final HashMap<String, ChunkProviderPTM> loadedWorlds = new HashMap<String, ChunkProviderPTM>();
    public final HashMap<String, Settings> worldsSettings = new HashMap<String, Settings>();


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
            File oldFolder = new File(this.getDataFolder(), worldName);
            if (oldFolder.exists() && oldFolder.isDirectory())
            {
                try
                {
                    PTMPlugin.copyFolder(oldFolder, baseFolder);
                    System.out.println("PhoenixTerrainMod: config files copied to new folder");
                    PTMPlugin.deleteFile(oldFolder);
                } catch (IOException e)
                {
                    System.out.println("PhoenixTerrainMod: error copying old directory, working with defaults");
                }


            }else
            if (!baseFolder.mkdirs())
                System.out.println("PhoenixTerrainMod: error create directory, working with defaults");

        }

        Settings worker = new Settings(baseFolder, this);

        worldsSettings.put(worldName, worker);

        System.out.println("PhoenixTerrainMod: settings for '" + worldName + "' loaded");
        return worker;
    }

    private static void copyFolder(File src, File dest) throws IOException
    {

        if (src.isDirectory())
        {

            //if directory not exists, create it
            if (!dest.exists())
            {
                //noinspection ResultOfMethodCallIgnored
                dest.mkdirs();

            }

            //list all the directory contents
            String files[] = src.list();

            for (String file : files)
            {
                //construct the src and dest file structure
                File srcFile = new File(src, file);
                File destFile = new File(dest, file);
                //recursive copy
                copyFolder(srcFile, destFile);
            }

        } else
        {
            //if file, then copy it
            //Use bytes stream to support all file types
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dest);

            byte[] buffer = new byte[1024];

            int length;
            //copy the file content in bytes
            while ((length = in.read(buffer)) > 0)
            {
                out.write(buffer, 0, length);
            }

            in.close();
            out.close();
        }
    }
    private static void deleteFile(File src) {

    // Make sure the file or directory exists and isn't write protected
    if (!src.exists())
      return;
    if (!src.canWrite())
      return;

    // If it is a directory, make sure it is empty
    if (src.isDirectory()) {
      String[] files = src.list();
        for(String file : files)
        {
            deleteFile(new File(file));
        }

    }
    if(!src.delete())
       System.out.println("PhoenixTerrainMod: can't delete " + src.getName());

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

