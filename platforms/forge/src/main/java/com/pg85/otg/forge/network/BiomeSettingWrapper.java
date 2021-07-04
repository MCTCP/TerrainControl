package com.pg85.otg.forge.network;

import com.pg85.otg.config.biome.BiomeConfig;

import net.minecraft.network.PacketBuffer;

public class BiomeSettingWrapper
{
	private int fogColor;
	private float fogDensity;

	public BiomeSettingWrapper(BiomeConfig config)
	{
		this.fogColor = config.getFogColor();
		this.fogDensity = config.getFogDensity();
	}
	
	public BiomeSettingWrapper(PacketBuffer buffer)
	{
		this.fogColor = buffer.readInt();
	}

	public int getFogColor()
	{
		return fogColor;
	}

	public float getFogDensity()
	{
		return fogDensity;
	}

	public void encode(PacketBuffer buffer)
	{
		buffer.writeInt(this.fogColor);
	}
	
	

}
