package com.khorn.terraincontrol.customobjects.bo3;

import com.khorn.terraincontrol.LocalWorld;

import java.util.Random;

/**
 * Something that appears in the block list of a BO3.
 *
 */
public abstract class BO3PlaceableFunction extends BO3Function
{

    public int x;
    public int y;
    public int z;

    public BO3PlaceableFunction(BO3Config holder)
    {
        super(holder);
    }

    /**
     * Spawns this block at the position. The saved x, y and z in this block are
     * ignored.
     * @param world The world to spawn in.
     * @param random The random number generator.
     * @param x The absolute x to spawn. The x-position in this object is
     * ignored.
     * @param y The absolute y to spawn. The y-position in this object is
     * ignored.
     * @param z The absolute z to spawn. The z-position in this object is
     * ignored.
     */
    public abstract void spawn(LocalWorld world, Random random, int x, int y, int z);

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public abstract BO3PlaceableFunction rotate();

}
