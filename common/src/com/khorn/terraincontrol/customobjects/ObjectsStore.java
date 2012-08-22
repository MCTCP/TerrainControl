package com.khorn.terraincontrol.customobjects;


import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.WorldConfig;

import java.io.File;
import java.util.ArrayList;

public class ObjectsStore
{
    private static ArrayList<CustomObject> ObjectsList = new ArrayList<CustomObject>();

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



    public static void ReadObjects(File directoryPath)
    {


    }

    public static void ReloadObjects(File directoryPath)
    {


    }

    public static CustomObject[] CompileObjectsForBiome(WorldConfig world, BiomeConfig biome)
    {

        return null;
    }

}
