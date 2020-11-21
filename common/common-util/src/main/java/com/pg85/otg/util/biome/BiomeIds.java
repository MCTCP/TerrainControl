package com.pg85.otg.util.biome;

/**
 * Immutable class to hold the biome ids of a biome.
 * <p>
 * Most biomes have just one id: it is used during terrain generation and it
 * is used to save to the map files. Some biomes have two ids: one used during
 * generation, one is saved to the map files. The id used during generation
 * has to be unique, the one saved to the map files doesn't have to be unique.
 */
public class BiomeIds
{
    private int otgBiomeId;
    private int savedId;

    /**
     * Creates a new virtual biome id.
     *
     * @param generationId The id used during terrain generation.
     * @param savedId The id used in the world save files (the .mca files in
     *            the region directory).
     */
    public BiomeIds(int otgBiomeId, int savedId)
    {
        this.otgBiomeId = otgBiomeId;
        this.savedId = savedId;
    }

    /**
     * Gets whether this biome is virtual. A biome is virtual if the id used
     * during terrain generation isn't the same as the id used in the world
     * save files.
     *
     * @return True if the biome is virtual, false otherwise.
     */
    public boolean isVirtual()
    {
    	return otgBiomeId > 255;
    }

    /**
     * Gets the id that is saved to the world save files.
     *
     * @return The id.
     */
    public int getSavedId()
    {
        return savedId;
    }

    /**
     * Gets the id used during terrain generation.
     *
     * @return The id.
     */
    public int getOTGBiomeId()
    {
        return otgBiomeId;
    }

    public void setSavedId(int value)
    {
    	savedId = value;
    }

    public void setOTGBiomeId(int value)
    {
    	otgBiomeId = value;
    }

    @Override
    public String toString()
    {
        if (isVirtual())
        {
            return otgBiomeId + " (otg), " + savedId + " (saved)";
        } else
        {
            return Integer.toString(savedId);
        }
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + savedId;
        result = prime * result + otgBiomeId;
        return result;
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
        if (!(obj instanceof BiomeIds))
        {
            return false;
        }
        BiomeIds other = (BiomeIds) obj;
        if (savedId != other.savedId)
        {
            return false;
        }
        if (otgBiomeId != other.otgBiomeId)
        {
            return false;
        }
        return true;
    }

}
