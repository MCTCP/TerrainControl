package com.pg85.otg.config.biome;

import com.pg85.otg.config.io.FileSettingsReader;
import com.pg85.otg.config.io.SettingsMap;
import com.pg85.otg.config.standard.BiomeStandardValues;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.util.biome.WeightedMobSpawnGroup;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;
import com.pg85.otg.util.minecraft.EntityCategory;

import java.io.File;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.*;

/**
 * This class searches for the appropriate file for each biome.
 * You give it a list of folders to search in, and it will find the location of
 * all BiomeConfigs. Files will be created for non-existing BiomeConfigs.
 * 
 */
public final class BiomeConfigFinder
{
	/**
	 * Constructs a new biome loader.
	 *
	 */
	public BiomeConfigFinder() { }

	/**
	 * Finds the biomes in the given directories.
	 * 
	 * @param worldBiomes The biomes to load.
	 * @param directories The directories to search in.
	 *
	 * @return A map of biome name --> location on disk.
	 */
	public Map<String, BiomeConfigStub> findBiomes(List<String> worldBiomes, int worldHeightScale, Collection<Path> directories, ILogger logger, IMaterialReader materialReader)
	{
		Map<String, BiomeConfigStub> biomeConfigsStore = new HashMap<String, BiomeConfigStub>();

		// Search all directories
		for (Path directoryPath  : directories)
		{
			File directory = directoryPath.toFile();
			// Account for the possibility that folder creation failed
			if (directory.exists())
			{
				loadBiomesFromDirectory(worldBiomes, worldHeightScale, biomeConfigsStore, directory, logger, materialReader);
			}
		}
		
		return biomeConfigsStore;
	}

	/**
	 * Loads the biomes from the given directory.
	 * 
	 * @param biomeConfigsStore Map to store all the found biome configs in.
	 * @param directory		 The directory to load from.
	 * @param worldBiomes	The biomes that should still be loaded. When a
	 *						  biome is found, it is removed from this map.
	 */
	private void loadBiomesFromDirectory(List<String> worldBiomes, int worldHeightScale, Map<String, BiomeConfigStub> biomeConfigsStore, File directory, ILogger logger, IMaterialReader materialReader)
	{
		for (File file : directory.listFiles())
		{
			// Search recursively
			if (file.isDirectory())
			{
				loadBiomesFromDirectory(worldBiomes, worldHeightScale, biomeConfigsStore, file, logger, materialReader);
				continue;
			}

			// Extract name from filename
			String biomeName = toBiomeName(file);
			if (biomeName == null)
			{
				// Not a valid biome file
				continue;
			}
			
			// Load biomeconfig
			File renamedFile = renameBiomeFile(file, biomeName, logger);
			SettingsMap settings = FileSettingsReader.read(biomeName, renamedFile, logger);
			BiomeConfigStub biomeConfigStub = new BiomeConfigStub(settings, file.toPath(), biomeName, logger, materialReader);
			biomeConfigsStore.put(biomeName, biomeConfigStub);
		}
	}

	/**
	 * Tries to rename the config file so that it has the correct extension.
	 * Does nothing if the config file already has the correct extension. If
	 * the rename fails, a message is printed.
	 * 
	 * @param toRename The file that should be renamed.
	 * @param biomeName The biome that the file has settings for.
	 * @return The renamed file.
	 */
	private File renameBiomeFile(File toRename, String biomeName, ILogger logger)
	{
		String preferredFileName = toFileName(biomeName);
		if (toRename.getName().equalsIgnoreCase(preferredFileName))
		{
			// No need to rename
			return toRename;
		}

		// Wrong extension, rename
		File newFile = new File(toRename.getParentFile(), preferredFileName);
		if (toRename.renameTo(newFile))
		{
			return newFile;
		} else {
			if(logger.getLogCategoryEnabled(LogCategory.CONFIGS))
			{
				logger.log(
					LogLevel.ERROR,
					LogCategory.CONFIGS,
					MessageFormat.format(
						"Failed to rename biome file {0} to {1}",
						toRename.getAbsolutePath(), 
						newFile.getAbsolutePath()
					)
				);
			}
			return toRename;
		}
	}

	/**
	 * Extracts the biome name out of the file name.
	 * 
	 * @param file The file to extract the biome name out of.
	 * @return The biome name, or null if the file is not a biome config file.
	 */
	private String toBiomeName(File file)
	{
		String fileName = file.getName();
		for (String extension : BiomeStandardValues.BiomeConfigExtensions)
		{
			if (fileName.endsWith(extension))
			{
				return fileName.substring(0, fileName.lastIndexOf(extension));
			}
		}

		// Invalid file name
		return null;
	}

	/**
	 * Gets the name of the file the biome should be saved in. This will use
	 * the extension as defined in the PluginConfig.ini file.
	 * 
	 * @param biomeName The biome.
	 * @return The name of the file the biome should be saved in.
	 */
	private String toFileName(String biomeName)
	{
		return biomeName + Constants.BiomeConfigFileExtension;
	}

	/**
	 * A stub for a BiomeConfig. At this stage, the raw settings are
	 * already loaded. Setting reading must not start before the inheritance
	 * settings are processed.
	 */
	public static final class BiomeConfigStub
	{
		private final SettingsMap settings;
		private final Path file;
		private final String biomeName;
		
		// Mob inheritance has to be done before the configs are actually loaded
		// Unfortunately can't handle this like BiomeExtends so have to put these
		// here and pass them to the BiomeConfig when it is created
		
		public boolean inheritMobsBiomeNameProcessed = false;		

		private final Map<EntityCategory, List<WeightedMobSpawnGroup>> spawnGroups = new HashMap<>();

		private final Map<EntityCategory, List<WeightedMobSpawnGroup>> spawnGroupsMerged = new HashMap<>();
						
		private BiomeConfigStub(SettingsMap settings, Path file, String biomeName, ILogger logger, IMaterialReader materialReader)
		{
			super();
			this.settings = settings;
			this.file = file;
			this.biomeName = biomeName;
			
			// Load mob settings here so we can process mob inheritance before loading the BiomeConfigs.

			spawnGroups.put(EntityCategory.MONSTER, settings.getSetting(BiomeStandardValues.SPAWN_MONSTERS, Collections.emptyList(), logger, materialReader));
			spawnGroups.put(EntityCategory.CREATURE, settings.getSetting(BiomeStandardValues.SPAWN_CREATURES, Collections.emptyList(), logger, materialReader));
			spawnGroups.put(EntityCategory.AMBIENT, settings.getSetting(BiomeStandardValues.SPAWN_AMBIENT_CREATURES, Collections.emptyList(), logger, materialReader));
			spawnGroups.put(EntityCategory.UNDERGROUND_WATER_CREATURE, settings.getSetting(BiomeStandardValues.SPAWN_UNDERGROUND_WATER_CREATURES, Collections.emptyList(), logger, materialReader));
			spawnGroups.put(EntityCategory.WATER_CREATURE, settings.getSetting(BiomeStandardValues.SPAWN_WATER_CREATURES, Collections.emptyList(), logger, materialReader));
			spawnGroups.put(EntityCategory.WATER_AMBIENT, settings.getSetting(BiomeStandardValues.SPAWN_WATER_AMBIENT_CREATURES, Collections.emptyList(), logger, materialReader));
			spawnGroups.put(EntityCategory.MISC, settings.getSetting(BiomeStandardValues.SPAWN_MISC_CREATURES, Collections.emptyList(), logger, materialReader));

			for (EntityCategory category : EntityCategory.values())
			{
				this.spawnGroupsMerged.put(category, new ArrayList<>(this.spawnGroups.get(category)));
			}
		}
		
		public void mergeMobs(BiomeConfigStub parent)
		{
			for (EntityCategory category : EntityCategory.values())
			{
				mergeMobs(parent.spawnGroupsMerged.get(category), category);
			}
			inheritMobsBiomeNameProcessed = true;
		}
		
		public void mergeMobs(List<WeightedMobSpawnGroup> inheritedSpawnGroups, EntityCategory entityCategory)
		{
			// Inherit only mobs that do not appear in this biomes' list
			// This way a biome's mob spawn settings can override inherited settings.
			List<WeightedMobSpawnGroup> childSpawnGroups = spawnGroupsMerged.get(entityCategory);

			List<WeightedMobSpawnGroup> mergedSpawnList = new ArrayList<>(childSpawnGroups);
			if(inheritedSpawnGroups != null)
			{
				for(WeightedMobSpawnGroup inheritedGroup : inheritedSpawnGroups)
				{
					boolean bFound = false;
					for(WeightedMobSpawnGroup childSpawnGroup : childSpawnGroups)
					{
						// When using no resourcedomain in entity name, assume "minecraft:"
						String compareFrom = childSpawnGroup.getMob().toLowerCase().trim();
						String compareTo = inheritedGroup.getMob().toLowerCase().trim();
						if(compareFrom.startsWith("minecraft:"))
						{
							if(!compareTo.contains(":"))
							{
								compareFrom = compareFrom.replace("minecraft:", "");
							}
						} else {
							if(compareTo.startsWith("minecraft:"))
							{
								compareTo = compareTo.replace("minecraft:", "");
							}
						}
						if(compareFrom.equals(compareTo))
						{
							bFound = true;
							break;
						}
					}
					if(!bFound)
					{
						mergedSpawnList.add(inheritedGroup);
					}
				}
			}

			this.spawnGroupsMerged.put(entityCategory, mergedSpawnList);
		}

		/**
		 * Gets the file the biome is stored in.
		 * @return The file.
		 */
		public Path getPath()
		{
			return file;
		}

		/**
		 * Gets the settings for the biome.
		 * @return The settings.
		 */
		public SettingsMap getSettings()
		{
			return settings;
		}

		/**
		 * Gets the name of this biome.
		 * @return The name.
		 */
		public String getBiomeName()
		{
			return this.biomeName;
		}

		public Collection<? extends WeightedMobSpawnGroup> getSpawner(EntityCategory entityCategory)
		{
			return spawnGroups.get(entityCategory);
		}

		public Collection<? extends WeightedMobSpawnGroup> getSpawnerMerged(EntityCategory entityCategory)
		{
			return spawnGroupsMerged.get(entityCategory);
		}
	}
}
