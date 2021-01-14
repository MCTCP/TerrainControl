package com.pg85.otg.config.biome;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.pg85.otg.config.ConfigFunction;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.interfaces.IBiomeConfig;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IResource;
import com.pg85.otg.util.interfaces.IWorldGenRegion;
import com.pg85.otg.util.materials.LocalMaterialData;

/**
 * Represents a Resource: something that can generate in the world.
 */
public abstract class Resource extends ConfigFunction<IBiomeConfig> implements Comparable<Resource>, IResource
{
    protected int frequency;
    protected LocalMaterialData material;
    protected double rarity;
    
    // Children must implement this constructor, or createResource will fail
    public Resource(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) { }
    
    /**
     * Convenience method for creating a resource. Used to create the default
     * resources.
     * @param config Biome config the resource will be in.
     * @param clazz  Class of the resource.
     * @param args   Parameters for the resource. The result of toString() of
     *               each arg will be passed to the resource.
     * @return A resource based on the given parameters.
     */
    static Resource createResource(IBiomeConfig config, ILogger logger, IMaterialReader materialReader, Class<? extends Resource> clazz, Object... args)
    {
        List<String> stringArgs = new ArrayList<String>(args.length);
        for (Object arg : args)
        {
            stringArgs.add("" + arg);
        }

        try
        {
            return clazz.getConstructor(IBiomeConfig.class, List.class, ILogger.class, IMaterialReader.class).newInstance(config, stringArgs, logger, materialReader);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int compareTo(Resource o)
    {
        return o.getPriority() - this.getPriority();
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

    /**
     * Returns the material. Resources that don't have a material will return
     * null.
     * @return The material of the resource this object represents.
     */
    public LocalMaterialData getMaterial()
    {
        return material;
    }
    
    /*
    protected void parseMaterials(WorldConfig worldConfig, LocalMaterialData material, MaterialSet sourceBlocks)
    {
		material.parseForWorld(worldConfig);

        if (sourceBlocks != null)
        {
            sourceBlocks.parseForWorld(worldConfig);
        }
    }
    */

    public int getPriority()
    {
        return 0;
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

    /**
     * This implementation of
     * {@link ConfigFunction#isAnalogousTo(ConfigFunction)} checks whether the
     * classes and the material are the same. For a lot of resources, this is
     * enough, bug other resources need to override this.
     */
    @Override
    public boolean isAnalogousTo(ConfigFunction<IBiomeConfig> other, ILogger logger)
    {
        if (!other.getClass().equals(this.getClass()))
        {
            return false;
        }
        Resource resource = (Resource) other;
        return resource.material.equals(this.material);
    }

    /**
     * Spawns the resource normally. Fires an event, which can be used to
     * cancel spawning.
     * @param world          The world.
     * @param random         The random number generator.
     * @param villageInChunk Whether there is a village in the chunk.
     * @param chunkCoord     The chunk coordinate.
     */
    public final void process(IWorldGenRegion worldGenregion, Random random, boolean villageInChunk, ChunkCoordinate chunkBeingPopulated, ILogger logger, IMaterialReader materialReader)
    {
        // Fire event
        //if (!worldGenregion.fireResourceProcessEvent(this, random, villageInChunk, chunkBeingPopulated.getChunkX(), chunkBeingPopulated.getChunkZ()))
        {
            //return;
        }

        // Spawn
        spawnInChunk(worldGenregion, random, villageInChunk, chunkBeingPopulated, logger, materialReader);
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
    public abstract void spawn(IWorldGenRegion world, Random random, boolean villageInChunk, int x, int z, ChunkCoordinate chunkBeingPopulated);

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
    protected void spawnInChunk(IWorldGenRegion worldGenRegion, Random random, boolean villageInChunk, ChunkCoordinate chunkBeingPopulated, ILogger logger, IMaterialReader materialReader)
    {
        int chunkX = chunkBeingPopulated.getBlockXCenter();
        int chunkZ = chunkBeingPopulated.getBlockZCenter();
        
        createCache();
        
        for (int t = 0; t < frequency; t++)
        {
            if (random.nextDouble() * 100.0 > rarity)
            {
                continue;
            }
            int x = chunkX + random.nextInt(ChunkCoordinate.CHUNK_SIZE);
            int z = chunkZ + random.nextInt(ChunkCoordinate.CHUNK_SIZE);
            spawn(worldGenRegion, random, false, x, z, chunkBeingPopulated);
        }
        
        clearCache();
    }
    
    protected void createCache()
    {
    	
    }
    
    protected void clearCache()
    {
    	
    }
}
