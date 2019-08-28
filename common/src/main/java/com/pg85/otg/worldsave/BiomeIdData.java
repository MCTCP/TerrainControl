package com.pg85.otg.worldsave;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.common.LocalWorld;
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
		
		File biomeIdDataFile = new File(worldSaveDir + File.separator + "OpenTerrainGenerator" + File.separator + "BiomeIds.txt");
		if(biomeIdDataFile.exists())
		{
			biomeIdDataFile.delete();
		}

		StringBuilder stringbuilder = new StringBuilder();

        if(loadedBiomeIdData != null)
        {
    		for(BiomeIdData biomeIdData : loadedBiomeIdData)
    		{
    			if(!biomeIdData.biomeName.startsWith(world.getName() + "_"))
    			{
    				stringbuilder.append((stringbuilder.length() == 0 ? "" : ",") + biomeIdData.biomeName + "," + biomeIdData.savedBiomeId + "," + biomeIdData.otgBiomeId);
    			}
			}
        }

		for(LocalBiome biome : serverConfigProvider.getBiomeArrayByOTGId())
		{
			if(biome != null)
			{
				stringbuilder.append((stringbuilder.length() == 0 ? "" : ",") + world.getName() + "_" + biome.getName() + "," + biome.getIds().getSavedId() + "," + biome.getIds().getOTGBiomeId());
			}			 
		}

		BufferedWriter writer = null;
        try
        {
        	biomeIdDataFile.getParentFile().mkdirs();
        	writer = new BufferedWriter(new FileWriter(biomeIdDataFile));
            writer.write(stringbuilder.toString());
            OTG.log(LogMarker.DEBUG, "Custom dimension data saved");
        }
        catch (IOException e)
        {
        	OTG.log(LogMarker.ERROR, "Could not save custom dimension data.");
            e.printStackTrace();
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
		File biomeIdDataFile = new File(worldSaveDir + File.separator + "OpenTerrainGenerator" + File.separator + "BiomeIds.txt");
		String[] biomeIdDataFileValues = {};
		if(biomeIdDataFile.exists())
		{
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
				    OTG.log(LogMarker.DEBUG, "Biome Id data loaded");
				} finally {
					reader.close();
				}

			}
			catch (FileNotFoundException e1)
			{
				e1.printStackTrace();
			}
			catch (IOException e1)
			{
				e1.printStackTrace();
			}
		}
		
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

		return biomeIdDatas.size() == 0 ? null : biomeIdDatas;
	}
}