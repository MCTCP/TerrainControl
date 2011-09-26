package com.Khorn.PTMBukkit.Listeners;


import com.Khorn.PTMBukkit.Commands.BaseCommand;
import com.Khorn.PTMBukkit.CustomObjects.CustomObjectGen;
import com.Khorn.PTMBukkit.PTMPlayer;
import com.Khorn.PTMBukkit.PTMPlugin;
import com.Khorn.PTMBukkit.WorldConfig;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.material.Dye;
import org.bukkit.material.MaterialData;

import java.util.Random;

public class PTMPlayerListener extends PlayerListener
{

    private PTMPlugin ptmPlugin;

    public PTMPlayerListener(PTMPlugin plugin)
    {
        this.ptmPlugin = plugin;

    }

    @Override
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        PTMPlayer player = ptmPlugin.GetPlayer(event.getPlayer());
        if (player.hasObjectToSpawn)
        {
            Block block = event.getClickedBlock();
            WorldConfig worldSettings = this.ptmPlugin.worldsSettings.get(block.getWorld().getName());
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

        if (!event.hasItem())
            return;
        MaterialData data = event.getItem().getData();

        if (data instanceof Dye && ((Dye) data).getColor() == DyeColor.WHITE)
        {

            Block block = event.getClickedBlock();
            if(block == null)
                return;
            if (block.getType() == Material.SAPLING)
                if (this.ptmPlugin.worldsSettings.containsKey(block.getWorld().getName()))
                {
                    Random rnd = new Random();
                    if (rnd.nextBoolean())
                    {
                        WorldConfig worldSettings = this.ptmPlugin.worldsSettings.get(block.getWorld().getName());
                        net.minecraft.server.World world = ((CraftWorld) block.getWorld()).getHandle();
                        if (CustomObjectGen.SpawnCustomTrees(world,new Random(),worldSettings, block.getX(), block.getY(), block.getZ()))
                        {
                            int amount = event.getItem().getAmount() - 1;
                            if (amount == 0)
                                event.getPlayer().getInventory().remove(event.getItem());
                            else
                                event.getItem().setAmount(amount);
                            event.setCancelled(true);
                        }
                    }

                }

        }
    }


}

