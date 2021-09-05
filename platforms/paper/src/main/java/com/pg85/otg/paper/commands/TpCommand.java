package com.pg85.otg.paper.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.pg85.otg.paper.gen.OTGNoiseChunkGenerator;
import com.pg85.otg.presets.Preset;
import com.pg85.otg.util.biome.OTGBiomeResourceLocation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap.Types;

public class TpCommand extends BaseCommand
{
	private static final DynamicCommandExceptionType ERROR_BIOME_NOT_FOUND = new DynamicCommandExceptionType(
			object -> new TranslatableComponent("commands.locatebiome.notFound", object));

	public TpCommand()
	{
		super("tp");
		this.helpMessage = "Teleports you to a specific OTG biome.";
		this.usage = "/otg tp <biome> [range]";
		this.detailedHelp = new String[]
		{ "<biome>: The name of the biome to teleport to.",
				"[range]: The radius in blocks to search for the target biome, defaults to 10000.",
				"Note: large numbers will make the command take a long time." };
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
		ServerLevel world = ((CraftWorld) player.getWorld()).getHandle();

		if (!(world.getChunkSource().getGenerator() instanceof OTGNoiseChunkGenerator))
		{
			sender.sendMessage("OTG is not enabled in this world");
			return true;
		}

		if (args.length >= 1)
		{
			String biome = args[0];
			Preset preset = ((OTGNoiseChunkGenerator) world.getChunkSource().getGenerator()).getPreset();

			ResourceLocation key = new ResourceLocation(new OTGBiomeResourceLocation(preset.getPresetFolder(),
					preset.getShortPresetName(), preset.getMajorVersion(), biome).toResourceLocationString());

			Biome biomeBase = (world.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).get(ResourceKey.create(Registry.BIOME_REGISTRY, key)));

			if (biomeBase == null)
			{
				sender.sendMessage("Invalid biome: " + biome + ".");
				return true;
			}
			
			int range = 10000;
			
			if (args.length >= 2) {
				try {
					range = Integer.parseInt(args[1]);
				} catch (NumberFormatException ex) {
					sender.sendMessage("Invalid search radius: " + (args[1] + "."));
					return true;
				}
			}

			BlockPos playerPos = new BlockPos(player.getLocation().getBlockX(),
					player.getLocation().getBlockY(), player.getLocation().getBlockZ());
			BlockPos pos = world.findNearestBiome(biomeBase, playerPos, range, 8);

			if (pos == null)
			{
				sender.sendMessage(ERROR_BIOME_NOT_FOUND.create(biome).getLocalizedMessage());
				return true;
			} else
			{
				int y = world.getChunkSource().getGenerator().getBaseHeight(
					pos.getX(),
					pos.getZ(),
					Types.MOTION_BLOCKING_NO_LEAVES,
					world);
				player.teleport(new Location(player.getWorld(), pos.getX(), y, pos.getZ()));
				player.sendMessage("Teleporting you to the nearest " + biome + ".");
			}

		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args)
	{
		List<String> options = new ArrayList<>();
		if (args.length == 2 && sender instanceof Player)
		{
			ServerLevel serverWorld = ((CraftWorld) ((Player) sender).getWorld()).getHandle();
			if (serverWorld.getChunkSource().getGenerator() instanceof OTGNoiseChunkGenerator)
			{
				List<String> biomeNames = new ArrayList<>();
				for (String name : ((OTGNoiseChunkGenerator) serverWorld.getChunkSource().getGenerator())
						.getPreset().getAllBiomeNames())
				{
					biomeNames.add(name.replace(' ', '_'));
				}
				StringUtil.copyPartialMatches(args[1], biomeNames, options);
			}
		}
		return options;
	}
}
