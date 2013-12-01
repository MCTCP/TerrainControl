package com.khorn.terraincontrol.bukkit.structuregens;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.bukkit.util.WorldHelper;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.BiomeConfig.VillageType;
import net.minecraft.server.v1_7_R1.*;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

public class VillageStart extends StructureStart
{
    // well ... thats what it does
    private boolean hasMoreThanTwoComponents = false;

    @SuppressWarnings("unchecked")
    public VillageStart(World world, Random random, int chunkX, int chunkZ, int size)
    {
        List<StructurePiece> villagePieces = WorldGenVillagePieces.a(random, size);
        
        int startX = (chunkX << 4) + 2;
        int startZ = (chunkZ << 4) + 2;
        WorldGenVillageStartPiece startPiece = new WorldGenVillageStartPiece(world.getWorldChunkManager(), 0, random, startX, startZ, villagePieces, size);

        // Apply the villageType setting
        LocalWorld worldTC = WorldHelper.toLocalWorld(world);
        int currentBiomeId = worldTC.getBiomeId(startX, startZ);
        BiomeConfig config = worldTC.getSettings().biomeConfigs[currentBiomeId];
        if (config != null)
        {
            // Ignore removed custom biomes
            changeToSandstoneVillage(startPiece, config.villageType == VillageType.sandstone);
        }
        
        this.a.add(startPiece);
        startPiece.a(startPiece, this.a, random);
        List<StructurePiece> arraylist1 = startPiece.j;
        List<StructurePiece> arraylist2 = startPiece.i;

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
    
    public VillageStart()
    {
        // Required by Minecraft's structure loading code
    }
    
    /**
     * Just sets the first boolean it can find in the
     * WorldGenVillageStartPiece.class to sandstoneVillage.
     * <p/>
     * @param sandstoneVillage Whether the village should be a sandstone
     *                         village.
     */
    private void changeToSandstoneVillage(WorldGenVillageStartPiece subject, boolean sandstoneVillage)
    {
        for (Field field : WorldGenVillageStartPiece.class.getFields())
        {
            if (field.getType().toString().equals("boolean"))
            {
                try
                {
                    field.setAccessible(true);
                    field.setBoolean(subject, sandstoneVillage);
                    break;
                } catch (Exception e)
                {
                    TerrainControl.log(Level.SEVERE, "Cannot make village a sandstone village!");
                    TerrainControl.printStackTrace(Level.SEVERE, e);
                }
            }
        }
    }

    @Override
    public boolean d()
    {
        return this.hasMoreThanTwoComponents;
    }
}
