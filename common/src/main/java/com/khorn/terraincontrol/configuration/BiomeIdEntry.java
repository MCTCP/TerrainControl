package com.khorn.terraincontrol.configuration;

/**
 * Represents a biome and its ID from a .bidl file.
 */
public class BiomeIdEntry
{

    public short biomeId;
    public String biomeName;

    public BiomeIdEntry(String biomeName, short biomeId)
    {
        this.biomeId = ((biomeId > BiomeIdListingConfig.maxBiomeCount || biomeId < 0) ? -1 : biomeId);
        this.biomeName = biomeName;
    }

    protected String makeString()
    {
        return "biome(" + biomeName + ',' + biomeId + ')';
    }

}
