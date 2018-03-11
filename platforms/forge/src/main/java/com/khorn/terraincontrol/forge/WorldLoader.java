package com.khorn.terraincontrol.forge;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.ClientConfigProvider;
import com.khorn.terraincontrol.configuration.ConfigFile;
import com.khorn.terraincontrol.configuration.ConfigProvider;
import com.khorn.terraincontrol.configuration.ServerConfigProvider;
import com.khorn.terraincontrol.forge.asm.mixin.iface.IMixinWorld;
import com.khorn.terraincontrol.forge.asm.mixin.iface.IMixinWorldProvider;
import com.khorn.terraincontrol.forge.generator.TXBiome;
import com.khorn.terraincontrol.forge.util.WorldHelper;
import com.khorn.terraincontrol.logging.LogMarker;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.util.IntIdentityHashBiMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * Responsible for loading and unloading the world.
 */
public final class WorldLoader
{
    private final File configsDir;
    private final Map<String, ServerConfigProvider> configMap = Maps.newHashMap();
    private final HashMap<String, ForgeWorld> worlds = new HashMap<String, ForgeWorld>();

    WorldLoader(File configsDir)
    {
        this.configsDir = Preconditions.checkNotNull(configsDir, "configsDir");
    }

    public ForgeWorld getWorld(String name)
    {
        return this.worlds.get(name);
    }

    public File getConfigsFolder()
    {
        return this.configsDir;
    }

    private File getWorldDir(String worldName)
    {
        return new File(this.configsDir, "worlds/" + worldName);
    }

    public void initializeTCWorld(World world) {
        final String worldName = WorldHelper.getWorldName(world);

        TerrainControl.log(LogMarker.INFO, "Checking if we have configs for \"{}\"..", worldName);
        final File worldConfigsFolder = this.getWorldDir(worldName);
        if (!worldConfigsFolder.exists()) {
            TerrainControl.log(LogMarker.INFO, "No configs found for \"{}\".", worldName);
            return;
        }

        final ForgeWorld tcWorld = new ForgeWorld(worldName);
        ServerConfigProvider config = this.configMap.get(worldName);
        if (config == null) {
            TerrainControl.log(LogMarker.INFO, "Loading configs for world \"{}\"..", tcWorld.getName());
        }

        this.worlds.put(worldName, tcWorld);
        config = new ServerConfigProvider(worldConfigsFolder, tcWorld);
        tcWorld.provideConfigs(config);
        this.configMap.put(worldName, config);

        ((IMixinWorld) world).setTCWorld(tcWorld);

        tcWorld.provideWorldInstance((WorldServer) world);

        ((IMixinWorldProvider) world.provider).setBiomeProvider(TXPlugin.instance.worldType.getBiomeProvider(world));
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            return;
        }

        final String worldName = WorldHelper.getWorldName(event.getWorld());
        this.configMap.remove(worldName);
        this.worlds.remove(worldName);
    }
}
