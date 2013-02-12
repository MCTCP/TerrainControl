package com.khorn.terraincontrol.forge.structuregens;

import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.BiomeConfig.RareBuildingType;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.forge.Biome;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.SpawnListEntry;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.StructureStart;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RareBuildingGen extends MapGenStructure
{
    public List<BiomeGenBase> biomeList;

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
        biomeList = new ArrayList<BiomeGenBase>();

        for (BiomeConfig biomeConfig : worldConfig.biomeConfigs)
        {
            if(biomeConfig == null)
                continue;
            if (biomeConfig.rareBuildingType != RareBuildingType.disabled)
            {
                biomeList.add(((Biome) biomeConfig.Biome).getHandle());
            }
        }

        this.scatteredFeatureSpawnList = new ArrayList();
        this.maxDistanceBetweenScatteredFeatures = worldConfig.maximumDistanceBetweenRareBuildings;
        // Minecraft's internal minimum distance is one chunk lower than TC's value
        this.minDistanceBetweenScatteredFeatures = worldConfig.minimumDistanceBetweenRareBuildings - 1;
        this.scatteredFeatureSpawnList.add(new SpawnListEntry(EntityWitch.class, 1, 1, 1));
    }

    @Override
    protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ)
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
        Random random = this.worldObj.setRandomSeed(var5, var6, 14357617);
        var5 *= this.maxDistanceBetweenScatteredFeatures;
        var6 *= this.maxDistanceBetweenScatteredFeatures;
        var5 += random.nextInt(this.maxDistanceBetweenScatteredFeatures - this.minDistanceBetweenScatteredFeatures);
        var6 += random.nextInt(this.maxDistanceBetweenScatteredFeatures - this.minDistanceBetweenScatteredFeatures);

        if (var3 == var5 && var4 == var6)
        {
            BiomeGenBase biomeAtPosition = this.worldObj.getWorldChunkManager().getBiomeGenAt(var3 * 16 + 8, var4 * 16 + 8);

            for (BiomeGenBase biome : biomeList)
            {
                if (biomeAtPosition.biomeID == biome.biomeID)
                {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    protected StructureStart getStructureStart(int chunkX, int chunkZ)
    {
        return new RareBuildingStart(this.worldObj, this.rand, chunkX, chunkZ);
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
