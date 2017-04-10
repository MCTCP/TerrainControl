package com.khorn.terraincontrol.forge.generator.structure;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.forge.ForgeEngine;

import net.minecraft.world.World;
import net.minecraft.world.gen.structure.ComponentScatteredFeaturePieces;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureStart;

import java.util.Random;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class RareBuildingStart extends StructureStart
{
    public RareBuildingStart(World world, Random random, int chunkX, int chunkZ)
    {
        LocalWorld localWorld = ((ForgeEngine)TerrainControl.getEngine()).getWorld(world);
        if(localWorld == null)
        {
        	localWorld = ((ForgeEngine)TerrainControl.getEngine()).getWorld(world);
        	throw new NotImplementedException();
        }
        BiomeConfig biomeConfig = localWorld.getBiome(chunkX * 16 + 8, chunkZ * 16 + 8).getBiomeConfig();
        StructureComponent building;
        switch (biomeConfig.rareBuildingType)
        {
            case desertPyramid:
                building = new ComponentScatteredFeaturePieces.DesertPyramid(random, chunkX * 16, chunkZ * 16);
                break;
            case jungleTemple:
                building = new ComponentScatteredFeaturePieces.JunglePyramid(random, chunkX * 16, chunkZ * 16);
                break;
            case swampHut:
                building = new ComponentScatteredFeaturePieces.SwampHut(random, chunkX * 16, chunkZ * 16);
                break;
            case igloo:
                building = new ComponentScatteredFeaturePieces.Igloo(random, chunkX * 16, chunkZ * 16);
                break;
            case disabled:
            default:
                // Should never happen, but on biome borders there is chance
                // that a structure gets started in a biome where it shouldn't.
                building = null;
                break;
        }

        if (building != null)
        {
            this.components.add(building);
        }

        this.updateBoundingBox();
    }

    public RareBuildingStart()
    {
        // Required by Minecraft's structure loading code
    }
}
