package com.pg85.otg.forge.blocks;

import javax.annotation.concurrent.Immutable;
import net.minecraft.nbt.NBTTagCompound;

@Immutable
public class OTGPortalData
{
    public static final OTGPortalData EMPTY_DATA = new OTGPortalData(null, null, 2000);
    private final String particleType;
    private final String mobType;
    private final int mobSpawnChance;

    public OTGPortalData()
    {
    	particleType = null;
    	mobType = null;
    	mobSpawnChance = 2000;
    }
    
    public OTGPortalData(String particleType, String mobType, int mobSpawnChance)
    {
        this.particleType = particleType;
        this.mobType = mobType;
        this.mobSpawnChance = mobSpawnChance;
    }

    public boolean isEmpty()
    {
        return (
    		this.particleType == null || 
    		this.particleType.isEmpty()
		) && (
			this.mobType == null || 
			this.mobType.isEmpty()
		);
    }

    public String getParticleType()
    {
        return this.particleType;
    }
    
    public String getMobType()
    {
        return this.mobType;
    }

    public int getMobSpawnChance()
    {
        return this.mobSpawnChance;
    }

    public void toNBT(NBTTagCompound nbt)
    {
    	if(this.particleType != null)
    	{
    		nbt.setString("ParticleType", this.particleType);
    	}
    	if(this.mobType != null)
    	{
    		nbt.setString("MobType", this.mobType);
    	}
        nbt.setInteger("MobSpawnChance", this.mobSpawnChance);
    }

    public static OTGPortalData fromNBT(NBTTagCompound nbt)
    {
    	String particleType = null;
        if (nbt.hasKey("ParticleType"))
        {
            particleType = nbt.getString("ParticleType");
        }
        
        String mobType = null;
        if (nbt.hasKey("MobType"))
        {
            mobType = nbt.getString("MobType");
        }
        
        int mobSpawnChance = 0;
        if (nbt.hasKey("MobSpawnChance"))
        {
        	mobSpawnChance = nbt.getInteger("MobSpawnChance");
        }
        
    	OTGPortalData portalData = new OTGPortalData(particleType, mobType, mobSpawnChance);
        
        if(portalData.isEmpty())
        {
        	return EMPTY_DATA;	
        }

        return portalData;
    }
}