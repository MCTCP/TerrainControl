package com.khorn.terraincontrol.bukkit;

import com.khorn.terraincontrol.BiomeIds;
import com.khorn.terraincontrol.configuration.BiomeConfig;

import net.minecraft.server.v1_7_R1.BiomeBase;


public class NullBiome extends BukkitBiome
{
    private String name;
    private static final BiomeIds NULL_ID = new BiomeIds(255);

    public NullBiome(String _name)
    {
        super(BiomeBase.OCEAN);
        this.name = _name;
    }

    @Override
    public boolean isCustom()
    {
        return true;
    }

    @Override
    public BiomeIds getIds()
    {
        return NULL_ID;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public void setEffects(BiomeConfig config)
    {

    }
}
