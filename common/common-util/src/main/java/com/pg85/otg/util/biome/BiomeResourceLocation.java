package com.pg85.otg.util.biome;

import com.pg85.otg.constants.Constants;

public class BiomeResourceLocation
{
	private static final String BIOME_RESOURCE_LOCATION_SEPARATOR = ".";

	private final String presetName;
	private final String biomeName;
	private final String resourceName;

	public BiomeResourceLocation(String presetName, String biomeName, String resourceName)
	{
		this.presetName = presetName.toLowerCase().trim().replaceAll("[^a-z0-9/_-]", "_");
		this.biomeName = biomeName.toLowerCase().trim().replaceAll("[^a-z0-9/_-]", "_");
		this.resourceName = resourceName;
	}

	public BiomeResourceLocation(String presetName, String biomeName)
	{
		this(presetName, biomeName, null);
	}

	public String getPresetName()
	{
		return this.presetName;
	}

	public String getResourceDomain()
	{
		return Constants.MOD_ID_SHORT;
	}

	public String getResourcePath()
	{
		if(this.resourceName != null)
		{
			return String.format("%s%s%s%s%s", this.presetName, BIOME_RESOURCE_LOCATION_SEPARATOR, this.biomeName, BIOME_RESOURCE_LOCATION_SEPARATOR, this.resourceName);
		} else {			
			return String.format("%s%s%s", this.presetName, BIOME_RESOURCE_LOCATION_SEPARATOR, this.biomeName);
		}
	}

	public String toResourceLocationString()
	{
		return String.format("%s%s%s", getResourceDomain(), ":", getResourcePath());
	}

	public BiomeResourceLocation withBiomeResource(String resourceName)
	{
		return new BiomeResourceLocation(this.presetName, this.biomeName, resourceName);
	}
}
