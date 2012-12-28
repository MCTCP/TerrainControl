package com.khorn.terraincontrol.bukkit.structuregens;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import net.minecraft.server.v1_4_6.StructurePiece;
import net.minecraft.server.v1_4_6.StructureStart;
import net.minecraft.server.v1_4_6.World;
import net.minecraft.server.v1_4_6.WorldGenVillagePieces;
import net.minecraft.server.v1_4_6.WorldGenVillageRoadPiece;

class WorldGenVillageStart extends StructureStart
{
    private boolean hasMoreThanTwoComponents = false;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public WorldGenVillageStart(World world, Random random, int chunkX, int chunkZ, int size)
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
                structurepiece.a(worldgenvillagestartpiece, (List) this.a, random);
            } else
            {
                componentCount = random.nextInt(arraylist1.size());
                structurepiece = arraylist1.remove(componentCount);
                structurepiece.a(worldgenvillagestartpiece, (List) this.a, random);
            }
        }

        this.c();
        componentCount = 0;
        Iterator iterator = this.a.iterator();

        while (iterator.hasNext())
        {
            StructurePiece structurepiece1 = (StructurePiece) iterator.next();

            if (!(structurepiece1 instanceof WorldGenVillageRoadPiece))
            {
                ++componentCount;
            }
        }

        this.hasMoreThanTwoComponents = componentCount > 2;
    }

    public boolean d()
    {
        return this.hasMoreThanTwoComponents;
    }
}
