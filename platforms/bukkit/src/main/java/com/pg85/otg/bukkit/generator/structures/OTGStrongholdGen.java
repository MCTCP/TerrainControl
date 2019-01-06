package com.pg85.otg.bukkit.generator.structures;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.pg85.otg.LocalBiome;
import com.pg85.otg.bukkit.BukkitBiome;
import com.pg85.otg.network.ServerConfigProvider;
import com.pg85.otg.util.helpers.ReflectionHelper;
import net.minecraft.server.v1_12_R1.BiomeBase;
import net.minecraft.server.v1_12_R1.WorldGenStronghold;

import java.util.List;

public class OTGStrongholdGen extends WorldGenStronghold
{

    public OTGStrongholdGen(ServerConfigProvider configs)
    {
        super(ImmutableMap.of(
                "distance", String.valueOf(configs.getWorldConfig().strongholdDistance),
                "count", String.valueOf(configs.getWorldConfig().strongholdCount),
                "spread",  String.valueOf(configs.getWorldConfig().strongholdSpread)));

        // Modify in which biomes the stronghold is allowed to spawn
        List<BiomeBase> allowedBiomes = Lists.newArrayList();

        for (LocalBiome biome : configs.getBiomeArray())
        {
            if (biome == null)
                continue;

            if (biome.getBiomeConfig().strongholdsEnabled)
            {
                allowedBiomes.add(((BukkitBiome) biome).getHandle());
            }
        }

        ReflectionHelper.setValueInFieldOfType(this, List.class, allowedBiomes);
    }

}
