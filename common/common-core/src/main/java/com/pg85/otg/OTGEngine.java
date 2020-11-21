package com.pg85.otg;

import com.pg85.otg.config.ConfigFunction;
import com.pg85.otg.config.PluginConfig;
import com.pg85.otg.config.biome.BiomeConfigFinder.BiomeConfigStub;
import com.pg85.otg.config.biome.BiomeConfig;
import com.pg85.otg.config.biome.BiomeLoadInstruction;
import com.pg85.otg.config.biome.BiomeResourcesManager;
import com.pg85.otg.config.customobjects.CustomObjectResourcesManager;
import com.pg85.otg.config.dimensions.DimensionsConfig;
import com.pg85.otg.config.dimensions.ModPackConfigManager;
import com.pg85.otg.config.io.FileSettingsReader;
import com.pg85.otg.config.io.FileSettingsWriter;
import com.pg85.otg.config.world.WorldConfig;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.customobjects.CustomObjectManager;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.logging.Logger;
import com.pg85.otg.presets.LocalPresetLoader;
import com.pg85.otg.presets.PresetNameProvider;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IModLoadedChecker;
import com.pg85.otg.util.interfaces.IPresetNameProvider;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;

public abstract class OTGEngine
{
	private final Path otgRootFolder;
	private final LocalPresetLoader presetLoader;
    private final Logger logger;
    private final IMaterialReader materialReader;
    private final IModLoadedChecker modLoadedChecker;
    private final IPresetNameProvider presetNameProvider;

    private PluginConfig pluginConfig;
    private DimensionsConfig dimensionsConfig;

    private BiomeResourcesManager biomeResourcesManager;
    private CustomObjectResourcesManager customObjectResourcesManager;
    private CustomObjectManager customObjectManager;
    private ModPackConfigManager modPackConfigManager;
    
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
    }

    public void onStart()
    {
    	// Load plugin config
    	
        File pluginConfigFile = Paths.get(getOTGRootFolder().toString(), Constants.PluginConfigFilename).toFile();
        this.pluginConfig = new PluginConfig(
    		FileSettingsReader.read(Constants.PluginConfigFilename, pluginConfigFile, this.logger), 
    		this.biomeResourcesManager,
    		this.logger, 
    		this.materialReader
		);
        this.logger.setLevel(this.pluginConfig.getLogLevel().getLevel());
        FileSettingsWriter.writeToFile(this.pluginConfig.getSettingsAsMap(), pluginConfigFile, this.pluginConfig.settingsMode, this.logger);
        
        // Create manager objects
        
        boolean spawnLog = getPluginConfig().spawnLog;
        boolean developerMode = getPluginConfig().developerMode;
        ILogger logger = this.logger;
        Path otgRootFolder = this.otgRootFolder;
        Path presetsDirectory = this.getPresetsDirectory();
        IMaterialReader materialReader = this.materialReader;

        this.customObjectResourcesManager = new CustomObjectResourcesManager();
        this.customObjectManager = new CustomObjectManager(spawnLog, developerMode, logger, otgRootFolder, presetsDirectory, this.customObjectResourcesManager);
        
        // Create BiomeResourcesManager, pass all config resources
        
        HashMap<String, Class<? extends ConfigFunction<?>>> configFunctions = new HashMap<>();
        configFunctions.putAll(WorldConfig.CONFIG_FUNCTIONS);
        configFunctions.putAll(BiomeConfig.CONFIG_FUNCTIONS);
        this.biomeResourcesManager = new BiomeResourcesManager(configFunctions);       
        
        // Create OTG folders
        
        File presetsDir = Paths.get(getOTGRootFolder().toString(), Constants.PRESETS_FOLDER).toFile();
        if(!presetsDir.exists())
        {
      		presetsDir.mkdirs();
        }
        
        File modPacksDir = Paths.get(getOTGRootFolder().toString(), Constants.MODPACK_CONFIGS_FOLDER).toFile();
        if(!modPacksDir.exists())
        {
        	modPacksDir.mkdirs();
        }
               
        File globalObjectsDir = Paths.get(getOTGRootFolder().toString(), Constants.GLOBAL_OBJECTS_FOLDER).toFile();
        if(!globalObjectsDir.exists())
        {
        	globalObjectsDir.mkdirs();
        }

        // Load presets and modpack configs
        
        this.presetLoader.loadPresetsFromDisk(this.biomeResourcesManager, spawnLog, logger, materialReader);
        this.modPackConfigManager = new ModPackConfigManager(otgRootFolder, this.biomeResourcesManager, spawnLog, logger, materialReader);        
    }
    
    // Managers

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

	// OTG Configs
	
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
    
	// OTG dirs

    public Path getOTGRootFolder()
    {
        return this.otgRootFolder;
    }

    public Path getPresetsDirectory()
    {
        return Paths.get(this.getOTGRootFolder().toString(), Constants.PRESETS_FOLDER);
    }

    // Logging
    
    public Logger getLogger()
    {
        return logger;
    }
    
    // Misc
    
	public abstract Collection<BiomeLoadInstruction> getDefaultBiomes();

	public abstract void mergeVanillaBiomeMobSpawnSettings(BiomeConfigStub biomeConfigStub, String biomeResourceLocation);
}
