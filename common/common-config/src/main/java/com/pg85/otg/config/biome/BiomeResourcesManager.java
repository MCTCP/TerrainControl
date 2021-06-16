package com.pg85.otg.config.biome;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.pg85.otg.config.ConfigFunction;
import com.pg85.otg.config.ErroredFunction;
import com.pg85.otg.config.io.IConfigFunctionProvider;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.util.interfaces.IBiomeConfig;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IWorldConfig;

public class BiomeResourcesManager implements IConfigFunctionProvider
{
	private Map<String, Class<? extends ConfigFunction<?>>> configFunctions;

	public BiomeResourcesManager(Map<String, Class<? extends ConfigFunction<?>>> configFunctions)
	{
		// Also store in this class
		this.configFunctions = new HashMap<String, Class<? extends ConfigFunction<?>>>();

		for(Entry<String, Class<? extends ConfigFunction<?>>> resource : configFunctions.entrySet())
		{
			registerConfigFunction(resource.getKey(), resource.getValue());
		}
	}

	private void registerConfigFunction(String name, Class<? extends ConfigFunction<?>> value)
	{
		configFunctions.put(name.toLowerCase(), value);
	}

	/**
	 * Returns a config function with the given name.
	 * @param <T>	Type of the holder of the config function.
	 * @param name	The name of the config function.
	 * @param holder The holder of the config function, like
	 *				{@link WorldConfig}.
	 * @param args	The args of the function.
	 * @return A config function with the given name, or null if the config
	 * function requires another holder. For invalid or non-existing config
	 * functions, it returns an instance of {@link ErroredFunction}.
	 */
	// It's checked with clazz.getConstructor(holder.getClass(), ...))
	@SuppressWarnings("unchecked")
	public <T> ConfigFunction<T> getConfigFunction(String name, T holder, List<String> args, ILogger logger, IMaterialReader materialReader)
	{
		// Get the class of the config function
		Class<? extends ConfigFunction<?>> clazz = configFunctions.get(name.toLowerCase());
		if (clazz == null)
		{
			return new ErroredFunction<T>(name, args, "Resource type " + name + " not found");
		}

		// Get a config function
		try
		{
			Constructor<? extends ConfigFunction<?>> constructor = null;
			if(holder instanceof IBiomeConfig)
			{
				constructor = clazz.getConstructor(IBiomeConfig.class, List.class, ILogger.class, IMaterialReader.class);
			}
			else if(holder instanceof IWorldConfig)
			{
				constructor = clazz.getConstructor(IWorldConfig.class, List.class, ILogger.class, IMaterialReader.class);				
			}
			return (ConfigFunction<T>) constructor.newInstance(holder, args, logger, materialReader);
		}
		catch (NoSuchMethodException e1)
		{
			// Probably uses another holder type
			return null;
		}
		catch (InstantiationException e)
		{
			throw new RuntimeException(e);
		}
		catch (IllegalAccessException e)
		{
			throw new RuntimeException(e);
		}
		catch (InvocationTargetException e)
		{
			Throwable cause = e.getCause();
			if (cause instanceof InvalidConfigException)
			{
				return new ErroredFunction<T>(name, args, cause.getMessage());
			}
			throw new RuntimeException(e);
		}
	}
}
