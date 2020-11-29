package com.pg85.otg.gen.biome.layers;

import java.util.ArrayList;
import java.util.List;

import com.pg85.otg.gen.biome.layers.type.DiagonalCrossSamplingLayer;
import com.pg85.otg.gen.biome.layers.util.LayerSampleContext;

public class BiomeIsleLayer implements DiagonalCrossSamplingLayer
{
	public static class IslesList
	{
		private List<Isle> isles = new ArrayList<>();
		
		public List<Isle> getIsles()
		{
			return this.isles;
		}
		
		public void addIsle(int biome, int chance, boolean[] biomeCanSpawnIn, boolean inOcean)
		{
	        Isle isle = new Isle();
	        isle.biomeId = biome;
	        isle.chance = chance;
	        isle.canSpawnIn = biomeCanSpawnIn;
	        isle.inOcean = inOcean;

	        // Pre-calculate the seeds unique for this layer
	        // (keep in mind that the resulting world seed is based on the base seed)
	        //int rngSeed = isle.biomeId;
	        //isle.scrambledWorldSeed = getScrambledWorldSeed(4000 + rngSeed, this.worldSeed);

	        this.isles.add(isle);
		}
	}

    private static class Isle
    {
        int biomeId;
        int chance = 10;
        boolean[] canSpawnIn = new boolean[1024];
        boolean inOcean = false;
    }

    private final IslesList isleBiomes;
   
    public BiomeIsleLayer(IslesList isleBiomes)
    {
    	this.isleBiomes = isleBiomes;
    }

	@Override
	public int sample(LayerSampleContext<?> context, int x, int z, int sw, int se, int ne, int nw, int center)
	{	
        boolean alreadySpawned;
        int nwCheck;
        int neCheck;
        int swCheck;
        int seCheck;
        int sample = center;
        
        // Start by just copying the biome from the child layer

        // Then decide whether an isle should spawn
        for (Isle isle : this.isleBiomes.getIsles())
        {
            // Make the scrambled world seed unique for each isle
            // (each island used to have its own layer)
            //this.scrambledWorldSeed = isle.scrambledWorldSeed;
            alreadySpawned = false;
                        
            if (isle.inOcean)
            {
                nwCheck = nw & BiomeLayers.LAND_BIT;
                neCheck = ne & BiomeLayers.LAND_BIT;
                swCheck = sw & BiomeLayers.LAND_BIT;
                seCheck = se & BiomeLayers.LAND_BIT;
            	
                if (
            		((center & BiomeLayers.LAND_BIT) == 0) && 
            		(nwCheck == 0) && 
            		(neCheck == 0) && 
            		(swCheck == 0) && 
            		(seCheck == 0) &&
            		context.nextInt(isle.chance) == 0
        		)
                {
                	sample = 
            			(sample & BiomeLayers.ICE_BIT) | 
            			//(sample & RiverBits) | 
            			BiomeLayers.LAND_BIT | 
            			isle.biomeId | 
            			BiomeLayers.ISLAND_BIT
        			;
                    alreadySpawned = true;
                }
            }
            if (!alreadySpawned)
            {
            	int centerCheck = (center & BiomeLayers.BIOME_BITS);
                nwCheck = (nw & BiomeLayers.BIOME_BITS);
                neCheck = (ne & BiomeLayers.BIOME_BITS);
                swCheck = (sw & BiomeLayers.BIOME_BITS);
                seCheck = (se & BiomeLayers.BIOME_BITS);
            	
                if (
            		isle.canSpawnIn[centerCheck] && 
            		isle.canSpawnIn[nwCheck] && 
            		isle.canSpawnIn[neCheck] && 
            		isle.canSpawnIn[swCheck] && 
            		isle.canSpawnIn[seCheck] && 
            		context.nextInt(isle.chance) == 0
        		)
                {
                	sample = 
            			(sample & BiomeLayers.LAND_BIT) | 
            			(sample & BiomeLayers.ICE_BIT) | 
            			//(sample & RiverBits) | 
            			isle.biomeId |
            			BiomeLayers.ISLAND_BIT
        			;
                }
            }
        }
        return sample;
	}	
}
