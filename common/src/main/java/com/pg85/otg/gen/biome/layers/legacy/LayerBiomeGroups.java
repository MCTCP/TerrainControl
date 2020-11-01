package com.pg85.otg.gen.biome.layers.legacy;

import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.config.biome.BiomeGroup;
import com.pg85.otg.config.biome.BiomeGroupManager;
import com.pg85.otg.gen.biome.ArraysCache;

import java.util.Map.Entry;
import java.util.SortedMap;

public class LayerBiomeGroups extends Layer
{

    private BiomeGroupManager biomeGroupManager;
    private int depth;
    private boolean freezeGroups;

    LayerBiomeGroups(Layer paramGenLayer, BiomeGroupManager biomeGroups, int depth, boolean freezeGroups)
    {
        this.child = paramGenLayer;
        this.biomeGroupManager = biomeGroups;
        this.depth = depth;
        this.freezeGroups = freezeGroups;
    }

    @Override
    public int[] getInts(LocalWorld world, ArraysCache arraysCache, int x, int z, int x_size, int z_size)
    {
        int[] childInts = this.child.getInts(world, arraysCache, x, z, x_size, z_size);
        int[] thisInts = arraysCache.getArray(x_size * z_size);

        int currentPiece;
        SortedMap<Integer, BiomeGroup> possibleGroups;
        int newGroupRarity;
        boolean improvedBiomeGroups = world.getConfigs().getWorldConfig().improvedBiomeGroups;
        for (int i = 0; i < z_size; i++)
        {
            for (int j = 0; j < x_size; j++)
            {
            	if(improvedBiomeGroups)
            	{
            		initChunkSeed(j + x, i + z);
            	}
                initGroupSeed(j + x, i + z);
            	
                currentPiece = childInts[(j + i * x_size)];

                if ((currentPiece & LandBit) != 0 && (currentPiece & BiomeGroupBits) == 0) // land without biome group
                {
                	// TODO: even with rarity 1 this always spawns the biome

                    possibleGroups = biomeGroupManager.getGroupDepthMap(depth);
                    if(improvedBiomeGroups)
                    {
	                    newGroupRarity = nextGroupInt(BiomeGroupManager.getMaxRarityFromPossibles(possibleGroups));
                    } else {
	                    newGroupRarity = nextGroupInt(BiomeGroupManager.getMaxRarityFromPossibles(possibleGroups)*Entropy);                    	
                    }
                    //>>	Spawn the biome based on the rarity spectrum
                    for (Entry<Integer, BiomeGroup> group : possibleGroups.entrySet())
                    {
                        if (
                    		(!improvedBiomeGroups && newGroupRarity/Entropy < group.getKey()) ||
                    		(improvedBiomeGroups && (newGroupRarity < group.getKey()))
                		)
                        {
                            if (group.getValue() != null)
                            {
                                currentPiece |= (group.getValue().getGroupId() << BiomeGroupShift) |
                                //>>	If the average temp of the group is cold
                                ((group.getValue().isColdGroup() && freezeGroups) ? IceBit : 0);
                            }
                            break;
                        }
                    }
                }
                thisInts[(j + i * x_size)] = currentPiece;
            }
        }
        return thisInts;
    }
}