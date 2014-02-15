package com.khorn.terraincontrol.bukkit;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import net.minecraft.server.v1_7_R1.Block;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.World;

import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControlEngine;
import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.logging.TCLogManager;
import com.khorn.terraincontrol.util.helpers.StringHelper;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;

public class BukkitEngine extends TerrainControlEngine
{
    private final TCPlugin plugin;
    private final Logger logger = LogManager.getLogger(getClass());

    public BukkitEngine(TCPlugin plugin)
    {
        this.plugin = plugin;

    }

    @Override
    public void log(Level level, String... messages)
    {
        this.log(level, "{0}", new Object[] {StringHelper.join(messages, " ")});
    }

    @Override
    public void log(Level level, String message, Object param)
    {
        log(level, message, new Object[] {param});

    }

    @Override
    public void log(Level level, String message, Object[] params)
    {
        LogRecord logRecord = new LogRecord(level, message);
        logRecord.setParameters(params);

        String formattedMessage = TCLogManager.FORMATTER.format(logRecord);

        if (level == Level.SEVERE)
        {
            logger.log(org.apache.logging.log4j.Level.ERROR, formattedMessage);
        } else if (level == Level.WARNING)
        {
            logger.log(org.apache.logging.log4j.Level.WARN, formattedMessage);
        } else if (level == Level.INFO)
        {
            logger.log(org.apache.logging.log4j.Level.INFO, formattedMessage);
        } else if (level == Level.CONFIG || level == Level.FINE)
        {
            logger.log(org.apache.logging.log4j.Level.DEBUG, formattedMessage);
        } else
        { // so level == Level.FINER || level == FINEST
            logger.log(org.apache.logging.log4j.Level.TRACE, formattedMessage);
        }
    }

    @Override
    public LocalWorld getWorld(String name)
    {
        World world = Bukkit.getWorld(name);
        if (world == null)
        {
            // World not loaded
            return null;
        }
        return plugin.worlds.get(world.getUID());
    }

    @Override
    public File getTCDataFolder()
    {
        return plugin.getDataFolder();
    }

    @Override
    public File getGlobalObjectsDirectory()
    {
        return new File(this.getTCDataFolder(), PluginStandardValues.BO_DirectoryName.stringValue());
    }

    @Override
    public LocalMaterialData readMaterial(String input) throws InvalidConfigException
    {
        // Try parsing as an internal Minecraft name
        // This is so that things like "minecraft:stone" aren't parsed
        // as the block "minecraft" with data "stone", but instead as the
        // block "minecraft:stone" with no block data.
        Block block = Block.b(input);
        if (block != null)
        {
            return new BukkitMaterialData(block, 0);
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

    private LocalMaterialData getMaterial0(String input) throws NumberFormatException, InvalidConfigException
    {
        String blockName = input;
        int blockData = 0;

        // When there is a . or a : in the name, extract block data
        int splitIndex = input.lastIndexOf(":");
        if (splitIndex == -1)
        {
            splitIndex = input.lastIndexOf(".");
        }
        if (splitIndex != -1)
        {
            blockName = input.substring(0, splitIndex - 1);
            blockData = Integer.parseInt(input.substring(splitIndex));
        }

        // Get the material belonging to the block and data
        Block block = Block.b(blockName);
        if (block != null)
        {
            return new BukkitMaterialData(block, blockData);
        }
        DefaultMaterial defaultMaterial = DefaultMaterial.getMaterial(blockName);
        if (defaultMaterial != null)
        {
            return new BukkitMaterialData(defaultMaterial, blockData);
        }

        // Failed
        throw new InvalidConfigException("Unkown material: " + input);
    }

    @Override
    public LocalMaterialData toLocalMaterialData(DefaultMaterial defaultMaterial, int blockData)
    {
        return new BukkitMaterialData(defaultMaterial, blockData);
    }

}
