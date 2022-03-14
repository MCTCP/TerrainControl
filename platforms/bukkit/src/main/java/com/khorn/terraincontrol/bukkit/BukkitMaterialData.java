package com.khorn.terraincontrol.bukkit;

import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.util.helpers.BlockHelper;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;
import net.minecraft.server.v1_12_R1.Block;
import net.minecraft.server.v1_12_R1.BlockFalling;
import net.minecraft.server.v1_12_R1.IBlockData;

/**
 * Implementation of LocalMaterial that wraps one of Minecraft's Blocks.
 * 
 */
public final class BukkitMaterialData implements LocalMaterialData
{

    /**
     * Gets a {@code BukkitMaterialData} of the given id and data.
     * @param id   The block id.
     * @param data The block data.
     * @return The {@code BukkitMateialData} instance.
     */
    public static BukkitMaterialData ofIds(int id, int data)
    {
        return new BukkitMaterialData(id, data);
    }

    /**
     * Gets a {@code BukkitMaterialData} of the given material and data.
     * @param material The material.
     * @param data     The block data.
     * @return The {@code BukkitMateialData} instance.
     */
    public static BukkitMaterialData ofDefaultMaterial(DefaultMaterial material, int data)
    {
        return ofIds(material.id, data);
    }

    /**
     * Gets a {@code BukkitMaterialData} of the given Minecraft block. The
     * default block data (usually 0) will be used.
     * @param block The material.
     * @return The {@code BukkitMateialData} instance.
     */
    public static BukkitMaterialData ofMinecraftBlock(Block block)
    {
        return ofIds(Block.getId(block), block.toLegacyData(block.getBlockData()));
    }

    /**
     * Gets a {@code BukkitMaterialData} of the given Minecraft blockData.
     * @param blockData The material an data.
     * @return The {@code BukkitMateialData} instance.
     */
    public static BukkitMaterialData ofMinecraftBlockData(IBlockData blockData)
    {
        Block block = blockData.getBlock();
        return new BukkitMaterialData(Block.getId(block), block.toLegacyData(blockData));
    }

    /**
     * Block id and data, calculated as {@code blockId << 4 | blockData}, or
     * without binary operators: {@code blockId * 16 + blockData}.
     *
     * <p>Note that Minecraft's Block.getCombinedId uses another format (at
     * least in Minecraft 1.8). However, Minecraft's ChunkSection uses the same
     * format as this field.
     */
    private final int combinedBlockId;

    private BukkitMaterialData(int blockId, int blockData)
    {
        this.combinedBlockId = blockId << 4 | blockData;
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
        if (combinedBlockId != other.combinedBlockId)
        {
            return false;
        }
        return true;
    }

    @Override
    public byte getBlockData()
    {
        return (byte) (combinedBlockId & 15);
    }

    @Override
    public int getBlockId()
    {
        return combinedBlockId >> 4;
    }

    @Override
    public String getName()
    {
        Block block = Block.getById(getBlockId());

        byte data = getBlockData();
        boolean nonDefaultData = block.toLegacyData(block.getBlockData()) != data;
        // Note that the above line is not equivalent to data != 0, as for
        // example pumpkins have a default data value of 2

        if (nonDefaultData)
        {
            return Block.REGISTRY.b(block) + ":" + data;
        }
        return Block.REGISTRY.b(block).toString();
    }

    @Override
    public int hashCode()
    {
        // From 4096 to 69632 when there are 4096 block ids
        return TerrainControl.SUPPORTED_BLOCK_IDS + combinedBlockId;
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
        return this.internalBlock().getMaterial().isLiquid();
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

        return this.internalBlock().getMaterial().isSolid();
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

    @SuppressWarnings("deprecation")
    @Override
    public LocalMaterialData withBlockData(int i)
    {
        if (i == getBlockData())
        {
            return this;
        }

        Block block = Block.getById(getBlockId());
        return ofMinecraftBlockData(block.fromLegacyData(i));
    }

    @Override
    public LocalMaterialData withDefaultBlockData()
    {
        Block block = Block.getById(getBlockId());
        byte defaultData = (byte) block.toLegacyData(block.getBlockData());
        return this.withBlockData(defaultData);
    }

    @SuppressWarnings("deprecation")
    public IBlockData internalBlock()
    {
        return Block.getById(getBlockId()).fromLegacyData(getBlockData());
    }

    @Override
    public LocalMaterialData rotate()
    {
        // Try to rotate
        DefaultMaterial defaultMaterial = toDefaultMaterial();
        if (defaultMaterial != DefaultMaterial.UNKNOWN_BLOCK)
        {
            // We only know how to rotate vanilla blocks
            byte blockDataByte = getBlockData();
            int newData = BlockHelper.rotateData(defaultMaterial, blockDataByte);
            if (newData != blockDataByte)
            {
                return ofDefaultMaterial(defaultMaterial, newData);
            }
        }

        // No changes, return object itself
        return this;
    }

    @Override
    public boolean isAir() {
        return combinedBlockId == 0;
    }

    @Override
    public boolean canFall()
    {
        return Block.getById(getBlockId()) instanceof BlockFalling;
    }

}
