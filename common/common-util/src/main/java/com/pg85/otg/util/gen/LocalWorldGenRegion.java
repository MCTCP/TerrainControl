package com.pg85.otg.util.gen;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.interfaces.ILogger;
import com.pg85.otg.util.interfaces.IPluginConfig;
import com.pg85.otg.util.interfaces.IWorldConfig;
import com.pg85.otg.util.interfaces.IWorldGenRegion;

public abstract class LocalWorldGenRegion implements IWorldGenRegion
{
	protected final String presetFolderName;
	private final IPluginConfig pluginConfig;
	private final IWorldConfig worldConfig;
	protected final ILogger logger;
	protected final DecorationBiomeCache decorationBiomeCache;
	protected final DecorationArea decorationArea;

	/** Creates a LocalWorldGenRegion to be used during chunk decoration */
	protected LocalWorldGenRegion(String presetFolderName, IPluginConfig pluginConfig, IWorldConfig worldConfig, ILogger logger, int worldRegionCenterX, int worldRegionCenterZ)
	{
		this.presetFolderName = presetFolderName;
		this.pluginConfig = pluginConfig;
		this.worldConfig = worldConfig;
		this.logger = logger;
		this.decorationBiomeCache = new DecorationBiomeCache(Constants.CHUNK_SIZE * 2, Constants.CHUNK_SIZE * 2, worldRegionCenterX, worldRegionCenterZ);
		//this.biomeCache = new DecorationBiomeCache(ChunkCoordinate.CHUNK_SIZE * 3, ChunkCoordinate.CHUNK_SIZE * 3, worldRegionCenterX - ChunkCoordinate.CHUNK_SIZE, worldRegionCenterZ - ChunkCoordinate.CHUNK_SIZE);
		this.decorationArea = new DecorationArea(0, Constants.CHUNK_SIZE, Constants.CHUNK_SIZE, 0, ChunkCoordinate.fromChunkCoords(worldRegionCenterX, worldRegionCenterZ));
		//this.decorationArea = new DecorationArea(ChunkCoordinate.CHUNK_SIZE, ChunkCoordinate.CHUNK_SIZE, ChunkCoordinate.CHUNK_SIZE, ChunkCoordinate.CHUNK_SIZE, ChunkCoordinate.fromChunkCoords(worldRegionCenterX, worldRegionCenterZ));
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
