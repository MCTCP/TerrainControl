package com.khorn.terraincontrol.forge.generator.structure;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.configuration.ConfigProvider;
import com.khorn.terraincontrol.forge.ForgeBiome;
import com.khorn.terraincontrol.util.minecraftTypes.StructureNames;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.util.BlockPos;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.BiomeGenBase.SpawnListEntry;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.StructureOceanMonument;
import net.minecraft.world.gen.structure.StructureStart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class OceanMonumentGen extends MapGenStructure
{
    private int gridSize;
    private int randomOffset;
    private final List<BiomeGenBase> monumentSpawnBiomes;
    private final List<SpawnListEntry> mobList;

    public OceanMonumentGen(ConfigProvider settings)
    {
        this.gridSize = settings.getWorldConfig().oceanMonumentGridSize;
        this.randomOffset = settings.getWorldConfig().oceanMonumentRandomOffset;
        this.monumentSpawnBiomes = new ArrayList<BiomeGenBase>();
        this.mobList = Arrays.asList(new SpawnListEntry(EntityGuardian.class, 1, 2, 4));

        for (LocalBiome biome : settings.getBiomeArray())
        {
            if (biome == null || !biome.getBiomeConfig().oceanMonumentsEnabled)
            {
                continue;
            }

            monumentSpawnBiomes.add(((ForgeBiome) biome).getHandle());
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
        Random random = this.worldObj.setRandomSeed(i1, j1, 10387313);
        i1 *= this.gridSize;
        j1 *= this.gridSize;
        i1 += (random.nextInt(this.randomOffset + 1) + random.nextInt(this.randomOffset + 1)) / 2;
        j1 += (random.nextInt(this.randomOffset + 1) + random.nextInt(this.randomOffset + 1)) / 2;

        if (k == i1 && l == j1)
        {
            if (this.worldObj.getWorldChunkManager().func_180300_a(new BlockPos(k * 16 + 8, 64, l * 16 + 8), (BiomeGenBase) null) != BiomeGenBase.deepOcean)
            {
                return false;
            }

            boolean flag = this.worldObj.getWorldChunkManager().areBiomesViable(k * 16 + 8, l * 16 + 8, 29, monumentSpawnBiomes);

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
        return new StructureOceanMonument.StartMonument(this.worldObj, this.rand, p_75049_1_, p_75049_2_);
    }

    public List<SpawnListEntry> getMonsterSpawnList()
    {
        return this.mobList;
    }

}