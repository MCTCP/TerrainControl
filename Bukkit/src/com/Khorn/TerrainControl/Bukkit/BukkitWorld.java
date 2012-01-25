package com.Khorn.TerrainControl.Bukkit;

import com.Khorn.TerrainControl.Bukkit.BiomeManager.IBiomeManager;
import com.Khorn.TerrainControl.Configuration.WorldConfig;
import com.Khorn.TerrainControl.DefaultBiome;
import com.Khorn.TerrainControl.DefaultMaterial;
import com.Khorn.TerrainControl.Generator.ResourceGens.TreeType;
import com.Khorn.TerrainControl.LocalBiome;
import com.Khorn.TerrainControl.LocalWorld;
import net.minecraft.server.BiomeBase;
import net.minecraft.server.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class BukkitWorld implements LocalWorld
{
    private TCChunkGenerator generator;
    private World world;
    private WorldConfig settings;
    private String name;
    private IBiomeManager biomeManager;

    private static int NextBiomeId = DefaultBiome.values().length;
    private static LocalBiome[] Biomes = new LocalBiome[64];
    private HashMap<String, LocalBiome> BiomeNames = new HashMap<String, LocalBiome>();
    private static ArrayList<LocalBiome> DefaultBiomes = new ArrayList<LocalBiome>();

    static
    {
        for (int i = 0; i < DefaultBiome.values().length; i++)
        {
            Biomes[i] = new BukkitBiome(BiomeBase.a[i]);
            DefaultBiomes.add(Biomes[i]);
        }
    }

    public BukkitWorld(String _name)
    {
        this.name = _name;
        for (LocalBiome biome : DefaultBiomes)
            this.BiomeNames.put(biome.getName(), biome);


    }

    public LocalBiome getNullBiome(String name)
    {
        return new NullBiome(name);
    }

    public LocalBiome AddBiome(String name)
    {
        LocalBiome biome = new BukkitBiome(new CustomBiome(NextBiomeId++, name));
        Biomes[biome.getId()] = biome;
        this.BiomeNames.put(biome.getName(),biome);
        return biome;
    }

    public int getBiomesCount()
    {
        return NextBiomeId;
    }

    public LocalBiome getBiomeById(int id)
    {
        return Biomes[id];
    }

    public int getBiomeIdByName(String name)
    {
        return this.BiomeNames.get(name).getId();
    }

    public ArrayList<LocalBiome> getDefaultBiomes()
    {
        return DefaultBiomes;
    }

    public int[] getBiomesUnZoomed(int[] biomeArray, int x, int z, int x_size, int z_size)
    {
        return this.biomeManager.getBiomesUnZoomedTC(biomeArray,x,z,x_size,z_size);
    }

    public float[] getTemperatures(int x, int z, int x_size, int z_size)
    {
        return this.biomeManager.getTemperaturesTC(x,z,x_size,z_size);
    }

    public int[] getBiomes(int[] biomeArray, int x, int z, int x_size, int z_size)
    {
        return this.biomeManager.getBiomesTC(biomeArray,x,z,x_size,z_size);
    }

    public int getBiome(int x, int z)
    {
       return this.biomeManager.getBiomeTC(x,z);
    }

    public LocalBiome getLocalBiome(int x, int z)
    {
        return Biomes[this.getBiome(x,z)];
    }

    public double getBiomeFactorForOldBM(int index)
    {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void PrepareTerrainObjects(int x, int z, byte[] chunkArray)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void PlaceDungeons(Random rand, int x, int y, int z)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void PlaceTree(TreeType type, Random rand, int x, int y, int z)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void PlacePonds(int BlockId, Random rand, int x, int y, int z)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void PlaceIce(int x, int z)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean PlaceTerrainObjects(Random rand, int chunk_x, int chunk_z)
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void DoReplace()
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getLiquidHeight(int x, int z)
    {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isEmpty(int x, int y, int z)
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getRawBlockId(int x, int y, int z)
    {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setRawBlockIdAndData(int x, int y, int z, int BlockId, int Data)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setRawBlockId(int x, int y, int z, int BlockId)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setBlockId(int x, int y, int z, int BlockId)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setBlockIdAndData(int x, int y, int z, int BlockId, int Data)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getHighestBlockYAt(int x, int z)
    {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public DefaultMaterial getMaterial(int x, int y, int z)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setChunksCreations(boolean createNew)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getLightLevel(int x, int y, int z)
    {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isLoaded(int x, int z)
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public WorldConfig getSettings()
    {
        return this.settings;
    }

    public String getName()
    {
        return this.name;
    }

    public long getSeed()
    {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getHeight()
    {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getHeightBits()
    {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public TCChunkGenerator getChunkGenerator()
    {
        return this.generator;
    }

    public void setSettings(WorldConfig worldConfig)
    {
        this.settings = worldConfig;
    }

    public void Init(World _world)
    {
        this.world = _world;
        this.world.seaLevel = this.settings.waterLevelMax;
        
        if(this.settings.ModeBiome != WorldConfig.BiomeMode.Default)
           this.biomeManager = (IBiomeManager)world.getWorldChunkManager();

        this.generator.Init(this);

    }

    public void setChunkGenerator(TCChunkGenerator _generator)
    {
        this.generator = _generator;
    }
}
