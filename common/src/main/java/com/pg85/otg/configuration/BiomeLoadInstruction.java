package com.pg85.otg.configuration;

import com.pg85.otg.configuration.standard.StandardBiomeTemplate;

/**
 * Information a {@link BiomeConfig} needs to be loaded properly.
 * 
 */
public class BiomeLoadInstruction
{
    private final String name;
    private final StandardBiomeTemplate biomeTemplate;

    /**
     * Creates a new BiomeLoadInstruction.
     * 
     * @param name Name of the biome to be loaded.
     * @param generationId Generation id of the biome to be loaded.
     * @param biomeTemplate Default settings of the biome to be loaded.
     */
    public BiomeLoadInstruction(String name, StandardBiomeTemplate biomeTemplate)
    {
        if (name == null || biomeTemplate == null)
        {
            throw new IllegalArgumentException("Parameters cannot be null (name: " + name + ", biomeTemplate: " + biomeTemplate + ")");
        }

        this.name = name;
        this.biomeTemplate = biomeTemplate;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + biomeTemplate.hashCode();
        result = prime * result + name.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!(obj instanceof BiomeLoadInstruction))
        {
            return false;
        }
        BiomeLoadInstruction other = (BiomeLoadInstruction) obj;
        if (!biomeTemplate.equals(other.biomeTemplate))
        {
            return false;
        }
        if (!name.equals(other.name))
        {
            return false;
        }
        return true;
    }

    /**
     * Gets the name of the biome that will be loaded.
     * 
     * @return The name.
     */
    public String getBiomeName()
    {
        return name;
    }

    /**
     * Gets the default settings of the biome that will be loaded.
     * 
     * @return The default settings.
     */
    public StandardBiomeTemplate getBiomeTemplate()
    {
        return biomeTemplate;
    }
}
