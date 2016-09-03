package com.khorn.terraincontrol.configuration;

import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.generator.resource.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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

        // Functions in WorldConfigs
        registerConfigFunction("BiomeGroup", BiomeGroup.class);

        // Functions in BiomeConfigs
        registerConfigFunction("AboveWaterRes", AboveWaterGen.class);
        registerConfigFunction("Boulder", BoulderGen.class);
        registerConfigFunction("Cactus", CactusGen.class);
        registerConfigFunction("CustomObject", CustomObjectGen.class);
        registerConfigFunction("CustomStructure", CustomStructureGen.class);
        registerConfigFunction("Dungeon", DungeonGen.class);
        registerConfigFunction("Grass", GrassGen.class);
        registerConfigFunction("Fossil", FossilGen.class);
        registerConfigFunction("IceSpike", IceSpikeGen.class);
        registerConfigFunction("Liquid", LiquidGen.class);
        registerConfigFunction("Ore", OreGen.class);
        registerConfigFunction("Plant", PlantGen.class);
        registerConfigFunction("Reed", ReedGen.class);
        registerConfigFunction("Sapling", SaplingGen.class);
        registerConfigFunction("SmallLake", SmallLakeGen.class);
        registerConfigFunction("SurfacePatch", SurfacePatchGen.class);
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
     * @param <T>    Type of the holder of the config function.
     * @param name   The name of the config function.
     * @param holder The holder of the config function, like
     *               {@link WorldConfig}.
     * @param args   The args of the function.
     * @return A config function with the given name, or null if the config
     * function requires another holder. For invalid or non-existing config
     * functions, it returns an instance of {@link ErroredFunction}.
     */
    @SuppressWarnings("unchecked")
    // It's checked with clazz.getConstructor(holder.getClass(), ...))
    public <T> ConfigFunction<T> getConfigFunction(String name, T holder, List<String> args)
    {
        // Get the class of the config function
        Class<? extends ConfigFunction<?>> clazz = configFunctions.get(name.toLowerCase());
        if (clazz == null)
        {
            return new ErroredFunction<T>(name, holder, args, "Resource type " + name + " not found");
        }

        // Get a config function
        try
        {
            Constructor<? extends ConfigFunction<?>> constructor = clazz.getConstructor(
                    holder.getClass(), List.class);
            return (ConfigFunction<T>) constructor.newInstance(holder, args);
        } catch (NoSuchMethodException e1)
        {
            // Probably uses another holder type
            return null;
        } catch (InstantiationException e)
        {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e)
        {
            Throwable cause = e.getCause();
            if (cause instanceof InvalidConfigException)
            {
                return new ErroredFunction<T>(name, holder, args, cause.getMessage());
            }
            throw new RuntimeException(e);
        }
    }

}
