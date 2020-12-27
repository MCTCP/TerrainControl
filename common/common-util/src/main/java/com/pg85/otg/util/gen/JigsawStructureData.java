package com.pg85.otg.util.gen;

/**
 * Helper class to hold jigsaw structure data for density calculations.
 */
public class JigsawStructureData
{
	public final int minX;
	public final int minY;
	public final int minZ;
	public final int maxX;
	public final int delta;
	public final int maxZ;
	public final boolean useDelta;
	public final int sourceX;
	public final int groundY;
	public final int sourceZ;

	public JigsawStructureData(int minX, int minY, int minZ, int maxX, int delta, int maxZ, boolean useDelta, int sourceX, int groundY, int sourceZ) {
		this.minX = minX;
		this.minY = minY;
		this.minZ = minZ;
		this.maxX = maxX;
		this.delta = delta;
		this.maxZ = maxZ;
		this.useDelta = useDelta;
		this.sourceX = sourceX;
		this.groundY = groundY;
		this.sourceZ = sourceZ;
	}
}
