package com.pg85.otg.spigot.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.pg85.otg.OTG;
import com.pg85.otg.logging.LogMarker;
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
import java.util.HashMap;
import java.util.List;

public class OTGCommandExecutor implements TabCompleter, CommandExecutor
{
	public static List<String> COMMANDS = new ArrayList<>(Arrays.asList("data", "map", "help", "spawn", "export", "edit", "finishedit", "region"));

	@Override
	public boolean onCommand (CommandSender sender, Command command, String s, String[] strings)
	{
		String cmd;
		String[] args = strings.length >= 2 ? Arrays.copyOfRange(strings, 1, strings.length) : new String[0];

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
			case "spawn":
				return SpawnCommand.execute(sender, parseArgs(strings));
			case "export":
				return ExportCommand.execute(sender, args);
			case "edit":
				return EditCommand.execute(sender, args);
			case "finishedit":
				return EditCommand.finish(sender);
			case "region":
				return ExportCommand.region(sender, args);
			case "flush":
				return FlushCommand.execute(sender, args);				
			case "help":
			default:
				return helpMessage(sender);
		}
	}

	private boolean mapBiomes (CommandSender sender, String[] args)
	{
		CraftWorld world;
		Player player;
		int size = 2048;
		int offsetX = 0;
		int offsetZ = 0;
		String name = "";
		for (int i = 1; i < args.length-1; i++)
		{
			if (args[i].equalsIgnoreCase("-s"))
				size = Integer.parseInt(args[i+1]);
			if (args[i].equalsIgnoreCase("-ox"))
				offsetX = Integer.parseInt(args[i+1]);
			if (args[i].equalsIgnoreCase("-oz"))
				offsetZ = Integer.parseInt(args[i+1]);
			if (args[i].equalsIgnoreCase("-n"))
				name = args[i+1];
		}
		if (sender instanceof Player)
		{
			player = (Player) sender;
			world = (CraftWorld) player.getWorld();
			if (offsetX == 0 && offsetZ == 0)
			{
				offsetX += player.getLocation().getBlockX();
				offsetZ += player.getLocation().getBlockZ();
			}
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

		for (int x = offsetX; x < img.getHeight() + offsetX; x++)
		{
			for (int z = offsetZ; z < img.getWidth() + offsetZ; z++)
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
		sender.sendMessage("/otg spawn <preset name> : <object name>");
		sender.sendMessage("/otg data <dataType>");
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
			case "export":
				return ExportCommand.tabCompleteExport(parseArgs(strings));
			case "edit":
				return EditCommand.tabComplete(parseArgs(strings), true);
			case "region":
				return ExportCommand.tabCompleteRegion(strings);
			case "spawn":
				return EditCommand.tabComplete(parseArgs(strings), false);
			case "finishedit":
			case "map":
			case "help":
				return new ArrayList<>();
		}
		return new ArrayList<>();
	}

	/** This method takes a list of strings from spigot and turns it into a map of arguments
	 *  - Normal arguments are mapped to their index as a string, i.e. "1": "biome bundle"
	 *  - Flags with one - are mapped as key to an empty string, to signify they're set, i.e. "-o": ""
	 *  - Flags with two -- are mapped as key to the subsequent argument, i.e. "--file": "test.bo3"
	 *
	 * 	This method also reads quoted strings as one string, without the quotes
	 *
	 * @param strings The command args
	 * @return The args, mapped by index or flag
	 */
	private HashMap<String, String> parseArgs(String[] strings)
	{
		HashMap<String, String> argsMap = new HashMap<>();
		String input = String.join(" ", strings);
		long count = input.chars().filter(c -> c == '"').count();
		StringReader reader = new StringReader(input);
		String str;
		int index = 1;
		try
		{
			reader.readString(); // Remove sub-command
			while (reader.getCursor() < input.length())
			{
				if (reader.peek() == ' ') reader.skip();
				if (count % 2 == 1 && reader.peek() == '"')
				{ // Non-ended quote, gotta just get the remainder
					str = reader.getRemaining();
					reader.setCursor(reader.getTotalLength());
				} else {
					str = reader.readString();
				}

				if (str.matches("-[a-z0-9]+")) // if str is a single line flag
				{
					argsMap.put(str, "");
					continue;
				}

				if (str.matches("--[a-z0-9]+")) // if str is a double line flag, means it has a payload
				{
					if (reader.canRead())
					{
						argsMap.put(str, reader.readString());
					}
					continue;
				}

				argsMap.put((index++) + "", str);
			}
		}
		catch (CommandSyntaxException e)
		{
			OTG.log(LogMarker.ERROR, "Command syntax error");
			e.printStackTrace();
		}

		return argsMap;
	}
}
