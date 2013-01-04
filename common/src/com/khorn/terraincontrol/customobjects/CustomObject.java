package com.khorn.terraincontrol.customobjects;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;

import java.util.Map;
import java.util.Random;

public interface CustomObject
{
    /**
     * Returns the name of this object.
     *
     * @return
     */
    public String getName();

    /**
     * Returns whether this object can spawn as a tree. UseWorld and UseBiome
     * should return true.
     *
     * @return Whether this object can spawn as a tree.
     */
    public boolean canSpawnAsTree();

    /**
     * Returns whether this object can spawn from the CustomObject() resource.
     * Vanilla trees should return false; everything else should return true.
     *
     * @return
     */
    public boolean canSpawnAsObject();

    /**
     * Spawns the object at the given position.
     *
     * @param world
     * @param x
     * @param y
     * @param z
     * @return Whether the attempt was successful.
     */
    public boolean spawn(LocalWorld world, Random random, int x, int y, int z);

    /**
     * Spawns the object at the given position. If the object isn't a tree, it
     * shouldn't spawn and it should return false.
     *
     * @param world
     * @param x
     * @param y
     * @param z
     * @return Whether the attempt was successful.
     */
    public boolean spawnAsTree(LocalWorld world, Random random, int x, int y, int z);

    /**
     * Spawns the object at the given position. It should search a suitable y
     * location by itself.
     *
     * @param world
     * @param x
     * @param z
     * @return Whether the attempt was successful.
     */
    public boolean spawn(LocalWorld world, Random random, int x, int z);

    /**
     * Spawns the object at the given position. It should search a suitable y
     * location by itself. If the object isn't a tree, it shouldn't spawn and it
     * should return false.
     *
     * @param world
     * @param x
     * @param z
     * @return Whether the attempt was successful.
     */
    public boolean spawnAsTree(LocalWorld world, Random random, int x, int z);

    /**
     * Spawns the object one or more times in a chunk. The object can search a good y position by
     * itself.
     *
     * @param world
     * @param random
     * @param chunkX
     * @param chunkZ
     * @return Whether at least one object spawned successfully.
     */
    public boolean process(LocalWorld world, Random random, int chunkX, int chunkZ);

    /**
     * Spawns the object in a chunk. The object can search a good y position by
     * itself. If the object isn't a tree, the object shouldn't spawn and it
     * should return false.
     *
     * @param world
     * @param random
     * @param chunkX
     * @param chunkZ
     * @return Whether at least one object spawned successfully.
     */
    public boolean processAsTree(LocalWorld world, Random random, int chunkX, int chunkZ);

    /**
     * Returns a copy of this object will all the settings applied. Can return
     * null if the settings are invalid.
     *
     * @param settings A Map with all the settings.
     * @return A copy of this object will all the settings applied.
     */
    public CustomObject applySettings(Map<String, String> settings);

    /**
     * Returns whether this object would like to spawn in this biome. BO2s will
     * return whether this biome is in their spawnInBiome setting.
     *
     * @param biome
     * @return
     */
    public boolean hasPreferenceToSpawnIn(LocalBiome biome);
}
