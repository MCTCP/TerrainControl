package com.khorn.terraincontrol.generator.resourcegens;

import java.util.List;
import java.util.Map;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.Resource;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.exception.InvalidResourceException;

public class ResourcesManager
{
    private Map<String, Class<? extends Resource>> resourceTypes;

    public ResourcesManager(Map<String, Class<? extends Resource>> resourceTypes)
    {
        // Also store in this class
        this.resourceTypes = resourceTypes;

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

    public void put(String name, Class<? extends Resource> value)
    {
        resourceTypes.put(name.toLowerCase(), value);
    }

    /**
     * Creates a resource with the specified name.
     * 
     * @param name
     *            Name of the resource, like Ore
     * @param args
     *            String representation of the args.
     * @return The resource, or null of no matching resource could be found.
     */
    public Resource getResource(String name, BiomeConfig biomeConfig, List<String> args)
    {
        if (resourceTypes.containsKey(name.toLowerCase()))
        {
            Resource resource;
            try
            {
                resource = resourceTypes.get(name.toLowerCase()).newInstance();
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
            resource.setWorldConfig(biomeConfig.worldConfig);
            try
            {
                resource.load(args);
            } catch (InvalidResourceException e)
            {
                TerrainControl.log("Invalid resource " + name + " in " + biomeConfig.Name + ": " + e.getMessage());
                return null;
            }

            return resource;

        }

        return null;
    }

}
