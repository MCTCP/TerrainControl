package com.pg85.otg.spigot.events;

import com.pg85.otg.OTG;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.StructureGrowEvent;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.spigot.OTGPlugin;
import org.bukkit.event.world.WorldLoadEvent;

import java.io.File;
import java.io.IOException;

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

	@EventHandler(priority = EventPriority.LOW)
	public void onWorldLoaded(WorldLoadEvent evt) {
		World world = evt.getWorld();
		File WORLD_PRELOADED_FILE = new File(world.getWorldFolder() + "/WORLD_PRELOADED");
		if (!WORLD_PRELOADED_FILE.exists()) {
			Location spawn = world.getSpawnLocation();
			int Y;
			for (Y = world.getMaxHeight()-1; world.getBlockAt(spawn.getBlockX(), Y, spawn.getBlockZ()).getType() == Material.AIR; Y--);
			world.setSpawnLocation(spawn.getBlockX(), Y, spawn.getBlockZ());
			try {
				WORLD_PRELOADED_FILE.createNewFile();
			} catch (IOException e) {
				ILogger log = OTG.getEngine().getLogger();
				log.log(LogLevel.WARN, LogCategory.MAIN,"Could not save data that the world is already loaded! Spawn will be reset next time the server restarts!");
				log.log(LogLevel.WARN, LogCategory.MAIN, "Message: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}
}
