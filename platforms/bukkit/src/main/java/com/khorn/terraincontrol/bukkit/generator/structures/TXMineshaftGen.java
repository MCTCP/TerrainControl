package com.khorn.terraincontrol.bukkit.generator.structures;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.bukkit.util.WorldHelper;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.BiomeConfig.MineshaftType;
import com.khorn.terraincontrol.util.ChunkCoordinate;
import com.khorn.terraincontrol.util.minecraftTypes.StructureNames;
import net.minecraft.server.v1_12_R1.*;

import java.util.Random;

public class TXMineshaftGen extends StructureGenerator
{
    // canSpawnStructureAtCoords
    @Override
    protected boolean a(int chunkX, int chunkZ)
    {
        Random rand = this.f;
        World worldMC = this.g;
        if (rand.nextInt(80) < Math.max(Math.abs(chunkX), Math.abs(chunkZ)))
        {
            LocalWorld world = WorldHelper.toLocalWorld(worldMC);
            LocalBiome biome = world.getBiome(chunkX * 16 + 8, chunkZ * 16 + 8);
            BiomeConfig biomeConfig = biome.getBiomeConfig();
            if (biomeConfig.mineshaftType == MineshaftType.disabled)
            {
                return false;
            }
            if (rand.nextDouble() * 100.0 < biomeConfig.mineshaftsRarity)
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public BlockPosition getNearestGeneratedFeature(World var1, BlockPosition var2, boolean var3)
    {
        int var5 = var2.getX() >> 4;
        int var6 = var2.getZ() >> 4;

        for (int var7 = 0; var7 <= 1000; ++var7)
        {
            for (int var8 = -var7; var8 <= var7; ++var8)
            {
                boolean var9 = var8 == -var7 || var8 == var7;

                for (int var10 = -var7; var10 <= var7; ++var10)
                {
                    boolean var11 = var10 == -var7 || var10 == var7;
                    if (var9 || var11)
                    {
                        int var12 = var5 + var8;
                        int var13 = var6 + var10;
                        this.f.setSeed(var12 ^ var13 ^ var1.getSeed());
                        this.f.nextInt();
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
    protected StructureStart b(int chunkX, int chunkZ)
    {
        LocalWorld world = WorldHelper.toLocalWorld(this.g);
        LocalBiome biome = world.getBiome(chunkX * ChunkCoordinate.CHUNK_X_SIZE + 8,
                chunkZ * ChunkCoordinate.CHUNK_Z_SIZE + 8);
        BiomeConfig biomeConfig = biome.getBiomeConfig();
        WorldGenMineshaft.Type mineshaftType = WorldGenMineshaft.Type.NORMAL;
        if (biomeConfig.mineshaftType == MineshaftType.mesa)
        {
            mineshaftType = WorldGenMineshaft.Type.MESA;
        }

        return new WorldGenMineshaftStart(this.g, this.f, chunkX, chunkZ, mineshaftType);
    }

    @Override
    public String a()
    {
        return StructureNames.MINESHAFT;
    }

}