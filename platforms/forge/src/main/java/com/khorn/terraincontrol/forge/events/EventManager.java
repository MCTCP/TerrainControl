package com.khorn.terraincontrol.forge.events;

import com.khorn.terraincontrol.LocalMaterialData;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.events.EventHandler;
import com.khorn.terraincontrol.forge.ForgeWorld;
import com.khorn.terraincontrol.generator.resource.*;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;
import cpw.mods.fml.common.registry.GameRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent.Decorate;
import net.minecraftforge.event.terraingen.OreGenEvent;
import net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate;
import net.minecraftforge.event.terraingen.TerrainGen;

/**
 * Translates TerrainControl events into MinecraftForge terrain events
 */
public class EventManager extends EventHandler
{
    // Two maps, key is world name, value is boolean
    private Map<String, Boolean> hasOreGenBegun = new HashMap<String, Boolean>();
    private Map<String, Boolean> hasDecorationBegun = new HashMap<String, Boolean>();

    @Override
    public boolean onResourceProcess(Resource resource, LocalWorld localWorld, Random random, boolean villageInChunk, int chunkX, int chunkZ, boolean isCancelled)
    {
        final ForgeWorld world = (ForgeWorld) localWorld;

        // Convert to Forge event and fire
        if (resource instanceof DungeonGen ||
                resource instanceof SmallLakeGen ||
                resource instanceof UndergroundLakeGen ||
                resource instanceof LiquidGen ||
                resource instanceof CustomObjectGen)
        {
            // Fire population event
            Populate.EventType forgeEvent = getPopulateEventType(resource.getMaterial());
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
            GenerateMinable.EventType forgeEvent = getOreEventType(resource.getMaterial());
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
            Decorate.EventType forgeEvent = getDecorateEventType(resource.getMaterial());
            return TerrainGen.decorate(world.getWorld(), random, chunkX, chunkZ, forgeEvent);
        }
    }

    @Override
    public void onPopulateStart(LocalWorld localWorld, Random random, boolean villageInChunk, int chunkX, int chunkZ)
    {
        final ForgeWorld world = (ForgeWorld) localWorld;

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
        final ForgeWorld world = (ForgeWorld) localWorld;

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

    private Decorate.EventType getDecorateEventType(LocalMaterialData block)
    {
        if (block == null)
        {
            // Some resources don't have a main material
            return Decorate.EventType.CUSTOM;
        }
        if (block.isMaterial(DefaultMaterial.WATER_LILY))
            return Decorate.EventType.LILYPAD;
        if (block.isMaterial(DefaultMaterial.CACTUS))
            return Decorate.EventType.CACTUS;
        if (block.isMaterial(DefaultMaterial.LONG_GRASS))
            return Decorate.EventType.GRASS;
        if (block.isMaterial(DefaultMaterial.DEAD_BUSH))
            return Decorate.EventType.DEAD_BUSH;
        if (block.isMaterial(DefaultMaterial.RED_ROSE) || block.isMaterial(DefaultMaterial.YELLOW_FLOWER) || block.isMaterial(DefaultMaterial.DOUBLE_PLANT))
            return Decorate.EventType.FLOWERS;
        if (block.isMaterial(DefaultMaterial.PUMPKIN))
            return Decorate.EventType.PUMPKIN;
        if (block.isMaterial(DefaultMaterial.BROWN_MUSHROOM) || block.isMaterial(DefaultMaterial.RED_MUSHROOM))
            return Decorate.EventType.SHROOM;
        if (block.isMaterial(DefaultMaterial.SUGAR_CANE_BLOCK))
            return Decorate.EventType.REED;
        if (block.isMaterial(DefaultMaterial.SAND))
            return Decorate.EventType.SAND;
        if (block.isMaterial(DefaultMaterial.CLAY))
            return Decorate.EventType.CLAY;
        return Decorate.EventType.CUSTOM;
    }

    private Populate.EventType getPopulateEventType(LocalMaterialData block)
    {
        if (block == null)
        {
            // BO2s and BO3s don't have one material
            return Populate.EventType.CUSTOM;
        }
        if (block.isMaterial(DefaultMaterial.WATER) || block.isMaterial(DefaultMaterial.STATIONARY_WATER))
            return Populate.EventType.LAKE;
        if (block.isMaterial(DefaultMaterial.LAVA) || block.isMaterial(DefaultMaterial.STATIONARY_LAVA))
            return Populate.EventType.LAVA;
        return Populate.EventType.CUSTOM;
    }

    private GenerateMinable.EventType getOreEventType(LocalMaterialData block)
    {
        if (block.isMaterial(DefaultMaterial.COAL_ORE))
            return GenerateMinable.EventType.COAL;
        if (block.isMaterial(DefaultMaterial.DIAMOND_ORE))
            return GenerateMinable.EventType.DIAMOND;
        if (block.isMaterial(DefaultMaterial.DIRT))
            return GenerateMinable.EventType.DIRT;
        if (block.isMaterial(DefaultMaterial.GOLD_ORE))
            return GenerateMinable.EventType.GOLD;
        if (block.isMaterial(DefaultMaterial.GRAVEL))
            return GenerateMinable.EventType.GRAVEL;
        if (block.isMaterial(DefaultMaterial.IRON_ORE))
            return GenerateMinable.EventType.IRON;
        if (block.isMaterial(DefaultMaterial.LAPIS_ORE))
            return GenerateMinable.EventType.LAPIS;
        if (block.isMaterial(DefaultMaterial.REDSTONE_ORE))
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
