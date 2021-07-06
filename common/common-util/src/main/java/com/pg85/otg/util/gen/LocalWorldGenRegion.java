package com.pg85.otg.util.gen;

import com.pg85.otg.interfaces.ICachedBiomeProvider;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IPluginConfig;
import com.pg85.otg.interfaces.IWorldConfig;
import com.pg85.otg.interfaces.IWorldGenRegion;
import com.pg85.otg.util.ChunkCoordinate;

public abstract class LocalWorldGenRegion implements IWorldGenRegion
{
	protected final String presetFolderName;
	private final IPluginConfig pluginConfig;
	private final IWorldConfig worldConfig;
	protected final ILogger logger;
	protected final DecorationBiomeCache decorationBiomeCache;
	protected final DecorationArea decorationArea;

	/** Creates a LocalWorldGenRegion to be used during chunk decoration */
	protected LocalWorldGenRegion(String presetFolderName, IPluginConfig pluginConfig, IWorldConfig worldConfig, ILogger logger, int worldRegionCenterX, int worldRegionCenterZ, ICachedBiomeProvider cachedBiomeProvider)
	{
		this.presetFolderName = presetFolderName;
		this.pluginConfig = pluginConfig;
		this.worldConfig = worldConfig;
		this.logger = logger;
		this.decorationArea = new DecorationArea(ChunkCoordinate.fromChunkCoords(worldRegionCenterX, worldRegionCenterZ));
		this.decorationBiomeCache = new DecorationBiomeCache(worldRegionCenterX, worldRegionCenterZ, cachedBiomeProvider);		
	}
	
	/** Creates a LocalWorldGenRegion to be used outside of world generation. */	
	protected LocalWorldGenRegion(String presetFolderName, IPluginConfig pluginConfig, IWorldConfig worldConfig, ILogger logger)
	{
		this.presetFolderName = presetFolderName;
		this.pluginConfig = pluginConfig;
		this.worldConfig = worldConfig;
		this.logger = logger;
		this.decorationBiomeCache = null;
		this.decorationArea = null;
	}
	
	@Override
	public IPluginConfig getPluginConfig()
	{
		return this.pluginConfig;
	}	
	
	@Override
	public IWorldConfig getWorldConfig()
	{
		return this.worldConfig;
	}
	
	@Override
	public String getPresetFolderName()
	{
		return this.presetFolderName;
	}
	
	@Override
	public DecorationArea getDecorationArea()
	{
		return this.decorationArea;
	}
}
