package com.khorn.terraincontrol.customobjects;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.customobjects.bo2.BO2Loader;
import com.khorn.terraincontrol.generator.resourcegens.TreeType;

public class CustomObjectManager
{
    /*
     * Khoorn's comment, copied from the removed class ObjectsStore:

    Load:
    1)Load here all objects.
    2)Start save coordinates thread.
    2)Pre compile objects (make arrays for different angle) ??
    3)Compile custom objects array for each biome. Based on world Bo2 list + Biome Bo2 list + Biome CustomTree list
       a) store in one array in biome
       b) store in different arrays in biome ???
    4) Load ObjectCoordinates from file and add that instance to save thread.

    New load
    1) Load all objects from world directory
    2) Search and load objects from plugin directory



    Spawn:
    1)CustomObject resource, Tree resource, sapling, command
    2)Select random object if needed.
    3)Check for biome and select CustomBiome array if needed.
    4)Check for spawn conditions.
    5)Check for collision
       a) Check for block collisions
       b) If out of loaded chunks and object.dig == false - drop.
       c) If out of loaded chunks and object.branch && !object.digBranch == true - drop
       d) ??
    6)Set blocks
       a) If out of loaded chunks - get ObjectBuffer from CoordinatesStore and save to it.
       b) If found branch start point  - select random branch from group and call 5 for it.


    Calculate branch size for in chunk check??
    Call branch in this chunk or in next ??
     
     */
    
    
    
    public final Map<String, CustomObjectLoader> loaders;
    public final Map<String, CustomObject> globalObjects;

    public CustomObjectManager(Map<String, CustomObjectLoader> loaders, Map<String, CustomObject> globalObjects)
    {
        // These are the actual lists, not just a copy.
        this.loaders = loaders;
        this.globalObjects = globalObjects;
        
        // Register loaders
        TerrainControl.registerCustomObjectLoader("bo2", new BO2Loader());
        
        // Load all global objects (they can overwrite special objects)
        TerrainControl.getEngine().getGlobalObjectsDirectory().mkdirs();
        this.globalObjects.putAll(loadObjects(TerrainControl.getEngine().getGlobalObjectsDirectory()));
        TerrainControl.log(this.globalObjects.size() + " global custom objects loaded.");
        
        // Put some default CustomObjects
        for(TreeType type: TreeType.values())
        {
            globalObjects.put(type.name().toLowerCase(), new TreeObject(type));
        }
        globalObjects.put("useworld", new UseWorld());
        globalObjects.put("usebiome", new UseBiome());
    }
    
    /**
     * Returns the global CustomObject with the given name.
     * @param name Name of the CustomObject, case-insensitive.
     * @return The CustomObject, or null if there isn't one with that name.
     */
    public CustomObject getCustomObject(String name)
    {
        return globalObjects.get(name.toLowerCase());
    }
    
    /**
     * Returns the CustomObject with the given name. It searches for a world object first, and then it searches for a global object.
     * @param name Name of the CustomObject, case-insensitive.
     * @param world The world to search in first before searching the global objects.
     * @return The CustomObject, or null if there isn't one with that name.
     */
    public CustomObject getCustomObject(String name, LocalWorld world)
    {
        return getCustomObject(name, world.getSettings());
    }
    
    /**
     * Returns the CustomObject with the given name. It searches for a world object first, and then it searches for a global object.
     * @param name Name of the CustomObject, case-insensitive.
     * @param config The config to search in first before searching the global objects.
     * @return The CustomObject, or null if there isn't one with that name.
     */
    public CustomObject getCustomObject(String name, WorldConfig config)
    {
        if(config.customObjects.containsKey(name.toLowerCase())) {
            return config.customObjects.get(name.toLowerCase());
        }
        return getCustomObject(name);
    }

    /**
     * Returns a Map with all CustomObjects in a directory in it. The Map will
     * have the lowercase object name as a key.
     * 
     * @param directory
     *            The directory to load from.
     * @return
     */
    public Map<String, CustomObject> loadObjects(File directory)
    {
        if (!directory.isDirectory())
        {
            throw new IllegalArgumentException("Given file is not a directory: " + directory.getAbsolutePath());
        }

        Map<String, CustomObject> objects = new HashMap<String, CustomObject>();
        for (File file : directory.listFiles())
        {
            // Get name and extension
            String[] fileName = file.getName().split("\\.");
            String objectName;
            String objectType;
            if (fileName.length == 1)
            {
                // Found an object without an extension
                objectName = fileName[0];
                objectType = "";
            } else
            {
                // Found an object with an extension
                objectType = fileName[fileName.length - 1];
                objectName = "";
                for (int i = 0; i < fileName.length - 2; i++)
                {
                    objectName += fileName[i];
                }
            }

            // Get the object
            CustomObjectLoader loader = loaders.get(objectType);
            if (loader != null)
            {
                objects.put(objectName.toLowerCase(), loader.loadFromFile(objectName, file));
            }
        }

        return objects;
    }
    
    /**
     * Parses a String in the format name(setting1=foo,setting2=bar) and returns a CustomObject.
     * @param string
     * @param world The world to search in
     * @return A CustomObject, or null if no one was found.
     */
    public CustomObject getObjectFromString(String string, LocalWorld world)
    {
        return this.getObjectFromString(string, world.getSettings());
    }
    
    /**
     * Parses a String in the format name(setting1=foo,setting2=bar) and returns a CustomObject.
     * @param string
     * @param config The config to search in
     * @return A CustomObject, or null if no one was found.
     */
    public CustomObject getObjectFromString(String string, WorldConfig config)
    {
        String[] parts = new String[]{string, ""};

        int start = string.indexOf("(");
        int end = string.lastIndexOf(")");
        if (start != -1 && end != -1)
        {
            parts[0] = string.substring(0, start);
            parts[1] = string.substring(start + 1, end);
        }

        CustomObject object = getCustomObject(parts[0], config);
        
        if(object != null && parts[1].length() != 0) {
            // More settings have been given
            Map<String, String> settingsMap = new HashMap<String, String>();
            
            String[] settings = parts[1].split(";");
            for(String setting: settings)
            {
                String[] settingParts = setting.split("=");
                if(settingParts.length == 1)
                {
                    // Boolean values
                    settingsMap.put(settingParts[0], "true");
                } else if(settingParts.length == 2)
                {
                    settingsMap.put(settingParts[0], settingParts[1]);
                }
            }
            
            if(settingsMap.size() > 0)
            {
                object = object.applySettings(settingsMap);
            }
        }
        
        return object;
    }
}
