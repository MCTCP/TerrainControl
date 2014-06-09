package com.khorn.terraincontrol.configuration;

import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.generator.resource.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigFunctionsManager
{

    private Map<String, Class<? extends ConfigFunction<?>>> configFunctions;

    public ConfigFunctionsManager()
    {
        // Also store in this class
        this.configFunctions = new HashMap<String, Class<? extends ConfigFunction<?>>>();

        // Add vanilla resources
        registerConfigFunction("AboveWaterRes", AboveWaterGen.class);
        registerConfigFunction("Boulder", BoulderGen.class);
        registerConfigFunction("Cactus", CactusGen.class);
        registerConfigFunction("CustomObject", CustomObjectGen.class);
        registerConfigFunction("CustomStructure", CustomStructureGen.class);
        registerConfigFunction("Dungeon", DungeonGen.class);
        registerConfigFunction("Grass", GrassGen.class);
        registerConfigFunction("IceSpike", IceSpikeGen.class);
        registerConfigFunction("Liquid", LiquidGen.class);
        registerConfigFunction("Ore", OreGen.class);
        registerConfigFunction("Plant", PlantGen.class);
        registerConfigFunction("Reed", ReedGen.class);
        registerConfigFunction("Sapling", SaplingGen.class);
        registerConfigFunction("SmallLake", SmallLakeGen.class);
        registerConfigFunction("Tree", TreeGen.class);
        registerConfigFunction("UndergroundLake", UndergroundLakeGen.class);
        registerConfigFunction("UnderWaterOre", UnderWaterOreGen.class);
        registerConfigFunction("Vein", VeinGen.class);
        registerConfigFunction("Vines", VinesGen.class);
        registerConfigFunction("Well", WellGen.class);
    }

    public void registerConfigFunction(String name, Class<? extends ConfigFunction<?>> value)
    {
        configFunctions.put(name.toLowerCase(), value);
    }

    /**
     * Returns a config function with the given name.
     * <p/>
     * @param name               The name of the config function.
     * @param holder             The holder of the config function.
     *                           WorldConfig or BO3.
     * @param locationOfResource The location of the config function, for
     *                           example TaigaBiomeConfig.ini.
     * @param args               The args of the function.
     * <p/>
     * @return A config function with the given name, or null of it wasn't
     *         found.
     */
    @SuppressWarnings("unchecked")
    // It's checked with if (!clazz.isAssignableFrom(holder.getClass()))
    public <T> ConfigFunction<T> getConfigFunction(String name, T holder, List<String> args)
    {
        // Get the class of the config function
        Class<? extends ConfigFunction<?>> clazz = configFunctions.get(name.toLowerCase());
        if (clazz == null)
        {
            return new ErroredFunction<T>(name, holder, args, "Resource type " + name + " not found");
        }

        // Get a config function
        ConfigFunction<T> configFunction;
        try
        {
            configFunction = (ConfigFunction<T>) clazz.newInstance();
        } catch (Exception e)
        {
            throw new RuntimeException("Reflection error while loading the resources: ", e);
        }

        // Check if config function is of the right type
        boolean matchingTypes = holder.getClass().isAssignableFrom(configFunction.getHolderType());
        if (!matchingTypes)
        {
            return new ErroredFunction<T>(name, holder, args, "Resource " + name + " cannot be placed in this config file");
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
