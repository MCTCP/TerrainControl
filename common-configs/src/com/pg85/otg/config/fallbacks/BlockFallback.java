package com.pg85.otg.config.fallbacks;

import java.util.ArrayList;
import java.util.List;

import com.pg85.otg.config.ConfigFunction;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.util.interfaces.IMaterialReader;

public class BlockFallback extends ConfigFunction<FallbackConfig>
{	
	public String materialFrom;
	public List<String> materialsTo;

	public BlockFallback(FallbackConfig fallbackConfig, List<String> args, ILogger logger, IMaterialReader materialReader)
	{
		try {
			assureSize(2, args);
		} catch (InvalidConfigException e) {
			// TODO Print proper error
			e.printStackTrace();
		}

		materialFrom = args.get(0);
		materialsTo = new ArrayList<String>();

		for (int i = 1; i < args.size(); i++)
		{
			materialsTo.add(args.get(i));
		}
	}

	@Override
	public boolean isAnalogousTo(ConfigFunction<FallbackConfig> other, ILogger logger)
	{
		return other.getClass().equals(getClass());
	}

	@Override
	public String toString()
	{
		String start = "BlockFallback(" + materialFrom;
		for (String s : materialsTo)
		{
			start += "," + s;
		}
		start += ")";
		return start;
	}

}
