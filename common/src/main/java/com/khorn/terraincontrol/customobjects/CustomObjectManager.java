package com.khorn.terraincontrol.customobjects;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.configuration.io.BracketSettingsReader;
import com.khorn.terraincontrol.customobjects.bo2.BO2Loader;
import com.khorn.terraincontrol.customobjects.bo3.BO3Loader;
import com.khorn.terraincontrol.logging.LogMarker;
import com.khorn.terraincontrol.util.minecraftTypes.TreeType;

import java.io.File;
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

    public final Map<String, CustomObjectLoader> loaders;
    public final Map<String, CustomObject> globalObjects;

    public CustomObjectManager()
    {
        // These are the actual lists, not just a copy.
        this.loaders = new HashMap<String, CustomObjectLoader>();
        this.globalObjects = new HashMap<String, CustomObject>();

        // Register loaders
        registerCustomObjectLoader("bo2", new BO2Loader());
        registerCustomObjectLoader("bo3", new BO3Loader());

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
        TerrainControl.getEngine().getGlobalObjectsDirectory().mkdirs();
        Map<String, CustomObject> loadedGlobalObjects = loadObjects(TerrainControl.getEngine().getGlobalObjectsDirectory());
        TerrainControl.log(LogMarker.INFO, "{} Global custom objects loaded", new Object[]{loadedGlobalObjects.size()});
        this.globalObjects.putAll(loadedGlobalObjects);
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
        globalObjects.put(object.getName().toLowerCase(), object);
    }

    /**
     * Returns the global CustomObject with the given name.
     *
     * @param name Name of the CustomObject, case-insensitive.
     * @return The CustomObject, or null if there isn't one with that name.
     */
    public CustomObject getCustomObject(String name)
    {
        return globalObjects.get(name.toLowerCase());
    }

    /**
     * Returns the CustomObject with the given name. It searches for a world
     * object first, and then it searches for a global object.
     *
     * @param name  Name of the CustomObject, case-insensitive.
     * @param world The world to search in first before searching the global
     *              objects.
     * @return The CustomObject, or null if there isn't one with that name.
     */
    public CustomObject getCustomObject(String name, LocalWorld world)
    {
        return getCustomObject(name, world.getSettings().worldConfig);
    }

    /**
     * Returns the CustomObject with the given name. It searches for a world
     * object first, and then it searches for a global object.
     *
     * @param name   Name of the CustomObject, case-insensitive.
     * @param config The config to search in first before searching the global
     *               objects.
     * @return The CustomObject, or null if there isn't one with that name.
     */
    public CustomObject getCustomObject(String name, WorldConfig config)
    {
        for (CustomObject object : config.customObjects)
        {
            if (object.getName().equalsIgnoreCase(name))
            {
                return object;
            }
        }
        return getCustomObject(name);
    }

    /**
     * Returns a Map with all CustomObjects in a directory and its sub-directories
     * in it. The Map will have the lowercase object name as a key. All objects
     * will have their onEnable method called after all objects are loaded.
     *
     * @param directory The directory to load from.
     * @return The map, as described.
     */
    public Map<String, CustomObject> loadObjects(File directory)
    {
        // Load all the objects
        Map<String, CustomObject> loadedObjects = loadObjectsRecursive(directory);

        // Enable all the objects
        for (CustomObject object : loadedObjects.values())
        {
            object.onEnable(loadedObjects);
        }

        return loadedObjects;
    }

    /**
     * Returns a Map with all CustomObjects in a directory and its sub-directories
     * in it. The Map will have the lowercase object name as a key. The objects
     * won't be enabled yet.
     *
     * @param directory The directory to load from.
     * @param enableObjects whether or not to enable objects 
     * @return The map, as described.
     */
    protected Map<String, CustomObject> loadObjectsRecursive(File directory)
    {
        if (!directory.isDirectory())
        {
            throw new IllegalArgumentException("Given file is not a directory: " + directory.getAbsolutePath());
        }

        // Load all objects from the files and folders under the directory
        Map<String, CustomObject> objects = new HashMap<String, CustomObject>();
        for (File file : directory.listFiles())
        {
            // Get name and extension
            String fileName = file.getName();
            int index = fileName.lastIndexOf('.');
            // If we come across a directory descend into it without enabling
            // the objects
            if (file.isDirectory())
            {
                objects.putAll(loadObjectsRecursive(file));
            } else if (index != -1)
            {
                String objectType = fileName.substring(index + 1, fileName.length());
                String objectName = fileName.substring(0, index);

                // Get the object
                CustomObjectLoader loader = loaders.get(objectType.toLowerCase());
                if (loader != null)
                {
                    objects.put(objectName.toLowerCase(), loader.loadFromFile(objectName, file));
                }
            }
        }

        return objects;
    }

    /**
     * Parses a String in the format name(setting1=foo,setting2=bar) and returns
     * a CustomObject.
     *
     * @param string
     * @param world  The world to search in
     * @return A CustomObject, or null if no one was found.
     */
    public CustomObject getObjectFromString(String string, LocalWorld world)
    {
        return this.getObjectFromString(string, world.getSettings().worldConfig);
    }

    /**
     * Parses a String in the format name(setting1=foo,setting2=bar) and returns
     * a CustomObject.
     *
     * @param string
     * @param config The config to search in
     * @return A CustomObject, or null if no one was found.
     */
    public CustomObject getObjectFromString(String string, WorldConfig config)
    {
        String objectName = string;
        String objectExtraSettings = "";

        int start = string.indexOf('(');
        int end = string.lastIndexOf(')');
        if (start != -1 && end != -1)
        {
            objectName = string.substring(0, start);
            objectExtraSettings = string.substring(start + 1, end);
        }

        CustomObject object = getCustomObject(objectName, config);

        if (object != null && objectExtraSettings.length() != 0)
        {
            object = object.applySettings(new BracketSettingsReader(object.getName(), objectExtraSettings));
        }

        return object;
    }
}
