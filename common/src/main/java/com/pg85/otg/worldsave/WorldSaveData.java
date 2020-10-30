package com.pg85.otg.worldsave;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import com.pg85.otg.OTG;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.configuration.standard.WorldStandardValues;
import com.pg85.otg.logging.LogMarker;

public class WorldSaveData
{
	public int version;
	
	public WorldSaveData() {}
	
	public WorldSaveData(String version)
	{
		this.version = Integer.parseInt(version);
	}
	
	public WorldSaveData(int version)
	{
		this.version = version;
	}
	   
    // Saving / Loading
    // TODO: It's crude but it works, can improve later
    
	public static void saveWorldSaveData(Path worldSaveDir, WorldSaveData worldSaveData)
	{
		File worldSaveDataFile = new File(worldSaveDir + File.separator + PluginStandardValues.PLUGIN_NAME + File.separator + WorldStandardValues.WorldSaveDataFileName);
		File worldSaveDataBackupFile = new File(worldSaveDir + File.separator + PluginStandardValues.PLUGIN_NAME + File.separator + WorldStandardValues.WorldSaveDataBackupFileName);

		StringBuilder stringbuilder = new StringBuilder();
		stringbuilder.append(worldSaveData.version);

		BufferedWriter writer = null;
        try
        {
    		if(!worldSaveDataFile.exists())
    		{
    			worldSaveDataFile.getParentFile().mkdirs();
    		} else {
    			Files.move(worldSaveDataFile.toPath(), worldSaveDataBackupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    		}
    		
        	writer = new BufferedWriter(new FileWriter(worldSaveDataFile));
            writer.write(stringbuilder.toString());
            OTG.log(LogMarker.DEBUG, "World save data saved");
        }
        catch (IOException e)
        {
			e.printStackTrace();
			throw new RuntimeException(
				"OTG encountered a critical error writing " + worldSaveDataFile.getAbsolutePath() + ", exiting. "
				+ "OTG automatically backs up files before writing and will try to use the backup when loading. "
				+ "If your world's " + WorldStandardValues.WorldSaveDataFileName + " and its backup have been corrupted, you can "
				+ "replace it with your own backup or create a new world with the same dimensions and copy its " 
				+ WorldStandardValues.WorldSaveDataFileName + "."
			);
        }
        finally
        {
            try
            {
                writer.close();
            } catch (Exception e) { }
        }
	}
		
	public static WorldSaveData loadWorldSaveData(Path worldSaveDir)
	{
		File worldSaveDataFile = new File(worldSaveDir + File.separator + PluginStandardValues.PLUGIN_NAME + File.separator + WorldStandardValues.WorldSaveDataFileName);
		File worldSaveDataBackupFile = new File(worldSaveDir + File.separator + PluginStandardValues.PLUGIN_NAME + File.separator + WorldStandardValues.WorldSaveDataBackupFileName);
		
		if(!worldSaveDataFile.exists() && !worldSaveDataBackupFile.exists())
		{
			return null;
		}
		
		String version = null;
		if(worldSaveDataFile.exists())
		{
			boolean bSuccess = false;		
			try {
				StringBuilder stringbuilder = new StringBuilder();
				BufferedReader reader = new BufferedReader(new FileReader(worldSaveDataFile));
				try {
					String line = reader.readLine();

				    while (line != null)
				    {
				    	stringbuilder.append(line);
				        line = reader.readLine();
				    }
				    if(stringbuilder.length() > 0)
				    {
				    	version = stringbuilder.toString().split(",")[0];
					    bSuccess = true;
					    OTG.log(LogMarker.DEBUG, "World save data loaded");				    	
				    }				    
				} finally {
					reader.close();
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
				OTG.log(LogMarker.WARN, "Failed to load " + worldSaveDataFile.getAbsolutePath() + ", trying to load backup.");
			}
			if(bSuccess)
			{
				return version != null && version.trim().length() > 0 ? new WorldSaveData(version) : null; 
			}
		}
		
		if(worldSaveDataBackupFile.exists())
		{
			boolean bSuccess = false;		
			try {
				StringBuilder stringbuilder = new StringBuilder();
				BufferedReader reader = new BufferedReader(new FileReader(worldSaveDataBackupFile));
				try {
					String line = reader.readLine();

				    while (line != null)
				    {
				    	stringbuilder.append(line);
				        line = reader.readLine();
				    }
				    if(stringbuilder.length() > 0)
				    {
				    	version = stringbuilder.toString().split(",")[0];
					    bSuccess = true;
					    OTG.log(LogMarker.DEBUG, "World save data loaded");				    	
				    }				    
				} finally {
					reader.close();
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
				OTG.log(LogMarker.WARN, "Failed to load " + worldSaveDataFile.getAbsolutePath() + ", trying to load backup.");
			}
			if(bSuccess)
			{
				return version != null && version.trim().length() > 0 ? new WorldSaveData(version) : null; 
			}
		}
		
		throw new RuntimeException(
				"OTG encountered a critical error loading " + worldSaveDataFile.getAbsolutePath() + " and could not load a backup, exiting."
				+ "OTG automatically backs up files before writing and tries to use the backup when loading. "
				+ "If your world's " + WorldStandardValues.WorldSaveDataFileName + " and its backup have been corrupted, you can "
				+ "replace it with your own backup or create a new world with the same dimensions and copy its " 
				+ WorldStandardValues.WorldSaveDataFileName + "."
			);
	}
}