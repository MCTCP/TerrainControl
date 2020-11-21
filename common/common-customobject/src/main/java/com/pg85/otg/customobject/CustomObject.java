package com.pg85.otg.customobject;

import com.pg85.otg.customobject.config.CustomObjectResourcesManager;
import com.pg85.otg.customobject.structures.CustomStructureCache;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.interfaces.ICustomObject;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IModLoadedChecker;
import com.pg85.otg.util.interfaces.IPresetNameProvider;
import com.pg85.otg.util.interfaces.IWorldGenRegion;

import java.nio.file.Path;
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
public interface CustomObject extends SpawnableObject, ICustomObject
{
    /**
     * Called after all objects are loaded. The settings should be loaded
     * inside this method.
     *
     * @param otherObjectsInDirectory A map of all other objects in the
     *                                directory. Keys are lowercase.
     */
    public boolean onEnable(Path otgRootFolder, boolean spawnLog, ILogger logger, CustomObjectManager customObjectManager, IPresetNameProvider presetNameProvider, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker);

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
    public boolean spawnForced(CustomStructureCache structureCache, IWorldGenRegion worldGenRegion, Random random, Rotation rotation, int x, int y, int z);
        
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
    public boolean spawnAsTree(CustomStructureCache structureCache, IWorldGenRegion worldGenRegion, Random random, int x, int z, int minY, int maxY, ChunkCoordinate chunkBeingPopulated);
    
    /**
     * Spawns the object one or more times in a chunk. The object can search a good y position by
     * itself.
     *
     * @param world      The world to spawn in.
     * @param random     Random number generator based on the world seed.
     * @param chunkCoord The chunk to spawn the objects in.
     * @return Whether at least one object spawned successfully.
     */
    public boolean process(CustomStructureCache structureCache, IWorldGenRegion worldGenRegion, Random random, ChunkCoordinate chunkCoord);

    boolean spawnFromSapling(CustomStructureCache structureCache, IWorldGenRegion worldGenRegion, Random random, Rotation rotation, int x, int y, int z);  

    public boolean loadChecks(IModLoadedChecker modLoadedChecker);

	// Disables biomeconfig replaceBlocks to save performance.
	boolean doReplaceBlocks();
}
