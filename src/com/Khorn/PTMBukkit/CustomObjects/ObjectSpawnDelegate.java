package com.Khorn.PTMBukkit.CustomObjects;

import com.Khorn.PTMBukkit.Generator.ObjectSpawner;
import org.bukkit.block.Block;

public class ObjectSpawnDelegate implements Runnable
{
    private ObjectSpawner objectSpawner;
    private int x;
    private  int y;
    private int z;

    public  ObjectSpawnDelegate(ObjectSpawner spawner, Block block)
    {
        objectSpawner = spawner;
        x = block.getX();
        y = block.getY();
        z = block.getZ();
    }


    public void run()
    {
        this.objectSpawner.SpawnCustomTrees(x,y,z);
    }
}
