package com.khorn.terraincontrol.forge;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControlEngine;
import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;
import com.khorn.terraincontrol.exception.InvalidConfigException;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.IForgeRegistryEntry;

public class ForgeEngine extends TerrainControlEngine
{

    protected WorldLoader worldLoader;
    protected Method ADD_OBJECT_RAW;

    public ForgeEngine(WorldLoader worldLoader)
    {
        super(new ForgeLogger());
        this.worldLoader = worldLoader;
        // setup reflection method in order to properly register virtual biomes
        try {
            ADD_OBJECT_RAW = Biome.REGISTRY.getClass().getDeclaredMethod("addObjectRaw", int.class, ResourceLocation.class, IForgeRegistryEntry.class);
            ADD_OBJECT_RAW.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    // Used to bypass Forge's API in order to properly register a virtual biome
    // that would otherwise be blocked by Forge due to virtual biome ID's surpassing 255.
    public void registerForgeBiome(int id, ResourceLocation resourceLocation, Biome biome) {
        try {
            this.ADD_OBJECT_RAW.invoke(Biome.REGISTRY, id, resourceLocation, biome);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public LocalWorld getWorld(String name)
    {
        return worldLoader.getWorld(name);
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
