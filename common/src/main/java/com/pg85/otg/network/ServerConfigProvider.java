package com.pg85.otg.network;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.configuration.biome.BiomeConfigFinder;
import com.pg85.otg.configuration.biome.BiomeConfigFinder.BiomeConfigStub;
import com.pg85.otg.configuration.biome.BiomeLoadInstruction;
import com.pg85.otg.configuration.fallbacks.FallbackConfig;
import com.pg85.otg.configuration.io.FileSettingsReader;
import com.pg85.otg.configuration.io.FileSettingsWriter;
import com.pg85.otg.configuration.io.SettingsMap;
import com.pg85.otg.configuration.standard.BiomeStandardValues;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.configuration.standard.WorldStandardValues;
import com.pg85.otg.configuration.world.WorldConfig;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.BiomeIds;
import com.pg85.otg.util.helpers.FileHelper;
import com.pg85.otg.util.minecraft.defaults.BiomeRegistryNames;
import com.pg85.otg.util.minecraft.defaults.DefaultBiome;
import com.pg85.otg.worldsave.BiomeIdData;
import com.pg85.otg.worldsave.WorldSaveData;

/**
 * Holds the WorldConfig and all BiomeConfigs.
 *
 * <h3>A note about {@link LocalWorld} usage</h3>
 * <p>Currently, a {@link LocalWorld} instance is passed to the constructor of
 * this class. That is bad design. The plugin should be able to read the
 * settings and then create a world based on that. Now the world is created, and
 * then the settings are injected. It is also strange that the configuration
 * code is now able to spawn a cow, to give one example.</p>
 *
 * <p>Fixing that will be a lot of work - {@link LocalWorld} is currently a God
 * class that is required everywhere. If a rewrite of that class is ever
 * planned, be sure to split that class up!</p>
 */
public final class ServerConfigProvider implements ConfigProvider
{
    private static final int MAX_INHERITANCE_DEPTH = 15;
    private LocalWorld world;
    private File settingsDir;
    private WorldConfig worldConfig;

    /**
     * Holds all biome configs. Generation Id => BiomeConfig
     * <p>
     * Must be simple array for fast access. Warning: some ids may contain
     * null values, always check.
     */
    private LocalBiome[] biomesByOTGId;   
    
    /**
     * The number of loaded biomes.
     */
    private int biomesCount;

    /**
     * Loads the settings from the given directory for the given world.
     * @param settingsDir The directory to load from.
     * @param world       The world to load the settings for.
     */
    public ServerConfigProvider(File settingsDir, LocalWorld world, File worldSaveFolder)
    {
        this.settingsDir = settingsDir;
        this.world = world;
        this.biomesByOTGId = new LocalBiome[world.getMaxBiomesCount()];
        
        loadSettings(worldSaveFolder, false);
    }

    @Override
    public WorldConfig getWorldConfig()
    {
        return worldConfig;
    }
    
    @Override
    public LocalBiome getBiomeByOTGIdOrNull(int id)
    {
        if (id < 0 || id > biomesByOTGId.length)
        {
            return null;
        }
        return biomesByOTGId[id];
    }

    private int getRequestedSavedId(String resourceLocation)
    {
    	return world.getRegisteredBiomeId(resourceLocation);
    }

    @Override
    public LocalBiome[] getBiomeArrayByOTGId()
    {
        return this.biomesByOTGId;
    }
    
    /**
     * For pre-v7 worlds: Gets the generation id that the given biome should have, based on
     * {@link DefaultBiome the default biomes} and
     * {@link WorldConfig#customBiomeGenerationIds the CustomBiomes setting}.
     * @param biomeConfig The biome.
     * @return The preferred generation id.
     */
    private int getRequestedGenerationId(BiomeConfig biomeConfig)
    {
        Integer requestedGenerationId = DefaultBiome.getId(biomeConfig.getName());
        if (requestedGenerationId == null)
        {
            requestedGenerationId = biomeConfig.worldConfig.customBiomeGenerationIds.get(biomeConfig.getName());
        }
        // For v7 and later worlds, worldConfig.customBiomeGenerationIds isn't used.
        if (requestedGenerationId == null)
        {
        	return -1;
        }
        return requestedGenerationId;
    }
    
    /**
     * Loads all settings. Expects the biomes array to be empty (filled with
     * nulls), the savedBiomes collection to be empty and the biomesCount
     * field to be zero.
     */
    private void loadSettings(File worldSaveFolder, boolean isReload)
    {   	
        SettingsMap worldConfigSettings = loadAndInitWorldConfig();
        loadFallbacks();
        loadBiomes(worldConfigSettings, worldSaveFolder, isReload);

        // We have to wait for the loading in order to get things like
        // temperature
        worldConfig.biomeGroupManager.processBiomeData(world);
    }
    
    private void loadFallbacks() {
        File fallbackFile = new File(settingsDir, WorldStandardValues.FALLBACK_FILE_NAME);
        SettingsMap settingsMap = FileSettingsReader.read(world.getName(), fallbackFile);   

        FallbackConfig fallbacks = new FallbackConfig(settingsMap);

       this.worldConfig.addWorldFallbacks(fallbacks);
       FileSettingsWriter.writeToFile(fallbacks.getSettingsAsMap(), fallbackFile, OTG.getPluginConfig().settingsMode);
    }

    private SettingsMap loadAndInitWorldConfig()
    {
        File worldConfigFile = new File(settingsDir, WorldStandardValues.WORLD_CONFIG_FILE_NAME);
        SettingsMap settingsMap = FileSettingsReader.read(world.getName(), worldConfigFile);

    	ArrayList<String> biomes = new ArrayList<String>();
    	File biomesDirectory = new File(settingsDir, WorldStandardValues.WORLD_BIOMES_DIRECTORY_NAME); 	

    	addBiomesFromDirRecursive(biomes, biomesDirectory);
    	
        this.worldConfig = new WorldConfig(settingsDir, settingsMap, world, biomes);
        FileSettingsWriter.writeToFile(worldConfig.getSettingsAsMap(), worldConfigFile, worldConfig.settingsMode);

        return settingsMap;
    }
    
    private void addBiomesFromDirRecursive(ArrayList<String> biomes, File biomesDirectory)
    {
    	if(biomesDirectory.exists())
    	{
	    	for(File biomeConfig : biomesDirectory.listFiles())
	    	{
	    		if(biomeConfig.isFile() && biomeConfig.getName().endsWith(BiomeStandardValues.BIOME_CONFIG_EXTENSION.getDefaultValue()))
	    		{
	    			biomes.add(biomeConfig.getName().replace(BiomeStandardValues.BIOME_CONFIG_EXTENSION.getDefaultValue(), ""));
	    		}
	    		else if(biomeConfig.isDirectory())
	    		{
	    			addBiomesFromDirRecursive(biomes, biomeConfig);
	    		}
	    	}
    	}
    }

    private void loadBiomes(SettingsMap worldConfigSettings, File worldSaveFolder, boolean isReload)
    {
        // Establish folders
        List<File> biomeDirs = new ArrayList<File>(2);
        // OpenTerrainGenerator/Presets/<WorldName>/<WorldBiomes/
        biomeDirs.add(new File(settingsDir, OTG.correctOldBiomeConfigFolder(settingsDir)));
        // OpenTerrainGenerator/GlobalBiomes/
        biomeDirs.add(new File(OTG.getEngine().getOTGRootFolder(), PluginStandardValues.BiomeConfigDirectoryName));

        FileHelper.makeFolders(biomeDirs);

        // Build a set of all biomes to load
        Collection<BiomeLoadInstruction> biomesToLoad = new HashSet<BiomeLoadInstruction>();

        // If we're creating a new world with new configs then add the default biomes
        if(worldConfigSettings.isNewConfig())
        {
        	Collection<? extends BiomeLoadInstruction> defaultBiomes = OTG.getEngine().getDefaultBiomes();
            for (BiomeLoadInstruction defaultBiome : defaultBiomes)
            {
            	this.worldConfig.worldBiomes.add(defaultBiome.getBiomeName());
        		biomesToLoad.add(new BiomeLoadInstruction(defaultBiome.getBiomeName(), defaultBiome.getBiomeTemplate()));
            }
        }
        
        // Load all files
        BiomeConfigFinder biomeConfigFinder = new BiomeConfigFinder(OTG.getPluginConfig().biomeConfigExtension);
        Map<String, BiomeConfigStub> biomeConfigStubs = biomeConfigFinder.findBiomes(this.worldConfig, this.worldConfig.worldHeightScale, biomeDirs, biomesToLoad);
        
        // Read all settings
        Map<String, BiomeConfig> loadedBiomes = readAndWriteSettings(this.worldConfig, biomeConfigStubs, true);
        
        // Index all necessary settings
        String loadedBiomeNames = indexSettings(this.worldConfig.customBiomeGenerationIds, worldConfigSettings.isNewConfig(), loadedBiomes, worldSaveFolder, isReload);

        OTG.log(LogMarker.DEBUG, "{} biomes Loaded", biomesCount);
        OTG.log(LogMarker.DEBUG, "{}", loadedBiomeNames);
    }    

    @Override
    public void reload()
    {
        // Clear biome collections
        Arrays.fill(this.biomesByOTGId, null);
        this.biomesCount = 0;

        // Load again
        loadSettings(this.world.getWorldSaveDir(), true);
    }

    public static Map<String, BiomeConfig> readAndWriteSettings(WorldConfig worldConfig, Map<String, BiomeConfigStub> biomeConfigStubs, boolean write)
    {
        Map<String, BiomeConfig> loadedBiomes = new HashMap<String, BiomeConfig>();

        for (BiomeConfigStub biomeConfigStub : biomeConfigStubs.values())
        {
            // Allow to let world settings influence biome settings
            //biomeConfigStub.getSettings().setFallback(worldConfigSettings); // TODO: Make sure this can be removed safely

            // Inheritance
            processInheritance(biomeConfigStubs, biomeConfigStub, 0);
            processMobInheritance(biomeConfigStubs, biomeConfigStub, 0);

            // Settings reading
            BiomeConfig biomeConfig = new BiomeConfig(biomeConfigStub.getLoadInstructions(), biomeConfigStub, biomeConfigStub.getSettings(), worldConfig);
            loadedBiomes.put(biomeConfigStub.getBiomeName(), biomeConfig);

            // Settings writing
            if(write)
            {
	            File writeFile = biomeConfigStub.getFile();
	            if (!biomeConfig.biomeExtends.isEmpty())
	            {
	                writeFile = new File(writeFile.getAbsolutePath() + ".inherited");
	            }
	            FileSettingsWriter.writeToFile(biomeConfig.getSettingsAsMap(), writeFile, worldConfig.settingsMode);
            }
        }

        return loadedBiomes;
    }
    
    private static void processInheritance(Map<String, BiomeConfigStub> biomeConfigStubs, BiomeConfigStub biomeConfigStub, int currentDepth)
    {
        if (biomeConfigStub.biomeExtendsProcessed)
        {
            // Already processed
            return;
        }

        String extendedBiomeName = biomeConfigStub.getSettings().getSetting(BiomeStandardValues.BIOME_EXTENDS);
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
            OTG.log(LogMarker.WARN, "The biome {} tried to extend the biome {}, but that biome doesn't exist.", biomeConfigStub.getBiomeName(), extendedBiomeName);
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
            processInheritance(biomeConfigStubs, extendedBiomeConfig, currentDepth + 1);
        }

        // Merge the two
        biomeConfigStub.getSettings().setFallback(extendedBiomeConfig.getSettings());

        // Done
        biomeConfigStub.biomeExtendsProcessed = true;
    }

    private static void processMobInheritance(Map<String, BiomeConfigStub> biomeConfigStubs, BiomeConfigStub biomeConfigStub, int currentDepth)
    {
        if (biomeConfigStub.inheritMobsBiomeNameProcessed)
        {
            // Already processed
            return;
        }

        String stubInheritMobsBiomeName = biomeConfigStub.getSettings().getSetting(BiomeStandardValues.INHERIT_MOBS_BIOME_NAME, biomeConfigStub.getLoadInstructions().getBiomeTemplate().defaultInheritMobsBiomeName);

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
			            OTG.log(LogMarker.WARN, "The biome {} tried to inherit mobs from itself.", new Object[] { biomeConfigStub.getBiomeName()});
			            continue;
		        	}
		        }
		        
		        // Check for too much recursion
		        if (currentDepth > MAX_INHERITANCE_DEPTH)
		        {
		            OTG.log(LogMarker.FATAL, "The biome {} cannot inherit mobs from biome {} - too much configs processed already! Cyclical inheritance?", new Object[] { biomeConfigStub.getFile().getName(), inheritMobsBiomeConfig.getFile().getName()});
		        }

		        if(inheritMobsBiomeConfig != null)
		        {
			        if (!inheritMobsBiomeConfig.inheritMobsBiomeNameProcessed)
			        {
			            // This biome has not been processed yet, do that first
			            processMobInheritance(biomeConfigStubs, inheritMobsBiomeConfig, currentDepth + 1);
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
    
    // TODO: This sorts, updates and registers biomes, saves biome id data and creates OTG biomes. 
    // This is used when creating new worlds and dims, and when loading them.
    // This has to work for modern (>v7) and legacy (<v7) worlds, that use different
    // rules for OTG biome ids and saved id's. 
    // This method does too much and handles too many different situations, split it up!
    // TODO: Drop support for legacy worlds for 1.13/1.14 and clean this up.
    private String indexSettings(Map<String, Integer> worldBiomes, boolean isNewWorldConfig, Map<String, BiomeConfig> loadedBiomes, File worldSaveFolder, boolean isReload)
    {
        StringBuilder loadedBiomeNames = new StringBuilder();
       
        ArrayList<BiomeConfig> nonVirtualBiomesExisting = new ArrayList<BiomeConfig>();    
        ArrayList<BiomeConfig> nonVirtualBiomes = new ArrayList<BiomeConfig>();
        ArrayList<BiomeConfig> virtualBiomesExisting = new ArrayList<BiomeConfig>();
        ArrayList<BiomeConfig> virtualBiomes = new ArrayList<BiomeConfig>();
                      
        // If this is a previously created world then load the biome id data and register biomes to the same OTG biome id as before.
        ArrayList<BiomeIdData> loadedBiomeIdData = BiomeIdData.loadBiomeIdData(worldSaveFolder);
        boolean hasWorldData = loadedBiomeIdData != null;
        if(hasWorldData) 
        {
        	boolean bFound = false;
            // When creating a world with dims, the worlddata is saved after the overworld is generated,
            // so when the dims are created, worlddata already exists, even though the dim's biomes haven't
        	// been registered yet.
        	for(BiomeIdData biomeIdData : loadedBiomeIdData)
        	{
    			if(biomeIdData.biomeName.startsWith(world.getName() + "_"))
    			{
    				bFound = true;
    				break;
    			}
        	}
        	hasWorldData = bFound;
        	if(!hasWorldData)
        	{
        		loadedBiomeIdData = null;
        	}
        }

        // Update configs with resourcelocation names for default biomes
        //if(!hasWorldData)
        {
        	for(Entry<String, BiomeConfig> entry : loadedBiomes.entrySet())
        	{
	        	// Update biomes for legacy worlds, default biomes should be referred to as minecraft:<biomename>
	        	if(
        			entry.getValue().replaceToBiomeName != null && 
					entry.getValue().replaceToBiomeName.trim().length() > 0	        			
				)
	        	{
	        		String defaultBiomeResourceLocation = BiomeRegistryNames.getRegistryNameForDefaultBiome(entry.getValue().replaceToBiomeName);
	        		if(defaultBiomeResourceLocation != null)
	        		{
	        			entry.getValue().replaceToBiomeName = defaultBiomeResourceLocation;
	        		}
	        	} else {
	        		// Default biomes must replacetobiomename themselves
	        		String defaultBiomeResourceLocation = BiomeRegistryNames.getRegistryNameForDefaultBiome(entry.getValue().getName());
	        		if(defaultBiomeResourceLocation != null)
	        		{
	        			entry.getValue().replaceToBiomeName = defaultBiomeResourceLocation;
	        		}
	        	}
        	}
        }
        
        List<BiomeConfig> loadedBiomeList = new ArrayList<BiomeConfig>(loadedBiomes.values());
        
        // For backwards compatibility load custom biomes from the world config
        // A world created with a previous version of OTG may not have worlddata
        if(!OTG.IsNewWorldBeingCreated && !hasWorldData && worldBiomes.size() > 0)
        {
        	loadedBiomeIdData = new ArrayList<BiomeIdData>();
	        for(Entry<String, Integer> worldBiome : worldBiomes.entrySet())
	        {
	        	BiomeConfig biomeConfig = loadedBiomes.get(worldBiome.getKey());
	        	
	        	loadedBiomeIdData.add(
        			new BiomeIdData(
    					world.getName() + "_" + worldBiome.getKey(), 
    					worldBiome.getValue(), 
    					worldBiome.getValue() > 255 || (
							biomeConfig.replaceToBiomeName != null && 
							biomeConfig.replaceToBiomeName.trim().length() > 0
						) ? -1 : worldBiome.getValue()
					)
    			);
	        }
        }
        
        // Get OTG world save version
        WorldSaveData worldSaveData = WorldSaveData.loadWorldSaveData(worldSaveFolder);
        
        // This is a legacy (pre-v7) world if its not being created and either has no worldsavedata or worldsavedata version 6. 
        // If this world has biome data but not worldsavedata, it's v7.
        // This ignores legacy worlds updated by v7 and earlier v8 versions unfortunately (though very few ppl should be affected).
        //boolean isLegacyWorld = !OTG.IsNewWorldBeingCreated && (!hasWorldData || (worldSaveData != null && worldSaveData.version == 6));        
        boolean isLegacyWorld = (!hasWorldData || (worldSaveData != null && worldSaveData.version == 6));
        if(worldSaveData == null)
        {
        	worldSaveData = new WorldSaveData(isLegacyWorld ? 6 : 8);
            WorldSaveData.saveWorldSaveData(worldSaveFolder, worldSaveData);
        }
        
        // For backwards compatibility with legacy worlds, sort the biomes by generation id.
        // TODO: Should biome load order matter for the biome gen? Or even biome id's? Could use names or resourcelocations instead?
        if(isLegacyWorld)
        {
	        Collections.sort(loadedBiomeList, new Comparator<BiomeConfig>() {
	            @Override
	            public int compare(BiomeConfig a, BiomeConfig b) {
	                return getRequestedGenerationId(a) - getRequestedGenerationId(b);
	            }
	        });
        }
                
        List<BiomeConfig> usedBiomes = loadedBiomeList; 
        if(loadedBiomeIdData != null)
        {
            // For existing worlds, only register biomes that were previously registered/used, don't include new ones.
        	usedBiomes = new ArrayList<BiomeConfig>();
        	
        	// For legacy worlds that have no biomedata, but only custombiomes in the worldconfig,
        	// Add the default biomes (they were automatically included with every world pre-v7).
        	if(!hasWorldData && !OTG.IsNewWorldBeingCreated)
        	{
	        	Collection<? extends BiomeLoadInstruction> defaultBiomes = OTG.getEngine().getDefaultBiomes();
	            for (BiomeLoadInstruction defaultBiome : defaultBiomes)
	            {
	            	for(BiomeConfig biomeConfig : loadedBiomeList)
	            	{
	            		if(defaultBiome.getBiomeName().equals(biomeConfig.getName()))
	            		{
	            			usedBiomes.add(biomeConfig);
	            		}
	            	}
	            }
        	}
        	
        	for(BiomeIdData biomeIdData : loadedBiomeIdData)
        	{
        		if(biomeIdData.biomeName.startsWith(world.getName() + "_"))
        		{
            		for(BiomeConfig biomeConfig : loadedBiomeList)
            		{
            			if((world.getName() + "_" + biomeConfig.getName()).equals(biomeIdData.biomeName))
            			{
            				if(!usedBiomes.contains(biomeConfig))
            				{
            					usedBiomes.add(biomeConfig);
            				}
        	            	if(OTG.getEngine().isOTGBiomeIdAvailable(world.getName(), biomeIdData.otgBiomeId))
        	            	{
                				OTG.getEngine().setOTGBiomeId(world.getName(), biomeIdData.otgBiomeId, biomeConfig, false);        	            		
        	            	}
        	            	else if((world.getName() + "_" + OTG.getEngine().getOTGBiomeIds(world.getName())[biomeIdData.otgBiomeId].getName()).equals(biomeIdData.biomeName))
        	            	{
        	            		OTG.getEngine().setOTGBiomeId(world.getName(), biomeIdData.otgBiomeId, biomeConfig, true);
        	            	} else {
        	            		// The only time a biomeId can be claimed already is when an 
        	            		// unloaded world is being reloaded.
        	            		throw new RuntimeException("Error: OTG Biome id " + biomeIdData.otgBiomeId + " for biome " + biomeConfig.getName() + " was taken by " + OTG.getBiomeByOTGId(biomeIdData.otgBiomeId).getName());       	            		
        	            	}
        	            	
            	        	if(biomeIdData.otgBiomeId > -1 && biomeIdData.otgBiomeId < 256)
            	        	{
            	            	if(biomeConfig.replaceToBiomeName != null && biomeConfig.replaceToBiomeName.trim().length() > 0)
            	            	{
            	            		throw new RuntimeException("Error: Biome \"" + biomeConfig.getName() + "\" has an id between 0-255 but uses replaceToBiomeName. Virtual biomes must have id's above 255, please check your WorldConfig's custom biomes setting.");
            	            	}            	        		
            	        		nonVirtualBiomesExisting.add(biomeConfig);
            	        	}
            	        	else if(biomeIdData.otgBiomeId > 255)
            	        	{
            	        		virtualBiomesExisting.add(biomeConfig);
            	        	}  
            				break;
            			}
            		}
        		}
        	}
        }
        
        // Set OTG biome id's for biomes, make sure there is enough space to register all biomes.
        for (BiomeConfig biomeConfig : usedBiomes)
        {            	
            // Statistics of the loaded biomes
            this.biomesCount++;
            loadedBiomeNames.append(biomeConfig.getName());
            loadedBiomeNames.append(", ");

            BiomeConfig[] otgIds2 = OTG.getEngine().getOTGBiomeIds(world.getName());
            
            int otgBiomeId = -1;
            
            // Exclude already registered biomes from loadedBiomeIdData / default biomes
            boolean bFound = false;
            for(int i = 0; i < otgIds2.length; i++)
            {
            	BiomeConfig biomeConfig2 = otgIds2[i];
            	if(biomeConfig == biomeConfig2)
            	{
            		bFound = true;
            		break;            		
            	}
        		// Forge dimensions: If a world is being reloaded after being unloaded replace the existing biomeConfig
            	else if(
        			biomeConfig2 != null &&
        			biomeConfig.getName().equals(biomeConfig2.getName()) &&
        			biomeConfig.worldConfig.getName().equals(biomeConfig2.worldConfig.getName())
    			)
            	{
            		OTG.getEngine().setOTGBiomeId(world.getName(), i, biomeConfig2, true);
            		otgBiomeId = i;
            		break;
            	}
            }
            if(bFound)
            {
            	continue; // biome is from loadedBiomeIdData, already registered.
            }
            
            if(otgBiomeId == -1)
            {          
            	// Find the next available id
	            for(int i = (!biomeConfig.replaceToBiomeName.isEmpty() ? 256 : 0); i < otgIds2.length; i++) // Virtual (replacetobiomename) biomes can only have id's above 255
	            {
	            	if((biomeConfig.replaceToBiomeName.isEmpty() && i > 255) || (biomeConfig.replaceToBiomeName.isEmpty() && i >= OTG.getEngine().getOTGBiomeIds(world.getName()).length))
	            	{
	            		OTG.log(LogMarker.FATAL, "Biome could not be registered, no free biome id's!");
	            		throw new RuntimeException("Biome could not be registered, no free biome id's!");
	            	}
	            	if(OTG.getEngine().isOTGBiomeIdAvailable(world.getName(), i))
	            	{
	            		otgBiomeId = i;
	            		OTG.getEngine().setOTGBiomeId(world.getName(), i, biomeConfig, false);
	            		break;
	            	}
	            }
	        	if(otgBiomeId > -1 && otgBiomeId < 256)
	        	{
	        		nonVirtualBiomes.add(biomeConfig);
	        	}
	        	else if(otgBiomeId > 255)
	        	{
	        		virtualBiomes.add(biomeConfig);
	        	}
            }        	
        }
                
        // When loading an existing world load the existing biomes first, new biomes after so they don't claim reserved biome id's.
        for (BiomeConfig biomeConfig : nonVirtualBiomesExisting)
        {
        	createAndRegisterBiome(loadedBiomeIdData, biomeConfig, isReload);
        }
        for (BiomeConfig biomeConfig : virtualBiomesExisting)
        {
        	createAndRegisterBiome(loadedBiomeIdData, biomeConfig, isReload);
        }
        for (BiomeConfig biomeConfig : nonVirtualBiomes)
        {
        	createAndRegisterBiome(loadedBiomeIdData, biomeConfig, isReload);
        }
        for (BiomeConfig biomeConfig : virtualBiomes)
        {
        	createAndRegisterBiome(loadedBiomeIdData, biomeConfig, isReload);
        }
        
        BiomeIdData.saveBiomeIdData(worldSaveFolder, this, this.world);
        
        // Forge dimensions are seperate worlds that can share biome configs so
        // use the highest maxSmoothRadius of any of the loaded worlds.
        // Worlds loaded before this one will not use biomes from this world
        // so no need to change their this.worldConfig.maxSmoothRadius
        ArrayList<LocalWorld> worlds = OTG.getAllWorlds();
        if(worlds != null)
        {
	        for(LocalWorld world : worlds)
	        {
	            if (this.worldConfig.maxSmoothRadius < world.getConfigs().getWorldConfig().maxSmoothRadius)
	            {
	                this.worldConfig.maxSmoothRadius = world.getConfigs().getWorldConfig().maxSmoothRadius;
	            }
	        }
        }

        if (this.biomesCount > 0)
        {
            // Remove last ", "
            loadedBiomeNames.delete(loadedBiomeNames.length() - 2, loadedBiomeNames.length());
        }
        return loadedBiomeNames.toString();
    }
    	
    private void createAndRegisterBiome(ArrayList<BiomeIdData> loadedBiomeIdData, BiomeConfig biomeConfig, boolean isReload)
    {   	       	
    	// Restore the saved id (if any)
    	int savedBiomeId = -1;
        if(loadedBiomeIdData != null)
        {
        	for(BiomeIdData biomeIdData : loadedBiomeIdData)
        	{
    			if((world.getName() + "_" + biomeConfig.getName()).equals(biomeIdData.biomeName))
    			{
    				savedBiomeId = biomeIdData.savedBiomeId;
    				break;
    			}
        	}
        }
        
        // Get the assigned OTG biome id
    	int otgBiomeId = -1;
    	BiomeConfig[] otgIds2 = OTG.getEngine().getOTGBiomeIds(world.getName());
    	for(int i = 0; i < otgIds2.length; i++)
    	{
    		if(otgIds2[i] == biomeConfig)
    		{
    			otgBiomeId = i;
    			break;
    		}
    	}
    	if(otgBiomeId == -1)
    	{
    		OTG.log(LogMarker.FATAL, "Biome was not registered, most likely there were no id's available.");
    		throw new RuntimeException("Biome was not registered, most likely there were no id's available.");
    	}
  	
        // Get correct saved id (defaults to generation id, but can be set to use the generation id of another biome)
        if (!biomeConfig.replaceToBiomeName.isEmpty())
        {
        	// This won't work when trying to replacetobiomename a replacetobiomename biome. ReplaceToBiomeName biome must be non-virtual. 
        	for(int i = 0; i < otgIds2.length; i++)
        	{
        		if(otgIds2[i] != null && otgIds2[i].getName() == biomeConfig.replaceToBiomeName)
        		{
        			savedBiomeId = i;
        			break;
    			}
        	}
        	if(savedBiomeId == -1)
        	{
        		savedBiomeId = getRequestedSavedId(biomeConfig.replaceToBiomeName);
        	}
        	
        	if(savedBiomeId == -1)
        	{
            	String[] replaceToBiomeNameArr = biomeConfig.replaceToBiomeName.split(",");
        		if(replaceToBiomeNameArr.length == 1)
        		{
	        		// This may be a legacy world that doesn't use resourcelocation notation, get the correct registry name
	        		String replaceToBiomeNameNew = BiomeRegistryNames.getRegistryNameForDefaultBiome(biomeConfig.replaceToBiomeName);
	        		
	        		if(replaceToBiomeNameNew != null)
	        		{
		        		savedBiomeId = getRequestedSavedId(replaceToBiomeNameNew);
		        		if(savedBiomeId != -1)
		        		{
		        			biomeConfig.replaceToBiomeName = replaceToBiomeNameNew;
		        		}
	        		}
        		}
        	}
        	
        	if(savedBiomeId == -1 || savedBiomeId > 255)
        	{
        		LocalBiome biome = world.getBiomeByNameOrNull(worldConfig.defaultOceanBiome);
        		if(biome != null)
        		{
        			savedBiomeId = biome.getIds().getOTGBiomeId(); // TODO: Re-implement replacetobiomename:virtualbiome
        		} else {
            		savedBiomeId = getRequestedSavedId(biomeConfig.replaceToBiomeName);
            		OTG.log(LogMarker.FATAL, "ReplaceToBiomeName: " + biomeConfig.replaceToBiomeName + " for biome " + biomeConfig.getName() + " could not be found. Please note that it is not possible to ReplaceToBiomeName to a ReplaceToBiomeName biome. Please update your biome configs.");
        			throw new RuntimeException("ReplaceToBiomeName: " + biomeConfig.replaceToBiomeName + " for biome " + biomeConfig.getName() + " could not be found. Please note that it is not possible to ReplaceToBiomeName to a ReplaceToBiomeName biome. Please update your biome configs.");
        		}
    		}
        }
               
        // Create biome
        LocalBiome biome = world.createBiomeFor(biomeConfig, new BiomeIds(otgBiomeId, savedBiomeId), this, isReload);
        
        this.biomesByOTGId[biome.getIds().getOTGBiomeId()] = biome;

        // Indexing ReplacedBlocks
        if (!this.worldConfig.biomeConfigsHaveReplacement)
        {
            this.worldConfig.biomeConfigsHaveReplacement = biomeConfig.replacedBlocks.hasReplaceSettings();
        }

        // Indexing MaxSmoothRadius
        if (this.worldConfig.maxSmoothRadius < biomeConfig.smoothRadius)
        {
            this.worldConfig.maxSmoothRadius = biomeConfig.smoothRadius;
        }

        // Indexing BiomeColor
        if (this.worldConfig.biomeMode == OTG.getBiomeModeManager().FROM_IMAGE)
        {
            if (this.worldConfig.biomeColorMap == null)
            {
                this.worldConfig.biomeColorMap = new HashMap<Integer, Integer>();
            }

            int color = biomeConfig.biomeColor;
            this.worldConfig.biomeColorMap.put(color, biome.getIds().getOTGBiomeId());
        }
    }   
    
	@Override
	public List<LocalBiome> getBiomeArrayLegacy()
	{        
        // For backwards compatibility, sort the biomes by saved id and return default biomes as if they were custom biomes 
        List<LocalBiome> nonDefaultbiomes = new ArrayList<LocalBiome>();
        LocalBiome[] defaultBiomes = new LocalBiome[256];
        for(LocalBiome biome : this.biomesByOTGId)
        {
        	if(biome != null)
        	{
        		Integer defaultBiomeId = DefaultBiome.getId(biome.getName());
        		if(defaultBiomeId != null)
        		{
        			defaultBiomes[defaultBiomeId.intValue()] = biome;
        		} else {
        			nonDefaultbiomes.add(biome);
        		}
        	}
        }

        List<LocalBiome> outputBiomes = new ArrayList<LocalBiome>();
        for(LocalBiome biome : defaultBiomes)
        {
        	if(biome != null)
        	{
        		outputBiomes.add(biome);
        	}
        }
        outputBiomes.addAll(nonDefaultbiomes);
        
		return outputBiomes;
	}
}
