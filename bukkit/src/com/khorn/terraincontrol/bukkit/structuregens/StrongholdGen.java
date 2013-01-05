package com.khorn.terraincontrol.bukkit.structuregens;

import com.khorn.terraincontrol.bukkit.BukkitBiome;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.WorldConfig;
import net.minecraft.server.v1_4_6.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class StrongholdGen extends StructureGenerator
{

    private List<BiomeBase> allowedBiomes;
    private boolean ranBiomeCheck;
    private ChunkCoordIntPair[] structureCoords;
    private double distance;
    private int spread;

    public StrongholdGen(WorldConfig worldConfig)
    {
        this.distance = worldConfig.strongholdDistance;
        this.structureCoords = new ChunkCoordIntPair[worldConfig.strongholdCount];
        this.spread = worldConfig.strongholdSpread;

        allowedBiomes = new ArrayList<BiomeBase>();

        for (BiomeConfig biomeConfig : worldConfig.biomeConfigs)
        {
            if (biomeConfig == null)
                continue;
            if (biomeConfig.strongholdsEnabled)
            {
                allowedBiomes.add(((BukkitBiome) biomeConfig.Biome).getHandle());
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected boolean a(int i, int j)
    {
        if (!this.ranBiomeCheck)
        {
            Random random = new Random();

            random.setSeed(this.c.getSeed());
            double d0 = random.nextDouble() * 3.141592653589793D * 2.0D;
            int k = 1;

            for (int l = 0; l < this.structureCoords.length; ++l)
            {
                double d1 = (1.25D * (double) k + random.nextDouble()) * this.distance * (double) k;
                int i1 = (int) Math.round(Math.cos(d0) * d1);
                int j1 = (int) Math.round(Math.sin(d0) * d1);
                ArrayList arraylist = new ArrayList();

                Collections.addAll(arraylist, this.allowedBiomes);
                ChunkPosition chunkposition = this.c.getWorldChunkManager().a((i1 << 4) + 8, (j1 << 4) + 8, 112, arraylist, random);

                if (chunkposition != null)
                {
                    i1 = chunkposition.x >> 4;
                    j1 = chunkposition.z >> 4;
                }

                this.structureCoords[l] = new ChunkCoordIntPair(i1, j1);
                d0 += 6.283185307179586D * (double) k / (double) this.spread;
                if (l == this.spread)
                {
                    k += 2 + random.nextInt(5);
                    this.spread += 1 + random.nextInt(2);
                }
            }

            this.ranBiomeCheck = true;
        }

        ChunkCoordIntPair[] achunkcoordintpair = this.structureCoords;

        for (ChunkCoordIntPair chunkcoordintpair : achunkcoordintpair)
        {
            if (i == chunkcoordintpair.x && j == chunkcoordintpair.z)
            {
                return true;
            }
        }

        return false;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected List p_()
    {
        ArrayList arraylist = new ArrayList();
        ChunkCoordIntPair[] achunkcoordintpair = this.structureCoords;

        for (ChunkCoordIntPair chunkcoordintpair : achunkcoordintpair)
        {
            if (chunkcoordintpair != null)
            {
                arraylist.add(chunkcoordintpair.a(64));
            }
        }

        return arraylist;
    }

    protected StructureStart b(int i, int j)
    {
        StrongholdStart worldgenstronghold2start = new StrongholdStart(this.c, this.b, i, j);

        while (worldgenstronghold2start.b().isEmpty() || ((WorldGenStrongholdStart) worldgenstronghold2start.b().get(0)).b == null)
        {
            worldgenstronghold2start = new StrongholdStart(this.c, this.b, i, j);
        }

        return worldgenstronghold2start;
    }
}
