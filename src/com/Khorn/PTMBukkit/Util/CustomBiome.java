package com.Khorn.PTMBukkit.Util;


import net.minecraft.server.BiomeBase;

public class CustomBiome extends BiomeBase
{
    public CustomBiome(int id,String name)
    {
        super(id);
        this.a(name);
    }
    public void SetTerrainGen(float surfaceAdd,float volatility)
    {
        this.q = surfaceAdd;
        this.r = volatility;
    }

}
