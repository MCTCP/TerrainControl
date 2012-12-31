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
public abstract class Resource extends ConfigFunction<WorldConfig>
{
    protected int blockId = -1;
    protected int blockData = -1;

    @Override
    public Class<WorldConfig> getHolderType()
    {
        return WorldConfig.class;
    }

    /**
     * Spawns the resource at this position, ignoring rarity and frequency.
     * 
     * @param world
     *            The world.
     * @param villageInChunk
     *            Whether there is a village in the chunk.
     * @param x
     *            The block x.
     * @param z
     *            The block z.
     */
    public abstract void spawn(LocalWorld world, Random random, boolean villageInChunk, int x, int z);

    /**
     * Spawns the resource normally. Can be cancelled by an event.
     * 
     * When you override this, don't forget to call the event!
     * 
     * @param world
     *            The world.
     * @param villageInChunk
     *            Whether there is a village in the chunk.
     * @param chunkX
     *            The chunk x.
     * @param chunkZ
     *            The chunk z.
     */
    public void process(LocalWorld world, Random random, boolean villageInChunk, int chunkX, int chunkZ)
    {
        // Fire event
        if (!TerrainControl.fireResourceProcessEvent(this, world, random, villageInChunk, chunkX, chunkZ))
        {
            return;
        }

        // Process
        for (int t = 0; t < frequency; t++)
        {
            if (random.nextInt(100) > rarity)
                continue;
            int x = chunkX * 16 + random.nextInt(16) + 8;
            int z = chunkZ * 16 + random.nextInt(16) + 8;
            spawn(world, random, false, x, z);
        }
    }

    /**
     * Convenience method for creating a resource. Used to create the default
     * resources.
     * 
     * @param world
     * @param clazz
     * @param args
     * @return
     */
    public static Resource createResource(WorldConfig config, Class<? extends Resource> clazz, Object... args)
    {
        List<String> stringArgs = new ArrayList<String>(args.length);
        for (Object arg : args)
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
        resource.setHolder(config);
        try
        {
            resource.load(stringArgs);
        } catch (InvalidResourceException e)
        {
            TerrainControl.log("Invalid default resource! Please report! " + clazz.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }

        return resource;
    }

    /**
     * Returns the block id. Resources that don't have a block id should return
     * -1.
     * 
     * @return
     */
    public int getBlockId()
    {
        return blockId;
    }
}
