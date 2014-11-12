package com.khorn.terraincontrol.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class BoundingBoxTest
{

    @Test
    public void testCreateEmpty()
    {
        BoundingBox emptyBox = BoundingBox.newEmptyBox();
        assertTrue(emptyBox.isEmpty());
    }

    @Test
    public void testResizeEmpty()
    {
        BoundingBox box = BoundingBox.newEmptyBox();
        box.expandToFit(10, 20, 30);

        // Test if box volume is exactly of that one block
        assertEquals(10, box.getMinX());
        assertEquals(20, box.getMinY());
        assertEquals(30, box.getMinZ());
        assertEquals(1, box.getWidth());
        assertEquals(1, box.getHeight());
        assertEquals(1, box.getDepth());
    }

    @Test
    public void testResizeTwice()
    {
        BoundingBox box = BoundingBox.newEmptyBox();
        box.expandToFit(1, 2, 3);
        box.expandToFit(-1, -2, -3);

        assertEquals(-1, box.getMinX());
        assertEquals(-2, box.getMinY());
        assertEquals(-3, box.getMinZ());

        assertEquals(3, box.getWidth());
        // ^ width = 3, as there are three blocks: block at -1, 0 and 1
        assertEquals(5, box.getHeight());
        assertEquals(7, box.getDepth());
    }

    @Test
    public void testRotate()
    {
        BoundingBox original = BoundingBox.newEmptyBox();
        original.expandToFit(1, 2, 3);

        // Check coord of rotated, check if original has not been modified
        BoundingBox rotated = original.rotate();
        assertEquals(1, original.getMinX());
        assertEquals(3, rotated.getMinX());

        assertEquals(3, original.getMinZ());
        assertEquals(-1, rotated.getMinZ());
    }

    @Test
    public void testEqualsAndHashcode()
    {
        BoundingBox one = BoundingBox.newEmptyBox();
        one.expandToFit(1, 2, 3);
        BoundingBox two = BoundingBox.newEmptyBox();
        two.expandToFit(1, 2, 3);

        assertEquals(one, two);
        assertEquals(one.hashCode(), two.hashCode());
    }
}
