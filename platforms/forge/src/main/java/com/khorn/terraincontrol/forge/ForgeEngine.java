package com.khorn.terraincontrol.forge;

import java.io.File;
import java.util.Map;

import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControlEngine;
import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;

public class ForgeEngine extends TerrainControlEngine
{

    protected WorldLoader worldLoader;

    protected Map<ResourceLocation, Biome> biomeMap;

    public ForgeEngine(WorldLoader worldLoader)
    {
        super(new ForgeLogger());
        this.worldLoader = worldLoader;
    }

    // Used to bypass Forge's API in order to properly register a virtual biome
    // that would otherwise be blocked by Forge due to virtual biome ID's
    // surpassing 255.
    public void registerForgeBiome(int id, ResourceLocation resourceLocation, Biome biome)
    {
        Biome.REGISTRY.registryObjects.put(resourceLocation, biome);
        Biome.REGISTRY.underlyingIntegerMap.put(biome, id);
        Biome.REGISTRY.inverseObjectRegistry.put(biome, resourceLocation);
    }

    @Override
    public LocalWorld getWorld(String name)
    {
        return this.worldLoader.getWorld(name);
    }

    @Override
    public File getTCDataFolder()
    {
        return this.worldLoader.getConfigsFolder();
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
