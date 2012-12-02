package com.khorn.terraincontrol.customobjects.bo3;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Random;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.ConfigFile;
import com.khorn.terraincontrol.customobjects.CustomObject;

public class BO3 extends ConfigFile implements CustomObject
{
    private String name;
    private String author;
    private String description;
    private boolean tree;

    public BO3(String name, File file)
    {
        ReadSettingsFile(file);
        this.name = name;
        ReadConfigSettings();
        CorrectSettings();
        WriteSettingsFile(file, true);
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public boolean canSpawnAsTree()
    {
        return tree;
    }

    @Override
    public boolean canSpawnAsObject()
    {
        return true;
    }

    @Override
    public boolean spawn(LocalWorld world, Random random, int x, int y, int z)
    {
        // TODO
        return false;
    }

    @Override
    public boolean spawnAsTree(LocalWorld world, Random random, int x, int y, int z)
    {
        // TODO
        return false;
    }

    @Override
    public boolean spawn(LocalWorld world, Random random, int x, int z)
    {
        // TODO
        return false;
    }

    @Override
    public boolean spawnAsTree(LocalWorld world, Random random, int x, int z)
    {
        // TODO
        return false;
    }

    @Override
    public void process(LocalWorld world, Random random, int chunkX, int chunkZ)
    {
        // TODO

    }

    @Override
    public void processAsTree(LocalWorld world, Random random, int chunkX, int chunkZ)
    {
        // TODO

    }

    @Override
    public CustomObject applySettings(Map<String, String> settings)
    {
        // TODO
        return this;
    }

    @Override
    public boolean hasPreferenceToSpawnIn(LocalBiome biome)
    {
        // TODO
        return false;
    }

    @Override
    protected void WriteConfigSettings() throws IOException
    {
        WriteTitle("BO3 object");
        WriteComment("This is the config file of a custom object.");
        WriteComment("If you add this object correctly to your BiomeConfigs, it will spawn in the world.");
        WriteComment("");
        WriteComment("This is the creator of this BO3 object:");
        WriteValue("author", author);
        WriteNewLine();
        WriteComment("A short description of this BO3 object:");
        WriteValue("description", description);
        WriteNewLine();
        WriteComment("The BO3 version, don't change this! It can be used by external applications to do a version check.");
        WriteValue("version=3");
        
        WriteTitle("Misc settings");
        WriteComment("This needs to be set to true to spawn the object in the Tree and Sapling resources.");
        WriteValue("tree", tree);
    }

    @Override
    protected void ReadConfigSettings()
    {
        author = ReadSettings(BO3Settings.author);
        description = ReadSettings(BO3Settings.description);
        
        tree = ReadSettings(BO3Settings.tree);
    }

    @Override
    protected void CorrectSettings()
    {
        // Stub method
    }

    @Override
    protected void RenameOldSettings()
    {
        // Stub method - there are no old setting to convert yet (:
    }

}
