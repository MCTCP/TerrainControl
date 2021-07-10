package com.pg85.spigot.events;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.StructureGrowEvent;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.presets.Preset;
import com.pg85.otg.spigot.OTGPlugin;
import com.pg85.otg.spigot.gen.OTGNoiseChunkGenerator;
import com.pg85.otg.spigot.networking.BiomeSettingSyncWrapper;
import com.pg85.otg.spigot.networking.OTGClientSyncManager;
import com.pg85.otg.spigot.networking.OTGPacketBuffer;

import io.netty.buffer.Unpooled;
import net.minecraft.server.v1_16_R3.WorldServer;

public class OTGHandler implements Listener
{
	private final SaplingHandler saplingHandler;
	private final OTGPlugin plugin;

	public OTGHandler(OTGPlugin plugin)
	{
		this.plugin = plugin;
		this.saplingHandler = new SaplingHandler();
		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
		Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(plugin, Constants.MOD_ID_SHORT + ":spigot");
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onStructureGrow(StructureGrowEvent event)
	{
		saplingHandler.onStructureGrow(event);
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event)
	{
		sendDataPacket(event.getPlayer());
	}
	
	@EventHandler
	public void onWorldChange(PlayerChangedWorldEvent event)
	{
		sendDataPacket(event.getPlayer());
	}

	private void sendDataPacket(Player player)
	{
		WorldServer world = ((CraftWorld) player.getWorld()).getHandle();

		if (!(world.getChunkProvider().getChunkGenerator() instanceof OTGNoiseChunkGenerator))
		{
			return;
		}

		Preset preset = ((OTGNoiseChunkGenerator) world.getChunkProvider().getChunkGenerator()).getPreset();

		OTGPacketBuffer buffer = new OTGPacketBuffer(Unpooled.buffer(32766));
		buffer.writeByte((byte) 1);

		for (IBiomeConfig biome : preset.getAllBiomeConfigs())
		{
			String key = biome.getRegistryKey().toResourceLocationString();
			BiomeSettingSyncWrapper wrapper = OTGClientSyncManager.getSyncedData().get(key);

			Bukkit.broadcastMessage(key);

			if (wrapper != null)
			{
				buffer.writeUtf(key);
				wrapper.encode(buffer);
			}
		}

		Bukkit.broadcastMessage(buffer.writerIndex() + "");
		Bukkit.getScheduler().runTaskLater(plugin, () ->
		{
			player.sendPluginMessage(plugin, Constants.MOD_ID_SHORT + ":spigot", buffer.array());
		}, 20);
	}
}
