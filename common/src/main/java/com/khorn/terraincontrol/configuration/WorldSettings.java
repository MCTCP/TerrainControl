package com.khorn.terraincontrol.configuration;

import com.khorn.terraincontrol.BiomeIds;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.io.FileSettingsReader;
import com.khorn.terraincontrol.configuration.io.FileSettingsWriter;
import com.khorn.terraincontrol.configuration.io.MemorySettingsReader;
import com.khorn.terraincontrol.configuration.io.SettingsReader;
import com.khorn.terraincontrol.configuration.standard.BiomeStandardValues;
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
    private File settingsDir;
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
    private final Collection<LocalBiome> savedBiomes = new HashSet<LocalBiome>();

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
        this.settingsDir = settingsDir;
        this.world = world;
        File worldConfigFile = new File(settingsDir, WorldStandardValues.WORLD_CONFIG_FILE_NAME);
        this.worldConfig = new WorldConfig(new FileSettingsReader(world.getName(), worldConfigFile), world);
        FileSettingsWriter.writeToFile(worldConfig, worldConfig.SettingsMode);
        this.checkOnly = checkOnly;
        this.biomes = new LocalBiome[world.getMaxBiomesCount()];

        load();
    }

    private void load()
    {
        // Establish folders
        List<File> biomeDirs = new ArrayList<File>(2);
        // TerrainControl/worlds/<WorldName>/<WorldBiomes/
        biomeDirs.add(new File(settingsDir, correctOldBiomeConfigFolder(settingsDir)));
        // TerrainControl/GlobalBiomes/
        biomeDirs.add(new File(TerrainControl.getEngine().getTCDataFolder(), PluginStandardValues.BiomeConfigDirectoryName));

        FileHelper.makeFolders(biomeDirs);

        // Build a set of all biomes to load
        Collection<BiomeLoadInstruction> biomesToLoad = new HashSet<BiomeLoadInstruction>();
        biomesToLoad.addAll(world.getDefaultBiomes());

        // This adds all custombiomes that have been listed in WorldConfig to
        // the arrayList
        for (Entry<String, Integer> entry : worldConfig.customBiomeGenerationIds.entrySet())
        {
            String biomeName = entry.getKey();
            int generationId = entry.getValue();
            biomesToLoad.add(new BiomeLoadInstruction(biomeName, generationId, new StandardBiomeTemplate(
                    worldConfig.worldHeightScale)));
        }

        // Load all files
        BiomeConfigFinder biomeConfigFinder = new BiomeConfigFinder(worldConfig, TerrainControl.getPluginConfig().biomeConfigExtension);
        Map<String, BiomeConfig> biomeConfigs = biomeConfigFinder.loadBiomesFromDirectories(biomeDirs, biomesToLoad);

        // Read all settings
        String loadedBiomeNames = readSettings(biomeConfigs);

        // Save all settings
        saveSettings();

        TerrainControl.log(LogMarker.INFO, "{} biomes Loaded", new Object[] {biomesCount});
        TerrainControl.log(LogMarker.DEBUG, "{}", new Object[] {loadedBiomeNames});
    }

    /**
     * Reloads the settings from disk.
     */
    public void reload()
    {
        // Clear biome collections
        Arrays.fill(this.biomes, null);
        this.savedBiomes.clear();
        this.biomesCount = 0;

        // Load again
        load();
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
            loadedBiomeNames.append(biomeConfig.getName());
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

            // Check generation id range
            int generationId = biomeConfig.generationId;
            if (generationId < 0 || generationId >= world.getMaxBiomesCount())
            {
                TerrainControl.log(LogMarker.ERROR,
                        "The biome id of the {} biome, {}, is too high. It must be between 0 and {}, inclusive.",
                        biomeConfig.getName(), generationId, world.getMaxBiomesCount() - 1);
                TerrainControl.log(LogMarker.ERROR, "The biome has been prevented from loading.");
                continue;
            }

            // Check for id conflicts
            if (biomes[generationId] != null)
            {
                TerrainControl.log(LogMarker.FATAL, "Duplicate biome id {} ({} and {})!", generationId, biomes[generationId].getName(),
                        biomeConfig.getName());
                TerrainControl.log(LogMarker.FATAL, "The biome {} has been prevented from loading.", new Object[] {biomeConfig.getName()});
                TerrainControl.log(LogMarker.INFO, "If you are updating an old pre-Minecraft 1.7 world, please read this wiki page:");
                TerrainControl.log(LogMarker.INFO, "https://github.com/Wickth/TerrainControl/wiki/Upgrading-an-old-map-to-Minecraft-1.7");
                continue;
            }

            // Get correct saved id (defaults to generation id, but can be set
            // to use the generation id of another biome)
            int savedId = biomeConfig.generationId;
            if (!biomeConfig.replaceToBiomeName.isEmpty())
            {
                BiomeConfig replaceToConfig = biomeConfigs.get(biomeConfig.replaceToBiomeName);
                if (replaceToConfig == null)
                {
                    biomeConfig.replaceToBiomeName = "";
                    TerrainControl.log(LogMarker.WARN, "Invalid ReplaceToBiomeName in biome {}: biome {} doesn't exist", biomeConfig.getName(),
                            biomeConfig.replaceToBiomeName);
                } else
                {
                    savedId = replaceToConfig.generationId;
                }
            }

            // Check saved id range
            if (savedId >= world.getMaxSavedBiomesCount())
            {
                TerrainControl.log(LogMarker.ERROR,
                        "Biomes with an id between {} and {} (inclusive) must have a valid ReplaceToBiomeName setting:",
                        world.getMaxBiomesCount(), world.getMaxSavedBiomesCount() - 1);
                TerrainControl.log(LogMarker.ERROR, "Minecraft can only save biomes with an id between 0 and {}, inclusive.",
                        world.getMaxBiomesCount() - 1);
                TerrainControl.log(LogMarker.ERROR, "This means that the biome {} with map file id {} had to be prevented from loading.",
                        biomeConfig.getName(), savedId);
                continue;
            }

            // Create biome
            LocalBiome biome = world.createBiomeFor(biomeConfig, new BiomeIds(generationId, savedId));
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
            if (this.worldConfig.NormalBiomes.contains(biomeConfig.getName()))
            {
                this.worldConfig.normalBiomesRarity += biomeConfig.biomeRarity;
            }
            if (this.worldConfig.IceBiomes.contains(biomeConfig.getName()))
            {
                this.worldConfig.iceBiomesRarity += biomeConfig.biomeRarity;
            }

            // Indexing MaxSmoothRadius
            if (this.worldConfig.maxSmoothRadius < biomeConfig.smoothRadius)
            {
                this.worldConfig.maxSmoothRadius = biomeConfig.smoothRadius;
            }

            // Indexing BiomeColor
            if (this.worldConfig.biomeMode == TerrainControl.getBiomeModeManager().FROM_IMAGE)
            {
                if (this.worldConfig.biomeColorMap == null)
                {
                    this.worldConfig.biomeColorMap = new HashMap<Integer, Integer>();
                }

                int color = biomeConfig.biomeColor;
                this.worldConfig.biomeColorMap.put(color, biome.getIds().getGenerationId());
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
        if (biomeConfig.biomeExtendsProcessed)
        {
            // Already processed earlier
            return;
        }

        String extendedBiomeName = biomeConfig.biomeExtends;
        if (extendedBiomeName == null || extendedBiomeName.length() == 0)
        {
            // Not extending anything
            biomeConfig.biomeExtendsProcessed = true;
            return;
        }

        // This biome extends another biome
        BiomeConfig extendedBiomeConfig = allBiomeConfigs.get(extendedBiomeName);
        if (extendedBiomeConfig == null)
        {
            TerrainControl.log(LogMarker.WARN, "The biome {} tried to extend the biome {}, but that biome doesn't exist.", new Object[] {
                    biomeConfig.getName(), extendedBiomeName});
            return;
        }

        // Check for too much recursion
        if (currentDepth > MAX_INHERITANCE_DEPTH)
        {
            TerrainControl.log(LogMarker.FATAL,
                    "The biome {} cannot extend the biome {} - too much configs processed already! Cyclical inheritance?", new Object[] {
                            biomeConfig.getName(), extendedBiomeConfig.getName()});
        }

        if (!extendedBiomeConfig.biomeExtendsProcessed)
        {
            // This biome has not been processed yet, do that first
            processInheritance(allBiomeConfigs, extendedBiomeConfig, currentDepth + 1);
        }

        // Merge the two
        biomeConfig.merge(extendedBiomeConfig);

        // Done
        biomeConfig.biomeExtendsProcessed = true;
    }

    // Read settings from the network
    public WorldSettings(DataInputStream stream, LocalWorld world) throws IOException
    {
        this.checkOnly = false;

        // Create WorldConfig
        SettingsReader worldSettingsReader = new MemorySettingsReader(world.getName());
        worldSettingsReader.putSetting(WorldStandardValues.WORLD_FOG, stream.readInt());
        worldSettingsReader.putSetting(WorldStandardValues.WORLD_NIGHT_FOG, stream.readInt());
        worldConfig = new WorldConfig(worldSettingsReader, world);

        // Custom biomes + ids
        int count = stream.readInt();
        while (count-- > 0)
        {
            String biomeName = ConfigFile.readStringFromStream(stream);
            int id = stream.readInt();
            worldConfig.customBiomeGenerationIds.put(biomeName, id);
        }

        // BiomeConfigs
        StandardBiomeTemplate defaultSettings = new StandardBiomeTemplate(worldConfig.worldHeightCap);
        biomes = new LocalBiome[world.getMaxBiomesCount()];

        count = stream.readInt();
        while (count-- > 0)
        {
            int id = stream.readInt();
            String biomeName = ConfigFile.readStringFromStream(stream);
            SettingsReader biomeReader = new MemorySettingsReader(biomeName);
            biomeReader.putSetting(BiomeStandardValues.BIOME_TEMPERATURE, stream.readFloat());
            biomeReader.putSetting(BiomeStandardValues.BIOME_WETNESS, stream.readFloat());
            biomeReader.putSetting(BiomeStandardValues.SKY_COLOR, stream.readInt());
            biomeReader.putSetting(BiomeStandardValues.WATER_COLOR, stream.readInt());
            biomeReader.putSetting(BiomeStandardValues.GRASS_COLOR, stream.readInt());
            biomeReader.putSetting(BiomeStandardValues.GRASS_COLOR_IS_MULTIPLIER, stream.readBoolean());
            biomeReader.putSetting(BiomeStandardValues.FOLIAGE_COLOR, stream.readInt());
            biomeReader.putSetting(BiomeStandardValues.FOLIAGE_COLOR_IS_MULTIPLIER, stream.readBoolean());

            BiomeLoadInstruction instruction = new BiomeLoadInstruction(biomeName, id, defaultSettings);
            BiomeConfig config = new BiomeConfig(biomeReader, instruction, worldConfig);
            config.process();
            LocalBiome biome = world.createBiomeFor(config, new BiomeIds(id));

            biomes[id] = biome;
            biome.setEffects();
        }

        savedBiomes.addAll(Arrays.asList(biomes));
    }

    private String correctOldBiomeConfigFolder(File settingsDir)
    {
        // Rename the old folder
        String biomeFolderName = WorldStandardValues.WORLD_BIOMES_DIRECTORY_NAME;
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
        ConfigFile.writeStringToStream(stream, worldConfig.getName());

        stream.writeInt(worldConfig.WorldFog);
        stream.writeInt(worldConfig.WorldNightFog);

        // Fetch all non-virtual custom biomes
        Collection<LocalBiome> nonVirtualCustomBiomes = new ArrayList<LocalBiome>(worldConfig.customBiomeGenerationIds.size());
        for (Integer generationId : worldConfig.customBiomeGenerationIds.values())
        {
            LocalBiome biome = biomes[generationId];
            if (!biome.getIds().isVirtual()) {
                nonVirtualCustomBiomes.add(biome);
            }
        }

        // Write them to the stream
        stream.writeInt(nonVirtualCustomBiomes.size());
        for (LocalBiome biome : nonVirtualCustomBiomes)
        {
            ConfigFile.writeStringToStream(stream, biome.getName());
            stream.writeInt(biome.getIds().getSavedId());
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
