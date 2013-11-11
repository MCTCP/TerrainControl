package com.khorn.terraincontrol.util.helpers;

import java.util.Random;

/**
 * Class to get a random generator which is constant for the given input.
 *
 */
public class RandomHelper
{
    /**
     * Gets a Random generator with a random seed. However, the same input
     * will always produce the same output.
     * 
     * @param x    X-coord to start with.
     * @param z    Z-coord to start with.
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
    
    /**
     * Gets a Random generator with a random seed. However, the same input
     * will always produce the same output.
     * 
     * @param x    X-coord to start with.
     * @param y    Y-coord to start with.
     * @param z    Z-coord to start with.
     * @param seed Seed to start with.
     * @return A random generator with a random seed.
     */
    public static Random getRandomForCoords(int x, int y, int z, long seed)
    {
        Random random = getRandomForCoords(x, z, seed);
        random.setSeed(random.nextInt() * y);
        return random;
    }

    private RandomHelper()
    {
    }
}
