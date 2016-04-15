package com.khorn.terraincontrol.forge;

import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.TerrainControlEngine;
import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.logging.LogMarker;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;
import net.minecraft.block.Block;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.lang.reflect.Field;

public class ForgeEngine extends TerrainControlEngine
{

    protected TCWorldType worldType;

    public ForgeEngine(TCWorldType worldType)
    {
        super(new ForgeLogger());
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
            TerrainControl.log(LogMarker.WARN, "Could not reflect the Minecraft directory, save location may be unpredicatble.");
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
        return ForgeMaterialData.ofString(input);
    }

    @Override
    public LocalMaterialData toLocalMaterialData(DefaultMaterial defaultMaterial, int blockData)
    {
        return ForgeMaterialData.ofDefaultMaterial(defaultMaterial, blockData);
    }

}
