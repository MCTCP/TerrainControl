package com.pg85.otg.util.gen;

import com.pg85.otg.util.ChunkCoordinate;

public class DecorationArea
{
	private final int minX;
	private final int maxX;
	private final int minZ;
	private final int maxZ;
	private final int width;
	private final int height;
	private final ChunkCoordinate chunkBeingDecorated;

	public DecorationArea(int top, int right, int bottom, int left, ChunkCoordinate chunkBeingDecorated)
	{
		this.width = left + right + ChunkCoordinate.CHUNK_SIZE;
		this.height = top + bottom + ChunkCoordinate.CHUNK_SIZE;
		this.minX = chunkBeingDecorated.getBlockX() - left;
		this.maxX = chunkBeingDecorated.getBlockX() + ChunkCoordinate.CHUNK_SIZE + right;
		this.minZ = chunkBeingDecorated.getBlockZ() - top;
		this.maxZ = chunkBeingDecorated.getBlockZ() + ChunkCoordinate.CHUNK_SIZE + bottom;
		this.chunkBeingDecorated = chunkBeingDecorated;
	}

	public boolean isInAreaBeingDecorated(int blockX, int blockZ)
	{
		return 
			blockX >= this.minX &&
			blockX < this.maxX &&
			blockZ >= this.minZ &&
			blockZ < this.maxZ
		;
	}

	public ChunkCoordinate getChunkBeingDecorated()
	{
		return this.chunkBeingDecorated;
	}

	public int getWidth()
	{
		return this.width;
	}

	public int getHeight()
	{
		return this.height;
	}

	public int getTop()
	{
		return this.minZ;
	}
	
	public int getLeft()
	{
		return this.minX;
	}
}
