package com.khorn.terraincontrol.configuration;

import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.generator.resource.ReedGen;
import com.khorn.terraincontrol.util.MaterialSet;
import com.khorn.terraincontrol.util.helpers.StringHelper;

import java.util.List;

/**
 * A config function resides in a {@link ConfigFile}. In such a file, it looks
 * like a function call: {@code FunctionName(arg1,arg2,...)}.
 *
 * <p>In addition to implementing the abstract methods, each non-abstract
 * subclass must have a constructor that takes exactly two args. The first one
 * is for the instance of the config file that contains the function (called
 * "the holder"), the second one is a {@code List<String>} that contains the
 * arguments. The constructor must then parse the arguments. When it encounters
 * an invalid argument, it must throw a {@link InvalidConfigException}.
 *
 * <p>See {@link ReedGen} for an example of a such a constructor.
 *
 * @param <T> The holder type (type of the class that contains the
 * {@link ConfigFunction}).
 */
public abstract class ConfigFunction<T>
{
    private final T holder;

    public ConfigFunction(T holder)
    {
        this.holder = holder;
    }

    /**
     * Checks the size of the given list.
     * @param size The minimum size of the list.
     * @param args The list to check.
     * @throws InvalidConfigException If the size of the list is small than
     * the given size.
     */
    protected final void assureSize(int size, List<String> args) throws InvalidConfigException
    {
        if (args.size() < size)
        {
            throw new InvalidConfigException("Too few arguments supplied");
        }
    }

    /**
     * Gets the holder of this config function.
     * @return The holder.
     */
    protected final T getHolder()
    {
        return holder;
    }

    /**
     * Returns whether or not the two resources are similar to each other AND
     * not equal. This should return true if two resources are of the same class
     * and if critical element are the same. For example source blocks. This 
     * will be used to test if a resource should be overridden via inheritance.
     * @param other The resource to compare with.
     * @return True if the resources are similar, false otherwise.
     */
    public abstract boolean isAnalogousTo(ConfigFunction<T> other);

    /**
     * Formats the material list as a string list.
     * @param materials The set of materials to be converted
     * @return A string in the format ",materialName,materialName,etc"
     */
    protected final String makeMaterials(MaterialSet materials)
    {
        return "," + materials.toString();
    }

    /**
     * Gets a String representation, like Tree(10,BigTree,50,Tree,100).
     * @return A String representation, like Tree(10,BigTree,50,Tree,100)
     */
    @Override
    public abstract String toString();

    /**
     * Parses the string and returns a number between minValue and
     * maxValue.
     * @param string   The string to parse.
     * @param minValue The minimum value.
     * @param maxValue The maximum value.
     * @return A double between min and max.
     * @throws InvalidConfigException If the number is invalid.
     */
    protected final double readDouble(String string, double minValue, double maxValue) throws InvalidConfigException
    {
        return StringHelper.readDouble(string, minValue, maxValue);
    }

    /**
     * Parses the string and returns a number between minValue and
     * maxValue.
     * @param string   The string to be parsed as an int.
     * @param minValue The minimum allowed value.
     * @param maxValue The maximum allowed value.
     * @return The int, between min and max (inclusive).
     * @throws InvalidConfigException If the number is invalid.
     */
    protected final int readInt(String string, int minValue, int maxValue) throws InvalidConfigException
    {
        return StringHelper.readInt(string, minValue, maxValue);
    }

    /**
     * Returns the material with the given name.
     * @param string Name of the material, case insensitive.
     * @return The material.
     * @throws InvalidConfigException If no material exists with the given name.
     */
    protected final LocalMaterialData readMaterial(String string) throws InvalidConfigException
    {
        return TerrainControl.readMaterial(string);
    }

    /**
     * Reads all materials from the start position until the end of the
     * list.
     * @param strings The input strings.
     * @param start   The position to start. The first element in the list
     *                has index 0, the last one size() - 1.
     * @return All block ids.
     * @throws InvalidConfigException If one of the elements in the list is
     *                                not a valid block id.
     */
    protected final MaterialSet readMaterials(List<String> strings, int start) throws InvalidConfigException
    {
        MaterialSet materials = new MaterialSet();
        for (int i = start; i < strings.size(); i++)
        {
            materials.parseAndAdd(strings.get(i));
        }

        return materials;
    }

    /**
     * Parses the string and returns the rarity between 0.000001 and 100
     * (inclusive)
     * @param string The string to parse.
     * @return The rarity.
     * @throws InvalidConfigException If the number is invalid.
     */
    protected final double readRarity(String string) throws InvalidConfigException
    {
        return StringHelper.readDouble(string, 0.000001, 100);
    }

}
