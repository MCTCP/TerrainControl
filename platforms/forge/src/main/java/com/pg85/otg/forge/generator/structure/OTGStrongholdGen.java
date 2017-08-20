package com.pg85.otg.forge.generator.structure;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableMap;
import com.pg85.otg.LocalBiome;
import com.pg85.otg.configuration.ServerConfigProvider;
import com.pg85.otg.forge.ForgeBiome;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.MapGenStronghold;

public class OTGStrongholdGen extends MapGenStronghold
{
    private List<Biome> allowedBiomes;

    public OTGStrongholdGen(ServerConfigProvider configs)
    {
        super(ImmutableMap.of(
                "distance", String.valueOf(configs.getWorldConfig().strongholdDistance),
                "count", String.valueOf(configs.getWorldConfig().strongholdCount),
                "spread",  String.valueOf(configs.getWorldConfig().strongholdSpread)));

        allowedBiomes = new ArrayList<Biome>();

        for (LocalBiome biome : configs.getBiomeArray())
        {
            if (biome == null)
                continue;
            if (biome.getBiomeConfig().strongholdsEnabled)
            {
                allowedBiomes.add(((ForgeBiome) biome).getHandle());
            }
        }
    }
}