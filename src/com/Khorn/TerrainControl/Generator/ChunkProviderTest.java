package com.Khorn.TerrainControl.Generator;


import com.Khorn.TerrainControl.WorldConfig;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ChunkProviderTest extends ChunkGenerator
{

    private ArrayList<BlockPopulator> populatorList = new ArrayList<BlockPopulator>();
    private WorldConfig worldSettings;


    public ChunkProviderTest(WorldConfig config)
    {
        this.worldSettings = config;

    }

    @Override
    public byte[] generate(World world, Random random, int i, int i1)
    {
        int chunkMaxY = 128;
        int chunkMaxX = 16;
        int chunkMaxZ = 16;
        if( worldSettings.Mode == WorldConfig.GenMode.NotGenerate)
            return new byte[chunkMaxX * chunkMaxY * chunkMaxZ];
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


