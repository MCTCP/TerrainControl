package com.pg85.otg.util;

public class BlockPos3D
{
	final int x;
	final int y;
	final int z;
	
	public BlockPos3D(int x, int y, int z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public boolean equals(Object other)
	{
		if(this == other)
		{
			return true;
		}
		if(other instanceof BlockPos3D)
		{
			if(((BlockPos3D)other).x == this.x && ((BlockPos3D)other).z == this.z)
			{
				return true;
			}
		}
		return false;
	}
}