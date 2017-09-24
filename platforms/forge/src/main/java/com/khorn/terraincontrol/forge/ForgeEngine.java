package com.khorn.terraincontrol.forge;

import java.io.File;
import java.lang.reflect.Field;
import java.util.BitSet;

import com.google.common.collect.BiMap;
import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControlEngine;
import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class ForgeEngine extends TerrainControlEngine
{

    protected WorldLoader worldLoader;

    private BiMap<ResourceLocation, Biome> biomeRegistryMap;
    private BitSet biomeAvailabilityMap;

    @SuppressWarnings("unchecked")
    public ForgeEngine(WorldLoader worldLoader)
    {
        super(new ForgeLogger());
        this.worldLoader = worldLoader;
        try {
            Field f = ForgeRegistries.BIOMES.getClass().getDeclaredField("names");
            f.setAccessible(true);
            this.biomeRegistryMap = (BiMap<ResourceLocation, Biome>) f.get(ForgeRegistries.BIOMES);
            f = ForgeRegistries.BIOMES.getClass().getDeclaredField("availabilityMap");
            f.setAccessible(true);
            this.biomeAvailabilityMap = (BitSet) f.get(ForgeRegistries.BIOMES);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    // Used to bypass Forge's API in order to properly register a virtual biome
    // that would otherwise be blocked by Forge due to virtual biome ID's
    // surpassing 255.
    public void registerForgeBiome(int id, ResourceLocation resourceLocation, Biome biome)
    {
        Biome.REGISTRY.registryObjects.put(resourceLocation, biome);
        Biome.REGISTRY.underlyingIntegerMap.put(biome, id);
        Biome.REGISTRY.inverseObjectRegistry.put(biome, resourceLocation);
        if (id >= 0 && id < 256) { 
            this.biomeAvailabilityMap.set(id);
        }
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

    public BiMap<ResourceLocation, Biome> getBiomeMap() {
        return this.biomeRegistryMap;
    }

    public BitSet getBiomeAvailabilityMap() {
        return this.biomeAvailabilityMap;
    }
}
