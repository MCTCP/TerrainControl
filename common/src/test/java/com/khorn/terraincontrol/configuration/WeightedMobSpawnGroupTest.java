package com.khorn.terraincontrol.configuration;

import static com.khorn.terraincontrol.configuration.WeightedMobSpawnGroup.fromJson;
import static com.khorn.terraincontrol.configuration.WeightedMobSpawnGroup.toJson;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.khorn.terraincontrol.exception.InvalidConfigException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import java.util.List;

@RunWith(JUnit4.class)
public class WeightedMobSpawnGroupTest
{

    @Test
    public void testSerialization() throws InvalidConfigException
    {
        WeightedMobSpawnGroup creeperGroup = new WeightedMobSpawnGroup("Creeper", 10, 3, 5);
        WeightedMobSpawnGroup sheepGroup = new WeightedMobSpawnGroup("Sheep", 5, 1, 3);
        List<WeightedMobSpawnGroup> groups = Arrays.asList(creeperGroup, sheepGroup);
        assertEquals(groups, fromJson(toJson(groups)));
    }

    @Test
    public void testValues()
    {
        WeightedMobSpawnGroup creeperGroup = new WeightedMobSpawnGroup("Creeper", 10, 3, 5);

        assertEquals("Creeper", creeperGroup.mob);
        assertEquals(10, creeperGroup.weight);
        assertEquals(3, creeperGroup.min);
        assertEquals(5, creeperGroup.max);

        assertEquals(creeperGroup.mob, creeperGroup.getConfigName());
        assertEquals(creeperGroup.weight, creeperGroup.getWeight());
        assertEquals(creeperGroup.min, creeperGroup.getMin());
        assertEquals(creeperGroup.max, creeperGroup.getMax());
    }

    @Test
    public void testMalformedJSON() throws InvalidConfigException
    {
        String[] tests = {
                "[{\"mob\": \"Creeper\": \"Invalid\", \"min\": 3, \"max\": 4, \"weight\": 10}]",
                // Missing value after mob
                "[{\"mob\":, \"min\": 3, \"max\": 4, \"weight\": 10}]",
                // Missing colon and value after mob
                "[{\"mob\", \"min\": 3, \"max\": 4, \"weight\": 10}]",
                // Missing [ and ]
                "{\"mob\", \"min\": 3, \"max\": 4, \"weight\": 10}"
        };

        for (String test : tests)
        {
            try
            {
                fromJson(test);
                fail("String was invalid, so fromJson should have thrown an exception");
            } catch (InvalidConfigException expected)
            {
            }
        }
    }
}
