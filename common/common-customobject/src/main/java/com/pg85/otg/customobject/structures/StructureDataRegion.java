package com.pg85.otg.customobject.structures;

import com.pg85.otg.constants.Constants;

class StructureDataRegion
{
	private boolean requiresSave = false;
	private CustomStructure[][] structures = new CustomStructure[Constants.REGION_SIZE][Constants.REGION_SIZE];
	
	boolean requiresSave()
	{
		return this.requiresSave;
	}
	
	void markSaved()
	{
		this.requiresSave = false;
	}

	void markSaveRequired()
	{
		this.requiresSave = true;
	}
	
	CustomStructure getStructure(int internalX, int internalZ)
	{
		return this.structures[internalX][internalZ];
	}

	void setStructure(int internalX, int internalZ, CustomStructure structure, boolean requiresSave)
	{
		this.structures[internalX][internalZ] = structure;
		this.requiresSave = this.requiresSave || requiresSave;
	}
}
