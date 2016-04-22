package com.khorn.terraincontrol.forge;

import com.google.common.base.Function;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.TerrainControlEngine;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;
import com.khorn.terraincontrol.events.EventPriority;
import com.khorn.terraincontrol.forge.events.*;
import com.khorn.terraincontrol.forge.generator.BiomeGenCustom;
import com.khorn.terraincontrol.forge.generator.ForgeVanillaBiomeGenerator;
import com.khorn.terraincontrol.forge.generator.structure.RareBuildingStart;
import com.khorn.terraincontrol.forge.generator.structure.VillageStart;
import com.khorn.terraincontrol.generator.biome.VanillaBiomeGenerator;
import com.khorn.terraincontrol.util.minecraftTypes.StructureNames;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nullable;
import java.io.File;

@Mod(modid = "TerrainControl", name = "TerrainControl", acceptableRemoteVersions = "*")
public class TCPlugin
{

    @EventHandler
    public void load(FMLInitializationEvent event)
    {
        // This is the place where the mod starts loading

        final ForgeEngine engine = new ForgeEngine(new TCWorldType(PluginStandardValues.PLUGIN_NAME));

        // Start TerrainControl engine, and Register world type
        TerrainControl.setEngine(engine);

        // Register Default biome generator
        engine.getBiomeModeManager().register(VanillaBiomeGenerator.GENERATOR_NAME, ForgeVanillaBiomeGenerator.class);

        // Register village and rare building starts
        MapGenStructureIO.registerStructure(RareBuildingStart.class, StructureNames.RARE_BUILDING);
        MapGenStructureIO.registerStructure(VillageStart.class, StructureNames.VILLAGE);

        // Register listening channel for listening to received configs.
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            FMLEventChannel eventDrivenChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(PluginStandardValues.ChannelName);
            eventDrivenChannel.register(new PacketHandler());
        }

        // Register player tracker, for sending configs.
        MinecraftForge.EVENT_BUS.register(new PlayerTracker());

        // Register sapling tracker, for custom tree growth.
        SaplingListener saplingListener = new SaplingListener();
        MinecraftForge.TERRAIN_GEN_BUS.register(saplingListener);
        MinecraftForge.EVENT_BUS.register(saplingListener);

        // Register colorizer, for biome colors
        Function<BiomeGenBase, BiomeConfig> getBiomeConfig = new Function<BiomeGenBase, BiomeConfig>()
        {
            @Override
            public BiomeConfig apply(BiomeGenBase input)
            {
                LocalBiome biome = engine.getWorld().getBiomeByName(input.getBiomeName());
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
    public void serverLoad(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new TCCommandHandler());
    }

}