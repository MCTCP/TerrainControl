package com.pg85.otg.config.dimensions;

import com.pg85.otg.config.standard.WorldStandardValues;

public class GameRulesEntry
{
	// Capital letters since we'll be serialising to yaml (and we want to make it look nice) 
	public boolean CommandBlockOutput = WorldStandardValues.CommandBlockOutput.getDefaultValue();
	public boolean DisableElytraMovementCheck = WorldStandardValues.DisableElytraMovementCheck.getDefaultValue();
	public boolean DoDaylightCycle = WorldStandardValues.DoDaylightCycle.getDefaultValue();
	public boolean DoEntityDrops = WorldStandardValues.DoEntityDrops.getDefaultValue();
	public boolean DoFireTick = WorldStandardValues.DoFireTick.getDefaultValue();
	public boolean DoLimitedCrafting = WorldStandardValues.DoLimitedCrafting.getDefaultValue();
	public boolean DoMobLoot = WorldStandardValues.DoMobLoot.getDefaultValue();
	public boolean DoMobSpawning = WorldStandardValues.DoMobSpawning.getDefaultValue();
	public boolean DoTileDrops = WorldStandardValues.DoTileDrops.getDefaultValue();
	public boolean DoWeatherCycle = WorldStandardValues.DoWeatherCycle.getDefaultValue();
	public boolean GameLoopFunction = WorldStandardValues.GameLoopFunction.getDefaultValue();
	public boolean KeepInventory = WorldStandardValues.KeepInventory.getDefaultValue();
	public boolean LogAdminCommands = WorldStandardValues.LogAdminCommands.getDefaultValue();
	public int MaxCommandChainLength = WorldStandardValues.MaxCommandChainLength.getDefaultValue();
	public int MaxEntityCramming = WorldStandardValues.MaxEntityCramming.getDefaultValue();
	public boolean MobGriefing = WorldStandardValues.MobGriefing.getDefaultValue();
	public boolean NaturalRegeneration = WorldStandardValues.NaturalRegeneration.getDefaultValue();
	public int RandomTickSpeed = WorldStandardValues.RandomTickSpeed.getDefaultValue();
	public boolean ReducedDebugInfo = WorldStandardValues.ReducedDebugInfo.getDefaultValue();
	public boolean SendCommandFeedback = WorldStandardValues.SendCommandFeedback.getDefaultValue();
	public boolean ShowDeathMessages = WorldStandardValues.ShowDeathMessages.getDefaultValue();
	public int SpawnRadius = WorldStandardValues.SpawnRadius.getDefaultValue();
	public boolean SpectatorsGenerateChunks = WorldStandardValues.SpectatorsGenerateChunks.getDefaultValue();
	
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