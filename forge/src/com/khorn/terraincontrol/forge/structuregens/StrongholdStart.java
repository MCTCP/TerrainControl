package com.khorn.terraincontrol.forge.structuregens;

import net.minecraft.world.World;
import net.minecraft.world.gen.structure.ComponentStrongholdStairs2;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureStart;
import net.minecraft.world.gen.structure.StructureStrongholdPieces;

import java.util.ArrayList;
import java.util.Random;

class StrongholdStart extends StructureStart
{
    @SuppressWarnings({"unchecked", "rawtypes"})
    public StrongholdStart(World world, Random random, int par3, int par4)
    {
        StructureStrongholdPieces.prepareStructurePieces();
        ComponentStrongholdStairs2 var5 = new ComponentStrongholdStairs2(0, random, (par3 << 4) + 2, (par4 << 4) + 2);
        this.components.add(var5);
        var5.buildComponent(var5, this.components, random);
        ArrayList var6 = var5.field_75026_c;

        while (!var6.isEmpty())
        {
            int var7 = random.nextInt(var6.size());
            StructureComponent var8 = (StructureComponent) var6.remove(var7);
            var8.buildComponent(var5, this.components, random);
        }

        this.updateBoundingBox();
        this.markAvailableHeight(world, random, 10);
    }
}
