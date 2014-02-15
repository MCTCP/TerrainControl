package com.khorn.terraincontrol.configuration;

import com.khorn.terraincontrol.BiomeIds;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;
import com.khorn.terraincontrol.configuration.standard.WorldStandardValues;
import com.khorn.terraincontrol.util.helpers.FileHelper;

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
public class WorldSettings
{

    private static final int MAX_INHERITANCE_DEPTH = 15;
    private LocalWorld world;
    public WorldConfig worldConfig;

    /**
     * Holds all biome configs. Generation Id => BiomeConfig
     * 
     * Must be simple array for fast access. Warning: some ids may contain
     * null values, always check
     */
    public BiomeConfig[] biomeConfigs;

    /**
     * Holds all biome configs that aren't virtual. These need to be sent to
     * all players on the server that have Terrain Control installed.
     */
    private final Collection<BiomeConfig> savedBiomes;

    /**
     * Set this to true to skip indexing of settings and avoiding tampering
     * with the array in Minecraft's BiomeBase class.
     */
    private final boolean checkOnly;

    /**
     * The number of loaded biomes.
     */
    public int biomesCount;

    public byte[] replaceToBiomeNameMatrix = new byte[256];

    public WorldSettings(File settingsDir, LocalWorld world, boolean checkOnly)
    {
        this.world = world;
        this.worldConfig = new WorldConfig(settingsDir, world);
        this.checkOnly = checkOnly;

        // Establish folders
        List<File> biomeDirs = new ArrayList<File>(2);
        // TerrainControl/worlds/<WorldName>/<WorldBiomes/
        biomeDirs.add(new File(settingsDir, correctOldBiomeConfigFolder(settingsDir)));
        // TerrainControl/GlobalBiomes/
        biomeDirs.add(new File(TerrainControl.getEngine().getTCDataFolder(), PluginStandardValues.BiomeConfigDirectoryName.stringValue()));
        
        FileHelper.makeFolders(biomeDirs);

        // Build biome replace matrix (by default, biomes are replaced with
        // itself)
        for (int i = 0; i < this.replaceToBiomeNameMatrix.length; i++)
        {
            this.replaceToBiomeNameMatrix[i] = (byte) i;
        }

        // Build a set of all biomes to load
        Set<LocalBiome> biomesToLoad = new HashSet<LocalBiome>();
        biomesToLoad.addAll(world.getDefaultBiomes());

        // This adds all custombiomes that have been listed in WorldConfig to
        // the arrayList
        for (Entry<String, BiomeIds> entry : worldConfig.CustomBiomeIds.entrySet())
        {
            String biomeName = entry.getKey();
            if (checkOnly)
            {
                biomesToLoad.add(world.getNullBiome(biomeName));
            } else
            {
                BiomeIds id = entry.getValue();
                biomesToLoad.add(world.addCustomBiome(entry.getKey(), id));
            }
        }

        // Load all files
        BiomeConfigFinder biomeConfigFinder = new BiomeConfigFinder(worldConfig, TerrainControl.getPluginConfig().biomeConfigExtension,
                world.getMaxBiomesCount());
        this.biomeConfigs = biomeConfigFinder.loadBiomesFromDirectories(biomeDirs, biomesToLoad);

        // Read all settings
        this.savedBiomes = new HashSet<BiomeConfig>();
        String loadedBiomeNames = readSettings();

        TerrainControl.log(Level.INFO, "{0} biomes Loaded", new Object[] {biomesCount});
        TerrainControl.log(Level.CONFIG, "{0}", new Object[] {loadedBiomeNames});

    }

    private String readSettings()
    {
        StringBuilder loadedBiomeNames = new StringBuilder();
        for (BiomeConfig biomeConfig : this.biomeConfigs)
        {
            if (biomeConfig == null)
            {
                continue;
            }

            // Statistics of the loaded biomes
            this.biomesCount++;
            loadedBiomeNames.append(biomeConfig.name);
            loadedBiomeNames.append(", ");

            // Check real id of virtual biomes
            BiomeIds id = biomeConfig.Biome.getIds();
            if (id.isVirtual() && world.getBiomeById(id.getGenerationId()) == null)
            {
                TerrainControl.log(Level.WARNING,
                        "All virtual biomes need a real biome to be replaced to. The id {0} was not found for biome {1}.",
                        new Object[] {id.getSavedId(), biomeConfig.name});
            }

            // Inheritance
            processInheritance(biomeConfig, 0);
            
            // Settings reading
            biomeConfig.process();

            // Skip indexing when only checking
            if (this.checkOnly)
            {
                continue;
            }
            
            // If not virtual, add to set
            if (!biomeConfig.Biome.getIds().isVirtual()) {
                savedBiomes.add(biomeConfig);
            }

            // Indexing ReplacedBlocks
            if (!this.worldConfig.BiomeConfigsHaveReplacement)
            {
                this.worldConfig.BiomeConfigsHaveReplacement = biomeConfig.replacedBlocks.hasReplaceSettings();
            }

            // Indexing ReplaceToBiomeName
            if (!biomeConfig.ReplaceBiomeName.isEmpty())
            {
                this.worldConfig.HaveBiomeReplace = true;
                this.replaceToBiomeNameMatrix[biomeConfig.Biome.getIds().getGenerationId()] = (byte) world
                        .getBiomeIdByName(biomeConfig.ReplaceBiomeName);
            }

            // Indexing BiomeRarity
            if (this.worldConfig.NormalBiomes.contains(biomeConfig.name))
            {
                this.worldConfig.normalBiomesRarity += biomeConfig.BiomeRarity;
            }
            if (this.worldConfig.IceBiomes.contains(biomeConfig.name))
            {
                this.worldConfig.iceBiomesRarity += biomeConfig.BiomeRarity;
            }

            // Indexing MaxSmoothRadius
            if (this.worldConfig.maxSmoothRadius < biomeConfig.SmoothRadius)
            {
                this.worldConfig.maxSmoothRadius = biomeConfig.SmoothRadius;
            }

            // Indexing BiomeColor
            if (this.worldConfig.biomeMode == TerrainControl.getBiomeModeManager().FROM_IMAGE)
            {
                if (this.worldConfig.biomeColorMap == null)
                {
                    this.worldConfig.biomeColorMap = new HashMap<Integer, Integer>();
                }

                try
                {
                    int color = Integer.decode(biomeConfig.BiomeColor);
                    if (color <= 0xFFFFFF)
                    {
                        this.worldConfig.biomeColorMap.put(color, biomeConfig.Biome.getIds().getGenerationId());
                    }
                } catch (NumberFormatException ex)
                {
                    TerrainControl.log(Level.WARNING, "Wrong color in " + biomeConfig.Biome.getName());
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

    private void processInheritance(BiomeConfig biomeConfig, int currentDepth)
    {
        if (biomeConfig.BiomeExtendsProcessed)
        {
            // Already processed earlier
            return;
        }

        String extendedBiomeName = biomeConfig.BiomeExtends;
        if (extendedBiomeName == null || extendedBiomeName.length() == 0)
        {
            // Not extending anything
            biomeConfig.BiomeExtendsProcessed = true;
            return;
        }

        // This biome extends another biome
        BiomeConfig extendedBiomeConfig = biomeConfigs[world.getBiomeIdByName(extendedBiomeName)];
        if (extendedBiomeConfig == null)
        {
            TerrainControl.log(Level.WARNING, "The biome {0} tried to extend the biome {1}, but that biome doesn't exist.", new Object[] {
                    biomeConfig.name, extendedBiomeName});
            return;
        }

        // Check for too much recursion
        if (currentDepth > MAX_INHERITANCE_DEPTH)
        {
            TerrainControl.log(Level.SEVERE,
                    "The biome {0} cannot extend the biome {1} - too much configs processed already! Cyclical inheritance?", new Object[] {
                            biomeConfig.name, extendedBiomeConfig.name});
        }

        if (!extendedBiomeConfig.BiomeExtendsProcessed)
        {
            // This biome has not been processed yet, do that first
            processInheritance(extendedBiomeConfig, currentDepth + 1);
        }

        // Merge the two
        biomeConfig.merge(extendedBiomeConfig);

        // Done
        biomeConfig.BiomeExtendsProcessed = true;
    }

    // Read settings from the network
    public WorldSettings(DataInputStream stream, LocalWorld world) throws IOException
    {
        this.checkOnly = false;

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
            BiomeIds id = new BiomeIds(stream.readInt());
            world.addCustomBiome(biomeName, id);
            worldConfig.CustomBiomeIds.put(biomeName, id);
        }

        // BiomeConfigs
        biomeConfigs = new BiomeConfig[world.getMaxBiomesCount()];

        count = stream.readInt();
        while (count-- > 0)
        {
            int id = stream.readInt();
            BiomeConfig config = new BiomeConfig(stream, worldConfig, world.getBiomeById(id));
            biomeConfigs[id] = config;
        }
        
        savedBiomes = Arrays.asList(biomeConfigs);
    }

    private String correctOldBiomeConfigFolder(File settingsDir)
    {
        // Rename the old folder
        String biomeFolderName = WorldStandardValues.BiomeConfigDirectoryName.stringValue();
        File oldBiomeConfigs = new File(settingsDir, "BiomeConfigs");
        if (oldBiomeConfigs.exists())
        {
            if (!oldBiomeConfigs.renameTo(new File(settingsDir, biomeFolderName)))
            {
                TerrainControl.log(Level.WARNING, "========================");
                TerrainControl.log(Level.WARNING, "Fould old `BiomeConfigs` folder, but it could not be renamed to `", biomeFolderName,
                        "`!");
                TerrainControl.log(Level.WARNING, "Please rename the folder manually.");
                TerrainControl.log(Level.WARNING, "========================");
                biomeFolderName = "BiomeConfigs";
            }
        }
        return biomeFolderName;
    }

    public void writeToStream(DataOutputStream stream) throws IOException
    {
        // General information
        ConfigFile.writeStringToStream(stream, worldConfig.name);

        stream.writeInt(worldConfig.WorldFog);
        stream.writeInt(worldConfig.WorldNightFog);

        // Custom biomes + ids
        stream.writeInt(worldConfig.CustomBiomeIds.size());
        for (Entry<String, BiomeIds> entry : worldConfig.CustomBiomeIds.entrySet())
        {
            ConfigFile.writeStringToStream(stream, entry.getKey());
            stream.writeInt(entry.getValue().getSavedId());
        }

        // BiomeConfigs
        stream.writeInt(savedBiomes.size());
        for (BiomeConfig config : savedBiomes)
        {
            if (config == null)
            {
                continue;
            }
            stream.writeInt(config.Biome.getIds().getSavedId());
            config.Serialize(stream);
        }
    }

}
