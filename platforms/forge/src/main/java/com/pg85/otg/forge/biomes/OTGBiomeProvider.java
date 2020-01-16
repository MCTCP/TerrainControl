package com.pg85.otg.forge.biomes;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;
import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.configuration.world.WorldConfig;
import com.pg85.otg.forge.ForgeWorld;
import com.pg85.otg.forge.generator.structure.OTGVillageGen;
import com.pg85.otg.generator.biome.BiomeGenerator;
import com.pg85.otg.generator.biome.OutputType;
import com.pg85.otg.util.minecraft.defaults.DefaultBiome;

import net.minecraft.init.Biomes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.gen.structure.MapGenVillage;

/**
 * Minecraft's biome generator class is {@link BiomeProvider}, we use
 * {@link BiomeGenerator}. This class provides a bridge between the two,
 * allowing us to use custom biome generators.
 */
public class OTGBiomeProvider extends BiomeProvider
{   
    private final BiomeGenerator biomeGenerator;
    private final ForgeWorld localWorld;
    
    public OTGBiomeProvider(ForgeWorld world, BiomeGenerator biomeGenerator)
    {
        this.localWorld = world;
        this.biomeGenerator = biomeGenerator;
    }
    
    @Override
    public Biome getBiome(BlockPos blockPos)
    {
        return ((ForgeBiome)this.localWorld.getBiome(blockPos.getX(), blockPos.getZ())).getHandle();
    }

    @Override
    public Biome getBiome(BlockPos pos, Biome defaultOption)
    {
        ForgeBiome biome = (ForgeBiome) this.localWorld.getBiome(pos.getX(), pos.getZ());
        if (biome != null)
        {
            return biome.getHandle();
        }
        return defaultOption;
    }

    @Override
    public Biome[] getBiomesForGeneration(Biome[] paramArrayOfBiomeBase, int x, int z, int width, int height)
    {
        if (paramArrayOfBiomeBase == null || (paramArrayOfBiomeBase.length < width * height))
        {
            paramArrayOfBiomeBase = new Biome[width * height];
        }

        int[] arrayOfInt = this.biomeGenerator.getBiomesUnZoomed(null, x, z, width, height,
                OutputType.DEFAULT_FOR_WORLD);

        // Replaces ids with BiomeBases
        for (int i = 0; i < width * height; i++)
        {
            paramArrayOfBiomeBase[i] = this.localWorld.getBiomeByOTGIdOrNull(arrayOfInt[i]).getHandle();
        }

        return paramArrayOfBiomeBase;
    }

    @Override
    public Biome[] getBiomes(Biome[] listToReuse, int x, int z, int width, int length, boolean cacheFlag)
    {
        if ((listToReuse == null) || (listToReuse.length < width * length))
        {
            listToReuse = new Biome[width * length];
        }

        int[] arrayOfInt = this.biomeGenerator.getBiomes(null, x, z, width, length, OutputType.DEFAULT_FOR_WORLD);

        // Replace ids with BiomeBases
        for (int i = 0; i < width * length; i++)
        {
            listToReuse[i] = this.localWorld.getBiomeByOTGIdOrNull(arrayOfInt[i]).getHandle();
        }

        return listToReuse;
    }

    @Override
    public boolean areBiomesViable(int x, int z, int radius, List<Biome> allowed)
    {
        // Hack for villages in other biomes
        // (The alternative would be to completely override the village spawn code)
        if (allowed == MapGenVillage.VILLAGE_SPAWN_BIOMES && this.localWorld.villageGen instanceof OTGVillageGen && this.localWorld.villageGen != null)
        {
            allowed = ((OTGVillageGen)this.localWorld.villageGen).villageSpawnBiomes;
        }

        int i = x - radius >> 2;
        int j = z - radius >> 2;
        int k = x + radius >> 2;
        int m = z + radius >> 2;

        int n = k - i + 1;
        int i1 = m - j + 1;
        Biome[] arrayOfInt = this.getBiomesForGeneration(null, i, j, n, i1);
        for (int i2 = 0; i2 < n * i1; i2++)
        {
            Biome localBiomeBase = arrayOfInt[i2];
            if (!allowed.contains(localBiomeBase))
                return false;
        }

        return true;
    }
    
    @Override
    public BlockPos findBiomePosition(int x, int z, int range, List<Biome> allowedBiomes, Random random)
    {       	
        int i = x - range >> 2;
        int j = z - range >> 2;
        int k = x + range >> 2;
        int m = z + range >> 2;

        int n = k - i + 1;
        int i1 = m - j + 1;
        int[] arrayOfInt = this.biomeGenerator.getBiomesUnZoomed(null, i, j, n, i1, OutputType.DEFAULT_FOR_WORLD);
        BlockPos blockPos = null;
        int i2 = 0;       
        
        for (int i3 = 0; i3 < arrayOfInt.length; i3++)
        {
        	ForgeBiome biome = this.localWorld.getBiomeByOTGIdOrNull(arrayOfInt[i3]);
        	int i4 = i + i3 % n << 2;
            int i5 = j + i3 / n << 2;
            Biome localBiomeBase = biome.biomeBase;            
            if (
        		allowedBiomes.contains(localBiomeBase) && 
        		!(
    				blockPos != null && 
    				random.nextInt(i2 + 1) != 0)
        		)
            {
                blockPos = new BlockPos(i4, 0, i5);
                i2++;
            }
        }

        return blockPos;
    }

    @Override
    public void cleanupCache()
    {
        this.biomeGenerator.cleanupCache();
    }
   
    /**
     * Gets the list of valid biomes for the player to spawn in.
     */
    public List<Biome> getBiomesToSpawnIn()
    {
    	// TODO: Disallowing any inner biomes for defaultocean/defaultfrozenocean as spawn atm, this includes things like beaches and mushroomisland.
    	// Will add an "allowedBiomes" setting to the worldconfig later that overrides the default behaviour.
    	WorldConfig worldConfig = this.localWorld.getConfigs().getWorldConfig();
    	List<Biome> biomesToSpawnIn = new ArrayList<Biome>();
    	List<LocalBiome> disallowedBiomes = new ArrayList<LocalBiome>();
    	disallowedBiomes.add(this.localWorld.getBiomeByNameOrNull(worldConfig.defaultOceanBiome));
    	disallowedBiomes.add(this.localWorld.getBiomeByNameOrNull(worldConfig.defaultFrozenOceanBiome));
    	getAllInnerBiomes(this.localWorld.getBiomeByNameOrNull(worldConfig.defaultOceanBiome), disallowedBiomes);
    	getAllInnerBiomes(this.localWorld.getBiomeByNameOrNull(worldConfig.defaultFrozenOceanBiome), disallowedBiomes);
    	for(LocalBiome biome : this.localWorld.getAllBiomes())
    	{
    		if(!disallowedBiomes.contains(biome))
    		{
    			biomesToSpawnIn.add(((ForgeBiome)biome).biomeBase);
    		}
    	}
        return biomesToSpawnIn;
    }
    
    private void getAllInnerBiomes(LocalBiome targetBiome, List<LocalBiome> foundBiomes)
    {    	
    	for(LocalBiome allBiomesBiome : this.localWorld.getAllBiomes())
    	{
    		if(!foundBiomes.contains(allBiomesBiome))
    		{
	    		if(allBiomesBiome.getBiomeConfig().isleInBiome != null && allBiomesBiome.getBiomeConfig().isleInBiome.size() > 0)
	    		{
	    			for(String isleinBiomeName : allBiomesBiome.getBiomeConfig().isleInBiome)
	    			{
	    				LocalBiome isleInBiome = this.localWorld.getBiomeByNameOrNull(isleinBiomeName);
	    				if(targetBiome == isleInBiome)
	    				{
	    					foundBiomes.add(allBiomesBiome);
	    					getAllInnerBiomes(allBiomesBiome, foundBiomes);
	    					break;
	    				}
	    			}
	    		}
    		}
    		if(!foundBiomes.contains(allBiomesBiome))
    		{
	    		if(allBiomesBiome.getBiomeConfig().biomeIsBorder != null && allBiomesBiome.getBiomeConfig().biomeIsBorder.size() > 0)
	    		{
	    			for(String borderBiomeName : allBiomesBiome.getBiomeConfig().biomeIsBorder)
	    			{
	    				LocalBiome borderBiome = this.localWorld.getBiomeByNameOrNull(borderBiomeName);
	    				if(targetBiome == borderBiome)
	    				{
	    					foundBiomes.add(allBiomesBiome);
	    					getAllInnerBiomes(allBiomesBiome, foundBiomes);
	    					break;
	    				}
	    			}
	    		}
    		}
    	}    	
    }
}