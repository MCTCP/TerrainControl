package com.pg85.otg.gen.resource;

import com.pg85.otg.config.biome.BiomeResourceBase;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.gen.noise.legacy.NoiseGeneratorSurfacePatchOctaves;
import com.pg85.otg.util.biome.ReplaceBlockMatrix;
import com.pg85.otg.util.interfaces.IBiomeConfig;
import com.pg85.otg.util.interfaces.ILogger;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IWorldGenRegion;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.MaterialSet;

import java.util.List;
import java.util.Random;

/**
 * Generates patches based on noise.
 * TODO: Expose the noise weights and scale to the config
 */
@SuppressWarnings("deprecation")
public class SurfacePatchResource  extends BiomeResourceBase implements IBasicResource
{
	private final LocalMaterialData material;
	private final LocalMaterialData decorationAboveReplacements;
	private final int maxAltitude;
	private final int minAltitude;
	/**
	 * To get nice patches, we need our own noise generator here
	 */
	private final NoiseGeneratorSurfacePatchOctaves noiseGen;
	private final Random random;
	private final MaterialSet sourceBlocks;

	public SurfacePatchResource(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
	{
		super(biomeConfig, args, logger, materialReader);
		assureSize(4, args);

		this.material = materialReader.readMaterial(args.get(0));
		this.decorationAboveReplacements = materialReader.readMaterial(args.get(1));
		this.minAltitude = readInt(args.get(2), Constants.WORLD_DEPTH, Constants.WORLD_HEIGHT - 1);
		this.maxAltitude = readInt(args.get(3), this.minAltitude, Constants.WORLD_HEIGHT - 1);
		this.sourceBlocks = readMaterials(args, 4, materialReader);
		this.random = new Random(2345L);
		this.noiseGen = new NoiseGeneratorSurfacePatchOctaves(this.random, 1);
	}

	@Override
	public void spawnForChunkDecoration(IWorldGenRegion worldGenRegion, Random random, boolean villageInChunk, ILogger logger, IMaterialReader materialReader)
	{
		int chunkX = worldGenRegion.getDecorationArea().getChunkBeingDecoratedCenterX();
		int chunkZ = worldGenRegion.getDecorationArea().getChunkBeingDecoratedCenterZ();
		int x;
		int z;
		for (int z0 = 0; z0 < Constants.CHUNK_SIZE; z0++)
		{
			for (int x0 = 0; x0 < Constants.CHUNK_SIZE; x0++)
			{
				x = chunkX + x0;
				z = chunkZ + z0;
				spawn(worldGenRegion, random, false, x, z);
			}
		}
	}
	
	public void spawn(IWorldGenRegion worldGenRegion, Random rand, boolean villageInChunk, int x, int z)
	{
		int y = worldGenRegion.getHighestBlockAboveYAt(x, z) - 1;
		if (y < this.minAltitude || y > this.maxAltitude)
		{
			return;
		}
		
		double yNoise = this.noiseGen.getYNoise(x * 0.25D, z * 0.25D);
		if (yNoise > 0.0D)
		{
			LocalMaterialData materialAtLocation = worldGenRegion.getMaterial(x, y, z);
			if (this.sourceBlocks.contains(materialAtLocation))
			{
				ReplaceBlockMatrix replaceBlocks = worldGenRegion.getBiomeConfigForDecoration(x, z).getReplaceBlocks();
				worldGenRegion.setBlock(x, y, z, this.material, replaceBlocks);
				if (yNoise < 0.12D)
				{
					worldGenRegion.setBlock(x, y + 1, z, this.decorationAboveReplacements, replaceBlocks);
				}
			}
		}
	}
	
	@Override
	public String toString()
	{
		return "SurfacePatch(" + this.material + "," + this.decorationAboveReplacements + "," + this.minAltitude + "," + this.maxAltitude + "," + this.sourceBlocks + ")";
	}	
}
