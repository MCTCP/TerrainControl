package com.pg85.otg.configuration.customobjects;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.helpers.StringHelper;
import com.pg85.otg.util.materials.MaterialHelper;
import com.pg85.otg.util.materials.MaterialSet;

import java.util.ArrayList;
import java.util.List;

public abstract class CustomObjectConfigFunction<T>
{
    public int x;
    public int z;
	
    protected T holder;
	
    /**
     * Has a value when valid == false, otherwise null.
     */
    protected String error;

    /**
     * Only has a value when {@link #invalidate(String, List, String)} is
     * called.
     */
    protected List<String> inputArgs;
    /**
     * Only has a value when {@link #invalidate(String, List, String)} is
     * called.
     */
    protected String inputName;
    protected boolean valid = true;
	
    /**
     * Convenience method for creating a config function. Used to create
     * the default config functions.
     *
     * @param <T>
     * @param clazz
     * @param args
     * @return
     */
    public static final <T> CustomObjectConfigFunction<T> create(T holder, Class<? extends CustomObjectConfigFunction<T>> clazz, Object... args)
    {
        List<String> stringArgs = new ArrayList<String>(args.length);
        for (Object arg : args)
        {
            stringArgs.add("" + arg);
        }

        CustomObjectConfigFunction<T> configFunction;
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
            OTG.log(LogMarker.FATAL, "Invalid default config function! Please report! {}: {}",
                    clazz.getName(), e.getMessage());
            OTG.printStackTrace(LogMarker.FATAL, e);
        }

        return configFunction;
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
     * Gets the error that occurred while reading this resource.
     * @return The error.
     * @throws IllegalStateException If the object {@link #isValid() is
     * valid}, so no error occurred.
     */
    public final String getError() throws IllegalStateException
    {
        if (isValid())
        {
            throw new IllegalStateException("Function is valid, so no error");
        }
        return error;
    }

    /**
     * Gets the holder of this config function.
     * @return The holder.
     */
    public final T getHolder()
    {
        return holder;
    }

    /**
     * Gets the class of the holder. The {@link #getHolder()holder of this
     * resource} will be an instance of this type. Multiple invocations of
     * this method on the same instance must always yield the same result.
     *
     * <p>This method is intended to combat Java's type erasure: it provides
     * access to the type parameter T.
     * @return The class.
     */
    public abstract Class<T> getHolderType();

    /**
     * Initializes the function: the holder is set and the arguments are read.
     * @param holder The holder to set. Must be of the type returned by
     *               {@link #getHolderType()}.
     * @param args   Arguments to parse.
     * @throws InvalidConfigException If the arguments are invalid.
     */
    final void init(T holder, List<String> args) throws InvalidConfigException
    {
        this.holder = holder;
        load(args);
    }

    /**
     * Invalidates this resource.
     * @param name  Name of this resource, for output.
     * @param args  Arguments used in this resource, for output.
     * @param error Error message detailing what went wrong.
     */
    final void invalidate(String name, List<String> args, String error)
    {
        valid = false;
        this.inputName = name;
        this.inputArgs = args;
        this.error = error;
    }

    /**
     * Returns whether or not the two resources are similar to each other AND
     * not equal. This should return true if two resources are of the same class
     * and if critical element are the same. For example source blocks. This
     * will be used to test if a resource should be overridden via inheritance.
     * @return
     */
    public abstract boolean isAnalogousTo(CustomObjectConfigFunction<T> other);

    /**
     * Returns true if this ConfigFunction has a correct syntax.
     * Returns false if the read method hasn't been called yet,
     * or if the function has an incorrect syntax.
     * <p/>
     * @return Whether this ConfigFunction has a correct syntax.
     */
    public final boolean isValid()
    {
        return valid;
    }

    /**
     * Parses the arguments. {@link #setHolder(Object)} must be called prior
     * to calling this method, as this method is allowed to use
     * {@link #getHolder()}.
     * @param args The arguments to parse.
     * @throws InvalidConfigException If the syntax is invalid.
     */
    protected abstract void load(List<String> args) throws InvalidConfigException;

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
     * Gets a String representation, like Tree(10,BigTree,50,Tree,100)
     * @return A String representation, like Tree(10,BigTree,50,Tree,100)
     */
    public abstract String makeString();

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
     * <p/>
     * @param string
     * @param minValue
     * @param maxValue
     * <p/>
     * @return
     * <p/>
     * @throws InvalidConfigException If the number is invalid.
     */
    protected final int readInt(String string, int minValue, int maxValue) throws InvalidConfigException
    {
        return StringHelper.readInt(string, minValue, maxValue);
    }

    /**
     * Parses the string and returns the boolean or false if no value could be found.
     */
    protected final boolean readBoolean(String string)
    {
    	return Boolean.parseBoolean(string);
    }

    /**
     * Returns the material with the given name.
     * @param string Name of the material, case insensitive.
     * @return The material.
     */
    protected final LocalMaterialData readMaterial(String string) throws InvalidConfigException
    {
        return MaterialHelper.readMaterial(string);
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
     * Sets the holder to the given parameter. Must only be used when manually
     * constructing this function. The holder must of the type returned by
     * {@link #getHolderType()}.
     * @param holder The hoilder.
     * @see #init(Object, List).
     */
    public final void setHolder(T holder)
    {
        this.holder = holder;
    }

    /**
     * @deprecated Use {@link #invalidate(String, List, String)} to invalidate
     * the object. Manually validating an object is no longer needed.
     * Re-validating is no longer possible, just create a new instance.
     */
    @Deprecated
    public final void setValid(boolean valid)
    {
        if (valid == false)
        {
            throw new UnsupportedOperationException("Use the invalidate method");
        }
        if (valid == true && !isValid())
        {
            throw new UnsupportedOperationException("Revalidating objects is no longer supported");
        }
        // So (valid == true && isValid()), so it's safe to do nothing
    }

    public final String write()
    {
        if (!valid)
        {
            // Show error message
            return "## INVALID " + inputName.toUpperCase() + " - " + error + " ##" + System.getProperty("line.separator") + inputName + "("
                    + StringHelper.join(inputArgs, ",") + ")";
        } else
        {
            return makeString();
        }
    }

}
