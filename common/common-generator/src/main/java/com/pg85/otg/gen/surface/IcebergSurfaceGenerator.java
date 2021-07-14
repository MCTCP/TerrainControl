package com.pg85.otg.gen.surface;

import java.util.Random;
import java.util.stream.IntStream;

import com.google.common.collect.ImmutableList;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.gen.noise.OctaveSimplexNoiseSampler;
import com.pg85.otg.interfaces.IBiome;
import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.interfaces.ISurfaceGeneratorNoiseProvider;
import com.pg85.otg.util.gen.ChunkBuffer;
import com.pg85.otg.util.gen.GeneratingChunk;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterials;

public class IcebergSurfaceGenerator implements SurfaceGenerator
{
	private OctaveSimplexNoiseSampler icebergNoise;
	private OctaveSimplexNoiseSampler icebergCutoffNoise;

	@Override
	public void spawn(long worldSeed, GeneratingChunk generatingChunk, ChunkBuffer chunkBuffer, IBiome biome, int x, int z)
	{
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
			chunkBuffer.setBlock(x, generatingChunk.heightCap - 2, z, biomeConfig.getBedrockBlockReplaced(generatingChunk.heightCap - 2));
		}

		double icebergHeight = 0;
		double icebergDepth = 0;
		int seaLevel = generatingChunk.getWaterLevel(x & 0xf, z & 0xf);

		double noise = generatingChunk.getNoise(x & 0xf, z & 0xf);
		float temperature = biome.getTemperatureAt(x, biomeConfig.getWaterLevelMax(), z);
		double icebergNoise = Math.min(Math.abs(noise), this.icebergNoise.sample((double)x * 0.1D, (double)z * 0.1D, false) * 15.0D);

		if (icebergNoise > 1.8D)
		{
			double icebergCutoff = Math.abs(this.icebergCutoffNoise.sample((double) x * 0.09765625D, (double) z * 0.09765625D, false));
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

		int dirtDepth = (int) (generatingChunk.getNoise(x & 0xf, z & 0xf) / 3.0D + 3.0D + generatingChunk.random.nextDouble() * 0.25D);

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
		LocalMaterialData blockOnCurrentPos;

		int topY = chunkBuffer.getHighestBlockForColumn(x & 0xf, z & 0xf);
		for (int y = Math.max(topY, (int)icebergHeight + 1); y >= 0; y--)
		{
			if (generatingChunk.mustCreateBedrockAt(biomeConfig.isFlatBedrock(), biomeConfig.isBedrockDisabled(), biomeConfig.isCeilingBedrock(), y))
			{
				// Place bedrock
				chunkBuffer.setBlock(x, y, z, biomeConfig.getBedrockBlockReplaced(y));
				continue;
			}

			// Store whether we set the iceberg here or not
			boolean setIceberg = false;

			// Place ice above and below the sea level, with a 99% and 85% chance respetively
			if (chunkBuffer.getBlock(x, y, z).isAir() && y < icebergHeight && random.nextDouble() > 0.01D) {
				chunkBuffer.setBlock(x, y, z, LocalMaterials.PACKED_ICE);
				setIceberg = true;
			} else if (chunkBuffer.getBlock(x, y, z).isMaterial(LocalMaterials.WATER) && y > (int)icebergDepth && y < seaLevel && icebergDepth != 0.0D && random.nextDouble() > 0.15D) {
				chunkBuffer.setBlock(x, y, z, LocalMaterials.PACKED_ICE);
				setIceberg = true;
			}

			if (chunkBuffer.getBlock(x, y, z).isMaterial(LocalMaterials.PACKED_ICE) && generatedSnow <= snowHeight && y > snowStart) {
				chunkBuffer.setBlock(x, y, z, LocalMaterials.SNOW_BLOCK);
				++snowHeight;
				setIceberg = true;
			}

			// Prevent generating surface on top of the iceberg
			if (setIceberg) {
				continue;
			}

			// Normal OTG surfacebuilder logic.
			
			// Surface blocks logic (grass, dirt, sand, sandstone)
			blockOnCurrentPos = chunkBuffer.getBlock(x, y, z);
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
							bIsAir = biomeConfig.getSurfaceBlockReplaced(y).isAir();
						}
						if(bIsAir)
						{
							if (biome.getTemperatureAt(x, y, z) < Constants.SNOW_AND_ICE_TEMP)
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
							currentSurfaceBlock = biomeConfig.getSurfaceBlockReplaced(y);
						}

						chunkBuffer.setBlock(x, y, z, currentSurfaceBlock);
					} else {
						if(useBiomeStoneBlockForGround)
						{
							// block should already be the replaced stoneblock
							continue;
						}
						else if(useLayerGroundBlockForGround)
						{
							chunkBuffer.setBlock(x, y, z, biomeConfig.getGroundBlockReplaced(y));
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
							chunkBuffer.setBlock(x, y, z, biomeConfig.getSandStoneBlockReplaced(y));
						} else {
							chunkBuffer.setBlock(x, y, z, biomeConfig.getGroundBlockReplaced(y));
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

						if (groundLayerDepth == 0 && dirtDepth > 1 && biomeGroundBlockIsSand)
						{
							groundLayerDepth = generatingChunk.random.nextInt(4) + Math.max(0, y - seaLevel);
							useSandStoneForGround = true;
						}
					}
				}
			}
		}
	}

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
	public String toString()
	{
		return "Iceberg";
	}

	public static IcebergSurfaceGenerator getFor(String settingValue)
	{
		if (settingValue.equalsIgnoreCase("Iceberg")) {
			return new IcebergSurfaceGenerator();
		}

		// Not iceberg
		return null;
	}
}
