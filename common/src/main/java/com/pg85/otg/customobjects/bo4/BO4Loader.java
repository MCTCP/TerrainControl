package com.pg85.otg.customobjects.bo4;

import java.io.File;

import com.pg85.otg.OTG;
import com.pg85.otg.config.customobjects.CustomObjectResourcesManager;
import com.pg85.otg.customobjects.CustomObject;
import com.pg85.otg.customobjects.CustomObjectLoader;
import com.pg85.otg.customobjects.bo4.bo4function.BO4BlockFunction;
import com.pg85.otg.customobjects.bo4.bo4function.BO4BranchFunction;
import com.pg85.otg.customobjects.bo4.bo4function.BO4EntityFunction;
import com.pg85.otg.customobjects.bo4.bo4function.BO4ModDataFunction;
import com.pg85.otg.customobjects.bo4.bo4function.BO4ParticleFunction;
import com.pg85.otg.customobjects.bo4.bo4function.BO4RandomBlockFunction;
import com.pg85.otg.customobjects.bo4.bo4function.BO4SpawnerFunction;
import com.pg85.otg.customobjects.bo4.bo4function.BO4WeightedBranchFunction;

public class BO4Loader implements CustomObjectLoader
{
    public BO4Loader()
    {
        // Register BO4 ConfigFunctions
        CustomObjectResourcesManager registry = OTG.getCustomObjectResourcesManager();
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
        registry.registerConfigFunction("Particle", BO4ParticleFunction.class);
        registry.registerConfigFunction("P", BO4ParticleFunction.class);
        registry.registerConfigFunction("Spawner", BO4SpawnerFunction.class);
        registry.registerConfigFunction("S", BO4SpawnerFunction.class);
        registry.registerConfigFunction("ModData", BO4ModDataFunction.class);
        registry.registerConfigFunction("MD", BO4ModDataFunction.class);
    }
    
    @Override
    public CustomObject loadFromFile(String objectName, File file)
    {
   		return new BO4(objectName, file);
    }

    @Override
    public void onShutdown() { }
}
