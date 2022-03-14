package com.pg85.otg.paper.gen;

import com.pg85.otg.core.OTG;
import com.pg85.otg.core.presets.Preset;
import com.pg85.otg.paper.PaperEngine;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.FifoMap;

import org.bukkit.World;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;
import org.bukkit.generator.ChunkGenerator;

import java.util.Random;

public class OTGPaperChunkGen extends ChunkGenerator
{
	public OTGNoiseChunkGenerator generator = null;
	private final FifoMap<ChunkCoordinate, ChunkData> chunkDataCache = new FifoMap<>(128);
	private final Preset preset;
	
	public OTGPaperChunkGen(Preset preset)
	{
		this.preset = preset;
	}
	
	// In case generator isn't loaded yet, expose preset.
	public Preset getPreset()
	{
		return this.preset;
	}
	
	@Override
	public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome)
	{		
		if (generator == null)
		{
			((PaperEngine) OTG.getEngine()).getPlugin().injectInternalGenerator(world);
			generator.fixBiomes(chunkX, chunkZ);
		}

		ChunkCoordinate chunkCoord = ChunkCoordinate.fromChunkCoords(chunkX, chunkZ);
		ChunkData chunkData = chunkDataCache.get(chunkCoord);
		if (chunkData == null)
		{
			chunkData = createChunkData(world);
			generator.buildNoiseSpigot(((CraftWorld)world).getHandle(), chunkData, chunkCoord, random);
			chunkDataCache.put(chunkCoord, chunkData);
		}
		return chunkData;
	}

	@Override
	public boolean isParallelCapable()
	{
		return true;
	}

	@Override
	public boolean shouldGenerateCaves()
	{
		return true;
	}

	@Override
	public boolean shouldGenerateDecorations()
	{
		return true;
	}

	@Override
	public boolean shouldGenerateMobs()
	{
		return true;
	}

	@Override
	public boolean shouldGenerateStructures()
	{
		return true;
	}
}
