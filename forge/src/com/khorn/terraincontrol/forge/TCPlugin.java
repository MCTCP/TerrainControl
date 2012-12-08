package com.khorn.terraincontrol.forge;

import java.io.File;
import java.util.logging.Level;

import net.minecraft.server.MinecraftServer;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.TerrainControlEngine;
import com.khorn.terraincontrol.configuration.TCDefaultValues;
import com.khorn.terraincontrol.customobjects.BODefaultValues;
import com.khorn.terraincontrol.util.Txt;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

@Mod(modid = "TerrainControl", name = "TerrainControl", version = "2.4.1")
@NetworkMod(clientSideRequired = false, serverSideRequired = false, versionBounds = "*")
public class TCPlugin implements TerrainControlEngine
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

        // Start engine
        TerrainControl.startEngine(this);
        TerrainControl.supportedBlockIds = 4095;
        // Register localization
        LanguageRegistry.instance().addStringLocalization("generator.TerrainControl", "TerrainControl");
        // Register world type
        worldType = new TCWorldType(this, 4, "TerrainControl");
        // Register channel
        if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            NetworkRegistry.instance().registerChannel(new PacketHandler(this), TCDefaultValues.ChannelName.stringValue());
        }
        // Register player tracker
        GameRegistry.registerPlayerTracker(new PlayerTracker(this));
    }

    @PostInit
    public void postInit(FMLPostInitializationEvent event)
    {
        // Stub Method
    }

    @Override
    public LocalWorld getWorld(String name)
    {
        LocalWorld world = worldType.worldTC;
        if(world == null)
        {
            return null;
        }
        String worldName = MinecraftServer.getServer().worldServers[0].getSaveHandler().getSaveDirectoryName();
        if(world.getName() == worldName)
        {
            return world;
        }
        else
        {
            // Outdated world stored
            worldType.worldTC = null;
            return null;
        }
        
    }

    @Override
    public void log(Level level, String... messages)
    {
        System.out.println("TerrainControl: " + Txt.implode(messages, ","));
    }

    @Override
    public File getGlobalObjectsDirectory()
    {
        return new File(terrainControlDirectory, BODefaultValues.BO_GlobalDirectoryName.stringValue());
    }

}
