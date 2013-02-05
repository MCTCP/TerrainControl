package com.khorn.terraincontrol.customobjects.bo3;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.ConfigFile;
import com.khorn.terraincontrol.configuration.ConfigFunction;
import com.khorn.terraincontrol.configuration.TCDefaultValues;
import com.khorn.terraincontrol.configuration.WorldConfig.ConfigMode;
import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.customobjects.bo3.BO3Settings.OutsideSourceBlock;
import com.khorn.terraincontrol.customobjects.bo3.BO3Settings.SpawnHeightSetting;

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
    public Map<String, CustomObject> otherObjectsInDirectory;

    public String author;
    public String description;
    public ConfigMode settingsMode;

    public boolean tree;
    public int frequency;
    public double rarity;
    public boolean rotateRandomly;
    public SpawnHeightSetting spawnHeight;
    public int minHeight;
    public int maxHeight;
    public ArrayList<String> excludedBiomes;

    public int sourceBlock;
    public int maxPercentageOutsideSourceBlock;
    public OutsideSourceBlock outsideSourceBlock;

    public BlockFunction[][] blocks = new BlockFunction[4][]; // four rotations
    public BO3Check[][] bo3Checks = new BO3Check[4][];

    public int maxBranchDepth;
    public BranchFunction[][] branches = new BranchFunction[4][];

    /**
     * Creates a BO3Config from a file.
     *
     * @param name
     * @param file
     */
    public BO3Config(String name, File file, Map<String, CustomObject> otherObjectsInDirectory)
    {
        this.file = file;
        this.name = name;
        this.otherObjectsInDirectory = otherObjectsInDirectory;

        readSettingsFile(file);

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
    public BO3Config(BO3 oldObject, Map<String, String> extraSettings)
    {
        this.file = oldObject.getSettings().file;
        this.name = oldObject.getName();

        this.settingsCache = oldObject.getSettings().settingsCache;
        this.settingsCache.putAll(extraSettings);

        // Make sure that the BO3 file won't get overwritten
        this.settingsCache.put(TCDefaultValues.SettingsMode.toString().toLowerCase(), ConfigMode.WriteDisable.toString());

        init();
    }

    private void init()
    {
        readConfigSettings();
        correctSettings();
        if (settingsMode != ConfigMode.WriteDisable)
        {
            writeSettingsFile(file, settingsMode == ConfigMode.WriteAll);
        }

        if (rotateRandomly)
        {
            rotateBlockAndChecks();
        }
    }

    @Override
    public void sayFileNotFound(File file)
    {
        // Ignore
    }

    public Map<String, String> getSettingsCache()
    {
        return settingsCache;
    }

    @Override
    protected void writeConfigSettings() throws IOException
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
        writeComment("Ignored by Tree(..), Sapling(..) and CustomStructure(..)");
        writeValue("Frequency", frequency);
        writeNewLine();
        writeComment("The rarity of the BO3 from 0 to 100. Each spawn attempt has rarity% chance to succeed when using the CustomObject(...) resource.");
        writeComment("Ignored by Tree(..), Sapling(..) and CustomStructure(..)");
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
        writeComment("Objects can have other objects attacthed to it: branches. Branches can also");
        writeComment("have branches attached to it, which can also have branches, etc. This is the");
        writeComment("maximum branch depth for this objects.");
        writeValue("MaxBranchDepth", maxBranchDepth);
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

        // Blocks and other things
        writeResources();
    }

    @Override
    protected void readConfigSettings()
    {
        author = readSettings(BO3Settings.author);
        description = readSettings(BO3Settings.description);
        settingsMode = readSettings(TCDefaultValues.SettingsMode);

        tree = readSettings(BO3Settings.tree);
        frequency = readSettings(BO3Settings.frequency);
        rarity = readSettings(BO3Settings.rarity);
        rotateRandomly = readSettings(BO3Settings.rotateRandomly);
        spawnHeight = readSettings(BO3Settings.spawnHeight);
        minHeight = readSettings(BO3Settings.minHeight);
        maxHeight = readSettings(BO3Settings.maxHeight);
        maxBranchDepth = readSettings(BO3Settings.maxBranchDepth);
        excludedBiomes = readSettings(BO3Settings.excludedBiomes);

        sourceBlock = readSettings(BO3Settings.sourceBlock);
        maxPercentageOutsideSourceBlock = readSettings(BO3Settings.maxPercentageOutsideSourceBlock);
        outsideSourceBlock = readSettings(BO3Settings.outsideSourceBlock);

        // Read the resources
        readResources();
    }

    private void readResources()
    {
        List<BlockFunction> tempBlocksList = new ArrayList<BlockFunction>();
        List<BO3Check> tempChecksList = new ArrayList<BO3Check>();
        List<BranchFunction> tempBranchesList = new ArrayList<BranchFunction>();

        for (Map.Entry<String, String> entry : this.settingsCache.entrySet())
        {
            String key = entry.getKey();
            int start = key.indexOf("(");
            int end = key.lastIndexOf(")");
            if (start != -1 && end != -1)
            {
                String name = key.substring(0, start);
                String[] props = readComplexString(key.substring(start + 1, end));

                ConfigFunction<BO3Config> res = TerrainControl.getConfigFunctionsManager().getConfigFunction(name, this, this.name + " on line " + entry.getValue(), Arrays.asList(props));

                if (res != null)
                {
                    if (res instanceof BlockFunction)
                    {
                        tempBlocksList.add((BlockFunction) res);
                    } else if (res instanceof BO3Check)
                    {
                        tempChecksList.add((BO3Check) res);
                    } else if (res instanceof BranchFunction)
                    {
                        tempBranchesList.add((BranchFunction) res);
                    }
                }
            }
        }

        // Store the blocks
        blocks[0] = tempBlocksList.toArray(new BlockFunction[tempBlocksList.size()]);
        bo3Checks[0] = tempChecksList.toArray(new BO3Check[tempChecksList.size()]);
        branches[0] = tempBranchesList.toArray(new BranchFunction[tempBranchesList.size()]);
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

        // Branches
        writeBigTitle("Branches");
        writeComment("Branches are objects that will spawn when this object spawns when it is used in");
        writeComment("the CustomStructure resource. Branches can also have branches, making complex");
        writeComment("structures possible.");
        writeComment("Branch(x,y,z,branchName,rotation,chance[,anotherBranchName,rotation,chance[,...]])");
        writeComment("branchName - name of the object to spawn.");
        writeComment("rotation - NORTH, SOUTH, EAST or WEST.");
        for (BranchFunction branch : branches[0])
        {
            writeValue(branch.makeString());
        }

    }

    @Override
    protected void correctSettings()
    {
        frequency = applyBounds(frequency, 1, 200);
        rarity = applyBounds(rarity, 0.000001, 100.0);
        minHeight = applyBounds(minHeight, TerrainControl.worldDepth, TerrainControl.worldHeight - 1);
        maxHeight = applyBounds(maxHeight, minHeight, TerrainControl.worldHeight);
        maxBranchDepth = applyBounds(maxBranchDepth, 1, Integer.MAX_VALUE);
        sourceBlock = applyBounds(sourceBlock, 0, TerrainControl.supportedBlockIds);
        maxPercentageOutsideSourceBlock = applyBounds(maxPercentageOutsideSourceBlock, 0, 100);
    }

    @Override
    protected void renameOldSettings()
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
            // Branches
            branches[i] = new BranchFunction[branches[i - 1].length];
            for (int j = 0; j < branches[i].length; j++)
            {
                branches[i][j] = branches[i - 1][j].rotate();
            }
        }
    }

}
