package com.pg85.otg.bukkit.generator.structures;

import java.util.Random;

import com.pg85.otg.bukkit.world.WorldHelper;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.biome.BiomeConfig;

import net.minecraft.server.v1_12_R1.StructurePiece;
import net.minecraft.server.v1_12_R1.StructureStart;
import net.minecraft.server.v1_12_R1.World;
import net.minecraft.server.v1_12_R1.WorldGenRegistration.WorldGenJungleTemple;
import net.minecraft.server.v1_12_R1.WorldGenRegistration.WorldGenPyramidPiece;
import net.minecraft.server.v1_12_R1.WorldGenRegistration.WorldGenWitchHut;
import net.minecraft.server.v1_12_R1.WorldGenRegistration.b;

public class RareBuildingStart extends StructureStart
{
    RareBuildingStart(World world, Random random, int chunkX, int chunkZ)
    {
        LocalWorld localWorld = WorldHelper.toLocalWorld(world);
        BiomeConfig biomeConfig = localWorld.getBiome(chunkX * 16 + 8, chunkZ * 16 + 8).getBiomeConfig();
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
            case igloo:
                building = new b(random, chunkX * 16, chunkZ * 16);
                break;
            case disabled:
            default:
                // Should never happen, but on biome borders there is chance
                // that a
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
        this.d();
    }

    public RareBuildingStart()
    {
        // Required by Minecraft's structure loading code
    }
}