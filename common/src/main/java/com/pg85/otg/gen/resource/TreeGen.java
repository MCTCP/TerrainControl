package com.pg85.otg.gen.resource;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.config.ConfigFunction;
import com.pg85.otg.config.biome.BiomeConfig;
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
    private CustomObject[] treeObjects;
    private int[] treeObjectMinChances;
    private int[] treeObjectMaxChances;
    private boolean treesLoaded = false;

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
    
	// TODO: Could this cause problems for developer mode / flushcache, trees not updating during a session?
    private void loadTrees(String worldName)
    {
    	if(!treesLoaded)
    	{
    		treesLoaded = true;
    		
		    treeObjects = new CustomObject[treeNames.size()];
		    treeObjectMinChances = new int[treeNames.size()];
		    treeObjectMaxChances = new int[treeNames.size()];
	    	
	    	for (int treeNumber = 0; treeNumber < treeNames.size(); treeNumber++)
	    	{
	        	String treeName = treeNames.get(treeNumber);
	        	CustomObject tree = null;
				int minHeight = -1;
				int maxHeight = -1;
	
			    treeObjectMinChances[treeNumber] = minHeight;
			    treeObjectMaxChances[treeNumber] = maxHeight;
				
	        	if(treeName.contains("("))
	        	{
	        		String[] params = treeName.replace(")", "").split("\\(");
	        		treeName = params[0];
	        		tree = OTG.getCustomObjectManager().getGlobalObjects().getObjectByName(treeName, worldName);
	        		treeObjects[treeNumber] = tree;    		    
	                if(tree == null)
	                {
	            		if(OTG.getPluginConfig().spawnLog)
	            		{
	            			OTG.log(LogMarker.WARN, "Error: Could not find BO3 for Tree, BO3: " + treeNames.get(treeNumber));
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
	        		    treeObjectMinChances[treeNumber] = minHeight;
	        		    treeObjectMaxChances[treeNumber] = maxHeight;        			
	    			} catch(NumberFormatException ex) {  }
	        	} else {
	        		tree = OTG.getCustomObjectManager().getGlobalObjects().getObjectByName(treeName, worldName);                
	    		    treeObjects[treeNumber] = tree;
	        		if(tree == null)
	                {
	            		if(OTG.getPluginConfig().spawnLog)
	            		{
	            			OTG.log(LogMarker.WARN, "Error: Could not find BO3 for Tree, BO3: " + treeNames.get(treeNumber));
	            		}
	            		continue;
	                }
	        	}
	    	}
    	}
    }
    
    @Override
    protected void spawnInChunk(LocalWorld world, Random random, boolean villageInChunk, ChunkCoordinate chunkCoord)
    {    	   	
    	// TODO: Make sure we stay within population bounds, anything outside won't be spawned (unless it's in an existing chunk).
    	
    	loadTrees(world.getName());
    	
        for (int i = 0; i < frequency; i++)
        {        	
            for (int treeNumber = 0; treeNumber < treeNames.size(); treeNumber++)
            {           	            
                if (random.nextInt(100) < treeChances.get(treeNumber))
                {                	
                    int x = chunkCoord.getBlockXCenter() + random.nextInt(ChunkCoordinate.CHUNK_SIZE);
                    int z = chunkCoord.getBlockZCenter() + random.nextInt(ChunkCoordinate.CHUNK_SIZE);               	
                    
                    CustomObject tree = treeObjects[treeNumber];
                    // Min/Max == -1 means use bo2/bo3 internal min/max height, otherwise use the optional min/max height defined with Tree()
            		if(tree != null && tree.spawnAsTree(world, random, x, z, treeObjectMinChances[treeNumber], treeObjectMaxChances[treeNumber], chunkCoord))
            		{
    	                // Success!
    	                break;
            		}
                }
            }
        }    		
    }	
}
