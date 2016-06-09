package com.khorn.terraincontrol.bukkit.generator.structures;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.bukkit.util.WorldHelper;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.BiomeConfig.MineshaftType;
import com.khorn.terraincontrol.util.minecraftTypes.StructureNames;
import net.minecraft.server.v1_10_R1.*;

import java.util.Random;

public class MineshaftGen extends StructureGenerator
{
    // canSpawnStructureAtCoords
    @Override
    protected boolean a(int chunkX, int chunkZ)
    {
        Random rand = this.f;
        World worldMC = this.g;
        if (rand.nextInt(80) < Math.max(Math.abs(chunkX), Math.abs(chunkZ)))
        {
            LocalWorld world = WorldHelper.toLocalWorld(worldMC);
            LocalBiome biome = world.getBiome(chunkX * 16 + 8, chunkZ * 16 + 8);
            BiomeConfig biomeConfig = biome.getBiomeConfig();
            if (biomeConfig.mineshaftType == MineshaftType.disabled)
            {
                return false;
            }
            if (rand.nextDouble() * 100.0 < biomeConfig.mineshaftsRarity)
            {
                return true;
            }
        }

        return false;
    }

    @Override
    protected StructureStart b(int chunkX, int chunkZ)
    {
        LocalWorld world = WorldHelper.toLocalWorld(this.g);
        LocalBiome biome = world.getBiome(chunkX << 4 + 8, chunkZ << 4 + 8);
        BiomeConfig biomeConfig = biome.getBiomeConfig();
        WorldGenMineshaft.Type mineshaftType = WorldGenMineshaft.Type.NORMAL;
        if (biomeConfig.mineshaftType == MineshaftType.mesa)
        {
            mineshaftType = WorldGenMineshaft.Type.MESA;
        }

        return new WorldGenMineshaftStart(this.g, this.f, chunkX, chunkZ, mineshaftType);
    }

    @Override
    public String a()
    {
        return StructureNames.MINESHAFT;
    }

}