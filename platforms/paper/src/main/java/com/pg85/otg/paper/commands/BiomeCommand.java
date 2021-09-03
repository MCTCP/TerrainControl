package com.pg85.otg.paper.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.paper.gen.OTGNoiseChunkGenerator;

import net.md_5.bungee.api.ChatColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData;

public class BiomeCommand extends BaseCommand {
	private static final List<String> TYPES = new ArrayList<>(Arrays.asList("info", "spawns"));

	public BiomeCommand() {
		super("biome");
		this.helpMessage = "Displays information about the biome you are in.";
		this.usage = "/otg biome";
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args) {
		return StringUtil.copyPartialMatches(args[1], TYPES, new ArrayList<>());
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Only players can execute this command");
			return true;
		}
		Player player = (Player) sender;
		ServerLevel world = ((CraftWorld) player.getWorld()).getHandle();

		if (!(world.getChunkSource().getGenerator() instanceof OTGNoiseChunkGenerator)) {
			sender.sendMessage("OTG is not enabled in this world");
			return true;
		}

		Biome biome = world.getBiome(new BlockPos(player.getLocation().getBlockX(), player.getLocation().getBlockY(),
				player.getLocation().getBlockZ()));

		String MCBiome = ((CraftServer) Bukkit.getServer()).getServer().registryAccess()
				.registryOrThrow(Registry.BIOME_REGISTRY).getKey(biome).toString();
		IBiomeConfig config = ((OTGNoiseChunkGenerator) world.getChunkSource().getGenerator()).getCachedBiomeProvider()
				.getBiomeConfig(player.getLocation().getBlockX(), player.getLocation().getBlockZ());

		String option = "";

		if (args.length >= 1) {
			option = args[0];
		}

		sender.sendMessage("=====================================================");
		sender.spigot().sendMessage(createComponent("According to OTG, you are in the ", ChatColor.GOLD),
				createComponent(config.getName(), ChatColor.GREEN), createComponent(" biome.", ChatColor.GOLD));
		sender.spigot().sendMessage(
				createComponent("Biome registry name: ", MCBiome, ChatColor.GOLD, ChatColor.GREEN).create());

		switch (option) {
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

	private void showBiomeInfo(Player sender, Biome biome, IBiomeConfig config) {

		sender.spigot().sendMessage(createComponent("Biome Category: ", biome.getBiomeCategory().toString(),
				ChatColor.GOLD, ChatColor.GREEN).create());
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

		sender.spigot()
				.sendMessage(createComponent("Base Temperature: ", String.format("%.2f", biome.getBaseTemperature()),
						ChatColor.GOLD, ChatColor.GREEN)
								.append(createComponent(" Current Temperature: ",
										String.format("%.2f",
												biome.getTemperature(new BlockPos(sender.getLocation().getX(),
														sender.getLocation().getY(), sender.getLocation().getZ()))),
										ChatColor.GOLD, ChatColor.GREEN).create())
								.create());
	}

	private void showBiomeMobs(Player sender, Biome biome, IBiomeConfig config) {
		sender.spigot().sendMessage(createComponent("Spawns:", ChatColor.GOLD));
		sender.spigot().sendMessage(createComponent("  Monsters:", ChatColor.GOLD));
		showSpawns(sender, biome.getMobSettings().getMobs(MobCategory.MONSTER));
		sender.spigot().sendMessage(createComponent("  Creatures:", ChatColor.GOLD));
		showSpawns(sender, biome.getMobSettings().getMobs(MobCategory.CREATURE));
		sender.spigot().sendMessage(createComponent("  Water Creatures:", ChatColor.GOLD));
		showSpawns(sender, biome.getMobSettings().getMobs(MobCategory.CREATURE));
		sender.spigot().sendMessage(createComponent("  Ambient Creatures:", ChatColor.GOLD));
		showSpawns(sender, biome.getMobSettings().getMobs(MobCategory.AMBIENT));
		sender.spigot().sendMessage(createComponent("  Water Ambient:", ChatColor.GOLD));
		showSpawns(sender, biome.getMobSettings().getMobs(MobCategory.WATER_AMBIENT));
		sender.spigot().sendMessage(createComponent("  Misc:", ChatColor.GOLD));
		showSpawns(sender, biome.getMobSettings().getMobs(MobCategory.MISC));

	}

	public void showSpawns(Player sender, WeightedRandomList<SpawnerData> list) {
		list.unwrap().forEach(spawn -> {
			sender.spigot()
					.sendMessage(createComponent("   - Entity: ", Registry.ENTITY_TYPE.getKey(spawn.type).toString(),
							ChatColor.GOLD, ChatColor.GREEN)
									.append(createComponent(", Weight: ", Integer.toString(spawn.getWeight().asInt()),
											ChatColor.GOLD, ChatColor.GREEN).create())
									.append(createComponent(", Min: ", Integer.toString(spawn.minCount), ChatColor.GOLD,
											ChatColor.GREEN).create())
									.append(createComponent(", Max: ", Integer.toString(spawn.maxCount), ChatColor.GOLD,
											ChatColor.GREEN).create())
									.create());
		});
	}
}
