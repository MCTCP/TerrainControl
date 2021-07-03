package com.pg85.otg.gen.biome.layers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.pg85.otg.gen.biome.layers.type.ParentedLayer;
import com.pg85.otg.gen.biome.layers.util.LayerSampleContext;
import com.pg85.otg.gen.biome.layers.util.LayerSampler;

public class BiomeBorderLayer implements ParentedLayer
{ 
	private final BorderedBiome[] borderedBiomes;

	public BiomeBorderLayer(BordersList borderBiomes)
	{
		this.borderedBiomes = borderBiomes.borderedBiomes;
	}
	
	private static class BorderBiome
	{
		private final int id;
		private final boolean[] allowedNearBiomes;
		
		private BorderBiome(int id, boolean[] allowedNearBiomes)
		{
			 this.id = id;
			 this.allowedNearBiomes = allowedNearBiomes;
		}
	}
	
	private static class BorderedBiome
	{
		private final ArrayList<BorderBiome> biomeBorders = new ArrayList<>();	
	}
	
	public static class BordersList
	{
		private final BorderedBiome[] borderedBiomes = new BorderedBiome[1024]; 

		public void addBorder(int biomeId, int borderedBiomeId, List<Integer> borderNearBiomes, List<Integer> notBorderNearBiomes)
		{
			BorderedBiome borderedBiome = this.borderedBiomes[borderedBiomeId];
			if(borderedBiome == null)
			{
				borderedBiome = new BorderedBiome();
				this.borderedBiomes[borderedBiomeId] = borderedBiome;
			}
			
			// If allowedNearBiomes whitelist is present,
			// ignore notAllowedNearBiomes blacklist.
			boolean[] allowedNearBiomes = new boolean[1024];
			if(borderNearBiomes != null && borderNearBiomes.size() > 0)
			{
				Arrays.fill(allowedNearBiomes, false);
				for (int borderNearBiome : borderNearBiomes)
				{			
					allowedNearBiomes[borderNearBiome] = true;
				}
			} else {
				Arrays.fill(allowedNearBiomes, true);
				for (int notBorderNearBiome : notBorderNearBiomes)
				{			
					allowedNearBiomes[notBorderNearBiome] = false;
				}				
			}
			BorderBiome borderBiome = new BorderBiome(biomeId, allowedNearBiomes);
			
			borderedBiome.biomeBorders.add(borderBiome);
		}
	}

	@Override
	public int sample(LayerSampleContext<?> context, LayerSampler parent, int x, int z)
	{
		int center = parent.sample(x, z);
		int cCheck = BiomeLayers.getBiomeFromLayer(center);
	
		// Check if there are border biomes for the biome in the center
		if (this.borderedBiomes[cCheck] != null)
		{
			// Check in a + formation for edges
			int northCheck = BiomeLayers.getBiomeFromLayer(parent.sample(x, z - 1));
			int southCheck = BiomeLayers.getBiomeFromLayer(parent.sample(x + 1, z));
			int eastCheck = BiomeLayers.getBiomeFromLayer(parent.sample(x, z + 1));
			int westCheck = BiomeLayers.getBiomeFromLayer(parent.sample(x - 1, z));			

			int nwCheck = 0;
			int neCheck = 0;
			int swCheck = 0;
			int seCheck = 0;			
			boolean bDiagonalCheckDone = false;

			// For each bordered biome, loop through border biomes untill we can place one.
			for(BorderBiome borderBiome : this.borderedBiomes[cCheck].biomeBorders)
			{
				if (
					borderBiome.allowedNearBiomes[northCheck] && 
					borderBiome.allowedNearBiomes[eastCheck] && 
					borderBiome.allowedNearBiomes[westCheck] && 
					borderBiome.allowedNearBiomes[southCheck]
				)
				{
					// Check if there is a neighbouring biome.
					if (
						(northCheck != cCheck) || 
						(eastCheck != cCheck) || 
						(westCheck != cCheck) || 
						(southCheck != cCheck)
					)
					{
						// Set the border biome
						return 
							(
								center & (
									BiomeLayers.ISLAND_BIT
									| BiomeLayers.RIVER_BITS
									| BiomeLayers.ICE_BIT
								)
							)
							| BiomeLayers.LAND_BIT
							| borderBiome.id
						;
					} else {
						if(!bDiagonalCheckDone)
						{
							// if it's not suitable, try again but sample in an X formation to make sure we didn't miss any potential edge
							nwCheck = BiomeLayers.getBiomeFromLayer(parent.sample(x - 1, z - 1));
							neCheck = BiomeLayers.getBiomeFromLayer(parent.sample(x + 1, z - 1));
							swCheck = BiomeLayers.getBiomeFromLayer(parent.sample(x - 1, z + 1));
							seCheck = BiomeLayers.getBiomeFromLayer(parent.sample(x + 1, z + 1));
							bDiagonalCheckDone = true;
						}

						if (
							borderBiome.allowedNearBiomes[nwCheck] && 
							borderBiome.allowedNearBiomes[neCheck] && 
							borderBiome.allowedNearBiomes[swCheck] && 
							borderBiome.allowedNearBiomes[seCheck]
						)
						{
							// Check if there is a neighbouring biome.
							if (
								(nwCheck != cCheck) || 
								(neCheck != cCheck) || 
								(swCheck != cCheck) || 
								(seCheck != cCheck)
							)
							{
								// Set the border biome
								return 
									(
										center & (
											BiomeLayers.ISLAND_BIT
											| BiomeLayers.RIVER_BITS
											| BiomeLayers.ICE_BIT
										)
									)
									| BiomeLayers.LAND_BIT
									| borderBiome.id
								;
							}
						}
					}
				}
			}
		}
		
		return center;
	}
}
