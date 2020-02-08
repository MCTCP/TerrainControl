package com.pg85.otg.forge.generator.structure;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.configuration.biome.BiomeConfig.MineshaftType;
import com.pg85.otg.forge.ForgeEngine;
import com.pg85.otg.forge.world.ForgeWorld;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.FifoMap;
import com.pg85.otg.util.minecraft.defaults.StructureNames;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.MapGenMineshaft;
import net.minecraft.world.gen.structure.StructureMineshaftStart;
import net.minecraft.world.gen.structure.StructureStart;

// TODO: This should inherit from MapGenMineshaft, or other mods may trip on it when Forge fires a spawn event.
public class OTGMineshaftGen extends OTGMapGenStructure
{
	public OTGMineshaftGen(ForgeWorld world)
	{
		super(world);
	}

	private class CachedCoord
	{
		boolean canSpawnMineShaft;
		double mineshaftsRarity;
		
		public CachedCoord(boolean canSpawnMineShaft2, double mineshaftsRarity2)
		{
			this.canSpawnMineShaft = canSpawnMineShaft2;
			this.mineshaftsRarity = mineshaftsRarity2;
		}
	}
	
	FifoMap<ChunkCoordinate, CachedCoord> cachedCoordsByChunk = new FifoMap<ChunkCoordinate, CachedCoord>(256);
	
	
    @Override
    protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ)
    {
        if (this.rand.nextInt(80) < Math.max(Math.abs(chunkX), Math.abs(chunkZ)))
        {
            ChunkCoordinate chunkCoord = ChunkCoordinate.fromChunkCoords(chunkX, chunkZ);
            CachedCoord cachedCoord = this.cachedCoordsByChunk.get(chunkCoord);
            if(cachedCoord == null)
            {
            	//if(this.usedCoords2.contains(chunkCoord))
            	//{
            		//this.doubleBiome++;
            	//} else {
            		//this.usedCoords2.add(chunkCoord);
            	//}
                
                LocalBiome biome = this.forgeWorld.getBiome(chunkCoord.getBlockXCenter(), chunkCoord.getBlockXCenter());
                BiomeConfig biomeConfig = biome.getBiomeConfig();
                cachedCoord = new CachedCoord(biomeConfig.mineshaftType != MineshaftType.disabled, biomeConfig.mineshaftsRarity);
                this.cachedCoordsByChunk.put(chunkCoord, cachedCoord);
            } else {
            	
            }
            if (!cachedCoord.canSpawnMineShaft)
            {
                return false;
            }
            if (this.rand.nextDouble() * 100.0 < cachedCoord.mineshaftsRarity)
            {
                return true;
            }
        }

        return false;
    }

    @Override
    protected StructureStart getStructureStart(int chunkX, int chunkZ)
    {
        LocalWorld world = ((ForgeEngine)OTG.getEngine()).getWorld(this.world);
        LocalBiome biome = world.getBiome(chunkX * ChunkCoordinate.CHUNK_X_SIZE + 8,
                chunkZ * ChunkCoordinate.CHUNK_Z_SIZE + 8);
        BiomeConfig biomeConfig = biome.getBiomeConfig();
        MapGenMineshaft.Type mineshaftType = MapGenMineshaft.Type.NORMAL;
        if (biomeConfig.mineshaftType == MineshaftType.mesa)
        {
            mineshaftType = MapGenMineshaft.Type.MESA;
        }

        return new StructureMineshaftStart(this.world, this.rand, chunkX, chunkZ, mineshaftType);
    }

    @Override
    public String getStructureName()
    {
        return StructureNames.MINESHAFT;
    }

	@Override
    public BlockPos getNearestStructurePos(World worldIn, BlockPos pos, boolean p_180706_3_)
    {
        int j = pos.getX() >> 4;
        int k = pos.getZ() >> 4;

        for (int l = 0; l <= 1000; ++l)
        {
            for (int i1 = -l; i1 <= l; ++i1)
            {
                boolean flag = i1 == -l || i1 == l;

                for (int j1 = -l; j1 <= l; ++j1)
                {
                    boolean flag1 = j1 == -l || j1 == l;

                    if (flag || flag1)
                    {
                        int k1 = j + i1;
                        int l1 = k + j1;
                        this.rand.setSeed((long)(k1 ^ l1) ^ worldIn.getSeed());
                        this.rand.nextInt();

                        if (this.canSpawnStructureAtCoords(k1, l1) && (!p_180706_3_ || !worldIn.isChunkGeneratedAt(k1, l1)))
                        {
                            return new BlockPos((k1 << 4) + 8, 64, (l1 << 4) + 8);
                        }
                    }
                }
            }
        }

        return null;
    }
}
