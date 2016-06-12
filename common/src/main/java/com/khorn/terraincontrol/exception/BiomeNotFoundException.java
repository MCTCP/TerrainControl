package com.khorn.terraincontrol.exception;

import com.khorn.terraincontrol.util.helpers.StringHelper;

import java.util.Collection;

/**
 * Thrown when a biome is not found. This exception has been created to avoid
 * null values propagating everywhere, which causes errors far removed from the
 * bugged code.
 *
 */
public class BiomeNotFoundException extends RuntimeException {

    /**
     * Generated serial version id, to prevent compiler warnings.
     */
    private static final long serialVersionUID = 6832663686946138366L;

    private final String biomeName;

    /**
     * Constructs a new exception. Used for failed lookups using a biome name.
     *
     * @param biomeName
     *            The biome name that was not found.
     * @param biomes
     *            All biomes in the list. {@link Object#toString()} is used to
     *            print the biomes.
     */
    public BiomeNotFoundException(String biomeName, Collection<?> biomes) {
        super("Biome " + biomeName + " not found; available biomes: " + StringHelper.join(biomes, ", "));
        this.biomeName = biomeName;
    }

    /**
     * Constructs a new exception. Used for failed lookups using a biome id.
     *
     * @param biomeId
     *            The id of the biome.
     * @param biomes
     *            All biomes in the list. {@link Object#toString()} is used to
     *            print the biomes.
     */
    public BiomeNotFoundException(int biomeId, Collection<?> biomes) {
        super("Biome with id " + biomeId + " not found; available biomes: " + StringHelper.join(biomes, ", "));
        this.biomeName = "biome-" + biomeId;
    }

    /**
     * Gets the name of the biome that could not be found.
     *
     * @return The name of the biome.
     */
    public String getBiomeName() {
        return biomeName;
    }

}
