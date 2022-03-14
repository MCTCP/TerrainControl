package com.pg85.otg.paper.events;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.SpawnChangeEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.event.world.WorldLoadEvent;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.core.OTG;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IWorldConfig;
import com.pg85.otg.paper.OTGPlugin;
import com.pg85.otg.paper.gen.OTGNoiseChunkGenerator;
import com.pg85.otg.paper.gen.OTGPaperChunkGen;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;

import net.minecraft.server.level.ServerLevel;
import org.bukkit.permissions.PermissionAttachment;

public class OTGHandler implements Listener
{
	private final SaplingHandler saplingHandler;

	public OTGHandler(OTGPlugin plugin)
	{
		this.saplingHandler = new SaplingHandler();
		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
		Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(plugin, Constants.MOD_ID_SHORT + ":spigot");
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerJoin(PlayerJoinEvent evt) {
		PermissionAttachment attachment = evt.getPlayer().addAttachment(OTGPlugin.plugin);
		attachment.setPermission("minecraft.command.otg", true);
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
		if (!WORLD_PRELOADED_FILE.exists() && world.getGenerator() instanceof OTGPaperChunkGen) {
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

	boolean spawnPointSet = false;
	@EventHandler(priority = EventPriority.LOW)
	public void onSpawnChange(SpawnChangeEvent event)
	{
		ServerLevel world = ((CraftWorld)event.getWorld()).getHandle();
		if ((world.getChunkSource().getGenerator() instanceof OTGNoiseChunkGenerator))
		{
			// TODO: How to cancel the event?
			if(this.spawnPointSet)
			{
				return;
			}
			this.spawnPointSet = true;

			IWorldConfig worldConfig = ((OTGNoiseChunkGenerator)(world.getChunkSource().getGenerator())).getPreset().getWorldConfig();
			if(worldConfig.getSpawnPointSet())
			{
				if(
					event.getWorld().getSpawnLocation().getBlockX() != worldConfig.getSpawnPointX() ||
					event.getWorld().getSpawnLocation().getBlockY() != worldConfig.getSpawnPointY() ||
					event.getWorld().getSpawnLocation().getBlockZ() != worldConfig.getSpawnPointZ()
				)
				{
					event.getWorld().setSpawnLocation(worldConfig.getSpawnPointX(), worldConfig.getSpawnPointY(), worldConfig.getSpawnPointZ());
				}
			}
		}
	}
}
