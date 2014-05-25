package com.khorn.terraincontrol.util.helpers;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class StringHelperTest
{

    @Test
    public void testReadCommaSeperatedString()
    {
        // Test empty string
        assertArrayEquals(StringHelper.readCommaSeperatedString(""), new String[0]);

        // Test simple strings
        assertArrayEquals(StringHelper.readCommaSeperatedString("a"), new String[] {"a"});
        assertArrayEquals(StringHelper.readCommaSeperatedString("a,b,cd"), new String[] {"a", "b", "cd"});

        // Test if commas inside braces are ignored
        assertArrayEquals(StringHelper.readCommaSeperatedString("a,b(c,d),e"), new String[] {"a", "b(c,d)", "e"});

        // Test handling of nested braces
        assertArrayEquals(StringHelper.readCommaSeperatedString("a,b(c,d(e,f),g),h"), new String[] {"a", "b(c,d(e,f),g)", "h"});

        // Test if whitespace is stripped
        assertArrayEquals(StringHelper.readCommaSeperatedString("   "), new String[0]);
        assertArrayEquals(StringHelper.readCommaSeperatedString("a, b(c,d), e"), new String[] {"a", "b(c,d)", "e"});
        assertArrayEquals(StringHelper.readCommaSeperatedString("  a, b(c,d), e  "), new String[] {"a", "b(c,d)", "e"});
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
