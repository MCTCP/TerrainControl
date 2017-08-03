package com.khorn.terraincontrol.bukkit.commands;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.bukkit.TCPerm;
import com.khorn.terraincontrol.bukkit.TXPlugin;
import com.khorn.terraincontrol.util.ChunkCoordinate;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class TPCommand extends BaseCommand
{
    public TPCommand(TXPlugin _plugin)
    {
        super(_plugin);
        name = "tp";
        perm = TCPerm.CMD_TP.node;
        usage = "tp <biome name or id>";
        workOnConsole = false;
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

    	String biomeName = "";
    	for(int i = 0; i < args.size(); i++)
    	{
    		biomeName += args.get(i);
    	}
    	if(biomeName != null && biomeName.length() > 0)
    	{
    		int biomeId = -1;
    		try
    		{
    			biomeId = Integer.parseInt(biomeName.replace(" ", ""));
    		}
    		catch(NumberFormatException ex) { }
    		
			ChunkCoordinate playerChunk = ChunkCoordinate.fromBlockCoords(playerX, playerZ);
    		
			Player player = (Player) sender;
			Location playerLoc = player.getLocation();
			
    		int maxRadius = 1000;
    		for(int cycle = 1; cycle < maxRadius; cycle++)
    		{
        		for(int x1 = playerX - cycle; x1 <= playerX + cycle; x1++)            			
        		{             
        			for(int z1 = playerZ - cycle; z1 <= playerZ + cycle; z1++)
        			{
        				if(x1 == playerX - cycle || x1 == playerX + cycle)
        				{
        					if(z1 == playerZ - cycle || z1 == playerZ + cycle)
        					{       						
        						ChunkCoordinate chunkCoord = ChunkCoordinate.fromChunkCoords(playerChunk.getChunkX() + (x1 - playerX), playerChunk.getChunkZ() + (z1 - playerZ));
        						
        						LocalBiome biome = world.getBiome(chunkCoord.getBlockXCenter(), chunkCoord.getBlockZCenter());
        						if(
    								biome != null &&
    								(
	    								(
											biomeId == -1 &&
											biome.getName().toLowerCase().replace(" ", "").equals(biomeName.toLowerCase().replace(" ", ""))
										) || (
											biomeId != -1 &&
											biome.getIds().getGenerationId() == biomeId
										)
									)
								)
        						{
        							Location loc = new Location(playerLoc.getWorld(), (double)chunkCoord.getBlockXCenter(), (double)world.getHighestBlockYAt(chunkCoord.getBlockXCenter(), chunkCoord.getBlockZCenter()), (double)chunkCoord.getBlockZCenter());
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