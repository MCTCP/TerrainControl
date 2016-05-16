package com.khorn.terraincontrol.generator.biome.layers;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.configuration.BiomeGroup;
import com.khorn.terraincontrol.configuration.BiomeGroupManager;
import com.khorn.terraincontrol.generator.biome.ArraysCache;

import java.util.Map.Entry;
import java.util.SortedMap;

public class LayerBiome extends Layer
{

    private BiomeGroupManager manager;
    private int depth;
    private double freezeTemp;

    public LayerBiome(long seed, Layer childLayer, BiomeGroupManager groupManager, int depth, double freezeTemp)
    {
        super(seed);
        this.child = childLayer;
        this.manager = groupManager;
        this.depth = depth;
        this.freezeTemp = freezeTemp;
    }

    @Override
    public int[] getInts(ArraysCache cache, int x, int z, int xSize, int zSize)
    {
        int[] childInts = this.child.getInts(cache, x, z, xSize, zSize);
        int[] thisInts = cache.getArray(xSize * zSize);

        for (int i = 0; i < zSize; i++)
        {
            for (int j = 0; j < xSize; j++)
            {
                initChunkSeed(j + x, i + z);
                int currentPiece = childInts[(j + i * xSize)];

                if ((currentPiece & BiomeGroupBits) != 0 && (currentPiece & BiomeBits) == 0)    // has biomegroup bits but not biome bits
                {
                    BiomeGroup group = manager.getGroupById((currentPiece & BiomeGroupBits) >> BiomeGroupShift);
                    SortedMap<Integer, LocalBiome> possibleBiomes = group.getDepthMap(depth);
                    //>>	Get Max Rarity
                    if (!possibleBiomes.isEmpty())
                    {
                        int newBiomeRarity = nextInt(BiomeGroupManager.getMaxRarityFromPossibles(possibleBiomes));
                        //>>	Spawn the biome based on the rarity spectrum
                        for (Entry<Integer, LocalBiome> biome : possibleBiomes.entrySet())
                        {
                            if (newBiomeRarity < biome.getKey())
                            {
                                if (biome.getValue() != null){
                                    currentPiece |= biome.getValue().getIds().getGenerationId() |
                                                    //>>	Set IceBit based on Biome Temperature
                                                    (biome.getValue().getBiomeConfig().biomeTemperature <= freezeTemp ? IceBit : 0);
                                }
                                break;
                            }
                        }
                    }
                }
                thisInts[(j + i * xSize)] = currentPiece;
            }
        }
        return thisInts;
    }

}
