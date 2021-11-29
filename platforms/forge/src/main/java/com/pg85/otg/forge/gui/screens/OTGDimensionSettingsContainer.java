package com.pg85.otg.forge.gui.screens;

import com.pg85.otg.config.dimensions.DimensionConfig;

import net.minecraft.world.gen.settings.DimensionGeneratorSettings;

public class OTGDimensionSettingsContainer
{
	public DimensionConfig dimensionConfig;
	public DimensionGeneratorSettings dimGenSettings;
	
	public OTGDimensionSettingsContainer(DimensionConfig dimensionConfig, DimensionGeneratorSettings dimGenSettings)
	{
		this.dimensionConfig = dimensionConfig;
		this.dimGenSettings = dimGenSettings;
	}
}
