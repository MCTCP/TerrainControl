package com.khorn.terraincontrol.forge.dimensions;

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

import org.apache.commons.io.FileUtils;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.ServerConfigProvider;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.forge.ForgeEngine;
import com.khorn.terraincontrol.forge.ForgeWorld;
import com.khorn.terraincontrol.forge.TXWorldServerMulti;
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

public class TXDimensionManager
{
	public static boolean isDimensionNameRegistered(String dimensionName)
	{
		for(int i = 2; i < Long.SIZE << 4; i++)
		{
			if(DimensionManager.isDimensionRegistered(i))
			{
				DimensionType dimensionType = DimensionManager.getProviderType(i);
				
				if(dimensionType.getSuffix() != null && dimensionType.getSuffix().equals("OTG") && dimensionType.getName().equals(dimensionName))
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
        
		DimensionManager.registerDimension(newDimId, DimensionType.register(dimensionName, "OTG", newDimId, WorldProviderTX.class, keepLoaded));	        			        		
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
	
	public static void DeleteDimension(int dimToRemove, ForgeWorld world, MinecraftServer server, boolean saveDimensionData)
	{
		if(DimensionManager.getWorld(dimToRemove) != null) // Can be null on the client if the world was unloaded(?)
		{
			DimensionManager.setWorld(dimToRemove, null, server);
		}
		if(DimensionManager.isDimensionRegistered(dimToRemove))
		{
			DimensionManager.unregisterDimension(dimToRemove);
		}
		
		world.unRegisterBiomes();
			
		((ForgeEngine)TerrainControl.getEngine()).getWorldLoader().RemoveUnloadedWorld(world.getName());

		TXDimensionManager.UnloadCustomDimensionData(dimToRemove);

		world.DeleteWorldSessionData();
		
		BitSet dimensionMap = null;
		try {
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
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		dimensionMap.clear(dimToRemove);
		
		if(saveDimensionData)
		{
			// This biome was unregistered via a console command, delete its world data
			File dimensionSaveDir = new File(world.getWorld().getSaveHandler().getWorldDirectory() + "/DIM" + dimToRemove);
			if(dimensionSaveDir.exists() && dimensionSaveDir.isDirectory())
			{
				TerrainControl.log(LogMarker.INFO, "Deleting world save data for dimension " + dimToRemove);
				try {
					FileUtils.deleteDirectory(dimensionSaveDir);								
				} catch (IOException e) {
					TerrainControl.log(LogMarker.ERROR, "Could not delete directory: " + e.toString());
					e.printStackTrace();
				}
			}
		
			SaveDimensionData();
		}
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

        // TODO: Allow for different settings for each dimension.
        // TODO: Changing seed here does work, but seed is forgotten after restart and overworld seed is used, fix this! <-- TODO: Is this still true?       
        
		long seedIn = (long) Math.floor((Math.random() * Long.MAX_VALUE));
		GameType gameType = mcServer.getGameType();
		boolean enableMapFeatures = overworld.getWorldInfo().isMapFeaturesEnabled(); // Whether the map features (e.g. strongholds) generation is enabled or disabled.
		boolean hardcoreMode = overworld.getWorldInfo().isHardcoreModeEnabled();
		WorldType worldTypeIn = overworld.getWorldType();
		
		WorldSettings settings = new WorldSettings(seedIn, gameType, enableMapFeatures, hardcoreMode, worldTypeIn);
		WorldInfo worldInfo = new WorldInfo(settings, overworld.getWorldInfo().getWorldName());        
        WorldServer world = (WorldServer)(new TXWorldServerMulti(mcServer, savehandler, dim, overworld, mcServer.profiler, worldInfo).init());
                
        ForgeWorld forgeWorld = (ForgeWorld) TerrainControl.getWorld(dimensionName);
		if(forgeWorld == null)
		{
			forgeWorld = (ForgeWorld) TerrainControl.getUnloadedWorld(dimensionName);
		}
        if(forgeWorld != null) // forgeWorld can be null for a dimension with a vanilla world
        {        	
	        ((ServerConfigProvider)forgeWorld.getConfigs()).getWorldConfig().worldSeed = "" + seedIn;
	        ((ServerConfigProvider)forgeWorld.getConfigs()).saveWorldConfig();
	        
        	WorldConfig worldConfig = ((ServerConfigProvider)forgeWorld.getConfigs()).getWorldConfig();
        	
	        world.getGameRules().setOrCreateGameRule("commandBlockOutput", worldConfig.commandBlockOutput); // Whether command blocks should notify admins when they perform commands
    		world.getGameRules().setOrCreateGameRule("disableElytraMovementCheck", worldConfig.disableElytraMovementCheck); // Whether the server should skip checking player speed when the player is wearing elytra. Often helps with jittering due to lag in multiplayer, but may also be used to travel unfairly long distances in survival mode (cheating).
    		world.getGameRules().setOrCreateGameRule("doDaylightCycle", worldConfig.doDaylightCycle); // Whether the day-night cycle and moon phases progress
			world.getGameRules().setOrCreateGameRule("doEntityDrops", worldConfig.doEntityDrops); // Whether entities that are not mobs should have drops
			world.getGameRules().setOrCreateGameRule("doFireTick", worldConfig.doFireTick); // Whether fire should spread and naturally extinguish
			//world.getGameRules().setOrCreateGameRule("doLimitedCrafting", worldConfig.doLimitedCrafting); // Whether players should only be able to craft recipes that they've unlocked first // TODO: Implement for 1.12
			world.getGameRules().setOrCreateGameRule("doMobLoot", worldConfig.doMobLoot); // Whether mobs should drop items
			world.getGameRules().setOrCreateGameRule("doMobSpawning", worldConfig.doMobSpawning); // Whether mobs should naturally spawn. Does not affect monster spawners.
			world.getGameRules().setOrCreateGameRule("doTileDrops", worldConfig.doTileDrops); // Whether blocks should have drops
			world.getGameRules().setOrCreateGameRule("doWeatherCycle", worldConfig.doWeatherCycle); // Whether the weather will change
	        //boolean gameLoopFunction = true; // The function to run every game tick // TODO: Implement for 1.12
			world.getGameRules().setOrCreateGameRule("keepInventory", worldConfig.keepInventory); // Whether the player should keep items in their inventory after death
			world.getGameRules().setOrCreateGameRule("logAdminCommands", worldConfig.logAdminCommands); // Whether to log admin commands to server log
	        //int maxCommandChainLength = 65536; // Determines the number at which the chain command block acts as a "chain". // TODO: Implement for 1.12
			world.getGameRules().setOrCreateGameRule("maxEntityCramming", worldConfig.maxEntityCramming); // The maximum number of other pushable entities a mob or player can push, before taking 3 doublehearts suffocation damage per half-second. Setting to 0 disables the rule. Damage affects survival-mode or adventure-mode players, and all mobs but bats. Pushable entities include non-spectator-mode players, any mob except bats, as well as boats and minecarts.
	        world.getGameRules().setOrCreateGameRule("mobGriefing", worldConfig.mobGriefing); // Whether creepers, zombies, endermen, ghasts, withers, ender dragons, rabbits, sheep, and villagers should be able to change blocks and whether villagers, zombies, skeletons, and zombie pigmen can pick up items
    		world.getGameRules().setOrCreateGameRule("naturalRegeneration", worldConfig.naturalRegeneration); // Whether the player can regenerate health naturally if their hunger is full enough (doesn't affect external healing, such as golden apples, the Regeneration effect, etc.)
    		world.getGameRules().setOrCreateGameRule("randomTickSpeed", worldConfig.randomTickSpeed); // How often a random block tick occurs (such as plant growth, leaf decay, etc.) per chunk section per game tick. 0 will disable random ticks, higher numbers will increase random ticks 
	        world.getGameRules().setOrCreateGameRule("reducedDebugInfo", worldConfig.reducedDebugInfo); // Whether the debug screen shows all or reduced information; and whether the effects of F3+B (entity hitboxes) and F3+G (chunk boundaries) are shown.
    		world.getGameRules().setOrCreateGameRule("sendCommandFeedback", worldConfig.sendCommandFeedback); // Whether the feedback from commands executed by a player should show up in chat. Also affects the default behavior of whether command blocks store their output text
			world.getGameRules().setOrCreateGameRule("showDeathMessages", worldConfig.showDeathMessages); // Whether death messages are put into chat when a player dies. Also affects whether a message is sent to the pet's owner when the pet dies.
			world.getGameRules().setOrCreateGameRule("spawnRadius", worldConfig.spawnRadius); // The number of blocks outward from the world spawn coordinates that a player will spawn in when first joining a server or when dying without a spawnpoint.
	        world.getGameRules().setOrCreateGameRule("spectatorsGenerateChunks", worldConfig.spectatorsGenerateChunks); // Whether players in spectator mode can generate chunks        	        	        

	        // Set difficulty, creative/survival/hardcore
	        /*
            if (worldserver.getWorldInfo().isHardcoreModeEnabled())
            {
                worldserver.getWorldInfo().setDifficulty(EnumDifficulty.HARD);
                worldserver.setAllowedSpawnTypes(true, true);
            }
            else if (this.isSinglePlayer())
            {
                worldserver.getWorldInfo().setDifficulty(difficulty);
                worldserver.setAllowedSpawnTypes(worldserver.getDifficulty() != EnumDifficulty.PEACEFUL, true);
            } else {
                worldserver.getWorldInfo().setDifficulty(difficulty);
                worldserver.setAllowedSpawnTypes(this.allowSpawnMonsters(), this.canSpawnAnimals);
            }
	        */	        
        }
        
        world.addEventListener(new ServerWorldEventHandler(mcServer, world));
        MinecraftForge.EVENT_BUS.post(new WorldEvent.Load(world));
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
				
				if(dimType != null && dimType.getSuffix() != null && dimType.getSuffix().equals("OTG"))
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
			
			if(dimType != null && dimType.getSuffix() != null && dimType.getSuffix().equals("OTG"))
			{
				DimensionManager.unregisterDimension(dimId);
				dimensionMap.clear(dimId);
			}
		}
	}
	
	public static OTGDimensionInfo GetOrderedDimensionData()
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
		
		return new OTGDimensionInfo(highestOrder, orderedDimensions);
	}
	
	public static void LoadCustomDimensionData()
	{
		OTGDimensionInfo otgDimData = GetOrderedDimensionData();
		
		// Recreate dimensions in the correct order						
		for(int i = 0; i <= otgDimData.highestOrder; i++)
		{
			if(otgDimData.orderedDimensions.containsKey(i))
			{
				DimensionData dimData = otgDimData.orderedDimensions.get(i);
				
				if(!DimensionManager.isDimensionRegistered(dimData.dimensionId))
				{	
					DimensionManager.registerDimension(dimData.dimensionId, DimensionType.register(dimData.dimensionName, "OTG", dimData.dimensionId, WorldProviderTX.class, dimData.keepLoaded));
					if(dimData.dimensionName.equals("DIM-Cartographer"))
					{
						Cartographer.CartographerDimension = dimData.dimensionId;
					}
					DimensionManager.initDimension(dimData.dimensionId);
				}
			}
		}
	}	
	
	private static Hashtable<Integer, Object> oldDims;
	public static void RemoveTCDims()
	{	
    	Hashtable dimensions = null;  	
   	
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
						if(value instanceof DimensionType || !(value instanceof WorldServer)) // Forge 1.11.2 - 13.20.0.2228 uses DimensionType, Forge 1.11.2 - 13.20.0.2315 uses Dimension
						{							
							dimensions = fieldAsHashTable;
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
		    	
		oldDims = new Hashtable<Integer, Object>();
		for(int i = 2; i < Long.SIZE << 4; i++)
		{
			if(DimensionManager.isDimensionRegistered(i))
			{
				DimensionType type = DimensionManager.getProviderType(i);
				if(type.getSuffix() != null && type.getSuffix().equals("OTG"))
				{
					oldDims.put(i, dimensions.get(i));
					dimensions.remove(i);
				}
			}
		}	
	}
	
	public static HashMap<Integer, String> GetAllOTGDimensions()
	{	
		HashMap<Integer, String> otgDims = new HashMap<Integer, String>();
		
		for(int i = 2; i < Long.SIZE << 4; i++)
		{
			if(DimensionManager.isDimensionRegistered(i))
			{
				DimensionType type = DimensionManager.getProviderType(i);
				if(type.getSuffix() != null && type.getSuffix().equals("OTG"))
				{
					otgDims.put(new Integer(type.getId()), type.getName());
				}
			}
		}
		
		return otgDims;
	}
	
	public static void ReAddTCDims()
	{
    	Hashtable dimensions = null;  	
       	
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
						if(value instanceof DimensionType || !(value instanceof WorldServer)) // Forge 1.11.2 - 13.20.0.2228 uses DimensionType, Forge 1.11.2 - 13.20.0.2315 uses Dimension
						{							
							dimensions = fieldAsHashTable;
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
    	
		for(Entry<Integer, Object> oldDim : oldDims.entrySet())
		{
			dimensions.put(oldDim.getKey(), oldDim.getValue());
		}
		oldDims = new Hashtable<Integer, Object>();
	}
}
