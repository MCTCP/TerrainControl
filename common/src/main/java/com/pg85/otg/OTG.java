package com.pg85.otg;

import com.pg85.otg.config.PluginConfig;
import com.pg85.otg.config.biome.BiomeResourcesManager;
import com.pg85.otg.config.customobjects.CustomObjectResourcesManager;
import com.pg85.otg.config.dimensions.DimensionsConfig;
import com.pg85.otg.customobjects.CustomObjectManager;
import com.pg85.otg.customobjects.bo4.BO4Config;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IModLoadedChecker;
import com.pg85.otg.util.interfaces.IPresetNameProvider;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

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
    /*
    public static BiomeModeManager getBiomeModeManager()
    {
        return Engine.getBiomeModeManager();
    }
    */

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
    /*
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
    */
    
    // Events
    
    /**
     * @see OTGEngine#registerEventHandler(EventHandler)
     */
    /*
    public static void registerEventHandler(EventHandler handler)
    {
        Engine.registerEventHandler(handler);
    }
    */

    /**
     * @see OTGEngine#registerEventHandler(EventHandler,
     * EventPriority)
     */
    /*
    public static void registerEventHandler(EventHandler handler, EventPriority priority)
    {
        Engine.registerEventHandler(handler, priority);
    }
    
    public static boolean fireReplaceBiomeBlocksEvent(int x, int z, ChunkBuffer chunkBuffer)
    {
    	return Engine.fireReplaceBiomeBlocksEvent(x, z, chunkBuffer);
    }
    */
    
    /**
     * @see OTGEngine#fireCanCustomObjectSpawnEvent(CustomObject,
     * LocalWorld, int, int, int)
     */
    /*
    public static boolean fireCanCustomObjectSpawnEvent(CustomObject object, IWorldGenRegion worldGenRegion, int x, int y, int z)
    {
        return Engine.fireCanCustomObjectSpawnEvent(object, worldGenRegion, x, y, z);
    }
    */

    /**
     * @see OTGEngine#firePopulationEndEvent(LocalWorld, Random,
     * boolean, ChunkCoordinate)
     */
    /*
    public static void firePopulationEndEvent(LocalWorld world, Random random, boolean villageInChunk, ChunkCoordinate chunkCoord)
    {
        Engine.firePopulationEndEvent(world, random, villageInChunk, chunkCoord);
    }
    */

    /**
     * @see OTGEngine#firePopulationStartEvent(LocalWorld, Random,
     * boolean, ChunkCoordinate)
     */
    /*
    public static void firePopulationStartEvent(LocalWorld world, Random random, boolean villageInChunk, ChunkCoordinate chunkCoord)
    {
        Engine.firePopulationStartEvent(world, random, villageInChunk, chunkCoord);
    }
    */

    /**
     * @see OTGEngine#fireResourceProcessEvent(Resource,
     * LocalWorld, Random, boolean, int, int)
     */
    /*
    public static boolean fireResourceProcessEvent(Resource resource, IWorldGenRegion worldGenregion, Random random, boolean villageInChunk, int chunkX, int chunkZ)
    {
        return Engine.fireResourceProcessEvent(resource, worldGenregion, random, villageInChunk, chunkX, chunkZ);
    }
    */   
    
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
        Engine.getLogger().log(level, e, maxDepth);
    }
	    
    public static boolean bo4DataExists(BO4Config config)
    {
		String filePath = 
			config.getFile().getAbsolutePath().endsWith(".BO4") ? config.getFile().getAbsolutePath().replace(".BO4", ".BO4Data") :
			config.getFile().getAbsolutePath().endsWith(".bo4") ? config.getFile().getAbsolutePath().replace(".bo4", ".BO4Data") :
			config.getFile().getAbsolutePath().endsWith(".BO3") ? config.getFile().getAbsolutePath().replace(".BO3", ".BO4Data") :
			config.getFile().getAbsolutePath().endsWith(".bo3") ? config.getFile().getAbsolutePath().replace(".bo3", ".BO4Data") :
			config.getFile().getAbsolutePath();

        File file = new File(filePath);
        return file.exists();
    }
    
    public static void generateBO4Data(BO4Config config, Path otgRootFolder, boolean spawnLog, ILogger logger, CustomObjectManager customObjectManager, IPresetNameProvider presetNameProvider, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
    {
        //write to disk
		String filePath = 
			config.getFile().getAbsolutePath().endsWith(".BO4") ? config.getFile().getAbsolutePath().replace(".BO4", ".BO4Data") :
			config.getFile().getAbsolutePath().endsWith(".bo4") ? config.getFile().getAbsolutePath().replace(".bo4", ".BO4Data") :
			config.getFile().getAbsolutePath().endsWith(".BO3") ? config.getFile().getAbsolutePath().replace(".BO3", ".BO4Data") :
			config.getFile().getAbsolutePath().endsWith(".bo3") ? config.getFile().getAbsolutePath().replace(".bo3", ".BO4Data") :
			config.getFile().getAbsolutePath();

        File file = new File(filePath);
        if(!file.exists())
        {
            try {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				DataOutputStream dos = new DataOutputStream(bos);
				config.writeToStream(dos, otgRootFolder, spawnLog, logger, customObjectManager, presetNameProvider, materialReader, manager, modLoadedChecker);
				byte[] compressedBytes = com.pg85.otg.util.CompressionUtils.compress(bos.toByteArray(), spawnLog, logger);
				dos.close();
				FileOutputStream fos = new FileOutputStream(file);
				DataOutputStream dos2 = new DataOutputStream(fos);
				dos2.write(compressedBytes, 0, compressedBytes.length);
				dos2.close();
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
