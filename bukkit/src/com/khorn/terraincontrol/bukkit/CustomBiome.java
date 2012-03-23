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
        
        // this.J.add(new BiomeMeta(EntityOcelot.class, 40, 4, 4));
        // This Is an example approach we could take:
        // Constructor: BiomeMeta(Class entityClass, int weight, int groupMin, int groupMax)
        // Default one for jungle is:
        // this.J.add(new BiomeMeta(EntityOcelot.class, 2, 1, 1));
        // Since data is stored in the world anvil files we can give users complete control over mob spawning :)
        // 
        // But is there issues with storing the custom ids? Will we get it clashes when minecraft adds new ones?
        // How should we handle this?
    }
}
