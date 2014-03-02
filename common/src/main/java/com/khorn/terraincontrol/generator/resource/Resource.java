package com.khorn.terraincontrol.generator.resource;

import com.khorn.terraincontrol.LocalMaterialData;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.ConfigFunction;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.logging.LogMarker;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Represents a Resource: something that can generate in the world.
 */
public abstract class Resource extends ConfigFunction<BiomeConfig>
{

    protected LocalMaterialData material;
    protected int frequency;
    protected double rarity;

    @Override
    public Class<BiomeConfig> getHolderType()
    {
        return BiomeConfig.class;
    }

    /**
     * Spawns the resource at this position, ignoring rarity and frequency.
     * <p/>
     * If you want chunk-control over the resource, override spawnInChunk
     * instead, and leave this method blank.
     * <p/>
     * @param world          The world.
     * @param villageInChunk Whether there is a village in the chunk.
     * @param x              The block x.
     * @param z              The block z.
     */
    public abstract void spawn(LocalWorld world, Random random, boolean villageInChunk, int x, int z);

    /**
     * Spawns the resource normally. Can be cancelled by an event.
     * <p/>
     * If you want to override this, override spawnInChunk instead.
     * <p/>
     * @param world          The world.
     * @param random         The random number generator.
     * @param villageInChunk Whether there is a village in the chunk.
     * @param chunkX         The chunk x.
     * @param chunkZ         The chunk z.
     */
    public final void process(LocalWorld world, Random random, boolean villageInChunk, int chunkX, int chunkZ)
    {

        // Don't process invalid resources OR Fire event
        if (!isValid() || !TerrainControl.fireResourceProcessEvent(this, world, random, villageInChunk, chunkX, chunkZ))
        {
            return;
        }

        // Spawn
        spawnInChunk(world, random, villageInChunk, chunkX, chunkZ);
    }

    /**
     * Called once per chunk, instead of once per attempt.
     * <p/>
     * @param world          The world.
     * @param random         The random number generator.
     * @param villageInChunk Whether there is a village in the chunk.
     * @param chunkX         The chunk x.
     * @param chunkZ         The chunk z.
     */
    protected void spawnInChunk(LocalWorld world, Random random, boolean villageInChunk, int chunkX, int chunkZ)
    {
        for (int t = 0; t < frequency; t++)
        {
            if (random.nextDouble() * 100.0 > rarity)
                continue;
            int x = chunkX * 16 + random.nextInt(16) + 8;
            int z = chunkZ * 16 + random.nextInt(16) + 8;
            spawn(world, random, false, x, z);
        }
    }

    /**
     * Convenience method for creating a resource. Used to create the
     * default resources.
     * <p/>
     * @param config
     * @param clazz
     * @param args
     * <p/>
     * @return A resource based on the given parameters.
     */
    public static Resource createResource(BiomeConfig config, Class<? extends Resource> clazz, Object... args)
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
            resource.setValid(true);
        } catch (InvalidConfigException e)
        {
            TerrainControl.log(LogMarker.FATAL, "Invalid default resource! Please report! {}: {}", new Object[]
            {
                clazz.getName(), e.getMessage()
            });
            TerrainControl.printStackTrace(LogMarker.FATAL, e);
            throw new RuntimeException(e);
        }

        return resource;
    }

    /**
     * Returns the material. Resources that don't have a material will return null.
     * <p/>
     * @return The material of the resource this object represents.
     */
    public LocalMaterialData getMaterial()
    {
        return material;
    }

    
    /**
     * Returns whether or not the two resources are similar to each other AND
     * not equal. This should return true if two resources are of the same class
     * and if critical element are the same. For example source blocks. This 
     * will be used to test if a resource should be overridden via inheritance.
     * @return
     */
    public abstract boolean isAnalogousTo(Resource other);

    /**
     * Returns whether or not the two resources are property-wise equal.
     * <p/>
     * @return
     */
    @Override
    public boolean equals(Object other)
    {
        if (other == null)
            return false;
        if (other == this)
            return true;
        if (getClass() != other.getClass())
            return false;
        final Resource compare = (Resource) other;

        // Check for null materials
        if (this.material == null)
        {
            if (compare.material != null)
            {
                return false;
            }
        }

        return this.material.equals(compare.material)
               && this.frequency == compare.frequency
               && this.rarity == compare.rarity;
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 53 * hash + (this.material == null? 0 : material.hashCode());
        hash = 53 * hash + this.frequency;
        hash = 53 * hash + (int) (Double.doubleToLongBits(this.rarity) ^ (Double.doubleToLongBits(this.rarity) >>> 32));
        return hash;
    }
    
}
