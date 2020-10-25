package com.pg85.otg.forge.biomes;

import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.configuration.biome.settings.WeightedMobSpawnGroup;
import com.pg85.otg.forge.asm.excluded.IOTGASMBiome;
import com.pg85.otg.forge.util.MobSpawnGroupHelper;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

/**
 * Used for all custom biomes.
 */
public class OTGBiome extends Biome implements IOTGASMBiome
{
    private int skyColor;
    int savedId;
    private int grassColor1 = 0xffffff;
    private int grassColor2 = 0xffffff;
    private int foliageColor1 = 0xffffff;
    private int foliageColor2 = 0xffffff;
    
    OTGBiome(BiomeConfig config, ResourceLocation registryKey)
    {
        super(new BiomePropertiesCustom(config));

        this.setRegistryName(registryKey);
        this.skyColor = config.skyColor;
        
        if(
    		!config.grassColorIsMultiplier && 
    		(
				config.grassColor != 0xffffff || 
				config.grassColor2 != 0xffffff
			)
		)
        {
        	this.grassColor1 = config.grassColor != 0xffffff ? config.grassColor : config.grassColor2;
        	this.grassColor2 = config.grassColor2 != 0xffffff ? config.grassColor2 : config.grassColor;
        }
        
        if(
    		!config.foliageColorIsMultiplier && 
    		(
				config.foliageColor != 0xffffff || 
				config.foliageColor2 != 0xffffff
			)
		)
        {
        	this.foliageColor1 = config.foliageColor != 0xffffff ? config.foliageColor : config.foliageColor2;
        	this.foliageColor2 = config.foliageColor2 != 0xffffff ? config.foliageColor2 : config.foliageColor;
        }
        
        // TODO: Is clearing really necessary here?
        // Don't use the TC default values for mob spawning for Forge,
        // instead we'll copy mobs lists from the vanilla biomes so we
        // also get mobs added by other mods.
        // These mobs should be included in config's mob lists.
        this.spawnableMonsterList.clear();
        this.spawnableCreatureList.clear();
        this.spawnableCaveCreatureList.clear();
        this.spawnableWaterCreatureList.clear();

        
        // Mob spawning
        addMobs(this.spawnableMonsterList, config.spawnMonstersMerged);
        addMobs(this.spawnableCreatureList, config.spawnCreaturesMerged);
        addMobs(this.spawnableWaterCreatureList, config.spawnWaterCreaturesMerged);
        addMobs(this.spawnableCaveCreatureList, config.spawnAmbientCreaturesMerged);
    }  
    
    // Sky color from Temp
    @Override
    public int getSkyColorByTemp(float v)
    {
        return this.skyColor;
    }

    @Override
    public String toString()
    {
        return "OTGBiome of " + biomeName;
    }

    // Fix for swamp colors (there's a custom noise applied to swamp colors)
    // TODO: Make these colors configurable via the BiomeConfig
    @SideOnly(Side.CLIENT)
    public int getGrassColorAtPos(BlockPos pos)
    {
    	if(this.biomeName.equals("Swampland") || this.biomeName.equals("Swampland M"))
    	{
	        double d0 = GRASS_COLOR_NOISE.getValue((double)pos.getX() * 0.0225D, (double)pos.getZ() * 0.0225D);
	        return d0 < -0.1D ? 5011004 : 6975545;
    	} else {
    		if(grassColor1 != 0xffffff || grassColor2 != 0xffffff)
    		{
    			double d0 = GRASS_COLOR_NOISE.getValue((double)pos.getX() * 0.0225D, (double)pos.getZ() * 0.0225D);
    			return d0 < -0.1D ? grassColor1 : grassColor2;
    		} else {
    			return super.getGrassColorAtPos(pos);
    		}
    	}
    }

    // Fix for swamp colors (there's a custom noise applied to swamp colors)
    // TODO: Make these colors configurable via the BiomeConfig
    @SideOnly(Side.CLIENT)
    public int getFoliageColorAtPos(BlockPos pos)
    {
    	if(this.biomeName.equals("Swampland") || this.biomeName.equals("Swampland M"))
    	{
    		return 6975545;
    	} else {
    		if(foliageColor1 != 0xffffff || foliageColor2 != 0xffffff)
    		{
	    		double d0 = GRASS_COLOR_NOISE.getValue((double)pos.getX() * 0.0225D, (double)pos.getZ() * 0.0225D);
	    		return d0 < -0.1D ? foliageColor1 : foliageColor2;
    		} else {
    			return super.getFoliageColorAtPos(pos);
    		}
    	}
    }

    // Adds the mobs to the internal list
    private void addMobs(List<SpawnListEntry> internalList, List<WeightedMobSpawnGroup> configList)//, boolean improvedMobSpawning)
    {
    	List<SpawnListEntry> newList = new ArrayList<SpawnListEntry>();
    	List<SpawnListEntry> newListParent = new ArrayList<SpawnListEntry>();
    	// Add mobs defined in bc's mob spawning settings
        List<SpawnListEntry> spawnListEntry = MobSpawnGroupHelper.toMinecraftlist(configList);
        newList.addAll(spawnListEntry);

    	for(SpawnListEntry entryParent : internalList)
    	{
			boolean bFound = false;
			for(SpawnListEntry entryChild : newList)
			{
				if(entryChild.entityClass.equals(entryParent.entityClass))
				{
					bFound = true;
				}
			}
			if(!bFound)
			{
				newListParent.add(entryParent);
			}
    	}
    	newList.addAll(newListParent);

        internalList.clear();

        for(SpawnListEntry spe : newList)
        {
        	if(spe.itemWeight > 0 && spe.maxGroupCount > 0)
        	{
        		internalList.add(spe);
        	}
        }
    }

    @Override
	public int getSavedId()
	{
		return savedId;
	}
}