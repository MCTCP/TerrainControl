package com.Khorn.PTMBukkit.Util;


import com.Khorn.PTMBukkit.BiomeConfig;
import net.minecraft.server.BiomeBase;

public class CustomBiome extends BiomeBase
{
    public CustomBiome(int id,String name)
    {
        super(id);
        this.a(name);
    }
    public void SetBiome(BiomeConfig config)
    {

        this.q = config.BiomeSurface;
        this.r = config.BiomeVolatility;
        this.n = config.SurfaceBlock;
        this.o = config.GroundBlock;


    }

}
