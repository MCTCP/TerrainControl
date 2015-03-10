package com.khorn.terraincontrol.generator.resource;

import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.generator.noise.NoiseGeneratorOldOctaves;
import com.khorn.terraincontrol.util.ChunkCoordinate;
import com.khorn.terraincontrol.util.MaterialSet;

import java.util.List;
import java.util.Random;

public class SwampyGen extends Resource
{
    /**
     * To get swampy swamps, we need our own noise generator here
     */
    private NoiseGeneratorOldOctaves noiseGen;
    private Random random;
    protected LocalMaterialData decorationAboveReplacements;
    private MaterialSet sourceBlocks;
    private int spawnY;

    @Override
    public void load(List<String> args) throws InvalidConfigException
    {
        assureSize(4, args);

        material = readMaterial(args.get(0));
        decorationAboveReplacements = readMaterial(args.get(1));
        spawnY = readInt(args.get(2), TerrainControl.WORLD_DEPTH, TerrainControl.WORLD_HEIGHT);
        sourceBlocks = readMaterials(args, 3);
        random = new Random(2345L);
        noiseGen = new NoiseGeneratorOldOctaves(random, 1);
    }

    @Override
    protected void spawnInChunk(LocalWorld world, Random random, boolean villageInChunk, ChunkCoordinate chunkCoord)
    {
        int chunkX = chunkCoord.getBlockXCenter();
        int chunkZ = chunkCoord.getBlockZCenter();
        for (int z0 = 0; z0 < ChunkCoordinate.CHUNK_Z_SIZE; z0++)
        {
            for (int x0 = 0; x0 < ChunkCoordinate.CHUNK_X_SIZE; x0++)
            {
                int x = chunkX + x0;
                int z = chunkZ + z0;
                spawn(world, random, false, x, z);
            }
        }
    }

    @Override
    public void spawn(LocalWorld world, Random rand, boolean villageInChunk, int x, int z)
    {
        int y = world.getHighestBlockYAt(x, z) - 1;
        if (y != spawnY)
            return;

        double yNoise = noiseGen.getYNoise((double) x * 0.25D, (double) z * 0.25D);
        if (yNoise > 0.0D)
        {
            LocalMaterialData materialAtLocation = world.getMaterial(x, y, z);
            if (sourceBlocks.contains(materialAtLocation))
            {
                world.setBlock(x, y, z, material);

                if (yNoise < 0.12D)
                {
                    world.setBlock(x, y + 1, z, decorationAboveReplacements);
                }
            }
        }
    }

    @Override
    public String makeString()
    {
        return "Swampy(" + material + "," + decorationAboveReplacements + "," + spawnY + "," + sourceBlocks + ")";
    }

}
