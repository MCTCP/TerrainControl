package com.khorn.terraincontrol.bukkit;

import com.khorn.terraincontrol.configuration.BiomeConfig;

import net.minecraft.server.BiomeBase;
import net.minecraft.server.BiomeMeta;

import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.block.CraftBlock;

import java.lang.reflect.Field;
import java.util.List;

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

    public void SetBiome(BiomeConfig config)
    {
        this.D = config.BiomeHeight;
        this.E = config.BiomeVolatility;
        this.A = config.SurfaceBlock;
        this.B = config.GroundBlock;
        this.F = config.BiomeTemperature;
        this.G = config.BiomeWetness;
        
        // This section modifies the BiomeMetas... or "SpawnGroups" as I would like to call them :P
        List<BiomeMeta> monsters = Obfu.getBiomeBase_MonsterBiomeMetas(this);
        List<BiomeMeta> creatures = Obfu.getBiomeBase_CreatureBiomeMetas(this);
        List<BiomeMeta> watercreatures = Obfu.getBiomeBase_WaterCreatureBiomeMetas(this);
        
        List<BiomeMeta> monsterMetas = Obfu.convertToBiomeMetaList(config.spawnMonsters);
        List<BiomeMeta> creatureMetas = Obfu.convertToBiomeMetaList(config.spawnCreatures);
        List<BiomeMeta> waterCreatureMetas = Obfu.convertToBiomeMetaList(config.spawnWaterCreatures);
        
        if ( ! config.spawnMonstersAddDefaults) monsters.clear();
        monsters.addAll(monsterMetas);

        if ( ! config.spawnCreaturesAddDefaults) creatures.clear();
        creatures.addAll(creatureMetas);
        
        if ( ! config.spawnWaterCreaturesAddDefaults) watercreatures.clear();
        watercreatures.addAll(waterCreatureMetas);
    }
}
