package com.khorn.terraincontrol.customobjects.bo2;

import com.khorn.terraincontrol.configuration.io.FileSettingsReader;
import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.customobjects.CustomObjectLoader;

import java.io.File;

public class BO2Loader implements CustomObjectLoader
{
    @Override
    public CustomObject loadFromFile(String objectName, File file)
    {
        return new BO2(new FileSettingsReader(objectName, file));
    }

    @Override
    public void onShutdown()
    {
        // Stub method
    }
}
