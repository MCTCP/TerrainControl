package com.pg85.otg.customobjects.structures;

public class StructureDataRegion
{
	private boolean requiresSave = false;
	private CustomStructure[][] structures = new CustomStructure[CustomStructureCache.REGION_SIZE][CustomStructureCache.REGION_SIZE];
	
	public boolean requiresSave()
	{
		return this.requiresSave;
	}
	
	public void markSaved()
	{
		this.requiresSave = false;
	}

	public void markSaveRequired()
	{
		this.requiresSave = true;
	}
	
	public CustomStructure getStructure(int internalX, int internalZ)
	{
		return this.structures[internalX][internalZ];
	}

	public void setStructure(int internalX, int internalZ, CustomStructure structure, boolean requiresSave)
	{
		this.structures[internalX][internalZ] = structure;
		this.requiresSave = this.requiresSave || requiresSave;
	}
}
