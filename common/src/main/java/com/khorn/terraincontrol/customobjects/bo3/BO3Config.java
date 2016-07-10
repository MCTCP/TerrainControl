package com.khorn.terraincontrol.customobjects.bo3;

import com.khorn.terraincontrol.configuration.ConfigFile;
import com.khorn.terraincontrol.configuration.ConfigFunction;
import com.khorn.terraincontrol.configuration.WorldConfig.ConfigMode;
import com.khorn.terraincontrol.configuration.io.SettingsMap;
import com.khorn.terraincontrol.configuration.standard.WorldStandardValues;
import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.customobjects.bo3.BO3Settings.OutsideSourceBlock;
import com.khorn.terraincontrol.customobjects.bo3.BO3Settings.SpawnHeightEnum;
import com.khorn.terraincontrol.util.BoundingBox;
import com.khorn.terraincontrol.util.MaterialSet;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultStructurePart;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BO3Config extends ConfigFile
{
    /**
     * The directory the BO3 is stored in.
     */
    public final File directory;

    /**
     * Map of other objects by name.
     */
    public Map<String, CustomObject> otherObjects;

    public String author;
    public String description;
    public ConfigMode settingsMode;
    public boolean tree;
    public int frequency;
    public double rarity;
    public boolean rotateRandomly;
    public SpawnHeightEnum spawnHeight;
    // Extra spawn height settings
    public int spawnHeightOffset;
    public int spawnHeightVariance;

    // Extrusion
    public BO3Settings.ExtrudeMode extrudeMode;
    public MaterialSet extrudeThroughBlocks;

    public int minHeight;
    public int maxHeight;
    public List<String> excludedBiomes;
    public MaterialSet sourceBlocks;
    public int maxPercentageOutsideSourceBlock;
    public OutsideSourceBlock outsideSourceBlock;
    public BO3PlaceableFunction[][] blocks = new BO3PlaceableFunction[4][]; // four
    // rotations
    public BO3Check[][] bo3Checks = new BO3Check[4][];
    public int maxBranchDepth;
    public BranchFunction[][] branches = new BranchFunction[4][];

    public BoundingBox[] boundingBoxes = new BoundingBox[4];

    /**
     * Creates a BO3Config from a file.
     *
     * @param reader       The settings of the BO3.
     * @param directory    The directory the BO3 is stored in.
     * @param otherObjects All other loaded objects by their name.
     */
    public BO3Config(SettingsMap reader, File directory, Map<String, CustomObject> otherObjects)
    {
        super(reader.getName());

        this.directory = directory;
        this.otherObjects = otherObjects;

        readConfigSettings(reader);
        correctSettings();
        rotateBlocksAndChecks();
    }

    @Override
    protected void writeConfigSettings(SettingsMap writer)
    {
        // The object
        writer.bigTitle("BO3 object",
                "This is the config file of a custom object.",
                "If you add this object correctly to your BiomeConfigs, it will spawn in the world.",
                "");

        writer.putSetting(BO3Settings.AUTHOR, author,
                "This is the creator of this BO3 object");

        writer.putSetting(BO3Settings.DESCRIPTION, description,
                "A short description of this BO3 object");

        writer.putSetting(BO3Settings.VERSION, "3",
                "The BO3 version, don't change this! It can be used by external applications to do a version check.");

        writer.putSetting(WorldStandardValues.SETTINGS_MODE, settingsMode,
                "The settings mode, WriteAll, WriteWithoutComments or WriteDisable. See WorldConfig.");

        // Main settings
        writer.bigTitle("Main settings");

        writer.putSetting(BO3Settings.TREE, tree,
                "This needs to be set to true to spawn the object in the Tree and Sapling resources.");

        writer.putSetting(BO3Settings.FREQUENCY, frequency,
                "The frequency of the BO3 from 1 to 200. Tries this many times to spawn this BO3 when using the CustomObject(...) resource.",
                "Ignored by Tree(..), Sapling(..) and CustomStructure(..)");

        writer.putSetting(BO3Settings.RARITY, rarity,
                "The rarity of the BO3 from 0 to 100. Each spawn attempt has rarity% chance to succeed when using the CustomObject(...) resource.",
                "Ignored by Tree(..), Sapling(..) and CustomStructure(..)");

        writer.putSetting(BO3Settings.ROTATE_RANDOMLY, rotateRandomly,
                "If you set this to true, the BO3 will be placed with a random rotation.");

        writer.putSetting(BO3Settings.SPAWN_HEIGHT, spawnHeight,
                "The spawn height of the BO3 - atMinY, randomY, highestBlock or highestSolidBlock.");

        writer.putSetting(BO3Settings.SPAWN_HEIGHT_OFFSET, spawnHeightOffset,
                "The offset from the spawn height to spawn this BO3",
                "Ex. SpawnHeight = highestSolidBlock, SpawnHeightOffset = 3; This object will spawn 3 blocks above the highest solid block");

        writer.putSetting(BO3Settings.SPAWN_HEIGHT_VARIANCE, spawnHeightVariance,
                "A random amount to offset the spawn location from the spawn offset height",
                "Ex. SpawnHeightOffset = 3, SpawnHeightVariance = 3; This object will spawn 3 to 6 blocks above the original spot it would have spawned");

        writer.smallTitle("Height Limits for the BO3.");

        writer.putSetting(BO3Settings.MIN_HEIGHT, minHeight,
                "When in randomY mode used as the minimum Y or in atMinY mode as the actual Y to spawn this BO3 at.");

        writer.putSetting(BO3Settings.MAX_HEIGHT, maxHeight,
                "When in randomY mode used as the maximum Y to spawn this BO3 at.");

        writer.smallTitle("Extrusion settings");

        writer.putSetting(BO3Settings.EXTRUDE_MODE, extrudeMode,
                "The style of extrusion you wish to use - BottomDown, TopUp, None (Default)");

        writer.putSetting(BO3Settings.EXTRUDE_THROUGH_BLOCKS, extrudeThroughBlocks,
                "The blocks to extrude your BO3 through");

        writer.putSetting(BO3Settings.MAX_BRANCH_DEPTH, maxBranchDepth,
                "Objects can have other objects attacthed to it: branches. Branches can also",
                "have branches attached to it, which can also have branches, etc. This is the",
                "maximum branch depth for this objects.");

        writer.putSetting(BO3Settings.EXCLUDED_BIOMES, excludedBiomes,
                "When spawned with the UseWorld keyword, this BO3 should NOT spawn in the following biomes.",
                "If you write the BO3 name directly in the BiomeConfigs, this will be ignored.");

        // Sourceblock
        writer.bigTitle("Source block settings");

        writer.putSetting(BO3Settings.SOURCE_BLOCKS, sourceBlocks,
                "The block(s) the BO3 should spawn in.");

        writer.putSetting(BO3Settings.MAX_PERCENTAGE_OUTSIDE_SOURCE_BLOCK, maxPercentageOutsideSourceBlock,
                "The maximum percentage of the BO3 that can be outside the SourceBlock.",
                "The BO3 won't be placed on a location with more blocks outside the SourceBlock than this percentage.");

        writer.putSetting(BO3Settings.OUTSIDE_SOURCE_BLOCK, outsideSourceBlock,
                "What to do when a block is about to be placed outside the SourceBlock? (dontPlace, placeAnyway)");

        // Blocks and other things
        writeResources(writer);
    }

    @Override
    protected void readConfigSettings(SettingsMap reader)
    {
        author = reader.getSetting(BO3Settings.AUTHOR);
        description = reader.getSetting(BO3Settings.DESCRIPTION);
        settingsMode = reader.getSetting(WorldStandardValues.SETTINGS_MODE);

        tree = reader.getSetting(BO3Settings.TREE);
        frequency = reader.getSetting(BO3Settings.FREQUENCY);
        rarity = reader.getSetting(BO3Settings.RARITY);
        rotateRandomly = reader.getSetting(BO3Settings.ROTATE_RANDOMLY);
        spawnHeight = reader.getSetting(BO3Settings.SPAWN_HEIGHT);
        spawnHeightOffset = reader.getSetting(BO3Settings.SPAWN_HEIGHT_OFFSET);
        spawnHeightVariance = reader.getSetting(BO3Settings.SPAWN_HEIGHT_VARIANCE);
        extrudeMode = reader.getSetting(BO3Settings.EXTRUDE_MODE);
        extrudeThroughBlocks = reader.getSetting(BO3Settings.EXTRUDE_THROUGH_BLOCKS);
        minHeight = reader.getSetting(BO3Settings.MIN_HEIGHT);
        maxHeight = reader.getSetting(BO3Settings.MAX_HEIGHT);
        maxBranchDepth = reader.getSetting(BO3Settings.MAX_BRANCH_DEPTH);
        excludedBiomes = new ArrayList<String>(reader.getSetting(BO3Settings.EXCLUDED_BIOMES));

        sourceBlocks = reader.getSetting(BO3Settings.SOURCE_BLOCKS);
        maxPercentageOutsideSourceBlock = reader.getSetting(BO3Settings.MAX_PERCENTAGE_OUTSIDE_SOURCE_BLOCK);
        outsideSourceBlock = reader.getSetting(BO3Settings.OUTSIDE_SOURCE_BLOCK);

        // Read the config functions
        readConfigFunctions(reader);
    }

    private void readConfigFunctions(SettingsMap reader)
    {
        BoundingBox box = BoundingBox.newEmptyBox();
        List<BO3PlaceableFunction> tempBlocksList = new ArrayList<BO3PlaceableFunction>();
        List<BO3Check> tempChecksList = new ArrayList<BO3Check>();
        List<BranchFunction> tempBranchesList = new ArrayList<BranchFunction>();

        for (ConfigFunction<BO3Config> res : reader.getConfigFunctions(this, true))
        {
            if (res instanceof BO3PlaceableFunction)
            {
                BO3PlaceableFunction block = (BO3PlaceableFunction) res;
                box.expandToFit(block.x, block.y, block.z);
                tempBlocksList.add(block);
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

        // Store the blocks
        blocks[0] = tempBlocksList.toArray(new BO3PlaceableFunction[tempBlocksList.size()]);
        bo3Checks[0] = tempChecksList.toArray(new BO3Check[tempChecksList.size()]);
        branches[0] = tempBranchesList.toArray(new BranchFunction[tempBranchesList.size()]);
        boundingBoxes[0] = box;
    }

    public void writeResources(SettingsMap writer)
    {
        // Blocks
        writer.bigTitle("Blocks",
                "All the blocks used in the BO3 are listed here. Possible blocks:",
                "Block(x,y,z,id[.data][,nbtfile.nbt)",
                "RandomBlock(x,y,z,id[:data][,nbtfile.nbt],chance[,id[:data][,nbtfile.nbt],chance[,...]])",
                " So RandomBlock(0,0,0,CHEST,chest.nbt,50,CHEST,anotherchest.nbt,100) will spawn a chest at",
                " the BO3 origin, and give it a 50% chance to have the contents of chest.nbt, or, if that",
                " fails, a 100% percent chance to have the contents of anotherchest.nbt.",
                "MinecraftObject(x,y,z,name)",
                " Spawns an object in the Mojang NBT structure format. For example, ",
                " MinecraftObject(0,0,0," + DefaultStructurePart.IGLOO_BOTTOM.getPath() + ")",
                " spawns the bottom part of an igloo.");

        writer.addConfigFunctions(Arrays.asList(blocks[0]));

        // BO3Checks
        writer.bigTitle("BO3 checks",
                "Require a condition at a certain location in order for the BO3 to be spawned.",
                "BlockCheck(x,y,z,BlockName[,BlockName[,...]]) - one of the blocks must be at the location",
                "BlockCheckNot(x,y,z,BlockName[,BlockName[,...]]) - all the blocks must not be at the location",
                "LightCheck(x,y,z,minLightLevel,maxLightLevel) - light must be between min and max (inclusive)",
                "",
                "You can use \"Solid\" as a BlockName for matching all solid blocks or \"All\" to match all blocks that aren't air.",
                "",
                "Examples:",
                "  BlockCheck(0,-1,0,GRASS,DIRT)  Require grass or dirt just below the object",
                "  BlockCheck(0,-1,0,Solid)       Require any solid block just below the object",
                "  BlockCheck(0,-1,0,WOOL)        Require any type of wool just below the object",
                "  BlockCheck(0,-1,0,WOOL:0)      Require white wool just below the object",
                "  BlockCheckNot(0,-1,0,WOOL:0)   Require that there is no white wool below the object",
                "  LightCheck(0,0,0,0,1)          Require almost complete darkness just below the object");

        writer.addConfigFunctions(Arrays.asList(bo3Checks[0]));

        // Branches
        writer.bigTitle("Branches",
                "Branches are objects that will spawn when this object spawns when it is used in",
                "the CustomStructure resource. Branches can also have branches, making complex",
                "structures possible. See the wiki for more details.",
                "",
                "Regular Branches spawn each branch with an independent chance of spawning.",
                "Branch(x,y,z,branchName,rotation,chance[,anotherBranchName,rotation,chance[,...]][IndividualChance])",
                "branchName - name of the object to spawn.",
                "rotation - NORTH, SOUTH, EAST or WEST.",
                "IndividualChance - The chance each branch has to spawn, assumed to be 100 when left blank",
                "",
                "Weighted Branches spawn branches with a dependent chance of spawning.",
                "WeightedBranch(x,y,z,branchName,rotation,chance[,anotherBranchName,rotation,chance[,...]][MaxChanceOutOf])",
                "MaxChanceOutOf - The chance all branches have to spawn out of, assumed to be 100 when left blank");
        writer.addConfigFunctions(Arrays.asList(branches[0]));
    }

    @Override
    protected void correctSettings()
    {
        maxHeight = higherThanOrEqualTo(maxHeight, minHeight);
    }

    @Override
    protected void renameOldSettings(SettingsMap reader)
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
            // Blocks (blocks[i - 1] is previous rotation)
            blocks[i] = new BO3PlaceableFunction[blocks[i - 1].length];
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
            // Bounding box
            boundingBoxes[i] = boundingBoxes[i - 1].rotate();
        }
    }

}
