package com.pg85.otg.util.helpers;

import java.util.Random;

/**
 * Class to get a random generator which is constant for the given input.
 *
 */
public class RandomHelper
{
	private RandomHelper()
	{
	}
	
	/**
	 * Gets a Random generator with a random seed. However, the same input
	 * will always produce the same output.
	 *
	 * @param x	X-coord to start with.
	 * @param z	Z-coord to start with.
	 * @param seed Seed to start with.
	 * @return A random generator with a random seed.
	 */
	public static Random getRandomForCoords(int x, int z, long seed)
	{
		Random random = new Random();
		random.setSeed(seed);
		long l1 = random.nextLong() + 1L;
		long l2 = random.nextLong() + 1L;
		random.setSeed(x * l1 + z * l2 ^ seed);
		return random;
	}

	public static Random getRandomForCoords(int x, int y, int z, long seed)
	{
		Random random = new Random();
		random.setSeed(seed);
		long l1 = random.nextLong() + 1L;
		long l2 = random.nextLong() + 1L;
		long l3 = random.nextLong() + 1L;
		random.setSeed(x * l1 + y * l2 + z * l3 ^ seed);
		return random;
	}

	/**
	 * Returns a random number between min and max, inclusive.
	 *
	 * @param random The random number generator.
	 * @param min The minimum value.
	 * @param max The maximum value.
	 * @return A random number between min and max, inclusive.
	 */
	public static int numberInRange(Random random, int min, int max)
	{
		return min + random.nextInt(max - min + 1);
	}
}
