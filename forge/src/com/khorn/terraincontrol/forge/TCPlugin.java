package com.khorn.terraincontrol.forge;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.TerrainControlEngine;
import com.khorn.terraincontrol.configuration.TCDefaultValues;
import com.khorn.terraincontrol.customobjects.BODefaultValues;
import com.khorn.terraincontrol.events.EventPriority;
import com.khorn.terraincontrol.util.StringHelper;
import cpw.mods.fml.common.*;
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
import cpw.mods.fml.relauncher.Side;
import net.minecraftforge.common.MinecraftForge;

import java.io.File;
import java.lang.reflect.Field;
import java.util.logging.Level;

@Mod(modid = "TerrainControl", name = "TerrainControl", version = "2.4.10")
@NetworkMod(clientSideRequired = false, serverSideRequired = false, versionBounds = "*")
public class TCPlugin implements TerrainControlEngine
{
    @Instance("TerrainControl")
    public static TCPlugin instance;

    public File terrainControlDirectory;
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

        // Set the directory
        try
        {
            Field minecraftDir = Loader.class.getDeclaredField("minecraftDir");
            minecraftDir.setAccessible(true);
            terrainControlDirectory = new File((File) minecraftDir.get(null), "mods" + File.separator + "TerrainControl");
        } catch (Throwable e)
        {
            terrainControlDirectory = new File("mods" + File.separator + "TerrainControl");
            System.out.println("Could not reflect the Minecraft directory, save location may be unpredicatble.");
            e.printStackTrace();
        }

        // Start TerrainControl engine
        TerrainControl.supportedBlockIds = 4095;
        TerrainControl.startEngine(this);

        // Register localization
        LanguageRegistry.instance().addStringLocalization("generator.TerrainControl", "TerrainControl");

        // Register world type
        worldType = new TCWorldType(this, "TerrainControl");

        // Register listening channel for listening to received configs.
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            NetworkRegistry.instance().registerChannel(new PacketHandler(this), TCDefaultValues.ChannelName.stringValue());
        }

        // Register player tracker, for sending configs.
        GameRegistry.registerPlayerTracker(new PlayerTracker(this));

        // Register sapling tracker, for custom tree growth.
        SaplingListener saplingListener = new SaplingListener();
        MinecraftForge.TERRAIN_GEN_BUS.register(saplingListener);
        MinecraftForge.EVENT_BUS.register(saplingListener);

        // Register to our own events, so that they can be fired again as Forge
        // events.
        // TODO: make this optional for people who haven't installed other
        // terrain mods, and don't want to lose performance.
        TerrainControl.registerEventHandler(new EventManager(), EventPriority.CANCELABLE);
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

    @Override
    public void log(Level level, String... messages)
    {
        FMLCommonHandler.instance().getFMLLogger().log(level, "TerrainControl: " + StringHelper.join(messages, ","));
    }

    @Override
    public File getGlobalObjectsDirectory()
    {
        return new File(terrainControlDirectory, BODefaultValues.BO_GlobalDirectoryName.stringValue());
    }
}
