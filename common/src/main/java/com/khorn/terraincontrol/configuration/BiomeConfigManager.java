package com.khorn.terraincontrol.configuration;

import com.khorn.terraincontrol.DefaultBiome;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import java.io.File;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;

/**
 *
 */
public final class BiomeConfigManager
{

    /*
     *
     */
    private final File worldBiomesDir;
    /*
     *
     */
    private final File globalBiomesDir;
    /*
     *
     */
    private final File worldDefaultBiomesDir;
    /*
     *
     */
    private byte[] ReplaceMatrixBiomes = new byte[256];
    /*
     *
     */
    private LocalWorld world;
    /*
     *
     */
    private WorldConfig worldConfig;
    /*
     *
     */
    private boolean checkOnly;
    /*
     * A running list of biomes that have been processed during the loading
     */
    private String LoadedBiomeNames = "";
    /*
     * Must be simple array for fast access.
     * ***** Beware! Some ids may contain null values; *****
     */
    private BiomeConfig[] biomeConfigs;
    /*
     * Overall biome count in this world.
     */
    private int biomesCount;

    /**
     *
     * @param settingsDir
     * @param world
     * @param wConfig
     * @param customBiomes
     * @param checkOnly
     */
    public BiomeConfigManager(File settingsDir, LocalWorld world, WorldConfig wConfig, Map<String, Integer> customBiomes, boolean checkOnly)
    {

        this.world = world;
        this.worldConfig = wConfig;
        this.checkOnly = checkOnly;

        // Check biome ids, These are the names from the worldConfig file
        //>>	Corrects any instances of incorrect biome id.
        for (String biomeName : customBiomes.keySet())
            if (customBiomes.get(biomeName) == -1)
                customBiomes.put(biomeName, world.getFreeBiomeId());
        this.worldConfig.CustomBiomeIds = customBiomes;

        //>> -- ESTRABLISH FOLDERS -- <<//
        //>>	TerrainControl/GlobalBiomes/
        this.globalBiomesDir = new File(TerrainControl.getEngine().getTCDataFolder(), TCDefaultValues.GlobalBiomeConfigDirectoryName.stringValue());
        //>>	TerrainControl/worlds/<WorldName>/<WorldBiomes/
        this.worldBiomesDir = new File(settingsDir, correctOldBiomeConfigFolder(settingsDir));
        //>>	TerrainControl/worlds/<WorldName>/WorldBiomes/Defaults/
        this.worldDefaultBiomesDir = new File(this.worldBiomesDir, TCDefaultValues.WorldDefaultBiomeConfigDirectoryName.stringValue());

        //>>	If there was an error in folder establishment, return.
        if (!makeBiomeFolders())
            return;

        // Build biome replace matrix
        for (int i = 0; i < this.ReplaceMatrixBiomes.length; i++)
            this.ReplaceMatrixBiomes[i] = (byte) i;

        //>>	Init the biomeConfigs Array
        biomeConfigs = new BiomeConfig[world.getMaxBiomesCount()];
        //>>	Set variable for biomeCount, MIGHT NOT NEED
        biomesCount = 0;

        //>>	This.biomeConfigs now contains all biomes listed in world.getDefaultBiomes()
        populateWorldDefaultBiomeConfigs(world);

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
        populateCustomBiomeConfigs(localBiomes, worldBiomesDir);

        processBiomeConfigs();

        TerrainControl.log(Level.INFO, "Loaded {0} biomes", new Object[]{ biomesCount });
        TerrainControl.logIfLevel(Level.ALL, Level.CONFIG, LoadedBiomeNames);

    }

    private boolean makeBiomeFolders()
    {
        boolean allFoldersExist = true;

        //>>	Create the folders if not present
        if (!globalBiomesDir.exists())
            if (!globalBiomesDir.mkdir())
            {
                TerrainControl.log(Level.WARNING, "Error creating Global directory.");
                allFoldersExist = false;
            }

        if (!worldBiomesDir.exists())
            if (!worldBiomesDir.mkdir())
            {
                TerrainControl.log(Level.WARNING, "Error creating World Biome configs directory.");
                allFoldersExist = false;
            }

        if (!worldDefaultBiomesDir.exists())
            if (!worldDefaultBiomesDir.mkdir())
            {
                TerrainControl.log(Level.WARNING, "Error creating World Default Biome configs directory.");
                allFoldersExist = false;
            }

        if (!allFoldersExist)
        {
            TerrainControl.log(Level.WARNING, "Potentially working with defaults.");
        }
        return allFoldersExist;
    }

    private String correctOldBiomeConfigFolder(File settingsDir)
    {
        //>>	Rename the old folder
        String biomeFolderName = TCDefaultValues.WorldBiomeConfigDirectoryName.stringValue();
        File oldBiomeConfigs = new File(settingsDir, "BiomeConfigs");
        if (oldBiomeConfigs.exists())
        {
            if (!oldBiomeConfigs.renameTo(new File(settingsDir, biomeFolderName)))
            {
                TerrainControl.log(Level.WARNING, "========================");
                TerrainControl.log(Level.WARNING, "Fould old `BiomeConfigs` folder, but it could not be renamed to `", biomeFolderName, "`!");
                TerrainControl.log(Level.WARNING, "Please rename the folder manually.");
                TerrainControl.log(Level.WARNING, "========================");
                biomeFolderName = "BiomeConfigs";
            }
        }
        return biomeFolderName;
    }

    private void populateWorldDefaultBiomeConfigs(LocalWorld world)
    {
        for (LocalBiome localBiome : world.getDefaultBiomes())
        {
            //>>	Upon loading a biome, check the worldDefault location first.
            BiomeConfig config = new BiomeConfig(worldDefaultBiomesDir, localBiome, this.worldConfig);

            //t>>   ========  TESTING: BASE BIOMES NO INHERITANCE =======
            config.BiomeExtends = "";
            config.BiomeExtendsProcessed = false;
            //t>>   ========  TESTING: BASE BIOMES NO INHERITANCE =======

            if (!config.readSuccess)
            {
                //>>	If a config doesnt exist at that location try the usual BiomeConfigs folder
                config = new BiomeConfig(worldBiomesDir, localBiome, this.worldConfig);
            }
            if (!config.readSuccess)
            {
                //>>	and if all else fails look in the globalBiomes folder
                //>>	if the biome does not exist here, one will be created
                config = new BiomeConfig(globalBiomesDir, localBiome, this.worldConfig);
            }
            TerrainControl.log(Level.FINER, config.file.getAbsolutePath());
            pushBiomeConfig(localBiome, config);
        }
    }

    private void populateCustomBiomeConfigs(ArrayList<LocalBiome> biomesToLoad, File biomeFolder)
    {
        for (LocalBiome localBiome : biomesToLoad)
        {
            BiomeConfig config = new BiomeConfig(biomeFolder, localBiome, this.worldConfig);
            pushBiomeConfig(localBiome, config);
        }
    }

    private void pushBiomeConfig(LocalBiome localBiome, BiomeConfig config)
    {
        if (biomesCount != 0)
            LoadedBiomeNames += ", ";
        LoadedBiomeNames += localBiome.getName() + (TCLogManager.getLogger().isLoggable(Level.FINE) ? (":" + localBiome.getId()) : "");
        // Add biome to the biome array
        if (biomeConfigs[localBiome.getId()] == null)
        {
            // Only if it won't overwrite another biome in the array
            biomesCount++;
        } else
        {
            TerrainControl.log(Level.WARNING, "Duplicate biome id {0} ({1} and {2})!", new Object[]{ localBiome.getId(), biomeConfigs[localBiome.getId()].name, config.name });
        }
        biomeConfigs[localBiome.getId()] = config;
    }

    private void processBiomeConfigs()
    {
        /** *********************
         * Proposed Algorithm
         ***********************
         * - Grab the settingsCache value for BiomeExtends              check
         * - Get the id of the biome to be extended and find it in
         *  biomeConfigs                                                check
         * - determine if need to extend what we found                  check
         * ---- decend until we find a non-extending biome              check
         * - merge the two biomeConfig's                                getting close
         * ---- special treatment for resources                             getting some out of bounds exceptions
         * - save results by overwritting approp. config                check
         * - make sure we dont process already processed configs        check
         * - ascend until no more extending can be done                 check
         * - Rinse / Repeat until done extending all biome configs      check
         */
        int xbiome = 0;
        selfInheritanceErrors = "";
        TerrainControl.log(Level.CONFIG, "=============== Biome Processing START ===============");

        for (BiomeConfig config : biomeConfigs)
        {
            if (config == null)
            {
                xbiome++;
                continue;
            }

            TerrainControl.log(Level.CONFIG, "Processing Biome: " + config.name + ":" + xbiome++);
            if (!config.BiomeExtendsProcessed)
            {
                TerrainControl.log(Level.CONFIG, "======== Inheritance Starting ========");

                doInheritance(config, true);

                TerrainControl.log(Level.CONFIG, "========= Inheritance Ending =========");
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
            //>>	EDIT: I found a much better solution for merging. Rather than
            //>>	Doing an either/or approach, I use the raw settings cache to merge
            //>>	non-resources, then run process, and do a special resource merge
            //>>	where all resource types have a method of detecting if they can 
            //>>	be merged or not. Still work in progress, but much cleaner than before.
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

            if (this.worldConfig.maxSmoothRadius < config.SmoothRadius)
                this.worldConfig.maxSmoothRadius = config.SmoothRadius;

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
        //>>	Inheritance Errors
        if (!selfInheritanceErrors.isEmpty() || !inheritanceErrors.isEmpty())
        {
            TerrainControl.log(Level.SEVERE, "======= ACTION REQUIRED =======");
            if (!selfInheritanceErrors.isEmpty())
            {
                TerrainControl.log(Level.WARNING, "A Biome can NOT extend itself, please fix the following biomes:\n{0}", new Object[]{ selfInheritanceErrors });
            }
            if (!inheritanceErrors.isEmpty())
            {
                String tempIE = "";
                for (StringBuilder cycle : inheritanceErrors)
                {
                    tempIE += cycle.toString() + "\n";
                }
                tempIE += "We will ignore the above biomes for inheritance purposes...";

                TerrainControl.log(Level.SEVERE, "Cyclical Inheritance(s) Found:\n{0}", tempIE);

            }
            TerrainControl.log(Level.SEVERE, "======= ACTION REQUIRED =======");
        }
    }
    //>>	
    //>>	
    private String selfInheritanceErrors;
    private LinkedList<BiomeConfig> biomeLoadingStack = new LinkedList<BiomeConfig>();
    private boolean cycleFound = false;
    private ArrayList<StringBuilder> inheritanceErrors = new ArrayList<StringBuilder>(10);

    private void doInheritance(BiomeConfig config, boolean isParent)
    {
        TerrainControl.log(Level.CONFIG, "GOGO: {0} DO INHERITANCE", new Object[]{config.name});
        if (!config.BiomeExtendsSeen)
        {
            config.BiomeExtendsSeen = true;
            biomeLoadingStack.push(config);
            TerrainControl.log(Level.CONFIG, "\tSTACK:::Pushing config; New Size: {0}", new Object[]{biomeLoadingStack.size()});
            if (!config.BiomeExtendsProcessed)
            {
                if (config.settingsCache.containsKey(TCDefaultValues.BiomeExtends.name().toLowerCase()))
                {
                    String biomeToExtend_Name = config.settingsCache.get(TCDefaultValues.BiomeExtends.name().toLowerCase());
                    if (!biomeToExtend_Name.isEmpty())
                    {
                        TerrainControl.log(Level.CONFIG, "\tBiome(" + biomeToExtend_Name + ") Processing!");
                        //>>	anti-self-inheritance
                        if (biomeToExtend_Name.equals(config.name))
                        {
                            if (!selfInheritanceErrors.isEmpty())
                            {
                                selfInheritanceErrors += ", ";
                            }
                            selfInheritanceErrors += biomeToExtend_Name;
                            TerrainControl.log(Level.CONFIG, "\t\tBiome(" + biomeToExtend_Name + ":null) being Autosarcophagous!");
                            return;
                        } else
                        {
                            Integer biomeToExtend_Id = this.worldConfig.CustomBiomeIds.get(biomeToExtend_Name);
                            if (biomeToExtend_Id == null) //>>	If not found as custom biome, look at the defaults
                                biomeToExtend_Id = DefaultBiome.getId(biomeToExtend_Name);
                            if (biomeToExtend_Id == null)
                            {
                                TerrainControl.log(Level.WARNING, "\t\tBiome2Extend(" + biomeToExtend_Name + ":null) not found. If you think this is in error, check your configs!");
                            } else
                            {
                                TerrainControl.log(Level.CONFIG, "\t\tBiome2Extend( " + biomeToExtend_Name + ":" + biomeToExtend_Id + ") was found!");
                                doInheritance(biomeConfigs[biomeToExtend_Id], false);
                                if (isParent == true)
                                {
                                    if (!this.cycleFound)
                                    {
                                        TerrainControl.log(Level.CONFIG, "\t\t\tSTACK:::Popping_A");
                                        BiomeConfig parentConfig = biomeLoadingStack.pop();

                                        while (!biomeLoadingStack.isEmpty())
                                        {
                                            TerrainControl.log(Level.CONFIG, "\t\t\tSTACK:::Popping_B");
                                            BiomeConfig child = biomeLoadingStack.pop();
                                            TerrainControl.log(Level.CONFIG, "\t\t\t\tMerging Biomes (" + parentConfig.name + ":" + child.name + ");");
                                            BiomeConfig merged = child.merge(parentConfig);
                                            if (merged == null)
                                            {
                                                TerrainControl.log(Level.SEVERE, "\t\tMerging returned null!!");
                                            } else
                                            {
                                                //>>	check in the CustomBiomes for the ID
                                                Integer mergedIndex = this.worldConfig.CustomBiomeIds.get(child.name);
                                                //>>	If not go and check the defaults...
                                                mergedIndex = (mergedIndex == null) ? DefaultBiome.getId(child.name) : mergedIndex;
                                                if (mergedIndex == null)
                                                {
                                                    TerrainControl.log(Level.SEVERE, "\t\tPlease make sure you include {0} in the `Biome Lists` portion of the WorldConfig", new Object[]{ child.name });
                                                } else
                                                {
                                                    biomeConfigs[mergedIndex] = merged;
                                                }

                                            }
                                            
                                            parentConfig = child;
                                        }
                                    } else
                                    {
                                        this.cycleFound = false;
                                    }
                                }
                            }
                        }
                    } else
                    {
                        TerrainControl.log(Level.CONFIG, "\t\t -A- Biome Does not extend other");
                        biomeLoadingStack.peek().BiomeExtendsProcessed = true;
                    }
                } else
                {
                    TerrainControl.log(Level.CONFIG, "\t\t -B- Biome Does not extend other");
                    biomeLoadingStack.peek().BiomeExtendsProcessed = true;
                }
            }
            
            if (isParent)
            {
                TerrainControl.log(Level.CONFIG, "\t\t\tSTACK:::Clearing");
                biomeLoadingStack.clear();
            }
        } else if (biomeLoadingStack.size() >= 2)
        {
            StringBuilder cycle = new StringBuilder(" ... <- ");
            BiomeConfig first = biomeLoadingStack.pollLast();
            cycle.append(first.name);
            cycle.append(" <- ");
            while (!biomeLoadingStack.isEmpty()){
                cycle.append(biomeLoadingStack.pollLast().name);
                cycle.append(" <- ");
            }
            cycle.append(first.name);
            cycle.append(" <- ...etc.");
            inheritanceErrors.add(cycle);
            this.cycleFound = true;
        } else
        {
            TerrainControl.log(Level.CONFIG, "\t\t -- Biome Already Processed");
            biomeLoadingStack.push(config);
        }
    }

    /**
     *
     * @return
     */
    public BiomeConfig[] getBiomeConfigs()
    {
        return biomeConfigs;
    }

    /**
     *
     * @param biomeConfigs
     */
    public void setBiomeConfigs(BiomeConfig[] biomeConfigs)
    {
        this.biomeConfigs = biomeConfigs;
    }

    /**
     *
     * @param index
     * @param biomeConfig
     */
    public void addBiomeConfig(int index, BiomeConfig biomeConfig)
    {
        this.biomeConfigs[index] = biomeConfig;
    }

    /**
     *
     * @return
     */
    public int getBiomesCount()
    {
        return biomesCount;
    }

    /**
     *
     * @param biomesCount
     */
    public void setBiomesCount(int biomesCount)
    {
        this.biomesCount = biomesCount;
    }

}
