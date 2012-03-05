package com.Khorn.TerrainControl.Bukkit;

import com.Khorn.TerrainControl.Bukkit.Commands.BaseCommand;
import com.Khorn.TerrainControl.Configuration.TCDefaultValues;
import com.Khorn.TerrainControl.Configuration.WorldConfig;
import com.Khorn.TerrainControl.CustomObjects.CustomObjectGen;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;


import java.io.*;
import java.util.Random;

public class TCListener implements Listener, PluginMessageListener
{
    private TCPlugin tcPlugin;
    private Random random;

    public TCListener(TCPlugin plugin)
    {
        this.tcPlugin = plugin;
        this.random = new Random();
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onWorldInit(WorldInitEvent event)
    {
        this.tcPlugin.WorldInit(event.getWorld());
    }

    @EventHandler(priority = EventPriority.NORMAL)
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

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        TCPlayer player = tcPlugin.GetPlayer(event.getPlayer());
        if (player.hasObjectToSpawn)
        {
            Block block = event.getClickedBlock();
            BukkitWorld bukkitWorld = this.tcPlugin.worlds.get(block.getWorld().getUID());
            if (bukkitWorld != null)
            {

                if (CustomObjectGen.GenerateCustomObject(bukkitWorld, new Random(), bukkitWorld.getSettings(), block.getX(), block.getY(), block.getZ(), player.object, true))
                    event.getPlayer().sendMessage(BaseCommand.MessageColor + player.object.name + " spawned");
                else
                    event.getPlayer().sendMessage(BaseCommand.ErrorColor + "This object cant spawn here");
                player.hasObjectToSpawn = false;
                event.setCancelled(true);
            }
        }
    }

    public void onPluginMessageReceived(String s, Player player, byte[] bytes)
    {
        if (bytes.length == 1)
        {
            if (bytes[0] == TCDefaultValues.ProtocolVersion.intValue())
            {
                World world = player.getWorld();

                if (this.tcPlugin.worlds.containsKey(world.getUID()))
                {
                    WorldConfig config = this.tcPlugin.worlds.get(world.getUID()).getSettings();

                    System.out.println("TerrainControl: client config requested for world " + config.WorldName);
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    DataOutputStream stream = new DataOutputStream(outputStream);

                    try
                    {
                        config.Serialize(stream);
                        stream.flush();

                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }

                    byte[] data = outputStream.toByteArray();

                    player.sendPluginMessage(this.tcPlugin, TCDefaultValues.ChannelName.stringValue(), data);
                }
            }
            else
            {
            	System.out.println("TerrainControl: client have old TC version");
            }
        }
    }
}
