package com.pg85.otg.config.dimensions;

import com.pg85.otg.config.standard.WorldStandardValues;

public class GameRulesEntry
{
	// Capital letters since we'll be serialising to yaml (and we want to make it look nice) 
	public boolean CommandBlockOutput = WorldStandardValues.CommandBlockOutput.getDefaultValue(null);
	public boolean DisableElytraMovementCheck = WorldStandardValues.DisableElytraMovementCheck.getDefaultValue(null);
	public boolean DoDaylightCycle = WorldStandardValues.DoDaylightCycle.getDefaultValue(null);
	public boolean DoEntityDrops = WorldStandardValues.DoEntityDrops.getDefaultValue(null);
	public boolean DoFireTick = WorldStandardValues.DoFireTick.getDefaultValue(null);
	public boolean DoLimitedCrafting = WorldStandardValues.DoLimitedCrafting.getDefaultValue(null);
	public boolean DoMobLoot = WorldStandardValues.DoMobLoot.getDefaultValue(null);
	public boolean DoMobSpawning = WorldStandardValues.DoMobSpawning.getDefaultValue(null);
	public boolean DoTileDrops = WorldStandardValues.DoTileDrops.getDefaultValue(null);
	public boolean DoWeatherCycle = WorldStandardValues.DoWeatherCycle.getDefaultValue(null);
	public boolean GameLoopFunction = WorldStandardValues.GameLoopFunction.getDefaultValue(null);
	public boolean KeepInventory = WorldStandardValues.KeepInventory.getDefaultValue(null);
	public boolean LogAdminCommands = WorldStandardValues.LogAdminCommands.getDefaultValue(null);
	public int MaxCommandChainLength = WorldStandardValues.MaxCommandChainLength.getDefaultValue(null);
	public int MaxEntityCramming = WorldStandardValues.MaxEntityCramming.getDefaultValue(null);
	public boolean MobGriefing = WorldStandardValues.MobGriefing.getDefaultValue(null);
	public boolean NaturalRegeneration = WorldStandardValues.NaturalRegeneration.getDefaultValue(null);
	public int RandomTickSpeed = WorldStandardValues.RandomTickSpeed.getDefaultValue(null);
	public boolean ReducedDebugInfo = WorldStandardValues.ReducedDebugInfo.getDefaultValue(null);
	public boolean SendCommandFeedback = WorldStandardValues.SendCommandFeedback.getDefaultValue(null);
	public boolean ShowDeathMessages = WorldStandardValues.ShowDeathMessages.getDefaultValue(null);
	public int SpawnRadius = WorldStandardValues.SpawnRadius.getDefaultValue(null);
	public boolean SpectatorsGenerateChunks = WorldStandardValues.SpectatorsGenerateChunks.getDefaultValue(null);
	
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