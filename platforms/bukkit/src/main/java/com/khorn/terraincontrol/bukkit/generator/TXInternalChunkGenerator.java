package com.khorn.terraincontrol.bukkit.generator;

import com.google.common.base.Preconditions;
import com.khorn.terraincontrol.bukkit.BukkitWorld;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.util.minecraftTypes.StructureNames;
import net.minecraft.server.v1_12_R1.BiomeBase;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.EnumCreatureType;
import net.minecraft.server.v1_12_R1.World;
import org.bukkit.craftbukkit.v1_12_R1.generator.CustomChunkGenerator;
import org.bukkit.generator.ChunkGenerator;

import java.util.List;

public class TXInternalChunkGenerator extends CustomChunkGenerator
{

    private final BukkitWorld localWorld;

    public TXInternalChunkGenerator(BukkitWorld world, ChunkGenerator generator)
    {
        super(world.getWorld(), world.getSeed(), generator);
        Preconditions.checkArgument(generator instanceof TXChunkGenerator, "Generator must be of the plugin");

        this.localWorld = world;
    }

    @Override
    public List<BiomeBase.BiomeMeta> getMobsFor(EnumCreatureType type, BlockPosition position)
    {
        WorldConfig worldConfig = localWorld.getConfigs().getWorldConfig();
        BiomeBase biomebase = localWorld.getWorld().getBiome(position);
        if (type == EnumCreatureType.MONSTER
                && worldConfig.rareBuildingsEnabled
                && localWorld.rareBuildingGen.isWitchHutAt(position))
        {
            return localWorld.rareBuildingGen.getWitchHutMobs();
        }

        if (type == EnumCreatureType.MONSTER
                && worldConfig.oceanMonumentsEnabled
                && localWorld.oceanMonumentGen.a(localWorld.getWorld(), position))
        {
            return localWorld.oceanMonumentGen.getMobs();
        }

        return biomebase.getMobs(type);
    }

    @Override
    public BlockPosition findNearestMapFeature(World mcWorld, String type, BlockPosition position, boolean bool)
    {
        WorldConfig worldConfig = localWorld.getConfigs().getWorldConfig();

        if (type.equals(StructureNames.MANSION))
        {
            if (worldConfig.mansionsEnabled)
                return localWorld.mansionGen.getNearestGeneratedFeature(mcWorld, position, bool);
        } else if (type.equals(StructureNames.MINESHAFT))
        {
            if (worldConfig.mineshaftsEnabled)
                return localWorld.mineshaftGen.getNearestGeneratedFeature(mcWorld, position, bool);
        } else if (type.equals(StructureNames.NETHER_FORTRESS))
        {
            if (worldConfig.netherFortressesEnabled)
                return localWorld.netherFortressGen.getNearestGeneratedFeature(mcWorld, position, bool);
        } else if (type.equals(StructureNames.OCEAN_MONUMENT))
        {
            if (worldConfig.oceanMonumentsEnabled)
                return localWorld.oceanMonumentGen.getNearestGeneratedFeature(mcWorld, position, bool);
        } else if (type.equals(StructureNames.RARE_BUILDING))
        {
            if (worldConfig.rareBuildingsEnabled)
                return localWorld.rareBuildingGen.getNearestGeneratedFeature(mcWorld, position, bool);
        } else if (type.equals(StructureNames.STRONGHOLD))
        {
            if (worldConfig.strongholdsEnabled)
                return localWorld.strongholdGen.getNearestGeneratedFeature(mcWorld, position, bool);
        } else if (type.equals(StructureNames.VILLAGE))
        {
            if (worldConfig.villagesEnabled)
                return localWorld.villageGen.getNearestGeneratedFeature(mcWorld, position, bool);
        }
        return null;
    }

}
