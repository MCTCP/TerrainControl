package com.pg85.otg.gen.resource;

import com.pg85.otg.config.biome.BiomeResourceBase;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IWorldGenRegion;
import com.pg85.otg.util.helpers.MathHelper;
import com.pg85.otg.util.helpers.RandomHelper;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.MaterialSet;

import java.util.List;
import java.util.Random;

/**
 * Generates an ore structure by placing multiple spheres along a line.
 */
public class OreResource extends BiomeResourceBase implements IBasicResource
{
	private final int frequency;
	private final double rarity;
	private final LocalMaterialData material;
	private final int maxAltitude;
	private final int numberOfBlocks;
	private final int minAltitude;
	private final MaterialSet sourceBlocks;
	private final boolean useExtendedParams;	
	private final int maxSpawn;	

	public OreResource(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
	{
		super(biomeConfig, args, logger, materialReader);
		assureSize(7, args);

		this.material = materialReader.readMaterial(args.get(0));
		this.numberOfBlocks = readInt(args.get(1), 1, 128);
		this.frequency = readInt(args.get(2), 1, 100);
		this.rarity = readRarity(args.get(3));
		this.minAltitude = readInt(args.get(4), Constants.WORLD_DEPTH, Constants.WORLD_HEIGHT - 1);
		this.maxAltitude = readInt(args.get(5), this.minAltitude, Constants.WORLD_HEIGHT - 1);

		// If there is a boolean parameter "true" after source blocks, read extended parameters (maxSpawn)
		boolean useExtendedParams = false;		
		int maxSpawn = 0;
		if(args.get(args.size() - 2).toLowerCase().trim().equals("true"))
		{
			try
			{
				maxSpawn = readInt(args.get(args.size() - 1), 0, Integer.MAX_VALUE);
				// Remove the extended parameters so materials can be read as usual
				args = args.subList(0, args.size() - 2);
				useExtendedParams = true;
			}
			catch (InvalidConfigException ex) { }
		}
		this.useExtendedParams = useExtendedParams;
		this.maxSpawn = maxSpawn;

		this.sourceBlocks = readMaterials(args, 6, materialReader);
	}

	@Override
	public void spawnForChunkDecoration(IWorldGenRegion worldGenRegion, Random random, ILogger logger, IMaterialReader materialReader)
	{
		// Override spawnForChunkDecoration so we can add a cache.
		
		// TODO: Remove this offset for 1.16?
		int chunkX = worldGenRegion.getDecorationArea().getChunkBeingDecoratedCenterX();
		int chunkZ = worldGenRegion.getDecorationArea().getChunkBeingDecoratedCenterZ();		
		int startX = worldGenRegion.getDecorationArea().getLeft();
		int startZ = worldGenRegion.getDecorationArea().getTop();
		int width = worldGenRegion.getDecorationArea().getWidth();
		int height = worldGenRegion.getDecorationArea().getHeight();
		// use a byte since y is always between 0-255
		byte[] highestBlocksCache = new byte[width * height];

		int x;
		int z;
		int spawned = 0;
		for (int t = 0; t < this.frequency; t++)
		{
			if (random.nextDouble() * 100.0 > this.rarity)
			{
				continue;
			}
			x = chunkX + random.nextInt(Constants.CHUNK_SIZE);
			z = chunkZ + random.nextInt(Constants.CHUNK_SIZE);
			if(spawn(worldGenRegion, random, false, x, z, highestBlocksCache, width, height, startX, startZ))
			{
				spawned++;
			}
			if(this.maxSpawn > 0 && spawned == this.maxSpawn)
			{
				return;
			}
		}
	}
	
	public boolean spawn(IWorldGenRegion worldGenRegion, Random rand, boolean villageInChunk, int x, int z, byte[] highestBlocksCache, int width, int height, int startX, int startZ)
	{
		if(worldGenRegion.getWorldConfig().isDisableOreGen())
		{
			if(this.material.isOre())
			{
				return true;
			}
		}

		float randomAngle = rand.nextFloat() * (float)Math.PI;
		double randomX1 = (double)((float)(x) - MathHelper.sin(randomAngle) * (float)this.numberOfBlocks / 8.0F);
		double randomX2 = (double)((float)(x) + MathHelper.sin(randomAngle) * (float)this.numberOfBlocks / 8.0F);
		double randomZ1 = (double)((float)(z) - MathHelper.cos(randomAngle) * (float)this.numberOfBlocks / 8.0F);
		double randomZ2 = (double)((float)(z) + MathHelper.cos(randomAngle) * (float)this.numberOfBlocks / 8.0F);
	
		int randomY = RandomHelper.numberInRange(rand, this.minAltitude, this.maxAltitude);
		double randomY1 = (double)(randomY + rand.nextInt(3) - 2);		
		double randomY2 = (double)(randomY + rand.nextInt(3) - 2);

		float currentNumberOfBlocksFraction;
		double xCenter;
		double yCenter;
		double zCenter;
		double randomSize;
		double xzRadius;
		double yRadius;
		int startWorldX;
		int startWorldY;
		int startWorldZ;
		int endWorldX;
		int endWorldY;
		int endWorldZ;
		double xDistanceToCenterFraction;
		double yDistanceToCenterFraction;
		double zDistanceToCenterFraction;
		int highestSolidBlock;
		boolean hasSpawned = false;

		// NumberOfBlocks determines the radius we try to place ore blobs in, each blob has a 
		// random diameter between 0 and numberOfBlocks / 16, so numberOfBlocks does two things.
		for (int currentNumberOfBlocks = 0; currentNumberOfBlocks < this.numberOfBlocks; currentNumberOfBlocks++)
		{
			currentNumberOfBlocksFraction = (float)currentNumberOfBlocks / (float)this.numberOfBlocks;
			xCenter = randomX2 + (randomX1 - randomX2) * (double)currentNumberOfBlocksFraction;
			yCenter = randomY2 + (randomY1 - randomY2) * (double)currentNumberOfBlocksFraction;
			zCenter = randomZ2 + (randomZ1 - randomZ2) * (double)currentNumberOfBlocksFraction;

			randomSize = rand.nextDouble() * (double)this.numberOfBlocks / 16.0D;
			xzRadius = ((double)(MathHelper.sin((float)Math.PI * currentNumberOfBlocksFraction) + 1.0F) * randomSize + 1.0D) / 2.0D;
			yRadius = ((double)(MathHelper.sin((float)Math.PI * currentNumberOfBlocksFraction) + 1.0F) * randomSize + 1.0D) / 2.0D;

			startWorldX = MathHelper.floor(xCenter - xzRadius);
			startWorldY = MathHelper.floor(yCenter - yRadius);
			startWorldZ = MathHelper.floor(zCenter - xzRadius);

			endWorldX = MathHelper.floor(xCenter + xzRadius);
			endWorldY = MathHelper.floor(yCenter + yRadius);
			endWorldZ = MathHelper.floor(zCenter + xzRadius);

			if(
				!worldGenRegion.getDecorationArea().isInAreaBeingDecorated(startWorldX, startWorldZ) ||
				!worldGenRegion.getDecorationArea().isInAreaBeingDecorated(endWorldX, endWorldZ) ||
				endWorldY < Constants.WORLD_DEPTH ||
				startWorldY >= Constants.WORLD_HEIGHT
			)
			{
				continue;
			}
			
			if(startWorldY < Constants.WORLD_DEPTH)
			{
				startWorldY = Constants.WORLD_DEPTH;
			}
			if(endWorldY >= Constants.WORLD_HEIGHT)
			{
				endWorldY = Constants.WORLD_HEIGHT;
			}

			for (int worldX = startWorldX; worldX <= endWorldX; worldX++)
			{
				xDistanceToCenterFraction = ((double)worldX + 0.5D - xCenter) / xzRadius;
				// Get the distance to the center as a fraction, then square it to get rid of negative values. 
				// The result must be < 1.0D, otherwise it exceeded xzRadius so exclude. 
				if (xDistanceToCenterFraction * xDistanceToCenterFraction < 1.0D)
				{
					for (int worldZ = startWorldZ; worldZ <= endWorldZ; worldZ++)
					{
						zDistanceToCenterFraction = ((double)worldZ + 0.5D - zCenter) / xzRadius;
						// Get the distance to the center as a fraction for xz axes, then square it to get rid of negative values.
						// Add up all squared results, the result must be < 1.0D, otherwise it exceeded xzRadius so exclude.						
						if (xDistanceToCenterFraction * xDistanceToCenterFraction + zDistanceToCenterFraction * zDistanceToCenterFraction < 1.0D)
						{
							// Optimisation, don't look for highestblock if we're already looking below 63, default worlds have base terrain height at 63.
							if(endWorldY > 63) 
							{
								highestSolidBlock = highestBlocksCache[(worldX - startX) * height + (worldZ - startZ)] & 0xFF; // byte to int conversion
								if(highestSolidBlock == 0)  // 0 is default / unset.
								{
									highestSolidBlock = worldGenRegion.getHeightMapHeight(worldX, worldZ);
									// TODO: This causes getHeightMapHeight to be called every time on a 0 height column, 
									// can't use -1 tho since we're using byte arrays. At least we're aborting the column 
									// immediately, since OreGen shouldn't be used to spawn things in empty columns. If
									// that's what you want, make a cloud generator or something, optimised for spawning in 
									// air/void.
									if(highestSolidBlock == -1)
									{
										highestSolidBlock = (byte)0; // Reset
										break;
									}
									highestBlocksCache[(worldX - startX) * height + (worldZ - startZ)] = (byte)highestSolidBlock;
								}
								if(endWorldY > highestSolidBlock)
								{
									endWorldY = highestSolidBlock;
								}
							}
							for (int worldY = startWorldY; worldY <= endWorldY; worldY++)
							{
								yDistanceToCenterFraction = ((double)worldY + 0.5D - yCenter) / yRadius;
								// Get the distance to the center as a fraction for each axis, then square it to get rid of negative values.
								// Add up all squared results, the result must be < 1.0D, otherwise it exceeded xzRadius/yRadius so exclude.
								if (xDistanceToCenterFraction * xDistanceToCenterFraction + yDistanceToCenterFraction * yDistanceToCenterFraction + zDistanceToCenterFraction * zDistanceToCenterFraction < 1.0D)
								{
									if(this.sourceBlocks.contains(worldGenRegion.getMaterial(worldX, worldY, worldZ)))
									{
										worldGenRegion.setBlock(worldX, worldY, worldZ, this.material);
										hasSpawned = true;
									}
								}
							}
						}
					}
				}
			}
		}

		return hasSpawned;
	}

	@Override
	public String toString()
	{
		return "Ore(" + this.material + "," + this.numberOfBlocks + "," + this.frequency + "," + this.rarity + "," + this.minAltitude + "," + this.maxAltitude + makeMaterials(this.sourceBlocks) + (this.useExtendedParams ? ",true," + this.maxSpawn : "") + ")";
	}	
}
