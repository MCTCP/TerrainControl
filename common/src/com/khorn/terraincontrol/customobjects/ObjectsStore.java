package com.khorn.terraincontrol.customobjects;


import java.io.File;
import java.util.ArrayList;

public class ObjectsStore
{
    private static ArrayList<CustomObject> objectsList = new ArrayList<CustomObject>();

    /*
     Load:
     1)Load here all objects.
     2)Start save coordinates thread.
     2)Pre compile objects (make arrays for different angle) ??
     3)Compile custom objects array for each biome. Based on world Bo2 list + Biome Bo2 list + Biome CustomTree list
        a) store in one array in biome
        b) store in different arrays in biome ???
     4) Load ObjectCoordinates from file and add that instance to save thread.

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


    private static File directory;

    public static void ReadObjects(File pluginPath)
    {
        directory = new File(pluginPath, BODefaultValues.BO_DirectoryName.stringValue());

        if (!directory.exists())
        {
            if (!directory.mkdirs())
            {
                System.out.println("BOB Plugin system encountered an error, aborting!");
                return;
            }
        }

        File[] files = directory.listFiles();
        for (File customObjectFile : files)
        {
            if (customObjectFile.isFile())
            {
                CustomObject object = new CustomObject(customObjectFile);
                if (object.IsValid)
                    objectsList.add(object);
            }
        }

        System.out.println("TerrainControl: " + objectsList.size() + " custom objects loaded");


    }

    public static String[] ParseString(String key)
    {
        String[] output = new String[]{key, ""};

        int start = key.indexOf("(");
        int end = key.lastIndexOf(")");
        if (start != -1 && end != -1)
        {
            output[0] = key.substring(0, start);
            output[1] = key.substring(start + 1, end);
        }
        return output;
    }


    public static CustomObject GetObjectFromName(String name)
    {
        CustomObject object = null;
        for (CustomObject customObject : objectsList)
            if (customObject.name.equals(name))
                object = customObject;
        return object;
    }


    public static CustomObjectCompiled Compile(String key)
    {
        String[] values = ParseString(key);
        CustomObject object = GetObjectFromName(values[0]);


        if (object == null)
            return null;

        return object.Compile(values[1]);


    }

    public static void ReloadObjects()
    {


    }

}
