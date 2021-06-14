package com.pg85.otg.util.biome;

import java.nio.file.Path;

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

	public BiomeResourceLocation(Path presetFolder, String presetShortName, int presetVersion, String biomeName, String resourceName)
	{
		this.presetVersion = presetVersion;
		this.presetFolder = presetFolder.toFile().getName();
		this.presetShortName = presetShortName != null && presetShortName.trim().length() > 0 ? presetShortName : this.presetFolder;		
		this.presetRegistryName = this.presetShortName.toLowerCase().trim().replaceAll("[^a-z0-9/_-]", "_") + (presetVersion == 0 ? "" : BIOME_RESOURCE_LOCATION_SEPARATOR + presetVersion);
		this.biomeName = biomeName.toLowerCase().trim().replaceAll("[^a-z0-9/_-]", "_");
		this.resourceName = resourceName;
	}

	public BiomeResourceLocation(Path presetFolder, String presetShortName, int presetVersion, String biomeName)
	{
		this(presetFolder, presetShortName, presetVersion, biomeName, null);
	}
	
	private BiomeResourceLocation(String presetFolderName, String presetShortName, int presetVersion, String presetRegistryName, String biomeName, String resourceName)
	{
		this.presetVersion = presetVersion;
		this.presetFolder = presetFolderName;
		this.presetShortName = presetShortName;		
		this.presetRegistryName = presetRegistryName;
		this.biomeName = biomeName;
		this.resourceName = resourceName;
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
		return new BiomeResourceLocation(this.presetFolder, this.presetShortName, this.presetVersion, this.presetRegistryName, this.biomeName, resourceName);
	}
}
