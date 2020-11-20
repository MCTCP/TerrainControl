package com.pg85.otg.util;

import com.pg85.otg.constants.Constants;

public class BiomeResourceLocation
{
	private static final String BIOME_RESOURCE_LOCATION_SEPARATOR = ".";

	private final String presetName;
	private final String resourcePath;

	public BiomeResourceLocation(String presetName, String biomeName)
	{
		this.presetName = presetName.toLowerCase().trim().replaceAll("[^a-z0-9/_-]", "_");
		this.resourcePath = biomeName.toLowerCase().trim().replaceAll("[^a-z0-9/_-]", "_");
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
		return String.format("%s%s%s", this.presetName, BIOME_RESOURCE_LOCATION_SEPARATOR, this.resourcePath);
	}

	public String toResourceLocationString()
	{
		return String.format("%s%s%s", getResourceDomain(), ":", getResourcePath());
	}
}
