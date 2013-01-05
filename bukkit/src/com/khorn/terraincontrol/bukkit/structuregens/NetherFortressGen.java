package com.khorn.terraincontrol.bukkit.structuregens;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.bukkit.util.WorldHelper;
import net.minecraft.server.v1_4_6.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NetherFortressGen extends StructureGenerator
{

    public List<BiomeMeta> spawnList = new ArrayList<BiomeMeta>();

    public NetherFortressGen()
    {
        this.spawnList.add(new BiomeMeta(EntityBlaze.class, 10, 2, 3));
        this.spawnList.add(new BiomeMeta(EntityPigZombie.class, 5, 4, 4));
        this.spawnList.add(new BiomeMeta(EntitySkeleton.class, 10, 4, 4));
        this.spawnList.add(new BiomeMeta(EntityMagmaCube.class, 3, 4, 4));
    }

    @SuppressWarnings("rawtypes")
    public List a()
    {
        return this.spawnList;
    }

    // canSpawnAtChunkCoords
    protected boolean a(int chunkX, int chunkZ)
    {
        Random rand = this.b;
        World worldObj = this.c;

        int var3 = chunkX >> 4;
        int var4 = chunkZ >> 4;
        rand.setSeed((long) (var3 ^ var4 << 4) ^ worldObj.getSeed());
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
                int biomeId = world.getCalculatedBiomeId(chunkX * 16 + 8, chunkZ * 16 + 8);
                if (!world.getSettings().biomeConfigs[biomeId].netherFortressesEnabled)
                {
                    return false;
                }
                return (chunkZ == (var4 << 4) + 4 + rand.nextInt(8));
            }
        }
    }

    protected StructureStart b(int i, int j)
    {
        return new NetherFortressStart(this.c, this.b, i, j);
    }
}
