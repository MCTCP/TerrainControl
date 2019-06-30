package com.pg85.otg.forge.generator.structure;

import it.unimi.dsi.fastutil.objects.ObjectIterator;

import java.util.Random;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.StructureStart;

public abstract class OTGMapGenStructure extends MapGenStructure
{
    public synchronized boolean chunkHasStructure(World worldIn, Random randomIn, ChunkPos chunkCoord)
    {
        this.initializeStructureData(worldIn);
        int i = (chunkCoord.x << 4) + 8;
        int j = (chunkCoord.z << 4) + 8;

        ObjectIterator<?> objectiterator = this.structureMap.values().iterator();

        while (objectiterator.hasNext())
        {
            StructureStart structurestart = (StructureStart)objectiterator.next();

            if (structurestart.isSizeableStructure() && structurestart.isValidForPostProcess(chunkCoord) && structurestart.getBoundingBox().intersectsWith(i, j, i + 15, j + 15))
            {
                return true;
            }
        }

        return false;
    }
}
