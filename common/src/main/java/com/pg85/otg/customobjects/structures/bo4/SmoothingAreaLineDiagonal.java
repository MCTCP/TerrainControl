package com.pg85.otg.customobjects.structures.bo4;

// if this line is a child line of a diagonal line
public class SmoothingAreaLineDiagonal extends SmoothingAreaLine
{       
	public int diagonalLineOriginPointX; // 12;
	public short diagonalLineoriginPointY; // 13;
	public int diagonalLineOriginPointZ; // 14;
	public int diagonalLineFinalDestinationPointX; // 15;
	public short diagonalLineFinalDestinationPointY; // 16;
	public int diagonalLineFinalDestinationPointZ; // 17;
	
	public SmoothingAreaLineDiagonal() { }
	
	public SmoothingAreaLineDiagonal(int beginPointX, short beginPointY, int beginPointZ, int endPointX, short endPointY, int endPointZ, int originPointX, short originPointY, int originPointZ, int finalDestinationPointX, short finalDestinationPointY, int finalDestinationPointZ, int diagonalLineOriginPointX, short diagonalLineOriginPointY, int diagonalLineOriginPointZ, int diagonalLineFinalDestinationPointX, short diagonalLineFinalDestinationPointY, int diagonalLineFinalDestinationPointZ)
	{
		super(beginPointX, beginPointY, beginPointZ, endPointX, endPointY, endPointZ, originPointX, originPointY, originPointZ, finalDestinationPointX, finalDestinationPointY, finalDestinationPointZ);
		this.diagonalLineOriginPointX = diagonalLineOriginPointX;
		this.diagonalLineoriginPointY = diagonalLineOriginPointY;
		this.diagonalLineOriginPointZ = diagonalLineOriginPointZ;
		this.diagonalLineFinalDestinationPointX = diagonalLineFinalDestinationPointX;
		this.diagonalLineFinalDestinationPointY = diagonalLineFinalDestinationPointY;
		this.diagonalLineFinalDestinationPointZ = diagonalLineFinalDestinationPointZ;
	}
}