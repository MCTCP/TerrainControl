package com.khorn.terraincontrol.forge;

import com.google.common.base.Function;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;
import com.khorn.terraincontrol.events.EventPriority;
import com.khorn.terraincontrol.forge.events.*;
import com.khorn.terraincontrol.forge.generator.ForgeVanillaBiomeGenerator;
import com.khorn.terraincontrol.forge.generator.structure.RareBuildingStart;
import com.khorn.terraincontrol.forge.generator.structure.VillageStart;
import com.khorn.terraincontrol.generator.biome.VanillaBiomeGenerator;
import com.khorn.terraincontrol.util.minecraftTypes.StructureNames;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;

import java.io.File;

@Mod(modid = "terraincontrol", name = "TerrainControl", acceptableRemoteVersions = "*")
public class TCPlugin
{
    private WorldLoader worldLoader;

    @EventHandler
    public void load(FMLInitializationEvent event)
    {
        // This is the place where the mod starts loading
        File configsDir = new File(Loader.instance().getConfigDir(), "TerrainControl");
        worldLoader = new WorldLoader(configsDir);

        // Create the world type. WorldType registers itself in the constructor
        // - that is Mojang code, so don't blame me
        new TCWorldType(worldLoader);

        // Start TerrainControl engine
        final ForgeEngine engine = new ForgeEngine(worldLoader);
        TerrainControl.setEngine(engine);

        // Register Default biome generator to TerrainControl
        engine.getBiomeModeManager().register(VanillaBiomeGenerator.GENERATOR_NAME, ForgeVanillaBiomeGenerator.class);

        // Register village and rare building starts
        MapGenStructureIO.registerStructure(RareBuildingStart.class, StructureNames.RARE_BUILDING);
        MapGenStructureIO.registerStructure(VillageStart.class, StructureNames.VILLAGE);

        // Register listening channel for listening to received configs.
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            ClientNetworkHandler networkHandler = new ClientNetworkHandler(worldLoader);
            FMLEventChannel eventDrivenChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(PluginStandardValues.ChannelName);
            eventDrivenChannel.register(networkHandler);
            MinecraftForge.EVENT_BUS.register(networkHandler);
        }

        // Register player tracker, for sending configs.
        MinecraftForge.EVENT_BUS.register(new PlayerTracker(worldLoader));

        // Register sapling tracker, for custom tree growth.
        SaplingListener saplingListener = new SaplingListener(worldLoader);
        MinecraftForge.TERRAIN_GEN_BUS.register(saplingListener);
        MinecraftForge.EVENT_BUS.register(saplingListener);

        // Register colorizer, for biome colors
        Function<Biome, BiomeConfig> getBiomeConfig = new Function<Biome, BiomeConfig>()
        {
            @Override
            public BiomeConfig apply(Biome input)
            {
                LocalWorld world = engine.getWorld();
                if (world == null)
                    return null;
                LocalBiome biome = world.getBiomeByName(input.getBiomeName());
                if (biome == null)
                    return null;
                return biome.getBiomeConfig();
            }
        };
        MinecraftForge.EVENT_BUS.register(new BiomeColorsListener(getBiomeConfig));

        // Register to our own events, so that they can be fired again as
        // Forge events.
        engine.registerEventHandler(new TCToForgeEventConverter(), EventPriority.CANCELABLE);
    }

    @EventHandler
    public void serverAboutToStart(FMLServerAboutToStartEvent event)
    {
        worldLoader.onWorldAboutToLoad(event.getServer());
    }

    @EventHandler
    public void serverLoad(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new TCCommandHandler(worldLoader));
    }

    @EventHandler
    public void serverStopped(FMLServerStoppingEvent event)
    {
        worldLoader.onServerStopped();
    }
}