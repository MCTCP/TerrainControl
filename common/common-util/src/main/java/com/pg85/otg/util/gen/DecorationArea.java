package com.pg85.otg.util.gen;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.util.ChunkCoordinate;

public class DecorationArea
{
	// Anything that uses decoration bounds should be using the methods in this class.
	// TODO: Refactor this, change the decorated area from 2x2 to 3x3 chunks and 
	// remove the +8 decoration offset used for resources. BO/Carver offsets are also
	// in here for clarity for the moment, should clean that up when resources are 
	// properly re-aligned.

	public static final int DECORATION_OFFSET = 8;
	public static final int CARVER_OFFSET = 8;
	public static final int BO_CHUNK_CENTER_X = 8;
	public static final int BO_CHUNK_CENTER_Z = 7;

	private static final int WIDTH_IN_CHUNKS = 2;
	private static final int HEIGHT_IN_CHUNKS = 2;
	public static final int WIDTH = WIDTH_IN_CHUNKS * Constants.CHUNK_SIZE;
	public static final int HEIGHT = HEIGHT_IN_CHUNKS * Constants.CHUNK_SIZE;
	
	private final int minX;
	private final int maxX;
	private final int minZ;
	private final int maxZ;
	private final int width;
	private final int height;
	private final ChunkCoordinate chunkBeingDecorated;

	public DecorationArea(ChunkCoordinate chunkBeingDecorated)
	{
		int top = 0;
		int right = Constants.CHUNK_SIZE;
		int bottom = Constants.CHUNK_SIZE;
		int left = 0;
		this.width = left + right + Constants.CHUNK_SIZE;
		this.height = top + bottom + Constants.CHUNK_SIZE;
		this.minX = chunkBeingDecorated.getBlockX() - left;
		this.maxX = chunkBeingDecorated.getBlockX() + Constants.CHUNK_SIZE + right;
		this.minZ = chunkBeingDecorated.getBlockZ() - top;
		this.maxZ = chunkBeingDecorated.getBlockZ() + Constants.CHUNK_SIZE + bottom;
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
	
	public ChunkCoordinate getChunkBeingDecorated()
	{
		return this.chunkBeingDecorated;
	}	
	
	public int getChunkBeingDecoratedCenterX()
	{
		return this.chunkBeingDecorated.getChunkX() * Constants.CHUNK_SIZE + DECORATION_OFFSET;
	}
	
	public int getChunkBeingDecoratedCenterZ()
	{
		return this.chunkBeingDecorated.getChunkZ() * Constants.CHUNK_SIZE + DECORATION_OFFSET;
	}	
}
