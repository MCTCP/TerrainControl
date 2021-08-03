package com.pg85.otg.spigot.commands;

import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.entity.Player;

import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.spigot.gen.OTGNoiseChunkGenerator;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_16_R3.BiomeBase;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.IRegistry;
import net.minecraft.server.v1_16_R3.WorldServer;

public class BiomeCommand extends BaseCommand
{
	public BiomeCommand()
	{
		this.name = "biome";
		this.helpMessage = "Displays information about the biome you are in.";
		this.usage = "/otg biome";
	}

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
		IBiomeConfig config = ((OTGNoiseChunkGenerator) world.getChunkProvider().getChunkGenerator())
				.getCachedBiomeProvider()
				.getBiomeConfig(player.getLocation().getBlockX(), player.getLocation().getBlockZ());

		TextComponent componentA;
		TextComponent componentB;

		sender.sendMessage("According to OTG, you are in the " + config.getName() + " biome.");

		componentA = new TextComponent("Biome registry name: ");
		componentA.setColor(ChatColor.GOLD);
		componentB = new TextComponent(MCBiome);
		componentB.setColor(ChatColor.GREEN);
		sender.spigot().sendMessage(new ComponentBuilder(componentA).append(componentB).create());

		componentA = new TextComponent("Biome Category: ");
		componentA.setColor(ChatColor.GOLD);
		componentB = new TextComponent(biome.t().toString());
		componentB.setColor(ChatColor.GREEN);
		sender.spigot().sendMessage(new ComponentBuilder(componentA).append(componentB).create());

		componentA = new TextComponent("Current Temperature: ");
		componentA.setColor(ChatColor.GOLD);
		componentB = new TextComponent(
				String.format("%.2f", biome.getAdjustedTemperature(new BlockPosition(player.getLocation().getX(),
						player.getLocation().getY(), player.getLocation().getZ()))));
		componentB.setColor(ChatColor.GREEN);
		sender.spigot().sendMessage(new ComponentBuilder(componentA).append(componentB).create());
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args)
	{
		return Collections.emptyList();
	}
}
