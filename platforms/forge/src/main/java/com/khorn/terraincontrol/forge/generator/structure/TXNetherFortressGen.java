package com.khorn.terraincontrol.forge.generator.structure;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.forge.util.WorldHelper;
import com.khorn.terraincontrol.util.minecraftTypes.StructureNames;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntitySkeleton;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraft.world.gen.structure.MapGenNetherBridge;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.StructureStart;

import java.util.ArrayList;
import java.util.List;

public class TXNetherFortressGen extends MapGenStructure
{
    public List<SpawnListEntry> spawnList = new ArrayList<SpawnListEntry>();

    public TXNetherFortressGen()
    {
        this.spawnList.add(new SpawnListEntry(EntityBlaze.class, 10, 2, 3));
        this.spawnList.add(new SpawnListEntry(EntityPigZombie.class, 5, 4, 4));
        this.spawnList.add(new SpawnListEntry(EntitySkeleton.class, 10, 4, 4));
        this.spawnList.add(new SpawnListEntry(EntityMagmaCube.class, 3, 4, 4));
    }

    public List<SpawnListEntry> getSpawnList()
    {
        return this.spawnList;
    }

    @Override
    protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ)
    {
        int var3 = chunkX >> 4;
        int var4 = chunkZ >> 4;
        this.rand.setSeed((long) (var3 ^ var4 << 4) ^ this.world.getSeed());
        this.rand.nextInt();

        if (this.rand.nextInt(3) != 0)
        {
            return false;
        } else
        {
            if (chunkX != (var3 << 4) + 4 + this.rand.nextInt(8))
            {
                return false;
            } else
            {
                LocalWorld world = WorldHelper.toLocalWorld(this.world);
                LocalBiome biome = world.getBiome(chunkX * 16 + 8, chunkZ * 16 + 8);
                if (!biome.getBiomeConfig().netherFortressesEnabled)
                {
                    return false;
                }
                return (chunkZ == (var4 << 4) + 4 + this.rand.nextInt(8));
            }
        }
    }

    @Override
    protected StructureStart getStructureStart(int chunkX, int chunkZ)
    {
        return new MapGenNetherBridge.Start(this.world, this.rand, chunkX, chunkZ);
    }

    @Override
    public String getStructureName()
    {
        return StructureNames.NETHER_FORTRESS;
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
