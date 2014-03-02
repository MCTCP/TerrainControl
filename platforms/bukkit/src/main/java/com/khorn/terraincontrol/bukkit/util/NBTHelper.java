package com.khorn.terraincontrol.bukkit.util;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.logging.LogMarker;
import com.khorn.terraincontrol.util.NamedBinaryTag;
import net.minecraft.server.v1_7_R1.*;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Map.Entry;

public class NBTHelper
{

    /**
     * Converts a net.minecraft.server compound NBT tag to a
     * net.minecraftwiki.wiki.NBTClass NBT compound tag.
     * 
     * @param nmsTag
     * @return
     */
    @SuppressWarnings("unchecked")
    // ^ We know that NBTTagCompound.map is a Map<String, NBTBase>
    //   So it is safe to suppress this warning
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
            NamedBinaryTag.Type type = NamedBinaryTag.Type.values()[nmsChildTag.getTypeId()];
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
     * @param nmsListTag
     * @return
     */
    private static NamedBinaryTag getNBTFromNMSTagList(String name, NBTTagList nmsListTag)
    {
        if (nmsListTag.size() == 0)
        {
            // Nothing to return
            return null;
        }

        NamedBinaryTag.Type listType = NamedBinaryTag.Type.values()[nmsListTag.d()];
        NamedBinaryTag listTag = new NamedBinaryTag(name, listType);

        // Add all child tags
        for (int i = 0; i < nmsListTag.size(); i++)
        {
            switch (listType)
            {
                case TAG_Int_Array:
                    listTag.addTag(new NamedBinaryTag(listType, null, nmsListTag.c(i)));
                    break;
                case TAG_Float:
                    listTag.addTag(new NamedBinaryTag(listType, null, nmsListTag.e(i)));
                    break;
                case TAG_Double:
                    listTag.addTag(new NamedBinaryTag(listType, null, nmsListTag.d(i)));
                    break;
                case TAG_String:
                    listTag.addTag(new NamedBinaryTag(listType, null, nmsListTag.f(i)));
                    break;
                case TAG_Compound:
                    listTag.addTag(getNBTFromNMSTagCompound(null, nmsListTag.get(i)));
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
     * @param nmsTag
     * @return
     */
    private static Object getValueFromNms(NBTBase nmsTag)
    {
        NamedBinaryTag.Type type = NamedBinaryTag.Type.values()[nmsTag.getTypeId()];
        switch (type)
        {
            case TAG_Byte:
                return ((NBTTagByte) nmsTag).f();
            case TAG_Short:
                return ((NBTTagShort) nmsTag).e();
            case TAG_Int:
                return ((NBTTagInt) nmsTag).d();
            case TAG_Long:
                return ((NBTTagLong) nmsTag).c();
            case TAG_Float:
                return ((NBTTagFloat) nmsTag).h();
            case TAG_Double:
                return ((NBTTagDouble) nmsTag).g();
            case TAG_Byte_Array:
                return ((NBTTagByteArray) nmsTag).c();
            case TAG_String:
                return ((NBTTagString) nmsTag).a_();
            case TAG_Int_Array:
                return ((NBTTagIntArray) nmsTag).c();
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
                    nmsTag.set(tag.getName(), createTagNms(tag.getType(), tag.getValue()));
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
                    nmsTag.add(createTagNms(tag.getType(), tag.getValue()));
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
     * Creates a net.minecraft.server.NBTBase tag. Doesn't work for ends,
     * lists and compounds.
     * 
     * @param type
     * @param value
     * @return
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
}

