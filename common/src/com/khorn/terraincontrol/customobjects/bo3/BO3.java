package com.khorn.terraincontrol.customobjects.bo3;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.ConfigFile;
import com.khorn.terraincontrol.configuration.ConfigFunction;
import com.khorn.terraincontrol.configuration.TCDefaultValues;
import com.khorn.terraincontrol.configuration.WorldConfig.ConfigMode;
import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.customobjects.bo3.BO3Settings.SpawnHeight;

public class BO3 extends ConfigFile implements CustomObject
{
    public File file;
    public String name;
    public String author;
    public String description;
    public ConfigMode settingsMode;

    public boolean tree;
    public int frequency;
    public double rarity;
    public boolean rotateRandomly;
    public SpawnHeight spawnHeight;
    public int minHeight;
    public int maxHeight;
    public ArrayList<String> excludedBiomes;

    public BlockFunction[][] blocks = new BlockFunction[4][]; // four rotations

    /**
     * Creates a BO3 from a file.
     * 
     * @param name
     * @param file
     */
    public BO3(String name, File file)
    {
        this.file = file;
        this.name = name;

        ReadSettingsFile(file);

        init();
    }

    /**
     * Creates a BO3 with the specified settings. Ignores the settings in the
     * settings file.
     * 
     * @param name
     * @param file
     * @param settings
     */
    public BO3(String name, File file, Map<String, String> settings)
    {
        this.file = file;
        this.name = name;
        this.SettingsCache = settings;
    }

    private void init()
    {
        ReadConfigSettings();
        CorrectSettings();
        if (settingsMode != ConfigMode.WriteDisable)
        {
            WriteSettingsFile(file, settingsMode == ConfigMode.WriteAll);
        }

        if (rotateRandomly)
        {
            rotateBlocks();
        }
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
        BlockFunction[] blocks = this.blocks[0];
        if (rotateRandomly)
        {
            blocks = this.blocks[random.nextInt(4)];
        }
        for (BlockFunction block : blocks)
        {
            block.spawn(world, random, x + block.x, y + block.y, z + block.z);
        }
        return true;
    }

    @Override
    public boolean spawnAsTree(LocalWorld world, Random random, int x, int y, int z)
    {
        if (tree)
        {
            return spawn(world, random, x, y, z);
        }
        return false;
    }

    @Override
    public boolean spawn(LocalWorld world, Random random, int x, int z)
    {
        if (spawnHeight == SpawnHeight.randomY)
        {
            return spawn(world, random, x, minHeight + random.nextInt(maxHeight), z);
        }
        if (spawnHeight == SpawnHeight.highestBlock)
        {
            int y = world.getHighestBlockYAt(x, z);
            if(y < minHeight || y > maxHeight)
            {
                return false;
            }
            return spawn(world, random, x, y, z);
        }
        if (spawnHeight == SpawnHeight.highestSolidBlock)
        {
            int y = world.getSolidHeight(x, z);
            if(y < minHeight || y > maxHeight)
            {
                return false;
            }
            return spawn(world, random, x, y, z);
        }
        return false;
    }

    @Override
    public boolean spawnAsTree(LocalWorld world, Random random, int x, int z)
    {
        if (tree)
        {
            return spawn(world, random, x, z);
        }
        return false;
    }

    @Override
    public void process(LocalWorld world, Random random, int chunkX, int chunkZ)
    {
        int chunkMiddleX = chunkX * 16 + 8;
        int chunkMiddleZ = chunkZ * 16 + 8;
        for (int i = 0; i < frequency; i++)
        {
            double test = random.nextDouble() * 100.0;
            if (rarity > test)
            {
                spawn(world, random, chunkMiddleX + random.nextInt(16), chunkMiddleZ + random.nextInt(16));
            }
        }
    }

    @Override
    public void processAsTree(LocalWorld world, Random random, int chunkX, int chunkZ)
    {
        if (!tree)
        {
            return;
        }
        process(world, random, chunkX, chunkZ);
    }

    @Override
    public CustomObject applySettings(Map<String, String> extraSettings)
    {
        Map<String, String> newSettings = new HashMap<String, String>();
        newSettings.putAll(SettingsCache);
        newSettings.putAll(extraSettings);
        return new BO3(getName(), file, newSettings);
    }

    @Override
    public boolean hasPreferenceToSpawnIn(LocalBiome biome)
    {
        if (excludedBiomes.contains("All") || excludedBiomes.contains("all") || excludedBiomes.contains(biome.getName()))
        {
            return false;
        }
        return true;
    }

    @Override
    protected void WriteConfigSettings() throws IOException
    {
        // The object
        WriteTitle("BO3 object");
        WriteComment("This is the config file of a custom object.");
        WriteComment("If you add this object correctly to your BiomeConfigs, it will spawn in the world.");
        WriteComment("");
        WriteComment("This is the creator of this BO3 object");
        WriteValue("Author", author);
        WriteNewLine();
        WriteComment("A short description of this BO3 object");
        WriteValue("Description", description);
        WriteNewLine();
        WriteComment("The BO3 version, don't change this! It can be used by external applications to do a version check.");
        WriteValue("Version", 3);
        WriteNewLine();
        WriteComment("The settings mode, WriteAll, WriteWithoutComments or WriteDisable. See WorldConfig.");
        WriteValue("SettingsMode", settingsMode.toString());

        // Main settings
        WriteTitle("Main settings");
        WriteComment("This needs to be set to true to spawn the object in the Tree and Sapling resources.");
        WriteValue("Tree", tree);
        WriteNewLine();
        WriteComment("The frequency of the BO3 from 1 to 200. Tries this many times to spawn this BO3 when using the CustomObject(...) resource.");
        WriteValue("Frequency", frequency);
        WriteNewLine();
        WriteComment("The rarity of the BO3 from 0 to 100. Each spawn attempt has rarity% chance to succeed when using the CustomObject(...) resource.");
        WriteValue("Rarity", rarity);
        WriteNewLine();
        WriteComment("If you set this to true, the BO3 will be placed with a random rotation.");
        WriteValue("RotateRandomly", rotateRandomly);
        WriteNewLine();
        WriteComment("The spawn height of the BO3 - randomY, highestBlock or highestSolidBlock.");
        WriteValue("SpawnHeight", spawnHeight.toString());
        WriteNewLine();
        WriteComment("The height limits for the BO3.");
        WriteValue("MinHeight", minHeight);
        WriteValue("MaxHeight", maxHeight);
        WriteNewLine();
        WriteComment("When spawned with the UseWorld keyword, this BO3 should NOT spawn in the following biomes.");
        WriteComment("If you write the BO3 name directly in the BiomeConfigs, this will be ignored.");
        WriteValue("ExcludedBiomes", excludedBiomes);

        // Blocks
        writeResources();
    }

    @Override
    protected void ReadConfigSettings()
    {
        author = ReadSettings(BO3Settings.author);
        description = ReadSettings(BO3Settings.description);
        settingsMode = ReadSettings(TCDefaultValues.SettingsMode);

        tree = ReadSettings(BO3Settings.tree);
        frequency = ReadSettings(BO3Settings.frequency);
        rarity = ReadSettings(BO3Settings.rarity);
        rotateRandomly = ReadSettings(BO3Settings.rotateRandomly);
        spawnHeight = ReadSettings(BO3Settings.spawnHeight);
        minHeight = ReadSettings(BO3Settings.minHeight);
        maxHeight = ReadSettings(BO3Settings.maxHeight);
        excludedBiomes = ReadSettings(BO3Settings.excludedBiomes);

        // Read the resources
        readResources();
    }

    private void readResources()
    {
        List<BlockFunction> tempBlocksList = new ArrayList<BlockFunction>();

        for (Map.Entry<String, String> entry : this.SettingsCache.entrySet())
        {
            String key = entry.getKey();
            int start = key.indexOf("(");
            int end = key.lastIndexOf(")");
            if (start != -1 && end != -1)
            {
                String name = key.substring(0, start);
                String[] props = ReadComplexString(key.substring(start + 1, end));

                ConfigFunction<BO3> res = TerrainControl.getConfigFunctionsManager().getConfigFunction(name, this, this.name + " on line " + entry.getValue(), Arrays.asList(props));

                if (res != null)
                {
                    if (res instanceof BlockFunction)
                    {
                        tempBlocksList.add((BlockFunction) res);
                    }
                }
            }
        }

        // Store the blocks
        blocks[0] = tempBlocksList.toArray(new BlockFunction[tempBlocksList.size()]);
    }

    public void writeResources() throws IOException
    {
        // Blocks
        WriteTitle("Blocks");
        WriteComment("All the blocks used in the BO3 are listed here.");
        WriteComment("Syntax: Block(id[.data],x,y,z[,nbtfile.nbt,chance[,anothernbtfile.nbt,chance[,...]]])");
        WriteComment("So Block(CHEST,0,0,0,chest.nbt,50,anotherchest.nbt,100) will spawn a chest at the BO3");
        WriteComment("origin, and give it a 50% chance to have the contents of chest.nbt, or, if that fails,");
        WriteComment("a 100% percent chance to have the contents of anotherchest.nbt.");
        for (BlockFunction block : blocks[0])
        {
            WriteValue(block.makeString());
        }
    }

    @Override
    protected void CorrectSettings()
    {
        frequency = applyBounds(frequency, 1, 200);
        rarity = applyBounds(rarity, 0.000001, 100.0);
        minHeight = applyBounds(minHeight, TerrainControl.worldDepth, TerrainControl.worldHeight - 1);
        maxHeight = applyBounds(maxHeight, minHeight + 1, TerrainControl.worldHeight);
    }

    @Override
    protected void RenameOldSettings()
    {
        // Stub method - there are no old setting to convert yet (:
    }

    /**
     * Rotates all the blocks
     */
    private void rotateBlocks()
    {
        for (int i = 1; i < 4; i++)
        {
            blocks[i] = new BlockFunction[blocks[i - 1].length];
            for (int j = 0; j < blocks[i].length; j++)
            {
                blocks[i][j] = blocks[i - 1][j].rotate();
            }
        }
    }

}
