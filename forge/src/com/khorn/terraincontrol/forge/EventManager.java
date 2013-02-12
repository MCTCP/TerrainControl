package com.khorn.terraincontrol.forge;

import com.khorn.terraincontrol.DefaultMaterial;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.events.EventHandler;
import com.khorn.terraincontrol.generator.resourcegens.*;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.*;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent.Decorate;
import net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable;
import net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Translates TerrainControl events into MinecraftForge terrain events
 */
public class EventManager extends EventHandler
{
    private Map<String, Boolean> hasOreGenBegun = new HashMap<String, Boolean>(); // world name, boolean
    private Map<String, Boolean> hasDecorationBegun = new HashMap<String, Boolean>();

    @Override
    public boolean onResourceProcess(Resource resource, LocalWorld localWorld, Random random, boolean villageInChunk, int chunkX, int chunkZ, boolean isCancelled)
    {
        final SingleWorld world = (SingleWorld) localWorld;

        // Convert to Forge event and fire
        if (resource instanceof DungeonGen ||
                resource instanceof SmallLakeGen ||
                resource instanceof UndergroundLakeGen ||
                resource instanceof LiquidGen ||
                resource instanceof CustomObjectGen)
        {
            // Fire population event
            Populate.EventType forgeEvent = getPopulateEventType(resource.getBlockId());
            return TerrainGen.populate(world.getChunkGenerator(), world.getWorld(), random, chunkX, chunkZ, villageInChunk, forgeEvent);
        } else if (resource instanceof OreGen)
        {
            if (!hasOreGenerationBegun(world))
            {
                // Fire ore generation start event
                MinecraftForge.ORE_GEN_BUS.post(new OreGenEvent.Pre(world.getWorld(), random, chunkX, chunkZ));
                setOreGenerationBegun(world, true);
            }
            // Fire ore generation event
            GenerateMinable.EventType forgeEvent = getOreEventType(resource.getBlockId());
            return TerrainGen.generateOre(world.getWorld(), random, null, chunkX, chunkZ, forgeEvent);
        } else
        {
            if (!hasDecorationBegun(world))
            {
                // Fire decoration start event
                MinecraftForge.EVENT_BUS.post(new DecorateBiomeEvent.Pre(world.getWorld(), random, chunkX, chunkZ));
                setDecorationBegun(world, true);
            }
            // Fire decoration event
            Decorate.EventType forgeEvent = getDecorateEventType(resource.getBlockId());
            return TerrainGen.decorate(world.getWorld(), random, chunkX, chunkZ, forgeEvent);
        }
    }

    @Override
    public void onPopulateStart(LocalWorld localWorld, Random random, boolean villageInChunk, int chunkX, int chunkZ)
    {
        final SingleWorld world = (SingleWorld) localWorld;

        // Reset states
        setDecorationBegun(world, false);
        setOreGenerationBegun(world, false);

        // Fire event
        final PopulateChunkEvent forgeEvent = new PopulateChunkEvent.Pre(world.getChunkGenerator(), world.getWorld(), random, chunkX, chunkZ, villageInChunk);
        MinecraftForge.EVENT_BUS.post(forgeEvent);
    }

    @Override
    public void onPopulateEnd(LocalWorld localWorld, Random random, boolean villageInChunk, int chunkX, int chunkZ)
    {
        final SingleWorld world = (SingleWorld) localWorld;

        // Fire all events

        // Decoration close
        if (hasDecorationBegun(world))
        {
            MinecraftForge.EVENT_BUS.post(new DecorateBiomeEvent.Post(world.getWorld(), random, chunkX, chunkZ));
            setDecorationBegun(world, false);
        }

        // Ore generation close
        if (hasOreGenerationBegun(world))
        {
            MinecraftForge.EVENT_BUS.post(new OreGenEvent.Post(world.getWorld(), random, chunkX, chunkZ));
            setOreGenerationBegun(world, false);
        }

        // Population close
        final PopulateChunkEvent forgeEvent = new PopulateChunkEvent.Post(world.getChunkGenerator(), world.getWorld(), random, chunkX, chunkZ, villageInChunk);
        MinecraftForge.EVENT_BUS.post(forgeEvent);

        // Population close (FML and ModLoader style)
        GameRegistry.generateWorld(chunkX, chunkZ, world.getWorld(), world.getChunkGenerator(), world.getChunkGenerator());
    }

    private Decorate.EventType getDecorateEventType(int blockId)
    {
        if (blockId == DefaultMaterial.WATER_LILY.id)
            return Decorate.EventType.LILYPAD;
        if (blockId == DefaultMaterial.CACTUS.id)
            return Decorate.EventType.CACTUS;
        if (blockId == DefaultMaterial.LONG_GRASS.id)
            return Decorate.EventType.GRASS;
        if (blockId == DefaultMaterial.DEAD_BUSH.id)
            return Decorate.EventType.DEAD_BUSH;
        if (blockId == DefaultMaterial.RED_ROSE.id || blockId == DefaultMaterial.YELLOW_FLOWER.id)
            return Decorate.EventType.FLOWERS;
        if (blockId == DefaultMaterial.PUMPKIN.id)
            return Decorate.EventType.PUMPKIN;
        if (blockId == DefaultMaterial.BROWN_MUSHROOM.id || blockId == DefaultMaterial.RED_MUSHROOM.id)
            return Decorate.EventType.SHROOM;
        if (blockId == DefaultMaterial.SUGAR_CANE_BLOCK.id)
            return Decorate.EventType.REED;
        if (blockId == DefaultMaterial.SAND.id)
            return Decorate.EventType.SAND;
        if (blockId == DefaultMaterial.CLAY.id)
            return Decorate.EventType.CLAY;
        return Decorate.EventType.CUSTOM;
    }

    private Populate.EventType getPopulateEventType(int blockId)
    {
        if (blockId == DefaultMaterial.WATER.id)
            return Populate.EventType.LAKE;
        if (blockId == DefaultMaterial.LAVA.id)
            return Populate.EventType.LAVA;
        return Populate.EventType.CUSTOM;
    }

    private GenerateMinable.EventType getOreEventType(int blockId)
    {
        if (blockId == DefaultMaterial.COAL_ORE.id)
            return GenerateMinable.EventType.COAL;
        if (blockId == DefaultMaterial.DIAMOND_ORE.id)
            return GenerateMinable.EventType.DIAMOND;
        if (blockId == DefaultMaterial.DIRT.id)
            return GenerateMinable.EventType.DIRT;
        if (blockId == DefaultMaterial.GOLD_ORE.id)
            return GenerateMinable.EventType.GOLD;
        if (blockId == DefaultMaterial.GRAVEL.id)
            return GenerateMinable.EventType.GRAVEL;
        if (blockId == DefaultMaterial.IRON_ORE.id)
            return GenerateMinable.EventType.IRON;
        if (blockId == DefaultMaterial.LAPIS_ORE.id)
            return GenerateMinable.EventType.LAPIS;
        if (blockId == DefaultMaterial.REDSTONE_ORE.id)
            return GenerateMinable.EventType.REDSTONE;
        return GenerateMinable.EventType.CUSTOM;
    }

    private boolean hasOreGenerationBegun(LocalWorld world)
    {
        return hasOreGenBegun.get(world.getName());
    }

    private boolean hasDecorationBegun(LocalWorld world)
    {
        return hasDecorationBegun.get(world.getName());
    }

    private void setOreGenerationBegun(LocalWorld world, boolean begun)
    {
        hasOreGenBegun.put(world.getName(), begun);
    }

    private void setDecorationBegun(LocalWorld world, boolean begun)
    {
        hasDecorationBegun.put(world.getName(), begun);
    }
}
