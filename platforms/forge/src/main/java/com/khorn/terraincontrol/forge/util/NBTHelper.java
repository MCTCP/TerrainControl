package com.khorn.terraincontrol.forge.util;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.logging.LogMarker;
import com.khorn.terraincontrol.util.NamedBinaryTag;
import net.minecraft.nbt.*;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Map.Entry;

public class NBTHelper
{

    /**
     * Converts a net.minecraft.server compound NBT tag to a
     * net.minecraftwiki.wiki.NBTClass NBT compound tag.
     *
     * @param name   Name of the Minecraft tag.
     * @param nmsTag The Minecraft tag.
     * @return The converted tag.
     */
    @SuppressWarnings("unchecked")
    // ^ We know that NBTTagCompound.map is a Map<String, NBTBase>
    // So it is safe to suppress this warning
    public static NamedBinaryTag getNBTFromNMSTagCompound(String name, NBTTagCompound nmsTag)
    {
        NamedBinaryTag compoundTag = new NamedBinaryTag(NamedBinaryTag.Type.TAG_Compound, name, new NamedBinaryTag[] {new NamedBinaryTag(NamedBinaryTag.Type.TAG_End, null, null)});

        // Get the child tags using some reflection magic
        Field mapField;
        Map<String, NBTBase> nmsChildTags = null;
        try
        {
            mapField = NBTTagCompound.class.getDeclaredField("map");
            mapField.setAccessible(true);
            nmsChildTags = (Map<String, NBTBase>) mapField.get(nmsTag);
        } catch (Exception e)
        {
            TerrainControl.printStackTrace(LogMarker.FATAL, e);
        }

        if (nmsChildTags == null)
        {
            // Cannot load the tag, return an empty tag
            return compoundTag;
        }

        // Add all child tags to the compound tag
        for (Entry<String, NBTBase> entry : nmsChildTags.entrySet())
        {
            NBTBase nmsChildTag = entry.getValue();
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
     * @param name       Name of the Minecraft tag.
     * @param nmsListTag The Minecraft tag.
     * @return The converted tag.
     */
    private static NamedBinaryTag getNBTFromNMSTagList(String name, NBTTagList nmsListTag)
    {
        if (nmsListTag.tagCount() == 0)
        {
            // Nothing to return
            return null;
        }

        NamedBinaryTag.Type listType = NamedBinaryTag.Type.values()[nmsListTag.getTagType()];
        NamedBinaryTag listTag = new NamedBinaryTag(name, listType);

        // Add all child tags
        for (int i = 0; i < nmsListTag.tagCount(); i++)
        {
            NBTBase nmsChildTag = nmsListTag.get(i);
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
                    NamedBinaryTag listChildTag = getNBTFromNMSTagList(null, (NBTTagList) nmsChildTag);
                    if (listChildTag != null)
                    {
                        listTag.addTag(listChildTag);
                    }
                    break;
                case TAG_Compound:
                    listTag.addTag(getNBTFromNMSTagCompound(null, (NBTTagCompound) nmsChildTag));
                    break;
                default:
                    TerrainControl.log(LogMarker.INFO, "Cannot convert list subtype {} from it's NMS value", new Object[] {listType});
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
     * @param nmsTag The Minecraft tag.
     * @return The value in the tag.
     */
    private static Object getValueFromNms(NBTBase nmsTag)
    {
        NamedBinaryTag.Type type = NamedBinaryTag.Type.values()[nmsTag.getId()];
        switch (type)
        {
            case TAG_Byte:
                return ((NBTTagByte) nmsTag).getByte();
            case TAG_Short:
                return ((NBTTagShort) nmsTag).getShort();
            case TAG_Int:
                return ((NBTTagInt) nmsTag).getInt();
            case TAG_Long:
                return ((NBTTagLong) nmsTag).getLong();
            case TAG_Float:
                return ((NBTTagFloat) nmsTag).getFloat();
            case TAG_Double:
                return ((NBTTagDouble) nmsTag).getDouble();
            case TAG_Byte_Array:
                return ((NBTTagByteArray) nmsTag).getByteArray();
            case TAG_String:
                return ((NBTTagString) nmsTag).getString();
            case TAG_Int_Array:
                return ((NBTTagIntArray) nmsTag).getIntArray();
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
                    nmsTag.setTag(tag.getName(), createTagNms(tag.getType(), tag.getValue()));
                    break;
                case TAG_List:
                    nmsTag.setTag(tag.getName(), getNMSFromNBTTagList(tag));
                    break;
                case TAG_Compound:
                    nmsTag.setTag(tag.getName(), getNMSFromNBTTagCompound(tag));
                    break;
                default:
                    break;
            }
        }
        return nmsTag;
    }

    /**
     * Converts a TerrainControl NBT list tag into an net.minecraft.server NBT
     * list tag.
     *
     * @param listTag The TerrainControl list tag.
     * @return The Minecraft list tag.
     */
    private static NBTTagList getNMSFromNBTTagList(NamedBinaryTag listTag)
    {
        NBTTagList nmsTag = new NBTTagList();
        NamedBinaryTag[] childTags = (NamedBinaryTag[]) listTag.getValue();
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
                    nmsTag.appendTag(createTagNms(tag.getType(), tag.getValue()));
                    break;
                case TAG_List:
                    nmsTag.appendTag(getNMSFromNBTTagList(tag));
                    break;
                case TAG_Compound:
                    nmsTag.appendTag(getNMSFromNBTTagCompound(tag));
                    break;
                default:
                    break;
            }
        }
        return nmsTag;
    }

    /**
     * Creates a Minecraft NBTBase tag. Doesn't work for ends, lists and
     * compounds.
     *
     * @param type Type of the tag.
     * @param value Value of the tag.
     * @return The Minecraft NBTBast tag.
     */
    private static NBTBase createTagNms(NamedBinaryTag.Type type, Object value)
    {
        switch (type)
        {
            case TAG_Byte:
                return new NBTTagByte((Byte) value);
            case TAG_Short:
                return new NBTTagShort((Short) value);
            case TAG_Int:
                return new NBTTagInt((Integer) value);
            case TAG_Long:
                return new NBTTagLong((Long) value);
            case TAG_Float:
                return new NBTTagFloat((Float) value);
            case TAG_Double:
                return new NBTTagDouble((Double) value);
            case TAG_Byte_Array:
                return new NBTTagByteArray((byte[]) value);
            case TAG_String:
                return new NBTTagString((String) value);
            case TAG_Int_Array:
                return new NBTTagIntArray((int[]) value);
            default:
                // Cannot make this into a tag
                throw new IllegalArgumentException(type + "doesn't have a simple value!");
        }
    }

    private NBTHelper()
    {
    }
}
