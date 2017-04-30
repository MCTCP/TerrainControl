package com.khorn.terraincontrol.forge;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map.Entry;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.ServerConfigProvider;
import com.khorn.terraincontrol.forge.generator.Cartographer;
import com.khorn.terraincontrol.logging.LogMarker;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.DimensionType;
import net.minecraft.world.GameType;
import net.minecraft.world.ServerWorldEventHandler;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;

public class TCDimensionManager
{
	public static boolean isDimensionNameRegistered(String dimensionName)
	{
		for(int i = 2; i < Long.SIZE << 4; i++)
		{
			if(DimensionManager.isDimensionRegistered(i))
			{
				DimensionType dimensionType = DimensionManager.getProviderType(i);
				
				if(dimensionType.getSuffix().equals("OTG") && dimensionType.getName().equals(dimensionName))
				{
    				return true;
				}
			}
		}
		return false;
	}
	
	static HashMap<Integer,Integer> dimensionsOrder;
	
	public static int createDimension(String dimensionName, boolean keepLoaded, boolean initDimension, boolean saveDimensionData)
	{
		int newDimId = DimensionManager.getNextFreeDimId();
        
		DimensionManager.registerDimension(newDimId, DimensionType.register(dimensionName, "OTG", newDimId, WorldProviderTC.class, keepLoaded));	        			        		
		if(initDimension)
		{
			initDimension(newDimId, dimensionName);
		}		
		
		int maxOrder = -1;
		for(Integer dimOrder : dimensionsOrder.values())
		{
			if(dimOrder > maxOrder)
			{
				maxOrder = dimOrder;
			}
		}		
		dimensionsOrder.put(newDimId, maxOrder + 1);
		
		if(saveDimensionData)
		{
			SaveDimensionData();
		}
		
		return newDimId;
	}
	
    private static void initDimension(int dim, String dimensionName)
    {
        WorldServer overworld = DimensionManager.getWorld(0);
        if (overworld == null)
        {
            throw new RuntimeException("Cannot Hotload Dim: Overworld is not Loaded!");
        }
        
        try
        {
            DimensionManager.getProviderType(dim);
        }
        catch (Exception e)
        {
            System.err.println("Cannot Hotload Dim: " + e.getMessage());
            return; // If a provider hasn't been registered then we can't hotload the dim
        }
        MinecraftServer mcServer = overworld.getMinecraftServer();
        ISaveHandler savehandler = overworld.getSaveHandler();
        //WorldSettings worldSettings = new WorldSettings(overworld.getWorldInfo());

        // TODO: Allow for different settings for each dimension.
        // TODO: Changing seed here does work, but seed is forgotten after restart and overworld seed is used, fix this!       
        
		long seedIn = (long) Math.floor((Math.random() * Long.MAX_VALUE));
		GameType gameType = mcServer.getGameType();
		boolean enableMapFeatures = overworld.getWorldInfo().isMapFeaturesEnabled(); // Whether the map features (e.g. strongholds) generation is enabled or disabled.
		boolean hardcoreMode = overworld.getWorldInfo().isHardcoreModeEnabled();
		WorldType worldTypeIn = overworld.getWorldType();
		
		WorldSettings settings = new WorldSettings(seedIn, gameType, enableMapFeatures, hardcoreMode, worldTypeIn);
		WorldInfo worldInfo = new WorldInfo(settings, overworld.getWorldInfo().getWorldName());        
		//WorldInfo worldInfo = new WorldInfo(settings, dimensionName);
        WorldServer world = (WorldServer)(new TCWorldServerMulti(mcServer, savehandler, dim, overworld, mcServer.theProfiler, worldInfo).init());
                
        ForgeWorld forgeWorld = (ForgeWorld) TerrainControl.getWorld(dimensionName);
		if(forgeWorld == null)
		{
			forgeWorld = (ForgeWorld) TerrainControl.getUnloadedWorld(dimensionName);
		}
        if(forgeWorld != null) // forgeWorld can be null for a dimension with a vanilla world
        {        	
	        ((ServerConfigProvider)forgeWorld.getConfigs()).getWorldConfig().worldSeed = "" + seedIn;
	        ((ServerConfigProvider)forgeWorld.getConfigs()).saveWorldConfig();
        }
        
        world.addEventListener(new ServerWorldEventHandler(mcServer, world));
        MinecraftForge.EVENT_BUS.post(new WorldEvent.Load(world));
        
        //if (!mcServer.isSinglePlayer())
        {
            //world.getWorldInfo().setGameType(gameType);
        }

        
        /*
			world.getGameRules().setOrCreateGameRule("commandBlockOutput", "true"); 
			world.getGameRules().setOrCreateGameRule("disableElytraMovementCheck, "true"); 
			world.getGameRules().setOrCreateGameRule("doDaylightCycle, "true"); 
			world.getGameRules().setOrCreateGameRule("doEntityDrops, "true"); 
			world.getGameRules().setOrCreateGameRule("doFireTick, "true"); 
			world.getGameRules().setOrCreateGameRule("doMobLoot, "true"); 
			world.getGameRules().setOrCreateGameRule("doMobSpawning, "true");
			world.getGameRules().setOrCreateGameRule("doTileDrops, "true"); 
			world.getGameRules().setOrCreateGameRule("keepInventory, "true"); 
			world.getGameRules().setOrCreateGameRule("logAdminCommands, "true"); 
			world.getGameRules().setOrCreateGameRule("mobGriefing, "true"); 
			world.getGameRules().setOrCreateGameRule("naturalRegeneration, "true"); 
			world.getGameRules().setOrCreateGameRule("randomTickSpeed, "true"); 
			world.getGameRules().setOrCreateGameRule("reducedDebugInfo, "true"); 
			world.getGameRules().setOrCreateGameRule("sendCommandFeedback, "true"); 
			world.getGameRules().setOrCreateGameRule("showDeathMessages, "true"); 
			world.getGameRules().setOrCreateGameRule("spawnRadius, "true"); 
			world.getGameRules().setOrCreateGameRule("spectatorsGenerateChunks, "true");
         */
        
        //world.getGameRules().setOrCreateGameRule(key, "true");
        
        //mcServer.setDifficultyForAllWorlds(mcServer.getDifficulty());
    }
    
    // Saving / Loading
    // TODO: It's crude but it works, can improve later
    
	public static void SaveDimensionData()
	{	
		World world = DimensionManager.getWorld(0);
		File dimensionDataFile = new File(world.getSaveHandler().getWorldDirectory() + "/OpenTerrainGenerator/Dimensions.txt");		
		if(dimensionDataFile.exists())
		{
			dimensionDataFile.delete();
		}		
		
		StringBuilder stringbuilder = new StringBuilder();

		for(int i = 2; i < Long.SIZE << 4; i++)
		{
			if(DimensionManager.isDimensionRegistered(i))
			{
				DimensionType dimType = DimensionManager.getProviderType(i);
				if(dimType != null)
				{
					ForgeWorld forgeWorld = (ForgeWorld) TerrainControl.getWorld(dimType.getName());
					if(forgeWorld == null)
					{
						forgeWorld = (ForgeWorld) TerrainControl.getUnloadedWorld(dimType.getName());
					}
					if(forgeWorld == null)
					{
						return; // If another mod added a dimension
					}
					
					if(forgeWorld != null)
					{
						stringbuilder.append((stringbuilder.length() == 0 ? "" : ",") + i + "," + dimType.getName() + "," + dimType.shouldLoadSpawn() + "," + forgeWorld.getSeed() + "," + dimensionsOrder.get(i));
					}
				}
			}
		}		
		
		BufferedWriter writer = null;
        try
        {
        	dimensionDataFile.getParentFile().mkdirs();
        	writer = new BufferedWriter(new FileWriter(dimensionDataFile));
            writer.write(stringbuilder.toString());
            TerrainControl.log(LogMarker.TRACE, "Custom dimension data saved");
        }
        catch (IOException e)
        {
        	TerrainControl.log(LogMarker.ERROR, "Could not save custom dimension data.");
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
	
	public static void UnloadAllCustomDimensionData()
	{
		dimensionsOrder = new HashMap<Integer,Integer>();
		
		BitSet dimensionMap = null;
		try
		{
			Field[] fields = DimensionManager.class.getDeclaredFields();
			for(Field field : fields)
			{
				Class<?> fieldClass = field.getType();
				if(fieldClass.equals(BitSet.class))
				{
					field.setAccessible(true);
					dimensionMap = (BitSet) field.get(new DimensionManager());
			        break;
				}
			}
		}
		catch (SecurityException e)
		{
			e.printStackTrace();
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}

		for(int i = 2; i < Long.SIZE << 4; i++)
		{
			if(DimensionManager.isDimensionRegistered(i))
			{				
				DimensionType dimType = DimensionManager.getProviderType(i);
				
				if(dimType != null && dimType.getSuffix().equals("OTG"))
				{
					DimensionManager.unregisterDimension(i);
					dimensionMap.clear(i);
				}
			}
		}
	}
	
	public static void UnloadCustomDimensionData(int dimId)
	{
		dimensionsOrder.remove(dimId);
		
		BitSet dimensionMap = null;
		try
		{
			Field[] fields = DimensionManager.class.getDeclaredFields();
			for(Field field : fields)
			{
				Class<?> fieldClass = field.getType();
				if(fieldClass.equals(BitSet.class))
				{
					field.setAccessible(true);
					dimensionMap = (BitSet) field.get(new DimensionManager());
			        break;
				}
			}
		}
		catch (SecurityException e)
		{
			e.printStackTrace();
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}

		if(DimensionManager.isDimensionRegistered(dimId))
		{				
			DimensionType dimType = DimensionManager.getProviderType(dimId);
			
			//if(dimType != null && !dimType.getSuffix().equals("OTG") && (dimId > 1))
			//{
				//throw new RuntimeException("Whatever it is you're trying to do, we didn't write any code for it (sorry). Please contact Team OTG about this crash.");
			//}
			
			if(dimType != null && dimType.getSuffix().equals("OTG"))
			{
				DimensionManager.unregisterDimension(dimId);
				dimensionMap.clear(dimId);
			}
		}
	}
	
	public static void LoadCustomDimensionData()
	{
		World world = DimensionManager.getWorld(0);
		File dimensionDataFile = new File(world.getSaveHandler().getWorldDirectory() + "/OpenTerrainGenerator/Dimensions.txt");				
		String[] dimensionDataFileValues = {};
		if(dimensionDataFile.exists())
		{
			try {
				StringBuilder stringbuilder = new StringBuilder();
				BufferedReader reader = new BufferedReader(new FileReader(dimensionDataFile));
				try {
					String line = reader.readLine();
	
				    while (line != null)
				    {
				    	stringbuilder.append(line);
				        line = reader.readLine();
				    }				    
				    if(stringbuilder.length() > 0)
				    {
				    	dimensionDataFileValues = stringbuilder.toString().split(",");
				    }
				    TerrainControl.log(LogMarker.TRACE, "Custom dimension data loaded");
				} finally {
					reader.close();
				}
				
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
			catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		
		ArrayList<DimensionData> dimensionData = new ArrayList<DimensionData>();
		if(dimensionDataFileValues.length > 0)
		{
			for(int i = 0; i < dimensionDataFileValues.length; i += 5)
			{
				DimensionData dimData = new DimensionData();
				dimData.dimensionId = Integer.parseInt(dimensionDataFileValues[i]);
				dimData.dimensionName = dimensionDataFileValues[i + 1];
				dimData.keepLoaded = Boolean.parseBoolean(dimensionDataFileValues[i + 2]);
				dimData.seed = Long.parseLong(dimensionDataFileValues[i + 3]);
				dimData.dimensionOrder = Integer.parseInt(dimensionDataFileValues[i + 4]);
				dimensionData.add(dimData);
			}
		}
		
		// Store the order in which dimensions were added
		dimensionsOrder = new HashMap<Integer, Integer>();
		HashMap<Integer, DimensionData> orderedDimensions = new HashMap<Integer, DimensionData>();
		int highestOrder = 0;
		for(DimensionData dimData : dimensionData)
		{
			dimensionsOrder.put(dimData.dimensionId, dimData.dimensionOrder);
			orderedDimensions.put(dimData.dimensionOrder, dimData);
			if(dimData.dimensionOrder > highestOrder)
			{
				highestOrder = dimData.dimensionOrder;
			}
		}
		// Recreate dimensions in the correct order		
				
		for(int i = 0; i <= highestOrder; i++)
		{
			if(orderedDimensions.containsKey(i))
			{
				DimensionData dimData = orderedDimensions.get(i);
				
				if(!DimensionManager.isDimensionRegistered(dimData.dimensionId))
				{	
					DimensionManager.registerDimension(dimData.dimensionId, DimensionType.register(dimData.dimensionName, "OTG", dimData.dimensionId, WorldProviderTC.class, dimData.keepLoaded));
					if(dimData.dimensionName.equals("DIM-Cartographer"))
					{
						Cartographer.CartographerDimension = dimData.dimensionId;
					}
					DimensionManager.initDimension(dimData.dimensionId);
				}
			}
		}
	}

	private static Hashtable<Integer, DimensionType> oldDims;
	public static void RemoveTCDims()
	{
    	Hashtable<Integer, DimensionType> dimensions = null;
		try
		{
			Field[] fields = DimensionManager.class.getDeclaredFields();
			for(Field field : fields)
			{
				Class<?> fieldClass = field.getType();
				if(fieldClass.equals(Hashtable.class))
				{
					field.setAccessible(true);
					Hashtable fieldAsHashTable = (Hashtable) field.get(new DimensionManager());
					if(fieldAsHashTable.values().size() > 0)
					{
						Object value = fieldAsHashTable.values().toArray()[0];																
						if(value instanceof DimensionType)
						{							
							dimensions = (Hashtable<Integer, DimensionType>) field.get(new DimensionManager());
					        break;
						}
					}
				}
			}
		}
		catch (SecurityException e)
		{
			e.printStackTrace();
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
    	
		oldDims = new Hashtable<Integer, DimensionType>();
		for(int i = 2; i < Long.SIZE << 4; i++)
		{
			if(dimensions.containsKey(i))
			{
				oldDims.put(i, dimensions.get(i));
				dimensions.remove(i);
			}
		} 
	}
	
	public static void ReAddTCDims()
	{
    	Hashtable<Integer, DimensionType> dimensions = null;
		try
		{
			Field[] fields = DimensionManager.class.getDeclaredFields();
			for(Field field : fields)
			{
				Class<?> fieldClass = field.getType();
				if(fieldClass.equals(Hashtable.class))
				{
					field.setAccessible(true);
					Hashtable fieldAsHashTable = (Hashtable) field.get(new DimensionManager());
					if(fieldAsHashTable.values().size() > 0)
					{
						Object value = fieldAsHashTable.values().toArray()[0];																
						if(value instanceof DimensionType)
						{							
							dimensions = (Hashtable<Integer, DimensionType>) field.get(new DimensionManager());
					        break;
						}
					}
				}
			}
		}
		catch (SecurityException e)
		{
			e.printStackTrace();
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
    	
		for(Entry<Integer, DimensionType> oldDim : oldDims.entrySet())
		{
			dimensions.put(oldDim.getKey(), oldDim.getValue());
		}		
		oldDims = new Hashtable<Integer, DimensionType>();
	}
}
