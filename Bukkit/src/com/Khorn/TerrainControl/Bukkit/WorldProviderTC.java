package com.Khorn.TerrainControl.Bukkit;

import net.minecraft.server.WorldProvider;

/**
 * We extend this file to be able to set the sea level.
 * In minecraft 1.2.3 this is used in a few places such as spawning algorithms for villages.
 */
public class WorldProviderTC extends WorldProvider
{
    protected int seaLevel = 64;
    
    @Override
    public int getSeaLevel()
    {
        return this.seaLevel;
    }
    
    public WorldProviderTC setSeaLevel(int value)
    {
        if (value < 1)
        {
            this.seaLevel = 1;
        }
        else if (value > 256)
        {
            this.seaLevel = 256;
        }
        else
        {
            this.seaLevel = value;
        }
        
        return this;
    }
}