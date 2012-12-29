package com.khorn.terraincontrol.forge;

import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent.Decorate;
import net.minecraftforge.event.terraingen.OreGenEvent;
import net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate;
import net.minecraftforge.event.terraingen.TerrainGen;

import com.khorn.terraincontrol.DefaultMaterial;
import com.khorn.terraincontrol.events.ChunkEvent;
import com.khorn.terraincontrol.events.EventHandler;
import com.khorn.terraincontrol.events.PopulateEvent;
import com.khorn.terraincontrol.events.ResourceEvent;

import cpw.mods.fml.common.registry.GameRegistry;

/**
 * Translates TerrainControl events into MinecraftForge terrain events
 * 
 */
public class EventManager extends EventHandler
{
    private static Decorate.EventType getAboveWaterEventType(ResourceEvent event)
    {
        if (event.getBlockId() == DefaultMaterial.WATER_LILY.id)
            return Decorate.EventType.LILYPAD;
        return Decorate.EventType.CUSTOM;
    }
    
    private static Decorate.EventType getCactusEventType(ResourceEvent event)
    {
        if (event.getBlockId() == DefaultMaterial.CACTUS.id)
            return Decorate.EventType.CACTUS;
        return Decorate.EventType.CUSTOM;
    }

    private static Decorate.EventType getGrassEventType(ResourceEvent event)
    {
        if (event.getBlockId() == DefaultMaterial.LONG_GRASS.id)
            return Decorate.EventType.GRASS;
        if (event.getBlockId() == DefaultMaterial.DEAD_BUSH.id)
            return Decorate.EventType.DEAD_BUSH;
        return Decorate.EventType.CUSTOM;
    }
    
    private static Populate.EventType getLiquidEventType(ResourceEvent event)
    {
        return event.getBlockId() == DefaultMaterial.WATER.id ? Populate.EventType.LAKE
             : event.getBlockId() == DefaultMaterial.LAVA.id ? Populate.EventType.LAVA
             : Populate.EventType.CUSTOM;
    }

    private static GenerateMinable.EventType getOreEventType(ResourceEvent event)
    {
        if (event.getBlockId() == DefaultMaterial.COAL_ORE.id)
            return GenerateMinable.EventType.COAL;
        if (event.getBlockId() == DefaultMaterial.DIAMOND_ORE.id)
            return GenerateMinable.EventType.DIAMOND;
        if (event.getBlockId() == DefaultMaterial.DIRT.id)
            return GenerateMinable.EventType.DIRT;
        if (event.getBlockId() == DefaultMaterial.GOLD_ORE.id)
            return GenerateMinable.EventType.GOLD;
        if (event.getBlockId() == DefaultMaterial.GRAVEL.id)
            return GenerateMinable.EventType.GRAVEL;
        if (event.getBlockId() == DefaultMaterial.IRON_ORE.id)
            return GenerateMinable.EventType.IRON;
        if (event.getBlockId() == DefaultMaterial.LAPIS_ORE.id)
            return GenerateMinable.EventType.LAPIS;
        if (event.getBlockId() == DefaultMaterial.REDSTONE_ORE.id)
            return GenerateMinable.EventType.REDSTONE;
        return GenerateMinable.EventType.CUSTOM;
    }
    
    private static Decorate.EventType getPlantEventType(ResourceEvent event)
    {
        if (event.getBlockId() == DefaultMaterial.RED_ROSE.id || event.getBlockId() == DefaultMaterial.YELLOW_FLOWER.id)
            return Decorate.EventType.FLOWERS;
        if (event.getBlockId() == DefaultMaterial.PUMPKIN.id)
            return Decorate.EventType.PUMPKIN;
        if (event.getBlockId() == DefaultMaterial.BROWN_MUSHROOM.id || event.getBlockId() == DefaultMaterial.RED_MUSHROOM.id)
            return Decorate.EventType.SHROOM;
        return Decorate.EventType.CUSTOM;
    }

    private static Decorate.EventType getReedEventType(ResourceEvent event) {
        if (event.getBlockId() == DefaultMaterial.SUGAR_CANE_BLOCK.id)
            return Decorate.EventType.REED;
        return Decorate.EventType.CUSTOM;
    }

    SingleWorld worldTC;

    World worldMC;

    private boolean hasDecorationBegun;

    private boolean hasOreGenBegun;

    public EventManager(SingleWorld worldTC, World worldMC)
    {
        this.worldTC = worldTC;
        this.worldMC = worldMC;
    }

    private void closeDecoration(PopulateEvent event) {
        if (hasDecorationBegun)
        {
            MinecraftForge.EVENT_BUS.post(new DecorateBiomeEvent.Post(worldMC, event.getRandom(), event.getChunkX(), event.getChunkZ()));
            hasDecorationBegun = false;
        }
    }

    private void closeOreGeneration(ChunkEvent event) {
        if (hasOreGenBegun)
        {
            MinecraftForge.ORE_GEN_BUS.post(new OreGenEvent.Post(worldMC, event.getRandom(), event.getChunkX(), event.getChunkZ()));
            hasOreGenBegun = false;
        }
    }

    private Decorate.EventType getDecorateEventType(ResourceEvent event)
    {
        switch(event.getType())
        {
        case CACTUS:
            return getCactusEventType(event);
        case ABOVE_WATER:
            return getAboveWaterEventType(event);
        case GRASS:
            return getGrassEventType(event);
        case PLANT:
            return getPlantEventType(event);
        case REED:
            return getReedEventType(event);
        case TREE:
            return Decorate.EventType.TREE;
        case UNDERWATER_ORE:
            if (event.getBlockId() == DefaultMaterial.CLAY.id)
                return Decorate.EventType.CLAY;
            if (event.getBlockId() == DefaultMaterial.SAND.id)
                return Decorate.EventType.SAND;
            break;
         default:
             // Use CUSTOM type
        }
        return Decorate.EventType.CUSTOM;
    }

    @Override
    public void onPopulateEvent(PopulateEvent event)
    {
        if (event.getWorld() != worldTC)
            return;

        boolean triggerFMLGeneration = false;
        
        final PopulateChunkEvent forgeEvent;
        switch (event.getType())
        {
        case BEGIN:
            resetState();
            forgeEvent = new PopulateChunkEvent.Pre(worldTC.getChunkGenerator(), worldMC, event.getRandom(), event.getChunkX(), event.getChunkZ(), false);
            break;
        default: // END
            closeOreGeneration(event);
            closeDecoration(event);
            forgeEvent = new PopulateChunkEvent.Post(worldTC.getChunkGenerator(), worldMC, event.getRandom(), event.getChunkX(), event.getChunkZ(), false);
            triggerFMLGeneration = true;
        }
        MinecraftForge.EVENT_BUS.post(forgeEvent);
        
        if (triggerFMLGeneration)
            GameRegistry.generateWorld(event.getChunkX(), event.getChunkZ(), worldMC, worldTC.getChunkGenerator(), worldTC.getChunkGenerator());
    }

    @Override
    public void onResourceEvent(ResourceEvent event)
    {
        if (event.getWorld() != worldTC)
            return;

        boolean isDecorationEvent = false;

        Populate.EventType forgeEventType = Populate.EventType.CUSTOM;
        switch (event.getType())
        {
        case DUNGEON:
            forgeEventType = Populate.EventType.DUNGEON;
            break;
        case SMALL_LAKE:
        case LIQUID:
           forgeEventType = getLiquidEventType(event);
            break;
        case ABOVE_WATER:
        case CACTUS:
        case GRASS:
        case ORE:
        case PLANT:
        case REED:
        case TREE:
        case UNDERWATER_ORE:
            isDecorationEvent = true;
            break;
        default:
            // Use custom type.
        }
        if (!isDecorationEvent)
            postForgeEvent(event, forgeEventType);
        else
            processDecorationEvent(event);
    }
    
    private void openDecoration(ResourceEvent event) {
        if (!hasDecorationBegun)
        {
            MinecraftForge.EVENT_BUS.post(new DecorateBiomeEvent.Pre(worldMC, event.getRandom(), event.getChunkX(), event.getChunkZ()));
            hasDecorationBegun = true;
        }
    }

    private void postForgeEvent(ResourceEvent event, Decorate.EventType forgeEventType)
    {
        closeOreGeneration(event);
        if (!TerrainGen.decorate(worldMC, event.getRandom(), event.getChunkX(), event.getChunkZ(), forgeEventType))
            event.cancel();
    }

    private void postForgeEvent(ResourceEvent event, Populate.EventType forgeEventType)
    {
        if (hasDecorationBegun)
        {
            MinecraftForge.EVENT_BUS.post(new DecorateBiomeEvent.Post(worldMC, event.getRandom(), event.getChunkX(), event.getChunkZ()));
            hasDecorationBegun = false;
        }
        if (!TerrainGen.populate(worldTC.getChunkGenerator(), worldMC, event.getRandom(), event.getChunkX(), event.getChunkZ(), event.hasGeneratedAVillage(), forgeEventType))
            event.cancel();
    }

    private void postForgeOreGenEvent(ResourceEvent event)
    {
        if (!hasOreGenBegun)
        {
            MinecraftForge.ORE_GEN_BUS.post(new OreGenEvent.Pre(worldMC, event.getRandom(), event.getChunkX(), event.getChunkZ()));
            hasOreGenBegun = true;
        }
        if (!TerrainGen.generateOre(worldMC, event.getRandom(), null, event.getChunkX(), event.getChunkZ(), getOreEventType(event)))
        {
            event.cancel();
        }
    }

    private void processDecorationEvent(ResourceEvent event)
    {
        openDecoration(event);
        switch(event.getType())
        {
        case ORE:
            postForgeOreGenEvent(event);
            break;
        default:
            postForgeEvent(event, getDecorateEventType(event));
       }
    }

    private void resetState()
    {
        hasDecorationBegun = false;
        hasOreGenBegun = false;
    }
}
