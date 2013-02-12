package com.khorn.terraincontrol.forge.structuregens;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.forge.util.WorldHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.*;

import java.util.Random;

public class RareBuildingStart extends StructureStart
{
    @SuppressWarnings("unchecked")
    public RareBuildingStart(World world, Random random, int chunkX, int chunkZ)
    {
        LocalWorld localWorld = WorldHelper.toLocalWorld(world);
        BiomeConfig biomeConfig = localWorld.getSettings().biomeConfigs[localWorld.getCalculatedBiomeId(chunkX * 16 + 8, chunkZ * 16 + 8)];
        StructureComponent building;
        switch (biomeConfig.rareBuildingType)
        {
            case desertPyramid:
                building = new ComponentScatteredFeatureDesertPyramid(random, chunkX * 16, chunkZ * 16);
                break;
            case jungleTemple:
                building = new ComponentScatteredFeatureJunglePyramid(random, chunkX * 16, chunkZ * 16);
                break;
            case swampHut:
                building = new ComponentScatteredFeatureSwampHut(random, chunkX * 16, chunkZ * 16);
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
            this.components.add(building);
        }

        this.updateBoundingBox();
    }
}
