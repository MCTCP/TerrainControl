package com.pg85.otg.worldsave;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.configuration.standard.WorldStandardValues;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.network.ServerConfigProvider;

public class BiomeIdData
{
	public String biomeName;
	public int otgBiomeId;
	public int savedBiomeId;
	
	public BiomeIdData() {}
	
	public BiomeIdData(String biomeName, int otgBiomeId, int savedBiomeId)
	{
		this.biomeName = biomeName;
		this.otgBiomeId = otgBiomeId;
		this.savedBiomeId = savedBiomeId;
	}
	   
    // Saving / Loading
    // TODO: It's crude but it works, can improve later
    
	public static void saveBiomeIdData(File worldSaveDir, ServerConfigProvider serverConfigProvider, LocalWorld world)
	{
        // If this is a previously created world then register biomes to the same OTG biome id as before.
        ArrayList<BiomeIdData> loadedBiomeIdData = loadBiomeIdData(worldSaveDir);
		
		File biomeIdDataFile = new File(worldSaveDir + File.separator + PluginStandardValues.PLUGIN_NAME + File.separator + WorldStandardValues.BiomeIdDataFileName);
		File biomeIdDataBackupFile = new File(worldSaveDir + File.separator + PluginStandardValues.PLUGIN_NAME + File.separator + WorldStandardValues.BiomeIdDataBackupFileName);
		
		StringBuilder stringbuilder = new StringBuilder();

        if(loadedBiomeIdData != null)
        {
    		for(BiomeIdData biomeIdData : loadedBiomeIdData)
    		{
    			stringbuilder.append((stringbuilder.length() == 0 ? "" : ",") + biomeIdData.biomeName + "," + biomeIdData.savedBiomeId + "," + biomeIdData.otgBiomeId);
			}
        }

        boolean bFound = false;
		for(LocalBiome biome : serverConfigProvider.getBiomeArrayByOTGId())
		{
			if(biome != null)
			{
				bFound = false;
				if(loadedBiomeIdData != null)
				{
					for(BiomeIdData biomeIdData : loadedBiomeIdData)
					{
						if(biomeIdData.biomeName.equals(world.getName() + "_" + biome.getName()))
						{
							bFound = true;
						}
					}
				}
				if(!bFound)
				{
					stringbuilder.append((stringbuilder.length() == 0 ? "" : ",") + world.getName() + "_" + biome.getName() + "," + biome.getIds().getSavedId() + "," + biome.getIds().getOTGBiomeId());
				}
			}			 
		}
		
		BufferedWriter writer = null;
        try
        {
    		if(!biomeIdDataFile.exists())
    		{
    			biomeIdDataFile.getParentFile().mkdirs();
    		} else {
    			Files.move(biomeIdDataFile.toPath(), biomeIdDataBackupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    		}
        	
        	writer = new BufferedWriter(new FileWriter(biomeIdDataFile));
            writer.write(stringbuilder.toString());
            OTG.log(LogMarker.DEBUG, "Custom dimension data saved");
        }
        catch (IOException e)
        {
			e.printStackTrace();
			throw new RuntimeException(
				"OTG encountered a critical error writing " + biomeIdDataFile.getAbsolutePath() + ", exiting. "
				+ "OTG automatically backs up files before writing and will try to use the backup when loading. "					
				+ "If your world's " + WorldStandardValues.BiomeIdDataFileName + " and its backup have been corrupted, "
				+ "you can replace it with a backup or create a new world with the same dimensions and copy its " 
				+ WorldStandardValues.BiomeIdDataFileName + ".");
        }
        finally
        {
            try
            {
                writer.close();
            } catch (Exception e) { }
        }
	}
		
	public static ArrayList<BiomeIdData> loadBiomeIdData(File worldSaveDir)
	{
		File biomeIdDataFile = new File(worldSaveDir + File.separator + PluginStandardValues.PLUGIN_NAME + File.separator + WorldStandardValues.BiomeIdDataFileName);
		File biomeIdDataBackupFile = new File(worldSaveDir + File.separator + PluginStandardValues.PLUGIN_NAME + File.separator + WorldStandardValues.BiomeIdDataBackupFileName);
		
		if(!biomeIdDataFile.exists() && !biomeIdDataBackupFile.exists())
		{
			return null;
		}
		
		if(biomeIdDataFile.exists())
		{
			String[] biomeIdDataFileValues = {};
			boolean bSuccess = false;
			try {
				StringBuilder stringbuilder = new StringBuilder();
				BufferedReader reader = new BufferedReader(new FileReader(biomeIdDataFile));
				try {
					String line = reader.readLine();

				    while (line != null)
				    {
				    	stringbuilder.append(line);
				        line = reader.readLine();
				    }
				    if(stringbuilder.length() > 0)
				    {
				    	biomeIdDataFileValues = stringbuilder.toString().split(",");				    	
				    }
				    bSuccess = true;
				    OTG.log(LogMarker.DEBUG, "Biome Id data loaded");				    
				} finally {
					reader.close();
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
				OTG.log(LogMarker.WARN, "Failed to load " + biomeIdDataFile.getAbsolutePath() + ", trying to load backup.");
			}
			
			if(bSuccess)
			{
				try
				{
					ArrayList<BiomeIdData> biomeIdDatas = parseBiomeIdData(biomeIdDataFileValues);
					return biomeIdDatas.size() == 0 ? null : biomeIdDatas;
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
					OTG.log(LogMarker.WARN, "Failed to load " + biomeIdDataFile.getAbsolutePath() + ", trying to load backup.");
				}
			}			
		}
		
		if(biomeIdDataBackupFile.exists())
		{
			String[] biomeIdDataFileValues = {};
			boolean bSuccess = false;
			try {
				StringBuilder stringbuilder = new StringBuilder();
				BufferedReader reader = new BufferedReader(new FileReader(biomeIdDataBackupFile));
				try {
					String line = reader.readLine();

				    while (line != null)
				    {
				    	stringbuilder.append(line);
				        line = reader.readLine();
				    }
				    if(stringbuilder.length() > 0)
				    {
				    	biomeIdDataFileValues = stringbuilder.toString().split(",");
				    }
				    OTG.log(LogMarker.DEBUG, "Biome Id data loaded");
				    bSuccess = true;
				} finally {
					reader.close();
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			
			if(bSuccess)
			{
				try
				{
					ArrayList<BiomeIdData> biomeIdDatas = parseBiomeIdData(biomeIdDataFileValues);
					return biomeIdDatas.size() == 0 ? null : biomeIdDatas;
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}
		}
		
		throw new RuntimeException(
			"OTG encountered a critical error loading " + biomeIdDataFile.getAbsolutePath() + " and could not load a backup, exiting. "
			+ "OTG automatically backs up files before writing and will try to use the backup when loading. "					
			+ "If your world's " + WorldStandardValues.BiomeIdDataFileName + " and its backup have been corrupted, "
			+ "you can replace it with a backup or create a new world with the same dimensions and copy its " 
			+ WorldStandardValues.BiomeIdDataFileName + " (search/replace the WorldName node if necessary).");			
	}
	
	private static ArrayList<BiomeIdData> parseBiomeIdData(String[] biomeIdDataFileValues)
	{
		ArrayList<BiomeIdData> biomeIdDatas = new ArrayList<BiomeIdData>();
		if(biomeIdDataFileValues.length > 0)
		{
			for(int i = 0; i < biomeIdDataFileValues.length; i += 3)
			{
				BiomeIdData biomeIdData = new BiomeIdData();
				biomeIdData.biomeName = biomeIdDataFileValues[i];
				biomeIdData.savedBiomeId = Integer.parseInt(biomeIdDataFileValues[i + 1]);
				biomeIdData.otgBiomeId = Integer.parseInt(biomeIdDataFileValues[i + 2]);				
				biomeIdDatas.add(biomeIdData);
			}
		}
		return biomeIdDatas;
	}
}