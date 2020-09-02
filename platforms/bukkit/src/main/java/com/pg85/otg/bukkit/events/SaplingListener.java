package com.pg85.otg.bukkit.events;

import com.pg85.otg.bukkit.materials.BukkitMaterialData;
import com.pg85.otg.bukkit.world.BukkitWorld;
import com.pg85.otg.bukkit.world.WorldHelper;
import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.generator.resource.SaplingGen;
import com.pg85.otg.generator.resource.SaplingType;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.IBlockData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.block.BlockState;
import org.bukkit.event.world.StructureGrowEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class SaplingListener
{
    void onStructureGrow(StructureGrowEvent event)
    {
        BukkitWorld world = (BukkitWorld) WorldHelper.toLocalWorld(event.getWorld());
        if (world == null)
        {
            return;
        }
        // Need the event location for later - might also need the material
        Location location = event.getLocation();
        IBlockData blockData = world.getWorld().getType(new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
        BukkitMaterialData material = BukkitMaterialData.ofMinecraftBlockData(blockData);
        SaplingGen sapling;

        // Get sapling type
        SaplingType saplingType = toSaplingType(event.getSpecies());
        // Adjust position for bigger saplings
        boolean wideTrunk = saplingType.requiresFourSaplings();
        if (wideTrunk || saplingType == SaplingType.Custom)
        {
            //Location lowestXZ = searchLowestXZLocation(location.getBlockY(), event.getBlocks());
            Location bigSaplingBlock = findFourSaplings(location, material, world);
            if (bigSaplingBlock == null)
            {
                // Not four saplings
                wideTrunk = false;
            }
            else
            {
                wideTrunk = true;
                location = bigSaplingBlock;
            }
        }
        // Add all sapling blocks to a list for later removal of saplings
        ArrayList<BlockState> saplingBlockStates = new ArrayList<>();
        byte data = 0;
        for (BlockState block : event.getBlocks())
        {
            if (block.getY() == location.getY()) {
                if (block.getX() == location.getX() && block.getZ() == location.getZ())
                {
                    saplingBlockStates.add(block);
                    // Get deprecated data value - only way to store the sapling type
                    data = block.getBlock().getData();
                }
                else if (wideTrunk && (block.getX() == location.getX() + 1 || block.getZ() == location.getZ() + 1))
                {
                    saplingBlockStates.add(block);
                }
            }
        }
        
        LocalBiome biome = world.getBiome(location.getBlockX(), location.getBlockZ());
        // When does this happen? Not sure, but better safe than sorry. It was like this before.
        if (biome == null) {
            return;
        }
        BiomeConfig biomeConfig = biome.getBiomeConfig();

        // Get sapling generator
        // Else is the standard behaviour for Spigot - allowing custom saplings is really just to future-proof it
        // Also means Spigot and Forge have identical implementation to allow easier debugging and fixing in future
        // Note: Custom replaces the old nullpointer from toSaplingType()
        if (saplingType == SaplingType.Custom)
        {
            sapling = biomeConfig.getCustomSaplingGen(material, wideTrunk);
        }
        else
        {
            sapling = biomeConfig.getSaplingGen(saplingType);
        }

        // If this doesn't have a sapling, check inherited resources
        if (sapling == null
                && biomeConfig.inheritSaplingResource
                && biomeConfig.replaceToBiomeName != null
                && biomeConfig.replaceToBiomeName.trim().length() > 0) {

            biome = world.getBiomeByNameOrNull(biomeConfig.replaceToBiomeName);
            if (biome == null) {
                return;
            }

            biomeConfig = biome.getBiomeConfig();

            if (saplingType == SaplingType.Custom)
            {
                sapling = biomeConfig.getCustomSaplingGen(material, wideTrunk);
            }
            else
            {
                sapling = biomeConfig.getSaplingGen(saplingType);
            }
        }
        // No Sapling Resource for this sapling type in this or inherited queue, we ignore this
        if (sapling == null)
        {
            return;
        }

        // Optimistically remove the saplings.
        for (BlockState b : saplingBlockStates) {
            b.setType(Material.AIR);
            b.update(true);
        }

        // Try 10 times to spawn tree
        boolean success = false;
        Random random = new Random();
        for (int i = 0; i < 10; i++)
        {
            if (sapling.growSapling(world, random, wideTrunk, location.getBlockX(), location.getBlockY(), location.getBlockZ()))
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
            // Cannot grow, so restore the saplings.
            event.setCancelled(true);
            for (BlockState b : saplingBlockStates) {
                b.getBlock().setTypeIdAndData(material.getBlockId(), data, false);
            }
        }
    }

    /**
     * Check that the sapling types are the same, ignoring growth stages.
     * @param sapling1 The first material to compare.
     * @param sapling2 The second material to compare.
     * @return True if both saplings are of the same type
     */
    private boolean isSameSapling(LocalMaterialData sapling1, LocalMaterialData sapling2)
    {
        if (sapling1 == null || sapling2 == null)
            return false;
        return 
    		sapling1.getBlockData() % 8 == sapling2.getBlockData() % 8 &&
        	sapling1.getBlockId() == sapling2.getBlockId();
    }

    /**
     * Gets whether the saplings are placed in a 2x2 pattern. If successful,
     * it returns a BlockPos that represents the top left
     * sapling (with the lowest x and z). If not, it returns null.
     *
     * @return BlockPos of sapling with lowest X and Z, or null if not four saplings
     */
    private Location findFourSaplings(Location loc, LocalMaterialData material, LocalWorld world)
    {
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        for (int treeOffsetX = 0; treeOffsetX >= -1; --treeOffsetX)
        {
            for (int treeOffsetZ = 0; treeOffsetZ >= -1; --treeOffsetZ)
            {
                if (isSameSapling(material, world.getMaterial(x + treeOffsetX, y, z + treeOffsetZ, null))
                        && isSameSapling(material, world.getMaterial(x + treeOffsetX + 1, y, z + treeOffsetZ, null))
                        && isSameSapling(material, world.getMaterial(x + treeOffsetX, y, z + treeOffsetZ + 1, null))
                        && isSameSapling(material, world.getMaterial(x + treeOffsetX + 1, y, z + treeOffsetZ + 1, null)))
                {
                    // Found! Adjust internal position
                    return loc.add(treeOffsetX, 0, treeOffsetZ);
                }
            }
        }
        return null;
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
            // These don't grow from saplings in the normal sense, they just generate naturally.
            // They're put as custom, because if a sapling growth event happens with these as type,
            // that's from a plugin and not vanilla. Does give a theoretical way to add more sapling types
            // to spigot using plugins, so leaving this as custom.
            case COCOA_TREE:
            case SWAMP:
            case JUNGLE_BUSH:
            case CHORUS_PLANT:
            default:
                return SaplingType.Custom;
        }
    }
}
