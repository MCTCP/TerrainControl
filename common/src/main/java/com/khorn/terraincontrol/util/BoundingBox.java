package com.khorn.terraincontrol.util;


/**
 * Represents a three-dimensional bounding box. Bounding boxes are initially
 * empty and can be expanded to fit a block by calling the
 * {@link #expandToFit(int, int, int)} method.
 *
 * <p>Bounding boxes are mutable. Usually bounding boxes gradually expand
 * until they contain the whole object. Creating a new object every time would
 * be quite expensive. (Keep in mind that for example BO3s can contain
 * thousands of blocks, and that you can have hundreds of BO3s on the server.)
 * You can create a defensive clone of the bounding box using the
 * {@link #clone()} method.
 * 
 * <p>Note that the max coords of the bounding box are exclusive. This means
 * that a box of (0, 0, 0) to (1, 1, 1) contains only one block, not 8. This is
 * unlike Minecraft's bounding box, where that box would include 8 blocks. Our
 * design allows us to calculate the width/height/depth of the box using simple
 * calculations like {@code maxX - minX}.
 */
public final class BoundingBox implements Cloneable
{
    // Min coords are inclusive
    private int minX;
    private int minY;
    private int minZ;

    // Max coords are exclusive
    private int maxX;
    private int maxY;
    private int maxZ;

    private BoundingBox()
    {
        // Private constructor
    }

    /**
     * Gets a new bounding box that is initially empty.
     * @return The bounding box.
     */
    public static BoundingBox newEmptyBox()
    {
        return new BoundingBox();
    }

    @Override
    public BoundingBox clone()
    {
        try
        {
            BoundingBox cloned = (BoundingBox) super.clone();
            cloned.minX = this.minX;
            cloned.minY = this.minY;
            cloned.minZ = this.minZ;
            cloned.maxX = this.maxX;
            cloned.maxY = this.maxY;
            cloned.maxZ = this.maxZ;
            return cloned;
        } catch (CloneNotSupportedException e)
        {
            throw new AssertionError(e);
        }
    }

    /**
     * Gets a new bounding box that is rotated 90 degrees to the original. The original bounding box will not be modified.
     * @return A new, rotated bounding box.
     */
    public BoundingBox rotate()
    {
        BoundingBox cloned = new BoundingBox();
        cloned.minX = this.minZ;
        cloned.minY = this.minY;
        cloned.minZ = -this.minX;
        cloned.maxX = this.maxZ;
        cloned.maxY = this.maxY;
        cloned.maxZ = -this.maxX;
        return cloned;
    }

    /**
     * Expands the current bounding box to fit the given block. If the
     * bounding box {@link #isEmpty() is currently empty} the bounding box
     * will contain a 1 x 1 x 1 volume for the given block after calling this
     * method.
     *
     * @param x X position of the block.
     * @param y Y position of the block.
     * @param z Z position of the block.
     */
    public void expandToFit(int x, int y, int z) {
        if (isEmpty())
        {
            setToOneBlockVolume(x, y, z);
            return;
        }

        if (x < this.minX)
        {
            this.minX = x;
        } else if (x >= this.maxX)
        {
            this.maxX = x + 1;
        }
        if (y < this.minY)
        {
            this.minY = y;
        } else if (y >= this.maxY)
        {
            this.maxY = y + 1;
        }
        if (z < this.minZ)
        {
            this.minZ = z;
        } else if (z >= this.maxZ)
        {
            this.maxZ = z + 1;
        }
    }

    public void expandToFitChunk(ChunkCoordinate chunk)
    {
        expandToFit(chunk.getBlockX(), 0, chunk.getBlockZ());
        expandToFit(chunk.getBlockX() + ChunkCoordinate.CHUNK_X_SIZE, ChunkCoordinate.CHUNK_Y_SIZE,
                chunk.getBlockZ() + ChunkCoordinate.CHUNK_Z_SIZE);
    }

    /**
     * Gets whether this bounding box is empty. Empty bounding boxes have a
     * width, height or depth of zero.
     * @return True if the bounding box is empty, false otherwise.
     */
    public boolean isEmpty()
    {
        return this.minX == this.maxX || this.minY == this.maxY || this.minZ == this.maxZ;
    }

    /**
     * Makes this bounding box fit the given block exactly. The bounding box
     * will have a resulting volume of 1.
     * 
     * @param x The x of the given block.
     * @param y The y of the given block.
     * @param z The z of the given block.
     */
    private void setToOneBlockVolume(int x, int y, int z)
    {
        this.minX = x;
        this.maxX = x + 1;
        this.minY = y;
        this.maxY = y + 1;
        this.minZ = z;
        this.maxZ = z + 1;
    }

    /**
     * Gets the lowest x position of this bounding box.
     * @return The lowest x.
     */
    public int getMinX()
    {
        return this.minX;
    }

    /**
     * Gets the lowest y position of this bounding box.
     * @return The lowest y.
     */
    public int getMinY()
    {
        return this.minY;
    }

    /**
     * Gets the lowest z position of this bounding box.
     * @return The lowest z.
     */
    public int getMinZ()
    {
        return this.minZ;
    }

    /**
     * Gets the width (along the x-axis) of this bounding box.
     * @return The width.
     */
    public int getWidth()
    {
        return this.maxX - this.minX;
    }

    /**
     * Gets the height (along the y-axis) of this bounding box.
     * @return The height.
     */
    public int getHeight()
    {
        return this.maxY - this.minY;
    }

    /**
     * Gets the depth (along the z-axis) of this bounding box.
     * @return The depth.
     */
    public int getDepth()
    {
        return this.maxZ - this.minZ;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.maxX;
        result = prime * result + this.maxY;
        result = prime * result + this.maxZ;
        result = prime * result + this.minX;
        result = prime * result + this.minY;
        result = prime * result + this.minZ;
        return result;
    }

    @Override
    public String toString()
    {
        return "BoundingBox[minX=" + this.minX + ", minY=" + this.minY + ", minZ=" + this.minZ + ", width=" + getWidth()
            + ", height=" + getHeight() + ", depth=" + getDepth() + "]";
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (!(obj instanceof BoundingBox))
        {
            return false;
        }
        BoundingBox other = (BoundingBox) obj;
        if (this.maxX != other.maxX)
        {
            return false;
        }
        if (this.maxY != other.maxY)
        {
            return false;
        }
        if (this.maxZ != other.maxZ)
        {
            return false;
        }
        if (this.minX != other.minX)
        {
            return false;
        }
        if (this.minY != other.minY)
        {
            return false;
        }
        if (this.minZ != other.minZ)
        {
            return false;
        }
        return true;
    }
}
