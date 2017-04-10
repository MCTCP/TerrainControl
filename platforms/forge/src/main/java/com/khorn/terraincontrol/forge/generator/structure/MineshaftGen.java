package com.khorn.terraincontrol.forge.generator.structure;

import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.BiomeConfig.MineshaftType;
import com.khorn.terraincontrol.forge.ForgeEngine;
import com.khorn.terraincontrol.forge.util.WorldHelper;
import com.khorn.terraincontrol.util.ChunkCoordinate;
import com.khorn.terraincontrol.util.minecraftTypes.StructureNames;

import net.minecraft.world.gen.structure.MapGenMineshaft;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.StructureMineshaftStart;
import net.minecraft.world.gen.structure.StructureStart;
import net.minecraftforge.common.DimensionManager;

public class MineshaftGen extends MapGenStructure
{
    @Override
    protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ)
    {
        if (this.rand.nextInt(80) < Math.max(Math.abs(chunkX), Math.abs(chunkZ)))
        {        	        	
            LocalWorld world = ((ForgeEngine)TerrainControl.getEngine()).getWorld(this.worldObj);
            LocalBiome biome = world.getBiome(chunkX * 16 + 8, chunkZ * 16 + 8);
            BiomeConfig biomeConfig = biome.getBiomeConfig();
            if (biomeConfig.mineshaftType == MineshaftType.disabled)
            {
                return false;
            }
            if (this.rand.nextDouble() * 100.0 < biomeConfig.mineshaftsRarity)
            {
                return true;
            }
        }

        return false;
    }

    @Override
    protected StructureStart getStructureStart(int chunkX, int chunkZ)
    {
        LocalWorld world = ((ForgeEngine)TerrainControl.getEngine()).getWorld(this.worldObj);
        LocalBiome biome = world.getBiome(chunkX * ChunkCoordinate.CHUNK_X_SIZE + 8,
                chunkZ * ChunkCoordinate.CHUNK_Z_SIZE + 8);
        BiomeConfig biomeConfig = biome.getBiomeConfig();
        MapGenMineshaft.Type mineshaftType = MapGenMineshaft.Type.NORMAL;
        if (biomeConfig.mineshaftType == MineshaftType.mesa)
        {
            mineshaftType = MapGenMineshaft.Type.MESA;
        }

        return new StructureMineshaftStart(this.worldObj, this.rand, chunkX, chunkZ, mineshaftType);
    }

    @Override
    public String getStructureName()
    {
        return StructureNames.MINESHAFT;
    }
}
