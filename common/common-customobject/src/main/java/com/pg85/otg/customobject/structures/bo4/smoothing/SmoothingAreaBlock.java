package com.pg85.otg.customobject.structures.bo4.smoothing;

class SmoothingAreaBlock
{
	enum enumSmoothingBlockType
	{
		FILLING,
		CUTTING
	}

	int x = 0;
    short y = -1;
    int z = 0;
	enumSmoothingBlockType smoothingBlockType = null;

    public SmoothingAreaBlock() { }
    
    public SmoothingAreaBlock(int x, short y, int z)
    {
    	this.x = x;
    	this.y = y;
    	this.z = z;
    }    
    
    public SmoothingAreaBlock(int x, short y, int z, enumSmoothingBlockType smoothingBlockType)
    {
    	this.x = x;
    	this.y = y;
    	this.z = z;
    	this.smoothingBlockType = smoothingBlockType;
    }
}
