package com.khorn.terraincontrol.generator;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.util.StringHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SurfaceLayer
{
    public static class GroundLayerChoice implements Comparable<GroundLayerChoice>
    {
        public final byte blockData;
        public final int blockId;
        public final float maxNoise;

        public GroundLayerChoice(int blockId, byte blockData, float maxNoise)
        {
            this.blockId = blockId;
            this.blockData = blockData;
            this.maxNoise = maxNoise;
        }

        @Override
        public int compareTo(GroundLayerChoice that)
        {
            float delta = this.maxNoise - that.maxNoise;
            // The number 65565 is just randomly chosen, any positive number
            // works fine as long as it can represent the floating point delta
            // as an integer
            return (int) (delta * 65565);
        }
    }

    // Must be sorted based on the noise field
    private List<GroundLayerChoice> surfaceLayerChoices;

    public SurfaceLayer(String[] args) throws InvalidConfigException
    {
        if (args.length < 2)
        {
            throw new InvalidConfigException("Needs at least two arguments");
        }
        
        surfaceLayerChoices = new ArrayList<GroundLayerChoice>();
        for (int i = 0; i < args.length - 1; i += 2)
        {
            int blockId = StringHelper.readBlockId(args[i]);
            byte blockData = (byte) StringHelper.readBlockData(args[i]);
            float maxNoise = (float) StringHelper.readDouble(args[i + 1], -20, 20);
            surfaceLayerChoices.add(new GroundLayerChoice(blockId, blockData, maxNoise));
        }
        Collections.sort(surfaceLayerChoices);
    }

    /**
     * Spawns this surface layer in the world.
     * @param world The world to spawn in.
     * @param noise The noise value, from -1 to 1.
     * @param x X position in the world.
     * @param z Z position in the world.
     */
    public void spawn(LocalWorld world, double noise, int x, int z)
    {
        int y = world.getSolidHeight(x, z) - 1;
        BiomeConfig config = world.getSettings().biomeConfigs[world.getBiomeId(x, z)];
        if (config == null || world.getTypeId(x, y, z) != config.surfaceBlock)
        {
            // Not the correct surface block here, so don't replace it
            // This can happen when another chunk populated part of this chunk
            return;
        }

        for (GroundLayerChoice groundLayer : this.surfaceLayerChoices)
        {
            if (noise <= groundLayer.maxNoise)
            {
                world.setBlock(x, y, z, groundLayer.blockId, groundLayer.blockData);
                return;
            }
        }
    }

    @Override
    public String toString()
    {
        StringBuilder stringBuilder = new StringBuilder();
        for (GroundLayerChoice groundLayer : this.surfaceLayerChoices)
        {
            stringBuilder.append(StringHelper.makeMaterial(groundLayer.blockId, groundLayer.blockData));
            stringBuilder.append(',');
            stringBuilder.append(groundLayer.maxNoise);
            stringBuilder.append(',');
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        return stringBuilder.toString();
    }

}
