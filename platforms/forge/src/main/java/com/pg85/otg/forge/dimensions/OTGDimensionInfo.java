package com.pg85.otg.forge.dimensions;

import java.util.HashMap;

public class OTGDimensionInfo
{
	// Used to recreate dimensions in the correct order
	public int highestOrder;
	public HashMap<Integer, DimensionData> orderedDimensions;
	
	public OTGDimensionInfo(int highestOrder, HashMap<Integer, DimensionData> orderedDimensions)
	{
		this.highestOrder = highestOrder;
		this.orderedDimensions = orderedDimensions;
	}
}
