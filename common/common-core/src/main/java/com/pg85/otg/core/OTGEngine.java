package com.pg85.otg.core;

import com.pg85.otg.config.ConfigFunction;
import com.pg85.otg.config.biome.BiomeResourcesManager;
import com.pg85.otg.config.io.FileSettingsReader;
import com.pg85.otg.config.io.FileSettingsWriter;
import com.pg85.otg.config.standard.WorldStandardValues;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.core.config.PluginConfig;
import com.pg85.otg.core.config.biome.BiomeConfig;
import com.pg85.otg.core.config.world.WorldConfig;
import com.pg85.otg.core.presets.LocalPresetLoader;
import com.pg85.otg.customobject.CustomObjectManager;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.customobject.structures.CustomStructureCache;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IModLoadedChecker;
import com.pg85.otg.interfaces.IPluginConfig;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Implemented and provided by the platform-specific layer on app start and accessed via OTG.startEngine()/OTG.getEngine(),
 * this class holds any objects and methods used during an app session.
 * 
 * Constructor parameters are platform-specific implementations of wrapper classes, such as a logger, material reader, 
 * preset loader etc. Implement these to provide support for a platform (Forge, Spigot etc).
 *  
 * OTGEngine.onStart() should be called on mod/plugin start, creates all OTG files and folders and registers all presets 
 * and biomes via the platform-specific preset loader provided as a constructor parameter. 
 */
public abstract class OTGEngine
{
	// Classes implemented/provided by the platform-specific layer.
	
	protected final LocalPresetLoader presetLoader;
	protected final ILogger logger;
	private final IModLoadedChecker modLoadedChecker;

	// Common classes
	
	private final Path otgRootFolder;
	private final Path globalObjectsFolder;
	protected PluginConfig pluginConfig;

	protected BiomeResourcesManager biomeResourcesManager;
	private CustomObjectResourcesManager customObjectResourcesManager;
	private CustomObjectManager customObjectManager;
	
	protected OTGEngine(ILogger logger, Path otgRootFolder, IModLoadedChecker modLoadedChecker, LocalPresetLoader presetLoader)
	{
		this.logger = logger;
		this.otgRootFolder = otgRootFolder;
		this.globalObjectsFolder = otgRootFolder.resolve(Constants.GLOBAL_OBJECTS_FOLDER);
		this.presetLoader = presetLoader;
		this.modLoadedChecker = modLoadedChecker;
	}
	
	// Get jar file that's running OTG, where we will find our default preset
	public abstract File getJarFile();
	
	// Startup / shutdown

	public void onStart()
	{
		// Load plugin config

		File pluginConfigFile = Paths.get(getOTGRootFolder().toString(), Constants.PluginConfigFilename).toFile();
		this.pluginConfig = new PluginConfig(
			FileSettingsReader.read(Constants.PluginConfigFilename, pluginConfigFile, (ILogger)null), 
			this.biomeResourcesManager,
			this.logger
		);
		this.logger.init(
			this.pluginConfig.getLogLevel().getLevel(), 
			this.pluginConfig.logCustomObjects(), 
			this.pluginConfig.logStructurePlotting(), 
			this.pluginConfig.logConfigs(),
			this.pluginConfig.logBiomeRegistry(),
			this.pluginConfig.logPerformance(),
			this.pluginConfig.logDecoration(),
			this.pluginConfig.logMobs(),
			this.pluginConfig.logPresets()
		);
		FileSettingsWriter.writeToFile(this.pluginConfig.getSettingsAsMap(), pluginConfigFile, this.pluginConfig.getSettingsMode(), this.logger);

		// Create OTG folders

		File presetsDir = Paths.get(getOTGRootFolder().toString(), Constants.PRESETS_FOLDER).toFile();
		if(!presetsDir.exists())
		{
			presetsDir.mkdirs();
		}

		File dimensionConfigsDir = Paths.get(getOTGRootFolder().toString(), Constants.DIMENSION_CONFIGS_FOLDER).toFile();
		if(!dimensionConfigsDir.exists())
		{
			dimensionConfigsDir.mkdirs();
		}

		File globalObjectsDir = this.globalObjectsFolder.toFile();
		if(!globalObjectsDir.exists())
		{
			globalObjectsDir.mkdirs();
		}

		unpackDefaultPresetAndExamples(presetsDir);

		// Create manager objects

		this.customObjectResourcesManager = new CustomObjectResourcesManager();
		this.customObjectManager = new CustomObjectManager(
			getPluginConfig().getDeveloperModeEnabled(), 
			this.logger, 
			this.otgRootFolder, 
			getPresetsDirectory(), 
			this.customObjectResourcesManager
		);

		// Create BiomeResourcesManager, pass all config resources

		HashMap<String, Class<? extends ConfigFunction<?>>> configFunctions = new HashMap<>();
		configFunctions.putAll(WorldConfig.CONFIG_FUNCTIONS);
		configFunctions.putAll(BiomeConfig.RESOURCE_QUEUE_RESOURCES);
		this.biomeResourcesManager = new BiomeResourcesManager(configFunctions);

		// Load presets

		this.presetLoader.loadPresetsFromDisk(this.biomeResourcesManager, this.logger);
	}

	private void unpackDefaultPresetAndExamples(File presetsDir)
	{
		JarFile jarFile = null;
		try
		{
			File jarFileLocation = getJarFile();
			if(jarFileLocation == null || !jarFileLocation.exists())
			{
				this.logger.log(LogLevel.WARN, LogCategory.MAIN, "Could not find root jar file, skipping default preset unpack (copy it manually for development).");
			} else {
				jarFile = new JarFile(jarFileLocation);
				Enumeration<JarEntry> entries = jarFile.entries();
				// Unpack default preset if none present
				if (new File(presetsDir.getPath() + File.separator + "Default").exists())
				{
					File wc = new File(presetsDir.getPath() + File.separator+ "Default" + File.separator + Constants.WORLD_CONFIG_FILE);
					if (wc.exists())
					{
						BufferedReader reader = new BufferedReader(new FileReader(wc));
						int oldMajorVer = parseMajorVersion(reader);
						int oldMinorVer = parseMinorVersion(reader);
						int newMajorVer = 0;
						int newMinorVer = 0;
	
						while (entries.hasMoreElements())
						{
							JarEntry jarEntry = entries.nextElement();
							if (jarEntry.getName().contains("Default/" + Constants.WORLD_CONFIG_FILE))
							{
								reader = new BufferedReader(new BufferedReader(new InputStreamReader(jarFile.getInputStream(jarEntry))));
								newMajorVer = parseMajorVersion(reader);
								newMinorVer = parseMinorVersion(reader);
							}
						}
						if(
							(newMajorVer < oldMajorVer) ||
							(newMajorVer == oldMajorVer && newMinorVer <= oldMinorVer)
						)
						{
							return;
						}
					}
				}
	
				String rootDir = getOTGRootFolder().toString();
				String defaultPresetPath = "resources/Presets/Default/";
				String dimensionConfigsPath = "resources/DimensionConfigs/";
				entries = jarFile.entries();
	
				while (entries.hasMoreElements())
				{
					JarEntry entry = entries.nextElement();
					if (
							entry.getName().startsWith(dimensionConfigsPath) ||
									entry.getName().startsWith(defaultPresetPath)
					)
					{
						File file = new File(rootDir + File.separator + (entry.getName().substring(10)));
	
						if (entry.isDirectory())
						{
							file.mkdirs();
						} else {
							file.createNewFile();
							FileOutputStream fos = new FileOutputStream(file);
							byte[] byteArray = new byte[4096];
							int i;
							java.io.InputStream is = jarFile.getInputStream(entry);
							while ((i = is.read(byteArray)) > 0)
							{
								fos.write(byteArray, 0, i);
							}
							is.close();
							fos.close();
						}
					}
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		} finally {
			if(jarFile != null)
			{
				try {
					jarFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private int parseMajorVersion(BufferedReader reader) throws IOException
	{
		return parseVersion(reader, WorldStandardValues.MAJOR_VERSION.getName());
	}
	
	private int parseMinorVersion(BufferedReader reader) throws IOException
	{
		return parseVersion(reader, WorldStandardValues.MINOR_VERSION.getName());
	}
	
	private int parseVersion(BufferedReader reader, String name) throws IOException
	{
		int version = -1;
		String line;
		// Filter out the line with Version in it
		while ((line = reader.readLine()) != null)
		{
			if (line.contains(name))
			{
				break;
			}
		}
		if (line != null)
		{
			String v = line.split(":")[1];
			v = v.trim();
			version = Integer.parseInt(v);
		}
		return version;
	}

	public void onShutdown()
	{
		// Shutdown all loaders
		this.customObjectManager.shutdown();
	}

	// Managers

	public BiomeResourcesManager getBiomeResourceManager()
	{
		return this.biomeResourcesManager;
	}
	
	public CustomObjectResourcesManager getCustomObjectResourcesManager()
	{
		return this.customObjectResourcesManager;
	}
	
	public CustomObjectManager getCustomObjectManager()
	{
		return this.customObjectManager;
	}
	
	public LocalPresetLoader getPresetLoader()
	{
		return this.presetLoader;
	}
	
	public IModLoadedChecker getModLoadedChecker()
	{
		return this.modLoadedChecker;
	}

	// OTG Configs
	
	public IPluginConfig getPluginConfig()
	{
		return this.pluginConfig;
	}

	// OTG dirs

	public Path getOTGRootFolder()
	{
		return this.otgRootFolder;
	}

	public Path getGlobalObjectsFolder()
	{
		return this.globalObjectsFolder;
	}

	public Path getPresetsDirectory()
	{
		return Paths.get(this.getOTGRootFolder().toString(), Constants.PRESETS_FOLDER);
	}

	// Logging

	public ILogger getLogger()
	{
		return this.logger;
	}
		
	// Builders/Factories
	
	public CustomStructureCache createCustomStructureCache(String presetFolderName, Path worldSavepath, long worldSeed, boolean isBo4Enabled)
	{
		// TODO: ModLoadedChecker
		return new CustomStructureCache(
			presetFolderName, 
			worldSavepath, 
			worldSeed, 
			isBo4Enabled, 
			getOTGRootFolder(), 
			getLogger(), 
			getCustomObjectManager(), 
			getPresetLoader().getMaterialReader(presetFolderName), 
			getCustomObjectResourcesManager(), 
			null
		);
	}
}
