package com.pg85.otg.forge.network;

import java.util.ArrayList;
import java.util.List;

import com.pg85.otg.config.biome.BiomeConfig;
import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.util.biome.ColorSet;
import com.pg85.otg.util.biome.ColorThreshold;
import com.pg85.otg.util.biome.SimpleColorSet;

import net.minecraft.network.PacketBuffer;

public class BiomeSettingSyncWrapper
{
	private int grassColor;
	private int foliageColor;
	private int waterColor;
	private float fogDensity;
	private ColorSet grassColorControl;
	private ColorSet foliageColorControl;
	private ColorSet waterColorControl;

	public BiomeSettingSyncWrapper(IBiomeConfig config)
	{
		this.grassColor = config.getGrassColor();
		this.foliageColor = config.getFoliageColor();
		this.waterColor = config.getWaterColor();

		this.fogDensity = config.getFogDensity();

		this.grassColorControl = config.getGrassColorControl();
		this.foliageColorControl = config.getFoliageColorControl();
		this.waterColorControl = config.getWaterColorControl();
	}

	public BiomeSettingSyncWrapper(PacketBuffer buffer)
	{
		this.grassColor = buffer.readInt();
		this.foliageColor = buffer.readInt();
		this.waterColor = buffer.readInt();

		this.fogDensity = buffer.readFloat();

		List<ColorThreshold> grassColors = new ArrayList<>();
		for (int i = 0; i < buffer.readInt(); i++)
		{
			grassColors.add(new ColorThreshold(buffer.readInt(), buffer.readFloat()));
		}
		this.grassColorControl = new SimpleColorSet(grassColors);

		List<ColorThreshold> foliageColors = new ArrayList<>();
		for (int i = 0; i < buffer.readInt(); i++)
		{
			foliageColors.add(new ColorThreshold(buffer.readInt(), buffer.readFloat()));
		}
		this.foliageColorControl = new SimpleColorSet(foliageColors);

		List<ColorThreshold> waterColors = new ArrayList<>();
		for (int i = 0; i < buffer.readInt(); i++)
		{
			waterColors.add(new ColorThreshold(buffer.readInt(), buffer.readFloat()));
		}
		this.waterColorControl = new SimpleColorSet(waterColors);

	}

	public int getGrassColor()
	{
		return grassColor;
	}

	public int getFoliageColor()
	{
		return foliageColor;
	}

	public int getWaterColor()
	{
		return waterColor;
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

	public void encode(PacketBuffer buffer)
	{
		buffer.writeInt(this.grassColor);
		buffer.writeInt(this.foliageColor);
		buffer.writeInt(this.waterColor);

		buffer.writeFloat(this.fogDensity);

		buffer.writeInt(this.grassColorControl.getLayers().size());
		for (ColorThreshold color : this.grassColorControl.getLayers())
		{
			buffer.writeInt(color.getColor());
			buffer.writeFloat(color.getMaxNoise());
		}

		buffer.writeInt(this.foliageColorControl.getLayers().size());
		for (ColorThreshold color : this.foliageColorControl.getLayers())
		{
			buffer.writeInt(color.getColor());
			buffer.writeFloat(color.getMaxNoise());
		}

		buffer.writeInt(this.waterColorControl.getLayers().size());
		for (ColorThreshold color : this.waterColorControl.getLayers())
		{
			buffer.writeInt(color.getColor());
			buffer.writeFloat(color.getMaxNoise());
		}
	}

}
