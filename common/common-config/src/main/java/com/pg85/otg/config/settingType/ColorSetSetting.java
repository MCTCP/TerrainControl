package com.pg85.otg.config.settingType;

import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.util.biome.ColorSet;
import com.pg85.otg.util.biome.SimpleColorSet;
import com.pg85.otg.util.helpers.StringHelper;

public class ColorSetSetting extends Setting<ColorSet>
{

	protected ColorSetSetting(String name)
	{
		super(name);
	}

	@Override
	public ColorSet getDefaultValue(IMaterialReader materialReader)
	{
		return new ColorSet();
	}

	@Override
	public ColorSet read(String string, IMaterialReader materialReader) throws InvalidConfigException
	{
		return new SimpleColorSet(StringHelper.readCommaSeperatedString(string), materialReader);
	}

}
