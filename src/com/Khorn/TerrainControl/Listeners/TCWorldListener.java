package com.Khorn.TerrainControl.Listeners;

import com.Khorn.TerrainControl.TCPlugin;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldListener;

public class TCWorldListener extends WorldListener
{
    private TCPlugin tcPlugin;

    public TCWorldListener(TCPlugin plugin)
    {
        this.tcPlugin = plugin;
    }

    @Override
    public void onWorldInit(WorldInitEvent event)
    {

        this.tcPlugin.WorldInit(event.getWorld());

    }


}
