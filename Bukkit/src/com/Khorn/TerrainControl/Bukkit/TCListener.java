package com.Khorn.TerrainControl.Bukkit;

import com.Khorn.TerrainControl.Bukkit.Commands.BaseCommand;
import com.Khorn.TerrainControl.CustomObjects.CustomObjectGen;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.event.world.WorldInitEvent;


import java.util.Random;

public class TCListener implements Listener
{
    private TCPlugin tcPlugin;
    private Random random;

    public TCListener(TCPlugin plugin)
    {
        this.tcPlugin = plugin;
        this.random = new Random();
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(event = WorldInitEvent.class, priority = EventPriority.HIGH)
    public void onWorldInit(WorldInitEvent event)
    {

        this.tcPlugin.WorldInit(event.getWorld());

    }

    @EventHandler(event = StructureGrowEvent.class, priority = EventPriority.NORMAL)
    public void onStructureGrow(StructureGrowEvent event)
    {
        BukkitWorld bukkitWorld = this.tcPlugin.worlds.get(event.getWorld().getUID());
        if (bukkitWorld != null && bukkitWorld.getSettings().HasCustomTrees)
        {
            if (this.random.nextInt(100) < bukkitWorld.getSettings().customTreeChance)
            {
                CustomObjectGen.SpawnCustomTrees(bukkitWorld, this.random, bukkitWorld.getSettings(), event.getLocation().getBlockX(), event.getLocation().getBlockY(), event.getLocation().getBlockZ());
                event.getBlocks().clear();
            }

        }

    }

    @EventHandler(event = PlayerInteractEvent.class, priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        TCPlayer player = tcPlugin.GetPlayer(event.getPlayer());
        if (player.hasObjectToSpawn)
        {
            Block block = event.getClickedBlock();
            BukkitWorld bukkitWorld = this.tcPlugin.worlds.get(block.getWorld().getUID());
            if (bukkitWorld != null)
            {
                net.minecraft.server.World world = ((CraftWorld) block.getWorld()).getHandle();

                if (CustomObjectGen.GenerateCustomObject(bukkitWorld, new Random(), bukkitWorld.getSettings(), block.getX(), block.getY(), block.getZ(), player.object, true))
                    event.getPlayer().sendMessage(BaseCommand.MessageColor + player.object.name + " spawned");
                else
                    event.getPlayer().sendMessage(BaseCommand.ErrorColor + "This object cant spawn here");
                player.hasObjectToSpawn = false;
                event.setCancelled(true);
            }

        }
    }

}
