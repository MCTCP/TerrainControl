package com.pg85.otg.presets;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;

import com.pg85.otg.config.biome.BiomeConfig;
import com.pg85.otg.config.world.WorldConfig;

/**
 * Represents an OTG preset, with all its world and biome configs, stored in /config/OpenTerrainGenerator/Presets/\<PresetName\>/.
 */
public class Preset
{
	private final Path presetDir;
	private final String name;
	private final WorldConfig worldConfig;
	private final HashMap<String, BiomeConfig> biomeConfigs = new HashMap<String, BiomeConfig>();
	private final String version;
	private final String author;
	private final String description;
	private final String shortPresetName;


	Preset(Path presetDir, String name, String shortPresetName, WorldConfig worldConfig, ArrayList<BiomeConfig> biomeConfigs)
	{
		this.presetDir = presetDir;
		this.name = name;
		this.shortPresetName = shortPresetName;
		this.worldConfig = worldConfig;
		this.author = worldConfig.getAuthor();
		this.description = worldConfig.getDescription();
		this.version = worldConfig.getVersion();

		for(BiomeConfig biomeConfig : biomeConfigs)
		{
			this.biomeConfigs.put(biomeConfig.getName(), biomeConfig);
		}
	}

	public Path getPresetDir()
	{
		return this.presetDir;
	}

	public String getName()
	{
		return this.name;
	}

	public String getShortPresetName()
	{
		return this.shortPresetName;
	}

	public WorldConfig getWorldConfig()
	{
		return this.worldConfig;
	}

	public BiomeConfig getBiomeConfig(String biomeName)
	{
		return this.biomeConfigs.get(biomeName);
	}

	public ArrayList<BiomeConfig> getAllBiomeConfigs()
	{
		return new ArrayList<BiomeConfig>(this.biomeConfigs.values());
	}

	public String getVersion()
	{
		return this.version;
	}

	public String getAuthor()
	{
		return this.author;
	}

	public String getDescription()
	{
		return this.description;
	}
}
