package com.khorn.terraincontrol.forge.generator.structure;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.BiomeConfig.VillageType;
import com.khorn.terraincontrol.forge.util.WorldHelper;
import com.khorn.terraincontrol.logging.LogMarker;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureStart;
import net.minecraft.world.gen.structure.StructureVillagePieces;
import net.minecraft.world.gen.structure.StructureVillagePieces.PieceWeight;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;

public class TXVillageStart extends StructureStart
{
    // well ... thats what it does
    private boolean hasMoreThanTwoComponents = false;

    public TXVillageStart(World world, Random random, int chunkX, int chunkZ, int size)
    {
        List<PieceWeight> villagePieces = StructureVillagePieces.getStructureVillageWeightedPieceList(random, size);

        int startX = (chunkX << 4) + 2;
        int startZ = (chunkZ << 4) + 2;
        StructureVillagePieces.Start startPiece = new StructureVillagePieces.Start(world.getBiomeProvider(), 0, random, startX, startZ, villagePieces, size);

        // Apply the villageType setting
        LocalWorld worldTC = WorldHelper.toLocalWorld(world);
        LocalBiome currentBiome = worldTC.getBiome(startX, startZ);
        BiomeConfig config = currentBiome.getBiomeConfig();
        if (config != null)
        {
            // Ignore removed custom biomes
            changeVillageType(startPiece, config.villageType == VillageType.wood ? 0 : config.villageType == VillageType.sandstone ? 1 : config.villageType == VillageType.savanna ? 2 : config.villageType == VillageType.taiga ? 3 : 0);
        }

        this.components.add(startPiece);
        startPiece.buildComponent(startPiece, this.components, random);
        List<StructureComponent> var8 = startPiece.pendingRoads;
        List<StructureComponent> var9 = startPiece.pendingHouses;
        int var10;

        while (!var8.isEmpty() || !var9.isEmpty())
        {
            StructureComponent var11;

            if (var8.isEmpty())
            {
                var10 = random.nextInt(var9.size());
                var11 = var9.remove(var10);
                var11.buildComponent(startPiece, this.components, random);
            } else
            {
                var10 = random.nextInt(var8.size());
                var11 = var8.remove(var10);
                var11.buildComponent(startPiece, this.components, random);
            }
        }

        this.updateBoundingBox();
        var10 = 0;

        for (Object component : this.components)
        {
            StructureComponent var12 = (StructureComponent) component;

            if (!(var12 instanceof StructureVillagePieces.Road))
            {
                ++var10;
            }
        }

        this.hasMoreThanTwoComponents = var10 > 2;
    }

    /**
     * Just sets the first boolean it can find in the
     * WorldGenVillageStartPiece.class to sandstoneVillage.
     *
     * @param subject          The village.
     * @param sandstoneVillage Whether the village should be a sandstone
     *                         village.
     */
    private void changeVillageType(StructureVillagePieces.Start subject, int villageType)
    {
        for (Field field : net.minecraft.world.gen.structure.StructureVillagePieces.Village.class.getDeclaredFields())
        {
            String fieldName = field.getName();
            if (fieldName.equals("structureType") || fieldName.equals("field_189928_h")) // field_189928_h may have to be updated for newer versions of mc/forge (> 1.10.2), see http://export.mcpbot.bspk.rs/ for obfuscated method/field names.
            {
                try
                {
                    field.setAccessible(true);
                    field.setInt(subject, villageType);
                    break;
                } catch (Exception e)
                {
                    TerrainControl.log(LogMarker.FATAL, "Cannot set specified village type!");
                    TerrainControl.printStackTrace(LogMarker.FATAL, e);
                }
            }
        }
    }

    /**
     * currently only defined for Villages, returns true if Village has more than 2 non-road components
     */
    @Override
    public boolean isSizeableStructure()
    {
        return this.hasMoreThanTwoComponents;
    }

    public TXVillageStart()
    {
        // Required by Minecraft's structure loading code
    }
}
