package com.pg85.otg.gen.surface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.stream.IntStream;

import com.google.common.collect.ImmutableList;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.gen.noise.OctaveSimplexNoiseSampler;
import com.pg85.otg.interfaces.IBiome;
import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.util.gen.ChunkBuffer;
import com.pg85.otg.util.gen.GeneratingChunk;
import com.pg85.otg.util.helpers.StringHelper;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterials;

public class IcebergSurfaceGenerator extends MultipleLayersSurfaceGenerator
{
	private OctaveSimplexNoiseSampler icebergNoise;
	private OctaveSimplexNoiseSampler icebergCutoffNoise;
	
	IcebergSurfaceGenerator(String[] args, IMaterialReader materialReader) throws InvalidConfigException	
	{
		super(args, materialReader);
	}
	
	@Override
	protected void spawnColumn(long worldSeed, MultipleLayersSurfaceGeneratorLayer layer, GeneratingChunk generatingChunk, ChunkBuffer chunkBuffer, IBiome biome, int xInWorld, int zInWorld)
	{
		int internalX = xInWorld & 0xf;
		int internalZ = zInWorld & 0xf;
		
		if (this.icebergNoise == null || this.icebergCutoffNoise == null)
		{
			Random random = new Random(worldSeed);
			this.icebergNoise = new OctaveSimplexNoiseSampler(random, IntStream.rangeClosed(-3, 0));
			this.icebergCutoffNoise = new OctaveSimplexNoiseSampler(random, ImmutableList.of(0));
		}

		Random random = generatingChunk.random;
		IBiomeConfig biomeConfig = biome.getBiomeConfig();
		
		// Bedrock on the ceiling
		if (biomeConfig.isCeilingBedrock())
		{
			// Moved one block lower to fix lighting issues
			chunkBuffer.setBlock(internalX, generatingChunk.heightCap - 2, internalZ, biomeConfig.getBedrockBlockReplaced(generatingChunk.heightCap - 2));
		}

		double icebergHeight = 0;
		double icebergDepth = 0;
		int seaLevel = generatingChunk.getWaterLevel(internalX, internalZ);

		double noise = generatingChunk.getNoise(internalX, internalZ);
		float temperature = biome.getTemperatureAt(xInWorld, biomeConfig.getWaterLevelMax(), zInWorld);
		double icebergNoise = Math.min(Math.abs(noise), this.icebergNoise.sample((double)xInWorld * 0.1D, (double)zInWorld * 0.1D, false) * 15.0D);

		if (icebergNoise > 1.8D)
		{
			double icebergCutoff = Math.abs(this.icebergCutoffNoise.sample((double) xInWorld * 0.09765625D, (double) zInWorld * 0.09765625D, false));
			icebergHeight = icebergNoise * icebergNoise * 1.2D;

			double maxHeight = Math.ceil(icebergCutoff * 40.0D) + 14.0D;
			if (icebergHeight > maxHeight) {
				icebergHeight = maxHeight;
			}

			if(temperature > 0.1F)
			{
				icebergHeight -= 2.0D;
			}
			
			if (icebergHeight > 2.0D) {
				icebergDepth = (double)seaLevel - icebergHeight - 7.0D;
				icebergHeight = icebergHeight + (double)seaLevel;
			} else {
				icebergHeight = 0.0D;
			}
		}

		int dirtDepth = (int) (generatingChunk.getNoise(internalX, internalZ) / 3.0D + 3.0D + generatingChunk.random.nextDouble() * 0.25D);

		int generatedSnow = 0;
		int snowHeight = 2 + random.nextInt(4);
		int snowStart = seaLevel + 18 + random.nextInt(10);

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
		LocalMaterialData blockOnCurrentPos;
		LocalMaterialData blockOnPreviousPos = null;

		int topY = chunkBuffer.getHighestBlockForColumn(internalX, internalZ);
		for (int y = Math.max(topY, (int)icebergHeight + 1); y >= 0; y--)
		{
			if (generatingChunk.mustCreateBedrockAt(biomeConfig.isFlatBedrock(), biomeConfig.isBedrockDisabled(), biomeConfig.isCeilingBedrock(), y))
			{
				// Place bedrock
				chunkBuffer.setBlock(internalX, y, internalZ, biomeConfig.getBedrockBlockReplaced(y));
				continue;
			}

			// Store whether we set the iceberg here or not
			boolean setIceberg = false;

			// Place ice above and below the sea level, with a 99% and 85% chance respectively
			if (chunkBuffer.getBlock(internalX, y, internalZ).isAir() && y < icebergHeight && random.nextDouble() > 0.01D) {
				chunkBuffer.setBlock(internalX, y, internalZ, biomeConfig.getPackedIceBlockReplaced(y));
				setIceberg = true;
			} else if (chunkBuffer.getBlock(internalX, y, internalZ).isMaterial(biomeConfig.getWaterBlockReplaced(y)) && y > (int)icebergDepth && y < seaLevel && icebergDepth != 0.0D && random.nextDouble() > 0.15D) {
				chunkBuffer.setBlock(internalX, y, internalZ, biomeConfig.getPackedIceBlockReplaced(y));
				setIceberg = true;
			}

			if (chunkBuffer.getBlock(internalX, y, internalZ).isMaterial(biomeConfig.getPackedIceBlockReplaced(y)) && generatedSnow <= snowHeight && y > snowStart) {
				chunkBuffer.setBlock(internalX, y, internalZ, biomeConfig.getSnowBlockReplaced(y));
				++snowHeight;
				setIceberg = true;
			}

			// Prevent generating surface on top of the iceberg
			if (setIceberg) {
				continue;
			}

			// SimpleSurfaceGenerator.spawnColumn logic.
			// TODO: DRY, refactor SimpleSurfaceGenerator.spawnColumn?

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
					groundLayerDepth = dirtDepth;

					// Set when variable was reset
					if (dirtDepth <= 0 && !biomeConfig.isRemoveSurfaceStone())
					{
						useAirForSurface = true;
						useIceForSurface = false;
						useWaterForSurface = false;
						useLayerSurfaceBlockForSurface = false;
						useSandStoneForGround = false;
						useBiomeStoneBlockForGround = true;
						useLayerGroundBlockForGround = false;
					}
					else if ((y >= seaLevel - 4) && (y <= seaLevel + 1))
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
					if (y < seaLevel && y > biomeConfig.getWaterLevelMin())
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

					if (y >= seaLevel - 1)
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
							dirtDepth > 1 &&
							(layerGroundBlockIsSand || (layer == null && biomeGroundBlockIsSand))
						)
						{
							groundLayerDepth = generatingChunk.random.nextInt(4) + Math.max(0, y - seaLevel);
							useSandStoneForGround = true;
						}
					}
				}
			}
			blockOnPreviousPos = blockOnCurrentPos;
		}
	}
	
	@Override
	public String toString()
	{
		StringBuilder stringBuilder = new StringBuilder();
		if(this.layers.size() > 0)
		{
			for (MultipleLayersSurfaceGeneratorLayer groundLayer : this.layers)
			{
				stringBuilder.append(groundLayer.surfaceBlock);
				stringBuilder.append(',').append(' ');
				stringBuilder.append(groundLayer.underWaterSurfaceBlock);
				stringBuilder.append(',').append(' ');				
				stringBuilder.append(groundLayer.groundBlock);
				stringBuilder.append(',').append(' ');
				stringBuilder.append(groundLayer.maxNoise);
				stringBuilder.append(',').append(' ');
			}
			// Delete last ", "
			stringBuilder.deleteCharAt(stringBuilder.length() - 2);
		}
		return "Iceberg " + stringBuilder.toString();
	}

	public static IcebergSurfaceGenerator getFor(String settingValue, IMaterialReader materialReader) throws InvalidConfigException
	{
		if (settingValue.toLowerCase().trim().startsWith("iceberg "))
		{
			String[] parts = StringHelper.readCommaSeperatedString(settingValue.toLowerCase().trim().substring("iceberg ".length()));			
			return new IcebergSurfaceGenerator(parts, materialReader);
		}
		else if(settingValue.toLowerCase().trim().equals("iceberg"))
		{		
			return new IcebergSurfaceGenerator(new String[0], materialReader);			
		}

		// Not iceberg
		return null;
	}
}
