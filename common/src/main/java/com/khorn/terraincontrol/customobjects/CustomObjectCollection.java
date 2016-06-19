package com.khorn.terraincontrol.customobjects;

import com.khorn.terraincontrol.configuration.io.BracketSettingsReader;
import com.khorn.terraincontrol.configuration.io.SettingsMap;
import com.khorn.terraincontrol.configuration.io.SimpleSettingsMap;
import com.khorn.terraincontrol.util.helpers.FileHelper;

import java.io.File;
import java.util.*;

/**
 * Represents a collection of custom objects. Those objects can be loaded from
 * a directory, or can be loaded manually and then added to this collection.
 *
 */
public class CustomObjectCollection implements Iterable<CustomObject>
{
    private final List<CustomObject> objects;
    private final Map<String, CustomObject> objectsByName;
    private CustomObjectCollection fallback;

    /**
     * Creates a new {@link CustomObjectCollection} instance with no loaded objects.
     */
    public CustomObjectCollection()
    {
        this.objects = new ArrayList<CustomObject>();
        this.objectsByName = new HashMap<String, CustomObject>();
    }

    /**
     * Creates a new {@link CustomObjectCollection} instance that loads the objects immediately.
     * @param loaders   Map of all custom object loaders, indexed by lowercase
     *                  extension without the dot, like "bo3".
     * @param directory The directory to load from. Subdirectories will be
     *                  searched too.
     */
    public CustomObjectCollection(Map<String, CustomObjectLoader> loaders, File directory)
    {
        this();
        load(loaders, directory);
    }

    /**
     * Loads all custom objects from the given directory and its
     * subdirectories. Any objects that were already loaded will be unloaded.
     * If the directory does not exist it will be created.
     * @param loaders   Map of all custom object loaders, indexed by lowercase
     *                  extension without the dot, like "bo3".
     * @param directory The directory to load from. Subdirectories will be
     *                  searched too.
     */
    public void load(Map<String, CustomObjectLoader> loaders, File directory)
    {
        if (!FileHelper.makeFolder(directory))
        {
            return;
        }

        Map<String, CustomObject> objects = loadObjectsRecursive(loaders, directory);
        for (CustomObject object : objects.values())
        {
            object.onEnable(objects);
            addLoadedObject(object);
        }
    }

    /**
     * Adds an object to the list of loaded objects. If an object with the
     * same name (case insensitive) already exists, nothing happens.
     * @param object The object to add to the list of loaded objects.
     */
    public void addLoadedObject(CustomObject object)
    {
        String lowerCaseName = object.getName().toLowerCase();
        if (!objectsByName.containsKey(lowerCaseName))
        {
            objectsByName.put(lowerCaseName, object);
            objects.add(object);
        }
    }

    /**
     * When a lookup by name fails, the given fallback is used instead to look
     * up by name.
     * @param customObjects The fallback.
     */
    public void setFallback(CustomObjectCollection customObjects)
    {
        this.fallback = customObjects;
    }

    /**
     * Loads all objects in a directory, and calls itself for any
     * subdirectories.
     *
     * @param loaders   The custom object loaders.
     * @param directory The directory to load from.
     * @return The objects in the directory.
     */
    private static Map<String, CustomObject> loadObjectsRecursive(Map<String, CustomObjectLoader> loaders, File directory)
    {
        if (!directory.isDirectory())
        {
            throw new IllegalArgumentException("Given file is not a directory: " + directory.getAbsolutePath());
        }

        Map<String, CustomObject> objects = new HashMap<String, CustomObject>();

        // Load all objects from the files and folders under the directory
        for (File file : directory.listFiles())
        {
            // Get name and extension
            String fileName = file.getName();
            int index = fileName.lastIndexOf('.');
            // If we come across a directory descend into it without enabling
            // the objects
            if (file.isDirectory())
            {
                objects.putAll(loadObjectsRecursive(loaders, file));
            } else if (index != -1)
            {
                String objectType = fileName.substring(index + 1, fileName.length());
                String objectName = fileName.substring(0, index);

                // Get the object
                CustomObjectLoader loader = loaders.get(objectType.toLowerCase());
                if (loader != null)
                {
                    CustomObject object = loader.loadFromFile(objectName, file);
                    objects.put(objectName.toLowerCase(), object);
                }
            }
        }

        return objects;
    }

    /**
     * Gets a random custom object from this collection.
     * @param random Random number generator.
     * @return The object, or null if there are no objects at all.
     */
    public CustomObject getRandomObject(Random random)
    {
        if (objects.isEmpty())
        {
            return null;
        }
        return objects.get(random.nextInt(objects.size()));
    }

    /**
     * Gets the object with the given name.
     * @param name Name of the object.
     * @return The object, or null if not found.
     */
    public CustomObject getObjectByName(String name)
    {
        CustomObject object = objectsByName.get(name.toLowerCase());
        if (object == null && fallback != null)
        {
            return fallback.getObjectByName(name);
        }
        return object;
    }

    /**
     * Parses a string in the format <code>name(setting1=foo,setting2=bar)
     * </code>. The object is retrieved using {@link #getObjectByName(String)}.
     * If the object doesn't exist this method will return null. Otherwise, it
     * will apply the given parameters (if any) to a copy of the object, and
     * it will return this modified copy.
     *
     * @param string The string to parse.
     * @return A CustomObject, or null if no one was found.
     */
    public CustomObject parseCustomObject(String string)
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

        CustomObject object = getObjectByName(objectName);

        if (object != null && objectExtraSettings.length() != 0)
        {
            SettingsMap extraSettings = new SimpleSettingsMap(object.getName(), false);
            BracketSettingsReader.readInto(extraSettings, objectExtraSettings);
            object = object.applySettings(extraSettings);
        }

        return object;
    }

    /**
     * Gets an unmodifiable view of all currently loaded objects.
     * @return A view.
     */
    public List<CustomObject> getAll()
    {
        return Collections.unmodifiableList(objects);
    }

    @Override
    public Iterator<CustomObject> iterator()
    {
        return objects.iterator();
    }

    /**
     * Gets whether there are no objects loaded.
     * @return True if there are no objects loaded, false otherwise.
     */
    public boolean isEmpty()
    {
        return objects.isEmpty();
    }

}
