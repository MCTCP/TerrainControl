package com.khorn.terraincontrol.bukkit.structuregens;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.bukkit.util.WorldHelper;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import net.minecraft.server.v1_5_R2.*;

import java.util.Random;

public class RareBuildingStart extends StructureStart
{
    @SuppressWarnings("unchecked")
    public RareBuildingStart(World world, Random random, int chunkX, int chunkZ)
    {
        LocalWorld localWorld = WorldHelper.toLocalWorld(world);
        BiomeConfig biomeConfig = localWorld.getSettings().biomeConfigs[localWorld.getCalculatedBiomeId(chunkX * 16 + 8, chunkZ * 16 + 8)];
        StructurePiece building;
        switch (biomeConfig.rareBuildingType)
        {
            case desertPyramid:
                building = new WorldGenPyramidPiece(random, chunkX * 16, chunkZ * 16);
                break;
            case jungleTemple:
                building = new WorldGenJungleTemple(random, chunkX * 16, chunkZ * 16);
                break;
            case swampHut:
                building = new WorldGenWitchHut(random, chunkX * 16, chunkZ * 16);
                break;
            case disabled:
            default:
                // Should never happen, but on biome borders there is chance that a
                // structure gets started in a biome where it shouldn't.
                building = null;
                break;
        }

        if (building != null)
        {
            // Add building to components
            this.a.add(building);
        }

        // Update boundingbox
        this.c();
    }
}
