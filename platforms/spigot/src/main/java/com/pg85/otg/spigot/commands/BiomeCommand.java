package com.pg85.otg.spigot.commands;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.spigot.gen.OTGNoiseChunkGenerator;

import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_16_R3.BiomeBase;
import net.minecraft.server.v1_16_R3.BiomeSettingsMobs.c;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.EnumCreatureType;
import net.minecraft.server.v1_16_R3.IRegistry;
import net.minecraft.server.v1_16_R3.WeightedRandom.WeightedRandomChoice;
import net.minecraft.server.v1_16_R3.WorldServer;

public class BiomeCommand extends BaseCommand
{
	private static final List<String> TYPES = new ArrayList<>(Arrays.asList("info", "spawns"));
	private static Field weightField;

	static
	{
		try
		{
			weightField = WeightedRandomChoice.class.getDeclaredField("a");
			weightField.setAccessible(true);
		} catch (ReflectiveOperationException e)
		{
			e.printStackTrace();
		}
	}

	public BiomeCommand()
	{
		this.name = "biome";
		this.helpMessage = "Displays information about the biome you are in.";
		this.usage = "/otg biome";
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args)
	{
		return StringUtil.copyPartialMatches(args[1], TYPES, new ArrayList<>());
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

		String option = "";

		if (args.length >= 1)
		{
			option = args[0];
		}

		sender.sendMessage("=====================================================");
		sender.spigot().sendMessage(createComponent("According to OTG, you are in the ", ChatColor.GOLD),
				createComponent(config.getName(), ChatColor.GREEN), createComponent(" biome.", ChatColor.GOLD));
		sender.spigot().sendMessage(
				createComponent("Biome registry name: ", MCBiome, ChatColor.GOLD, ChatColor.GREEN).create());

		switch (option)
		{
		case "info":
			showBiomeInfo(player, biome, config);
			break;
		case "spawns":
			showBiomeMobs(player, biome, config);
			break;
		default:
			break;
		}

		return true;
	}

	private void showBiomeInfo(Player sender, BiomeBase biome, IBiomeConfig config)
	{

		sender.spigot().sendMessage(
				createComponent("Biome Category: ", biome.t().toString(), ChatColor.GOLD, ChatColor.GREEN).create());
		sender.spigot().sendMessage(
				createComponent("Inherit Mobs: ", config.getInheritMobsBiomeName(), ChatColor.GOLD, ChatColor.GREEN)
						.create());

		sender.spigot().sendMessage(
				createComponent("Base Size: ", Integer.toString(config.getBiomeSize()), ChatColor.GOLD, ChatColor.GREEN)
						.append(createComponent(" Biome Rarity: ", Integer.toString(config.getBiomeRarity()),
								ChatColor.GOLD, ChatColor.GREEN).create())
						.create());

		sender.spigot().sendMessage(createComponent("Biome Height: ", String.format("%.2f", config.getBiomeHeight()),
				ChatColor.GOLD, ChatColor.GREEN).create());

		sender.spigot().sendMessage(createComponent("Volatility: ", String.format("%.2f", config.getBiomeVolatility()),
				ChatColor.GOLD, ChatColor.GREEN)
						.append(createComponent(" Volatility1: ", String.format("%.2f", config.getVolatility1()),
								ChatColor.GOLD, ChatColor.GREEN).create())
						.append(createComponent(" Volatility2: ", String.format("%.2f", config.getVolatility2()),
								ChatColor.GOLD, ChatColor.GREEN).create())
						.create());

		sender.spigot().sendMessage(
				createComponent("Base Temperature: ", String.format("%.2f", biome.k()), ChatColor.GOLD, ChatColor.GREEN)
						.append(createComponent(" Current Temperature: ",
								String.format("%.2f",
										biome.getAdjustedTemperature(new BlockPosition(sender.getLocation().getX(),
												sender.getLocation().getY(), sender.getLocation().getZ()))),
								ChatColor.GOLD, ChatColor.GREEN).create())
						.create());
	}

	private void showBiomeMobs(Player sender, BiomeBase biome, IBiomeConfig config)
	{
		sender.spigot().sendMessage(createComponent("Spawns:", ChatColor.GOLD));
		sender.spigot().sendMessage(createComponent("  Monsters:", ChatColor.GOLD));
		showSpawns(sender, biome.b().a(EnumCreatureType.MONSTER));
		sender.spigot().sendMessage(createComponent("  Creatures:", ChatColor.GOLD));
		showSpawns(sender, biome.b().a(EnumCreatureType.CREATURE));
		sender.spigot().sendMessage(createComponent("  Water Creatures:", ChatColor.GOLD));
		showSpawns(sender, biome.b().a(EnumCreatureType.CREATURE));
		sender.spigot().sendMessage(createComponent("  Ambient Creatures:", ChatColor.GOLD));
		showSpawns(sender, biome.b().a(EnumCreatureType.AMBIENT));
		sender.spigot().sendMessage(createComponent("  Water Ambient:", ChatColor.GOLD));
		showSpawns(sender, biome.b().a(EnumCreatureType.WATER_AMBIENT));
		sender.spigot().sendMessage(createComponent("  Misc:", ChatColor.GOLD));
		showSpawns(sender, biome.b().a(EnumCreatureType.MISC));

	}

	public void showSpawns(Player sender, List<c> list)
	{
		list.forEach(spawn ->
		{
			try
			{
				sender.spigot()
						.sendMessage(createComponent("   - Entity: ", IRegistry.ENTITY_TYPE.getKey(spawn.c).toString(),
								ChatColor.GOLD, ChatColor.GREEN).append(
										createComponent(", Weight: ", Integer.toString(weightField.getInt(spawn)),
												ChatColor.GOLD, ChatColor.GREEN).create())
										.append(createComponent(", Min: ", Integer.toString(spawn.d), ChatColor.GOLD,
												ChatColor.GREEN).create())
										.append(createComponent(", Max: ", Integer.toString(spawn.e), ChatColor.GOLD,
												ChatColor.GREEN).create())
										.create());
			} catch (ReflectiveOperationException e)
			{
				e.printStackTrace();
			}
		});
	}
}
