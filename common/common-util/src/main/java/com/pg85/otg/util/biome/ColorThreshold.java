package com.pg85.otg.util.biome;


public class ColorThreshold implements Comparable<ColorThreshold>
{
	final float maxNoise;
	final int color;

	public ColorThreshold(int color, float maxNoise) {
		this.color = color;
		this.maxNoise = maxNoise;
	}

	public float getMaxNoise()
	{
		return maxNoise;
	}

	public int getColor()
	{
		return color;
	}

	@Override
	public int compareTo(ColorThreshold that)
	{
		float delta = this.maxNoise - that.maxNoise;
		// The number 65565 is just randomly chosen, any positive number
		// works fine as long as it can represent the floating point delta
		// as an integer
		return (int) (delta * 65565);
	}
}
