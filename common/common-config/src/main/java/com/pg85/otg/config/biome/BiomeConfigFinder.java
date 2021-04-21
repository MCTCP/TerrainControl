package com.pg85.otg.config.biome;

import com.pg85.otg.config.io.FileSettingsReader;
import com.pg85.otg.config.io.SettingsMap;
import com.pg85.otg.config.io.SimpleSettingsMap;
import com.pg85.otg.config.standard.BiomeStandardValues;
import com.pg85.otg.config.standard.MojangSettings.EntityCategory;
import com.pg85.otg.config.standard.StandardBiomeTemplate;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.biome.WeightedMobSpawnGroup;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.minecraft.BiomeRegistryNames;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * @param preferredBiomeFileExtension Biome files that do not exist yet
     *            are created with this extension.
     */
    public BiomeConfigFinder() { }

    /**
     * Finds the biomes in the given directories.
     * 
     * @param directories The directories to search in.
     * @param biomesToLoad The biomes to load.
     *
     * @return A map of biome name --> location on disk.
     */
    public Map<String, BiomeConfigStub> findBiomes(List<String> worldBiomes, int worldHeightScale, Collection<Path> directories, Collection<BiomeLoadInstruction> biomesToLoad, ILogger logger, IMaterialReader materialReader, Collection<? extends BiomeLoadInstruction> defaultBiomes)
    {
        Map<String, BiomeConfigStub> biomeConfigsStore = new HashMap<String, BiomeConfigStub>();

        // Switch to a Map<String, LocalBiome>
        Map<String, BiomeLoadInstruction> remainingBiomes = new HashMap<String, BiomeLoadInstruction>();
        for (BiomeLoadInstruction biome : biomesToLoad)
        {
            remainingBiomes.put(biome.getBiomeName(), biome);
        }

        // Search all directories
        for (Path directoryPath  : directories)
        {
        	File directory = directoryPath.toFile();
            // Account for the possibility that folder creation failed
            if (directory.exists())
            {
                loadBiomesFromDirectory(worldBiomes, worldHeightScale, biomeConfigsStore, directory, remainingBiomes, logger, materialReader, defaultBiomes);
            }
        }
        
        // Create all biomes that weren't loaded
        Path preferredDirectory = directories.iterator().next();
        for (BiomeLoadInstruction localBiome : remainingBiomes.values())
        {
        	Path newConfigFile = Paths.get(preferredDirectory.toString(), toFileName(localBiome));
            SettingsMap settings = new SimpleSettingsMap(localBiome.getBiomeName(), true);
            BiomeConfigStub biomeConfigStub = new BiomeConfigStub(settings, newConfigFile, localBiome, logger, materialReader);
            biomeConfigsStore.put(localBiome.getBiomeName(), biomeConfigStub);
        }

        return biomeConfigsStore;
    }

    /**
     * Loads the biomes from the given directory.
     * 
     * @param biomeConfigsStore Map to store all the found biome configs in.
     * @param directory         The directory to load from.
     * @param remainingBiomes   The biomes that should still be loaded. When a
     *                          biome is found, it is removed from this map.
     */
    private void loadBiomesFromDirectory(List<String> worldBiomes, int worldHeightScale, Map<String, BiomeConfigStub> biomeConfigsStore, File directory, Map<String, BiomeLoadInstruction> remainingBiomes, ILogger logger, IMaterialReader materialReader, Collection<? extends BiomeLoadInstruction> defaultBiomes)
    {
        for (File file : directory.listFiles())
        {
            // Search recursively
            if (file.isDirectory())
            {
                loadBiomesFromDirectory(worldBiomes, worldHeightScale, biomeConfigsStore, file, remainingBiomes, logger, materialReader, defaultBiomes);
                continue;
            }

            // Extract name from filename
            String biomeName = toBiomeName(file);
            if (biomeName == null)
            {
                // Not a valid biome file
                continue;
            }
                        
            // Load file with standard template to get replacetobiomename setting
            BiomeLoadInstruction preloadedBiome = new BiomeLoadInstruction(biomeName, new StandardBiomeTemplate(worldHeightScale));
            File preloadedRenamedFile = renameBiomeFile(file, preloadedBiome, logger);
            SettingsMap preloadedSettings = FileSettingsReader.read(biomeName, preloadedRenamedFile, logger);
            BiomeConfigStub preloadedBiomeConfigStub = new BiomeConfigStub(preloadedSettings, file.toPath(), preloadedBiome, logger, materialReader);
            
            // Get the correct LocalBiome, 
            
            // For legacy worlds that use the custombiomes list but don't have the vanilla biomes listed make sure the default biome templates are used. 
            // For newly created configs the default biomes have been added to remainingBiomes so those already use the correct templates. 
            BiomeLoadInstruction biome = remainingBiomes.get(biomeName);
            if (biome == null)
            {
            	// If a biome has replacetobiomename set to a vanilla biome and has the same name as the vanilla biome then it should use the vanilla biome's template. 
            	String replaceToBiomeName = preloadedBiomeConfigStub.settings.getSetting(BiomeStandardValues.VANILLA_BIOME, "", logger, materialReader);
            	if(replaceToBiomeName != null && replaceToBiomeName.length() > 0)
            	{
	                for (BiomeLoadInstruction defaultBiome : defaultBiomes)
	                {
	                	if(biomeName.equals(defaultBiome.getBiomeName()) && replaceToBiomeName.equals(BiomeRegistryNames.getRegistryNameForDefaultBiome(defaultBiome.getBiomeName())))
	                	{
	                		biome = new BiomeLoadInstruction(defaultBiome.getBiomeName(), defaultBiome.getBiomeTemplate());
	                		break;
	                	}
	                }
            	}
            	// If this is a new world using legacy configs then update the config by adding replaceToBiomeName              	
            	// If this biome is not listed in custombiomes, does not have replacetobiomename set and has the same name as a vanilla biome then
            	// assume it is a legacy config
        		else if(!worldBiomes.contains(biomeName)) 
    			{
	                for (BiomeLoadInstruction defaultBiome : defaultBiomes)
	                {
	                	if(biomeName.equals(defaultBiome.getBiomeName()))
	                	{
	                		biome = new BiomeLoadInstruction(defaultBiome.getBiomeName(), defaultBiome.getBiomeTemplate());
	                		break;
	                	}
	                }
            	}
            }
        	if(biome == null)
        	{
        		biome = new BiomeLoadInstruction(biomeName, new StandardBiomeTemplate(worldHeightScale));
        	}

            // Load biome and remove it from the to-do list
            File renamedFile = renameBiomeFile(file, biome, logger);
            SettingsMap settings = FileSettingsReader.read(biomeName, renamedFile, logger);
            BiomeConfigStub biomeConfigStub = new BiomeConfigStub(settings, file.toPath(), biome, logger, materialReader);
            biomeConfigsStore.put(biomeName, biomeConfigStub);
            
            if(remainingBiomes.containsKey(biome.getBiomeName()))
            {
            	remainingBiomes.remove(biome.getBiomeName());
            }
        }
    }

    /**
     * Tries to rename the config file so that it has the correct extension.
     * Does nothing if the config file already has the correct extension. If
     * the rename fails, a message is printed.
     * 
     * @param toRename The file that should be renamed.
     * @param biome The biome that the file has settings for.
     * @return The renamed file.
     */
    private File renameBiomeFile(File toRename, BiomeLoadInstruction biome, ILogger logger)
    {
        String preferredFileName = toFileName(biome);
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
        	logger.log(LogMarker.ERROR, "Failed to rename biome file {} to {}",
                    new Object[] {toRename.getAbsolutePath(), newFile.getAbsolutePath()});
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
                String biomeName = fileName.substring(0, fileName.lastIndexOf(extension));
                return biomeName;
            }
        }

        // Invalid file name
        return null;
    }

    /**
     * Gets the name of the file the biome should be saved in. This will use
     * the extension as defined in the PluginConfig.ini file.
     * 
     * @param biome The biome.
     * @return The name of the file the biome should be saved in.
     */
    private String toFileName(BiomeLoadInstruction biome)
    {
        return biome.getBiomeName() + Constants.BiomeConfigFileExtension;
    }

    /**
     * A stub for a {@link BiomeConfig}. At this stage, the raw settings are
     * already loaded. Setting reading must not start before the inheritance
     * settings are processed.
     */
    public final class BiomeConfigStub
    {
        private final SettingsMap settings;
        private final Path file;
        private final BiomeLoadInstruction loadInstructions;
        public boolean biomeExtendsProcessed = false;
        
        // Mob inheritance has to be done before the configs are actually loaded
        // Unfortunately can't handle this like BiomeExtends so have to put these
        // here and pass them to the BiomeConfig when it is created
        
        public boolean inheritMobsBiomeNameProcessed = false;        
       
        private List<WeightedMobSpawnGroup> spawnMonsters = new ArrayList<WeightedMobSpawnGroup>();
        private List<WeightedMobSpawnGroup> spawnCreatures = new ArrayList<WeightedMobSpawnGroup>();
        private List<WeightedMobSpawnGroup> spawnWaterCreatures = new ArrayList<WeightedMobSpawnGroup>();
        private List<WeightedMobSpawnGroup> spawnAmbientCreatures = new ArrayList<WeightedMobSpawnGroup>();
        private List<WeightedMobSpawnGroup> spawnWaterAmbientCreatures = new ArrayList<WeightedMobSpawnGroup>();
        private List<WeightedMobSpawnGroup> spawnMiscCreatures = new ArrayList<WeightedMobSpawnGroup>();
        
        private List<WeightedMobSpawnGroup> spawnMonstersMerged = new ArrayList<WeightedMobSpawnGroup>();
        private List<WeightedMobSpawnGroup> spawnCreaturesMerged = new ArrayList<WeightedMobSpawnGroup>();
        private List<WeightedMobSpawnGroup> spawnWaterCreaturesMerged = new ArrayList<WeightedMobSpawnGroup>();
        private List<WeightedMobSpawnGroup> spawnAmbientCreaturesMerged = new ArrayList<WeightedMobSpawnGroup>();
        private List<WeightedMobSpawnGroup> spawnWaterAmbientCreaturesMerged = new ArrayList<WeightedMobSpawnGroup>();
        private List<WeightedMobSpawnGroup> spawnMiscCreaturesMerged = new ArrayList<WeightedMobSpawnGroup>();
                        
        private BiomeConfigStub(SettingsMap settings, Path file, BiomeLoadInstruction loadInstructions, ILogger logger, IMaterialReader materialReader)
        {
            super();
            this.settings = settings;
            this.file = file;
            this.loadInstructions = loadInstructions;
            
            // Load mob settings here so we can process mob inheritance before loading the BiomeConfigs.
            
            StandardBiomeTemplate defaultSettings = loadInstructions.getBiomeTemplate();
            
            // Apply default values only when no mob spawning settings are present in the config
            if(settings.hasSetting(BiomeStandardValues.SPAWN_MONSTERS))
            {
    	        this.spawnMonsters = settings.getSetting(BiomeStandardValues.SPAWN_MONSTERS, null, logger, materialReader);
    	        if(this.spawnMonsters == null)
    	        {
    	        	this.spawnMonsters = new ArrayList<WeightedMobSpawnGroup>();
    	        }
            } else {
        		this.spawnMonsters = defaultSettings.defaultMonsters;
            }

            if(settings.hasSetting(BiomeStandardValues.SPAWN_CREATURES))
            {
    	    	this.spawnCreatures = settings.getSetting(BiomeStandardValues.SPAWN_CREATURES, new ArrayList<WeightedMobSpawnGroup>(), logger, materialReader);
    	        if(this.spawnCreatures == null)
    	        {
    	        	this.spawnCreatures = new ArrayList<WeightedMobSpawnGroup>();
    	        }
            } else {
            	this.spawnCreatures = defaultSettings.defaultCreatures;
            }

            if(settings.hasSetting(BiomeStandardValues.SPAWN_WATER_CREATURES))
            {
    	    	this.spawnWaterCreatures = settings.getSetting(BiomeStandardValues.SPAWN_WATER_CREATURES, new ArrayList<WeightedMobSpawnGroup>(), logger, materialReader);    	
    	        if(this.spawnWaterCreatures == null)
    	        {
    	        	this.spawnWaterCreatures = new ArrayList<WeightedMobSpawnGroup>();
    	        }
            } else {
            	this.spawnWaterCreatures = defaultSettings.defaultWaterCreatures;
            }
            
            if(settings.hasSetting(BiomeStandardValues.SPAWN_AMBIENT_CREATURES))
            {
    	    	this.spawnAmbientCreatures = settings.getSetting(BiomeStandardValues.SPAWN_AMBIENT_CREATURES, new ArrayList<WeightedMobSpawnGroup>(), logger, materialReader);
    	        if(this.spawnAmbientCreatures == null)
    	        {
    	        	this.spawnAmbientCreatures = new ArrayList<WeightedMobSpawnGroup>();
    	        }
            } else {
            	this.spawnAmbientCreatures = defaultSettings.defaultAmbientCreatures;
            }

            if(settings.hasSetting(BiomeStandardValues.SPAWN_WATER_AMBIENT_CREATURES))
            {
    	    	this.spawnWaterAmbientCreatures = settings.getSetting(BiomeStandardValues.SPAWN_WATER_AMBIENT_CREATURES, new ArrayList<WeightedMobSpawnGroup>(), logger, materialReader);
    	        if(this.spawnWaterAmbientCreatures == null)
    	        {
    	        	this.spawnWaterAmbientCreatures = new ArrayList<WeightedMobSpawnGroup>();
    	        }
            } else {
            	this.spawnWaterAmbientCreatures = defaultSettings.defaultWaterAmbientCreatures;
            }
            
            if(settings.hasSetting(BiomeStandardValues.SPAWN_MISC_CREATURES))
            {
    	    	this.spawnMiscCreatures = settings.getSetting(BiomeStandardValues.SPAWN_MISC_CREATURES, new ArrayList<WeightedMobSpawnGroup>(), logger, materialReader);
    	        if(this.spawnMiscCreatures == null)
    	        {
    	        	this.spawnMiscCreatures = new ArrayList<WeightedMobSpawnGroup>();
    	        }
            } else {
            	this.spawnMiscCreatures = defaultSettings.defaultMiscCreatures;
            }            
            
    		this.spawnMonstersMerged.addAll(this.spawnMonsters);
    		this.spawnCreaturesMerged.addAll(this.spawnCreatures);    		
    		this.spawnWaterCreaturesMerged.addAll(this.spawnWaterCreatures);
    		this.spawnAmbientCreaturesMerged.addAll(this.spawnAmbientCreatures);
    		this.spawnWaterAmbientCreaturesMerged.addAll(this.spawnWaterAmbientCreatures);
    		this.spawnMiscCreaturesMerged.addAll(this.spawnMiscCreatures);
        }
        
        public void mergeMobs(BiomeConfigStub parent)
        {
        	mergeMobs(parent.spawnMonstersMerged, EntityCategory.MONSTER);
        	mergeMobs(parent.spawnCreaturesMerged, EntityCategory.CREATURE);
        	mergeMobs(parent.spawnAmbientCreaturesMerged, EntityCategory.AMBIENT_CREATURE);
        	mergeMobs(parent.spawnWaterCreaturesMerged, EntityCategory.WATER_CREATURE);
        	mergeMobs(parent.spawnWaterAmbientCreaturesMerged, EntityCategory.WATER_AMBIENT);
        	mergeMobs(parent.spawnMiscCreaturesMerged, EntityCategory.MISC);

            inheritMobsBiomeNameProcessed = true;
        }
        
        public void mergeMobs(List<WeightedMobSpawnGroup> parentSpawnableMonsterList, EntityCategory entityCategory)
        {
        	// Inherit only mobs that do not appear in this biomes' list
        	// This way a biome's mob spawn settings can override inherited settings.
        	List<WeightedMobSpawnGroup> childSpawnableMonsterList = null;
        	switch(entityCategory)
        	{
	        	case MONSTER:
	        		childSpawnableMonsterList = this.spawnMonstersMerged;
	            	break;
	        	case CREATURE:
	        		childSpawnableMonsterList = this.spawnCreaturesMerged;
	        		break;
	        	case AMBIENT_CREATURE:
	        		childSpawnableMonsterList = this.spawnAmbientCreaturesMerged;        		
	        		break;
	        	case WATER_CREATURE:
	        		childSpawnableMonsterList = this.spawnWaterCreaturesMerged;        		
	        		break;
	        	case WATER_AMBIENT:
	        		childSpawnableMonsterList = this.spawnWaterAmbientCreaturesMerged;
	        		break;
	        	case MISC:
	        		childSpawnableMonsterList = this.spawnMiscCreaturesMerged;
	        		break;
        	}
        	
        	List<WeightedMobSpawnGroup> newSpawnableMobsList = new ArrayList<WeightedMobSpawnGroup>();
        	newSpawnableMobsList.addAll(childSpawnableMonsterList);
        	if(parentSpawnableMonsterList != null)
        	{
    	    	for(WeightedMobSpawnGroup weightedMobSpawnGroupParent : parentSpawnableMonsterList)
    	    	{
    	    		boolean bFound = false;
    	    		for(WeightedMobSpawnGroup weightedMobSpawnGroupChild : childSpawnableMonsterList)
    	    		{
        	    		// When using no resourcedomain in entity name, assume "minecraft:"
    	    			String compareFrom = weightedMobSpawnGroupChild.getMob().toLowerCase().trim();
    	    			String compareTo = weightedMobSpawnGroupParent.getMob().toLowerCase().trim();
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
    	    			newSpawnableMobsList.add(weightedMobSpawnGroupParent);
    	    		}
    	    	}
        	}
        	
        	switch(entityCategory)
        	{
	        	case MONSTER:
	        		this.spawnMonstersMerged = newSpawnableMobsList;
	            	break;
	        	case CREATURE:
	        		this.spawnCreaturesMerged = newSpawnableMobsList;        		
	        		break;
	        	case AMBIENT_CREATURE:
	        		this.spawnAmbientCreaturesMerged = newSpawnableMobsList;        		
	        		break;
	        	case WATER_CREATURE:
	        		this.spawnWaterCreaturesMerged = newSpawnableMobsList;        		
	        		break;
	        	case WATER_AMBIENT:
	        		this.spawnWaterAmbientCreaturesMerged = newSpawnableMobsList;
	        		break;
	        	case MISC:
	        		this.spawnMiscCreaturesMerged = newSpawnableMobsList;
	        		break;
				default:
					break;
        	}
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
         * Gets the instructions used for loading the biome.
         * @return The instructions.
         */
        public BiomeLoadInstruction getLoadInstructions()
        {
            return loadInstructions;
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
            return loadInstructions.getBiomeName();
        }

		public Collection<? extends WeightedMobSpawnGroup> getSpawner(EntityCategory entityCategory)
		{
        	switch(entityCategory)
        	{
	        	case MONSTER:
	        		return this.spawnMonsters;
	        	case CREATURE:
	        		return this.spawnCreatures;        		
	        	case AMBIENT_CREATURE:
	        		return this.spawnAmbientCreatures;        		
	        	case WATER_CREATURE:
	        		return this.spawnWaterCreatures;        		
	        	case WATER_AMBIENT:
	        		return this.spawnWaterAmbientCreatures;
	        	case MISC:
	        		return this.spawnMiscCreatures;
				default:
					break;
        	}			
			return null;
		}

		public Collection<? extends WeightedMobSpawnGroup> getSpawnerMerged(EntityCategory entityCategory)
		{
        	switch(entityCategory)
        	{
	        	case MONSTER:
	        		return this.spawnMonstersMerged;
	        	case CREATURE:
	        		return this.spawnCreaturesMerged;        		
	        	case AMBIENT_CREATURE:
	        		return this.spawnAmbientCreaturesMerged;        		
	        	case WATER_CREATURE:
	        		return this.spawnWaterCreaturesMerged;        		
	        	case WATER_AMBIENT:
	        		return this.spawnWaterAmbientCreaturesMerged;
	        	case MISC:
	        		return this.spawnMiscCreaturesMerged;
				default:
					break;
        	}			
			return null;
		}
    }
}
