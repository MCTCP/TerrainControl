package com.khorn.terraincontrol.forge.structuregens;

import net.minecraft.world.World;
import net.minecraft.world.gen.structure.ComponentVillageRoadPiece;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureStart;
import net.minecraft.world.gen.structure.StructureVillagePieces;

import java.util.ArrayList;
import java.util.Random;

class StructureVillageStart extends StructureStart
{
    /**
     * well ... thats what it does
     */
    private boolean hasMoreThanTwoComponents = false;

    @SuppressWarnings({"rawtypes", "unchecked"})
    public StructureVillageStart(World world, Random random, int chunkX, int chunkZ, int size)
    {
        ArrayList<StructureComponent> villagePieces = StructureVillagePieces.getStructureVillageWeightedPieceList(random, size);
        VillageStartPiece startPiece = new VillageStartPiece(world, 0, random, (chunkX << 4) + 2, (chunkZ << 4) + 2, villagePieces, size);
        this.components.add(startPiece);
        startPiece.buildComponent(startPiece, this.components, random);
        ArrayList var8 = startPiece.field_74930_j;
        ArrayList var9 = startPiece.field_74932_i;
        int var10;

        while (!var8.isEmpty() || !var9.isEmpty())
        {
            StructureComponent var11;

            if (var8.isEmpty())
            {
                var10 = random.nextInt(var9.size());
                var11 = (StructureComponent) var9.remove(var10);
                var11.buildComponent(startPiece, this.components, random);
            } else
            {
                var10 = random.nextInt(var8.size());
                var11 = (StructureComponent) var8.remove(var10);
                var11.buildComponent(startPiece, this.components, random);
            }
        }

        this.updateBoundingBox();
        var10 = 0;

        for (Object component : this.components)
        {
            StructureComponent var12 = (StructureComponent) component;

            if (!(var12 instanceof ComponentVillageRoadPiece))
            {
                ++var10;
            }
        }

        this.hasMoreThanTwoComponents = var10 > 2;
    }

    /**
     * currently only defined for Villages, returns true if Village has more than 2 non-road components
     */
    public boolean isSizeableStructure()
    {
        return this.hasMoreThanTwoComponents;
    }
}
