package com.pg85.otg.util.materials;

class MaterialSetEntry
{
    private LocalMaterialData material;
    private final boolean includesBlockData;

    MaterialSetEntry(LocalMaterialData material, boolean includesBlockData)
    {
        this.material = material;
        this.includesBlockData = includesBlockData;
    }

    @Override
    public boolean equals(Object other)
    {
        // Uses hashCode, as it is guaranteed to be unique for this class
        if (other instanceof MaterialSetEntry)
        {
            return other.hashCode() == hashCode();
        }
        return false;
    }

    /**
     * Gets the hashCode of this entry, which is equal to either
     * {@link LocalMaterialData#hashCode()} or
     * {@link LocalMaterialData#hashCodeWithoutBlockData()}. This means that
     * the hashCode is unique.
     *
     * @return The unique hashCode.
     */
    @Override
    public int hashCode()
    {
        //if (includesBlockData)
        //{
            return material.hashCode();
        //} else
        //{
            //return material.hashCodeWithoutBlockData();
        //}
    }

    /*
    public void parseForWorld(WorldConfig worldConfig)
    {
    	material.parseForWorld(worldConfig);
    }
    */

    @Override
    public String toString()
    {
        return material.toString();
    }

    /**
     * Rotates this check 90 degrees. If block data was ignored in this check,
     * it will still be ignored, otherwise the block data will be rotated too.
     * 
     * @return The rotated check.
     */
    MaterialSetEntry rotate()
    {
        if (!includesBlockData)
        {
            // Don't rotate block data
            return new MaterialSetEntry(material, false);
        } else
        {
            // Actually rotate block data, to maintain check correctness
            return new MaterialSetEntry(material.rotate(), true);
        }
    }
}
