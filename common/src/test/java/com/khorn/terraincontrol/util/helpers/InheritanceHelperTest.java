package com.khorn.terraincontrol.util.helpers;

import static org.junit.Assert.assertEquals;

import com.khorn.terraincontrol.generator.resource.DungeonGen;
import com.khorn.terraincontrol.generator.resource.Resource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RunWith(JUnit4.class)
public class InheritanceHelperTest
{

    @Test
    public void testEmptyChildList()
    {
        // If the child list is empty, all resources in the parent list must
        // be copied to the child.
        List<Resource> parentList = Arrays.asList(
                Resource.createResource(null, DungeonGen.class, 4, 100, 10, 30),
                Resource.createResource(null, DungeonGen.class, 1, 50, 30, 60)
                );
        List<Resource> childList = Collections.emptyList();

        List<Resource> result = InheritanceHelper.mergeLists(childList, parentList);
        assertEquals(parentList, result);
    }

    @Test
    public void testOverrides()
    {
        // The resource in the parent list shouldn't be added to the child
        // list, as the child already got a similar resource

        List<Resource> parentList = Arrays.asList(
                Resource.createResource(null, DungeonGen.class, 4, 100, 10, 30));

        List<Resource> childList = Arrays.asList(
                Resource.createResource(null, DungeonGen.class, 1, 50, 30, 60));

        List<Resource> result = InheritanceHelper.mergeLists(childList, parentList);
        assertEquals(childList, result);
    }
}
