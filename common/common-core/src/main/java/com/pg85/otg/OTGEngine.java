package com.pg85.otg;

import com.pg85.otg.config.ConfigFunction;
import com.pg85.otg.config.PluginConfig;
import com.pg85.otg.config.biome.BiomeConfigFinder.BiomeConfigStub;
import com.pg85.otg.config.biome.BiomeConfig;
import com.pg85.otg.config.biome.BiomeLoadInstruction;
import com.pg85.otg.config.biome.BiomeResourcesManager;
import com.pg85.otg.config.io.FileSettingsReader;
import com.pg85.otg.config.io.FileSettingsWriter;
import com.pg85.otg.config.world.WorldConfig;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.customobject.CustomObjectManager;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.customobject.structures.CustomStructureCache;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.presets.LocalPresetLoader;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IModLoadedChecker;
import com.pg85.otg.util.interfaces.IPluginConfig;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;

public abstract class OTGEngine
{
	// Classes implemented/provided by the platform-specific layer.
	
	private final LocalPresetLoader presetLoader;
	private final ILogger logger;
	private final IMaterialReader materialReader;
	private final IModLoadedChecker modLoadedChecker;

	// Common classes
	
	private final Path otgRootFolder;
	private PluginConfig pluginConfig;

	private BiomeResourcesManager biomeResourcesManager;
	private CustomObjectResourcesManager customObjectResourcesManager;
	private CustomObjectManager customObjectManager;
	
	protected OTGEngine(ILogger logger, Path otgRootFolder, IMaterialReader materialReader, IModLoadedChecker modLoadedChecker, LocalPresetLoader presetLoader)
	{
		this.logger = logger;
		this.otgRootFolder = otgRootFolder;
		this.presetLoader = presetLoader;
		this.materialReader = materialReader;
		this.modLoadedChecker = modLoadedChecker;
	}
	
	void onShutdown()
	{
		// Shutdown all loaders
		customObjectManager.shutdown();
	}

	void onStart()
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
		FileSettingsWriter.writeToFile(this.pluginConfig.getSettingsAsMap(), pluginConfigFile, this.pluginConfig.getSettingsMode(), this.logger);
		
		// Create manager objects
		
		boolean spawnLog = getPluginConfig().getSpawnLogEnabled();
		boolean developerMode = getPluginConfig().getDeveloperModeEnabled();
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

		// Load presets
		
		this.presetLoader.loadPresetsFromDisk(this.biomeResourcesManager, spawnLog, logger, materialReader);
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

	// OTG Configs
	
	public IPluginConfig getPluginConfig()
	{
		return pluginConfig;
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
	
	public ILogger getLogger()
	{
		return logger;
	}
	
	// Misc
	
	public abstract Collection<BiomeLoadInstruction> getDefaultBiomes();

	public abstract void mergeVanillaBiomeMobSpawnSettings(BiomeConfigStub biomeConfigStub, String biomeResourceLocation);
	
	// Builders/Factories
	
	public CustomStructureCache createCustomStructureCache(String worldName, Path worldSavepath, int dimId, long worldSeed, boolean isBo4Enabled)
	{
		// TODO: ModLoadedChecker
		return new CustomStructureCache(worldName, worldSavepath, dimId, worldSeed, isBo4Enabled, getOTGRootFolder(), getPluginConfig().getSpawnLogEnabled(), getLogger(), getCustomObjectManager(), getMaterialReader(), getCustomObjectResourcesManager(), null);
	}
}
