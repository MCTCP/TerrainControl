package com.khorn.terraincontrol.configuration;

import com.khorn.terraincontrol.DefaultMaterial;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.exception.InvalidResourceException;

import java.util.ArrayList;
import java.util.List;

public abstract class ConfigFunction<T>
{
    protected int frequency;
    protected int rarity;
    private T holder;

    @SuppressWarnings("unchecked")
    public void setHolder(Object holder)
    {
        this.holder = (T) holder;
    }

    public T getHolder()
    {
        return holder;
    }

    public abstract Class<T> getHolderType();

    /**
     * Convenience method for creating a config function. Used to create the
     * default config functions.
     *
     * @param <T>
     * @param clazz
     * @param args
     * @return
     */
    public static <T> ConfigFunction<?> create(T holder, Class<? extends ConfigFunction<T>> clazz, Object... args)
    {
        List<String> stringArgs = new ArrayList<String>(args.length);
        for (Object arg : args)
        {
            stringArgs.add("" + arg);
        }

        ConfigFunction<?> configFunction;
        try
        {
            configFunction = clazz.newInstance();
        } catch (InstantiationException e)
        {
            return null;
        } catch (IllegalAccessException e)
        {
            return null;
        }
        configFunction.setHolder(holder);
        try
        {
            configFunction.load(stringArgs);
        } catch (InvalidResourceException e)
        {
            TerrainControl.log("Invalid default config function! Please report! " + clazz.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }

        return configFunction;
    }

    /**
     * Loads the settings. Returns false if one of the arguments contains an
     * error.
     *
     * @param args List of args.
     * @return Returns false if one of the arguments contains an error,
     *         otherwise true.
     * @throws InvalidResourceException If the resoure is invalid.
     */
    public abstract void load(List<String> args) throws InvalidResourceException;

    /**
     * Gets a String representation, like Tree(10,BigTree,50,Tree,100)
     *
     * @return A String representation, like Tree(10,BigTree,50,Tree,100)
     */
    public abstract String makeString();

    /**
     * Parses the string and returns a number between minValue and maxValue.
     * Returns Resource.INCORRECT_NUMBER if the string is not a number.
     *
     * @param string
     * @param minValue
     * @param maxValue
     * @return
     * @throws InvalidResourceException If the number is invalid.
     */
    protected int readInt(String string, int minValue, int maxValue) throws InvalidResourceException
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
    protected int readBlockId(String string) throws InvalidResourceException
    {
        if (string.indexOf('.') != -1)
        {
            // Ignore block data
            string = string.split("\\.")[0];
        }

        DefaultMaterial material = DefaultMaterial.getMaterial(string);
        if (material != null)
        {
            return material.id;
        }

        return readInt(string, 0, TerrainControl.supportedBlockIds);
    }

    /**
     * Gets the block data from a material string.
     *
     * @param string
     * @return
     * @throws InvalidResourceException
     */
    protected int readBlockData(String string) throws InvalidResourceException
    {
        if (string.indexOf('.') == -1)
        {
            // No block data
            return 0;
        }

        // Get block data
        string = string.split("\\.")[1];
        return readInt(string, 0, 16);
    }

    protected void assureSize(int size, List<String> args) throws InvalidResourceException
    {
        if (args.size() < size)
        {
            throw new InvalidResourceException("Too few arguments supplied");
        }
    }

    /**
     * Gets the material name back from the id and data.
     *
     * @param id   The block id
     * @param data The block data
     * @return String in the format blockname[.blockdata]
     */
    protected String makeMaterial(int id, int data)
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
            materialString = materialString + "." + data;
        }

        return materialString;
    }

    /**
     * Gets the material name back from the id.
     *
     * @param id The block id
     * @return String in the format blockname
     */
    protected String makeMaterial(int id)
    {
        return makeMaterial(id, 0);
    }

    /**
     * Returns a String in the format ",materialName,materialName,etc"
     *
     * @param ids
     * @return
     */
    protected String makeMaterial(List<Integer> ids)
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
