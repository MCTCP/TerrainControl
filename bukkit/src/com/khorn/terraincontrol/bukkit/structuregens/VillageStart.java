package com.khorn.terraincontrol.bukkit.structuregens;

import net.minecraft.server.v1_6_R1.*;

import java.util.List;
import java.util.Random;

public class VillageStart extends StructureStart
{
    // well ... thats what it does
    private boolean hasMoreThanTwoComponents = false;

    @SuppressWarnings("unchecked")
    public VillageStart(World world, Random random, int chunkX, int chunkZ, int size)
    {
        List<StructurePiece> villagePieces = WorldGenVillagePieces.a(random, size);
        VillageStartPiece startPiece = new VillageStartPiece(world, 0, random, (chunkX << 4) + 2, (chunkZ << 4) + 2, villagePieces, size);

        this.a.add(startPiece);
        startPiece.buildComponent(startPiece, this.a, random);
        List<StructurePiece> arraylist1 = startPiece.getPiecesListJ();
        List<StructurePiece> arraylist2 = startPiece.getPiecesListI();

        int componentCount;

        while (!arraylist1.isEmpty() || !arraylist2.isEmpty())
        {
            StructurePiece structurepiece;

            if (arraylist1.isEmpty())
            {
                componentCount = random.nextInt(arraylist2.size());
                structurepiece = arraylist2.remove(componentCount);
                structurepiece.a(startPiece, this.a, random);
            } else
            {
                componentCount = random.nextInt(arraylist1.size());
                structurepiece = arraylist1.remove(componentCount);
                structurepiece.a(startPiece, this.a, random);
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
