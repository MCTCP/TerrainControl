package com.khorn.terraincontrol.bukkit;

import net.minecraft.server.v1_4_6.WorldProvider;



/**
 * We extend this file to be able to set the sea level.
 * In minecraft 1.2.3 this is used in a few places such as spawning algorithms for villages.
 * The value seem to be hardcoded in CraftWorld and we are a bit unsure about if that matters.
 * At least it should be a good thing that we set the value here.
 */
public class TCWorldProvider extends WorldProvider
{
    protected BukkitWorld localWorld;
    
    public TCWorldProvider(BukkitWorld localWorld)
    {
        this.localWorld = localWorld;
    }
    
    @Override
    public int getSeaLevel()
    {
        return localWorld.getSettings().waterLevelMax;
    }

    @Override
    public String getName()
    {
        return "Overworld";
    }
}