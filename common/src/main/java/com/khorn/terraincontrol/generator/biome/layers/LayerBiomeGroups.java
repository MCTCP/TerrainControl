package com.khorn.terraincontrol.generator.biome.layers;

import com.khorn.terraincontrol.configuration.BiomeGroup;
import com.khorn.terraincontrol.configuration.BiomeGroupManager;
import com.khorn.terraincontrol.generator.biome.ArraysCache;

import java.util.Map.Entry;
import java.util.SortedMap;

public class LayerBiomeGroups extends Layer
{

    private BiomeGroupManager biomeGroupManager;
    private int depth;
    private boolean freezeGroups;

    public LayerBiomeGroups(Layer paramGenLayer, BiomeGroupManager biomeGroups, int depth, boolean freezeGroups)
    {
        this.child = paramGenLayer;
        this.biomeGroupManager = biomeGroups;
        this.depth = depth;
        this.freezeGroups = freezeGroups;
    }

    @Override
    public int[] getInts(ArraysCache arraysCache, int x, int z, int x_size, int z_size)
    {
        int[] childInts = this.child.getInts(arraysCache, x, z, x_size, z_size);
        int[] thisInts = arraysCache.getArray(x_size * z_size);
        
        for (int i = 0; i < z_size; i++)
        {
            for (int j = 0; j < x_size; j++)
            {
                initGroupSeed(j + x, i + z);
                int currentPiece = childInts[(j + i * x_size)];               

                if ((currentPiece & LandBit) != 0 && (currentPiece & BiomeGroupBits) == 0)    // land without biome group
                {
                    SortedMap<Integer, BiomeGroup> possibleGroups = biomeGroupManager.getGroupDepthMap(depth);
                    int newGroupRarity = nextGroupInt(BiomeGroupManager.getMaxRarityFromPossibles(possibleGroups)*entropy);
                        //>>	Spawn the biome based on the rarity spectrum
                        for (Entry<Integer, BiomeGroup> group : possibleGroups.entrySet())
                        {
                            if (newGroupRarity/entropy < group.getKey())
                            {   
                                if (group.getValue() != null){
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