package com.pg85.otg.util.gen;

import com.pg85.otg.util.interfaces.IWorldConfig;
import com.pg85.otg.util.interfaces.IWorldGenRegion;

public abstract class LocalWorldGenRegion implements IWorldGenRegion
{
	protected final String presetFolderName;
	private final IWorldConfig worldConfig;
	
	protected LocalWorldGenRegion(String presetFolderName, IWorldConfig worldConfig)
	{
		this.presetFolderName = presetFolderName;
		this.worldConfig = worldConfig;
	}
	
	public IWorldConfig getWorldConfig()
	{
		return this.worldConfig;
	}
	
	public String getPresetFolderName()
	{
		return this.presetFolderName;
	}
}
