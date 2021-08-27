package com.pg85.otg.forge.api;

import java.util.Optional;

import com.pg85.otg.forge.gen.OTGNoiseChunkGenerator;
import com.pg85.otg.interfaces.IBiomeConfig;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

public class OTGForgeAPI
{
	@SuppressWarnings("resource")
	public static IBiomeConfig getOTGBiome(ServerWorld world, BlockPos pos)
	{
		if (!(world.getChunkSource().generator instanceof OTGNoiseChunkGenerator))
		{
			return null;
		}

		return ((OTGNoiseChunkGenerator) world.getChunkSource().generator).getCachedBiomeProvider()
				.getBiomeConfig(pos.getX(), pos.getZ());
	}

	public static Optional<IBiomeConfig> getOTGBiomeOptional(ServerWorld world, BlockPos pos)
	{
		return Optional.ofNullable(getOTGBiome(world, pos));
	}
}
