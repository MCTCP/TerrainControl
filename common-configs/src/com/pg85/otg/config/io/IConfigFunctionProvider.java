package com.pg85.otg.config.io;

import java.util.List;
import com.pg85.otg.config.ConfigFunction;

public interface IConfigFunctionProvider
{
	public <T> ConfigFunction<T> getConfigFunction(String name, T holder, List<String> args);
}
