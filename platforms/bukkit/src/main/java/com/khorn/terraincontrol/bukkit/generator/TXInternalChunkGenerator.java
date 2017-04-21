package com.khorn.terraincontrol.bukkit.generator;

import com.google.common.base.Preconditions;
import com.khorn.terraincontrol.bukkit.BukkitWorld;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.util.minecraftTypes.StructureNames;
import net.minecraft.server.v1_11_R1.*;
import org.bukkit.craftbukkit.v1_11_R1.generator.CustomChunkGenerator;
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

    public List<BiomeBase.BiomeMeta> getMobsFor(EnumCreatureType type, BlockPosition position)
    {
        WorldConfig worldConfig = this.localWorld.getConfigs().getWorldConfig();
        BiomeBase biomebase = this.localWorld.getWorld().getBiome(position);
        if (type == EnumCreatureType.MONSTER && worldConfig.rareBuildingsEnabled
                && this.localWorld.rareBuildingGen.isWitchHutAt(position))
        {
            return this.localWorld.rareBuildingGen.getWitchHutMobs();
        }

        if (type == EnumCreatureType.MONSTER
                && worldConfig.oceanMonumentsEnabled
                && this.localWorld.oceanMonumentGen.a(this.localWorld.getWorld(), position))
        {
            return this.localWorld.oceanMonumentGen.getMobs();
        }

        return biomebase.getMobs(type);
    }

    @Override
    public BlockPosition findNearestMapFeature(World mcWorld, String type, BlockPosition position, boolean bool)
    {
        WorldConfig worldConfig = this.localWorld.getConfigs().getWorldConfig();

        if (type.equals(StructureNames.MANSION))
        {
            if (worldConfig.mansionsEnabled)
                return this.localWorld.mansionGen.getNearestGeneratedFeature(mcWorld, position, bool);
        } else if (type.equals(StructureNames.MINESHAFT))
        {
            if (worldConfig.mineshaftsEnabled)
                return this.localWorld.mineshaftGen.getNearestGeneratedFeature(mcWorld, position, bool);
        } else if (type.equals(StructureNames.NETHER_FORTRESS))
        {
            if (worldConfig.netherFortressesEnabled)
                return this.localWorld.netherFortressGen.getNearestGeneratedFeature(mcWorld, position, bool);
        } else if (type.equals(StructureNames.OCEAN_MONUMENT))
        {
            if (worldConfig.oceanMonumentsEnabled)
                return this.localWorld.oceanMonumentGen.getNearestGeneratedFeature(mcWorld, position, bool);
        } else if (type.equals(StructureNames.RARE_BUILDING))
        {
            if (worldConfig.rareBuildingsEnabled)
                return this.localWorld.rareBuildingGen.getNearestGeneratedFeature(mcWorld, position, bool);
        } else if (type.equals(StructureNames.STRONGHOLD))
        {
            if (worldConfig.strongholdsEnabled)
                return this.localWorld.strongholdGen.getNearestGeneratedFeature(mcWorld, position, bool);
        } else if (type.equals(StructureNames.VILLAGE))
        {
            if (worldConfig.villagesEnabled)
                return this.localWorld.villageGen.getNearestGeneratedFeature(mcWorld, position, bool);
        }
        return null;
    }

}
