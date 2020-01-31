package com.pg85.otg.generator.resource;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.ConfigFunction;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.customobjects.CustomObject;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.ChunkCoordinate;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TreeGen extends Resource
{
    private final List<Integer> treeChances;
    private final List<String> treeNames;

    public TreeGen(BiomeConfig biomeConfig, List<String> args) throws InvalidConfigException
    {
        super(biomeConfig);
        assureSize(3, args);

        frequency = readInt(args.get(0), 1, 100);

        treeNames = new ArrayList<String>();
        treeChances = new ArrayList<Integer>();

        for (int i = 1; i < args.size() - 1; i += 2)
        {
            treeNames.add(args.get(i));
            treeChances.add(readInt(args.get(i + 1), 1, 100));
        }
    }

    @Override
    public boolean equals(Object other)
    {
        if (!super.equals(other))
            return false;
        if (other == null)
            return false;
        if (other == this)
            return true;
        if (getClass() != other.getClass())
            return false;
        final TreeGen compare = (TreeGen) other;
        return (this.treeNames == null ? this.treeNames == compare.treeNames
                        : this.treeNames.equals(compare.treeNames))
                && (this.treeChances == null ? this.treeChances == compare.treeChances
                        : this.treeChances.equals(compare.treeChances));
    }

    @Override
    public int getPriority()
    {
        return -31;
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 53 * hash + super.hashCode();
        hash = 53 * hash + (this.treeNames != null ? this.treeNames.hashCode() : 0);
        hash = 53 * hash + (this.treeChances != null ? this.treeChances.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean isAnalogousTo(ConfigFunction<BiomeConfig> other)
    {
        if (getClass() == other.getClass())
        {
            try
            {
                TreeGen otherO = (TreeGen) other;
                return otherO.treeNames.size() == this.treeNames.size() && otherO.treeNames.containsAll(this.treeNames);
            }
            catch (Exception ex)
            {
                OTG.log(LogMarker.WARN, ex.getMessage());
            }
        }
        return false;
    }

    @Override
    public String toString()
    {
        String output = "Tree(" + frequency;
        for (int i = 0; i < treeNames.size(); i++)
        {
            output += "," + treeNames.get(i) + "," + treeChances.get(i);
        }
        return output + ")";
    }

    @Override
    public void spawn(LocalWorld world, Random random, boolean villageInChunk, int x, int z, ChunkCoordinate chunkBeingPopulated)
    {
        // Left blank, as spawnInChunk already handles this
    }
    
    @Override
    protected void spawnInChunk(LocalWorld world, Random random, boolean villageInChunk, ChunkCoordinate chunkCoord)
    {    	   	
    	// TODO: Make sure we stay within population bounds, anything outside won't be spawned (unless it's in an existing chunk).
    	
        for (int i = 0; i < frequency; i++)
        {        	
            for (int treeNumber = 0; treeNumber < treeNames.size(); treeNumber++)
            {           	            
                if (random.nextInt(100) < treeChances.get(treeNumber))
                {                	
                    int x = chunkCoord.getBlockXCenter() + random.nextInt(ChunkCoordinate.CHUNK_X_SIZE);
                    int z = chunkCoord.getBlockZCenter() + random.nextInt(ChunkCoordinate.CHUNK_Z_SIZE);               	
                    
                    //CustomObject tree = getTrees(world.getName()).get(treeNumber);
                    
    	        	String treeName = treeNames.get(treeNumber);
    	        	CustomObject tree = null;
        			int minHeight = 0;
        			int maxHeight = 0;
    	        	if(treeName.contains("("))
    	        	{
    	        		String[] params = treeName.replace(")", "").split("\\(");
    	        		treeName = params[0];
    	        		tree = OTG.getCustomObjectManager().getGlobalObjects().getObjectByName(treeName, world.getName());
                        if(tree == null)
                        {
                    		if(OTG.getPluginConfig().spawnLog)
                    		{
                    			BiomeConfig biomeConfig = world.getBiome(chunkCoord.getChunkX() * 16 + 15, chunkCoord.getChunkZ() * 16 + 15).getBiomeConfig();
                    			OTG.log(LogMarker.WARN, "Error: Could not find BO3 for Tree in biome " + biomeConfig.getName() + ". BO3: " + treeNames.get(treeNumber));
                    		}
                    		continue;
                        }                    
    	        		
            			params = params[1].split(";");
            			String sMinHeight = params[0].toLowerCase().replace("minheight=", "");
            			String sMaxHeight = params[1].toLowerCase().replace("maxheight=", "");

            			try
            			{
    	        			minHeight = Integer.parseInt(sMinHeight);
    	        			maxHeight = Integer.parseInt(sMaxHeight);
            			} catch(NumberFormatException ex) {  }	        				        			

                		if(tree.spawnAsTree(world, random, x, z, minHeight, maxHeight, chunkCoord))
                		{
        	                // Success!
        	                break;
                		}

    	        	} else {
    	        		tree = OTG.getCustomObjectManager().getGlobalObjects().getObjectByName(treeName, world.getName());
                        if(tree == null)
                        {
                    		if(OTG.getPluginConfig().spawnLog)
                    		{
                    			BiomeConfig biomeConfig = world.getBiome(chunkCoord.getChunkX() * 16 + 15, chunkCoord.getChunkZ() * 16 + 15).getBiomeConfig();
                    			OTG.log(LogMarker.WARN, "Error: Could not find BO3 for Tree in biome " + biomeConfig.getName() + ". BO3: " + treeNames.get(treeNumber));
                    		}
                    		continue;
                        }                        	        		
                		if(tree.spawnAsTree(world, random, x, z, -1, -1, chunkCoord))
                		{
        	                // Success!
        	                break;
                		}
    	        	}
                }
            }
        }    		
    }	
}
