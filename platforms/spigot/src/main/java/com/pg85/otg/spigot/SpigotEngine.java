package com.pg85.otg.spigot;

import com.pg85.otg.OTG;
import com.pg85.otg.OTGEngine;
import com.pg85.otg.config.biome.BiomeConfigFinder;
import com.pg85.otg.config.biome.BiomeLoadInstruction;
import com.pg85.otg.config.minecraft.DefaultBiome;
import com.pg85.otg.spigot.biome.SpigotMojangSettings;
import com.pg85.otg.spigot.materials.SpigotMaterialReader;
import com.pg85.otg.spigot.presets.SpigotPresetLoader;
import com.pg85.otg.spigot.util.SpigotLogger;
import com.pg85.otg.spigot.util.SpigotPluginLoadedChecker;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SpigotEngine extends OTGEngine
{
	private final JavaPlugin plugin;

	protected SpigotEngine (JavaPlugin plugin)
	{
		super(new SpigotLogger(),
				plugin.getDataFolder().toPath(),
				new SpigotMaterialReader(),
				new SpigotPluginLoadedChecker(),
				new SpigotPresetLoader(plugin.getDataFolder()));
		this.plugin = plugin;
	}

	@Override
	public Collection<BiomeLoadInstruction> getDefaultBiomes ()
	{
		List<BiomeLoadInstruction> standardBiomes = new ArrayList<>();
		for (DefaultBiome defaultBiome : DefaultBiome.values())
		{
			int id = defaultBiome.Id;
			BiomeLoadInstruction instruction = defaultBiome.getLoadInstructions(SpigotMojangSettings.fromId(id),
					128, OTG.getEngine().getLogger());
			standardBiomes.add(instruction);
		}
		return standardBiomes;
	}

	@Override
	public void mergeVanillaBiomeMobSpawnSettings (BiomeConfigFinder.BiomeConfigStub biomeConfigStub, String biomeResourceLocation)
	{
		// TODO: Implement this
	}

	@Override
	public File getJarFile()
	{
		String fileName = plugin.getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
		// URLEncoded string, decode.
		try {
			fileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException e) { }
		
		if(fileName != null)
		{
			File modFile = new File(fileName);
			if(modFile.isFile())
			{
				return modFile;
			}
		}
		return null;
	}
}
