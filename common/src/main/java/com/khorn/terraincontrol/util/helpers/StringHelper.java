package com.khorn.terraincontrol.util.helpers;

import com.khorn.terraincontrol.exception.InvalidConfigException;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Some methods for string parsing and printing.
 * 
 */
public abstract class StringHelper
{
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
     * Parses the string and returns a number between minValue and maxValue.
     * 
     * @param string
     *            The string to parse.
     * @param minValue
     *            The minimum value, inclusive.
     * @param maxValue
     *            The maximum value, inclusive.
     * @return The number in the String, capped at the minValue and maxValue.
     * @throws InvalidConfigException
     *             If the number is invalid.
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
        } catch (NumberFormatException e)
        {
            throw new InvalidConfigException("Incorrect number: " + string);
        }
    }

    /**
     * Parses the string and returns a number between minValue and maxValue.
     * 
     * @param string
     *            The string to parse.
     * @param minValue
     *            The minimum value, inclusive.
     * @param maxValue
     *            The maximum value, inclusive.
     * @return The number in the String, capped at the minValue and maxValue.
     * @throws InvalidConfigException
     *             If the number is invalid.
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
        } catch (NumberFormatException e)
        {
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
     *            The line to parse.
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

        for (char c : line.toCharArray())
        {
            if (c == ',' && inBracer == 0)
            {
                buffer.add(line.substring(lastFound, index).trim());
                lastFound = index + 1;
            }

            if (c == '(')
                inBracer++;
            if (c == ')')
                inBracer--;

            index++;
        }
        buffer.add(line.substring(lastFound, index).trim());

        String[] output = new String[0];

        if (inBracer == 0)
            output = buffer.toArray(output);

        return output;
    }

    /**
     * Gets whether the input specifies which block data should be used.
     * <p>
     * A few examples: "WOOL" doesn't specify block data, while "WOOL:0" does.
     * "buildcraft:blockRedLaser" doesn't specify block data, even though it
     * contains a colon. However, "buildcraft:blockRedLaser:0" does specify
     * block data.
     * 
     * @param materialString
     *            The input.
     * @return True if the input specifies block data, false otherwise.
     */
    public static boolean specifiesBlockData(String materialString) {
        int indexOfColon = materialString.lastIndexOf(":");
        if (indexOfColon > 0)
        {
            String blockDataString = materialString.substring(indexOfColon + 1);
            try {
                Integer.parseInt(blockDataString);
                // If we have reached this point, the text after the last colon
                // was numeric, so it was indeed block data
                return true;
            } catch (NumberFormatException e) {
            }
        }
        return false;
    }

    private StringHelper()
    {
    }

    /**
     * Parses the string and returns a number between minValue and maxValue.
     * 
     * @param string
     *            The string to parse.
     * @param minValue
     *            The minimum value, inclusive.
     * @param maxValue
     *            The maximum value, inclusive.
     * @return The number in the String, capped at the minValue and maxValue.
     * @throws InvalidConfigException
     *             If the number is invalid.
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
        } catch (NumberFormatException e)
        {
            throw new InvalidConfigException("Incorrect number: " + string);
        }
    }
}
