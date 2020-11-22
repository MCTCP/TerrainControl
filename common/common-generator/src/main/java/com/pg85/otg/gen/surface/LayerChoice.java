package com.pg85.otg.gen.surface;

import com.pg85.otg.util.biome.ReplacedBlocksMatrix;
import com.pg85.otg.util.materials.LocalMaterialData;

class LayerChoice implements Comparable<LayerChoice>
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

    LocalMaterialData getSurfaceBlockReplaced(boolean biomeConfigsHaveReplacement, ReplacedBlocksMatrix replaceBlocks, int y)
    {    	
    	// TODO: BiomeConfig should always be the same, this layer should only be used in a single biome,
    	// Make this prettier?
    	Init(replaceBlocks);
    	if(this.surfaceBlockIsReplaced)
    	{
    		return this.surfaceBlock.parseWithBiomeAndHeight(biomeConfigsHaveReplacement, replaceBlocks, y);	
    	}
    	return this.surfaceBlock;
    }
    
    LocalMaterialData getGroundBlockReplaced(boolean biomeConfigsHaveReplacement, ReplacedBlocksMatrix replaceBlocks, int y)
    {
    	// TODO: BiomeConfig should always be the same, this layer should only be used in a single biome,
    	// Make this prettier?
    	Init(replaceBlocks);
    	if(this.groundBlockIsReplaced)
    	{
    		return this.groundBlock.parseWithBiomeAndHeight(biomeConfigsHaveReplacement, replaceBlocks, y);	
    	}
    	return this.groundBlock;
    }
    
    private void Init(ReplacedBlocksMatrix replacedBlocks)
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
