package com.khorn.terraincontrol.forge;

import com.khorn.terraincontrol.DefaultBiome;
import com.khorn.terraincontrol.biomegenerators.BiomeGenerator;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.gen.structure.MapGenVillage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Actually a biome manager.
 *
 */
public class TCWorldChunkManager extends WorldChunkManager
{
    private BiomeGenerator biomeManager;
    private ArrayList<BiomeGenBase> biomesToSpawnIn = new ArrayList<BiomeGenBase>();

    private SingleWorld localWorld;

    public TCWorldChunkManager(SingleWorld world)
    {
        this.biomesToSpawnIn.add(BiomeGenBase.biomeList[DefaultBiome.FOREST.Id]);
        this.biomesToSpawnIn.add(BiomeGenBase.biomeList[DefaultBiome.PLAINS.Id]);
        this.biomesToSpawnIn.add(BiomeGenBase.biomeList[DefaultBiome.TAIGA.Id]);
        this.biomesToSpawnIn.add(BiomeGenBase.biomeList[DefaultBiome.DESERT_HILLS.Id]);
        this.biomesToSpawnIn.add(BiomeGenBase.biomeList[DefaultBiome.FOREST_HILLS.Id]);
        this.biomesToSpawnIn.add(BiomeGenBase.biomeList[DefaultBiome.JUNGLE.Id]);
        this.biomesToSpawnIn.add(BiomeGenBase.biomeList[DefaultBiome.JUNGLE_HILLS.Id]);

        this.localWorld = world;
    }

    public void setBiomeManager(BiomeGenerator manager)
    {
        this.biomeManager = manager;
    }

    @SuppressWarnings("rawtypes")
    public List a()
    {
        return this.biomesToSpawnIn;
    }

    // get biome
    @Override
    public BiomeGenBase getBiomeGenAt(int paramInt1, int paramInt2)
    {
        return BiomeGenBase.biomeList[biomeManager.getBiome(paramInt1, paramInt2)];
    }

    // rain
    @Override
    public float[] getRainfall(float[] paramArrayOfFloat, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {
        return biomeManager.getRainfall(paramArrayOfFloat, paramInt1, paramInt2, paramInt3, paramInt4);
    }

    // Temperature
    @Override
    public float[] getTemperatures(float[] paramArrayOfFloat, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {
        return biomeManager.getTemperatures(paramArrayOfFloat, paramInt1, paramInt2, paramInt3, paramInt4);
    }

    @Override
    public BiomeGenBase[] getBiomesForGeneration(BiomeGenBase[] paramArrayOfBiomeBase, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {
        int[] arrayOfInt = this.biomeManager.getBiomesUnZoomed(null, paramInt1, paramInt2, paramInt3, paramInt4);
        if (paramArrayOfBiomeBase == null || paramArrayOfBiomeBase.length < arrayOfInt.length)
        {
            paramArrayOfBiomeBase = new BiomeGenBase[arrayOfInt.length];
        }

        for (int i = 0; i < paramInt3 * paramInt4; i++)
        {
            paramArrayOfBiomeBase[i] = BiomeGenBase.biomeList[arrayOfInt[i]];
        }

        return paramArrayOfBiomeBase;
    }

    @Override
    public BiomeGenBase[] getBiomeGenAt(BiomeGenBase[] paramArrayOfBiomeBase, int paramInt1, int paramInt2, int paramInt3, int paramInt4, boolean paramBoolean)
    {
        int[] arrayOfInt = this.biomeManager.getBiomes(null, paramInt1, paramInt2, paramInt3, paramInt4);
        if (paramArrayOfBiomeBase == null || paramArrayOfBiomeBase.length < arrayOfInt.length)
        {
            paramArrayOfBiomeBase = new BiomeGenBase[arrayOfInt.length];
        }

        for (int i = 0; i < paramInt3 * paramInt4; i++)
        {
            paramArrayOfBiomeBase[i] = BiomeGenBase.biomeList[arrayOfInt[i]];
        }

        return paramArrayOfBiomeBase;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean areBiomesViable(int paramInt1, int paramInt2, int paramInt3, List paramList)
    {
        // Hack for StructureVillagePieces.getNextComponentVillagePath(..)
        // (The alternative would be to completely override the village spawn
        // code)
        if (paramList == MapGenVillage.villageSpawnBiomes)
        {
            paramList = localWorld.villageGen.villageSpawnBiomes;
        }

        int i = paramInt1 - paramInt3 >> 2;
        int j = paramInt2 - paramInt3 >> 2;
        int k = paramInt1 + paramInt3 >> 2;
        int m = paramInt2 + paramInt3 >> 2;

        int n = k - i + 1;
        int i1 = m - j + 1;
        BiomeGenBase[] arrayOfInt = this.getBiomesForGeneration(null, i, j, n, i1);
        for (int i2 = 0; i2 < n * i1; i2++)
        {
            BiomeGenBase localBiomeBase = arrayOfInt[i2];
            if (!paramList.contains(localBiomeBase))
                return false;
        }

        return true;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public ChunkPosition findBiomePosition(int paramInt1, int paramInt2, int paramInt3, List paramList, Random paramRandom)
    {
        int i = paramInt1 - paramInt3 >> 2;
        int j = paramInt2 - paramInt3 >> 2;
        int k = paramInt1 + paramInt3 >> 2;
        int m = paramInt2 + paramInt3 >> 2;

        int n = k - i + 1;
        int i1 = m - j + 1;
        int[] arrayOfInt = this.biomeManager.getBiomesUnZoomed(null, i, j, n, i1);
        ChunkPosition localChunkPosition = null;
        int i2 = 0;
        for (int i3 = 0; i3 < arrayOfInt.length; i3++)
        {
            if (arrayOfInt[i3] >= DefaultBiome.values().length)
                continue;
            int i4 = i + i3 % n << 2;
            int i5 = j + i3 / n << 2;
            BiomeGenBase localBiomeBase = BiomeGenBase.biomeList[arrayOfInt[i3]];
            if ((!paramList.contains(localBiomeBase)) || ((localChunkPosition != null) && (paramRandom.nextInt(i2 + 1) != 0)))
                continue;
            localChunkPosition = new ChunkPosition(i4, 0, i5);
            i2++;
        }

        return localChunkPosition;
    }

    @Override
    public void cleanupCache()
    {
        this.biomeManager.cleanupCache();
    }
}