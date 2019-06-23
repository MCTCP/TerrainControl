package com.pg85.otg.forge.generator.structure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.forge.biomes.ForgeBiome;
import com.pg85.otg.network.ConfigProvider;
import com.pg85.otg.util.minecraftTypes.StructureNames;

import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraft.world.gen.structure.StructureOceanMonument;
import net.minecraft.world.gen.structure.StructureStart;

public class OTGOceanMonumentGen extends OTGMapGenStructure
{
    private int spacing = 32;
    private int separation = 5;

    private int gridSize;
    private int randomOffset;
    private final List<Biome> monumentSpawnBiomes;
    private final List<SpawnListEntry> mobList;

    public OTGOceanMonumentGen(ConfigProvider settings)
    {
        this.gridSize = settings.getWorldConfig().oceanMonumentGridSize;
        this.randomOffset = settings.getWorldConfig().oceanMonumentRandomOffset;
        this.monumentSpawnBiomes = new ArrayList<Biome>();
        this.mobList = Arrays.asList(new SpawnListEntry(EntityGuardian.class, 1, 2, 4));

        for (LocalBiome biome : settings.getBiomeArrayByOTGId())
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
            boolean flag = this.world.getBiomeProvider().areBiomesViable(k * 16 + 8, l * 16 + 8, 16,this.monumentSpawnBiomes);

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
    public BlockPos getNearestStructurePos(World worldIn, BlockPos pos, boolean findUnexplored)
    {
        this.world = worldIn;
        return findNearestStructurePosBySpacing(worldIn, this, pos, this.spacing, this.separation, 10387313, true, 100, findUnexplored);
    }
}