package com.pg85.otg.spigot.gen;

import com.pg85.otg.OTG;
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
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.generator.ChunkGenerator;

import java.util.Random;

public class SpigotChunkGenerator extends ChunkGenerator
{
	public final OTGNoiseChunkGenerator generator;
	public final Preset preset;
	private final String worldName;
	private final long seed;
	private final boolean init = false;
	private final FifoMap<ChunkCoordinate, ChunkData> chunkDataCache = new FifoMap<>(128);

	public SpigotChunkGenerator (String worldName, String presetName, long seed)
	{
		this.worldName = worldName;
		this.preset = OTG.getEngine().getPresetLoader().getPresetByName(presetName);
		this.seed = seed;
		this.generator = new OTGNoiseChunkGenerator(
				new DimensionConfig(preset.getName()),
				new OTGBiomeProvider(preset.getName(), seed, false, false, ((CraftServer) Bukkit.getServer()).getServer().customRegistry.b(IRegistry.ay)),
				seed,
				GeneratorSettingBase::i
		);
	}

	@Override
	public ChunkData generateChunkData (World world, Random random, int chunkX, int chunkZ, BiomeGrid biome)
	{
		if (!init)
		{
			generator.world = ((CraftWorld) Bukkit.getWorld(worldName)).getHandle();
		}

		ChunkCoordinate chunkCoord = ChunkCoordinate.fromChunkCoords(chunkX, chunkZ);
		ChunkData chunkData = chunkDataCache.get(chunkCoord);
		if (chunkData == null)
		{
			chunkData = createChunkData(world);
			generator.buildNoiseSpigot(chunkData, chunkCoord);
		}
		return chunkData;

	}

	@Override
	public boolean shouldGenerateCaves ()
	{
		return true;
	}

	@Override
	public boolean shouldGenerateDecorations ()
	{
		return true;
	}

	@Override
	public boolean shouldGenerateMobs ()
	{
		return true;
	}

	@Override
	public boolean shouldGenerateStructures ()
	{
		return true;
	}
}
