package com.pg85.otg.forge.network;

import java.util.ArrayList;
import java.util.List;

import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.util.biome.ColorSet;
import com.pg85.otg.util.biome.ColorThreshold;
import com.pg85.otg.util.biome.SimpleColorSet;

import io.netty.buffer.ByteBuf;

public class BiomeSettingSyncWrapper
{
	private float fogDensity;
	private ColorSet grassColorControl;
	private ColorSet foliageColorControl;
	private ColorSet waterColorControl;

	public BiomeSettingSyncWrapper(IBiomeConfig config)
	{
		this.fogDensity = config.getFogDensity();

		this.grassColorControl = config.getGrassColorControl();
		this.foliageColorControl = config.getFoliageColorControl();
		this.waterColorControl = config.getWaterColorControl();
	}

	public BiomeSettingSyncWrapper(ByteBuf buffer)
	{
		this.fogDensity = buffer.readFloat();
		byte size;

		List<ColorThreshold> grassColors = new ArrayList<>();
		size = buffer.readByte();
		for (int i = size; i > 0; i--)
		{
			grassColors.add(new ColorThreshold(buffer.readInt(), buffer.readFloat()));
		}
		this.grassColorControl = new SimpleColorSet(grassColors);

		List<ColorThreshold> foliageColors = new ArrayList<>();
		size = buffer.readByte();
		for (int i = size; i > 0; i--)
		{
			foliageColors.add(new ColorThreshold(buffer.readInt(), buffer.readFloat()));
		}
		this.foliageColorControl = new SimpleColorSet(foliageColors);

		List<ColorThreshold> waterColors = new ArrayList<>();
		size = buffer.readByte();
		for (int i = size; i > 0; i--)
		{
			waterColors.add(new ColorThreshold(buffer.readInt(), buffer.readFloat()));
		}
		this.waterColorControl = new SimpleColorSet(waterColors);

	}

	public float getFogDensity()
	{
		return fogDensity;
	}

	public ColorSet getGrassColorControl()
	{
		return grassColorControl;
	}

	public ColorSet getFoliageColorControl()
	{
		return foliageColorControl;
	}

	public ColorSet getWaterColorControl()
	{
		return waterColorControl;
	}

	public void encode(ByteBuf buffer)
	{
		buffer.writeFloat(this.fogDensity);

		buffer.writeByte((byte) this.grassColorControl.getLayers().size());
		for (ColorThreshold color : this.grassColorControl.getLayers())
		{
			buffer.writeInt(color.getColor());
			buffer.writeFloat(color.getMaxNoise());
		}

		buffer.writeByte((byte) this.foliageColorControl.getLayers().size());
		for (ColorThreshold color : this.foliageColorControl.getLayers())
		{
			buffer.writeInt(color.getColor());
			buffer.writeFloat(color.getMaxNoise());
		}

		buffer.writeByte((byte) this.waterColorControl.getLayers().size());
		for (ColorThreshold color : this.waterColorControl.getLayers())
		{
			buffer.writeInt(color.getColor());
			buffer.writeFloat(color.getMaxNoise());
		}
	}
}
