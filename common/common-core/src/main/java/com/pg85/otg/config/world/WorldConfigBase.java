package com.pg85.otg.config.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.pg85.otg.config.ConfigFile;
import com.pg85.otg.constants.SettingsEnums.ConfigMode;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.biome.ReplaceBlocks;
import com.pg85.otg.util.biome.ReplacedBlocksMatrix;
import com.pg85.otg.util.interfaces.IMaterialReader;
import com.pg85.otg.util.interfaces.IWorldConfig;
import com.pg85.otg.util.materials.LocalMaterialData;

abstract class WorldConfigBase extends ConfigFile implements IWorldConfig
{
	protected int worldHeightCap;
	protected int worldHeightScale;
	protected double fractureHorizontal;
	protected double fractureVertical;
	protected int generationDepth;
	private int maxSmoothRadius = 2;
	
	protected ArrayList<String> worldBiomes = new ArrayList<String>();
	protected int biomeRarityScale;
	
	protected List<ReplaceBlocks> replaceBlocksList = null;
	private HashMap<LocalMaterialData,LocalMaterialData> replaceBlocksDict = null;
	
	protected boolean isOTGPlus;
	protected long resourcesSeed;
	protected boolean disableOreGen;
	protected boolean fullyFreezeLakes;
	protected boolean betterSnowFall;
	private boolean biomeConfigsHaveReplacement = false;
	protected boolean removeSurfaceStone;
	protected ConfigMode settingsMode;
    
	protected int waterLevelMin;
	protected int waterLevelMax;
	
	protected boolean populationBoundsCheck;
	protected int maximumCustomStructureRadius;
	protected boolean mineshaftsEnabled;
	protected boolean oceanMonumentsEnabled;
	protected boolean rareBuildingsEnabled;
	protected boolean strongholdsEnabled;
	protected boolean villagesEnabled;
	
	protected boolean ceilingBedrock;
	protected boolean disableBedrock;
	protected boolean flatBedrock;
	protected LocalMaterialData bedrockBlock;
	
	protected LocalMaterialData cooledLavaBlock;
	protected LocalMaterialData iceBlock;
	protected LocalMaterialData waterBlock;
	
    // Caves
	protected int individualCaveRarity;
	protected int caveRarity;
	protected int caveFrequency;
	protected int caveMaxAltitude;
	protected int caveMinAltitude;
	protected int caveSystemFrequency;
	protected int caveSystemPocketChance;
	protected int caveSystemPocketMinSize;
	protected int caveSystemPocketMaxSize;
	protected boolean evenCaveDistribution;

    // Ravines
	protected int ravineRarity;
	protected int ravineMinAltitude;
	protected int ravineMaxAltitude;
	protected int ravineMinLength;
	protected int ravineMaxLength;
	protected double ravineDepth;
	
	// TODO: Create getters/setters for these and make protected
	
	public String author;
	public String description;
	public String worldPackerModName;
	public String worldSeed;   
	
	public boolean canDoLightning;
	public boolean canDoRainSnowIce;
	public int worldBorderRadius;
	public int preGenerationRadius;
	public int cloudHeight;
	
    public String dimensionBelow;
    public String dimensionAbove;
    public int dimensionBelowHeight;
    public int dimensionAboveHeight;
    
    public ArrayList<LocalMaterialData> dimensionPortalMaterials;
    public String portalColor;
    public String portalParticleType; 
    public String portalMobType;
    public int portalMobSpawnChance;
    
    public List<String> dimensions = new ArrayList<String>();
	
	// Game rules for worlds used as dimensions with Forge // TODO: Apply to overworld too?
    
	public String commandBlockOutput; // Whether command blocks should notify admins when they perform commands
	public boolean canDropChunk; // Called to determine if the chunk at the given chunk coordinates within the provider's world can be dropped. Used in WorldProviderSurface to prevent spawn chunks from being unloaded.
	public boolean canRespawnHere; // True if the player can respawn in this dimension.
	public String disableElytraMovementCheck; // Whether the server should skip checking player speed when the player is wearing elytra. Often helps with jittering due to lag in multiplayer, but may also be used to travel unfairly long distances in survival mode (cheating).
	public String doDaylightCycle; // Whether the day-night cycle and moon phases progress
	public String doEntityDrops; // Whether entities that are not mobs should have drops
	public String doFireTick; // Whether fire should spread and naturally extinguish
	public String doLimitedCrafting; // Whether players should only be able to craft recipes that they've unlocked first
    public String doMobLoot; // Whether mobs should drop items
    public String doMobSpawning; // Whether mobs should naturally spawn. Does not affect monster spawners.
    public String doTileDrops; // Whether blocks should have drops
    public String doWeatherCycle; // Whether the weather will change
    public String gameLoopFunction = "true"; // The function to run every game tick
    public String keepInventory; // Whether the player should keep items in their inventory after death
    public String logAdminCommands; // Whether to log admin commands to server log
    public String maxCommandChainLength = "65536"; // Determines the number at which the chain command block acts as a "chain".
    public String maxEntityCramming; // The maximum number of other pushable entities a mob or player can push, before taking 3 doublehearts suffocation damage per half-second. Setting to 0 disables the rule. Damage affects survival-mode or adventure-mode players, and all mobs but bats. Pushable entities include non-spectator-mode players, any mob except bats, as well as boats and minecarts.
    public String mobGriefing; // Whether creepers, zombies, endermen, ghasts, withers, ender dragons, rabbits, sheep, and villagers should be able to change blocks and whether villagers, zombies, skeletons, and zombie pigmen can pick up items
    public String naturalRegeneration; // Whether the player can regenerate health naturally if their hunger is full enough (doesn't affect external healing, such as golden apples, the Regeneration effect, etc.)
    public String randomTickSpeed; // How often a random block tick occurs (such as plant growth, leaf decay, etc.) per chunk section per game tick. 0 will disable random ticks, higher numbers will increase random ticks
    public String reducedDebugInfo; // Whether the debug screen shows all or reduced information; and whether the effects of F3+B (entity hitboxes) and F3+G (chunk boundaries) are shown.
    public String sendCommandFeedback; // Whether the feedback from commands executed by a player should show up in chat. Also affects the default behavior of whether command blocks store their output text
    public String showDeathMessages; // Whether death messages are put into chat when a player dies. Also affects whether a message is sent to the pet's owner when the pet dies.
    public String spawnRadius; // The number of blocks outward from the world spawn coordinates that a player will spawn in when first joining a server or when dying without a spawnpoint.
    public String spectatorsGenerateChunks; // Whether players in spectator mode can generate chunks

    public String welcomeMessage; // A message to display to the user when they transfer to this dimension.
    public String departMessage; // A Message to display to the user when they transfer out of this dimension.
    public boolean hasSkyLight; // A boolean that tells if a world does not have a sky. Used in calculating weather and skylight. Also affects GetActualHeight(), hasNoSky = true worlds are seen as 128 height worlds, which affects nether portal placement/detection.
    public boolean isSurfaceWorld; // Returns 'true' if in the "main surface world", but 'false' if in the Nether or End dimensions. Affects: Clock, Compass, sky/cloud rendering, allowed to sleep here, zombie pigmen spawning in portal frames.

    public boolean doesWaterVaporize; // True for nether, any water that is placed vaporises.

    public boolean doesXZShowFog; // Returns true if the given X,Z coordinate should show environmental fog. True for Nether.

    public boolean useCustomFogColor = false;
    public double fogColorRed;
    public double fogColorGreen;
	public double fogColorBlue;

	public boolean isSkyColored; // Is set to false for End (black sky?)
   
    public boolean isNightWorld; // Sky is always moon and stars but light levels are same as day
    public double voidFogYFactor; // A double value representing the Y value relative to the top of the map at which void fog is at its maximum. The default factor of 0.03125 relative to 256, for example, means the void fog will be at its maximum at (256*0.03125), or 8.
    public double gravityFactor; // 0.08D; Affects entities jumping and falling
    public boolean shouldMapSpin; // Determine if the cursor on the map should 'spin' when rendered, like it does for the player in the nether.
    public int respawnDimension; // Dimension that players respawn in when dying in this dimension, defaults to 0, only applies when canRespawnHere = false.
    public int movementFactor; // The dimension's movement factor. Whenever a player or entity changes dimension from world A to world B, their coordinates are multiplied by worldA.provider.getMovementFactor() / worldB.provider.getMovementFactor(). Example: Overworld factor is 1, nether factor is 8. Traveling from overworld to nether multiplies coordinates by 1/8.

    public String itemsToAddOnJoinDimension; // Similar to the /give command, gives players items when they enter a dimension/world.
    public String itemsToRemoveOnJoinDimension; // The opposite of the /give command, removes items from players inventories when they enter a dimension/world.
    public String itemsToAddOnLeaveDimension; // Similar to the /give command, gives players items when they leave a dimension/world.
    public String itemsToRemoveOnLeaveDimension; // The opposite of the /give command, removes items from players inventories when they leave a dimension/world.
    public String itemsToAddOnRespawn; // Similar to the /give command, gives players items when they respawn in a dimension/world.

    public boolean teleportToSpawnOnly; // If this is set to true then portals to this dimension will always teleport players to the world's spawn point.
    
    public boolean spawnPointSet;
    public int spawnPointX;
    public int spawnPointY;
    public int spawnPointZ;

    public boolean playersCanBreakBlocks;
    public boolean explosionsCanBreakBlocks;
    public boolean playersCanPlaceBlocks;
    	
	//
	
	protected WorldConfigBase(String configName)
	{
		super(configName);
	}
	
	public LocalMaterialData getDefaultBedrockBlock()
	{
		return this.bedrockBlock;
	}

	public LocalMaterialData getBedrockBlockReplaced(ReplacedBlocksMatrix replaceBlocks, int y)
	{		
		if(replaceBlocks.replacesBedrock)
		{
			return this.bedrockBlock.parseWithBiomeAndHeight(this.biomeConfigsHaveReplacement, replaceBlocks, y);
		}
		return this.bedrockBlock;
	}
	
    public double getFractureHorizontal()
    {
        return this.fractureHorizontal < 0.0D ? 1.0D / (Math.abs(this.fractureHorizontal) + 1.0D) : this.fractureHorizontal + 1.0D;
    }

    public double getFractureVertical()
    {
        return this.fractureVertical < 0.0D ? 1.0D / (Math.abs(this.fractureVertical) + 1.0D) : this.fractureVertical + 1.0D;
    }
    
    public HashMap<LocalMaterialData,LocalMaterialData> getReplaceBlocksDict(IMaterialReader materialReader)
    {
    	if(this.replaceBlocksDict != null)
    	{
    		return this.replaceBlocksDict;
    	}
    	if(this.replaceBlocksDict == null && this.replaceBlocksList != null)
    	{
    		replaceBlocksDict = new HashMap<LocalMaterialData,LocalMaterialData>();
    		for(ReplaceBlocks blockNames : this.replaceBlocksList)
    		{
    			try {
    				// TODO: If the block is unknown it will return the ReplaceUnknownBlockWithMaterial instead.
    				// This can cause unexpected results like wrong blocks being replaced when ReplaceUnknownBlockWithMaterial is used as sourceBlock or targetBlock.
    				this.replaceBlocksDict.put(materialReader.readMaterial(blockNames.getSourceBlock()), materialReader.readMaterial(blockNames.getTargetBlock()));
				} catch (InvalidConfigException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    	}
    	return this.replaceBlocksDict;
    }

	public boolean doPopulationBoundsCheck()
	{
		return this.populationBoundsCheck;
	}
	
	public boolean getBedrockDisabled()
	{
		return this.disableBedrock;
	}	
	
	public int getWorldHeightCap()
	{
		return this.worldHeightCap;
	}
	
	public int getRavineRarity()
	{
		return this.ravineRarity;
	}
	
	public int getRavineMinLength()
	{
		return this.ravineMinLength;
	}
	
	public int getRavineMaxLength()
	{
		return this.ravineMaxLength;
	}
	
	public double getRavineDepth()
	{
		return this.ravineDepth;
	}
	
	public int getRavineMinAltitude()
	{
		return this.ravineMinAltitude;
	}
	
	public int getRavineMaxAltitude()
	{
		return this.ravineMaxAltitude;
	}
	
	public int getCaveFrequency()
	{
		return this.caveFrequency;
	}
	
	public int getCaveRarity()
	{
		return this.caveRarity;
	}
	
	public boolean isEvenCaveDistribution()
	{
		return this.evenCaveDistribution;
	}
	
	public int getCaveMinAltitude()
	{
		return this.caveMinAltitude;
	}
	
	public int getCaveMaxAltitude()
	{
		return this.caveMaxAltitude;
	}
	
	public int getCaveSystemFrequency()
	{
		return this.caveSystemFrequency;
	}
	
	public int getIndividualCaveRarity()
	{
		return this.individualCaveRarity;
	}
	
	public int getCaveSystemPocketMinSize()
	{
		return this.caveSystemPocketMinSize;
	}
	
	public int getCaveSystemPocketChance()
	{
		return this.caveSystemPocketChance;
	}
	
	public int getCaveSystemPocketMaxSize()
	{
		return this.caveSystemPocketMaxSize;
	}
		
	public boolean isBetterSnowFall()
	{
		return this.betterSnowFall;
	}
	
	public boolean isFullyFreezeLakes()
	{
		return this.fullyFreezeLakes;
	}
	
	public boolean isOTGPlus()
	{
		return this.isOTGPlus;
	}
	
	public long getResourcesSeed()
	{
		return this.resourcesSeed;
	}
	
	public boolean isDisableOreGen()
	{
		return this.disableOreGen;
	}
	
	public int getMaximumCustomStructureRadius()
	{
		return this.maximumCustomStructureRadius;
	}
	
	public boolean setBiomeConfigsHaveReplacement(boolean biomeConfigsHaveReplacement)
	{
		return this.biomeConfigsHaveReplacement = biomeConfigsHaveReplacement;
	}
	
	public boolean getBiomeConfigsHaveReplacement()
	{
		return this.biomeConfigsHaveReplacement;
	}

	public int getGenerationDepth()
	{
		return this.generationDepth;
	}
	
	public int getBiomeRarityScale()
	{
		return this.biomeRarityScale;
	}
	
	public LocalMaterialData getCooledLavaBlock()
	{
		return this.cooledLavaBlock;
	}
		
	public LocalMaterialData getIceBlock()
	{
		return this.iceBlock;
	}
	
	public boolean getIsCeilingBedrock()
	{
		return this.ceilingBedrock;
	}
	
	public boolean getIsFlatBedrock()
	{
		return this.flatBedrock;
	}
	
	public int getMaxSmoothRadius()
	{
		return this.maxSmoothRadius;
	}
	
	public boolean getMineshaftsEnabled()
	{
		return this.mineshaftsEnabled;
	}
	
	public boolean getOceanMonumentsEnabled()
	{
		return this.oceanMonumentsEnabled;
	}
	
	public boolean getRareBuildingsEnabled()
	{
		return this.rareBuildingsEnabled;
	}
	
	public boolean getRemoveSurfaceStone()
	{
		return this.removeSurfaceStone;
	}
	
	public ConfigMode getSettingsMode()
	{
		return this.settingsMode;
	}
	
	public boolean getStrongholdsEnabled()
	{
		return this.strongholdsEnabled;
	}
	
	public boolean getVillagesEnabled()
	{
		return this.villagesEnabled;
	}
	
	public LocalMaterialData getWaterBlock()
	{
		return this.waterBlock;
	}
	
	public int getWaterLevelMax()
	{
		return this.waterLevelMax;
	}
	
	public int getWaterLevelMin()
	{
		return this.waterLevelMin;
	}
	
	public ArrayList<String> getWorldBiomes()
	{
		return this.worldBiomes;
	}
	
	public int getWorldHeightScale()
	{
		return this.worldHeightScale;
	}
	
	public void setMaxSmoothRadius(int smoothRadius)
	{
		this.maxSmoothRadius = smoothRadius;
	}
}
