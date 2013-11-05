package com.khorn.terraincontrol.customobjects;

import com.khorn.terraincontrol.LocalWorld;

/**
 * Class to get the spawn height of objects part of a structure.
 * Three default implementations are provided.
 * <p />
 * Structures consist of multiple {@link StructuredCustomObject}s. Structures
 * can be quite big, so not all parts are in loaded chunks. An initial guess
 * of the y coordinate of all parts is made when starting a structure.
 * However, when the spawn height depends on the terrain this guess will be
 * incorrect. The purpose of this class is to correct the y location just
 * before the object is spawned.
 * <p />
 * It is up to the object that started the structure to provide the correct
 * implementation. Structures like villages want all their parts to be placed
 * on the surface, so they will always return the y position of the highest
 * block on the column as the correct location to spawn. Structures like
 * strongholds however have a random y (within limits) as a start position and
 * place all parts relative to the start, without depending on the terrain.
 * For them the guess is always correct, so they just return the given y
 * position.
 *
 */
public interface StructurePartSpawnHeight
{
    /** 
     * Use the y position provided in this object .
     */
    public static final StructurePartSpawnHeight PROVIDED = new StructurePartSpawnHeight()
    {

        @Override
        public int getCorrectY(LocalWorld world, int x, int y, int z)
        {
            return y;
        }
    };

    /** 
     * Use the highest block on the x,z column 
     */
    public static final StructurePartSpawnHeight HIGHEST_BLOCK = new StructurePartSpawnHeight()
    {

        @Override
        public int getCorrectY(LocalWorld world, int x, int y, int z)
        {
            return world.getHighestBlockYAt(x, z);
        }

    };

    /** 
     * Use the highest solid block on the x,z column 
     */
    public static final StructurePartSpawnHeight HIGHEST_SOLID_BLOCK = new StructurePartSpawnHeight()
    {

        @Override
        public int getCorrectY(LocalWorld world, int x, int y, int z)
        {
            return world.getSolidHeight(x, z);
        }
    };

    /**
     * Gets the correct y position for this part of the structure. An y
     * position based on the coordinates of the object this object is attached
     * to is already given (see the y parameter of the Branch function of BO3s
     * for example). This can be ignored, for example to let all objects spawn
     * on the surface no matter what.
     * 
     * @param world The world the object is spawning in.
     * @param x     The x position the object is spawning on.
     * @param y     The guessed y position the object is spawning on.
     * @param z     The z position the object is spawning on.
     * @return The y position the object should spawn on instead.
     */
    public int getCorrectY(LocalWorld world, int x, int y, int z);
}
