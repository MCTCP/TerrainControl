package com.khorn.terraincontrol.generator.biome;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.logging.LogMarker;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Class used to register IBiomeManager
 */
public class BiomeModeManager
{
    // Stores all biome managers
    private Map<String, Class<? extends BiomeGenerator>> registered = new HashMap<String, Class<? extends BiomeGenerator>>();

    public final Class<NormalBiomeGenerator> NORMAL = register("Normal", NormalBiomeGenerator.class);
    public final Class<FromImageBiomeGenerator> FROM_IMAGE = register("FromImage", FromImageBiomeGenerator.class);
    public final Class<OldBiomeGenerator> OLD_GENERATOR = register("OldGenerator", OldBiomeGenerator.class);
    public final Class<VanillaBiomeGenerator> VANILLA = register("Default", VanillaBiomeGenerator.class);

    /**
     * Register a biome manager. Should be called before the config files
     * are read.
     * <p/>
     * @param manager The biome manager to register.
     * <p/>
     * @return
     * <p/>
     * @return The biome manager that was just registered.
     */
    public <T extends BiomeGenerator> Class<T> register(String name, Class<T> clazz)
    {
        registered.put(name, clazz);
        return clazz;
    }

    /**
     * Get the biome manager with the specified name.
     * <p/>
     * @param name The name of the biome manager. Name is case-insensitive.
     * <p/>
     * @return The biome manager, or the normal biome generator if not
     *         found.
     */
    public Class<? extends BiomeGenerator> getBiomeManager(String name)
    {
        for (String key : registered.keySet())
        {
            if (key.equalsIgnoreCase(name))
            {
                return registered.get(key);
            }
        }
        // Fall back on normal mode
        TerrainControl.log(LogMarker.WARN, "{} is not a valid biome mode, falling back on Normal.", (Object) name);
        return NORMAL;
    }

    /**
     * Does the reflection logic for you.
     * <p/>
     * @param clazz The BiomeGenerator class to instantiate.
     * @param world The world of the biome generator.
     * @param cache The biome cache object.
     * <p/>
     * @return The instantiated object.
     */
    public <T extends BiomeGenerator> BiomeGenerator create(Class<T> clazz, LocalWorld world, BiomeCache cache)
    {
        try
        {
            return clazz.getConstructor(new Class[]
            {
                LocalWorld.class, BiomeCache.class
            }).newInstance(world, cache);
        } catch (Exception e)
        {
            TerrainControl.log(LogMarker.FATAL, "Cannot properly reflect biome manager, falling back on BiomeMode:Normal");
            TerrainControl.printStackTrace(LogMarker.FATAL, e);
            return new NormalBiomeGenerator(world, cache);
        }
    }

    /**
     * Gets the name of the biome generator, based on how it registered
     * itself.
     * <p/>
     * @param clazz The biome generator.
     * <p/>
     * @return The name of the biome generator, or null if the biome
     *         generator wasn't registered.
     */
    public String getName(Class<? extends BiomeGenerator> clazz)
    {
        for (Entry<String, Class<? extends BiomeGenerator>> entry : registered.entrySet())
        {
            if (entry.getValue().equals(clazz))
            {
                return entry.getKey();
            }
        }

        return null;
    }

}
