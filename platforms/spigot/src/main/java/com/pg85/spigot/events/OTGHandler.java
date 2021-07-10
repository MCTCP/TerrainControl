package com.pg85.spigot.events;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.StructureGrowEvent;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.spigot.OTGPlugin;

public class OTGHandler implements Listener
{
	private final SaplingHandler saplingHandler;

	public OTGHandler(OTGPlugin plugin)
	{
		this.saplingHandler = new SaplingHandler();
		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
		Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(plugin, Constants.MOD_ID_SHORT + ":spigot");
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onStructureGrow(StructureGrowEvent event)
	{
		saplingHandler.onStructureGrow(event);
	}
}
