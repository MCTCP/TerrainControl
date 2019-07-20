package com.pg85.otg.forge.generator.structure;

import java.util.ArrayList;

import com.pg85.otg.OTG;
import com.pg85.otg.forge.world.ForgeWorld;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.ChunkCoordinate;

import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.structure.MapGenStructure;

public abstract class OTGMapGenStructure extends MapGenStructure
{	
	ForgeWorld forgeWorld;
	int doubleBiome;
	//ArrayList<ChunkCoordinate> usedCoords2 = new ArrayList<ChunkCoordinate>();
	
	public OTGMapGenStructure(ForgeWorld world)
	{
		this.forgeWorld = world;
	}

	@Override
    public void generate(World worldIn, int x, int z, ChunkPrimer primer)
    {
        int i = this.range;
        this.world = worldIn;
        this.rand.setSeed(worldIn.getSeed());
        long j = this.rand.nextLong();
        long k = this.rand.nextLong();

        //doubleBiome = 0;
        
        for (int l = x - i; l <= x + i; ++l)
        {
            for (int i1 = z - i; i1 <= z + i; ++i1)
            {
            	// TODO: Make sure this doesn't mess up villages and mineshafts.
            	// It looks like MC plots structurestarts in already populated chunks,
            	// which means any structure parts in populated chunks never get spawned.
            	// Structure parts in unpopulated chunks will get spawned though.
            	// Checking for insidepregeneratedregion disallows structurestarts in populated areas.
                //if(!this.forgeWorld.isInsidePregeneratedRegion(ChunkCoordinate.fromChunkCoords(l, i1)))
                {
                    long j1 = (long)l * j;
                    long k1 = (long)i1 * k;
                    this.rand.setSeed(j1 ^ k1 ^ worldIn.getSeed());
	                this.recursiveGenerate(worldIn, l, i1, x, z, primer);
                }
            }
        }
        //if(this instanceof OTGMineshaftGen)
        {
        	//OTG.log(LogMarker.INFO, "DOUBLEBIOME: " + doubleBiome);
        }
    }    	
}
