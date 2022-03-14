package com.pg85.otg.util.helpers;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.pg85.otg.exceptions.InvalidConfigException;

/**
 * Some methods for string parsing and printing.
 * 
 */
public abstract class StringHelper
{
	private StringHelper() { }
	
	public static String join(final Collection<?> coll, final String glue)
	{
		return join(coll.toArray(new Object[coll.size()]), glue);
	}

	public static String join(final Object[] list, final String glue)
	{
		StringBuilder ret = new StringBuilder(100);
		for (int i = 0; i < list.length; i++)
		{
			if (i != 0)
			{
				ret.append(glue);
			}
			ret.append(list[i]);
		}
		return ret.toString();
	}	

	/**
	 * Turns the given name into a name suitable for computers, so without
	 * strange chars that wouldn't be valid in a Java field.
	 * @param name The original name.
	 * @return The modified name
	 */
	public static String toComputerFriendlyName(String name)
	{
		char[] charArray = name.toCharArray();
		for (int i = 0; i < charArray.length; i++) {
			if (!Character.isJavaIdentifierPart(charArray[i]))
			{
				charArray[i] = '_';
			} else {
				charArray[i] = Character.toLowerCase(charArray[i]);
			}
		}
		return new String(charArray);
	}

	/**
	 * Parses the string and returns a number between minValue and maxValue.
	 * 
	 * @param string
	 *			The string to parse.
	 * @param minValue
	 *			The minimum value, inclusive.
	 * @param maxValue
	 *			The maximum value, inclusive.
	 * @return The number in the String, capped at the minValue and maxValue.
	 * @throws InvalidConfigException
	 *			 If the number is invalid.
	 */
	public static int readInt(String string, int minValue, int maxValue) throws InvalidConfigException
	{
		try
		{
			int number = Integer.parseInt(string);
			if (number < minValue)
			{
				return minValue;
			}
			if (number > maxValue)
			{
				return maxValue;
			}
			return number;
		} catch (NumberFormatException e) {
			try
			{
				int number = (int)Double.parseDouble(string);
				if (number < minValue)
				{
					return minValue;
				}
				if (number > maxValue)
				{
					return maxValue;
				}
				return number;
			} catch (NumberFormatException e2) {
				throw new InvalidConfigException("Incorrect number: " + string);				
			}
		}
	}
	
	//TODO document
	public static int readColor(String string) throws InvalidConfigException {
		try
		{
			Integer integer = Integer.decode(string);
			if (integer.intValue() > 0xffffff || integer.intValue() < 0)
			{
				throw new InvalidConfigException("Color must have 6 hexadecimal digits");
			}
			return integer;
		} catch (NumberFormatException e) {
			throw new InvalidConfigException("Invalid color " + string);
		}
	}

	/**
	 * Parses the string and returns a number between minValue and maxValue.
	 * 
	 * @param string
	 *			The string to parse.
	 * @param minValue
	 *			The minimum value, inclusive.
	 * @param maxValue
	 *			The maximum value, inclusive.
	 * @return The number in the String, capped at the minValue and maxValue.
	 * @throws InvalidConfigException
	 *			 If the number is invalid.
	 */
	public static double readDouble(String string, double minValue, double maxValue) throws InvalidConfigException
	{
		try
		{
			double number = Double.parseDouble(string);
			if (number < minValue)
			{
				return minValue;
			}
			if (number > maxValue)
			{
				return maxValue;
			}
			return number;
		} catch (NumberFormatException e) {
			throw new InvalidConfigException("Incorrect number: " + string);
		}
	}

	/**
	 * Parses a string in the format <code>part1,part2,..</code>, which will
	 * return <code>["part1", "part2", ..]</code>. Commas (',') inside braces
	 * are ignored, so <code>part1,part2(extravalue,anothervalue),part3</code>
	 * gets parsed as
	 * <code>["part1", "part2(extravalue,anothervalue)", "part3"]</code>
	 * instead of
	 * <code>["part1", "part2(extravalue", "anothervalue)", "part3"]</code>.
	 *
	 * <p>Extra whitespace around each part is removed using
	 * {@link String#trim()}. <code>part1, part2</code> will become
	 * <code>["part1", "part2"]</code> and not <code>["part1", " part2"]</code>
	 *
	 * <p>An empty string, or a string consisting of only whitespace, will
	 * result in an empty array.
	 *
	 * @param line
	 *			The line to parse.
	 * @return The parts of the string.
	 */
	public static String[] readCommaSeperatedString(String line)
	{
		if (line.trim().isEmpty())
		{
			// Empty lines have no elements, not one empty element
			return new String[0];
		}

		List<String> buffer = new LinkedList<String>();

		int index = 0;
		int lastFound = 0;
		int inBracer = 0;

		// This excludes any comma's inside brackets/braces (?)
		for (char c : line.toCharArray())
		{
			if (c == ',' && inBracer == 0)
			{
				buffer.add(line.substring(lastFound, index).trim());
				lastFound = index + 1;
			}
			if (c == '(' || c == '[')
			{
				inBracer++;
			}
			if (c == ')' || c == ']')
			{
				inBracer--;
			}
			index++;
		}
		buffer.add(line.substring(lastFound, index).trim());

		String[] output = new String[0];

		if (inBracer == 0)
		{
			output = buffer.toArray(output);
		}

		return output;
	}

	/**
	 * Parses the string and returns a number between minValue and maxValue.
	 * 
	 * @param string
	 *			The string to parse.
	 * @param minValue
	 *			The minimum value, inclusive.
	 * @param maxValue
	 *			The maximum value, inclusive.
	 * @return The number in the String, capped at the minValue and maxValue.
	 * @throws InvalidConfigException
	 *			 If the number is invalid.
	 */
	public static long readLong(String string, long minValue, long maxValue) throws InvalidConfigException
	{
		try
		{
			long number = Long.parseLong(string);
			if (number < minValue)
			{
				return minValue;
			}
			if (number > maxValue)
			{
				return maxValue;
			}
			return number;
		} catch (NumberFormatException e) {
			throw new InvalidConfigException("Incorrect number: " + string);
		}
	}
}
