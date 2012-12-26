package com.khorn.terraincontrol.bukkit.structuregens;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.server.v1_4_6.StructurePiece;
import net.minecraft.server.v1_4_6.StructureStart;
import net.minecraft.server.v1_4_6.World;
import net.minecraft.server.v1_4_6.WorldGenStrongholdPieces;
import net.minecraft.server.v1_4_6.WorldGenStrongholdStart;

class StrongholdStart extends StructureStart {

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public StrongholdStart(World world, Random random, int i, int j) {
        WorldGenStrongholdPieces.a();
        WorldGenStrongholdStart worldgenstrongholdstart = new WorldGenStrongholdStart(0, random, (i << 4) + 2, (j << 4) + 2);

        this.a.add(worldgenstrongholdstart);
        worldgenstrongholdstart.a(worldgenstrongholdstart, this.a, random);
        ArrayList arraylist = worldgenstrongholdstart.c;

        while (!arraylist.isEmpty()) {
            int k = random.nextInt(arraylist.size());
            StructurePiece structurepiece = (StructurePiece) arraylist.remove(k);

            structurepiece.a((StructurePiece) worldgenstrongholdstart, (List) this.a, random);
        }

        this.c();
        this.a(world, random, 10);
    }
}

