package com.pg85.otg.common.presets;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.pg85.otg.OTG;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.configuration.biome.BiomeConfigFinder;
import com.pg85.otg.configuration.biome.BiomeLoadInstruction;
import com.pg85.otg.configuration.biome.BiomeConfigFinder.BiomeConfigStub;
import com.pg85.otg.configuration.fallbacks.FallbackConfig;
import com.pg85.otg.configuration.io.FileSettingsReader;
import com.pg85.otg.configuration.io.FileSettingsWriter;
import com.pg85.otg.configuration.io.SettingsMap;
import com.pg85.otg.configuration.preset.Preset;
import com.pg85.otg.configuration.standard.BiomeStandardValues;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.configuration.standard.WorldStandardValues;
import com.pg85.otg.configuration.world.WorldConfig;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.BiomeResourceLocation;
import com.pg85.otg.util.minecraft.defaults.BiomeRegistryNames;

public abstract class LocalPresetLoader
{
	protected static final int MAX_INHERITANCE_DEPTH = 15;
	protected final File presetsDir;
	protected final HashMap<String, Preset> presets = new HashMap<String, Preset>();

	public LocalPresetLoader(Path otgRootFolder)
	{
        this.presetsDir = Paths.get(otgRootFolder.toString(), File.separator + PluginStandardValues.PRESETS_FOLDER).toFile();
	}

	public Preset getPresetByName(String name)
	{
		return this.presets.get(name);
	}

	public ArrayList<Preset> getAllPresets()
	{
		return new ArrayList<Preset>(presets.values());
	}

	public void loadPresetsFromDisk()
	{
	    ArrayList<String> worldNames = new ArrayList<String>();
	    if(this.presetsDir.exists() && this.presetsDir.isDirectory())
	    {
	    	for(File presetDir : this.presetsDir.listFiles())
	    	{
	    		if(presetDir.isDirectory())
	    		{
	    			for(File file : presetDir.listFiles())
	    			{
	    				if(file.getName().equals(WorldStandardValues.WORLD_CONFIG_FILE_NAME))
	    				{
			    			worldNames.add(presetDir.getName());			    			
			    			Preset preset = loadPreset(presetDir.toPath());
			    			presets.put(preset.getName(), preset);
					        break;
	    				}
	    			}
	    		}
	    	}
		}
	}	

    private Preset loadPreset(Path presetDir)
    {
        File worldConfigFile = new File(presetDir.toString(), WorldStandardValues.WORLD_CONFIG_FILE_NAME);
    	File biomesDirectory = new File(presetDir.toString(), WorldStandardValues.WORLD_BIOMES_DIRECTORY_NAME);
    	String presetName = presetDir.toFile().getName();

        SettingsMap worldConfigSettings = FileSettingsReader.read(presetName, worldConfigFile);
        WorldConfig worldConfig = new WorldConfig(presetDir, worldConfigSettings, addBiomesFromDirRecursive(biomesDirectory));
        FileSettingsWriter.writeToFile(worldConfig.getSettingsAsMap(), worldConfigFile, worldConfig.settingsMode);

        loadFallbacks(presetDir, worldConfig);
        ArrayList<BiomeConfig> biomeConfigs = loadBiomeConfigs(presetDir, worldConfig);

        // We have to wait for the loading in order to get things like temperature
        //worldConfig.biomeGroupManager.processBiomeData(); // TODO: Re-implement this for 1.16

        return new Preset(presetName, worldConfig, biomeConfigs);       
    }

    private ArrayList<String> addBiomesFromDirRecursive(File biomesDirectory)
    {
    	ArrayList<String> biomes = new ArrayList<String>();
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
	    			biomes.addAll(addBiomesFromDirRecursive(biomeConfig));
	    		}
	    	}
    	}
    	return biomes;
    }

    private void loadFallbacks(Path presetDir, WorldConfig worldConfig)
    {
        File fallbackFile = new File(presetDir.toString(), WorldStandardValues.FALLBACK_FILE_NAME);
        SettingsMap settingsMap = FileSettingsReader.read(presetDir.toFile().getName(), fallbackFile);   

        FallbackConfig fallbacks = new FallbackConfig(settingsMap);

        worldConfig.addWorldFallbacks(fallbacks);
        FileSettingsWriter.writeToFile(fallbacks.getSettingsAsMap(), fallbackFile, OTG.getPluginConfig().settingsMode);
    }    

    private ArrayList<BiomeConfig> loadBiomeConfigs(Path presetDir, WorldConfig worldConfig)
    {
    	String presetName = presetDir.getFileName().toString();

        // Establish folders
        List<Path> biomeDirs = new ArrayList<Path>(2);
        biomeDirs.add(Paths.get(presetDir.toString(), WorldStandardValues.WORLD_BIOMES_DIRECTORY_NAME));

        // Build a set of all biomes to load
        Collection<BiomeLoadInstruction> biomesToLoad = new HashSet<BiomeLoadInstruction>();

        // Load all files
        BiomeConfigFinder biomeConfigFinder = new BiomeConfigFinder(OTG.getPluginConfig().biomeConfigExtension);
        Map<String, BiomeConfigStub> biomeConfigStubs = biomeConfigFinder.findBiomes(worldConfig, worldConfig.worldHeightScale, biomeDirs, biomesToLoad);

        // Read all settings
        ArrayList<BiomeConfig> biomeConfigs = readAndWriteSettings(worldConfig, biomeConfigStubs, presetName, true);

        // Update settings dynamically, these changes don't get written back to the file
        processSettings(worldConfig, biomeConfigs, presetName);

        OTG.log(LogMarker.DEBUG, "{} biomes Loaded", biomeConfigs.size());
        OTG.log(LogMarker.DEBUG, "{}", biomeConfigs.stream().map(item -> item.getName()).collect(Collectors.joining(", ")));

        return biomeConfigs;
    }

    private static ArrayList<BiomeConfig> readAndWriteSettings(WorldConfig worldConfig, Map<String, BiomeConfigStub> biomeConfigStubs, String presetName, boolean write)
    {
        ArrayList<BiomeConfig> biomeConfigs = new ArrayList<BiomeConfig>();

        for (BiomeConfigStub biomeConfigStub : biomeConfigStubs.values())
        {
            // Inheritance
            processInheritance(biomeConfigStubs, biomeConfigStub, 0);
            processMobInheritance(biomeConfigStubs, biomeConfigStub, 0);

            // Settings reading
            BiomeConfig biomeConfig = new BiomeConfig(biomeConfigStub.getLoadInstructions(), biomeConfigStub, biomeConfigStub.getSettings(), worldConfig, presetName);
            biomeConfigs.add(biomeConfig);

            // Settings writing
            if(write)
            {
	            Path writeFile = biomeConfigStub.getPath();
	            if (!biomeConfig.biomeExtends.isEmpty())
	            {
	                writeFile = Paths.get(writeFile + ".inherited");
	            }
	            FileSettingsWriter.writeToFile(biomeConfig.getSettingsAsMap(), writeFile.toFile(), worldConfig.settingsMode);
            }
        }

        return biomeConfigs;
    }

    private void processSettings(WorldConfig worldConfig, ArrayList<BiomeConfig> biomeConfigs, String presetName)
    {
        // Update configs with resourcelocation names for default biomes
    	for(BiomeConfig biomeConfig : biomeConfigs)
    	{
        	// Update biomes for legacy worlds, default biomes should be referred to as minecraft:<biomename>
        	if(
    			biomeConfig.replaceToBiomeName != null && 
				biomeConfig.replaceToBiomeName.trim().length() > 0	        			
			)
        	{
        		String defaultBiomeResourceLocation = BiomeRegistryNames.getRegistryNameForDefaultBiome(biomeConfig.replaceToBiomeName);
        		if(defaultBiomeResourceLocation != null)
        		{
        			biomeConfig.replaceToBiomeName = defaultBiomeResourceLocation;
        		}
        	} else {
        		// Default biomes must replacetobiomename themselves
        		String defaultBiomeResourceLocation = BiomeRegistryNames.getRegistryNameForDefaultBiome(biomeConfig.getName());
        		if(defaultBiomeResourceLocation != null)
        		{
        			biomeConfig.replaceToBiomeName = defaultBiomeResourceLocation;
        		}
        	}

            // Index ReplacedBlocks
            if (!worldConfig.biomeConfigsHaveReplacement)
            {
                worldConfig.biomeConfigsHaveReplacement = biomeConfig.replacedBlocks.hasReplaceSettings();
            }

            // Index maxSmoothRadius
            if (worldConfig.maxSmoothRadius < biomeConfig.smoothRadius)
            {
                worldConfig.maxSmoothRadius = biomeConfig.smoothRadius;
            }
    		if (worldConfig.maxSmoothRadius < biomeConfig.CHCSmoothRadius)
    		{
    			worldConfig.maxSmoothRadius = biomeConfig.CHCSmoothRadius;
    		}

            // Index BiomeColor
            if (worldConfig.biomeMode == OTG.getBiomeModeManager().FROM_IMAGE)
            {
                if (worldConfig.biomeColorMap == null)
                {
                    worldConfig.biomeColorMap = new HashMap<Integer, BiomeResourceLocation>();
                }

                int color = biomeConfig.biomeColor;
                worldConfig.biomeColorMap.put(color, new BiomeResourceLocation(presetName, biomeConfig.getName()));
            }
    	}
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
		            OTG.log(LogMarker.FATAL, "The biome {} cannot inherit mobs from biome {} - too much configs processed already! Cyclical inheritance?", new Object[] { biomeConfigStub.getPath().toFile().getName(), inheritMobsBiomeConfig.getPath().toFile().getName()});
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

	public abstract void registerBiomes();
}