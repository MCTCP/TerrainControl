package com.pg85.otg.util.biome;

import java.nio.file.Path;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.interfaces.IBiomeResourceLocation;

public class OTGBiomeResourceLocation implements IBiomeResourceLocation
{
	private static final String BIOME_RESOURCE_LOCATION_SEPARATOR = ".";

	private final String presetFolder;
	private final String presetShortName;
	private final int presetMajorVersion;
	private final String presetRegistryName;
	private final String biomeName;
	private final String resourceName;

	public OTGBiomeResourceLocation(Path presetFolder, String presetShortName, int presetMajorVersion, String biomeName, String resourceName)
	{
		this.presetMajorVersion = presetMajorVersion;
		this.presetFolder = presetFolder.toFile().getName();
		this.presetShortName = presetShortName != null && presetShortName.trim().length() > 0 ? presetShortName : this.presetFolder;
		this.presetRegistryName = this.presetShortName.toLowerCase().trim().replaceAll("[^a-z0-9/_-]", "_");		this.biomeName = biomeName.toLowerCase().trim().replaceAll("[^a-z0-9/_-]", "_");
		this.resourceName = resourceName;
	}

	public OTGBiomeResourceLocation(Path presetFolder, String presetShortName, int presetMajorVersion, String biomeName)
	{
		this(presetFolder, presetShortName, presetMajorVersion, biomeName, null);
	}
	
	public IBiomeResourceLocation withBiomeResource(String resourceName)
	{
		return new OTGBiomeResourceLocation(this.presetFolder, this.presetShortName, this.presetMajorVersion, this.presetRegistryName, this.biomeName, resourceName);
	}
	
	private OTGBiomeResourceLocation(String presetFolderName, String presetShortName, int presetMajorVersion, String presetRegistryName, String biomeName, String resourceName)
	{
		this.presetMajorVersion = presetMajorVersion;
		this.presetFolder = presetFolderName;
		this.presetShortName = presetShortName;		
		this.presetRegistryName = presetRegistryName;
		this.biomeName = biomeName;
		this.resourceName = resourceName;
	}
	
	@Override
	public String getPresetFolderName()
	{
		return this.presetFolder;
	}
	
	@Override
	public String toResourceLocationString()
	{
		return String.format("%s%s%s", getResourceDomain(), ":", getResourcePath());
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
	
	@Override
	public boolean equals(Object other)
	{
		if (this == other)
		{
			return true;
		}
		if (!(other instanceof OTGBiomeResourceLocation))
		{
			return false;
		}
		return ((OTGBiomeResourceLocation)other).toResourceLocationString().equals(this.toResourceLocationString());
	}

	@Override
	public int hashCode()
	{
		return toResourceLocationString().hashCode();
	}	
}
