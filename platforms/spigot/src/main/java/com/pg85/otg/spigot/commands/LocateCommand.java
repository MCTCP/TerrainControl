package com.pg85.otg.spigot.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.pg85.otg.presets.Preset;
import com.pg85.otg.spigot.gen.OTGNoiseChunkGenerator;
import com.pg85.otg.util.biome.OTGBiomeResourceLocation;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_16_R3.BiomeBase;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.ChatMessage;
import net.minecraft.server.v1_16_R3.IRegistry;
import net.minecraft.server.v1_16_R3.MinecraftKey;
import net.minecraft.server.v1_16_R3.ResourceKey;
import net.minecraft.server.v1_16_R3.WorldServer;

public class LocateCommand implements BaseCommand
{
	private static final DynamicCommandExceptionType ERROR_BIOME_NOT_FOUND = new DynamicCommandExceptionType(
			object -> new ChatMessage("commands.locatebiome.notFound", object));

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

		if (args.length >= 1)
		{
			String biome = args[0];
			Preset preset = ((OTGNoiseChunkGenerator) world.getChunkProvider().getChunkGenerator()).getPreset();

			MinecraftKey key = new MinecraftKey(new OTGBiomeResourceLocation(preset.getPresetFolder(),
					preset.getShortPresetName(), preset.getMajorVersion(), biome).toResourceLocationString());

			BiomeBase biomeBase = (world.r().b(IRegistry.ay).a(ResourceKey.a(IRegistry.ay, key)));

			if (biomeBase == null)
			{
				sender.sendMessage("Invalid biome: " + biome + ".");
			}

			BlockPosition playerPos = new BlockPosition(player.getLocation().getBlockX(),
					player.getLocation().getBlockY(), player.getLocation().getBlockZ());
			BlockPosition pos = world.a(biomeBase, playerPos, 10000, 8);

			if (pos == null)
			{
				sender.sendMessage(ERROR_BIOME_NOT_FOUND.create(biomeBase).getLocalizedMessage());
				return true;
			} else
			{

				TextComponent coordComponent = new TextComponent("[" + pos.getX() + " ~ " + pos.getZ() + "] ");
				coordComponent.setClickEvent(
						new ClickEvent(Action.SUGGEST_COMMAND, "/tp @s " + pos.getX() + " ~ " + pos.getZ()));
				sender.spigot().sendMessage(new ComponentBuilder(new TextComponent("The nearest " + biome + " is at "))
						.append(coordComponent)
						.append(new TextComponent("(" + Integer.toString((int)Math.round(
								Math.sqrt(pos.distanceSquared(playerPos.getX(), playerPos.getY(), playerPos.getZ(), false))))
								+ " blocks away)"))
						.create());
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
			WorldServer serverWorld = ((CraftWorld) ((Player) sender).getWorld()).getHandle();
			if (serverWorld.getChunkProvider().getChunkGenerator() instanceof OTGNoiseChunkGenerator)
			{
				StringUtil.copyPartialMatches(args[1],
						((OTGNoiseChunkGenerator) serverWorld.getChunkProvider().getChunkGenerator()).getPreset()
								.getAllBiomeNames(),
						options);
			}
		}
		return options;
	}
}
