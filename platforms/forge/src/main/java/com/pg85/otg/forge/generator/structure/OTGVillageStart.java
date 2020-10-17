package com.pg85.otg.forge.generator.structure;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.configuration.biome.BiomeConfig.VillageType;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.forge.ForgeEngine;

import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureStart;
import net.minecraft.world.gen.structure.StructureVillagePieces;
import net.minecraft.world.gen.structure.StructureVillagePieces.PieceWeight;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;

public class OTGVillageStart extends StructureStart
{
    // well ... thats what it does
    private boolean hasMoreThanTwoComponents = false;

    public OTGVillageStart()
    {
        // Required by Minecraft's structure loading code
    }
    
    // TODO: Extra large villages aren't working?
    OTGVillageStart(World world, Random random, int chunkX, int chunkZ, int size)
    {
        List<PieceWeight> villagePieces = StructureVillagePieces.getStructureVillageWeightedPieceList(random, size);

        int startX = (chunkX << 4) + 2;
        int startZ = (chunkZ << 4) + 2;
        StructureVillagePieces.Start startPiece = new StructureVillagePieces.Start(world.getBiomeProvider(), 0, random, startX, startZ, villagePieces, size);
        
        // Apply the villageType setting
        LocalWorld worldTC = ((ForgeEngine)OTG.getEngine()).getWorld(world);                
        LocalBiome currentBiome = worldTC.getBiome(startX, startZ);
        BiomeConfig config = currentBiome.getBiomeConfig();
        if (config != null)
        {
            // Ignore removed custom biomes
        	// Normal village = 0
            // Desert village = 1
            // Savanna village = 2
        	// Taiga village = 3
        	
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
                }
                catch (Exception e)
                {
                    OTG.log(LogMarker.FATAL, "Cannot make village a sandstone village!");
                    OTG.printStackTrace(LogMarker.FATAL, e);
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
}
