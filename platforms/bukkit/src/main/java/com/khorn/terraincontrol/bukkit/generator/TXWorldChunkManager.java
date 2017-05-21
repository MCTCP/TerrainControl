package com.khorn.terraincontrol.bukkit.generator;

import com.khorn.terraincontrol.bukkit.BukkitWorld;
import com.khorn.terraincontrol.generator.biome.BiomeGenerator;
import com.khorn.terraincontrol.generator.biome.OutputType;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultBiome;
import net.minecraft.server.v1_12_R1.BiomeBase;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.WorldChunkManager;
import net.minecraft.server.v1_12_R1.WorldGenVillage;

import java.util.List;
import java.util.Random;

/**
 * Minecraft's biome generator class is WorldChunkManager, we use
 * BiomeGenerator. This class provides a bridge between the two, allowing us to
 * use custom biome generators.
 */
public class TXWorldChunkManager extends WorldChunkManager
{
    private BukkitWorld localWorld;
    private BiomeGenerator biomeGenerator;

    public TXWorldChunkManager(BukkitWorld world, BiomeGenerator biomeGenerator)
    {
        this.localWorld = world;
        this.biomeGenerator = biomeGenerator;
    }

    @Override
    public BiomeBase getBiome(BlockPosition blockPos)
    {
        return localWorld.getBiomeById(biomeGenerator.getBiome(blockPos.getX(), blockPos.getZ())).getHandle();
    }

    @Override
    public BiomeBase[] getBiomes(BiomeBase[] paramArrayOfBiomeBase, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {
        if ((paramArrayOfBiomeBase == null) || (paramArrayOfBiomeBase.length < paramInt3 * paramInt4))
        {
            paramArrayOfBiomeBase = new BiomeBase[paramInt3 * paramInt4];
        }

        int[] arrayOfInt = this.biomeGenerator.getBiomesUnZoomed(null, paramInt1, paramInt2, paramInt3, paramInt4, OutputType.DEFAULT_FOR_WORLD);

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

        int[] localObject = this.biomeGenerator.getBiomes(null, paramInt1, paramInt2, paramInt3, paramInt4, OutputType.DEFAULT_FOR_WORLD);

        // Replace ids with BiomeBases
        for (int i = 0; i < paramInt3 * paramInt4; i++)
        {
            paramArrayOfBiomeBase[i] = localWorld.getBiomeById(localObject[i]).getHandle();
        }

        return paramArrayOfBiomeBase;
    }

    @Override
    // areBiomesViable
    public boolean a(int paramInt1, int paramInt2, int paramInt3, List<BiomeBase> paramList)
    {
        // Hack for villages in other biomes
        // (The alternative would be to completely override the village spawn
        // code)
        if (paramList == WorldGenVillage.a && localWorld.villageGen != null)
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
    public BlockPosition a(int paramInt1, int paramInt2, int paramInt3, List paramList, Random paramRandom)
    {
        int i = paramInt1 - paramInt3 >> 2;
        int j = paramInt2 - paramInt3 >> 2;
        int k = paramInt1 + paramInt3 >> 2;
        int m = paramInt2 + paramInt3 >> 2;

        int n = k - i + 1;
        int i1 = m - j + 1;
        int[] arrayOfInt = this.biomeGenerator.getBiomesUnZoomed(null, i, j, n, i1, OutputType.DEFAULT_FOR_WORLD);
        BlockPosition localBlockPosition = null;
        int i2 = 0;
        for (int i3 = 0; i3 < arrayOfInt.length; i3++)
        {
            if (arrayOfInt[i3] >= DefaultBiome.values().length)
                continue;
            int i4 = i + i3 % n << 2;
            int i5 = j + i3 / n << 2;
            BiomeBase localBiomeBase = BiomeBase.getBiome(arrayOfInt[i3]);
            if ((!paramList.contains(localBiomeBase)) || ((localBlockPosition != null) && (paramRandom.nextInt(i2 + 1) != 0)))
                continue;
            localBlockPosition = new BlockPosition(i4, 0, i5);
            i2++;
        }

        return localBlockPosition;
    }

    @Override
    public void b()
    {
        this.biomeGenerator.cleanupCache();
    }
}