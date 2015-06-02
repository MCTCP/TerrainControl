package com.khorn.terraincontrol.bukkit.generator.structures;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.bukkit.BukkitBiome;
import com.khorn.terraincontrol.configuration.BiomeConfig.RareBuildingType;
import com.khorn.terraincontrol.configuration.WorldSettings;
import com.khorn.terraincontrol.util.minecraftTypes.StructureNames;
import net.minecraft.server.v1_8_R3.BiomeBase;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.StructureGenerator;
import net.minecraft.server.v1_8_R3.StructureStart;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RareBuildingGen extends StructureGenerator
{
    public List<BiomeBase> biomeList;

    /**
     * the maximum distance between scattered features
     */
    private int maxDistanceBetweenScatteredFeatures;

    /**
     * the minimum distance between scattered features
     */
    private int minDistanceBetweenScatteredFeatures;

    public RareBuildingGen(WorldSettings configs)
    {
        biomeList = new ArrayList<BiomeBase>();

        for (LocalBiome biome : configs.biomes)
        {
            if (biome == null)
                continue;
            if (biome.getBiomeConfig().rareBuildingType != RareBuildingType.disabled)
            {
                biomeList.add(((BukkitBiome) biome).getHandle());
            }
        }

        this.maxDistanceBetweenScatteredFeatures = configs.worldConfig.maximumDistanceBetweenRareBuildings;
        // Minecraft's internal minimum distance is one chunk lower than TC's
        // value
        this.minDistanceBetweenScatteredFeatures = configs.worldConfig.minimumDistanceBetweenRareBuildings - 1;
    }

    @Override
    // canSpawnStructureAtChunkCoords
    protected boolean a(int chunkX, int chunkZ)
    {
        int var3 = chunkX;
        int var4 = chunkZ;

        if (chunkX < 0)
        {
            chunkX -= this.maxDistanceBetweenScatteredFeatures - 1;
        }

        if (chunkZ < 0)
        {
            chunkZ -= this.maxDistanceBetweenScatteredFeatures - 1;
        }

        int var5 = chunkX / this.maxDistanceBetweenScatteredFeatures;
        int var6 = chunkZ / this.maxDistanceBetweenScatteredFeatures;
        Random random = this.c.a(var5, var6, 14357617);
        var5 *= this.maxDistanceBetweenScatteredFeatures;
        var6 *= this.maxDistanceBetweenScatteredFeatures;
        var5 += random.nextInt(this.maxDistanceBetweenScatteredFeatures - this.minDistanceBetweenScatteredFeatures);
        var6 += random.nextInt(this.maxDistanceBetweenScatteredFeatures - this.minDistanceBetweenScatteredFeatures);

        if (var3 == var5 && var4 == var6)
        {
            BiomeBase biomeAtPosition = this.c.getWorldChunkManager().getBiome(new BlockPosition(var3 * 16 + 8, 0, var4 * 16 + 8));

            for (BiomeBase biome : biomeList)
            {
                if (biomeAtPosition.equals(biome))
                {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    // getStructureStart
    protected StructureStart b(int chunkX, int chunkZ)
    {
        return new RareBuildingStart(this.c, this.b, chunkX, chunkZ);
    }

    @Override
    public String a()
    {
        return StructureNames.RARE_BUILDING;
    }
}
