package com.khorn.terraincontrol.bukkit.events;

import com.khorn.terraincontrol.bukkit.TCPlugin;
import com.khorn.terraincontrol.configuration.standard.PluginStandardValues;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerRegisterChannelEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldUnloadEvent;

public class TCListener implements Listener
{
    private final TCPlugin tcPlugin;
    private final TCSender tcSender;
    private final SaplingListener saplingListener;

    public TCListener(TCPlugin plugin)
    {
        this.tcPlugin = plugin;
        this.tcSender = new TCSender(plugin);
        this.saplingListener = new SaplingListener();
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onWorldInit(WorldInitEvent event)
    {
        this.tcPlugin.onWorldInit(event.getWorld());
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event)
    {
        this.tcPlugin.onWorldUnload(event.getWorld());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onStructureGrow(StructureGrowEvent event)
    {
        saplingListener.onStructureGrow(event);
    }


    @EventHandler
    public void onPlayerRegisterChannel(PlayerRegisterChannelEvent event)
    {
        // Sends custom colors on join
        if (event.getChannel().equals(PluginStandardValues.ChannelName))
        {
            tcSender.send(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event)
    {
        // Resends the packet so that the biomes are right again
        Player player = event.getPlayer();
        if (player.getListeningPluginChannels().contains(PluginStandardValues.ChannelName))
        {
            tcSender.send(player);
        }
    }

}
