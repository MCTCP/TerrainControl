package com.pg85.otg.customobject.bofunctions;

import java.util.List;
import java.util.Random;

import com.pg85.otg.customobject.config.CustomObjectConfigFile;
import com.pg85.otg.customobject.config.CustomObjectConfigFunction;
import com.pg85.otg.util.nbt.NBTHelper;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IWorldGenRegion;
import com.pg85.otg.util.biome.ReplaceBlockMatrix;
import com.pg85.otg.util.nbt.NamedBinaryTag;
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
				if (nbt.getTag("Items") != null) {
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
				}

				if (nbt.getTag("SkullType") != null) {
					byte val = (byte)nbt.getTag("SkullType").getValue();
					switch ((int)val) {
						case 1:
							this.material = materialReader.readMaterial("minecraft:wither_skeleton_skull");
							break;
						case 2:
							this.material = materialReader.readMaterial("minecraft:zombie_head");
							break;
						case 3:
							this.material = materialReader.readMaterial("minecraft:player_head");
							break;
						case 4:
							this.material = materialReader.readMaterial("minecraft:creeper_head");
							break;
						case 5:
							this.material = materialReader.readMaterial("minecraft:dragon_head");
							break;
						default:
							this.material = materialReader.readMaterial("minecraft:skeleton_skull");
					}
				}

				if (nbt.getTag("Item") != null) {
					if (nbt.getTag("Item").getType() == NamedBinaryTag.Type.TAG_String) {
						String val = (String) nbt.getTag("Item").getValue();
						String[]vals = val.split(":");
						this.material = materialReader.readMaterial("minecraft:potted_" + (val.length() > 0 ? val.split(":")[1] : val));
					} else if (nbt.getTag("Item").getType() == NamedBinaryTag.Type.TAG_Int) {
						String val = materialReader.readMaterial(Integer.toString(((int)nbt.getTag("Item").getValue()))).getName();
						if (val.split(":").length > 1) {
							this.material = materialReader.readMaterial("minecraft:potted_" + val.split(":")[1]);
						} else {
							this.material = materialReader.readMaterial("minecraft:potted_" + val);
						}
					}
					/*
					 * The following commented-out code is a testemant to Frank's stupidity.
					 * To anyone reading this, Frank is a complete and total idiot.
					 */
				 /*if (val.equals("minecraft:cactus")) {
				 	this.material = materialReader.readMaterial("minecraft:potted_cactus");
				 } else if (val.equals("minecraft:dandelion")) {
				 	this.material = materialReader.readMaterial("minecraft:potted_dandelion");
				 } else if (val.equals("minecraft:poppy")) {
				 	this.material = materialReader.readMaterial("minecraft:potted_poppy");
				 } else if (val.equals("minecraft:blue_orchid")) {
				 	this.material = materialReader.readMaterial("minecraft:potted_blue_orchid");
				 } else if (val.equals("minecraft:allium")) {
				 	this.material = materialReader.readMaterial("minecraft:potted_allium");
				 } else if (val.equals("minecraft:azure_bluet")) {
				 	this.material = materialReader.readMaterial("minecraft:potted_azure_bluet");
				 } else if (val.equals("minecraft:red_tulip")) {
				 	this.material = materialReader.readMaterial("minecraft:potted_red_tulip");
				 } else if (val.equals("minecraft:orange_tulip")) {
				 	this.material = materialReader.readMaterial("minecraft:potted_orange_tulip");
				 } else if (val.equals("minecraft:white_tulip")) {
				 	this.material = materialReader.readMaterial("minecraft:potted_white_tulip");
				 } else if (val.equals("minecraft:pink_tulip")) {
				 	this.material = materialReader.readMaterial("minecraft:potted_pink_tulip");
				 } else if (val.equals("minecraft:oxeye_daisy")) {
				 	this.material = materialReader.readMaterial("minecraft:potted_oxeye_daisy");
				 } else if (val.equals("minecraft:cornflower")) {
				 	this.material = materialReader.readMaterial("minecraft:potted_cornflower");
				 } else if (val.equals("minecraft:lily_of_the_valley")) {
				 	this.material = materialReader.readMaterial("minecraft:potted_lily_of_the_valley");
				 } else if (val.equals("minecraft:wither_rose")) {
				 	this.material = materialReader.readMaterial("minecraft:potted_wither_rose");
				 } else if (val.equals("minecraft:oak_sapling")) {
				 	this.material = materialReader.readMaterial("minecraft:potted_oak_sapling");
				 } else if (val.equals("minecraft:spruce_sapling")) {
				 	this.material = materialReader.readMaterial("minecraft:potted_spruce_sapling");
				 } else if (val.equals("minecraft:birch_sapling")) {
				 	this.material = materialReader.readMaterial("minecraft:potted_birch_sapling");
				 } else if (val.equals("minecraft:"))*/
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
