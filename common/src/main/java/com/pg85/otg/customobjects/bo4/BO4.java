package com.pg85.otg.customobjects.bo4;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.configuration.io.FileSettingsReaderOTGPlus;
import com.pg85.otg.configuration.io.FileSettingsWriterOTGPlus;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.configuration.world.WorldConfig.ConfigMode;
import com.pg85.otg.customobjects.CustomObject;
import com.pg85.otg.customobjects.bo3.StructurePartSpawnHeight;
import com.pg85.otg.customobjects.bo4.bo4function.BO4BlockFunction;
import com.pg85.otg.customobjects.bo4.bo4function.BO4RandomBlockFunction;
import com.pg85.otg.customobjects.structures.Branch;
import com.pg85.otg.customobjects.structures.CustomStructureCoordinate;
import com.pg85.otg.customobjects.structures.StructuredCustomObject;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.generator.surface.MesaSurfaceGenerator;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.bo3.BoundingBox;
import com.pg85.otg.util.bo3.NamedBinaryTag;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.helpers.MaterialHelper;
import com.pg85.otg.util.minecraft.defaults.DefaultMaterial;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class BO4 implements StructuredCustomObject
{
	// TODO: Make this prettier	
    // Original top blocks are cached to figure out the surface block material to replace to when spawning structures and smoothing areas
    public static HashMap<ChunkCoordinate, LocalMaterialData> OriginalTopBlocks = new HashMap<ChunkCoordinate, LocalMaterialData>();
	
	public boolean isInvalidConfig;
    private BO4Config settings;
    private final String name;
    private final File file;

    /**
     * Creates a BO3 from a file.
     *
     * @param name Name of the BO3.
     * @param file File of the BO3. If the file does not exist, a BO3 with the default settings is created.
     */
    BO4(String name, File file)
    {
        this.name = name;
        this.file = file;
    }
    
    @Override
    public String getName()
    {
        return name;
    }

    public BO4Config getSettings()
    {
        return settings;
    }
    
    @Override
    public boolean onEnable()
    {
    	try
    	{
    		this.settings = new BO4Config(new FileSettingsReaderOTGPlus(name, file), true);
    		if(this.settings.settingsMode != ConfigMode.WriteDisable)
    		{
    			FileSettingsWriterOTGPlus.writeToFile(this.settings, this.settings.settingsMode);
    		}
    	}
    	catch(InvalidConfigException ex)
    	{
    		isInvalidConfig = true;
    		return false;
    	}
    	
    	return true;
    }
    
    public void generateBO4Data()
    {
        //write to disk
		String filePath = 
			this.settings.getFile().getAbsolutePath().endsWith(".BO4") ? this.settings.getFile().getAbsolutePath().replace(".BO4", ".BO4Data") :
			this.settings.getFile().getAbsolutePath().endsWith(".bo4") ? this.settings.getFile().getAbsolutePath().replace(".bo4", ".BO4Data") :
			this.settings.getFile().getAbsolutePath().endsWith(".BO3") ? this.settings.getFile().getAbsolutePath().replace(".BO3", ".BO4Data") :
			this.settings.getFile().getAbsolutePath().endsWith(".bo3") ? this.settings.getFile().getAbsolutePath().replace(".bo3", ".BO4Data") :
			this.settings.getFile().getAbsolutePath();

        File file = new File(filePath);
        if(!file.exists())
        {
            try {                
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				DataOutputStream dos = new DataOutputStream(bos);
				this.settings.writeToStream(dos);
				byte[] compressedBytes = com.pg85.otg.util.CompressionUtils.compress(bos.toByteArray());
				dos.close();
				FileOutputStream fos = new FileOutputStream(file);
				DataOutputStream dos2 = new DataOutputStream(fos);
				dos2.write(compressedBytes, 0, compressedBytes.length);
				dos2.close();
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
    
    @Override
    public boolean canSpawnAsTree()
    {
        return false;
    }

    // Used for saplings
    @Override
    public boolean canRotateRandomly()
    {
    	return false;
    }
    
    // Used to safely spawn this object from a grown sapling
    // BO4s cannot be spawned from saplings
    @Override
    public boolean spawnFromSapling(LocalWorld world, Random random, Rotation rotation, int x, int y, int z)
    {
        return false;
    }
        
    // Used for Tree() and CustomObject
    @Override
    public boolean trySpawnAt(LocalWorld world, Random random, Rotation rotation, int x, int y, int z)
    {
    	return false;
    }
    
    @Override
    public boolean trySpawnAt(LocalWorld world, Random random, Rotation rotation, int x, int y, int z, int minY, int maxY)
    {
    	return false;
    }
    
    // Used for spawning saplings and customobjects without doing checks (for growing saplings, /spawn command, StructureAtSpawn etc).
    @Override
    public boolean spawnForced(LocalWorld world, Random random, Rotation rotation, int x, int y, int z)
    {
    	return false;
    }
    
	@Override
	public boolean spawnAsTree(LocalWorld world, Random random, int x, int z)
	{
		return false;
	}
	
	@Override
	public boolean spawnAsTree(LocalWorld world, Random random, int x, int z, int minY, int maxY)
	{
		return false;
	}
	
	// This method is only used to spawn CustomObject.
    @Override
    public boolean process(LocalWorld world, Random random, ChunkCoordinate chunkCoord)
    {
    	return false;
    }

    @Override
    public Branch[] getBranches()
    {
        return settings.getbranches();
    }
    
    @Override
    public Branch[] getBranches(Rotation rotation)
    {
    	return null;
    }

    @Override
    public CustomStructureCoordinate makeCustomObjectCoordinate(LocalWorld world, Random random, int chunkX, int chunkZ)
    {
    	return null;
    }

    @Override
    public StructurePartSpawnHeight getStructurePartSpawnHeight()
    {
        return null;
    }

    @Override
    public BoundingBox getBoundingBox(Rotation rotation)
    {
        return null;
    }

    // This used to be in CustomObject?
    @Override
    public int getMaxBranchDepth() 
    {
        return -1;
    }
    
    public boolean trySpawnAt(LocalWorld world, Random random, Rotation rotation, ChunkCoordinate chunkCoord, int x, int y, int z, String replaceAbove, String replaceBelow, boolean replaceWithBiomeBlocks, String replaceWithSurfaceBlock, String replaceWithGroundBlock, boolean spawnUnderWater, int waterLevel, boolean isStructureAtSpawn, boolean doReplaceAboveBelowOnly)
    {
    	return spawnForcedOTGPlus(world, random, rotation, chunkCoord, x, y, z, replaceAbove, replaceBelow, replaceWithBiomeBlocks, replaceWithSurfaceBlock, replaceWithGroundBlock, spawnUnderWater, waterLevel, isStructureAtSpawn, doReplaceAboveBelowOnly);
    }
    
    public boolean isCollidable()
    {
    	return getSettings().isCollidable();
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
	    if(OTG.getPluginConfig().developerMode)
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
		    		world.setBlock(x, y, z, MaterialHelper.toLocalMaterialData(DefaultMaterial.GLOWSTONE, 0), null, true);
		    	}
		    	return;
		    }
	    }
	    world.setBlock(x, y, z, material, metaDataTag, true);
    }
   
    private boolean spawnForcedOTGPlus(LocalWorld world, Random random, Rotation rotation, ChunkCoordinate chunkCoord, int x, int y, int z, String replaceAbove, String replaceBelow, boolean replaceWithBiomeBlocks, String replaceWithSurfaceBlock, String replaceWithGroundBlock, boolean spawnUnderWater, int waterLevel, boolean isStructureAtSpawn, boolean doReplaceAboveBelowOnly)
    {
    	//OTG.log(LogMarker.INFO, "Spawning " + this.getName());

    	LocalMaterialData replaceBelowMaterial = null;
    	LocalMaterialData replaceAboveMaterial = null;

    	LocalMaterialData bo3SurfaceBlock = null;
    	LocalMaterialData bo3GroundBlock = null;
    	LocalMaterialData airMaterial = null;

    	airMaterial = MaterialHelper.toLocalMaterialData(DefaultMaterial.AIR, 0);

		if(settings == null)
		{
			OTG.log(LogMarker.FATAL, "Settings was null for BO3 " + this.getName() + ". This should not be happening, please contact the developer.");
			throw new RuntimeException("Settings was null for BO3 " + this.getName() + ". This should not be happening, please contact the developer.");
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
			replaceBelowMaterial = settings.replaceBelow != null && settings.replaceBelow.toLowerCase().equals("none") ? null : replaceBelow != null && replaceBelow.length() > 0 ? MaterialHelper.readMaterial(replaceBelow) : null;
		} catch (InvalidConfigException e1) {
			replaceBelowMaterial = MaterialHelper.toLocalMaterialData(DefaultMaterial.DIRT, 0);
			if(OTG.getPluginConfig().spawnLog)
			{
				OTG.log(LogMarker.INFO, "Value " + settings.replaceBelow + " for replaceBelow in BO3 " + this.getName() + " was not recognised. Using DIRT instead.");
			}
		}
    	try {
			replaceAboveMaterial = settings.replaceAbove != null && settings.replaceAbove.toLowerCase().equals("none") ? null : replaceAbove != null && replaceAbove.length() > 0 ? MaterialHelper.readMaterial(replaceAbove) : null;
		} catch (InvalidConfigException e1) {
			replaceAboveMaterial = MaterialHelper.toLocalMaterialData(DefaultMaterial.AIR, 0);
			if(OTG.getPluginConfig().spawnLog)
			{
				OTG.log(LogMarker.INFO, "Value " + settings.replaceAbove + " for replaceAbove in BO3 " + this.getName() + " was not recognised. Using AIR instead.");
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
        for (BO4BlockFunction block : settings.getBlocks())
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
	        		int highestBlockY = world.getHighestBlockYAt(x + newBlock.x, z + newBlock.z, true, true, false, false);
	        		if(highestBlockY <= PluginStandardValues.WORLD_DEPTH)
	        		{
	        			highestBlockY = 1;
	        		}
	        		if(highestBlockY >= PluginStandardValues.WORLD_HEIGHT)
	        		{
	        			highestBlockY = 255;
	        		}
	        		OriginalTopBlocks.put(ChunkCoordinate.fromChunkCoords(x + newBlock.x, z + newBlock.z), world.getMaterial(x + newBlock.x, highestBlockY, z + newBlock.z, true));
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
                            blockToQueueForSpawn = new BO4BlockFunction();

	        				if(spawnUnderWater && blockY >= waterLevel)// && replaceAboveMaterial.isLiquid())
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

    						sourceBlockMaterial = world.getMaterial(blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z, true);

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

	    						sourceBlockMaterial = world.getMaterial(blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z, true);

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
                    blockToQueueForSpawn = new BO4BlockFunction();
                    blockToQueueForSpawn.x = x + newBlock.x;
                    blockToQueueForSpawn.y = (short) (y + newBlock.y);
                    blockToQueueForSpawn.z = z + newBlock.z;
					blockToQueueForSpawn.material = newBlock.material;

					blockToQueueForSpawn.metaDataName = block.metaDataName;
					blockToQueueForSpawn.metaDataTag = block.metaDataTag;

					sourceBlockMaterial = world.getMaterial(blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z, true);

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
        				short blockY = (short) (y + block.y + 1); // TODO: This is wrong, should be the lowest block in the BO3 at these x-z coordinates. replaceAbove should be done before any blocks in this column are placed

        				int heighestBlockToReplace = world.getHighestBlockYAt(x + block.x, z + block.z, true, true, false, false);

	        			// TODO: Use world height constant (dunno what its called and where its at)??
	        			while(blockY <= heighestBlockToReplace && blockY > y + block.y)
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

                            sourceBlockMaterial = world.getMaterial(blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z, true);

            	        	if(!OriginalTopBlocks.containsKey(ChunkCoordinate.fromChunkCoords(blockToQueueForSpawn.x, blockToQueueForSpawn.z)))
            	        	{
            	        		int highestBlockY = world.getHighestBlockYAt(blockToQueueForSpawn.x, blockToQueueForSpawn.z, true, true, false, false);
            	        		if(highestBlockY <= PluginStandardValues.WORLD_DEPTH)
            	        		{
            	        			highestBlockY = 1;
            	        		}
            	        		if(highestBlockY >= PluginStandardValues.WORLD_HEIGHT)
            	        		{
            	        			highestBlockY = 255;
            	        		}
            	        		OriginalTopBlocks.put(ChunkCoordinate.fromChunkCoords(blockToQueueForSpawn.x, blockToQueueForSpawn.z), world.getMaterial(blockToQueueForSpawn.x, highestBlockY, blockToQueueForSpawn.z, true));
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

	    						sourceBlockMaterial = world.getMaterial(blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z, true);

	            	        	if(!OriginalTopBlocks.containsKey(ChunkCoordinate.fromChunkCoords(blockToQueueForSpawn.x, blockToQueueForSpawn.z)))
	            	        	{
	            	        		int highestBlockY = world.getHighestBlockYAt(blockToQueueForSpawn.x, blockToQueueForSpawn.z, true, true, false, false);
	            	        		if(highestBlockY <= PluginStandardValues.WORLD_DEPTH)
	            	        		{
	            	        			highestBlockY = 1;
	            	        		}
	            	        		if(highestBlockY >= PluginStandardValues.WORLD_HEIGHT)
	            	        		{
	            	        			highestBlockY = 255;
	            	        		}
	            	        		OriginalTopBlocks.put(ChunkCoordinate.fromChunkCoords(blockToQueueForSpawn.x, blockToQueueForSpawn.z), world.getMaterial(blockToQueueForSpawn.x, highestBlockY, blockToQueueForSpawn.z, true));
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
                    blockToQueueForSpawn = new BO4BlockFunction();
                    blockToQueueForSpawn.x = x + block.x;
                    blockToQueueForSpawn.y = (short) (y + block.y);
                    blockToQueueForSpawn.z = z + block.z;
					blockToQueueForSpawn.material = block.material;

					blockToQueueForSpawn.metaDataName = block.metaDataName;
					blockToQueueForSpawn.metaDataTag = block.metaDataTag;

					sourceBlockMaterial = world.getMaterial(blockToQueueForSpawn.x, blockToQueueForSpawn.y, blockToQueueForSpawn.z, true);

    	        	if(!OriginalTopBlocks.containsKey(ChunkCoordinate.fromChunkCoords(blockToQueueForSpawn.x, blockToQueueForSpawn.z)))
    	        	{
    	        		int highestBlockY = world.getHighestBlockYAt(blockToQueueForSpawn.x, blockToQueueForSpawn.z, true, true, false, false);
    	        		if(highestBlockY > PluginStandardValues.WORLD_DEPTH)
    	        		{
    	        			OriginalTopBlocks.put(ChunkCoordinate.fromChunkCoords(blockToQueueForSpawn.x, blockToQueueForSpawn.z), world.getMaterial(blockToQueueForSpawn.x, highestBlockY, blockToQueueForSpawn.z, true));
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
        	if(OTG.getPluginConfig().spawnLog)
        	{
        		OTG.log(LogMarker.WARN, "BO3 " + this.getName() + " tried to spawn blocks outside of the chunk being populated, the blocks have been ignored. This can happen if a BO3 is not sliced into 16x16 pieces or has branches positioned in such a way that they cross a chunk border. OTG is more strict than TC in how branching BO3's used as CustomStructures() should be designed, BO3 creators have to design their BO3's and position their branches so that they fit neatly into a 16x16 grid. Hopefully in a future release OTG can be made to automatically slice branching structures instead of forcing the BO3 creator to do it.");
        	}
        }

        if(OTG.getPluginConfig().spawnLog && (System.currentTimeMillis() - startTime) > 50)
        {
        	OTG.log(LogMarker.WARN, "Warning: Spawning BO3 " + this.getName()  + " took " + (System.currentTimeMillis() - startTime) + " Ms.");
        }

        return true;
    }
}
