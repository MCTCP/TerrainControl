package com.khorn.terraincontrol.customobjects;

import java.io.File;

public interface CustomObjectLoader
{
    public CustomObject loadFromFile(String objectName, File file);
}
