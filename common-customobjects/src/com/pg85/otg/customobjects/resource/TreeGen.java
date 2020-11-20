package com.pg85.otg.customobjects.resource;

import com.pg85.otg.config.ConfigFunction;
import com.pg85.otg.config.customobjects.CustomObjectResourcesManager;
import com.pg85.otg.customobjects.CustomObject;
import com.pg85.otg.customobjects.CustomObjectManager;
import com.pg85.otg.customobjects.structures.CustomStructureCache;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.ILogger;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.interfaces.IBiomeConfig;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IModLoadedChecker;
import com.pg85.otg.util.interfaces.IPresetNameProvider;
import com.pg85.otg.util.interfaces.IWorldGenRegion;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TreeGen extends CustomObjectResource
{
    private final List<Integer> treeChances;
    private final List<String> treeNames;
    private CustomObject[] treeObjects;
    private int[] treeObjectMinChances;
    private int[] treeObjectMaxChances;
    private boolean treesLoaded = false;

    public TreeGen(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
    {
    	super(biomeConfig, args, logger, materialReader);
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
        return 
    		(
				this.treeNames == null ? 
				this.treeNames == compare.treeNames
				: this.treeNames.equals(compare.treeNames)
			) && (
        		this.treeChances == null ? 
				this.treeChances == compare.treeChances
                : this.treeChances.equals(compare.treeChances)
    		)
        ;
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
    public boolean isAnalogousTo(ConfigFunction<IBiomeConfig> other, ILogger logger)
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
                logger.log(LogMarker.WARN, ex.getMessage());
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
    public void spawn(IWorldGenRegion worldGenRegion, Random random, boolean villageInChunk, int x, int z, ChunkCoordinate chunkBeingPopulated)
    {
        // Left blank, as spawnInChunk already handles this
    }
    
	// TODO: Could this cause problems for developer mode / flushcache, trees not updating during a session?
    private void loadTrees(String worldName, Path otgRootFolder, boolean spawnLog, ILogger logger, CustomObjectManager customObjectManager, IPresetNameProvider presetNameProvider, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
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
	        		tree = customObjectManager.getGlobalObjects().getObjectByName(treeName, worldName, otgRootFolder, spawnLog, logger, customObjectManager, presetNameProvider, materialReader, manager, modLoadedChecker);
	        		treeObjects[treeNumber] = tree;    		    
	                if(tree == null)
	                {
	            		if(spawnLog)
	            		{
	            			logger.log(LogMarker.WARN, "Error: Could not find BO3 for Tree, BO3: " + treeNames.get(treeNumber));
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
	        		tree = customObjectManager.getGlobalObjects().getObjectByName(treeName, worldName, otgRootFolder, spawnLog, logger, customObjectManager, presetNameProvider, materialReader, manager, modLoadedChecker);                
	    		    treeObjects[treeNumber] = tree;
	        		if(tree == null)
	                {
	            		if(spawnLog)
	            		{
	            			logger.log(LogMarker.WARN, "Error: Could not find BO3 for Tree, BO3: " + treeNames.get(treeNumber));
	            		}
	            		continue;
	                }
	        	}
	    	}
    	}
    }
    
    @Override
    protected void spawnInChunk(CustomStructureCache structureCache, IWorldGenRegion worldGenRegion, Random random, boolean villageInChunk, ChunkCoordinate chunkCoord, Path otgRootFolder, boolean spawnLog, ILogger logger, CustomObjectManager customObjectManager, IPresetNameProvider presetNameProvider, IMaterialReader materialReader, CustomObjectResourcesManager manager, IModLoadedChecker modLoadedChecker)
    {
    	// TODO: Make sure we stay within population bounds, anything outside won't be spawned (unless it's in an existing chunk).

    	loadTrees(worldGenRegion.getWorldName(), otgRootFolder, spawnLog, logger, customObjectManager, presetNameProvider, materialReader, manager, modLoadedChecker);

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
            		if(tree != null && tree.spawnAsTree(structureCache, worldGenRegion, random, x, z, treeObjectMinChances[treeNumber], treeObjectMaxChances[treeNumber], chunkCoord))
            		{
    	                // Success!
    	                break;
            		}
                }
            }
        }    		
    }	
}
