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

        this.w = config.BiomeHeight;
        this.x = config.BiomeVolatility;
        this.t = config.SurfaceBlock;
        this.u = config.GroundBlock;
        this.y = config.BiomeTemperature;
        this.z = config.BiomeWetness;


    }

}
