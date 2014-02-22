package com.khorn.terraincontrol.bukkit.generator;

import com.khorn.terraincontrol.bukkit.BukkitWorld;
import com.khorn.terraincontrol.generator.biome.BiomeGenerator;
import com.khorn.terraincontrol.generator.biome.OutputType;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultBiome;
import net.minecraft.server.v1_7_R1.BiomeBase;
import net.minecraft.server.v1_7_R1.ChunkPosition;
import net.minecraft.server.v1_7_R1.WorldChunkManager;
import net.minecraft.server.v1_7_R1.WorldGenVillage;

import java.util.List;
import java.util.Random;

public class TCWorldChunkManager extends WorldChunkManager
{
    private BukkitWorld localWorld;
    private BiomeGenerator biomeManager;

    public TCWorldChunkManager(BukkitWorld world)
    {
        localWorld = world;
    }

    public void setBiomeManager(BiomeGenerator manager)
    {
        this.biomeManager = manager;
    }

    @Override
    public BiomeBase getBiome(int paramInt1, int paramInt2)
    {
        return localWorld.getBiomeById(biomeManager.getBiome(paramInt1, paramInt2)).getHandle();
    }

    @Override
    public float[] getWetness(float[] paramArrayOfFloat, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {
        return biomeManager.getRainfall(paramArrayOfFloat, paramInt1, paramInt2, paramInt3, paramInt4);
    }

    @Override
    public BiomeBase[] getBiomes(BiomeBase[] paramArrayOfBiomeBase, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {
        if ((paramArrayOfBiomeBase == null) || (paramArrayOfBiomeBase.length < paramInt3 * paramInt4))
        {
            paramArrayOfBiomeBase = new BiomeBase[paramInt3 * paramInt4];
        }

        int[] arrayOfInt = this.biomeManager.getBiomesUnZoomed(null, paramInt1, paramInt2, paramInt3, paramInt4, OutputType.DEFAULT_FOR_WORLD);

        // Replaces ids with BiomeBases
        for (int i = 0; i < paramInt3 * paramInt4; i++)
        {
            paramArrayOfBiomeBase[i] = localWorld.getBiomeById(arrayOfInt[i]).getHandle();
        }

        return paramArrayOfBiomeBase;
    }

    @Override
    public BiomeBase[] a(BiomeBase[] paramArrayOfBiomeBase, int paramInt1, int paramInt2, int paramInt3, int paramInt4, boolean paramBoolean)
    {
        if ((paramArrayOfBiomeBase == null) || (paramArrayOfBiomeBase.length < paramInt3 * paramInt4))
        {
            paramArrayOfBiomeBase = new BiomeBase[paramInt3 * paramInt4];
        }

        int[] localObject = this.biomeManager.getBiomes(null, paramInt1, paramInt2, paramInt3, paramInt4, OutputType.DEFAULT_FOR_WORLD);

        // Replace ids with BiomeBases
        for (int i = 0; i < paramInt3 * paramInt4; i++)
        {
            paramArrayOfBiomeBase[i] = localWorld.getBiomeById(localObject[i]).getHandle();
        }

        return paramArrayOfBiomeBase;
    }

    @SuppressWarnings("rawtypes")
    @Override
    // areBiomesViable
    public boolean a(int paramInt1, int paramInt2, int paramInt3, List paramList)
    {
        // Hack for villages in other biomes
        // (The alternative would be to completely override the village spawn
        // code)
        if (paramList == WorldGenVillage.e && localWorld.villageGen != null)
        {
            paramList = localWorld.villageGen.villageSpawnBiomes;
        }

        int i = paramInt1 - paramInt3 >> 2;
        int j = paramInt2 - paramInt3 >> 2;
        int k = paramInt1 + paramInt3 >> 2;
        int m = paramInt2 + paramInt3 >> 2;

        int n = k - i + 1;
        int i1 = m - j + 1;
        BiomeBase[] arrayOfInt = this.getBiomes(null, i, j, n, i1);
        for (int i2 = 0; i2 < n * i1; i2++)
        {
            BiomeBase localBiomeBase = arrayOfInt[i2];
            if (!paramList.contains(localBiomeBase))
                return false;
        }

        return true;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public ChunkPosition a(int paramInt1, int paramInt2, int paramInt3, List paramList, Random paramRandom)
    {
        int i = paramInt1 - paramInt3 >> 2;
        int j = paramInt2 - paramInt3 >> 2;
        int k = paramInt1 + paramInt3 >> 2;
        int m = paramInt2 + paramInt3 >> 2;

        int n = k - i + 1;
        int i1 = m - j + 1;
        int[] arrayOfInt = this.biomeManager.getBiomesUnZoomed(null, i, j, n, i1, OutputType.DEFAULT_FOR_WORLD);
        ChunkPosition localChunkPosition = null;
        int i2 = 0;
        for (int i3 = 0; i3 < arrayOfInt.length; i3++)
        {
            if (arrayOfInt[i3] >= DefaultBiome.values().length)
                continue;
            int i4 = i + i3 % n << 2;
            int i5 = j + i3 / n << 2;
            BiomeBase localBiomeBase = BiomeBase.getBiome(arrayOfInt[i3]);
            if ((!paramList.contains(localBiomeBase)) || ((localChunkPosition != null) && (paramRandom.nextInt(i2 + 1) != 0)))
                continue;
            localChunkPosition = new ChunkPosition(i4, 0, i5);
            i2++;
        }

        return localChunkPosition;
    }

    @Override
    public void b()
    {
        this.biomeManager.cleanupCache();
    }
}