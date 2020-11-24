package com.pg85.otg.util.interfaces;

import java.util.ArrayList;

import com.pg85.otg.constants.SettingsEnums.BiomeMode;
import com.pg85.otg.constants.SettingsEnums.ConfigMode;
import com.pg85.otg.util.biome.ReplacedBlocksMatrix;
import com.pg85.otg.util.materials.LocalMaterialData;

public interface IWorldConfig
{
	public boolean doPopulationBoundsCheck();
	public boolean getBedrockDisabled();
	public int getWorldHeightCap();
	public int getRavineRarity();
	public int getRavineMinLength();
	public int getRavineMaxLength();
	public double getRavineDepth();
	public int getRavineMinAltitude();
	public int getRavineMaxAltitude();
	public int getCaveFrequency();
	public int getCaveRarity();
	public boolean isEvenCaveDistribution();
	public int getCaveMinAltitude();
	public int getCaveMaxAltitude();
	public int getCaveSystemFrequency();
	public int getIndividualCaveRarity();
	public int getCaveSystemPocketMinSize();
	public int getCaveSystemPocketChance();
	public int getCaveSystemPocketMaxSize();
	public boolean isBetterSnowFall();
	public boolean isFullyFreezeLakes();
	public boolean isOTGPlus();
	public long getResourcesSeed();
	public boolean isDisableOreGen();
	public int getMaximumCustomStructureRadius();
	public boolean getBiomeConfigsHaveReplacement();
	public boolean setBiomeConfigsHaveReplacement(boolean biomeConfigsHaveReplacement);
	public double getFractureHorizontal();
	public double getFractureVertical();
	public LocalMaterialData getBedrockBlockReplaced(ReplacedBlocksMatrix replacedBlocks, int y);
	public LocalMaterialData getDefaultBedrockBlock();
	public int getBiomeRarityScale();
	public LocalMaterialData getCooledLavaBlock();
	public int getGenerationDepth();
	public LocalMaterialData getIceBlock();
	public boolean getIsCeilingBedrock();
	public boolean getIsFlatBedrock();
	public int getMaxSmoothRadius();
	public boolean getMineshaftsEnabled();
	public boolean getOceanMonumentsEnabled();
	public boolean getRareBuildingsEnabled();
	public boolean getRemoveSurfaceStone();
	public ConfigMode getSettingsMode();
	public boolean getStrongholdsEnabled();
	public boolean getVillagesEnabled();
	public LocalMaterialData getWaterBlock();
	public int getWaterLevelMax();
	public int getWaterLevelMin();
	public ArrayList<String> getWorldBiomes();
	public int getWorldHeightScale();
	public void setMaxSmoothRadius(int smoothRadius);
	public String getDefaultOceanBiome();
	public int getLandFuzzy();
	public int getLandRarity();
	public int getLandSize();
	public BiomeMode getBiomeMode();
}
