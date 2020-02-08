package com.pg85.otg.generator.surface;

import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.generator.ChunkBuffer;
import com.pg85.otg.generator.GeneratingChunk;
import com.pg85.otg.util.helpers.StringHelper;
import com.pg85.otg.util.materials.MaterialHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MultipleLayersSurfaceGenerator extends SimpleSurfaceGenerator
{
    // Must be sorted based on the noise field
    private List<LayerChoice> layerChoices;

    public MultipleLayersSurfaceGenerator(String[] args) throws InvalidConfigException
    {
        if (args.length < 2)
        {
            throw new InvalidConfigException("Needs at least two arguments");
        }

        layerChoices = new ArrayList<LayerChoice>();
        for (int i = 0; i < args.length - 2; i += 3)
        {
            LocalMaterialData surfaceBlock = MaterialHelper.readMaterial(args[i]);
            LocalMaterialData groundBlock = MaterialHelper.readMaterial(args[i+1]);
            float maxNoise = (float) StringHelper.readDouble(args[i + 2], -20, 20);
            layerChoices.add(new LayerChoice(surfaceBlock, groundBlock, maxNoise));
        }
        Collections.sort(layerChoices);
    }

    @Override
    public void spawn(LocalWorld world, GeneratingChunk generatingChunkInfo, ChunkBuffer chunkBuffer, BiomeConfig config, int xInWorld, int zInWorld)
    {
        int x = xInWorld & 0xf;
        int z = zInWorld & 0xf;
        double noise = generatingChunkInfo.getNoise(x, z);

        for (LayerChoice layer : this.layerChoices)
        {
            if (noise <= layer.maxNoise)
            {
                spawnColumn(world, layer.surfaceBlock, layer.groundBlock, generatingChunkInfo, chunkBuffer, config, x, z);
                return;
            }
        }

        // Fall back on normal column
        spawnColumn(world, config.surfaceBlock, config.groundBlock, generatingChunkInfo, chunkBuffer, config, x, z);
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
