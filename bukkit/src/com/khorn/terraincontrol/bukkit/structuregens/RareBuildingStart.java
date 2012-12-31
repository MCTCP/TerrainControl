package com.khorn.terraincontrol.bukkit.structuregens;

import java.util.Random;

import net.minecraft.server.v1_4_6.StructurePiece;
import net.minecraft.server.v1_4_6.StructureStart;
import net.minecraft.server.v1_4_6.World;
import net.minecraft.server.v1_4_6.WorldGenJungleTemple;
import net.minecraft.server.v1_4_6.WorldGenPyramidPiece;
import net.minecraft.server.v1_4_6.WorldGenWitchHut;


import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.bukkit.util.WorldHelper;

public class RareBuildingStart extends StructureStart
{
    @SuppressWarnings("unchecked")
    public RareBuildingStart(World world, Random random, int chunkX, int chunkZ)
    {
        LocalWorld localWorld = WorldHelper.toLocalWorld(world);
        BiomeConfig biomeConfig = localWorld.getSettings().biomeConfigs.get(localWorld.getCalculatedBiomeId(chunkX * 16 + 8, chunkZ * 16 + 8));
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
