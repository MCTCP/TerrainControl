package com.khorn.terraincontrol.generator.surface;

import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.util.helpers.StringHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimpleSurfaceGenerator implements SurfaceGenerator
{
    public static class LayerChoice implements Comparable<LayerChoice>
    {
        public final LocalMaterialData surfaceBlock;
        public final LocalMaterialData groundBlock;
        public final float maxNoise;

        public LayerChoice(LocalMaterialData surfaceBlock, LocalMaterialData groundBlock, float maxNoise)
        {
            this.surfaceBlock = surfaceBlock;
            this.groundBlock = groundBlock;
            this.maxNoise = maxNoise;
        }

        @Override
        public int compareTo(LayerChoice that)
        {
            float delta = this.maxNoise - that.maxNoise;
            // The number 65565 is just randomly chosen, any positive number
            // works fine as long as it can represent the floating point delta
            // as an integer
            return (int) (delta * 65565);
        }
    }

    // Must be sorted based on the noise field
    private List<LayerChoice> layerChoices;

    public SimpleSurfaceGenerator(String[] args) throws InvalidConfigException
    {
        if (args.length < 2)
        {
            throw new InvalidConfigException("Needs at least two arguments");
        }
        
        layerChoices = new ArrayList<LayerChoice>();
        for (int i = 0; i < args.length - 2; i += 3)
        {
            LocalMaterialData surfaceBlock = TerrainControl.readMaterial(args[i]);
            LocalMaterialData groundBlock = TerrainControl.readMaterial(args[i+1]);
            float maxNoise = (float) StringHelper.readDouble(args[i + 2], -20, 20);
            layerChoices.add(new LayerChoice(surfaceBlock, groundBlock, maxNoise));
        }
        Collections.sort(layerChoices);
    }

    @Override
    public void spawn(LocalWorld world, double noise, int x, int z)
    {
        int y = world.getSolidHeight(x, z) - 1;
        BiomeConfig config = world.getCalculatedBiome(x, z).getBiomeConfig();
        if (config == null || !world.getMaterial(x, y, z).equals(config.surfaceBlock))
        {
            // Not the correct surface block here, so don't replace it
            // This can happen when another chunk populated part of this chunk
            return;
        }

        for (LayerChoice layer : this.layerChoices)
        {
            if (noise <= layer.maxNoise)
            {
                world.setBlock(x, y, z, layer.surfaceBlock);
                for (int i = 1; i < 4; i++)
                {
                    world.setBlock(x, y - i, z, layer.groundBlock);
                }
                return;
            }
        }
    }

    @Override
    public String toString()
    {
        StringBuilder stringBuilder = new StringBuilder();
        for (LayerChoice groundLayer : this.layerChoices)
        {
            stringBuilder.append(groundLayer.surfaceBlock);
            stringBuilder.append(',').append(' ');
            stringBuilder.append(groundLayer.groundBlock);
            stringBuilder.append(',').append(' ');
            stringBuilder.append(groundLayer.maxNoise);
            stringBuilder.append(',').append(' ');
        }
        // Delete last ", "
        stringBuilder.deleteCharAt(stringBuilder.length() - 2);
        return stringBuilder.toString();
    }

}
