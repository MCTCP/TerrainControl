package com.pg85.otg.util;

public enum OTGDirection
{
	DOWN(0, -1, 0),
	UP(0, 1, 0),
	NORTH(0, 0, -1),
	SOUTH(0, 0, 1),
	WEST(-1, 0, 0),
	EAST(1, 0, 0);

	private final int dx;
	private final int dy;
	private final int dz;

	OTGDirection(int dx, int dy, int dz) {
		this.dx = dx;
		this.dy = dy;
		this.dz = dz;
	}

	public int getX()
	{
		return dx;
	}

	public int getY()
	{
		return dy;
	}

	public int getZ()
	{
		return dz;
	}

	public OTGDirection getClockWise() {
		return switch (this)
				{
					case NORTH -> EAST;
					case SOUTH -> WEST;
					case WEST -> NORTH;
					case EAST -> SOUTH;
					default -> throw new IllegalStateException("Unable to get CW direction of " + this);
				};
	}

	public OTGDirection getCounterClockWise() {
		return switch (this)
				{
					case NORTH -> WEST;
					case SOUTH -> EAST;
					case WEST -> SOUTH;
					case EAST -> NORTH;
					default -> throw new IllegalStateException("Unable to get CCW direction of " + this);
				};
	}
}
