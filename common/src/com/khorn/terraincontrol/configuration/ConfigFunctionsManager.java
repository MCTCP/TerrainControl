package com.khorn.terraincontrol.configuration;

import java.util.List;
import java.util.Map;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.exception.InvalidResourceException;
import com.khorn.terraincontrol.generator.resourcegens.AboveWaterGen;
import com.khorn.terraincontrol.generator.resourcegens.CactusGen;
import com.khorn.terraincontrol.generator.resourcegens.CustomObjectGen;
import com.khorn.terraincontrol.generator.resourcegens.DungeonGen;
import com.khorn.terraincontrol.generator.resourcegens.GrassGen;
import com.khorn.terraincontrol.generator.resourcegens.LiquidGen;
import com.khorn.terraincontrol.generator.resourcegens.OreGen;
import com.khorn.terraincontrol.generator.resourcegens.PlantGen;
import com.khorn.terraincontrol.generator.resourcegens.ReedGen;
import com.khorn.terraincontrol.generator.resourcegens.SaplingGen;
import com.khorn.terraincontrol.generator.resourcegens.SmallLakeGen;
import com.khorn.terraincontrol.generator.resourcegens.TreeGen;
import com.khorn.terraincontrol.generator.resourcegens.UnderWaterOreGen;
import com.khorn.terraincontrol.generator.resourcegens.UndergroundLakeGen;
import com.khorn.terraincontrol.generator.resourcegens.VinesGen;

public class ConfigFunctionsManager
{
    private Map<String, Class<? extends ConfigFunction<?>>> configFunctions;

    public ConfigFunctionsManager(Map<String, Class<? extends ConfigFunction<?>>> configFunctions)
    {
        // Also store in this class
        this.configFunctions = configFunctions;

        // Add vanilla resources
        put("AboveWaterRes", AboveWaterGen.class);
        put("Cactus", CactusGen.class);
        put("CustomObject", CustomObjectGen.class);
        put("Dungeon", DungeonGen.class);
        put("Grass", GrassGen.class);
        put("Liquid", LiquidGen.class);
        put("Ore", OreGen.class);
        put("Plant", PlantGen.class);
        put("Reed", ReedGen.class);
        put("Sapling", SaplingGen.class);
        put("SmallLake", SmallLakeGen.class);
        put("Tree", TreeGen.class);
        put("UndergroundLake", UndergroundLakeGen.class);
        put("UnderWaterOre", UnderWaterOreGen.class);
        put("Vines", VinesGen.class);
    }

    public void put(String name, Class<? extends ConfigFunction<?>> value)
    {
        configFunctions.put(name.toLowerCase(), value);
    }

    /**
     * Returns a config function with the given name.
     * 
     * @param name
     *            The name of the config function.
     * @param holder
     *            The holder of the config function. WorldConfig or BO3.
     * @param locationOfResource
     *            The location of the config function, for example
     *            TaigaBiomeConfig.ini.
     * @param args
     *            The args of the function.
     * @return A config function with the given name, or null of it wasn't
     *         found.
     */
    @SuppressWarnings("unchecked")
    // It's checked with if (!clazz.isAssignableFrom(holder.getClass()))
    public <T> ConfigFunction<T> getConfigFunction(String name, T holder, String locationOfResource, List<String> args)
    {
        // Check if config function exists
        if (!configFunctions.containsKey(name.toLowerCase()))
        {
            TerrainControl.log("Invalid resource " + name + " in " + locationOfResource + ": resource type not found!");
            return null;
        }

        ConfigFunction<?> configFunction;
        Class<? extends ConfigFunction<?>> clazz = configFunctions.get(name.toLowerCase());

        // Get a config function
        try
        {
            configFunction = (ConfigFunction<?>) clazz.newInstance();
        } catch (InstantiationException e)
        {
            TerrainControl.log("Reflection error (Instantiation) while loading the resources: " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (IllegalAccessException e)
        {
            TerrainControl.log("Reflection error (IllegalAccess) while loading the resources: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        
        // Check if config function is of the right type
        boolean matchingTypes = true;
        try
        {
            matchingTypes = holder.getClass().isAssignableFrom((Class<?>) clazz.getMethod("getType").invoke(configFunction));
        } catch (Exception e)
        {
            TerrainControl.log("Reflection error (" + e.getClass().getSimpleName() + ") while loading the resources: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        if (!matchingTypes)
        {
            TerrainControl.log("Invalid resource " + name + " in " + locationOfResource + ": cannot be placed in this config file!");
            return null;
        }

        // Set the holder
        configFunction.setHolder(holder);

        // Load it
        try
        {
            configFunction.load(args);
        } catch (InvalidResourceException e)
        {
            TerrainControl.log("Invalid resource " + name + " in " + locationOfResource + ": " + e.getMessage());
            return null;
        }

        // Return it
        return (ConfigFunction<T>) configFunction;
    }

}
