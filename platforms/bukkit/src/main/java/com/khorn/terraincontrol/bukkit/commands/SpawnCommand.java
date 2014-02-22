package com.khorn.terraincontrol.bukkit.commands;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.bukkit.TCPerm;
import com.khorn.terraincontrol.bukkit.TCPlugin;
import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.util.Rotation;
import org.bukkit.Material;
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
        usage = "spawn Name [World]";
        workOnConsole = false;
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args)
    {
        Player me = (Player) sender;
        Random random = new Random();

        LocalWorld bukkitWorld = this.getWorld(me, args.size() > 1 ? args.get(1) : "");

        if (args.isEmpty())
        {
            me.sendMessage(ERROR_COLOR + "You must enter the name of the BO2.");
            return true;
        }
        CustomObject spawnObject = null;

        if (bukkitWorld != null)
            spawnObject = TerrainControl.getCustomObjectManager().getObjectFromString(args.get(0), bukkitWorld);

        if (spawnObject == null)
        {
            sender.sendMessage(ERROR_COLOR + "Object not found, use '/tc list' to list the available ones.");
            return true;
        }

        Block block = this.getWatchedBlock(me, true);
        if (block == null)
            return true;
        
        if (spawnObject.spawnForced(bukkitWorld, random, Rotation.NORTH, block.getX(), block.getY(), block.getZ()))
        {
            me.sendMessage(BaseCommand.MESSAGE_COLOR + spawnObject.getName() + " was spawned.");
        } else
        {
            me.sendMessage(BaseCommand.ERROR_COLOR + "Object can't be spawned over there.");
        }

        return true;
    }

    public Block getWatchedBlock(Player me, boolean verbose)
    {
        if (me == null)
            return null;

        Block block;
        Block previousBlock = null;

        Iterator<Block> itr = new BlockIterator(me, 200);
        while (itr.hasNext())
        {
            block = itr.next();
            if (block.getType() != Material.AIR && block.getType() != Material.LONG_GRASS)
            {
                return previousBlock;
            }
            previousBlock = block;
        }

        if (verbose)
        {
            me.sendMessage(ERROR_COLOR + "No block in sight.");
        }

        return null;
    }
}