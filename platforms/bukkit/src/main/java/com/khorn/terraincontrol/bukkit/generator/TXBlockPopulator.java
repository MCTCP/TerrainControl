package com.khorn.terraincontrol.bukkit.generator;


import com.khorn.terraincontrol.bukkit.BukkitWorld;
import com.khorn.terraincontrol.generator.ObjectSpawner;
import com.khorn.terraincontrol.util.ChunkCoordinate;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;

import java.util.Random;

public class TXBlockPopulator extends BlockPopulator
{
    private ObjectSpawner spawner;

    public TXBlockPopulator(BukkitWorld world)
    {
        this.spawner = new ObjectSpawner(world.getConfigs(), world);
    }

    @Override
    public void populate(World world, Random random, Chunk chunk)
    {
        this.spawner.populate(ChunkCoordinate.fromChunkCoords(chunk.getX(), chunk.getZ()));
    }
}
