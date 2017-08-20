package com.pg85.otg.bukkit.events;

import com.pg85.otg.bukkit.OTGPlugin;
import com.pg85.otg.configuration.standard.PluginStandardValues;
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

public class OTGListener implements Listener
{
    private final OTGPlugin txPlugin;
    private final OTGSender tcSender;
    private final SaplingListener saplingListener;

    public OTGListener(OTGPlugin plugin)
    {
        this.txPlugin = plugin;
        this.tcSender = new OTGSender(plugin);
        this.saplingListener = new SaplingListener();
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onWorldInit(WorldInitEvent event)
    {
        this.txPlugin.onWorldInit(event.getWorld());
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event)
    {
        this.txPlugin.onWorldUnload(event.getWorld());
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
