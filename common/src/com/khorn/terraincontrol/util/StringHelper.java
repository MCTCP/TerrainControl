package com.khorn.terraincontrol.util;

import com.khorn.terraincontrol.DefaultMaterial;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.exception.InvalidResourceException;

import java.util.Collection;
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
        StringBuilder ret = new StringBuilder();
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
     * Returns Resource.INCORRECT_NUMBER if the string is not a number.
     *
     * @param string    The string to parse.
     * @param minValue  The minimum value, inclusive.
     * @param maxValue  The maximum value, inclusive.
     * @return          The number in the String, capped at the minValue and maxValue.
     * @throws InvalidResourceException If the number is invalid.
     */
    public static int readInt(String string, int minValue, int maxValue) throws InvalidResourceException
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
            throw new InvalidResourceException("Incorrect number: " + string);
        }
    }

    /**
     * Returns the block id with the given name.
     *
     * @param string
     * @return
     */
    public static int readBlockId(String string) throws InvalidResourceException
    {
        if(string.contains("SPONGE")) TerrainControl.log("parsing. " + string);
        // Parse . (Deprecated)
        if (string.indexOf('.') != -1)
        {
            // Ignore block data
            string = string.split("\\.")[0];
        }
        
        // Parse :
        if (string.indexOf(':') != -1)
        {
            // Ignore block data
            string = string.split(":")[0];
            TerrainControl.log("parsing " + string);
        }

        DefaultMaterial material = DefaultMaterial.getMaterial(string);
        if (material != null)
        {
            return material.id;
        }

        return readInt(string, 0, TerrainControl.supportedBlockIds);
    }

    /**
     * Gets the block data from a material string. Capped between 0 and 15 (inclusive).
     *
     * @param string    The String to parse, in the format name/id[:data/.data]
     * @return The block data.
     * @throws InvalidResourceException If the input is invalid.
     */
    public static int readBlockData(String string) throws InvalidResourceException
    {
        if (string.indexOf(':') != -1)
        {
            // Found new syntax
            string = string.split(":")[1];
            return readInt(string, 0, 15);
        }
        if (string.indexOf('.') != -1)
        {
            // Found old syntax
            string = string.split("\\.")[1];
            return readInt(string, 0, 15);
        }
        // No block data
        return 0;       
    }

    /**
     * Gets the material name back from the id and data.
     *
     * @param id   The block id
     * @param data The block data
     * @return String in the format blockname[.blockdata]
     */
    public static String makeMaterial(int id, int data)
    {
        String materialString = "" + id;
        DefaultMaterial material = DefaultMaterial.getMaterial(id);
        if (material != DefaultMaterial.UNKNOWN_BLOCK)
        {
            // No name, return number as String
            materialString = material.toString();
        }

        if (data > 0)
        {
            materialString = materialString + ":" + data;
        }

        return materialString;
    }

    /**
     * Gets the material name back from the id.
     *
     * @param id The block id
     * @return String in the format blockname
     */
    public static String makeMaterial(int id)
    {
        return makeMaterial(id, 0);
    }

    /**
     * Returns a String in the format ",materialName,materialName,etc"
     *
     * @param ids The block ids to parse.
     * @return String in the format ",materialName,materialName,etc"
     */
    public static String makeMaterial(List<Integer> ids)
    {
        String string = "";
        for (int blockId : ids)
        {
            string += ",";
            string += makeMaterial(blockId);
        }
        return string;
    }
}
