package com.khorn.terraincontrol.customobjects;


import java.io.File;
import java.util.ArrayList;

public class ObjectsStore
{

    /*
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


    public static File GlobalDirectory;

    public static void ReadObjects(File pluginPath)
    {
        GlobalDirectory = new File(pluginPath, BODefaultValues.BO_GlobalDirectoryName.stringValue());

        if (!GlobalDirectory.exists())
        {
            if (!GlobalDirectory.mkdirs())
            {
                System.out.println("TerrainControl: can`t create GlobalObjects directory");
            }
        }


        //objectsList = LoadObjectsFromDirectory(directory);

        //System.out.println("TerrainControl: " + objectsList.size() + " custom objects loaded");


    }


    public static ArrayList<CustomObject> LoadObjectsFromDirectory(File path)
    {
        ArrayList<CustomObject> outputList = new ArrayList<CustomObject>();

        File[] files = path.listFiles();
        if (files == null)
            return outputList;

        for (File customObjectFile : files)
        {
            if (customObjectFile.isFile())
            {
                CustomObject object = new CustomObject(customObjectFile);
                if (object.IsValid)
                    outputList.add(object);
            }
        }
        return outputList;


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

    public static CustomObject GetObjectFromDirectory(String name, File directory)
    {
        if (!directory.exists())
            return null;

        File[] files = directory.listFiles();
        if (files == null)
            return null;

        for (File customObjectFile : files)
        {
            if (customObjectFile.isFile())
            {

                String fileName = customObjectFile.getName();

                if (!fileName.toLowerCase().endsWith(BODefaultValues.BO_Extension.stringValue().toLowerCase()))
                    continue;

                fileName = fileName.substring(0, fileName.length() - 4);

                if (fileName.equals(name))
                {
                    CustomObject object = new CustomObject(customObjectFile);
                    if (object.IsValid)
                        return object;
                }
            }
        }

        return null;

    }


    public static CustomObjectCompiled CompileString(String key, File directory)
    {
        String[] values = ParseString(key);
        CustomObject object = GetObjectFromDirectory(values[0], directory);


        if (object == null)
            return null;

        return object.Compile(values[1]);


    }
}
