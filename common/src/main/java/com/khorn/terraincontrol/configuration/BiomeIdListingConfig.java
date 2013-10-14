package com.khorn.terraincontrol.configuration;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import static com.khorn.terraincontrol.configuration.ConfigFile.readComplexString;

public final class BiomeIdListingConfig extends ConfigFile
{

    public static int maxBiomeCount;
    public List<BiomeIdEntry> biomes;

    public BiomeIdListingConfig(File settingsDir, LocalWorld world, WorldConfig config)
    {
        super(world.getName() + ".bidl", settingsDir);
        maxBiomeCount = world.getMaxBiomesCount();
        biomes = new ArrayList<BiomeIdEntry>(maxBiomeCount);
        this.readSettingsFile();
    }

    private void writeBiomeIds()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void readConfigSettings()
    {

        List<BiomeIdEntry> tempBiomeList = new ArrayList<BiomeIdEntry>();

        for (Map.Entry<String, String> entry : this.settingsCache.entrySet())
        {
            String key = entry.getKey();
            int start = key.indexOf("(");
            int end = key.lastIndexOf(")");
            if (start != -1 && end != -1)
            {
                String name = key.substring(0, start);
                String[] props = readComplexString(key.substring(start + 1, end));

                if ("Biome".equals(name) && props.length == 2)
                {
                    tempBiomeList.add(new BiomeIdEntry(props[0], Short.parseShort(props[1])));
                }

                String propsString = "";
                for (String string : props)
                {
                    if (!propsString.isEmpty())
                        propsString += ", ";
                    propsString += string;
                }
                TerrainControl.log(Level.OFF, "Name: {0}, props: {1}", new Object[]
                {
                    name, propsString
                });

            }
        }
    }

    @Override
    protected void writeConfigSettings() throws IOException
    {
        // The modes
        writeBigTitle("Auto Biome ID Persistence and Tracking");

        writeComment("DO NOT EVER EDIT THIS FILE.");
        writeComment("Doing so WILL break your world.");
        writeBiomeIds();
    }

    @Override
    protected void correctSettings()
    {
    }

    @Override
    protected void renameOldSettings()
    {
        throw new UnsupportedOperationException("Will not be supported.");
    }

}