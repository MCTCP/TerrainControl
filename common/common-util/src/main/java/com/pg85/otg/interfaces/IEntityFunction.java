package com.pg85.otg.interfaces;

import com.pg85.otg.util.bo3.NamedBinaryTag;

public interface IEntityFunction
{
	public double getX();
	public int getY();
	public double getZ();
	public int getGroupSize();
	public String getNameTagOrNBTFileName();
	public String getResourceLocation();
	public String getMetaData();
	NamedBinaryTag getNBTTag();
	public String makeString();
}
