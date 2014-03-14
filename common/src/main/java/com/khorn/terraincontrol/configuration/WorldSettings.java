package com.khorn.terraincontrol.configuration;

import com.khorn.terraincontrol.BiomeIds;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;
import com.khorn.terraincontrol.configuration.standard.StandardBiomeTemplate;
import com.khorn.terraincontrol.configuration.standard.WorldStandardValues;
import com.khorn.terraincontrol.logging.LogMarker;
import com.khorn.terraincontrol.util.helpers.FileHelper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

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
     * null values, always check.
     */
    public LocalBiome[] biomes;

    /**
     * Holds all biomes that aren't virtual. These need to be sent to all
     * players on the server that have Terrain Control installed.
     */
    private final Collection<LocalBiome> savedBiomes;

    /**
     * Set this to true to skip indexing of settings and avoiding tampering
     * with the array in Minecraft's BiomeBase class.
     */
    private final boolean checkOnly;

    /**
     * The number of loaded biomes.
     */
    public int biomesCount;

    public WorldSettings(File settingsDir, LocalWorld world, boolean checkOnly)
    {
        this.world = world;
        this.worldConfig = new WorldConfig(settingsDir, world);
        this.checkOnly = checkOnly;
        this.biomes = new LocalBiome[world.getMaxBiomesCount()];

        // Establish folders
        List<File> biomeDirs = new ArrayList<File>(2);
        // TerrainControl/worlds/<WorldName>/<WorldBiomes/
        biomeDirs.add(new File(settingsDir, correctOldBiomeConfigFolder(settingsDir)));
        // TerrainControl/GlobalBiomes/
        biomeDirs.add(new File(TerrainControl.getEngine().getTCDataFolder(), PluginStandardValues.BiomeConfigDirectoryName.stringValue()));

        FileHelper.makeFolders(biomeDirs);

        // Build a set of all biomes to load
        Collection<BiomeLoadInstruction> biomesToLoad = new HashSet<BiomeLoadInstruction>();
        biomesToLoad.addAll(world.getDefaultBiomes());

        // This adds all custombiomes that have been listed in WorldConfig to
        // the arrayList
        for (Entry<String, BiomeIds> entry : worldConfig.CustomBiomeIds.entrySet())
        {
            String biomeName = entry.getKey();
            BiomeIds ids = entry.getValue();
            biomesToLoad.add(new BiomeLoadInstruction(biomeName, ids.getGenerationId(), new StandardBiomeTemplate(
                    worldConfig.worldHeightScale)));
        }

        // Load all files
        BiomeConfigFinder biomeConfigFinder = new BiomeConfigFinder(worldConfig, TerrainControl.getPluginConfig().biomeConfigExtension);
        Map<String, BiomeConfig> biomeConfigs = biomeConfigFinder.loadBiomesFromDirectories(biomeDirs, biomesToLoad);

        // Read all settings
        this.savedBiomes = new HashSet<LocalBiome>();
        String loadedBiomeNames = readSettings(biomeConfigs);

        // Save all settings
        saveSettings();

        TerrainControl.log(LogMarker.INFO, "{} biomes Loaded", new Object[] {biomesCount});
        TerrainControl.log(LogMarker.DEBUG, "{}", new Object[] {loadedBiomeNames});

    }

    private String readSettings(Map<String, BiomeConfig> biomeConfigs)
    {
        StringBuilder loadedBiomeNames = new StringBuilder();
        for (BiomeConfig biomeConfig : biomeConfigs.values())
        {
            if (biomeConfig == null)
            {
                continue;
            }

            // Statistics of the loaded biomes
            this.biomesCount++;
            loadedBiomeNames.append(biomeConfig.name);
            loadedBiomeNames.append(", ");

            // Inheritance
            processInheritance(biomeConfigs, biomeConfig, 0);

            // Settings reading
            biomeConfig.process();

            // Skip indexing when only checking
            if (this.checkOnly)
            {
                continue;
            }

            // Check for id conflicts
            int generationId = biomeConfig.generationId;
            if (biomes[generationId] != null)
            {
                TerrainControl.log(LogMarker.FATAL, "Duplicate biome id {} ({} and {})!", generationId, biomes[generationId].getName(),
                        biomeConfig.name);
                TerrainControl.log(LogMarker.FATAL, "The biome {} has been prevented from loading.", new Object[] {biomeConfig.name});
                TerrainControl.log(LogMarker.INFO, "If you are updating an old pre-Minecraft 1.7 world, please read this wiki page:");
                TerrainControl.log(LogMarker.INFO, "https://github.com/Wickth/TerrainControl/wiki/Upgrading-an-old-map-to-Minecraft-1.7");

                continue;
            }

            // Get correct saved id (defaults to generation id, but can be set
            // to use the generation id of another biome)
            int savedId = biomeConfig.generationId;
            if (!biomeConfig.ReplaceBiomeName.isEmpty())
            {
                BiomeConfig replaceToConfig = biomeConfigs.get(biomeConfig.ReplaceBiomeName);
                if (replaceToConfig == null)
                {
                    biomeConfig.ReplaceBiomeName = "";
                    TerrainControl.log(LogMarker.WARN, "Invalid ReplaceToBiomeName in biome {}: biome {} doesn't exist", biomeConfig.name,
                            biomeConfig.ReplaceBiomeName);
                } else
                {
                    savedId = replaceToConfig.generationId;
                }
            }

            LocalBiome biome = world.createBiomeFor(biomeConfig, new BiomeIds(biomeConfig.generationId, savedId));
            this.biomes[biome.getIds().getGenerationId()] = biome;

            // If not virtual, add to saved biomes set
            if (!biome.getIds().isVirtual())
            {
                savedBiomes.add(biome);
            }

            // Indexing ReplacedBlocks
            if (!this.worldConfig.BiomeConfigsHaveReplacement)
            {
                this.worldConfig.BiomeConfigsHaveReplacement = biomeConfig.replacedBlocks.hasReplaceSettings();
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
                        this.worldConfig.biomeColorMap.put(color, biome.getIds().getGenerationId());
                    }
                } catch (NumberFormatException ex)
                {
                    TerrainControl.log(LogMarker.WARN, "Wrong color in {}", biomeConfig.name);
                }
            }

            // Setting effects
            biome.setEffects();
        }

        if (this.biomesCount > 0)
        {
            // Remove last ", "
            loadedBiomeNames.delete(loadedBiomeNames.length() - 2, loadedBiomeNames.length());
        }
        return loadedBiomeNames.toString();
    }

    private void saveSettings()
    {
        for (LocalBiome biome : this.biomes)
        {
            if (biome != null)
            {
                biome.getBiomeConfig().outputToFile();
            }
        }
    }

    private void processInheritance(Map<String, BiomeConfig> allBiomeConfigs, BiomeConfig biomeConfig, int currentDepth)
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
        BiomeConfig extendedBiomeConfig = allBiomeConfigs.get(extendedBiomeName);
        if (extendedBiomeConfig == null)
        {
            TerrainControl.log(LogMarker.WARN, "The biome {} tried to extend the biome {}, but that biome doesn't exist.", new Object[] {
                    biomeConfig.name, extendedBiomeName});
            return;
        }

        // Check for too much recursion
        if (currentDepth > MAX_INHERITANCE_DEPTH)
        {
            TerrainControl.log(LogMarker.FATAL,
                    "The biome {} cannot extend the biome {} - too much configs processed already! Cyclical inheritance?", new Object[] {
                            biomeConfig.name, extendedBiomeConfig.name});
        }

        if (!extendedBiomeConfig.BiomeExtendsProcessed)
        {
            // This biome has not been processed yet, do that first
            processInheritance(allBiomeConfigs, extendedBiomeConfig, currentDepth + 1);
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
            worldConfig.CustomBiomeIds.put(biomeName, id);
        }

        // BiomeConfigs
        biomes = new LocalBiome[world.getMaxBiomesCount()];

        count = stream.readInt();
        while (count-- > 0)
        {
            int id = stream.readInt();
            BiomeConfig config = new BiomeConfig(stream, id, worldConfig);
            LocalBiome biome = world.createBiomeFor(config, new BiomeIds(id));
            biomes[id] = biome;
            biome.setEffects();
        }

        savedBiomes = Arrays.asList(biomes);
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
                TerrainControl.log(LogMarker.WARN, "========================");
                TerrainControl.log(LogMarker.WARN, "Fould old `BiomeConfigs` folder, but it could not be renamed to `", biomeFolderName,
                        "`!");
                TerrainControl.log(LogMarker.WARN, "Please rename the folder manually.");
                TerrainControl.log(LogMarker.WARN, "========================");
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
        for (LocalBiome biome : savedBiomes)
        {
            if (biome == null)
            {
                continue;
            }
            stream.writeInt(biome.getIds().getSavedId());
            biome.getBiomeConfig().writeToStream(stream);
        }
    }

}
