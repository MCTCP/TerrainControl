package com.khorn.terraincontrol.forge;

import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.TerrainControlEngine;
import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.logging.LogMarker;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;

import java.io.File;
import java.lang.reflect.Field;

public class ForgeEngine extends TerrainControlEngine
{

    protected TCWorldType worldType;

    public ForgeEngine(TCWorldType worldType)
    {
        super(FMLCommonHandler.instance().getFMLLogger());
        this.worldType = worldType;
    }

    @Override
    public LocalWorld getWorld(String name)
    {
        LocalWorld world = worldType.worldTC;
        if (world == null)
        {
            return null;
        }
        if (world.getName().equals(name))
        {
            return world;
        }
        return null;
    }

    /**
     * Gets the world loaded by Terrain Control.
     * <p />
     * Note: this method may be removed in the future, when multiworld support
     * is introduced.
     * <p/>
     * <p>
     * @return The world loaded by Terrain Control, or null if no world is
     *         loaded.
     */
    public LocalWorld getWorld()
    {
        return worldType.worldTC;
    }

    @Override
    public File getTCDataFolder()
    {
        File dataFolder;
        try
        {
            Field minecraftDir = Loader.class.getDeclaredField("minecraftDir");
            minecraftDir.setAccessible(true);
            dataFolder = new File((File) minecraftDir.get(null), "mods" + File.separator + "TerrainControl");
        } catch (Throwable e)
        {
            dataFolder = new File("mods" + File.separator + "TerrainControl");
            System.out.println("Could not reflect the Minecraft directory, save location may be unpredicatble.");
            TerrainControl.printStackTrace(LogMarker.FATAL, e);
        }
        return dataFolder;
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
        net.minecraft.block.Block block = net.minecraft.block.Block.getBlockFromName(input);
        if (block != null)
        {
            return new ForgeMaterialData(block, 0);
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
            blockName = input.substring(0, splitIndex);
            blockData = Integer.parseInt(input.substring(splitIndex + 1));
        }

        // Get the material belonging to the block and data
        net.minecraft.block.Block block = net.minecraft.block.Block.getBlockFromName(blockName);
        if (block != null)
        {
            return new ForgeMaterialData(block, blockData);
        }
        DefaultMaterial defaultMaterial = DefaultMaterial.getMaterial(blockName);
        if (defaultMaterial != DefaultMaterial.UNKNOWN_BLOCK)
        {
            return new ForgeMaterialData(defaultMaterial, blockData);
        }

        // Failed
        throw new InvalidConfigException("Unknown material: " + input);
    }

    @Override
    public LocalMaterialData toLocalMaterialData(DefaultMaterial defaultMaterial, int blockData)
    {
        return new ForgeMaterialData(defaultMaterial, blockData);
    }

}
