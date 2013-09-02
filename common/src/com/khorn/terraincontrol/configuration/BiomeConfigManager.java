package com.khorn.terraincontrol.configuration;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

/**
 *
 */
public class BiomeConfigManager
{

    private final File worldBiomesDir;
    private final File globalBiomesDir;
    private int biomesCount; // Overall biome count in this world.
    private byte[] ReplaceMatrixBiomes = new byte[256];
    private LocalWorld world;
    private WorldConfig worldConfig;
    private boolean checkOnly;
    private String LoadedBiomeNames = "";

    public BiomeConfigManager(File settingsDir, LocalWorld world, WorldConfig wConfig, Map<String, Integer> customBiomes, boolean checkOnly)
    {

        this.world = world;
        this.worldConfig = wConfig;
        this.checkOnly = checkOnly;

        // Check biome ids, These are the names from the worldConfig file
        for (String biomeName : customBiomes.keySet())
            if (customBiomes.get(biomeName) == -1)
                customBiomes.put(biomeName, world.getFreeBiomeId());

        this.worldBiomesDir = new File(settingsDir, TCDefaultValues.WorldBiomeConfigDirectoryName.stringValue());
        this.globalBiomesDir = new File(TerrainControl.getEngine().getTCDataFolder(), TCDefaultValues.GlobalBiomeConfigDirectoryName.stringValue());


        if (!globalBiomesDir.exists())
        {
            if (!globalBiomesDir.mkdir())
            {
                TerrainControl.log(Level.WARNING, "Error creating biome configs directory, working with defaults");
                return;
            }
        }

        if (!worldBiomesDir.exists())
        {
            if (!worldBiomesDir.mkdir())
            {
                TerrainControl.log(Level.WARNING, "Error creating biome configs directory, working with defaults");
                return;
            }
        }

        // Build biome replace matrix
        for (int i = 0; i < this.ReplaceMatrixBiomes.length; i++)
            this.ReplaceMatrixBiomes[i] = (byte) i;

        //>>	Init the biomeConfigs Array
        this.worldConfig.biomeConfigs = new BiomeConfig[world.getMaxBiomesCount()];
        //>>	Set variable for biomeCount, MIGHT NOT NEED
        this.biomesCount = 0;

        //>>	This arrayList now contains all biomes listed in `DefaultBiome`
        populateBiomeConfigs(new ArrayList<LocalBiome>(world.getDefaultBiomes()), globalBiomesDir);

        ArrayList<LocalBiome> localBiomes = new ArrayList<LocalBiome>(customBiomes.size());
        //>>	This adds all custombiomes that have been listed in WorldConfig to the arrayList
        for (Iterator<Entry<String, Integer>> it = customBiomes.entrySet().iterator(); it.hasNext();)
        {
            Entry<String, Integer> entry = it.next();
            if (checkOnly)
                localBiomes.add(world.getNullBiome(entry.getKey()));
            else
                localBiomes.add(world.AddBiome(entry.getKey(), entry.getValue()));
        }
        populateBiomeConfigs(localBiomes, worldBiomesDir);

        processBiomeConfigs();

        TerrainControl.log(Level.INFO, "Loaded {0} biomes", new Object[]
        {
            biomesCount
        });
        TerrainControl.logIfLevel(Level.ALL, Level.CONFIG, LoadedBiomeNames);

    }

    private void populateBiomeConfigs(ArrayList<LocalBiome> biomesToLoad, File biomeFolder)
    {
        for (LocalBiome localBiome : biomesToLoad)
        {
            BiomeConfig config = new BiomeConfig(biomeFolder, localBiome, this.worldConfig);
            if (this.biomesCount != 0)
                LoadedBiomeNames += ", ";
            LoadedBiomeNames += localBiome.getName();
            // Add biome to the biome array
            if (this.worldConfig.biomeConfigs[localBiome.getId()] == null)
            {
                // Only if it won't overwrite another biome in the array
                this.biomesCount++;
            } else
            {
                TerrainControl.log(Level.WARNING, "Duplicate biome id " + localBiome.getId() + " (" + this.worldConfig.biomeConfigs[localBiome.getId()].name + " and " + config.name + ")!");
            }
            this.worldConfig.biomeConfigs[localBiome.getId()] = config;
        }
    }

    private void processBiomeConfigs()
    {
       /***********************
         * Proposed Algorithm
         ***********************
         * - Grab the settingsCache value for BiomeExtends
         * - Get the id of the biome to be extended and find it in
         * biomeConfigs
         * - determine if need to extend what we found
         * ---- decend until we find a non-extending biome
         * - merge the two biomeConfig's
         * ---- special treatment for resources
         * - save results by overwritting approp. config
         * - unset entend var to prevent multi-extending
         * - ascend until no more extending can be done
         * - Rinse / Repeat until done extending all biome configs
         */
        int xbiome = 0;
        String autosarcophagousBiomes = "";
        TerrainControl.log(Level.INFO, "=============== Biome Processing START ===============");
        for (BiomeConfig config : this.worldConfig.biomeConfigs)
        {
            if (config == null)
            {
                xbiome++;
                continue;
            }
            if ("Avatar".equals(config.name))
            {
                String xtemp = "";
                for (Entry<String, String> string : config.settingsCache.entrySet())
                {
                    if (!xtemp.isEmpty())
                        xtemp += ", ";
                    if (string.getKey().equals(TCDefaultValues.BiomeExtends.name().toLowerCase()))
                        xtemp += string.getKey() + ":" + string.getValue();
                }
                TerrainControl.log(Level.CONFIG, "Settings: " + xtemp + "\n for " + config.name + "   :::   " + TCDefaultValues.BiomeExtends.name());
            }
            TerrainControl.log(Level.CONFIG, "Biome attempting to load: " + config.name + ":" + xbiome++);

            if (config.settingsCache.containsKey(TCDefaultValues.BiomeExtends.name().toLowerCase()))
            {
                String biomeToExtend_Name = config.settingsCache.get(TCDefaultValues.BiomeExtends.name().toLowerCase());
                if (!biomeToExtend_Name.isEmpty())
                {
                    TerrainControl.log(Level.SEVERE, "Biome(" + biomeToExtend_Name + ") Processing!");
                    //>>	anti-self-inheritance
                    if (biomeToExtend_Name.equals(config.name))
                    {
                        if (!autosarcophagousBiomes.isEmpty())
                        {
                            autosarcophagousBiomes += ", ";
                        }
                        autosarcophagousBiomes += biomeToExtend_Name;
                        TerrainControl.log(Level.CONFIG, "Biome(" + biomeToExtend_Name + ":null) being Autosarcophagous!");
                    } else
                    {
                        Integer biomeToExtend_Id = this.worldConfig.CustomBiomeIds.get(biomeToExtend_Name);
                        if (biomeToExtend_Id == null)
                        {
                            TerrainControl.log(Level.WARNING, "Biome2Extend(" + biomeToExtend_Name + ":null) not found. If you think this is in error, check your configs!");
                        } else
                        {
                            BiomeConfig biomeToExtend_Config = this.worldConfig.biomeConfigs[biomeToExtend_Id];
                            config = merge(biomeToExtend_Config, config);
                            TerrainControl.log(Level.WARNING, "Biome2Extend( " + biomeToExtend_Name + ":" + biomeToExtend_Id + ") was found!");
                        }
                    }
                }
            }
            //t>>	Process needs a special way of handling resource entries to avoid
            //t>>	having both parent and child resources get saved. Need to figure 
            //t>>	out how resource loading works. Comment that function in ConfigFile.
            //>>	
            //t>>	Also, another possbility:
            //t>>	Look into using readConfigSettings() instead of the low level
            //t>>	readSettingsFile() method, then write a merge method in BiomeConfigs
            //t>>	that individually copies over all variables. This will be more fine
            //t>>	tuned but less maintainable. If we can isolate the resource loading
            //t>>	and give it special treatment, that is far more desirable from what
            //t>>	I can see from the code....
            config.process();

            if (this.checkOnly)
                continue;

            if (!config.ReplaceBiomeName.equals(""))
            {
                this.worldConfig.HaveBiomeReplace = true;
                this.ReplaceMatrixBiomes[config.Biome.getId()] = (byte) world.getBiomeIdByName(config.ReplaceBiomeName);
            }

            if (this.worldConfig.NormalBiomes.contains(config.name))
                this.worldConfig.normalBiomesRarity += config.BiomeRarity;
            if (this.worldConfig.IceBiomes.contains(config.name))
                this.worldConfig.iceBiomesRarity += config.BiomeRarity;

            if (!this.worldConfig.BiomeConfigsHaveReplacement)
                this.worldConfig.BiomeConfigsHaveReplacement = config.ReplaceCount > 0;
            //>>	OLD LOCATION OF SETTINGS CACHE POPULATION & BIOME COUNT INCREMENT

            if (this.worldConfig.biomeMode == TerrainControl.getBiomeModeManager().FROM_IMAGE)
            {
                if (this.worldConfig.biomeColorMap == null)
                    this.worldConfig.biomeColorMap = new HashMap<Integer, Integer>();

                try
                {
                    int color = Integer.decode(config.BiomeColor);
                    if (color <= 0xFFFFFF)
                        this.worldConfig.biomeColorMap.put(color, config.Biome.getId());
                } catch (NumberFormatException ex)
                {
                    TerrainControl.log(Level.WARNING, "Wrong color in " + config.Biome.getName());
                }
            }
        }
        if (!autosarcophagousBiomes.isEmpty())
        {
            TerrainControl.log(Level.WARNING, "A Biome can NOT extend itself, please fix the following biomes: " + autosarcophagousBiomes);
        }
    }

    public static BiomeConfig merge(BiomeConfig baseBiome, BiomeConfig extendingBiome)
    {
        TerrainControl.log(Level.SEVERE, "Starting Merge!");
        for (String key : baseBiome.settingsCache.keySet())
        {
            if (!extendingBiome.settingsCache.containsKey(key))
            {
                extendingBiome.settingsCache.put(key, baseBiome.settingsCache.get(key));
                TerrainControl.log(Level.SEVERE, "Setting(" + key + "," + baseBiome.settingsCache.get(key));
            }
        }
        return extendingBiome;
    }

}
