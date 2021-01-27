package com.pg85.otg.spigot.gen;

import com.pg85.otg.config.dimensions.DimensionConfig;
import com.pg85.otg.presets.Preset;
import com.pg85.otg.spigot.biome.OTGBiomeProvider;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.FifoMap;
import net.minecraft.server.v1_16_R3.GeneratorSettingBase;
import net.minecraft.server.v1_16_R3.IRegistry;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.generator.ChunkGenerator;

import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class SpigotChunkGenerator extends ChunkGenerator
{
	public ReentrantLock initLock = new ReentrantLock();
	public OTGNoiseChunkGenerator generator = null;
	public final Preset preset;
	private final FifoMap<ChunkCoordinate, ChunkData> chunkDataCache = new FifoMap<>(128);

	public SpigotChunkGenerator(Preset preset)
	{
		this.preset = preset;
	}

	@Override
	public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome)
	{
		if (generator == null)
		{
			initLock.lock();
			// Check if someone else made this object since last time
			if (generator == null)
			{
				generator = new OTGNoiseChunkGenerator(
						new DimensionConfig(preset.getName()),
						new OTGBiomeProvider(preset.getName(), world.getSeed(), false, false, ((CraftServer) Bukkit.getServer()).getServer().customRegistry.b(IRegistry.ay)),
						world.getSeed(),
						GeneratorSettingBase::i
				);
			}
			initLock.unlock();
		}
		ChunkCoordinate chunkCoord = ChunkCoordinate.fromChunkCoords(chunkX, chunkZ);
		ChunkData chunkData = chunkDataCache.get(chunkCoord);
		if (chunkData == null)
		{
			chunkData = createChunkData(world);
			generator.buildNoiseSpigot(chunkData, chunkCoord, random);
			chunkDataCache.put(chunkCoord, chunkData);
		}
		return chunkData;

	}

	@Override
	public boolean isParallelCapable()
	{
		// Experimental, requires we're thread safe, which is not the case
		// OreGen borks with this bc of cache
		return false;
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
