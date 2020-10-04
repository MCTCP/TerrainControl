package com.pg85.otg.generator.surface;

import static com.pg85.otg.util.ChunkCoordinate.CHUNK_Y_SIZE;

import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.configuration.standard.WorldStandardValues;
import com.pg85.otg.configuration.world.WorldConfig;
import com.pg85.otg.generator.ChunkBuffer;
import com.pg85.otg.generator.GeneratingChunk;
import com.pg85.otg.util.materials.MaterialHelper;
import com.pg85.otg.util.minecraft.defaults.DefaultMaterial;

/**
 * Implementation of {@link SurfaceGenerator} that does absolutely nothing.
 *
 */
public class SimpleSurfaceGenerator implements SurfaceGenerator
{    
    @Override
    public LocalMaterialData getSurfaceBlockAtHeight(LocalWorld world, BiomeConfig biomeConfig, int xInWorld, int yInWorld, int zInWorld)
    {
    	return biomeConfig.getDefaultSurfaceBlock();
    }
    
	@Override
	public LocalMaterialData getGroundBlockAtHeight(LocalWorld world, BiomeConfig biomeConfig, int xInWorld, int yInWorld, int zInWorld)
	{
		return biomeConfig.getDefaultGroundBlock();
	}
	
    @Override
    public void spawn(LocalWorld world, GeneratingChunk generatingChunk, ChunkBuffer chunkBuffer, BiomeConfig biomeConfig, int xInWorld, int zInWorld)
    {
        spawnColumn(world, null, null, generatingChunk, chunkBuffer, biomeConfig, xInWorld & 0xf, zInWorld & 0xf);
    }

    // net.minecraft.world.biome.Biome.generateBiomeTerrain
    protected final void spawnColumn(LocalWorld world, LocalMaterialData defaultSurfaceBlock, LocalMaterialData defaultGroundBlock, GeneratingChunk generatingChunk, ChunkBuffer chunkBuffer, BiomeConfig biomeConfig, int x, int z)
    {
    	WorldConfig worldConfig = biomeConfig.worldConfig;
        float currentTemperature = biomeConfig.biomeTemperature;
        int surfaceBlocksNoise = (int) (generatingChunk.getNoise(x, z) / 3.0D + 3.0D + generatingChunk.random.nextDouble() * 0.25D);

        // Bedrock on the ceiling
        if (worldConfig.ceilingBedrock)
        {
            // Moved one block lower to fix lighting issues
            chunkBuffer.setBlock(x, generatingChunk.heightCap - 2, z, worldConfig.getBedrockBlockReplaced(world, biomeConfig, generatingChunk.heightCap - 2));
        }

        // Loop from map height to zero to place bedrock and surface blocks
        
        int surfaceBlocksCount = -1;
        LocalMaterialData stoneBlock;
        LocalMaterialData currentSurfaceBlock = null;
        LocalMaterialData currentGroundBlock = null;
        boolean useWaterForSurface = false;
        boolean useIceForSurface = false;
        boolean useAirForSurface = false;
        boolean useDefaultSurfaceBlockForSurface = true;
        boolean useBiomeStoneBlockForGround = false;
        boolean useDefaultGroundBlockForGround = true;
        final int currentWaterLevel = generatingChunk.getWaterLevel(x, z);
        LocalMaterialData blockOnCurrentPos;
        for (int y = CHUNK_Y_SIZE - 1; y >= 0; y--)
        {
            if (generatingChunk.mustCreateBedrockAt(worldConfig, y))
            {
                // Place bedrock
                chunkBuffer.setBlock(x, y, z, worldConfig.getBedrockBlockReplaced(world, biomeConfig, y));
            } else {
                // Surface blocks logic (grass, dirt, sand, sandstone)
                blockOnCurrentPos = chunkBuffer.getBlock(x, y, z);
            	stoneBlock = biomeConfig.getDefaultStoneBlock();
                if (blockOnCurrentPos.isEmptyOrAir())
                {
                    // Reset when air is found
                    surfaceBlocksCount = -1;
                }
                // TODO: stoneblock couldve been replaced by others during the replacebiomeblocks event?
                else if (blockOnCurrentPos.equals(stoneBlock))
                {
                    if (surfaceBlocksCount == -1)
                    {
                        // Set when variable was reset
                        if (surfaceBlocksNoise <= 0 && !worldConfig.removeSurfaceStone)
                        {
                            useAirForSurface = true;
                            useIceForSurface = false;
                            useWaterForSurface = false;
                            useDefaultSurfaceBlockForSurface = false;
                            useBiomeStoneBlockForGround = true;
                            useDefaultGroundBlockForGround = false;
                        }
                        else if ((y >= currentWaterLevel - 4) && (y <= currentWaterLevel + 1))
                        {
                        	useAirForSurface = false;
                        	useIceForSurface = false;
                        	useWaterForSurface = false;
                        	useDefaultSurfaceBlockForSurface = true;
                            useBiomeStoneBlockForGround = false;
                            useDefaultGroundBlockForGround = true;
                        }
                        
                        // Use blocks for the top of the water instead
                        // when on water
                        if (y < currentWaterLevel && y > worldConfig.waterLevelMin)
                        {
                        	boolean bIsAir = useAirForSurface;
                        	if(!bIsAir && useDefaultSurfaceBlockForSurface)
                        	{
                        		bIsAir = (defaultSurfaceBlock != null ? defaultSurfaceBlock.parseWithBiomeAndHeight(world, biomeConfig, y) : biomeConfig.getSurfaceBlockReplaced(world, y)).isAir();
                        	}
                        	if(bIsAir)
                        	{
	                            if (currentTemperature < WorldStandardValues.SNOW_AND_ICE_TEMP)
	                            {
	                            	useAirForSurface = false;
	                            	useIceForSurface = true;
	                            	useWaterForSurface = false;
	                            	useDefaultSurfaceBlockForSurface = false;
	                            } else {
	                            	useAirForSurface = false;
	                            	useIceForSurface = false;
	                            	useWaterForSurface = true;
	                            	useDefaultSurfaceBlockForSurface = false;
	                            }
                        	}
                        }

                        // Place surface block
                        surfaceBlocksCount = surfaceBlocksNoise;                       
                        if (y >= currentWaterLevel - 1)
                        {
                        	if(useAirForSurface)
                        	{
                        		currentSurfaceBlock = MaterialHelper.AIR;
                        	}
                        	else if(useIceForSurface)
                        	{
                        		currentSurfaceBlock = biomeConfig.getIceBlockReplaced(world, y);
                        	}
                        	else if(useWaterForSurface)
                        	{
                        		currentSurfaceBlock = biomeConfig.getWaterBlockReplaced(world, y);
                        	}
                        	else if(useDefaultSurfaceBlockForSurface)
                        	{
                        		currentSurfaceBlock = defaultSurfaceBlock != null ? defaultSurfaceBlock.parseWithBiomeAndHeight(world, biomeConfig, y) : biomeConfig.getSurfaceBlockReplaced(world, y);
                        	}
                        	
                        	chunkBuffer.setBlock(x, y, z, currentSurfaceBlock);
                        } else {
                            if(useBiomeStoneBlockForGround)
                            {
                            	currentGroundBlock = stoneBlock.parseWithBiomeAndHeight(world, biomeConfig, y);
                            }
                            else if(useDefaultGroundBlockForGround)
                            {
                            	currentGroundBlock = defaultGroundBlock != null ? defaultGroundBlock.parseWithBiomeAndHeight(world, biomeConfig, y) : biomeConfig.getGroundBlockReplaced(world, y);
                            }
                            
                        	chunkBuffer.setBlock(x, y, z, currentGroundBlock);
                        }
                    }
                    else if (surfaceBlocksCount > 0)
                    {
                        if(useBiomeStoneBlockForGround)
                        {
                        	currentGroundBlock = stoneBlock.parseWithBiomeAndHeight(world, biomeConfig, y);
                        }
                        else if(useDefaultGroundBlockForGround)
                        {
                        	currentGroundBlock = defaultGroundBlock != null ? defaultGroundBlock.parseWithBiomeAndHeight(world, biomeConfig, y) : biomeConfig.getGroundBlockReplaced(world, y);
                        }
                        
                        // Place ground block
                        surfaceBlocksCount--;
                        chunkBuffer.setBlock(x, y, z, currentGroundBlock);

                        // Place sandstone under stand
                        if ((surfaceBlocksCount == 0) && (currentGroundBlock.isMaterial(DefaultMaterial.SAND)) && surfaceBlocksNoise > 1)
                        {
                            surfaceBlocksCount = generatingChunk.random.nextInt(4) + Math.max(0, y - generatingChunk.getWaterLevel(x, z));
                            currentGroundBlock = currentGroundBlock.getBlockData() == 1 ? MaterialHelper.RED_SANDSTONE : MaterialHelper.SANDSTONE;
                        }
                    }
                }             
            }
        }
    }

    @Override
    public String toString()
    {
        // Make sure that empty name is written to the config files
        return "";
    }
}
