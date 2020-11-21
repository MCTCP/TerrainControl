package com.pg85.otg.config.customobjects;

import com.pg85.otg.config.ErroredFunction;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.util.interfaces.ICustomObjectResourcesManager;
import com.pg85.otg.util.interfaces.IMaterialReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomObjectResourcesManager implements ICustomObjectResourcesManager
{
    private Map<String, ArrayList<Class<? extends CustomObjectConfigFunction<?>>>> configFunctions;

    public CustomObjectResourcesManager()
    {
        // Also store in this class
        this.configFunctions = new HashMap<String, ArrayList<Class<? extends CustomObjectConfigFunction<?>>>>();
    }

    public void registerConfigFunction(String name, Class<? extends CustomObjectConfigFunction<?>> value)
    {
    	ArrayList<Class<? extends CustomObjectConfigFunction<?>>> list = configFunctions.get(name.toLowerCase());
    	if(list == null)
    	{
    		list = new ArrayList<Class<? extends CustomObjectConfigFunction<?>>>();
    		configFunctions.put(name.toLowerCase(), list);
    	}
    	list.add(value);
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
    public <T> CustomObjectConfigFunction<T> getConfigFunction(String name, T holder, List<String> args, boolean spawnLog, ILogger logger, IMaterialReader materialReader)
    {
    	// If a Block() tag has the parameters of a RandomBlock tag then transform it into a RandomBlock
    	// This allows users to edit Bo3's and change Blocks to RandomBlocks with a simple find/replace.
    	if(name.toLowerCase().trim().equals("block") && args.size() > 5)
    	{
    		name = "RandomBlock";
    	}

        // Get the class of the config function
        ArrayList<Class<? extends CustomObjectConfigFunction<?>>> clazzes = configFunctions.get(name.toLowerCase());
        if (clazzes == null)
        {
            return new CustomObjectErroredFunction<T>(name, holder, args, "Resource type " + name + " not found");
        }

        // Get a config function
        CustomObjectConfigFunction<T> configFunction = null;
        for(Class<? extends CustomObjectConfigFunction<?>> clazz : clazzes)
        {
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
	        	continue;
	        }
	
	        // Initialize the function
	        try
	        {
	            configFunction.init(holder, args, spawnLog, logger, materialReader);
	        } catch (InvalidConfigException e)
	        {
	            configFunction.invalidate(name, args, e.getMessage());
	        }
	        break;
        }
        if(configFunction == null)
        {
        	return new CustomObjectErroredFunction<T>(name, holder, args, "Resource " + name + " cannot be placed in this config file");
        }
        return configFunction;
    }
}
