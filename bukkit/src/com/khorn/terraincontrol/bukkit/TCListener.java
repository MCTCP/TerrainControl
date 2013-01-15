package com.khorn.terraincontrol.bukkit;

import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.generator.resourcegens.SaplingGen;
import com.khorn.terraincontrol.generator.resourcegens.SaplingType;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.event.world.WorldInitEvent;

import java.util.Random;

public class TCListener implements Listener
{
    private TCPlugin tcPlugin;

    public TCListener(TCPlugin plugin)
    {
        this.tcPlugin = plugin;
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onWorldInit(WorldInitEvent event)
    {
        this.tcPlugin.onWorldInit(event.getWorld());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onStructureGrow(StructureGrowEvent event)
    {
        BukkitWorld bukkitWorld = this.tcPlugin.worlds.get(event.getWorld().getUID());
        if (bukkitWorld == null)
            return;

        int x = event.getLocation().getBlockX();
        int y = event.getLocation().getBlockY();
        int z = event.getLocation().getBlockZ();

        int biomeId = bukkitWorld.getCalculatedBiomeId(x, z);
        if (bukkitWorld.getSettings().biomeConfigs[biomeId] == null)
            return;

        BiomeConfig biomeConfig = bukkitWorld.getSettings().biomeConfigs[biomeId];
        SaplingGen sapling;

        switch (event.getSpecies())
        {
            case REDWOOD:
            case TALL_REDWOOD: // Both share the same sapling
                sapling = biomeConfig.getSaplingGen(SaplingType.Redwood);
                break;
            case BIRCH:
                sapling = biomeConfig.getSaplingGen(SaplingType.Birch);
                break;
            case JUNGLE:
                sapling = biomeConfig.getSaplingGen(SaplingType.BigJungle);
                break;
            case SMALL_JUNGLE:
                sapling = biomeConfig.getSaplingGen(SaplingType.SmallJungle);
                break;
            case TREE:
            case BIG_TREE: // Both share the same sapling
                sapling = biomeConfig.getSaplingGen(SaplingType.Oak);
                break;
            case RED_MUSHROOM:
                sapling = biomeConfig.getSaplingGen(SaplingType.RedMushroom);
                break;
            case BROWN_MUSHROOM:
                sapling = biomeConfig.getSaplingGen(SaplingType.BrownMushroom);
                break;
            default:
                sapling = null;
        }

        if (sapling != null)
        {
            boolean success = false;
            for (int i = 0; i < 10; i++)
            {
                if (sapling.growSapling(bukkitWorld, new Random(), x, y, z))
                {
                    success = true;
                    break;
                }
            }

            if (success)
            {
                // Just spawned the tree, clear the blocks list to prevent Bukkit spawning another tree
                event.getBlocks().clear();
            } else
            {
                // Cannot grow, so leave the sapling there
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        // Sends the packet
        // Because the player doesn't send the REGISTER packet immediately, we
        // have to wait a second (20 ticks).
        Bukkit.getScheduler().runTaskLater(tcPlugin, new TCSender(tcPlugin, event.getPlayer()), 20);
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event)
    {
        // Resends the packet so that the biomes are right again
        Bukkit.getScheduler().runTaskLater(tcPlugin, new TCSender(tcPlugin, event.getPlayer()), 1);
    }

}
