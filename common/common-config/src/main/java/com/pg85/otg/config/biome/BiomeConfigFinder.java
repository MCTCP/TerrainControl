package com.pg85.otg.config.biome;

import com.pg85.otg.config.io.FileSettingsReader;
import com.pg85.otg.config.io.SettingsMap;
import com.pg85.otg.config.standard.BiomeStandardValues;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.biome.WeightedMobSpawnGroup;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.minecraft.EntityCategory;

import java.io.File;
import java.nio.file.Path;
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
     * @param directory         The directory to load from.
     * @param remainingBiomes   The biomes that should still be loaded. When a
     *                          biome is found, it is removed from this map.
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
     * @param biome The biome that the file has settings for.
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
    private String toFileName(String biomeName)
    {
        return biomeName + Constants.BiomeConfigFileExtension;
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
        private final String biomeName;
        
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
                        
        private BiomeConfigStub(SettingsMap settings, Path file, String biomeName, ILogger logger, IMaterialReader materialReader)
        {
            super();
            this.settings = settings;
            this.file = file;
            this.biomeName = biomeName;
            
            // Load mob settings here so we can process mob inheritance before loading the BiomeConfigs.
            
            // Apply default values only when no mob spawning settings are present in the config
            if(settings.hasSetting(BiomeStandardValues.SPAWN_MONSTERS))
            {
    	        this.spawnMonsters = settings.getSetting(BiomeStandardValues.SPAWN_MONSTERS, null, logger, materialReader);
    	        if(this.spawnMonsters == null)
    	        {
    	        	this.spawnMonsters = new ArrayList<WeightedMobSpawnGroup>();
    	        }
            } else {
        		this.spawnMonsters = BiomeStandardValues.SPAWN_MONSTERS.getDefaultValue();
            }

            if(settings.hasSetting(BiomeStandardValues.SPAWN_CREATURES))
            {
    	    	this.spawnCreatures = settings.getSetting(BiomeStandardValues.SPAWN_CREATURES, new ArrayList<WeightedMobSpawnGroup>(), logger, materialReader);
    	        if(this.spawnCreatures == null)
    	        {
    	        	this.spawnCreatures = new ArrayList<WeightedMobSpawnGroup>();
    	        }
            } else {
            	this.spawnCreatures = BiomeStandardValues.SPAWN_CREATURES.getDefaultValue();
            }

            if(settings.hasSetting(BiomeStandardValues.SPAWN_WATER_CREATURES))
            {
    	    	this.spawnWaterCreatures = settings.getSetting(BiomeStandardValues.SPAWN_WATER_CREATURES, new ArrayList<WeightedMobSpawnGroup>(), logger, materialReader);    	
    	        if(this.spawnWaterCreatures == null)
    	        {
    	        	this.spawnWaterCreatures = new ArrayList<WeightedMobSpawnGroup>();
    	        }
            } else {
            	this.spawnWaterCreatures = BiomeStandardValues.SPAWN_WATER_CREATURES.getDefaultValue();
            }
            
            if(settings.hasSetting(BiomeStandardValues.SPAWN_AMBIENT_CREATURES))
            {
    	    	this.spawnAmbientCreatures = settings.getSetting(BiomeStandardValues.SPAWN_AMBIENT_CREATURES, new ArrayList<WeightedMobSpawnGroup>(), logger, materialReader);
    	        if(this.spawnAmbientCreatures == null)
    	        {
    	        	this.spawnAmbientCreatures = new ArrayList<WeightedMobSpawnGroup>();
    	        }
            } else {
            	this.spawnAmbientCreatures = BiomeStandardValues.SPAWN_AMBIENT_CREATURES.getDefaultValue();
            }

            if(settings.hasSetting(BiomeStandardValues.SPAWN_WATER_AMBIENT_CREATURES))
            {
    	    	this.spawnWaterAmbientCreatures = settings.getSetting(BiomeStandardValues.SPAWN_WATER_AMBIENT_CREATURES, new ArrayList<WeightedMobSpawnGroup>(), logger, materialReader);
    	        if(this.spawnWaterAmbientCreatures == null)
    	        {
    	        	this.spawnWaterAmbientCreatures = new ArrayList<WeightedMobSpawnGroup>();
    	        }
            } else {
            	this.spawnWaterAmbientCreatures = BiomeStandardValues.SPAWN_WATER_AMBIENT_CREATURES.getDefaultValue();
            }
            
            if(settings.hasSetting(BiomeStandardValues.SPAWN_MISC_CREATURES))
            {
    	    	this.spawnMiscCreatures = settings.getSetting(BiomeStandardValues.SPAWN_MISC_CREATURES, new ArrayList<WeightedMobSpawnGroup>(), logger, materialReader);
    	        if(this.spawnMiscCreatures == null)
    	        {
    	        	this.spawnMiscCreatures = new ArrayList<WeightedMobSpawnGroup>();
    	        }
            } else {
            	this.spawnMiscCreatures = BiomeStandardValues.SPAWN_MISC_CREATURES.getDefaultValue();
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
