package com.pg85.otg.forge.api;

import java.util.Optional;

import com.pg85.otg.forge.gen.OTGNoiseChunkGenerator;
import com.pg85.otg.interfaces.IBiomeConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public class OTGForgeAPI
{
	@SuppressWarnings("resource")
	public static IBiomeConfig getOTGBiome(ServerLevel world, BlockPos pos)
	{
		if (!(world.getChunkSource().getGenerator() instanceof OTGNoiseChunkGenerator))
		{
			return null;
		}

		return ((OTGNoiseChunkGenerator) world.getChunkSource().getGenerator()).getCachedBiomeProvider()
				.getBiomeConfig(pos.getX(), pos.getZ());
	}

	public static Optional<IBiomeConfig> getOTGBiomeOptional(ServerLevel world, BlockPos pos)
	{
		return Optional.ofNullable(getOTGBiome(world, pos));
	}
}
