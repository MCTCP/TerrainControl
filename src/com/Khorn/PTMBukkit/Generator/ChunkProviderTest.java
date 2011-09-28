package com.Khorn.PTMBukkit.Generator;


import com.Khorn.PTMBukkit.WorldConfig;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ChunkProviderTest extends ChunkGenerator
{

    private ArrayList<BlockPopulator> populatorList = new ArrayList<BlockPopulator>();
    private WorldConfig worldSettings;

    private static int ChunkMaxY = 128;
    private static int ChunkMaxX = 16;
    private static int ChunkMaxZ = 16;


    public ChunkProviderTest(WorldConfig config)
    {
        this.worldSettings = config;

    }

    @Override
    public byte[] generate(World world, Random random, int i, int i1)
    {
        if(worldSettings.Mode == WorldConfig.GenMode.BiomeTest || worldSettings.Mode == WorldConfig.GenMode.NotGenerate)
            return new byte[ChunkMaxX * ChunkMaxY * ChunkMaxZ];
        return new byte[0];
    }

    @Override
    public List<BlockPopulator> getDefaultPopulators(World world)
    {
        return populatorList;
    }

    @Override
    public Location getFixedSpawnLocation(World world, Random random)
    {
        return new Location(world, 0, 61, 0);
    }
}


