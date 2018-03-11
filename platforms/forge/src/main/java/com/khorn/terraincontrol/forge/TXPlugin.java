package com.khorn.terraincontrol.forge;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.events.EventPriority;
import com.khorn.terraincontrol.forge.events.*;
import com.khorn.terraincontrol.forge.generator.ForgeVanillaBiomeGenerator;
import com.khorn.terraincontrol.forge.generator.structure.TXRareBuildingStart;
import com.khorn.terraincontrol.forge.generator.structure.TXVillageStart;
import com.khorn.terraincontrol.generator.biome.VanillaBiomeGenerator;
import com.khorn.terraincontrol.util.minecraftTypes.StructureNames;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

import java.io.File;

@Mod(modid = "terraincontrol", name = "TerrainControl", acceptableRemoteVersions = "*")
public class TXPlugin
{
    @Mod.Instance
    public static TXPlugin instance;

    public TXWorldType worldType;
    public WorldLoader worldLoader;

    @EventHandler
    public void load(FMLInitializationEvent event)
    {
        // This is the place where the mod starts loading
        File configsDir = new File(Loader.instance().getConfigDir(), "TerrainControl");
        this.worldLoader = new WorldLoader(configsDir);

        // Create the world type. WorldType registers itself in the constructor
        // - that is Mojang code, so don't blame me
        this.worldType = new TXWorldType(this.worldLoader);

        // Start TerrainControl engine
        final ForgeEngine engine = new ForgeEngine(this.worldLoader);
        TerrainControl.setEngine(engine);

        // Register Default biome generator to TerrainControl
        engine.getBiomeModeManager().register(VanillaBiomeGenerator.GENERATOR_NAME, ForgeVanillaBiomeGenerator.class);

        // Register village and rare building starts
        MapGenStructureIO.registerStructure(TXRareBuildingStart.class, StructureNames.RARE_BUILDING);
        MapGenStructureIO.registerStructure(TXVillageStart.class, StructureNames.VILLAGE);

        // Register sapling tracker, for custom tree growth.
        SaplingListener saplingListener = new SaplingListener(this.worldLoader);
        MinecraftForge.TERRAIN_GEN_BUS.register(saplingListener);
        MinecraftForge.EVENT_BUS.register(this.worldLoader);
        MinecraftForge.EVENT_BUS.register(saplingListener);

        // Register to our own events, so that they can be fired again as Forge events.
        engine.registerEventHandler(new TCToForgeEventConverter(), EventPriority.CANCELABLE);
    }

    @EventHandler
    public void serverLoad(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new TXCommandHandler());
    }
}