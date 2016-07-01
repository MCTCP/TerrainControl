package com.khorn.terraincontrol.customobjects;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.io.SettingsMap;
import com.khorn.terraincontrol.generator.SpawnableObject;
import com.khorn.terraincontrol.util.BoundingBox;
import com.khorn.terraincontrol.util.ChunkCoordinate;
import com.khorn.terraincontrol.util.Rotation;

import java.util.Map;
import java.util.Random;

/**
 * A custom object.
 *
 * <p>A custom object is a user-provided object that can be spawned in the
 * world. Unlike a plain {@link SpawnableObject}, it can have spawn conditions.
 * Also unlike a plain {@link SpawnableObject}, it can have other objects
 * attached to it using a {@link #getBranches(Rotation) branch system}.
 *
 */
public interface CustomObject extends SpawnableObject
{
    /**
     * Creates a new object with all settings applied.
     *
     * @param settings The settings. The settings of the existing object will
     * be {@link SettingsMap#setFallback(SettingsMap) set as a
     * fallback}.
     * @return A copy of this object will all the settings applied.
     */
    CustomObject applySettings(SettingsMap settings);

    /**
     * Returns whether this object can be placed with a random rotation. If
     * not, the rotation should always be NORTH.
     *
     * @return Whether this object can be placed with a random rotation.
     */
    boolean canRotateRandomly();

    /**
     * Returns whether this object can spawn from the CustomObject() resource.
     * Vanilla trees should return false; everything else should return true.
     *
     * @return Whether this object can spawn as an object.
     */
    boolean canSpawnAsObject();

    /**
     * Returns whether this object can spawn as a tree. UseWorld and UseBiome
     * should return true.
     *
     * @return Whether this object can spawn as a tree.
     */
    boolean canSpawnAsTree();

    /**
     * Returns whether the location would theoretically allow the object to
     * spawn there. Frequency/rarity is ignored.
     *
     * @param world    The world to check.
     * @param rotation Rotation to spawn the object in.
     * @param x        X coord of the object origin.
     * @param y        Y coord of the object origin.
     * @param z        Z coord of the object origin.
     * @return Whether the location allows for this object.
     */
    boolean canSpawnAt(LocalWorld world, Rotation rotation, int x, int y, int z);

    /**
     * Gets the bounding box of this object if it is rotated in the given way.
     * @param rotation The rotation of the object.
     * @return The bounding box.
     */
    BoundingBox getBoundingBox(Rotation rotation);

    /**
     * Returns a list of all branches in this object. Null is not a valid
     * return value, return an empty list instead.
     *
     * @param rotation The rotation for the branches.
     * @return A list of all branches in this object.
     */
    Branch[] getBranches(Rotation rotation);

    /**
     * Branches can have branches which can have branches, etc. This
     * method returns the limit of searching for branches.
     * 
     * @return The maximum branch depth.
     */
    int getMaxBranchDepth();

    /**
     * Returns the height at which the whole structure should spawn.
     * 
     * @return The height.
     */
    StructurePartSpawnHeight getStructurePartSpawnHeight();

    /**
     * Returns whether this object has branches attached to it.
     * 
     * @return Whether this object has branches attached to it.
     */
    boolean hasBranches();

    /**
     * Returns whether this object would like to spawn in this biome. BO2s will
     * return whether this biome is in their spawnInBiome setting.
     *
     * @param biome The biome.
     * @return True if the object is set to spawn in this biome, false otherwise.
     */
    boolean hasPreferenceToSpawnIn(LocalBiome biome);

    /**
     * Create a coordinate for this at a random position in the chunk.
     * Should respect it's own rarity setting. Can return null.
     *
     * @param random Random number generator based on the world seed.
     * @param chunkX The chunk x.
     * @param chunkZ The chunk z.
     * @return The CustomObjectCoordinate
     */
    CustomObjectCoordinate makeCustomObjectCoordinate(Random random, int chunkX, int chunkZ);

    /**
     * Called after all objects are loaded. The settings should be loaded
     * inside this method.
     *
     * @param otherObjectsInDirectory A map of all other objects in the
     *                                directory. Keys are lowercase.
     */
    void onEnable(Map<String, CustomObject> otherObjectsInDirectory);

    /**
     * Spawns the object one or more times in a chunk. The object can search a good y position by
     * itself.
     *
     * @param world      The world to spawn in.
     * @param random     Random number generator based on the world seed.
     * @param chunkCoord The chunk to spawn the objects in.
     * @return Whether at least one object spawned successfully.
     */
    boolean process(LocalWorld world, Random random, ChunkCoordinate chunkCoord);
}
