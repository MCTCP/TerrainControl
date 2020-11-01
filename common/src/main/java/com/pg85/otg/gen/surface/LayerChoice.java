package com.pg85.otg.gen.surface;

import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.common.materials.LocalMaterialData;
import com.pg85.otg.config.biome.BiomeConfig;
import com.pg85.otg.config.biome.settings.ReplacedBlocksMatrix;

public class LayerChoice implements Comparable<LayerChoice>
{
	protected final LocalMaterialData surfaceBlock;
	protected final LocalMaterialData groundBlock;
	final float maxNoise;

    private boolean initialized = false;
    private boolean surfaceBlockIsReplaced;
    private boolean groundBlockIsReplaced;

    LayerChoice(LocalMaterialData surfaceBlock, LocalMaterialData groundBlock, float maxNoise)
    {
        this.surfaceBlock = surfaceBlock;
        this.groundBlock = groundBlock;
        this.maxNoise = maxNoise;
    }

    public LocalMaterialData getSurfaceBlockReplaced(LocalWorld world, BiomeConfig biomeConfig, int y)
    {    	
    	// TODO: BiomeConfig should always be the same, this layer should only be used in a single biome,
    	// Make this prettier?
    	Init(biomeConfig.replacedBlocks);
    	if(this.surfaceBlockIsReplaced)
    	{
    		return this.surfaceBlock.parseWithBiomeAndHeight(world, biomeConfig, y);	
    	}
    	return this.surfaceBlock;
    }
    
    public LocalMaterialData getGroundBlockReplaced(LocalWorld world, BiomeConfig biomeConfig, int y)
    {
    	// TODO: BiomeConfig should always be the same, this layer should only be used in a single biome,
    	// Make this prettier?
    	Init(biomeConfig.replacedBlocks);
    	if(this.groundBlockIsReplaced)
    	{
    		return this.groundBlock.parseWithBiomeAndHeight(world, biomeConfig, y);	
    	}
    	return this.groundBlock;
    }
    
    public void Init(ReplacedBlocksMatrix replacedBlocks)
    {
    	if(!initialized)
    	{
    		initialized = true;
    		surfaceBlockIsReplaced = replacedBlocks.replacesBlock(surfaceBlock);
    		groundBlockIsReplaced = replacedBlocks.replacesBlock(groundBlock);
    	}
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