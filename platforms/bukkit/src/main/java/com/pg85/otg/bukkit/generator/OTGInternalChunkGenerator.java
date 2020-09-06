package com.pg85.otg.bukkit.generator;

import java.util.List;

import com.google.common.base.Preconditions;
import com.pg85.otg.bukkit.world.BukkitWorld;
import com.pg85.otg.configuration.world.WorldConfig;
import com.pg85.otg.util.minecraft.defaults.StructureNames;

import net.minecraft.server.v1_12_R1.BiomeBase;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.EnumCreatureType;
import net.minecraft.server.v1_12_R1.World;

import org.bukkit.craftbukkit.v1_12_R1.generator.CustomChunkGenerator;
import org.bukkit.generator.ChunkGenerator;

public class OTGInternalChunkGenerator extends CustomChunkGenerator
{
    private final BukkitWorld localWorld;

    public OTGInternalChunkGenerator(BukkitWorld world, ChunkGenerator generator)
    {
        super(world.getWorld(), world.getSeed(), generator);
        Preconditions.checkArgument(generator instanceof OTGChunkGenerator, "Generator must be of the plugin");

        this.localWorld = world;
    }
    
	public List<BiomeBase.BiomeMeta> getMobsFor(EnumCreatureType type, BlockPosition position)
	{
		WorldConfig worldConfig = localWorld.getConfigs().getWorldConfig();
		BiomeBase biomebase = localWorld.getWorld().getBiome(position);
		if (type == EnumCreatureType.MONSTER && worldConfig.rareBuildingsEnabled && localWorld.rareBuildingGen.isWitchHutAt(position))
		{
			return localWorld.rareBuildingGen.getWitchHutMobs();
		}
		
		if (type == EnumCreatureType.MONSTER && worldConfig.oceanMonumentsEnabled && localWorld.oceanMonumentGen.a(localWorld.getWorld(), position))
		{
			return localWorld.oceanMonumentGen.getMobs();
		}
		return biomebase.getMobs(type);
	}    

    @Override
    public BlockPosition findNearestMapFeature(World mcWorld, String type, BlockPosition position, boolean bool)
    {
        WorldConfig worldConfig = localWorld.getConfigs().getWorldConfig();

        if (type.equals(StructureNames.WOODLAND_MANSION))
        {
            if (worldConfig.woodLandMansionsEnabled)
            {
                return localWorld.woodLandMansionGen.getNearestGeneratedFeature(mcWorld, position, bool);
            }
        }
        else if (type.equals(StructureNames.MINESHAFT))
        {
            if (worldConfig.mineshaftsEnabled)
            {
                return localWorld.mineshaftGen.getNearestGeneratedFeature(mcWorld, position, bool);
            }
        }
        else if (type.equals(StructureNames.NETHER_FORTRESS))
        {
            if (worldConfig.netherFortressesEnabled)
            {
                return localWorld.netherFortressGen.getNearestGeneratedFeature(mcWorld, position, bool);
            }
        }
        else if (type.equals(StructureNames.OCEAN_MONUMENT))
        {
            if (worldConfig.oceanMonumentsEnabled)
            {
                return localWorld.oceanMonumentGen.getNearestGeneratedFeature(mcWorld, position, bool);
            }
        }
        else if (type.equals(StructureNames.RARE_BUILDING) || type.equals("Temple"))
        {
            if (worldConfig.rareBuildingsEnabled)
            {
                return localWorld.rareBuildingGen.getNearestGeneratedFeature(mcWorld, position, bool);
            }
        } else if (type.equals(StructureNames.STRONGHOLD))
        {
            if (worldConfig.strongholdsEnabled)
            {
                return localWorld.strongholdGen.getNearestGeneratedFeature(mcWorld, position, bool);
            }
        } else if (type.equals(StructureNames.VILLAGE) || type.equals("Village"))
        {
            if (worldConfig.villagesEnabled)
            {
                return localWorld.villageGen.getNearestGeneratedFeature(mcWorld, position, bool);
            }
        }
        return null;
    }

}