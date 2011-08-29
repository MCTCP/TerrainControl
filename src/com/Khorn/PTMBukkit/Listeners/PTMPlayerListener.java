package com.Khorn.PTMBukkit.Listeners;


import com.Khorn.PTMBukkit.PTMPlugin;
import com.Khorn.PTMBukkit.Settings;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.inventory.ItemStack;
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
        if(!event.hasItem())
            return;
        MaterialData data = event.getItem().getData();

        if (data instanceof Dye && ((Dye) data).getColor() == DyeColor.WHITE)
        {

            Block block = event.getClickedBlock();
            if (block.getType() == Material.SAPLING)
                if (this.ptmPlugin.worldsSettings.containsKey(block.getWorld().getName()))
                {
                    Random rnd = new Random();
                    if (rnd.nextBoolean())
                    {
                        Settings worldSettings = this.ptmPlugin.worldsSettings.get(block.getWorld().getName());
                        if (worldSettings.objectSpawner.SpawnCustomTrees(block.getX(), block.getY(), block.getZ()))
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

