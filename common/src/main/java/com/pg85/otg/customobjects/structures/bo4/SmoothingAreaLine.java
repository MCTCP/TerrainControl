package com.pg85.otg.customobjects.structures.bo4;

public class SmoothingAreaLine
{
	public int beginPointX;
	public short beginPointY;
	public int beginPointZ;

	public int endPointX;
	public short endPointY;
	public int endPointZ;

	public int originPointX;
	public short originPointY;
	public int originPointZ;

	public int finalDestinationPointX;
    public short finalDestinationPointY;
    public int finalDestinationPointZ;
    
    public SmoothingAreaLine() {}
    
    public SmoothingAreaLine(int beginPointX, short beginPointY, int beginPointZ, int endPointX, short endPointY, int endPointZ, int originPointX, short originPointY, int originPointZ, int finalDestinationPointX, short finalDestinationPointY, int finalDestinationPointZ)
    {
    	this.beginPointX = beginPointX;
    	this.beginPointY = beginPointY;
    	this.beginPointZ = beginPointZ;

    	this.endPointX = endPointX;
    	this.endPointY = endPointY;
    	this.endPointZ = endPointZ;

    	this.originPointX = originPointX;
    	this.originPointY = originPointY;
    	this.originPointZ = originPointZ;

    	this.finalDestinationPointX = finalDestinationPointX;
    	this.finalDestinationPointY = finalDestinationPointY;
    	this.finalDestinationPointZ = finalDestinationPointZ;
    }
}