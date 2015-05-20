package com.khorn.terraincontrol.bukkit.generator.structures;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.bukkit.BukkitBiome;
import com.khorn.terraincontrol.configuration.ConfigProvider;
import com.khorn.terraincontrol.util.minecraftTypes.StructureNames;
import net.minecraft.server.v1_8_R3.BiomeBase;
import net.minecraft.server.v1_8_R3.StructureGenerator;
import net.minecraft.server.v1_8_R3.StructureStart;
import net.minecraft.server.v1_8_R3.WorldGenMonument.WorldGenMonumentStart;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The ocean monument generator.
 *
 * <p>Minecraft defines two parameters: spacing and separation. We use two
 * more descriptive parameters: gridSize and randomOffset. They are directly
 * related: <code>GridSize = spacing</code> and <code>spacing - separation =
 * randomOffset + 1</code>, in other words, <code>randomOffset = spacing -
 * separation - 1</code>
 *
 */
public class OceanMonumentGen extends StructureGenerator
{

    private final List<BiomeBase> monumentSpawnBiomes;

    private int randomOffset;
    private int gridSize;

    public OceanMonumentGen(ConfigProvider settings)
    {
        this.gridSize = settings.getWorldConfig().oceanMonumentGridSize;
        this.randomOffset = settings.getWorldConfig().oceanMonumentRandomOffset;
        this.monumentSpawnBiomes = new ArrayList<BiomeBase>();

        for (LocalBiome biome : settings.getBiomeArray())
        {
            if (biome == null || !biome.getBiomeConfig().oceanMonumentsEnabled)
            {
                continue;
            }

            monumentSpawnBiomes.add(((BukkitBiome) biome).getHandle());
        }
    }

    @Override
    public String a()
    {
        return StructureNames.OCEAN_MONUMENT;
    }

    @Override
    // canSpawnStructureAtChunkCoords
    protected boolean a(int chunkX, int chunkZ)
    {
        int originalChunkX = chunkX;
        int originalChunkZ = chunkZ;

        if (chunkX < 0)
        {
            chunkX -= this.gridSize - 1;
        }

        if (chunkZ < 0)
        {
            chunkZ -= this.gridSize - 1;
        }

        int structureChunkX = chunkX / this.gridSize;
        int structureChunkZ = chunkZ / this.gridSize;
        Random random = this.c.a(structureChunkX, structureChunkZ, 10387313);

        structureChunkX *= this.gridSize;
        structureChunkZ *= this.gridSize;
        // Adding one to the randomOffset ensures that randomOffset = 0
        // disables randomness instead of 1, as one would expect
        structureChunkX += (random.nextInt(this.randomOffset + 1) + random.nextInt(this.randomOffset + 1)) / 2;
        structureChunkZ += (random.nextInt(this.randomOffset + 1) + random.nextInt(this.randomOffset + 1)) / 2;
        if (originalChunkX == structureChunkX && originalChunkZ == structureChunkZ)
        {
            boolean flag = this.c.getWorldChunkManager().a(originalChunkX * 16 + 8, originalChunkZ * 16 + 8, 29, monumentSpawnBiomes);

            if (flag)
            {
                return true;
            }
        }

        return false;
    }

    @Override
    protected StructureStart b(int i, int j)
    {
        return new WorldGenMonumentStart(this.c, this.b, i, j);
    }

}
