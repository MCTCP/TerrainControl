package com.khorn.terraincontrol.util.minecraftTypes;

import org.bukkit.entity.EntityType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class MobNamesTest
{
    /**
     * Tests if we have an entry for every Bukkit entry.
     */
    @Test
    public void testBukkitTypesExistInMobNames()
    {
        for (EntityType type : EntityType.values())
        {
            if (type.isAlive() && type.isSpawnable() && type != EntityType.ARMOR_STAND)
            {
                // Throws IllegalArgumentException if no entry exists
                MobNames.valueOf(type.toString());
            }
        }
    }

    /**
     * Tests if Bukkit has an entry for all our entries.
     */
    @Test
    public void testMobNamesExistInBukkit()
    {
        // Check if all our entries exist in Bukkit
        for (MobNames name : MobNames.values())
        {
            // Also throws IllegalArgumentException if no entry exists
            EntityType.valueOf(name.toString());
        }
    }

}
