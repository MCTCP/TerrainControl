package com.pg85.otg.gen.resource;

import com.pg85.otg.config.biome.ResourceBase;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.helpers.RandomHelper;
import com.pg85.otg.util.interfaces.IBiomeConfig;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IWorldGenRegion;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.MaterialSet;

import java.util.List;
import java.util.Random;

public class VeinResource extends ResourceBase implements IBasicResource
{
	final LocalMaterialData material;
	final int maxAltitude;
	final int minAltitude;
	final int oreFrequency;
	final int oreRarity;
	final int oreAvgSize;
	final MaterialSet sourceBlocks;
	private final int maxSizeInBlocks;
	private final int minSizeInBlocks;		
	private final double veinRarity;

	public VeinResource(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
	{
		super(biomeConfig, args, logger, materialReader);
		assureSize(9, args);

		this.material = materialReader.readMaterial(args.get(0));
		this.minSizeInBlocks = readInt(args.get(1), 10, 200);
		this.maxSizeInBlocks = readInt(args.get(2), this.minSizeInBlocks, 201);
		this.veinRarity = readDouble(args.get(3), 0.0000001, 100);
		this.oreAvgSize = readInt(args.get(4), 1, 64);
		this.oreFrequency = readInt(args.get(5), 1, 100);
		this.oreRarity = readInt(args.get(6), 1, 100);
		this.minAltitude = readInt(args.get(7), Constants.WORLD_DEPTH, Constants.WORLD_HEIGHT - 1);
		this.maxAltitude = readInt(args.get(8), this.minAltitude, Constants.WORLD_HEIGHT - 1);
		this.sourceBlocks = readMaterials(args, 9, materialReader);
	}

	/**
	 * @return The vein that starts in the chunk, or null.
	 */
	private Vein getVeinStartInChunk(IWorldGenRegion worldGenRegion, int chunkX, int chunkZ)
	{
		// Create a random generator that is constant for this chunk and vein
		Random random = RandomHelper.getRandomForCoords(chunkX, chunkZ, this.material.hashCode() * (this.minSizeInBlocks + this.maxSizeInBlocks + 100) + worldGenRegion.getSeed());

		if (random.nextDouble() * 100.0 < this.veinRarity)
		{
			int veinX = chunkX * 16 + random.nextInt(16) + 8;
			int veinY = RandomHelper.numberInRange(random, this.minAltitude, this.maxAltitude);
			int veinZ = chunkZ * 16 + random.nextInt(16) + 8;
			int veinSize = RandomHelper.numberInRange(random, this.minSizeInBlocks, this.maxSizeInBlocks);
			return new Vein(veinX, veinY, veinZ, veinSize);
		}

		return null;
	}

	@Override
	public void spawnForChunkDecoration(IWorldGenRegion worldGenRegion, Random random, boolean villageInChunk, ChunkCoordinate chunkBeingPopulated, ILogger logger, IMaterialReader materialReader)
	{
		// Find all veins that reach this chunk, and spawn them
		int searchRadius = (this.maxSizeInBlocks + 15) / 16;
		
		if(worldGenRegion.getWorldConfig().isDisableOreGen())
		{
			if(this.material.isOre())
			{
				return;
			}
		}
		
		int currentChunkX = chunkBeingPopulated.getChunkX();
		int currentChunkZ = chunkBeingPopulated.getChunkZ();
		Vein vein;
		for (int searchChunkX = currentChunkX - searchRadius; searchChunkX < currentChunkX + searchRadius; searchChunkX++)
		{
			for (int searchChunkZ = currentChunkZ - searchRadius; searchChunkZ < currentChunkZ + searchRadius; searchChunkZ++)
			{
				vein = getVeinStartInChunk(worldGenRegion, searchChunkX, searchChunkZ);
				if (vein != null && vein.reachesChunk(currentChunkX, currentChunkZ))
				{
					vein.spawn(worldGenRegion, random, chunkBeingPopulated, this);
				}
			}
		}
	}

	@Override
	public String toString()
	{
		return "Vein(" + 
			this.material + "," + 
			this.minSizeInBlocks + "," + 
			this.maxSizeInBlocks + "," + 
			this.veinRarity + "," + 
			this.oreAvgSize + "," + 
			this.oreFrequency + "," + 
			this.oreRarity + "," + 
			this.minAltitude + "," + 
			this.maxAltitude + 
			makeMaterials(this.sourceBlocks) + 
		")";
	}	
}
