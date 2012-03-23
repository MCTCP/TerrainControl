package com.khorn.terraincontrol.bukkit.commands;

import com.khorn.terraincontrol.bukkit.BukkitWorld;
import com.khorn.terraincontrol.bukkit.TCPerm;
import com.khorn.terraincontrol.bukkit.TCPlugin;
import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.customobjects.CustomObjectGen;

import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class SpawnCommand extends BaseCommand
{
    public SpawnCommand(TCPlugin _plugin)
    {
        super(_plugin);
        name = "spawn";
        perm = TCPerm.CMD_SPAWN.node;
        usage = "spawn BOBName";
        workOnConsole = false;
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args)
    {
        Player me = (Player) sender;
        
        BukkitWorld bukkitWorld = this.getWorld(me, "");
        if (bukkitWorld == null)
        {
            // TODO: When we use a centralized BO2 repo this check will not be needed and BO2's should be spawnable in any world.
            me.sendMessage(ErrorColor + "TerrainControl is not enabled for this world.");
            return true;
        }
        
        if (args.size() == 0)
        {
            me.sendMessage(ErrorColor + "You must enter the name of the BO2.");
            return true;
        }
        String bo2Name = args.get(0);
        
        CustomObject spawnObject = null;
        for (CustomObject object : bukkitWorld.getSettings().Objects)
        {
            if (object.name.equalsIgnoreCase(bo2Name))
            {
                spawnObject = object;
                break;
            }
        }
        
        if (spawnObject == null)
        {
            sender.sendMessage(ErrorColor + "BO2 not found, use '/tc list' to list the available ones.");
            return true;
        }
        
        Block block = this.getWatchedBlock(me, true);
        if (block == null) return true;
        
        if (CustomObjectGen.GenerateCustomObject(bukkitWorld, new Random(), bukkitWorld.getSettings(), block.getX(), block.getY(), block.getZ(), spawnObject, true))
        {
            me.sendMessage(BaseCommand.MessageColor + spawnObject.name + " was spawned.");
        }
        else
        {
            me.sendMessage(BaseCommand.ErrorColor + "BO2 cant be spawned over there.");
        }

        return true;
    }
    
    public Block getWatchedBlock(Player me, boolean verboose)
    {
        if (me == null) return null;
        
        Block block = null;
        
        Iterator<Block> itr = new BlockIterator(me, 200);
        while (itr.hasNext())
        {
            block = itr.next();
            if (block.getTypeId() != 0)
            {
                return block;
            }
        }
        
        if (verboose)
        {
            me.sendMessage(ErrorColor.toString()+"No block in sight.");
        }
        
        return null;
    }
}