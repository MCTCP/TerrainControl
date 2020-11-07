package com.pg85.otg.customobjects.structures.bo4.smoothing;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.Map.Entry;

import com.pg85.otg.common.LocalWorldGenRegion;
import com.pg85.otg.common.materials.LocalMaterialData;
import com.pg85.otg.customobjects.bo4.BO4;
import com.pg85.otg.customobjects.bo4.bo4function.BO4BlockFunction;
import com.pg85.otg.customobjects.bo4.bo4function.BO4RandomBlockFunction;
import com.pg85.otg.customobjects.structures.CustomStructureCache;
import com.pg85.otg.customobjects.structures.CustomStructureCoordinate;
import com.pg85.otg.customobjects.structures.bo4.BO4CustomStructureCoordinate;
import com.pg85.otg.customobjects.structures.bo4.smoothing.SmoothingAreaBlock.enumSmoothingBlockType;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.bo3.Rotation;

public class SmoothingAreaGenerator
{ 
    // A smoothing area is drawn around all outer blocks (or blocks neighbouring air) on the lowest layer of blocks in each BO3 of this branching structure that has a SmoothRadius set greater than 0.
	// Holds all unspawned smoothing area lines per chunk.
	public Map<ChunkCoordinate, ArrayList<SmoothingAreaLine>> smoothingAreasToSpawn = new HashMap<ChunkCoordinate, ArrayList<SmoothingAreaLine>>();	
    private Map<ChunkCoordinate, ArrayList<SmoothingAreaLine>> smoothingAreasToSpawnPerLineDestination = new HashMap<ChunkCoordinate, ArrayList<SmoothingAreaLine>>();
	   
    public ArrayList<ChunkCoordinate> getSmoothingAreaChunkCoords()
    {
    	return new ArrayList<ChunkCoordinate>(smoothingAreasToSpawn.keySet());
    }
    
	// Adds a smoothing area around the lowest layer of blocks in all BO4's within this branching structure that have smoothRadius set to a value higher than 0.
	// The smoothing area is a collection of lines starting from the edge of the BO4 going outwards and connecting to the surrounding terrain. Each line starts 
    // from a non-air block at y0 in the BO4 that has no neighbouring (non-air) block on one of four sides (taking into account any neighbouring branches that connect 
    // seamlessly). The line is drawn starting at that block and then goes outward in the direction where no neighbouring block was found, the length of the line 
    // being the smoothRadius. Later, when the BO4 blocks and the smoothing areas are actually being spawned, the y-value of the endpoint is detected via a 
    // highestblock check in the terrain and the lines of blocks we've plotted are spawned, creating a linear slope. Smoothing areas both fill (create a slope to) 
    // lower-lying terrain, and cut away any higher-laying terrain to create a slope from the terrain to the edge of the BO4, whether it's above or below the edge of 
    // the bo4.
    // *SmoothStartTop:true can be used to make smoothing area lines start at the highest block in each column that has a no neighbouring (non-air) block on one of 
    // four sides, instead of all blocks at y 0 in the bo4.
    // *Settings like SpawnUnderWater can be used to make smoothing areas place underwater and fill with water up to biome waterlevel where necessary.
	public void calculateSmoothingAreas(Map<ChunkCoordinate, Stack<BO4CustomStructureCoordinate>> objectsToSpawn, BO4CustomStructureCoordinate start, LocalWorldGenRegion worldGenRegion)
    {
        // TODO: Don't check neighbouring BO4's with SmoothRadius -1

        // Get all solid blocks on the lowest layer of this BO4 that border an air block or have no neighbouring blocks
        // This may include blocks on the border of this BO4 that are supposed to seamlessly border another BO4, remove those later since they shouldnt be smoothed
        Map<ChunkCoordinate, ArrayList<BlockCoordsAndNeighbours>> smoothToBlocksPerChunk = new HashMap<ChunkCoordinate, ArrayList<BlockCoordsAndNeighbours>>();

        ArrayList<BlockCoordsAndNeighbours> smoothToBlocks;
        ChunkCoordinate chunkCoord;
        Stack<BO4CustomStructureCoordinate> bO3sInChunk;
        boolean bFoundNeighbour1;
        boolean bFoundNeighbour2;
        boolean bFoundNeighbour3;
        boolean bFoundNeighbour4;
        BO4CustomStructureCoordinate neighbouringBlockCoords;
        int normalizedNeigbouringBlockX;
        int normalizedNeigbouringBlockY;
        int normalizedNeigbouringBlockZ;
        BO4 bO3InChunk;
        boolean SmoothStartTop;
        int smoothRadius;
        BO4BlockFunction[][] heightMap;
        BO4BlockFunction block;
        
        int xOffset;
        int yOffset;
        int zOffset;
		int smoothHeightOffset;

        CustomStructureCoordinate blockCoords;
        Object[] smoothDirections;
        
        // Get all BO4's that are a part of this branching structure
        for(Entry<ChunkCoordinate, Stack<BO4CustomStructureCoordinate>> chunkCoordSet : objectsToSpawn.entrySet())
        {
            chunkCoord = chunkCoordSet.getKey();
            bO3sInChunk = chunkCoordSet.getValue();
            smoothToBlocks = new ArrayList<BlockCoordsAndNeighbours>();

            for(BO4CustomStructureCoordinate objectInChunk : bO3sInChunk)
            {
            	// Ignore any bo4's that are overridden via ReplacesBO4
            	if(objectInChunk.isSpawned)
            	{
            		continue;
            	}

            	bO3InChunk = ((BO4)objectInChunk.getObject());
            	SmoothStartTop = ((BO4)start.getObject()).getConfig().overrideChildSettings && bO3InChunk.getConfig().overrideChildSettings ? ((BO4)start.getObject()).getConfig().smoothStartTop : bO3InChunk.getConfig().smoothStartTop;
                smoothRadius = ((BO4)start.getObject()).getConfig().overrideChildSettings && bO3InChunk.getConfig().overrideChildSettings && bO3InChunk.getConfig().smoothRadius == -1 ? -1 : ((BO4)start.getObject()).getConfig().smoothRadius;
                if(smoothRadius == -1 || bO3InChunk.getConfig().smoothRadius == -1)
                {
                	smoothRadius = 0;
                }
                if(smoothRadius > 0)
                {
        			heightMap = bO3InChunk.getConfig().getSmoothingHeightMap((BO4)start.getObject());

                    // if !SmoothStartTop then for each BO3 that has a smoothradius > 0 get the lowest layer of blocks and determine smooth area starting points
                	// if SmoothStartTop then for each BO3 that has a smoothradius > 0 get the highest blocks of the BO4 and determine smooth area starting points

                	for(int x = 0; x <= 15; x ++)
                	{
                		for(int z = 0; z <= 15; z ++)
                		{
                			block = heightMap[x][z];

                			if(block != null)
                			{
                                bFoundNeighbour1 = false;
                                bFoundNeighbour2 = false;
                                bFoundNeighbour3 = false;
                                bFoundNeighbour4 = false;

                                // Check if any neighbouring blocks are air or non-existent within this BO3
                                if(block.x - 1 >= 0 && heightMap[block.x - 1][block.z] != null)
                                {
                                    bFoundNeighbour1 = true;
                                }
                                if(block.x + 1 <= 15 && heightMap[block.x + 1][block.z] != null)
                                {
                                    bFoundNeighbour2 = true;
                                }
                                if(block.z - 1 >= 0 && heightMap[block.x][block.z - 1] != null)
                                {
                                    bFoundNeighbour3 = true;
                                }
                                if(block.z + 1 <= 15 && heightMap[block.x][block.z + 1] != null)
                                {
                                    bFoundNeighbour4 = true;
                                }

                                // If one of the neighbouring blocks has not been found in this BO4 and the block is on the edge of the BO3
                                // then check for other BO4's that may have blocks that border this block.
                                // If a solid neighbouring block is found then don't smooth in that direction.

                                if(!bFoundNeighbour1 && block.x - 1 < 0)
                                {
                                    // Check if the BO4 contains a block at the location of the neighbouring block
                                    // Normalize the coordinates of the neighbouring block taking into consideration rotation
                                    neighbouringBlockCoords = BO4CustomStructureCoordinate.getRotatedSmoothingCoords(block.x - 1, block.y, block.z, objectInChunk.getRotation());
                                    normalizedNeigbouringBlockX = neighbouringBlockCoords.getX() + (objectInChunk.getX());
                                    normalizedNeigbouringBlockY = neighbouringBlockCoords.getY() + objectInChunk.getY();
                                    normalizedNeigbouringBlockZ = neighbouringBlockCoords.getZ() + (objectInChunk.getZ());

                                    bFoundNeighbour1 = findNeighbouringBlock(SmoothStartTop, normalizedNeigbouringBlockX, normalizedNeigbouringBlockY, normalizedNeigbouringBlockZ, objectsToSpawn, objectInChunk, start);
                                }
                                if(!bFoundNeighbour2 && block.x + 1 > 15)
                                {
                                    // Check if the BO4 contains a block at the location of the neighbouring block
                                    //Normalize the coordinates of the neigbouring block taking into consideration rotation
                                    neighbouringBlockCoords = BO4CustomStructureCoordinate.getRotatedSmoothingCoords(block.x + 1, block.y, block.z, objectInChunk.getRotation());
                                    normalizedNeigbouringBlockX = neighbouringBlockCoords.getX() + (objectInChunk.getX());
                                    normalizedNeigbouringBlockY = neighbouringBlockCoords.getY() + objectInChunk.getY();
                                    normalizedNeigbouringBlockZ = neighbouringBlockCoords.getZ() + (objectInChunk.getZ());

                                    bFoundNeighbour2 = findNeighbouringBlock(SmoothStartTop, normalizedNeigbouringBlockX, normalizedNeigbouringBlockY, normalizedNeigbouringBlockZ, objectsToSpawn, objectInChunk, start);
                                }
                                if(!bFoundNeighbour3 && block.z - 1 < 0)
                                {
                                    // Check if the BO4 contains a block at the location of the neighbouring block
                                    //Normalize the coordinates of the neigbouring block taking into consideration rotation
                                    neighbouringBlockCoords = BO4CustomStructureCoordinate.getRotatedSmoothingCoords(block.x, block.y, block.z - 1, objectInChunk.getRotation());
                                    normalizedNeigbouringBlockX = neighbouringBlockCoords.getX() + (objectInChunk.getX());
                                    normalizedNeigbouringBlockY = neighbouringBlockCoords.getY() + objectInChunk.getY();
                                    normalizedNeigbouringBlockZ = neighbouringBlockCoords.getZ() + (objectInChunk.getZ());

                                    bFoundNeighbour3 = findNeighbouringBlock(SmoothStartTop, normalizedNeigbouringBlockX, normalizedNeigbouringBlockY, normalizedNeigbouringBlockZ, objectsToSpawn, objectInChunk, start);
                                }
                                if(!bFoundNeighbour4 && block.z + 1 > 15)
                                {
                                    // Check if the BO4 contains a block at the location of the neighbouring block
                                    // Normalize the coordinates of the neighbouring block taking into consideration rotation
                                    neighbouringBlockCoords = BO4CustomStructureCoordinate.getRotatedSmoothingCoords(block.x, (short)block.y, block.z + 1, objectInChunk.getRotation());
                                    normalizedNeigbouringBlockX = neighbouringBlockCoords.getX() + (objectInChunk.getX());
                                    normalizedNeigbouringBlockY = neighbouringBlockCoords.getY() + objectInChunk.getY();
                                    normalizedNeigbouringBlockZ = neighbouringBlockCoords.getZ() + (objectInChunk.getZ());

                                    bFoundNeighbour4 = findNeighbouringBlock(SmoothStartTop, normalizedNeigbouringBlockX, normalizedNeigbouringBlockY, normalizedNeigbouringBlockZ, objectsToSpawn, objectInChunk, start);
                                }

                                // Only blocks that have air blocks or no blocks as neighbours should be part of the smoothing area
                                if(!bFoundNeighbour1 || !bFoundNeighbour2 || !bFoundNeighbour3 || !bFoundNeighbour4)
                                {
                                    // The first block of the smoothing area is placed at a 1 block offset in the direction of the smoothing area 
                                	// so that it is not directly underneath or above the origin block for outside corner blocks (blocks with no 
                                	// neighbouring block on 2 adjacent sides) that means they will be placed at x AND z offsets of plus or minus one.
                                    xOffset = 0;
                                    yOffset = 0;
                                    zOffset = 0;

                            		smoothHeightOffset = ((BO4)start.getObject()).getConfig().overrideChildSettings && bO3InChunk.getConfig().overrideChildSettings ? ((BO4)start.getObject()).getConfig().smoothHeightOffset : bO3InChunk.getConfig().smoothHeightOffset;
                                	yOffset += smoothHeightOffset;
                                	
                                    // Find smooth end point and normalize coord
                                    // Add each chunk between the smooth-beginning and end points to a list along with the line-segment information (startcoords in chunk, 
                                   	// endcoords in chunk, originCoords, finaldestinationCoords). Later when a chunk is being spawned the list is consulted in order to 
                                   	// merge all smoothing lines into 1 smoothing area for the chunk.
                                    // Note: We can only find x and z coordinates for the smoothing lines at this point. In order to find the Y endpoint for a smoothing 
                                	// line we need to do a highestblock check, potentially in an unloaded chunk. This problem is handled during spawning, y endpoints are 
                                	// are calculated on-demand, so only when a smoothing line segment in a chunk being spawned requires a y-endpoint.
                                    
                                    if(!bFoundNeighbour1)
                                    {
                                        xOffset = -1;
                                        blockCoords = BO4CustomStructureCoordinate.getRotatedSmoothingCoords(block.x + xOffset, (short)(block.y + yOffset), block.z, objectInChunk.getRotation());
                                        smoothDirections = rotateSmoothDirections(true, false, false, false, objectInChunk.getRotation());                                                                               
                                        smoothToBlocks.add(new BlockCoordsAndNeighbours(objectInChunk, blockCoords.getX(), blockCoords.getY(), blockCoords.getZ(), (Boolean)smoothDirections[0], (Boolean)smoothDirections[1], (Boolean)smoothDirections[2], (Boolean)smoothDirections[3]));                                       

                                        if(!bFoundNeighbour3)
                                        {
                                            zOffset = -1;
                                            blockCoords = BO4CustomStructureCoordinate.getRotatedSmoothingCoords(block.x + xOffset, (short)(block.y + yOffset), block.z + zOffset, objectInChunk.getRotation());
                                            smoothDirections = rotateSmoothDirections(true, false, true, false, objectInChunk.getRotation());
                                            smoothToBlocks.add(new BlockCoordsAndNeighbours(objectInChunk, blockCoords.getX(), blockCoords.getY(), blockCoords.getZ(), (Boolean)smoothDirections[0], (Boolean)smoothDirections[1], (Boolean)smoothDirections[2], (Boolean)smoothDirections[3]));
                                        }
                                        if(!bFoundNeighbour4)
                                        {
                                            zOffset = 1;
                                            blockCoords = BO4CustomStructureCoordinate.getRotatedSmoothingCoords(block.x + xOffset, (short)(block.y + yOffset), block.z + zOffset, objectInChunk.getRotation());
                                            smoothDirections = rotateSmoothDirections(true, false, false, true, objectInChunk.getRotation());
                                            smoothToBlocks.add(new BlockCoordsAndNeighbours(objectInChunk, blockCoords.getX(), blockCoords.getY(), blockCoords.getZ(), (Boolean)smoothDirections[0], (Boolean)smoothDirections[1], (Boolean)smoothDirections[2], (Boolean)smoothDirections[3]));
                                        }
                                    }

                                    if(!bFoundNeighbour2)
                                    {
                                        xOffset = 1;
                                        blockCoords = BO4CustomStructureCoordinate.getRotatedSmoothingCoords(block.x + xOffset, (short)(block.y + yOffset), block.z, objectInChunk.getRotation());
                                        smoothDirections = rotateSmoothDirections(false, true, false, false, objectInChunk.getRotation());
                                        smoothToBlocks.add(new BlockCoordsAndNeighbours(objectInChunk, blockCoords.getX(), blockCoords.getY(), blockCoords.getZ(), (Boolean)smoothDirections[0], (Boolean)smoothDirections[1], (Boolean)smoothDirections[2], (Boolean)smoothDirections[3]));

                                        if(!bFoundNeighbour3)
                                        {
                                            zOffset = -1;
                                            blockCoords = BO4CustomStructureCoordinate.getRotatedSmoothingCoords(block.x + xOffset, (short)(block.y + yOffset), block.z + zOffset, objectInChunk.getRotation());
                                            smoothDirections = rotateSmoothDirections(false, true, true, false, objectInChunk.getRotation());
                                            smoothToBlocks.add(new BlockCoordsAndNeighbours(objectInChunk, blockCoords.getX(), blockCoords.getY(), blockCoords.getZ(), (Boolean)smoothDirections[0], (Boolean)smoothDirections[1], (Boolean)smoothDirections[2], (Boolean)smoothDirections[3]));
                                        }
                                        if(!bFoundNeighbour4)
                                        {
                                            zOffset = 1;
                                            blockCoords = BO4CustomStructureCoordinate.getRotatedSmoothingCoords(block.x + xOffset, (short)(block.y + yOffset), block.z + zOffset, objectInChunk.getRotation());
                                            smoothDirections = rotateSmoothDirections(false, true, false, true, objectInChunk.getRotation());
                                            smoothToBlocks.add(new BlockCoordsAndNeighbours(objectInChunk, blockCoords.getX(), blockCoords.getY(), blockCoords.getZ(), (Boolean)smoothDirections[0], (Boolean)smoothDirections[1], (Boolean)smoothDirections[2], (Boolean)smoothDirections[3]));
                                        }
                                    }

                                    if(!bFoundNeighbour3)
                                    {
                                        zOffset = -1;
                                        blockCoords = BO4CustomStructureCoordinate.getRotatedSmoothingCoords(block.x, (short)(block.y + yOffset), block.z + zOffset, objectInChunk.getRotation());
                                        smoothDirections = rotateSmoothDirections(false, false, true, false, objectInChunk.getRotation());
                                        smoothToBlocks.add(new BlockCoordsAndNeighbours(objectInChunk, blockCoords.getX(), blockCoords.getY(), blockCoords.getZ(), (Boolean)smoothDirections[0], (Boolean)smoothDirections[1], (Boolean)smoothDirections[2], (Boolean)smoothDirections[3]));
                                    }
                                    
                                    if(!bFoundNeighbour4)
                                    {
                                        zOffset = 1;
                                        blockCoords = BO4CustomStructureCoordinate.getRotatedSmoothingCoords(block.x, (short)(block.y + yOffset), block.z + zOffset, objectInChunk.getRotation());
                                        smoothDirections = rotateSmoothDirections(false, false, false, true, objectInChunk.getRotation());
                                        smoothToBlocks.add(new BlockCoordsAndNeighbours(objectInChunk, blockCoords.getX(), blockCoords.getY(), blockCoords.getZ(), (Boolean)smoothDirections[0], (Boolean)smoothDirections[1], (Boolean)smoothDirections[2], (Boolean)smoothDirections[3]));
                                    }
                                }
                			}
                		}
            		}
                }
            }

            if(!smoothToBlocksPerChunk.containsKey(chunkCoord))
            {
            	smoothToBlocksPerChunk.put(chunkCoord, smoothToBlocks);
            } else {
                // only happens in chunks that have horizontal/vertical lines as well as diagonal ones
            	smoothToBlocksPerChunk.get(chunkCoord).addAll(smoothToBlocks);
            }
        }

        calculateBeginAndEndPointsPerChunk(smoothToBlocksPerChunk, ((BO4)start.getObject()).getConfig().smoothRadius - 1);
    }
	
	// Rotates neighbouring block info for a block
	private Object[] rotateSmoothDirections(Boolean smoothDirection1, Boolean smoothDirection2, Boolean smoothDirection3, Boolean smoothDirection4, Rotation rotation)
    {
    	// smoothDirection1 -1x WEST
    	// smoothDirection2 +1x EAST
    	// smoothDirection3 -1z NORTH
    	// smoothDirection4 +1z SOUTH
		if(rotation == Rotation.NORTH)
		{
			return new Object[] { smoothDirection1, smoothDirection2, smoothDirection3, smoothDirection4 };
		}
		else if(rotation == Rotation.EAST)
		{
			return new Object[] { smoothDirection4, smoothDirection3, smoothDirection1, smoothDirection2 };
		}
		else if(rotation == Rotation.SOUTH)
		{
			return new Object[] { smoothDirection2, smoothDirection1, smoothDirection4, smoothDirection3 };
		} else {
			return new Object[] { smoothDirection3, smoothDirection4, smoothDirection2, smoothDirection1 };
		}
    }	
	
	// Checks if the block has any neighbouring blocks, if not it's a smoothing line start point	

	private boolean findNeighbouringBlock(boolean SmoothStartTop, int normalizedNeigbouringBlockX, int normalizedNeigbouringBlockY, int normalizedNeigbouringBlockZ, Map<ChunkCoordinate, Stack<BO4CustomStructureCoordinate>> objectsToSpawn, BO4CustomStructureCoordinate objectInChunk, BO4CustomStructureCoordinate start)
	{
        // Get the chunk that the neighbouring block is in
        ChunkCoordinate neighbouringBlockChunk = null;
        ChunkCoordinate searchTarget = ChunkCoordinate.fromBlockCoords(normalizedNeigbouringBlockX, normalizedNeigbouringBlockZ);
        for(ChunkCoordinate chunkInStructure : objectsToSpawn.keySet())
        {
            // Find the chunk that contains the coordinates being looked for
            if(chunkInStructure.getChunkX() == searchTarget.getChunkX() && chunkInStructure.getChunkZ() == searchTarget.getChunkZ())
            {
                neighbouringBlockChunk = chunkInStructure;
                break;
            }
        }
        if(neighbouringBlockChunk != null)
        {
            //found the neighbouring chunk
        	Stack<BO4CustomStructureCoordinate> bO3sInNeighbouringBlockChunk = objectsToSpawn.get(neighbouringBlockChunk);        	
            if(bO3sInNeighbouringBlockChunk != null)
            {
            	BO4CustomStructureCoordinate blockToCheckCoords;
                int normalizedBlockToCheckX;
                int normalizedBlockToCheckY;
                int normalizedBlockToCheckZ;
                BO4BlockFunction blockToCheck;
                BO4BlockFunction[][] neighbouringBO3HeightMap;
                for(CustomStructureCoordinate bO3ToCheck : bO3sInNeighbouringBlockChunk)
                {
                    if(bO3ToCheck != objectInChunk)
                    {
                        // Now find the actual block
                    	neighbouringBO3HeightMap = ((BO4)bO3ToCheck.getObject()).getConfig().getSmoothingHeightMap((BO4)start.getObject());

                    	for(int x = 0; x < 16; x++)
                    	{
                        	for(int z = 0; z < 16; z++)
                        	{
	                    		blockToCheck = neighbouringBO3HeightMap[x][z];
	                    		if(blockToCheck != null)
	                    		{
		                            blockToCheckCoords = BO4CustomStructureCoordinate.getRotatedSmoothingCoords(blockToCheck.x, (short)blockToCheck.y, blockToCheck.z, bO3ToCheck.getRotation());
		                            normalizedBlockToCheckX = blockToCheckCoords.getX() + (bO3ToCheck.getX());
		                            normalizedBlockToCheckY = blockToCheckCoords.getY() + bO3ToCheck.getY();
		                            normalizedBlockToCheckZ = blockToCheckCoords.getZ() + (bO3ToCheck.getZ());
		
		                            if(normalizedNeigbouringBlockX == normalizedBlockToCheckX && (normalizedNeigbouringBlockY == normalizedBlockToCheckY || SmoothStartTop) && normalizedNeigbouringBlockZ == normalizedBlockToCheckZ)
		                            {
		                                // Neighbouring block found
		                            	if(isMaterialSmoothingAnchor(blockToCheck, bO3ToCheck, start))
		                                {
		                                    return true;
		                                }
		                            }
	                    		}
                        	}
                    	}
                    }
                }
            }
        }
        return false;
	}
	
	// Checks if a given block has a material that is viable as a smoothing area line start point.
    private boolean isMaterialSmoothingAnchor(BO4BlockFunction blockToCheck, CustomStructureCoordinate bO3ToCheck, CustomStructureCoordinate start)
    {
		boolean isSmoothAreaAnchor = false;
		if(blockToCheck instanceof BO4RandomBlockFunction)
		{
			for(LocalMaterialData material : ((BO4RandomBlockFunction)blockToCheck).blocks)
			{
				// TODO: Material should never be null, fix the code in RandomBlockFunction.load() that causes this.
				if(material == null)
				{
					continue;
				}
				if(
					material.isSmoothAreaAnchor(
						((BO4)start.getObject()).getConfig().overrideChildSettings && 
						((BO4)bO3ToCheck.getObject()).getConfig().overrideChildSettings ? 
							((BO4)start.getObject()).getConfig().smoothStartWood : 
							((BO4)bO3ToCheck.getObject()).getConfig().smoothStartWood, 
						((BO4)start.getObject()).getConfig().spawnUnderWater
					)
				)
				{
					isSmoothAreaAnchor = true;
					break;
				}
			}
		}

        // Neighbouring block found
    	if(
			isSmoothAreaAnchor ||
			(
				!(
					blockToCheck instanceof BO4RandomBlockFunction
				) && 
				blockToCheck.material.isSmoothAreaAnchor(
					((BO4)start.getObject()).getConfig().overrideChildSettings && 
					((BO4)bO3ToCheck.getObject()).getConfig().overrideChildSettings ? 
						((BO4)start.getObject()).getConfig().smoothStartWood : 
						((BO4)bO3ToCheck.getObject()).getConfig().smoothStartWood, 
					((BO4)start.getObject()).getConfig().spawnUnderWater
				)
			)
		)
    	{
    		return true;
    	}
    	
    	return false;
    }	



    // Now that we have all the smoothing area start points on the edges of the BO4, calculate the xz end points (y is determined only when spawning since it requires terrain height checks). 
    // The lines we plot may traverse several chunks so divide them up into one chunk segments and make a collection of line segments per chunk.
    // For each line-segment store the beginning and endpoints within the chunk as well as the origin and final destination coordinate.
    private void calculateBeginAndEndPointsPerChunk(Map<ChunkCoordinate, ArrayList<BlockCoordsAndNeighbours>> smoothToBlocksPerChunk, int smoothRadius)
    {
        Map<ChunkCoordinate, ArrayList<SmoothingAreaLine>> smoothingAreasToSpawn = new HashMap<ChunkCoordinate, ArrayList<SmoothingAreaLine>>();
        
        // Loop through smooth-line starting blocks
        for(Entry<ChunkCoordinate, ArrayList<BlockCoordsAndNeighbours>> chunkCoordSet : smoothToBlocksPerChunk.entrySet())
        {
            for(BlockCoordsAndNeighbours blockCoordsAndNeighbours : chunkCoordSet.getValue())
            {
                // Find smooth end point and normalize coord
                // Add each chunk between the smooth-beginning and end points to a list along with the line-segment information (startcoords in chunk, 
            	// endcoords in chunk, originCoords, finaldestinationCoords). Later when a chunk is being spawned the list is consulted in order to 
            	// merge all smoothing lines into 1 smoothing area for the chunk.
                // Note: Unfortunately we can only find x and z coordinates for the smoothing lines at this point. In order to find the Y endpoint
                // for a smoothing line we need to do a height check in the landscape, which may be in an unloaded chunk.
                // This problem is handled later during spawning, y end-points requiring height-checks are calculated only on-demand when chunks are 
            	// being spawned, if a height check in unloaded chunks is required, OTG generates only base terrain for the height check (and caches the
            	// result for use later during chunk generation), without actually generating the chunk in the world.

                boolean isCornerBlock = 
            		(blockCoordsAndNeighbours.smoothInDirection1 && blockCoordsAndNeighbours.smoothInDirection3) || 
            		(blockCoordsAndNeighbours.smoothInDirection1 && blockCoordsAndNeighbours.smoothInDirection4) || 
            		(blockCoordsAndNeighbours.smoothInDirection2 && blockCoordsAndNeighbours.smoothInDirection3) || 
            		(blockCoordsAndNeighbours.smoothInDirection2 && blockCoordsAndNeighbours.smoothInDirection4)
        		;
                
                // Non-corner blocks (straight lines)
                if(!isCornerBlock && blockCoordsAndNeighbours.smoothInDirection1)
                {
                    plotStraightLine(blockCoordsAndNeighbours.blockX, blockCoordsAndNeighbours.blockY, blockCoordsAndNeighbours.blockZ, smoothRadius, smoothingAreasToSpawn, blockCoordsAndNeighbours.bO3, 1);                    
                }
                if(!isCornerBlock && blockCoordsAndNeighbours.smoothInDirection2)
                {
                    plotStraightLine(blockCoordsAndNeighbours.blockX, blockCoordsAndNeighbours.blockY, blockCoordsAndNeighbours.blockZ, smoothRadius, smoothingAreasToSpawn, blockCoordsAndNeighbours.bO3, 2);
                }
                if(!isCornerBlock && blockCoordsAndNeighbours.smoothInDirection3)
                {
                    plotStraightLine(blockCoordsAndNeighbours.blockX, blockCoordsAndNeighbours.blockY, blockCoordsAndNeighbours.blockZ, smoothRadius, smoothingAreasToSpawn, blockCoordsAndNeighbours.bO3, 3);
                }
                if(!isCornerBlock && blockCoordsAndNeighbours.smoothInDirection4)
                {
                	plotStraightLine(blockCoordsAndNeighbours.blockX, blockCoordsAndNeighbours.blockY, blockCoordsAndNeighbours.blockZ, smoothRadius, smoothingAreasToSpawn, blockCoordsAndNeighbours.bO3, 4);
                }

                // Corner blocks (angled lines)
                if(blockCoordsAndNeighbours.smoothInDirection1 && blockCoordsAndNeighbours.smoothInDirection3)
                {
                	plotCorner(blockCoordsAndNeighbours.blockX, blockCoordsAndNeighbours.blockY, blockCoordsAndNeighbours.blockZ, smoothRadius, smoothingAreasToSpawn, blockCoordsAndNeighbours.bO3, 1);
                }
                if(blockCoordsAndNeighbours.smoothInDirection1 && blockCoordsAndNeighbours.smoothInDirection4)
                {
                	plotCorner(blockCoordsAndNeighbours.blockX, blockCoordsAndNeighbours.blockY, blockCoordsAndNeighbours.blockZ, smoothRadius, smoothingAreasToSpawn, blockCoordsAndNeighbours.bO3, 2);
                }
                if(blockCoordsAndNeighbours.smoothInDirection2 && blockCoordsAndNeighbours.smoothInDirection3)
                {
                	plotCorner(blockCoordsAndNeighbours.blockX, blockCoordsAndNeighbours.blockY, blockCoordsAndNeighbours.blockZ, smoothRadius, smoothingAreasToSpawn, blockCoordsAndNeighbours.bO3, 3);
                }
                if(blockCoordsAndNeighbours.smoothInDirection2 && blockCoordsAndNeighbours.smoothInDirection4)
                {
                	plotCorner(blockCoordsAndNeighbours.blockX, blockCoordsAndNeighbours.blockY, blockCoordsAndNeighbours.blockZ, smoothRadius, smoothingAreasToSpawn, blockCoordsAndNeighbours.bO3, 4);
                }
            }
        }

		fillSmoothingLineCaches(smoothingAreasToSpawn);
    }

    void plotStraightLine(int blockX, short blockY, int blockZ, int smoothRadius, Map<ChunkCoordinate, ArrayList<SmoothingAreaLine>> smoothingAreasToSpawn, BO4CustomStructureCoordinate bO4, int direction)
    {
    	int normalizedSmoothStartPointBlockX = 0;
    	int normalizedSmoothStartPointBlockZ = 0;
    	int normalizedSmoothFinalEndPointBlockX = 0;
    	int normalizedSmoothFinalEndPointBlockZ = 0;    	
    	ChunkCoordinate destinationChunk;      
        boolean bFound;
        ArrayList<ChunkCoordinate> smoothingAreasToSpawnForThisBlock = new ArrayList<ChunkCoordinate>();
        int beginPointX;
        short beginPointY;
        int beginPointZ;  
        int endPointX;
        int endPointZ;
        SmoothingAreaLine objectToAdd;
        ArrayList<SmoothingAreaLine> beginningAndEndpoints;
        ArrayList<SmoothingAreaLine> beginAndEndPoints;
        
        if(direction == 1)
        {
        	normalizedSmoothFinalEndPointBlockX = blockX - smoothRadius + (bO4.getX());
        	normalizedSmoothFinalEndPointBlockZ = blockZ + (bO4.getZ());
        }
        if(direction == 2)
        {
        	normalizedSmoothFinalEndPointBlockX = blockX + smoothRadius + (bO4.getX());
        	normalizedSmoothFinalEndPointBlockZ = blockZ + (bO4.getZ());
        }
        if(direction == 3)
        {
        	normalizedSmoothFinalEndPointBlockX = blockX + (bO4.getX());
        	normalizedSmoothFinalEndPointBlockZ = blockZ - smoothRadius + (bO4.getZ());
        }
    	if(direction == 4)
    	{
    		normalizedSmoothFinalEndPointBlockX = blockX + (bO4.getX());
    		normalizedSmoothFinalEndPointBlockZ = blockZ + smoothRadius + (bO4.getZ());	
    	}
        ChunkCoordinate finalDestinationChunk = ChunkCoordinate.fromBlockCoords(normalizedSmoothFinalEndPointBlockX, normalizedSmoothFinalEndPointBlockZ);       
        
        // First get all chunks between the beginning- and end-points
        for(int i = 0; i <= smoothRadius; i++)
        {        	
            if(direction == 1)
            {
            	normalizedSmoothStartPointBlockX = blockX - i + (bO4.getX());
                normalizedSmoothStartPointBlockZ = blockZ + (bO4.getZ());
            }
            if(direction == 2)
            {
            	normalizedSmoothStartPointBlockX = blockX + i + (bO4.getX());
                normalizedSmoothStartPointBlockZ = blockZ + (bO4.getZ());
            }        	
        	if(direction == 3)
        	{
            	normalizedSmoothStartPointBlockX = blockX + (bO4.getX());
                normalizedSmoothStartPointBlockZ = blockZ - i + (bO4.getZ());
        	}
        	if(direction == 4)
        	{
        		normalizedSmoothStartPointBlockX = blockX + (bO4.getX());
        		normalizedSmoothStartPointBlockZ = blockZ + i + (bO4.getZ());
        	}
            destinationChunk = ChunkCoordinate.fromBlockCoords(normalizedSmoothStartPointBlockX, normalizedSmoothStartPointBlockZ);

            // Check if we havent handled this chunk yet for the current line
            bFound = false;
            for(ChunkCoordinate cCoord : smoothingAreasToSpawnForThisBlock)
            {
                if(destinationChunk.equals(cCoord))
                {
                    bFound = true;
                    break;
                }
            }
            if(!bFound)
            {
                // Get the coordinates of the beginning and endpoint relative to the chunk's coordinates
                beginPointX = normalizedSmoothStartPointBlockX;
                beginPointY = (short) (blockY + bO4.getY());
                beginPointZ = normalizedSmoothStartPointBlockZ;
                
                endPointX = normalizedSmoothStartPointBlockX;
                endPointZ = normalizedSmoothStartPointBlockZ;

                if(!finalDestinationChunk.equals(destinationChunk))
                {
                	if(direction == 1)
                	{
                        // The smoothing area expands beyond this chunk so put the endpoint at the chunk border (0 because we're moving in the - direction)
                        endPointX = destinationChunk.getChunkX() * 16;
                	}
                	if(direction == 2)
                	{
                        // The smoothing area expands beyond this chunk so put the endpoint at the chunk border (15 because we're moving in the + direction)
                        endPointX = destinationChunk.getChunkX() * 16 + 15;
                	}
                	if(direction == 3)
                	{
                        // The smoothing area expands beyond this chunk so put the endpoint at the chunk border (0 because we're moving in the - direction)
                        endPointZ = destinationChunk.getChunkZ() * 16;
                	}
                	if(direction == 4)
                	{
                        // The smoothing area expands beyond this chunk so put the endpoint at the chunk border (15 because we're moving in the + direction)
                        endPointZ = destinationChunk.getChunkZ() * 16 + 15;
                	}                	
                } else {
                    // Get the endpoint by adding the remaining smoothRadius
                    if(direction == 1)
                    {
                        endPointX = normalizedSmoothStartPointBlockX -= (smoothRadius - i);
                    }
                    if(direction == 2)
                    {
                    	endPointX = normalizedSmoothStartPointBlockX += (smoothRadius - i);
                    }                	
                	if(direction == 3)
                	{
                		endPointZ = normalizedSmoothStartPointBlockZ -= (smoothRadius - i);
                	}
                	if(direction == 4)
                	{
                		endPointZ = normalizedSmoothStartPointBlockZ += (smoothRadius - i);
                	}
                }

               	objectToAdd = new SmoothingAreaLine(beginPointX, beginPointZ, endPointX, endPointZ, blockX + (bO4.getX()), beginPointY, blockZ + (bO4.getZ()), normalizedSmoothFinalEndPointBlockX, normalizedSmoothFinalEndPointBlockZ);

                // Check if there are already start and endpoints for this chunk
            	beginAndEndPoints = smoothingAreasToSpawn.get(destinationChunk);
            	if(beginAndEndPoints != null)
            	{
                    beginAndEndPoints.add(objectToAdd);
            	} else {
                    beginningAndEndpoints = new ArrayList<SmoothingAreaLine> ();
                	beginningAndEndpoints.add(objectToAdd);
                    smoothingAreasToSpawn.put(destinationChunk, beginningAndEndpoints);
                }
                smoothingAreasToSpawnForThisBlock.add(destinationChunk);
            }
        }
    }
    
    // Plots a quarter-circle of blocks for each smoothing area corner block, in order to create rounded corners around the structure.
    // For each corner, creates smoothing lines fanning out from the center of the circle to fill the entire quarter-circle. 
    void plotCorner(int blockX, int blockY, int blockZ, int smoothRadius, Map<ChunkCoordinate, ArrayList<SmoothingAreaLine>> smoothingAreasToSpawn, BO4CustomStructureCoordinate bO4, int direction)
    {    	
    	int originPointX = blockX + bO4.getX();
    	short originPointY = (short)(blockY + bO4.getY());
    	int originPointZ = blockZ + bO4.getZ();

    	// Plot a quarter circle
    	int minAngle = 0;
    	int maxAngle = 0;
    	if(direction == 1)
    	{
        	minAngle = 180;
        	maxAngle = 270;
    	}
    	if(direction == 2)
    	{
        	minAngle = 90;
        	maxAngle = 180;
    	}
    	if(direction == 3)
    	{
        	minAngle = 270;
        	maxAngle = 360;
    	}
    	if(direction == 4)
    	{
        	minAngle = 0;
        	maxAngle = 90;
    	}
    	    	
    	// Place one line per block on the circle's edge.
    	double angleChangePerBlock = 360 / (2 * Math.PI * smoothRadius);

    	int destinationPointX = 0;
    	int destinationPointZ = 0;
    	int previousDestinationPointX = 0;
    	int previousDestinationPointZ = 0;
    	
        for(double angle = minAngle; (int)Math.floor(angle) <= maxAngle; angle += angleChangePerBlock)
        {
        	destinationPointX = originPointX + (int)Math.round(smoothRadius * Math.cos(angle * Math.PI / 180.0d));
        	destinationPointZ = originPointZ + (int)Math.round(smoothRadius * Math.sin(angle * Math.PI / 180.0d));

        	// The way we're drawing a circle (one line for each block on the edge of the circle), our pattern
        	// will leave holes. We need to fill up any diagonal gaps in our line.
        	if(angle != minAngle)
        	{
       			if(
   					previousDestinationPointX != destinationPointX &&
   					previousDestinationPointZ != destinationPointZ
				)
       			{
            		if(direction == 1)
            		{
            			plotLineAtAngle(originPointX, originPointY, originPointZ, destinationPointX, destinationPointZ + 1, smoothingAreasToSpawn);
            		}
            		if(direction == 2)
            		{
            			plotLineAtAngle(originPointX, originPointY, originPointZ, destinationPointX + 1, destinationPointZ, smoothingAreasToSpawn);
            		}
            		if(direction == 3)
            		{
            			plotLineAtAngle(originPointX, originPointY, originPointZ, destinationPointX - 1, destinationPointZ, smoothingAreasToSpawn);
            		}
            		if(direction == 4)
            		{
            			plotLineAtAngle(originPointX, originPointY, originPointZ, destinationPointX, destinationPointZ - 1, smoothingAreasToSpawn);
            		}
       			}
        	}
        	previousDestinationPointX = destinationPointX;
        	previousDestinationPointZ = destinationPointZ;
        	
        	plotLineAtAngle(originPointX, originPointY, originPointZ, destinationPointX, destinationPointZ, smoothingAreasToSpawn);
        }
    }
    
    private void plotLineAtAngle(int originPointX, short originPointY, int originPointZ, int destinationPointX, int destinationPointZ, Map<ChunkCoordinate, ArrayList<SmoothingAreaLine>> smoothingAreasToSpawn)
    {
    	ChunkCoordinate currentChunk;
    	ChunkCoordinate previousChunk = null;
    	int z;
        ArrayList<SmoothingAreaLine> beginningAndEndpoints;
        ArrayList<SmoothingAreaLine> beginAndEndPoints;
        SmoothingAreaLine objectToAdd;
        int previousX = 0;
        int previousZ = 0;
        int beginPointX = originPointX;
        int beginPointZ = originPointZ;
    	
    	// Traverse each diagonal line, when it finishes or crosses into a new chunk, save a line section.
        int deltaX = Math.abs(destinationPointX - beginPointX);
        int deltaZ = Math.abs(destinationPointZ - beginPointZ);

        // http://rosettacode.org/wiki/Bitmap/Bresenham%27s_line_algorithm#Java
        // delta of exact value and rounded value of the dependent variable
        int d = 0;
        int dx2 = 2 * deltaX; // slope scaling factors to
        int dz2 = 2 * deltaZ; // avoid floating point

        int ix = beginPointX < destinationPointX ? 1 : -1; // increment direction
        int iz = beginPointZ < destinationPointZ ? 1 : -1;

        int x = beginPointX;
        z = beginPointZ;

        if (deltaX >= deltaZ)
        {
            while (true)
            {            	
            	// If the line has fully crossed a chunk or ends in this chunk, save a line-segment
            	currentChunk = ChunkCoordinate.fromBlockCoords(x, z);
            	if(
        			(previousChunk != null && !previousChunk.equals(currentChunk)) ||
        			(x == destinationPointX && z == destinationPointZ)
    			)
            	{
                	beginAndEndPoints = null;
                	objectToAdd = null;
                	if(previousChunk != null && !previousChunk.equals(currentChunk))
                	{
                		objectToAdd = new SmoothingAreaLine(beginPointX, beginPointZ, previousX, previousZ, originPointX, originPointY, originPointZ, destinationPointX, destinationPointZ);
                        beginPointX = x;
                        beginPointZ = z;
                        
                        // Check if there are already start and endpoints for this chunk
                        beginAndEndPoints = smoothingAreasToSpawn.get(previousChunk);	                	
	                    if(beginAndEndPoints != null)
	                    {
	                    	beginAndEndPoints.add(objectToAdd);
	                    } else {
	                        beginningAndEndpoints = new ArrayList<SmoothingAreaLine>();
	                        beginningAndEndpoints.add(objectToAdd);                    	
	                        smoothingAreasToSpawn.put(previousChunk, beginningAndEndpoints);
	                    }                         
                	}
                	if(x == destinationPointX && z == destinationPointZ)
                	{
                		objectToAdd = new SmoothingAreaLine(beginPointX, beginPointZ, x, z, originPointX, originPointY, originPointZ, destinationPointX, destinationPointZ);

                		// Check if there are already start and endpoints for this chunk
                        beginAndEndPoints = smoothingAreasToSpawn.get(currentChunk);	                	
	                    if(beginAndEndPoints != null)
	                    {
	                    	beginAndEndPoints.add(objectToAdd);
	                    } else {
	                        beginningAndEndpoints = new ArrayList<SmoothingAreaLine>();
	                        beginningAndEndpoints.add(objectToAdd);                    	
	                        smoothingAreasToSpawn.put(currentChunk, beginningAndEndpoints);
	                    }                		
                	}
            	}

            	previousChunk = ChunkCoordinate.fromBlockCoords(x, z);
            	previousX = x;
            	previousZ = z;
            	//

                if (x == destinationPointX)
                {
                    break;
                }
                x += ix;
                d += dz2;
                if (d > deltaX)
                {
                    z += iz;
                    d -= dx2;
                }
            }
        } else {
            while (true)
            {
            	// If the line has fully crossed a chunk or ends in this chunk, save a line-segment
            	currentChunk = ChunkCoordinate.fromBlockCoords(x, z);
            	if(
        			(previousChunk != null && !previousChunk.equals(currentChunk)) ||
        			(x == destinationPointX && z == destinationPointZ)
    			)
            	{
                	beginAndEndPoints = null;
                	objectToAdd = null;
                	if(previousChunk != null && !previousChunk.equals(currentChunk))
                	{
                		objectToAdd = new SmoothingAreaLine(beginPointX, beginPointZ, previousX, previousZ, originPointX, originPointY, originPointZ, destinationPointX, destinationPointZ);
                        beginPointX = x;
                        beginPointZ = z;
                        // Check if there are already start and endpoints for this chunk
                        beginAndEndPoints = smoothingAreasToSpawn.get(previousChunk);	                	
	                    if(beginAndEndPoints != null)
	                    {
	                    	beginAndEndPoints.add(objectToAdd);
	                    } else {
	                        beginningAndEndpoints = new ArrayList<SmoothingAreaLine>();
	                        beginningAndEndpoints.add(objectToAdd);                    	
	                        smoothingAreasToSpawn.put(previousChunk, beginningAndEndpoints);
	                    }                         
                	}
                	if(x == destinationPointX && z == destinationPointZ)
                	{
                		objectToAdd = new SmoothingAreaLine(beginPointX, beginPointZ, x, z, originPointX, originPointY, originPointZ, destinationPointX, destinationPointZ);
                        // Check if there are already start and endpoints for this chunk
                        beginAndEndPoints = smoothingAreasToSpawn.get(currentChunk);	                	
	                    if(beginAndEndPoints != null)
	                    {
	                    	beginAndEndPoints.add(objectToAdd);
	                    } else {
	                        beginningAndEndpoints = new ArrayList<SmoothingAreaLine>();
	                        beginningAndEndpoints.add(objectToAdd);                    	
	                        smoothingAreasToSpawn.put(currentChunk, beginningAndEndpoints);
	                    }                		
                	}
            	}

            	previousChunk = ChunkCoordinate.fromBlockCoords(x, z);
            	previousX = x;
            	previousZ = z;
            	//

                if (z == destinationPointZ)
                {
                    break;
                }
                z += iz;
                d += dx2;
                if (d > deltaZ)
                {
                    x += ix;
                    d -= dz2;
                }
            }
        }
    }
    
    // Fill smoothingAreasToSpawn to store plotted smoothing areas until they're spawned, 
    // fill smoothingAreasToSpawnPerLineDestination so we can update all y endpoints at once after doing a terrain height check.
    
    public void fillSmoothingLineCaches(Map<ChunkCoordinate, ArrayList<SmoothingAreaLine>> smoothingAreasToSpawn)
	{    	
    	this.smoothingAreasToSpawn.clear();
    	this.smoothingAreasToSpawnPerLineDestination.clear();
    	
		ChunkCoordinate destinationCoords;
		ArrayList<SmoothingAreaLine> smoothingAreaLines;
        ArrayList<SmoothingAreaLine> smoothingAreasAtEndpoint;
		for(Entry<ChunkCoordinate, ArrayList<SmoothingAreaLine>> smoothingArea : smoothingAreasToSpawn.entrySet())
		{
			smoothingAreaLines = smoothingArea.getValue();
			for(SmoothingAreaLine smoothingAreaLine : smoothingAreaLines)
			{
				destinationCoords = ChunkCoordinate.fromChunkCoords(smoothingAreaLine.finalDestinationPointX, smoothingAreaLine.finalDestinationPointZ);
				smoothingAreasAtEndpoint = smoothingAreasToSpawnPerLineDestination.get(destinationCoords);				
				if(smoothingAreasAtEndpoint == null)
				{
					smoothingAreasAtEndpoint = new ArrayList<SmoothingAreaLine>();
					smoothingAreasToSpawnPerLineDestination.put(destinationCoords, smoothingAreasAtEndpoint);
				}
				if(!smoothingAreasAtEndpoint.contains(smoothingAreaLine))
				{
					smoothingAreasAtEndpoint.add(smoothingAreaLine);
				}
			}
		}
		this.smoothingAreasToSpawn.putAll(smoothingAreasToSpawn);		
	}
    
    public void clearChunkFromCache(ChunkCoordinate chunkCoordinate)
    {
    	smoothingAreasToSpawn.remove(chunkCoordinate);
    }
    
	// Merges all the smoothing lines that were plotted earlier into one smoothing area per chunk and then spawns the smoothing area.
	// Returns false if a smoothing area could not be finalised and spawning has to be delayed until other chunks have spawned.
    public void spawnSmoothAreas(ChunkCoordinate chunkCoordinate, CustomStructureCoordinate start, CustomStructureCache structureCache, LocalWorldGenRegion worldGenRegion, ChunkCoordinate chunkBeingPopulated)
    {
        // Get all smoothing areas (lines) that should spawn in this chunk for this branching structure
        ArrayList<SmoothingAreaLine> smoothingAreaInChunk = smoothingAreasToSpawn.get(chunkCoordinate);
        if(smoothingAreaInChunk != null && chunkCoordinate != null)
        {
            // Merge all smooth areas (lines) so that in one x + z coordinate there can be a maximum of 2 smoothing area blocks, 1 going up and 1 going down (first pass and second pass)
        	mergeAndSpawnSmoothingAreas(chunkCoordinate, smoothingAreaInChunk, structureCache, worldGenRegion, start);

            // We'll still be using the chunks that smoothing areas
            // spawn in for chunk based collision detection so keep them
            // but empty them of blocks
            // TODO: Is this still needed?
            // smoothingAreasToSpawn.put(chunkCoordinate, null);
        }
    }

    // Merges all the smoothing lines that were plotted earlier into one smoothing area per chunk
    private void mergeAndSpawnSmoothingAreas(ChunkCoordinate chunkCoordinate, ArrayList<SmoothingAreaLine> smoothingAreas, CustomStructureCache structureCache, LocalWorldGenRegion worldGenRegion, CustomStructureCoordinate start)
    {
    	// Make sure there's only 2 blocks per column, one filling and one cutting line.
    	// The lowest block in each column is the filling line, the highest is the cutting line
    	// A cutting line is only required if the end point is above the start point.
    	
    	ChunkCoordinate destinationCoords;
    	ArrayList<SmoothingAreaLine> smoothingAreaLinesAtEndPoint;   	
    	short relativeY;
        int smoothRadius = ((BO4)start.getObject()).getConfig().smoothRadius - 1;
    	// For all smoothing lines in this chunk, determine the y coordinates
    	for(SmoothingAreaLine smoothingBeginAndEndPoints : smoothingAreas)
    	{    	    		
			// Get final destinationPoint y via height check
    		// TODO: Final destination point for child lines of a
    		// filling diagonal line should never be above their origin,
    		// cutting lines should never be below their origin
			if(smoothingBeginAndEndPoints.finalDestinationPointY == -1)
			{
				smoothingBeginAndEndPoints.finalDestinationPointY = (short)worldGenRegion.getHighestBlockYAt(
					smoothingBeginAndEndPoints.finalDestinationPointX, 
					smoothingBeginAndEndPoints.finalDestinationPointZ, 
					true, false, true, true, true, null
				);
				
				// Set the y coord for every smoothing line that uses this coord as an end point, so we don't have to do multiple height checks.
				destinationCoords = ChunkCoordinate.fromChunkCoords(smoothingBeginAndEndPoints.finalDestinationPointX, smoothingBeginAndEndPoints.finalDestinationPointZ);
				smoothingAreaLinesAtEndPoint = this.smoothingAreasToSpawnPerLineDestination.get(destinationCoords);
				if(smoothingAreaLinesAtEndPoint != null) // smoothingAreaLinesAtEndPoint can be null when Pitman deletes and re-generates region files (sigh).
				{			
					for(SmoothingAreaLine lineAtEndPoint : new ArrayList<SmoothingAreaLine>(smoothingAreaLinesAtEndPoint))
					{
						if(
							lineAtEndPoint.finalDestinationPointX == smoothingBeginAndEndPoints.finalDestinationPointX &&
							lineAtEndPoint.finalDestinationPointZ == smoothingBeginAndEndPoints.finalDestinationPointZ
						)
						{
							lineAtEndPoint.finalDestinationPointY = smoothingBeginAndEndPoints.finalDestinationPointY;

							// Make the cache self-cleaning, clear the line from the cache, we won't be needing it anymore.
							smoothingAreaLinesAtEndPoint.remove(lineAtEndPoint);
							if(smoothingAreaLinesAtEndPoint.isEmpty())
							{
								this.smoothingAreasToSpawnPerLineDestination.remove(destinationCoords);
							}
							
							// Mark the structure cache chunk region for saving (structure start region is also marked at the end of bo4 spawnForChunk)
							structureCache.markRegionForSaving(
								ChunkCoordinate.fromBlockCoords(
									lineAtEndPoint.beginPointX,
									lineAtEndPoint.beginPointZ
								).toRegionCoord()
							);
						}
					}
				}
			}
			
			// Get beginPointY based on position in line
			if(smoothingBeginAndEndPoints.beginPointY == -1)
			{
    			relativeY = (short)Math.round(
                    (double)
                    (
                        (double)Math.abs(smoothingBeginAndEndPoints.originPointY - smoothingBeginAndEndPoints.finalDestinationPointY) * 
                        (double)(	                        		
        					Point2D.distance(
    	        				smoothingBeginAndEndPoints.originPointX, 
    	        				smoothingBeginAndEndPoints.originPointZ, 
    	        				smoothingBeginAndEndPoints.beginPointX, 
    	        				smoothingBeginAndEndPoints.beginPointZ
    	    				) / (double)smoothRadius
                		)
                    )
                );
				if(smoothingBeginAndEndPoints.originPointY >= smoothingBeginAndEndPoints.finalDestinationPointY)
				{
					smoothingBeginAndEndPoints.beginPointY = (short)(smoothingBeginAndEndPoints.originPointY - relativeY); 
				} else {
					smoothingBeginAndEndPoints.beginPointY = (short)(smoothingBeginAndEndPoints.originPointY + relativeY);
				}
			}

			// Get endpointY based on position in line
			if(smoothingBeginAndEndPoints.endPointY == -1)
			{
    			relativeY = (short)Math.round(
                    (double)
                    (
                        (double)Math.abs(smoothingBeginAndEndPoints.originPointY - smoothingBeginAndEndPoints.finalDestinationPointY) * 
                        (double)(	                        		
        					Point2D.distance(
    	        				smoothingBeginAndEndPoints.originPointX, 
    	        				smoothingBeginAndEndPoints.originPointZ, 
    	        				smoothingBeginAndEndPoints.endPointX, 
    	        				smoothingBeginAndEndPoints.endPointZ
    	    				) / (double)smoothRadius
                		)
                    )
                );				
				if(smoothingBeginAndEndPoints.originPointY >= smoothingBeginAndEndPoints.finalDestinationPointY)
				{
					smoothingBeginAndEndPoints.endPointY = (short)(smoothingBeginAndEndPoints.originPointY - relativeY); 
				} else {
					smoothingBeginAndEndPoints.endPointY = (short)(smoothingBeginAndEndPoints.originPointY + relativeY);
				}				
			}
    	}
    	// For all columns that contain smoothing blocks, make sure there are 2 y coords per column:
    	// - One coord is used for the cutting line, and must be above the bo4.
    	// - One coord is used for the filling line, and must be equal to or below the height of the bo4.
    	   	
    	HashMap<ChunkCoordinate, SmoothingAreaColumn> smoothingBlocksPerColumn = new HashMap<ChunkCoordinate, SmoothingAreaColumn>();
    	for(SmoothingAreaLine smoothingBeginAndEndPoints : smoothingAreas)
    	{
    		mergeLine(smoothingBeginAndEndPoints, chunkCoordinate, smoothingBeginAndEndPoints, smoothingBlocksPerColumn, smoothRadius);    		
    	}

    	// For each column, make sure there is only one cutting line (the lowest cutting block in the column)
    	// and one filling line (the highest filling block in the column).
    	// TODO: This causes problems when multiple lines on a diferent axis target the same endpoint
    	for(Entry<ChunkCoordinate, SmoothingAreaColumn> smoothingBlocksInColumn : smoothingBlocksPerColumn.entrySet())
    	{
    		smoothingBlocksInColumn.getValue().processBlocks(worldGenRegion, chunkCoordinate, ((BO4)start.getObject()).getConfig());
    	}
    }
    
    private void mergeLine(SmoothingAreaLine smoothingAreaLine, ChunkCoordinate chunkBeingSpawned, SmoothingAreaLine smoothingBeginAndEndPoints, HashMap<ChunkCoordinate, SmoothingAreaColumn> smoothingBlocksPerColumn, int smoothRadius)
    {
    	short blockY;
    	ChunkCoordinate columnCoords;
    	SmoothingAreaColumn column;
    	short relativeY;
    	
    	int z;
    	
        int deltaX = Math.abs(smoothingAreaLine.finalDestinationPointX - smoothingAreaLine.originPointX);
        int deltaZ = Math.abs(smoothingAreaLine.finalDestinationPointZ - smoothingAreaLine.originPointZ);

        // http://rosettacode.org/wiki/Bitmap/Bresenham%27s_line_algorithm#Java
        // delta of exact value and rounded value of the dependent variable
        int d = 0;
        int dx2 = 2 * deltaX; // slope scaling factors to
        int dz2 = 2 * deltaZ; // avoid floating point

        int ix = smoothingAreaLine.originPointX < smoothingAreaLine.finalDestinationPointX ? 1 : -1; // increment direction
        int iz = smoothingAreaLine.originPointZ < smoothingAreaLine.finalDestinationPointZ ? 1 : -1;

        int x = smoothingAreaLine.originPointX;
        z = smoothingAreaLine.originPointZ;

        if (deltaX >= deltaZ)
        {
            while (true)
            {
            	//
            	ChunkCoordinate currentChunk = ChunkCoordinate.fromBlockCoords(x, z);
            	if(currentChunk.equals(chunkBeingSpawned))
            	{
	    			relativeY = (short)Math.round(
	                    (double)
	                    (
	                        (double)Math.abs(smoothingBeginAndEndPoints.originPointY - smoothingBeginAndEndPoints.finalDestinationPointY) * 
	                        (double)(	                        		
	        					Point2D.distance(
	    	        				smoothingBeginAndEndPoints.originPointX, 
	    	        				smoothingBeginAndEndPoints.originPointZ, 
	    	        				x, 
	    	        				z
	    	    				) / (double)smoothRadius
	                		)
	                    )
	                );
					if(smoothingBeginAndEndPoints.originPointY >= smoothingBeginAndEndPoints.finalDestinationPointY)
					{
						blockY = (short)(smoothingBeginAndEndPoints.originPointY - relativeY); 
					} else {
						blockY = (short)(smoothingBeginAndEndPoints.originPointY + relativeY);
					}
					
	    			columnCoords = ChunkCoordinate.fromChunkCoords(x, z);
					column = smoothingBlocksPerColumn.get(columnCoords);
					if(column == null)
					{
						column = new SmoothingAreaColumn(x, z);
						smoothingBlocksPerColumn.put(columnCoords, column);
					}
	   				column.addBlock(
						new SmoothingAreaBlock(
							x,
							(short)blockY,
							z,
							smoothingBeginAndEndPoints.originPointY >= smoothingBeginAndEndPoints.finalDestinationPointY ? enumSmoothingBlockType.FILLING : enumSmoothingBlockType.CUTTING
						)
					);
            	}
            	//
            	
                if (x == smoothingAreaLine.finalDestinationPointX)
                {
                    break;
                }
                x += ix;
                d += dz2;
                if (d > deltaX)
                {
                    z += iz;
                    d -= dx2;
                }                
            }
        } else {
            while (true)
            {
            	//
            	ChunkCoordinate currentChunk = ChunkCoordinate.fromBlockCoords(x, z);
            	if(currentChunk.equals(chunkBeingSpawned))
            	{
	    			relativeY = (short)Math.round(
	                    (double)
	                    (
	                        (double)Math.abs(smoothingBeginAndEndPoints.originPointY - smoothingBeginAndEndPoints.finalDestinationPointY) * 
	                        (double)(	                        		
	        					Point2D.distance(
	    	        				smoothingBeginAndEndPoints.originPointX, 
	    	        				smoothingBeginAndEndPoints.originPointZ, 
	    	        				x, 
	    	        				z
	    	    				) / (double)smoothRadius
	                		)
	                    )
	                );
					if(smoothingBeginAndEndPoints.originPointY >= smoothingBeginAndEndPoints.finalDestinationPointY)
					{
						blockY = (short)(smoothingBeginAndEndPoints.originPointY - relativeY); 
					} else {
						blockY = (short)(smoothingBeginAndEndPoints.originPointY + relativeY);
					}
					
	    			columnCoords = ChunkCoordinate.fromChunkCoords(x, z);
					column = smoothingBlocksPerColumn.get(columnCoords);
					if(column == null)
					{
						column = new SmoothingAreaColumn(x, z);
						smoothingBlocksPerColumn.put(columnCoords, column);
					}
	   				column.addBlock(
						new SmoothingAreaBlock(
							x,
							(short)blockY,
							z,
							smoothingBeginAndEndPoints.originPointY >= smoothingBeginAndEndPoints.finalDestinationPointY ? enumSmoothingBlockType.FILLING : enumSmoothingBlockType.CUTTING
						)
					);
            	}
            	//
   				
                if (z == smoothingAreaLine.finalDestinationPointZ)
                {
                    break;
                }
                z += iz;
                d += dx2;
                if (d > deltaZ)
                {
                    x += ix;
                    d -= dz2;
                }
            }
        }
    }
}
