package com.pg85.otg.util.interfaces;

public interface IBiome
{
	IBiomeConfig getBiomeConfig();

    boolean isCustom();

    String getName();

    /**
     * Gets the temperature at the given position, if this biome would be
     * there. This temperature is based on a base temperature value, but it
     * will be lower at higher altitudes.
     */
    float getTemperatureAt(int x, int y, int z);	
}
