package com.khorn.terraincontrol.forge.structuregens;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.BiomeConfig.VillageType;
import com.khorn.terraincontrol.forge.util.WorldHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.gen.structure.ComponentVillageStartPiece;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Random;

public class VillageStartPiece extends ComponentVillageStartPiece
{
    public final WorldChunkManager worldChunkManager;

    @SuppressWarnings("rawtypes")
    public VillageStartPiece(World world, int par2, Random par3Random, int par4, int par5, ArrayList par6ArrayList, int size)
    {
        super(world.getWorldChunkManager(), par2, par3Random, par4, par5, par6ArrayList, size);
        this.worldChunkManager = world.getWorldChunkManager();

        // Whether the village is a sandstone village
        BiomeGenBase currentBiomeGenBase = worldChunkManager.getBiomeGenAt(par4, par5);
        LocalWorld worldTC = WorldHelper.toLocalWorld(world);
        BiomeConfig config = worldTC.getSettings().biomeConfigs[currentBiomeGenBase.biomeID];
        setSandstoneVillage(config.villageType == VillageType.sandstone);

        this.startPiece = this;
    }

    /**
     * Just sets the first boolean it can find in the WorldGenVillageStartPiece.class to sandstoneVillage.
     *
     * @param sandstoneVillage Whether the village should be a sandstone village.
     */
    private void setSandstoneVillage(boolean sandstoneVillage)
    {
        for (Field field : ComponentVillageStartPiece.class.getFields())
        {
            if (field.getType().toString().equals("boolean"))
            {
                try
                {
                    field.setAccessible(true);
                    field.setBoolean(this, sandstoneVillage);
                    break;
                } catch (Exception e)
                {
                    TerrainControl.log("Cannot make village a sandstone village!");
                    e.printStackTrace();
                }
            }
        }
    }

    public WorldChunkManager getWorldChunkManager()
    {
        return this.worldChunkManager;
    }
}
