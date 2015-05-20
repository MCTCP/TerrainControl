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
    public final Class<BeforeGroupsBiomeGenerator> BEFORE_GROUPS = register("BeforeGroups", BeforeGroupsBiomeGenerator.class);

    /**
     * Register a biome register. Should be called before the config files
     * are read.
     * @param name Name of the biome generator, used in config files.
     * @param clazz The biome generator to register.
     * @return The biome register that was just registered.
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
     * Creates an instance of the given biome generator. If an error occurs
     * during instantiation, a message is logged and the normal biome
     * generator is returned.
     * @param clazz The BiomeGenerator class to instantiate.
     * @param world The world of the biome generator.
     * @return The biome generator.
     * @see #createCached(Class, LocalWorld)
     */
    public <T extends BiomeGenerator> BiomeGenerator create(Class<T> clazz, LocalWorld world)
    {
        try
        {
            return clazz.getConstructor(LocalWorld.class).newInstance(world);
        } catch (Exception e)
        {
            TerrainControl.log(LogMarker.FATAL, "Cannot properly reflect biome manager, falling back on BiomeMode:Normal");
            TerrainControl.printStackTrace(LogMarker.FATAL, e);
            return new NormalBiomeGenerator(world);
        }
    }

    /**
     * Same as {@link #create(Class, LocalWorld)}, but the returned biome
     * generator is now guaranteed to be cached: if the generator with the
     * given class isn't cached, it is wrapped in a cache.
     * @param biomeModeClass Class to create a biome generator from.
     * @param world          World the biome generates for.
     * @return The cached biome generator.
     */
    public BiomeGenerator createCached(Class<? extends BiomeGenerator> biomeModeClass, LocalWorld world)
    {
        return CachedBiomeGenerator.makeCached(create(biomeModeClass, world));
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
