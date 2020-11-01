package com.pg85.otg.gen.biome.layers.legacy;

public class LayerZoomFuzzy extends LayerZoom
{

    LayerZoomFuzzy(long seed, int defaultOceanId, Layer childLayer)
    {
        super(seed, defaultOceanId, childLayer);
    }

    @Override
    protected int getRandomOf4(int a, int b, int c, int d)
    {
        return this.getRandomInArray(a, b, c, d);
    }

}