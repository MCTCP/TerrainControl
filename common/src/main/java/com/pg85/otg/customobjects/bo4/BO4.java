package com.pg85.otg.customobjects.bo4;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.configuration.io.FileSettingsReaderOTGPlus;
import com.pg85.otg.configuration.io.FileSettingsWriterOTGPlus;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.configuration.world.WorldConfig.ConfigMode;
import com.pg85.otg.customobjects.bo4.bo4function.BO4BlockFunction;
import com.pg85.otg.customobjects.bo4.bo4function.BO4RandomBlockFunction;
import com.pg85.otg.customobjects.structures.Branch;
import com.pg85.otg.customobjects.structures.StructuredCustomObject;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.generator.surface.MesaSurfaceGenerator;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.bo3.NamedBinaryTag;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.materials.MaterialHelper;
import com.pg85.otg.util.minecraft.defaults.DefaultMaterial;

public class BO4 implements StructuredCustomObject
{
	// TODO: Make this prettier	
    // Original top blocks are cached to figure out the surface block material to replace to when spawning structures and smoothing areas
    public static HashMap<ChunkCoordinate, LocalMaterialData> OriginalTopBlocks = new HashMap<ChunkCoordinate, LocalMaterialData>();
	
	public boolean isInvalidConfig;
    private BO4Config config;
    private final String name;
    private final File file;

    /**
     * Creates a BO3 from a file.
     *
     * @param name Name of the BO3.
     * @param file File of the BO3. If the file does not exist, a BO3 with the default settings is created.
     */
    public BO4(String name, File file)
    {
        this.name = name;
        this.file = file;
    }
    
    // Used by BO4Creator to export BO4's from WorldEdit selection, prevents instant writing on onEnable.
    public BO4(String name, File file, BO4Config settings)
    {
        this.name = name;
        this.file = file;
        this.config = settings;
    }
    
    @Override
    public String getName()
    {
        return name;
    }

    public BO4Config getConfig()
    {
        return config;
    }
    
    @Override
    public boolean onEnable()
    {
    	try
    	{
    		this.config = new BO4Config(new FileSettingsReaderOTGPlus(name, file), true);
    		if(this.config.settingsMode != ConfigMode.WriteDisable && !this.config.isBO4Data)
    		{
    			FileSettingsWriterOTGPlus.writeToFile(this.config, this.config.settingsMode);
    		}
    	}
    	catch(InvalidConfigException ex)
    	{
    		isInvalidConfig = true;
    		return false;
    	}
    	
    	return true;
    }
    
    @Override
    public boolean canSpawnAsTree()
    {
        return false;
    }

    @Override
    public boolean canRotateRandomly()
    {
    	return false;
    }   

	@Override
	public boolean loadChecks() {
		return true;
	}
    
    @Override
    public boolean spawnFromSapling(LocalWorld world, Random random, Rotation rotation, int x, int y, int z)
    {
        return false;
    }
            
    // BO4 CustomStructures cannot be force-spawned, only plotted in unloaded chunks and then spawned when the chunk is populated.
    @Override
    public boolean spawnForced(LocalWorld world, Random random, Rotation rotation, int x, int y, int z)
    {
    	return false;
    }
    
	@Override
	public boolean spawnAsTree(LocalWorld world, Random random, int x, int z, int minY, int maxY, ChunkCoordinate chunkBeingPopulated)
	{
		return false;
	}

	// TODO: Implement this, move code?
    @Override
    public boolean process(LocalWorld world, Random random, ChunkCoordinate chunkCoord)
    {
    	return false;
    }

    public Branch[] getBranches()
    {
        return config.getbranches();
    }
      
    public boolean isCollidable()
    {
    	return getConfig().isCollidable();
    }
    
    // BO4's should always spawn within population bounds, so there is no SpawnForced, only TrySpawnAt
    public boolean trySpawnAt(LocalWorld world, Random random, Rotation rotation, ChunkCoordinate chunkCoord, int x, int y, int z, String replaceAbove, String replaceBelow, boolean replaceWithBiomeBlocks, String replaceWithSurfaceBlock, String replaceWithGroundBlock, boolean spawnUnderWater, int waterLevel, boolean isStructureAtSpawn, boolean doReplaceAboveBelowOnly, ChunkCoordinate chunkBeingPopulated)
    {
    	//OTG.log(LogMarker.INFO, "Spawning " + this.getName());

    	LocalMaterialData replaceBelowMaterial = null;
    	LocalMaterialData replaceAboveMaterial = null;

    	LocalMaterialData bo3SurfaceBlock = null;
    	LocalMaterialData bo3GroundBlock = null;
    	LocalMaterialData airMaterial = null;

    	airMaterial = MaterialHelper.toLocalMaterialData(DefaultMaterial.AIR, 0);

		if(config == null)
		{
			OTG.log(LogMarker.FATAL, "Settings was null for BO4 " + this.getName() + ". This should not be happening, please contact the developer.");
			throw new RuntimeException("Settings was null for BO4 " + this.getName() + ". This should not be happening, please contact the developer.");
		}

    	try {
    		bo3SurfaceBlock = replaceWithSurfaceBlock != null && replaceWithSurfaceBlock.length() > 0 ? MaterialHelper.readMaterial(replaceWithSurfaceBlock) : MaterialHelper.readMaterial("GRASS");
		} catch (InvalidConfigException e1) {
			bo3SurfaceBlock = MaterialHelper.toLocalMaterialData(DefaultMaterial.GRASS, 0);
			if(OTG.getPluginConfig().spawnLog)
			{
				OTG.log(LogMarker.WARN, "Value " + replaceWithSurfaceBlock + " for replaceWithSurfaceBlock in BO3 " + this.getName() + " was not recognised. Using GRASS instead.");
			}
		}
    	try {
    		bo3GroundBlock = replaceWithGroundBlock != null && replaceWithGroundBlock.length() > 0 ? MaterialHelper.readMaterial(replaceWithGroundBlock) : MaterialHelper.readMaterial("DIRT");
		} catch (InvalidConfigException e1) {
			bo3GroundBlock = MaterialHelper.toLocalMaterialData(DefaultMaterial.DIRT, 0);
			if(OTG.getPluginConfig().spawnLog)
			{
				OTG.log(LogMarker.WARN, "Value " + replaceWithGroundBlock + " for replaceWithGroundBlock in BO3 " + this.getName() + " was not recognised. Using DIRT instead.");
			}
		}

    	try {
			replaceBelowMaterial = config.replaceBelow != null && config.replaceBelow.toLowerCase().equals("none") ? null : replaceBelow != null && replaceBelow.length() > 0 ? MaterialHelper.readMaterial(replaceBelow) : null;
		} catch (InvalidConfigException e1) {
			replaceBelowMaterial = MaterialHelper.toLocalMaterialData(DefaultMaterial.DIRT, 0);
			if(OTG.getPluginConfig().spawnLog)
			{
				OTG.log(LogMarker.INFO, "Value " + config.replaceBelow + " for replaceBelow in BO3 " + this.getName() + " was not recognised. Using DIRT instead.");
			}
		}
    	try {
			replaceAboveMaterial = config.replaceAbove != null && config.replaceAbove.toLowerCase().equals("none") ? null : replaceAbove != null && replaceAbove.length() > 0 ? MaterialHelper.readMaterial(replaceAbove) : null;
		} catch (InvalidConfigException e1) {
			replaceAboveMaterial = MaterialHelper.toLocalMaterialData(DefaultMaterial.AIR, 0);
			if(OTG.getPluginConfig().spawnLog)
			{
				OTG.log(LogMarker.INFO, "Value " + config.replaceAbove + " for replaceAbove in BO3 " + this.getName() + " was not recognised. Using AIR instead.");
			}
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
				biomeSurfaceBlock = MaterialHelper.toLocalMaterialData(DefaultMaterial.GRASS,0);
	        }

	        biomeGroundBlock = biomeConfig.groundBlock;
	        if(biomeGroundBlock == null)
	        {
				biomeGroundBlock = MaterialHelper.toLocalMaterialData(DefaultMaterial.DIRT,0);
	        }

	        if(biomeSurfaceBlock.toDefaultMaterial().equals(DefaultMaterial.SNOW))
	        {
	        	biomeSurfaceBlock = MaterialHelper.toLocalMaterialData(DefaultMaterial.SNOW_BLOCK,0);
	        }
	        if(biomeGroundBlock.toDefaultMaterial().equals(DefaultMaterial.SNOW))
	        {
	        	biomeGroundBlock = MaterialHelper.toLocalMaterialData(DefaultMaterial.SNOW_BLOCK,0);
	        }
    	}

    	// Get the right coordinates based on rotation

    	ArrayList<Object[]> coordsAboveDone = new ArrayList<Object[]>();
    	ArrayList<Object[]> coordsBelowDone = new ArrayList<Object[]>();

    	BO4BlockFunction blockToQueueForSpawn = new BO4BlockFunction();
    	LocalMaterialData sourceBlockMaterial;

    	boolean outOfBounds = false;

        // Spawn
    	long startTime = System.currentTimeMillis();
        for (BO4BlockFunction block : config.getBlocks())
        {
        	if(block instanceof BO4RandomBlockFunction)
        	{
        		BO4RandomBlockFunction randomBlockFunction = ((BO4RandomBlockFunction)block);
                for (int i = 0; i < randomBlockFunction.blockCount; i++)
                {
                    if (random.nextInt(100) < randomBlockFunction.blockChances[i])
                    {
                    	block.metaDataName = randomBlockFunction.metaDataNames[i];
                    	block.metaDataTag = randomBlockFunction.metaDataTags[i];
                    	block.material = randomBlockFunction.blocks[i];
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
	        	BO4BlockFunction newBlock = new BO4BlockFunction();
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
            		newBlock.z = -block.x + 15;
            		newBlock.material = block.material.rotate();
            	}
            	if(rotations == 2)
            	{
            		newBlock.x = -block.x + 15;
            		newBlock.z = -block.z + 15;
            		newBlock.material = block.material.rotate();
            		newBlock.material = newBlock.material.rotate();
            	}
            	if(rotations == 3)
            	{
            		newBlock.x = -block.z + 15;
            		newBlock.z = block.x;
            		newBlock.material = block.material.rotate();
            		newBlock.material = newBlock.material.rotate();
            		newBlock.material = newBlock.material.rotate();
            	}
            	newBlock.y = block.y;

	        	newBlock.metaDataName = block.metaDataName;
	        	newBlock.metaDataTag = block.metaDataTag;

	        	if(!OriginalTopBlocks.containsKey(ChunkCoordinate.fromChunkCoords(x + newBlock.x, z + newBlock.z)))
	        	{
	        		int highestBlockY = world.getHighestBlockYAt(x + newBlock.x, z + newBlock.z, true, true, false, false, true, chunkBeingPopulated);        		
	        		if(highestBlockY <= PluginStandardValues.WORLD_DEPTH) // Can happen for chunks filled with air, or null chunks (TODO: Null chunks shouldn't happen here, confirm they don't.)
	        		{
	        			highestBlockY = 1;
	        		}
	        		if(highestBlockY >= PluginStandardValues.WORLD_HEIGHT)
	        		{
	        			highestBlockY = 255;
	        		}
	        		OriginalTopBlocks.put(ChunkCoordinate.fromChunkCoords(x + newBlock.x, z + newBlock.z), world.getMaterial(x + newBlock.x, highestBlockY, z + newBlock.z, chunkBeingPopulated));
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
	        			int blockY = y + newBlock.y + 1; // TODO: This is wrong, should be the lowest block in the BO4 at these x-z coordinates. ReplaceAbove should be done before any blocks in this column are placed
        				int highestBlockToReplace = world.getHighestBlockYAt(x + newBlock.x, z + newBlock.z, true, true, false, false, true, chunkBeingPopulated);

	        			while(blockY <= highestBlockToReplace && blockY > y + newBlock.y)
	        			{
                            blockToQueueForSpawn = new BO4BlockFunction();

	        				if(spawnUnderWater && blockY >= waterLevel)
			            	{
	        					blockToQueueForSpawn.material = airMaterial;
			            	} else {
			            		blockToQueueForSpawn.material = replaceAboveMaterial;
			            	}

	        				// TODO: Make override leaves and air configurable
	        				// TODO: Make replaceAbove height configurable
                            blockToQueueForSpawn.x = x + newBlock.x;
                            blockToQueueForSpawn.y = (short) blockY;
                            blockToQueueForSpawn.z = z + newBlock.z;

    						blockToQueueForSpawn.metaDataName = block.metaDataName;
    						blockToQueueForSpawn.metaDataTag = block.metaDataTag;

    						sourceBlockMaterial = world.getMaterial(blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z, chunkBeingPopulated);

                			if(!sourceBlockMaterial.toDefaultMaterial().equals(blockToQueueForSpawn.material.toDefaultMaterial()) || sourceBlockMaterial.getBlockData() != blockToQueueForSpawn.material.getBlockData())
                			{
            					ChunkCoordinate destChunk = ChunkCoordinate.fromBlockCoords(blockToQueueForSpawn.x, blockToQueueForSpawn.z);
            					if(chunkCoord.equals(destChunk))
            					{
        							setBlock(world, blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z, blockToQueueForSpawn.material, blockToQueueForSpawn.metaDataTag, isStructureAtSpawn, chunkBeingPopulated);
            					} else {
            						outOfBounds = true;
            					}
                			}

	        				blockY += 1;
	        			}
	        		}
	        	}
	        	
	        	if(replaceBelowMaterial != null && block.y == 0 && !block.material.isEmptyOrAir() && doReplaceAboveBelowOnly)
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
	        			while(blockY > PluginStandardValues.WORLD_DEPTH)
	        			{
	        				if(blockY < PluginStandardValues.WORLD_HEIGHT)
	        				{
	                            blockToQueueForSpawn = new BO4BlockFunction();
	                            blockToQueueForSpawn.x = x + newBlock.x;
	                            blockToQueueForSpawn.y = (short) blockY;
	                            blockToQueueForSpawn.z = z + newBlock.z;
	                            blockToQueueForSpawn.material = replaceBelowMaterial;

	    						blockToQueueForSpawn.metaDataName = block.metaDataName;
	    						blockToQueueForSpawn.metaDataTag = block.metaDataTag;

	    						sourceBlockMaterial = world.getMaterial(blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z, chunkBeingPopulated);

	                			if(!sourceBlockMaterial.isSolid() && !sourceBlockMaterial.toDefaultMaterial().equals(blockToQueueForSpawn.material.toDefaultMaterial()) || sourceBlockMaterial.getBlockData() != blockToQueueForSpawn.material.getBlockData())
	                			{
                					ChunkCoordinate destChunk = ChunkCoordinate.fromBlockCoords(blockToQueueForSpawn.x, blockToQueueForSpawn.z);
                					if(chunkCoord.equals(destChunk))
                					{
            							setBlock(world, blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z, blockToQueueForSpawn.material, blockToQueueForSpawn.metaDataTag, isStructureAtSpawn, chunkBeingPopulated);
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
                    blockToQueueForSpawn = new BO4BlockFunction();
                    blockToQueueForSpawn.x = x + newBlock.x;
                    blockToQueueForSpawn.y = (short) (y + newBlock.y);
                    blockToQueueForSpawn.z = z + newBlock.z;
					blockToQueueForSpawn.material = newBlock.material;

					blockToQueueForSpawn.metaDataName = block.metaDataName;
					blockToQueueForSpawn.metaDataTag = block.metaDataTag;

					sourceBlockMaterial = world.getMaterial(blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z, chunkBeingPopulated);

    				if(replaceWithBiomeBlocks)
    				{
                        if(isOnBiomeBorder)
                        {
        	                biome = world.getBiome(blockToQueueForSpawn.x, blockToQueueForSpawn.z);
        	                biomeConfig = biome.getBiomeConfig();

        	    	        biomeSurfaceBlock = biomeConfig.surfaceBlock;
        	    	        if(biomeSurfaceBlock == null)
        	    	        {
        	    				biomeSurfaceBlock = MaterialHelper.toLocalMaterialData(DefaultMaterial.GRASS,0);
        	    	        }

        	    	        biomeGroundBlock = biomeConfig.groundBlock;
        	    	        if(biomeGroundBlock == null)
        	    	        {
        	    				biomeGroundBlock = MaterialHelper.toLocalMaterialData(DefaultMaterial.DIRT,0);
        	    	        }

    		    	        if(biomeSurfaceBlock.toDefaultMaterial().equals(DefaultMaterial.SNOW))
    		    	        {
    		    	        	biomeSurfaceBlock = MaterialHelper.toLocalMaterialData(DefaultMaterial.SNOW_BLOCK,0);
    		    	        }
    		    	        if(biomeGroundBlock.toDefaultMaterial().equals(DefaultMaterial.SNOW))
    		    	        {
    		    	        	biomeGroundBlock = MaterialHelper.toLocalMaterialData(DefaultMaterial.SNOW_BLOCK,0);
    		    	        }
                        }

    					if(blockToQueueForSpawn.material.toDefaultMaterial().equals(bo3GroundBlock.toDefaultMaterial()) && blockToQueueForSpawn.material.getBlockData() == bo3GroundBlock.getBlockData())
    					{
    						blockToQueueForSpawn.material = biomeGroundBlock;
    					}
    					else if(blockToQueueForSpawn.material.toDefaultMaterial().equals(bo3SurfaceBlock.toDefaultMaterial()) && blockToQueueForSpawn.material.getBlockData() == bo3SurfaceBlock.getBlockData())
    					{
    						blockToQueueForSpawn.material = biomeSurfaceBlock;

    	        			LocalMaterialData originalSurfaceBlock = OriginalTopBlocks.get(ChunkCoordinate.fromChunkCoords(blockToQueueForSpawn.x, blockToQueueForSpawn.z));
    	        			if(
	        					originalSurfaceBlock != null &&
	        					!originalSurfaceBlock.isLiquid() && 
	        					!originalSurfaceBlock.isEmptyOrAir() &&
	        					originalSurfaceBlock.toDefaultMaterial() != DefaultMaterial.UNKNOWN_BLOCK 
        					)
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
							setBlock(world, blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z, blockToQueueForSpawn.material, blockToQueueForSpawn.metaDataTag, isStructureAtSpawn, chunkBeingPopulated);
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
        				short blockY = (short) (y + block.y + 1); // TODO: This is wrong, should be the lowest block in the BO3 at these x-z coordinates. replaceAbove should be done before any blocks in this column are placed

        				int highestBlockToReplace = world.getHighestBlockYAt(x + block.x, z + block.z, true, true, false, false, true, chunkBeingPopulated);

	        			while(blockY <= highestBlockToReplace && blockY > y + block.y)
	        			{
                            blockToQueueForSpawn = new BO4BlockFunction();

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

                            sourceBlockMaterial = world.getMaterial(blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z, chunkBeingPopulated);

            	        	if(!OriginalTopBlocks.containsKey(ChunkCoordinate.fromChunkCoords(blockToQueueForSpawn.x, blockToQueueForSpawn.z)))
            	        	{
            	        		int highestBlockY = world.getHighestBlockYAt(blockToQueueForSpawn.x, blockToQueueForSpawn.z, true, true, false, false, true, chunkBeingPopulated);
            	        		if(highestBlockY <= PluginStandardValues.WORLD_DEPTH)
            	        		{
            	        			highestBlockY = 1;
            	        		}
            	        		if(highestBlockY >= PluginStandardValues.WORLD_HEIGHT)
            	        		{
            	        			highestBlockY = 255;
            	        		}
            	        		OriginalTopBlocks.put(ChunkCoordinate.fromChunkCoords(blockToQueueForSpawn.x, blockToQueueForSpawn.z), world.getMaterial(blockToQueueForSpawn.x, highestBlockY, blockToQueueForSpawn.z, chunkBeingPopulated));
            	        	}

                			if(!sourceBlockMaterial.toDefaultMaterial().equals(blockToQueueForSpawn.material.toDefaultMaterial()) || sourceBlockMaterial.getBlockData() != blockToQueueForSpawn.material.getBlockData())
                			{
            					ChunkCoordinate destChunk = ChunkCoordinate.fromBlockCoords(blockToQueueForSpawn.x, blockToQueueForSpawn.z);
            					if(chunkCoord.equals(destChunk))
            					{
           							setBlock(world, blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z, blockToQueueForSpawn.material, blockToQueueForSpawn.metaDataTag, isStructureAtSpawn, chunkBeingPopulated);
            					} else {
            						outOfBounds = true;
            					}
                			}
	        				blockY += 1;
	        			}
	        		}
	        	}
    			
    			if(replaceBelowMaterial != null && block.y == 0 && !block.material.isEmptyOrAir() && doReplaceAboveBelowOnly)
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
	        			short blockY = (short) (y + block.y - 1);

	        			while(blockY > PluginStandardValues.WORLD_DEPTH)
	        			{
	        				if(blockY < PluginStandardValues.WORLD_HEIGHT)
	        				{
	                            blockToQueueForSpawn = new BO4BlockFunction();
	                            blockToQueueForSpawn.x = x + block.x;
	                            blockToQueueForSpawn.y = blockY;
	                            blockToQueueForSpawn.z = z + block.z;
	                            blockToQueueForSpawn.material = replaceBelowMaterial;

	    						blockToQueueForSpawn.metaDataName = block.metaDataName;
	    						blockToQueueForSpawn.metaDataTag = block.metaDataTag;

	    						sourceBlockMaterial = world.getMaterial(blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z, chunkBeingPopulated);

	            	        	if(!OriginalTopBlocks.containsKey(ChunkCoordinate.fromChunkCoords(blockToQueueForSpawn.x, blockToQueueForSpawn.z)))
	            	        	{
	            	        		int highestBlockY = world.getHighestBlockYAt(blockToQueueForSpawn.x, blockToQueueForSpawn.z, true, true, false, false, true, chunkBeingPopulated);
	            	        		if(highestBlockY <= PluginStandardValues.WORLD_DEPTH)
	            	        		{
	            	        			highestBlockY = 1;
	            	        		}
	            	        		if(highestBlockY >= PluginStandardValues.WORLD_HEIGHT)
	            	        		{
	            	        			highestBlockY = 255;
	            	        		}
	            	        		OriginalTopBlocks.put(ChunkCoordinate.fromChunkCoords(blockToQueueForSpawn.x, blockToQueueForSpawn.z), world.getMaterial(blockToQueueForSpawn.x, highestBlockY, blockToQueueForSpawn.z, chunkBeingPopulated));
	            	        	}

	                			if(!sourceBlockMaterial.isSolid() && !sourceBlockMaterial.toDefaultMaterial().equals(blockToQueueForSpawn.material.toDefaultMaterial()) || sourceBlockMaterial.getBlockData() != blockToQueueForSpawn.material.getBlockData())
	                			{
                					ChunkCoordinate destChunk = ChunkCoordinate.fromBlockCoords(blockToQueueForSpawn.x, blockToQueueForSpawn.z);
                					if(chunkCoord.equals(destChunk))
                					{
            							setBlock(world, blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z, blockToQueueForSpawn.material, blockToQueueForSpawn.metaDataTag, isStructureAtSpawn, chunkBeingPopulated);
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
                    blockToQueueForSpawn = new BO4BlockFunction();
                    blockToQueueForSpawn.x = x + block.x;
                    blockToQueueForSpawn.y = (short) (y + block.y);
                    blockToQueueForSpawn.z = z + block.z;
					blockToQueueForSpawn.material = block.material;

					blockToQueueForSpawn.metaDataName = block.metaDataName;
					blockToQueueForSpawn.metaDataTag = block.metaDataTag;

					sourceBlockMaterial = world.getMaterial(blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z, chunkBeingPopulated);

    	        	if(!OriginalTopBlocks.containsKey(ChunkCoordinate.fromChunkCoords(blockToQueueForSpawn.x, blockToQueueForSpawn.z)))
    	        	{
    	        		int highestBlockY = world.getHighestBlockYAt(blockToQueueForSpawn.x, blockToQueueForSpawn.z, true, true, false, false, true, chunkBeingPopulated);
    	        		if(highestBlockY > PluginStandardValues.WORLD_DEPTH)
    	        		{
    	        			OriginalTopBlocks.put(ChunkCoordinate.fromChunkCoords(blockToQueueForSpawn.x, blockToQueueForSpawn.z), world.getMaterial(blockToQueueForSpawn.x, highestBlockY, blockToQueueForSpawn.z, chunkBeingPopulated));
    	        		} else {
    	        			OriginalTopBlocks.put(ChunkCoordinate.fromChunkCoords(blockToQueueForSpawn.x, blockToQueueForSpawn.z), null);
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
    		    	        	biomeSurfaceBlock = MaterialHelper.toLocalMaterialData(DefaultMaterial.SNOW_BLOCK,0);
    		    	        }
    		    	        if(biomeGroundBlock.toDefaultMaterial().equals(DefaultMaterial.SNOW))
    		    	        {
    		    	        	biomeGroundBlock = MaterialHelper.toLocalMaterialData(DefaultMaterial.SNOW_BLOCK,0);
    		    	        }
                        }

    					if(blockToQueueForSpawn.material.toDefaultMaterial().equals(bo3GroundBlock.toDefaultMaterial()) && blockToQueueForSpawn.material.getBlockData() == bo3GroundBlock.getBlockData())
    					{
    						blockToQueueForSpawn.material = biomeGroundBlock;
    					}
    					else if(blockToQueueForSpawn.material.toDefaultMaterial().equals(bo3SurfaceBlock.toDefaultMaterial()) && blockToQueueForSpawn.material.getBlockData() == bo3SurfaceBlock.getBlockData())
    					{
    						blockToQueueForSpawn.material = biomeSurfaceBlock;

    	        			LocalMaterialData originalSurfaceBlock = OriginalTopBlocks.get(ChunkCoordinate.fromChunkCoords(blockToQueueForSpawn.x, blockToQueueForSpawn.z));
    	        			if(
	        					originalSurfaceBlock != null && 
	        					originalSurfaceBlock.toDefaultMaterial() != DefaultMaterial.UNKNOWN_BLOCK && 
	        					!originalSurfaceBlock.isLiquid() && 
	        					!originalSurfaceBlock.isEmptyOrAir()
        					)
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
   							setBlock(world, blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z, blockToQueueForSpawn.material, blockToQueueForSpawn.metaDataTag, isStructureAtSpawn, chunkBeingPopulated);
    					} else {
    						outOfBounds = true;
    					}
    				}
				}
        	}
        }
        if(outOfBounds)
        {
        	if(OTG.getPluginConfig().spawnLog)
        	{
        		OTG.log(LogMarker.WARN, "BO4 " + this.getName() + " tried to spawn blocks outside of the chunk being populated, the blocks have been ignored. This can happen if a BO3 is not sliced into 16x16 pieces or has branches positioned in such a way that they cross a chunk border. OTG is more strict than TC in how branching BO4's used as CustomStructures() should be designed, BO4 creators have to design their BO4's and position their branches so that they fit neatly into a 16x16 grid. Hopefully in a future release OTG can be made to automatically slice branching structures instead of forcing the BO4 creator to do it.");
        	}
        }

        if(OTG.getPluginConfig().spawnLog && (System.currentTimeMillis() - startTime) > 50)
        {
        	OTG.log(LogMarker.WARN, "Warning: Spawning BO4 " + this.getName()  + " took " + (System.currentTimeMillis() - startTime) + " Ms.");
        }

        return true;
    }
    
    private void setBlock(LocalWorld world, int x, int y, int z, LocalMaterialData material, NamedBinaryTag metaDataTag, boolean isStructureAtSpawn, ChunkCoordinate chunkBeingPopulated)
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
	    if(OTG.getPluginConfig().developerMode)
	    {
		    DefaultMaterial worldMaterial = world.getMaterial(x, y, z, chunkBeingPopulated).toDefaultMaterial();
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
		    		world.setBlock(x, y, z, MaterialHelper.toLocalMaterialData(DefaultMaterial.GLOWSTONE, 0), null, chunkBeingPopulated);
		    	}
		    	return;
		    }
	    }
	    world.setBlock(x, y, z, material, metaDataTag, chunkBeingPopulated);
    }
}
