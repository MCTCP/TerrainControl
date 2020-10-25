package com.pg85.otg.bukkit.commands;

import com.pg85.otg.bukkit.OTGPerm;
import com.pg85.otg.bukkit.OTGPlugin;
import com.pg85.otg.bukkit.biomes.BukkitBiome;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.util.ChunkCoordinate;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class TPCommand extends BaseCommand
{
    TPCommand(OTGPlugin _plugin)
    {
        super(_plugin);
        name = "tp";
        perm = OTGPerm.CMD_TP.node;
        usage = "tp <biome/dimension name or id> [-p player]";
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args)
    {
        Location location = this.getLocation(sender);
        int playerX = location.getBlockX();
        int playerZ = location.getBlockZ();

        LocalWorld world = this.getWorld(sender, "");
        
        if (world == null)
        {
            sender.sendMessage(ERROR_COLOR + "Plugin is not enabled for this world.");
            return true;
        }
        
        Player player = null;
        if (args.contains("-p")) {
            String name = args.get(args.size()-1);
            player = sender.getServer().getPlayer(name);
            if (player == null)
            {
                sender.sendMessage(ERROR_COLOR + "Could not find player " + name);
                return true;
            }
        } else if (!(sender instanceof Player)) {
            sender.sendMessage(ERROR_COLOR + "Must be a player to send this command without player argument " + name);
            return true;
        }
        if(player == null)
        {
        	player = (Player) sender;
        }

    	String biomeName = "";
    	for(int i = 0; i < args.size(); i++)
    	{
    		// If there's a -p flag, then the final arg is a player name
    		if(args.get(i).equalsIgnoreCase("-p"))
    		{
    			break;
    		}
			biomeName += args.get(i) + " ";
    	}
    	biomeName = biomeName.trim();
    	if(biomeName.length() > 0)
    	{
    		int biomeId = -1;
    		try
    		{
    			biomeId = Integer.parseInt(biomeName.replace(" ", ""));
    		}
    		catch(NumberFormatException ex) { }
    		
			ChunkCoordinate playerChunk = ChunkCoordinate.fromBlockCoords(playerX, playerZ);    		
			Location playerLoc = player.getLocation();

    		int maxRadius = 500;

    		if(biomeId == -1)
    		{
    			BukkitBiome targetBiome = (BukkitBiome)world.getBiomeByNameOrNull(biomeName);
	    		if(targetBiome != null)
	    		{
	    			biomeId = targetBiome.getIds().getOTGBiomeId();
	    		}
    		}
    		
    		if(biomeId != -1)
    		{
    	        sender.sendMessage(MESSAGE_COLOR + "Searching for destination biome \"" + VALUE_COLOR + biomeName + MESSAGE_COLOR + "\".");
    			
        		for(int cycle = 1; cycle < maxRadius; cycle++)
        		{
            		for(int x1 = playerX - cycle; x1 <= playerX + cycle; x1++)
            		{
            			for(int z1 = playerZ - cycle; z1 <= playerZ + cycle; z1++)
            			{
        					if(x1 == playerX - cycle || x1 == playerX + cycle || z1 == playerZ - cycle || z1 == playerZ + cycle)
        					{
        						ChunkCoordinate chunkCoord = ChunkCoordinate.fromChunkCoords(playerChunk.getChunkX() + (x1 - playerX), playerChunk.getChunkZ() + (z1 - playerZ));	        	
        						
        						BukkitBiome biome = (BukkitBiome)world.getBiome(chunkCoord.getBlockXCenter(), chunkCoord.getBlockZCenter());

        						if(
    								biome != null &&
									biome.getIds().getOTGBiomeId() == biomeId
								)
        						{
        							Location loc = new Location(playerLoc.getWorld(), (double)chunkCoord.getBlockXCenter(), (double)world.getHighestBlockAboveYAt(chunkCoord.getBlockXCenter(), chunkCoord.getBlockZCenter(), null), (double)chunkCoord.getBlockZCenter());
        							sender.sendMessage("Teleporting to \"" + biomeName + "\".");
        							player.teleport(loc);
        							return true;
        						}
        					}
            			}
            		}
        		}
    		}
			sender.sendMessage(ERROR_COLOR + "Could not find biome \"" + biomeName + "\".");
    	}

        return true;
    }
}