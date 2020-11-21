package com.pg85.otg.util.gen;

import com.pg85.otg.util.interfaces.IWorldConfig;
import com.pg85.otg.util.interfaces.IWorldGenRegion;

public abstract class LocalWorldGenRegion implements IWorldGenRegion
{
	private IWorldConfig worldConfig;
	
	protected LocalWorldGenRegion(IWorldConfig worldConfig)
	{
		this.worldConfig = worldConfig;
	}
	
	public IWorldConfig getWorldConfig()
	{
		return this.worldConfig;
	}
}
