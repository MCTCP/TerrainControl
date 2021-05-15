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
	private final String shortPresetName;
	
	// Note: Since we're not using Supplier<>, we need to be careful about any classes fetching 
	// and caching our worldconfig/biomeconfigs etc, or they won't update when reloaded from disk.
	// BiomeGen and ChunkGen cache some settings during a session, so they'll only update on world exit/rejoin.
	private WorldConfig worldConfig;
	private HashMap<String, BiomeConfig> biomeConfigs = new HashMap<String, BiomeConfig>();
	private String version;
	private String author;
	private String description;

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
	
	public void update(Preset preset)
	{
		this.worldConfig = preset.worldConfig;
		this.biomeConfigs = preset.biomeConfigs;
		this.author = preset.author;
		this.description = preset.description; 
		this.version = preset.version;
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
