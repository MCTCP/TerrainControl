package com.pg85.otg;

import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.BiomeResourcesManager;
import com.pg85.otg.configuration.PluginConfig;
import com.pg85.otg.configuration.customobjects.CustomObjectResourcesManager;
import com.pg85.otg.configuration.dimensions.DimensionsConfig;
import com.pg85.otg.customobjects.CustomObject;
import com.pg85.otg.customobjects.CustomObjectManager;
import com.pg85.otg.events.EventHandler;
import com.pg85.otg.events.EventPriority;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.generator.ChunkBuffer;
import com.pg85.otg.generator.biome.BiomeModeManager;
import com.pg85.otg.generator.resource.Resource;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.FifoMap;
import com.pg85.otg.util.minecraftTypes.DefaultMaterial;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OTG
{		
	// Used to determine if a new world is being created or if a world save already exists
	// TODO: Make this prettier.
	public static boolean isNewWorldBeingCreated = false;
    static FifoMap<String, LocalMaterialData> cachedMaterials = new FifoMap<String, LocalMaterialData>(4096);
	
    /**
     * The engine that powers Open Terrain Generator.
     */
    private static OTGEngine engine;
	
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
        return engine;
    }
    
    /**
     * Sets the engine and calls its {@link OTGEngine#onStart()
     * onStart()} method.
     * <p>
     * @param engine The engine.
     */
    public static void setEngine(OTGEngine engine)
    {
        if (OTG.engine != null)
        {
            throw new IllegalStateException("Engine is already set.");
        }

        OTG.engine = engine;
        engine.onStart();
    }

    /**
     * Nulls out static references to free up memory. Should be called on
     * shutdown. Engine can be restarted after this.
     */
    public static void stopEngine()
    {
        engine.onShutdown();
        engine = null;
    }
    
    // Managers
    
    /**
     * Returns the biome managers. Register your own biome manager here.
     * <p>
     * @return The biome managers.
     */
    public static BiomeModeManager getBiomeModeManager()
    {
        return engine.getBiomeModeManager();
    }

    public static BiomeResourcesManager getBiomeResourcesManager()
    {
        return engine.getBiomeResourceManager();
    }

    public static CustomObjectResourcesManager getCustomObjectResourcesManager()
    {
        return engine.getCustomObjectResourcesManager();
    }

    public static CustomObjectManager getCustomObjectManager()
    {
        return engine.getCustomObjectManager();
    }
    
    // Plugin
    
    public static PluginConfig getPluginConfig()
    {
        return engine.getPluginConfig();
    }    
    
    // Dimensions config
    
	/**
	 * A config for each dimension of the currently active world
	 */
    public static DimensionsConfig getDimensionsConfig()
    {    	
    	return engine.getDimensionsConfig();
    }

    public static void setDimensionsConfig(DimensionsConfig dimensionsConfig)
    {
    	engine.setDimensionsConfig(dimensionsConfig);
    }
    
    // Biomes
    
    // For bukkit plugin developers, do not remove. See: https://github.com/MCTCP/TerrainControl/wiki/Developer-page
    /*
	* Convienence method to quickly get the biome name at the given
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
       return world.getSavedBiome(x, z).getName();
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
        return engine.getWorld(name);
    }

    public static LocalWorld getUnloadedWorld(String name)
    {
    	return engine.getUnloadedWorld(name);
    }
    
    public static ArrayList<LocalWorld> getAllWorlds()
    {
        return engine.getAllWorlds();
    }
    
    // Materials

    /**
     * @see OTGEngine#readMaterial(String)
     */
    public static LocalMaterialData readMaterial(String name) throws InvalidConfigException
    {
    	// TODO: Make sure it won't cause problems to return the same material object multiple times, is it not changed anywhere?
    	LocalMaterialData material = cachedMaterials.get(name);
    	if(material != null)
    	{
    		return material;
    	}
    	else if(cachedMaterials.containsKey(name))
    	{
    		throw new InvalidConfigException("Cannot read block: " + name);
    	}

    	String originalName = name;
    	
    	// Spigot interprets snow as SNOW_LAYER and that's how TC has always seen it too so keep it that way (even though minecraft:snow is actually a snow block).
    	if(name.toLowerCase().equals("snow"))
    	{
    		name = "SNOW_LAYER";
    	}
    	// Spigot interprets water as FLOWING_WATER and that's how TC has always seen it too so keep it that way (even though minecraft:water is actually stationary water).
    	if(name.toLowerCase().equals("water"))
    	{
    		name = "FLOWING_WATER";
    	}
    	// Spigot interprets lava as FLOWING_LAVA and that's how TC has always seen it too so keep it that way (even though minecraft:lava is actually stationary lava).
    	if(name.toLowerCase().equals("lava"))
    	{
    		name = "FLOWING_LAVA";
    	}

    	try
    	{
    		material = engine.readMaterial(name);
    	}
    	catch(InvalidConfigException ex)
    	{
    		cachedMaterials.put(originalName, null);
    		throw ex;
    	}

    	cachedMaterials.put(originalName, material);

        return material;
    }

    /**
     * @see OTGEngine#toLocalMaterialData(DefaultMaterial, int)
     */
    public static LocalMaterialData toLocalMaterialData(DefaultMaterial defaultMaterial, int blockData)
    {
        return engine.toLocalMaterialData(defaultMaterial, blockData);
    }

    // Events
    
    /**
     * @see OTGEngine#registerEventHandler(EventHandler)
     */
    public static void registerEventHandler(EventHandler handler)
    {
        engine.registerEventHandler(handler);
    }

    /**
     * @see OTGEngine#registerEventHandler(EventHandler,
     * EventPriority)
     */
    public static void registerEventHandler(EventHandler handler, EventPriority priority)
    {
        engine.registerEventHandler(handler, priority);
    }
    
    public static boolean fireReplaceBiomeBlocksEvent(int x, int z, ChunkBuffer chunkBuffer, LocalWorld localWorld)
    {
    	return engine.fireReplaceBiomeBlocksEvent(x, z, chunkBuffer, localWorld);
    }
    
    /**
     * @see OTGEngine#fireCanCustomObjectSpawnEvent(CustomObject,
     * LocalWorld, int, int, int)
     */
    public static boolean fireCanCustomObjectSpawnEvent(CustomObject object, LocalWorld world, int x, int y, int z)
    {
        return engine.fireCanCustomObjectSpawnEvent(object, world, x, y, z);
    }

    /**
     * @see OTGEngine#firePopulationEndEvent(LocalWorld, Random,
     * boolean, ChunkCoordinate)
     */
    public static void firePopulationEndEvent(LocalWorld world, Random random, boolean villageInChunk, ChunkCoordinate chunkCoord)
    {
        engine.firePopulationEndEvent(world, random, villageInChunk, chunkCoord);
    }

    /**
     * @see OTGEngine#firePopulationStartEvent(LocalWorld, Random,
     * boolean, ChunkCoordinate)
     */
    public static void firePopulationStartEvent(LocalWorld world, Random random, boolean villageInChunk, ChunkCoordinate chunkCoord)
    {
        engine.firePopulationStartEvent(world, random, villageInChunk, chunkCoord);
    }

    /**
     * @see OTGEngine#fireResourceProcessEvent(Resource,
     * LocalWorld, Random, boolean, int, int)
     */
    public static boolean fireResourceProcessEvent(Resource resource, LocalWorld world, Random random, boolean villageInChunk, int chunkX, int chunkZ)
    {
        return engine.fireResourceProcessEvent(resource, world, random, villageInChunk, chunkX, chunkZ);
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
        engine.getLogger().log(level, messages);
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
        engine.getLogger().log(level, message, params);
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
        engine.getLogger().log(level, stringWriter.toString());
    }
}
