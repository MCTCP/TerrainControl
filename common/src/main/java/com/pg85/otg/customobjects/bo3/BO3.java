package com.pg85.otg.customobjects.bo3;

import com.pg85.otg.LocalBiome;
import com.pg85.otg.LocalMaterialData;
import com.pg85.otg.LocalWorld;
import com.pg85.otg.OTG;
import com.pg85.otg.configuration.BiomeConfig;
import com.pg85.otg.configuration.WorldConfig.ConfigMode;
import com.pg85.otg.configuration.io.FileSettingsReaderOTGPlus;
import com.pg85.otg.configuration.io.FileSettingsWriterOTGPlus;
import com.pg85.otg.configuration.io.SettingsReaderOTGPlus;
import com.pg85.otg.customobjects.*;
import com.pg85.otg.customobjects.bo3.BO3Settings.OutsideSourceBlock;
import com.pg85.otg.customobjects.bo3.BO3Settings.SpawnHeightEnum;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.generator.surface.MesaSurfaceGenerator;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.BoundingBox;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.NamedBinaryTag;
import com.pg85.otg.util.Rotation;
import com.pg85.otg.util.helpers.MathHelper;
import com.pg85.otg.util.helpers.RandomHelper;
import com.pg85.otg.util.minecraftTypes.DefaultMaterial;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

public class BO3 implements StructuredCustomObject
{
	// OTG+

	public boolean isInvalidConfig;

    boolean measured = false;
    int minX = Integer.MAX_VALUE;
    int maxX = Integer.MIN_VALUE;
    int minZ = Integer.MAX_VALUE;
    int maxZ = Integer.MIN_VALUE;
    boolean isCollidable = false;

    public boolean isCollidable()
    {
    	measure();
    	return isCollidable;
    }

    public boolean is32x32 = false;

    /**
     * Creates a BO3 with the specified settings. Ignores the settings in the
     * settings file.
     *
     * @param oldObject     The object where this object is based on
     * @param extraSettings The settings to override
     */
    public BO3(BO3 oldObject, SettingsReaderOTGPlus extraSettings)
    {
        try
        {
			this.settings = new BO3Config(extraSettings, null);
			//this.settings.inheritBO3 = oldObject.getName();
		}
        catch (InvalidConfigException e)
        {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        //this.settings.file = oldObject.file;

        //FileSettingsWriter.writeToFile(this.settings, this.settings.settingsMode);

		this.settings.inheritBO3 = oldObject.getName();

        this.name = settings.getName();
        this.file = settings.getFile();
    }

    // Used for Tree() and CustomObject
    @Override
    public boolean trySpawnAt(LocalWorld world, Random random, Rotation rotation, int x, int y, int z)
    {
    	return trySpawnAt(false, null, world, random, rotation, x, y, z);
    }

	// This should only be used for CustomObject and OTG CustomStructure
    public boolean trySpawnAt(boolean skipChecks, CustomObjectStructure structure, LocalWorld world, Random random, Rotation rotation, int x, int y, int z)
    {
    	if(!skipChecks)
    	{
	        if (y < OTG.WORLD_DEPTH || y >= OTG.WORLD_HEIGHT) // Isn't this already done before this method is called?
	        {
	            return false;
	        }

	        // Height check
	        if (y < settings.minHeight || y > settings.maxHeight)
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

        BlockFunction[] blocks = settings.blocks[rotation.getRotationId()];

        if(!skipChecks)
        {
            HashSet<ChunkCoordinate> loadedChunks = new HashSet<ChunkCoordinate>();
            ChunkCoordinate chunkCoord;
	        for (BlockFunction block : blocks)
	        {
	            if (y + block.y < OTG.WORLD_DEPTH || y + block.y >= OTG.WORLD_HEIGHT)
	            {
	                return false;
	            }

	           	chunkCoord = ChunkCoordinate.fromBlockCoords(x + block.x, z + block.z);
	        	if(!loadedChunks.contains(chunkCoord))
	    		{
	        		if(!world.isLoaded(x + block.x, y + block.y, z + block.z))
		            {
	                    // Cannot spawn BO3, part of world is not loaded
		                return false;
		            }
		            loadedChunks.add(chunkCoord);
	    		}
	        }
        }

        ArrayList<BlockFunction> blocksToSpawn = new ArrayList<BlockFunction>();

        ObjectExtrusionHelper oeh = new ObjectExtrusionHelper(settings.extrudeMode, settings.extrudeThroughBlocks);
        HashSet<ChunkCoordinate> chunks = new HashSet<ChunkCoordinate>();

        int blocksOutsideSourceBlock = 0;
        int maxBlocksOutsideSourceBlock = (int)Math.ceil(blocks.length * (settings.maxPercentageOutsideSourceBlock / 100.0));
        for (BlockFunction block : blocks)
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
	    		!settings.sourceBlocks.contains(world.getMaterial(x + block.x, y + block.y, z + block.z, settings.isOTGPlus))
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
            if (block instanceof BlockFunction)
            {
                oeh.addBlock((BlockFunction) block);
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

        for (BlockFunction block : blocksToSpawn)
        {
        	block.spawn(world, random, x + block.x, y + block.y, z + block.z, false);
        }

        oeh.extrude(world, random, x, y, z);

        HashSet<ModDataFunction> newModDataInObject = new HashSet<ModDataFunction>();
    	ModDataFunction[] modDataInObject = settings.modDataFunctions[rotation.getRotationId()];
		for (ModDataFunction modData : modDataInObject)
    	{
    		ModDataFunction newModData = new ModDataFunction();

    		newModData.y = y + modData.y;
    		newModData.x = x + modData.x;
    		newModData.z = z + modData.z;

        	newModData.modData = modData.modData;
        	newModData.modId = modData.modId;

        	newModDataInObject.add(newModData);
        	chunks.add(ChunkCoordinate.fromBlockCoords(newModData.x, newModData.z));
        	chunksCustomObject.add(ChunkCoordinate.fromBlockCoords(newModData.x, newModData.z));
    	}

		HashSet<SpawnerFunction> newSpawnerDataInObject = new HashSet<SpawnerFunction>();
    	SpawnerFunction[] spawnerDataInObject = settings.spawnerFunctions[rotation.getRotationId()];
		for (SpawnerFunction spawnerData : spawnerDataInObject)
    	{
    		SpawnerFunction newSpawnerData = new SpawnerFunction();

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

		HashSet<ParticleFunction> newParticleDataInObject = new HashSet<ParticleFunction>();
    	ParticleFunction[] particleDataInObject = settings.particleFunctions[rotation.getRotationId()];
		for (ParticleFunction particleData : particleDataInObject)
    	{
    		ParticleFunction newParticleData = new ParticleFunction();

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
			structure.modData.addAll(newModDataInObject);
			structure.particleData.addAll(newParticleDataInObject);
			structure.spawnerData.addAll(newSpawnerDataInObject);

   			for(ChunkCoordinate structureCoord : chunks)
    		{
				if(world.getStructureCache().worldInfoChunks.containsKey(structureCoord))
				{
					CustomObjectStructure existingObject = world.getStructureCache().worldInfoChunks.get(structureCoord);
					structure.modData.addAll(existingObject.modData);
					structure.particleData.addAll(existingObject.particleData);
					structure.spawnerData.addAll(existingObject.spawnerData);
				}
				world.getStructureCache().worldInfoChunks.put(structureCoord, structure);
    		}
		} else {

			CustomObjectStructure placeHolderStructure = new CustomObjectStructure(new CustomObjectCoordinate(world, this, this.getName(), Rotation.NORTH, x, 0, z));
			placeHolderStructure.modData.addAll(newModDataInObject);
			placeHolderStructure.particleData.addAll(newParticleDataInObject);
			placeHolderStructure.spawnerData.addAll(newSpawnerDataInObject);

   			for(ChunkCoordinate structureCoord : chunksCustomObject)
    		{
				if(world.getStructureCache().worldInfoChunks.containsKey(structureCoord))
				{
					CustomObjectStructure existingObject = world.getStructureCache().worldInfoChunks.get(structureCoord);
					existingObject.modData.addAll(placeHolderStructure.modData);
					existingObject.particleData.addAll(placeHolderStructure.particleData);
					existingObject.spawnerData.addAll(placeHolderStructure.spawnerData);
				} else {
					world.getStructureCache().worldInfoChunks.put(structureCoord, placeHolderStructure);
				}
    		}
		}

    	EntityFunction[] entityDataInObject = settings.entityFunctions[rotation.getRotationId()];
    	for (EntityFunction entity : entityDataInObject)
    	{
    		EntityFunction newEntityData = new EntityFunction();

        	newEntityData.y = y + entity.y;
        	newEntityData.x = x + entity.x;
        	newEntityData.z = z + entity.z;

        	newEntityData.mobName = entity.mobName;
        	newEntityData.groupSize = entity.groupSize;
        	newEntityData.nameTagOrNBTFileName = entity.nameTagOrNBTFileName;
        	newEntityData.originalNameTagOrNBTFileName = entity.originalNameTagOrNBTFileName;

        	world.SpawnEntity(newEntityData);
    	}

        return true;
    }

    // Used by OTG+ CustomStructure
    public boolean trySpawnAt(LocalWorld world, Random random, Rotation rotation, ChunkCoordinate chunkCoord, int x, int y, int z, String replaceAbove, String replaceBelow, boolean replaceWithBiomeBlocks, String replaceWithSurfaceBlock, String replaceWithGroundBlock, boolean spawnUnderWater, int waterLevel, boolean isStructureAtSpawn, boolean doReplaceAboveBelowOnly)
    {
    	// This should only be used for OTG+ CustomStructure()
    	if(settings.isOTGPlus)
    	{
			if(!measure())
			{
				return false;
			}

	    	return spawnForcedOTGPlus(world, random, rotation, chunkCoord, x, y, z, replaceAbove, replaceBelow, replaceWithBiomeBlocks, replaceWithSurfaceBlock, replaceWithGroundBlock, spawnUnderWater, waterLevel, isStructureAtSpawn, doReplaceAboveBelowOnly);
    	} else {
    		throw new RuntimeException();
    	}
    }

    private boolean measure()
    {
    	if(!measured)
    	{
	        for (BlockFunction block : settings.getBlocks())
	        {
	        	isCollidable = true;

	        	if(block.x < minX)
	        	{
	        		minX = block.x;
	        	}
	        	if(block.x > maxX)
	        	{
	        		maxX = block.x;
	        	}
	        	if(block.z < minZ)
	        	{
	        		minZ = block.z;
	        	}
	        	if(block.z > maxZ)
	        	{
	        		maxZ = block.z;
	        	}
	        }

	        if(minX == Integer.MAX_VALUE)
	        {
	        	minX = -8;
	        }
	        if(maxX == Integer.MIN_VALUE)
	        {
	        	maxX = -8;
	        }
	        if(minZ == Integer.MAX_VALUE)
	        {
	        	minZ = -7;
	        }
	        if(maxZ == Integer.MIN_VALUE)
	        {
	        	maxZ = -7;
	        }

	        measured = true;

            if(Math.abs(minX - maxX) > 31 || Math.abs(minZ - maxZ) > 31)
            {
            	OTG.log(LogMarker.INFO, "BO3 was too large to spawn (> 32x32) " + this.getName() + " XSize " + (Math.abs(minX - maxX) + 1) + " ZSize " + (Math.abs(minZ - maxZ) + 1) + ". Convert it to a branching BO3 and add it using CustomStructure()");
            	return false;
            }
            if(Math.abs(minX - maxX) > 15 || Math.abs(minZ - maxZ) > 15)
            {
            	is32x32 = true;
            }
    	}
    	return true;
    }

    public static HashMap<ChunkCoordinate, LocalMaterialData> originalTopBlocks = new HashMap<ChunkCoordinate, LocalMaterialData>();
    public boolean spawnForcedOTGPlus(LocalWorld world, Random random, Rotation rotation, ChunkCoordinate chunkCoord, int x, int y, int z, String replaceAbove, String replaceBelow, boolean replaceWithBiomeBlocks, String replaceWithSurfaceBlock, String replaceWithGroundBlock, boolean spawnUnderWater, int waterLevel, boolean isStructureAtSpawn, boolean doReplaceAboveBelowOnly)
    {
    	//OTG.log(LogMarker.INFO, "Spawning " + this.getName());

    	if(!measured) { throw new RuntimeException(); }

    	LocalMaterialData replaceBelowMaterial = null;
    	LocalMaterialData replaceAboveMaterial = null;

    	LocalMaterialData bo3SurfaceBlock = null;
    	LocalMaterialData bo3GroundBlock = null;
    	LocalMaterialData airMaterial = null;

    	airMaterial = OTG.toLocalMaterialData(DefaultMaterial.AIR, 0);

		if(settings == null)
		{
			OTG.log(LogMarker.INFO, "Settings was null for BO3 " + this.getName() + ". This should not be happening, please contact the developer.");
			throw new RuntimeException();
		}

    	try {
    		bo3SurfaceBlock = replaceWithSurfaceBlock != null && replaceWithSurfaceBlock.length() > 0 ? OTG.readMaterial(replaceWithSurfaceBlock) : OTG.readMaterial("GRASS");
		} catch (InvalidConfigException e1) {
			bo3SurfaceBlock = OTG.toLocalMaterialData(DefaultMaterial.GRASS, 0);
			OTG.log(LogMarker.INFO, "Value " + replaceWithSurfaceBlock + " for replaceWithSurfaceBlock in BO3 " + this.getName() + " was not recognised. Using GRASS instead.");
		}
    	try {
    		bo3GroundBlock = replaceWithGroundBlock != null && replaceWithGroundBlock.length() > 0 ? OTG.readMaterial(replaceWithGroundBlock) : OTG.readMaterial("DIRT");
		} catch (InvalidConfigException e1) {
			bo3GroundBlock = OTG.toLocalMaterialData(DefaultMaterial.DIRT, 0);
			OTG.log(LogMarker.INFO, "Value " + replaceWithGroundBlock + " for replaceWithGroundBlock in BO3 " + this.getName() + " was not recognised. Using DIRT instead.");
		}

    	try {
			replaceBelowMaterial = settings.replaceBelow != null && settings.replaceBelow.toLowerCase().equals("none") ? null : replaceBelow != null && replaceBelow.length() > 0 ? OTG.readMaterial(replaceBelow) : null;
		} catch (InvalidConfigException e1) {
			replaceBelowMaterial = OTG.toLocalMaterialData(DefaultMaterial.DIRT, 0);
			OTG.log(LogMarker.INFO, "Value " + settings.replaceBelow + " for replaceBelow in BO3 " + this.getName() + " was not recognised. Using DIRT instead.");
		}
    	try {
			replaceAboveMaterial = settings.replaceAbove != null && settings.replaceAbove.toLowerCase().equals("none") ? null : replaceAbove != null && replaceAbove.length() > 0 ? OTG.readMaterial(replaceAbove) : null;
		} catch (InvalidConfigException e1) {
			replaceAboveMaterial = OTG.toLocalMaterialData(DefaultMaterial.AIR, 0);
			OTG.log(LogMarker.INFO, "Value " + settings.replaceAbove + " for replaceAbove in BO3 " + this.getName() + " was not recognised. Using AIR instead.");
		}

    	boolean isOnBiomeBorder = false;

    	LocalBiome biome = null;
    	LocalBiome biome2 = null;
    	LocalBiome biome3 = null;
    	LocalBiome biome4 = null;

    	BiomeConfig biomeConfig = null;

        LocalMaterialData biomeSurfaceBlock = null;
        LocalMaterialData biomeGroundBlock = null;

    	if(replaceWithBiomeBlocks)
    	{
	    	biome = world.getBiome(x, z);
	    	biome2 = world.getBiome(x + 15, z);
	    	biome3 = world.getBiome(x, z + 15);
	    	biome4 = world.getBiome(x + 15, z + 15);

	        if(!(biome == biome2 && biome == biome3 && biome == biome4))
	        {
	        	isOnBiomeBorder = true;
	        }

	        biomeConfig = biome.getBiomeConfig();

	        biomeSurfaceBlock = biomeConfig.surfaceBlock;
	        if(biomeSurfaceBlock == null)
	        {
				biomeSurfaceBlock = OTG.toLocalMaterialData(DefaultMaterial.GRASS,0);
	        }

	        biomeGroundBlock = biomeConfig.groundBlock;
	        if(biomeGroundBlock == null)
	        {
				biomeGroundBlock = OTG.toLocalMaterialData(DefaultMaterial.DIRT,0);
	        }

	        if(biomeSurfaceBlock.toDefaultMaterial().equals(DefaultMaterial.SNOW))
	        {
	        	biomeSurfaceBlock = OTG.toLocalMaterialData(DefaultMaterial.SNOW_BLOCK,0);
	        }
	        if(biomeGroundBlock.toDefaultMaterial().equals(DefaultMaterial.SNOW))
	        {
	        	biomeGroundBlock = OTG.toLocalMaterialData(DefaultMaterial.SNOW_BLOCK,0);
	        }
    	}

    	// Get the right coordinates based on rotation

    	ArrayList<Object[]> coordsAboveDone = new ArrayList<Object[]>();
    	ArrayList<Object[]> coordsBelowDone = new ArrayList<Object[]>();

    	BlockFunction blockToQueueForSpawn = new BlockFunction();
    	LocalMaterialData sourceBlockMaterial;

    	boolean outOfBounds = false;

        // Spawn
    	long startTime = System.currentTimeMillis();
        for (BlockFunction block : settings.getBlocks())
        {
        	if(block instanceof RandomBlockFunction)
        	{
                for (int i = 0; i < ((RandomBlockFunction)block).blockCount; i++)
                {
                    if (random.nextInt(100) < ((RandomBlockFunction)block).blockChances[i])
                    {
                    	block.metaDataName = ((RandomBlockFunction)block).metaDataNames[i];
                    	block.metaDataTag = ((RandomBlockFunction)block).metaDataTags[i];
                    	block.material = ((RandomBlockFunction)block).blocks[i];
                    	break;
                    }
                }
        	}

            if(block.material == null || block.material.toDefaultMaterial() == DefaultMaterial.UNKNOWN_BLOCK)
            {
            	continue;
            }

        	if(rotation != Rotation.NORTH)
        	{
	        	BlockFunction newBlock = new BlockFunction();
            	int rotations = 0;
            	// How many counter-clockwise rotations have to be applied?
        		if(rotation == Rotation.WEST)
        		{
        			rotations = 1;
        		}
        		else if(rotation == Rotation.SOUTH)
        		{
        			rotations = 2;
        		}
        		else if(rotation == Rotation.EAST)
        		{
        			rotations = 3;
        		}

                // Apply rotation
            	if(rotations == 0)
            	{
            		newBlock.x = block.x;
            		newBlock.z = block.z;
            	}
            	if(rotations == 1)
            	{
            		newBlock.x = block.z;
            		newBlock.z = -block.x + (is32x32 ? 31 : 15);
            		newBlock.material = block.material.rotate();
            	}
            	if(rotations == 2)
            	{
            		newBlock.x = -block.x + (is32x32 ? 31 : 15);
            		newBlock.z = -block.z + (is32x32 ? 31 : 15);
            		newBlock.material = block.material.rotate();
            		newBlock.material = newBlock.material.rotate();
            	}
            	if(rotations == 3)
            	{
            		newBlock.x = -block.z + (is32x32 ? 31 : 15);
            		newBlock.z = block.x;
            		newBlock.material = block.material.rotate();
            		newBlock.material = newBlock.material.rotate();
            		newBlock.material = newBlock.material.rotate();
            	}
            	newBlock.y = block.y;

	        	newBlock.metaDataName = block.metaDataName;
	        	newBlock.metaDataTag = block.metaDataTag;

	        	if(!originalTopBlocks.containsKey(ChunkCoordinate.fromChunkCoords(x + newBlock.x, z + newBlock.z)))
	        	{
	        		int highestBlockY = world.getHighestBlockYAt(x + newBlock.x, z + newBlock.z, true, true, false, false);
	        		if(highestBlockY <= OTG.WORLD_DEPTH)
	        		{
	        			highestBlockY = 1;
	        		}
	        		if(highestBlockY >= OTG.WORLD_HEIGHT)
	        		{
	        			highestBlockY = 255;
	        		}
	        		originalTopBlocks.put(ChunkCoordinate.fromChunkCoords(x + newBlock.x, z + newBlock.z), world.getMaterial(x + newBlock.x, highestBlockY, z + newBlock.z, settings.isOTGPlus));
	        	}

	        	// TODO: See BlockFunction.Spawn for what should be done with metadata

	        	if(replaceAboveMaterial != null && doReplaceAboveBelowOnly)
	        	{
	        		boolean bFound = false;
        			for(Object[] coords : coordsAboveDone)
        			{
        				if((Integer)coords[0] == x + newBlock.x && (Integer)coords[1] == z + newBlock.z)
        				{
        					bFound = true;
        					break;
        				}
        			}

	        		if(!bFound)
	        		{
	        			coordsAboveDone.add(new Object[] { x + newBlock.x, z + newBlock.z });
	        			int blockY = y + newBlock.y + 1; // TODO: This is wrong, should be the lowest block in the BO3 at these x-z coordinates. ReplaceAbove should be done before any blocks in this column are placed
        				int highestBlockToReplace = world.getHighestBlockYAt(x + newBlock.x, z + newBlock.z, true, true, false, false);

	        			// TODO: Use world height constant (dunno what its called and where its at)??
	        			while(blockY <= highestBlockToReplace && blockY > y + newBlock.y)
	        			{
                            blockToQueueForSpawn = new BlockFunction();

	        				if(spawnUnderWater && blockY >= waterLevel)// && replaceAboveMaterial.isLiquid())
			            	{
	        					blockToQueueForSpawn.material = airMaterial;
			            	} else {
			            		blockToQueueForSpawn.material = replaceAboveMaterial;
			            	}

	        				// TODO: Make override leaves and air configurable
	        				// TODO: Make replaceAbove height configurable
                            blockToQueueForSpawn.x = x + newBlock.x;
                            blockToQueueForSpawn.y = blockY;
                            blockToQueueForSpawn.z = z + newBlock.z;

    						blockToQueueForSpawn.metaDataName = block.metaDataName;
    						blockToQueueForSpawn.metaDataTag = block.metaDataTag;

    						sourceBlockMaterial = world.getMaterial(blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z, settings.isOTGPlus);

                			if(!sourceBlockMaterial.toDefaultMaterial().equals(blockToQueueForSpawn.material.toDefaultMaterial()) || sourceBlockMaterial.getBlockData() != blockToQueueForSpawn.material.getBlockData())
                			{
            					ChunkCoordinate destChunk = ChunkCoordinate.fromBlockCoords(blockToQueueForSpawn.x, blockToQueueForSpawn.z);
            					if(chunkCoord.equals(destChunk))
            					{
        							setBlock(world, blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z, blockToQueueForSpawn.material, blockToQueueForSpawn.metaDataTag, isStructureAtSpawn);
            					} else {
            						outOfBounds = true;
            					}
                			}

	        				blockY += 1;
	        			}
	        		}
	        	}
	        	if(replaceBelowMaterial != null && block.y == 0 && !block.material.isAir() && doReplaceAboveBelowOnly)
	        	{
	        		boolean bFound = false;
        			for(Object[] coords : coordsBelowDone)
        			{
        				if((Integer)coords[0] == x + newBlock.x && (Integer)coords[1] == z + newBlock.z)
        				{
        					bFound = true;
        					break;
        				}
        			}

	        		if(!bFound)
	        		{
	        			coordsBelowDone.add(new Object[] { x + newBlock.x, z + newBlock.z });
	        			int blockY = y + newBlock.y - 1;

        				// TODO: Make override leaves and air configurable
        				// TODO: Make replaceBelow height configurable
	        			while(blockY > OTG.WORLD_DEPTH)
	        			{
	        				if(blockY < OTG.WORLD_HEIGHT)
	        				{
	                            blockToQueueForSpawn = new BlockFunction();
	                            blockToQueueForSpawn.x = x + newBlock.x;
	                            blockToQueueForSpawn.y = blockY;
	                            blockToQueueForSpawn.z = z + newBlock.z;
	                            blockToQueueForSpawn.material = replaceBelowMaterial;

	    						blockToQueueForSpawn.metaDataName = block.metaDataName;
	    						blockToQueueForSpawn.metaDataTag = block.metaDataTag;

	    						sourceBlockMaterial = world.getMaterial(blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z, settings.isOTGPlus);

	                			if(!sourceBlockMaterial.isSolid() && !sourceBlockMaterial.toDefaultMaterial().equals(blockToQueueForSpawn.material.toDefaultMaterial()) || sourceBlockMaterial.getBlockData() != blockToQueueForSpawn.material.getBlockData())
	                			{
                					ChunkCoordinate destChunk = ChunkCoordinate.fromBlockCoords(blockToQueueForSpawn.x, blockToQueueForSpawn.z);
                					if(chunkCoord.equals(destChunk))
                					{
            							setBlock(world, blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z, blockToQueueForSpawn.material, blockToQueueForSpawn.metaDataTag, isStructureAtSpawn);
                					} else {
                						outOfBounds = true;
                					}
	                			}
	                			else if(sourceBlockMaterial.isSolid())
	                			{
	                				break;
	                			}
	        				}

	        				blockY -= 1;
	        			}
        			}
	        	}
	        	if(y + newBlock.y > 0 && y + newBlock.y < 256 && !doReplaceAboveBelowOnly)
	        	{
                    blockToQueueForSpawn = new BlockFunction();
                    blockToQueueForSpawn.x = x + newBlock.x;
                    blockToQueueForSpawn.y = y + newBlock.y;
                    blockToQueueForSpawn.z = z + newBlock.z;
					blockToQueueForSpawn.material = newBlock.material;

					blockToQueueForSpawn.metaDataName = block.metaDataName;
					blockToQueueForSpawn.metaDataTag = block.metaDataTag;

					sourceBlockMaterial = world.getMaterial(blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z, settings.isOTGPlus);

    				if(replaceWithBiomeBlocks)
    				{
                        if(isOnBiomeBorder)
                        {
        	                biome = world.getBiome(blockToQueueForSpawn.x, blockToQueueForSpawn.z);
        	                biomeConfig = biome.getBiomeConfig();

        	    	        biomeSurfaceBlock = biomeConfig.surfaceBlock;
        	    	        if(biomeSurfaceBlock == null)
        	    	        {
        	    				biomeSurfaceBlock = OTG.toLocalMaterialData(DefaultMaterial.GRASS,0);
        	    	        }

        	    	        biomeGroundBlock = biomeConfig.groundBlock;
        	    	        if(biomeGroundBlock == null)
        	    	        {
        	    				biomeGroundBlock = OTG.toLocalMaterialData(DefaultMaterial.DIRT,0);
        	    	        }

    		    	        if(biomeSurfaceBlock.toDefaultMaterial().equals(DefaultMaterial.SNOW))
    		    	        {
    		    	        	biomeSurfaceBlock = OTG.toLocalMaterialData(DefaultMaterial.SNOW_BLOCK,0);
    		    	        }
    		    	        if(biomeGroundBlock.toDefaultMaterial().equals(DefaultMaterial.SNOW))
    		    	        {
    		    	        	biomeGroundBlock = OTG.toLocalMaterialData(DefaultMaterial.SNOW_BLOCK,0);
    		    	        }
                        }

    					if(blockToQueueForSpawn.material.toDefaultMaterial().equals(bo3GroundBlock.toDefaultMaterial()) && blockToQueueForSpawn.material.getBlockData() == bo3GroundBlock.getBlockData())
    					{
    						blockToQueueForSpawn.material = biomeGroundBlock;
    					}
    					else if(blockToQueueForSpawn.material.toDefaultMaterial().equals(bo3SurfaceBlock.toDefaultMaterial()) && blockToQueueForSpawn.material.getBlockData() == bo3SurfaceBlock.getBlockData())
    					{
    						blockToQueueForSpawn.material = biomeSurfaceBlock;

    	        			LocalMaterialData originalSurfaceBlock = originalTopBlocks.get(ChunkCoordinate.fromChunkCoords(blockToQueueForSpawn.x, blockToQueueForSpawn.z));
    	        			if(originalSurfaceBlock != null && originalSurfaceBlock.toDefaultMaterial() != DefaultMaterial.UNKNOWN_BLOCK && !originalSurfaceBlock.isLiquid() && !originalSurfaceBlock.isAir())
    	        			{
    	        				blockToQueueForSpawn.material = originalSurfaceBlock;
    	        			}
    					}

                        if(
                    		biomeConfig.surfaceAndGroundControl != null &&
                    		biomeConfig.surfaceAndGroundControl instanceof MesaSurfaceGenerator
                		)
                        {
                        	if(
                    			(
            						blockToQueueForSpawn.material.toDefaultMaterial().equals(biomeGroundBlock.toDefaultMaterial()) &&
            						blockToQueueForSpawn.material.getBlockData() == biomeGroundBlock.getBlockData()
        						)
        						||
        						(
    								blockToQueueForSpawn.material.toDefaultMaterial().equals(biomeSurfaceBlock.toDefaultMaterial()) &&
    								blockToQueueForSpawn.material.getBlockData() == biomeSurfaceBlock.getBlockData()
								)
            				)
                        	{
            		        	LocalMaterialData customBlockData = ((MesaSurfaceGenerator)biomeConfig.surfaceAndGroundControl).getCustomBlockData(world, biomeConfig, blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z);
            		        	if(customBlockData != null)
            		        	{
            		        		blockToQueueForSpawn.material = customBlockData;
            		        	}
                        	}
                        }
    				}

    				if(spawnUnderWater && blockToQueueForSpawn.material.toDefaultMaterial().equals(DefaultMaterial.TORCH) && sourceBlockMaterial.isLiquid())
    				{
    					continue;
    				}

    				if(!sourceBlockMaterial.toDefaultMaterial().equals(blockToQueueForSpawn.material.toDefaultMaterial()) || sourceBlockMaterial.getBlockData() != blockToQueueForSpawn.material.getBlockData())
    				{
    					ChunkCoordinate destChunk = ChunkCoordinate.fromBlockCoords(blockToQueueForSpawn.x, blockToQueueForSpawn.z);
    					if(chunkCoord.equals(destChunk))
    					{
							setBlock(world, blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z, blockToQueueForSpawn.material, blockToQueueForSpawn.metaDataTag, isStructureAtSpawn);
    					} else {
    						outOfBounds = true;
    					}
    				}
	        	}
        	} else {
    			if(replaceAboveMaterial != null && doReplaceAboveBelowOnly)
	        	{
	        		boolean bFound = false;
        			for(Object[] coords : coordsAboveDone)
        			{
        				if((Integer)coords[0] == x + block.x && (Integer)coords[1] == z + block.z)
        				{
        					bFound = true;
        					break;
        				}
        			}

	        		if(!bFound)
	        		{
	        			coordsAboveDone.add(new Object[] { x + block.x, z + block.z });
	        			int blockY = y + block.y + 1; // TODO: This is wrong, should be the lowest block in the BO3 at these x-z coordinates. replaceAbove should be done before any blocks in this column are placed

        				int heighestBlockToReplace = world.getHighestBlockYAt(x + block.x, z + block.z, true, true, false, false);

	        			// TODO: Use world height constant (dunno what its called and where its at)??
	        			while(blockY <= heighestBlockToReplace && blockY > y + block.y)
	        			{
                            blockToQueueForSpawn = new BlockFunction();

	        				if(spawnUnderWater && blockY >= waterLevel)// && replaceAboveMaterial.isLiquid())
			            	{
	        					blockToQueueForSpawn.material = airMaterial;
			            	} else {
			            		blockToQueueForSpawn.material = replaceAboveMaterial;
			            	}

                            blockToQueueForSpawn.x = x + block.x;
                            blockToQueueForSpawn.y = blockY;
                            blockToQueueForSpawn.z = z + block.z;

    						blockToQueueForSpawn.metaDataName = block.metaDataName;
    						blockToQueueForSpawn.metaDataTag = block.metaDataTag;

                            sourceBlockMaterial = world.getMaterial(blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z, settings.isOTGPlus);

            	        	if(!originalTopBlocks.containsKey(ChunkCoordinate.fromChunkCoords(blockToQueueForSpawn.x, blockToQueueForSpawn.z)))
            	        	{
            	        		int highestBlockY = world.getHighestBlockYAt(blockToQueueForSpawn.x, blockToQueueForSpawn.z, true, true, false, false);
            	        		if(highestBlockY <= OTG.WORLD_DEPTH)
            	        		{
            	        			highestBlockY = 1;
            	        		}
            	        		if(highestBlockY >= OTG.WORLD_HEIGHT)
            	        		{
            	        			highestBlockY = 255;
            	        		}
            	        		originalTopBlocks.put(ChunkCoordinate.fromChunkCoords(blockToQueueForSpawn.x, blockToQueueForSpawn.z), world.getMaterial(blockToQueueForSpawn.x, highestBlockY, blockToQueueForSpawn.z, settings.isOTGPlus));
            	        	}

                			if(!sourceBlockMaterial.toDefaultMaterial().equals(blockToQueueForSpawn.material.toDefaultMaterial()) || sourceBlockMaterial.getBlockData() != blockToQueueForSpawn.material.getBlockData())
                			{
            					ChunkCoordinate destChunk = ChunkCoordinate.fromBlockCoords(blockToQueueForSpawn.x, blockToQueueForSpawn.z);
            					if(chunkCoord.equals(destChunk))
            					{
           							setBlock(world, blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z, blockToQueueForSpawn.material, blockToQueueForSpawn.metaDataTag, isStructureAtSpawn);
            					} else {
            						outOfBounds = true;
            					}
                			}
	        				blockY += 1;
	        			}
	        		}
	        	}
    			if(replaceBelowMaterial != null && block.y == 0 && !block.material.isAir() && doReplaceAboveBelowOnly)
    			{
	        		boolean bFound = false;
        			for(Object[] coords : coordsBelowDone)
        			{
        				if((Integer)coords[0] == x + block.x && (Integer)coords[1] == z + block.z)
        				{
        					bFound = true;
        					break;
        				}
        			}

	        		if(!bFound)
	        		{
	        			coordsBelowDone.add(new Object[] { x + block.x, z + block.z });
	        			int blockY = y + block.y - 1;

	        			while(blockY > OTG.WORLD_DEPTH)
	        			{
	        				if(blockY < OTG.WORLD_HEIGHT)
	        				{
	                            blockToQueueForSpawn = new BlockFunction();
	                            blockToQueueForSpawn.x = x + block.x;
	                            blockToQueueForSpawn.y = blockY;
	                            blockToQueueForSpawn.z = z + block.z;
	                            blockToQueueForSpawn.material = replaceBelowMaterial;

	    						blockToQueueForSpawn.metaDataName = block.metaDataName;
	    						blockToQueueForSpawn.metaDataTag = block.metaDataTag;

	    						sourceBlockMaterial = world.getMaterial(blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z, settings.isOTGPlus);

	            	        	if(!originalTopBlocks.containsKey(ChunkCoordinate.fromChunkCoords(blockToQueueForSpawn.x, blockToQueueForSpawn.z)))
	            	        	{
	            	        		int highestBlockY = world.getHighestBlockYAt(blockToQueueForSpawn.x, blockToQueueForSpawn.z, true, true, false, false);
	            	        		if(highestBlockY <= OTG.WORLD_DEPTH)
	            	        		{
	            	        			highestBlockY = 1;
	            	        		}
	            	        		if(highestBlockY >= OTG.WORLD_HEIGHT)
	            	        		{
	            	        			highestBlockY = 255;
	            	        		}
	            	        		originalTopBlocks.put(ChunkCoordinate.fromChunkCoords(blockToQueueForSpawn.x, blockToQueueForSpawn.z), world.getMaterial(blockToQueueForSpawn.x, highestBlockY, blockToQueueForSpawn.z, settings.isOTGPlus));
	            	        	}

	                			if(!sourceBlockMaterial.isSolid() && !sourceBlockMaterial.toDefaultMaterial().equals(blockToQueueForSpawn.material.toDefaultMaterial()) || sourceBlockMaterial.getBlockData() != blockToQueueForSpawn.material.getBlockData())
	                			{
                					ChunkCoordinate destChunk = ChunkCoordinate.fromBlockCoords(blockToQueueForSpawn.x, blockToQueueForSpawn.z);
                					if(chunkCoord.equals(destChunk))
                					{
            							setBlock(world, blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z, blockToQueueForSpawn.material, blockToQueueForSpawn.metaDataTag, isStructureAtSpawn);
                					} else {
                						outOfBounds = true;
                					}
	                			}
	                			else if(sourceBlockMaterial.isSolid())
	                			{
	                				break;
	                			}
	        				}
	        				blockY -= 1;
	        			}
	        		}
	        	}
    			if(y + block.y > 0 && y + block.y < 256 && !doReplaceAboveBelowOnly)
    			{
                    blockToQueueForSpawn = new BlockFunction();
                    blockToQueueForSpawn.x = x + block.x;
                    blockToQueueForSpawn.y = y + block.y;
                    blockToQueueForSpawn.z = z + block.z;
					blockToQueueForSpawn.material = block.material;

					blockToQueueForSpawn.metaDataName = block.metaDataName;
					blockToQueueForSpawn.metaDataTag = block.metaDataTag;

					sourceBlockMaterial = world.getMaterial(blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z, settings.isOTGPlus);

    	        	if(!originalTopBlocks.containsKey(ChunkCoordinate.fromChunkCoords(blockToQueueForSpawn.x, blockToQueueForSpawn.z)))
    	        	{
    	        		int highestBlockY = world.getHighestBlockYAt(blockToQueueForSpawn.x, blockToQueueForSpawn.z, true, true, false, false);
    	        		if(highestBlockY > OTG.WORLD_DEPTH)
    	        		{
    	        			originalTopBlocks.put(ChunkCoordinate.fromChunkCoords(blockToQueueForSpawn.x, blockToQueueForSpawn.z), world.getMaterial(blockToQueueForSpawn.x, highestBlockY, blockToQueueForSpawn.z, settings.isOTGPlus));
    	        		} else {
    	        			originalTopBlocks.put(ChunkCoordinate.fromChunkCoords(blockToQueueForSpawn.x, blockToQueueForSpawn.z), null);
    	        		}
    	        	}

    				if(replaceWithBiomeBlocks)
    				{
                        if(isOnBiomeBorder)
                        {
        	                biome = world.getBiome(blockToQueueForSpawn.x, blockToQueueForSpawn.z);
        	                biomeConfig = biome.getBiomeConfig();

    		                biomeSurfaceBlock = biomeConfig.surfaceBlock;
    		                if(biomeSurfaceBlock == null)
    		                {
    		                	biomeSurfaceBlock = bo3SurfaceBlock;
    		                }

    		                biomeGroundBlock = biomeConfig.groundBlock;
    		                if(biomeGroundBlock == null)
    		                {
    		                	biomeGroundBlock = bo3GroundBlock;
    		                }

    		    	        if(biomeSurfaceBlock.toDefaultMaterial().equals(DefaultMaterial.SNOW))
    		    	        {
    		    	        	biomeSurfaceBlock = OTG.toLocalMaterialData(DefaultMaterial.SNOW_BLOCK,0);
    		    	        }
    		    	        if(biomeGroundBlock.toDefaultMaterial().equals(DefaultMaterial.SNOW))
    		    	        {
    		    	        	biomeGroundBlock = OTG.toLocalMaterialData(DefaultMaterial.SNOW_BLOCK,0);
    		    	        }
                        }

    					if(blockToQueueForSpawn.material.toDefaultMaterial().equals(bo3GroundBlock.toDefaultMaterial()) && blockToQueueForSpawn.material.getBlockData() == bo3GroundBlock.getBlockData())
    					{
    						blockToQueueForSpawn.material = biomeGroundBlock;
    					}
    					else if(blockToQueueForSpawn.material.toDefaultMaterial().equals(bo3SurfaceBlock.toDefaultMaterial()) && blockToQueueForSpawn.material.getBlockData() == bo3SurfaceBlock.getBlockData())
    					{
    						blockToQueueForSpawn.material = biomeSurfaceBlock;

    	        			LocalMaterialData originalSurfaceBlock = originalTopBlocks.get(ChunkCoordinate.fromChunkCoords(blockToQueueForSpawn.x, blockToQueueForSpawn.z));
    	        			if(originalSurfaceBlock != null && originalSurfaceBlock.toDefaultMaterial() != DefaultMaterial.UNKNOWN_BLOCK && !originalSurfaceBlock.isLiquid() && !originalSurfaceBlock.isAir())
    	        			{
    	        				blockToQueueForSpawn.material = originalSurfaceBlock;
    	        			}
    					}

                        if(
                    		biomeConfig.surfaceAndGroundControl != null &&
                    		biomeConfig.surfaceAndGroundControl instanceof MesaSurfaceGenerator
                		)
                        {
                        	if(
                    			(
            						blockToQueueForSpawn.material.toDefaultMaterial().equals(biomeGroundBlock.toDefaultMaterial()) &&
            						blockToQueueForSpawn.material.getBlockData() == biomeGroundBlock.getBlockData()
        						)
        						||
        						(
    								blockToQueueForSpawn.material.toDefaultMaterial().equals(biomeSurfaceBlock.toDefaultMaterial()) &&
    								blockToQueueForSpawn.material.getBlockData() == biomeSurfaceBlock.getBlockData()
								)
            				)
                        	{
            		        	LocalMaterialData customBlockData = biomeConfig.surfaceAndGroundControl.getCustomBlockData(world, biomeConfig, blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z);
            		        	if(customBlockData != null)
            		        	{
            		        		blockToQueueForSpawn.material = customBlockData;
            		        	}
                        	}
                        }
    				}

    				if(spawnUnderWater && blockToQueueForSpawn.material.toDefaultMaterial().equals(DefaultMaterial.TORCH) && sourceBlockMaterial.isLiquid())
    				{
    					continue;
    				}

    				if(!sourceBlockMaterial.toDefaultMaterial().equals(blockToQueueForSpawn.material.toDefaultMaterial()) || sourceBlockMaterial.getBlockData() != blockToQueueForSpawn.material.getBlockData())
    				{
    					ChunkCoordinate destChunk = ChunkCoordinate.fromBlockCoords(blockToQueueForSpawn.x, blockToQueueForSpawn.z);
    					if(chunkCoord.equals(destChunk))
    					{
   							setBlock(world, blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z, blockToQueueForSpawn.material, blockToQueueForSpawn.metaDataTag, isStructureAtSpawn);
    					} else {
    						outOfBounds = true;
    					}
    				}
				}
        	}
        }
        if(outOfBounds)
        {
        	OTG.log(LogMarker.WARN, "BO3 " + this.getName() + " tried to spawn blocks outside of the chunk being populated, the blocks have been ignored. This can happen if a BO3 is not sliced into 16x16 pieces or has branches positioned in such a way that they cross a chunk border. OTG is more strict than TC in how branching BO3's used as CustomStructures() should be designed, BO3 creators have to design their BO3's and position their branches so that they fit neatly into a 16x16 grid. Hopefully in a future release OTG can be made to automatically slice branching structures instead of forcing the BO3 creator to do it.");
        }

        if(OTG.getPluginConfig().SpawnLog && (System.currentTimeMillis() - startTime) > 50)
        {
        	OTG.log(LogMarker.WARN, "Warning: Spawning BO3 " + this.getName()  + " took " + (System.currentTimeMillis() - startTime) + " Ms.");
        }

        return true;
    }

    private void setBlock(LocalWorld world, int x, int y, int z, LocalMaterialData material, NamedBinaryTag metaDataTag, boolean isStructureAtSpawn)
    {
	    HashMap<DefaultMaterial,LocalMaterialData> blocksToReplace = world.getConfigs().getWorldConfig().getReplaceBlocksDict();
	    if(blocksToReplace != null && blocksToReplace.size() > 0)
	    {
	    	LocalMaterialData targetBlock = blocksToReplace.get(material.toDefaultMaterial());
	    	if(targetBlock != null)
	    	{
	    		material = targetBlock;
	    	}
	    }
	    if(OTG.getPluginConfig().DeveloperMode)
	    {
		    DefaultMaterial worldMaterial = world.getMaterial(x, y, z, false).toDefaultMaterial();
		    if(
	    		worldMaterial == DefaultMaterial.GOLD_BLOCK ||
	    		worldMaterial == DefaultMaterial.IRON_BLOCK ||
	    		worldMaterial == DefaultMaterial.REDSTONE_BLOCK ||
	    		worldMaterial == DefaultMaterial.DIAMOND_BLOCK ||
	    		worldMaterial == DefaultMaterial.LAPIS_BLOCK ||
	    		worldMaterial == DefaultMaterial.COAL_BLOCK ||
	    		worldMaterial == DefaultMaterial.QUARTZ_BLOCK ||
	    		worldMaterial == DefaultMaterial.EMERALD_BLOCK
    		)
		    {
		    	if(
		    		material.toDefaultMaterial() == DefaultMaterial.GOLD_BLOCK ||
					material.toDefaultMaterial() == DefaultMaterial.IRON_BLOCK ||
					material.toDefaultMaterial() == DefaultMaterial.REDSTONE_BLOCK ||
					material.toDefaultMaterial() == DefaultMaterial.DIAMOND_BLOCK ||
					material.toDefaultMaterial() == DefaultMaterial.LAPIS_BLOCK ||
					material.toDefaultMaterial() == DefaultMaterial.COAL_BLOCK ||
					material.toDefaultMaterial() == DefaultMaterial.QUARTZ_BLOCK ||
					material.toDefaultMaterial() == DefaultMaterial.EMERALD_BLOCK
    			)
		    	{
		    		world.setBlock(x, y, z, OTG.toLocalMaterialData(DefaultMaterial.GLOWSTONE, 0), null, true);
		    	}
		    	return;
		    }
	    }
	    world.setBlock(x, y, z, material, metaDataTag, true);
    }

	//

    private BO3Config settings;
    private final String name;
    private final File file;

    /**
     * Creates a BO3 from a file.
     *
     * @param name Name of the BO3.
     * @param file File of the BO3. If the file does not exist, a BO3 with the default settings is created.
     */
    public BO3(String name, File file)
    {
        this.name = name;
        this.file = file;
    }

    @Override
    public void onEnable(Map<String, CustomObject> otherObjectsInDirectory)
    {
    	try
    	{
    		this.settings = new BO3Config(new FileSettingsReaderOTGPlus(name, file), otherObjectsInDirectory);
    		if(this.settings.settingsMode != ConfigMode.WriteDisable)
    		{
    			FileSettingsWriterOTGPlus.writeToFile(this.settings, this.settings.settingsMode);
    		}
    	}
    	catch(InvalidConfigException ex)
    	{
    		isInvalidConfig = true;
    	}

    	// TODO: Is this really necessary?
    	if(this.settings != null && this.settings.isOTGPlus)
    	{
	    	this.settings.blocks = null;
	    	this.settings.bo3Checks = null;
	    	this.settings.branches = null;
	    	this.settings.boundingBoxes = null;
	    	this.settings.entityFunctions = null;
	    	this.settings.particleFunctions = null;
	    	this.settings.modDataFunctions = null;
	    	this.settings.spawnerFunctions = null;
    	}
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
    public int getOffsetAndVariance(Random random, int offset, int variance)
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
        return MathHelper.clamp(offset + variance, OTG.WORLD_DEPTH, OTG.WORLD_HEIGHT);
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
    public boolean canSpawnAsTree()
    {
        return settings.tree;
    }

    @Override
    public boolean canSpawnAsObject()
    {
        return true;
    }

    @Override
    public boolean canRotateRandomly()
    {
        return settings.rotateRandomly;
    }

    // Used for spawning saplings and customobjects without doing checks (for growing saplings, /spawn command, StructureAtSpawn etc).
    @Override
    public boolean spawnForced(LocalWorld world, Random random, Rotation rotation, int x, int y, int z)
    {
		return trySpawnAt(true, null, world, random, rotation, x, y, z);
    }

    public boolean spawnAsTree(LocalWorld world, Random random, int x, int z)
    {
		return spawn(world, random, x, z);
    }

    protected boolean spawn(LocalWorld world, Random random, int x, int z)
    {
        Rotation rotation = settings.rotateRandomly ? Rotation.getRandomRotation(random) : Rotation.NORTH;
        int y = 0;
        if (settings.spawnHeight == SpawnHeightEnum.randomY)
        {
            y = settings.minHeight == settings.maxHeight ? settings.minHeight : RandomHelper.numberInRange(random, settings.minHeight, settings.maxHeight);
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
        return trySpawnAt(world, random, rotation, x, y, z);
    }

    @Override
    public boolean process(LocalWorld world, Random random, ChunkCoordinate chunkCoord)
    {
    	// This method is only used to spawn CustomObject.

        boolean atLeastOneObjectHasSpawned = false;

        int chunkMiddleX = chunkCoord.getBlockXCenter();
        int chunkMiddleZ = chunkCoord.getBlockZCenter();
        for (int i = 0; i < settings.frequency; i++)
        {
            if (settings.rarity > random.nextDouble() * 100.0)
            {
                int x = chunkMiddleX + random.nextInt(ChunkCoordinate.CHUNK_X_SIZE);
                int z = chunkMiddleZ + random.nextInt(ChunkCoordinate.CHUNK_Z_SIZE);

                if (spawn(world, random, x, z))
                {
                    atLeastOneObjectHasSpawned = true;
                }
            }
        }

        return atLeastOneObjectHasSpawned;
    }

    // TODO: Not used?
    @Override
    public CustomObject applySettings(SettingsReaderOTGPlus extraSettings)
    {
        extraSettings.setFallbackReader(this.settings.reader);
        return new BO3(this, extraSettings);
    }

    @Override
    public boolean hasPreferenceToSpawnIn(LocalBiome biome)
    {
        if (settings.excludedBiomes.contains("All") || settings.excludedBiomes.contains("all") || settings.excludedBiomes.contains(biome.getName()))
        {
            return false;
        }
        return true;
    }

    @Override
    public boolean hasBranches()
    {
    	if(settings.isOTGPlus)
    	{
            return settings.getbranches().length != 0;
    	} else {
    		return settings.branches[0].length != 0;
    	}
    }

    @Override
    public Branch[] getBranches(Rotation rotation)
    {
    	if(settings.isOTGPlus)
    	{
    		throw new RuntimeException();
    	} else {
    		return settings.branches[rotation.getRotationId()];
    	}
    }

    @Override
    public Branch[] getBranches()
    {
        return settings.getbranches();
    }

    @Override
    public CustomObjectCoordinate makeCustomObjectCoordinate(LocalWorld world, Random random, int chunkX, int chunkZ)
    {
    	if(settings.isOTGPlus)
    	{
    		if(OTG.getPluginConfig().SpawnLog)
    		{
    			OTG.log(LogMarker.WARN, "Tried to spawn a OTG+ enabled BO3 as CustomStructure in a non-OTG+ enabled world. BO3: " + settings.getName());
    		}
    		return null;
    	} else {
	        if (settings.rarity > random.nextDouble() * 100.0)
	        {
	            Rotation rotation = settings.rotateRandomly ? Rotation.getRandomRotation(random) : Rotation.NORTH;
	            int height = RandomHelper.numberInRange(random, settings.minHeight, settings.maxHeight);
	            return new CustomObjectCoordinate(world, this, this.getName(), rotation, chunkX * 16 + 8 + random.nextInt(16), height, chunkZ * 16 + 8 + random.nextInt(16));
	        }
	        return null;
    	}
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
}
