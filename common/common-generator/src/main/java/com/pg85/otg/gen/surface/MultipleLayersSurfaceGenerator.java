package com.pg85.otg.gen.surface;

import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.IBiome;
import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.ISurfaceGeneratorNoiseProvider;
import com.pg85.otg.util.gen.ChunkBuffer;
import com.pg85.otg.util.gen.GeneratingChunk;
import com.pg85.otg.util.helpers.StringHelper;
import com.pg85.otg.util.materials.LocalMaterialData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class MultipleLayersSurfaceGenerator extends SimpleSurfaceGenerator
{
	// Must be sorted based on the noise field
	private List<MultipleLayersSurfaceGeneratorLayer> layers;

	MultipleLayersSurfaceGenerator(String[] args, IMaterialReader materialReader) throws InvalidConfigException
	{
		if (args.length < 2)
		{
			throw new InvalidConfigException("Needs at least two arguments");
		}

		layers = new ArrayList<MultipleLayersSurfaceGeneratorLayer>();
		for (int i = 0; i < args.length - 2; i += 3)
		{
			LocalMaterialData surfaceBlock = materialReader.readMaterial(args[i]);
			LocalMaterialData groundBlock = materialReader.readMaterial(args[i+1]);
			float maxNoise = (float) StringHelper.readDouble(args[i + 2], -20, 20);
			layers.add(new MultipleLayersSurfaceGeneratorLayer(surfaceBlock, groundBlock, maxNoise));
		}
		Collections.sort(layers);
	}

	@Override
	public LocalMaterialData getSurfaceBlockAtHeight(ISurfaceGeneratorNoiseProvider noiseProvider, IBiomeConfig biomeConfig, int xInWorld, int yInWorld, int zInWorld)
	{
		double noise = noiseProvider.getBiomeBlocksNoiseValue(xInWorld, zInWorld);
		for (MultipleLayersSurfaceGeneratorLayer layer : this.layers)
		{
			if (noise <= layer.maxNoise)
			{
				return layer.getSurfaceBlockReplaced(yInWorld, biomeConfig);
			}
		}		
		return biomeConfig.getSurfaceBlockReplaced(yInWorld);
	}

	@Override
	public LocalMaterialData getGroundBlockAtHeight(ISurfaceGeneratorNoiseProvider noiseProvider, IBiomeConfig biomeConfig, int xInWorld, int yInWorld, int zInWorld)
	{	
		double noise = noiseProvider.getBiomeBlocksNoiseValue(xInWorld, zInWorld);  		
		for (MultipleLayersSurfaceGeneratorLayer layer : this.layers)
		{
			if (noise <= layer.maxNoise)
			{
				return layer.getGroundBlockReplaced(yInWorld, biomeConfig);
			}
		}		
		return biomeConfig.getGroundBlockReplaced(yInWorld);
	}

	@Override
	public void spawn(long worldSeed, GeneratingChunk generatingChunkInfo, ChunkBuffer chunkBuffer, IBiome biome, int xInWorld, int zInWorld)
	{
		int x = xInWorld & 0xf;
		int z = zInWorld & 0xf;
		double noise = generatingChunkInfo.getNoise(x, z);

		for (MultipleLayersSurfaceGeneratorLayer layer : this.layers)
		{
			if (noise <= layer.maxNoise)
			{
				spawnColumn(layer, generatingChunkInfo, chunkBuffer, biome, x, z);
				return;
			}
		}

		// Fall back on normal column
		spawnColumn(null, generatingChunkInfo, chunkBuffer, biome, x, z);
	}

	@Override
	public String toString()
	{
		StringBuilder stringBuilder = new StringBuilder();
		for (MultipleLayersSurfaceGeneratorLayer groundLayer : this.layers)
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
