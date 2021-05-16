package com.pg85.otg.presets;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import com.pg85.otg.OTG;
import com.pg85.otg.config.biome.BiomeConfig;
import com.pg85.otg.config.biome.BiomeConfigFinder;
import com.pg85.otg.config.biome.BiomeConfigFinder.BiomeConfigStub;
import com.pg85.otg.config.io.FileSettingsReader;
import com.pg85.otg.config.io.FileSettingsWriter;
import com.pg85.otg.config.io.IConfigFunctionProvider;
import com.pg85.otg.config.io.SettingsMap;
import com.pg85.otg.config.standard.BiomeStandardValues;
import com.pg85.otg.config.world.WorldConfig;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IWorldConfig;
import com.pg85.otg.util.minecraft.BiomeRegistryNames;

/**
 * A base class for a platform-specific preset loader, which loads 
 * all presets from disk when the OTG Engine is started at app start, 
 * and registers all biomes with their worldconfig/biomeconfig settings.
 */
public abstract class LocalPresetLoader
{
	private static final int MAX_INHERITANCE_DEPTH = 15;
	protected final File presetsDir;
	protected final HashMap<String, Preset> presets = new HashMap<String, Preset>();
	protected final HashMap<String, String> aliasMap = new HashMap<String, String>();

	public LocalPresetLoader(Path otgRootFolder)
	{
		this.presetsDir = Paths.get(otgRootFolder.toString(), File.separator + Constants.PRESETS_FOLDER).toFile();
	}

	public abstract void registerBiomes();

	public abstract BiomeConfig getBiomeConfig(String resourceLocationString);

	public abstract BiomeConfig getBiomeConfig(String presetName, int biomeId);	
		
	public Preset getPresetByName(String name)
	{
		// Example: preset is stored as "Biome Bundle v7", but also accepts "Biome Bundle"
		if (aliasMap.containsKey(name))
		{
			return this.presets.get(aliasMap.get(name));
		}
		return this.presets.get(name);
	}

	public ArrayList<Preset> getAllPresets()
	{
		return new ArrayList<Preset>(presets.values());
	}

	public Set<String> getAllPresetNames() {
		return presets.keySet();
	}
	
	public String getDefaultPresetName()
	{
		// TODO: Generate default preset on install
		return this.presets.keySet().size() > 0 ? (String) this.presets.keySet().toArray()[0] : Constants.DEFAULT_PRESET_NAME;
	}
		
	public void loadPresetsFromDisk(IConfigFunctionProvider biomeResourcesManager, boolean spawnLog, ILogger logger, IMaterialReader materialReader)
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
							Preset preset = loadPreset(presetDir.toPath(), biomeResourcesManager, spawnLog, logger, materialReader);
							presets.put(preset.getName(), preset);
							aliasMap.put(preset.getShortPresetName(), preset.getName());
							break;
						}
					}
				}
			}
		}
	}

	protected Preset loadPreset(Path presetDir, IConfigFunctionProvider biomeResourcesManager, boolean spawnLog, ILogger logger, IMaterialReader materialReader)
	{
		File worldConfigFile = new File(presetDir.toString(), Constants.WORLD_CONFIG_FILE);
		File biomesDirectory = new File(presetDir.toString(), Constants.WORLD_BIOMES_FOLDER);
		if(!biomesDirectory.exists())
		{
			biomesDirectory = new File(presetDir.toString(), Constants.LEGACY_WORLD_BIOMES_FOLDER);
		}
		String presetName = presetDir.toFile().getName();
		
		SettingsMap worldConfigSettings = FileSettingsReader.read(presetName, worldConfigFile, logger);
		WorldConfig worldConfig = new WorldConfig(presetDir, worldConfigSettings, addBiomesFromDirRecursive(biomesDirectory), biomeResourcesManager, spawnLog, logger, materialReader);
		FileSettingsWriter.writeToFile(worldConfig.getSettingsAsMap(), worldConfigFile, worldConfig.getSettingsMode(), logger);

		String shortPresetName = worldConfig.getShortPresetName();
		if (shortPresetName.isEmpty())
		{
			shortPresetName = presetName; // Set short name to preset name as a fallback
		} else {
			shortPresetName += worldConfig.getVersion().equals("0.0") ? "" : worldConfig.getVersion().split("\\.")[0];
		}

		// use shortPresetName to register the biomes, instead of presetName
		ArrayList<BiomeConfig> biomeConfigs = loadBiomeConfigs(shortPresetName, presetDir, biomesDirectory.toPath(), worldConfig, biomeResourcesManager, spawnLog, logger, materialReader);

		// We have to wait for the loading in order to get things like temperature
		// TODO: Re-implement this for 1.16
		//worldConfig.biomeGroupManager.processBiomeData();

		return new Preset(presetDir, presetName, shortPresetName, worldConfig, biomeConfigs);
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

	private ArrayList<BiomeConfig> loadBiomeConfigs(String presetName, Path presetDir, Path presetBiomesDir, IWorldConfig worldConfig, IConfigFunctionProvider biomeResourcesManager, boolean spawnLog, ILogger logger, IMaterialReader materialReader)
	{
		// Establish folders
		List<Path> biomeDirs = new ArrayList<Path>(2);
		biomeDirs.add(presetBiomesDir);
		
		// Load all files
		BiomeConfigFinder biomeConfigFinder = new BiomeConfigFinder();
		Map<String, BiomeConfigStub> biomeConfigStubs = biomeConfigFinder.findBiomes(worldConfig.getWorldBiomes(), worldConfig.getWorldHeightScale(), biomeDirs, logger, materialReader);

		// Read all settings
		ArrayList<BiomeConfig> biomeConfigs = readAndWriteSettings(worldConfig, biomeConfigStubs, presetDir, presetName, true, biomeResourcesManager, spawnLog, logger, materialReader);

		// Update settings dynamically, these changes don't get written back to the file
		processSettings(worldConfig, biomeConfigs, presetName);

		OTG.log(LogMarker.DEBUG, "{} biomes Loaded", biomeConfigs.size());
		OTG.log(LogMarker.DEBUG, "{}", biomeConfigs.stream().map(item -> item.getName()).collect(Collectors.joining(", ")));

		return biomeConfigs;
	}

	private static ArrayList<BiomeConfig> readAndWriteSettings(IWorldConfig worldConfig, Map<String, BiomeConfigStub> biomeConfigStubs, Path settingsDir, String presetName, boolean write, IConfigFunctionProvider biomeResourcesManager, boolean spawnLog, ILogger logger, IMaterialReader materialReader)
	{
		ArrayList<BiomeConfig> biomeConfigs = new ArrayList<BiomeConfig>();

		for (BiomeConfigStub biomeConfigStub : biomeConfigStubs.values())
		{
			// Inheritance
			processInheritance(biomeConfigStubs, biomeConfigStub, 0, logger);
			processMobInheritance(biomeConfigStubs, biomeConfigStub, 0, logger);

			// Settings reading
			BiomeConfig biomeConfig = new BiomeConfig(biomeConfigStub.getBiomeName(), biomeConfigStub, settingsDir, biomeConfigStub.getSettings(), worldConfig, presetName, biomeResourcesManager, spawnLog, logger, materialReader);
			biomeConfigs.add(biomeConfig);

			// Settings writing
			if(write)
			{
				Path writeFile = biomeConfigStub.getPath();
				if (!biomeConfig.getBiomeExtends().isEmpty())
				{
					writeFile = Paths.get(writeFile + ".inherited");
				}
				FileSettingsWriter.writeToFile(biomeConfig.getSettingsAsMap(), writeFile.toFile(), worldConfig.getSettingsMode(), logger);
			}
		}

		return biomeConfigs;
	}

	private void processSettings(IWorldConfig worldConfig, ArrayList<BiomeConfig> biomeConfigs, String presetName)
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

	private static void processInheritance(Map<String, BiomeConfigStub> biomeConfigStubs, BiomeConfigStub biomeConfigStub, int currentDepth, ILogger logger)
	{
		if (biomeConfigStub.biomeExtendsProcessed)
		{
			// Already processed
			return;
		}

		String extendedBiomeName = biomeConfigStub.getSettings().getSetting(BiomeStandardValues.BIOME_EXTENDS, logger, null);
		if (extendedBiomeName.isEmpty())
		{
			// Not extending anything
			biomeConfigStub.biomeExtendsProcessed = true;
			return;
		}

		// This biome extends another biome
		BiomeConfigStub extendedBiomeConfig = biomeConfigStubs.get(extendedBiomeName);
		if (extendedBiomeConfig == null)
		{
			OTG.log(LogMarker.WARN, 
				"The biome {} tried to extend the biome {}, but that biome doesn't exist.", 
				biomeConfigStub.getBiomeName(), extendedBiomeName);
			return;
		}

		// Check for too much recursion
		if (currentDepth > MAX_INHERITANCE_DEPTH)
		{
			OTG.log(LogMarker.FATAL,
				"The biome {} cannot extend the biome {} - too much configs processed already! Cyclical inheritance?",
				biomeConfigStub.getBiomeName(), extendedBiomeConfig.getBiomeName());
		}

		if (!extendedBiomeConfig.biomeExtendsProcessed)
		{
			// This biome has not been processed yet, do that first
			processInheritance(biomeConfigStubs, extendedBiomeConfig, currentDepth + 1, logger);
		}

		// Merge the two
		biomeConfigStub.getSettings().setFallback(extendedBiomeConfig.getSettings());

		// Done
		biomeConfigStub.biomeExtendsProcessed = true;
	}

	private static void processMobInheritance(Map<String, BiomeConfigStub> biomeConfigStubs, BiomeConfigStub biomeConfigStub, int currentDepth, ILogger logger)
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
						if(OTG.getEngine().getPluginConfig().getDeveloperModeEnabled())
						{
							OTG.log(LogMarker.WARN, "The biome {} tried to inherit mobs from itself.", new Object[] { biomeConfigStub.getBiomeName()});
						}
						continue;
					}
				}

				// Check for too much recursion
				if (currentDepth > MAX_INHERITANCE_DEPTH)
				{
					if(OTG.getEngine().getPluginConfig().getDeveloperModeEnabled())
					{
						OTG.log(LogMarker.FATAL, "The biome {} cannot inherit mobs from biome {} - too much configs processed already! Cyclical inheritance?", new Object[] { biomeConfigStub.getPath().toFile().getName(), inheritMobsBiomeConfig.getPath().toFile().getName()});
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
					OTG.getEngine().mergeVanillaBiomeMobSpawnSettings(biomeConfigStub, inheritMobsBiomeName);
					continue;
				}
			}

			// Done
			biomeConfigStub.inheritMobsBiomeNameProcessed = true;
		}
	}
}
