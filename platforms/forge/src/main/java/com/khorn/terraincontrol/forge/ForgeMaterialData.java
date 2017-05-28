package com.khorn.terraincontrol.forge;

import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.util.helpers.BlockHelper;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;

/**
 * Implementation of LocalMaterial that wraps one of Minecraft's Blocks.
 * 
 */
public class ForgeMaterialData implements LocalMaterialData
{

    public static ForgeMaterialData ofString(String input) throws InvalidConfigException
    {
        // Try parsing as an internal Minecraft name
        // This is so that things like "minecraft:stone" aren't parsed
        // as the block "minecraft" with data "stone", but instead as the
        // block "minecraft:stone" with no block data.
        net.minecraft.block.Block block = net.minecraft.block.Block.getBlockFromName(input);
        if (block != null)
        {
            return ForgeMaterialData.ofMinecraftBlock(block);
        }

        try
        {
            // Try block(:data) syntax
            return getMaterial0(input);
        } catch (NumberFormatException e)
        {
            throw new InvalidConfigException("Unknown material: " + input);
        }
    }

    @SuppressWarnings("deprecation")
    private static ForgeMaterialData getMaterial0(String input) throws NumberFormatException, InvalidConfigException
    {
        String blockName = input;
        int blockData = -1;

        // When there is a . or a : in the name, extract block data
        int splitIndex = input.lastIndexOf(":");
        if (splitIndex == -1)
        {
            splitIndex = input.lastIndexOf(".");
        }
        if (splitIndex != -1)
        {
            blockName = input.substring(0, splitIndex);
            blockData = Integer.parseInt(input.substring(splitIndex + 1));
        }

        // Parse block name
        Block block = Block.getBlockFromName(blockName);
        if (block == null)
        {
            DefaultMaterial defaultMaterial = DefaultMaterial.getMaterial(blockName);
            if (defaultMaterial != DefaultMaterial.UNKNOWN_BLOCK)
            {
                block = Block.getBlockById(defaultMaterial.id);
            }
        }

        // Get the block
        if (block != null)
        {
            if (blockData == -1)
            {
                // Use default
                return ForgeMaterialData.ofMinecraftBlock(block);
            } else
            {
                // Use specified data
                try
                {
                    return ForgeMaterialData.ofMinecraftBlockState(block.getStateFromMeta(blockData));
                } catch (IllegalArgumentException e)
                {
                    throw new InvalidConfigException("Illegal block data for the block type, cannot use " + input);
                }
            }
        }

        // Failed
        throw new InvalidConfigException("Unknown material: " + input);
    }

    /**
     * Gets a {@code BukkitMaterialData} of the given id and data.
     * @param id   The block id.
     * @param data The block data.
     * @return The {@code BukkitMateialData} instance.
     */
    @SuppressWarnings("deprecation")
    private static ForgeMaterialData ofIds(int id, int data)
    {
        Block block = Block.getBlockById(id);
        IBlockState blockData = block.getStateFromMeta(data);
        return ofMinecraftBlockState(blockData);
    }

    /**
     * Gets a {@code BukkitMaterialData} of the given material and data.
     * @param material The material.
     * @param data     The block data.
     * @return The {@code BukkitMateialData} instance.
     */
    static ForgeMaterialData ofDefaultMaterial(DefaultMaterial material, int data)
    {
        return ofIds(material.id, data);
    }

    /**
     * Gets a {@code BukkitMaterialData} of the given Minecraft block. The
     * default block data (usually 0) will be used.
     * @param block The material.
     * @return The {@code BukkitMateialData} instance.
     */
    static ForgeMaterialData ofMinecraftBlock(Block block)
    {
        return ofMinecraftBlockState(block.getDefaultState());
    }

    /**
     * Gets a {@code BukkitMaterialData} of the given Minecraft blockData.
     * @param blockData The material an data.
     * @return The {@code BukkitMateialData} instance.
     */
    public static ForgeMaterialData ofMinecraftBlockState(IBlockState blockData)
    {
        return new ForgeMaterialData(blockData);
    }

    private final IBlockState blockData;

    private ForgeMaterialData(IBlockState blockData)
    {
        this.blockData = blockData;
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
        if (!this.blockData.equals(other.blockData))
        {
            return false;
        }
        return true;
    }

    @Override
    public byte getBlockData()
    {
        return (byte) this.blockData.getBlock().getMetaFromState(this.blockData);
    }

    @Override
    public int getBlockId()
    {
        return Block.getIdFromBlock(this.blockData.getBlock());
    }

    @Override
    public String getName()
    {
        Block block = this.blockData.getBlock();
        byte data = getBlockData();

        boolean nonDefaultData = !block.getDefaultState().equals(this.blockData);
        // Note that the above line is not equivalent to data != 0, as for
        // example pumpkins have a default data value of 2

        if (nonDefaultData)
        {
            return Block.REGISTRY.getNameForObject(this.blockData.getBlock()) + ":" + data;
        }
        return Block.REGISTRY.getNameForObject(this.blockData.getBlock()).toString();

    }

    @Override
    public int hashCode()
    {
        // From 4096 to 69632 when there are 4096 block ids
        return TerrainControl.SUPPORTED_BLOCK_IDS + getBlockId() * 16 + getBlockData();
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
        return this.blockData.getMaterial().isLiquid();
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

        return this.blockData.getMaterial().isSolid();
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

        Block block = this.blockData.getBlock();
        return ofMinecraftBlockState(block.getStateFromMeta(i));
    }

    @Override
    public LocalMaterialData withDefaultBlockData()
    {
        Block block = this.blockData.getBlock();
        return this.withBlockData(block.getMetaFromState(block.getDefaultState()));
    }

    public IBlockState internalBlock()
    {
        return this.blockData;
    }

    @SuppressWarnings("deprecation")
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
                return ofMinecraftBlockState(this.blockData.getBlock().getStateFromMeta(newData));
            }
        }

        // No changes, return object itself
        return this;
    }

    @Override
    public boolean isAir()
    {
        return this.blockData.getMaterial() == Material.AIR;
    }

    @Override
    public boolean canFall()
    {
        return this.blockData.getBlock() instanceof BlockFalling;
    }

}
