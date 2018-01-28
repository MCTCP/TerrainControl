package com.pg85.otg.forge;

import com.pg85.otg.OTG;
import com.pg85.otg.configuration.ServerConfigProvider;
import com.pg85.otg.configuration.WorldConfig;

import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.village.VillageCollection;
import net.minecraft.world.GameType;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.border.IBorderListener;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.storage.DerivedWorldInfo;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;

public class OTGWorldServerMulti extends WorldServer
{
    private final WorldServer delegate;
    private IBorderListener borderListener;

    // TODO: This is not used atm, can be used together with asm for replacing the Worldserver for the overworld, nether and end.
    public OTGWorldServerMulti(MinecraftServer server, ISaveHandler saveHandlerIn, WorldInfo info, int dimensionId, Profiler profilerIn)
    {
    	super(server, saveHandlerIn, info, dimensionId, profilerIn);
    	this.delegate = null;
    }

	// TODO: This is not used atm, can be used together with asm for replacing the Worldserver for the overworld, nether and end.
    public OTGWorldServerMulti(MinecraftServer server, ISaveHandler saveHandlerIn, int dimensionId, WorldServer delegate, Profiler profilerIn)
	{
    	super(server, saveHandlerIn, delegate.getWorldInfo(), dimensionId, profilerIn);
    	this.delegate = delegate;
        this.borderListener = new IBorderListener()
        {
            public void onSizeChanged(WorldBorder border, double newSize)
            {
            	getWorldBorder().setTransition(newSize);
            }
            public void onTransitionStarted(WorldBorder border, double oldSize, double newSize, long time)
            {
            	getWorldBorder().setTransition(oldSize, newSize, time);
            }
            public void onCenterChanged(WorldBorder border, double x, double z)
            {
            	getWorldBorder().setCenter(x, z);
            }
            public void onWarningTimeChanged(WorldBorder border, int newTime)
            {
            	getWorldBorder().setWarningTime(newTime);
            }
            public void onWarningDistanceChanged(WorldBorder border, int newDistance)
            {
            	getWorldBorder().setWarningDistance(newDistance);
            }
            public void onDamageAmountChanged(WorldBorder border, double newAmount)
            {
            	getWorldBorder().setDamageAmount(newAmount);
            }
            public void onDamageBufferChanged(WorldBorder border, double newSize)
            {
            	getWorldBorder().setDamageBuffer(newSize);
            }
        };
        this.delegate.getWorldBorder().addListener(this.borderListener);

        // This constructor is only used by MC when initialising the default dimensions
        // The overworld should be the only default dimension affected by OTG.
        if(dimensionId == 0)
        {
	        // TODO: Is this really needed for clients?
	        // TODO: Changing seed here does work, but seed is forgotten after restart and overworld seed is used, fix this! <-- TODO: Is this still true?

			long seedIn = (long) Math.floor((Math.random() * Long.MAX_VALUE));
			GameType gameType = server.getGameType();
			boolean enableMapFeatures = getWorldInfo().isMapFeaturesEnabled(); // Whether the map features (e.g. strongholds) generation is enabled or disabled.
			boolean hardcoreMode = getWorldInfo().isHardcoreModeEnabled();
			WorldType worldTypeIn = getWorldType();

			WorldSettings settings = new WorldSettings(seedIn, gameType, enableMapFeatures, hardcoreMode, worldTypeIn);
			settings.setGeneratorOptions("OpenTerrainGenerator");
			WorldInfo worldInfo = new WorldInfo(settings, getWorldInfo().getWorldName());

			this.worldInfo = worldInfo;

	        ForgeWorld forgeWorld = ((ForgeEngine)OTG.getEngine()).getOverWorld();
	        if(forgeWorld != null) // forgeWorld can be null for a dimension with a vanilla world
	        {
		        forgeWorld.getConfigs().getWorldConfig().worldSeed = "" + seedIn;
		        if(forgeWorld.getConfigs() instanceof ServerConfigProvider)
		        {
		        	((ServerConfigProvider)forgeWorld.getConfigs()).saveWorldConfig();
		        }

	        	WorldConfig worldConfig = forgeWorld.getConfigs().getWorldConfig();

		        getGameRules().setOrCreateGameRule("commandBlockOutput", worldConfig.commandBlockOutput); // Whether command blocks should notify admins when they perform commands
	    		getGameRules().setOrCreateGameRule("disableElytraMovementCheck", worldConfig.disableElytraMovementCheck); // Whether the server should skip checking player speed when the player is wearing elytra. Often helps with jittering due to lag in multiplayer, but may also be used to travel unfairly long distances in survival mode (cheating).
	    		getGameRules().setOrCreateGameRule("doDaylightCycle", worldConfig.doDaylightCycle); // Whether the day-night cycle and moon phases progress
				getGameRules().setOrCreateGameRule("doEntityDrops", worldConfig.doEntityDrops); // Whether entities that are not mobs should have drops
				getGameRules().setOrCreateGameRule("doFireTick", worldConfig.doFireTick); // Whether fire should spread and naturally extinguish
				//getGameRules().setOrCreateGameRule("doLimitedCrafting", worldConfig.doLimitedCrafting); // Whether players should only be able to craft recipes that they've unlocked first // TODO: Implement for 1.12
				getGameRules().setOrCreateGameRule("doMobLoot", worldConfig.doMobLoot); // Whether mobs should drop items
				getGameRules().setOrCreateGameRule("doMobSpawning", worldConfig.doMobSpawning); // Whether mobs should naturally spawn. Does not affect monster spawners.
				getGameRules().setOrCreateGameRule("doTileDrops", worldConfig.doTileDrops); // Whether blocks should have drops
				getGameRules().setOrCreateGameRule("doWeatherCycle", worldConfig.doWeatherCycle); // Whether the weather will change
		        //boolean gameLoopFunction = true; // The function to run every game tick // TODO: Implement for 1.12
				getGameRules().setOrCreateGameRule("keepInventory", worldConfig.keepInventory); // Whether the player should keep items in their inventory after death
				getGameRules().setOrCreateGameRule("logAdminCommands", worldConfig.logAdminCommands); // Whether to log admin commands to server log
		        //int maxCommandChainLength = 65536; // Determines the number at which the chain command block acts as a "chain". // TODO: Implement for 1.12
				getGameRules().setOrCreateGameRule("maxEntityCramming", worldConfig.maxEntityCramming); // The maximum number of other pushable entities a mob or player can push, before taking 3 doublehearts suffocation damage per half-second. Setting to 0 disables the rule. Damage affects survival-mode or adventure-mode players, and all mobs but bats. Pushable entities include non-spectator-mode players, any mob except bats, as well as boats and minecarts.
		        getGameRules().setOrCreateGameRule("mobGriefing", worldConfig.mobGriefing); // Whether creepers, zombies, endermen, ghasts, withers, ender dragons, rabbits, sheep, and villagers should be able to change blocks and whether villagers, zombies, skeletons, and zombie pigmen can pick up items
	    		getGameRules().setOrCreateGameRule("naturalRegeneration", worldConfig.naturalRegeneration); // Whether the player can regenerate health naturally if their hunger is full enough (doesn't affect external healing, such as golden apples, the Regeneration effect, etc.)
	    		getGameRules().setOrCreateGameRule("randomTickSpeed", worldConfig.randomTickSpeed); // How often a random block tick occurs (such as plant growth, leaf decay, etc.) per chunk section per game tick. 0 will disable random ticks, higher numbers will increase random ticks
		        getGameRules().setOrCreateGameRule("reducedDebugInfo", worldConfig.reducedDebugInfo); // Whether the debug screen shows all or reduced information; and whether the effects of F3+B (entity hitboxes) and F3+G (chunk boundaries) are shown.
	    		getGameRules().setOrCreateGameRule("sendCommandFeedback", worldConfig.sendCommandFeedback); // Whether the feedback from commands executed by a player should show up in chat. Also affects the default behavior of whether command blocks store their output text
				getGameRules().setOrCreateGameRule("showDeathMessages", worldConfig.showDeathMessages); // Whether death messages are put into chat when a player dies. Also affects whether a message is sent to the pet's owner when the pet dies.
				getGameRules().setOrCreateGameRule("spawnRadius", worldConfig.spawnRadius); // The number of blocks outward from the world spawn coordinates that a player will spawn in when first joining a server or when dying without a spawnpoint.
		        getGameRules().setOrCreateGameRule("spectatorsGenerateChunks", worldConfig.spectatorsGenerateChunks); // Whether players in spectator mode can generate chunks

		        // Set difficulty, creative/survival/hardcore
	            //if (worldserver.getWorldInfo().isHardcoreModeEnabled())
	            //{
	                //worldserver.getWorldInfo().setDifficulty(EnumDifficulty.HARD);
	                //worldserver.setAllowedSpawnTypes(true, true);
	            //}
	            //else if (this.isSinglePlayer())
	            //{
	                //worldserver.getWorldInfo().setDifficulty(difficulty);
	                //worldserver.setAllowedSpawnTypes(worldserver.getDifficulty() != EnumDifficulty.PEACEFUL, true);
	            //} else {
	                //worldserver.getWorldInfo().setDifficulty(difficulty);
	                //worldserver.setAllowedSpawnTypes(this.allowSpawnMonsters(), this.canSpawnAnimals);
	            //}
	        }
        }
	}

    public OTGWorldServerMulti(MinecraftServer server, ISaveHandler saveHandlerIn, int dimensionId, WorldServer delegate, Profiler profilerIn, WorldInfo worldInfo)
    {
        super(server, saveHandlerIn, new DerivedWorldInfo(worldInfo), dimensionId, profilerIn);
        this.delegate = delegate;
        this.borderListener = new IBorderListener()
        {
            public void onSizeChanged(WorldBorder border, double newSize)
            {
                getWorldBorder().setTransition(newSize);
            }
            public void onTransitionStarted(WorldBorder border, double oldSize, double newSize, long time)
            {
            	getWorldBorder().setTransition(oldSize, newSize, time);
            }
            public void onCenterChanged(WorldBorder border, double x, double z)
            {
            	getWorldBorder().setCenter(x, z);
            }
            public void onWarningTimeChanged(WorldBorder border, int newTime)
            {
            	getWorldBorder().setWarningTime(newTime);
            }
            public void onWarningDistanceChanged(WorldBorder border, int newDistance)
            {
            	getWorldBorder().setWarningDistance(newDistance);
            }
            public void onDamageAmountChanged(WorldBorder border, double newAmount)
            {
            	getWorldBorder().setDamageAmount(newAmount);
            }
            public void onDamageBufferChanged(WorldBorder border, double newSize)
            {
            	getWorldBorder().setDamageBuffer(newSize);
            }
        };
        this.delegate.getWorldBorder().addListener(this.borderListener);
    }

    /**
     * Saves the chunks to disk.
     */
    protected void saveLevel() throws MinecraftException
    {
        this.perWorldStorage.saveAllData();
    }

    public World init()
    {
    	if(this.delegate != null)
    	{
	        this.mapStorage = this.delegate.getMapStorage();
	        this.worldScoreboard = this.delegate.getScoreboard();
	        this.lootTable = this.delegate.getLootTableManager();
	        this.advancementManager = this.delegate.getAdvancementManager();
	        String s = VillageCollection.fileNameForProvider(this.provider);
	        VillageCollection villagecollection = (VillageCollection)this.perWorldStorage.getOrLoadData(VillageCollection.class, s);

	        if (villagecollection == null)
	        {
	            this.villageCollection = new VillageCollection(this);
	            this.perWorldStorage.setData(s, this.villageCollection);
	        }
	        else
	        {
	            this.villageCollection = villagecollection;
	            this.villageCollection.setWorldsForAll(this);
	        }

	        this.initCapabilities();
    	} else {
    		super.init();
    	}
        return this;
    }

    /**
     * Syncs all changes to disk and wait for completion.
     */
    @Override
    public void flush()
    {
        super.flush();
        if(this.delegate != null)
        {
        	this.delegate.getWorldBorder().removeListener(this.borderListener); // Unlink ourselves, to prevent world leak.
        }
    }

    /**
     * Called during saving of a world to give children worlds a chance to save additional data. Only used to save
     * WorldProviderEnd's data in Vanilla.
     */
    public void saveAdditionalData()
    {
        this.provider.onWorldSave();
    }
}
