package com.khorn.terraincontrol.bukkit;

import com.khorn.terraincontrol.configuration.BiomeConfig;
import net.minecraft.server.BiomeBase;
import net.minecraft.server.BiomeMeta;
import net.minecraft.server.EntityOcelot;

import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.block.CraftBlock;

import java.lang.reflect.Field;

public class CustomBiome extends BiomeBase
{
    @SuppressWarnings("MismatchedReadAndWriteOfArray")
    public CustomBiome(int id, String name)
    {
        super(id);
        this.a(name);

        try
        {
            Field biomeMapping = CraftBlock.class.getDeclaredField("BIOME_MAPPING");

            biomeMapping.setAccessible(true);

            Biome[] mappingArray = (Biome[]) biomeMapping.get(null);
            mappingArray[id] = Biome.OCEAN;

        }
        catch (NoSuchFieldException e)
        {
            e.printStackTrace();  
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public void SetBiome(BiomeConfig config)
    {
        this.D = config.BiomeHeight;
        this.E = config.BiomeVolatility;
        this.A = config.SurfaceBlock;
        this.B = config.GroundBlock;
        this.F = config.BiomeTemperature;
        this.G = config.BiomeWetness;
        
        // This section modifies the BiomeMetas... or "SpawnGroups" as I would like to call them :P
        if ( ! config.spawnCreaturesAddDefaults)
        {
            this.K.clear();
        }
        this.K.addAll(config.spawnCreatures);
        
        if ( ! config.spawnMonstersAddDefaults)
        {
            this.J.clear();
        }
        this.J.addAll(config.spawnMonsters);
        
        if ( ! config.spawnWaterCreaturesAddDefaults)
        {
            this.L.clear();
        }
        this.L.addAll(config.spawnWaterCreatures);
    }
}
