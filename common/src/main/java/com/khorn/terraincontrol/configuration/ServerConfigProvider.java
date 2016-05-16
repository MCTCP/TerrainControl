package com.khorn.terraincontrol.configuration;

import com.khorn.terraincontrol.BiomeIds;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.io.FileSettingsReader;
import com.khorn.terraincontrol.configuration.io.FileSettingsWriter;
import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;
import com.khorn.terraincontrol.configuration.standard.StandardBiomeTemplate;
import com.khorn.terraincontrol.configuration.standard.WorldStandardValues;
import com.khorn.terraincontrol.customobjects.CustomObjectCollection;
import com.khorn.terraincontrol.customobjects.CustomObjectLoader;
import com.khorn.terraincontrol.logging.LogMarker;
import com.khorn.terraincontrol.util.helpers.FileHelper;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;

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
    private CustomObjectCollection customObjects;
    private WorldConfig worldConfig;

    /**
     * Holds all biome configs. Generation Id => BiomeConfig
     * <p>
     * Must be simple array for fast access. Warning: some ids may contain
     * null values, always check.
     */
    private LocalBiome[] biomes;

    /**
     * Holds all biomes that aren't virtual. These need to be sent to all
     * players on the server that have Terrain Control installed.
     */
    private final Collection<LocalBiome> savedBiomes = new HashSet<LocalBiome>();

    /**
     * The number of loaded biomes.
     */
    private int biomesCount;

    /**
     * Loads the settings from the given directory for the given world.
     * @param settingsDir The directory to load from.
     * @param world       The world to load the settings for.
     */
    public ServerConfigProvider(File settingsDir, LocalWorld world)
    {
        this.settingsDir = settingsDir;
        this.world = world;
        this.biomes = new LocalBiome[world.getMaxBiomesCount()];

        loadSettings();
    }

    /**
     * Loads all settings. Expects the biomes array to be empty (filled with
     * nulls), the savedBiomes collection to be empty and the biomesCount
     * field to be zero.
     */
    private void loadSettings()
    {

        loadCustomObjects();
        loadWorldConfig();
        loadBiomes();

        // We have to wait for the loading in order to get things like
        // temperature
        worldConfig.biomeGroupManager.processBiomeData(world);
    }

    private void loadCustomObjects()
    {
        File worldObjectsDir = new File(settingsDir, WorldStandardValues.WORLD_OBJECTS_DIRECTORY_NAME);

        // Migrate folders
        File oldWorldObjectsDir = new File(settingsDir, "BOBPlugins");
        if (!FileHelper.migrateFolder(oldWorldObjectsDir, worldObjectsDir))
        {
            TerrainControl.log(LogMarker.WARN, "Failed to move old world"
                    + " custom objects from {} to {} in world {}."
                    + " Please move the old objects manually.",
                    oldWorldObjectsDir.getName(), worldObjectsDir.getName(), world.getName());
        }

        Map<String, CustomObjectLoader> objectLoaders =
                TerrainControl.getCustomObjectManager().getObjectLoaders();

        customObjects = new CustomObjectCollection(objectLoaders, worldObjectsDir);
        customObjects.setFallback(TerrainControl.getCustomObjectManager().getGlobalObjects());
        TerrainControl.log(LogMarker.INFO, "{} world custom objects loaded.", customObjects.getAll().size());
    }

    private void loadWorldConfig()
    {
        File worldConfigFile = new File(settingsDir, WorldStandardValues.WORLD_CONFIG_FILE_NAME);
        this.worldConfig = new WorldConfig(new FileSettingsReader(world.getName(), worldConfigFile), world, customObjects);
        FileSettingsWriter.writeToFile(worldConfig, worldConfig.SettingsMode);

    }

    private void loadBiomes()
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

    @Override
    public WorldConfig getWorldConfig()
    {
        return worldConfig;
    }

    @Override
    public LocalBiome getBiomeByIdOrNull(int id)
    {
        if (id < 0 || id > biomes.length)
        {
            return null;
        }
        return biomes[id];
    }

    @Override
    public void reload()
    {
        // Clear biome collections
        Arrays.fill(this.biomes, null);
        this.savedBiomes.clear();
        this.biomesCount = 0;

        // Load again
        loadSettings();
    }

    private String readSettings(Map<String, BiomeConfig> biomeConfigs)
    {
        StringBuilder loadedBiomeNames = new StringBuilder();
        for (BiomeConfig biomeConfig : biomeConfigs.values())
        {
            // Inheritance
            processInheritance(biomeConfigs, biomeConfig, 0);

            // Settings reading
            biomeConfig.process();
        }

        // Now that all settings are loaded, we can index them,
        // cross-reference between biomes, etc.
        for (BiomeConfig biomeConfig : biomeConfigs.values())
        {
            // Statistics of the loaded biomes
            this.biomesCount++;
            loadedBiomeNames.append(biomeConfig.getName());
            loadedBiomeNames.append(", ");

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
                TerrainControl.log(LogMarker.FATAL, "The biome {} has been prevented from loading.", new Object[]{biomeConfig.getName()});
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
                    TerrainControl.log(LogMarker.WARN, "Invalid ReplaceToBiomeName in biome {}: biome {} doesn't exist", biomeConfig.getName(),
                            biomeConfig.replaceToBiomeName);
                    biomeConfig.replaceToBiomeName = "";
                } else if (!replaceToConfig.replaceToBiomeName.isEmpty())
                {
                    TerrainControl.log(LogMarker.WARN, "Invalid ReplaceToBiomeName in biome {}: biome {} also has a ReplaceToBiomeName value",
                            biomeConfig.getName(), biomeConfig.replaceToBiomeName);
                    biomeConfig.replaceToBiomeName = "";
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

    @Override
    public LocalBiome[] getBiomeArray()
    {
        return this.biomes;
    }

    @Override
    public CustomObjectCollection getCustomObjects()
    {
        return customObjects;
    }

}
