package com.khorn.terraincontrol.bukkit.generator.structures;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.bukkit.BukkitBiome;
import com.khorn.terraincontrol.configuration.ServerConfigProvider;
import com.khorn.terraincontrol.util.minecraftTypes.StructureNames;
import net.minecraft.server.v1_12_R1.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TXMansionGen extends StructureGenerator
{
    private final int maxDistance;
    private final int minDistance;
    /**
     * Roofed forest and mutated roofed forest
     */
    private final List<BiomeBase> biomeList;
    private ChunkProviderGenerate defaultTerrainGeneratorOrNull;

    public TXMansionGen(ServerConfigProvider configs)
    {
        biomeList = new ArrayList<BiomeBase>();

        for (LocalBiome biome : configs.getBiomeArray())
        {
            if (biome == null)
                continue;
            if (biome.getBiomeConfig().mansionsEnabled)
            {
                biomeList.add(((BukkitBiome) biome).getHandle());
            }
        }

        this.maxDistance = configs.getWorldConfig().maximumDistanceBetweenMansions;
        this.minDistance = configs.getWorldConfig().minimumDistanceBetweenMansions;
    }

    private ChunkProviderGenerate getDefaultTerrainGenerator(World world)
    {
        if (this.defaultTerrainGeneratorOrNull == null)
        {
            this.defaultTerrainGeneratorOrNull = new ChunkProviderGenerate(world, world.getSeed(), false, "")
            {

            };
        }
        return this.defaultTerrainGeneratorOrNull;
    }

    @Override
    public String a()
    {
        return StructureNames.MANSION;
    }

    @Override
    protected boolean a(int var1, int var2)
    {
        int var3 = var1;
        int var4 = var2;
        if (var1 < 0)
        {
            var3 = var1 - (maxDistance - 1);
        }

        if (var2 < 0)
        {
            var4 = var2 - (maxDistance - 1);
        }

        int var5 = var3 / maxDistance;
        int var6 = var4 / maxDistance;
        Random var7 = this.g.a(var5, var6, 10387319);
        var5 *= maxDistance;
        var6 *= maxDistance;
        var5 += (var7.nextInt(maxDistance - minDistance + 1) + var7.nextInt(maxDistance - minDistance + 1)) / 2;
        var6 += (var7.nextInt(maxDistance - minDistance + 1) + var7.nextInt(maxDistance - minDistance + 1)) / 2;
        if (var1 == var5 && var2 == var6)
        {
            boolean var8 = this.g.getWorldChunkManager().a(var1 * 16 + 8, var2 * 16 + 8, 32, biomeList);
            if (var8)
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public BlockPosition getNearestGeneratedFeature(World var1, BlockPosition blockPos, boolean var3)
    {
        this.g = var1;
        return a(var1, this, blockPos, maxDistance, minDistance - 1, 10387319, true, 100, var3);
    }

    @Override
    protected StructureStart b(int var1, int var2)
    {
        return new WorldGenWoodlandMansion.a(this.g, getDefaultTerrainGenerator(this.g), this.f, var1, var2);
    }

}
