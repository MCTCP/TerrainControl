package com.khorn.terraincontrol.customobjects;

import java.io.File;
import java.util.Hashtable;

public class ObjectCoordinatesStore implements Runnable
{

    public Hashtable<ObjectCoordinate,ObjectBuffer> Coordinates;

    public ObjectCoordinatesStore()
    {
       this.Coordinates = new Hashtable<>();

    }

    public void ReadStore(File data)
    {

    }

    // Save thread.
    @Override
    public void run()
    {

    }
}
