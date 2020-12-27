package com.pg85.otg.spigot.gen;

import com.pg85.otg.OTG;
import com.pg85.otg.config.dimensions.DimensionConfig;
import com.pg85.otg.presets.Preset;
import com.pg85.otg.spigot.biome.OTGBiomeProvider;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.FifoMap;
import net.minecraft.server.v1_16_R3.*;
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
	private final int seed;
	private boolean init = false;
	// private StructureManager structureManager = null;
	private final FifoMap<ChunkCoordinate, ChunkData> chunkDataCache = new FifoMap<>(128);
	// private WorldServer worldServer = null;

	public SpigotChunkGenerator (String worldName, String presetName)
	{
		this.worldName = worldName;
		this.preset = OTG.getEngine().getPresetLoader().getPresetByName(presetName);
		this.seed = 0;
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
		// Return an empty chunkData -> let our OTGNoiseChunkGen run buildNoise when carving is called
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

			/*
			OTG.log(LogMarker.INFO, "Adding decorations");
			RegionLimitedWorldAccess region = new RegionLimitedWorldAccess(this.worldServer, new ArrayList<>(Collections.singleton(chunk)));
			generator.addDecorations(region, this.structureManager);
			OTG.log(LogMarker.INFO, "Adding mobs");
			generator.addMobs(region);
			OTG.log(LogMarker.INFO, "Chunk completed!");
			*/
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
