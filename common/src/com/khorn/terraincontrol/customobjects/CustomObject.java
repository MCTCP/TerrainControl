package com.khorn.terraincontrol.customobjects;


import com.khorn.terraincontrol.configuration.ConfigFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CustomObject extends ConfigFile
{


    public boolean IsValid = false;
    public File FilePath;
    public String Name = "";


    public CustomObject(File objectFile)
    {
        FilePath = objectFile;
        Name = objectFile.getName();

        if(!Name.toLowerCase().endsWith(BODefaultValues.BO_Extension.stringValue().toLowerCase()))
            return;

        //Remove extension.
        Name = Name.substring(0, Name.length() - 4);


        ReadSettingsFile(objectFile);
        CorrectSettings();
        if (SettingsCache.containsKey("[META]") && SettingsCache.containsKey("[DATA]"))
            this.IsValid = true;

        if (!this.IsValid)
            return;

        ReadConfigSettings();

    }


    public CustomObjectCompiled Compile(String settingsLine)
    {
        HashMap<String, String> newSettings = new HashMap<String, String>();
        for (Map.Entry<String, String> entry : this.SettingsCache.entrySet())
            if (BODefaultValues.Contains(entry.getKey()) || ObjectCoordinate.isCoordinateString(entry.getKey()))
                newSettings.put(entry.getKey(), entry.getValue());

        String[] keys = settingsLine.split(";");
        String changedSettings = "";
        boolean first = true;

        for (String key : keys)
        {
            String[] values = null;
            if (key.contains("="))
                values = key.split("=", 2);
            else if (key.contains(":"))
                values = key.split("=", 2);
            if (values == null)
                continue;
            if (BODefaultValues.Contains(values[0].toLowerCase()) || ObjectCoordinate.isCoordinateString(values[0]))
            {
                newSettings.put(values[0].toLowerCase(), values[1]);
                changedSettings = changedSettings + (first ? "" : ";") + key;
                if (first)
                    first = false;
            }
        }

        return new CustomObjectCompiled(newSettings, Name, changedSettings, this);

    }


    @Override
    protected void ReadConfigSettings()
    {

    }

    @Override
    protected void WriteConfigSettings() throws IOException
    {

    }

    @Override
    protected void CorrectSettings()
    {

    }

    @Override
    protected void RenameOldSettings()
    {
    }
}
