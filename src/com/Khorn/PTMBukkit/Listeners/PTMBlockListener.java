package com.Khorn.PTMBukkit.Listeners;


import com.Khorn.PTMBukkit.CustomObjects.ObjectSpawnDelegate;
import com.Khorn.PTMBukkit.PTMPlugin;
import com.Khorn.PTMBukkit.Settings;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.Random;

public class PTMBlockListener extends BlockListener
{
    private PTMPlugin ptmPlugin;

    public PTMBlockListener(PTMPlugin plugin)
    {
        this.ptmPlugin = plugin;
    }

    @Override
    public void onBlockPlace(BlockPlaceEvent event)
    {
        if (event.getBlock().getTypeId() == net.minecraft.server.Block.SAPLING.id)
        {
            if (this.ptmPlugin.worldsSettings.containsKey(event.getBlock().getWorld().getName()))
            {
                Settings worldSettings = this.ptmPlugin.worldsSettings.get(event.getBlock().getWorld().getName());
                if (worldSettings.HasCustomTrees)
                {
                    Runnable task = new ObjectSpawnDelegate(worldSettings.objectSpawner, event.getBlock());
                    Random rnd = new Random();
                    int delay = rnd.nextInt(worldSettings.customTreeMaxTime - worldSettings.customTreeMinTime) + worldSettings.customTreeMinTime;

                    this.ptmPlugin.getServer().getScheduler().scheduleAsyncDelayedTask(this.ptmPlugin, task, 10*delay);
                }

            }

        }

    }
}
