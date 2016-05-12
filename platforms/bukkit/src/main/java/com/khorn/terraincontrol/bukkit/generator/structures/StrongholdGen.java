package com.khorn.terraincontrol.bukkit.generator.structures;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.bukkit.BukkitBiome;
import com.khorn.terraincontrol.configuration.WorldSettings;
import com.khorn.terraincontrol.util.helpers.ReflectionHelper;
import net.minecraft.server.v1_9_R2.BiomeBase;
import net.minecraft.server.v1_9_R2.WorldGenStronghold;

import java.util.List;

public class StrongholdGen extends WorldGenStronghold
{

    public StrongholdGen(WorldSettings configs)
    {
        super(ImmutableMap.of(
                "distance", String.valueOf(configs.worldConfig.strongholdDistance),
                "count", String.valueOf(configs.worldConfig.strongholdCount),
                "spread",  String.valueOf(configs.worldConfig.strongholdSpread)));

        // Modify in which biomes the stronghold is allowed to spawn
        List<BiomeBase> allowedBiomes = Lists.newArrayList();

        for (LocalBiome biome : configs.biomes)
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
