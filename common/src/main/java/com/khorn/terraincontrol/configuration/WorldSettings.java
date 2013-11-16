package com.khorn.terraincontrol.configuration;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.standard.BiomeStandardValues;
import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;
import com.khorn.terraincontrol.configuration.standard.WorldStandardValues;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultBiome;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;

/**
 * Holds the WorldConfig and all BiomeConfigs.
 */
public final class WorldSettings
{

    private Map<String, File> BiomeDirs = new LinkedHashMap<String, File>(4);
    public byte[] ReplaceBiomesMatrix = new byte[256];
    private LocalWorld world;
    public WorldConfig worldConfig;
    private boolean checkOnly;
    /*
     * A running list of biomes that have been processed during the loading
     */
    private String LoadedBiomeNames = "";
    /*
     * Must be simple array for fast access.
     * ***** Beware! Some ids may contain null values; *****
     */
    public BiomeConfig[] biomeConfigs;
    /*
     * Overall biome count in this world.
     */
    public int biomesCount;

    /**
     *
     * @param settingsDir
     * @param world
     * @param wConfig
     * @param customBiomes
     * @param checkOnly
     */
    public WorldSettings(File settingsDir, LocalWorld world, boolean checkOnly)
    {

        this.world = world;
        this.worldConfig = new WorldConfig(settingsDir, world);
        this.checkOnly = checkOnly;

        // Check biome ids, These are the names from the worldConfig file
        //>>	Corrects any instances of incorrect biome id.
        Map<String, Integer> customBiomes = worldConfig.CustomBiomeIds;
        for (String biomeName : customBiomes.keySet())
            if (customBiomes.get(biomeName) == -1)
                customBiomes.put(biomeName, world.getFreeBiomeId());
        this.worldConfig.CustomBiomeIds = customBiomes;

        //>> -- ESTRABLISH FOLDERS -- <<//
        //>>	TerrainControl/worlds/<WorldName>/<WorldBiomes/
        this.BiomeDirs.put("world", new File(settingsDir, correctOldBiomeConfigFolder(settingsDir)));
        //>>	TerrainControl/GlobalBiomes/
        this.BiomeDirs.put("global", new File(TerrainControl.getEngine().getTCDataFolder(), PluginStandardValues.BiomeConfigDirectoryName.stringValue()));
        
        //>>	If there was an error in folder establishment, return.
        if (!makeBiomeFolders())
            return;

        // Build biome replace matrix
        for (int i = 0; i < this.ReplaceBiomesMatrix.length; i++)
            this.ReplaceBiomesMatrix[i] = (byte) i;

        //>>	Init the biomeConfigs Array
        biomeConfigs = new BiomeConfig[world.getMaxBiomesCount()];
        //>>	Set variable for biomeCount, MIGHT NOT NEED
        biomesCount = 0;

        //>>	This.biomeConfigs now contains all biomes listed in world.getDefaultBiomes()
        populateWorldDefaultBiomeConfigs(world);

        Map<String, LocalBiome> localBiomes = new HashMap<String, LocalBiome>(customBiomes.size() * 2);
        //>>	This adds all custombiomes that have been listed in WorldConfig to the arrayList
        for (Iterator<Entry<String, Integer>> it = customBiomes.entrySet().iterator(); it.hasNext();)
        {
            Entry<String, Integer> entry = it.next();
            if (checkOnly)
                localBiomes.put(entry.getKey(), world.getNullBiome(entry.getKey()));
            else
            {
                int id = worldConfig.CustomBiomeIds.get(entry.getKey());
                if (id == -1)
                    id = world.getFreeBiomeId();
                localBiomes.put(entry.getKey(), world.AddCustomBiome(entry.getKey(), id));
            }
        }
        // Add virtual biomes to world
        for (Iterator<Entry<String, Integer>> it = worldConfig.VirtualBiomeIds.entrySet().iterator(); it.hasNext();)
        {
            Entry<String, Integer> entry = it.next();
            if (checkOnly)
                localBiomes.put(entry.getKey(), world.getNullBiome(entry.getKey()));
            else
            {
                int realId = worldConfig.VirtualBiomeRealIds.get(entry.getValue());
                if (world.getBiomeById(realId) == null)
                {
                    TerrainControl.log(Level.WARNING, "Wrong real id for virtual biome {0}!", new Object[]{ entry.getKey() });
                    continue;
                }

                localBiomes.put(entry.getKey(), world.AddVirtualBiome(entry.getKey(), realId, entry.getValue()));
            }
        }
        populateCustomBiomeConfigs(localBiomes);

        processBiomeConfigs();

        TerrainControl.logIfLevel(Level.INFO, Level.OFF, "{0} Biomes Loaded", new Object[]{ biomesCount });
        TerrainControl.logIfLevel(Level.ALL, Level.CONFIG, "{0} Biomes Loaded:\n{1}", new Object[]{ biomesCount, LoadedBiomeNames });

    }

    private boolean makeBiomeFolders()
    {
        boolean allFoldersExist = true;

        //>>	Create the folders if not present
        if (this.BiomeDirs.containsKey("global") && !this.BiomeDirs.get("global").exists())
            if (!this.BiomeDirs.get("global").mkdir())
            {
                TerrainControl.log(Level.WARNING, "Error creating Global directory.");
                allFoldersExist = false;
            }

        if (this.BiomeDirs.containsKey("world") && !this.BiomeDirs.get("world").exists())
            if (!this.BiomeDirs.get("world").mkdir())
            {
                TerrainControl.log(Level.WARNING, "Error creating World Biome configs directory.");
                allFoldersExist = false;
            }

        if (!allFoldersExist)
            TerrainControl.log(Level.WARNING, "Potentially working with defaults.");
        return allFoldersExist;
    }

    private String correctOldBiomeConfigFolder(File settingsDir)
    {
        //>>	Rename the old folder
        String biomeFolderName = WorldStandardValues.BiomeConfigDirectoryName.stringValue();
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
        //>>	Establish a list of biomes
        Map<String, LocalBiome> biomesRemaining = null;
        //>>	Iterate over all acceptable directories, loading desired default biomes as we go
        for (Entry<String, File> entry : BiomeDirs.entrySet())
            biomesRemaining = loadBiomesRecursive(entry.getValue(), world.getDefaultBiomes());
        //>>	Biomes that come up not loaded are created here
        makeBiomeConfigs(biomesRemaining, this.BiomeDirs.get("global"));
    }

    private void populateCustomBiomeConfigs(Map<String, LocalBiome> biomesToLoad)
    {
        //>>	Establish a list of biomes
        Map<String, LocalBiome> biomesRemaining = null;
        //>>	Iterate over all acceptable directories, loading desired custom biomes as we go
        for (Entry<String, File> entry : BiomeDirs.entrySet())
            biomesRemaining = loadBiomesRecursive(entry.getValue(), biomesToLoad);
        //>>	Biomes that come up not loaded are created here
        makeBiomeConfigs(biomesRemaining, this.BiomeDirs.get("world"));

    }
    
    private void makeBiomeConfigs(Map<String, LocalBiome> biomesToMake, File folder){
        if (biomesToMake != null) //>>	If we get something to make
            for (Entry<String, LocalBiome> biome : biomesToMake.entrySet())
            { //>>	Go ahead and create a new BiomeConfig for it
                pushBiomeConfig(biome.getValue(), new BiomeConfig(folder, biome.getValue(), this.worldConfig));
            }
    }

    /**
     * Creates a BiomeConfig for all files in a directory and it's
     * sub-directories that satisfy the following conditions:
     * 1.) the filename contains a valid TC biomeconfig extension, and
     * 2.) the biomeName part of the filename must be associated with an entry
     * in biomesToLoad
     * <p/>
     * @param directory    The directory to load from.
     * @param biomesToLoad The biomes that should be loaded while iterating over
     *                     the directory and sub-directories
     * @return A Map<String, LocalBiome> of biomes that were not loaded
     *         during the recursive directory loading process
     */
    private Map<String, LocalBiome> loadBiomesRecursive(File directory, ArrayList<LocalBiome> biomesToLoad)
    {
        //>>	We need a faster way of looking up biomeNames, so a hashmap will
        //>>	be created for you if you only have an arraylist
        Map<String, LocalBiome> acceptableBiomes = new HashMap<String, LocalBiome>(biomesToLoad.size() *2);
        for (LocalBiome lb : biomesToLoad){
            acceptableBiomes.put(lb.getName(), lb);
        }
        return loadBiomesRecursive(directory, acceptableBiomes);
    }
    
    /**
     * Creates a BiomeConfig for all files in a directory and it's
     * sub-directories that satisfy the following conditions:
     * 1.) the filename contains a valid TC biomeconfig extension, and
     * 2.) the biomeName part of the filename must be associated with an entry
     * in biomesToLoad
     * <p/>
     * @param directory    The directory to load from.
     * @param biomesToLoad The biomes that should be loaded while iterating over
     *                     the directory and sub-directories
     * @return  A Map<String, LocalBiome> of biomes that were not loaded
     *         during the recursive directory loading process
     */
    private Map<String, LocalBiome> loadBiomesRecursive(File directory, Map<String, LocalBiome> biomesToLoad)
    {
        if (!directory.isDirectory())
            throw new IllegalArgumentException("Given file is not a directory: " + directory.getAbsolutePath());

        for (File file : directory.listFiles())
        { //>>	For each file in this directory
            if (file.isDirectory())
            { //>>	If the file is a directory, recurse
                loadBiomesRecursive(file, biomesToLoad);
            } else //>>	Else, try and load a biomeConfig from it
            {
                //>>	Get name and determine if file has one of our extensions
                String fileName = file.getName();
                ArrayList<String> extensions = BiomeStandardValues.BiomeConfigExtensions.stringArrayListValue();
                //>>	Initially set to extension not found
                int index = -1;
                for (String ext : extensions)
                { //>>	Search for acceptable extensions
                    index = fileName.indexOf(ext);
                    if (index != -1)
                    {
                        //>>	stop looking if we find an extension
                        break;
                    }
                }
                if (index != -1)
                { //>>	Process biome file if valid extension found
                    //>>	Get BiomeName
                    String biomeName = fileName.substring(0, index);
                    //>>	Use biomeName to find LocalBiome object
                    LocalBiome lb = null;
                    if (biomesToLoad.containsKey(biomeName))
                        lb = biomesToLoad.remove(biomeName);
                    //>>	If we found a LocalBiome from biomeName, push a new BiomeConfig onto the biomeconfigs array
                    if (lb != null)
                    {
                        pushBiomeConfig(lb, new BiomeConfig(biomeName, file, lb, this.worldConfig));
                    }
                } else //>>	Else ignore this one and go to next file...
                    continue;
            }
        }
        return biomesToLoad;
    }

    private void pushBiomeConfig(LocalBiome localBiome, BiomeConfig config)
    {
        if (biomesCount != 0)
            LoadedBiomeNames += ", ";
        LoadedBiomeNames += localBiome.getName() + (TerrainControl.getLogger().isLoggable(Level.FINE) ? (":" + localBiome.getId() + (localBiome.isVirtual() ? config.worldConfig.VirtualBiomeIds.get(localBiome.getName()) + ":" : "")) : "");
        // Add biome to the biome array
        if (biomeConfigs[localBiome.getId()] == null)
        {
            // Only if it won't overwrite another biome in the array
            biomesCount++;
        } else
            TerrainControl.log(Level.WARNING, "Duplicate biome id {0} ({1} and {2})!", new Object[]{ localBiome.getId(), biomeConfigs[localBiome.getId()].name, config.name });
        //>>	This will, by default, treat the last biome of a specific localBiome.getId() as the one we save.
        biomeConfigs[localBiome.getId()] = config;
    }

    private void processBiomeConfigs()
    {
        int xbiome = 0;
        selfInheritanceErrors = "";
        TerrainControl.log(Level.FINER, "=============== Biome Processing START ===============");

        for (BiomeConfig config : biomeConfigs)
        {
            if (config == null)
            {
                xbiome++;
                continue;
            }

            TerrainControl.log(Level.FINE, "Processing Biome: " + config.name + ":" + xbiome++);
            if (!config.BiomeExtendsProcessed)
            {
                TerrainControl.log(Level.FINER, "======== Inheritance Starting ========");

                doInheritance(config, true);

                TerrainControl.log(Level.FINER, "========= Inheritance Ending =========");
            }
            config.process();
            config.outputToFile();

            if (this.checkOnly)
                continue;

            if (!config.ReplaceBiomeName.isEmpty())
            {
                this.worldConfig.HaveBiomeReplace = true;
                this.ReplaceBiomesMatrix[config.Biome.getId()] = (byte) world.getBiomeIdByName(config.ReplaceBiomeName);
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
                StringBuilder tempIE = new StringBuilder(200);
                for (StringBuilder cycle : inheritanceErrors)
                {
                    tempIE.append(cycle.toString()).append("\n");
                }
                TerrainControl.log(Level.SEVERE, "Cyclical Inheritance(s) Found:\n{0}", tempIE.append("We will ignore the above biomes for inheritance purposes...").toString());
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
        TerrainControl.log(Level.FINER, "DO INHERITANCE: {0}", new Object[]{ config.name });
        if (!config.BiomeExtendsSeen)
        {
            config.BiomeExtendsSeen = true;
            biomeLoadingStack.push(config);
            TerrainControl.log(Level.FINEST, "\tSTACK:::Pushing config; New Size: {0}", new Object[]{ biomeLoadingStack.size() });
            if (!config.BiomeExtendsProcessed)
            {
                if (config.settingsCache.containsKey(BiomeStandardValues.BiomeExtends.name().toLowerCase()))
                {
                    String biomeToExtend_Name = config.settingsCache.get(BiomeStandardValues.BiomeExtends.name().toLowerCase());
                    if (!biomeToExtend_Name.isEmpty())
                    {
                        TerrainControl.log(Level.FINER, "\tBiome(" + biomeToExtend_Name + ") Processing!");
                        //>>	anti-self-inheritance
                        if (biomeToExtend_Name.equals(config.name))
                        {
                            if (!selfInheritanceErrors.isEmpty())
                            {
                                selfInheritanceErrors += ", ";
                            }
                            selfInheritanceErrors += biomeToExtend_Name;
                            TerrainControl.log(Level.FINER, "\t\tBiome(" + biomeToExtend_Name + ":null) being Autosarcophagous!");
                            return;
                        } else
                        {
                            Integer biomeToExtend_Id = this.worldConfig.CustomBiomeIds.get(biomeToExtend_Name);
                            if (biomeToExtend_Id == null) //>>	If not found as custom biome, look at the defaults
                                biomeToExtend_Id = DefaultBiome.getId(biomeToExtend_Name);
                            if (biomeToExtend_Id == null)
                            {
                                TerrainControl.log(Level.WARNING, "\t\tBiomeExtends(" + biomeToExtend_Name + ":null) not found. If you think this is in error, check your configs!");
                            } else
                            {
                                TerrainControl.log(Level.FINER, "\t\tBiome2Extend( " + biomeToExtend_Name + ":" + biomeToExtend_Id + ") was found!");
                                doInheritance(biomeConfigs[biomeToExtend_Id], false);
                                if (isParent == true)
                                {
                                    if (!this.cycleFound)
                                    {
                                        TerrainControl.log(Level.FINEST, "\t\t\tSTACK:::Popping_A");
                                        BiomeConfig parentConfig = biomeLoadingStack.pop();

                                        while (!biomeLoadingStack.isEmpty())
                                        {
                                            TerrainControl.log(Level.FINEST, "\t\t\tSTACK:::Popping_B");
                                            BiomeConfig child = biomeLoadingStack.pop();
                                            TerrainControl.log(Level.FINEST, "\t\t\t\tMerging Biomes (" + parentConfig.name + ":" + child.name + ");");
                                            BiomeConfig merged = child.merge(parentConfig);
                                            if (merged == null)
                                            {
                                                TerrainControl.log(Level.SEVERE, "\t\tBiomeConfig merging returned null!!");
                                            } else
                                            {
                                                //>>	check in the CustomBiomes for the ID
                                                Integer mergedIndex = this.worldConfig.CustomBiomeIds.get(child.name);
                                                //>>	If not go and check the defaults...
                                                mergedIndex = (mergedIndex == null) ? DefaultBiome.getId(child.name) : mergedIndex;
                                                if (mergedIndex == null)
                                                {
                                                    //>>	If nothing found, something is configured wrong
                                                    TerrainControl.log(Level.SEVERE, "\t\tPlease make sure you include {0} in the `Custom biomes` and `Biome Lists` portions of the WorldConfig", new Object[]{ child.name });
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
                        TerrainControl.log(Level.FINEST, "\t\t -A- Biome Does not extend other");
                        biomeLoadingStack.peek().BiomeExtendsProcessed = true;
                    }
                } else
                {
                    TerrainControl.log(Level.FINEST, "\t\t -B- Biome Does not extend other");
                    biomeLoadingStack.peek().BiomeExtendsProcessed = true;
                }
            }
            if (isParent)
            {
                TerrainControl.log(Level.FINEST, "\t\t\tSTACK:::Clearing");
                biomeLoadingStack.clear();
            }
        } else if (biomeLoadingStack.size() >= 2)
        {
            //>>	This forms the cyclical reference chain for output later and
            //>>	adds it to a queue of errors
            StringBuilder cycle = new StringBuilder(" ... <- ");
            BiomeConfig first = biomeLoadingStack.pollLast();
            cycle.append(first.name);
            cycle.append(" <- ");
            while (!biomeLoadingStack.isEmpty())
            {
                cycle.append(biomeLoadingStack.pollLast().name);
                cycle.append(" <- ");
            }
            cycle.append(first.name);
            cycle.append(" <- ...etc.");
            inheritanceErrors.add(cycle);
            this.cycleFound = true;
        } else
        {
            TerrainControl.log(Level.FINEST, "\t\t -- Biome Already Processed");
            //>>	Make sure we count Biomes that have already been processed
            //>>	Without this, pre-processed configs dont get merged.
            biomeLoadingStack.push(config);
        }
    }

    public void writeToStream(DataOutputStream stream) throws IOException
    {
        // General information
        ConfigFile.writeStringToStream(stream, worldConfig.name);

        stream.writeInt(worldConfig.WorldFog);
        stream.writeInt(worldConfig.WorldNightFog);

        // Custom biomes + ids
        stream.writeInt(worldConfig.CustomBiomeIds.size());
        for (Iterator<Entry<String, Integer>> it = worldConfig.CustomBiomeIds.entrySet().iterator(); it.hasNext();)
        {
            Entry<String, Integer> entry = it.next();
            ConfigFile.writeStringToStream(stream, entry.getKey());
            stream.writeInt(entry.getValue());
        }

        // BiomeConfigs
        stream.writeInt(biomesCount);
        for (BiomeConfig config : biomeConfigs)
        {
            if (config == null)
                continue;
            stream.writeInt(config.Biome.getId());
            config.Serialize(stream);
        }
    }

    // Needed for creating world config from network packet
    public WorldSettings(DataInputStream stream, LocalWorld world) throws IOException
    {
        this.BiomeDirs.clear();

        // General information
        worldConfig = new WorldConfig(world);

        worldConfig.WorldFog = stream.readInt();
        worldConfig.WorldNightFog = stream.readInt();

        worldConfig.WorldFogR = ((worldConfig.WorldFog & 0xFF0000) >> 16) / 255F;
        worldConfig.WorldFogG = ((worldConfig.WorldFog & 0xFF00) >> 8) / 255F;
        worldConfig.WorldFogB = (worldConfig.WorldFog & 0xFF) / 255F;

        worldConfig.WorldNightFogR = ((worldConfig.WorldNightFog & 0xFF0000) >> 16) / 255F;
        worldConfig.WorldNightFogG = ((worldConfig.WorldNightFog & 0xFF00) >> 8) / 255F;
        worldConfig.WorldNightFogB = (worldConfig.WorldNightFog & 0xFF) / 255F;

        // Custom biomes + ids
        int count = stream.readInt();
        while (count-- > 0)
        {
            String biomeName = ConfigFile.readStringFromStream(stream);
            int id = stream.readInt();
            world.AddCustomBiome(biomeName, id);
            worldConfig.CustomBiomeIds.put(biomeName, id);
        }
        //TODO Check all this code.
        // BiomeConfigs
        biomeConfigs = new BiomeConfig[world.getMaxBiomesCount()];

        count = stream.readInt();
        while (count-- > 0)
        {
            int id = stream.readInt();
            BiomeConfig config = new BiomeConfig(stream, worldConfig, world.getBiomeById(id));
            biomeConfigs[id] = config;
        }

    }

}