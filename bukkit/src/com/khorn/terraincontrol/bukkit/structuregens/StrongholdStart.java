package com.khorn.terraincontrol.bukkit.structuregens;

import net.minecraft.server.v1_5_R3.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

public class StrongholdStart extends StructureStart
{

    @SuppressWarnings({"unchecked", "rawtypes"})
    public StrongholdStart(World world, Random random, int i, int j)
    {
        WorldGenStrongholdPieces.a();
        WorldGenStrongholdStart worldgenstrongholdstart = new WorldGenStrongholdStart(0, random, (i << 4) + 2, (j << 4) + 2);

        this.a.add(worldgenstrongholdstart);
        worldgenstrongholdstart.a(worldgenstrongholdstart, this.a, random);
        ArrayList arraylist = worldgenstrongholdstart.c;

        while (!arraylist.isEmpty())
        {
            int k = random.nextInt(arraylist.size());
            StructurePiece structurepiece = (StructurePiece) arraylist.remove(k);

            structurepiece.a(worldgenstrongholdstart, this.a, random);
        }

        this.c();
        this.a(world, random, 10);
    }

    // One method to help MCPC+ dynamically rename things.
    // It has problems with classes that extend native Minecraft classes
    @SuppressWarnings("unchecked")
    public LinkedList<WorldGenStrongholdStart> getComponents()
    {
        return b();
    }
}
