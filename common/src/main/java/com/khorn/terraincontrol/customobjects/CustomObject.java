package com.khorn.terraincontrol.customobjects;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.io.SettingsReader;
import com.khorn.terraincontrol.util.ChunkCoordinate;
import com.khorn.terraincontrol.util.Rotation;

import java.util.Map;
import java.util.Random;

public interface CustomObject
{
    /**
     * Called after all objects are loaded. The settings should be loaded
     * inside this method.
     *
     * @param otherObjectsInDirectory A map of all other objects in the
     *                                directory. Keys are lowercase.
     */
    public void onEnable(Map<String, CustomObject> otherObjectsInDirectory);

    /**
     * Returns the name of this object.
     *
     * @return The name, without the extension.
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
     * @return Whether this object can spawn as an object.
     */
    public boolean canSpawnAsObject();

    /**
     * Returns whether this object can be placed with a random rotation. If
     * not, the rotation should always be NORTH.
     *
     * @return Whether this object can be placed with a random rotation.
     */
    public boolean canRotateRandomly();

    /**
     * Spawns the object at the given position. It shouldn't execute any checks.
     *
     * @param world
     * @param x
     * @param y
     * @param z
     * @return Whether the attempt was successful. (It should never fail, but you never know.)
     */
    public boolean spawnForced(LocalWorld world, Random random, Rotation rotation, int x, int y, int z);

    /**
     * Returns whether the location would theoretically allow the object to
     * spawn there. Frequency/rarity is ignored.
     *
     * @param world The world to check.
     * @param x     X coord of the object origin.
     * @param y     Y coord of the object origin.
     * @param z     Z coord of the object origin.
     * @return Whether the location allows for this object.
     */
    public boolean canSpawnAt(LocalWorld world, Rotation rotation, int x, int y, int z);

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
     * @param world      The world to spawn in.
     * @param random     Random number generator based on the world seed.
     * @param chunkCoord The chunk to spawn the objects in.
     * @return Whether at least one object spawned successfully.
     */
    public boolean process(LocalWorld world, Random random, ChunkCoordinate chunkCoord);

    /**
     * Creates a new object with all settings applied.
     *
     * @param settings The settings. The settings of the existing object will
     * be {@link SettingsReader#setFallbackReader(SettingsReader) set as a
     * fallback}.
     * @return A copy of this object will all the settings applied.
     */
    public CustomObject applySettings(SettingsReader settings);

    /**
     * Returns whether this object would like to spawn in this biome. BO2s will
     * return whether this biome is in their spawnInBiome setting.
     *
     * @param biome
     * @return
     */
    public boolean hasPreferenceToSpawnIn(LocalBiome biome);
}
