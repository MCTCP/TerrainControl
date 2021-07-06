package com.pg85.otg.spigot.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.pg85.otg.OTG;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.interfaces.ICachedBiomeProvider;
import com.pg85.otg.spigot.biome.OTGBiomeProvider;
import com.pg85.otg.spigot.gen.OTGNoiseChunkGenerator;
import com.pg85.otg.spigot.gen.OTGSpigotChunkGen;
import com.pg85.otg.spigot.gen.SpigotChunkBuffer;
import com.pg85.otg.spigot.materials.SpigotMaterialData;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;
import com.pg85.otg.util.materials.LocalMaterials;

import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.Blocks;
import net.minecraft.server.v1_16_R3.IBlockData;

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
	public static List<String> COMMANDS = new ArrayList<>(Arrays.asList("data", "mapbiomes","mapterrain", "help", "spawn", "export", "edit", "finishedit", "region", "flush", "structure"));

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
			case "mapbiomes":
				return mapBiomes(sender, strings);
			case "mapterrain":
				return mapTerrain(sender, strings);
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
			case "structure":
				return StructureCommand.execute(sender, args);
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
		} else {
			sender.sendMessage("Only in-game for now");
			return true;
		}
		if (!(world.getHandle().getChunkProvider().chunkGenerator instanceof OTGNoiseChunkGenerator))
		{
			sender.sendMessage("This is not an OTG world");
			return true;
		}

		ICachedBiomeProvider provider = ((OTGNoiseChunkGenerator)world.getHandle().getChunkProvider().chunkGenerator).getCachedBiomeProvider();

		BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);

		int progressUpdate = img.getHeight() / 8;

		for (int noiseX = 0; noiseX < img.getHeight(); noiseX++)
		{
			for (int noiseZ = 0; noiseZ < img.getWidth(); noiseZ++)
			{
				// TODO: Fetch biome data per chunk, not per column, probably very slow atm.
				// TODO: Forge doesn't use an offset, always starts at 0,0?
				img.setRGB(noiseX, noiseZ, provider.getNoiseBiomeConfig(noiseX + offsetX, noiseZ + offsetZ, true).getBiomeColor());
			}
			if (noiseX % progressUpdate == 0)
			{
				sender.sendMessage((((double) noiseX / img.getHeight()) * 100) + "% Done mapping");
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
	
	private boolean mapTerrain (CommandSender sender, String[] args)
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
		} else {
			sender.sendMessage("Only in-game for now");
			return true;
		}
		if (!(world.getHandle().getChunkProvider().chunkGenerator.getWorldChunkManager() instanceof OTGBiomeProvider))
		{
			sender.sendMessage("This is not an OTG world");
			return true;
		}
	
		BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
		int progressUpdate = img.getHeight() / 8;
		HighestBlockInfo highestBlockInfo;
		int min = 0;
		int max = 255;
		int range = max - min;
		int distance;
		float relativeDistance;
		int shadePercentage;
		int rgbColor;
		int progress = 0;
		for (int chunkX = 0; chunkX < (int)Math.ceil(img.getWidth() / 16f); chunkX++)
		{
			for (int chunkZ = 0; chunkZ < (int)Math.ceil(img.getHeight() / 16f); chunkZ++)
			{
				SpigotChunkBuffer chunk = ((OTGSpigotChunkGen)world.getHandle().generator).generator.getChunkWithoutLoadingOrCaching(world.getHandle().getRandom(), ChunkCoordinate.fromChunkCoords(chunkX, chunkZ));
				for(int internalX = 0; internalX < Constants.CHUNK_SIZE; internalX++)
				{
					for(int internalZ = 0; internalZ < Constants.CHUNK_SIZE; internalZ++)
					{
						if(
							chunkX * Constants.CHUNK_SIZE + internalX < img.getWidth() &&
							chunkZ * Constants.CHUNK_SIZE + internalZ < img.getHeight()
						)
						{
							highestBlockInfo = getHighestBlockInfoInUnloadedChunk(chunk, internalX, internalZ);
			
							// Color depth relative to waterlevel
							//int worldHeight = 255;
							//int worldWaterLevel = 63;
							//int min = worldWaterLevel - worldHeight;
							//int max = worldWaterLevel + worldHeight;
							// Color depth relative to 0-255						
							distance = -min + highestBlockInfo.y;
							relativeDistance = (float)distance / (float)range;
							shadePercentage = (int)Math.floor(relativeDistance * 2 * 100);
							rgbColor = shadeColor(highestBlockInfo.material.internalBlock().getBlock().s().rgb, shadePercentage);
							img.setRGB(chunkX * Constants.CHUNK_SIZE + internalX, chunkZ * Constants.CHUNK_SIZE + internalZ, rgbColor);						
						}
					}
				}
				progress++;
				if (progress % progressUpdate == 0)
				{
					sender.sendMessage((((double) chunkX / (int)Math.ceil(img.getWidth() / 16f)) * 100) + "% Done mapping");
				}				
			}
		}

		String fileName = player.getWorld().getName()+" " + name + " terrain.png";
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

	private static int shadeColor(int rgbColor, int percent)
	{
		int red = (rgbColor >> 16) & 0xFF;
		int green = (rgbColor >> 8) & 0xFF;
		int blue = rgbColor & 0xFF;
		
		red = red * percent / 100;
		red = red > 255 ? 255 : red;
		green = green * percent / 100;
		green = green > 255 ? 255 : green;
		blue = blue * percent / 100;
		blue = blue > 255 ? 255 : blue;

		return 65536 * red + 256 * green + blue;
	}

	private HighestBlockInfo getHighestBlockInfoInUnloadedChunk(SpigotChunkBuffer chunk, int internalX, int internalZ)
	{
		// TODO: Just use heightmaps?
		IBlockData blockInChunk;
		for (int y = chunk.getHighestBlockForColumn(internalX, internalZ); y >= 0; y--)
		{
			blockInChunk = chunk.getChunk().getType(new BlockPosition(internalX, y, internalZ));
			if (blockInChunk != null && blockInChunk.getBlock() != Blocks.AIR)
			{
				return new HighestBlockInfo((SpigotMaterialData)SpigotMaterialData.ofBlockData(blockInChunk), y);					
			}
		}
		return new HighestBlockInfo((SpigotMaterialData)LocalMaterials.AIR, 63);
	}
	
	public class HighestBlockInfo
	{
		public final SpigotMaterialData material;
		public final int y;
		
		public HighestBlockInfo(SpigotMaterialData material, int y)
		{
			this.material = material;
			this.y = y;
		}
	}	

	private boolean helpMessage (CommandSender sender)
	{
		// TODO: Update this
		sender.sendMessage("OTG Help");
		sender.sendMessage("/otg map -> Creates a 2048 x 2048 biome map of the world.");
		sender.sendMessage("/otg spawn <preset name> : <object name> : <coords> (BO3 only) or <force> (BO4 only)");
		sender.sendMessage("/otg data <dataType>");
		sender.sendMessage("/otg structure");
		sender.sendMessage("/otg flush");
		sender.sendMessage("/otg edit");
		sender.sendMessage("/otg export");
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
			case "flush":
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
			OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.MAIN, String.format("Command syntax error: ", (Object[])e.getStackTrace()));
		}

		return argsMap;
	}
}
