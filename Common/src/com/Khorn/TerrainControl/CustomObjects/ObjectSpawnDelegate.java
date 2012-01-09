package com.Khorn.TerrainControl.CustomObjects;

import com.Khorn.TerrainControl.Configuration.WorldConfig;
import net.minecraft.server.World;
import org.bukkit.block.Block;

import java.util.Random;

public class ObjectSpawnDelegate implements Runnable
{
    private World world;
    private WorldConfig worldSettings;
    private int x;
    private  int y;
    private int z;

    public  ObjectSpawnDelegate(World _world, WorldConfig _worldSettings, Block block)
    {
        world = _world;
        worldSettings = _worldSettings;
        x = block.getX();
        y = block.getY();
        z = block.getZ();
    }


    public void run()
    {
        CustomObjectGen.SpawnCustomTrees(world, new Random(), this.worldSettings, x, y, z);
    }
}
