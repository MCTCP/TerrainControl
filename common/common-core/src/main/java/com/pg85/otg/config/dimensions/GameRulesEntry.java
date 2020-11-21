package com.pg85.otg.config.dimensions;

import com.pg85.otg.config.standard.WorldStandardValues;

class GameRulesEntry
{
	// Capital letters since we'll be serialising to yaml (and we want to make it look nice) 
	boolean CommandBlockOutput = WorldStandardValues.CommandBlockOutput.getDefaultValue(null);
	boolean DisableElytraMovementCheck = WorldStandardValues.DisableElytraMovementCheck.getDefaultValue(null);
	boolean DoDaylightCycle = WorldStandardValues.DoDaylightCycle.getDefaultValue(null);
	boolean DoEntityDrops = WorldStandardValues.DoEntityDrops.getDefaultValue(null);
	boolean DoFireTick = WorldStandardValues.DoFireTick.getDefaultValue(null);
	boolean DoLimitedCrafting = WorldStandardValues.DoLimitedCrafting.getDefaultValue(null);
	boolean DoMobLoot = WorldStandardValues.DoMobLoot.getDefaultValue(null);
	boolean DoMobSpawning = WorldStandardValues.DoMobSpawning.getDefaultValue(null);
	boolean DoTileDrops = WorldStandardValues.DoTileDrops.getDefaultValue(null);
	boolean DoWeatherCycle = WorldStandardValues.DoWeatherCycle.getDefaultValue(null);
	boolean GameLoopFunction = WorldStandardValues.GameLoopFunction.getDefaultValue(null);
	boolean KeepInventory = WorldStandardValues.KeepInventory.getDefaultValue(null);
	boolean LogAdminCommands = WorldStandardValues.LogAdminCommands.getDefaultValue(null);
	int MaxCommandChainLength = WorldStandardValues.MaxCommandChainLength.getDefaultValue(null);
	int MaxEntityCramming = WorldStandardValues.MaxEntityCramming.getDefaultValue(null);
	boolean MobGriefing = WorldStandardValues.MobGriefing.getDefaultValue(null);
	boolean NaturalRegeneration = WorldStandardValues.NaturalRegeneration.getDefaultValue(null);
	int RandomTickSpeed = WorldStandardValues.RandomTickSpeed.getDefaultValue(null);
	boolean ReducedDebugInfo = WorldStandardValues.ReducedDebugInfo.getDefaultValue(null);
	boolean SendCommandFeedback = WorldStandardValues.SendCommandFeedback.getDefaultValue(null);
	boolean ShowDeathMessages = WorldStandardValues.ShowDeathMessages.getDefaultValue(null);
	int SpawnRadius = WorldStandardValues.SpawnRadius.getDefaultValue(null);
	boolean SpectatorsGenerateChunks = WorldStandardValues.SpectatorsGenerateChunks.getDefaultValue(null);
	
	public GameRulesEntry clone()
	{
		GameRulesEntry clone = new GameRulesEntry();
		
		clone.CommandBlockOutput = this.CommandBlockOutput;
		clone.DisableElytraMovementCheck = this.DisableElytraMovementCheck;
		clone.DoDaylightCycle = this.DoDaylightCycle;
		clone.DoEntityDrops = this.DoEntityDrops;
		clone.DoFireTick = this.DoFireTick;
		clone.DoLimitedCrafting = this.DoLimitedCrafting;
		clone.DoMobLoot = this.DoMobLoot;
		clone.DoMobSpawning = this.DoMobSpawning;
		clone.DoTileDrops = this.DoTileDrops;
		clone.DoWeatherCycle = this.DoWeatherCycle;
		clone.GameLoopFunction = this.GameLoopFunction;
		clone.KeepInventory = this.KeepInventory;
		clone.LogAdminCommands = this.LogAdminCommands;
		clone.MaxCommandChainLength = this.MaxCommandChainLength;
		clone.MaxEntityCramming = this.MaxEntityCramming;
		clone.MobGriefing = this.MobGriefing;
		clone.NaturalRegeneration = this.NaturalRegeneration;
		clone.RandomTickSpeed = this.RandomTickSpeed;
		clone.ReducedDebugInfo = this.ReducedDebugInfo;
		clone.SendCommandFeedback = this.SendCommandFeedback;
		clone.ShowDeathMessages = this.ShowDeathMessages;
		clone.SpawnRadius = this.SpawnRadius;
		clone.SpectatorsGenerateChunks = this.SpectatorsGenerateChunks;	
		
		return clone;
	}
}