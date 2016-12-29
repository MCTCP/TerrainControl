package com.khorn.terraincontrol.forge.generator.structure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.configuration.ConfigProvider;
import com.khorn.terraincontrol.forge.ForgeBiome;
import com.khorn.terraincontrol.util.minecraftTypes.StructureNames;

import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.init.Biomes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.StructureOceanMonument;
import net.minecraft.world.gen.structure.StructureStart;

public class TXOceanMonumentGen extends MapGenStructure
{
    private int gridSize;
    private int randomOffset;
    private final List<Biome> monumentSpawnBiomes;
    private final List<SpawnListEntry> mobList;

    public TXOceanMonumentGen(ConfigProvider settings)
    {
        this.gridSize = settings.getWorldConfig().oceanMonumentGridSize;
        this.randomOffset = settings.getWorldConfig().oceanMonumentRandomOffset;
        this.monumentSpawnBiomes = new ArrayList<Biome>();
        this.mobList = Arrays.asList(new SpawnListEntry(EntityGuardian.class, 1, 2, 4));

        for (LocalBiome biome : settings.getBiomeArray())
        {
            if (biome == null || !biome.getBiomeConfig().oceanMonumentsEnabled)
            {
                continue;
            }

            this.monumentSpawnBiomes.add(((ForgeBiome) biome).getHandle());
        }
    }

    @Override
    protected boolean canSpawnStructureAtCoords(int p_75047_1_, int p_75047_2_)
    {
        int k = p_75047_1_;
        int l = p_75047_2_;

        if (p_75047_1_ < 0)
        {
            p_75047_1_ -= this.gridSize - 1;
        }

        if (p_75047_2_ < 0)
        {
            p_75047_2_ -= this.gridSize - 1;
        }

        int i1 = p_75047_1_ / this.gridSize;
        int j1 = p_75047_2_ / this.gridSize;
        Random random = this.world.setRandomSeed(i1, j1, 10387313);
        i1 *= this.gridSize;
        j1 *= this.gridSize;
        i1 += (random.nextInt(this.randomOffset + 1) + random.nextInt(this.randomOffset + 1)) / 2;
        j1 += (random.nextInt(this.randomOffset + 1) + random.nextInt(this.randomOffset + 1)) / 2;

        if (k == i1 && l == j1)
        {
            BiomeProvider biomeProvider = this.world.getBiomeProvider();
            if (biomeProvider.getBiome(new BlockPos(k * 16 + 8, 64, l * 16 + 8), (Biome) null) != Biomes.DEEP_OCEAN)
            {
                return false;
            }

            boolean flag = this.world.getBiomeProvider().areBiomesViable(k * 16 + 8, l * 16 + 8, 29, this.monumentSpawnBiomes);

            if (flag)
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public String getStructureName()
    {
        return StructureNames.OCEAN_MONUMENT;
    }

    @Override
    protected StructureStart getStructureStart(int p_75049_1_, int p_75049_2_)
    {
        return new StructureOceanMonument.StartMonument(this.world, this.rand, p_75049_1_, p_75049_2_);
    }

    public List<SpawnListEntry> getMonsterSpawnList()
    {
        return this.mobList;
    }

    @Override
    public BlockPos getClosestStrongholdPos(World worldIn, BlockPos pos, boolean p_180706_3_)
    {
        this.world = worldIn;
        return findNearestStructurePosBySpacing(worldIn, this, pos, this.gridSize, this.gridSize - this.randomOffset - 1, 10387313, true, 100, p_180706_3_);
    }
}