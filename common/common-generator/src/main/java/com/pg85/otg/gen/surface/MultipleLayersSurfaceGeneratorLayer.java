package com.pg85.otg.gen.surface;

import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.util.biome.ReplaceBlockMatrix;
import com.pg85.otg.util.materials.LocalMaterialData;

// TODO: We're probably only implementing comparable here for biome inheritance, 
// which we burned with fire because it required code like this for every setting, remove?
class MultipleLayersSurfaceGeneratorLayer implements Comparable<MultipleLayersSurfaceGeneratorLayer>
{
	protected final LocalMaterialData surfaceBlock;
	protected final LocalMaterialData groundBlock;
	final float maxNoise;

	private boolean initialized = false;
	private boolean surfaceBlockIsReplaced;
	private boolean groundBlockIsReplaced;

	MultipleLayersSurfaceGeneratorLayer(LocalMaterialData surfaceBlock, LocalMaterialData groundBlock, float maxNoise)
	{
		this.surfaceBlock = surfaceBlock;
		this.groundBlock = groundBlock;
		this.maxNoise = maxNoise;
	}

	LocalMaterialData getSurfaceBlockReplaced(int y, IBiomeConfig biomeConfig)
	{
		// TODO: Make this prettier?
		Init(biomeConfig.getReplaceBlocks());
		LocalMaterialData materialData = null;
		if(this.surfaceBlockIsReplaced)
		{
			materialData = this.surfaceBlock.parseWithBiomeAndHeight(biomeConfig.biomeConfigsHaveReplacement(), biomeConfig.getReplaceBlocks(), y);
		}
		if(materialData == null)
		{
			materialData = this.surfaceBlock;
		}
		if(materialData.isAir() && y < biomeConfig.getWaterLevelMax() && y >= biomeConfig.getWaterLevelMin())
		{
			materialData = biomeConfig.getWaterBlockReplaced(y);
		}
		return materialData;
	}
	
	LocalMaterialData getGroundBlockReplaced(int y, IBiomeConfig biomeConfig)
	{
		// TODO: Make this prettier?
		Init(biomeConfig.getReplaceBlocks());
		LocalMaterialData materialData = null;
		if(this.groundBlockIsReplaced)
		{
			materialData = this.groundBlock.parseWithBiomeAndHeight(biomeConfig.biomeConfigsHaveReplacement(), biomeConfig.getReplaceBlocks(), y);
		}
		if(materialData == null)
		{
			materialData = this.groundBlock;
		}
		if(materialData.isAir() && y < biomeConfig.getWaterLevelMax() && y >= biomeConfig.getWaterLevelMin())
		{
			materialData = biomeConfig.getWaterBlockReplaced(y);
		}
		return materialData;
	}
	
	private void Init(ReplaceBlockMatrix replacedBlocks)
	{
		if(!initialized)
		{
			initialized = true;
			surfaceBlockIsReplaced = replacedBlocks.replacesBlock(surfaceBlock);
			groundBlockIsReplaced = replacedBlocks.replacesBlock(groundBlock);
		}
	}
	
	@Override
	public int compareTo(MultipleLayersSurfaceGeneratorLayer that)
	{
		float delta = this.maxNoise - that.maxNoise;
		// The number 65565 is just randomly chosen, any positive number
		// works fine as long as it can represent the floating point delta
		// as an integer
		return (int) (delta * 65565);
	}
}
