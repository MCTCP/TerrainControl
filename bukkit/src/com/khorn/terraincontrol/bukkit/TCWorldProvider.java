package com.khorn.terraincontrol.bukkit;

import net.minecraft.server.v1_5_R2.WorldProvider;
import net.minecraft.server.v1_5_R2.WorldProviderNormal;

/**
 * We extend this file to be able to set the sea level.
 * In Minecraft this is used in a few places such as spawning algorithms for villages.
 * The value seem to be hardcoded in CraftWorld and we are a bit unsure about if that matters.
 * At least it should be a good thing that we set the value here.
 */
public class TCWorldProvider extends WorldProviderNormal
{
    protected BukkitWorld localWorld;
    private final WorldProvider oldWorldProvider;

    public TCWorldProvider(BukkitWorld localWorld, WorldProvider oldWorldProvider)
    {
        this.localWorld = localWorld;
        this.oldWorldProvider = oldWorldProvider;
        this.a(localWorld.getWorld());
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

    /**
     * Returns the world provider that was replaced by the current world provider.
     * When the plugin disables, this needs to be restored.
     * 
     * @return The old world provider.
     */
    public WorldProvider getOldWorldProvider()
    {
        return oldWorldProvider;
    }
}