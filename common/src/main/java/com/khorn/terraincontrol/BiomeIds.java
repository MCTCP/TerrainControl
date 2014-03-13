package com.khorn.terraincontrol;

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
    private final int generationId;
    private final int savedId;

    /**
     * Creates a new, non-virtual biome id.
     * 
     * @param id The id of the biome.
     */
    public BiomeIds(int id)
    {
        this.generationId = id;
        this.savedId = id;
    }

    /**
     * Creates a new virtual biome id.
     * 
     * @param generationId The id used during terrain generation.
     * @param savedId The id used in the world save files (the .mca files in
     *            the region directory).
     */
    public BiomeIds(int generationId, int savedId)
    {
        this.generationId = generationId;
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
        return savedId != generationId;
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
    public int getGenerationId()
    {
        return generationId;
    }

    @Override
    public String toString()
    {
        if (isVirtual())
        {
            return generationId + " (gen), " + savedId + " (saved)";
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
        result = prime * result + generationId;
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
        if (generationId != other.generationId)
        {
            return false;
        }
        return true;
    }

}
