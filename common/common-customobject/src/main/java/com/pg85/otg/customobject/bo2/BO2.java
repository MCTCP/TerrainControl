package com.pg85.otg.customobject.bo2;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Random;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.customobject.CustomObject;
import com.pg85.otg.customobject.CustomObjectManager;
import com.pg85.otg.customobject.config.CustomObjectConfigFile;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.customobject.config.io.SettingsReaderOTGPlus;
import com.pg85.otg.customobject.config.io.SettingsWriterOTGPlus;
import com.pg85.otg.customobject.structures.CustomStructureCache;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.bo3.NamedBinaryTag;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.interfaces.ICustomObjectManager;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IModLoadedChecker;
import com.pg85.otg.util.interfaces.IPresetNameProvider;
import com.pg85.otg.util.interfaces.IWorldGenRegion;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterials;
import com.pg85.otg.util.materials.MaterialSet;

/**
 * The good old BO2.
 */
public class BO2 extends CustomObjectConfigFile implements CustomObject
{	
    private ObjectCoordinate[][] data = new ObjectCoordinate[4][];

    private MaterialSet spawnOnBlockType;
    private MaterialSet collisionBlockType;

    private boolean spawnWater;
    private boolean spawnLava;
    private boolean spawnAboveGround;
    private boolean spawnUnderGround;

    private boolean spawnSunlight;
    private boolean spawnDarkness;

    private boolean randomRotation;
    private boolean dig;
    private boolean tree;
    private boolean branch;
    private boolean needsFoundation;
    private boolean doReplaceBlocks;
    private int rarity;
    private double collisionPercentage;
    private int spawnElevationMin;
    private int spawnElevationMax;

    BO2(SettingsReaderOTGPlus reader)
    {
        super(reader);
    }

    @Override
    public boolean canSpawnAsTree()
    {
        return tree;
    }

    @Override
    public boolean doReplaceBlocks()
    {
    	return this.doReplaceBlocks;
    }
    
    @Override
    public boolean canRotateRandomly()
    {
        return randomRotation;
    }
    
    // Used to safely spawn this object from a grown sapling
    @Override
    public boolean spawnFromSapling(CustomStructureCache structureCache, IWorldGenRegion worldGenRegion, Random random, Rotation rotation, int x, int y, int z)
    {
        ObjectCoordinate[] data = this.data[rotation.getRotationId()];
        ArrayList<ObjectCoordinate> blocksToSpawn = new ArrayList<ObjectCoordinate>();

        for (ObjectCoordinate point : data)
        {
            LocalMaterialData material = worldGenRegion.getMaterial(x + point.x, y + point.y, z + point.z, null);

            // Do not spawn if non-tree blocks are in the way
            if (
        		!material.isAir() && 
        		!material.isLogOrLeaves()
    		)
            {
                return false;
            }

            // Only overwrite air
            if (material.isAir())
            {
                blocksToSpawn.add(point);
            }
        }

        for (ObjectCoordinate point : blocksToSpawn)
        {
            setBlock(worldGenRegion, (x + point.x), y + point.y, z + point.z, point.material, null, false, null, false);
        }
        return true;
    }

    @Override
    public boolean spawnForced(CustomStructureCache structureCache, IWorldGenRegion worldGenRegion, Random random, Rotation rotation, int x, int y, int z)
    {
        ObjectCoordinate[] data = this.data[rotation.getRotationId()];

        LocalMaterialData worldMaterial;
        // Spawn
        for (ObjectCoordinate point : data)
        {
            if ((worldMaterial = worldGenRegion.getMaterial(x + point.x, y + point.y, z + point.z, null)) != null)
            {
    			if(worldMaterial.isAir())
            	{
    				setBlock(worldGenRegion, x + point.x, y + point.y, z + point.z, point.material, null, false, null, this.doReplaceBlocks());
	            }
				else if (dig)
	            {
	                setBlock(worldGenRegion, x + point.x, y + point.y, z + point.z, point.material, null, false, null, this.doReplaceBlocks());
	            }
            }
        }
        return true;
    }
    
    private boolean canSpawnAt(IWorldGenRegion worldGenRegion, Rotation rotation, int x, int y, int z, ChunkCoordinate chunkBeingPopulated)
    {
        // Basic checks
    	
        if (y < Constants.WORLD_DEPTH || y >= Constants.WORLD_HEIGHT)  // Isn't this already done before this method is called?
        {
            return false;
        }
    	
        if ((y < spawnElevationMin) || (y > spawnElevationMax))
        {
            return false;
        }

        if (!spawnOnBlockType.contains(worldGenRegion.getMaterial(x, y - 1, z, chunkBeingPopulated)))
        {
            return false;
        }

        LocalMaterialData worldMaterial = worldGenRegion.getMaterial(x, y - 5, z, chunkBeingPopulated);
        if (needsFoundation && worldMaterial == null || worldMaterial.isAir())
        {
            return false;
        }

        LocalMaterialData checkBlock = !spawnWater || !spawnLava ? worldGenRegion.getMaterial(x, y + 2, z, chunkBeingPopulated) : null;
        if(checkBlock == null)
        {
        	// Tried to spawn in unloaded chunks when populationBoundsCheck:false.
        	return false;
        }
        if (!spawnWater)
        {
            if (checkBlock.isMaterial(LocalMaterials.WATER))
            {
                return false;
            }
        }
        if (!spawnLava)
        {
            if (checkBlock.isMaterial(LocalMaterials.LAVA))
            {
                return false;
            }
        }

        int checkLight = !spawnSunlight || !spawnDarkness ? worldGenRegion.getLightLevel(x, y + 2, z, chunkBeingPopulated) : 0;
        if(checkLight == -1)
        {
        	// Tried to spawn in unloaded chunk.
        	return false;
        }
        if (!spawnSunlight)
        {
            if (checkLight > 8)
            {
                return false;
            }
        }
        if (!spawnDarkness)
        {
            if (checkLight < 9)
            {
                return false;
            }
        }        

        HashSet<ChunkCoordinate> loadedChunks = new HashSet<ChunkCoordinate>();
        ChunkCoordinate chunkCoord;
        ObjectCoordinate[] objData = this.data[rotation.getRotationId()];
        for (ObjectCoordinate point : objData)
        {
            if (
        		y + point.y < Constants.WORLD_DEPTH || 
        		y + point.y >= Constants.WORLD_HEIGHT
    		)
            {
                return false;
            }
        	
        	chunkCoord = ChunkCoordinate.fromBlockCoords((x + point.x), (z + point.z));
    		
        	if(!loadedChunks.contains(chunkCoord))
    		{     
        		if(chunkBeingPopulated != null && !ChunkCoordinate.IsInAreaBeingPopulated(x + point.x, z + point.z, chunkBeingPopulated))
	            //if (!world.chunkExists((x + point.x), (y + point.y), (z + point.z)))
	            {
	                // Cannot spawn BO2, part of world is not loaded
	                return false;
	            }
	            loadedChunks.add(chunkCoord);
    		}
        }
        
        if (!dig && (int)Math.floor(collisionPercentage) < 100)
        {
	        // Check all blocks
	        int faultCounter = 0;
	        int maxBlocksOutsideSourceBlock = (int)Math.ceil(objData.length * (collisionPercentage / 100.0));
	        LocalMaterialData material;
	        for (ObjectCoordinate point : objData)
	        {
	            if (
            		(material = worldGenRegion.getMaterial((x + point.x), (y + point.y), (z + point.z), chunkBeingPopulated)) == null || 
    				collisionBlockType.contains(material)
				)
	            {
	                faultCounter++;
	                // Don't spawn if blocks would be cut off.
	                if (material == null || faultCounter > maxBlocksOutsideSourceBlock)
	                {
	                    return false;
	                }
	            }
	        }
        }

        // Call event
        //if (!worldGenRegion.fireCanCustomObjectSpawnEvent(this, x, y, z))
        {
            // Cancelled
            //return false;
        }

        return true;
    }

    @Override
    public boolean spawnAsTree(CustomStructureCache structureCache, IWorldGenRegion worldGenRegion, Random random, int x, int z, int minY, int maxY, ChunkCoordinate chunkBeingPopulated)
    {
   		return spawn(worldGenRegion, random, x, z, minY == -1 ? this.spawnElevationMin : minY, maxY == -1 ? this.spawnElevationMax : maxY, chunkBeingPopulated, false);
    } 
    
    private boolean spawn(IWorldGenRegion worldGenRegion, Random random, int x, int z, int minY, int maxY, ChunkCoordinate chunkBeingPopulated, boolean replaceBlocks)
    {
        int y;
        if (spawnAboveGround)
        {
            y = worldGenRegion.getBlockAboveSolidHeight(x, z, chunkBeingPopulated);
        }
        else if (spawnUnderGround)
        {
            int solidHeight = worldGenRegion.getBlockAboveSolidHeight(x, z, chunkBeingPopulated);
            if (solidHeight < 1 || solidHeight <= minY)
            {
                return false;
            }
            if (solidHeight > maxY)
            {
                solidHeight = maxY;
            }
            y = random.nextInt(solidHeight - minY) + minY;
        } else {
            y = worldGenRegion.getHighestBlockAboveYAt(x, z, chunkBeingPopulated);
        }

        if (y < 0)
        {
            return false;
        }

        Rotation rotation = randomRotation ? Rotation.getRandomRotation(random) : Rotation.NORTH;

        if (!canSpawnAt(worldGenRegion, rotation, x, y, z, chunkBeingPopulated))
        {
            return false;
        }
        
        ObjectCoordinate[] data = this.data[rotation.getRotationId()];
        
        LocalMaterialData worldMaterial;
        // Spawn
        for (ObjectCoordinate point : data)
        {
            if ((worldMaterial = worldGenRegion.getMaterial(x + point.x, y + point.y, z + point.z, chunkBeingPopulated)) != null)
            {
        		if(worldMaterial.isAir())
	            {
	                setBlock(worldGenRegion, (x + point.x), y + point.y, z + point.z, point.material, null, false, chunkBeingPopulated, replaceBlocks);
	            }
				else if (dig)
	            {
	                setBlock(worldGenRegion, (x + point.x), y + point.y, z + point.z, point.material, null, false, chunkBeingPopulated, replaceBlocks);
	            }
            }
        }
        return true;
    }

    // Called during population.
    @Override
    public boolean process(CustomStructureCache structureCache, IWorldGenRegion worldGenRegion, Random rand, ChunkCoordinate chunkCoord)
    {
        if (branch)
        {
            return false;
        }

        int randomRoll = rand.nextInt(100);
        int ObjectRarity = rarity;
        boolean objectSpawned = false;

        while (randomRoll < ObjectRarity)
        {
            ObjectRarity -= 100;

            int x = chunkCoord.getBlockX() + rand.nextInt(ChunkCoordinate.CHUNK_SIZE);
            int z = chunkCoord.getBlockZ() + rand.nextInt(ChunkCoordinate.CHUNK_SIZE);

            // TODO: Are BO2/BO3 trees ever spawned via this method? If so, then don't replace blocks.
            objectSpawned = spawn(worldGenRegion, rand, x, z, this.spawnElevationMin, this.spawnElevationMax, chunkCoord, this.doReplaceBlocks());
        }

        return objectSpawned;
    }

    @Override
    protected void writeConfigSettings(SettingsWriterOTGPlus writer, boolean spawnLog, ILogger logger, IMaterialReader materialReader, CustomObjectResourcesManager manager) throws IOException
    {
        // It doesn't write.
    }

    @Override
    protected void readConfigSettings(Path otgRootFolder, boolean spawnLog, ILogger logger, ICustomObjectManager customObjectManager, IPresetNameProvider presetNameProvider, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
    {
        this.spawnOnBlockType = readSettings(BO2Settings.SPAWN_ON_BLOCK_TYPE, spawnLog, logger, materialReader, manager);
        this.collisionBlockType = readSettings(BO2Settings.COLLISTION_BLOCK_TYPE, spawnLog, logger, materialReader, manager);

        this.spawnSunlight = readSettings(BO2Settings.SPAWN_SUNLIGHT, spawnLog, logger, materialReader, manager);
        this.spawnDarkness = readSettings(BO2Settings.SPAWN_DARKNESS, spawnLog, logger, materialReader, manager);
        this.spawnWater = readSettings(BO2Settings.SPAWN_WATER, spawnLog, logger, materialReader, manager);
        this.spawnLava = readSettings(BO2Settings.SPAWN_LAVA, spawnLog, logger, materialReader, manager);
        this.spawnAboveGround = readSettings(BO2Settings.SPAWN_ABOVE_GROUND, spawnLog, logger, materialReader, manager);
        this.spawnUnderGround = readSettings(BO2Settings.SPAWN_UNDER_GROUND, spawnLog, logger, materialReader, manager);

        this.randomRotation = readSettings(BO2Settings.RANDON_ROTATION, spawnLog, logger, materialReader, manager);
        this.dig = readSettings(BO2Settings.DIG, spawnLog, logger, materialReader, manager);
        this.tree = readSettings(BO2Settings.TREE, spawnLog, logger, materialReader, manager);
        this.branch = readSettings(BO2Settings.BRANCH, spawnLog, logger, materialReader, manager);
        this.needsFoundation = readSettings(BO2Settings.NEEDS_FOUNDATION, spawnLog, logger, materialReader, manager);
        this.doReplaceBlocks = readSettings(BO2Settings.DO_REPLACE_BLOCKS, spawnLog, logger, materialReader, manager);
        this.rarity = readSettings(BO2Settings.RARITY, spawnLog, logger, materialReader, manager);
        this.collisionPercentage = readSettings(BO2Settings.COLLISION_PERCENTAGE, spawnLog, logger, materialReader, manager);
        this.spawnElevationMin = readSettings(BO2Settings.SPAWN_ELEVATION_MIN, spawnLog, logger, materialReader, manager);
        this.spawnElevationMax = readSettings(BO2Settings.SPAWN_ELEVATION_MAX, spawnLog, logger, materialReader, manager);

        this.readCoordinates(materialReader);
    }

    @Override
    protected void correctSettings()
    {
        // Stub method
    }

    @Override
    protected void renameOldSettings()
    {
        // Stub method
    }

    private void readCoordinates(IMaterialReader materialReader)
    {
        ArrayList<ObjectCoordinate> coordinates = new ArrayList<ObjectCoordinate>();

        // TODO: Reimplement this?
        /*
         * for (RawSettingValue line : reader.getRawSettings()) { String[]
         * lineSplit = line.getRawValue().split(":", 2); if (lineSplit.length !=
         * 2) continue;
         * 
         * ObjectCoordinate buffer =
         * ObjectCoordinate.getCoordinateFromString(lineSplit[0], lineSplit[1]);
         * if (buffer != null) coordinates.add(buffer); }
         */
        for (Entry<String, String> line : reader.getRawSettings())
        {
            ObjectCoordinate buffer = ObjectCoordinate.getCoordinateFromString(line.getKey(), line.getValue(), materialReader);
            if (buffer != null)
            {
                coordinates.add(buffer);
            }
        }

        data[0] = new ObjectCoordinate[coordinates.size()];
        data[1] = new ObjectCoordinate[coordinates.size()];
        data[2] = new ObjectCoordinate[coordinates.size()];
        data[3] = new ObjectCoordinate[coordinates.size()];

        for (int i = 0; i < coordinates.size(); i++)
        {
            ObjectCoordinate coordinate = coordinates.get(i);

            data[0][i] = coordinate;
            coordinate = coordinate.rotate();
            data[1][i] = coordinate;
            coordinate = coordinate.rotate();
            data[2][i] = coordinate;
            coordinate = coordinate.rotate();
            data[3][i] = coordinate;
        }

    }

    private void setBlock(IWorldGenRegion worldGenRegion, int x, int y, int z, LocalMaterialData material, NamedBinaryTag metaDataTag, boolean isStructureAtSpawn, ChunkCoordinate chunkBeingPopulated, boolean replaceBlocks)
    {
    	/* TODO: Don't think anyone actually uses this? Remove if noone complains about missing it..
        HashMap<LocalMaterialData, LocalMaterialData> blocksToReplace = world.getConfigs().getWorldConfig().getReplaceBlocksDict();
        if (blocksToReplace != null && blocksToReplace.size() > 0)
        {
            LocalMaterialData targetBlock = blocksToReplace.get(material);
            if (targetBlock != null)
            {
                material = targetBlock;
            }
        }
        */
    	worldGenRegion.setBlock(x, y, z, material, metaDataTag, chunkBeingPopulated, replaceBlocks);
    }

    boolean isEnabled = false;
    
    @Override
    public boolean onEnable(Path otgRootFolder, boolean spawnLog, ILogger logger, CustomObjectManager customObjectManager, IPresetNameProvider presetNameProvider, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
    {
    	if(!isEnabled)
    	{
    		isEnabled = true;
            enable(otgRootFolder, spawnLog, logger, customObjectManager, presetNameProvider, materialReader, manager, modLoadedChecker);
    	}
        return true;
    }

    private void enable(Path otgRootFolder, boolean spawnLog, ILogger logger, CustomObjectManager customObjectManager, IPresetNameProvider presetNameProvider, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
    {
        readConfigSettings(otgRootFolder, spawnLog, logger, customObjectManager, presetNameProvider, materialReader, manager, modLoadedChecker);
        correctSettings();
    }

    @Override
    public boolean loadChecks(IModLoadedChecker modLoadedChecker)
    {
        return true;
    }
}
