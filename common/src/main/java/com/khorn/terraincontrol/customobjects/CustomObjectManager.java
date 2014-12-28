package com.khorn.terraincontrol.customobjects;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.customobjects.bo2.BO2Loader;
import com.khorn.terraincontrol.customobjects.bo3.BO3Loader;
import com.khorn.terraincontrol.logging.LogMarker;
import com.khorn.terraincontrol.util.minecraftTypes.TreeType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is the registry for the custom object types. It also stores
 * the global objects. World objects are stored in the WorldConfig class.
 * <p />
 * 
 * Terrain Control supports multiple types of custom objects. By default, it
 * supports BO2s, BO3s and a number of special "objects" like trees and
 * UseWorld.
 * <p />
 * 
 * All those implement CustomObject. Plugin developers can register their
 * own custom object types. If you have a number of CustomObjects that you
 * want to register, just add your object to the global objects in the
 * onStart event using registerGlobalObject. If you have your own file
 * format, just use registerCustomObjectLoader(extension, loader).
 * <p />
 * 
 * Even trees are custom objects. If you want to add your own tree type, add
 * your tree to the global objects and make sure that it's canSpawnAsObject
 * returns false.
 * <p />
 * 
 * If your object implements StructuredCustomObject instead of CustomObject,
 * it will be able to have other objects attached to it, forming a
 * structure. As long as each individual object fits in a chunk, Terrain
 * Control will make sure that the structure gets spawned correctly, chunk
 * for chunk.
 */
public class CustomObjectManager
{

    private final Map<String, CustomObjectLoader> loaders;
    private final CustomObjectCollection globalCustomObjects;

    public CustomObjectManager()
    {
        // These are the actual lists, not just a copy.
        this.loaders = new HashMap<String, CustomObjectLoader>();

        // Register loaders
        registerCustomObjectLoader("bo2", new BO2Loader());
        registerCustomObjectLoader("bo3", new BO3Loader());

        this.globalCustomObjects = new CustomObjectCollection();

        // Put some default CustomObjects
        for (TreeType type : TreeType.values())
        {
            registerGlobalObject(new TreeObject(type));
        }
        registerGlobalObject(new UseWorld());
        registerGlobalObject(new UseBiome());
        registerGlobalObject(new UseWorldAll());
        registerGlobalObject(new UseBiomeAll());
    }

    public void loadGlobalObjects()
    {
        // Load all global objects (they can overwrite special objects)
        this.globalCustomObjects.load(this.loaders, TerrainControl.getEngine().getGlobalObjectsDirectory());
        TerrainControl.log(LogMarker.INFO, "{} Global custom objects loaded", globalCustomObjects.getAll().size());
    }

    /**
     * Registers a custom object loader. Register before the config files are
     * getting loaded, please!
     *
     * @param extension The extension of the file. This loader will be responsible for
     *                  all files with this extension.
     * @param loader    The loader.
     */
    public void registerCustomObjectLoader(String extension, CustomObjectLoader loader)
    {
        loaders.put(extension.toLowerCase(), loader);
    }

    /**
     * Register a global object.
     *
     * @param object The object to register.
     */
    public void registerGlobalObject(CustomObject object)
    {
        globalCustomObjects.addLoadedObject(object);
    }

    /**
     * Gets all global objects.
     * @return The global objects.
     */
    public CustomObjectCollection getGlobalObjects()
    {
        return globalCustomObjects;
    }

    /**
     * Gets an unmodifiable view of all object loaders, indexed by the
     * lowercase extension without the dot (for example "bo3").
     * @return The loaders.
     */
    public Map<String, CustomObjectLoader> getObjectLoaders()
    {
        return Collections.unmodifiableMap(loaders);
    }

    /**
     * Calls the {@link CustomObjectLoader#onShutdown()} method of each
     * loader, then unloads them.
     */
    public void shutdown()
    {
        for (CustomObjectLoader loader : loaders.values())
        {
            loader.onShutdown();
        }
        loaders.clear();
    }

}
