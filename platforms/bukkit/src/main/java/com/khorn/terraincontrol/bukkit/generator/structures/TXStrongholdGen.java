package com.khorn.terraincontrol.bukkit.generator.structures;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.bukkit.BukkitBiome;
import com.khorn.terraincontrol.configuration.ServerConfigProvider;
import com.khorn.terraincontrol.util.helpers.ReflectionHelper;
import net.minecraft.server.v1_12_R1.BiomeBase;
import net.minecraft.server.v1_12_R1.WorldGenStronghold;

import java.util.List;

public class TXStrongholdGen extends WorldGenStronghold
{

    public TXStrongholdGen(ServerConfigProvider configs)
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
