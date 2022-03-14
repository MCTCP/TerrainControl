package com.khorn.terraincontrol.bukkit.generator.structures;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.bukkit.util.WorldHelper;
import com.khorn.terraincontrol.util.minecraftTypes.StructureNames;
import net.minecraft.server.v1_12_R1.*;
import net.minecraft.server.v1_12_R1.BiomeBase.BiomeMeta;
import net.minecraft.server.v1_12_R1.WorldGenNether.WorldGenNetherStart;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TXNetherFortressGen extends StructureGenerator
{

    public List<BiomeMeta> spawnList = new ArrayList<BiomeMeta>();

    public TXNetherFortressGen()
    {
        this.spawnList.add(new BiomeMeta(EntityBlaze.class, 10, 2, 3));
        this.spawnList.add(new BiomeMeta(EntityPigZombie.class, 5, 4, 4));
        this.spawnList.add(new BiomeMeta(EntitySkeleton.class, 10, 4, 4));
        this.spawnList.add(new BiomeMeta(EntityMagmaCube.class, 3, 4, 4));
    }

    public List<BiomeMeta> b()
    {
        return this.spawnList;
    }

    // canSpawnAtChunkCoords
    @Override
    protected boolean a(int chunkX, int chunkZ)
    {
        Random rand = this.f;
        World worldObj = this.g;

        int var3 = chunkX >> 4;
        int var4 = chunkZ >> 4;
        rand.setSeed(var3 ^ var4 << 4 ^ worldObj.getSeed());
        rand.nextInt();

        if (rand.nextInt(3) != 0)
        {
            return false;
        } else
        {
            if (chunkX != (var3 << 4) + 4 + rand.nextInt(8))
            {
                return false;
            } else
            {
                LocalWorld world = WorldHelper.toLocalWorld(worldObj);
                LocalBiome biome = world.getBiome(chunkX * 16 + 8, chunkZ * 16 + 8);
                if (!biome.getBiomeConfig().netherFortressesEnabled)
                {
                    return false;
                }
                return (chunkZ == (var4 << 4) + 4 + rand.nextInt(8));
            }
        }
    }

    @Override
    public BlockPosition getNearestGeneratedFeature(World var1, BlockPosition var2, boolean var3)
    {
        int var5 = var2.getX() >> 4;
        int var6 = var2.getZ() >> 4;

        for(int var7 = 0; var7 <= 1000; ++var7)
        {
            for(int var8 = -var7; var8 <= var7; ++var8)
            {
                boolean var9 = var8 == -var7 || var8 == var7;

                for (int var10 = -var7; var10 <= var7; ++var10) {
                    boolean var11 = var10 == -var7 || var10 == var7;
                    if (var9 || var11)
                    {
                        int var12 = var5 + var8;
                        int var13 = var6 + var10;
                        if (this.a(var12, var13) && (!var3 || !var1.b(var12, var13)))
                        {
                            return new BlockPosition((var12 << 4) + 8, 64, (var13 << 4) + 8);
                        }
                    }
                }
            }
        }

        return null;
    }

    @Override
    protected StructureStart b(int i, int j)
    {
        return new WorldGenNetherStart(this.g, this.f, i, j);
    }

    @Override
    public String a()
    {
        return StructureNames.NETHER_FORTRESS;
    }
}
