package com.pg85.otg.forge.util;

import com.pg85.otg.util.bo3.NamedBinaryTag;

import net.minecraft.nbt.ByteArrayNBT;
import net.minecraft.nbt.ByteNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.DoubleNBT;
import net.minecraft.nbt.FloatNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntArrayNBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.LongNBT;
import net.minecraft.nbt.ShortNBT;
import net.minecraft.nbt.StringNBT;

public class NBTHelper
{
    private NBTHelper() { }

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
     * Creates a Minecraft NBTBase tag. Doesn't work for ends, lists and
     * compounds.
     *
     * @param type Type of the tag.
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
}
