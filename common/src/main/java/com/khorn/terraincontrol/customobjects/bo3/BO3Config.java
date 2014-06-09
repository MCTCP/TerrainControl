package com.khorn.terraincontrol.customobjects.bo3;

import com.khorn.terraincontrol.configuration.ConfigFile;
import com.khorn.terraincontrol.configuration.ConfigFunction;
import com.khorn.terraincontrol.configuration.WorldConfig.ConfigMode;
import com.khorn.terraincontrol.configuration.io.SettingsReader;
import com.khorn.terraincontrol.configuration.io.SettingsWriter;
import com.khorn.terraincontrol.configuration.standard.WorldStandardValues;
import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.customobjects.bo3.BO3Settings.OutsideSourceBlock;
import com.khorn.terraincontrol.customobjects.bo3.BO3Settings.SpawnHeightEnum;
import com.khorn.terraincontrol.util.MaterialSet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BO3Config extends ConfigFile
{

    public Map<String, CustomObject> otherObjectsInDirectory;
    public String author;
    public String description;
    public ConfigMode settingsMode;
    public boolean tree;
    public int frequency;
    public double rarity;
    public boolean rotateRandomly;
    public SpawnHeightEnum spawnHeight;
    public int minHeight;
    public int maxHeight;
    /*
     * Using ArrayList instead of List to avoid breaking BO3Tools.
     * Eventually we probably want to get rid entirely of (Array)List<String>
     * for biome lists and use a proper BiomeList.
     */
    public ArrayList<String> excludedBiomes;
    public MaterialSet sourceBlocks;
    public int maxPercentageOutsideSourceBlock;
    public OutsideSourceBlock outsideSourceBlock;
    public BlockFunction[][] blocks = new BlockFunction[4][]; // four
                                                              // rotations
    public BO3Check[][] bo3Checks = new BO3Check[4][];
    public int maxBranchDepth;
    public BranchFunction[][] branches = new BranchFunction[4][];

    /**
     * Creates a BO3Config from a file.
     * 
     * @param name The name of the BO3 without the extension.
     * @param file The file of the BO3.
     */
    public BO3Config(SettingsReader reader, Map<String, CustomObject> otherObjectsInDirectory)
    {
        super(reader);

        this.otherObjectsInDirectory = otherObjectsInDirectory;

        readConfigSettings();
        correctSettings();
        rotateBlocksAndChecks();
    }

    @Override
    protected void writeConfigSettings(SettingsWriter writer) throws IOException
    {
        // The object
        writer.bigTitle("BO3 object");
        writer.comment("This is the config file of a custom object.");
        writer.comment("If you add this object correctly to your BiomeConfigs, it will spawn in the world.");
        writer.comment("");
        writer.comment("This is the creator of this BO3 object");
        writer.setting(BO3Settings.AUTHOR, author);

        writer.comment("A short description of this BO3 object");
        writer.setting(BO3Settings.DESCRIPTION, description);

        writer.comment("The BO3 version, don't change this! It can be used by external applications to do a version check.");
        writer.setting(BO3Settings.VERSION, "3");

        writer.comment("The settings mode, WriteAll, WriteWithoutComments or WriteDisable. See WorldConfig.");
        writer.setting(WorldStandardValues.SETTINGS_MODE, settingsMode);

        // Main settings
        writer.bigTitle("Main settings");
        writer.comment("This needs to be set to true to spawn the object in the Tree and Sapling resources.");
        writer.setting(BO3Settings.TREE, tree);

        writer.comment("The frequency of the BO3 from 1 to 200. Tries this many times to spawn this BO3 when using the CustomObject(...) resource.");
        writer.comment("Ignored by Tree(..), Sapling(..) and CustomStructure(..)");
        writer.setting(BO3Settings.FREQUENCY, frequency);

        writer.comment("The rarity of the BO3 from 0 to 100. Each spawn attempt has rarity% chance to succeed when using the CustomObject(...) resource.");
        writer.comment("Ignored by Tree(..), Sapling(..) and CustomStructure(..)");
        writer.setting(BO3Settings.RARITY, rarity);

        writer.comment("If you set this to true, the BO3 will be placed with a random rotation.");
        writer.setting(BO3Settings.ROTATE_RANDOMLY, rotateRandomly);

        writer.comment("The spawn height of the BO3 - randomY, highestBlock or highestSolidBlock.");
        writer.setting(BO3Settings.SPAWN_HEIGHT, spawnHeight);

        writer.comment("The height limits for the BO3.");
        writer.setting(BO3Settings.MIN_HEIGHT, minHeight);
        writer.setting(BO3Settings.MAX_HEIGHT, maxHeight);

        writer.comment("Objects can have other objects attacthed to it: branches. Branches can also");
        writer.comment("have branches attached to it, which can also have branches, etc. This is the");
        writer.comment("maximum branch depth for this objects.");
        writer.setting(BO3Settings.MAX_BRANCH_DEPTH, maxBranchDepth);

        writer.comment("When spawned with the UseWorld keyword, this BO3 should NOT spawn in the following biomes.");
        writer.comment("If you writer.write the BO3 name directly in the BiomeConfigs, this will be ignored.");
        writer.setting(BO3Settings.EXCLUDED_BIOMES, excludedBiomes);

        // Sourceblock
        writer.bigTitle("Source block settings");
        writer.comment("The block(s) the BO3 should spawn in.");
        writer.setting(BO3Settings.SOURCE_BLOCKS, sourceBlocks);

        writer.comment("The maximum percentage of the BO3 that can be outside the SourceBlock.");
        writer.comment("The BO3 won't be placed on a location with more blocks outside the SourceBlock than this percentage.");
        writer.setting(BO3Settings.MAX_PERCENTAGE_OUTSIDE_SOURCE_BLOCK, maxPercentageOutsideSourceBlock);

        writer.comment("What to do when a block is about to be placed outside the SourceBlock? (dontPlace, placeAnyway)");
        writer.setting(BO3Settings.OUTSIDE_SOURCE_BLOCK, outsideSourceBlock);

        // Blocks and other things
        writeResources(writer);
    }

    @Override
    protected void readConfigSettings()
    {
        author = readSettings(BO3Settings.AUTHOR);
        description = readSettings(BO3Settings.DESCRIPTION);
        settingsMode = readSettings(WorldStandardValues.SETTINGS_MODE);

        tree = readSettings(BO3Settings.TREE);
        frequency = readSettings(BO3Settings.FREQUENCY);
        rarity = readSettings(BO3Settings.RARITY);
        rotateRandomly = readSettings(BO3Settings.ROTATE_RANDOMLY);
        spawnHeight = readSettings(BO3Settings.SPAWN_HEIGHT);
        minHeight = readSettings(BO3Settings.MIN_HEIGHT);
        maxHeight = readSettings(BO3Settings.MAX_HEIGHT);
        maxBranchDepth = readSettings(BO3Settings.MAX_BRANCH_DEPTH);
        excludedBiomes = new ArrayList<String>(readSettings(BO3Settings.EXCLUDED_BIOMES));

        sourceBlocks = readSettings(BO3Settings.SOURCE_BLOCKS);
        maxPercentageOutsideSourceBlock = readSettings(BO3Settings.MAX_PERCENTAGE_OUTSIDE_SOURCE_BLOCK);
        outsideSourceBlock = readSettings(BO3Settings.OUTSIDE_SOURCE_BLOCK);

        // Read the resources
        readResources();
    }

    private void readResources()
    {
        ArrayList<BlockFunction> tempBlocksList = new ArrayList<BlockFunction>();
        List<BO3Check> tempChecksList = new ArrayList<BO3Check>();
        List<BranchFunction> tempBranchesList = new ArrayList<BranchFunction>();

        for (ConfigFunction<BO3Config> res : reader.getConfigFunctions(this, true))
        {
            if (res.isValid())
            {
                if (res instanceof BlockFunction)
                {
                    tempBlocksList.add((BlockFunction) res);
                } else if (res instanceof BO3Check)
                {
                    tempChecksList.add((BO3Check) res);
                } else if (res instanceof WeightedBranchFunction)
                {
                    tempBranchesList.add((WeightedBranchFunction) res);
                } else if (res instanceof BranchFunction)
                {
                    tempBranchesList.add((BranchFunction) res);
                }
            }
        }

        // Store the blocks
        blocks[0] = tempBlocksList.toArray(new BlockFunction[tempBlocksList.size()]);
        bo3Checks[0] = tempChecksList.toArray(new BO3Check[tempChecksList.size()]);
        branches[0] = tempBranchesList.toArray(new BranchFunction[tempBranchesList.size()]);
    }

    public void writeResources(SettingsWriter writer) throws IOException
    {
        // Blocks
        writer.bigTitle("Blocks");
        writer.comment("All the blocks used in the BO3 are listed here. Possible blocks:");
        writer.comment("Block(x,y,z,id[.data][,nbtfile.nbt)");
        writer.comment("RandomBlock(x,y,z,id[:data][,nbtfile.nbt],chance[,id[:data][,nbtfile.nbt],chance[,...]])");
        writer.comment("So RandomBlock(0,0,0,CHEST,chest.nbt,50,CHEST,anotherchest.nbt,100) will spawn a chest at");
        writer.comment("the BO3 origin, and give it a 50% chance to have the contents of chest.nbt, or, if that");
        writer.comment("fails, a 100% percent chance to have the contents of anotherchest.nbt.");
        for (BlockFunction block : blocks[0])
        {
            writer.function(block);
        }

        // BO3Checks
        writer.bigTitle("BO3 checks");
        writer.comment("Require a condition at a certain location in order for the BO3 to be spawned.");
        writer.comment("BlockCheck(x,y,z,BlockName[,BlockName[,...]]) - one of the blocks must be at the location");
        writer.comment("BlockCheckNot(x,y,z,BlockName[,BlockName[,...]]) - all the blocks must not be at the location");
        writer.comment("LightCheck(x,y,z,minLightLevel,maxLightLevel) - light must be between min and max (inclusive)");
        writer.comment("");
        writer.comment("You can use \"Solid\" as a BlockName for matching all solid blocks or \"All\" to match all blocks that aren't air.");
        writer.comment("");
        writer.comment("Examples:");
        writer.comment("  BlockCheck(0,-1,0,GRASS,DIRT)  Require grass or dirt just below the object");
        writer.comment("  BlockCheck(0,-1,0,Solid)       Require any solid block just below the object");
        writer.comment("  BlockCheck(0,-1,0,WOOL)        Require any type of wool just below the object");
        writer.comment("  BlockCheck(0,-1,0,WOOL:0)      Require white wool just below the object");
        writer.comment("  BlockCheckNot(0,-1,0,WOOL:0)   Require that there is no white wool below the object");
        writer.comment("  LightCheck(0,0,0,0,1)          Require almost complete darkness just below the object");
        for (BO3Check check : bo3Checks[0])
        {
            writer.function(check);
        }

        // Branches
        writer.bigTitle("Branches");
        writer.comment("Branches are objects that will spawn when this object spawns when it is used in");
        writer.comment("the CustomStructure resource. Branches can also have branches, making complex");
        writer.comment("structures possible. See the wiki for more details.");
        writer.comment("");
        writer.comment("Regular Branches spawn each branch with an independent chance of spawning.");
        writer.comment("Branch(x,y,z,branchName,rotation,chance[,anotherBranchName,rotation,chance[,...]][IndividualChance])");
        writer.comment("branchName - name of the object to spawn.");
        writer.comment("rotation - NORTH, SOUTH, EAST or WEST.");
        writer.comment("IndividualChance - The chance each branch has to spawn, assumed to be 100 when left blank");
        writer.comment("");
        writer.comment("Weighted Branches spawn branches with a dependent chance of spawning.");
        writer.comment("WeightedBranch(x,y,z,branchName,rotation,chance[,anotherBranchName,rotation,chance[,...]][MaxChanceOutOf])");
        writer.comment("MaxChanceOutOf - The chance all branches have to spawn out of, assumed to be 100 when left blank");
//        writer.writeComment("Example1: WeightedBranch(0,0,0,branch1,NORTH,2,branch2,NORTH,6,10)");
//        writer.writeComment("   branch1 will have a 2 in 10 (20%) chance of spawning, branch2 will have a 6 in 10 (60%) chance to spawn,");
//        writer.writeComment("   and there is a 2 in 10 (20%) chance nothing will spawn");
//        writer.writeComment("Example1A: WeightedBranch(0,0,0,branch1,NORTH,10,branch2,NORTH,30,50)");
//        writer.writeComment("   Same chance as Example1");
//        writer.writeComment("   branch1 will have a 10 in 50 (20%) chance of spawning, branch2 will have a 30 in 50 (60%) chance to spawn,");
//        writer.writeComment("   and there is a 10 in 50 (20%) chance nothing will spawn");
//        writer.writeComment("Example2: WeightedBranch(0,0,0,branch1,NORTH,10,branch2,NORTH,30)");
//        writer.writeComment("   branch1 will have a 10 in 100 (10%) chance of spawning, branch2 will have a 30 in 100 (30%) chance to spawn,");
//        writer.writeComment("   and there is a 60 in 100 (60%) chance nothing will spawn");

        for (BranchFunction branch : branches[0])
        {
            writer.function(branch);
        }

    }

    @Override
    protected void correctSettings()
    {
        maxHeight = higherThan(maxHeight, minHeight);
    }

    @Override
    protected void renameOldSettings()
    {
        // Stub method - there are no old setting to convert yet (:
    }

    /**
     * Rotates all the blocks and all the checks
     */
    public void rotateBlocksAndChecks()
    {
        for (int i = 1; i < 4; i++)
        {
            // Blocks (blocks[i - 1]  is previous rotation)
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
