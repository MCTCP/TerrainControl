package com.khorn.terraincontrol.forge;

import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.util.helpers.BlockHelper;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

import java.util.IdentityHashMap;
import java.util.Map;


/**
 * Implementation of LocalMaterial that wraps one of Minecraft's Blocks.
 * 
 */
public class ForgeMaterialData implements LocalMaterialData
{
    /**
     * Caches all unique blocks with their BukkitMaterialData equivalent. If
     * uncached you'll easily see more than 50000 BukkitMaterialData instances
     * in memory. Doens't support block data yet, but this can maybe be added
     * in Minecraft 1.8 when BlockState instances are added.
     */
    private static final Map<Block, ForgeMaterialData> CACHE = new IdentityHashMap<Block, ForgeMaterialData>();

    /**
     * Gets a {@code BukkitMaterialData} of the given id and data.
     * @param id   The block id.
     * @param data The block data.
     * @return The {@code BukkitMateialData} instance.
     */
    public static ForgeMaterialData ofIds(int id, int data)
    {
        return ofMinecraftBlock(Block.getBlockById(id), data);
    }

    /**
     * Gets a {@code BukkitMaterialData} of the given material and data.
     * @param material The material.
     * @param data     The block data.
     * @return The {@code BukkitMateialData} instance.
     */
    public static ForgeMaterialData ofDefaultMaterial(DefaultMaterial material, int data)
    {
        return ofIds(material.id, data);
    }

    /**
     * Gets a {@code BukkitMaterialData} of the given Minecraft block and data.
     * @param material The material.
     * @param data     The block data.
     * @return The {@code BukkitMateialData} instance.
     */
    public static ForgeMaterialData ofMinecraftBlock(Block block, int data)
    {
        if (data != 0)
        {
            // Cache doens't support block data
            return new ForgeMaterialData(block, data);
        }

        ForgeMaterialData cached = CACHE.get(block);
        if (cached != null)
        {
            // Found cache entry
            return cached;
        }

        // Create cache entry
        ForgeMaterialData newObject = new ForgeMaterialData(block, data);
        CACHE.put(block, newObject);
        return newObject;
    }

    private final Block block;
    private final byte data;

    private ForgeMaterialData(Block block, int data)
    {
        this.block = block;
        this.data = (byte) data;
    }

    @Override
    public boolean canSnowFallOn()
    {
        return toDefaultMaterial().canSnowFallOn();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!(obj instanceof ForgeMaterialData))
        {
            return false;
        }
        ForgeMaterialData other = (ForgeMaterialData) obj;
        if (!block.equals(other.block))
        {
            return false;
        }
        if (data != other.data)
        {
            return false;
        }
        return true;
    }

    @Override
    public byte getBlockData()
    {
        return data;
    }

    @Override
    public int getBlockId()
    {
        return Block.getIdFromBlock(block);
    }

    @Override
    public String getName()
    {
        DefaultMaterial defaultMaterial = toDefaultMaterial();
        if (defaultMaterial == DefaultMaterial.UNKNOWN_BLOCK)
        {
            // Use Minecraft's name
            if (data != 0)
            {
                return Block.blockRegistry.getNameForObject(block) + ":" + data;
            }
            return Block.blockRegistry.getNameForObject(block);
        } else
        {
            // Use our name
            if (data != 0)
            {
                return defaultMaterial.name() + ":" + data;
            }
            return defaultMaterial.name();
        }
    }

    @Override
    public int hashCode()
    {
        // From 4096 to 69632 when there are 4096 block ids
        return TerrainControl.SUPPORTED_BLOCK_IDS + getBlockId() * 16 + data;
    }
    
    @Override
    public int hashCodeWithoutBlockData()
    {
        // From 0 to 4095 when there are 4096 block ids
        return getBlockId();
    }


    @Override
    public boolean isLiquid()
    {
        return block.getMaterial().isLiquid();
    }

    @Override
    public boolean isMaterial(DefaultMaterial material)
    {
        return material.id == getBlockId();
    }

    @Override
    public boolean isSolid()
    {
        // Let us override whether materials are solid
        DefaultMaterial defaultMaterial = toDefaultMaterial();
        if (defaultMaterial != DefaultMaterial.UNKNOWN_BLOCK)
        {
            return defaultMaterial.isSolid();
        }

        return block.getMaterial().isSolid();
    }

    @Override
    public DefaultMaterial toDefaultMaterial()
    {
        return DefaultMaterial.getMaterial(getBlockId());
    }

    @Override
    public String toString()
    {
        return getName();
    }

    @Override
    public LocalMaterialData withBlockData(int i)
    {
        if (i == this.data)
        {
            // No need to create new instance
            return this;
        }
        return new ForgeMaterialData(block, i);
    }

    public Block internalBlock()
    {
        return block;
    }

    @Override
    public LocalMaterialData rotate()
    {
        // Try to rotate
        DefaultMaterial defaultMaterial = toDefaultMaterial();
        if (defaultMaterial != DefaultMaterial.UNKNOWN_BLOCK)
        {
            // We only know how to rotate vanilla blocks
            int newData = BlockHelper.rotateData(defaultMaterial, data);
            if (newData != data)
            {
                return new ForgeMaterialData(block, newData);
            }
        }

        // No changes, return object itself
        return this;
    }

    @Override
    public boolean isAir() {
        return block == Blocks.air;
    }

}
