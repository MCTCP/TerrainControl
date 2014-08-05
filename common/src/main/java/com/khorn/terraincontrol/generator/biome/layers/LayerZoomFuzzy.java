package com.khorn.terraincontrol.generator.biome.layers;

public class LayerZoomFuzzy extends LayerZoom
{

    public LayerZoomFuzzy(long seed, Layer childLayer)
    {
        super(seed, childLayer);
    }

    @Override
    protected int getRandomOf4(int a, int b, int c, int d)
    {
        return this.getRandomInArray(a, b, c, d);
    }

}