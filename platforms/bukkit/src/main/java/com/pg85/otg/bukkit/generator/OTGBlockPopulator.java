package com.pg85.otg.bukkit.generator;


import com.pg85.otg.bukkit.BukkitWorld;
import com.pg85.otg.generator.ObjectSpawner;
import com.pg85.otg.util.ChunkCoordinate;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;

import java.util.Random;

public class OTGBlockPopulator extends BlockPopulator
{
    private ObjectSpawner spawner;

    public ObjectSpawner getObjectSpawner()
    {    	
    	return spawner;
    }
    
    public OTGBlockPopulator(BukkitWorld world)
    {
        this.spawner = new ObjectSpawner(world.getConfigs(), world);
    }

    @Override
    public void populate(World world, Random random, Chunk chunk)
    {
        this.spawner.populate(ChunkCoordinate.fromChunkCoords(chunk.getX(), chunk.getZ()));
    }
}
