package com.pg85.otg.bukkit.events;

import com.pg85.otg.OTG;
import com.pg85.otg.bukkit.OTGPlugin;
import com.pg85.otg.bukkit.world.BukkitWorld;
import com.pg85.otg.bukkit.world.WorldHelper;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.util.ChunkCoordinate;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerRegisterChannelEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import com.pg85.otg.bukkit.BukkitEngine;

public class OTGListener implements Listener
{
    private final OTGPlugin otgPlugin;
    private final OTGSender otgSender;
    private final SaplingListener saplingListener;

    public OTGListener(OTGPlugin plugin)
    {
        this.otgPlugin = plugin;
        this.otgSender = new OTGSender(plugin);
        this.saplingListener = new SaplingListener();
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onWorldInit(WorldInitEvent event)
    {
        this.otgPlugin.onWorldInit(event.getWorld());
    }   
    
    // TODO: WorldSave and WorldUnload events don't appear to be working for spigot (using onDisable atm)?
    @EventHandler
    public void onWorldSave(WorldSaveEvent event)
    {
    	((BukkitEngine)OTG.getEngine()).onSave(event.getWorld());
    }
    
    // TODO: WorldSave and WorldUnload events don't appear to be working for spigot (using onDisable atm)?
    @EventHandler
	public void onUnload(WorldUnloadEvent event)
	{
		((BukkitEngine)OTG.getEngine()).onSave(event.getWorld());
	}
    
    @EventHandler
	public void onChunkUnload(ChunkUnloadEvent unloadEvent)
	{
    	BukkitWorld bukkitWorld = (BukkitWorld)WorldHelper.toLocalWorld(unloadEvent.getWorld());
		if(bukkitWorld != null && bukkitWorld.getChunkGenerator() != null && unloadEvent.getChunk() != null)
		{
			bukkitWorld.getChunkGenerator().clearChunkFromCache(ChunkCoordinate.fromChunkCoords(unloadEvent.getChunk().getX(), unloadEvent.getChunk().getZ()));
		}
	}
    
    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event)
    {
        this.otgPlugin.onWorldUnload(event.getWorld());
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
            otgSender.send(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event)
    {
        // Resends the packet so that the biomes are right again
        Player player = event.getPlayer();
        if (player.getListeningPluginChannels().contains(PluginStandardValues.ChannelName))
        {
            otgSender.send(player);
        }
    }
}
