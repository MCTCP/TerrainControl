package com.khorn.terraincontrol.bukkit.events;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.bukkit.util.WorldHelper;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.generator.resource.SaplingGen;
import com.khorn.terraincontrol.generator.resource.SaplingType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.block.BlockState;
import org.bukkit.event.world.StructureGrowEvent;

import java.util.List;
import java.util.Random;

class SaplingListener
{
    void onStructureGrow(StructureGrowEvent event)
    {
        LocalWorld world = WorldHelper.toLocalWorld(event.getWorld());
        if (world == null)
        {
            return;
        }

        Location location = event.getLocation();

        LocalBiome biome = world.getBiome(location.getBlockX(), location.getBlockZ());
        if (biome == null)
        {
            return;
        }

        BiomeConfig biomeConfig = biome.getBiomeConfig();

        // Get sapling type
        SaplingType saplingType = toSaplingType(event.getSpecies());
        if (saplingType == null)
        {
            return;
        }

        // Adjust position for bigger saplings
        if (saplingType.requiresFourSaplings())
        {
            Location lowestXZ = searchLowestXZLocation(location.getBlockY(), event.getBlocks());
            if (lowestXZ == null)
            {
                return;
            }
            location = lowestXZ;
        }

        // Get generator
        SaplingGen sapling = biomeConfig.getSaplingGen(saplingType);
        if (sapling == null)
        {
            return;
        }

        // Try 10 times to spawn tree
        boolean success = false;
        for (int i = 0; i < 10; i++)
        {
            if (sapling.growSapling(world, new Random(), location.getBlockX(), location.getBlockY(), location.getBlockZ()))
            {
                success = true;
                break;
            }
        }

        if (success)
        {
            // Just spawned the tree, clear the blocks list to prevent
            // Bukkit spawning another tree
            event.getBlocks().clear();
        } else
        {
            // Cannot grow, so leave the sapling there
            event.setCancelled(true);
        }
    }

    /**
     * Unfortunately, Bukkit provides no way to get the corner of 2x2 sapling
     * structures. (It just returns the location of the sapling the player
     * clicked on with bonemeal.) We need this position to properly spawn
     * custom objects. This method scans all blocks that are going to be
     * placed, and finds the block of type <code>LOG</code> or
     * <code>LOG_2</code> with the lowest x/z position.
     * 
     * @param y The y to search on. This is the y at which the sapling was
     *            placed.
     * @param blocks The blocks of the grown vanilla tree to search.
     * @return The lowest x/z location.
     */
    private Location searchLowestXZLocation(int y, List<BlockState> blocks)
    {
        BlockState lowestXZ = null;
        for (BlockState blockState : blocks)
        {
            // Check if block has correct y
            if (blockState.getY() != y)
            {
                continue;
            }

            // Check if block is a log
            if (blockState.getType() != Material.LOG && blockState.getType() != Material.LOG_2)
            {
                continue;
            }

            if (lowestXZ == null)
            {
                // Found a candidate
                lowestXZ = blockState;
                continue;
            }

            if (blockState.getX() <= lowestXZ.getX() && blockState.getZ() <= lowestXZ.getZ())
            {
                // Found a better candidate
                lowestXZ = blockState;
                continue;
            }
        }

        if (lowestXZ == null)
        {
            return null;
        } else
        {
            return lowestXZ.getLocation();
        }
    }

    private SaplingType toSaplingType(TreeType treeType)
    {
        switch (treeType)
        {
            case REDWOOD:
            case TALL_REDWOOD: // Both share the same sapling
                return SaplingType.Redwood;
            case BIRCH:
            case TALL_BIRCH:
                return SaplingType.Birch;
            case JUNGLE:
                return SaplingType.BigJungle;
            case SMALL_JUNGLE:
                return SaplingType.SmallJungle;
            case TREE:
            case BIG_TREE: // Both share the same sapling
                return SaplingType.Oak;
            case RED_MUSHROOM:
                return SaplingType.RedMushroom;
            case BROWN_MUSHROOM:
                return SaplingType.BrownMushroom;
            case ACACIA:
                return SaplingType.Acacia;
            case DARK_OAK:
                return SaplingType.DarkOak;
            case MEGA_REDWOOD:
                return SaplingType.HugeRedwood;
            default:
                return null;
        }
    }
}
