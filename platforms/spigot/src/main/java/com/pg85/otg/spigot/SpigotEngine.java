package com.pg85.otg.spigot;

import com.pg85.otg.OTGEngine;
import com.pg85.otg.config.biome.BiomeConfigFinder;
import com.pg85.otg.config.biome.BiomeLoadInstruction;
import com.pg85.otg.spigot.materials.SpigotMaterialReader;
import com.pg85.otg.spigot.presets.SpigotPresetLoader;
import com.pg85.otg.spigot.util.SpigotLogger;
import com.pg85.otg.spigot.util.SpigotPluginLoadedChecker;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Paths;
import java.util.Collection;

public class SpigotEngine extends OTGEngine
{
	protected SpigotEngine (JavaPlugin plugin)
	{
		super(new SpigotLogger(),
				plugin.getDataFolder().toPath(),
				new SpigotMaterialReader(),
				new SpigotPluginLoadedChecker(),
				new SpigotPresetLoader(plugin.getDataFolder()));
	}

	@Override
	public Collection<BiomeLoadInstruction> getDefaultBiomes ()
	{
		// TODO: This does nothing
		return null;
	}

	@Override
	public void mergeVanillaBiomeMobSpawnSettings (BiomeConfigFinder.BiomeConfigStub biomeConfigStub, String biomeResourceLocation)
	{
		// TODO: Implement this
	}
}
