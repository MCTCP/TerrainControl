
package com.khorn.terraincontrol.generator.biome.layers;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.generator.biome.ArraysCache;

/**
 * Older revision of {@link LayerBiome}, created to simulate the old behaviour.
 *
 */
public class LayerBiomeBeforeGroups extends Layer
{

    private LocalBiome[] biomes;
    private LocalBiome[] ice_biomes;

    public LayerBiomeBeforeGroups(long seed, Layer childLayer, LocalBiome[] biomes, LocalBiome[] ice_biomes)
    {
        super(seed);
        this.child = childLayer;
        this.biomes = biomes;
        this.ice_biomes = ice_biomes;
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

                if ((currentPiece & BiomeBits) == 0)    // without biome
                {
                    if (this.biomes.length > 0 && (currentPiece & IceBit) == 0) // Normal
                                                                                // Biome
                    {
                        LocalBiome biome = this.biomes[nextInt(this.biomes.length)];
                        if (biome != null)
                            currentPiece |= biome.getIds().getGenerationId();
                    } else if (this.ice_biomes.length > 0 && (currentPiece & IceBit) != 0) // Ice
                                                                                           // biome
                    {
                        LocalBiome biome = this.ice_biomes[nextInt(this.ice_biomes.length)];
                        if (biome != null)
                            currentPiece |= biome.getIds().getGenerationId();
                    }
                }
                thisInts[(j + i * xSize)] = currentPiece;
            }
        }
        return thisInts;
    }

}

