package com.khorn.terraincontrol.forge;

import java.io.File;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.TCDefaultValues;
import com.khorn.terraincontrol.customobjects.ObjectsStore;

import net.minecraft.src.ModLoader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.SidedProxy;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

@Mod(modid = "TerrainControl", name = "TerrainControl", version = "2.3.2")
@NetworkMod(clientSideRequired = false, serverSideRequired = false)
public class TCPlugin
{
    @Instance("TerrainControl")
    public static TCPlugin instance;

    public final File terrainControlDirectory = new File("mods" + File.separator + "TerrainControl");
    private TCWorldType worldType;

    @PreInit
    public void preInit(FMLPreInitializationEvent event)
    {
        // Stub Method
    }

    @Init
    public void load(FMLInitializationEvent event)
    {
        // This is the place where the mod starts loading

        // Register localization
        LanguageRegistry.instance().addStringLocalization("generator.TerrainControl", "TerrainControl");
        // Load global custom objects
        ObjectsStore.ReadObjects(terrainControlDirectory);
        // Register world type
        worldType = new TCWorldType(this, 4, "TerrainControl");
        // Register channel
        NetworkRegistry.instance().registerChannel(new PacketHandler(this), TCDefaultValues.ChannelName.stringValue());
        // Register player tracker
        GameRegistry.registerPlayerTracker(new PlayerTracker(this));
    }

    @PostInit
    public void postInit(FMLPostInitializationEvent event)
    {
        // Stub Method
    }

    public LocalWorld getWorld()
    {
        return worldType.worldTC;
    }

}
