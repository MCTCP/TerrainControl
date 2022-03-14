package com.pg85.otg.util.biome;

import java.util.ArrayList;
import java.util.List;

public class ColorSet
{

	protected List<ColorThreshold> layers = new ArrayList<>();

	public List<ColorThreshold> getLayers()
	{
		return layers;
	}

	public int getColor(double noise, int def)
	{
		if (this.layers.isEmpty())
		{
			return def;
		}
		for (ColorThreshold color : this.layers)
		{
			if (noise <= color.maxNoise)
			{
				return color.getColor();
			}
		}
		return def;
	}

	@Override
	public String toString()
	{
		// Make sure that empty name is written to the config files
		return "";
	}
}
