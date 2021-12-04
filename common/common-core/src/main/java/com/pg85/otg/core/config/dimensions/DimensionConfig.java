package com.pg85.otg.core.config.dimensions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.core.OTG;

/**
 * Used for Forge MP at world creation, defines the overworld/nether/end and any custom dimensions for a world.
 * May also be used for ModPack Configs in the future, hence the yaml code.
 */
public class DimensionConfig
{
	private boolean isModpackConfig = false;
	// Use capitals since we're serialising to yaml and want to make it look nice.
	public int Version;
	public String ModpackName;
	public OTGOverWorld Overworld;
	public OTGDimension Nether;
	public OTGDimension End;
	public List<OTGDimension> Dimensions = new ArrayList<>();
	public Settings Settings;
	public GameRules GameRules;
	
	// Parameterless constructor for deserialisation
	public DimensionConfig() { }
	
	public boolean isModpackConfig()
	{
		return this.isModpackConfig;
	}
	
	public static DimensionConfig fromDisk(String fileName)
	{		
		File dimensionConfig = new File(OTG.getEngine().getOTGRootFolder().toString(), Constants.DIMENSION_CONFIGS_FOLDER + File.separator + fileName + ".yaml");
		if(dimensionConfig.exists())
		{
			DimensionConfig dimConfig = new DimensionConfig();
			String content = "";
			try
			{
				content = new String(Files.readAllBytes(dimensionConfig.toPath()));
			}
			catch (IOException e) 
			{
				e.printStackTrace();
			}
			DimensionConfig loadedConfig = fromYamlString(content);
			if(loadedConfig != null)
			{
				dimConfig.isModpackConfig = true;
				dimConfig.Version = loadedConfig.Version;
				dimConfig.ModpackName = loadedConfig.ModpackName;
				dimConfig.Overworld = loadedConfig.Overworld;
				dimConfig.Nether = loadedConfig.Nether;
				dimConfig.End = loadedConfig.End;
				dimConfig.Dimensions = loadedConfig.Dimensions;
				dimConfig.GameRules = loadedConfig.GameRules;
				dimConfig.Settings = loadedConfig.Settings;
				return dimConfig;
			}
		}
		return null;
	}

	public static DimensionConfig fromYamlString(String input)
	{
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		DimensionConfig dimConfig = null;

		try {
			dimConfig = mapper.readValue(input, DimensionConfig.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return dimConfig;
	}

	public String toYamlString()
	{
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		try {
			return mapper.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static class OTGOverWorld extends OTGDimension
	{
		public String NonOTGWorldType; // Only used for MP atm to create non-otg overworlds.
		public String NonOTGGeneratorSettings; // Only used for MP atm to create non-otg overworlds.
		
		public OTGOverWorld()
		{
			super();
		}

		public OTGOverWorld(String presetFolderName, long seed, String nonOTGWorldType, String nonOTGGeneratorSettings)
		{
			super(presetFolderName, seed);
			this.NonOTGWorldType = nonOTGWorldType;
			this.NonOTGGeneratorSettings = nonOTGGeneratorSettings;
		}
	}

	public static class OTGDimension
	{
		public String PresetFolderName;
		public String PortalBlocks;
		public String PortalColor;
		public String PortalMob;
		public String PortalIgnitionSource;
		public long Seed;

		public OTGDimension() {}
		
		public OTGDimension(String presetFolderName, long seed)
		{
			this.PresetFolderName = presetFolderName;
			this.Seed = seed;
		}
	}

	public class Settings
	{
		public boolean GenerateStructures;
		public boolean BonusChest;
		
		public Settings() {}
	}
	
	public class GameRules
	{
		public boolean DoFireTick;
		public boolean MobGriefing;
		public boolean KeepInventory;
		public boolean DoMobSpawning;
		public boolean DoMobLoot;
		public boolean DoTileDrops;
		public boolean DoEntityDrops;
		public boolean CommandBlockOutput;
		public boolean NaturalRegeneration;
		public boolean DoDaylightCycle;
		public boolean LogAdminCommands;
		public boolean ShowDeathMessages;
		public int RandomTickSpeed;
		public boolean SendCommandFeedback;
		public boolean SpectatorsGenerateChunks;
		public int SpawnRadius;
		public boolean DisableElytraMovementCheck;
		public int MaxEntityCramming;
		public boolean DoWeatherCycle;
		public boolean DoLimitedCrafting;
		public int MaxCommandChainLength;
		public boolean AnnounceAdvancements;
		public boolean DisableRaids;
		public boolean DoInsomnia;
		public boolean DrowningDamage;
		public boolean FallDamage;
		public boolean FireDamage;
		public boolean DoPatrolSpawning;
		public boolean DoTraderSpawning;
		public boolean ForgiveDeadPlayers;
		public boolean UniversalAnger;
		
		public GameRules() {}
	}
}
