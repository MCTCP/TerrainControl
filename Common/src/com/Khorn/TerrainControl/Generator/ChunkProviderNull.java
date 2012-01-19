package com.Khorn.TerrainControl.Generator;


import com.Khorn.TerrainControl.Configuration.WorldConfig;

public class ChunkProviderNull
{

    private WorldConfig worldSettings;


    public ChunkProviderNull(WorldConfig config)
    {
        this.worldSettings = config;

    }


    public byte[] generate( int i, int i1)
    {
        int chunkMaxY = 128;
        int chunkMaxX = 16;
        int chunkMaxZ = 16;
        if( worldSettings.ModeTerrain == WorldConfig.TerrainMode.NotGenerate)
            return new byte[chunkMaxX * chunkMaxY * chunkMaxZ];
        return new byte[0];
    }

}


