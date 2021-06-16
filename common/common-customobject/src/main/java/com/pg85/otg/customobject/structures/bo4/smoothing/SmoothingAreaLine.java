package com.pg85.otg.customobject.structures.bo4.smoothing;

public class SmoothingAreaLine
{
	public int beginPointX;
	public short beginPointY = -1;
	public int beginPointZ;

	public int endPointX;
	public short endPointY = -1;
	public int endPointZ;

	public int originPointX;
	public short originPointY = -1;
	public int originPointZ;

	public int finalDestinationPointX;
	public short finalDestinationPointY = -1;
	public int finalDestinationPointZ;
	
	public SmoothingAreaLine() {}
	
	public SmoothingAreaLine(int beginPointX, short beginPointY, int beginPointZ, int endPointX, short endPointY, int endPointZ, int originPointX, short originPointY, int originPointZ, int finalDestinationPointX, short finalDestinationPointY, int finalDestinationPointZ)
	{
		this(beginPointX, beginPointZ, endPointX, endPointZ, originPointX, originPointY, originPointZ, finalDestinationPointX, finalDestinationPointZ);
		this.beginPointY = beginPointY;
		this.endPointY = endPointY;
		this.finalDestinationPointY = finalDestinationPointY;
	}
	
	SmoothingAreaLine(int beginPointX, int beginPointZ, int endPointX, int endPointZ, int originPointX, short originPointY, int originPointZ, int finalDestinationPointX, int finalDestinationPointZ)
	{
		this.beginPointX = beginPointX;
		this.beginPointZ = beginPointZ;

		this.endPointX = endPointX;
		this.endPointZ = endPointZ;

		this.originPointX = originPointX;
		this.originPointY = originPointY;
		this.originPointZ = originPointZ;

		this.finalDestinationPointX = finalDestinationPointX;
		this.finalDestinationPointZ = finalDestinationPointZ;
	}
}