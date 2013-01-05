package com.khorn.terraincontrol.forge.structuregens;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.forge.util.WorldHelper;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.world.biome.SpawnListEntry;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.StructureStart;

import java.util.ArrayList;
import java.util.List;

public class NetherFortressGen extends MapGenStructure
{
    public List<SpawnListEntry> spawnList = new ArrayList<SpawnListEntry>();

    public NetherFortressGen()
    {
        this.spawnList.add(new SpawnListEntry(EntityBlaze.class, 10, 2, 3));
        this.spawnList.add(new SpawnListEntry(EntityPigZombie.class, 5, 4, 4));
        this.spawnList.add(new SpawnListEntry(EntitySkeleton.class, 10, 4, 4));
        this.spawnList.add(new SpawnListEntry(EntityMagmaCube.class, 3, 4, 4));
    }

    @SuppressWarnings({"rawtypes", "UnusedDeclaration"})
    public List getSpawnList()
    {
        return this.spawnList;
    }

    protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ)
    {
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

    protected StructureStart getStructureStart(int chunkX, int chunkZ)
    {
        return new NetherFortressStart(this.worldObj, this.rand, chunkX, chunkZ);
    }
}
