package com.Khorn.TerrainControl.Bukkit;

import com.Khorn.TerrainControl.Configuration.BiomeConfig;
import net.minecraft.server.BiomeBase;
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
        this.F = config.BiomeTemperature;
        this.G = config.BiomeWetness;
    }
}
