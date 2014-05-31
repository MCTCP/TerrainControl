package com.khorn.terraincontrol.configuration;

import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.logging.LogMarker;
import com.khorn.terraincontrol.util.MaterialSet;
import com.khorn.terraincontrol.util.helpers.StringHelper;

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

    public void setHolder(T holder)
    {
        this.holder = holder;
    }

    public T getHolder()
    {
        return holder;
    }

    public abstract Class<T> getHolderType();

    /**
     * Convenience method for creating a config function. Used to create
     * the
     * default config functions.
     *
     * @param <T>
     * @param clazz
     * @param args
     *              <p/>
     * @return
     */
    public static <T> ConfigFunction<T> create(T holder, Class<? extends ConfigFunction<T>> clazz, Object... args)
    {
        List<String> stringArgs = new ArrayList<String>(args.length);
        for (Object arg : args)
        {
            stringArgs.add("" + arg);
        }

        ConfigFunction<T> configFunction;
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
            TerrainControl.log(LogMarker.FATAL, "Invalid default config function! Please report! {}: {}", 
                    new Object[] { clazz.getName(), e.getMessage() });
            TerrainControl.printStackTrace(LogMarker.FATAL, e);
        }

        return configFunction;
    }

    /**
     * Loads the settings. Will set the internal invalid flag to true
     * if something went wrong.
     * <p/>
     * @param args List of args
     * <p/>
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
     * <p/>
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
     * <p/>
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
     * Loads the settings. Returns false if one of the arguments contains
     * an error.
     * <p>
     * @param args List of args.
     * <p>
     * @throws InvalidConfigException If the syntax is invalid.
     */
    protected abstract void load(List<String> args) throws InvalidConfigException;

    /**
     * Gets a String representation, like Tree(10,BigTree,50,Tree,100)
     * <p/>
     * @return A String representation, like Tree(10,BigTree,50,Tree,100)
     */
    public abstract String makeString();

    /**
     * Parses the string and returns a number between minValue and
     * maxValue.
     * <p/>
     * @param string
     * @param minValue
     * @param maxValue
     * <p/>
     * @return
     * <p/>
     * @throws InvalidConfigException If the number is invalid.
     */
    protected int readInt(String string, int minValue, int maxValue) throws InvalidConfigException
    {
        return StringHelper.readInt(string, minValue, maxValue);
    }

    /**
     * Parses the string and returns a number between minValue and
     * maxValue.
     * <p/>
     * @param string   The string to parse.
     * @param minValue The minimum value.
     * @param maxValue The maximum value.
     * <p/>
     * @return A double between min and max.
     * <p/>
     * @throws InvalidConfigException If the number is invalid.
     */
    protected double readDouble(String string, double minValue, double maxValue) throws InvalidConfigException
    {
        return StringHelper.readDouble(string, minValue, maxValue);
    }

    /**
     * Parses the string and returns the rarity between 0.000001 and 100
     * (inclusive)
     * <p/>
     * @param string The string to parse.
     * <p/>
     * @return The rarity.
     * <p/>
     * @throws InvalidConfigException If the number is invalid.
     */
    protected double readRarity(String string) throws InvalidConfigException
    {
        return StringHelper.readDouble(string, 0.000001, 100);
    }

    /**
     * Returns the material with the given name.
     * @param string Name of the material, case insensitive.
     * @return The material.
     */
    protected LocalMaterialData readMaterial(String string) throws InvalidConfigException
    {
        return TerrainControl.readMaterial(string);
    }

    /**
     * Reads all materials from the start position until the end of the
     * list.
     * <p/>
     * @param strings The input strings.
     * @param start   The position to start. The first element in the list
     *                has index 0, the last one size() - 1.
     * <p/>
     * @return All block ids.
     * <p/>
     * @throws InvalidConfigException If one of the elements in the list is
     *                                not a valid block id.
     */
    protected MaterialSet readMaterials(List<String> strings, int start) throws InvalidConfigException
    {
        MaterialSet materials = new MaterialSet();
        for (int i = start; i < strings.size(); i++)
        {
            materials.parseAndAdd(strings.get(i));
        }

        return materials;
    }

    protected void assureSize(int size, List<String> args) throws InvalidConfigException
    {
        if (args.size() < size)
        {
            throw new InvalidConfigException("Too few arguments supplied");
        }
    }

    /**
     * Returns a String in the format ",materialName,materialName,etc"
     * <p/>
     * @param materials The set of materials to be converted
     * @return
     */
    protected String makeMaterials(MaterialSet materials)
    {
        return "," + materials.toString();
    }

}
