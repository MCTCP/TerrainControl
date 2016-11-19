package com.khorn.terraincontrol.configuration;

import com.khorn.terraincontrol.BiomeIds;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.BiomeConfigFinder.BiomeConfigStub;
import com.khorn.terraincontrol.configuration.io.FileSettingsReader;
import com.khorn.terraincontrol.configuration.io.FileSettingsWriter;
import com.khorn.terraincontrol.configuration.io.SettingsMap;
import com.khorn.terraincontrol.configuration.standard.BiomeStandardValues;
import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;
import com.khorn.terraincontrol.configuration.standard.StandardBiomeTemplate;
import com.khorn.terraincontrol.configuration.standard.WorldStandardValues;
import com.khorn.terraincontrol.customobjects.CustomObjectCollection;
import com.khorn.terraincontrol.customobjects.CustomObjectLoader;
import com.khorn.terraincontrol.logging.LogMarker;
import com.khorn.terraincontrol.util.helpers.FileHelper;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultBiome;

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

        SettingsMap worldConfigSettings = loadWorldConfig();
        loadBiomes(worldConfigSettings);

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

        Map<String, CustomObjectLoader> objectLoaders = TerrainControl.getCustomObjectManager().getObjectLoaders();

        customObjects = new CustomObjectCollection(objectLoaders, worldObjectsDir);
        customObjects.setFallback(TerrainControl.getCustomObjectManager().getGlobalObjects());
        TerrainControl.log(LogMarker.INFO, "{} world custom objects loaded.", customObjects.getAll().size());
    }

    private SettingsMap loadWorldConfig()
    {
        File worldConfigFile = new File(settingsDir, WorldStandardValues.WORLD_CONFIG_FILE_NAME);
        SettingsMap settingsMap = FileSettingsReader.read(world.getName(), worldConfigFile);
        this.worldConfig = new WorldConfig(settingsDir, settingsMap, world, customObjects);
        FileSettingsWriter.writeToFile(worldConfig.getSettingsAsMap(), worldConfigFile, worldConfig.SettingsMode);

        return settingsMap;
    }

    private void loadBiomes(SettingsMap worldConfigSettings)
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
        Map<String, BiomeConfigStub> biomeConfigStubs = biomeConfigFinder.findBiomes(biomeDirs, biomesToLoad);

        // Read all settings
        Map<String, BiomeConfig> loadedBiomes = readAndWriteSettings(worldConfigSettings, biomeConfigStubs);

        // Index all necessary settings
        String loadedBiomeNames = indexSettings(loadedBiomes);

        TerrainControl.log(LogMarker.INFO, "{} biomes Loaded", biomesCount);
        TerrainControl.log(LogMarker.DEBUG, "{}", loadedBiomeNames);
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

    private Map<String, BiomeConfig> readAndWriteSettings(SettingsMap worldConfigSettings, Map<String, BiomeConfigStub> biomeConfigStubs)
    {
        Map<String, BiomeConfig> loadedBiomes = new HashMap<String, BiomeConfig>();

        for (BiomeConfigStub biomeConfigStub : biomeConfigStubs.values())
        {
            // Inheritance
            processInheritance(biomeConfigStubs, biomeConfigStub, 0);

            // Settings reading
            BiomeConfig biomeConfig = new BiomeConfig(biomeConfigStub.getLoadInstructions(), biomeConfigStub.getSettings(), worldConfig);
            loadedBiomes.put(biomeConfigStub.getBiomeName(), biomeConfig);

            // Settings writing
            File writeFile = biomeConfigStub.getFile();
            if (!biomeConfig.biomeExtends.isEmpty())
            {
                writeFile = new File(writeFile.getAbsolutePath() + ".inherited");
            }
            FileSettingsWriter.writeToFile(biomeConfig.getSettingsAsMap(), writeFile, worldConfig.SettingsMode);
        }

        return loadedBiomes;
    }

    /**
     * Gets the generation id that the given biome should have, based on
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
        if (requestedGenerationId == null)
        {
            throw new RuntimeException(biomeConfig.getName() + " is not a default biome and not a custom biome. This is a bug!");
        }
        return requestedGenerationId;
    }

    private String indexSettings(Map<String, BiomeConfig> loadedBiomes)
    {
        StringBuilder loadedBiomeNames = new StringBuilder();

        List<BiomeConfig> loadedBiomeList = new ArrayList<BiomeConfig>(loadedBiomes.values());
        Collections.sort(loadedBiomeList, new Comparator<BiomeConfig>() {
            @Override
            public int compare(BiomeConfig a, BiomeConfig b) {
                return getRequestedGenerationId(a) - getRequestedGenerationId(b);
            }
        });

        // Now that all settings are loaded, we can index them,
        // cross-reference between biomes, etc.
        for (BiomeConfig biomeConfig : loadedBiomeList)
        {
            // Statistics of the loaded biomes
            this.biomesCount++;
            loadedBiomeNames.append(biomeConfig.getName());
            loadedBiomeNames.append(", ");

            int requestedGenerationId = getRequestedGenerationId(biomeConfig);

            // Get correct saved id (defaults to generation id, but can be set
            // to use the generation id of another biome)
            int requestedSavedId = requestedGenerationId;
            if (!biomeConfig.replaceToBiomeName.isEmpty())
            {
                BiomeConfig replaceToConfig = loadedBiomes.get(biomeConfig.replaceToBiomeName);
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
                    requestedSavedId = getRequestedGenerationId(replaceToConfig);
                }
            }

            // Create biome
            LocalBiome biome = world.createBiomeFor(biomeConfig, new BiomeIds(requestedGenerationId, requestedSavedId));

            int generationId = biome.getIds().getGenerationId();

            this.biomes[generationId] = biome;
            // Update WorldConfig with actual id
            worldConfig.customBiomeGenerationIds.put(biome.getName(), generationId);

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

    private void processInheritance(Map<String, BiomeConfigStub> biomeConfigStubs, BiomeConfigStub biomeConfigStub, int currentDepth)
    {
        if (biomeConfigStub.biomeExtendsProcessed)
        {
            // Already processed earlier
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
            TerrainControl.log(LogMarker.WARN, "The biome {} tried to extend the biome {}, but that biome doesn't exist.",
                    biomeConfigStub.getBiomeName(), extendedBiomeName);
            return;
        }

        // Check for too much recursion
        if (currentDepth > MAX_INHERITANCE_DEPTH)
        {
            TerrainControl.log(LogMarker.FATAL,
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
                TerrainControl.log(LogMarker.WARN, "Fould old `BiomeConfigs` folder, but it could not be renamed to `", biomeFolderName, "`!");
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
