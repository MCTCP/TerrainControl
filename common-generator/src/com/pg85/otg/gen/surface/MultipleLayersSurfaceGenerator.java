package com.pg85.otg.gen.surface;

import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.gen.ChunkBuffer;
import com.pg85.otg.util.gen.GeneratingChunk;
import com.pg85.otg.util.helpers.StringHelper;
import com.pg85.otg.util.interfaces.IBiomeConfig;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IWorldGenRegion;
import com.pg85.otg.util.materials.LocalMaterialData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MultipleLayersSurfaceGenerator extends SimpleSurfaceGenerator
{
    // Must be sorted based on the noise field
    private List<LayerChoice> layerChoices;

    public MultipleLayersSurfaceGenerator(String[] args, IMaterialReader materialReader) throws InvalidConfigException
    {
        if (args.length < 2)
        {
            throw new InvalidConfigException("Needs at least two arguments");
        }

        layerChoices = new ArrayList<LayerChoice>();
        for (int i = 0; i < args.length - 2; i += 3)
        {
            LocalMaterialData surfaceBlock = materialReader.readMaterial(args[i]);
            LocalMaterialData groundBlock = materialReader.readMaterial(args[i+1]);
            float maxNoise = (float) StringHelper.readDouble(args[i + 2], -20, 20);
            layerChoices.add(new LayerChoice(surfaceBlock, groundBlock, maxNoise));
        }
        Collections.sort(layerChoices);
    }

    @Override
    public LocalMaterialData getSurfaceBlockAtHeight(IWorldGenRegion worldGenRegion, IBiomeConfig biomeConfig, int xInWorld, int yInWorld, int zInWorld)
    {
    	double noise = worldGenRegion.getBiomeBlocksNoiseValue(xInWorld, zInWorld);    	
        for (LayerChoice layer : this.layerChoices)
        {
            if (noise <= layer.maxNoise)
            {
            	return layer.getSurfaceBlockReplaced(biomeConfig.biomeConfigsHaveReplacement(), biomeConfig.getReplaceBlocks(), yInWorld);
            }
        }        
        return biomeConfig.getSurfaceBlockReplaced(yInWorld);
    }

	@Override
	public LocalMaterialData getGroundBlockAtHeight(IWorldGenRegion worldGenRegion, IBiomeConfig biomeConfig, int xInWorld, int yInWorld, int zInWorld)
	{	
   		double noise = worldGenRegion.getBiomeBlocksNoiseValue(xInWorld, zInWorld);  		
        for (LayerChoice layer : this.layerChoices)
        {
            if (noise <= layer.maxNoise)
            {
            	return layer.getGroundBlockReplaced(biomeConfig.biomeConfigsHaveReplacement(), biomeConfig.getReplaceBlocks(), yInWorld);
            }
        }        
        return biomeConfig.getGroundBlockReplaced(yInWorld);
	}

    @Override
    public void spawn(long worldSeed, GeneratingChunk generatingChunkInfo, ChunkBuffer chunkBuffer, IBiomeConfig config, int xInWorld, int zInWorld)
    {
        int x = xInWorld & 0xf;
        int z = zInWorld & 0xf;
        double noise = generatingChunkInfo.getNoise(x, z);

        for (LayerChoice layer : this.layerChoices)
        {
            if (noise <= layer.maxNoise)
            {
                spawnColumn(layer, generatingChunkInfo, chunkBuffer, config, x, z);
                return;
            }
        }

        // Fall back on normal column
        spawnColumn(null, generatingChunkInfo, chunkBuffer, config, x, z);
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
