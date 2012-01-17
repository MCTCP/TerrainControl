package com.Khorn.TerrainControl.Bukkit.Listeners;


import com.Khorn.TerrainControl.Bukkit.Commands.BaseCommand;
import com.Khorn.TerrainControl.Configuration.WorldConfig;
import com.Khorn.TerrainControl.CustomObjects.CustomObjectGen;
import com.Khorn.TerrainControl.TCPlayer;
import com.Khorn.TerrainControl.TCPlugin;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.material.Dye;
import org.bukkit.material.MaterialData;

import java.util.Random;

public class TCPlayerListener extends PlayerListener
{

    private TCPlugin tcPlugin;

    public TCPlayerListener(TCPlugin plugin)
    {
        this.tcPlugin = plugin;

    }

    @Override
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        TCPlayer player = tcPlugin.GetPlayer(event.getPlayer());
        if (player.hasObjectToSpawn)
        {
            Block block = event.getClickedBlock();
            WorldConfig worldSettings = this.tcPlugin.worldsSettings.get(block.getWorld().getName());
            if (worldSettings != null)
            {
                net.minecraft.server.World world = ((CraftWorld) block.getWorld()).getHandle();

                if (CustomObjectGen.GenerateCustomObject(world, new Random(), worldSettings, block.getX(), block.getY(), block.getZ(), player.object, true))
                    event.getPlayer().sendMessage(BaseCommand.MessageColor + player.object.name + " spawned");
                else
                    event.getPlayer().sendMessage(BaseCommand.ErrorColor + "This object cant spawn here");
                player.hasObjectToSpawn = false;
                event.setCancelled(true);
                return;
            }

        }
    }


}

