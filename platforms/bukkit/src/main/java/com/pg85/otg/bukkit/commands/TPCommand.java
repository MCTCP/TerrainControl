package com.pg85.otg.bukkit.commands;

import com.pg85.otg.bukkit.BukkitBiome;
import com.pg85.otg.bukkit.OTGPerm;
import com.pg85.otg.bukkit.OTGPlugin;
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
        usage = "tp <biome name or id>";
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
        		for(int cycle = 1; cycle < maxRadius; cycle++)
        		{
            		for(int x1 = playerX - cycle; x1 <= playerX + cycle; x1++)
            		{
        				if(x1 == playerX - cycle || x1 == playerX + cycle)
        				{
                			for(int z1 = playerZ - cycle; z1 <= playerZ + cycle; z1++)
                			{
            					if(z1 == playerZ - cycle || z1 == playerZ + cycle)
            					{
            						ChunkCoordinate chunkCoord = ChunkCoordinate.fromChunkCoords(playerChunk.getChunkX() + (x1 - playerX), playerChunk.getChunkZ() + (z1 - playerZ));	        	
            						
            						BukkitBiome biome = (BukkitBiome)world.getBiome(chunkCoord.getBlockXCenter(), chunkCoord.getBlockZCenter());

            						if(
        								biome != null &&
										biome.getIds().getOTGBiomeId() == biomeId        	        	    										
    								)
            						{
            							Location loc = new Location(playerLoc.getWorld(), (double)chunkCoord.getBlockXCenter(), (double)world.getHighestBlockYAt(chunkCoord.getBlockXCenter(), chunkCoord.getBlockZCenter()), (double)chunkCoord.getBlockZCenter());
            							sender.sendMessage("Teleporting to \"" + biomeName + "\".");
            							player.teleport(loc);
            							return true;
            						}
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