package com.khorn.terraincontrol.bukkit;

import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControlEngine;
import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;
import net.minecraft.server.v1_12_R1.Block;

import java.io.File;

public class BukkitEngine extends TerrainControlEngine
{

    private final TXPlugin plugin;

    public BukkitEngine(TXPlugin plugin)
    {
        super(new BukkitLogger(plugin.getLogger()));
        this.plugin = plugin;
    }

    @Override
    public LocalWorld getWorld(String name)
    {
        return plugin.worlds.get(name);
    }

    @Override
    public File getTCDataFolder()
    {
        return plugin.getDataFolder();
    }

    @Override
    public File getGlobalObjectsDirectory()
    {
        return new File(this.getTCDataFolder(), PluginStandardValues.BO_DirectoryName);
    }

    @Override
    public LocalMaterialData readMaterial(String input) throws InvalidConfigException
    {
        // Try parsing as an internal Minecraft name
        // This is so that things like "minecraft:stone" aren't parsed
        // as the block "minecraft" with data "stone", but instead as the
        // block "minecraft:stone" with no block data.
        Block block = Block.getByName(input);
        if (block != null)
        {
            return BukkitMaterialData.ofMinecraftBlock(block);
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
    private LocalMaterialData getMaterial0(String input) throws NumberFormatException, InvalidConfigException
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
        Block block = Block.getByName(blockName);
        if (block == null)
        {
            DefaultMaterial defaultMaterial = DefaultMaterial.getMaterial(blockName);
            if (defaultMaterial != DefaultMaterial.UNKNOWN_BLOCK)
            {
                block = Block.getById(defaultMaterial.id);
            }
        }

        // Get the block
        if (block != null)
        {
            if (blockData == -1)
            {
                // Use default
                return BukkitMaterialData.ofMinecraftBlock(block);
            } else
            {
                // Use specified data
                try
                {
                    return BukkitMaterialData.ofMinecraftBlockData(block.fromLegacyData(blockData));
                } catch (IllegalArgumentException e)
                {
                    throw new InvalidConfigException("Illegal block data for the block type, cannot use " + input);
                }
            }
        }

        // Failed
        throw new InvalidConfigException("Unknown material: " + input);
    }

    @Override
    public LocalMaterialData toLocalMaterialData(DefaultMaterial defaultMaterial, int blockData)
    {
        return BukkitMaterialData.ofDefaultMaterial(defaultMaterial, blockData);
    }

}
