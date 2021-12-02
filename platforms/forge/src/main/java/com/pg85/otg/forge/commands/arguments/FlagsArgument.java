package com.pg85.otg.forge.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

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
	public String parse(StringReader reader) throws CommandSyntaxException {
		return reader.readString();
	}
}
