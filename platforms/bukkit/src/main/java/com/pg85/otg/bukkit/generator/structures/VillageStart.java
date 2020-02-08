package com.pg85.otg.bukkit.generator.structures;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;

import com.pg85.otg.OTG;
import com.pg85.otg.bukkit.world.WorldHelper;
import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.biome.BiomeConfig.VillageType;
import com.pg85.otg.logging.LogMarker;

import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.StructurePiece;
import net.minecraft.server.v1_12_R1.StructureStart;
import net.minecraft.server.v1_12_R1.World;
import net.minecraft.server.v1_12_R1.WorldGenVillagePieces;
import net.minecraft.server.v1_12_R1.WorldGenVillagePieces.WorldGenVillagePieceWeight;
import net.minecraft.server.v1_12_R1.WorldGenVillagePieces.WorldGenVillageRoadPiece;
import net.minecraft.server.v1_12_R1.WorldGenVillagePieces.WorldGenVillageStartPiece;

public class VillageStart extends StructureStart
{
    private boolean hasMoreThanTwoComponents = false;

    public VillageStart()
    {
        // Required by Minecraft's structure loading code
    }
    
    VillageStart(World world, Random random, int chunkX, int chunkZ, int size)
    {
        List<WorldGenVillagePieceWeight> villagePieces = WorldGenVillagePieces.a(random, size);

        int startX = (chunkX << 4) + 2;
        int startZ = (chunkZ << 4) + 2;
        WorldGenVillageStartPiece startPiece = new WorldGenVillageStartPiece(world.getWorldChunkManager(), 0, random, startX, startZ, villagePieces, size);

        // Apply the villageType setting
        LocalWorld worldTC = WorldHelper.toLocalWorld(world);
        LocalBiome biome = worldTC.getBiome(startX, startZ);
        if (biome != null)
        {
            // Ignore removed custom biomes
        	// Normal village = 0
            // Desert village = 1
            // Savanna village = 2
        	// Taiga village = 3

        	changeVillageType(startPiece, biome.getBiomeConfig().villageType == VillageType.wood ? 0 : biome.getBiomeConfig().villageType == VillageType.sandstone ? 1 : biome.getBiomeConfig().villageType == VillageType.savanna ? 2 : biome.getBiomeConfig().villageType == VillageType.taiga ? 3 : 0);
        }

        this.a.add(startPiece);
        startPiece.a(startPiece, this.a, random);
        List<StructurePiece> arraylist1 = startPiece.f;
        List<StructurePiece> arraylist2 = startPiece.e;

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

        this.d();
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

    /**
     * Changes a village to a sandstone village. (Just sets the first
     * boolean it can find in the WorldGenVillageStartPiece.class to
     * sandstoneVillage.)
     *
     * @param subject The village.
     * @param sandstoneVillage Whether the village should be a sandstone
     *            village.
     */
    private void changeVillageType(WorldGenVillageStartPiece subject, int villageType)
    {
        Class<?> villageClass = WorldGenVillageStartPiece.class.getSuperclass().getSuperclass();
    	for (Field field : villageClass.getDeclaredFields())
    	{
        	String fieldName = field.getName();
            if (fieldName.equals("structureType") || fieldName.equals("h")) // "h" may have to be updated for newer versions of mc(> 1.10.2), see http://export.mcpbot.bspk.rs/ for obfuscated method/field names.
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

    @Override
    public boolean a()
    {
        return this.hasMoreThanTwoComponents;
    }

    @Override
    public void a(NBTTagCompound nbttagcompound)
    {
        super.a(nbttagcompound);
        nbttagcompound.setBoolean("Valid", this.hasMoreThanTwoComponents);
    }

    @Override
    public void b(NBTTagCompound nbttagcompound)
    {
        super.b(nbttagcompound);
        this.hasMoreThanTwoComponents = nbttagcompound.getBoolean("Valid");
    }
}