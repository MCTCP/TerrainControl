package com.khorn.terraincontrol.util.helpers;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class StringHelperTest
{

    @Test
    public void testReadCommaSeperatedString()
    {
        assertArrayEquals(StringHelper.readCommaSeperatedString(""), new String[] {""});
        assertArrayEquals(StringHelper.readCommaSeperatedString("a"), new String[] {"a"});
        assertArrayEquals(StringHelper.readCommaSeperatedString("a,b,cd"), new String[] {"a", "b", "cd"});
        assertArrayEquals(StringHelper.readCommaSeperatedString("a,b(c,d),e"), new String[] {"a", "b(c,d)", "e"});
    }

    @Test
    public void testSpecifiesBlockData()
    {
        assertFalse(StringHelper.specifiesBlockData("wool"));
        assertTrue(StringHelper.specifiesBlockData("wool:0"));
        assertTrue(StringHelper.specifiesBlockData("wool:1"));
        assertFalse(StringHelper.specifiesBlockData("minecraft:wool"));
        assertTrue(StringHelper.specifiesBlockData("minecraft:wool:0"));
        assertTrue(StringHelper.specifiesBlockData("minecraft:wool:1"));
    }

}
