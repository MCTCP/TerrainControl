package com.pg85.otg.customobject.bofunctions;

import java.util.List;
import java.util.Random;

import com.pg85.otg.customobject.config.CustomObjectConfigFile;
import com.pg85.otg.customobject.config.CustomObjectConfigFunction;
import com.pg85.otg.customobject.util.NBTHelper;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IWorldGenRegion;
import com.pg85.otg.util.biome.ReplaceBlockMatrix;
import com.pg85.otg.util.bo3.NamedBinaryTag;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.minecraft.BlockNames;

/**
 * Represents a block in a BO3.
 */
public abstract class BlockFunction<T extends CustomObjectConfigFile> extends CustomObjectConfigFunction<T>
{
	public LocalMaterialData material;
	public short y;
	public NamedBinaryTag nbt;
	public String nbtName;

	@Override
	public void load(List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
	{
		assureSize(4, args);
		// Those limits are arbitrary, LocalWorld.setBlock will limit it
		// correctly based on what chunks can be accessed
		x = readInt(args.get(0), -100, 100);
		y = (short) readInt(args.get(1), -1000, 1000);
		z = readInt(args.get(2), -100, 100);

		material = readMaterial(args.get(3), materialReader);
		if(material == null)
		{
			throw new InvalidConfigException("Material \"" + args.get(3) + "\" could not be read.");
		}

		if (args.size() >= 5)
		{
			// Code that converts legacy block ids inside chests - Frank
			nbt = NBTHelper.loadMetadata(args.get(4), getHolder().getFile(), logger);
			if (nbt != null)
			{
				if (nbt.getTag("Items") == null) {
					return;
				}
				for (NamedBinaryTag item : (NamedBinaryTag[]) nbt.getTag("Items").getValue()) {
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
				nbtName = args.get(4);
			}
		}
	}

	@Override
	public String makeString()
	{
		String start = "B(" + x + ',' + y + ',' + z + ',' + material;
		if (nbt != null)
		{
			start += ',' + nbtName;
		}
		return start + ')';
	}

	public abstract void spawn(IWorldGenRegion worldGenRegion, Random random, int x, int y, int z);
	
	public abstract void spawn(IWorldGenRegion worldGenRegion, Random random, int x, int y, int z, ReplaceBlockMatrix replaceBlocks);
	
	@Override
	public boolean isAnalogousTo(CustomObjectConfigFunction<T> other)
	{
		if (!getClass().equals(other.getClass()))
		{
			return false;
		}
		BlockFunction<T> block = (BlockFunction<T>) other;
		return block.x == x && block.y == y && block.z == z;
	}
}
