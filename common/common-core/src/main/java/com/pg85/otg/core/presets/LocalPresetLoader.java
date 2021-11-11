package com.pg85.otg.core.presets;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

import com.pg85.otg.config.biome.BiomeConfigFinder;
import com.pg85.otg.config.biome.BiomeConfigFinder.BiomeConfigStub;
import com.pg85.otg.config.io.FileSettingsReader;
import com.pg85.otg.config.io.FileSettingsWriter;
import com.pg85.otg.config.io.IConfigFunctionProvider;
import com.pg85.otg.config.io.SettingsMap;
import com.pg85.otg.config.standard.BiomeStandardValues;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.core.config.biome.BiomeConfig;
import com.pg85.otg.core.config.world.WorldConfig;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IWorldConfig;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;
import com.pg85.otg.util.minecraft.BiomeRegistryNames;

/**
 * A base class for a platform-specific preset loader, which loads 
 * all presets from disk when the OTG Engine is started at app start, 
 * and registers all biomes with their worldconfig/biomeconfig settings.
 */
public abstract class LocalPresetLoader
{	
	private static final int MAX_INHERITANCE_DEPTH = 15;
	protected final Object materialReaderLock = new Object();
	protected final File presetsDir;
	protected final HashMap<String, Preset> presets = new HashMap<>();
	protected final HashMap<String, String> aliasMap = new HashMap<>();
	protected HashMap<String, IMaterialReader> materialReaderByPresetFolderName = new HashMap<>();

	public LocalPresetLoader(Path otgRootFolder)
	{
		this.presetsDir = Paths.get(otgRootFolder.toString(), File.separator + Constants.PRESETS_FOLDER).toFile();
	}

	public IMaterialReader getMaterialReader(String presetFolderName)
	{
		IMaterialReader materialReader;
		synchronized(this.materialReaderLock)
		{
			materialReader = this.materialReaderByPresetFolderName.get(presetFolderName);
			if(materialReader == null)
			{
				materialReader = createMaterialReader();
				this.materialReaderByPresetFolderName.put(presetFolderName, materialReader);
			}
		}
		return materialReader;
	}

	// Creates a preset-specific materialreader, have to do this
	// only when loading each preset since each preset may have
	// its own block fallbacks / block dictionaries.
	protected abstract IMaterialReader createMaterialReader();

	public abstract void registerBiomes();
	
	protected abstract void mergeVanillaBiomeMobSpawnSettings(BiomeConfigStub biomeConfigStub, String inheritMobsBiomeName);
	
	public Preset getPresetByShortNameOrFolderName(String name)
	{
		// Example: preset is stored as "Biome Bundle v7", but also accepts "Biome Bundle"
		if (aliasMap.containsKey(name))
		{
			return this.presets.get(aliasMap.get(name));
		}
		return this.presets.get(name);
	}
	
	public Preset getPresetByFolderName(String name)
	{
		return this.presets.get(name);
	}

	public ArrayList<Preset> getAllPresets()
	{
		return new ArrayList<Preset>(presets.values());
	}

	public Set<String> getAllPresetFolderNames()
	{
		return presets.keySet();
	}
	
	public String getDefaultPresetFolderName()
	{
		return this.presets.keySet().size() == 0 ? Constants.DEFAULT_PRESET_NAME : this.presets.keySet().contains(Constants.DEFAULT_PRESET_NAME) ? Constants.DEFAULT_PRESET_NAME : (String) this.presets.keySet().toArray()[0];
	}
		
	public void loadPresetsFromDisk(IConfigFunctionProvider biomeResourcesManager, ILogger logger)
	{
		if(this.presetsDir.exists() && this.presetsDir.isDirectory())
		{
			for(File presetDir : this.presetsDir.listFiles())
			{
				if(presetDir.isDirectory())
				{
					for(File file : presetDir.listFiles())
					{
						if(file.getName().equals(Constants.WORLD_CONFIG_FILE))
						{
							Preset preset = loadPreset(presetDir.toPath(), biomeResourcesManager, logger);
							this.presets.put(preset.getFolderName(), preset);
							this.aliasMap.put(preset.getShortPresetName(), preset.getFolderName());
							break;
						}
					}
				}
			}
		}
	}
	
	protected Preset loadPreset(Path presetDir, IConfigFunctionProvider biomeResourcesManager, ILogger logger)
	{
		File worldConfigFile = new File(presetDir.toString(), Constants.WORLD_CONFIG_FILE);
		File biomesDirectory = new File(presetDir.toString(), Constants.WORLD_BIOMES_FOLDER);
		if(!biomesDirectory.exists())
		{
			biomesDirectory = new File(presetDir.toString(), Constants.LEGACY_WORLD_BIOMES_FOLDER);
		}
		String presetFolderName = presetDir.toFile().getName();
		
		SettingsMap worldConfigSettings = FileSettingsReader.read(presetFolderName, worldConfigFile, logger);
		WorldConfig worldConfig = new WorldConfig(presetDir, worldConfigSettings, addBiomesFromDirRecursive(biomesDirectory), biomeResourcesManager, logger, getMaterialReader(presetFolderName), presetFolderName);
		FileSettingsWriter.writeToFile(worldConfig.getSettingsAsMap(), worldConfigFile, worldConfig.getSettingsMode(), logger);

		// use shortPresetName to register the biomes, instead of presetName
		ArrayList<BiomeConfig> biomeConfigs = loadBiomeConfigs(worldConfig.getShortPresetName(), worldConfig.getMajorVersion(), presetDir, biomesDirectory.toPath(), worldConfig, biomeResourcesManager, logger, getMaterialReader(presetFolderName));

		return new Preset(presetDir, worldConfig.getShortPresetName(), worldConfig, biomeConfigs);
	}
	
	private ArrayList<String> addBiomesFromDirRecursive(File biomesDirectory)
	{
		ArrayList<String> biomes = new ArrayList<String>();
		if(biomesDirectory.exists())
		{
			for(File biomeConfig : biomesDirectory.listFiles())
			{
				if(biomeConfig.isFile() && biomeConfig.getName().endsWith(Constants.BiomeConfigFileExtension))
				{
					biomes.add(biomeConfig.getName().replace(Constants.BiomeConfigFileExtension, ""));
				}
				else if(biomeConfig.isDirectory())
				{
					biomes.addAll(addBiomesFromDirRecursive(biomeConfig));
				}
			}
		}
		return biomes;
	}

	private ArrayList<BiomeConfig> loadBiomeConfigs(String presetShortName, int presetMajorVersion, Path presetDir, Path presetBiomesDir, IWorldConfig worldConfig, IConfigFunctionProvider biomeResourcesManager, ILogger logger, IMaterialReader materialReader)
	{
		// Establish folders
		List<Path> biomeDirs = new ArrayList<Path>(2);
		biomeDirs.add(presetBiomesDir);
		
		// Load all files
		BiomeConfigFinder biomeConfigFinder = new BiomeConfigFinder();
		Map<String, BiomeConfigStub> biomeConfigStubs = biomeConfigFinder.findBiomes(worldConfig.getWorldBiomes(), worldConfig.getWorldHeightScale(), biomeDirs, logger, materialReader);

		// Read all settings
		ArrayList<BiomeConfig> biomeConfigs = readAndWriteSettings(worldConfig, biomeConfigStubs, presetDir, presetShortName, presetMajorVersion, true, biomeResourcesManager, logger, materialReader);

		// Update settings dynamically, these changes don't get written back to the file
		processSettings(worldConfig, biomeConfigs);

		if(logger.getLogCategoryEnabled(LogCategory.CONFIGS) && logger.canLogForPreset(presetDir.getFileName().toString()))
		{
			logger.log(
				LogLevel.INFO,
				LogCategory.CONFIGS,
				MessageFormat.format(
					"{0} biomes Loaded", 
					biomeConfigs.size()
				)
			);
			logger.log(
				LogLevel.INFO, 
				LogCategory.CONFIGS,
				biomeConfigs.stream().map(
					item -> item.getName()
				).collect(
					Collectors.joining(", ")
				)
			);
		}
		return biomeConfigs;
	}

	private ArrayList<BiomeConfig> readAndWriteSettings(IWorldConfig worldConfig, Map<String, BiomeConfigStub> biomeConfigStubs, Path presetDir, String presetShortName, int presetMajorVersion, boolean write, IConfigFunctionProvider biomeResourcesManager, ILogger logger, IMaterialReader materialReader)
	{
		ArrayList<BiomeConfig> biomeConfigs = new ArrayList<BiomeConfig>();

		for (BiomeConfigStub biomeConfigStub : biomeConfigStubs.values())
		{
			// Inheritance
			processMobInheritance(biomeConfigStubs, biomeConfigStub, 0, logger);

			// Settings reading
			BiomeConfig biomeConfig = new BiomeConfig(biomeConfigStub.getBiomeName(), biomeConfigStub, presetDir, biomeConfigStub.getSettings(), worldConfig, presetShortName, presetMajorVersion, biomeResourcesManager, logger, materialReader);
			biomeConfigs.add(biomeConfig);

			// Settings writing
			if(write)
			{
				Path writeFile = biomeConfigStub.getPath();
				FileSettingsWriter.writeToFile(biomeConfig.getSettingsAsMap(), writeFile.toFile(), worldConfig.getSettingsMode(), logger);
			}
		}

		return biomeConfigs;
	}

	private void processSettings(IWorldConfig worldConfig, ArrayList<BiomeConfig> biomeConfigs)
	{
		for(BiomeConfig biomeConfig : biomeConfigs)
		{
			// Index ReplacedBlocks
			if (!worldConfig.getBiomeConfigsHaveReplacement())
			{
				worldConfig.setBiomeConfigsHaveReplacement(biomeConfig.hasReplaceBlocksSettings());
			}

			// Index maxSmoothRadius
			if (worldConfig.getMaxSmoothRadius() < biomeConfig.getSmoothRadius())
			{
				worldConfig.setMaxSmoothRadius(biomeConfig.getSmoothRadius());
			}
			if (worldConfig.getMaxSmoothRadius() < biomeConfig.getCHCSmoothRadius())
			{
				worldConfig.setMaxSmoothRadius(biomeConfig.getCHCSmoothRadius());
			}
		}
	}

	private void processMobInheritance(Map<String, BiomeConfigStub> biomeConfigStubs, BiomeConfigStub biomeConfigStub, int currentDepth, ILogger logger)
	{
		if (biomeConfigStub.inheritMobsBiomeNameProcessed)
		{
			// Already processed
			return;
		}

		String stubInheritMobsBiomeName = biomeConfigStub.getSettings().getSetting(BiomeStandardValues.INHERIT_MOBS_BIOME_NAME, BiomeStandardValues.INHERIT_MOBS_BIOME_NAME.getDefaultValue(), logger, null);

		if(stubInheritMobsBiomeName != null && stubInheritMobsBiomeName.length() > 0)
		{
			String[] inheritMobsBiomeNames = stubInheritMobsBiomeName.split(",");
			for(String inheritMobsBiomeName : inheritMobsBiomeNames)
			{
				if (inheritMobsBiomeName.isEmpty())
				{
					// Not extending anything
					continue;
				}

				// This biome inherits mobs from another biome
				BiomeConfigStub inheritMobsBiomeConfig = biomeConfigStubs.get(inheritMobsBiomeName);

				if (inheritMobsBiomeConfig == null || inheritMobsBiomeConfig == biomeConfigStub) // Most likely a legacy config that is not using resourcelocation yet, for instance: Plains instead of minecraft:plains. Try to convert.
				{
					String vanillaBiomeName = BiomeRegistryNames.getRegistryNameForDefaultBiome(inheritMobsBiomeName);
					if(vanillaBiomeName != null)
					{
						inheritMobsBiomeConfig = null;
						inheritMobsBiomeName = vanillaBiomeName;
					}
					else if(inheritMobsBiomeConfig == biomeConfigStub)
					{
						if(logger.getLogCategoryEnabled(LogCategory.MOBS))
						{
							logger.log(
								LogLevel.ERROR,
								LogCategory.MOBS,
								MessageFormat.format("The biome {0} tried to inherit mobs from itself.", biomeConfigStub.getBiomeName())
							);
						}
						continue;
					}
				}

				// Check for too much recursion
				if (currentDepth > MAX_INHERITANCE_DEPTH)
				{
					if(logger.getLogCategoryEnabled(LogCategory.MOBS))
					{
						logger.log(
							LogLevel.ERROR,
							LogCategory.MOBS,
							MessageFormat.format(
								"The biome {0} cannot inherit mobs from biome {1} - too many configs processed already! Cyclical inheritance?", 
								biomeConfigStub.getPath().toFile().getName(), 
								inheritMobsBiomeConfig.getPath().toFile().getName()
							)
						);
					}
				}

				if(inheritMobsBiomeConfig != null)
				{
					if (!inheritMobsBiomeConfig.inheritMobsBiomeNameProcessed)
					{
						// This biome has not been processed yet, do that first
						processMobInheritance(biomeConfigStubs, inheritMobsBiomeConfig, currentDepth + 1, logger);
					}

					// Merge the two
					biomeConfigStub.mergeMobs(inheritMobsBiomeConfig);
				} else {

					// This is a vanilla biome or a biome added by another mod.
					mergeVanillaBiomeMobSpawnSettings(biomeConfigStub, inheritMobsBiomeName);
					continue;
				}
			}

			// Done
			biomeConfigStub.inheritMobsBiomeNameProcessed = true;
		}
	}
}
