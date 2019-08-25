package com.pg85.otg.worldsave;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.pg85.otg.OTG;
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
    
	public static void saveWorldSaveData(File worldSaveDir, WorldSaveData worldSaveData)
	{
		File worldSaveDataFile = new File(worldSaveDir + File.separator + "OpenTerrainGenerator" + File.separator + "WorldSave.txt");
		if(worldSaveDataFile.exists())
		{
			worldSaveDataFile.delete();
		}

		StringBuilder stringbuilder = new StringBuilder();
		stringbuilder.append(worldSaveData.version);

		BufferedWriter writer = null;
        try
        {
        	worldSaveDataFile.getParentFile().mkdirs();
        	writer = new BufferedWriter(new FileWriter(worldSaveDataFile));
            writer.write(stringbuilder.toString());
            OTG.log(LogMarker.DEBUG, "World save data saved");
        }
        catch (IOException e)
        {
        	OTG.log(LogMarker.ERROR, "Could not save world save data.");
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
		
	public static WorldSaveData loadWorldSaveData(File worldSaveDir)
	{
		File worldSaveDataFile = new File(worldSaveDir + File.separator + "OpenTerrainGenerator" + File.separator + "WorldSave.txt");
		String version = null;
		if(worldSaveDataFile.exists())
		{
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
				    }
				    OTG.log(LogMarker.DEBUG, "World save data loaded");
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
		
		return version != null && version.trim().length() > 0 ? new WorldSaveData(version) : null;
	}
}