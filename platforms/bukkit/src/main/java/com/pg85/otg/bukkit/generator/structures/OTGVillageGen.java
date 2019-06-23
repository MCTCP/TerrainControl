package com.pg85.otg.bukkit.generator.structures;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.pg85.otg.OTG;
import com.pg85.otg.bukkit.BukkitBiome;
import com.pg85.otg.bukkit.util.WorldHelper;
import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.biome.BiomeConfig.VillageType;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.network.ServerConfigProvider;
import com.pg85.otg.util.minecraftTypes.StructureNames;

import net.minecraft.server.v1_12_R1.BiomeBase;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.StructureGenerator;
import net.minecraft.server.v1_12_R1.StructurePiece;
import net.minecraft.server.v1_12_R1.StructureStart;
import net.minecraft.server.v1_12_R1.World;
import net.minecraft.server.v1_12_R1.WorldGenVillagePieces;
import net.minecraft.server.v1_12_R1.WorldGenVillagePieces.WorldGenVillagePieceWeight;
import net.minecraft.server.v1_12_R1.WorldGenVillagePieces.WorldGenVillageRoadPiece;
import net.minecraft.server.v1_12_R1.WorldGenVillagePieces.WorldGenVillageStartPiece;

public class OTGVillageGen extends StructureGenerator
{

    /**
     * A list of all the biomes villages can spawn in.
     */
    public List<BiomeBase> villageSpawnBiomes;

    /**
     * Village size, 0 for normal, 1 for flat map
     */
    private int size;
    private int distance;
    private int minimumDistance;

    public OTGVillageGen(ServerConfigProvider configs)
    {
        size = configs.getWorldConfig().villageSize;
        distance = configs.getWorldConfig().villageDistance;
        minimumDistance = 8;

        // Add all village biomes to the list
        villageSpawnBiomes = new ArrayList<BiomeBase>();
        for (LocalBiome biome : configs.getBiomeArrayByOTGId())
        {
            if (biome == null)
                continue;
            if (biome.getBiomeConfig().villageType != VillageType.disabled)
            {
                villageSpawnBiomes.add(((BukkitBiome) biome).getHandle());
            }
        }
    }


    public BlockPosition getNearestGeneratedFeature(World world, BlockPosition blockposition, boolean flag)
    {
        this.g = world;
        return a(world, this, blockposition, this.distance, this.minimumDistance, 10387312, false, 100, flag);
    }

    @Override
    protected boolean a(int chunkX, int chunkZ)
    {
        int k = chunkX;
        int l = chunkZ;

        if (chunkX < 0)
        {
            chunkX -= this.distance - 1;
        }

        if (chunkZ < 0)
        {
            chunkZ -= this.distance - 1;
        }

        int i1 = chunkX / this.distance;
        int j1 = chunkZ / this.distance;
        Random random = this.g.a(i1, j1, 10387312);

        i1 *= this.distance;
        j1 *= this.distance;
        i1 += random.nextInt(this.distance - this.minimumDistance);
        j1 += random.nextInt(this.distance - this.minimumDistance);
        if (k == i1 && l == j1)
        {
            boolean flag = this.g.getWorldChunkManager().a(k * 16 + 8, l * 16 + 8, 0, villageSpawnBiomes);

            if (flag)
            {
                return true;
            }
        }

        return false;
    }

    @Override
    protected StructureStart b(int chunkX, int chunkZ)
    {
        return new VillageStart(this.g, this.f, chunkX, chunkZ, this.size);
    }

    @Override
    public String a()
    {
        return StructureNames.VILLAGE;
    }

    public static class VillageStart extends StructureStart
    {
        // well ... thats what it does
        private boolean hasMoreThanTwoComponents = false;

        public VillageStart(World world, Random random, int chunkX, int chunkZ, int size)
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

        public VillageStart()
        {
            // Required by Minecraft's structure loading code
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
            Class villageClass = WorldGenVillageStartPiece.class.getSuperclass().getSuperclass();
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
}
