package com.Khorn.PTMBukkit.Generator.ObjectGens;

import net.minecraft.server.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

class StrongholdStart extends StructureStart
{
    public StrongholdStart(World paramWorld, Random paramRandom, int paramInt1, int paramInt2)
    {
        WorldGenStrongholdPieces.a();

        WorldGenStrongholdStairs2 localWorldGenStrongholdStairs2 = new WorldGenStrongholdStairs2(0, paramRandom, (paramInt1 << 4) + 2, (paramInt2 << 4) + 2);
        this.a.add(localWorldGenStrongholdStairs2);
        localWorldGenStrongholdStairs2.a(localWorldGenStrongholdStairs2, this.a, paramRandom);

        ArrayList localArrayList = localWorldGenStrongholdStairs2.b;
        while (!localArrayList.isEmpty())
        {
            int i = paramRandom.nextInt(localArrayList.size());
            StructurePiece localStructurePiece = (StructurePiece) localArrayList.remove(i);
            localStructurePiece.a(localWorldGenStrongholdStairs2, this.a, paramRandom);
        }

        c();
        a(paramWorld, paramRandom, 10);
    }

}