package com.pg85.otg.customobjects.bo3;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.io.FileSettingsReaderOTGPlus;
import com.pg85.otg.configuration.io.FileSettingsWriterOTGPlus;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.configuration.world.WorldConfig.ConfigMode;
import com.pg85.otg.customobjects.bo3.BO3Settings.OutsideSourceBlock;
import com.pg85.otg.customobjects.bo3.BO3Settings.SpawnHeightEnum;
import com.pg85.otg.customobjects.bo3.bo3function.BO3BlockFunction;
import com.pg85.otg.customobjects.bo3.bo3function.BO3EntityFunction;
import com.pg85.otg.customobjects.bo3.bo3function.BO3ModDataFunction;
import com.pg85.otg.customobjects.bo3.bo3function.BO3ParticleFunction;
import com.pg85.otg.customobjects.bo3.bo3function.BO3SpawnerFunction;
import com.pg85.otg.customobjects.bo3.checks.BO3Check;
import com.pg85.otg.customobjects.structures.Branch;
import com.pg85.otg.customobjects.structures.CustomStructure;
import com.pg85.otg.customobjects.structures.CustomStructureCoordinate;
import com.pg85.otg.customobjects.structures.StructuredCustomObject;
import com.pg85.otg.customobjects.structures.bo3.BO3CustomStructure;
import com.pg85.otg.customobjects.structures.bo3.BO3CustomStructureCoordinate;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.bo3.BoundingBox;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.helpers.MathHelper;
import com.pg85.otg.util.helpers.RandomHelper;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

public class BO3 implements StructuredCustomObject
{
    private BO3Config settings;
    private final String name;
    private final File file;

    /**
     * Creates a BO3 from a file.
     *
     * @param name Name of the BO3.
     * @param file File of the BO3. If the file does not exist, a BO3 with the default settings is created.
     */
    BO3(String name, File file)
    {
        this.name = name;
        this.file = file;
    }
    
    @Override
    public String getName()
    {
        return name;
    }

    public BO3Config getSettings()
    {
        return settings;
    }
    
    @Override
    public boolean onEnable()
    {
    	try
    	{
    		this.settings = new BO3Config(new FileSettingsReaderOTGPlus(name, file));
    		if(this.settings.settingsMode != ConfigMode.WriteDisable)
    		{
    			FileSettingsWriterOTGPlus.writeToFile(this.settings, this.settings.settingsMode);
    		}
    	}
    	catch(InvalidConfigException ex)
    	{
    		return false;
    	}
    	return true;
    }    

    @Override
    public boolean canSpawnAsTree()
    {
        return settings.tree;
    }

    // Used for saplings
    @Override
    public boolean canRotateRandomly()
    {
        return settings.rotateRandomly;
    }
        
    // Used for Tree() and CustomObject
    @Override
    public boolean trySpawnAt(LocalWorld world, Random random, Rotation rotation, int x, int y, int z)
    {
    	return trySpawnAt(false, null, world, random, rotation, x, y, z, settings.minHeight, settings.maxHeight);
    }
    
    @Override
    public boolean trySpawnAt(LocalWorld world, Random random, Rotation rotation, int x, int y, int z, int minY, int maxY)
    {
    	return trySpawnAt(false, null, world, random, rotation, x, y, z, minY, maxY);
    }
    
    // Used for spawning saplings and customobjects without doing checks (for growing saplings, /spawn command, StructureAtSpawn etc).
    @Override
    public boolean spawnForced(LocalWorld world, Random random, Rotation rotation, int x, int y, int z)
    {
		return trySpawnAt(true, null, world, random, rotation, x, y, z, settings.minHeight, settings.maxHeight);
    }

	// This method is only used to spawn CustomObject.
    @Override
    public boolean process(LocalWorld world, Random random, ChunkCoordinate chunkCoord)
    {
        boolean atLeastOneObjectHasSpawned = false;

        int chunkMiddleX = chunkCoord.getBlockXCenter();
        int chunkMiddleZ = chunkCoord.getBlockZCenter();
        for (int i = 0; i < settings.frequency; i++)
        {
            if (settings.rarity > random.nextDouble() * 100.0)
            {
                int x = chunkMiddleX + random.nextInt(ChunkCoordinate.CHUNK_X_SIZE);
                int z = chunkMiddleZ + random.nextInt(ChunkCoordinate.CHUNK_Z_SIZE);

                if (spawn(world, random, x, z, settings.minHeight, settings.maxHeight))
                {
                    atLeastOneObjectHasSpawned = true;
                }
            }
        }

        return atLeastOneObjectHasSpawned;
    }

    @Override
    public Branch[] getBranches()
    {
        return null;
    }
    
    @Override
    public Branch[] getBranches(Rotation rotation)
    {
   		return settings.branches[rotation.getRotationId()];
    }

    @Override
    public CustomStructureCoordinate makeCustomObjectCoordinate(LocalWorld world, Random random, int chunkX, int chunkZ)
    {
        if (settings.rarity > random.nextDouble() * 100.0)
        {
            Rotation rotation = settings.rotateRandomly ? Rotation.getRandomRotation(random) : Rotation.NORTH;
            int height = RandomHelper.numberInRange(random, settings.minHeight, settings.maxHeight);
            return new BO3CustomStructureCoordinate(world, this, this.getName(), rotation, chunkX * 16 + 8 + random.nextInt(16), (short)height, chunkZ * 16 + 7 + random.nextInt(16));
        }
        return null;
    }

    @Override
    public StructurePartSpawnHeight getStructurePartSpawnHeight()
    {
        return this.settings.spawnHeight.toStructurePartSpawnHeight();
    }

    // TODO: Reimplement this?
    @Override
    public BoundingBox getBoundingBox(Rotation rotation)
    {
        return this.settings.boundingBoxes[rotation.getRotationId()];
    }

    @Override
    public int getMaxBranchDepth() // This used to be in CustomObject?
    {
        return settings.maxBranchDepth;
    }
    
    /**
     * Computes the offset and variance for spawning a bo3
     *
     * @param random   Random number generator.
     * @param offset   Base spawn offset.
     * @param variance Max variance from this offset.
     *
     * @return The sum of the offset and variance.
     */
    private int getOffsetAndVariance(Random random, int offset, int variance)
    {
        if (variance == 0)
        {
            return offset;
        } else if (variance < 0)
        {
            variance = -random.nextInt(MathHelper.abs(variance) + 1);
        } else
        {
            variance = random.nextInt(variance + 1);
        }
        return MathHelper.clamp(offset + variance, PluginStandardValues.WORLD_DEPTH, PluginStandardValues.WORLD_HEIGHT);
    }
   
    @Override
    public boolean spawnAsTree(LocalWorld world, Random random, int x, int z)
    {
    	return spawn(world, random, x, z, this.getSettings().minHeight, this.getSettings().maxHeight);
    }
    
    @Override
    public boolean spawnAsTree(LocalWorld world, Random random, int x, int z, int minY, int maxY)
    {
		return spawn(world, random, x, z, minY, maxY);
    }

    // Used for customobject and trees
    private boolean spawn(LocalWorld world, Random random, int x, int z, int minY, int maxY)
    {
        Rotation rotation = settings.rotateRandomly ? Rotation.getRandomRotation(random) : Rotation.NORTH;
        int y = 0;
        if (settings.spawnHeight == SpawnHeightEnum.randomY)
        {
            y = minY == maxY ? minY : RandomHelper.numberInRange(random, minY, maxY);
        }
        if (settings.spawnHeight == SpawnHeightEnum.highestBlock)
        {
            y = world.getHighestBlockYAt(x, z);
        }
        if (settings.spawnHeight == SpawnHeightEnum.highestSolidBlock)
        {
            y = world.getSolidHeight(x, z);
        }
        // Offset by static and random settings values
        y += this.getOffsetAndVariance(random, settings.spawnHeightOffset, settings.spawnHeightVariance);
        return trySpawnAt(world, random, rotation, x, y, z, minY, maxY);
    }
    
    // Used for saplings, trees, customobjects and customstructures
    // SkipChecks is used for spawning saplings and customobjects without doing checks (for growing saplings, /spawn command, StructureAtSpawn etc). TODO: Split this into 2 methods?
    public boolean trySpawnAt(boolean skipChecks, CustomStructure structure, LocalWorld world, Random random, Rotation rotation, int x, int y, int z, int minY, int maxY)
    {
    	if(!skipChecks)
    	{
	        if (y < PluginStandardValues.WORLD_DEPTH || y >= PluginStandardValues.WORLD_HEIGHT) // Isn't this already done before this method is called?
	        {
	            return false;
	        }

	        // Height check
	        if (y < minY || y > maxY)
	        {
	            return false;
	        }

	        BO3Check[] checks = settings.bo3Checks[rotation.getRotationId()];

	        // Check for spawning
	        for (BO3Check check : checks)
	        {
	            if (check.preventsSpawn(world, x + check.x, y + check.y, z + check.z))
	            {
	                // A check failed
	                return false;
	            }
	        }
    	}

    	// TODO: Optimise this! Should we really have to check for each block if the world is loaded? ><
        BO3BlockFunction[] blocks = settings.getBlocks(rotation.getRotationId());       
        if(!skipChecks)
        {
            HashSet<ChunkCoordinate> loadedChunks = new HashSet<ChunkCoordinate>();
            ChunkCoordinate chunkCoord;
	        for (BO3BlockFunction block : blocks)
	        {
	            if (y + block.y < PluginStandardValues.WORLD_DEPTH || y + block.y >= PluginStandardValues.WORLD_HEIGHT)
	            {
	                return false;
	            }

	           	chunkCoord = ChunkCoordinate.fromBlockCoords(x + block.x, z + block.z);
	        	if(!loadedChunks.contains(chunkCoord))
	    		{
	        		if(!world.isLoaded(x + block.x, y + block.y, z + block.z)) 
		            {
	                    // Cannot spawn BO3, part of world is not loaded TODO: Is this really necessary / should this be necessary? Aren't invalid BO3 customstructures detected when loading branches? Branches shouldn't be able to spawn outside of bounds.
		                return false;
		            }
		            loadedChunks.add(chunkCoord);
	    		}
	        }
        }

        ArrayList<BO3BlockFunction> blocksToSpawn = new ArrayList<BO3BlockFunction>();

        ObjectExtrusionHelper oeh = new ObjectExtrusionHelper(settings.extrudeMode, settings.extrudeThroughBlocks);
        HashSet<ChunkCoordinate> chunks = new HashSet<ChunkCoordinate>();

        int blocksOutsideSourceBlock = 0;
        int maxBlocksOutsideSourceBlock = (int)Math.ceil(blocks.length * (settings.maxPercentageOutsideSourceBlock / 100.0));
        for (BO3BlockFunction block : blocks)
        {
            if (
        		!skipChecks &&
	    		(
					(
						settings.maxPercentageOutsideSourceBlock < 100 &&
						blocksOutsideSourceBlock <= maxBlocksOutsideSourceBlock
					) ||
					settings.outsideSourceBlock == OutsideSourceBlock.dontPlace
	            ) &&
	    		!settings.sourceBlocks.contains(world.getMaterial(x + block.x, y + block.y, z + block.z, false))
        	)
            {
                blocksOutsideSourceBlock++;
                if (blocksOutsideSourceBlock > maxBlocksOutsideSourceBlock)
                {
                    // Too many blocks outside source block
                    return false;
                }

                if(settings.outsideSourceBlock == OutsideSourceBlock.placeAnyway)
                {
            		chunks.add(ChunkCoordinate.fromBlockCoords(x + block.x, z + block.z));
                	blocksToSpawn.add(block);
                }
            } else {
        		chunks.add(ChunkCoordinate.fromBlockCoords(x + block.x, z + block.z));
            	blocksToSpawn.add(block);
            }
            if (block instanceof BO3BlockFunction)
            {
                oeh.addBlock((BO3BlockFunction) block);
            }
        }

        // Call event
        if (!skipChecks && !OTG.fireCanCustomObjectSpawnEvent(this, world, x, y, z))
        {
            // Cancelled
            return false;
        }

        // Spawn

        HashSet<ChunkCoordinate> chunksCustomObject = new HashSet<ChunkCoordinate>();

        for (BO3BlockFunction block : blocksToSpawn)
        {
        	block.spawn(world, random, x + block.x, y + block.y, z + block.z, true);
        }

        oeh.extrude(world, random, x, y, z);

        HashSet<BO3ModDataFunction> newModDataInObject = new HashSet<BO3ModDataFunction>();
    	BO3ModDataFunction[] modDataInObject = settings.modDataFunctions[rotation.getRotationId()];
		for (BO3ModDataFunction modData : modDataInObject)
    	{
    		BO3ModDataFunction newModData = new BO3ModDataFunction();

    		newModData.y = y + modData.y;
    		newModData.x = x + modData.x;
    		newModData.z = z + modData.z;

        	newModData.modData = modData.modData;
        	newModData.modId = modData.modId;

        	newModDataInObject.add(newModData);
        	chunks.add(ChunkCoordinate.fromBlockCoords(newModData.x, newModData.z));
        	chunksCustomObject.add(ChunkCoordinate.fromBlockCoords(newModData.x, newModData.z));
    	}

		HashSet<BO3SpawnerFunction> newSpawnerDataInObject = new HashSet<BO3SpawnerFunction>();
    	BO3SpawnerFunction[] spawnerDataInObject = settings.spawnerFunctions[rotation.getRotationId()];
		for (BO3SpawnerFunction spawnerData : spawnerDataInObject)
    	{
    		BO3SpawnerFunction newSpawnerData = new BO3SpawnerFunction();

    		newSpawnerData.y = y + spawnerData.y;
        	newSpawnerData.x = x + spawnerData.x;
        	newSpawnerData.z = z + spawnerData.z;

        	newSpawnerData.mobName = spawnerData.mobName;
        	newSpawnerData.originalnbtFileName = spawnerData.originalnbtFileName;
        	newSpawnerData.nbtFileName = spawnerData.nbtFileName;
        	newSpawnerData.groupSize = spawnerData.groupSize;
        	newSpawnerData.interval = spawnerData.interval;
        	newSpawnerData.spawnChance = spawnerData.spawnChance;
        	newSpawnerData.maxCount= spawnerData.maxCount;

        	newSpawnerData.despawnTime = spawnerData.despawnTime;

        	newSpawnerData.velocityX = spawnerData.velocityX;
        	newSpawnerData.velocityY = spawnerData.velocityY;
        	newSpawnerData.velocityZ = spawnerData.velocityZ;

        	newSpawnerData.velocityXSet = spawnerData.velocityXSet;
        	newSpawnerData.velocityYSet = spawnerData.velocityYSet;
        	newSpawnerData.velocityZSet = spawnerData.velocityZSet;

        	newSpawnerData.yaw = spawnerData.yaw;
        	newSpawnerData.pitch = spawnerData.pitch;

        	newSpawnerDataInObject.add(newSpawnerData);
        	chunks.add(ChunkCoordinate.fromBlockCoords(newSpawnerData.x, newSpawnerData.z));
        	chunksCustomObject.add(ChunkCoordinate.fromBlockCoords(newSpawnerData.x, newSpawnerData.z));
    	}

		HashSet<BO3ParticleFunction> newParticleDataInObject = new HashSet<BO3ParticleFunction>();
    	BO3ParticleFunction[] particleDataInObject = settings.particleFunctions[rotation.getRotationId()];
		for (BO3ParticleFunction particleData : particleDataInObject)
    	{
    		BO3ParticleFunction newParticleData = new BO3ParticleFunction();

    		newParticleData.y = y + particleData.y;
    		newParticleData.x = x + particleData.x;
    		newParticleData.z = z + particleData.z;

    		newParticleData.particleName = particleData.particleName;

    		newParticleData.interval = particleData.interval;

    		newParticleData.velocityX = particleData.velocityX;
    		newParticleData.velocityY = particleData.velocityY;
        	newParticleData.velocityZ = particleData.velocityZ;

        	newParticleData.velocityXSet = particleData.velocityXSet;
        	newParticleData.velocityYSet = particleData.velocityYSet;
        	newParticleData.velocityZSet = particleData.velocityZSet;

        	newParticleDataInObject.add(newParticleData);
        	chunks.add(ChunkCoordinate.fromBlockCoords(newParticleData.x, newParticleData.z));
        	chunksCustomObject.add(ChunkCoordinate.fromBlockCoords(newParticleData.x, newParticleData.z));
    	}

		if(structure != null)
		{
			structure.modDataManager.modData.addAll(newModDataInObject);
			structure.particlesManager.particleData.addAll(newParticleDataInObject);
			structure.spawnerManager.spawnerData.addAll(newSpawnerDataInObject);

   			for(ChunkCoordinate structureCoord : chunks)
    		{
				if(world.getStructureCache().worldInfoChunks.containsKey(structureCoord))
				{
					CustomStructure existingObject = world.getStructureCache().worldInfoChunks.get(structureCoord);
					structure.modDataManager.modData.addAll(existingObject.modDataManager.modData);
					structure.particlesManager.particleData.addAll(existingObject.particlesManager.particleData);
					structure.spawnerManager.spawnerData.addAll(existingObject.spawnerManager.spawnerData);
				}
				world.getStructureCache().worldInfoChunks.put(structureCoord, structure);
    		}
		} else {

			CustomStructure placeHolderStructure = new BO3CustomStructure(new BO3CustomStructureCoordinate(world, this, this.getName(), Rotation.NORTH, x, (short)0, z));
			placeHolderStructure.modDataManager.modData.addAll(newModDataInObject);
			placeHolderStructure.particlesManager.particleData.addAll(newParticleDataInObject);
			placeHolderStructure.spawnerManager.spawnerData.addAll(newSpawnerDataInObject);

   			for(ChunkCoordinate structureCoord : chunksCustomObject)
    		{
				if(world.getStructureCache().worldInfoChunks.containsKey(structureCoord))
				{
					CustomStructure existingObject = world.getStructureCache().worldInfoChunks.get(structureCoord);
					existingObject.modDataManager.modData.addAll(placeHolderStructure.modDataManager.modData);
					existingObject.particlesManager.particleData.addAll(placeHolderStructure.particlesManager.particleData);
					existingObject.spawnerManager.spawnerData.addAll(placeHolderStructure.spawnerManager.spawnerData);
				} else {
					world.getStructureCache().worldInfoChunks.put(structureCoord, placeHolderStructure);
				}
    		}
		}

    	BO3EntityFunction[] entityDataInObject = settings.entityFunctions[rotation.getRotationId()];
    	for (BO3EntityFunction entity : entityDataInObject)
    	{
    		BO3EntityFunction newEntityData = new BO3EntityFunction();

        	newEntityData.y = y + entity.y;
        	newEntityData.x = x + entity.x;
        	newEntityData.z = z + entity.z;

        	newEntityData.mobName = entity.mobName;
        	newEntityData.groupSize = entity.groupSize;
        	newEntityData.nameTagOrNBTFileName = entity.nameTagOrNBTFileName;
        	newEntityData.originalNameTagOrNBTFileName = entity.originalNameTagOrNBTFileName;

        	world.spawnEntity(newEntityData);
    	}

        return true;
    }
}
