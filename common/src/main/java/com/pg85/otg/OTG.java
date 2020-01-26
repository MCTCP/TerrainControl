package com.pg85.otg;

import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.PluginConfig;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.configuration.biome.settings.BiomeResourcesManager;
import com.pg85.otg.configuration.customobjects.CustomObjectResourcesManager;
import com.pg85.otg.configuration.dimensions.DimensionsConfig;
import com.pg85.otg.configuration.io.FileSettingsReader;
import com.pg85.otg.configuration.io.SettingsMap;
import com.pg85.otg.configuration.standard.WorldStandardValues;
import com.pg85.otg.configuration.world.WorldConfig;
import com.pg85.otg.customobjects.CustomObject;
import com.pg85.otg.customobjects.CustomObjectManager;
import com.pg85.otg.events.EventHandler;
import com.pg85.otg.events.EventPriority;
import com.pg85.otg.generator.ChunkBuffer;
import com.pg85.otg.generator.biome.BiomeModeManager;
import com.pg85.otg.generator.resource.Resource;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.ChunkCoordinate;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OTG
{		
	// Used to determine if a new world is being created or if a world save already exists
	// TODO: Make this prettier.
	public static boolean IsNewWorldBeingCreated = false;
	
    /**
     * The engine that powers Open Terrain Generator.
     */
    private static OTGEngine Engine;
	
    private OTG()
    {
        // Forbidden to instantiate.
    }
	
    // Engine
    	
    /**
     * Returns the engine, containing the API methods.
     * <p>
     * @return The engine
     */
    public static OTGEngine getEngine()
    {
        return Engine;
    }
    
    /**
     * Sets the engine and calls its {@link OTGEngine#onStart()
     * onStart()} method.
     * <p>
     * @param engine The engine.
     */
    public static void setEngine(OTGEngine engine)
    {
        if (OTG.Engine != null)
        {
            throw new IllegalStateException("Engine is already set.");
        }

        OTG.Engine = engine;
        engine.onStart();
    }

    /**
     * Nulls out static references to free up memory. Should be called on
     * shutdown. Engine can be restarted after this.
     */
    public static void stopEngine()
    {
        Engine.onShutdown();
        Engine = null;
    }
    
    // Managers
    
    /**
     * Returns the biome managers. Register your own biome manager here.
     * <p>
     * @return The biome managers.
     */
    public static BiomeModeManager getBiomeModeManager()
    {
        return Engine.getBiomeModeManager();
    }

    public static BiomeResourcesManager getBiomeResourcesManager()
    {
        return Engine.getBiomeResourceManager();
    }

    public static CustomObjectResourcesManager getCustomObjectResourcesManager()
    {
        return Engine.getCustomObjectResourcesManager();
    }

    public static CustomObjectManager getCustomObjectManager()
    {
        return Engine.getCustomObjectManager();
    }
    
    // Plugin
    
    public static PluginConfig getPluginConfig()
    {
        return Engine.getPluginConfig();
    }    
    
    // Dimensions config
    
	/**
	 * A config for each dimension of the currently active world
	 */
    public static DimensionsConfig getDimensionsConfig()
    {    	
    	return Engine.getDimensionsConfig();
    }

    public static void setDimensionsConfig(DimensionsConfig dimensionsConfig)
    {
    	Engine.setDimensionsConfig(dimensionsConfig);
    }
    
    // Biomes
    
    // For bukkit plugin developers, do not remove. See: https://github.com/MCTCP/TerrainControl/wiki/Developer-page
    /*
	* Convenience method to quickly get the biome name at the given
	* coordinates. Will return null if the world isn't loaded by OTG
	* <p>
	* @param worldName The world name.
	* @param x         The block x in the world.
	* @param z         The block z in the world.
	* @return The biome name, or null if the world isn't managed by Terrain
    *         Control.
    */
	public static String getBiomeName(String worldName, int x, int z)
	{
	   LocalWorld world = getWorld(worldName);
       if (world == null)
       {
    	   // World isn't loaded by OTG
    	   return null;
       }       
       
       return world.getSavedBiomeName(x, z);
   	}

   	public static LocalBiome getBiomeByOTGId(int id)
   	{
	   ArrayList<LocalWorld> worlds = getAllWorlds();
	   if(worlds != null)
	   {
		   for(LocalWorld world : worlds)
		   {
			   LocalBiome biome = world.getBiomeByOTGIdOrNull(id);
			   if(biome != null)
			   {
				   return biome;
			   }
		   }
	   }
	   return null;
   	}
   	
    public static LocalBiome getBiome(String name, String worldName)
    {    	
        ArrayList<LocalWorld> worlds = getAllWorlds();
        if(worlds != null)
        {
	        for(LocalWorld world : worlds)
	        {
	        	if(world.getName().toLowerCase().equals(worldName.toLowerCase()))
	        	{
		        	LocalBiome biome = world.getBiomeByNameOrNull(name);
		        	if(biome != null)
		        	{
		        		return biome;
		        	}
	        	}
	        }
        }
        return null;
    }

    // Worlds
    
    public static LocalWorld getWorld(String name)
    {
        return Engine.getWorld(name);
    }

    public static LocalWorld getUnloadedWorld(String name)
    {
    	return Engine.getUnloadedWorld(name);
    }
    
    public static ArrayList<LocalWorld> getAllWorlds()
    {
        return Engine.getAllWorlds();
    }
    
    // Events
    
    /**
     * @see OTGEngine#registerEventHandler(EventHandler)
     */
    public static void registerEventHandler(EventHandler handler)
    {
        Engine.registerEventHandler(handler);
    }

    /**
     * @see OTGEngine#registerEventHandler(EventHandler,
     * EventPriority)
     */
    public static void registerEventHandler(EventHandler handler, EventPriority priority)
    {
        Engine.registerEventHandler(handler, priority);
    }
    
    public static boolean fireReplaceBiomeBlocksEvent(int x, int z, ChunkBuffer chunkBuffer, LocalWorld localWorld)
    {
    	return Engine.fireReplaceBiomeBlocksEvent(x, z, chunkBuffer, localWorld);
    }
    
    /**
     * @see OTGEngine#fireCanCustomObjectSpawnEvent(CustomObject,
     * LocalWorld, int, int, int)
     */
    public static boolean fireCanCustomObjectSpawnEvent(CustomObject object, LocalWorld world, int x, int y, int z)
    {
        return Engine.fireCanCustomObjectSpawnEvent(object, world, x, y, z);
    }

    /**
     * @see OTGEngine#firePopulationEndEvent(LocalWorld, Random,
     * boolean, ChunkCoordinate)
     */
    public static void firePopulationEndEvent(LocalWorld world, Random random, boolean villageInChunk, ChunkCoordinate chunkCoord)
    {
        Engine.firePopulationEndEvent(world, random, villageInChunk, chunkCoord);
    }

    /**
     * @see OTGEngine#firePopulationStartEvent(LocalWorld, Random,
     * boolean, ChunkCoordinate)
     */
    public static void firePopulationStartEvent(LocalWorld world, Random random, boolean villageInChunk, ChunkCoordinate chunkCoord)
    {
        Engine.firePopulationStartEvent(world, random, villageInChunk, chunkCoord);
    }

    /**
     * @see OTGEngine#fireResourceProcessEvent(Resource,
     * LocalWorld, Random, boolean, int, int)
     */
    public static boolean fireResourceProcessEvent(Resource resource, LocalWorld world, Random random, boolean villageInChunk, int chunkX, int chunkZ)
    {
        return Engine.fireResourceProcessEvent(resource, world, random, villageInChunk, chunkX, chunkZ);
    }    
    
    // Logging
    
    /**
     * Logs the messages with the given importance. Message will be
     * prefixed with [OpenTerrainGenerator], so don't do that yourself.
     * <p>
     * @param messages The messages to log.
     * @param level    The severity of the message
     */
    public static void log(LogMarker level, List<String> messages)
    {
        Engine.getLogger().log(level, messages);
    }

    /**
     * Logs a format string message with the given importance. Message will
     * be prefixed with [OpenTerrainGenerator], so don't do that yourself.
     * <p>
     * @param message The messages to log formatted similar to Logger.log()
     *                with the same args.
     * @param level   The severity of the message
     * @param params  The parameters belonging to {0...} in the message
     *                string
     */
    public static void log(LogMarker level, String message, Object... params)
    {
        Engine.getLogger().log(level, message, params);
    }

    /**
     * Prints the stackTrace of the provided Throwable object
     * <p>
     * @param level The log level to log this stack trace at
     * @param e     The Throwable object to obtain stack trace information from
     */
    public static void printStackTrace(LogMarker level, Throwable e)
    {
        printStackTrace(level, e, Integer.MAX_VALUE);
    }

    /**
     * Prints the stackTrace of the provided Throwable object to a certain
     * depth
     * <p>
     * @param level    The log level to log this stack trace at
     * @param e        The Throwable object to obtain stack trace information
     *                 from
     * @param maxDepth The max number of trace elements to print
     */
    public static void printStackTrace(LogMarker level, Throwable e, int maxDepth)
    {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        Engine.getLogger().log(level, stringWriter.toString());
    }
    
    public static String correctOldBiomeConfigFolder(File settingsDir)
    {
        // Rename the old folder
        String biomeFolderName = WorldStandardValues.WORLD_BIOMES_DIRECTORY_NAME;
        File oldBiomeConfigs = new File(settingsDir, "BiomeConfigs");
        if (oldBiomeConfigs.exists())
        {
            if (!oldBiomeConfigs.renameTo(new File(settingsDir, biomeFolderName)))
            {
                OTG.log(LogMarker.WARN, "========================");
                OTG.log(LogMarker.WARN, "Found old `BiomeConfigs` folder, but it could not be renamed to `", biomeFolderName, "`!");
                OTG.log(LogMarker.WARN, "Please rename the folder manually.");
                OTG.log(LogMarker.WARN, "========================");
                biomeFolderName = "BiomeConfigs";
            }
        }
        return biomeFolderName;
    }
        
	public static WorldConfig loadWorldConfigFromDisk(File worldDir)
	{
        File worldConfigFile = new File(worldDir, WorldStandardValues.WORLD_CONFIG_FILE_NAME);
        if(!worldConfigFile.exists())
        {
        	return null;
        }
        SettingsMap settingsMap = FileSettingsReader.read(worldDir.getName(), worldConfigFile);
        return new WorldConfig(worldDir, settingsMap, null, null);
	}
}
