package com.pg85.otg.forge.event;

import com.pg85.otg.config.dimensions.DimensionConfig;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.forge.gen.OTGNoiseChunkGenerator;
import com.pg85.otg.interfaces.IWorldConfig;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Dimension;
import net.minecraft.world.GameRules;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.world.SleepFinishedTimeEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

// Used to allow sleeping in OTG dimensions, apply GameRules for MP and set spawn point from WorldConfig.
@EventBusSubscriber(modid = Constants.MOD_ID_SHORT)
public class WorldHandler
{
	@SubscribeEvent
	public void onSetSpawn(WorldEvent.CreateSpawnPosition event)
	{		
		if(event.getWorld() instanceof ServerWorld)
		{
			// If a fixed spawn point is configured in the WorldConfig, apply it.
			if(((ServerWorld)event.getWorld()).getWorldServer().getChunkSource().generator instanceof OTGNoiseChunkGenerator)
			{
				IWorldConfig worldConfig = ((OTGNoiseChunkGenerator)((ServerWorld)event.getWorld()).getWorldServer().getChunkSource().generator).getPreset().getWorldConfig(); 
				if(worldConfig.getSpawnPointSet())
				{
					event.setCanceled(true);
					((ServerWorld)event.getWorld()).getWorldServer().setDefaultSpawnPos(new BlockPos(worldConfig.getSpawnPointX(), worldConfig.getSpawnPointY(), worldConfig.getSpawnPointZ()), worldConfig.getSpawnPointAngle());
				}
			}

			// If a modpack config is being used, apply the configured gamerules (if any).
			// TODO: Only needed for MP?
			DimensionConfig modpackConfig = DimensionConfig.fromDisk(Constants.MODPACK_CONFIG_NAME);
			if(modpackConfig != null && modpackConfig.GameRules != null)
			{
				GameRules gameRules = ((ServerWorld)event.getWorld()).getGameRules();
				// TODO: doImmediateRespawn
				gameRules.getRule(GameRules.RULE_DOFIRETICK).set(modpackConfig.GameRules.DoFireTick, (MinecraftServer)null);
				gameRules.getRule(GameRules.RULE_MOBGRIEFING).set(modpackConfig.GameRules.MobGriefing, (MinecraftServer)null);
				gameRules.getRule(GameRules.RULE_KEEPINVENTORY).set(modpackConfig.GameRules.KeepInventory, (MinecraftServer)null);
				gameRules.getRule(GameRules.RULE_DOMOBSPAWNING).set(modpackConfig.GameRules.DoMobSpawning, (MinecraftServer)null);
				gameRules.getRule(GameRules.RULE_DOMOBLOOT).set(modpackConfig.GameRules.DoMobLoot, (MinecraftServer)null);
				gameRules.getRule(GameRules.RULE_DOBLOCKDROPS).set(modpackConfig.GameRules.DoTileDrops, (MinecraftServer)null);
				gameRules.getRule(GameRules.RULE_DOENTITYDROPS).set(modpackConfig.GameRules.DoEntityDrops, (MinecraftServer)null);
				gameRules.getRule(GameRules.RULE_COMMANDBLOCKOUTPUT).set(modpackConfig.GameRules.CommandBlockOutput, (MinecraftServer)null);
				gameRules.getRule(GameRules.RULE_NATURAL_REGENERATION).set(modpackConfig.GameRules.NaturalRegeneration, (MinecraftServer)null);
				gameRules.getRule(GameRules.RULE_DAYLIGHT).set(modpackConfig.GameRules.DoDaylightCycle, (MinecraftServer)null);
				gameRules.getRule(GameRules.RULE_LOGADMINCOMMANDS).set(modpackConfig.GameRules.LogAdminCommands, (MinecraftServer)null);
				gameRules.getRule(GameRules.RULE_SHOWDEATHMESSAGES).set(modpackConfig.GameRules.ShowDeathMessages, (MinecraftServer)null);
				gameRules.getRule(GameRules.RULE_RANDOMTICKING).value = modpackConfig.GameRules.RandomTickSpeed;
				gameRules.getRule(GameRules.RULE_SENDCOMMANDFEEDBACK).set(modpackConfig.GameRules.SendCommandFeedback, (MinecraftServer)null);
				gameRules.getRule(GameRules.RULE_SPECTATORSGENERATECHUNKS).set(modpackConfig.GameRules.SpectatorsGenerateChunks, (MinecraftServer)null);
				gameRules.getRule(GameRules.RULE_SPAWN_RADIUS).value = modpackConfig.GameRules.SpawnRadius;
				gameRules.getRule(GameRules.RULE_DISABLE_ELYTRA_MOVEMENT_CHECK).set(modpackConfig.GameRules.DisableElytraMovementCheck, (MinecraftServer)null);
				gameRules.getRule(GameRules.RULE_MAX_ENTITY_CRAMMING).value = modpackConfig.GameRules.MaxEntityCramming;
				gameRules.getRule(GameRules.RULE_WEATHER_CYCLE).set(modpackConfig.GameRules.DoWeatherCycle, (MinecraftServer)null);
				gameRules.getRule(GameRules.RULE_LIMITED_CRAFTING).set(modpackConfig.GameRules.DoLimitedCrafting, (MinecraftServer)null);
				gameRules.getRule(GameRules.RULE_MAX_COMMAND_CHAIN_LENGTH).value = modpackConfig.GameRules.MaxCommandChainLength;
				gameRules.getRule(GameRules.RULE_ANNOUNCE_ADVANCEMENTS).set(modpackConfig.GameRules.AnnounceAdvancements, (MinecraftServer)null);
				gameRules.getRule(GameRules.RULE_DISABLE_RAIDS).set(modpackConfig.GameRules.DisableRaids, (MinecraftServer)null);
				gameRules.getRule(GameRules.RULE_DOINSOMNIA).set(modpackConfig.GameRules.DoInsomnia, (MinecraftServer)null);
				gameRules.getRule(GameRules.RULE_DROWNING_DAMAGE).set(modpackConfig.GameRules.DrowningDamage, (MinecraftServer)null);
				gameRules.getRule(GameRules.RULE_FALL_DAMAGE).set(modpackConfig.GameRules.FallDamage, (MinecraftServer)null);
				gameRules.getRule(GameRules.RULE_FIRE_DAMAGE).set(modpackConfig.GameRules.FireDamage, (MinecraftServer)null);
				gameRules.getRule(GameRules.RULE_DO_PATROL_SPAWNING).set(modpackConfig.GameRules.DoPatrolSpawning, (MinecraftServer)null);
				gameRules.getRule(GameRules.RULE_DO_TRADER_SPAWNING).set(modpackConfig.GameRules.DoTraderSpawning, (MinecraftServer)null);
				gameRules.getRule(GameRules.RULE_FORGIVE_DEAD_PLAYERS).set(modpackConfig.GameRules.ForgiveDeadPlayers, (MinecraftServer)null);
				gameRules.getRule(GameRules.RULE_UNIVERSAL_ANGER).set(modpackConfig.GameRules.UniversalAnger, (MinecraftServer)null);
			}
		}
	}	
	
	// Beds don't work in non-overworld dimensions since DerivedWorldInfo doesn't implement 
	// setDayTime (time is shared with the overworld, so can't tick more than one dim).
	// For non-overworld OTG dims, when players finish sleeping apply the new time to the 
	// overworld. 
	// TODO: Improve dimensions implementation, allow separate time/weather/gamerules per dim.
	@SubscribeEvent
    public static void onSleepFinished(SleepFinishedTimeEvent event)
    {
		if(event.getWorld() instanceof ServerWorld)
		{
			if(!((ServerWorld)event.getWorld()).dimension().location().equals(Dimension.OVERWORLD.location()))
			{
				ChunkGenerator chunkGenerator = ((ServerWorld)event.getWorld()).getChunkSource().generator;
				if(chunkGenerator instanceof OTGNoiseChunkGenerator)
				{
					((ServerWorld)event.getWorld()).getServer().overworld().setDayTime(event.getNewTime());
				}
			}
		}
    }
}
