package com.khorn.terraincontrol.bukkit.util;

import com.khorn.terraincontrol.configuration.Tag;
import net.minecraft.server.v1_5_R2.*;

import java.lang.reflect.Field;
import java.util.Map;

public class NBTHelper
{

    /**
     * Converts a net.minecraft.server compound NBT tag to a
     * net.minecraftwiki.wiki.NBTClass NBT compound tag.
     *
     * @param nmsTag
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static Tag getNBTFromNMSTagCompound(NBTTagCompound nmsTag)
    {
        Tag compoundTag = new Tag(Tag.Type.TAG_Compound, nmsTag.getName(), new Tag[]{new Tag(Tag.Type.TAG_End, null, null)});

        // Get the child tags using some reflection magic
        Field mapField;
        Map nmsChildTags = null;
        try
        {
            mapField = NBTTagCompound.class.getDeclaredField("map");
            mapField.setAccessible(true);
            nmsChildTags = (Map) mapField.get(nmsTag);
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        if (nmsChildTags == null)
        {
            // Cannot load the tag, return an empty tag
            return compoundTag;
        }

        // Add all child tags to the compound tag
        for (Object nmsChildTagName : nmsChildTags.keySet())
        {
            NBTBase nmsChildTag = (NBTBase) nmsChildTags.get(nmsChildTagName);
            Tag.Type type = Tag.Type.values()[nmsChildTag.getTypeId()];
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
                    compoundTag.addTag(new Tag(type, nmsChildTag.getName(), getValueFromNms(nmsChildTag)));
                    break;
                case TAG_List:
                    Tag listChildTag = getNBTFromNMSTagList((NBTTagList) nmsChildTag);
                    if (listChildTag != null)
                    {
                        compoundTag.addTag(listChildTag);
                    }
                    break;
                case TAG_Compound:
                    compoundTag.addTag(getNBTFromNMSTagCompound((NBTTagCompound) nmsChildTag));
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
     * @param nmsListTag
     * @return
     */
    private static Tag getNBTFromNMSTagList(NBTTagList nmsListTag)
    {
        if (nmsListTag.size() == 0)
        {
            // Nothing to return
            return null;
        }

        Tag.Type listType = Tag.Type.values()[nmsListTag.get(0).getTypeId()];
        Tag listTag = new Tag(nmsListTag.getName(), listType);

        // Add all child tags
        for (int i = 0; i < nmsListTag.size(); i++)
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
                    listTag.addTag(new Tag(listType, nmsChildTag.getName(), getValueFromNms(nmsChildTag)));
                    break;
                case TAG_List:
                    Tag listChildTag = getNBTFromNMSTagList((NBTTagList) nmsChildTag);
                    if (listChildTag != null)
                    {
                        listTag.addTag(listChildTag);
                    }
                    break;
                case TAG_Compound:
                    listTag.addTag(getNBTFromNMSTagCompound((NBTTagCompound) nmsChildTag));
                    break;
                default:
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
     * @param nmsTag
     * @return
     */
    private static Object getValueFromNms(NBTBase nmsTag)
    {
        Tag.Type type = Tag.Type.values()[nmsTag.getTypeId()];
        switch (type)
        {
            case TAG_Byte:
                return ((NBTTagByte) nmsTag).data;
            case TAG_Short:
                return ((NBTTagShort) nmsTag).data;
            case TAG_Int:
                return ((NBTTagInt) nmsTag).data;
            case TAG_Long:
                return ((NBTTagLong) nmsTag).data;
            case TAG_Float:
                return ((NBTTagFloat) nmsTag).data;
            case TAG_Double:
                return ((NBTTagDouble) nmsTag).data;
            case TAG_Byte_Array:
                return ((NBTTagByteArray) nmsTag).data;
            case TAG_String:
                return ((NBTTagString) nmsTag).data;
            case TAG_Int_Array:
                return ((NBTTagIntArray) nmsTag).data;
            default:
                // Cannot read this from a tag
                throw new IllegalArgumentException(type + "doesn't have a simple value!");
        }
    }

    /**
     * Converts a net.minecraftwiki.wiki.NBTClass NBT compound tag into an
     * net.minecraft.server NBT compound tag.
     *
     * @param compoundTag
     * @return
     */
    public static NBTTagCompound getNMSFromNBTTagCompound(Tag compoundTag)
    {
        NBTTagCompound nmsTag = new NBTTagCompound(compoundTag.getName());
        Tag[] childTags = (Tag[]) compoundTag.getValue();
        for (Tag tag : childTags)
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
                    nmsTag.set(tag.getName(), createTagNms(tag.getType(), tag.getName(), tag.getValue()));
                    break;
                case TAG_List:
                    nmsTag.set(tag.getName(), getNMSFromNBTTagList(tag));
                    break;
                case TAG_Compound:
                    nmsTag.set(tag.getName(), getNMSFromNBTTagCompound(tag));
                    break;
                default:
                    break;
            }
        }
        return nmsTag;
    }

    /**
     * Converts a net.minecraftwiki.wiki.NBTClass NBT list tag into an
     * net.minecraft.server NBT list tag.
     *
     * @param listTag
     * @return
     */
    private static NBTTagList getNMSFromNBTTagList(Tag listTag)
    {
        NBTTagList nmsTag = new NBTTagList(listTag.getName());
        Tag[] childTags = (Tag[]) listTag.getValue();
        for (Tag tag : childTags)
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
                    nmsTag.add(createTagNms(tag.getType(), tag.getName(), tag.getValue()));
                    break;
                case TAG_List:
                    nmsTag.add(getNMSFromNBTTagList(tag));
                    break;
                case TAG_Compound:
                    nmsTag.add(getNMSFromNBTTagCompound(tag));
                    break;
                default:
                    break;
            }
        }
        return nmsTag;
    }

    /**
     * Creates a net.minecraft.server.NBTBase tag. Doesn't work for ends, lists
     * and compounds.
     *
     * @param type
     * @param name
     * @param value
     * @return
     */
    private static NBTBase createTagNms(Tag.Type type, String name, Object value)
    {
        switch (type)
        {
            case TAG_Byte:
                return new NBTTagByte(name, (Byte) value);
            case TAG_Short:
                return new NBTTagShort(name, (Short) value);
            case TAG_Int:
                return new NBTTagInt(name, (Integer) value);
            case TAG_Long:
                return new NBTTagLong(name, (Long) value);
            case TAG_Float:
                return new NBTTagFloat(name, (Float) value);
            case TAG_Double:
                return new NBTTagDouble(name, (Double) value);
            case TAG_Byte_Array:
                return new NBTTagByteArray(name, (byte[]) value);
            case TAG_String:
                return new NBTTagString(name, (String) value);
            case TAG_Int_Array:
                return new NBTTagIntArray(name, (int[]) value);
            default:
                // Cannot make this into a tag
                throw new IllegalArgumentException(type + "doesn't have a simple value!");
        }
    }
}
