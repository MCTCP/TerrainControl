package com.pg85.otg.forge.dimensions;

// TODO: Since dimensionId's are stored in the dimensionconfig, and load order shouldn't matter (not even for 
// biome registration, since biome id's are saved after creation), is this still needed? 
// * This contains data for generated dims tho, which may not match or be edited via the config.yaml after creation (dimname/keeploaded). 
public class DimensionData
{
	int dimensionOrder;
	public int dimensionId;
	public String dimensionName;
	boolean keepLoaded;
	long seed = 0; // TODO: Why is this not used? Remove?
}
