package com.pg85.otg.gen.biome.layers;

import java.util.Arrays;
import java.util.List;

import com.pg85.otg.gen.biome.layers.type.ParentedLayer;
import com.pg85.otg.gen.biome.layers.util.LayerSampleContext;
import com.pg85.otg.gen.biome.layers.util.LayerSampler;

public class BiomeBorderLayer implements ParentedLayer
{ 
    boolean[][] bordersFrom = new boolean[1024][];
    private int[] bordersTo = new int[1024];
	
	public BiomeBorderLayer(BordersList borderBiomes)
    {
		this.bordersFrom = borderBiomes.bordersFrom;
		this.bordersTo = borderBiomes.bordersTo;
    }
	
	public static class BordersList
	{
	    boolean[][] bordersFrom = new boolean[1024][];
	    private int[] bordersTo = new int[1024];
						
		public void addBorder(int biomeId, int biomeParent, List<Integer> notBorderNearBiomes)
		{
			this.bordersFrom[biomeParent] = new boolean[1024];
			Arrays.fill(this.bordersFrom[biomeParent], true);
	        for (int notBorderNearBiome : notBorderNearBiomes)
	        {           
	            this.bordersFrom[biomeParent][notBorderNearBiome] = false;
	        }
	        this.bordersTo[biomeParent] = biomeId;
		}
	}
	
	@Override
	public int sample(LayerSampleContext<?> context, LayerSampler parent, int x, int z)
	{
        int center = parent.sample(x, z);
        int cCheck = (center & BiomeLayers.BIOME_BITS);
               
        if (bordersFrom[cCheck] != null)
        {
            int northCheck = (parent.sample(x, z - 1) & BiomeLayers.BIOME_BITS);
            int southCheck = (parent.sample(x + 1, z) & BiomeLayers.BIOME_BITS);
            int eastCheck = (parent.sample(x, z + 1) & BiomeLayers.BIOME_BITS);
            int westCheck = (parent.sample(x - 1, z) & BiomeLayers.BIOME_BITS);
            boolean[] biomeFrom = bordersFrom[cCheck];
            
            if (biomeFrom[northCheck] && biomeFrom[eastCheck] && biomeFrom[westCheck] && biomeFrom[southCheck])
            {
                if ((northCheck != cCheck) || (eastCheck != cCheck) || (westCheck != cCheck) || (southCheck != cCheck))
                {
                    // if it is suitable, set the edge biome
                	center = 
            			(
            			center & (
        					BiomeLayers.ISLAND_BIT | 
        					//RiverBits | 
        					BiomeLayers.ICE_BIT
	    					)
	        			) | 
	        			BiomeLayers.LAND_BIT | 
	        			bordersTo[cCheck]
        			;
                } else {
	                // if it's not suitable, try again but sample in an X formation to make sure we didn't miss any potential edge
	                int nwCheck = parent.sample(x - 1, z - 1) & BiomeLayers.BIOME_BITS;
	                int neCheck = parent.sample(x + 1, z - 1) & BiomeLayers.BIOME_BITS;
	                int swCheck = parent.sample(x - 1, z + 1) & BiomeLayers.BIOME_BITS;
	                int seCheck = parent.sample(x + 1, z + 1) & BiomeLayers.BIOME_BITS;
	                
	                if (biomeFrom[nwCheck] && biomeFrom[neCheck] && biomeFrom[swCheck] && biomeFrom[seCheck])
	                {
	                    if ((nwCheck != cCheck) || (neCheck != cCheck) || (swCheck != cCheck) || (seCheck != cCheck))
	                    {
	                        // if the second test is suitable, set the edge biome
	                    	center = 
	                    		(
                    				center & (
	            						BiomeLayers.ISLAND_BIT 
	            						//| RiverBits 
	            						| BiomeLayers.ICE_BIT
	        						)
	            				)
	                    		| BiomeLayers.LAND_BIT 
	                    		| bordersTo[cCheck]
	        				;
	                    }
	                    // if the selection isn't suitable at this point, we can be almost certain that it's not an edge
	                }
                }
            }
        }
        
        return center;
	}
}
