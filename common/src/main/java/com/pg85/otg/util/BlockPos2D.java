package com.pg85.otg.util;

public class BlockPos2D
{
	int x;
	int z;
	
	public BlockPos2D(int x, int z)
	{
		this.x = x;
		this.z = z;
	}
	
	public boolean equals(Object other)
	{
		if(this == other)
		{
			return true;
		}
		if(other instanceof BlockPos2D)
		{
			if(((BlockPos2D)other).x == this.x && ((BlockPos2D)other).z == this.z)
			{
				return true;
			}
		}
		return false;
	}
}