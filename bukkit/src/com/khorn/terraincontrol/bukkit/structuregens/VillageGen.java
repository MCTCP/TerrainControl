package com.khorn.terraincontrol.bukkit.structuregens;

import com.khorn.terraincontrol.bukkit.BukkitBiome;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.BiomeConfig.VillageType;
import com.khorn.terraincontrol.configuration.WorldConfig;
import net.minecraft.server.v1_5_R2.BiomeBase;
import net.minecraft.server.v1_5_R2.StructureGenerator;
import net.minecraft.server.v1_5_R2.StructureStart;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VillageGen extends StructureGenerator
{

    /**
     * A list of all the biomes villages can spawn in.
     */
    public List<BiomeBase> villageSpawnBiomes;

    /**
     * Village size, 0 for normal, 1 for flat map
     */
    private int size;
    private int distance;
    private int minimumDistance;

    public VillageGen(WorldConfig worldConfig)
    {
        size = worldConfig.villageSize;
        distance = worldConfig.villageDistance;
        minimumDistance = 8;

        // Add all village biomes to the list
        villageSpawnBiomes = new ArrayList<BiomeBase>();
        for (BiomeConfig config : worldConfig.biomeConfigs)
        {
            if (config == null)
                continue;
            if (config.villageType != VillageType.disabled)
            {
                villageSpawnBiomes.add(((BukkitBiome) config.Biome).getHandle());
            }
        }
    }

    @Override
    protected boolean a(int chunkX, int chunkZ)
    {
        int k = chunkX;
        int l = chunkZ;

        if (chunkX < 0)
        {
            chunkX -= this.distance - 1;
        }

        if (chunkZ < 0)
        {
            chunkZ -= this.distance - 1;
        }

        int i1 = chunkX / this.distance;
        int j1 = chunkZ / this.distance;
        Random random = this.c.H(i1, j1, 10387312);

        i1 *= this.distance;
        j1 *= this.distance;
        i1 += random.nextInt(this.distance - this.minimumDistance);
        j1 += random.nextInt(this.distance - this.minimumDistance);
        if (k == i1 && l == j1)
        {
            boolean flag = this.c.getWorldChunkManager().a(k * 16 + 8, l * 16 + 8, 0, villageSpawnBiomes);

            if (flag)
            {
                return true;
            }
        }

        return false;
    }

    @Override
    protected StructureStart b(int chunkX, int chunkZ)
    {
        return new VillageStart(this.c, this.b, chunkX, chunkZ, this.size);
    }
}
