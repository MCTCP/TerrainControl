package com.pg85.otg.util.interfaces;

public interface IBiome
{

	float getTemperatureAt(int x, int blockToFreezeY, int z);

	IBiomeConfig getBiomeConfig();

}
