package com.khorn.terraincontrol.bukkit.structuregens;

import net.minecraft.server.v1_5_R2.*;

import java.util.ArrayList;
import java.util.Random;

public class VillageStart extends StructureStart
{
    private boolean hasMoreThanTwoComponents = false;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public VillageStart(World world, Random random, int chunkX, int chunkZ, int size)
    {
        ArrayList listOfPieces = WorldGenVillagePieces.a(random, size);
        VillageStartPiece worldgenvillagestartpiece = new VillageStartPiece(world, 0, random, (chunkX << 4) + 2, (chunkZ << 4) + 2, listOfPieces, size);

        this.a.add(worldgenvillagestartpiece);
        worldgenvillagestartpiece.a(worldgenvillagestartpiece, this.a, random);
        ArrayList<StructurePiece> arraylist1 = worldgenvillagestartpiece.j;
        ArrayList<StructurePiece> arraylist2 = worldgenvillagestartpiece.i;

        int componentCount;

        while (!arraylist1.isEmpty() || !arraylist2.isEmpty())
        {
            StructurePiece structurepiece;

            if (arraylist1.isEmpty())
            {
                componentCount = random.nextInt(arraylist2.size());
                structurepiece = arraylist2.remove(componentCount);
                structurepiece.a(worldgenvillagestartpiece, this.a, random);
            } else
            {
                componentCount = random.nextInt(arraylist1.size());
                structurepiece = arraylist1.remove(componentCount);
                structurepiece.a(worldgenvillagestartpiece, this.a, random);
            }
        }

        this.c();
        componentCount = 0;

        for (Object anA : this.a)
        {
            StructurePiece structurepiece1 = (StructurePiece) anA;

            if (!(structurepiece1 instanceof WorldGenVillageRoadPiece))
            {
                ++componentCount;
            }
        }

        this.hasMoreThanTwoComponents = componentCount > 2;
    }

    @Override
    public boolean d()
    {
        return this.hasMoreThanTwoComponents;
    }
}
