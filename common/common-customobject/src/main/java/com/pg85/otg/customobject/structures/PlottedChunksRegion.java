package com.pg85.otg.customobject.structures;

import java.util.Arrays;

import com.pg85.otg.constants.Constants;

public class PlottedChunksRegion
{
	private boolean requiresSave = false;
	private boolean[][] plottedChunks = new boolean[Constants.REGION_SIZE][Constants.REGION_SIZE];

	public PlottedChunksRegion() { }

	PlottedChunksRegion(boolean[][] plottedChunks)
	{
		this.plottedChunks = plottedChunks;
	}

	boolean requiresSave()
	{
		return this.requiresSave;
	}

	void markSaved()
	{
		this.requiresSave = false;
	}

	public boolean getChunk(int internalX, int internalZ)
	{
		return this.plottedChunks[internalX][internalZ];
	}

	public void setChunk(int internalX, int internalZ)
	{
		this.plottedChunks[internalX][internalZ] = true;
		this.requiresSave = true;
	}

	public boolean[][] getArray()
	{
		return this.plottedChunks;
	}

	static PlottedChunksRegion getFilledRegion()
	{
		boolean[][] plottedChunks = new boolean[Constants.REGION_SIZE][Constants.REGION_SIZE];
		for(int i = 0; i < Constants.REGION_SIZE; i++)
		{
			Arrays.fill(plottedChunks[i], true);
		}
		return new PlottedChunksRegion(plottedChunks);
	}
}
