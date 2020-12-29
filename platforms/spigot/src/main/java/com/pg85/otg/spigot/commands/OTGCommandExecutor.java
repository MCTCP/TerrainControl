package com.pg85.otg.spigot.commands;

import com.pg85.otg.spigot.biome.OTGBiomeProvider;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OTGCommandExecutor implements TabCompleter, CommandExecutor
{
	public static List<String> COMMANDS = new ArrayList<>(Arrays.asList("data", "map", "help"));

	@Override
	public boolean onCommand (CommandSender sender, Command command, String s, String[] strings)
	{
		String cmd;
		if (strings.length >= 1)
			cmd = strings[0].toLowerCase();
		else
			cmd = "help";
		switch (cmd)
		{
			case "data":
				return DataCommand.execute(sender, strings);
			case "map":
				return mapBiomes(sender, strings);
			case "help":
			default:
				return helpMessage(sender);
		}
	}

	private boolean mapBiomes (CommandSender sender, String[] args)
	{
		CraftWorld world;
		Player player;
		int size = 200;
		int offsetX = 0;
		int offsetY = 0;
		String name = "";
		for (int i = 1; i < args.length-1; i++)
		{
			if (args[i].equalsIgnoreCase("-s"))
				size = Integer.parseInt(args[i+1]);
			if (args[i].equalsIgnoreCase("-ox"))
				offsetX = Integer.parseInt(args[i+1]);
			if (args[i].equalsIgnoreCase("-oy"))
				offsetY = Integer.parseInt(args[i+1]);
			if (args[i].equalsIgnoreCase("-n"))
				name = args[i+1];
		}
		if (sender instanceof Player)
		{
			player = (Player) sender;
			world = (CraftWorld) player.getWorld();
			if (offsetX == 0) offsetX += player.getLocation().getBlockX();
			if (offsetY == 0) offsetY += player.getLocation().getBlockY();
		}
		else {
			sender.sendMessage("Only in-game for now");
			return true;
		}
		if (!(world.getHandle().getChunkProvider().chunkGenerator.getWorldChunkManager() instanceof OTGBiomeProvider))
		{
			sender.sendMessage("This is not an OTG world");
			return true;
		}

		OTGBiomeProvider provider = (OTGBiomeProvider) world.getHandle().getChunkProvider().chunkGenerator.getWorldChunkManager();

		BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);

		int progressUpdate = img.getHeight() / 8;

		for (int x = 0; x < img.getHeight(); x++)
		{
			for (int z = 0; z < img.getWidth(); z++)
			{
				img.setRGB(x, z, provider.configLookup[provider.getSampler().sample(x, z)].getBiomeColor());
			}
			if (x % progressUpdate == 0)
			{
				sender.sendMessage((((double) x / img.getHeight()) * 100) + "% Done mapping");
			}
		}

		String fileName = player.getWorld().getName()+" "+name+" biomes.png";
		sender.sendMessage("Finished mapping! The resulting image is located at " + fileName + ".");
		Path p = Paths.get(fileName);
		try
		{
			ImageIO.write(img, "png", p.toAbsolutePath().toFile());
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return true;
	}

	private boolean helpMessage (CommandSender sender)
	{
		sender.sendMessage("OTG Help");
		sender.sendMessage("/otg map -> Creates a 2048 x 2048 biome map of the world.");
		return true;
	}

	@Override
	public List<String> onTabComplete (CommandSender sender, Command command, String s, String[] strings)
	{

		if (strings.length == 0)
			return COMMANDS;
		if (strings.length == 1)
			return StringUtil.copyPartialMatches(strings[0], COMMANDS, new ArrayList<>());
		switch (strings[0])
		{
			case "data":
				return DataCommand.tabComplete(strings);
			case "map":
			case "help":
				return new ArrayList<>();
		}
		return new ArrayList<>();
	}
}
