package com.khorn.terraincontrol.forge.structuregens;

import net.minecraft.world.World;
import net.minecraft.world.gen.structure.ComponentNetherBridgeStartPiece;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureStart;

import java.util.ArrayList;
import java.util.Random;

public class NetherFortressStart extends StructureStart
{

    @SuppressWarnings({"unchecked", "rawtypes"})
    public NetherFortressStart(World world, Random random, int chunkX, int chunkZ)
    {
        ComponentNetherBridgeStartPiece var5 = new ComponentNetherBridgeStartPiece(random, (chunkX << 4) + 2, (chunkZ << 4) + 2);
        this.components.add(var5);
        var5.buildComponent(var5, this.components, random);
        ArrayList list = var5.field_74967_d;

        while (!list.isEmpty())
        {
            int var7 = random.nextInt(list.size());
            StructureComponent var8 = (StructureComponent) list.remove(var7);
            var8.buildComponent(var5, this.components, random);
        }

        this.updateBoundingBox();
        this.setRandomHeight(world, random, 48, 70);
    }

}
