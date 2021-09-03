package com.pg85.otg.paper.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import com.pg85.otg.customobject.util.Corner;

import net.minecraft.core.BlockPos;

public class RegionCommand extends BaseCommand
{
	protected static final HashMap<Player, Region> playerSelectionMap = new HashMap<>();

	public RegionCommand()
	{
		super("region");
		this.helpMessage = "Allows for setting and modifying regions";
		this.usage = "Please see /otg help region.";
	}

	private static final List<String> directions = Arrays.asList("down", "east", "north", "south", "up", "west");
	private static final List<String> regionSubCommands = Arrays.asList("clear","expand", "mark", "shrink");
	private static final List<String> points = Arrays.asList("pos1","pos2", "center");

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] strings)
	{
		if (strings.length == 2)
		{
			return StringUtil.copyPartialMatches(strings[1], regionSubCommands, new ArrayList<>());
		}

		if (strings[1].equalsIgnoreCase("expand") || strings[1].equalsIgnoreCase("shrink")) {
			if (strings.length == 3)
			{
				return StringUtil.copyPartialMatches(strings[2], directions, new ArrayList<>());
			}
		}

		if (strings[1].equalsIgnoreCase("mark"))
		{
			return StringUtil.copyPartialMatches(strings[2], points, new ArrayList<>());
		}

		return new ArrayList<>();
	}

	@Override
	public boolean execute(CommandSender source, String[] args)
	{
		if (!(source instanceof Player))
		{
			source.sendMessage("Only players can execute this command");
			return false;
		}
		Player player = ((Player) source);
		if (!playerSelectionMap.containsKey(player))
		{
			playerSelectionMap.put(player, new Region());
		}

		if (args.length == 0)
		{
			source.sendMessage("placeholder help message");
			return true;
		}
		
		Region region = playerSelectionMap.get(player);

		switch (args[0])
		{
			case "mark":
				if (args.length > 1)
				{
					mark(player, args[1]);
				}
				else if (region.setPos(player.getLocation()))
				{
					source.sendMessage("Point 1 marked");
				} else {
					source.sendMessage("Point 2 marked");
				}
				return true;
			case "clear":
				region.clear();
				player.sendMessage("Position cleared");
				return true;
			case "shrink":
			case "expand":
				if (region.getMax() == null) {
					source.sendMessage("Please mark two positions before modifying or exporting the region");return true; }
				if (args.length < 3) {
					source.sendMessage("Please specify a direction and an amount to expand by"); return true;}
				String direction = args[1];
				int value = Integer.parseInt(args[2]);
				if (args[0].equalsIgnoreCase("shrink")) value = -value;
				expand(player, direction, value);
				return true;
			default:
				return false;
		}
	}

	public void mark(Player source, String input)
	{
		Region r = playerSelectionMap.get(source);
		switch(input)
		{
			case "min":
			case "pos1":
			case "1":
			{
				r.setPos1(source.getLocation());
				source.sendMessage("Point 1 marked");
				return;
			}
			case "max":
			case "pos2":
			case "2":
			{
				r.setPos2(source.getLocation());
				source.sendMessage("Point 2 marked");
				return;
			}
			case "center":
			{
				r.setCenter(Region.cornerFromLocation(source.getLocation()));
				source.sendMessage("Center marked");
				return;
			}
			default:
			{
				source.sendMessage(input + " is not recognized");
			}
		}
	}

	public void expand(Player source, String direction, Integer value)
	{
		Region region = playerSelectionMap.get(source);
		if (region.getMax() == null)
		{
			source.sendMessage("Please mark two positions before modifying or exporting the region");
			return;
		}

		switch (direction)
		{
			case "south": // positive Z
				if (region.pos2.getZ() >= region.pos1.getZ())
					region.setPos2(region.pos2.south(value));
				else
					region.setPos1(region.pos1.south(value));
				break;
			case "north": // negative Z
				if (region.pos2.getZ() < region.pos1.getZ())
					region.setPos2(region.pos2.north(value));
				else
					region.setPos1(region.pos1.north(value));
				break;
			case "east": // positive X
				if (region.pos2.getX() >= region.pos1.getX())
					region.setPos2(region.pos2.east(value));
				else
					region.setPos1(region.pos1.east(value));
				break;
			case "west": // negative X
				if (region.pos2.getX() < region.pos1.getX())
					region.setPos2(region.pos2.west(value));
				else
					region.setPos1(region.pos1.west(value));
				break;
			case "up": // positive y
				if (region.pos2.getY() >= region.pos1.getY())
					region.setPos2(region.pos2.above(value));
				else
					region.setPos1(region.pos1.above(value));
				break;
			case "down": // negative y
				if (region.pos2.getY() < region.pos1.getY())
					region.setPos2(region.pos2.below(value));
				else
					region.setPos1(region.pos1.below(value));
				break;
			default:
				source.sendMessage("Unrecognized direction " + direction);
				return;
		}

		source.sendMessage("Region modified");
	}
	
	public static class Region
	{
		private BlockPos pos1 = null;
		private BlockPos pos2 = null;
		private Corner center = null;
		private boolean flip = true;

		public boolean setPos(Location loc)
		{
			return setPos(new BlockPos(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
		}
		
		// Returns whether it set pos1 or not
		public boolean setPos(BlockPos BlockPos)
		{
			// alternate between setting min and max
			// Flip initializes as true, meaning we set min first
			if (flip)
				pos1 = BlockPos;
			else
				pos2 = BlockPos;
			flip = !flip;
			return !flip;
		}

		public static Corner cornerFromLocation(Location loc)
		{
			return new Corner(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
		}

		public void clear()
		{
			pos1 = null;
			pos2 = null;
			center = null;
		}

		public Corner getMin()
		{
			if (pos1 == null || pos2 == null) {
				return null;
			}
			return new Corner(
				Math.min(pos1.getX(), pos2.getX()),
				Math.min(pos1.getY(), pos2.getY()),
				Math.min(pos1.getZ(), pos2.getZ())
			);
		}

		public Corner getMax()
		{
			if (pos1 == null || pos2 == null) {
				return null;
			}
			return new Corner(
				Math.max(pos1.getX(), pos2.getX()),
				Math.max(pos1.getY(), pos2.getY()),
				Math.max(pos1.getZ(), pos2.getZ())
			);
		}

		public void setPos1(Location loc)
		{
			setPos1(new BlockPos(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
		}

		public void setPos1(BlockPos pos)
		{
			flip = false;
			this.pos1 = pos;
		}

		public void setPos2(Location loc)
		{
			setPos2(new BlockPos(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
		}

		public void setPos2(BlockPos pos)
		{
			flip = true;
			this.pos2 = pos;
		}

		public Corner getCenter()
		{
			return center;
		}

		public void setCenter(Corner center)
		{
			this.center = center;
		}
	}
}
