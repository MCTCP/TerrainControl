package com.khorn.terraincontrol.bukkit.events;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalMaterialData;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.bukkit.BukkitMaterialData;
import com.khorn.terraincontrol.bukkit.util.WorldHelper;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.exception.BiomeNotFoundException;
import com.khorn.terraincontrol.generator.resource.SaplingGen;
import com.khorn.terraincontrol.generator.resource.SaplingType;
import net.minecraft.server.v1_12_R1.Blocks;
import net.minecraft.server.v1_12_R1.IBlockState;
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

        LocalBiome biome;
        try
        {
            biome = world.getSavedBiome(location.getBlockX(), location.getBlockZ());
        } catch (BiomeNotFoundException e)
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

        // Get sapling material
        LocalMaterialData sapling = world.getMaterial(location.getBlockX(), location.getBlockY(), location.getBlockZ());

        // Adjust position for bigger saplings
        boolean wideTrunk = saplingType.requiresFourSaplings();
        if (wideTrunk)
        {
            Location lowestXZ = searchLowestXZLocation(location.getBlockY(), event.getBlocks());
            if (lowestXZ == null)
            {
                return;
            }
            location = lowestXZ;
        }

        // Get generator
        SaplingGen saplingGen = biomeConfig.getSaplingGen(saplingType);
        if (saplingGen == null)
        {
            return;
        }

        // Remove saplings
        BukkitMaterialData air = BukkitMaterialData.ofMinecraftBlock(Blocks.AIR);
        int saplingX = location.getBlockX();
        int saplingY = location.getBlockY();
        int saplingZ = location.getBlockZ();
        if (wideTrunk)
        {
            world.setBlock(saplingX, saplingY, saplingZ, air);
            world.setBlock(saplingX + 1, saplingY, saplingZ, air);
            world.setBlock(saplingX, saplingY, saplingZ + 1, air);
            world.setBlock(saplingX + 1, saplingY, saplingZ + 1, air);
        } else
        {
            world.setBlock(saplingX, saplingY, saplingZ, air);
        }

        // Try 10 times to spawn tree
        boolean saplingGrown = false;
        Random random = new Random();
        for (int i = 0; i < 10; i++)
        {
            if (saplingGen.growSapling(world, random, wideTrunk, location.getBlockX(), location.getBlockY(), location.getBlockZ()))
            {
                saplingGrown = true;
                break;
            }
        }

        if (!saplingGrown)
        {
            // Restore sapling
            if (wideTrunk)
            {
                world.setBlock(saplingX, saplingY, saplingZ, sapling);
                world.setBlock(saplingX + 1, saplingY, saplingZ, sapling);
                world.setBlock(saplingX, saplingY, saplingZ + 1, sapling);
                world.setBlock(saplingX + 1, saplingY, saplingZ + 1, sapling);
            } else
            {
                world.setBlock(saplingX, saplingY, saplingZ, sapling);
            }
        }

        if (saplingGrown)
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
            // Ignore blocks at incorrect y
            if (blockState.getY() != y)
            {
                continue;
            }

            // Ignore blocks that are not a log
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
