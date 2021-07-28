package com.pg85.otg.customobject.bo3.bo3function;

import java.util.List;
import java.util.Random;

import com.pg85.otg.customobject.bo3.BO3Config;
import com.pg85.otg.customobject.util.NBTHelper;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IWorldGenRegion;
import com.pg85.otg.util.biome.ReplaceBlockMatrix;
import com.pg85.otg.util.bo3.NamedBinaryTag;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.minecraft.BlockNames;

public class BO3RandomBlockFunction extends BO3BlockFunction
{
	public LocalMaterialData[] blocks;
	public byte[] blockChances;
	public String[] metaDataNames;
	public NamedBinaryTag[] metaDataTags;

	public byte blockCount = 0;
	
	public BO3RandomBlockFunction() { }
	
	public BO3RandomBlockFunction(BO3Config holder)
	{
		super(holder);
	}
	
	public BO3RandomBlockFunction rotate()
	{
		BO3RandomBlockFunction rotatedBlock = new BO3RandomBlockFunction();
		rotatedBlock.x = z;
		rotatedBlock.y = y;
		rotatedBlock.z = -x;
		rotatedBlock.blockCount = blockCount;
		rotatedBlock.blocks = new LocalMaterialData[blockCount];
		for (int i = 0; i < blockCount; i++)
		{
			rotatedBlock.blocks[i] = blocks[i].rotate();
		}
		rotatedBlock.blockChances = blockChances;
		rotatedBlock.metaDataTags = metaDataTags;
		rotatedBlock.metaDataNames = metaDataNames;

		return rotatedBlock;
	}
	
	@Override
	public void load(List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
	{
		assureSize(5, args);
		x = readInt(args.get(0), -100, 100);
		y = (short) readInt(args.get(1), -1000, 1000);
		z = readInt(args.get(2), -100, 100);

		// Now read the random parts
		int i = 3;
		int size = args.size();

		// Get number of blocks first, params can vary so can't just count.
		while (i < size)
		{			
			i++;
			if (i >= size)
			{
				throw new InvalidConfigException("Missing chance parameter");
			}
			try
			{
				readInt(args.get(i), 1, 100);
			}
			catch (InvalidConfigException e)
			{
				// Get the chance
				i++;
				if (i >= size)
				{
					throw new InvalidConfigException("Missing chance parameter");
				}
				readInt(args.get(i), 1, 100);
			}
			i++;
			blockCount++;
		}
		
		this.blocks = new LocalMaterialData[blockCount];
		this.blockChances = new byte[blockCount];
		this.metaDataNames = new String[blockCount];
		this.metaDataTags = new NamedBinaryTag[blockCount];
		
		i = 3;
		blockCount = 0;
		while (i < size)
		{
			// Parse chance and metadata
			this.blocks[blockCount] = materialReader.readMaterial(args.get(i));
			i++;
			if (i >= size)
			{
				throw new InvalidConfigException("Missing chance parameter");
			}
			try
			{
				blockChances[blockCount] = (byte) readInt(args.get(i), 1, 100);
			}
			catch (InvalidConfigException e)
			{
				// Maybe it's a NBT file?

				// Get the file
				NamedBinaryTag metaData = NBTHelper.loadMetadata(args.get(i), this.getHolder().getFile(), logger);
				if (metaData != null)
				{
					if (metaData.getTag("Items") == null) {
						continue;
					}
					// Code that converts legacy block ids inside chests - Frank
					for (NamedBinaryTag item : (NamedBinaryTag[]) metaData.getTag("Items").getValue()) {
						if (item.getTag("id").getType() == NamedBinaryTag.Type.TAG_Short) {
							short val = (short)item.getTag("id").getValue();
							item.removeSubTag(item.getTag("id"));
							NamedBinaryTag[] newItemValue = new NamedBinaryTag[((NamedBinaryTag[])item.getValue()).length + 1];
							System.arraycopy(item.getValue(), 0, newItemValue, 0, newItemValue.length - 1);
							String strVal = "minecraft:" + BlockNames.blockNameFromLegacyBlockId(val);
							newItemValue[newItemValue.length-2] = new NamedBinaryTag(NamedBinaryTag.Type.TAG_String, "id", strVal);
							newItemValue[newItemValue.length-1] = new NamedBinaryTag(NamedBinaryTag.Type.TAG_End, "", null);
							item.setValue(newItemValue);
						}
					}
					metaDataNames[blockCount] = args.get(i);
					metaDataTags[blockCount] = metaData;
				}

				// Get the chance
				i++;
				if (i >= size)
				{
					throw new InvalidConfigException("Missing chance parameter");
				}
				blockChances[blockCount] = (byte) readInt(args.get(i), 1, 100);
			}

			i++;
			blockCount++;
		}
	}

	@Override
	public String makeString()
	{
		String text = "RB(" + x + "," + y + "," + z;
		for (int i = 0; i < blockCount; i++)
		{
			if (metaDataTags[i] == null)
			{
				text += "," + blocks[i] + "," + blockChances[i];
			} else
			{
				text += "," + blocks[i] + "," + metaDataNames[i] + "," + blockChances[i];
			}
		}
		return text + ")";
	}	

	@Override
	public void spawn(IWorldGenRegion worldGenRegion, Random random, int x, int y, int z)
	{
		for (int i = 0; i < blockCount; i++)
		{
			if (random.nextInt(100) < blockChances[i])
			{
				worldGenRegion.setBlock(x, y, z, blocks[i], metaDataTags[i]);
				break;
			}
		}
	}
	
	@Override
	public void spawn(IWorldGenRegion worldGenRegion, Random random, int x, int y, int z, ReplaceBlockMatrix replaceBlocks)
	{
		for (int i = 0; i < blockCount; i++)
		{
			if (random.nextInt(100) < blockChances[i])
			{
				worldGenRegion.setBlock(x, y, z, blocks[i], metaDataTags[i], replaceBlocks);
				break;
			}
		}
	}
	
	@Override
	public Class<BO3Config> getHolderType()
	{
		return BO3Config.class;
	}
}
