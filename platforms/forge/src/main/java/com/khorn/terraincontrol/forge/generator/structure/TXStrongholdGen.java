package com.khorn.terraincontrol.forge.generator.structure;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableMap;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.configuration.ServerConfigProvider;
import com.khorn.terraincontrol.forge.ForgeBiome;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.MapGenStronghold;

public class TXStrongholdGen extends MapGenStronghold
{
    private List<Biome> allowedBiomes;

    public TXStrongholdGen(ServerConfigProvider configs)
    {
        super(ImmutableMap.of(
                "distance", String.valueOf(configs.getWorldConfig().strongholdDistance),
                "count", String.valueOf(configs.getWorldConfig().strongholdCount),
                "spread",  String.valueOf(configs.getWorldConfig().strongholdSpread)));

        this.allowedBiomes = new ArrayList<Biome>();

        for (LocalBiome biome : configs.getBiomeArray())
        {
            if (biome == null)
                continue;
            if (biome.getBiomeConfig().strongholdsEnabled)
            {
                this.allowedBiomes.add(((ForgeBiome) biome).getHandle());
            }
        }
    }
}
