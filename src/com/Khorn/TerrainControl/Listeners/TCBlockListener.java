package com.Khorn.TerrainControl.Listeners;


import com.Khorn.TerrainControl.Configuration.WorldConfig;
import com.Khorn.TerrainControl.CustomObjects.ObjectSpawnDelegate;
import com.Khorn.TerrainControl.TCPlugin;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.Random;

public class TCBlockListener extends BlockListener
{
    private TCPlugin tcPlugin;
    private Random rnd = new Random();

    public TCBlockListener(TCPlugin plugin)
    {
        this.tcPlugin = plugin;
    }

    @Override
    public void onBlockPlace(BlockPlaceEvent event)
    {
        if (event.getBlock().getTypeId() == net.minecraft.server.Block.SAPLING.id)
        {
            WorldConfig worldSettings = this.tcPlugin.worldsSettings.get(event.getBlock().getWorld().getName());
            if (worldSettings != null)
            {
                net.minecraft.server.World world = ((CraftWorld)event.getBlock().getWorld()).getHandle();

                if (worldSettings.HasCustomTrees)
                {
                    Runnable task = new ObjectSpawnDelegate(world,worldSettings, event.getBlock());
                    int delay = this.rnd.nextInt(worldSettings.customTreeMaxTime - worldSettings.customTreeMinTime) + worldSettings.customTreeMinTime;

                    this.tcPlugin.getServer().getScheduler().scheduleAsyncDelayedTask(this.tcPlugin, task, 10*delay);
                }

            }

        }

    }
}
