package com.khorn.terraincontrol.bukkit.generator.structures;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.bukkit.BukkitBiome;
import com.khorn.terraincontrol.configuration.WorldSettings;
import com.khorn.terraincontrol.util.minecraftTypes.StructureNames;
import net.minecraft.server.v1_7_R1.*;

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

    public StrongholdGen(WorldSettings configs)
    {
        this.distance = configs.worldConfig.strongholdDistance;
        this.structureCoords = new ChunkCoordIntPair[configs.worldConfig.strongholdCount];
        this.spread = configs.worldConfig.strongholdSpread;

        allowedBiomes = new ArrayList<BiomeBase>();

        for (LocalBiome biome : configs.biomes)
        {
            if (biome == null)
                continue;
            if (biome.getBiomeConfig().strongholdsEnabled)
            {
                allowedBiomes.add(((BukkitBiome) biome).getHandle());
            }
        }
    }

    @Override
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
                double d1 = (1.25D * k + random.nextDouble()) * this.distance * k;
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
                d0 += 6.283185307179586D * k / this.spread;
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

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected List o_()
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

    @Override
    protected StructureStart b(int chunkX, int chunkZ)
    {
        WorldGenStronghold2Start strongholdStart = new WorldGenStronghold2Start(this.c, this.b, chunkX, chunkZ);

        while (strongholdStart.b().isEmpty() || ((WorldGenStrongholdStart) strongholdStart.b().get(0)).b == null)
        {
            strongholdStart = new WorldGenStronghold2Start(this.c, this.b, chunkX, chunkZ);
        }

        return strongholdStart;
    }

    // Two methods to help MCPC+ dynamically rename things.
    // It has problems with classes that extend native Minecraft classes
    public void prepare(World world, int chunkX, int chunkZ)
    {
        a(null, world, chunkX, chunkZ, null);
    }

    public void place(World world, Random random, int chunkX, int chunkZ)
    {
        a(world, random, chunkX, chunkZ);
    }

    @Override
    public String a()
    {
        return StructureNames.STRONGHOLD;
    }
}
