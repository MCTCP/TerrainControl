package com.khorn.terraincontrol.generator.resourcegens;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.ConfigFunction;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.exception.InvalidResourceException;

/**
 * Represents a Resource: something that can generate in the world.
 *
 */
public abstract class Resource extends ConfigFunction
{
    /**
     * Spawns the resource at this position, ignoring rarity and frequency.
     * 
     * @param world
     * @param chunkX
     * @param chunkZ
     */
    public abstract void spawn(LocalWorld world, Random random, int x, int z);

    /**
     * Spawns the resource normally.
     * 
     * @param world
     * @param chunkX
     * @param chunkZ
     */
    public void process(LocalWorld world, Random random, int chunkX, int chunkZ)
    {
        for (int t = 0; t < frequency; t++)
        {
            if (random.nextInt(100) > rarity)
                continue;
            int x = chunkX * 16 + random.nextInt(16) + 8;
            int z = chunkZ * 16 + random.nextInt(16) + 8;
            spawn(world, random, x, z);
        }
    }
    
    /**
     * Convenience method for creating a resource. Used to create the default resources.
     * @param world
     * @param clazz
     * @param args
     * @return
     */
    public static Resource create(WorldConfig config, Class<? extends Resource> clazz, Object... args)
    {
        List<String> stringArgs = new ArrayList<String>(args.length);
        for(Object arg: args)
        {
            stringArgs.add("" + arg);
        }
        
        Resource resource;
        try
        {
            resource = clazz.newInstance();
        } catch (InstantiationException e)
        {
            return null;
        } catch (IllegalAccessException e)
        {
            return null;
        }
        resource.setWorldConfig(config);
        try {
            resource.load(stringArgs);
        } catch(InvalidResourceException e)
        {
            TerrainControl.log("Invalid default resource! Please report! " + clazz.getName() + ": "+e.getMessage());
            e.printStackTrace();
        }
        
        return resource;
    }
}
