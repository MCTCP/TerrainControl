package com.Khorn.PTMBukkit.Listeners;

import com.Khorn.PTMBukkit.PTMPlugin;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldListener;

public class PTMWorldListener extends WorldListener
{
    private PTMPlugin ptmPlugin;

    public PTMWorldListener(PTMPlugin plugin)
    {
        this.ptmPlugin = plugin;
    }

    @Override
    public void onWorldInit(WorldInitEvent event)
    {

        this.ptmPlugin.WorldInit(event.getWorld());

    }


}
