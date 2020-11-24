package com.pg85.otg.gen.biome.layers;

import static com.pg85.otg.gen.biome.layers.BiomeLayers.BIOME_BITS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.pg85.otg.gen.biome.NewBiomeData;
import com.pg85.otg.gen.biome.layers.util.LayerSampleContext;
import com.pg85.otg.gen.biome.layers.util.LayerSampler;

/**
 * Places the biomes at a specific depth, given the biome groups.
 */
class BiomeBeforeGroupsLayer extends BiomeLayerBase
{
	private final NewBiomeGroup normalGroup;
	private final NewBiomeGroup iceGroup;
	
	BiomeBeforeGroupsLayer(BiomeLayerData data, int depth)
	{
		super(data, depth);

		NewBiomeGroup configNormalGroup = this.data.groupRegistry.get(1);
		NewBiomeGroup configIceGroup = this.data.groupRegistry.get(2);
		this.normalGroup = new NewBiomeGroup();
		this.normalGroup.totalGroupRarity = configNormalGroup.totalGroupRarity;
		this.iceGroup = new NewBiomeGroup();
		this.iceGroup.totalGroupRarity = configIceGroup.totalGroupRarity;
		
        List<NewBiomeData> normalBiomes= new ArrayList<NewBiomeData>();
        List<NewBiomeData> iceBiomes= new ArrayList<NewBiomeData>();
        
        for (NewBiomeData biome : configNormalGroup.biomes)
        {
            if (biome.biomeSize != depth)
            {
                continue;
            }
            for (int t = 0; t < biome.rarity; t++)
            {
            	normalBiomes.add(biome);
            }
            this.normalGroup.totalGroupRarity -= biome.rarity;
        }
        if (!normalBiomes.isEmpty())
        {
        	this.normalGroup.biomes = Arrays.asList(normalBiomes.toArray(new NewBiomeData[normalBiomes.size() + this.normalGroup.totalGroupRarity]));
        }
        
        for (NewBiomeData biome : configIceGroup.biomes)
        {
            if (biome.biomeSize != depth)
            {
                continue;
            }
            for (int t = 0; t < biome.rarity; t++)
            {
            	iceBiomes.add(biome);
            }
            this.iceGroup.totalGroupRarity -= biome.rarity;
        }
        if (!iceBiomes.isEmpty())
        {
        	this.iceGroup.biomes = Arrays.asList(iceBiomes.toArray(new NewBiomeData[iceBiomes.size() + this.iceGroup.totalGroupRarity]));
        }
	}

	@Override
	public int sample(LayerSampleContext<?> context, LayerSampler parent, int x, int z)
	{
		int sample = parent.sample(x, z);

		if (
			// If biome bits have not yet been set (this column has not been cached), do so now.
			(sample & BIOME_BITS) == 0 && 
			BiomeLayers.isLand(sample)
		)
		{
			NewBiomeData biomeData = null;
			if (this.normalGroup.biomes.size() > 0 && (sample & BiomeLayers.ICE_BIT) == 0)
			{
				biomeData = this.normalGroup.biomes.get(context.nextInt(this.normalGroup.biomes.size()));
			}
            if (this.iceGroup.biomes.size() > 0 && (sample & BiomeLayers.ICE_BIT) != 0)
            {
            	biomeData = this.iceGroup.biomes.get(context.nextInt(this.iceGroup.biomes.size()));
            }
            if (biomeData != null)
            {
                return sample | biomeData.id |
                    // Set IceBit based on Biome Temperature
                    (biomeData.biomeTemperature <= this.data.frozenOceanTemperature ? BiomeLayers.ICE_BIT : 0)
                ;
            }
		}

		return sample;
	}
}
