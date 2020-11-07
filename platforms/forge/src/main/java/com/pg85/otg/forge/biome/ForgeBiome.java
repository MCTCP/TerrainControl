package com.pg85.otg.forge.biome;

import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.config.biome.BiomeConfig;
import com.pg85.otg.util.BiomeIds;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

public class ForgeBiome implements LocalBiome
{
	private final Biome biomeBase;
	private final BiomeConfig biomeConfig;
    
    public ForgeBiome(Biome biomeBase, BiomeConfig biomeConfig)
    {
    	this.biomeBase = biomeBase;
    	this.biomeConfig = biomeConfig;
    }

    @Override
    public float getTemperatureAt(int x, int y, int z)
    {
        return this.biomeBase.getTemperature(new BlockPos(x, y, z));
    }

	@Override
	public BiomeConfig getBiomeConfig()
	{
		return this.biomeConfig;
	}    
    
	@Override
	public boolean isCustom()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getName()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BiomeIds getIds()
	{
		// TODO Auto-generated method stub
		return null;
	}
}
