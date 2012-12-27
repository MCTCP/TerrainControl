package com.khorn.terraincontrol.forge.structuregens;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.gen.structure.ComponentVillageStartPiece;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.BiomeConfig.VillageType;
import com.khorn.terraincontrol.forge.util.WorldHelper;

public class VillageStartPiece extends ComponentVillageStartPiece
{
    public final WorldChunkManager worldChunkMngr;

    public final boolean inDesert;
    public final BiomeGenBase biome;

    /** Village size, 0 for normal, 1 for flap map */
    public final int terrainType;

    @SuppressWarnings("rawtypes")
    public VillageStartPiece(World world, int par2, Random par3Random, int par4, int par5, ArrayList par6ArrayList, int size)
    {
        super(world.getWorldChunkManager(), par2, par3Random, par4, par5, par6ArrayList, size);
        this.worldChunkMngr = world.getWorldChunkManager();
        this.structureVillageWeightedPieceList = par6ArrayList;
        this.terrainType = size;
        BiomeGenBase currentBiomeGenBase = worldChunkMngr.getBiomeGenAt(par4, par5);
        LocalWorld worldTC = WorldHelper.toLocalWorld(world);
        BiomeConfig config = worldTC.getSettings().biomeConfigs[currentBiomeGenBase.biomeID];
        if (config.villageType == VillageType.sandstone)
        {
            this.inDesert = true;
        } else
        {
            this.inDesert = false;
        }

        this.biome = currentBiomeGenBase;
        this.startPiece = this;
    }

    public WorldChunkManager getWorldChunkManager()
    {
        return this.worldChunkMngr;
    }
}
