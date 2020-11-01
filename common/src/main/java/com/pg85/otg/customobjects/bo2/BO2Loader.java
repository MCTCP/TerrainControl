package com.pg85.otg.customobjects.bo2;

import com.pg85.otg.config.io.FileSettingsReaderOTGPlus;
import com.pg85.otg.customobjects.CustomObject;
import com.pg85.otg.customobjects.CustomObjectLoader;

import java.io.File;

public class BO2Loader implements CustomObjectLoader
{
    @Override
    public CustomObject loadFromFile(String objectName, File file)
    {
        return new BO2(new FileSettingsReaderOTGPlus(objectName, file));
    }

    @Override
    public void onShutdown() { }
}
