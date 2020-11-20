package com.pg85.otg;

import com.pg85.otg.config.PluginConfig;
import com.pg85.otg.config.biome.BiomeLoadInstruction;
import com.pg85.otg.config.biome.BiomeResourcesManager;
import com.pg85.otg.config.biome.BiomeConfigFinder.BiomeConfigStub;
import com.pg85.otg.config.customobjects.CustomObjectResourcesManager;
import com.pg85.otg.config.dimensions.DimensionsConfig;
import com.pg85.otg.config.dimensions.ModPackConfigManager;
import com.pg85.otg.config.io.FileSettingsReader;
import com.pg85.otg.config.io.FileSettingsWriter;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.customobjects.CustomObjectManager;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.logging.Logger;
import com.pg85.otg.presets.LocalPresetLoader;
import com.pg85.otg.presets.PresetNameProvider;
import com.pg85.otg.util.interfaces.IBiomeConfig;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IModLoadedChecker;
import com.pg85.otg.util.interfaces.IPresetNameProvider;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public abstract class OTGEngine
{
	//
	private final Path otgRootFolder;
	private final LocalPresetLoader presetLoader;
	//
	
	private HashMap<String, IBiomeConfig[]> otgBiomeIdsByWorld = new HashMap<String, IBiomeConfig[]>();
    //private BiomeModeManager biomeManagers;
    //private List<EventHandler> cancelableEventHandlers = new ArrayList<EventHandler>(5);
    private BiomeResourcesManager biomeResourcesManager;
    private CustomObjectResourcesManager customObjectResourcesManager;
    private CustomObjectManager customObjectManager;
    //private List<EventHandler> monitoringEventHandlers = new ArrayList<EventHandler>(5);
    private PluginConfig pluginConfig;
    private DimensionsConfig dimensionsConfig = null;
    private ModPackConfigManager modPackConfigManager;
    private final Logger logger;
    private final IMaterialReader materialReader;
    private final IModLoadedChecker modLoadedChecker;
    private final IPresetNameProvider presetNameProvider;

    protected OTGEngine(Logger logger, Path otgRootFolder, IMaterialReader materialReader, IModLoadedChecker modLoadedChecker, LocalPresetLoader presetLoader)
    {
        this.logger = logger;
        this.otgRootFolder = otgRootFolder;
        this.presetLoader = presetLoader;
        this.materialReader = materialReader;
        this.modLoadedChecker = modLoadedChecker;
        this.presetNameProvider = new PresetNameProvider();
    }
    
    public void onShutdown()
    {
        // Shutdown all loaders
        customObjectManager.shutdown();
        /*
        cancelableEventHandlers.clear();
        monitoringEventHandlers.clear();
        cancelableEventHandlers = null;
        monitoringEventHandlers = null;
        */
    }

    public void onStart()
    {
        // Start the engine

        // Do pluginConfig loading and then log anything that happened
        File pluginConfigFile = Paths.get(getOTGRootFolder().toString(), Constants.PluginConfigFilename).toFile();
        //SettingsMap settingsReader, IConfigFunctionProvider biomeResourcesManager, boolean spawnLog, ILogger logger, IMaterialReader materialReader
        //(SettingsMap settingsReader, IConfigFunctionProvider biomeResourcesManager, boolean spawnLog, ILogger logger, IMaterialReader materialReader)
        this.pluginConfig = new PluginConfig(
    		FileSettingsReader.read(Constants.PluginConfigFilename, pluginConfigFile, this.logger), 
    		this.biomeResourcesManager,
    		this.logger, 
    		this.materialReader
		);
        this.logger.setLevel(this.pluginConfig.getLogLevel().getLevel());
        FileSettingsWriter.writeToFile(this.pluginConfig.getSettingsAsMap(), pluginConfigFile, this.pluginConfig.settingsMode, this.logger);
        
        boolean spawnLog = getPluginConfig().spawnLog;
        boolean developerMode = getPluginConfig().developerMode;
        ILogger logger = this.logger;
        Path otgRootFolder = this.otgRootFolder;
        Path presetsDirectory = this.getPresetsDirectory();
        IMaterialReader materialReader = this.materialReader;
        
        this.biomeResourcesManager = new BiomeResourcesManager();
        this.customObjectResourcesManager = new CustomObjectResourcesManager();
        //this.biomeManagers = new BiomeModeManager();
        this.customObjectManager = new CustomObjectManager(spawnLog, developerMode, logger, otgRootFolder, presetsDirectory, this.customObjectResourcesManager);

        File presetsDir = Paths.get(getOTGRootFolder().toString(), Constants.PRESETS_FOLDER).toFile();
        if(!presetsDir.exists())
        {
      		presetsDir.mkdirs();
        }
        this.presetLoader.loadPresetsFromDisk(this.biomeResourcesManager, spawnLog, logger, materialReader);
        
        File modPacksDir = Paths.get(getOTGRootFolder().toString(), Constants.MODPACK_CONFIGS_FOLDER).toFile();
        if(!modPacksDir.exists())
        {
        	modPacksDir.mkdirs();
        }
        
        this.modPackConfigManager = new ModPackConfigManager(otgRootFolder, this.biomeResourcesManager, spawnLog, logger, materialReader);
        
        File globalObjectsDir = Paths.get(getOTGRootFolder().toString(), Constants.GLOBAL_OBJECTS_FOLDER).toFile();
        if(!globalObjectsDir.exists())
        {
        	globalObjectsDir.mkdirs();
        }
        
        // Fire start event
        /*
        for (EventHandler handler : cancelableEventHandlers)
        {
            handler.onStart();
        }
        for (EventHandler handler : monitoringEventHandlers)
        {
            handler.onStart();
        }
        */
    }
    
    // Events
    
    /**
     * Register your event handler here with normal priority. You can do this
     * before OpenTerrainGenerator is started.
     * <p/>
     * <p>
     * @param handler The handler that will receive the events.
     */
    /*
    public void registerEventHandler(EventHandler handler)
    {
        cancelableEventHandlers.add(handler);
    }
    */

    /**
     * Register you event handler here with the given priority. You can do
     * this before OpenTerrainGenerator is started.
     * <p/>
     * <p>
     * @param handler  The handler that will receive the events.
     * @param priority The priority of the event.
     */
    /*
    public void registerEventHandler(EventHandler handler, EventPriority priority)
    {
        if (priority == EventPriority.CANCELABLE)
        {
            cancelableEventHandlers.add(handler);
        } else {
            monitoringEventHandlers.add(handler);
        }
    }
    
	public boolean fireReplaceBiomeBlocksEvent(int x, int z, ChunkBuffer chunkBuffer)
	{
        return true;
	}
    */
    
    /**
     * Fires the canCustomObjectSpawn event.
     * <p>
     * @see EventHandler#canCustomObjectSpawn(CustomObject, LocalWorld, int,
     * int, int, boolean)
     * @return True if the event handlers allow that the object is spawned,
     *         false otherwise.
     */
	/*
    public boolean fireCanCustomObjectSpawnEvent(ICustomObject object, IWorldGenRegion worldGenRegion, int x, int y, int z)
    {
        boolean success = true;
        for (EventHandler handler : cancelableEventHandlers)
        {
            if (!handler.canCustomObjectSpawn(object, worldGenRegion, x, y, z, !success))
            {
                success = false;
            }
        }
        for (EventHandler handler : monitoringEventHandlers)
        {
            handler.canCustomObjectSpawn(object, worldGenRegion, x, y, z, !success);
        }
        return success;
    }
    */

    /**
     * Fires the onPopulateEnd event.
     * <p>
     * @see EventHandler#onPopulateEnd(LocalWorld, Random, boolean, int, int)
     */
    /*
    public void firePopulationEndEvent(LocalWorld world, Random random, boolean villageInChunk, ChunkCoordinate chunkCoord)
    {
        for (EventHandler handler : cancelableEventHandlers)
            handler.onPopulateEnd(world, random, villageInChunk, chunkCoord.getChunkX(), chunkCoord.getChunkZ());
        for (EventHandler handler : monitoringEventHandlers)
            handler.onPopulateEnd(world, random, villageInChunk, chunkCoord.getChunkX(), chunkCoord.getChunkZ());
    }
    */

    /**
     * Fires the onPopulateStart event.
     * <p>
     * @see EventHandler#onPopulateStart(LocalWorld, Random, boolean, int,
     * int)
     */
    /*
    public void firePopulationStartEvent(LocalWorld world, Random random, boolean villageInChunk, ChunkCoordinate chunkCoord)
    {
        for (EventHandler handler : cancelableEventHandlers)
            handler.onPopulateStart(world, random, villageInChunk, chunkCoord.getChunkX(), chunkCoord.getChunkZ());
        for (EventHandler handler : monitoringEventHandlers)
            handler.onPopulateStart(world, random, villageInChunk, chunkCoord.getChunkX(), chunkCoord.getChunkZ());
    }
    */

    /**
     * Fires the onResourceProcess event.
     * <p>
     * @see EventHandler#onResourceProcess(Resource, LocalWorld, Random,
     * boolean, int, int, boolean)
     * @return True if the event handlers allow that the resource is spawned,
     *         false otherwise.
     */
	/*
    public boolean fireResourceProcessEvent(Resource resource, IWorldGenRegion worldGenregion, Random random, boolean villageInChunk, int chunkX,
            int chunkZ)
    {    	
        boolean success = true;
        for (EventHandler handler : cancelableEventHandlers)
        {
            if (!handler.onResourceProcess(resource, worldGenregion, random, villageInChunk, chunkX, chunkZ, !success))
            {
                success = false;
            }
        }
        for (EventHandler handler : monitoringEventHandlers)
        {
            handler.onResourceProcess(resource, worldGenregion, random, villageInChunk, chunkX, chunkZ, !success);
        }
        return success;
    }
    */
    
    // Managers

    /**
     * Returns the biome managers. Register your own biome manager here.
     * <p/>
     * <p>
     * @return The biome managers.
     */
    /*
    public BiomeModeManager getBiomeModeManager()
    {
        return biomeManagers;
    }   
    */

    public BiomeResourcesManager getBiomeResourceManager()
    {
        return biomeResourcesManager;
    }
    
    public CustomObjectResourcesManager getCustomObjectResourcesManager()
    {
        return customObjectResourcesManager;
    }
    
    public CustomObjectManager getCustomObjectManager()
    {
        return customObjectManager;
    }

	public ModPackConfigManager getModPackConfigManager()
	{
		return this.modPackConfigManager;
	}
    
	public LocalPresetLoader getPresetLoader()
	{
		return this.presetLoader;
	}
	
	public IModLoadedChecker getModLoadedChecker()
	{
		return this.modLoadedChecker;
	}
	
	public IMaterialReader getMaterialReader()
	{
		return this.materialReader;
	}

	public IPresetNameProvider getPresetNameProvider()
	{
		return this.presetNameProvider;
	}	

	// OTG dirs

    public Path getOTGRootFolder()
    {
        return this.otgRootFolder;
    }

    public Path getPresetsDirectory()
    {
        return Paths.get(this.getOTGRootFolder().toString(), Constants.PRESETS_FOLDER);
    }
    
    // Plugin configs
    
    public PluginConfig getPluginConfig()
    {
        return pluginConfig;
    }   
    
    public DimensionsConfig getDimensionsConfig()
    {    	
    	return this.dimensionsConfig;
    }

    public void setDimensionsConfig(DimensionsConfig dimensionsConfig)
    {
    	this.dimensionsConfig = dimensionsConfig;
    }

	// Worlds
	
    //public abstract LocalWorld getWorld(String name);

    //public abstract LocalWorld getUnloadedWorld(String name);
    
    //public abstract ArrayList<LocalWorld> getAllWorlds();

    // Biomes
    
	public void setOTGBiomeId(String worldName, int i, IBiomeConfig biomeConfig, boolean replaceExisting)
	{
    	if(!this.otgBiomeIdsByWorld.containsKey(worldName))
    	{
    		this.otgBiomeIdsByWorld.put(worldName, new IBiomeConfig[1024]);
    	}
    	if(replaceExisting || this.otgBiomeIdsByWorld.get(worldName)[i] == null)
    	{
    		this.otgBiomeIdsByWorld.get(worldName)[i] = biomeConfig;
    	} else {
    		throw new RuntimeException("Tried to register OTG biome " + biomeConfig.getName() + " with id " + i + " but the id is in use by biome " + this.otgBiomeIdsByWorld.get(worldName)[i].getName() + ". OTG 1.12.2 v7 and above use dynamic biome id's for new worlds, this avoids the problem completely.");
    	}
	}

    public IBiomeConfig[] getOTGBiomeIds(String worldName)
    {
    	return this.otgBiomeIdsByWorld.containsKey(worldName) ? this.otgBiomeIdsByWorld.get(worldName) : new IBiomeConfig[1024];
    }
    
	public boolean isOTGBiomeIdAvailable(String worldName, int i)
	{
		return !this.otgBiomeIdsByWorld.containsKey(worldName) || this.otgBiomeIdsByWorld.get(worldName)[i] == null;
	}

	// Logging
	
    public Logger getLogger()
    {
        return logger;
    }

	public abstract boolean areEnoughBiomeIdsAvailableForPresets(ArrayList<String> presetNames);

	public abstract Collection<BiomeLoadInstruction> getDefaultBiomes();

	public abstract void mergeVanillaBiomeMobSpawnSettings(BiomeConfigStub biomeConfigStub, String biomeResourceLocation);
}
