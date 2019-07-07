package com.pg85.otg.customobjects.bo4;

import com.pg85.otg.OTG;
import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.configuration.customobjects.CustomObjectConfigFile;
import com.pg85.otg.configuration.customobjects.CustomObjectConfigFunction;
import com.pg85.otg.configuration.io.FileSettingsReaderOTGPlus;
import com.pg85.otg.configuration.io.SettingsReaderOTGPlus;
import com.pg85.otg.configuration.io.SettingsWriterOTGPlus;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.configuration.standard.WorldStandardValues;
import com.pg85.otg.configuration.world.WorldConfig.ConfigMode;
import com.pg85.otg.customobjects.CustomObject;
import com.pg85.otg.customobjects.bo4.BO4Config;
import com.pg85.otg.customobjects.bo4.BO4Settings;
import com.pg85.otg.customobjects.bo4.bo4function.BO4BlockFunction;
import com.pg85.otg.customobjects.bo4.bo4function.BO4BranchFunction;
import com.pg85.otg.customobjects.bo4.bo4function.BO4EntityFunction;
import com.pg85.otg.customobjects.bo4.bo4function.BO4ModDataFunction;
import com.pg85.otg.customobjects.bo4.bo4function.BO4ParticleFunction;
import com.pg85.otg.customobjects.bo4.bo4function.BO4RandomBlockFunction;
import com.pg85.otg.customobjects.bo4.bo4function.BO4SpawnerFunction;
import com.pg85.otg.customobjects.bo4.bo4function.BO4WeightedBranchFunction;
import com.pg85.otg.customobjects.structures.bo4.BO4CustomStructureCoordinate;
import com.pg85.otg.customobjects.bo3.BO3Settings;
import com.pg85.otg.customobjects.bo3.BO3Settings.SpawnHeightEnum;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.bo3.Rotation;
import com.pg85.otg.util.minecraft.defaults.DefaultMaterial;
import com.pg85.otg.util.minecraft.defaults.DefaultStructurePart;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BO4Config extends CustomObjectConfigFile
{
	public String author;
    public String description;
    ConfigMode settingsMode;
    public int frequency;
    
    public int minHeight;
    public int maxHeight;

    private boolean rotateRandomly;
    public SpawnHeightEnum spawnHeight;
    
    BO4BlockFunction[][] blocks = new BO4BlockFunction[4][]; // four rotations
    BO4BranchFunction[][] branches = new BO4BranchFunction[4][];
    BO4ParticleFunction[][] particleFunctions = new BO4ParticleFunction[4][];
    BO4SpawnerFunction[][] spawnerFunctions = new BO4SpawnerFunction[4][];
    BO4ModDataFunction[][] modDataFunctions = new BO4ModDataFunction[4][];
    BO4EntityFunction[][] entityFunctions = new BO4EntityFunction[4][];
    
    private Map<ChunkCoordinate, BO4BlockFunction> heightMap;
    
    private boolean inheritedBO3Loaded;
    
    // These are used in CustomObjectStructure when determining the minimum area in chunks that
    // this branching structure needs to be able to spawn
    public int minimumSizeTop = -1;
    public int minimumSizeBottom = -1;
    public int minimumSizeLeft = -1;
    public int minimumSizeRight = -1;

    public int timesSpawned = 0;
	
    public int branchFrequency;
    // Define groups that this BO3 belongs to with a range in chunks that members of each group should have to each other
    private String branchFrequencyGroup;
    public HashMap<String, Integer> branchFrequencyGroups;

    private int minX;
    private int maxX;
    private int minY;
    private int maxY;
    private int minZ;
    private int maxZ;

    private ArrayList<String> inheritedBO3s;

    // Adjusts the height by this number before spawning. Handy when using "highestblock" for lowering BO3s that have a lot of ground under them included
    public int heightOffset;
    private Rotation inheritBO3Rotation;
    // If this is set to true then any air blocks in the bo3 will not be spawned
    private boolean removeAir;
    // Defaults to false. Set to true if this BO3 should spawn at the player spawn point. When the server starts one of the structures that has IsSpawnPoint set to true is selected randomly and is spawned, the others never get spawned.)
    public boolean isSpawnPoint;

    // Replaces all the non-air blocks that are above this BO3 or its smoothing area with the given block material (should be WATER or AIR or NONE), also applies to smoothing areas although it intentionally leaves some of the terrain above them intact. WATER can be used in combination with SpawnUnderWater to fill any air blocks underneath waterlevel with water (and any above waterlevel with air).
    public String replaceAbove;
    // Replaces all non-air blocks underneath the BO3 (but not its smoothing area) with the designated material until a solid block is found.
    public String replaceBelow;
    // Defaults to true. If set to true then every block in the BO3 of the materials defined in ReplaceWithGroundBlock or ReplaceWithSurfaceBlock will be replaced by the GroundBlock or SurfaceBlock materials configured for the biome the block is spawned in.
    public boolean replaceWithBiomeBlocks;
    // Replaces all the blocks of the given material in the BO3 with the GroundBlock configured for the biome it spawns in
    public String replaceWithGroundBlock;
    // Replaces all the blocks of the given material in the BO3 with the SurfaceBlock configured for the biome it spawns in
    public String replaceWithSurfaceBlock;
    // Define a group that this BO3 belongs to and a range in chunks that members of this group should have to each other
    private String bo3Group;
    public HashMap<String, Integer> bo3Groups;
    // If this is set to true then this BO3 can spawn on top of or inside other BO3's
    public boolean canOverride;

    // Copies the blocks and branches of an existing BO3 into this one
    private String inheritBO3;
    // Should the smoothing area go to the top or the bottom blocks in the bo3?
    public boolean smoothStartTop;
    public boolean smoothStartWood;
    // The size of the smoothing area
    public int smoothRadius;
    // The materials used for the smoothing area
    public String smoothingSurfaceBlock;
    public String smoothingGroundBlock;
    // If true then root BO3 smoothing and height settings are used for all children
    public boolean overrideChildSettings;
    public boolean overrideParentHeight;

    // Used to make sure that dungeons can only spawn underneath other structures
    public boolean mustBeBelowOther;

    private String replacesBO3;
    public ArrayList<String> replacesBO3Branches;
    private String mustBeInside;
    public ArrayList<String> mustBeInsideBranches;
    private String cannotBeInside;
    public ArrayList<String> cannotBeInsideBranches;

    public int smoothHeightOffset;
    public boolean canSpawnOnWater;
    public boolean spawnOnWaterOnly;
    public boolean spawnUnderWater;
    public boolean spawnAtWaterLevel;

    private String worldName;

    private BO4BlockFunction[] blocksOTGPlus;
    private BO4BranchFunction[] branchesOTGPlus;
    private BO4ModDataFunction[] modDataOTGPlus;
    private BO4SpawnerFunction[] spawnerDataOTGPlus;
    private BO4ParticleFunction[] particleDataOTGPlus;
    private BO4EntityFunction[] entityDataOTGPlus;
       
    /**
     * Creates a BO3Config from a file.
     *
     * @param reader       The settings of the BO3.
     * @param directory    The directory the BO3 is stored in.
     * @param otherObjects All other loaded objects by their name.
     */
    BO4Config(SettingsReaderOTGPlus reader) throws InvalidConfigException
    {
        super(reader);
        init();
    }

    private void init() throws InvalidConfigException
    {
        this.minX = Integer.MAX_VALUE;
        this.maxX = Integer.MIN_VALUE;
        this.minY = Integer.MAX_VALUE;
        this.maxY = Integer.MIN_VALUE;
        this.minZ = Integer.MAX_VALUE;
        this.maxZ = Integer.MIN_VALUE;
        readConfigSettings();
    }

	public int getXOffset()
	{
		return minX < -8 ? -minX : maxX > 7 ? -minX : 8;
	}

	public int getZOffset()
	{
		return minZ < -7 ? -minZ : maxZ > 8 ? -minZ : 7;
	}

    public int getminX()
    {
    	return minX + this.getXOffset(); // + xOffset makes sure that the value returned is never negative which is necessary for the collision detection code for CustomStructures in OTG (it assumes the furthest top and left blocks are at => 0 x or >= 0 z in the BO3)
    }

    public int getmaxX()
    {
    	return maxX + this.getXOffset(); // + xOffset makes sure that the value returned is never negative which is necessary for the collision detection code for CustomStructures in OTG (it assumes the furthest top and left blocks are at => 0 x or >= 0 z in the BO3)
    }

    public int getminY()
    {
    	return minY;
    }

    public int getmaxY()
    {
    	return maxY;
    }

    public int getminZ()
    {
    	return minZ + this.getZOffset(); // + zOffset makes sure that the value returned is never negative which is necessary for the collision detection code for CustomStructures in OTG (it assumes the furthest top and left blocks are at => 0 x or >= 0 z in the BO3)
    }

    public int getmaxZ()
    {
    	return maxZ + this.getZOffset(); // + zOffset makes sure that the value returned is never negative which is necessary for the collision detection code for CustomStructures in OTG (it assumes the furthest top and left blocks are at => 0 x or >= 0 z in the BO3)
    }

    public ArrayList<String> getInheritedBO3s()
    {
		return inheritedBO3s;
    }

    public Map<ChunkCoordinate, BO4BlockFunction> getHeightMap(BO4 start)
    {
    	if(heightMap == null)
    	{
	        heightMap = new HashMap<ChunkCoordinate, BO4BlockFunction>();
	        BO4BlockFunction[] blocks = getBlocks();

	        // make heightmap containing the highest or lowest blocks in this chunk
	    	for(int x = 0; x <= 15; x ++)
	    	{
	    		for(int z = 0; z <= 15; z ++)
	    		{
	            	// Check if this is the highest block in the BO3 at these x-z coordinates
	            	for(BO4BlockFunction block2 : blocks)
	            	{
	            		if(block2.x < 0 || block2.x > 15 || block2.z < 0 || block2.z > 15)
	            		{
	            			throw new RuntimeException();
	            		}

	            		if(x == block2.x && z == block2.z)
	            		{
	            			boolean isSmoothAreaAnchor = false;
	            			if(block2 instanceof BO4RandomBlockFunction)
	            			{
            					for(LocalMaterialData material : ((BO4RandomBlockFunction)block2).blocks)
		            			{
            						// TODO: Material should never be null, fix the code in RandomBlockFunction.load() that causes this.
            						if(material == null)
            						{
            							continue;
            						}
            						if(material.isSmoothAreaAnchor(start.getSettings().overrideChildSettings && overrideChildSettings ? start.getSettings().smoothStartWood : smoothStartWood, start.getSettings().spawnUnderWater))
            						{
            							isSmoothAreaAnchor = true;
            							break;
            						}
		            			}
	            			}

	            			if(
            					isSmoothAreaAnchor ||
        						(
    								!(block2 instanceof BO4RandomBlockFunction) &&
    								block2.material.isSmoothAreaAnchor(start.getSettings().overrideChildSettings && overrideChildSettings ? start.getSettings().smoothStartWood : smoothStartWood, start.getSettings().spawnUnderWater)
								)
        					)
	            			{
		            			if(
		            				(!(start.getSettings().overrideChildSettings && overrideChildSettings ? start.getSettings().smoothStartTop : smoothStartTop) && block2.y == getminY()) ||
			        				((start.getSettings().overrideChildSettings && overrideChildSettings ? start.getSettings().smoothStartTop : smoothStartTop) && (heightMap.get(ChunkCoordinate.fromChunkCoords(block2.x, block2.z)) == null || block2.y > heightMap.get(ChunkCoordinate.fromChunkCoords(block2.x, block2.z)).y))
		    					)
		            			{
		            				heightMap.put(ChunkCoordinate.fromChunkCoords(x, z), block2);
		            			}
	            			}
	            		}
	            	}
	    		}
	    	}
    	}
    	return heightMap;
    }

    public BO4BlockFunction[] getBlocks()
    {
    	//return blocksOTGPlus;
    	readBlocks();
    	
    	BO4BlockFunction[] a = blocksOTGPlus.clone();
    	blocksOTGPlus = null;
    	
    	return a;
    }

    protected BO4BranchFunction[] getbranches()
    {
    	return branchesOTGPlus;
    }

    public BO4ModDataFunction[] getModData()
    {
    	return modDataOTGPlus;
    }

    public BO4SpawnerFunction[] getSpawnerData()
    {
    	return spawnerDataOTGPlus;
    }

    public BO4ParticleFunction[] getParticleData()
    {
    	return particleDataOTGPlus;
    }

    public BO4EntityFunction[] getEntityData()
    {
    	return entityDataOTGPlus;
    }

    private void loadInheritedBO3(boolean blocksOnly)
    {
    	if(this.inheritBO3 != null && this.inheritBO3.trim().length() > 0 && (!inheritedBO3Loaded || blocksOnly))
    	{
    		File currentFile = this.getFile().getParentFile();
    		worldName = currentFile.getName();
    		while(currentFile.getParentFile() != null && !currentFile.getName().toLowerCase().equals(PluginStandardValues.PresetsDirectoryName))
    		{
    			worldName = currentFile.getName();
    			currentFile = currentFile.getParentFile();
    			if(worldName.toLowerCase().equals("globalobjects"))
    			{
    				worldName = null;
    				break;
    			}
    		}

    		CustomObject parentBO3 = OTG.getCustomObjectManager().getGlobalObjects().getObjectByName(this.inheritBO3, worldName);
			if(parentBO3 != null)
			{
		    	inheritedBO3Loaded = true;

		    	if(!blocksOnly)
		    	{
	    			inheritedBO3s.addAll(((BO4)parentBO3).getSettings().getInheritedBO3s());
	
					removeAir = ((BO4)parentBO3).getSettings().removeAir;
					replaceAbove = replaceAbove == null || replaceAbove.length() == 0 ? ((BO4)parentBO3).getSettings().replaceAbove : replaceAbove;
					replaceBelow = replaceBelow == null || replaceBelow.length() == 0 ? ((BO4)parentBO3).getSettings().replaceBelow : replaceBelow;
	
					BO4CustomStructureCoordinate rotatedParentMaxCoords = BO4CustomStructureCoordinate.getRotatedBO3Coords(((BO4)parentBO3).getSettings().maxX, ((BO4)parentBO3).getSettings().maxY, ((BO4)parentBO3).getSettings().maxZ, inheritBO3Rotation);
					BO4CustomStructureCoordinate rotatedParentMinCoords = BO4CustomStructureCoordinate.getRotatedBO3Coords(((BO4)parentBO3).getSettings().minX, ((BO4)parentBO3).getSettings().minY, ((BO4)parentBO3).getSettings().minZ, inheritBO3Rotation);
	
					int parentMaxX = rotatedParentMaxCoords.getX() > rotatedParentMinCoords.getX() ? rotatedParentMaxCoords.getX() : rotatedParentMinCoords.getX();
					int parentMinX = rotatedParentMaxCoords.getX() < rotatedParentMinCoords.getX() ? rotatedParentMaxCoords.getX() : rotatedParentMinCoords.getX();
	
					int parentMaxY = rotatedParentMaxCoords.getY() > rotatedParentMinCoords.getY() ? rotatedParentMaxCoords.getY() : rotatedParentMinCoords.getY();
					int parentMinY = rotatedParentMaxCoords.getY() < rotatedParentMinCoords.getY() ? rotatedParentMaxCoords.getY() : rotatedParentMinCoords.getY();
	
					int parentMaxZ = rotatedParentMaxCoords.getZ() > rotatedParentMinCoords.getZ() ? rotatedParentMaxCoords.getZ() : rotatedParentMinCoords.getZ();
					int parentMinZ = rotatedParentMaxCoords.getZ() < rotatedParentMinCoords.getZ() ? rotatedParentMaxCoords.getZ() : rotatedParentMinCoords.getZ();
	
					if(parentMaxX > this.maxX)
					{
						this.maxX = parentMaxX;
					}
					if(parentMinX < this.minX)
					{
						this.minX = parentMinX;
					}
					if(parentMaxY > this.maxY)
					{
						this.maxY = parentMaxY;
					}
					if(parentMinY < this.minY)
					{
						this.minY = parentMinY;
					}
					if(parentMaxZ > this.maxZ)
					{
						this.maxZ = parentMaxZ;
					}
					if(parentMinZ < this.minZ)
					{
						this.minZ = parentMinZ;
					}
		    	}

				if(blocksOnly)
				{
					ArrayList<BO4BlockFunction> newBlocks = new ArrayList<BO4BlockFunction>();
					if(this.blocksOTGPlus != null)
					{
						for(BO4BlockFunction block : this.blocksOTGPlus)
						{
							newBlocks.add(block);
						}
					}
					for(BO4BlockFunction block : ((BO4)parentBO3).getSettings().getBlocks().clone())
					{
						newBlocks.add(block.rotate(inheritBO3Rotation));
					}
					this.blocksOTGPlus = newBlocks.toArray(new BO4BlockFunction[newBlocks.size()]);
				}

				if(!blocksOnly)
				{
					ArrayList<BO4BranchFunction> newBranches = new ArrayList<BO4BranchFunction>();
					if(this.branchesOTGPlus != null)
					{
						for(BO4BranchFunction branch : this.branchesOTGPlus)
						{
							newBranches.add(branch);
						}
					}
					for(BO4BranchFunction branch : ((BO4)parentBO3).getSettings().branchesOTGPlus.clone())
					{
						newBranches.add(branch.rotate(inheritBO3Rotation));
					}
					this.branchesOTGPlus = newBranches.toArray(new BO4BranchFunction[newBranches.size()]);
	
					ArrayList<BO4ModDataFunction> newModData = new ArrayList<BO4ModDataFunction>();
					if(this.modDataOTGPlus != null)
					{
						for(BO4ModDataFunction modData : this.modDataOTGPlus)
						{
							newModData.add(modData);
						}
					}
					for(BO4ModDataFunction modData : ((BO4)parentBO3).getSettings().modDataOTGPlus.clone())
					{
						newModData.add(modData.rotate(inheritBO3Rotation));
					}
					this.modDataOTGPlus = newModData.toArray(new BO4ModDataFunction[newModData.size()]);
	
					ArrayList<BO4SpawnerFunction> newSpawnerData = new ArrayList<BO4SpawnerFunction>();
					if(this.spawnerDataOTGPlus != null)
					{
						for(BO4SpawnerFunction spawnerData : this.spawnerDataOTGPlus)
						{
							newSpawnerData.add(spawnerData);
						}
					}
					for(BO4SpawnerFunction spawnerData : ((BO4)parentBO3).getSettings().spawnerDataOTGPlus.clone())
					{
						newSpawnerData.add(spawnerData.rotate(inheritBO3Rotation));
					}
					this.spawnerDataOTGPlus = newSpawnerData.toArray(new BO4SpawnerFunction[newSpawnerData.size()]);
	
					ArrayList<BO4ParticleFunction> newParticleData = new ArrayList<BO4ParticleFunction>();
					if(this.particleDataOTGPlus != null)
					{
						for(BO4ParticleFunction particleData : this.particleDataOTGPlus)
						{
							newParticleData.add(particleData);
						}
					}
					for(BO4ParticleFunction particleData : ((BO4)parentBO3).getSettings().particleDataOTGPlus.clone())
					{
						newParticleData.add(particleData.rotate(inheritBO3Rotation));
					}
					this.particleDataOTGPlus = newParticleData.toArray(new BO4ParticleFunction[newParticleData.size()]);
	
					ArrayList<BO4EntityFunction> newEntityData = new ArrayList<BO4EntityFunction>();
					if(this.entityDataOTGPlus != null)
					{
						for(BO4EntityFunction entityData : this.entityDataOTGPlus)
						{
							newEntityData.add(entityData);
						}
					}
					for(BO4EntityFunction entityData : ((BO4)parentBO3).getSettings().entityDataOTGPlus.clone())
					{
						newEntityData.add(entityData.rotate(inheritBO3Rotation));
					}
					this.entityDataOTGPlus = newEntityData.toArray(new BO4EntityFunction[newEntityData.size()]);
		
					inheritedBO3s.addAll(((BO4)parentBO3).getSettings().getInheritedBO3s());
				}
			}
	    	if(!inheritedBO3Loaded)
	    	{
	    		if(OTG.getPluginConfig().spawnLog)
	    		{
	    			OTG.log(LogMarker.WARN, "could not load BO3 parent for InheritBO3: " + this.inheritBO3 + " in BO3 " + this.getName());
	    		}
	    	}
    	}
    }

    private void readResources(boolean blocksOnly) throws InvalidConfigException
    {
        List<BO4BlockFunction> tempBlocksList = new ArrayList<BO4BlockFunction>();
        List<BO4BranchFunction> tempBranchesList = new ArrayList<BO4BranchFunction>();
        List<BO4EntityFunction> tempEntitiesList = new ArrayList<BO4EntityFunction>();
        List<BO4ModDataFunction> tempModDataList = new ArrayList<BO4ModDataFunction>();
        List<BO4ParticleFunction> tempParticlesList = new ArrayList<BO4ParticleFunction>();
        List<BO4SpawnerFunction> tempSpawnerList = new ArrayList<BO4SpawnerFunction>();

        for (CustomObjectConfigFunction<BO4Config> res : reader.getConfigFunctions(this, true))
        {
            if (res.isValid())
            {
                if (res instanceof BO4BlockFunction || res instanceof BO4RandomBlockFunction) // TODO: check for RandomBlockFunction seems redundant here?
                {
            		if(res instanceof BO4RandomBlockFunction)
            		{
            			BO4RandomBlockFunction newRes = new BO4RandomBlockFunction();
            			newRes.blocks = ((BO4RandomBlockFunction)res).blocks;
            			newRes.blockChances = ((BO4RandomBlockFunction)res).blockChances;
            			newRes.blockCount = ((BO4RandomBlockFunction)res).blockCount;
                    	newRes.x = ((BO4RandomBlockFunction)res).x;
                    	newRes.y = ((BO4RandomBlockFunction)res).y;
                    	newRes.z = ((BO4RandomBlockFunction)res).z;
                    	newRes.metaDataTag = ((BO4RandomBlockFunction)res).metaDataTag;
                    	newRes.metaDataTags = ((BO4RandomBlockFunction)res).metaDataTags;
                    	newRes.metaDataName = ((BO4RandomBlockFunction)res).metaDataName;
                    	newRes.metaDataNames = ((BO4RandomBlockFunction)res).metaDataNames;
                    	newRes.material = ((BO4RandomBlockFunction)res).material;
                    	tempBlocksList.add(newRes);
            		} else {
            			if(!this.removeAir || !((BO4BlockFunction)res).material.toDefaultMaterial().equals(DefaultMaterial.AIR))
            			{
	                    	BO4BlockFunction newRes = new BO4BlockFunction();
	                    	newRes.x = ((BO4BlockFunction)res).x;
	                    	newRes.y = ((BO4BlockFunction)res).y;
	                    	newRes.z = ((BO4BlockFunction)res).z;
	                    	newRes.metaDataTag = ((BO4BlockFunction)res).metaDataTag;
	                    	newRes.metaDataName = ((BO4BlockFunction)res).metaDataName;
	                    	newRes.material = ((BO4BlockFunction)res).material;
	                    	tempBlocksList.add(newRes);
            			}
            		}

            		if(!blocksOnly)
            		{
	                	// Get the real size of this BO3
	                	if(((BO4BlockFunction)res).x < minX)
	                	{
	                		minX = ((BO4BlockFunction)res).x;
	                	}
	                	if(((BO4BlockFunction)res).x > maxX)
	                	{
	                		maxX = ((BO4BlockFunction)res).x;
	                	}
	                	if(((BO4BlockFunction)res).y < minY)
	                	{
	                		minY = ((BO4BlockFunction)res).y;
	                	}
	                	if(((BO4BlockFunction)res).y > maxY)
	                	{
	                		maxY = ((BO4BlockFunction)res).y;
	                	}
	                	if(((BO4BlockFunction)res).z < minZ)
	                	{
	                		minZ = ((BO4BlockFunction)res).z;
	                	}
	                	if(((BO4BlockFunction)res).z > maxZ)
	                	{
	                		maxZ = ((BO4BlockFunction)res).z;
	                	}
            		}
                }
                else if(!blocksOnly)
                {
	                if (res instanceof BO4WeightedBranchFunction)
	                {
	                    tempBranchesList.add((BO4WeightedBranchFunction) res);
	                }
	                else if (res instanceof BO4BranchFunction)
	                {
	                	tempBranchesList.add((BO4BranchFunction) res);
	                }
	                else if (res instanceof BO4ModDataFunction)
	                {
	                    tempModDataList.add((BO4ModDataFunction) res);
	                }
	                else if (res instanceof BO4SpawnerFunction)
	                {
	                	tempSpawnerList.add((BO4SpawnerFunction) res);
	                }
	                else if (res instanceof BO4ParticleFunction)
	                {
	                	tempParticlesList.add((BO4ParticleFunction) res);
	                }
	                else if (res instanceof BO4EntityFunction)
	                {
	                	tempEntitiesList.add((BO4EntityFunction) res);
	                }
                }
            }
        }

        if(!blocksOnly)
        {
	        if(minX == Integer.MAX_VALUE)
	        {
	        	minX = -8;
	        }
	        if(maxX == Integer.MIN_VALUE)
	        {
	        	maxX = -8;
	        }
	        if(minY == Integer.MAX_VALUE)
	        {
	        	minY = 0;
	        }
	        if(maxY == Integer.MIN_VALUE)
	        {
	        	maxY = 0;
	        }
	        if(minZ == Integer.MAX_VALUE)
	        {
	        	minZ = -7;
	        }
	        if(maxZ == Integer.MIN_VALUE)
	        {
	        	maxZ = -7;
	        }
        }

        // TODO: OTG+ Doesn't do CustomObject BO3's, only check for 16x16, not 32x32?
        boolean illegalBlock = false;
        if(blocksOnly)
        {
	        for(BO4BlockFunction block : tempBlocksList)
	        {
	    		block.x += this.getXOffset();
	    		block.z += this.getZOffset();

        		if(block.x > 15 || block.z > 15)
        		{
        			illegalBlock = true;
        		}

	    		if(block.x < 0 || block.z < 0)
	    		{
	    			illegalBlock = true;
	    		}
	        }
	        try
	        {
	        	blocksOTGPlus = tempBlocksList.toArray(new BO4BlockFunction[tempBlocksList.size()]);
	        }
	        catch(java.lang.ArrayStoreException ex)
	        {
	        	blocksOTGPlus = tempBlocksList.toArray(new BO4BlockFunction[tempBlocksList.size()]);
	        }
        }

        if(!blocksOnly)
        {
			boolean illegalModData = false;
	        for(BO4ModDataFunction modData : tempModDataList)
	        {
	        	// This is done when reading blocks so should also be done for blockchecks and moddata!

				modData.x += this.getXOffset();
				modData.z += this.getZOffset();

        		if(modData.x > 15 || modData.z > 15)
        		{
        			illegalModData = true;
        		}

	    		if(modData.x < 0 || modData.z < 0)
	    		{
	    			illegalModData = true;
	    		}
	        }
	        modDataOTGPlus = tempModDataList.toArray(new BO4ModDataFunction[tempModDataList.size()]);

			boolean illegalSpawnerData = false;
	        for(BO4SpawnerFunction spawnerData : tempSpawnerList)
	        {
	        	// This is done when reading blocks so should also be done for blockchecks and moddata!

	        	spawnerData.x += this.getXOffset();
	        	spawnerData.z += this.getZOffset();

        		if(spawnerData.x > 15 || spawnerData.z > 15)
        		{
        			illegalSpawnerData = true;
        		}

	    		if(spawnerData.x < 0 || spawnerData.z < 0)
	    		{
	    			illegalSpawnerData = true;
	    		}
	        }
	        spawnerDataOTGPlus = tempSpawnerList.toArray(new BO4SpawnerFunction[tempSpawnerList.size()]);

			boolean illegalParticleData = false;
	        for(BO4ParticleFunction particleData : tempParticlesList)
	        {
	        	// This is done when reading blocks so should also be done for blockchecks and moddata!

	        	particleData.x += this.getXOffset();
	        	particleData.z += this.getZOffset();

        		if(particleData.x > 15 || particleData.z > 15)
        		{
        			illegalParticleData = true;
        		}

	    		if(particleData.x < 0 || particleData.z < 0)
	    		{
	    			illegalParticleData = true;
	    		}
	        }
	        particleDataOTGPlus = tempParticlesList.toArray(new BO4ParticleFunction[tempParticlesList.size()]);

			boolean illegalEntityData = false;
	        for(BO4EntityFunction entityData : tempEntitiesList)
	        {
	        	// This is done when reading blocks so should also be done for blockchecks and moddata!

	        	entityData.x += this.getXOffset();
	        	entityData.z += this.getZOffset();

        		if(entityData.x > 15 || entityData.z > 15)
        		{
        			illegalEntityData = true;
        		}

	    		if(entityData.x < 0 || entityData.z < 0)
	    		{
	    			illegalEntityData = true;
	    		}
	        }
	        entityDataOTGPlus = tempEntitiesList.toArray(new BO4EntityFunction[tempEntitiesList.size()]);

			if(OTG.getPluginConfig().spawnLog)
			{
				if(illegalBlock)
				{
					OTG.log(LogMarker.WARN, "Warning: BO3 contains Blocks or RandomBlocks that are placed outside the chunk(s) that the BO3 will be placed in. This can slow down world generation. BO3: " + this.getName());
				}
				if(illegalModData)
				{
					OTG.log(LogMarker.WARN, "Warning: BO3 contains ModData that may be placed outside the chunk(s) that the BO3 will be placed in. This can slow down world generation. BO3: " + this.getName());
				}
				if(illegalSpawnerData)
				{
					OTG.log(LogMarker.WARN, "Warning: BO3 contains a Spawner() that may be placed outside the chunk(s) that the BO3 will be placed in. This can slow down world generation. BO3: " + this.getName());
				}
				if(illegalParticleData)
				{
					OTG.log(LogMarker.WARN, "Warning: BO3 contains a Particle() that may be placed outside the chunk(s) that the BO3 will be placed in. This can slow down world generation. BO3: " + this.getName());
				}
				if(illegalEntityData)
				{
					OTG.log(LogMarker.WARN, "Warning: BO3 contains an Entity() that may be placed outside the chunk(s) that the BO3 will be placed in. This can slow down world generation. BO3: " + this.getName());
				}
			}

	        // Store the blocks
	        branchesOTGPlus = tempBranchesList.toArray(new BO4BranchFunction[tempBranchesList.size()]);

	    	if(branchesOTGPlus.length > 0) // If this BO3 has branches then it must be max 16x16
	    	{
	    		if(Math.abs(minX - maxX) > 15 || Math.abs(minZ - maxZ) > 15)
	    		{
	    			OTG.log(LogMarker.INFO, "BO3 " + this.getName() + " was too large, branching BO3's can be max 16x16 blocks.");
	    			throw new InvalidConfigException("BO3 " + this.getName() + " was too large, branching BO3's can be max 16x16 blocks.");
	    		}
	    	} else {
	    		if(Math.abs(minX - maxX) > 15 || Math.abs(minZ - maxZ) > 15) // If this BO3 is larger than 16x16 then it can only be used as a customObject
	    		{
	    			OTG.log(LogMarker.INFO, "BO4 " + this.getName() + " was too large, BO4's used as CustomStructure() can be max 16x16 blocks.");
	    			throw new InvalidConfigException("BO4 " + this.getName() + " was too large, BO4's used as CustomStructure() can be max 16x16 blocks.");
	    		}
	    	}
        }
    	//} else {

    		// These lists are primarily used by non-OTG+ BO3's. For OTG+ BO3's these lists are used only when writing to the bo3, after that they are discarded.

        	tempBlocksList = new ArrayList<BO4BlockFunction>();
        	tempBranchesList = new ArrayList<BO4BranchFunction>();
        	tempEntitiesList = new ArrayList<BO4EntityFunction>();
        	tempModDataList = new ArrayList<BO4ModDataFunction>();
        	tempParticlesList = new ArrayList<BO4ParticleFunction>();
        	tempSpawnerList = new ArrayList<BO4SpawnerFunction>();

            for (CustomObjectConfigFunction<BO4Config> res : reader.getConfigFunctions((BO4Config)this, true))
            {
                if (res instanceof BO4BlockFunction)
                {
                	BO4BlockFunction block = (BO4BlockFunction) res;
                    tempBlocksList.add(block);
                }
                else if(!blocksOnly)
                {
	                if (res instanceof BO4WeightedBranchFunction)
	                {
	                    tempBranchesList.add((BO4WeightedBranchFunction) res);
	                }
	                else if (res instanceof BO4BranchFunction)
	                {
	                    tempBranchesList.add((BO4BranchFunction) res);
	                }
	                else if (res instanceof BO4EntityFunction)
	                {
	                    tempEntitiesList.add((BO4EntityFunction) res);
	                }
	                else if (res instanceof BO4ParticleFunction)
	                {
	                	tempParticlesList.add((BO4ParticleFunction) res);
	                }
	                else if (res instanceof BO4ModDataFunction)
	                {
	                	tempModDataList.add((BO4ModDataFunction) res);
	                }
	                else if (res instanceof BO4SpawnerFunction)
	                {
	                	tempSpawnerList.add((BO4SpawnerFunction) res);
	                }
                }
            }

            // Store the blocks
            blocks[0] = tempBlocksList.toArray(new BO4BlockFunction[tempBlocksList.size()]);
            if(!blocksOnly)
            {
	            branches[0] = tempBranchesList.toArray(new BO4BranchFunction[tempBranchesList.size()]);
	            entityFunctions[0] = tempEntitiesList.toArray(new BO4EntityFunction[tempEntitiesList.size()]);
	            particleFunctions[0] = tempParticlesList.toArray(new BO4ParticleFunction[tempParticlesList.size()]);
	            modDataFunctions[0] = tempModDataList.toArray(new BO4ModDataFunction[tempModDataList.size()]);
	            spawnerFunctions[0] = tempSpawnerList.toArray(new BO4SpawnerFunction[tempSpawnerList.size()]);
            }
    	//}
    }

    /**
     * Gets the file this config will be written to. May be null if the config
     * will never be written.
     * @return The file.
     */
    public File getFile()
    {
    	return this.reader.getFile();
    }

    @Override
    protected void writeConfigSettings(SettingsWriterOTGPlus writer) throws IOException
    {
        // The object
        writer.bigTitle("BO3 object");
        writer.comment("This is the config file of a custom object.");
		writer.comment("If you add this object correctly to your BiomeConfigs, it will spawn in the world.");
		writer.comment("");

		writer.comment("This is the creator of this BO3 object");
        writer.setting(BO4Settings.AUTHOR, author);

        writer.comment("A short description of this BO3 object");
        writer.setting(BO4Settings.DESCRIPTION, description);

        writer.comment("The BO3 version, don't change this! It can be used by external applications to do a version check.");
        writer.setting(BO4Settings.VERSION, "3");

        writer.comment("The settings mode, WriteAll, WriteWithoutComments or WriteDisable. See WorldConfig.");
        writer.setting(WorldStandardValues.SETTINGS_MODE_BO3, settingsMode);

        // Main settings
        writer.bigTitle("Main settings");

		writer.comment("This BO3 can only spawn at least Frequency chunks distance away from any other BO3 with the exact same name.");
		writer.comment("You can use this to make this BO3 spawn in groups or make sure that this BO3 only spawns once every X chunks.");
        writer.setting(BO4Settings.FREQUENCY, frequency);

		writer.comment("If you set this to true, the BO3 will be placed with a random rotation.");
		writer.comment("This is broken for BO4's atm, will fix this a.s.a.p.");
        writer.setting(BO4Settings.ROTATE_RANDOMLY, rotateRandomly);

        writer.comment("The spawn height of the BO3: randomY, highestBlock or highestSolidBlock.");
        writer.setting(BO3Settings.SPAWN_HEIGHT, spawnHeight);

        writer.smallTitle("Height Limits for the BO3.");

		writer.comment("When in randomY mode used as the minimum Y or in atMinY mode as the actual Y to spawn this BO3 at.");
        writer.setting(BO4Settings.MIN_HEIGHT, minHeight);

		writer.comment("When in randomY mode used as the maximum Y to spawn this BO3 at.");
        writer.setting(BO4Settings.MAX_HEIGHT, maxHeight);

        // Sourceblock
        writer.bigTitle("Source block settings");

        writer.comment("OTG+ settings #");

        writer.comment("Copies the blocks and branches of an existing BO3 into this BO3. You can still add blocks and branches in this BO3, they will be added on top of the inherited blocks and branches.");
        writer.setting(BO4Settings.INHERITBO3, inheritBO3);
        writer.comment("Rotates the inheritedBO3's resources (blocks, spawners, checks etc) and branches, defaults to NORTH (no rotation).");
        writer.setting(BO4Settings.INHERITBO3ROTATION, inheritBO3Rotation);

        writer.comment("Defaults to true, if true and this is the starting BO3 for this branching structure then this BO3's smoothing and height settings are used for all children (branches).");
        writer.setting(BO4Settings.OVERRIDECHILDSETTINGS, overrideChildSettings);
        writer.comment("Defaults to false, if true then this branch uses it's own height settings (SpawnHeight, minHeight, maxHeight, spawnAtWaterLevel) instead of those defined in the starting BO3 for this branching structure.");
        writer.setting(BO4Settings.OVERRIDEPARENTHEIGHT, overrideParentHeight);
        writer.comment("If this is set to true then this BO3 can spawn on top of or inside an existing BO3. If this is set to false then this BO3 will use a bounding box to detect collisions with other BO3's, if a collision is detected then this BO3 won't spawn and the current branch is rolled back.");
        writer.setting(BO4Settings.CANOVERRIDE, canOverride);

        writer.comment("This branch can only spawn at least branchFrequency chunks (x,z) distance away from any other branch with the exact same name.");
        writer.setting(BO4Settings.BRANCH_FREQUENCY, branchFrequency);
        writer.comment("Define groups that this branch belongs to along with a minimum (x,z) range in chunks that this branch must have between it and any other members of this group if it is to be allowed to spawn. Syntax is \"GroupName:Frequency, GoupName2:Frequency2\" etc so for example a branch that belongs to 3 groups: \"BranchFrequencyGroup: Ships:10, Vehicles:5, FloatingThings:3\".");
        writer.setting(BO4Settings.BRANCH_FREQUENCY_GROUP, branchFrequencyGroup);

        writer.comment("If this is set to true then this BO3 can only spawn underneath an existing BO3. Used to make sure that dungeons only appear underneath buildings.");
        writer.setting(BO4Settings.MUSTBEBELOWOTHER, mustBeBelowOther);

        writer.comment("Used with CanOverride: true. A comma-seperated list of BO3s, this BO3's bounding box must collide with one of the BO3's in the list or this BO3 fails to spawn and the current branch is rolled back.");
        writer.setting(BO4Settings.MUSTBEINSIDE, mustBeInside);

        writer.comment("Used with CanOverride: true. A comma-seperated list of BO3s, this BO3's bounding box cannot collide with any of the BO3's in the list or this BO3 fails to spawn and the current branch is rolled back.");
        writer.setting(BO4Settings.CANNOTBEINSIDE, cannotBeInside);

        writer.comment("Used with CanOverride: true. A comma-seperated list of BO3s, if this BO3's bounding box collides with any of the BO3's in the list then those BO3's won't spawn any blocks. This does not remove or roll back any BO3's.");
        writer.setting(BO4Settings.REPLACESBO3, replacesBO3);

        writer.comment("Defaults to true. Set to false if the BO3 is not allowed to spawn on a water block");
        writer.setting(BO4Settings.CANSPAWNONWATER, canSpawnOnWater);

        writer.comment("Defaults to false. Set to true if the BO3 is allowed to spawn only on a water block");
        writer.setting(BO4Settings.SPAWNONWATERONLY, spawnOnWaterOnly);

        writer.comment("Defaults to false. Set to true if the BO3 and its smoothing area should ignore water when looking for the highest block to spawn on. Defaults to false (things spawn on top of water)");
        writer.setting(BO4Settings.SPAWNUNDERWATER, spawnUnderWater);

        writer.comment("Defaults to false. Set to true if the BO3 should spawn at water level");
        writer.setting(BO4Settings.SPAWNATWATERLEVEL, spawnAtWaterLevel);

        writer.comment("Spawns the BO3 at a Y offset of this value. Handy when using highestBlock for lowering BO3s into the surrounding terrain when there are layers of ground included in the BO3, also handy when using SpawnAtWaterLevel to lower objects like ships into the water.");
        writer.setting(BO4Settings.HEIGHT_OFFSET, heightOffset);

        writer.comment("If set to true removes all AIR blocks from the BO3 so that it can be flooded or buried.");
        writer.setting(BO4Settings.REMOVEAIR, removeAir);

        writer.comment("Replaces all the non-air blocks that are above this BO3 or its smoothing area with the given block material (should be WATER or AIR or NONE), also applies to smoothing areas although OTG intentionally leaves some of the terrain above them intact. WATER can be used in combination with SpawnUnderWater to fill any air blocks underneath waterlevel with water (and any above waterlevel with air).");
        writer.setting(BO4Settings.REPLACEABOVE, replaceAbove);

        writer.comment("Replaces all air blocks underneath the BO3 (but not its smoothing area) with the specified material until a solid block is found.");
        writer.setting(BO4Settings.REPLACEBELOW, replaceBelow);

        writer.comment("Defaults to true. If set to true then every block in the BO3 of the materials defined in ReplaceWithGroundBlock or ReplaceWithSurfaceBlock will be replaced by the GroundBlock or SurfaceBlock materials configured for the biome the block is spawned in.");
        writer.setting(BO4Settings.REPLACEWITHBIOMEBLOCKS, replaceWithBiomeBlocks);

        writer.comment("Defaults to DIRT, Replaces all the blocks of the given material in the BO3 with the GroundBlock configured for the biome it spawns in.");
        writer.setting(BO4Settings.REPLACEWITHGROUNDBLOCK, replaceWithGroundBlock);

        writer.comment("Defaults to GRASS, Replaces all the blocks of the given material in the BO3 with the SurfaceBlock configured for the biome it spawns in.");
        writer.setting(BO4Settings.REPLACEWITHSURFACEBLOCK, replaceWithSurfaceBlock);

        writer.comment("Makes the terrain around the BO3 slope evenly towards the edges of the BO3. The given value is the distance in blocks around the BO3 from where the slope should start and can be any positive number.");
        writer.setting(BO4Settings.SMOOTHRADIUS, smoothRadius);

        writer.comment("Moves the smoothing area up or down relative to the BO3 (at the points where the smoothing area is connected to the BO3). Handy when using SmoothStartTop: false and the BO3 has some layers of ground included, in that case we can set the HeightOffset to a negative value to lower the BO3 into the ground and we can set the SmoothHeightOffset to a positive value to move the smoothing area starting height up.");
        writer.setting(BO4Settings.SMOOTH_HEIGHT_OFFSET, smoothHeightOffset);

        writer.comment("Should the smoothing area be attached at the bottom or the top of the edges of the BO3? Defaults to false (bottom). Using this setting can make things slower so try to avoid using it and use SmoothHeightOffset instead if for instance you have a BO3 with some ground layers included. The only reason you should need to use this setting is if you have a BO3 with edges that have an irregular height (like some hills).");
        writer.setting(BO4Settings.SMOOTHSTARTTOP, smoothStartTop);

        writer.comment("Should the smoothing area attach itself to \"log\" block or ignore them? Defaults to false (ignore logs).");
        writer.setting(BO4Settings.SMOOTHSTARTWOOD, smoothStartWood);

        writer.comment("The block used for smoothing area surface blocks, defaults to biome SurfaceBlock.");
        writer.setting(BO4Settings.SMOOTHINGSURFACEBLOCK, smoothingSurfaceBlock);

        writer.comment("The block used for smoothing area ground blocks, defaults to biome GroundBlock.");
        writer.setting(BO4Settings.SMOOTHINGGROUNDBLOCK, smoothingGroundBlock);

        writer.comment("Define groups that this BO3 belongs to along with a minimum range in chunks that this BO3 must have between it and any other members of this group if it is to be allowed to spawn. Syntax is \"GroupName:Frequency, GoupName2:Frequency2\" etc so for example a BO3 that belongs to 3 groups: \"BO3Group: Ships:10, Vehicles:5, FloatingThings:3\".");
		writer.setting(BO4Settings.BO3GROUP, bo3Group);

        writer.comment("Defaults to false. Set to true if this BO3 should spawn at the player spawn point. When the server starts the spawn point is determined and the BO3's for the biome it is in are loaded, one of these BO3s that has IsSpawnPoint set to true (if any) is selected randomly and is spawned at the spawn point regardless of its rarity (so even Rarity:0, IsSpawnPoint: true BO3's can get spawned as the spawn point!).");
        writer.setting(BO4Settings.ISSPAWNPOINT, isSpawnPoint);

        // Blocks and other things
        writeResources(writer);
    }

    @Override
    protected void readConfigSettings() throws InvalidConfigException
    {
        branchFrequency = readSettings(BO4Settings.BRANCH_FREQUENCY);
        
        branchFrequencyGroup = readSettings(BO4Settings.BRANCH_FREQUENCY_GROUP);
        branchFrequencyGroups = new HashMap<String, Integer>();
        if(branchFrequencyGroup != null && branchFrequencyGroup.trim().length() > 0)
        {
	        String[] groupStrings = branchFrequencyGroup.split(",");
	        if(groupStrings != null && groupStrings.length > 0)
	        {
	        	for(int i = 0; i < groupStrings.length; i++)
	        	{
	            	String[] groupString = groupStrings[i].trim().length() > 0 ? groupStrings[i].split(":") : null;
	            	if(groupString != null && groupString.length == 2)
	            	{
	            		branchFrequencyGroups.put(groupString[0].trim(), Integer.parseInt(groupString[1].trim()));
	            	}
	        	}
	        }
        }
        
        heightOffset = readSettings(BO4Settings.HEIGHT_OFFSET);
        inheritBO3Rotation = readSettings(BO4Settings.INHERITBO3ROTATION);

        removeAir = readSettings(BO4Settings.REMOVEAIR);
        isSpawnPoint = readSettings(BO4Settings.ISSPAWNPOINT);
        replaceAbove = readSettings(BO4Settings.REPLACEABOVE);
        replaceBelow = readSettings(BO4Settings.REPLACEBELOW);
        replaceWithBiomeBlocks = readSettings(BO4Settings.REPLACEWITHBIOMEBLOCKS);
        replaceWithGroundBlock = readSettings(BO4Settings.REPLACEWITHGROUNDBLOCK);
        replaceWithSurfaceBlock = readSettings(BO4Settings.REPLACEWITHSURFACEBLOCK);
        
        bo3Group = readSettings(BO4Settings.BO3GROUP);
        bo3Groups = new HashMap<String, Integer>();
        if(bo3Group != null && bo3Group.trim().length() > 0)
        {
	        String[] groupStrings = bo3Group.split(",");
	        if(groupStrings != null && groupStrings.length > 0)
	        {
	        	for(int i = 0; i < groupStrings.length; i++)
	        	{
	            	String[] groupString = groupStrings[i].trim().length() > 0 ? groupStrings[i].split(":") : null;
	            	if(groupString != null && groupString.length == 2)
	            	{
	            		bo3Groups.put(groupString[0].trim(), Integer.parseInt(groupString[1].trim()));
	            	}
	        	}
	        }
        }
        
        canOverride = readSettings(BO4Settings.CANOVERRIDE);
        mustBeBelowOther = readSettings(BO4Settings.MUSTBEBELOWOTHER);
        
        mustBeInside = readSettings(BO4Settings.MUSTBEINSIDE);
        mustBeInsideBranches = new ArrayList<String>();
        if(mustBeInside != null && mustBeInside.trim().length() > 0)
        {
	        String[] mustBeInsideStrings = mustBeInside.split(",");
	        if(mustBeInsideStrings != null && mustBeInsideStrings.length > 0)
	        {
	        	for(int i = 0; i < mustBeInsideStrings.length; i++)
	        	{
	            	String mustBeInsideString = mustBeInsideStrings[i].trim();
	            	if(mustBeInsideString.length() > 0)
	            	{
            			mustBeInsideBranches.add(mustBeInsideString);
	            	}
	        	}
	        }
        }
        
        cannotBeInside =  readSettings(BO4Settings.CANNOTBEINSIDE);
        cannotBeInsideBranches = new ArrayList<String>();
        if(cannotBeInside != null && cannotBeInside.trim().length() > 0)
        {
	        String[] cannotBeInsideStrings = cannotBeInside.split(",");
	        if(cannotBeInsideStrings != null && cannotBeInsideStrings.length > 0)
	        {
	        	for(int i = 0; i < cannotBeInsideStrings.length; i++)
	        	{
	            	String cannotBeInsideString = cannotBeInsideStrings[i].trim();
	            	if(cannotBeInsideString.length() > 0)
	            	{
	            		cannotBeInsideBranches.add(cannotBeInsideString);
	            	}
	        	}
	        }
        }
        
        replacesBO3 = readSettings(BO4Settings.REPLACESBO3);
        replacesBO3Branches = new ArrayList<String>();
        if(replacesBO3 != null && replacesBO3.trim().length() > 0)
        {
	        String[] replacesBO3Strings = replacesBO3.split(",");
	        if(replacesBO3Strings != null && replacesBO3Strings.length > 0)
	        {
	        	for(int i = 0; i < replacesBO3Strings.length; i++)
	        	{
	            	String replacesBO3String = replacesBO3Strings[i].trim();
	            	if(replacesBO3String.length() > 0)
	            	{
	            		replacesBO3Branches.add(replacesBO3String);
	            	}
	        	}
	        }
        }

        //smoothHeightOffset = readSettings(BO3Settings.SMOOTH_HEIGHT_OFFSET).equals("HeightOffset") ? heightOffset : Integer.parseInt(readSettings(BO3Settings.SMOOTH_HEIGHT_OFFSET));
        smoothHeightOffset = readSettings(BO4Settings.SMOOTH_HEIGHT_OFFSET);
        canSpawnOnWater = readSettings(BO4Settings.CANSPAWNONWATER);
        spawnOnWaterOnly = readSettings(BO4Settings.SPAWNONWATERONLY);
        spawnUnderWater = readSettings(BO4Settings.SPAWNUNDERWATER);
        spawnAtWaterLevel = readSettings(BO4Settings.SPAWNATWATERLEVEL);
        inheritBO3 = readSettings(BO4Settings.INHERITBO3);
        overrideChildSettings = readSettings(BO4Settings.OVERRIDECHILDSETTINGS);
        overrideParentHeight = readSettings(BO4Settings.OVERRIDEPARENTHEIGHT);
        smoothRadius = readSettings(BO4Settings.SMOOTHRADIUS);
        smoothStartTop = readSettings(BO4Settings.SMOOTHSTARTTOP);
        smoothStartWood = readSettings(BO4Settings.SMOOTHSTARTWOOD);
        smoothingSurfaceBlock = readSettings(BO4Settings.SMOOTHINGSURFACEBLOCK);
        smoothingGroundBlock = readSettings(BO4Settings.SMOOTHINGGROUNDBLOCK);

        // Make sure that the BO3 wont try to spawn below Y 0 because of the height offset
        if(heightOffset < 0 && minHeight < -heightOffset)
        {
        	minHeight = -heightOffset;
        }

		inheritedBO3s = new ArrayList<String>();
		inheritedBO3s.add(this.getName()); // TODO: Make this cleaner?
		if(inheritBO3 != null && inheritBO3.trim().length() > 0)
		{
			inheritedBO3s.add(inheritBO3);
		}

        author = readSettings(BO4Settings.AUTHOR);
        description = readSettings(BO4Settings.DESCRIPTION);
        settingsMode = readSettings(WorldStandardValues.SETTINGS_MODE_BO3);

        frequency = readSettings(BO4Settings.FREQUENCY);
        rotateRandomly = readSettings(BO4Settings.ROTATE_RANDOMLY);
        spawnHeight = readSettings(BO3Settings.SPAWN_HEIGHT);
        minHeight = readSettings(BO4Settings.MIN_HEIGHT);
        maxHeight = readSettings(BO4Settings.MAX_HEIGHT);
		maxHeight = maxHeight < minHeight ? minHeight : maxHeight;

        // Read the resources
        readResources(false);

        this.reader.flushCache();

    	// Merge inherited resources
       	loadInheritedBO3(false);
    }
    
    private void readBlocks()
    {    	
    	((FileSettingsReaderOTGPlus) this.reader).readSettings();
    	
    	 this.blocks = new BO4BlockFunction[4][];
    	
        // Read the resources
        try {
			readResources(true);
		} catch (InvalidConfigException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
        
        this.reader.flushCache();
        
    	// Merge inherited resources
       	loadInheritedBO3(true);
        
        this.blocks = null;
    }

    private void writeResources(SettingsWriterOTGPlus writer) throws IOException
    {
        writer.bigTitle("Blocks");
        writer.comment("All the blocks used in the BO3 are listed here. Possible blocks:");
        writer.comment("Block(x,y,z,id[.data][,nbtfile.nbt)");
        writer.comment("RandomBlock(x,y,z,id[:data][,nbtfile.nbt],chance[,id[:data][,nbtfile.nbt],chance[,...]])");
        writer.comment(" So RandomBlock(0,0,0,CHEST,chest.nbt,50,CHEST,anotherchest.nbt,100) will spawn a chest at");
        writer.comment(" the BO3 origin, and give it a 50% chance to have the contents of chest.nbt, or, if that");
        writer.comment(" fails, a 100% percent chance to have the contents of anotherchest.nbt.");
        writer.comment("MinecraftObject(x,y,z,name) (TODO: This may not work anymore and needs to be tested.");
        writer.comment(" Spawns an object in the Mojang NBT structure format. For example, ");
        writer.comment(" MinecraftObject(0,0,0," + DefaultStructurePart.IGLOO_BOTTOM.getPath() + ")");
        writer.comment(" spawns the bottom part of an igloo.");

        for(BO4BlockFunction func : Arrays.asList(blocks[0]))
        {
        	writer.function(func);
        }

        writer.bigTitle("Branches");
        writer.comment("Branches are child-BO3's that spawn if this BO3 is configured to spawn as a");
        writer.comment("CustomStructure resource in a biome config. Branches can have branches,");
        writer.comment("making complex structures possible. See the wiki for more details.");
        writer.comment("");
        writer.comment("Regular Branches spawn each branch with an independent chance of spawning.");
        writer.comment("Branch(x,y,z,isRequiredBranch,branchName,rotation,chance,branchDepth[,anotherBranchName,rotation,chance,branchDepth[,...]][IndividualChance])");
        writer.comment("branchName - name of the object to spawn.");
        writer.comment("rotation - NORTH, SOUTH, EAST or WEST.");
        writer.comment("IndividualChance - The chance each branch has to spawn, assumed to be 100 when left blank");
        writer.comment("isRequiredBranch - If this is set to true then at least one of the branches in this BO3 must spawn at these x,y,z coordinates. If no branch can spawn there then this BO3 fails to spawn and its branch is rolled back.");
        writer.comment("isRequiredBranch:true branches must spawn or the current branch is rolled back entirely. This is useful for grouping BO3's that must spawn together, for instance a single room made of multiple BO3's/branches.");
        writer.comment("If all parts of the room are connected together via isRequiredBranch:true branches then either the entire room will spawns or no part of it will spawn.");
        writer.comment("*Note: When isRequiredBranch:true only one BO3 can be added per Branch() and it will automatically have a rarity of 100.0.");
        writer.comment("isRequiredBranch:false branches are used to make optional parts of structures, for instance the middle section of a tunnel that has a beginning, middle and end BO3/branch and can have a variable length by repeating the middle BO3/branch.");
        writer.comment("By making the start and end branches isRequiredBranch:true and the middle branch isRequiredbranch:false you can make it so that either:");
		writer.comment("A. A tunnel spawns with at least a beginning and end branch");
		writer.comment("B. A tunnel spawns with a beginning and end branch and as many middle branches as will fit in the available space.");
		writer.comment("C. No tunnel spawns at all because there wasn't enough space to spawn at least a beginning and end branch.");
        writer.comment("branchDepth - When creating a chain of branches that contains optional (isRequiredBranch:false) branches branch depth is configured for the first BO3 in the chain to determine the maximum length of the chain.");
        writer.comment("branchDepth - 1 is inherited by each isRequiredBranch:false branch in the chain. When branchDepth is zero isRequiredBranch:false branches cannot spawn and the chain ends. In the case of the tunnel this means the last middle branch would be");
        writer.comment("rolled back and an IsRequiredBranch:true end branch could be spawned in its place to make sure the tunnel has a proper ending.");
        writer.comment("Instead of inheriting branchDepth - 1 from the parent branchDepth can be overridden by child branches if it is set higher than 0 (the default value).");
        writer.comment("isRequiredBranch:true branches do inherit branchDepth and pass it on to their own branches, however they cannot be prevented from spawning by it and also don't subtract 1 from branchDepth when inheriting it.");
        writer.comment("");
        writer.comment("Weighted Branches spawn branches with a dependent chance of spawning.");
        writer.comment("WeightedBranch(x,y,z,isRequiredBranch,branchName,rotation,chance,branchDepth[,anotherBranchName,rotation,chance,branchDepth[,...]][MaxChanceOutOf])");
        writer.comment("*Note: isRequiredBranch must be set to false. It is not possible to use isRequiredBranch:true with WeightedBranch() since isRequired:true branches must spawn and automatically have a rarity of 100.0.");
        writer.comment("MaxChanceOutOf - The chance all branches have to spawn out of, assumed to be 100 when left blank");

        for(BO4BranchFunction func : Arrays.asList(branches[0]))
        {
        	writer.function(func);
        }

        writer.bigTitle("Entities");
        writer.comment("Forge only (this may have changed, check for updates).");
        writer.comment("An EntityFunction spawns an entity instead of a block. The entity is spawned only once when the BO3 is spawned.");
        writer.comment("Entities are persistent by default so they don't de-spawn when no player is near, they are only unloaded.");
        writer.comment("Usage: Entity(x,y,z,entityName,groupSize,NameTagOrNBTFileName) or Entity(x,y,z,mobName,groupSize)");
        writer.comment("Use /otg entities to get a list of entities that can be used as entityName, this includes entities added by other mods and non-living entities.");
        writer.comment("NameTagOrNBTFileName can be either a nametag for the mob or an .txt file with nbt data (such as myentityinfo.txt).");
        writer.comment("In the text file you can use the same mob spawning parameters used with the /summon command to equip the");
        writer.comment("entity and give it custom attributes etc. You can copy the DATA part of a summon command including surrounding ");
        writer.comment("curly braces to a .txt file, for instance for: \"/summon Skeleton x y z {DATA}\"");

        for(BO4EntityFunction func : Arrays.asList(entityFunctions[0]))
        {
        	writer.function(func);
        }

        writer.bigTitle("Particles");
        writer.comment("Forge only (this may have changed, check for updates).");
        writer.comment("Creates an invisible particle spawner at the given location that spawns particles every x milliseconds.");
        writer.comment("Usage: Particle(x,y,z,particleName,interval,velocityX,velocityY,velocityZ)");
        writer.comment("velocityX, velocityY and velocityZ are optional.");
        writer.comment("Only vanilla particle names can be used, for 1.11.2 these are;");
        writer.comment("explode, largeexplode, hugeexplosion, fireworksSpark, bubble, splash, wake, suspended");
		writer.comment("depthsuspend, crit, magicCrit, smoke, largesmoke, spell, instantSpell, mobSpell");
		writer.comment("mobSpellAmbient, witchMagic, dripWater, dripLava, angryVillager, happyVillager");
		writer.comment("townaura, note, portal, enchantmenttable, flame, lava, footstep, cloud, reddust");
		writer.comment("snowballpoof,  snowshovel, slime, heart, barrier, iconcrack, blockcrack, blockdust");
		writer.comment("droplet, take, mobappearance, dragonbreath, endRod, damageIndicator, sweepAttack");
		writer.comment("fallingdust, totem, spit.");
		writer.comment("velocityX,velocityY,velocityZ - Spawn the enemy with the given velocity. If this is not filled in then a small random velocity is applied.");

        for(BO4ParticleFunction func : Arrays.asList(particleFunctions[0]))
        {
        	writer.function(func);
        }

        writer.bigTitle("Spawners");
        writer.comment("Forge only (this may have changed, check for updates).");
        writer.comment("Creates an invisible entity spawner at the given location that spawns entities every x seconds.");
        writer.comment("Entities can only spawn if their spawn requirements are met (zombies/skeletons only spawn in the dark etc). Max entity count for the server is ignored, each spawner has its own maxCount setting.");
        writer.comment("Usage: Spawner(x,y,z,entityName,nbtFileName,groupSize,interval,spawnChance,maxCount,despawnTime,velocityX,velocityY,velocityZ,yaw,pitch)");
        writer.comment("nbtFileName, despawnTime, velocityX, velocityY, velocityZ, yaw and pitch are optional");
        writer.comment("Example Spawner(0, 0, 0, Villager, 1, 5, 100, 5) or Spawner(0, 0, 0, Villager, villager1.txt, 1, 5, 100, 5) or Spawner(0, 0, 0, Villager, 1, 5, 100, 5, 30, 1, 1, 1, 0, 0)");
        writer.comment("entityName - Name of the entity to spawn, use /otg entities to get a list of entities that can be used as entityName, this includes entities added by other mods and non-living entities.");
        writer.comment("nbtFileName - A .txt file with nbt data (such as myentityinfo.txt).");
        writer.comment("In the text file you can use the same mob spawning parameters used with the /summon command to equip the");
        writer.comment("entity and give it custom attributes etc. You can copy the DATA part of a summon command including surrounding ");
        writer.comment("curly braces to a .txt file, for instance for: \"/summon Skeleton x y z {DATA}\"");
        writer.comment("groupSize - Number of entities that should spawn for each successful spawn attempt.");
        writer.comment("interval - Time in seconds between each spawn attempt.");
        writer.comment("spawnChance - For each spawn attempt, the chance between 0-100 that the spawn attempt will succeed.");
        writer.comment("maxCount - The maximum amount of this kind of entity that can exist within 32 blocks. If there are already maxCount or more entities of this type in a 32 radius this spawner will not spawn anything.");
        writer.comment("despawnTime - After despawnTime seconds, if there is no player within 32 blocks of the entity it will despawn..");
        writer.comment("velocityX,velocityY,velocityZ,yaw,pitch - Spawn the enemy with the given velocity and angle, handy for making traps and launchers (shooting arrows and fireballs etc).");

        for(BO4SpawnerFunction func : Arrays.asList(spawnerFunctions[0]))
        {
        	writer.function(func);
        }

        // ModData
        writer.bigTitle("ModData");
        writer.comment("Forge only.");
        writer.comment("Use the ModData() tag to include data that other mods can use");
        writer.comment("Mod makers can use ModData and the /otg GetModData command to test IMC communications between OTG");
        writer.comment("and their mod.");
        writer.comment("Normal users can use it to spawn some mobs and blocks on command.");
        writer.comment("ModData(x,y,z,\"ModName\", \"MyModDataAsText\"");
        writer.comment("Example: ModData(x,y,z,MyCystomNPCMod,SpawnBobHere/WithAPotato/And50Health)");
        writer.comment("Try not to use exotic/reserved characters, like brackets and comma's etc, this stuff isn't fool-proof.");
        writer.comment("Also, use this only to store IDs/object names etc for your mod, DO NOT include things like character dialogue,");
        writer.comment("messages on signs, loot lists etc in this file. As much as possible just store id's/names here and store all the data related to those id's/names in your own mod.");
        writer.comment("OTG has some built in ModData commands for basic mob and block spawning.");
        writer.comment("These are mostly just a demonstration for mod makers to show how ModData.");
        writer.comment("can be used by other mods.");
        writer.comment("For mob spawning in OTG use: ModData(x,y,z,OTG,mob/MobType/Count/Persistent/Name)");
        writer.comment("mob: Makes OTG recognise this as a mob spawning command.");
        writer.comment("MobType: Lower-case, no spaces. Any vanilla mob like dragon, skeleton, wither, villager etc");
        writer.comment("Count: The number of mobs to spawn");
        writer.comment("Persistent (true/false): Should the mobs never de-spawn? If set to true the mob will get a");
        writer.comment("name-tag ingame so you can recognise it.");
        writer.comment("Name: A name-tag for the monster/npc.");
        writer.comment("Example: ModData(0,0,0,OTG,villager/1/true/Bob)");
        writer.comment("To spawn blocks using ModData use: ModData(x,y,z,OTG,block/material)");
        writer.comment("block: Makes OTG recognise this as a block spawning command.");
        writer.comment("material: id or text, custom blocks can be added using ModName:MaterialName.");
        writer.comment("To send all ModData within a radius in chunks around the player to the specified mod");
        writer.comment("use this console command: /otg GetModData ModName Radius");
        writer.comment("ModName: name of the mod, for OTG commands use OTG ");
        writer.comment("Radius (optional): Radius in chunks around the player.");

        for(BO4ModDataFunction func : Arrays.asList(modDataFunctions[0]))
        {
        	writer.function(func);
        }
    }

    @Override
    protected void correctSettings()
    {

    }

    @Override
    protected void renameOldSettings()
    {
        // Stub method - there are no old setting to convert yet (:
    }
}
