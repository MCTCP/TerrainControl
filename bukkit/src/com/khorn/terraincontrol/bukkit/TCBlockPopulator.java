package com.khorn.terraincontrol.bukkit;


import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_4_5.CraftChunk;
import org.bukkit.generator.BlockPopulator;

import com.khorn.terraincontrol.generator.ObjectSpawner;

public class TCBlockPopulator extends BlockPopulator
{
    private BukkitWorld world;
    private ObjectSpawner spawner;

    public TCBlockPopulator(BukkitWorld _world)
    {
        this.world = _world;
        this.spawner = new ObjectSpawner(_world.getSettings(),_world);
    }

    @Override
    public void populate(World world, Random random, Chunk chunk)
    {
        this.world.LoadChunk(((CraftChunk) chunk).getHandle());
        this.spawner.populate(chunk.getX(),chunk.getZ());
    }
}
