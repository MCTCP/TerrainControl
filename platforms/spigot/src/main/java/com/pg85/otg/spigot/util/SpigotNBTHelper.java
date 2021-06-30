package com.pg85.otg.spigot.util;

import com.pg85.otg.OTG;
import com.pg85.otg.spigot.gen.SpigotWorldGenRegion;
import com.pg85.otg.util.bo3.LocalNBTHelper;
import com.pg85.otg.util.bo3.NamedBinaryTag;
import com.pg85.otg.util.gen.LocalWorldGenRegion;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;

import net.minecraft.server.v1_16_R3.*;

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.Map;

public class SpigotNBTHelper extends LocalNBTHelper
{
	@Override
	public NamedBinaryTag getNBTFromLocation(LocalWorldGenRegion world, int x, int y, int z)
	{
		TileEntity tileEntity = ((SpigotWorldGenRegion) world).getTileEntity(new BlockPosition(x, y, z));
		if (tileEntity == null)
		{
			return null;
		}
		NBTTagCompound nmsTag = new NBTTagCompound();
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
	public static NamedBinaryTag getNBTFromNMSTagCompound(String name, NBTTagCompound nmsTag)
	{
		NamedBinaryTag compoundTag = new NamedBinaryTag(NamedBinaryTag.Type.TAG_Compound, name,
			new NamedBinaryTag[]{new NamedBinaryTag(NamedBinaryTag.Type.TAG_End, null, null)});

		// Get the child tags using some reflection magic
		Field mapField;
		Map<String, NBTBase> nmsChildTags = null;
		try
		{
			mapField = NBTTagCompound.class.getDeclaredField("map");
			mapField.setAccessible(true);
			nmsChildTags = (Map<String, NBTBase>) mapField.get(nmsTag);
		}
		catch (Exception e)
		{			
			if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.CUSTOM_OBJECTS))
			{
				OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.CUSTOM_OBJECTS, String.format("SpigotNBTHelper: ", (Object[]) e.getStackTrace()));	
			}
		}

		if (nmsChildTags == null)
		{
			// Cannot load the tag, return an empty tag
			return compoundTag;
		}

		// Add all child tags to the compound tag
		for (Map.Entry<String, NBTBase> entry : nmsChildTags.entrySet())
		{
			NBTBase nmsChildTag = entry.getValue();
			NamedBinaryTag.Type type = NamedBinaryTag.Type.values()[nmsChildTag.getTypeId()];
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
					compoundTag.addTag(new NamedBinaryTag(type, entry.getKey(), getValueFromNms(nmsChildTag)));
					break;
				case TAG_List:
					NamedBinaryTag listChildTag = getNBTFromNMSTagList(entry.getKey(), (NBTTagList) nmsChildTag);
					if (listChildTag != null)
					{
						compoundTag.addTag(listChildTag);
					}
					break;
				case TAG_Compound:
					compoundTag.addTag(getNBTFromNMSTagCompound(entry.getKey(), (NBTTagCompound) nmsChildTag));
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
	private static NamedBinaryTag getNBTFromNMSTagList(String name, NBTTagList nmsListTag)
	{
		if (nmsListTag.size() == 0)
		{
			// Nothing to return
			return null;
		}

		NamedBinaryTag.Type listType = NamedBinaryTag.Type.values()[nmsListTag.d_()];
		NamedBinaryTag listTag = new NamedBinaryTag(name, listType);

		// Add all child tags
		for (int i = 0; i < nmsListTag.size(); i++)
		{
			switch (listType)
			{
				case TAG_Int_Array:
					listTag.addTag(new NamedBinaryTag(listType, null, nmsListTag.d(i)));
					break;
				case TAG_Float:
					listTag.addTag(new NamedBinaryTag(listType, null, nmsListTag.i(i)));
					break;
				case TAG_Double:
					listTag.addTag(new NamedBinaryTag(listType, null, nmsListTag.f(i)));
					break;
				case TAG_String:
					listTag.addTag(new NamedBinaryTag(listType, null, nmsListTag.getString(i)));
					break;
				case TAG_Compound:
					listTag.addTag(getNBTFromNMSTagCompound(null, (NBTTagCompound) nmsListTag.get(i)));
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
	private static Object getValueFromNms(NBTBase nmsTag)
	{
		NamedBinaryTag.Type type = NamedBinaryTag.Type.values()[nmsTag.getTypeId()];
		switch (type)
		{
			case TAG_Byte:
				return ((NBTTagByte) nmsTag).asByte();
			case TAG_Short:
				return ((NBTTagShort) nmsTag).asShort();
			case TAG_Int:
				return ((NBTTagInt) nmsTag).asInt();
			case TAG_Long:
				return ((NBTTagLong) nmsTag).asLong();
			case TAG_Float:
				return ((NBTTagFloat) nmsTag).asFloat();
			case TAG_Double:
				return ((NBTTagDouble) nmsTag).asDouble();
			case TAG_Byte_Array:
				return ((NBTTagByteArray) nmsTag).getBytes();
			case TAG_String:
				return nmsTag.asString();
			case TAG_Int_Array:
				return ((NBTTagIntArray) nmsTag).getInts();
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
	public static NBTTagCompound getNMSFromNBTTagCompound(NamedBinaryTag compoundTag)
	{
		NBTTagCompound nmsTag = new NBTTagCompound();
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
					nmsTag.set(tag.getName(), createTagNms(tag.getType(), tag.getValue()));
					break;
				case TAG_List:
					nmsTag.set(tag.getName(), getNMSFromNBTTagList(tag));
					break;
				case TAG_Compound:
					nmsTag.set(tag.getName(), getNMSFromNBTTagCompound(tag));
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
	private static NBTTagList getNMSFromNBTTagList(NamedBinaryTag listTag)
	{
		NBTTagList nmsTag = new NBTTagList();
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
	private static NBTBase createTagNms(NamedBinaryTag.Type type, Object value)
	{
		switch (type)
		{
			case TAG_Byte:
				return NBTTagByte.a((Byte) value);
			case TAG_Short:
				return NBTTagShort.a((Short) value);
			case TAG_Int:
				return NBTTagInt.a((Integer) value);
			case TAG_Long:
				return NBTTagLong.a((Long) value);
			case TAG_Float:
				return NBTTagFloat.a((Float) value);
			case TAG_Double:
				return NBTTagDouble.a((Double) value);
			case TAG_Byte_Array:
				return new NBTTagByteArray((byte[]) value);
			case TAG_String:
				return NBTTagString.a((String) value);
			case TAG_Int_Array:
				return new NBTTagIntArray((int[]) value);
			default:
				// Cannot make this into a tag
				throw new IllegalArgumentException(type + "doesn't have a simple value!");
		}
	}
}
