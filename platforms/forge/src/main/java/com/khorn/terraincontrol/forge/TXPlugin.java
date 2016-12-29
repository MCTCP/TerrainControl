package com.khorn.terraincontrol.forge;

import com.google.common.base.Function;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;
import com.khorn.terraincontrol.events.EventPriority;
import com.khorn.terraincontrol.exception.BiomeNotFoundException;
import com.khorn.terraincontrol.forge.client.events.ClientNetworkEventListener;
import com.khorn.terraincontrol.forge.events.*;
import com.khorn.terraincontrol.forge.generator.ForgeVanillaBiomeGenerator;
import com.khorn.terraincontrol.forge.generator.structure.TXRareBuildingStart;
import com.khorn.terraincontrol.forge.generator.structure.TXVillageStart;
import com.khorn.terraincontrol.generator.biome.VanillaBiomeGenerator;
import com.khorn.terraincontrol.util.minecraftTypes.StructureNames;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.File;

@Mod(modid = "terraincontrol", name = "TerrainControl", acceptableRemoteVersions = "*")
public class TXPlugin
{
    private WorldLoader worldLoader;

    @EventHandler
    public void load(FMLInitializationEvent event)
    {
        // This is the place where the mod starts loading
        File configsDir = new File(Loader.instance().getConfigDir(), "TerrainControl");
        this.worldLoader = new WorldLoader(configsDir);
        // Register World listener for tracking world unloads and loads.
        MinecraftForge.EVENT_BUS.register(new WorldListener(this.worldLoader));

        // Create the world type. WorldType registers itself in the constructor
        // - that is Mojang code, so don't blame me
        new TXWorldType(this.worldLoader);

        // Start TerrainControl engine
        final ForgeEngine engine = new ForgeEngine(this.worldLoader);
        TerrainControl.setEngine(engine);

        // Register Default biome generator to TerrainControl
        engine.getBiomeModeManager().register(VanillaBiomeGenerator.GENERATOR_NAME, ForgeVanillaBiomeGenerator.class);

        // Register village and rare building starts
        MapGenStructureIO.registerStructure(TXRareBuildingStart.class, StructureNames.RARE_BUILDING);
        MapGenStructureIO.registerStructure(TXVillageStart.class, StructureNames.VILLAGE);

        // Register listening channel for listening to received configs.
        if (event.getSide() == Side.CLIENT)
        {
            ClientNetworkEventListener networkHandler = new ClientNetworkEventListener(this.worldLoader);
            FMLEventChannel eventDrivenChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(
                    PluginStandardValues.ChannelName);
            eventDrivenChannel.register(networkHandler);
            MinecraftForge.EVENT_BUS.register(networkHandler);
        }

        // Register player tracker, for sending configs.
        MinecraftForge.EVENT_BUS.register(new PlayerTracker(this.worldLoader));

        // Register sapling tracker, for custom tree growth.
        SaplingListener saplingListener = new SaplingListener(this.worldLoader);
        MinecraftForge.TERRAIN_GEN_BUS.register(saplingListener);
        MinecraftForge.EVENT_BUS.register(saplingListener);

        // Register colorizer, for biome colors
        Function<Biome, BiomeConfig> getBiomeConfig = new Function<Biome, BiomeConfig>()
        {
            @Override
            public BiomeConfig apply(Biome input)
            {
                LocalBiome biome = null;
                for (LocalWorld world : TXPlugin.this.worldLoader.worlds.values())
                {
                    try
                    {
                        biome = world.getBiomeByName(input.getBiomeName());
                        break;
                    } catch (BiomeNotFoundException e)
                    {
                        // Ignored, try in next world
                    }
                }

                if (biome == null)
                {
                    return null;
                }

                return biome.getBiomeConfig();
            }
        };
        MinecraftForge.EVENT_BUS.register(new BiomeColorsListener(getBiomeConfig));

        // Register to our own events, so that they can be fired again as Forge events.
        engine.registerEventHandler(new TCToForgeEventConverter(), EventPriority.CANCELABLE);
    }

    @SideOnly(Side.CLIENT)
    @EventHandler
    public void onIntegratedServerAboutToStart(FMLServerAboutToStartEvent event)
    {
        this.worldLoader.onServerAboutToLoad();
    }

    @SideOnly(Side.SERVER)
    @EventHandler
    public void onDedicatedServerPostInit(FMLPostInitializationEvent event)
    {
        this.worldLoader.onServerAboutToLoad();
    }

    @SideOnly(Side.SERVER)
    @EventHandler
    public void onDedicatedServerStopped(FMLServerStoppingEvent event)
    {
        this.worldLoader.onServerStopped();
    }

    @EventHandler
    public void serverLoad(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new TXCommandHandler(this.worldLoader));
    }

}