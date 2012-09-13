package com.khorn.terraincontrol.customobjects;


import com.khorn.terraincontrol.configuration.ConfigFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class CustomObject extends ConfigFile
{


    public HashSet<String> SpawnInBiome;


    public boolean IsValid = false;
    public File FilePath;
    public String name = "";
    public String GroupId = "";


    public CustomObject(File objectFile)
    {
        FilePath = objectFile;
        name = objectFile.getName();
        //Remove extension.
        name = name.substring(0, name.length() - 4);

        ReadSettingsFile(objectFile);
        CorrectSettings();
        if (SettingsCache.containsKey("[META]") && SettingsCache.containsKey("[DATA]"))
            this.IsValid = true;

        if (!this.IsValid)
            return;

        ReadConfigSettings();

    }

    public boolean CheckBiome(String biomeName)
    {
        return (SpawnInBiome.contains(BODefaultValues.BO_ALL_KEY.stringValue()) || SpawnInBiome.contains(BODefaultValues.BO_ALL_KEY.stringValue().toLowerCase()) || SpawnInBiome.contains(biomeName));
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
            if (key.contains(":"))
                values = key.split("=", 2);
            if (values == null)
                continue;
            if (BODefaultValues.Contains(values[0]) || ObjectCoordinate.isCoordinateString(values[0]))
            {
                newSettings.put(values[0], values[1]);
                changedSettings = changedSettings + (first ? "" : ";") + key;
                if (first)
                    first = false;
            }
        }

        return new CustomObjectCompiled(newSettings, name, changedSettings, this);

    }


    @Override
    protected void ReadConfigSettings()
    {
        this.GroupId = ReadModSettings(BODefaultValues.groupId.name(), BODefaultValues.groupId.stringValue());

        this.SpawnInBiome = new HashSet<String>(ReadModSettings(BODefaultValues.spawnInBiome.name(), BODefaultValues.spawnInBiome.StringArrayListValue()));

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
