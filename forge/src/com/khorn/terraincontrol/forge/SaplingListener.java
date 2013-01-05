package com.khorn.terraincontrol.forge;

import com.khorn.terraincontrol.DefaultMaterial;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.forge.util.WorldHelper;
import com.khorn.terraincontrol.generator.resourcegens.SaplingGen;
import net.minecraft.world.World;
import net.minecraftforge.event.Event.Result;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.terraingen.SaplingGrowTreeEvent;

public class SaplingListener
{
    @ForgeSubscribe
    public void onSaplingGrow(SaplingGrowTreeEvent event)
    {
        // Get the Terrain Control world
        World world = event.world;
        LocalWorld worldTC = WorldHelper.toLocalWorld(event.world);
        if (worldTC == null)
        {
            // World not managed by TerrainControl
            return;
        }

        // Get the BiomeConfig
        BiomeConfig biomeConfig = worldTC.getSettings().biomeConfigs[worldTC.getBiomeId(event.x, event.z)];

        // Get the sapling data
        int saplingId = world.getBlockId(event.x, event.y, event.z);
        int saplingData = world.getBlockMetadata(event.x, event.y, event.z) & 3;

        // Check if it's a vanilla sapling
        if (saplingId != DefaultMaterial.SAPLING.id)
        {
            // Maybe support for modded saplings in the future?
            return;
        }

        // Get the sapling
        SaplingGen saplingGen = biomeConfig.SaplingTypes[saplingData];
        if (saplingGen == null)
        {
            // Try the non-specific SaplingResource
            saplingGen = biomeConfig.SaplingResource;
        }

        // Grow tree
        if (saplingGen != null)
        {
            // Remove sapling
            worldTC.setBlock(event.x, event.y, event.z, DefaultMaterial.AIR.id, 0);

            boolean success = false;
            for (int i = 0; i < 10; i++)
            {
                if (saplingGen.growSapling(worldTC, event.rand, event.x, event.y, event.z))
                {
                    success = true;
                    break;
                }
            }
            if (!success)
            {
                // Restore sapling
                worldTC.setBlock(event.x, event.y, event.z, DefaultMaterial.SAPLING.id, saplingData);
            }

            // Cancel event (we have grown a tree by ourselves)
            event.setResult(Result.DENY);
        }
    }
}
