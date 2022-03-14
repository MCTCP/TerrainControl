package com.pg85.otg.customobject.structures.bo4.smoothing;

import com.pg85.otg.customobject.structures.bo4.BO4CustomStructureCoordinate;

class BlockCoordsAndNeighbours
{
	public BO4CustomStructureCoordinate bO3;
	public int blockX;
	public short blockY;
	public int blockZ;
	public boolean smoothInDirection1;
	public boolean smoothInDirection2;
	public boolean smoothInDirection3;
	public boolean smoothInDirection4;
	
	public BlockCoordsAndNeighbours(BO4CustomStructureCoordinate bO3, int blockX, short blockY, int blockZ, boolean smoothInDirection1, boolean smoothInDirection2, boolean smoothInDirection3, boolean smoothInDirection4)
	{
		this.bO3 = bO3;
		this.blockX = blockX;
		this.blockY = blockY;
		this.blockZ = blockZ;
		this.smoothInDirection1 = smoothInDirection1;
		this.smoothInDirection2 = smoothInDirection2;
		this.smoothInDirection3 = smoothInDirection3;
		this.smoothInDirection4 = smoothInDirection4;
	}
}