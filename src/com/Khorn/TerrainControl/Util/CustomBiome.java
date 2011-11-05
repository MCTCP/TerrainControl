package com.Khorn.TerrainControl.Util;


import com.Khorn.TerrainControl.Configuration.BiomeConfig;
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

        this.q = config.BiomeHeight;
        this.r = config.BiomeVolatility;
        this.n = config.SurfaceBlock;
        this.o = config.GroundBlock;


    }

}
