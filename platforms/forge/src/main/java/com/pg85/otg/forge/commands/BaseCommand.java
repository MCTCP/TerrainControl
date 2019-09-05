package com.pg85.otg.forge.commands;

import java.util.List;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.forge.ForgeEngine;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public abstract class BaseCommand {
	public static final TextFormatting ERROR_COLOR = TextFormatting.RED;
	public static final TextFormatting MESSAGE_COLOR = TextFormatting.GREEN;
	public static final TextFormatting VALUE_COLOR = TextFormatting.DARK_GREEN;

	String name;
	String description;
	String usage;
	public boolean needsOp = true;

	public abstract boolean onCommand(ICommandSender sender, List<String> args);

	/**
	 * Gets the {@link LocalWorld} the sender has provided. If the sender provided
	 * an empty string, the current world of the sender is returned. This may be
	 * null if the sender is not in a world loaded by Open Terrain Generator. If the
	 * sender provided a non-empty string, but Open Terrain Generator has no world
	 * loaded with that name, null will be returned.
	 * 
	 * @param sender    The sender.
	 * @param worldName The world name the sender provided. May be empty.
	 * @return The world, or null if not found.
	 */
	protected LocalWorld getWorld(ICommandSender sender, String worldName) {
		if (worldName.isEmpty()) {
			World mcWorld = sender.getEntityWorld();
			return ((ForgeEngine) OTG.getEngine()).getWorld(mcWorld);
		}

		return OTG.getWorld(worldName);
	}

	/**
	 * If the sender has a location (it is a player or a command block), this method
	 * returns their location. If not, this method returns null.
	 * 
	 * @param sender The sender.
	 * @return The location, or null if not found.
	 */
	protected BlockPos getLocation(ICommandSender sender) {
		return sender.getPosition();
	}

	protected void listMessage(ICommandSender sender, List<String> lines, int page, String... headers) {
		int pageCount = (lines.size() >> 3) + 1;
		if (page > pageCount) {
			page = pageCount;
		}

		sender.sendMessage(
				new TextComponentString(TextFormatting.AQUA + headers[0] + " - page " + page + "/" + pageCount));
		for (int headerId = 1; headerId < headers.length; headerId++) {
			// Send all remaining headers
			sender.sendMessage(new TextComponentString(TextFormatting.AQUA + headers[headerId]));
		}

		page--;

		for (int i = page * 8; i < lines.size() && i < (page * 8 + 8); i++) {
			sender.sendMessage(new TextComponentString(TextFormatting.AQUA + lines.get(i)));
		}
	}
}

/*
 * protected void listMessage(CommandSender sender, List<String> lines, int
 * page, String... headers) { int pageCount = (lines.size() >> 3) + 1; if (page
 * > pageCount) { page = pageCount; }
 * 
 * sender.sendMessage(ChatColor.AQUA.toString() + headers[0] + " - page " + page
 * + "/" + pageCount); for (int headerId = 1; headerId < headers.length;
 * headerId++) { // Send all remaining headers sender.sendMessage(ChatColor.AQUA
 * + headers[headerId]); }
 * 
 * page--;
 * 
 * for (int i = page * 8; i < lines.size() && i < (page * 8 + 8); i++) {
 * sender.sendMessage(lines.get(i)); } }
 * 
 * public String getHelp() { String ret = "do that"; Permission permission =
 * Bukkit.getPluginManager().getPermission(perm); if (permission != null) {
 * String desc = permission.getDescription(); if (desc != null &&
 * desc.trim().length() > 0) { ret = desc.trim(); } } return ret; } }
 */