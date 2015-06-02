package com.khorn.terraincontrol.forge.generator.structure;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.configuration.BiomeConfig.RareBuildingType;
import com.khorn.terraincontrol.configuration.WorldSettings;
import com.khorn.terraincontrol.forge.ForgeBiome;
import com.khorn.terraincontrol.util.minecraftTypes.StructureNames;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.util.BlockPos;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.BiomeGenBase.SpawnListEntry;
import net.minecraft.world.gen.structure.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RareBuildingGen extends MapGenStructure
{
    public List<BiomeGenBase> biomeList;

    /**
     * contains possible spawns for scattered features
     */
    private List<SpawnListEntry> scatteredFeatureSpawnList;

    /**
     * the maximum distance between scattered features
     */
    private int maxDistanceBetweenScatteredFeatures;

    /**
     * the minimum distance between scattered features
     */
    private int minDistanceBetweenScatteredFeatures;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public RareBuildingGen(WorldSettings configs)
    {
        biomeList = new ArrayList<BiomeGenBase>();

        for (LocalBiome biome : configs.biomes)
        {
            if (biome == null)
                continue;
            if (biome.getBiomeConfig().rareBuildingType != RareBuildingType.disabled)
            {
                biomeList.add(((ForgeBiome) biome).getHandle());
            }
        }

        this.scatteredFeatureSpawnList = new ArrayList();
        this.maxDistanceBetweenScatteredFeatures = configs.worldConfig.maximumDistanceBetweenRareBuildings;
        // Minecraft's internal minimum distance is one lower than TC's value
        this.minDistanceBetweenScatteredFeatures = configs.worldConfig.minimumDistanceBetweenRareBuildings - 1;
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
            BiomeGenBase biomeAtPosition = this.worldObj.getWorldChunkManager()
                    .func_180631_a(new BlockPos(var3 * 16 + 8, 0, var4 * 16 + 8));

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
     * Returns possible spawn mobs for scattered features
     * @return The possible mobs.
     */
    public List<SpawnListEntry> getMonsterSpawnList()
    {
        return this.scatteredFeatureSpawnList;
    }

    public boolean isSwampHutAtLocation(BlockPos blockPos)
    {
        StructureStart structurestart = this.func_175797_c(blockPos);

        if (structurestart != null && structurestart instanceof MapGenScatteredFeature.Start && !structurestart.getComponents().isEmpty())
        {
            StructureComponent structurecomponent = (StructureComponent) structurestart.getComponents().getFirst();
            return structurecomponent instanceof ComponentScatteredFeaturePieces.SwampHut;
        }
        else
        {
            return false;
        }
    }

    @Override
    public String getStructureName()
    {
        return StructureNames.RARE_BUILDING;
    }
}
