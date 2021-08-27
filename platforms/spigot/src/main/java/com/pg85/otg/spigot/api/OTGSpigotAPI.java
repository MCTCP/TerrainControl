package com.pg85.otg.spigot.api;

import java.util.Optional;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;

import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.spigot.gen.OTGNoiseChunkGenerator;

import net.minecraft.server.v1_16_R3.WorldServer;

public class OTGSpigotAPI
{
	public static IBiomeConfig getOTGBiome(Location location)
	{
		WorldServer world = ((CraftWorld) location.getWorld()).getHandle();

		if (!(world.getChunkProvider().getChunkGenerator() instanceof OTGNoiseChunkGenerator))
		{
			return null;
		}

		return ((OTGNoiseChunkGenerator) world.getChunkProvider().getChunkGenerator()).getCachedBiomeProvider()
				.getBiomeConfig(location.getBlockX(), location.getBlockZ());
	}

	public static Optional<IBiomeConfig> getOTGBiomeOptional(Location location)
	{
		return Optional.ofNullable(getOTGBiome(location));
	}
}
