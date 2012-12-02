package com.khorn.terraincontrol.customobjects.bo3;

import java.io.File;

import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.customobjects.CustomObjectLoader;

public class BO3Loader implements CustomObjectLoader
{
    public CustomObject loadFromFile(String objectName, File file)
    {
        return new BO3(objectName, file);
    }
}
