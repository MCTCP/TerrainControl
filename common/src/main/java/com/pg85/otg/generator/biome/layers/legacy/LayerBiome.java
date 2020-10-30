package com.pg85.otg.generator.biome.layers.legacy;

import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.biome.BiomeGroup;
import com.pg85.otg.configuration.biome.BiomeGroupManager;
import com.pg85.otg.generator.biome.ArraysCache;

import java.util.Map.Entry;
import java.util.SortedMap;

public class LayerBiome extends Layer
{
    private BiomeGroupManager manager;
    private int depth;
    private double freezeTemp;

    LayerBiome(long seed, int defaultOceanId, Layer childLayer, BiomeGroupManager groupManager, int depth, double freezeTemp)
    {
        super(seed, defaultOceanId);
        this.child = childLayer;
        this.manager = groupManager;
        this.depth = depth;
        this.freezeTemp = freezeTemp;
    }

    @Override
    public int[] getInts(LocalWorld world, ArraysCache cache, int x, int z, int xSize, int zSize)
    {
        int[] childInts = this.child.getInts(world, cache, x, z, xSize, zSize);
        int[] thisInts = cache.getArray(xSize * zSize);

        SortedMap<Integer, LocalBiome> possibleBiomes;
        BiomeGroup group;
        int currentPiece;
        int newBiomeRarity;
        
        for (int i = 0; i < zSize; i++)
        {
            for (int j = 0; j < xSize; j++)
            {
                initChunkSeed(j + x, i + z);
                currentPiece = childInts[(j + i * xSize)];

                if ((currentPiece & BiomeGroupBits) != 0 && ((currentPiece & BiomeBitsAreSetBit) == 0 || (currentPiece & BiomeBits) == this.defaultOceanId))    // has biomegroup bits but not biome bits
                {
                    group = manager.getGroupById((currentPiece & BiomeGroupBits) >> BiomeGroupShift);
                    possibleBiomes = group.getDepthMapOrHigher(depth);
                    // Get Max Rarity
                    if (!possibleBiomes.isEmpty())
                    {
                        newBiomeRarity = nextInt(BiomeGroupManager.getMaxRarityFromPossibles(possibleBiomes));
                        // Spawn the biome based on the rarity spectrum
                        for (Entry<Integer, LocalBiome> biome : possibleBiomes.entrySet())
                        {
                            if (newBiomeRarity < biome.getKey())
                            {
                                if (biome.getValue() != null && biome.getValue().getBiomeConfig().biomeSize == this.depth)
                                {
                                    currentPiece |= biome.getValue().getIds().getOTGBiomeId() |
                                        // Set IceBit based on Biome Temperature
                                        (biome.getValue().getBiomeConfig().biomeTemperature <= freezeTemp ? IceBit : 0) |
                                        // Set BiomeBitsAreSetBit
                                        BiomeBitsAreSetBit
                                        ;
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
