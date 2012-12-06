package com.khorn.terraincontrol.bukkit;

import net.minecraft.server.v1_4_5.WorldProvider;


/**
 * We extend this file to be able to set the sea level.
 * In minecraft 1.2.3 this is used in a few places such as spawning algorithms for villages.
 * The value seem to be hardcoded in CraftWorld and we are a bit unsure about if that matters.
 * At least it should be a good thing that we set the value here.
 */
public class TCWorldProvider extends WorldProvider
{
    protected int seaLevel = 64;
    
    @Override
    public int getSeaLevel()
    {
        return this.seaLevel;
    }
    
    public TCWorldProvider setSeaLevel(int value)
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
    
    public String getName()
    {
        return "Terrain Control";
    }
}