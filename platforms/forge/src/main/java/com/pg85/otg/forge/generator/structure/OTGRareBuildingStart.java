package com.pg85.otg.forge.generator.structure;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.forge.ForgeEngine;

import net.minecraft.world.World;
import net.minecraft.world.gen.structure.ComponentScatteredFeaturePieces;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureStart;

import java.util.Random;

public class OTGRareBuildingStart extends StructureStart
{
    // Required by Minecraft's structure loading code
    public OTGRareBuildingStart()
    {
    	super();
    }
	
    OTGRareBuildingStart(World world, Random random, int chunkX, int chunkZ)
    {
        LocalWorld localWorld = ((ForgeEngine)OTG.getEngine()).getWorld(world);
        if(localWorld == null)
        {
        	localWorld = ((ForgeEngine)OTG.getEngine()).getWorld(world);
        	throw new RuntimeException("Whatever it is you're trying to do, we didn't write any code for it (sorry). Please contact Team OTG about this crash.");
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
}
