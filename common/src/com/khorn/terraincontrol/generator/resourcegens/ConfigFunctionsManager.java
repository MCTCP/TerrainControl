package com.khorn.terraincontrol.generator.resourcegens;

import java.util.List;
import java.util.Map;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.ConfigFunction;
import com.khorn.terraincontrol.exception.InvalidResourceException;

public class ConfigFunctionsManager
{
    private Map<String, Class<? extends ConfigFunction>> configFunctions;

    public ConfigFunctionsManager(Map<String, Class<? extends ConfigFunction>> configFunctions)
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

    public void put(String name, Class<? extends ConfigFunction> value)
    {
        configFunctions.put(name.toLowerCase(), value);
    }

    /**
     * Creates a ConfigFunction with the specified name.
     * 
     * @param name
     *            Name of the resource, like Ore
     * @param args
     *            String representation of the args.
     * @return The resource, or null of no matching resource could be found.
     */
    public ConfigFunction getConfigFunction(String name, BiomeConfig biomeConfig, List<String> args)
    {
        if (configFunctions.containsKey(name.toLowerCase()))
        {
            ConfigFunction configFunction;
            try
            {
                configFunction = configFunctions.get(name.toLowerCase()).newInstance();
            } catch (InstantiationException e)
            {
                TerrainControl.log("Reflection error while loading the resources: " + e.getMessage());
                e.printStackTrace();
                return null;
            } catch (IllegalAccessException e)
            {
                TerrainControl.log("Reflection error while loading the resources: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
            configFunction.setWorldConfig(biomeConfig.worldConfig);
            try
            {
                configFunction.load(args);
            } catch (InvalidResourceException e)
            {
                TerrainControl.log("Invalid resource " + name + " in " + biomeConfig.Name + ": " + e.getMessage());
                return null;
            }

            return configFunction;

        }

        return null;
    }

}
