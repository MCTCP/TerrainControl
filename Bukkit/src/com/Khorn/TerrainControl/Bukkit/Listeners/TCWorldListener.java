package com.Khorn.TerrainControl.Bukkit.Listeners;

import com.Khorn.TerrainControl.Configuration.WorldConfig;
import com.Khorn.TerrainControl.CustomObjects.CustomObjectGen;
import com.Khorn.TerrainControl.TCPlugin;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldListener;

import java.util.Random;

public class TCWorldListener extends WorldListener
{
    private TCPlugin tcPlugin;
    private Random random;

    public TCWorldListener(TCPlugin plugin)
    {
        this.tcPlugin = plugin;
        this.random = new Random();
    }

    @Override
    public void onWorldInit(WorldInitEvent event)
    {

        this.tcPlugin.WorldInit(event.getWorld());

    }

    @Override
    public void onStructureGrow(StructureGrowEvent event)
    {
        WorldConfig config = this.tcPlugin.worldsSettings.get(event.getWorld().getName());  //Todo too long!
        if (config != null && config.HasCustomTrees)
        {
            if(this.random.nextInt(100) < config.customTreeChance)
            {
                CustomObjectGen.SpawnCustomTrees(((CraftWorld)event.getWorld()).getHandle(),this.random,config,event.getLocation().getBlockX(),event.getLocation().getBlockY(),event.getLocation().getBlockZ());
                event.setCancelled(true);
            }

        }

    }
}
