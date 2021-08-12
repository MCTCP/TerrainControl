package com.pg85.otg.customobject.bo3;

import com.pg85.otg.customobject.CustomObject;
import com.pg85.otg.customobject.CustomObjectLoader;
import com.pg85.otg.customobject.bo3.bo3function.BO3BlockFunction;
import com.pg85.otg.customobject.bo3.bo3function.BO3BranchFunction;
import com.pg85.otg.customobject.bo3.bo3function.BO3EntityFunction;
import com.pg85.otg.customobject.bo3.bo3function.BO3MinecraftObjectFunction;
import com.pg85.otg.customobject.bo3.bo3function.BO3RandomBlockFunction;
import com.pg85.otg.customobject.bo3.bo3function.BO3WeightedBranchFunction;
import com.pg85.otg.customobject.bo3.checks.BlockCheck;
import com.pg85.otg.customobject.bo3.checks.BlockCheckAbsolute;
import com.pg85.otg.customobject.bo3.checks.BlockCheckAbsoluteNot;
import com.pg85.otg.customobject.bo3.checks.BlockCheckNot;
import com.pg85.otg.customobject.bo3.checks.LightCheck;
import com.pg85.otg.customobject.bo3.checks.ModCheck;
import com.pg85.otg.customobject.bo3.checks.ModCheckNot;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.interfaces.ILogger;

import java.io.*;

public class BO3Loader implements CustomObjectLoader
{
	public BO3Loader(CustomObjectResourcesManager registry)
	{
		// Register BO3 ConfigFunctions
		registry.registerConfigFunction("Block", BO3BlockFunction.class);
		registry.registerConfigFunction("B", BO3BlockFunction.class);
		registry.registerConfigFunction("Branch", BO3BranchFunction.class);
		registry.registerConfigFunction("BR", BO3BranchFunction.class);
		registry.registerConfigFunction("WeightedBranch", BO3WeightedBranchFunction.class);
		registry.registerConfigFunction("WBR", BO3WeightedBranchFunction.class);
		registry.registerConfigFunction("RandomBlock", BO3RandomBlockFunction.class);
		registry.registerConfigFunction("RB", BO3RandomBlockFunction.class);
		registry.registerConfigFunction("MinecraftObject", BO3MinecraftObjectFunction.class);
		registry.registerConfigFunction("MCO", BO3MinecraftObjectFunction.class);
		registry.registerConfigFunction("BlockCheck", BlockCheck.class);
		registry.registerConfigFunction("BC", BlockCheck.class);
		registry.registerConfigFunction("BlockCheckNot", BlockCheckNot.class);
		registry.registerConfigFunction("BCN", BlockCheckNot.class);
		registry.registerConfigFunction("BlockCheckAbsolute", BlockCheckAbsolute.class);
		registry.registerConfigFunction("BCA", BlockCheckAbsolute.class);
		registry.registerConfigFunction("BlockCheckAbsoluteNot", BlockCheckAbsoluteNot.class);
		registry.registerConfigFunction("BCAN", BlockCheckAbsoluteNot.class);
		registry.registerConfigFunction("LightCheck", LightCheck.class);
		registry.registerConfigFunction("LC", LightCheck.class);
		registry.registerConfigFunction("Entity", BO3EntityFunction.class);
		registry.registerConfigFunction("E", BO3EntityFunction.class);
		registry.registerConfigFunction("ModCheck", ModCheck.class);
		registry.registerConfigFunction("MC", ModCheck.class);
		registry.registerConfigFunction("ModCheckNot", ModCheckNot.class);
		registry.registerConfigFunction("MCN", ModCheckNot.class);
	}

	@Override
	public CustomObject loadFromFile(String objectName, File file, ILogger logger)
	{
		return new BO3(objectName, file);
	}
}
