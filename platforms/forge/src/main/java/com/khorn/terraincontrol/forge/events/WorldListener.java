package com.khorn.terraincontrol.forge.events;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.forge.ForgeWorld;
import com.khorn.terraincontrol.forge.WorldLoader;
import com.khorn.terraincontrol.forge.util.WorldHelper;
import com.khorn.terraincontrol.logging.LogMarker;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class WorldListener
{

    private WorldLoader worldLoader;

    public WorldListener(WorldLoader worldLoader)
    {
        this.worldLoader = worldLoader;
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event)
    {
        World mcWorld = event.getWorld();
        ForgeWorld forgeWorld = this.worldLoader.getWorld(WorldHelper.getName(mcWorld));
        if (forgeWorld == null)
        {
            return;
        }

        //this.worldLoader.unloadWorld(forgeWorld);
        TerrainControl.log(LogMarker.INFO, "Why would we need to unload world \"{}\"?", forgeWorld.getName());
    }
}
