package com.Khorn.PTMBukkit.Listeners;


import com.Khorn.PTMBukkit.CustomObjects.ObjectSpawnDelegate;
import com.Khorn.PTMBukkit.PTMPlugin;
import com.Khorn.PTMBukkit.WorldConfig;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.Random;

public class PTMBlockListener extends BlockListener
{
    private PTMPlugin ptmPlugin;
    private Random rnd = new Random();

    public PTMBlockListener(PTMPlugin plugin)
    {
        this.ptmPlugin = plugin;
    }

    @Override
    public void onBlockPlace(BlockPlaceEvent event)
    {
        if (event.getBlock().getTypeId() == net.minecraft.server.Block.SAPLING.id)
        {
            WorldConfig worldSettings = this.ptmPlugin.worldsSettings.get(event.getBlock().getWorld().getName());
            if (worldSettings != null)
            {
                net.minecraft.server.World world = ((CraftWorld)event.getBlock().getWorld()).getHandle();

                if (worldSettings.HasCustomTrees)
                {
                    Runnable task = new ObjectSpawnDelegate(world,worldSettings, event.getBlock());
                    int delay = this.rnd.nextInt(worldSettings.customTreeMaxTime - worldSettings.customTreeMinTime) + worldSettings.customTreeMinTime;

                    this.ptmPlugin.getServer().getScheduler().scheduleAsyncDelayedTask(this.ptmPlugin, task, 10*delay);
                }

            }

        }

    }
}
