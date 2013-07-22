package com.khorn.terraincontrol.forge.structuregens;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.forge.util.WorldHelper;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.StructureMineshaftStart;
import net.minecraft.world.gen.structure.StructureStart;

public class MineshaftGen extends MapGenStructure
{
    protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ)
    {
        if (rand.nextInt(80) < Math.max(Math.abs(chunkX), Math.abs(chunkZ)))
        {
            LocalWorld world = WorldHelper.toLocalWorld(this.worldObj);
            int biomeId = world.getCalculatedBiomeId(chunkX * 16 + 8, chunkZ * 16 + 8);
            if (rand.nextDouble() * 100.0 < world.getSettings().biomeConfigs[biomeId].mineshaftsRarity)
            {
                return true;
            }
        }

        return false;
    }

    protected StructureStart getStructureStart(int par1, int par2)
    {
        return new StructureMineshaftStart(this.worldObj, this.rand, par1, par2);
    }
}
