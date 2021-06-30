package com.pg85.otg.forge.util;

import com.pg85.otg.OTG;
import com.pg85.otg.forge.gen.ForgeWorldGenRegion;
import com.pg85.otg.util.bo3.LocalNBTHelper;
import com.pg85.otg.util.bo3.NamedBinaryTag;
import com.pg85.otg.util.gen.LocalWorldGenRegion;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;

import net.minecraft.nbt.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import java.text.MessageFormat;
import java.util.Set;

public class ForgeNBTHelper extends LocalNBTHelper
{
	@Override
	public NamedBinaryTag getNBTFromLocation(LocalWorldGenRegion world, int x, int y, int z)
	{
		TileEntity tileEntity = ((ForgeWorldGenRegion) world).getTileEntity(new BlockPos(x, y, z));
		if (tileEntity == null)
		{
			return null;
		}
		CompoundNBT nbt = new CompoundNBT();
		tileEntity.save(nbt);
		// Double up, just to be safe. It should be x, y, z.
		//nmsTag.remove("pos");
		nbt.remove("x");
		nbt.remove("y");
		nbt.remove("z");
		return getNBTFromNMSTagCompound(null, nbt);
	}

	/**
	 * Converts a net.minecraft.server compound NBT tag to a
	 * net.minecraftwiki.wiki.NBTClass NBT compound tag.
	 *
	 * @param name		Name of the Minecraft tag.
	 * @param compoundNBT The Minecraft tag.
	 * @return The converted tag.
	 */
	public static NamedBinaryTag getNBTFromNMSTagCompound(String name, CompoundNBT compoundNBT)
	{
		NamedBinaryTag compoundTag = new NamedBinaryTag(NamedBinaryTag.Type.TAG_Compound, name, new NamedBinaryTag[]{new NamedBinaryTag(NamedBinaryTag.Type.TAG_End, null, null)});

		// Get the child tags using some reflection magic
		//Map<String, INBT> nmsChildTags = ObfuscationReflectionHelper.getPrivateValue(CompoundNBT.class, compoundNBT, "field_74784_a");

		//if (nmsChildTags == null)
		//{
		// Cannot load the tag, return an empty tag
		//	return compoundTag;
		//}

		Set<String> keys = compoundNBT.getAllKeys();

		// Add all child tags to the compound tag
		for (String key : keys)
		{
			INBT nmsChildTag = compoundNBT.get(key);

			if (nmsChildTag == null)
			{
				if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.CUSTOM_OBJECTS))
				{
					OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.CUSTOM_OBJECTS, "Failed to read NBT property " + key + " from tag " + compoundNBT.getId());
				}
				continue;
			}

			NamedBinaryTag.Type type = NamedBinaryTag.Type.values()[nmsChildTag.getId()];
			switch (type)
			{
				case TAG_End:
					break;
				case TAG_Byte:
				case TAG_Short:
				case TAG_Int:
				case TAG_Long:
				case TAG_Float:
				case TAG_Double:
				case TAG_Byte_Array:
				case TAG_String:
				case TAG_Int_Array:
					compoundTag.addTag(new NamedBinaryTag(type, key, getValueFromNms(nmsChildTag)));
					break;
				case TAG_List:
					NamedBinaryTag listChildTag = getNBTFromNMSTagList(key, (ListNBT) nmsChildTag);
					if (listChildTag != null)
					{
						compoundTag.addTag(listChildTag);
					}
					break;
				case TAG_Compound:
					compoundTag.addTag(getNBTFromNMSTagCompound(key, (CompoundNBT) nmsChildTag));
					break;
				default:
					break;
			}
		}

		return compoundTag;
	}

	/**
	 * Converts a net.minecraft.server list NBT tag to a
	 * net.minecraftwiki.wiki.NBTClass NBT list tag.
	 *
	 * @param name	Name of the Minecraft tag.
	 * @param listNBT The Minecraft tag.
	 * @return The converted tag.
	 */
	private static NamedBinaryTag getNBTFromNMSTagList(String name, ListNBT listNBT)
	{
		if (listNBT.size() == 0)
		{
			// Nothing to return
			return null;
		}

		NamedBinaryTag.Type listType = NamedBinaryTag.Type.values()[listNBT.getElementType()];
		NamedBinaryTag listTag = new NamedBinaryTag(name, listType);

		// Add all child tags
		for (int i = 0; i < listNBT.size(); i++)
		{
			INBT nmsChildTag = listNBT.get(i);
			switch (listType)
			{
				case TAG_End:
					break;
				case TAG_Byte:
				case TAG_Short:
				case TAG_Int:
				case TAG_Long:
				case TAG_Float:
				case TAG_Double:
				case TAG_Byte_Array:
				case TAG_String:
				case TAG_Int_Array:
					listTag.addTag(new NamedBinaryTag(listType, null, getValueFromNms(nmsChildTag)));
					break;
				case TAG_List:
					NamedBinaryTag listChildTag = getNBTFromNMSTagList(null, (ListNBT) nmsChildTag);
					if (listChildTag != null)
					{
						listTag.addTag(listChildTag);
					}
					break;
				case TAG_Compound:
					listTag.addTag(getNBTFromNMSTagCompound(null, (CompoundNBT) nmsChildTag));
					break;
				default:
					if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.CUSTOM_OBJECTS))
					{
						OTG.getEngine().getLogger().log(
							LogLevel.ERROR,
							LogCategory.CUSTOM_OBJECTS,
							MessageFormat.format(
								"Cannot convert list subtype {0} from it's NMS value", 
								listType
							)
						);
					}
					break;
			}
		}
		return listTag;
	}

	// Internal methods below

	/**
	 * Gets the value from a nms tag (since that object doesn't have a simple
	 * value field)
	 *
	 * @param inbt The Minecraft tag.
	 * @return The value in the tag.
	 */
	private static Object getValueFromNms(INBT inbt)
	{
		NamedBinaryTag.Type type = NamedBinaryTag.Type.values()[inbt.getId()];
		switch (type)
		{
			case TAG_Byte:
				return ((ByteNBT) inbt).getAsByte();
			case TAG_Short:
				return ((ShortNBT) inbt).getAsShort();
			case TAG_Int:
				return ((IntNBT) inbt).getAsInt();
			case TAG_Long:
				return ((LongNBT) inbt).getAsLong();
			case TAG_Float:
				return ((FloatNBT) inbt).getAsFloat();
			case TAG_Double:
				return ((DoubleNBT) inbt).getAsDouble();
			case TAG_Byte_Array:
				return ((ByteArrayNBT) inbt).getAsByteArray();
			case TAG_String:
				return inbt.getAsString();
			case TAG_Int_Array:
				return ((IntArrayNBT) inbt).getAsIntArray();
			default:
				// Cannot read this from a tag
				throw new IllegalArgumentException(type + "doesn't have a simple value!");
		}
	}

	/**
	 * Converts a net.minecraftwiki.wiki.NBTClass NBT compound tag into an
	 * net.minecraft.server NBT compound tag.
	 *
	 * @param compoundTag Our tag.
	 * @return The Minecraft tag.
	 */
	public static CompoundNBT getNMSFromNBTTagCompound(NamedBinaryTag compoundTag)
	{
		CompoundNBT nmsTag = new CompoundNBT();
		NamedBinaryTag[] childTags = (NamedBinaryTag[]) compoundTag.getValue();
		if (childTags == null)
		{
			return nmsTag;
		}
		for (NamedBinaryTag tag : childTags)
		{
			switch (tag.getType())
			{
				case TAG_End:
					break;
				case TAG_Byte:
				case TAG_Short:
				case TAG_Int:
				case TAG_Long:
				case TAG_Float:
				case TAG_Double:
				case TAG_Byte_Array:
				case TAG_String:
				case TAG_Int_Array:
					nmsTag.put(tag.getName(), createTagNms(tag.getType(), tag.getValue()));
					break;
				case TAG_List:
					nmsTag.put(tag.getName(), getNMSFromNBTTagList(tag));
					break;
				case TAG_Compound:
					nmsTag.put(tag.getName(), getNMSFromNBTTagCompound(tag));
					break;
				default:
					break;
			}
		}
		return nmsTag;
	}

	/**
	 * Converts a OpenTerrainGenerator NBT list tag into an net.minecraft.server NBT
	 * list tag.
	 *
	 * @param listTag The OpenTerrainGenerator list tag.
	 * @return The Minecraft list tag.
	 */
	private static ListNBT getNMSFromNBTTagList(NamedBinaryTag listTag)
	{
		ListNBT nmsTag = new ListNBT();
		NamedBinaryTag[] childTags = (NamedBinaryTag[]) listTag.getValue();
		for (NamedBinaryTag tag : childTags)
		{
			switch (tag.getType())
			{
				case TAG_Byte:
				case TAG_Short:
				case TAG_Int:
				case TAG_Long:
				case TAG_Float:
				case TAG_Double:
				case TAG_Byte_Array:
				case TAG_String:
				case TAG_Int_Array:
					nmsTag.add(createTagNms(tag.getType(), tag.getValue()));
					break;
				case TAG_List:
					nmsTag.add(getNMSFromNBTTagList(tag));
					break;
				case TAG_Compound:
					nmsTag.add(getNMSFromNBTTagCompound(tag));
					break;
				case TAG_End:
				default:
					break;
			}
		}
		return nmsTag;
	}

	/**
	 * Creates a Minecraft INBT tag. Doesn't work for ends, lists and
	 * compounds.
	 *
	 * @param type  Type of the tag.
	 * @param value Value of the tag.
	 * @return The Minecraft NBTBast tag.
	 */
	private static INBT createTagNms(NamedBinaryTag.Type type, Object value)
	{
		switch (type)
		{
			case TAG_Byte:
				return ByteNBT.valueOf((Byte) value);
			case TAG_Short:
				return ShortNBT.valueOf((Short) value);
			case TAG_Int:
				return IntNBT.valueOf((Integer) value);
			case TAG_Long:
				return LongNBT.valueOf((Long) value);
			case TAG_Float:
				return FloatNBT.valueOf((Float) value);
			case TAG_Double:
				return DoubleNBT.valueOf((Double) value);
			case TAG_Byte_Array:
				return new ByteArrayNBT((byte[]) value);
			case TAG_String:
				return StringNBT.valueOf((String) value);
			case TAG_Int_Array:
				return new IntArrayNBT((int[]) value);
			default:
				// Cannot make this into a tag
				throw new IllegalArgumentException(type + "doesn't have a simple value!");
		}
	}
}
