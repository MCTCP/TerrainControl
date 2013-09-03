package com.khorn.terraincontrol.bukkit.structuregens;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.bukkit.util.WorldHelper;
import net.minecraft.server.v1_6_R2.StructureGenerator;
import net.minecraft.server.v1_6_R2.StructureStart;
import net.minecraft.server.v1_6_R2.World;
import net.minecraft.server.v1_6_R2.WorldGenMineshaftStart;

import java.util.Random;

public class MineshaftGen extends StructureGenerator
{
    // canSpawnStructureAtCoords
    @Override
    protected boolean a(int chunkX, int chunkZ)
    {
        Random rand = b;
        World worldMC = c;
        if (rand.nextInt(80) < Math.max(Math.abs(chunkX), Math.abs(chunkZ)))
        {
            LocalWorld world = WorldHelper.toLocalWorld(worldMC);
            int biomeId = world.getCalculatedBiomeId(chunkX * 16 + 8, chunkZ * 16 + 8);
            if (rand.nextDouble() * 100.0 < world.getSettings().biomeConfigManager.getBiomeConfigs()[biomeId].mineshaftsRarity)
            {
                return true;
            }
        }

        return false;
    }

    @Override
    protected StructureStart b(int i, int j)
    {
        return new WorldGenMineshaftStart(this.c, this.b, i, j);
    }

    // Two methods to help MCPC+ dynamically rename things.
    // It has problems with classes that extend native Minecraft classes
    public void prepare(World world, int chunkX, int chunkZ, byte[] chunkArray)
    {
        a(null, world, chunkX, chunkZ, chunkArray);
    }

    public void place(World world, Random random, int chunkX, int chunkZ)
    {
        a(world, random, chunkX, chunkZ);
    }

}