package com.pg85.otg.forge.gui.screens;

import com.pg85.otg.core.config.dimensions.DimensionConfig;

import net.minecraft.world.level.levelgen.WorldGenSettings;

public class OTGDimensionSettingsContainer
{
	public DimensionConfig dimensionConfig;
	public WorldGenSettings dimGenSettings;
	
	public OTGDimensionSettingsContainer(DimensionConfig dimensionConfig, WorldGenSettings dimGenSettings)
	{
		this.dimensionConfig = dimensionConfig;
		this.dimGenSettings = dimGenSettings;
	}
}
