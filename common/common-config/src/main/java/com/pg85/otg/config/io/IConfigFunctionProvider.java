package com.pg85.otg.config.io;

import java.util.List;
import com.pg85.otg.config.ConfigFunction;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;

public interface IConfigFunctionProvider
{
	public <T> ConfigFunction<T> getConfigFunction(String name, T holder, List<String> args, ILogger logger, IMaterialReader materialReader);
}
