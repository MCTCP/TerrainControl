package com.pg85.otg.customobjects.bo3;

import com.pg85.otg.configuration.customobjects.CustomObjectConfigFile;
import com.pg85.otg.configuration.customobjects.CustomObjectConfigFunction;
import com.pg85.otg.configuration.io.SettingsReaderOTGPlus;
import com.pg85.otg.configuration.io.SettingsWriterOTGPlus;
import com.pg85.otg.configuration.standard.WorldStandardValues;
import com.pg85.otg.configuration.world.WorldConfig.ConfigMode;
import com.pg85.otg.customobjects.bo3.BO3Settings.OutsideSourceBlock;
import com.pg85.otg.customobjects.bo3.BO3Settings.SpawnHeightEnum;
import com.pg85.otg.customobjects.bo3.bo3function.BO3BlockFunction;
import com.pg85.otg.customobjects.bo3.bo3function.BO3BranchFunction;
import com.pg85.otg.customobjects.bo3.bo3function.BO3EntityFunction;
import com.pg85.otg.customobjects.bo3.bo3function.BO3ModDataFunction;
import com.pg85.otg.customobjects.bo3.bo3function.BO3ParticleFunction;
import com.pg85.otg.customobjects.bo3.bo3function.BO3SpawnerFunction;
import com.pg85.otg.customobjects.bo3.bo3function.BO3WeightedBranchFunction;
import com.pg85.otg.customobjects.bo3.checks.BO3Check;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.bo3.BoundingBox;
import com.pg85.otg.util.materials.MaterialSet;
import com.pg85.otg.util.minecraft.defaults.DefaultStructurePart;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BO3Config extends CustomObjectConfigFile
{
	private boolean isOTGPlus; // Legacy setting
    
    public String author;
    public String description;
    ConfigMode settingsMode;
    boolean tree;
    int frequency;
    double rarity;
    protected boolean rotateRandomly;
    public SpawnHeightEnum spawnHeight;
    // Extra spawn height settings
    public int spawnHeightOffset;
    int spawnHeightVariance;

    // Extrusion
    BO3Settings.ExtrudeMode extrudeMode;
    MaterialSet extrudeThroughBlocks;

    public int minHeight;
    public int maxHeight;
    private List<String> excludedBiomes;
    protected MaterialSet sourceBlocks;
    int maxPercentageOutsideSourceBlock;
    OutsideSourceBlock outsideSourceBlock;
    BO3BlockFunction[][] blocks = new BO3BlockFunction[4][]; // four rotations
    BO3Check[][] bo3Checks = new BO3Check[4][];
    int maxBranchDepth;
    BO3BranchFunction[][] branches = new BO3BranchFunction[4][];

    BoundingBox[] boundingBoxes = new BoundingBox[4];

    BO3ParticleFunction[][] particleFunctions = new BO3ParticleFunction[4][];
    BO3SpawnerFunction[][] spawnerFunctions = new BO3SpawnerFunction[4][];
    BO3ModDataFunction[][] modDataFunctions = new BO3ModDataFunction[4][];
    BO3EntityFunction[][] entityFunctions = new BO3EntityFunction[4][];

    /**
     * Creates a BO3Config from a file.
     *
     * @param reader       The settings of the BO3.
     * @param directory    The directory the BO3 is stored in.
     * @param otherObjects All other loaded objects by their name.
     */
    protected BO3Config(SettingsReaderOTGPlus reader) throws InvalidConfigException
    {
        super(reader);
        init();
    }

    private void init() throws InvalidConfigException
    {
    	this.isOTGPlus = false;
        readConfigSettings();
    	rotateBlocksAndChecks();
    }
    
    private void readResources(boolean blocksOnly) throws InvalidConfigException
    {
        List<BO3BlockFunction> tempBlocksList = new ArrayList<BO3BlockFunction>();
        List<BO3Check> tempChecksList = new ArrayList<BO3Check>();
        List<BO3BranchFunction> tempBranchesList = new ArrayList<BO3BranchFunction>();
        List<BO3EntityFunction> tempEntitiesList = new ArrayList<BO3EntityFunction>();
        List<BO3ModDataFunction> tempModDataList = new ArrayList<BO3ModDataFunction>();
        List<BO3ParticleFunction> tempParticlesList = new ArrayList<BO3ParticleFunction>();
        List<BO3SpawnerFunction> tempSpawnerList = new ArrayList<BO3SpawnerFunction>();

    	tempBlocksList = new ArrayList<BO3BlockFunction>();
    	tempChecksList = new ArrayList<BO3Check>();
    	tempBranchesList = new ArrayList<BO3BranchFunction>();
    	tempEntitiesList = new ArrayList<BO3EntityFunction>();
    	tempModDataList = new ArrayList<BO3ModDataFunction>();
    	tempParticlesList = new ArrayList<BO3ParticleFunction>();
    	tempSpawnerList = new ArrayList<BO3SpawnerFunction>();

        BoundingBox box = BoundingBox.newEmptyBox();

        for (CustomObjectConfigFunction<BO3Config> res : reader.getConfigFunctions(this, true))
        {
            if (res instanceof BO3BlockFunction)
            {
            	BO3BlockFunction block = (BO3BlockFunction) res;
                box.expandToFit(block.x, block.y, block.z);
                tempBlocksList.add(block);
            }
            else if(!blocksOnly)
            {
                if (res instanceof BO3Check)
                {
                    tempChecksList.add((BO3Check) res);
                }
                else if (res instanceof BO3WeightedBranchFunction)
                {
                    tempBranchesList.add((BO3WeightedBranchFunction) res);
                }
                else if (res instanceof BO3BranchFunction)
                {
                    tempBranchesList.add((BO3BranchFunction) res);
                }
                else if (res instanceof BO3EntityFunction)
                {
                    tempEntitiesList.add((BO3EntityFunction) res);
                }
                else if (res instanceof BO3ParticleFunction)
                {
                	tempParticlesList.add((BO3ParticleFunction) res);
                }
                else if (res instanceof BO3ModDataFunction)
                {
                	tempModDataList.add((BO3ModDataFunction) res);
                }
                else if (res instanceof BO3SpawnerFunction)
                {
                	tempSpawnerList.add((BO3SpawnerFunction) res);
                }
            }
        }

        // Store the blocks
        blocks[0] = tempBlocksList.toArray(new BO3BlockFunction[tempBlocksList.size()]);
        if(!blocksOnly)
        {
            bo3Checks[0] = tempChecksList.toArray(new BO3Check[tempChecksList.size()]);
            branches[0] = tempBranchesList.toArray(new BO3BranchFunction[tempBranchesList.size()]);
            boundingBoxes[0] = box;
            entityFunctions[0] = tempEntitiesList.toArray(new BO3EntityFunction[tempEntitiesList.size()]);
            particleFunctions[0] = tempParticlesList.toArray(new BO3ParticleFunction[tempParticlesList.size()]);
            modDataFunctions[0] = tempModDataList.toArray(new BO3ModDataFunction[tempModDataList.size()]);
            spawnerFunctions[0] = tempSpawnerList.toArray(new BO3SpawnerFunction[tempSpawnerList.size()]);
        }
    }

    /**
     * Gets the file this config will be written to. May be null if the config
     * will never be written.
     * @return The file.
     */
    public File getFile()
    {
    	return this.reader.getFile();
    }

    protected BO3BranchFunction[] getbranches()
    {
    	return branches[0];
    }

    public BO3ModDataFunction[] getModData()
    {
    	return modDataFunctions[0];
    }

    public BO3SpawnerFunction[] getSpawnerData()
    {
    	return spawnerFunctions[0];
    }

    public BO3ParticleFunction[] getParticleData()
    {
    	return particleFunctions[0];
    }

    public BO3Check[] getBO3Checks()
    {
    	return bo3Checks[0];
    }

    public BO3EntityFunction[] getEntityData()
    {
    	return entityFunctions[0];
    }
    
    @Override
    protected void writeConfigSettings(SettingsWriterOTGPlus writer) throws IOException
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
        writer.setting(WorldStandardValues.SETTINGS_MODE_BO3, settingsMode);

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

        writer.comment("The spawn height of the BO3: randomY, highestBlock or highestSolidBlock.");
        writer.setting(BO3Settings.SPAWN_HEIGHT, spawnHeight);

		writer.comment("The offset from the spawn height to spawn this BO3");
		writer.comment("Ex. SpawnHeight = highestSolidBlock, SpawnHeightOffset = 3; This object will spawn 3 blocks above the highest solid block");
        writer.setting(BO3Settings.SPAWN_HEIGHT_OFFSET, spawnHeightOffset);

		writer.comment("A random amount to offset the spawn location from the spawn offset height");
		writer.comment("Ex. SpawnHeightOffset = 3, SpawnHeightVariance = 3; This object will spawn 3 to 6 blocks above the original spot it would have spawned");
        writer.setting(BO3Settings.SPAWN_HEIGHT_VARIANCE, spawnHeightVariance);

        writer.smallTitle("Height Limits for the BO3.");

		writer.comment("When in randomY mode used as the minimum Y or in atMinY mode as the actual Y to spawn this BO3 at.");
        writer.setting(BO3Settings.MIN_HEIGHT, minHeight);

		writer.comment("When in randomY mode used as the maximum Y to spawn this BO3 at.");
        writer.setting(BO3Settings.MAX_HEIGHT, maxHeight);

        writer.smallTitle("Extrusion settings");

		writer.comment("The style of extrusion you wish to use - BottomDown, TopUp, None (Default)");
        writer.setting(BO3Settings.EXTRUDE_MODE, extrudeMode);

		writer.comment("The blocks to extrude your BO3 through");
        writer.setting(BO3Settings.EXTRUDE_THROUGH_BLOCKS, extrudeThroughBlocks);

		writer.comment("Objects can have other objects attacthed to it: branches. Branches can also");
		writer.comment("have branches attached to it, which can also have branches, etc. This is the");
		writer.comment("maximum branch depth for this objects.");
        writer.setting(BO3Settings.MAX_BRANCH_DEPTH, maxBranchDepth);

		writer.comment("When spawned with the UseWorld keyword, this BO3 should NOT spawn in the following biomes.");
		writer.comment("If you write the BO3 name directly in the BiomeConfigs, this will be ignored.");
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

        writer.comment("OTG+ settings #");

        writer.comment("Legacy setting, rename this file to .BO4 instead. Set this to true to enable the advanced customstructure features of OTG+.");
        writer.setting(BO3Settings.IS_OTG_PLUS, isOTGPlus);

        // Blocks and other things
        writeResources(writer);
    }

    @Override
    protected void readConfigSettings() throws InvalidConfigException
    {
    	isOTGPlus = readSettings(BO3Settings.IS_OTG_PLUS);

    	if(isOTGPlus)
    	{
    		throw new InvalidConfigException("isOTGPlus: true for a .bo3 file, file must be .bo4.");
    	}
    	
        author = readSettings(BO3Settings.AUTHOR);
        description = readSettings(BO3Settings.DESCRIPTION);
        settingsMode = readSettings(WorldStandardValues.SETTINGS_MODE_BO3);

        tree = readSettings(BO3Settings.TREE);
        frequency = readSettings(BO3Settings.FREQUENCY);
        rarity = readSettings(BO3Settings.RARITY);
        rotateRandomly = readSettings(BO3Settings.ROTATE_RANDOMLY);
        spawnHeight = readSettings(BO3Settings.SPAWN_HEIGHT);
        spawnHeightOffset = readSettings(BO3Settings.SPAWN_HEIGHT_OFFSET);
        spawnHeightVariance = readSettings(BO3Settings.SPAWN_HEIGHT_VARIANCE);
        extrudeMode = readSettings(BO3Settings.EXTRUDE_MODE);
        extrudeThroughBlocks = readSettings(BO3Settings.EXTRUDE_THROUGH_BLOCKS);
        minHeight = readSettings(BO3Settings.MIN_HEIGHT);
        maxHeight = readSettings(BO3Settings.MAX_HEIGHT);
		maxHeight = maxHeight < minHeight ? minHeight : maxHeight;
        maxBranchDepth = readSettings(BO3Settings.MAX_BRANCH_DEPTH);
        excludedBiomes = new ArrayList<String>(readSettings(BO3Settings.EXCLUDED_BIOMES));

        sourceBlocks = readSettings(BO3Settings.SOURCE_BLOCKS);
        maxPercentageOutsideSourceBlock = readSettings(BO3Settings.MAX_PERCENTAGE_OUTSIDE_SOURCE_BLOCK);
        outsideSourceBlock = readSettings(BO3Settings.OUTSIDE_SOURCE_BLOCK);

        // Read the resources
        readResources(false);

        this.reader.flushCache();
    }
    
    private void writeResources(SettingsWriterOTGPlus writer) throws IOException
    {
        writer.bigTitle("Blocks");
        writer.comment("All the blocks used in the BO3 are listed here. Possible blocks:");
        writer.comment("Block(x,y,z,id[.data][,nbtfile.nbt)");
        writer.comment("RandomBlock(x,y,z,id[:data][,nbtfile.nbt],chance[,id[:data][,nbtfile.nbt],chance[,...]])");
        writer.comment(" So RandomBlock(0,0,0,CHEST,chest.nbt,50,CHEST,anotherchest.nbt,100) will spawn a chest at");
        writer.comment(" the BO3 origin, and give it a 50% chance to have the contents of chest.nbt, or, if that");
        writer.comment(" fails, a 100% percent chance to have the contents of anotherchest.nbt.");
        writer.comment("MinecraftObject(x,y,z,name) (TODO: This may not work anymore and needs to be tested.");
        writer.comment(" Spawns an object in the Mojang NBT structure format. For example, ");
        writer.comment(" MinecraftObject(0,0,0," + DefaultStructurePart.IGLOO_BOTTOM.getPath() + ")");
        writer.comment(" spawns the bottom part of an igloo.");

        for(BO3BlockFunction func : Arrays.asList(blocks[0]))
        {
        	writer.function(func);
        }

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

        for(BO3Check func : Arrays.asList(bo3Checks[0]))
        {
        	writer.function(func);
        }

        writer.bigTitle("Branches");
        writer.comment("Branches are child-BO3's that spawn if this BO3 is configured to spawn as a");
        writer.comment("CustomStructure resource in a biome config. Branches can have branches,");
        writer.comment("making complex structures possible. See the wiki for more details.");
        writer.comment("");
        writer.comment("Regular Branches spawn each branch with an independent chance of spawning.");
        writer.comment("Branch(x,y,z,branchName,rotation,chance[,anotherBranchName,rotation,chance[,...]][IndividualChance])");
        writer.comment("branchName - name of the object to spawn.");
        writer.comment("rotation - NORTH, SOUTH, EAST or WEST.");
        writer.comment("IndividualChance - The chance each branch has to spawn, assumed to be 100 when left blank");
        writer.comment("isRequiredBranch - If this is set to true then at least one of the branches in this BO3 must spawn at these x,y,z coordinates. If no branch can spawn there then this BO3 fails to spawn and its branch is rolled back.");
        writer.comment("isRequiredBranch:true branches must spawn or the current branch is rolled back entirely. This is useful for grouping BO3's that must spawn together, for instance a single room made of multiple BO3's/branches.");
        writer.comment("If all parts of the room are connected together via isRequiredBranch:true branches then either the entire room will spawns or no part of it will spawn.");
        writer.comment("*Note: When isRequiredBranch:true only one BO3 can be added per Branch() and it will automatically have a rarity of 100.0.");
        writer.comment("isRequiredBranch:false branches are used to make optional parts of structures, for instance the middle section of a tunnel that has a beginning, middle and end BO3/branch and can have a variable length by repeating the middle BO3/branch.");
        writer.comment("By making the start and end branches isRequiredBranch:true and the middle branch isRequiredbranch:false you can make it so that either:");
		writer.comment("A. A tunnel spawns with at least a beginning and end branch");
		writer.comment("B. A tunnel spawns with a beginning and end branch and as many middle branches as will fit in the available space.");
		writer.comment("C. No tunnel spawns at all because there wasn't enough space to spawn at least a beginning and end branch.");
        writer.comment("branchDepth - When creating a chain of branches that contains optional (isRequiredBranch:false) branches branch depth is configured for the first BO3 in the chain to determine the maximum length of the chain.");
        writer.comment("branchDepth - 1 is inherited by each isRequiredBranch:false branch in the chain. When branchDepth is zero isRequiredBranch:false branches cannot spawn and the chain ends. In the case of the tunnel this means the last middle branch would be");
        writer.comment("rolled back and an IsRequiredBranch:true end branch could be spawned in its place to make sure the tunnel has a proper ending.");
        writer.comment("Instead of inheriting branchDepth - 1 from the parent branchDepth can be overridden by child branches if it is set higher than 0 (the default value).");
        writer.comment("isRequiredBranch:true branches do inherit branchDepth and pass it on to their own branches, however they cannot be prevented from spawning by it and also don't subtract 1 from branchDepth when inheriting it.");
        writer.comment("");
        writer.comment("Weighted Branches spawn branches with a dependent chance of spawning.");
        writer.comment("WeightedBranch(x,y,z,branchName,rotation,chance[,anotherBranchName,rotation,chance[,...]][MaxChanceOutOf])");
        writer.comment("*Note: isRequiredBranch must be set to false. It is not possible to use isRequiredBranch:true with WeightedBranch() since isRequired:true branches must spawn and automatically have a rarity of 100.0.");
        writer.comment("MaxChanceOutOf - The chance all branches have to spawn out of, assumed to be 100 when left blank");

        for(BO3BranchFunction func : Arrays.asList(branches[0]))
        {
        	writer.function(func);
        }

        writer.bigTitle("Entities");
        writer.comment("Forge only (this may have changed, check for updates).");
        writer.comment("An EntityFunction spawns an entity instead of a block. The entity is spawned only once when the BO3 is spawned.");
        writer.comment("Entities are persistent by default so they don't de-spawn when no player is near, they are only unloaded.");
        writer.comment("Usage: Entity(x,y,z,entityName,groupSize,NameTagOrNBTFileName) or Entity(x,y,z,mobName,groupSize)");
        writer.comment("Use /otg entities to get a list of entities that can be used as entityName, this includes entities added by other mods and non-living entities.");
        writer.comment("NameTagOrNBTFileName can be either a nametag for the mob or an .txt file with nbt data (such as myentityinfo.txt).");
        writer.comment("In the text file you can use the same mob spawning parameters used with the /summon command to equip the");
        writer.comment("entity and give it custom attributes etc. You can copy the DATA part of a summon command including surrounding ");
        writer.comment("curly braces to a .txt file, for instance for: \"/summon Skeleton x y z {DATA}\"");

        for(BO3EntityFunction func : Arrays.asList(entityFunctions[0]))
        {
        	writer.function(func);
        }

        writer.bigTitle("Particles");
        writer.comment("Forge only (this may have changed, check for updates).");
        writer.comment("Creates an invisible particle spawner at the given location that spawns particles every x milliseconds.");
        writer.comment("Usage: Particle(x,y,z,particleName,interval,velocityX,velocityY,velocityZ)");
        writer.comment("velocityX, velocityY and velocityZ are optional.");
        writer.comment("Only vanilla particle names can be used, for 1.11.2 these are;");
        writer.comment("explode, largeexplode, hugeexplosion, fireworksSpark, bubble, splash, wake, suspended");
		writer.comment("depthsuspend, crit, magicCrit, smoke, largesmoke, spell, instantSpell, mobSpell");
		writer.comment("mobSpellAmbient, witchMagic, dripWater, dripLava, angryVillager, happyVillager");
		writer.comment("townaura, note, portal, enchantmenttable, flame, lava, footstep, cloud, reddust");
		writer.comment("snowballpoof,  snowshovel, slime, heart, barrier, iconcrack, blockcrack, blockdust");
		writer.comment("droplet, take, mobappearance, dragonbreath, endRod, damageIndicator, sweepAttack");
		writer.comment("fallingdust, totem, spit.");
		writer.comment("velocityX,velocityY,velocityZ - Spawn the enemy with the given velocity. If this is not filled in then a small random velocity is applied.");

        for(BO3ParticleFunction func : Arrays.asList(particleFunctions[0]))
        {
        	writer.function(func);
        }

        writer.bigTitle("Spawners");
        writer.comment("Forge only (this may have changed, check for updates).");
        writer.comment("Creates an invisible entity spawner at the given location that spawns entities every x seconds.");
        writer.comment("Entities can only spawn if their spawn requirements are met (zombies/skeletons only spawn in the dark etc). Max entity count for the server is ignored, each spawner has its own maxCount setting.");
        writer.comment("Usage: Spawner(x,y,z,entityName,nbtFileName,groupSize,interval,spawnChance,maxCount,despawnTime,velocityX,velocityY,velocityZ,yaw,pitch)");
        writer.comment("nbtFileName, despawnTime, velocityX, velocityY, velocityZ, yaw and pitch are optional");
        writer.comment("Example Spawner(0, 0, 0, Villager, 1, 5, 100, 5) or Spawner(0, 0, 0, Villager, villager1.txt, 1, 5, 100, 5) or Spawner(0, 0, 0, Villager, 1, 5, 100, 5, 30, 1, 1, 1, 0, 0)");
        writer.comment("entityName - Name of the entity to spawn, use /otg entities to get a list of entities that can be used as entityName, this includes entities added by other mods and non-living entities.");
        writer.comment("nbtFileName - A .txt file with nbt data (such as myentityinfo.txt).");
        writer.comment("In the text file you can use the same mob spawning parameters used with the /summon command to equip the");
        writer.comment("entity and give it custom attributes etc. You can copy the DATA part of a summon command including surrounding ");
        writer.comment("curly braces to a .txt file, for instance for: \"/summon Skeleton x y z {DATA}\"");
        writer.comment("groupSize - Number of entities that should spawn for each successful spawn attempt.");
        writer.comment("interval - Time in seconds between each spawn attempt.");
        writer.comment("spawnChance - For each spawn attempt, the chance between 0-100 that the spawn attempt will succeed.");
        writer.comment("maxCount - The maximum amount of this kind of entity that can exist within 32 blocks. If there are already maxCount or more entities of this type in a 32 radius this spawner will not spawn anything.");
        writer.comment("despawnTime - After despawnTime seconds, if there is no player within 32 blocks of the entity it will despawn..");
        writer.comment("velocityX,velocityY,velocityZ,yaw,pitch - Spawn the enemy with the given velocity and angle, handy for making traps and launchers (shooting arrows and fireballs etc).");

        for(BO3SpawnerFunction func : Arrays.asList(spawnerFunctions[0]))
        {
        	writer.function(func);
        }

        // ModData
        writer.bigTitle("ModData");
        writer.comment("Forge only.");
        writer.comment("Use the ModData() tag to include data that other mods can use");
        writer.comment("Mod makers can use ModData and the /otg GetModData command to test IMC communications between OTG");
        writer.comment("and their mod.");
        writer.comment("Normal users can use it to spawn some mobs and blocks on command.");
        writer.comment("ModData(x,y,z,\"ModName\", \"MyModDataAsText\"");
        writer.comment("Example: ModData(x,y,z,MyCystomNPCMod,SpawnBobHere/WithAPotato/And50Health)");
        writer.comment("Try not to use exotic/reserved characters, like brackets and comma's etc, this stuff isn't fool-proof.");
        writer.comment("Also, use this only to store IDs/object names etc for your mod, DO NOT include things like character dialogue,");
        writer.comment("messages on signs, loot lists etc in this file. As much as possible just store id's/names here and store all the data related to those id's/names in your own mod.");
        writer.comment("OTG has some built in ModData commands for basic mob and block spawning.");
        writer.comment("These are mostly just a demonstration for mod makers to show how ModData.");
        writer.comment("can be used by other mods.");
        writer.comment("For mob spawning in OTG use: ModData(x,y,z,OTG,mob/MobType/Count/Persistent/Name)");
        writer.comment("mob: Makes OTG recognise this as a mob spawning command.");
        writer.comment("MobType: Lower-case, no spaces. Any vanilla mob like dragon, skeleton, wither, villager etc");
        writer.comment("Count: The number of mobs to spawn");
        writer.comment("Persistent (true/false): Should the mobs never de-spawn? If set to true the mob will get a");
        writer.comment("name-tag ingame so you can recognise it.");
        writer.comment("Name: A name-tag for the monster/npc.");
        writer.comment("Example: ModData(0,0,0,OTG,villager/1/true/Bob)");
        writer.comment("To spawn blocks using ModData use: ModData(x,y,z,OTG,block/material)");
        writer.comment("block: Makes OTG recognise this as a block spawning command.");
        writer.comment("material: id or text, custom blocks can be added using ModName:MaterialName.");
        writer.comment("To send all ModData within a radius in chunks around the player to the specified mod");
        writer.comment("use this console command: /otg GetModData ModName Radius");
        writer.comment("ModName: name of the mod, for OTG commands use OTG ");
        writer.comment("Radius (optional): Radius in chunks around the player.");

        for(BO3ModDataFunction func : Arrays.asList(modDataFunctions[0]))
        {
        	writer.function(func);
        }
    }

    @Override
    protected void correctSettings()
    {

    }

    @Override
    protected void renameOldSettings()
    {
        // Stub method - there are no old setting to convert yet (:
    }

    /**
     * Rotates all the blocks and all the checks
     */
    private void rotateBlocksAndChecks()
    {
        for (int i = 1; i < 4; i++)
        {
            // Blocks (blocks[i - 1] is previous rotation)
            blocks[i] = new BO3BlockFunction[blocks[i - 1].length];
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
            branches[i] = new BO3BranchFunction[branches[i - 1].length];
            for (int j = 0; j < branches[i].length; j++)
            {
                branches[i][j] = branches[i - 1][j].rotate();
            }
            // Bounding box
            boundingBoxes[i] = boundingBoxes[i - 1].rotate();

            entityFunctions[i] = new BO3EntityFunction[entityFunctions[i - 1].length];
            for (int j = 0; j < entityFunctions[i].length; j++)
            {
            	entityFunctions[i][j] = entityFunctions[i - 1][j].rotate();
            }

            particleFunctions[i] = new BO3ParticleFunction[particleFunctions[i - 1].length];
            for (int j = 0; j < particleFunctions[i].length; j++)
            {
            	particleFunctions[i][j] = particleFunctions[i - 1][j].rotate();
            }

            spawnerFunctions[i] = new BO3SpawnerFunction[spawnerFunctions[i - 1].length];
            for (int j = 0; j < spawnerFunctions[i].length; j++)
            {
            	spawnerFunctions[i][j] = spawnerFunctions[i - 1][j].rotate();
            }

            modDataFunctions[i] = new BO3ModDataFunction[modDataFunctions[i - 1].length];
            for (int j = 0; j < modDataFunctions[i].length; j++)
            {
            	modDataFunctions[i][j] = modDataFunctions[i - 1][j].rotate();
            }
        }
    }
}
