package com.pg85.otg.forge.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;

public class FlagsArgument implements ArgumentType<String>
{

	private FlagsArgument()
	{
	}

	public static FlagsArgument create()
	{
		return new FlagsArgument();
	}

	@Override
	public String parse(StringReader reader)
	{
		final String text = reader.getRemaining();
		reader.setCursor(reader.getTotalLength());
		return text;
	}
}
