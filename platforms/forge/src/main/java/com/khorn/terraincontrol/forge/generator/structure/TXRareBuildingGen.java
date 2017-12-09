package com.khorn.terraincontrol.forge.generator.structure;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.configuration.BiomeConfig.RareBuildingType;
import com.khorn.terraincontrol.configuration.ServerConfigProvider;
import com.khorn.terraincontrol.forge.ForgeBiome;
import com.khorn.terraincontrol.util.minecraftTypes.StructureNames;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.MapGenScatteredFeature;
import net.minecraft.world.gen.structure.StructureStart;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TXRareBuildingGen extends MapGenScatteredFeature
{
    private List<Biome> biomeList;

    public TXRareBuildingGen(ServerConfigProvider configs) {

        this.biomeList = new ArrayList<Biome>();

        for (LocalBiome biome : configs.getBiomeArray())
        {
            if (biome == null)
                continue;
            if (biome.getBiomeConfig().rareBuildingType != RareBuildingType.disabled)
            {
                this.biomeList.add(((ForgeBiome) biome).getHandle());
            }
        }

        this.maxDistanceBetweenScatteredFeatures = configs.getWorldConfig().maximumDistanceBetweenRareBuildings;
        // Minecraft's internal minimum distance is one lower than TC's value
        this.minDistanceBetweenScatteredFeatures = configs.getWorldConfig().minimumDistanceBetweenRareBuildings - 1;
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
        Random random = this.world.setRandomSeed(var5, var6, 14357617);
        var5 *= this.maxDistanceBetweenScatteredFeatures;
        var6 *= this.maxDistanceBetweenScatteredFeatures;
        var5 += random.nextInt(this.maxDistanceBetweenScatteredFeatures - this.minDistanceBetweenScatteredFeatures);
        var6 += random.nextInt(this.maxDistanceBetweenScatteredFeatures - this.minDistanceBetweenScatteredFeatures);

        if (var3 == var5 && var4 == var6)
        {
            Biome biomeAtPosition = this.world.getBiomeProvider().getBiome(
                    new BlockPos(var3 * 16 + 8, 0, var4 * 16 + 8));

            if (this.biomeList.contains(biomeAtPosition))
            {
                return true;
            }
        }

        return false;
    }

    @Override
    protected StructureStart getStructureStart(int chunkX, int chunkZ)
    {
        return new TXRareBuildingStart(this.world, this.rand, chunkX, chunkZ);
    }

    @Override
    public String getStructureName()
    {
        return StructureNames.RARE_BUILDING;
    }

    @Override
    public BlockPos getNearestStructurePos(World worldIn, BlockPos pos, boolean p_180706_3_)
    {
        this.world = worldIn;
        return findNearestStructurePosBySpacing(worldIn, this, pos, this.maxDistanceBetweenScatteredFeatures, 8, 14357617, false, 100, p_180706_3_);
    }
}
