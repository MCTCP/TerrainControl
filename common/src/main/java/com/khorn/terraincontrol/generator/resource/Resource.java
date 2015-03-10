package com.khorn.terraincontrol.generator.resource;

import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.ConfigFunction;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.logging.LogMarker;
import com.khorn.terraincontrol.util.ChunkCoordinate;

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
     * Spawns the resource on a specific column. This method is normally
     * called by {@link #spawnInChunk(LocalWorld, Random, boolean,
     * ChunkCoordinate)}, which calls it for random columns in the chunk
     * based on the frequency and rarity of the resource..
     *
     * <p>
     * If you want chunk-control over the resource, override spawnInChunk
     * instead. In that case, you are allowed to leave this method blank.
     * @param world          The world.
     * @param random         Random number generator based on the world seed.
     * @param villageInChunk Whether there is a village in the chunk.
     * @param x              The block x.
     * @param z              The block z.
     */
    public abstract void spawn(LocalWorld world, Random random, boolean villageInChunk, int x, int z);

    /**
     * Spawns the resource normally. Fires an event, which can be used to
     * cancel spawning.
     * @param world          The world.
     * @param random         The random number generator.
     * @param villageInChunk Whether there is a village in the chunk.
     * @param chunkCoord     The chunk coordinate.
     */
    public final void process(LocalWorld world, Random random, boolean villageInChunk, ChunkCoordinate chunkCoord)
    {
        // Don't process invalid resources OR Fire event
        if (!isValid() || !TerrainControl.fireResourceProcessEvent(this, world,
                random, villageInChunk, chunkCoord.getChunkX(), chunkCoord.getChunkZ()))
        {
            return;
        }

        // Spawn
        spawnInChunk(world, random, villageInChunk, chunkCoord);
    }

    /**
     * Places the resource in a chunk. The default implementation simply calls
     * {@link #spawn(LocalWorld, Random, boolean, int, int)} for random points
     * in the chunk, based on the frequency and rarity. Subclasses can
     * override this for more fine-tuned behaviour.
     * 
     * @param world          The world.
     * @param random         The random number generator.
     * @param villageInChunk Whether there is a village in the chunk.
     * @param chunkCoord     The chunk coordinate.
     */
    protected void spawnInChunk(LocalWorld world, Random random, boolean villageInChunk, ChunkCoordinate chunkCoord)
    {
        int chunkX = chunkCoord.getBlockXCenter();
        int chunkZ = chunkCoord.getBlockZCenter();
        for (int t = 0; t < frequency; t++)
        {
            if (random.nextDouble() * 100.0 > rarity)
                continue;
            int x = chunkX + random.nextInt(ChunkCoordinate.CHUNK_X_SIZE);
            int z = chunkZ + random.nextInt(ChunkCoordinate.CHUNK_Z_SIZE);
            spawn(world, random, false, x, z);
        }
    }

    /**
     * Convenience method for creating a resource. Used to create the default
     * resources.
     * @param config Biome config the resource will be in.
     * @param clazz  Class of the resource.
     * @param args   Parameters for the resource. The result of toString() of
     *               each arg will be passed to the resource.
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
        try
        {
            resource.init(config, stringArgs);
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
     * This implementation of
     * {@link ConfigFunction#isAnalogousTo(ConfigFunction)} checks whether the
     * classes and the material are the same. For a lot of resources, this is
     * enough, bug other resources need to override this.
     */
    @Override
    public boolean isAnalogousTo(ConfigFunction<BiomeConfig> other) {
        if (!other.getClass().equals(this.getClass()))
        {
            return false;
        }
        Resource resource = (Resource) other;
        return resource.material.equals(this.material);
    }

    /**
     * Returns the material. Resources that don't have a material will return
     * null.
     * @return The material of the resource this object represents.
     */
    public LocalMaterialData getMaterial()
    {
        return material;
    }

    /**
     * Returns whether or not the two resources are property-wise equal.
     * @return {@inheritDoc}
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
        hash = 53 * hash + (this.material == null ? 0 : material.hashCode());
        hash = 53 * hash + this.frequency;
        hash = 53 * hash + (int) (Double.doubleToLongBits(this.rarity) ^ (Double.doubleToLongBits(this.rarity) >>> 32));
        return hash;
    }

}
