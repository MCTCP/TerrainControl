package com.pg85.otg.network;

public class BiomeIdData
{
	public String biomeName;
	public int otgBiomeId;
	public int savedBiomeId;
	
	public BiomeIdData() {}
	
	public BiomeIdData(String biomeName, int otgBiomeId, int savedBiomeId)
	{
		this.biomeName = biomeName;
		this.otgBiomeId = otgBiomeId;
		this.savedBiomeId = savedBiomeId;
	}
}