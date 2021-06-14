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
	private final Path presetFolder;
	private String presetFolderName;
	private String shortPresetName;
	
	// Note: Since we're not using Supplier<>, we need to be careful about any classes fetching 
	// and caching our worldconfig/biomeconfigs etc, or they won't update when reloaded from disk.
	// BiomeGen and ChunkGen cache some settings during a session, so they'll only update on world exit/rejoin.
	private WorldConfig worldConfig;
	private HashMap<String, BiomeConfig> biomeConfigs = new HashMap<String, BiomeConfig>();
	private int version;
	private String author;
	private String description;
	
	public Preset(Path presetFolder, String shortPresetName, WorldConfig worldConfig, ArrayList<BiomeConfig> biomeConfigs)
	{
		this.presetFolder = presetFolder;
		this.presetFolderName = presetFolder.toFile().getName();
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

	public Path getPresetFolder()
	{
		return this.presetFolder;
	}

	public String getFolderName()
	{
		return this.presetFolderName;
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
	
	public int getVersion()
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
