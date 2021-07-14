package com.pg85.otg.gen.surface;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.interfaces.IBiome;
import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.interfaces.ISurfaceGeneratorNoiseProvider;
import com.pg85.otg.util.gen.ChunkBuffer;
import com.pg85.otg.util.gen.GeneratingChunk;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterials;

public class SimpleSurfaceGenerator implements SurfaceGenerator
{
	@Override
	public LocalMaterialData getSurfaceBlockAtHeight(ISurfaceGeneratorNoiseProvider noiseProvider, IBiomeConfig biomeConfig, int xInWorld, int yInWorld, int zInWorld)
	{	
		return biomeConfig.getSurfaceBlockReplaced(yInWorld);
	}
	
	@Override
	public LocalMaterialData getGroundBlockAtHeight(ISurfaceGeneratorNoiseProvider noiseProvider, IBiomeConfig biomeConfig, int xInWorld, int yInWorld, int zInWorld)
	{
		return biomeConfig.getGroundBlockReplaced(yInWorld);
	}
	
	@Override
	public void spawn(long worldSeed, GeneratingChunk generatingChunk, ChunkBuffer chunkBuffer, IBiome biome, int xInWorld, int zInWorld)
	{
		spawnColumn(worldSeed, null, generatingChunk, chunkBuffer, biome, xInWorld, zInWorld);
	}

	// net.minecraft.world.biome.Biome.generateBiomeTerrain
	protected void spawnColumn(long worldSeed, MultipleLayersSurfaceGeneratorLayer layer, GeneratingChunk generatingChunk, ChunkBuffer chunkBuffer, IBiome biome, int xInWorld, int zInWorld)
	{
		int internalX = xInWorld & 0xf;
		int internalZ = zInWorld & 0xf;
		IBiomeConfig biomeConfig = biome.getBiomeConfig(); 
		// Used to create a variable depth ground layer per column
		int biomeBlocksNoise = (int) (generatingChunk.getNoise(internalX, internalZ) / 3.0D + 3.0D + generatingChunk.random.nextDouble() * 0.25D);

		// Bedrock on the ceiling
		if (biomeConfig.isCeilingBedrock())
		{
			// Moved one block lower to fix lighting issues
			chunkBuffer.setBlock(internalX, generatingChunk.heightCap - 2, internalZ, biomeConfig.getBedrockBlockReplaced(generatingChunk.heightCap - 2));
		}

		// Traverse down the block column to place bedrock, ground and surface blocks
		
		int groundLayerDepth = -1;
		LocalMaterialData currentSurfaceBlock = null;
		boolean useWaterForSurface = false;
		boolean useIceForSurface = false;
		boolean useAirForSurface = false;
		boolean useLayerSurfaceBlockForSurface = true;
		boolean useBiomeStoneBlockForGround = false;
		boolean useLayerGroundBlockForGround = true;
		boolean useSandStoneForGround = false;
		boolean biomeGroundBlockIsSand = biomeConfig.getDefaultGroundBlock().isMaterial(LocalMaterials.SAND);
		boolean layerGroundBlockIsSand = layer != null && layer.groundBlock.isMaterial(LocalMaterials.SAND);
		final int currentWaterLevel = generatingChunk.getWaterLevel(internalX, internalZ);
		LocalMaterialData blockOnCurrentPos;
		LocalMaterialData blockOnPreviousPos = null;
		
		int highestBlockInColumn = chunkBuffer.getHighestBlockForColumn(internalX, internalZ);
		for (int y = highestBlockInColumn; y >= 0; y--)
		{
			if (generatingChunk.mustCreateBedrockAt(biomeConfig.isFlatBedrock(), biomeConfig.isBedrockDisabled(), biomeConfig.isCeilingBedrock(), y))
			{
				// Place bedrock
				chunkBuffer.setBlock(internalX, y, internalZ, biomeConfig.getBedrockBlockReplaced(y));
			} else {

				// Surface blocks logic (grass, dirt, sand, sandstone)
				blockOnCurrentPos = chunkBuffer.getBlock(internalX, y, internalZ);
				if (blockOnCurrentPos.isEmptyOrAir())
				{
					// Reset when air is found
					groundLayerDepth = -1;
				}
				// The water block is much less likely to be replaced so lookups should be quicker,
				// do a != waterblockreplaced rather than an == stoneblockreplaced. Since we know
				// there can be only air, (replaced) water and stone in the chunk atm (unless some 
				// other mod did funky magic, which might cause problems).
				// TODO: This'll cause issues with surfaceandgroundcontrol if users configure the 
				// same biome water block as surface/ground/stone block.				
				// TODO: If other mods have problems bc of replaced blocks in the chunk during ReplaceBiomeBlocks, 
				// do replaceblock for stone/water here instead of when initially filling the chunk.
				else if(!blockOnCurrentPos.equals(biomeConfig.getWaterBlockReplaced(y)))
				{
					// Place surface/ground down to a certain depth per column,
					// determined via noise. groundLayerDepth == 0 means we're 
					// done until we hit an air block, in which case reset.
					if (groundLayerDepth == -1)
					{
						// Place surface block
						// Reset the ground layer depth
						groundLayerDepth = biomeBlocksNoise;
						
						// Set when variable was reset
						if (biomeBlocksNoise <= 0 && !biomeConfig.isRemoveSurfaceStone())
						{
							useAirForSurface = true;
							useIceForSurface = false;
							useWaterForSurface = false;
							useLayerSurfaceBlockForSurface = false;
							useSandStoneForGround = false;
							useBiomeStoneBlockForGround = true;
							useLayerGroundBlockForGround = false;
						}
						else if ((y >= currentWaterLevel - 4) && (y <= currentWaterLevel + 1))
						{
							useAirForSurface = false;
							useIceForSurface = false;
							useWaterForSurface = false;
							useLayerSurfaceBlockForSurface = true;
							useSandStoneForGround = false;
							useBiomeStoneBlockForGround = false;
							useLayerGroundBlockForGround = true;
						}
						
						// Use blocks for the top of the water instead
						// when on water
						if (y < currentWaterLevel && y > biomeConfig.getWaterLevelMin())
						{
							boolean bIsAir = useAirForSurface;
							if(!bIsAir && useLayerSurfaceBlockForSurface)
							{
								bIsAir = (layer != null ? layer.getSurfaceBlockReplaced(y, biomeConfig) : biomeConfig.getSurfaceBlockReplaced(y)).isAir();
							}
							if(bIsAir)
							{
								if (biome.getTemperatureAt(xInWorld, y, zInWorld) < Constants.SNOW_AND_ICE_TEMP)
								{
									useAirForSurface = false;
									useIceForSurface = true;
									useWaterForSurface = false;
									useLayerSurfaceBlockForSurface = false;
								} else {
									useAirForSurface = false;
									useIceForSurface = false;
									useWaterForSurface = true;
									useLayerSurfaceBlockForSurface = false;
								}
							}
						}

						if (y >= currentWaterLevel - 1)
						{
							if(useAirForSurface)
							{
								currentSurfaceBlock = LocalMaterials.AIR;
							}
							else if(useIceForSurface)
							{
								currentSurfaceBlock = biomeConfig.getIceBlockReplaced(y);
							}
							else if(useWaterForSurface)
							{
								currentSurfaceBlock = biomeConfig.getWaterBlockReplaced(y);
							}
							else if(useLayerSurfaceBlockForSurface)
							{
								currentSurfaceBlock = layer != null ? layer.getSurfaceBlockReplaced(y, biomeConfig) : biomeConfig.getSurfaceBlockReplaced(y);
							}
							
							chunkBuffer.setBlock(internalX, y, internalZ, currentSurfaceBlock);
						} else {
							if(useBiomeStoneBlockForGround)
							{
								// block should already be the replaced stoneblock
								blockOnPreviousPos = blockOnCurrentPos;								
								continue;
							}
							else if(useLayerGroundBlockForGround)
							{
								if(blockOnPreviousPos != null && blockOnPreviousPos.isLiquid())
								{
									chunkBuffer.setBlock(internalX, y, internalZ, layer != null ? layer.getUnderWaterSurfaceBlockReplaced(y, biomeConfig) : biomeConfig.getUnderWaterSurfaceBlockReplaced(y));
								} else {
									chunkBuffer.setBlock(internalX, y, internalZ, layer != null ? layer.getGroundBlockReplaced(y, biomeConfig) : biomeConfig.getGroundBlockReplaced(y));
								}
							}							
						}
					}
					// groundLayerDepth > 0 means we have ground layer left to spawn, 0 is done.
					else if (groundLayerDepth > 0)
					{
						groundLayerDepth--;
						// Place ground/stone block 
						if(useBiomeStoneBlockForGround)
						{
							// block should already be the replaced stoneblock
							blockOnPreviousPos = blockOnCurrentPos;
							continue;
						}
						else if(useLayerGroundBlockForGround)
						{
							if(useSandStoneForGround)
							{
								// TODO: Reimplement this when block data works
								//chunkBuffer.setBlock(x, y, z,
									//(layerGroundBlockIsSand ? layer.groundBlock : biomeConfig.getDefaultGroundBlock())
									//.getBlockData() == 1 ? 
										//biomeConfig.getRedSandStoneBlockReplaced(world, y) : 
										//biomeConfig.getSandStoneBlockReplaced(world, y)
								//);
								chunkBuffer.setBlock(internalX, y, internalZ, biomeConfig.getSandStoneBlockReplaced(y));
							} else {
								if(blockOnPreviousPos != null && blockOnPreviousPos.isLiquid())
								{
									chunkBuffer.setBlock(internalX, y, internalZ, layer != null ? layer.getUnderWaterSurfaceBlockReplaced(y, biomeConfig) : biomeConfig.getUnderWaterSurfaceBlockReplaced(y));
								} else {
									chunkBuffer.setBlock(internalX, y, internalZ, layer != null ? layer.getGroundBlockReplaced(y, biomeConfig) : biomeConfig.getGroundBlockReplaced(y));
								}
							}

							// When a ground layer of sand is done spawning, if the BiomeBlocksNoise is above 1
							// spawn layers of sandstone underneath.
							// If we end up at (y >= currentWaterLevel - 4) && (y <= currentWaterLevel + 1)
							// after doing this, the groundblock is set back to sand and we repeat the process,
							// otherwise we stop and leave blocks as stone, until we're done or hit air.

							// BiomeBlocksNoise is used to create a pattern of sandstone vs stone columns.
							// For waterlevel, the higher above it we are, the taller the sandstone sections become.
							// For vanilla deserts, this makes the sand layer deeper around waterlevel, affecting
							// mostly flat terrain, while hills have only a 1 block layer of sand and more sandstone.

							if (
								groundLayerDepth == 0 &&
								biomeBlocksNoise > 1 &&
								(layerGroundBlockIsSand || (layer == null && biomeGroundBlockIsSand))
							)
							{
								groundLayerDepth = generatingChunk.random.nextInt(4) + Math.max(0, y - currentWaterLevel);
								useSandStoneForGround = true;
							}
						} 
					}
				}
				blockOnPreviousPos = blockOnCurrentPos;
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
