package com.pg85.otg.forge.generator.structure;

import com.pg85.otg.forge.ForgeWorld;

import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.structure.MapGenStructure;

public abstract class OTGMapGenStructure extends MapGenStructure
{	
	ForgeWorld forgeWorld;
	
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
        
        for (int l = x - i; l <= x + i; ++l)
        {
            for (int i1 = z - i; i1 <= z + i; ++i1)
            {
                long j1 = (long)l * j;
                long k1 = (long)i1 * k;
                this.rand.setSeed(j1 ^ k1 ^ worldIn.getSeed());
                this.recursiveGenerate(worldIn, l, i1, x, z, primer);
            }
        }
    }    	
}
