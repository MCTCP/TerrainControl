package com.pg85.otg.generator.surface;

import com.pg85.otg.common.LocalMaterialData;

public class LayerChoice implements Comparable<LayerChoice>
{
    final LocalMaterialData surfaceBlock;
    final LocalMaterialData groundBlock;
    final float maxNoise;

    LayerChoice(LocalMaterialData surfaceBlock, LocalMaterialData groundBlock, float maxNoise)
    {
        this.surfaceBlock = surfaceBlock;
        this.groundBlock = groundBlock;
        this.maxNoise = maxNoise;
    }

    @Override
    public int compareTo(LayerChoice that)
    {
        float delta = this.maxNoise - that.maxNoise;
        // The number 65565 is just randomly chosen, any positive number
        // works fine as long as it can represent the floating point delta
        // as an integer
        return (int) (delta * 65565);
    }
}