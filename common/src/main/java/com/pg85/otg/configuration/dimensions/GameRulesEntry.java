package com.pg85.otg.configuration.dimensions;

import com.pg85.otg.configuration.standard.WorldStandardValues;

public class GameRulesEntry
{
	public boolean CommandBlockOutput = WorldStandardValues.commandBlockOutput.getDefaultValue();
	public boolean DisableElytraMovementCheck = WorldStandardValues.disableElytraMovementCheck.getDefaultValue();
	public boolean DoDaylightCycle = WorldStandardValues.doDaylightCycle.getDefaultValue();
	public boolean DoEntityDrops = WorldStandardValues.doEntityDrops.getDefaultValue();
	public boolean DoFireTick = WorldStandardValues.doFireTick.getDefaultValue();
	public boolean DoLimitedCrafting = WorldStandardValues.doLimitedCrafting.getDefaultValue();
	public boolean DoMobLoot = WorldStandardValues.doMobLoot.getDefaultValue();
	public boolean DoMobSpawning = WorldStandardValues.doMobSpawning.getDefaultValue();
	public boolean DoTileDrops = WorldStandardValues.doTileDrops.getDefaultValue();
	public boolean DoWeatherCycle = WorldStandardValues.doWeatherCycle.getDefaultValue();
	public boolean GameLoopFunction = WorldStandardValues.gameLoopFunction.getDefaultValue();
	public boolean KeepInventory = WorldStandardValues.keepInventory.getDefaultValue();
	public boolean LogAdminCommands = WorldStandardValues.logAdminCommands.getDefaultValue();
	public int MaxCommandChainLength = WorldStandardValues.maxCommandChainLength.getDefaultValue();
	public int MaxEntityCramming = WorldStandardValues.maxEntityCramming.getDefaultValue();
	public boolean MobGriefing = WorldStandardValues.mobGriefing.getDefaultValue();
	public boolean NaturalRegeneration = WorldStandardValues.naturalRegeneration.getDefaultValue();
	public int RandomTickSpeed = WorldStandardValues.randomTickSpeed.getDefaultValue();
	public boolean ReducedDebugInfo = WorldStandardValues.reducedDebugInfo.getDefaultValue();
	public boolean SendCommandFeedback = WorldStandardValues.sendCommandFeedback.getDefaultValue();
	public boolean ShowDeathMessages = WorldStandardValues.showDeathMessages.getDefaultValue();
	public int SpawnRadius = WorldStandardValues.spawnRadius.getDefaultValue();
	public boolean SpectatorsGenerateChunks = WorldStandardValues.spectatorsGenerateChunks.getDefaultValue();
	
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