package com.pg85.otg.customobject.bo4;

import java.io.File;

import com.pg85.otg.customobject.CustomObject;
import com.pg85.otg.customobject.CustomObjectLoader;
import com.pg85.otg.customobject.bo4.bo4function.BO4BlockFunction;
import com.pg85.otg.customobject.bo4.bo4function.BO4BranchFunction;
import com.pg85.otg.customobject.bo4.bo4function.BO4EntityFunction;
import com.pg85.otg.customobject.bo4.bo4function.BO4RandomBlockFunction;
import com.pg85.otg.customobject.bo4.bo4function.BO4WeightedBranchFunction;
import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.interfaces.ILogger;

public class BO4Loader implements CustomObjectLoader
{
	public BO4Loader(CustomObjectResourcesManager registry)
	{
		// Register BO4 ConfigFunctions
		registry.registerConfigFunction("Block", BO4BlockFunction.class);
		registry.registerConfigFunction("B", BO4BlockFunction.class);
		registry.registerConfigFunction("Branch", BO4BranchFunction.class);
		registry.registerConfigFunction("BR", BO4BranchFunction.class);
		registry.registerConfigFunction("WeightedBranch", BO4WeightedBranchFunction.class);
		registry.registerConfigFunction("WBR", BO4WeightedBranchFunction.class);
		registry.registerConfigFunction("RandomBlock", BO4RandomBlockFunction.class);
		registry.registerConfigFunction("RB", BO4RandomBlockFunction.class);
		registry.registerConfigFunction("Entity", BO4EntityFunction.class);
		registry.registerConfigFunction("E", BO4EntityFunction.class);
	}
	
	@Override
	public CustomObject loadFromFile(String objectName, File file, ILogger logger)
	{
			return new BO4(objectName, file);
	}

	@Override
	public void onShutdown() { }
}
