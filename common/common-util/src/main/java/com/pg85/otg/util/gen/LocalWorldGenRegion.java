package com.pg85.otg.util.gen;

import com.pg85.otg.util.interfaces.IWorldConfig;
import com.pg85.otg.util.interfaces.IWorldGenRegion;

public abstract class LocalWorldGenRegion implements IWorldGenRegion
{
	protected final String presetName;
	private final IWorldConfig worldConfig;
	
	protected LocalWorldGenRegion(String presetName, IWorldConfig worldConfig)
	{
		this.presetName = presetName;
		this.worldConfig = worldConfig;
	}
	
	public IWorldConfig getWorldConfig()
	{
		return this.worldConfig;
	}
}
