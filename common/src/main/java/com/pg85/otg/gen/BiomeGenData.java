package com.pg85.otg.gen;

/**
 * Class to hold biome generation data until biome configs are working. This class will eventually be removed.
 */
public class BiomeGenData
{
	public static final BiomeGenData[] LOOKUP = new BiomeGenData[256];

	public int color = 0xFFFFFF;

	public int smoothRadius = 2;

	// depth
	public float biomeHeight = 0.5f;
	// scale
	public float biomeVolatility = 0.25f;

	public double volatility1 = 1;

	public double volatility2 = 1;
	public double horizontalFracture = 1;
	public double verticalFracture = 1;

	public double volatilityWeight1 = 0.5;

	public double volatilityWeight2 = 0.4583;

	public double maxAverageHeight = 0;
	public double maxAverageDepth = 0;

	public double[] chc = new double[33];

	public double getVolatilityWeight1()
	{
		return (volatilityWeight1 - 0.5) * 24.0;
	}

	public double getVolatilityWeight2()
	{
		return (0.5 - volatilityWeight2) * 24.0;
	}

	public static final BiomeGenData INSTANCE = new BiomeGenData();
	public static final BiomeGenData OCEAN = ocean();
	public static final BiomeGenData PLAINS = plains();
	public static final BiomeGenData FOREST = forest();
	public static final BiomeGenData DESERT = desert();

	private static BiomeGenData ocean()
	{
		BiomeGenData data = new BiomeGenData();
		data.biomeHeight = -0.5f;
		data.biomeVolatility = 0.1f;
		data.horizontalFracture = 0.5;
		data.verticalFracture = 0.5;
		data.smoothRadius = 12;
		data.color = 0x0f4db8;
		LOOKUP[0] = data;

		return data;
	}

	private static BiomeGenData plains()
	{
		BiomeGenData data = new BiomeGenData();
		data.biomeHeight = 0.2f;
		data.biomeVolatility = 0.125f;
		data.horizontalFracture = 0.5;
		data.verticalFracture = 0.5;
		data.smoothRadius = 12;
		data.color = 0x9ec949;
		LOOKUP[1] = data;

		return data;
	}

	private static BiomeGenData forest()
	{
		BiomeGenData data = new BiomeGenData();
		data.biomeHeight = 1.2f;
		data.biomeVolatility = 0.45f;
		data.horizontalFracture = 0.5;
		data.verticalFracture = 0.5;
		data.volatility1 = 2.25;
		data.volatility2 = 2.25;
		data.smoothRadius = 12;
		data.color = 0x37bd58;
		LOOKUP[2] = data;
		
		return data;
	}


	private static BiomeGenData desert()
	{
		BiomeGenData data = new BiomeGenData();
		data.biomeHeight = 0.8f;
		data.biomeVolatility = 0.25f;
		data.horizontalFracture = 0.5;
		data.verticalFracture = 0.5;
		data.volatility1 = 1.5;
		data.volatility2 = 1.5;
		data.smoothRadius = 12;
		data.color = 0xc8d166;
		LOOKUP[3] = data;

		return data;
	}
}
