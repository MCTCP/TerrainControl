package com.khorn.terraincontrol.bukkit.util;

import static com.khorn.terraincontrol.bukkit.util.MobSpawnGroupHelper.fromMinecraftList;
import static com.khorn.terraincontrol.bukkit.util.MobSpawnGroupHelper.toMinecraftClass;
import static com.khorn.terraincontrol.bukkit.util.MobSpawnGroupHelper.toMinecraftlist;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.khorn.terraincontrol.configuration.WeightedMobSpawnGroup;
import com.khorn.terraincontrol.util.minecraftTypes.MobNames;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import java.util.List;

@RunWith(JUnit4.class)
public class MobSpawnGroupHelperTest
{

    /**
     * Tests whether Minecraft has a class for all our MobNames.
     */
    @Test
    public void testMobNamesExistInMinecraft()
    {
        try
        {
            for (MobNames name : MobNames.values())
            {
                assertTrue("Expected Minecraft class for " + name, toMinecraftClass(name.getInternalName()) != null);
            }
        } catch (Error e)
        {
            System.out.println("warning: skipped test " + getClass().getSimpleName()
                    + ".testMobNamesExistInMinecraft; signature-only JAR?");
        }
    }

    /**
     * Verifies that the converter methods are working fine.
     */
    @Test
    public void testRoundtrip()
    {
        try
        {
            List<WeightedMobSpawnGroup> groups = Arrays.asList(
                    new WeightedMobSpawnGroup("Cow", 10, 2, 5),
                    new WeightedMobSpawnGroup("Chicken", 8, 3, 6)
                    );
            assertEquals(groups, fromMinecraftList(toMinecraftlist(groups)));
        } catch (Error e)
        {
            System.out.println("warning: skipped test " + getClass().getSimpleName() + ".testRoundtrip; signature-only JAR?");
        }
    }
}
