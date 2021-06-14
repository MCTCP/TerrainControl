package com.pg85.otg.util.biome;

import com.pg85.otg.constants.Constants;

public class BiomeResourceLocation
{
	private static final String BIOME_RESOURCE_LOCATION_SEPARATOR = ".";

	private final String presetFolder;
	private final String presetShortName;
	private final int presetVersion;
	private final String presetRegistryName;
	private final String biomeName;
	private final String resourceName;

	public BiomeResourceLocation(String presetShortName, int presetVersion, String presetFolder, String biomeName, String resourceName)
	{
		this.presetShortName = presetShortName;
		this.presetVersion = presetVersion;
		this.presetFolder = presetFolder;
		this.presetRegistryName = presetShortName.toLowerCase().trim().replaceAll("[^a-z0-9/_-]", "_") + BIOME_RESOURCE_LOCATION_SEPARATOR + (presetVersion == 0 ? "" : presetVersion);
		this.biomeName = biomeName.toLowerCase().trim().replaceAll("[^a-z0-9/_-]", "_");
		this.resourceName = resourceName;
	}

	public BiomeResourceLocation(String presetShortName, int presetVersion, String presetFolder, String biomeName)
	{
		this(presetShortName, presetVersion, presetFolder, biomeName, null);
	}
	
	public String getPresetFolderName()
	{
		return this.presetFolder;
	}	

	private String getResourceDomain()
	{
		return Constants.MOD_ID_SHORT;
	}

	private String getResourcePath()
	{
		if(this.resourceName != null)
		{
			return String.format("%s%s%s%s%s", this.presetRegistryName, BIOME_RESOURCE_LOCATION_SEPARATOR, this.biomeName, BIOME_RESOURCE_LOCATION_SEPARATOR, this.resourceName);
		} else {			
			return String.format("%s%s%s", this.presetRegistryName, BIOME_RESOURCE_LOCATION_SEPARATOR, this.biomeName);
		}
	}

	public String toResourceLocationString()
	{
		return String.format("%s%s%s", getResourceDomain(), ":", getResourcePath());
	}

	public BiomeResourceLocation withBiomeResource(String resourceName)
	{
		return new BiomeResourceLocation(this.presetShortName, this.presetVersion, this.presetFolder, this.biomeName, resourceName);
	}
}
