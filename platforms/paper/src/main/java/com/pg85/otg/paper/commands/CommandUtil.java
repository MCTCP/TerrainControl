package com.pg85.otg.paper.commands;

import java.util.HashMap;
import java.util.Map;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.pg85.otg.OTG;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;

public class CommandUtil
{
	
	/** This method takes a list of strings from spigot and turns it into a map of arguments
	 *  - Normal arguments are mapped to their index as a string, i.e. "1": "biome bundle"
	 *  - Flags with one - are mapped as key to an empty string, to signify they're set, i.e. "-o": ""
	 *  - Flags with two -- are mapped as key to the subsequent argument, i.e. "--file": "test.bo3"
	 *
	 * 	This method also reads quoted strings as one string, without the quotes
	 *
	 * @param strings The command args
	 * @return The args, mapped by index or flag
	 */
	public static Map<String, String> parseArgs(String[] strings, boolean stripFirst)
	{
		HashMap<String, String> argsMap = new HashMap<>();
		String input = String.join(" ", strings);
		long count = input.chars().filter(c -> c == '"').count();
		StringReader reader = new StringReader(input);
		String str;
		int index = 1;
		try
		{
			if (stripFirst) {
				reader.readString();
			}
			while (reader.getCursor() < input.length())
			{
				if (reader.peek() == ' ') reader.skip();
				if (count % 2 == 1 && reader.peek() == '"')
				{ // Non-ended quote, gotta just get the remainder
					str = reader.getRemaining();
					reader.setCursor(reader.getTotalLength());
				} else {
					str = reader.readString();
				}

				// If the next char is one not read by the stringreader, we add it on to the current argument and then keep reading
				// If the illegal char is at the end of a word, readString should just return empty because space is another illegal char
				// I'm not sure what happens if you have multiple spaces after one another, things might get strange
				if (reader.getCursor() < input.length() && !StringReader.isAllowedInUnquotedString(reader.peek()))
				{
					char c = reader.peek();
					reader.skip();
					str = str + c + reader.readString();
				}

				if (str.startsWith("-"))
				{
					if (str.startsWith("--")) // if str is a double line flag, means it has a payload
					{
						if (reader.canRead())
						{
							argsMap.put(str, reader.readString());
						}
						continue;
					}

					if (str.startsWith("-")) // if str is a single line flag
					{
						argsMap.put(str, "");
						continue;
					}
				}

				argsMap.put((index++) + "", str);
			}
		}
		catch (CommandSyntaxException e)
		{
			OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.MAIN, String.format("Command syntax error: ", (Object[])e.getStackTrace()));
		}

		return argsMap;
	}

}
