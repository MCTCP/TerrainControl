package com.pg85.otg.paper.util;

import com.pg85.otg.OTG;
import com.pg85.otg.paper.gen.PaperWorldGenRegion;
import com.pg85.otg.util.nbt.LocalNBTHelper;
import com.pg85.otg.util.nbt.NamedBinaryTag;
import com.pg85.otg.util.gen.LocalWorldGenRegion;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.text.MessageFormat;
import java.util.Set;

public class PaperNBTHelper extends LocalNBTHelper
{
	@Override
	public NamedBinaryTag getNBTFromLocation(LocalWorldGenRegion world, int x, int y, int z)
	{
		BlockEntity tileEntity = ((PaperWorldGenRegion) world).getTileEntity(new BlockPos(x, y, z));
		if (tileEntity == null)
		{
			return null;
		}
		CompoundTag nmsTag = new CompoundTag();
		tileEntity.save(nmsTag);
		nmsTag.remove("x");
		nmsTag.remove("y");
		nmsTag.remove("z");
		return getNBTFromNMSTagCompound(null, nmsTag);
	}

	/**
	 * Converts a net.minecraft.server compound NBT tag to a
	 * net.minecraftwiki.wiki.NBTClass NBT compound tag.
	 *
	 * @param name	Name of the Minecraft tag.
	 * @param nmsTag The Minecraft tag.
	 * @return The converted tag.
	 */
	// ^ We know that NBTTagCompound.map is a Map<String, NBTBase>
	// So it is safe to suppress this warning
	@SuppressWarnings("unchecked")
	public static NamedBinaryTag getNBTFromNMSTagCompound(String name, CompoundTag nmsTag)
	{
		NamedBinaryTag compoundTag = new NamedBinaryTag(NamedBinaryTag.Type.TAG_Compound, name,
			new NamedBinaryTag[]{new NamedBinaryTag(NamedBinaryTag.Type.TAG_End, null, null)});

		Set<String> keys = nmsTag.getAllKeys();

		// Add all child tags to the compound tag
		for (String key : keys)
		{
			Tag nmsChildTag = nmsTag.get(key);

			if (nmsChildTag == null)
			{
				if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.CUSTOM_OBJECTS))
				{
					OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.CUSTOM_OBJECTS, "Failed to read NBT property " + key + " from tag " + nmsTag.getAsString());
				}
				continue;
			}

			NamedBinaryTag.Type type = NamedBinaryTag.Type.values()[nmsChildTag.getId()];
			switch (type)
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
					compoundTag.addTag(new NamedBinaryTag(type, key, getValueFromNms(nmsChildTag)));
					break;
				case TAG_List:
					NamedBinaryTag listChildTag = getNBTFromNMSTagList(key, (ListTag) nmsChildTag);
					if (listChildTag != null)
					{
						compoundTag.addTag(listChildTag);
					}
					break;
				case TAG_Compound:
					compoundTag.addTag(getNBTFromNMSTagCompound(key, (CompoundTag) nmsChildTag));
					break;
				case TAG_End:
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
	 * @param name		Name of the Minecraft tag.
	 * @param nmsListTag The Minecraft tag.
	 * @return The converted tag.
	 */
	private static NamedBinaryTag getNBTFromNMSTagList(String name, ListTag nmsListTag)
	{
		if (nmsListTag.size() == 0)
		{
			// Nothing to return
			return null;
		}

		NamedBinaryTag.Type listType = NamedBinaryTag.Type.values()[nmsListTag.size()];
		NamedBinaryTag listTag = new NamedBinaryTag(name, listType);

		// Add all child tags
		for (int i = 0; i < nmsListTag.size(); i++)
		{
			switch (listType)
			{
				case TAG_Int_Array:
					listTag.addTag(new NamedBinaryTag(listType, null, nmsListTag.get(i)));
					break;
				case TAG_Float:
					listTag.addTag(new NamedBinaryTag(listType, null, nmsListTag.getFloat(i)));
					break;
				case TAG_Double:
					listTag.addTag(new NamedBinaryTag(listType, null, nmsListTag.getDouble(i)));
					break;
				case TAG_String:
					listTag.addTag(new NamedBinaryTag(listType, null, nmsListTag.getString(i)));
					break;
				case TAG_Compound:
					listTag.addTag(getNBTFromNMSTagCompound(null, (CompoundTag) nmsListTag.get(i)));
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
	 * Gets the value from a nms tag (since that object doesn't have a simple value
	 * field)
	 *
	 * @param nmsTag The minecraft tag
	 * @return The value contained in the tag
	 */
	private static Object getValueFromNms(Tag nmsTag)
	{
		NamedBinaryTag.Type type = NamedBinaryTag.Type.values()[nmsTag.getId()];
		return switch (type)
			{
				case TAG_Byte -> ((ByteTag) nmsTag).getAsByte();
				case TAG_Short -> ((ShortTag) nmsTag).getAsShort();
				case TAG_Int -> ((IntTag) nmsTag).getAsInt();
				case TAG_Long -> ((LongTag) nmsTag).getAsLong();
				case TAG_Float -> ((FloatTag) nmsTag).getAsFloat();
				case TAG_Double -> ((DoubleTag) nmsTag).getAsDouble();
				case TAG_Byte_Array -> ((ByteArrayTag) nmsTag).getAsByteArray();
				case TAG_String -> nmsTag.getAsString();
				case TAG_Int_Array -> ((IntArrayTag) nmsTag).getAsIntArray();
				default ->
					// Cannot read this from a tag
					throw new IllegalArgumentException(type + "doesn't have a simple value!");
			};
	}

	/**
	 * Converts a net.minecraftwiki.wiki.NBTClass NBT compound tag into an
	 * net.minecraft.server NBT compound tag.
	 *
	 * @param compoundTag Our tag.
	 * @return The Minecraft tag.
	 */
	public static CompoundTag getNMSFromNBTTagCompound(NamedBinaryTag compoundTag)
	{
		CompoundTag nmsTag = new CompoundTag();
		NamedBinaryTag[] childTags = (NamedBinaryTag[]) compoundTag.getValue();
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
					nmsTag.put(tag.getName(), createTagNms(tag.getType(), tag.getValue()));
					break;
				case TAG_List:
					nmsTag.put(tag.getName(), getNMSFromNBTTagList(tag));
					break;
				case TAG_Compound:
					nmsTag.put(tag.getName(), getNMSFromNBTTagCompound(tag));
					break;
				case TAG_End:
				default:
					break;
			}
		}
		return nmsTag;
	}

	/**
	 * Converts a 'net.minecraftwiki.wiki.NBTClass NBT list tag into an
	 * net.minecraft.server NBT list tag.
	 *
	 * @param listTag OTG nbt tag
	 * @return An equivalent NMS list NBT
	 */
	private static ListTag getNMSFromNBTTagList(NamedBinaryTag listTag)
	{
		ListTag nmsTag = new ListTag();
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
	 * Creates a net.minecraft.server.NBTBase tag. Doesn't work for ends, lists and
	 * compounds.
	 *
	 * @param type  The NBT tag type
	 * @param value The NBT tag value
	 * @return the minecraft NBT tag
	 */
	private static Tag createTagNms(NamedBinaryTag.Type type, Object value)
	{
		return switch (type)
			{
				case TAG_Byte -> ByteTag.valueOf((Byte) value);
				case TAG_Short -> ShortTag.valueOf((Short) value);
				case TAG_Int -> IntTag.valueOf((Integer) value);
				case TAG_Long -> LongTag.valueOf((Long) value);
				case TAG_Float -> FloatTag.valueOf((Float) value);
				case TAG_Double -> DoubleTag.valueOf((Double) value);
				case TAG_Byte_Array -> new ByteArrayTag((byte[]) value);
				case TAG_String -> StringTag.valueOf((String) value);
				case TAG_Int_Array -> new IntArrayTag((int[]) value);
				default ->
					// Cannot make this into a tag
					throw new IllegalArgumentException(type + "doesn't have a simple value!");
			};
	}
}
