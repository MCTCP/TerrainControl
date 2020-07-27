package com.pg85.otg.forge.blocks;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileEntityPortal extends TileEntity
{
	public TileEntityPortal() { }
	
	public TileEntityPortal(OTGPortalData portalData)
	{
		otgPortalData = portalData;
	}

    OTGPortalData otgPortalData = OTGPortalData.EMPTY_DATA;

    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        this.otgPortalData = OTGPortalData.fromNBT(compound);
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);

        if (this.otgPortalData != null)
        {
            this.otgPortalData.toNBT(compound);
        }

        return compound;
    }
}