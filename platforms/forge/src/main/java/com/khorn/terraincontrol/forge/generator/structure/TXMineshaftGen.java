package com.khorn.terraincontrol.forge.generator.structure;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.BiomeConfig.MineshaftType;
import com.khorn.terraincontrol.forge.util.WorldHelper;
import com.khorn.terraincontrol.util.ChunkCoordinate;
import com.khorn.terraincontrol.util.minecraftTypes.StructureNames;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.MapGenMineshaft;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.StructureMineshaftStart;
import net.minecraft.world.gen.structure.StructureStart;

public class TXMineshaftGen extends MapGenStructure
{
    @Override
    protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ)
    {
        if (this.rand.nextInt(80) < Math.max(Math.abs(chunkX), Math.abs(chunkZ)))
        {
            LocalWorld world = WorldHelper.toLocalWorld(this.world);
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
        LocalWorld world = WorldHelper.toLocalWorld(this.world);
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
    public BlockPos getClosestStrongholdPos(World worldIn, BlockPos pos, boolean p_180706_3_)
    {
        // int i = 1000;
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
