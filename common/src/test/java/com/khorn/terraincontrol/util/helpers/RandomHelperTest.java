package com.khorn.terraincontrol.util.helpers;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.Random;

public class RandomHelperTest
{

    @Test
    public void testConsistency()
    {
        // Random number generator with a known seed, so results should always
        // be the same (required for terrain generation to be reproducible)
        Random random = new Random(15);

        assertEquals(6, RandomHelper.numberInRange(random, 0, 10));
        assertEquals(0, RandomHelper.numberInRange(random, 0, 10));
        assertEquals(0, RandomHelper.numberInRange(random, 0, 10));
        assertEquals(7, RandomHelper.numberInRange(random, 0, 10));
    }

    @Test
    public void testZeroRange()
    {
        assertEquals(3, RandomHelper.numberInRange(new Random(), 3, 3));
    }
}
