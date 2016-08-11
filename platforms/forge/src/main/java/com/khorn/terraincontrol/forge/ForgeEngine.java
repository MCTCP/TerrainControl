package com.khorn.terraincontrol.forge;

import java.io.File;

import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControlEngine;
import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;

public class ForgeEngine extends TerrainControlEngine
{

    protected WorldLoader worldLoader;

    public ForgeEngine(WorldLoader worldLoader)
    {
        super(new ForgeLogger());
        this.worldLoader = worldLoader;
    }

    @Override
    public LocalWorld getWorld(String name)
    {
        return worldLoader.getWorld(name);
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
        return worldLoader.getMainWorld();
    }

    @Override
    public File getTCDataFolder()
    {
        return worldLoader.getConfigsFolder();
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
