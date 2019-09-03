package com.pg85.otg.forge.generator.structure;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.forge.ForgeWorld;
import com.pg85.otg.forge.biomes.ForgeBiome;
import com.pg85.otg.network.ConfigProvider;
import com.pg85.otg.util.minecraft.defaults.StructureNames;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.gen.structure.StructureStart;

public class OTGWoodLandMansionGen extends OTGMapGenStructure
{
	static
	{
		MapGenStructureIO.registerStructure(OTGWoodLandMansionStart.class, "MansionOTG");
	}

    private int spacing = 80;
    private int separation = 20;

    private final List<Biome> woodLandMansionSpawnBiomes;

    public OTGWoodLandMansionGen(ConfigProvider settings, ForgeWorld world)
    {
    	super(world);
        this.woodLandMansionSpawnBiomes = new ArrayList<Biome>();

        for (LocalBiome biome : settings.getBiomeArrayByOTGId())
        {
            if (biome == null || !biome.getBiomeConfig().woodLandMansionsEnabled)
            {
                continue;
            }

            this.woodLandMansionSpawnBiomes.add(((ForgeBiome) biome).getHandle());
        }
    }

    protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ)
    {
        int i = chunkX;
        int j = chunkZ;

        if (chunkX < 0)
        {
            i = chunkX - 79;
        }

        if (chunkZ < 0)
        {
            j = chunkZ - 79;
        }

        int k = i / 80;
        int l = j / 80;
        Random random = this.world.setRandomSeed(k, l, 10387319);
        k = k * 80;
        l = l * 80;
        k = k + (random.nextInt(60) + random.nextInt(60)) / 2;
        l = l + (random.nextInt(60) + random.nextInt(60)) / 2;

        if (chunkX == k && chunkZ == l)
        {
        	boolean flag = this.world.getBiomeProvider().areBiomesViable(chunkX * 16 + 8, chunkZ * 16 + 8, 32, woodLandMansionSpawnBiomes);

            if (flag)
            {
            	//OTG.log(LogMarker.INFO, "SPAWNING MANSION AT " + (chunkX * 16 + 8) + " 100 " + (chunkZ * 16 + 8));
                return true;
            }
        }

        return false;
    }

    public BlockPos getNearestStructurePos(World worldIn, BlockPos pos, boolean findUnexplored)
    {
        this.world = worldIn;
        BiomeProvider biomeprovider = worldIn.getBiomeProvider();
        return biomeprovider.isFixedBiome() && !woodLandMansionSpawnBiomes.contains(biomeprovider.getFixedBiome()) ? null : findNearestStructurePosBySpacing(worldIn, this, pos, this.spacing, this.separation, 10387319, true, 100, findUnexplored);
        //return biomeprovider.isFixedBiome() && biomeprovider.getFixedBiome() != Biomes.ROOFED_FOREST ? null : findNearestStructurePosBySpacing(worldIn, this, pos, this.spacing, this.separation, 10387319, true, 100, findUnexplored);
    }

    @Override
    public String getStructureName()
    {
        return StructureNames.WOODLAND_MANSION;
    }

    protected StructureStart getStructureStart(int chunkX, int chunkZ)
    {
    	//if(this.world.getChunkProvider() instanceof ChunkProviderOverworld)
    	{
    		return new OTGWoodLandMansionStart(this.world, this.rand, chunkX, chunkZ);
    	}
    	//return null;
    }
}