package com.khorn.terraincontrol.forge;

import java.util.NoSuchElementException;

import net.minecraft.world.WorldType;

abstract class WorldTypeHelper
{

    static int getNextWorldTypeID() {
        for (int i = 0; i < WorldType.worldTypes.length; i++)
            if (WorldType.worldTypes[i] == null) return i;
        throw new NoSuchElementException("No more WorldType indexes available.");
    }

}
