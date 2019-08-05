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
 * Note: This class implements some fishy stuff in equals() and hashCode().
 * As a result, comparing any OTGBiome with biomeName "Ocean" to any other
 * OTGBiome with biomeName "Ocean" will return true.
 */
public class OTGBiome extends Biome implements IOTGASMBiome
{
    private int skyColor;
    int savedId;

    OTGBiome(BiomeConfig config, ResourceLocation registryKey)
    {
        super(new BiomePropertiesCustom(config));

        this.setRegistryName(registryKey);
        
        this.skyColor = config.skyColor;

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
    		return super.getGrassColorAtPos(pos);
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
    		return super.getFoliageColorAtPos(pos);
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