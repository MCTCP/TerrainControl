package com.pg85.otg.configuration.customobjects;

import com.pg85.otg.configuration.ErroredFunction;
import com.pg85.otg.configuration.world.WorldConfig;
import com.pg85.otg.exception.InvalidConfigException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomObjectConfigFunctionsManager
{

    private Map<String, Class<? extends CustomObjectConfigFunction<?>>> configFunctions;

    public CustomObjectConfigFunctionsManager()
    {
        // Also store in this class
        this.configFunctions = new HashMap<String, Class<? extends CustomObjectConfigFunction<?>>>();
    }

    public void registerConfigFunction(String name, Class<? extends CustomObjectConfigFunction<?>> value)
    {
        configFunctions.put(name.toLowerCase(), value);
    }

    /**
     * Returns a config function with the given name.
     * @param <T>    Type of the holder of the config function.
     * @param name   The name of the config function.
     * @param holder The holder of the config function, like
     *               {@link WorldConfig}.
     * @param args   The args of the function.
     * @return A config function with the given name, or null if the config
     * function requires another holder. For invalid or non-existing config
     * functions, it returns an instance of {@link ErroredFunction}.
     */
    @SuppressWarnings("unchecked")
    // It's checked with clazz.getConstructor(holder.getClass(), ...))
    public <T> CustomObjectConfigFunction<T> getConfigFunction(String name, T holder, List<String> args)
    {
    	// If a Block() tag has the parameters of a RandomBlock tag then transform it into a RandomBlock
    	// This allows users to edit Bo3's and change Blocks to RandomBlocks with a simple find/replace.
    	if(name.toLowerCase().trim().equals("block") && args.size() > 5)
    	{
    		name = "RandomBlock";
    	}

        // Get the class of the config function
        Class<? extends CustomObjectConfigFunction<?>> clazz = configFunctions.get(name.toLowerCase());
        if (clazz == null)
        {
            return new CustomObjectErroredFunction<T>(name, holder, args, "Resource type " + name + " not found");
        }

        // Get a config function
        CustomObjectConfigFunction<T> configFunction;
        try
        {
            configFunction = (CustomObjectConfigFunction<T>) clazz.newInstance();
        } catch (Exception e)
        {
            throw new RuntimeException("Reflection error while loading the resources: ", e);
        }

        // Check if config function is of the right type
        boolean matchingTypes = holder.getClass().isAssignableFrom(configFunction.getHolderType());
        if (!matchingTypes)
        {
            return new CustomObjectErroredFunction<T>(name, holder, args, "Resource " + name + " cannot be placed in this config file");
        }

        // Initialize the function
        try
        {
            configFunction.init(holder, args);
        } catch (InvalidConfigException e)
        {
            configFunction.invalidate(name, args, e.getMessage());
        }
        return configFunction;
    }
}
