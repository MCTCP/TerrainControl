package com.khorn.terraincontrol.forge;

import java.util.HashMap;

public class OTGDimensionInfo
{
	public int highestOrder;
	public HashMap<Integer, DimensionData> orderedDimensions;
	
	public OTGDimensionInfo(int highestOrder, HashMap<Integer, DimensionData> orderedDimensions)
	{
		this.highestOrder = highestOrder;
		this.orderedDimensions = orderedDimensions;
	}
}
