package com.pg85.otg.util.biome;

import com.pg85.otg.interfaces.IBiomeResourceLocation;

public class MCBiomeResourceLocation implements IBiomeResourceLocation
{	
	private final String domain;
	private final String path;
	private final String presetFolder;
	
	public MCBiomeResourceLocation(String domain, String path, String presetFolderName)
	{
		this.domain = domain;
		this.path = path;
		this.presetFolder = presetFolderName;
	}

	@Override
	public String getPresetFolderName()
	{
		return this.presetFolder;
	}
	
	@Override
	public String toResourceLocationString()
	{
		return String.format("%s%s%s", this.domain, ":", this.path);
	}
	
	@Override
	public boolean equals(Object other)
	{
		if (this == other)
		{
			return true;
		}
		if (!(other instanceof MCBiomeResourceLocation))
		{
			return false;
		}
		return ((MCBiomeResourceLocation)other).toResourceLocationString().equals(this.toResourceLocationString());
	}
	
	@Override
	public int hashCode()
	{
		return toResourceLocationString().hashCode();
	}
}
