package com.Khorn.TerrainControl.Bukkit;


import com.Khorn.TerrainControl.Generator.ObjectSpawner;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftChunk;
import org.bukkit.generator.BlockPopulator;

import java.util.Random;

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
