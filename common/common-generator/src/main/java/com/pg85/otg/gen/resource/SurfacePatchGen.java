package com.pg85.otg.gen.resource;

import com.pg85.otg.config.ConfigFunction;
import com.pg85.otg.config.biome.Resource;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.gen.noise.legacy.NoiseGeneratorSurfacePatchOctaves;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.interfaces.IBiomeConfig;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IWorldGenRegion;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.MaterialSet;

import java.util.List;
import java.util.Random;

/**
 * Generates patches based on noise.
 * TODO: Expose the noise weights and scale to the config
 */
public class SurfacePatchGen extends Resource
{
    private LocalMaterialData decorationAboveReplacements;
    private final int maxAltitude;
    private final int minAltitude;
    /**
     * To get nice patches, we need our own noise generator here
     */
    private final NoiseGeneratorSurfacePatchOctaves noiseGen;
    private final Random random;
    private final MaterialSet sourceBlocks;

    public SurfacePatchGen(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
    {
    	super(biomeConfig, args, logger, materialReader);
        assureSize(4, args);

        material = materialReader.readMaterial(args.get(0));
        decorationAboveReplacements = materialReader.readMaterial(args.get(1));
        minAltitude = readInt(args.get(2), Constants.WORLD_DEPTH, Constants.WORLD_HEIGHT - 1);
        maxAltitude = readInt(args.get(3), minAltitude, Constants.WORLD_HEIGHT - 1);
        sourceBlocks = readMaterials(args, 4, materialReader);
        random = new Random(2345L);
        noiseGen = new NoiseGeneratorSurfacePatchOctaves(random, 1);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!super.equals(obj))
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        SurfacePatchGen other = (SurfacePatchGen) obj;
        if (!decorationAboveReplacements.equals(other.decorationAboveReplacements))
        {
            return false;
        }
        if (maxAltitude != other.maxAltitude)
        {
            return false;
        }
        if (minAltitude != other.minAltitude)
        {
            return false;
        }
        if (!sourceBlocks.equals(other.sourceBlocks))
        {
            return false;
        }
        return true;
    }

    @Override
    public int getPriority()
    {
        return -10;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((decorationAboveReplacements == null) ? 0 : decorationAboveReplacements.hashCode());
        result = prime * result + maxAltitude;
        result = prime * result + minAltitude;
        result = prime * result + ((sourceBlocks == null) ? 0 : sourceBlocks.hashCode());
        return result;
    }

    @Override
    public boolean isAnalogousTo(ConfigFunction<IBiomeConfig> other, ILogger logger)
    {
        if (!other.getClass().equals(this.getClass()))
        {
            return false;
        }
        SurfacePatchGen resource = (SurfacePatchGen) other;
        return resource.material.equals(this.material)
                && resource.decorationAboveReplacements.equals(this.decorationAboveReplacements);
    }

    @Override
    public String toString()
    {
        return "SurfacePatch(" + material + "," + decorationAboveReplacements + ","
                + minAltitude + "," + maxAltitude + "," + sourceBlocks + ")";
    }

    @Override
    public void spawn(IWorldGenRegion worldGenRegion, Random rand, boolean villageInChunk, int x, int z, ChunkCoordinate chunkBeingPopulated)
    {
    	// Make sure we stay within population bounds, anything outside won't be spawned (unless it's in an existing chunk).
    	
        int y = worldGenRegion.getHighestBlockAboveYAt(x, z, chunkBeingPopulated) - 1;
        if (y < minAltitude || y > maxAltitude)
            return;
        
        //parseMaterials(worldGenRegion.getWorldConfig(), material, sourceBlocks);
		
        double yNoise = noiseGen.getYNoise(x * 0.25D, z * 0.25D);
        if (yNoise > 0.0D)
        {
            LocalMaterialData materialAtLocation = worldGenRegion.getMaterial(x, y, z, chunkBeingPopulated);
            if (sourceBlocks.contains(materialAtLocation))
            {
            	worldGenRegion.setBlock(x, y, z, material, null, chunkBeingPopulated, true);

                if (yNoise < 0.12D)
                {
                	worldGenRegion.setBlock(x, y + 1, z, decorationAboveReplacements, null, chunkBeingPopulated, true);
                }
            }
        }
    }

    @Override
    protected void spawnInChunk(IWorldGenRegion worldGenRegion, Random random, boolean villageInChunk, ChunkCoordinate chunkBeingPopulated, ILogger logger, IMaterialReader materialReader)
    {
        int chunkX = chunkBeingPopulated.getBlockXCenter();
        int chunkZ = chunkBeingPopulated.getBlockZCenter();
        for (int z0 = 0; z0 < ChunkCoordinate.CHUNK_SIZE; z0++)
        {
            for (int x0 = 0; x0 < ChunkCoordinate.CHUNK_SIZE; x0++)
            {
                int x = chunkX + x0;
                int z = chunkZ + z0;
                spawn(worldGenRegion, random, false, x, z, chunkBeingPopulated);
            }
        }
    }
}
