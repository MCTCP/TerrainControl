package com.khorn.terraincontrol;

import com.khorn.terraincontrol.configuration.ConfigFunctionsManager;
import com.khorn.terraincontrol.configuration.PluginConfig;
import com.khorn.terraincontrol.configuration.io.FileSettingsReader;
import com.khorn.terraincontrol.configuration.io.FileSettingsWriter;
import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;
import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.customobjects.CustomObjectLoader;
import com.khorn.terraincontrol.customobjects.CustomObjectManager;
import com.khorn.terraincontrol.events.EventHandler;
import com.khorn.terraincontrol.events.EventPriority;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.generator.biome.BiomeModeManager;
import com.khorn.terraincontrol.generator.resource.Resource;
import com.khorn.terraincontrol.logging.LogFactory;
import com.khorn.terraincontrol.logging.Logger;
import com.khorn.terraincontrol.util.ChunkCoordinate;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class TerrainControlEngine
{

    private BiomeModeManager biomeManagers;
    private List<EventHandler> cancelableEventHandlers = new ArrayList<EventHandler>(5);
    private ConfigFunctionsManager configFunctionsManager;
    private CustomObjectManager customObjectManager;
    private List<EventHandler> monitoringEventHandlers = new ArrayList<EventHandler>(5);
    private PluginConfig pluginConfig;
    private Logger logger;

    public TerrainControlEngine()
    {
        logger = LogFactory.getLogger();
    }

    public TerrainControlEngine(org.apache.logging.log4j.Logger log4jLogger)
    {
        logger = LogFactory.getLogger(log4jLogger);
    }

    /**
     * Fires the canCustomObjectSpawn event.
     * <p>
     * @see EventHandler#canCustomObjectSpawn(CustomObject, LocalWorld, int,
     * int, int, boolean)
     * @return True if the event handlers allow that the object is spawned,
     *         false otherwise.
     */
    public boolean fireCanCustomObjectSpawnEvent(CustomObject object, LocalWorld world, int x, int y, int z)
    {
        boolean success = true;
        for (EventHandler handler : cancelableEventHandlers)
        {
            if (!handler.canCustomObjectSpawn(object, world, x, y, z, !success))
            {
                success = false;
            }
        }
        for (EventHandler handler : monitoringEventHandlers)
        {
            handler.canCustomObjectSpawn(object, world, x, y, z, !success);
        }
        return success;
    }

    /**
     * Fires the onPopulateEnd event.
     * <p>
     * @see EventHandler#onPopulateEnd(LocalWorld, Random, boolean, int, int)
     */
    public void firePopulationEndEvent(LocalWorld world, Random random, boolean villageInChunk, ChunkCoordinate chunkCoord)
    {
        for (EventHandler handler : cancelableEventHandlers)
            handler.onPopulateEnd(world, random, villageInChunk, chunkCoord.getChunkX(), chunkCoord.getChunkZ());
        for (EventHandler handler : monitoringEventHandlers)
            handler.onPopulateEnd(world, random, villageInChunk, chunkCoord.getChunkX(), chunkCoord.getChunkZ());
    }

    /**
     * Fires the onPopulateStart event.
     * <p>
     * @see EventHandler#onPopulateStart(LocalWorld, Random, boolean, int,
     * int)
     */
    public void firePopulationStartEvent(LocalWorld world, Random random, boolean villageInChunk, ChunkCoordinate chunkCoord)
    {
        for (EventHandler handler : cancelableEventHandlers)
            handler.onPopulateStart(world, random, villageInChunk, chunkCoord.getChunkX(), chunkCoord.getChunkZ());
        for (EventHandler handler : monitoringEventHandlers)
            handler.onPopulateStart(world, random, villageInChunk, chunkCoord.getChunkX(), chunkCoord.getChunkZ());
    }

    /**
     * Fires the onResourceProcess event.
     * <p>
     * @see EventHandler#onResourceProcess(Resource, LocalWorld, Random,
     * boolean, int, int, boolean)
     * @return True if the event handlers allow that the resource is spawned,
     *         false otherwise.
     */
    public boolean fireResourceProcessEvent(Resource resource, LocalWorld world, Random random, boolean villageInChunk, int chunkX,
            int chunkZ)
    {
        boolean success = true;
        for (EventHandler handler : cancelableEventHandlers)
        {
            if (!handler.onResourceProcess(resource, world, random, villageInChunk, chunkX, chunkZ, !success))
            {
                success = false;
            }
        }
        for (EventHandler handler : monitoringEventHandlers)
        {
            handler.onResourceProcess(resource, world, random, villageInChunk, chunkX, chunkZ, !success);
        }
        return success;
    }

    /**
     * Returns the biome managers. Register your own biome manager here.
     * <p/>
     * <p>
     * @return The biome managers.
     */
    public BiomeModeManager getBiomeModeManager()
    {
        return biomeManagers;
    }

    /**
     * Returns the Resource manager.
     * <p/>
     * <p>
     * @return The Resource manager.
     */
    public ConfigFunctionsManager getConfigFunctionsManager()
    {
        return configFunctionsManager;
    }

    /**
     * Returns the CustomObject manager, with hooks to spawn CustomObjects.
     * <p/>
     * <p>
     * @return The CustomObject manager.
     */
    public CustomObjectManager getCustomObjectManager()
    {
        return customObjectManager;
    }

    /**
     * Returns the folder where the global objects are stored in.
     * <p/>
     * <p>
     * @return Folder where the global objects are stored.
     */
    public abstract File getGlobalObjectsDirectory();

    /**
     * Gets the logger to which all messages should be logged.
     * @return The logger.
     */
    public Logger getLogger()
    {
        return logger;
    }

    /**
     * Returns the global config file.
     * <p>
     * @return The global config file.
     */
    public PluginConfig getPluginConfig()
    {
        return pluginConfig;
    }

    /**
     * Returns the root data folder for TerrainControl.
     * <p/>
     * <p>
     * @return The root data folder for TerrainControl.
     */
    public abstract File getTCDataFolder();

    /**
     * Returns the world object with the given name.
     * <p/>
     * <p>
     * @param name The name of the world.
     * <p/>
     * @return The world object.
     */
    public abstract LocalWorld getWorld(String name);

    public void onShutdown()
    {
        // Shutdown all loaders
        for (CustomObjectLoader loader : customObjectManager.loaders.values())
        {
            loader.onShutdown();
        }

        // Null out values to help the garbage collector
        customObjectManager = null;
        configFunctionsManager = null;
        biomeManagers = null;
        pluginConfig = null;
        cancelableEventHandlers.clear();
        monitoringEventHandlers.clear();
        cancelableEventHandlers = null;
        monitoringEventHandlers = null;
    }

    public void onStart()
    {
        // Start the engine
        configFunctionsManager = new ConfigFunctionsManager();
        customObjectManager = new CustomObjectManager();
        biomeManagers = new BiomeModeManager();

        // Do pluginConfig loading and then log anything that happened
        // LogManager and PluginConfig are now decoupled, thank the lord!
        pluginConfig = new PluginConfig(new FileSettingsReader("PluginConfig", new File(getTCDataFolder(),
                PluginStandardValues.ConfigFilename)));
        FileSettingsWriter.writeToFile(pluginConfig, pluginConfig.SettingsMode);
        logger.setLevel(pluginConfig.getLogLevel().getLevel());

        // Fire start event
        for (EventHandler handler : cancelableEventHandlers)
        {
            handler.onStart();
        }
        for (EventHandler handler : monitoringEventHandlers)
        {
            handler.onStart();
        }

        // Load global objects after the event has been fired, so that custom
        // object types are also taken into account
        customObjectManager.loadGlobalObjects();
    }

    /**
     * Register your event handler here with normal priority. You can do this
     * before TerrainControl is started.
     * <p/>
     * <p>
     * @param handler The handler that will receive the events.
     */
    public void registerEventHandler(EventHandler handler)
    {
        cancelableEventHandlers.add(handler);
    }

    /**
     * Register you event handler here with the given priority. You can do
     * this before TerrainControl is started.
     * <p/>
     * <p>
     * @param handler  The handler that will receive the events.
     * @param priority The priority of the event.
     */
    public void registerEventHandler(EventHandler handler, EventPriority priority)
    {
        if (priority == EventPriority.CANCELABLE)
        {
            cancelableEventHandlers.add(handler);
        } else
        {
            monitoringEventHandlers.add(handler);
        }
    }

    /**
     * Gets the material with the given name. The name can be one of
     * Minecraft's material names, a modded material name, one of the names
     * from {@link DefaultMaterial} or a block id (deprecated). Block data can
     * be included
     * in the name using the "blockName:blockData" syntax or the "blockName.id"
     * syntax (deprecated).
     * <p>
     * Examples of valid block names:
     * <ul>
     * <li>STONE</li>
     * <li>minecraft:stone</li>
     * <li>Stone</li>
     * <li>Wool:1</li>
     * <li>Wool.1 <i>(deprecated, use ':')</i></li>
     * <li>minecraft:wool:1</li>
     * <li>35:1 <i>(deprecated, use block name)</i></li>
     * <li>35.1 <i>(deprecated, use block name and ':')</i></li>
     * <li>buildcraft:blockRedLaser <i>(only when BuildCraft is
     * installed)</i></li>
     * </ul>
     * <p>
     * @param name The name of the material.
     * @return The material, or null if not found.
     * <p>
     * @throws InvalidConfigException If no material with that name exists.
     */
    public abstract LocalMaterialData readMaterial(String name) throws InvalidConfigException;

    /**
     * Creates a {@link LocalMaterialData} based on the given
     * {@link DefaultMaterial} and block data.
     * <p>
     * @param defaultMaterial The block type.
     * @param blockData       The block data.
     * @return The materialData.
     */
    public abstract LocalMaterialData toLocalMaterialData(DefaultMaterial defaultMaterial, int blockData);

}
