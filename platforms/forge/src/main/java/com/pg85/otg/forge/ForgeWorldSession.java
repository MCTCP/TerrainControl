package com.pg85.otg.forge;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.common.WorldSession;
import com.pg85.otg.configuration.dimensions.DimensionConfig;
import com.pg85.otg.customobjects.bo3.bo3function.ParticleFunction;
import com.pg85.otg.forge.generator.Pregenerator;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.ChunkCoordinate;

public class ForgeWorldSession extends WorldSession
{
	ArrayList<ParticleFunction> ParticleFunctions = new ArrayList<ParticleFunction>();

	Pregenerator pregenerator;

	int worldBorderRadius;
	ChunkCoordinate worldBorderCenterPoint;

	public ForgeWorldSession(LocalWorld world)
	{
		super(world);
		pregenerator = new Pregenerator(world);
		// Don't load world border data on MP client
		if(((ForgeWorld)world).world != null)
		{
			LoadWorldBorderData();
		}
	}

	public Pregenerator getPregenerator()
	{
		return pregenerator;
	}

	@Override
	public ArrayList<ParticleFunction> getParticleFunctions()
	{
		return ParticleFunctions;
	}

	@Override
	public int getWorldBorderRadius()
	{
		return worldBorderRadius;
	}

	@Override
	public ChunkCoordinate getWorldBorderCenterPoint()
	{
		return worldBorderCenterPoint;
	}

	@Override
	public int getPregenerationRadius()
	{
		return pregenerator.getPregenerationRadius();
	}

	@Override
	public int setPregenerationRadius(int value)
	{
		return pregenerator.setPregenerationRadius(value);
	}

	@Override
	public int getPregeneratedBorderLeft()
	{
		return pregenerator.getPregenerationBorderLeft();
	}

	@Override
	public int getPregeneratedBorderRight()
	{
		return pregenerator.getPregenerationBorderRight();
	}

	@Override
	public int getPregeneratedBorderTop()
	{
		return pregenerator.getPregenerationBorderTop();
	}

	@Override
	public int getPregeneratedBorderBottom()
	{
		return pregenerator.getPregenerationBorderBottom();
	}

	@Override
	public ChunkCoordinate getPreGeneratorCenterPoint()
	{
		return pregenerator.getPregenerationCenterPoint();
	}

	@Override
	public boolean getPreGeneratorIsRunning()
	{
		return pregenerator.getPregeneratorIsRunning();
	}
		
    // Saving / Loading
    // TODO: It's crude but it works, can improve later
    
	void SaveWorldBorderData()
	{			
		int dimensionId = world.getDimensionId();
		File worldBorderFile = new File(world.getWorldSaveDir() + "/OpenTerrainGenerator/" + (dimensionId != 0 ? "DIM-" + dimensionId + "/" : "") + "WorldBorder.txt");		
		if(worldBorderFile.exists())
		{
			worldBorderFile.delete();
		}		
		
		StringBuilder stringbuilder = new StringBuilder();
		stringbuilder.append(worldBorderRadius + "," + worldBorderCenterPoint.getChunkX() + "," + worldBorderCenterPoint.getChunkZ());		
		
		BufferedWriter writer = null;
        try
        {
        	worldBorderFile.getParentFile().mkdirs();
        	writer = new BufferedWriter(new FileWriter(worldBorderFile));
            writer.write(stringbuilder.toString());
            OTG.log(LogMarker.DEBUG, "World border data saved");
        }
        catch (IOException e)
        {
        	OTG.log(LogMarker.ERROR, "Could not save world border data.");
            e.printStackTrace();
        }
        finally
        {   
            try
            {           	
                writer.close();
            }
            catch (Exception e) { }
        }
	}

	void LoadWorldBorderData()
	{	
		int dimensionId = world.getDimensionId();
		File worldBorderFile = new File(world.getWorldSaveDir() + "/OpenTerrainGenerator/" + (dimensionId != 0 ? "DIM-" + dimensionId + "/" : "") + "WorldBorder.txt");				
		String[] worldBorderFileValues = {};
		if(worldBorderFile.exists())
		{
			try
			{
				StringBuilder stringbuilder = new StringBuilder();
				BufferedReader reader = new BufferedReader(new FileReader(worldBorderFile));
				try
				{
					String line = reader.readLine();	
				    while (line != null)
				    {
				    	stringbuilder.append(line);
				        line = reader.readLine();
				    }				    
				    if(stringbuilder.length() > 0)
				    {
				    	worldBorderFileValues = stringbuilder.toString().split(",");
				    }
				    OTG.log(LogMarker.DEBUG, "Pre-generator data loaded");
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

		if(worldBorderFileValues.length > 0)
		{			
			worldBorderRadius = Integer.parseInt(worldBorderFileValues[0]);			
			worldBorderCenterPoint = ChunkCoordinate.fromChunkCoords(Integer.parseInt(worldBorderFileValues[1]), Integer.parseInt(worldBorderFileValues[2]));			
		} else {
			DimensionConfig dimConfig = OTG.getDimensionsConfig().getDimensionConfig(world.getName());
			worldBorderRadius = dimConfig.WorldBorderRadiusInChunks;
			worldBorderCenterPoint = world.getSpawnChunk();
			SaveWorldBorderData();
		}
	}
}
