package com.khorn.terraincontrol.bukkit.generator.structures;

import com.khorn.terraincontrol.configuration.ConfigProvider;
import com.khorn.terraincontrol.util.minecraftTypes.StructureNames;
import net.minecraft.server.v1_8_R1.*;

import java.util.Arrays;
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

    private final List<BiomeBase> monumentSpawnBiomes = Arrays.asList(BiomeBase.OCEAN, BiomeBase.DEEP_OCEAN, BiomeBase.RIVER,
            BiomeBase.FROZEN_OCEAN, BiomeBase.FROZEN_RIVER);

    private int randomOffset;
    private int gridSize;

    public OceanMonumentGen(ConfigProvider settings)
    {
        this.gridSize = settings.getWorldConfig().oceanMonumentGridSize;
        this.randomOffset = settings.getWorldConfig().oceanMonumentRandomOffset;

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

    // Two methods to help Cauldron dynamically rename things.
    // It has problems with classes that extend native Minecraft classes
    public void prepare(World world, int chunkX, int chunkZ)
    {
        a(null, world, chunkX, chunkZ, null);
    }

    public void place(World world, Random random, ChunkCoordIntPair chunk)
    {
        a(world, random, chunk);
    }
}
