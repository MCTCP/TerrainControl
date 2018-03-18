package com.pg85.otg.forge.generator.structure;

import com.pg85.otg.LocalBiome;
import com.pg85.otg.LocalWorld;
import com.pg85.otg.OTG;
import com.pg85.otg.configuration.BiomeConfig;
import com.pg85.otg.configuration.BiomeConfig.MineshaftType;
import com.pg85.otg.forge.ForgeEngine;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.minecraftTypes.StructureNames;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.MapGenMineshaft;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.StructureMineshaftStart;
import net.minecraft.world.gen.structure.StructureStart;

public class OTGMineshaftGen extends OTGMapGenStructure
{
    @Override
    protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ)
    {
        if (this.rand.nextInt(80) < Math.max(Math.abs(chunkX), Math.abs(chunkZ)))
        {
            LocalWorld world = ((ForgeEngine)OTG.getEngine()).getWorld(this.world);
            if(world == null)
            {
            	world = ((ForgeEngine)OTG.getEngine()).getWorld(this.world);
            	throw new RuntimeException("Whatever it is you're trying to do, we didn't write any code for it (sorry). Please contact Team OTG about this crash.");
            }
            LocalBiome biome = world.getBiome(chunkX * 16 + 8, chunkZ * 16 + 8);
            BiomeConfig biomeConfig = biome.getBiomeConfig();
            if (biomeConfig.mineshaftType == MineshaftType.disabled)
            {
                return false;
            }
            if (this.rand.nextDouble() * 100.0 < biomeConfig.mineshaftsRarity)
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
