package com.pg85.otg.gen.biome.layers;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

import com.pg85.otg.constants.SettingsEnums.ImageMode;
import com.pg85.otg.gen.biome.layers.type.ParentedLayer;
import com.pg85.otg.gen.biome.layers.util.LayerSampleContext;
import com.pg85.otg.interfaces.ILayerSampler;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;

public class FromImageLayer implements ParentedLayer
{
	private final BiomeLayerData data;
	private int[] biomeMap;
	private int mapHeight;
	private int mapWidth;

	FromImageLayer(BiomeLayerData data, ILogger logger)
	{
		this.data = data;

		// Read from file
		try
		{
			final File image = new File(data.presetDir.toFile(), data.imageFile);
			final BufferedImage map = ImageIO.read(image);

			this.mapWidth = map.getWidth(null);
			this.mapHeight = map.getHeight(null);
			int[] colorMap = new int[this.mapHeight * this.mapWidth];

			map.getRGB(0, 0, this.mapWidth, this.mapHeight, colorMap, 0, this.mapWidth);

			// Rotate RGBs if need
			switch (data.imageOrientation)
			{
				case North:
					// Default behavior - nothing to rotate
					break;
				case South:
					// Rotate picture 180 degrees
					int[] colorMap180 = new int[colorMap.length];
					for (int y = 0; y < this.mapHeight; y++)
					{
						for (int x = 0; x < this.mapWidth; x++)
						{
							colorMap180[(this.mapHeight - 1 - y) * this.mapWidth + this.mapWidth - 1 - x] = colorMap[y * this.mapWidth + x];
						}
					}
					colorMap = colorMap180;
					break;
				case West:
					// Rotate picture CW
					int[] colorMapCW = new int[colorMap.length];
					for (int y = 0; y < this.mapHeight; y++)
					{
						for (int x = 0; x < this.mapWidth; x++)
						{
							colorMapCW[x * this.mapHeight + this.mapHeight - 1 - y] = colorMap[y * this.mapWidth + x];
						}
					}
					colorMap = colorMapCW;
					this.mapWidth = map.getHeight(null);
					this.mapHeight = map.getWidth(null);
					break;
				case East:
					// Rotate picture CCW
					int[] colorMapCCW = new int[colorMap.length];
					for (int y = 0; y < this.mapHeight; y++)
					{
						for (int x = 0; x < this.mapWidth; x++)
						{
							colorMapCCW[(this.mapWidth - 1 - x) * this.mapHeight + y] = colorMap[y * this.mapWidth + x];
						}
					}
					colorMap = colorMapCCW;
					this.mapWidth = map.getHeight(null);
					this.mapHeight = map.getWidth(null);
					break;
			}

			this.biomeMap = new int[colorMap.length];

			for (int nColor = 0; nColor < colorMap.length; nColor++)
			{
				int color = colorMap[nColor] & 0x00FFFFFF;

				if (data.biomeColorMap.containsKey(color))
				{
					this.biomeMap[nColor] = data.biomeColorMap.get(color);
				} else {
					// ContinueNormal interprets a -1 as "Use the childLayer"
					if (this.data.imageMode == ImageMode.ContinueNormal)
					{
						this.biomeMap[nColor] = -1;
					} else {
						this.biomeMap[nColor] = this.data.imageFillBiome;
					}
				}
			}
		}
		catch (IOException ioexception)
		{
			logger.log(LogLevel.FATAL, LogCategory.CONFIGS, String.format("FromImageLayer encountered a critical error: ", (Object[])ioexception.getStackTrace()));
			throw new RuntimeException(String.format("FromImageLayer encountered a critical error: ", (Object[])ioexception.getStackTrace()));
		}
	}

	@Override
	public int sample(LayerSampleContext<?> context, ILayerSampler parent, int x, int z)
	{
		int Buffer_x;
		int Buffer_z;
		int Buffer_xq;
		int Buffer_zq;
		switch (this.data.imageMode)
		{
			case Repeat:
				Buffer_x = (x - this.data.imageXOffset) % this.mapWidth;
				Buffer_z = (z - this.data.imageZOffset) % this.mapHeight;

				// Take care of negatives
				if (Buffer_x < 0)
				{
					Buffer_x += this.mapWidth;
				}
				if (Buffer_z < 0)
				{
					Buffer_z += this.mapHeight;
				}
				return this.biomeMap[Buffer_x + Buffer_z * this.mapWidth];
			case Mirror:
				// Improved repeat mode
				Buffer_xq = (x - this.data.imageXOffset) % (2 * this.mapWidth);
				Buffer_zq = (z - this.data.imageZOffset) % (2 * this.mapHeight);
				if (Buffer_xq < 0)
				{
					Buffer_xq += 2 * this.mapWidth;
				}
				if (Buffer_zq < 0)
				{
					Buffer_zq += 2 * this.mapHeight;
				}
				Buffer_x = Buffer_xq % this.mapWidth;
				Buffer_z = Buffer_zq % this.mapHeight;
				if (Buffer_xq >= this.mapWidth)
				{
					Buffer_x = this.mapWidth - 1 - Buffer_x;
				}
				if (Buffer_zq >= this.mapHeight)
				{
					Buffer_z = this.mapHeight - 1 - Buffer_z;
				}
				return this.biomeMap[Buffer_x + Buffer_z * this.mapWidth];
			case ContinueNormal:
				int childBiome = 0;
				Buffer_x = x - this.data.imageXOffset;
				Buffer_z = z - this.data.imageZOffset;
				// if X or Z is outside map bounds
				if (Buffer_x < 0 || Buffer_x >= this.mapWidth || Buffer_z < 0 || Buffer_z >= this.mapHeight)
				{
					if (parent != null)
					{
						childBiome = parent.sample(x, z);
						return childBiome;
					} else {
						return this.data.imageFillBiome;
					}
				} else {
					int biome_id_buffer = this.biomeMap[Buffer_x + Buffer_z * this.mapWidth];
					// If set to -1 in the constructor above, uses the childlayer instead of the fillbiome if it exists.
					if (biome_id_buffer == -1)
					{
						if (parent != null)
						{
							childBiome = parent.sample(x, z);
							return childBiome;
						} else {
							return this.data.imageFillBiome;
						}
					}
					return biome_id_buffer;
				}
			case FillEmpty:
				// Some fastened version
				Buffer_x = x - this.data.imageXOffset;
				Buffer_z = z - this.data.imageZOffset;
				if (Buffer_x < 0 || Buffer_x >= this.mapWidth || Buffer_z < 0 || Buffer_z >= this.mapHeight)
				{
					return this.data.imageFillBiome;
				} else {
					return this.biomeMap[Buffer_x + Buffer_z * this.mapWidth];
				}
		}
		
		return parent.sample(x, z);
	}
}
