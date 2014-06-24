package com.khorn.terraincontrol.bukkit;

import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.util.helpers.BlockHelper;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;
import net.minecraft.server.v1_7_R1.Block;

/**
 * Implementation of LocalMaterial that wraps one of Minecraft's Blocks.
 * 
 */
public class BukkitMaterialData implements LocalMaterialData
{
    private final Block block;
    private final byte data;

    public BukkitMaterialData(Block block, int data)
    {
        this.block = block;
        this.data = (byte) data;
    }

    public BukkitMaterialData(DefaultMaterial defaultMaterial, int data)
    {
        this.block = Block.e(defaultMaterial.id);
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
        if (!(obj instanceof BukkitMaterialData))
        {
            return false;
        }
        BukkitMaterialData other = (BukkitMaterialData) obj;
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
        return Block.b(block);
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
                return Block.REGISTRY.c(block) + ":" + data;
            }
            return Block.REGISTRY.c(block);
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
            return this;
        }
        return new BukkitMaterialData(block, i);
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
                return new BukkitMaterialData(block, newData);
            }
        }

        // No changes, return object itself
        return this;
    }

}
