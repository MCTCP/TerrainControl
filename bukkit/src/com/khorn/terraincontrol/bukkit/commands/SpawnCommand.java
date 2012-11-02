package com.khorn.terraincontrol.bukkit.commands;

import com.khorn.terraincontrol.bukkit.BukkitWorld;
import com.khorn.terraincontrol.bukkit.TCPerm;
import com.khorn.terraincontrol.bukkit.TCPlugin;
import com.khorn.terraincontrol.customobjects.CustomObjectCompiled;
import com.khorn.terraincontrol.customobjects.CustomObjectGen;
import com.khorn.terraincontrol.customobjects.ObjectsStore;
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

        BukkitWorld bukkitWorld = this.getWorld(me, args.size() > 1 ? args.get(1) : "");

        if (args.size() == 0)
        {
            me.sendMessage(ErrorColor + "You must enter the name of the BO2.");
            return true;
        }
        CustomObjectCompiled spawnObject = null;

        if (bukkitWorld != null)
            spawnObject = ObjectsStore.CompileString(args.get(0), bukkitWorld.getSettings().CustomObjectsDirectory);

        if (spawnObject == null)
        {
            me.sendMessage(BaseCommand.MessageColor + "BO2 not found in world directory. Searching in global directory.");
            spawnObject = ObjectsStore.CompileString(args.get(0), ObjectsStore.GlobalDirectory);
        }

        if (spawnObject == null)
        {
            sender.sendMessage(ErrorColor + "BO2 not found, use '/tc list' to list the available ones.");
            return true;
        }

        Block block = this.getWatchedBlock(me, true);
        if (block == null)
            return true;

        if (CustomObjectGen.GenerateCustomObject(bukkitWorld, new Random(), block.getX(), block.getY(), block.getZ(), spawnObject))
        {
            me.sendMessage(BaseCommand.MessageColor + spawnObject.Name + " was spawned.");
        } else
        {
            me.sendMessage(BaseCommand.ErrorColor + "BO2 cant be spawned over there.");
        }

        return true;
    }

    public Block getWatchedBlock(Player me, boolean verbose)
    {
        if (me == null)
            return null;

        Block block;

        Iterator<Block> itr = new BlockIterator(me, 200);
        while (itr.hasNext())
        {
            block = itr.next();
            if (block.getTypeId() != 0)
            {
                return block;
            }
        }

        if (verbose)
        {
            me.sendMessage(ErrorColor + "No block in sight.");
        }

        return null;
    }
}