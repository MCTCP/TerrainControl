package com.khorn.terraincontrol.customobjects.bo3;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.ConfigFile;
import com.khorn.terraincontrol.configuration.ConfigFunction;
import com.khorn.terraincontrol.configuration.TCDefaultValues;
import com.khorn.terraincontrol.configuration.WorldConfig.ConfigMode;
import com.khorn.terraincontrol.customobjects.bo3.BO3Settings.OutsideSourceBlock;
import com.khorn.terraincontrol.customobjects.bo3.BO3Settings.SpawnHeight;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BO3Config extends ConfigFile
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

    public int sourceBlock;
    public int maxPercentageOutsideSourceBlock;
    public OutsideSourceBlock outsideSourceBlock;

    public BlockFunction[][] blocks = new BlockFunction[4][]; // four rotations
    public BO3Check[][] bo3Checks = new BO3Check[4][];

    /**
     * Creates a BO3Config from a file.
     *
     * @param name
     * @param file
     */
    public BO3Config(String name, File file)
    {
        this.file = file;
        this.name = name;

        ReadSettingsFile(file);

        init();
    }

    /**
     * Creates a BO3Config with the specified settings. Ignores the settings in the
     * settings file.
     *
     * @param name
     * @param file
     * @param settings
     */
    public BO3Config(String name, File file, Map<String, String> settings)
    {
        this.file = file;
        this.name = name;
        this.SettingsCache = settings;

        init();
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
            rotateBlockAndChecks();
        }
    }

    public Map<String, String> getSettingsCache()
    {
        return SettingsCache;
    }

    @Override
    protected void WriteConfigSettings() throws IOException
    {
        // The object
        writeBigTitle("BO3 object");
        writeComment("This is the config file of a custom object.");
        writeComment("If you add this object correctly to your BiomeConfigs, it will spawn in the world.");
        writeComment("");
        writeComment("This is the creator of this BO3 object");
        writeValue("Author", author);
        writeNewLine();
        writeComment("A short description of this BO3 object");
        writeValue("Description", description);
        writeNewLine();
        writeComment("The BO3 version, don't change this! It can be used by external applications to do a version check.");
        writeValue("Version", 3);
        writeNewLine();
        writeComment("The settings mode, WriteAll, WriteWithoutComments or WriteDisable. See WorldConfig.");
        writeValue("SettingsMode", settingsMode.toString());

        // Main settings
        writeBigTitle("Main settings");
        writeComment("This needs to be set to true to spawn the object in the Tree and Sapling resources.");
        writeValue("Tree", tree);
        writeNewLine();
        writeComment("The frequency of the BO3 from 1 to 200. Tries this many times to spawn this BO3 when using the CustomObject(...) resource.");
        writeValue("Frequency", frequency);
        writeNewLine();
        writeComment("The rarity of the BO3 from 0 to 100. Each spawn attempt has rarity% chance to succeed when using the CustomObject(...) resource.");
        writeValue("Rarity", rarity);
        writeNewLine();
        writeComment("If you set this to true, the BO3 will be placed with a random rotation.");
        writeValue("RotateRandomly", rotateRandomly);
        writeNewLine();
        writeComment("The spawn height of the BO3 - randomY, highestBlock or highestSolidBlock.");
        writeValue("SpawnHeight", spawnHeight.toString());
        writeNewLine();
        writeComment("The height limits for the BO3.");
        writeValue("MinHeight", minHeight);
        writeValue("MaxHeight", maxHeight);
        writeNewLine();
        writeComment("When spawned with the UseWorld keyword, this BO3 should NOT spawn in the following biomes.");
        writeComment("If you write the BO3 name directly in the BiomeConfigs, this will be ignored.");
        writeValue("ExcludedBiomes", excludedBiomes);

        // Sourceblock
        writeBigTitle("Source block settings");
        writeComment("The block the BO3 should spawn in");
        writeValue("SourceBlock", sourceBlock);
        writeNewLine();
        writeComment("The maximum percentage of the BO3 that can be outside the SourceBlock.");
        writeComment("The BO3 won't be placed on a location with more blocks outside the SourceBlock than this percentage.");
        writeValue("MaxPercentageOutsideSourceBlock", maxPercentageOutsideSourceBlock);
        writeNewLine();
        writeComment("What to do when a block is about to be placed outside the SourceBlock? (dontPlace, placeAnyway)");
        writeValue("OutsideSourceBlock", outsideSourceBlock.toString());

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

        sourceBlock = ReadSettings(BO3Settings.sourceBlock);
        maxPercentageOutsideSourceBlock = ReadSettings(BO3Settings.maxPercentageOutsideSourceBlock);
        outsideSourceBlock = ReadSettings(BO3Settings.outsideSourceBlock);

        // Read the resources
        readResources();
    }

    private void readResources()
    {
        List<BlockFunction> tempBlocksList = new ArrayList<BlockFunction>();
        List<BO3Check> tempChecksList = new ArrayList<BO3Check>();

        for (Map.Entry<String, String> entry : this.SettingsCache.entrySet())
        {
            String key = entry.getKey();
            int start = key.indexOf("(");
            int end = key.lastIndexOf(")");
            if (start != -1 && end != -1)
            {
                String name = key.substring(0, start);
                String[] props = ReadComplexString(key.substring(start + 1, end));

                ConfigFunction<BO3Config> res = TerrainControl.getConfigFunctionsManager().getConfigFunction(name, this, this.name + " on line " + entry.getValue(), Arrays.asList(props));

                if (res != null)
                {
                    if (res instanceof BlockFunction)
                    {
                        tempBlocksList.add((BlockFunction) res);
                    } else if (res instanceof BO3Check)
                    {
                        tempChecksList.add((BO3Check) res);
                    }
                }
            }
        }

        // Store the blocks
        blocks[0] = tempBlocksList.toArray(new BlockFunction[tempBlocksList.size()]);
        bo3Checks[0] = tempChecksList.toArray(new BO3Check[tempChecksList.size()]);
    }

    public void writeResources() throws IOException
    {
        // Blocks
        writeBigTitle("Blocks");
        writeComment("All the blocks used in the BO3 are listed here. Possible blocks:");
        writeComment("Block(x,y,z,id[.data][,nbtfile.nbt)");
        writeComment("RandomBlock(x,y,z,id[:data][,nbtfile.nbt],chance[,id[:data][,nbtfile.nbt],chance[,...]])");
        writeComment("So RandomBlock(0,0,0,CHEST,chest.nbt,50,CHEST,anotherchest.nbt,100) will spawn a chest at");
        writeComment("the BO3 origin, and give it a 50% chance to have the contents of chest.nbt, or, if that");
        writeComment("fails, a 100% percent chance to have the contents of anotherchest.nbt.");
        for (BlockFunction block : blocks[0])
        {
            writeValue(block.makeString());
        }

        // BO3Checks
        writeBigTitle("BO3 checks");
        writeComment("Require a condition at a certain location in order for the BO3 to be spawned.");
        writeComment("BlockCheck(x,y,z,id[.data][,id[.data][,...]])");
        for (BO3Check check : bo3Checks[0])
        {
            writeValue(check.makeString());
        }
    }

    @Override
    protected void CorrectSettings()
    {
        frequency = applyBounds(frequency, 1, 200);
        rarity = applyBounds(rarity, 0.000001, 100.0);
        minHeight = applyBounds(minHeight, TerrainControl.worldDepth, TerrainControl.worldHeight - 1);
        maxHeight = applyBounds(maxHeight, minHeight, TerrainControl.worldHeight);
        sourceBlock = applyBounds(sourceBlock, 0, TerrainControl.supportedBlockIds);
        maxPercentageOutsideSourceBlock = applyBounds(maxPercentageOutsideSourceBlock, 0, 100);
    }

    @Override
    protected void RenameOldSettings()
    {
        // Stub method - there are no old setting to convert yet (:
    }

    /**
     * Rotates all the blocks and all the checks
     */
    private void rotateBlockAndChecks()
    {
        for (int i = 1; i < 4; i++)
        {
            // Blocks
            blocks[i] = new BlockFunction[blocks[i - 1].length];
            for (int j = 0; j < blocks[i].length; j++)
            {
                blocks[i][j] = blocks[i - 1][j].rotate();
            }
            // BO3 checks
            bo3Checks[i] = new BO3Check[bo3Checks[i - 1].length];
            for (int j = 0; j < bo3Checks[i].length; j++)
            {
                bo3Checks[i][j] = bo3Checks[i - 1][j].rotate();
            }
        }
    }

}
