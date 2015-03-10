package com.khorn.terraincontrol.generator.resource;

import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.generator.noise.NoiseGeneratorOldOctaves;
import com.khorn.terraincontrol.util.MaterialSet;

import java.util.List;
import java.util.Random;

public class SwampyGen extends Resource
{
    /**
     * To get swampy swamps, we need our own noise generator here
     */
    private NoiseGeneratorOldOctaves noise;
    private Random random;
    protected LocalMaterialData material2;
    private MaterialSet materialcheck;

    @Override
    public void load(List<String> args) throws InvalidConfigException
    {
        assureSize(4, args);

        material = readMaterial(args.get(0));
        material2 = readMaterial(args.get(1));
        frequency = readInt(args.get(2), TerrainControl.WORLD_DEPTH, TerrainControl.WORLD_HEIGHT);
        materialcheck = readMaterials(args,3);
        //  Statics
        uniformSpawn = true;
        random = new Random(2345L);
        noise = new NoiseGeneratorOldOctaves(random, 1);
    }

    @Override
    public void spawn(LocalWorld world, Random rand, boolean villageInChunk, int x, int z)
    {
        int y = world.getHighestBlockYAt(x, z) - 1;
        if (y != frequency)
            return;

        double yNoise = noise.getYNoise((double) x * 0.25D, (double) z * 0.25D);
        if (yNoise > 0.0D)
        {
            LocalMaterialData materialAtLocation = world.getMaterial(x, y, z);
            if (!materialcheck.contains(materialAtLocation))
            {
                world.setBlock(x, y, z, material);

                if (yNoise < 0.12D)
                {
                    world.setBlock(x, y + 1, z, material2);
                }
            }
        }
    }

    @Override
    public String makeString()
    {
        return "Swampy(" + material + "," + material2 + "," + frequency + "," + materialcheck.toString() + ")";
    }

}
