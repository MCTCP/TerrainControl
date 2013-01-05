package com.khorn.terraincontrol.bukkit.structuregens;

import com.khorn.terraincontrol.bukkit.BukkitBiome;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.BiomeConfig.RareBuildingType;
import com.khorn.terraincontrol.configuration.WorldConfig;
import net.minecraft.server.v1_4_6.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RareBuildingGen extends StructureGenerator
{
    public List<BiomeBase> biomeList;

    /**
     * contains possible spawns for scattered features
     */
    @SuppressWarnings("rawtypes")
    private List scatteredFeatureSpawnList;

    /**
     * the maximum distance between scattered features
     */
    private int maxDistanceBetweenScatteredFeatures;

    /**
     * the minimum distance between scattered features
     */
    private int minDistanceBetweenScatteredFeatures;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public RareBuildingGen(WorldConfig worldConfig)
    {
        biomeList = new ArrayList<BiomeBase>();

        for (BiomeConfig biomeConfig : worldConfig.biomeConfigs)
        {
            if (biomeConfig == null)
                continue;
            if (biomeConfig.rareBuildingType != RareBuildingType.disabled)
            {
                biomeList.add(((BukkitBiome) biomeConfig.Biome).getHandle());
            }
        }

        this.scatteredFeatureSpawnList = new ArrayList();
        this.maxDistanceBetweenScatteredFeatures = worldConfig.maximumDistanceBetweenRareBuildings;
        // Minecraft's internal minimum distance is one chunk lower than TC's value
        this.minDistanceBetweenScatteredFeatures = worldConfig.minimumDistanceBetweenRareBuildings - 1;
        this.scatteredFeatureSpawnList.add(new BiomeMeta(EntityWitch.class, 1, 1, 1));
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
        Random random = this.c.F(var5, var6, 14357617);
        var5 *= this.maxDistanceBetweenScatteredFeatures;
        var6 *= this.maxDistanceBetweenScatteredFeatures;
        var5 += random.nextInt(this.maxDistanceBetweenScatteredFeatures - this.minDistanceBetweenScatteredFeatures);
        var6 += random.nextInt(this.maxDistanceBetweenScatteredFeatures - this.minDistanceBetweenScatteredFeatures);

        if (var3 == var5 && var4 == var6)
        {
            BiomeBase biomeAtPosition = this.c.getWorldChunkManager().getBiome(var3 * 16 + 8, var4 * 16 + 8);

            for (BiomeBase biome : biomeList)
            {
                if (biomeAtPosition.id == biome.id)
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

    /**
     * returns possible spawns for scattered features
     */
    @SuppressWarnings({"rawtypes", "UnusedDeclaration"})
    public List getScatteredFeatureSpawnList()
    {
        return this.scatteredFeatureSpawnList;
    }
}
