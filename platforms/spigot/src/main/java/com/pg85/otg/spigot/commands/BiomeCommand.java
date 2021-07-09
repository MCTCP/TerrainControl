package com.pg85.otg.spigot.commands;

import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.entity.Player;

import com.pg85.otg.spigot.gen.OTGNoiseChunkGenerator;

import net.minecraft.server.v1_16_R3.BiomeBase;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.IRegistry;
import net.minecraft.server.v1_16_R3.WorldServer;

public class BiomeCommand implements BaseCommand
{
	@Override
	public boolean execute(CommandSender sender, String[] args)
	{
		if (!(sender instanceof Player))
		{
			sender.sendMessage("Only players can execute this command");
			return true;
		}
		Player player = (Player) sender;
		WorldServer world = ((CraftWorld) player.getWorld()).getHandle();

		if (!(world.getChunkProvider().getChunkGenerator() instanceof OTGNoiseChunkGenerator))
		{
			sender.sendMessage("OTG is not enabled in this world");
			return true;
		}

		BiomeBase biome = world.getBiome(new BlockPosition(player.getLocation().getBlockX(),
				player.getLocation().getBlockY(), player.getLocation().getBlockZ()));

		String MCBiome = ((CraftServer) Bukkit.getServer()).getServer().getCustomRegistry().b(IRegistry.ay)
				.getKey(biome).toString();
		String OTGBiome = ((OTGNoiseChunkGenerator) world.getChunkProvider().getChunkGenerator())
				.getCachedBiomeProvider()
				.getBiomeConfig(player.getLocation().getBlockX(), player.getLocation().getBlockZ()).getName();

		sender.sendMessage("According to OTG, you are in the " + OTGBiome + " biome.");
		sender.sendMessage("Biome registry name: " + MCBiome);
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args)
	{
		return Collections.emptyList();
	}
}
