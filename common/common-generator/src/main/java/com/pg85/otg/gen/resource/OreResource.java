package com.pg85.otg.gen.resource;

import com.pg85.otg.config.biome.ResourceBase;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.helpers.MathHelper;
import com.pg85.otg.util.helpers.RandomHelper;
import com.pg85.otg.util.interfaces.IBiomeConfig;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IWorldGenRegion;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.MaterialSet;

import java.util.List;
import java.util.Random;

/**
 * Generates an ore structure by placing multiple spheres along a line.
 */
public class OreResource  extends ResourceBase implements IBasicResource
{
	private final int frequency;
	private final double rarity;
	private final LocalMaterialData material;
	private final int maxAltitude;
	private final int maxSize;
	private final int minAltitude;
	private final MaterialSet sourceBlocks;

	public OreResource(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
	{
		super(biomeConfig, args, logger, materialReader);
		assureSize(7, args);

		this.material = materialReader.readMaterial(args.get(0));
		this.maxSize = readInt(args.get(1), 1, 128);
		this.frequency = readInt(args.get(2), 1, 100);
		this.rarity = readRarity(args.get(3));
		this.minAltitude = readInt(args.get(4), Constants.WORLD_DEPTH, Constants.WORLD_HEIGHT - 1);
		this.maxAltitude = readInt(args.get(5), this.minAltitude, Constants.WORLD_HEIGHT - 1);
		this.sourceBlocks = readMaterials(args, 6, materialReader);
	}

	@Override
	public void spawnForChunkDecoration(IWorldGenRegion worldGenRegion, Random random, boolean villageInChunk, ChunkCoordinate chunkBeingDecorated, ILogger logger, IMaterialReader materialReader)
	{
		// Override spawnInChunk so we can add a cache.
		
		// use a byte since y is always between 0-255
		byte[][] highestBlocksCache = new byte[32][32];

		int chunkX = chunkBeingDecorated.getBlockXCenter();
		int chunkZ = chunkBeingDecorated.getBlockZCenter();		
		
		int x;
		int z;
		for (int t = 0; t < this.frequency; t++)
		{
			if (random.nextDouble() * 100.0 > this.rarity)
			{
				continue;
			}
			x = chunkX + random.nextInt(ChunkCoordinate.CHUNK_SIZE);
			z = chunkZ + random.nextInt(ChunkCoordinate.CHUNK_SIZE);
			spawn(worldGenRegion, random, false, x, z, chunkBeingDecorated, highestBlocksCache);
		}
	}
	
	public void spawn(IWorldGenRegion worldGenregion, Random rand, boolean villageInChunk, int x, int z, ChunkCoordinate chunkBeingDecorated, byte[][] highestBlocksCache)
	{
		if(worldGenregion.getWorldConfig().isDisableOreGen())
		{
			if(this.material.isOre())
			{
				return;
			}
		}
		
		int y = RandomHelper.numberInRange(rand, this.minAltitude, this.maxAltitude);
		
		float f = rand.nextFloat() * (float)Math.PI;
		double d0 = (double)((float)(x + 8) + MathHelper.sin(f) * (float)this.maxSize / 8.0F);
		double d1 = (double)((float)(x + 8) - MathHelper.sin(f) * (float)this.maxSize / 8.0F);
		double d2 = (double)((float)(z + 8) + MathHelper.cos(f) * (float)this.maxSize / 8.0F);
		double d3 = (double)((float)(z + 8) - MathHelper.cos(f) * (float)this.maxSize / 8.0F);
		double d4 = (double)(y + rand.nextInt(3) - 2);
		double d5 = (double)(y + rand.nextInt(3) - 2);
		
		float iFactor;
		double d6;
		double d7;
		double d8;

		double d9;
		double d10;
		double d11;

		int j;
		int k;
		int l;

		int i1;
		int j1;
		int k1; 
		
		double d13;
		double d14;
		double d15;
		
		LocalMaterialData material;
		int highestSolidBlock;			
		
		int areaBeingDecoratedSize = 32;
				
		// TODO: This seems to be really poorly optimised, re-design this.
		for (int i = 0; i < this.maxSize; i++)
		{
			iFactor = (float) i / (float) this.maxSize;
			d6 = d0 + (d1 - d0) * (double)iFactor;
			d7 = d4 + (d5 - d4) * (double)iFactor;
			d8 = d2 + (d3 - d2) * (double)iFactor;

			d9 = rand.nextDouble() * (double)this.maxSize / 16.0D;
			d10 = (double)(MathHelper.sin((float)Math.PI * iFactor) + 1.0F) * d9 + 1.0D;
			d11 = (double)(MathHelper.sin((float)Math.PI * iFactor) + 1.0F) * d9 + 1.0D;
			
			j = MathHelper.floor(d6 - d10 / 2.0D);
			k = MathHelper.floor(d7 - d11 / 2.0D);
			l = MathHelper.floor(d8 - d10 / 2.0D);

			i1 = MathHelper.floor(d6 + d10 / 2.0D);
			j1 = MathHelper.floor(d7 + d11 / 2.0D);
			k1 = MathHelper.floor(d8 + d10 / 2.0D);			
			
			if(j < chunkBeingDecorated.getBlockX())
			{
				continue;
			}
			if(j > chunkBeingDecorated.getBlockX() + areaBeingDecoratedSize - 1)
			{
				continue;
			}
			if(i1 < chunkBeingDecorated.getBlockX())
			{
				continue;
			}
			if(i1 > chunkBeingDecorated.getBlockX() + areaBeingDecoratedSize - 1)
			{
				continue;
			}
			
			if(l < chunkBeingDecorated.getBlockZ())
			{
				continue;
			}
			if(l > chunkBeingDecorated.getBlockZ() + areaBeingDecoratedSize - 1)
			{
				continue;
			}
			if(k1 < chunkBeingDecorated.getBlockZ())
			{
				continue;
			}
			if(k1 > chunkBeingDecorated.getBlockZ() + areaBeingDecoratedSize - 1)
			{
				continue;
			}
			
			if(k < Constants.WORLD_DEPTH)
			{
				continue;
			}
			if(k > Constants.WORLD_HEIGHT - 1)
			{
				continue;
			}
			
			for (int i3 = j; i3 <= i1; i3++)
			{
				d13 = ((double)i3 + 0.5D - d6) / (d10 / 2.0D);
				if (d13 * d13 < 1.0D)
				{					
					for (int i5 = l; i5 <= k1; i5++)
					{
						if(j1 > 63) // Optimisation, don't look for highestblock if we're already looking below 63, default worlds have base terrain height at 63.
						{
							highestSolidBlock = highestBlocksCache[i3 - chunkBeingDecorated.getBlockX()][i5 - chunkBeingDecorated.getBlockZ()] & 0xFF; // byte to int conversion
							if(highestSolidBlock == 0)  // 0 is default / unset.
							{
								highestSolidBlock = worldGenregion.getHeightMapHeight(i3, i5, chunkBeingDecorated);
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
								highestBlocksCache[i3 - chunkBeingDecorated.getBlockX()][i5 - chunkBeingDecorated.getBlockZ()] = (byte)highestSolidBlock;
							}
							if(j1 > highestSolidBlock)
							{
								j1 = highestSolidBlock;
							}
						}
						for (int i4 = k; i4 <= j1; i4++)
						{
							d14 = ((double)i4 + 0.5D - d7) / (d11 / 2.0D);
							if (d13 * d13 + d14 * d14 < 1.0D)
							{
								d15 = ((double)i5 + 0.5D - d8) / (d10 / 2.0D);
								if((d13 * d13 + d14 * d14 + d15 * d15 < 1.0D))
								{
									material = worldGenregion.getMaterial(i3, i4, i5, chunkBeingDecorated);
									if(this.sourceBlocks.contains(material))
									{
										worldGenregion.setBlock(i3, i4, i5, this.material, null, chunkBeingDecorated, true);
									}
								}
							}
						}
					}
				}
			}
		}
	}

	@Override
	public String toString()
	{
		return "Ore(" + this.material + "," + this.maxSize + "," + this.frequency + "," + this.rarity + "," + this.minAltitude + "," + this.maxAltitude + makeMaterials(this.sourceBlocks) + ")";
	}	
}
