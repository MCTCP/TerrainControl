package com.Khorn.TerrainControl.Util;

import net.minecraft.server.World;
import org.bukkit.BlockChangeDelegate;

public class WorldWithChunkCheck implements BlockChangeDelegate
{
    private World world;

    public WorldWithChunkCheck(World _world)
    {
        world = _world;
    }

    public boolean setRawTypeId(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {
        return world.setRawTypeId(paramInt1,paramInt2,paramInt3,paramInt4);
    }

    public boolean setRawTypeIdAndData(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5)
    {
        return world.setRawTypeIdAndData(paramInt1,paramInt2,paramInt3,paramInt4,paramInt5);
    }

    public int getTypeId(int paramInt1, int paramInt2, int paramInt3)
    {
        if(world.isLoaded(paramInt1,paramInt2,paramInt3))
            return world.getTypeId(paramInt1,paramInt2,paramInt3);
        return -1;
    }

    public int getHeight()
    {
        return world.height;
    }
}
