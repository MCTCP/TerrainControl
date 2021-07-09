package com.pg85.otg.util.biome;

import java.util.List;

import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.util.helpers.StringHelper;

public class SimpleColorSet extends ColorSet
{

	public SimpleColorSet(String[] args, IMaterialReader materialReader) throws InvalidConfigException
	{
		for (int i = 0; i < args.length - 1; i += 2)
		{
			Integer color = StringHelper.readColor(args[i]);
			float maxNoise = (float) StringHelper.readDouble(args[i + 1], -1, 1);
			layers.add(new ColorThreshold(color, maxNoise));
		}
	}

	public SimpleColorSet(List<ColorThreshold> list)
	{
		layers = list;
	}

	@Override
	public String toString()
	{
		if (this.layers.isEmpty())
		{
			return "";
		}

		StringBuilder stringBuilder = new StringBuilder();
		for (ColorThreshold layer : this.layers)
		{
			stringBuilder.append("#" + Integer.toHexString(layer.getColor() | 0x1000000).substring(1).toUpperCase());
			stringBuilder.append(',').append(' ');
			stringBuilder.append(layer.maxNoise);
			stringBuilder.append(',').append(' ');
		}
		// Delete last ", "
		stringBuilder.deleteCharAt(stringBuilder.length() - 2);
		return stringBuilder.toString();
	}
}
