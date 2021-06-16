package com.pg85.otg.config.biome;

import java.util.ArrayList;
import java.util.List;

import com.pg85.otg.config.ConfigFunction;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.util.interfaces.IBiomeConfig;
import com.pg85.otg.util.interfaces.IMaterialReader;

/**
 * Represents a BiomeConfig ResourceQueue resource.
 */
public abstract class ResourceBase extends ConfigFunction<IBiomeConfig>
{
	static ResourceBase createResource(IBiomeConfig config, ILogger logger, IMaterialReader materialReader, Class<? extends ResourceBase> clazz, Object... args)
	{
		List<String> stringArgs = new ArrayList<String>(args.length);
		for (Object arg : args)
		{
			stringArgs.add("" + arg);
		}

		try
		{
			return clazz.getConstructor(IBiomeConfig.class, List.class, ILogger.class, IMaterialReader.class).newInstance(config, stringArgs, logger, materialReader);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	// Children must implement this constructor, or createResource will fail
	public ResourceBase(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) { }
}
