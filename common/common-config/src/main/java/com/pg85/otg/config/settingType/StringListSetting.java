package com.pg85.otg.config.settingType;

import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.util.helpers.StringHelper;

import java.util.Arrays;
import java.util.List;

/**
 * Reads and writes a list of strings. Strings are read using
 * {@link StringHelper#readCommaSeperatedString(String)}, and written with a
 * ", " between each string.
 *
 */
class StringListSetting extends Setting<List<String>>
{
	private String[] defaultValue;

	StringListSetting(String name, String... defaultValue)
	{
		super(name);
		this.defaultValue = defaultValue;
	}

	@Override
	public List<String> getDefaultValue(IMaterialReader materialReader)
	{
		return Arrays.asList(defaultValue);
	}

	@Override
	public List<String> read(String string, IMaterialReader materialReader) throws InvalidConfigException
	{
		return Arrays.asList(StringHelper.readCommaSeperatedString(string));
	}

	@Override
	public String write(List<String> value)
	{
		return StringHelper.join(value, ", ");
	}

}
