package com.khorn.terraincontrol.configuration;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.util.StringHelper;

import java.util.ArrayList;
import java.util.List;

public abstract class ConfigFunction<T>
{
    private T holder;

    // Error handling
    private boolean valid;
    private String error;
    private String inputName;
    private List<String> inputArgs;

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
        } catch (InvalidConfigException e)
        {
            TerrainControl.log("Invalid default config function! Please report! " + clazz.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }

        return configFunction;
    }

    /**
     * Loads the settings. Will set the internal invalid flag to true
     * if something went wrong.
     * 
     * @param args List of args
     * @throws InvalidConfigException If the syntax is invalid.
     */
    public final void read(String name, List<String> args) throws InvalidConfigException
    {
        try
        {
            load(args);
        } catch (InvalidConfigException e)
        {
            this.valid = false;
            this.error = e.getMessage();
            this.inputArgs = args;
            this.inputName = name;
            // Rethrow
            throw e;
        }
        valid = true;
    }

    /**
     * Returns true if this ConfigFunction has a correct syntax.
     * Returns false if the read method hasn't been called yet,
     * or if the function has an incorrect syntax.
     * 
     * @return Whether this ConfigFunction has a correct syntax.
     */
    public boolean isValid()
    {
        return valid;
    }
    
    /**
     * Forcibly sets the internal valid flag of this function to the 
     * specified value. As long as you are using the read method you
     * shouldn't use this, as the read method will automatically set
     * it to the correct value.
     * 
     * @param valid New value for the valid flag.
     */
    public void setValid(boolean valid)
    {
        this.valid = valid;
    }

    public final String write()
    {
        if (!valid)
        {
            // Show error message
            return "## INVALID " + inputName.toUpperCase() + " - " + error + " ##" + System.getProperty("line.separator") + inputName + "(" + StringHelper.join(inputArgs, ",") + ")";
        } else
        {
            return makeString();
        }

    }

    /**
     * Loads the settings. Returns false if one of the arguments contains an
     * error.
     *
     * @param args List of args.
     * @return Returns false if one of the arguments contains an error,
     *         otherwise true.
     * @throws InvalidConfigException If the syntax is invalid.
     */
    protected abstract void load(List<String> args) throws InvalidConfigException;

    /**
     * Gets a String representation, like Tree(10,BigTree,50,Tree,100)
     *
     * @return A String representation, like Tree(10,BigTree,50,Tree,100)
     */
    protected abstract String makeString();

    /**
     * Parses the string and returns a number between minValue and maxValue.
     *
     * @param string
     * @param minValue
     * @param maxValue
     * @return
     * @throws InvalidConfigException If the number is invalid.
     */
    protected int readInt(String string, int minValue, int maxValue) throws InvalidConfigException
    {
        return StringHelper.readInt(string, minValue, maxValue);
    }

    /**
     * Parses the string and returns a number between minValue and maxValue.
     *
     * @param string   The string to parse.
     * @param minValue The minimum value.
     * @param maxValue The maximum value.
     * @return A double between min and max.
     * @throws InvalidConfigException If the number is invalid.
     */
    protected double readDouble(String string, double minValue, double maxValue) throws InvalidConfigException
    {
        return StringHelper.readDouble(string, minValue, maxValue);
    }

    /**
     * Parses the string and returns the rarity between 0.000001 and 100 (inclusive)
     * 
     * @param string The string to parse.
     * @return The rarity.
     * @throws InvalidConfigException If the number is invalid.
     */
    protected double readRarity(String string) throws InvalidConfigException
    {
        return StringHelper.readDouble(string, 0.000001, 100);
    }

    /**
     * Returns the block id with the given name.
     *
     * @param string
     * @return
     */
    protected int readBlockId(String string) throws InvalidConfigException
    {
        return StringHelper.readBlockId(string);
    }

    /**
     * Reads all block ids from the start position until the end of the list.
     * 
     * @param strings The input strings.
     * @param start   The position to start. The first element in the list
     *                has index 0, the last one size() - 1.
     * @return All block ids.
     * @throws InvalidConfigException If one of the elements in the list is not a valid block id.
     */
    protected List<Integer> readBlockIds(List<String> strings, int start) throws InvalidConfigException
    {
        List<Integer> blockIds = new ArrayList<Integer>();
        for (int i = start; i < strings.size(); i++)
        {
            blockIds.add(StringHelper.readBlockId(strings.get(i)));
        }

        return blockIds;
    }

    /**
     * Gets the block data from a material string.
     *
     * @param string
     * @return
     * @throws InvalidConfigException
     */
    protected int readBlockData(String string) throws InvalidConfigException
    {
        return StringHelper.readBlockData(string);
    }

    protected void assureSize(int size, List<String> args) throws InvalidConfigException
    {
        if (args.size() < size)
        {
            throw new InvalidConfigException("Too few arguments supplied");
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
        return StringHelper.makeMaterial(id, data);
    }

    /**
     * Gets the material name back from the id.
     *
     * @param id The block id
     * @return String in the format blockname
     */
    protected String makeMaterial(int id)
    {
        return StringHelper.makeMaterial(id);
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
