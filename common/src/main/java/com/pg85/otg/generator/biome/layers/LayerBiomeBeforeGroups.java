
package com.pg85.otg.generator.biome.layers;

import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.generator.biome.ArraysCache;

/**
 * Older revision of {@link LayerBiome}, created to simulate the old behaviour.
 *
 */
public class LayerBiomeBeforeGroups extends Layer
{

    private LocalBiome[] biomes;
    private LocalBiome[] ice_biomes;

    LayerBiomeBeforeGroups(long seed, Layer childLayer, LocalBiome[] biomes, LocalBiome[] ice_biomes)
    {
        super(seed);
        this.child = childLayer;
        this.biomes = biomes;
        this.ice_biomes = ice_biomes;
    }

    @Override
    public int[] getInts(LocalWorld world, ArraysCache cache, int x, int z, int xSize, int zSize)
    {
        int[] childInts = this.child.getInts(world, cache, x, z, xSize, zSize);
        int[] thisInts = cache.getArray(xSize * zSize);

        int currentPiece;
        LocalBiome biome;
        for (int i = 0; i < zSize; i++)
        {
            for (int j = 0; j < xSize; j++)
            {
                initChunkSeed(j + x, i + z);
                currentPiece = childInts[(j + i * xSize)];

                if ((currentPiece & BiomeBitsAreSetBit) == 0) // without biome
                {
                    if (this.biomes.length > 0 && (currentPiece & IceBit) == 0) // Normal biome
                    {
                        biome = this.biomes[nextInt(this.biomes.length)];
                        if (biome != null)
                        {
                            currentPiece |= biome.getIds().getOTGBiomeId() | BiomeBitsAreSetBit;
                        }
                    }
                    else if (this.ice_biomes.length > 0 && (currentPiece & IceBit) != 0) // Ice biome
                    {
                        biome = this.ice_biomes[nextInt(this.ice_biomes.length)];
                        if (biome != null)
                        {
                            currentPiece |= biome.getIds().getOTGBiomeId() | BiomeBitsAreSetBit;
                        }
                    }
                }            

                thisInts[(j + i * xSize)] = currentPiece;
            }
        }
        return thisInts;
    }

}

