package com.khorn.terraincontrol.forge.util;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.Tag;
import net.minecraft.nbt.*;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

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
    // So it is safe to suppress this warning
    public static Tag getNBTFromNMSTagCompound(String name, NBTTagCompound nmsTag)
    {
        Tag compoundTag = new Tag(Tag.Type.TAG_Compound, name, new Tag[] {new Tag(Tag.Type.TAG_End, null, null)});

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
            TerrainControl.printStackTrace(Level.SEVERE, e);
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
            Tag.Type type = Tag.Type.values()[nmsChildTag.getId()];
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
                    compoundTag.addTag(new Tag(type, entry.getKey(), getValueFromNms(nmsChildTag)));
                    break;
                case TAG_List:
                    Tag listChildTag = getNBTFromNMSTagList(entry.getKey(), (NBTTagList) nmsChildTag);
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
    private static Tag getNBTFromNMSTagList(String name, NBTTagList nmsListTag)
    {
        if (nmsListTag.tagCount() == 0)
        {
            // Nothing to return
            return null;
        }

        Tag.Type listType = Tag.Type.values()[nmsListTag.func_150303_d()];
        Tag listTag = new Tag(name, listType);

        // Add all child tags
        for (int i = 0; i < nmsListTag.tagCount(); i++)
        {
            switch (listType)
            {
                case TAG_Int_Array:
                    listTag.addTag(new Tag(listType, null, nmsListTag.func_150306_c(i)));
                    break;
                case TAG_Float:
                    listTag.addTag(new Tag(listType, null, nmsListTag.func_150308_e(i)));
                    break;
                case TAG_Double:
                    listTag.addTag(new Tag(listType, null, nmsListTag.func_150309_d(i)));
                    break;
                case TAG_String:
                    listTag.addTag(new Tag(listType, null, nmsListTag.func_150307_f(i)));
                    break;
                case TAG_Compound:
                    listTag.addTag(getNBTFromNMSTagCompound(null, nmsListTag.func_150305_b(i)));
                    break;
                default:
                    TerrainControl.log(Level.INFO, "Cannot convert list subtype {0} from it's NMS value", new Object[] {listType});
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
        Tag.Type type = Tag.Type.values()[nmsTag.getId()];
        switch (type)
        {
            case TAG_Byte:
                return ((NBTTagByte) nmsTag).func_150290_f();
            case TAG_Short:
                return ((NBTTagShort) nmsTag).func_150289_e();
            case TAG_Int:
                return ((NBTTagInt) nmsTag).func_150287_d();
            case TAG_Long:
                return ((NBTTagLong) nmsTag).func_150291_c();
            case TAG_Float:
                return ((NBTTagFloat) nmsTag).func_150288_h();
            case TAG_Double:
                return ((NBTTagDouble) nmsTag).func_150286_g();
            case TAG_Byte_Array:
                return ((NBTTagByteArray) nmsTag).func_150292_c();
            case TAG_String:
                return ((NBTTagString) nmsTag).func_150285_a_();
            case TAG_Int_Array:
                return ((NBTTagIntArray) nmsTag).func_150302_c();
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
        NBTTagCompound nmsTag = new NBTTagCompound();
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
     * Converts a net.minecraftwiki.wiki.NBTClass NBT list tag into an
     * net.minecraft.server NBT list tag.
     * 
     * @param listTag
     * @return
     */
    private static NBTTagList getNMSFromNBTTagList(Tag listTag)
    {
        NBTTagList nmsTag = new NBTTagList();
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
     * Creates a net.minecraft.server.NBTBase tag. Doesn't work for ends,
     * lists and compounds.
     * 
     * @param type
     * @param value
     * @return
     */
    private static NBTBase createTagNms(Tag.Type type, Object value)
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
