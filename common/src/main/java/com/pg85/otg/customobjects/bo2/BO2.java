package com.pg85.otg.customobjects.bo2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Random;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.customobjects.CustomObjectConfigFile;
import com.pg85.otg.configuration.io.SettingsReaderOTGPlus;
import com.pg85.otg.configuration.io.SettingsWriterOTGPlus;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.customobjects.CustomObject;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.bo3.NamedBinaryTag;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.materials.MaterialSet;
import com.pg85.otg.util.minecraft.defaults.DefaultMaterial;

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
    public boolean canRotateRandomly()
    {
        return randomRotation;
    }
    
    // Used to safely spawn this object from a grown sapling
    @Override
    public boolean spawnFromSapling(LocalWorld world, Random random, Rotation rotation, int x, int y, int z)
    {
        ObjectCoordinate[] data = this.data[rotation.getRotationId()];
        ArrayList<ObjectCoordinate> blocksToSpawn = new ArrayList<ObjectCoordinate>();

        for (ObjectCoordinate point : data)
        {
            DefaultMaterial material = world.getMaterial(x + point.x, y + point.y, z + point.z, null).toDefaultMaterial();

            // Do not spawn if non-tree blocks are in the way
            if (!material.isAir() && material != DefaultMaterial.LOG && material != DefaultMaterial.LOG_2 && material != DefaultMaterial.LEAVES && material != DefaultMaterial.LEAVES_2)
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
            setBlock(world, (x + point.x), y + point.y, z + point.z, point.material, null, false, null);
        }
        return true;
    }

    @Override
    public boolean spawnForced(LocalWorld world, Random random, Rotation rotation, int x, int y, int z)
    {
        ObjectCoordinate[] data = this.data[rotation.getRotationId()];

        LocalMaterialData worldMaterial;
        // Spawn
        for (ObjectCoordinate point : data)
        {
            if ((worldMaterial = world.getMaterial(x + point.x, y + point.y, z + point.z, null)) != null)
            {
    			if(worldMaterial.isAir())
            	{
    				setBlock(world, x + point.x, y + point.y, z + point.z, point.material, null, false, null);
	            }
				else if (dig)
	            {
	                setBlock(world, x + point.x, y + point.y, z + point.z, point.material, null, false, null);
	            }
            }
        }
        return true;
    }
    
    private boolean canSpawnAt(LocalWorld world, Rotation rotation, int x, int y, int z, ChunkCoordinate chunkBeingPopulated)
    {    	
        // Basic checks
    	
        if (y < PluginStandardValues.WORLD_DEPTH || y >= PluginStandardValues.WORLD_HEIGHT)  // Isn't this already done before this method is called?
        {
            return false;
        }
    	
        if ((y < spawnElevationMin) || (y > spawnElevationMax))
        {
            return false;
        }

        if (!spawnOnBlockType.contains(world.getMaterial(x, y - 1, z, chunkBeingPopulated)))
        {
            return false;
        }

        LocalMaterialData worldMaterial = world.getMaterial(x, y - 5, z, chunkBeingPopulated);
        if (needsFoundation && worldMaterial == null || worldMaterial.isAir())
        {
            return false;
        }

        LocalMaterialData checkBlock = !spawnWater || !spawnLava ? world.getMaterial(x, y + 2, z, chunkBeingPopulated) : null;
        DefaultMaterial checkBlockDefaultMaterial = checkBlock != null ? checkBlock.toDefaultMaterial() : null;
        if(checkBlock == null)
        {
        	// Tried to spawn in unloaded chunks when populationBoundsCheck:false.
        	return false;
        }
        if (!spawnWater)
        {
            if (checkBlockDefaultMaterial.equals(DefaultMaterial.WATER) || checkBlockDefaultMaterial.equals(DefaultMaterial.STATIONARY_WATER))
            {
                return false;
            }
        }
        if (!spawnLava)
        {
            if (checkBlockDefaultMaterial.equals(DefaultMaterial.LAVA) || checkBlockDefaultMaterial.equals(DefaultMaterial.STATIONARY_LAVA))
            {
                return false;
            }
        }

        int checkLight = !spawnSunlight || !spawnDarkness ? world.getLightLevel(x, y + 2, z, chunkBeingPopulated) : 0;
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
            if (y + point.y < PluginStandardValues.WORLD_DEPTH || y + point.y >= PluginStandardValues.WORLD_HEIGHT)
            {
                return false;
            }
        	
        	chunkCoord = ChunkCoordinate.fromBlockCoords((x + point.x), (z + point.z));
    		
        	if(!loadedChunks.contains(chunkCoord))
    		{     
        		if(chunkBeingPopulated != null && !OTG.IsInAreaBeingPopulated(x + point.x, z + point.z, chunkBeingPopulated))
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
	            if ((material = world.getMaterial((x + point.x), (y + point.y), (z + point.z), chunkBeingPopulated)) == null || collisionBlockType.contains(material))
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
        if (!OTG.fireCanCustomObjectSpawnEvent(this, world, x, y, z))
        {
            // Cancelled
            return false;
        }

        return true;
    }    

    @Override
    public boolean spawnAsTree(LocalWorld world, Random random, int x, int z, int minY, int maxY, ChunkCoordinate chunkBeingPopulated)
    {
   		return spawn(world, random, x, z, minY == -1 ? this.spawnElevationMin : minY, maxY == -1 ? this.spawnElevationMax : maxY, chunkBeingPopulated);
    } 
    
    private boolean spawn(LocalWorld world, Random random, int x, int z, int minY, int maxY, ChunkCoordinate chunkBeingPopulated)
    {
        int y;
        if (spawnAboveGround)
        {
            y = world.getBlockAboveSolidHeight(x, z, chunkBeingPopulated);
        }
        else if (spawnUnderGround)
        {
            int solidHeight = world.getBlockAboveSolidHeight(x, z, chunkBeingPopulated);
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
            y = world.getHighestBlockAboveYAt(x, z, chunkBeingPopulated);
        }

        if (y < 0)
        {
            return false;
        }

        Rotation rotation = randomRotation ? Rotation.getRandomRotation(random) : Rotation.NORTH;

        if (!canSpawnAt(world, rotation, x, y, z, chunkBeingPopulated))
        {
            return false;
        }
        
        ObjectCoordinate[] data = this.data[rotation.getRotationId()];
        
        LocalMaterialData worldMaterial;
        // Spawn
        for (ObjectCoordinate point : data)
        {
            if ((worldMaterial = world.getMaterial(x + point.x, y + point.y, z + point.z, chunkBeingPopulated)) != null)
            {
        		if(worldMaterial.isAir())
	            {
	                setBlock(world, (x + point.x), y + point.y, z + point.z, point.material, null, false, chunkBeingPopulated);
	            }
				else if (dig)
	            {
	                setBlock(world, (x + point.x), y + point.y, z + point.z, point.material, null, false, chunkBeingPopulated);
	            }
            }
        }
        return true;
    }

    // Called during population.
    @Override
    public boolean process(LocalWorld world, Random rand, ChunkCoordinate chunkCoord)
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

            int x = chunkCoord.getBlockX() + rand.nextInt(ChunkCoordinate.CHUNK_X_SIZE);
            int z = chunkCoord.getBlockZ() + rand.nextInt(ChunkCoordinate.CHUNK_Z_SIZE);

            objectSpawned = spawn(world, rand, x, z, this.spawnElevationMin, this.spawnElevationMax, chunkCoord);
        }

        return objectSpawned;
    }

    @Override
    protected void writeConfigSettings(SettingsWriterOTGPlus writer) throws IOException
    {
        // It doesn't write.
    }

    @Override
    protected void readConfigSettings()
    {
        this.spawnOnBlockType = readSettings(BO2Settings.SPAWN_ON_BLOCK_TYPE);
        this.collisionBlockType = readSettings(BO2Settings.COLLISTION_BLOCK_TYPE);

        this.spawnSunlight = readSettings(BO2Settings.SPAWN_SUNLIGHT);
        this.spawnDarkness = readSettings(BO2Settings.SPAWN_DARKNESS);
        this.spawnWater = readSettings(BO2Settings.SPAWN_WATER);
        this.spawnLava = readSettings(BO2Settings.SPAWN_LAVA);
        this.spawnAboveGround = readSettings(BO2Settings.SPAWN_ABOVE_GROUND);
        this.spawnUnderGround = readSettings(BO2Settings.SPAWN_UNDER_GROUND);

        this.randomRotation = readSettings(BO2Settings.RANDON_ROTATION);
        this.dig = readSettings(BO2Settings.DIG);
        this.tree = readSettings(BO2Settings.TREE);
        this.branch = readSettings(BO2Settings.BRANCH);
        this.needsFoundation = readSettings(BO2Settings.NEEDS_FOUNDATION);
        this.rarity = readSettings(BO2Settings.RARITY);
        this.collisionPercentage = readSettings(BO2Settings.COLLISION_PERCENTAGE);
        this.spawnElevationMin = readSettings(BO2Settings.SPAWN_ELEVATION_MIN);
        this.spawnElevationMax = readSettings(BO2Settings.SPAWN_ELEVATION_MAX);

        this.readCoordinates();
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

    private void readCoordinates()
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
            ObjectCoordinate buffer = ObjectCoordinate.getCoordinateFromString(line.getKey(), line.getValue());
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

    private void setBlock(LocalWorld world, int x, int y, int z, LocalMaterialData material, NamedBinaryTag metaDataTag, boolean isStructureAtSpawn, ChunkCoordinate chunkBeingPopulated)
    {
        HashMap<DefaultMaterial, LocalMaterialData> blocksToReplace = world.getConfigs().getWorldConfig().getReplaceBlocksDict();
        if (blocksToReplace != null && blocksToReplace.size() > 0)
        {
            LocalMaterialData targetBlock = blocksToReplace.get(material.toDefaultMaterial());
            if (targetBlock != null)
            {
                material = targetBlock;
            }
        }
        
        material.parseForWorld(world);

        world.setBlock(x, y, z, material, metaDataTag, chunkBeingPopulated);
    }

    @Override
    public boolean onEnable()
    {
        enable();
        return true;
    }

    private void enable()
    {
        readConfigSettings();
        correctSettings();
    }

    @Override
    public boolean loadChecks()
    {
        return true;
    }
}
