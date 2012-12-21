package com.khorn.terraincontrol.bukkit;

import java.lang.reflect.Field;
import java.util.List;

import net.minecraft.server.v1_4_6.BiomeBase;
import net.minecraft.server.v1_4_6.BiomeMeta;

import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_4_6.block.CraftBlock;

import com.khorn.terraincontrol.configuration.BiomeConfig;

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

        } catch (NoSuchFieldException e)
        {
            e.printStackTrace();
        } catch (IllegalAccessException e)
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

        try
        {
            Field temp;
            Field humid;
            try
            {
                temp = BiomeBase.class.getField("temperature");
                humid = BiomeBase.class.getField("humidity");
            } catch (NoSuchFieldException e)
            {
                temp = BiomeBase.class.getField("F");
                humid = BiomeBase.class.getField("G");
            }

            temp.setFloat(this, config.BiomeTemperature);
            humid.setFloat(this, config.BiomeWetness);
        } catch (NoSuchFieldException e)
        {
            e.printStackTrace();
        } catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }

        // This section modifies the BiomeMetas... or "SpawnGroups" as I would like to call them :P
        List<BiomeMeta> monsters = Obfu.getBiomeBase_MonsterBiomeMetas(this);
        List<BiomeMeta> creatures = Obfu.getBiomeBase_CreatureBiomeMetas(this);
        List<BiomeMeta> watercreatures = Obfu.getBiomeBase_WaterCreatureBiomeMetas(this);

        List<BiomeMeta> monsterMetas = Obfu.convertToBiomeMetaList(config.spawnMonsters);
        List<BiomeMeta> creatureMetas = Obfu.convertToBiomeMetaList(config.spawnCreatures);
        List<BiomeMeta> waterCreatureMetas = Obfu.convertToBiomeMetaList(config.spawnWaterCreatures);

        if (!config.spawnMonstersAddDefaults)
            monsters.clear();
        monsters.addAll(monsterMetas);

        if (!config.spawnCreaturesAddDefaults)
            creatures.clear();
        creatures.addAll(creatureMetas);

        if (!config.spawnWaterCreaturesAddDefaults)
            watercreatures.clear();
        watercreatures.addAll(waterCreatureMetas);
    }
}
