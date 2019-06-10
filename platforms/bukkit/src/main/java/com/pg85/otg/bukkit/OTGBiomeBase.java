package com.pg85.otg.bukkit;

import com.pg85.otg.OTG;
import com.pg85.otg.bukkit.util.EnumHelper;
import com.pg85.otg.bukkit.util.MobSpawnGroupHelper;
import com.pg85.otg.bukkit.util.WorldHelper;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.configuration.biome.WeightedMobSpawnGroup;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.configuration.standard.WorldStandardValues;
import com.pg85.otg.logging.LogMarker;
import com.pg85.otg.util.BiomeIds;
import com.pg85.otg.util.helpers.StringHelper;
import net.minecraft.server.v1_12_R1.BiomeBase;
import net.minecraft.server.v1_12_R1.MinecraftKey;
import org.bukkit.block.Biome;

import java.util.List;

public class OTGBiomeBase extends BiomeBase
{
    private static final int MAX_OTG_BIOME_ID = 4095;
    public int otgBiomeId;
    public int savedId;

    /**
     * Mojang made the methods on BiomeBase.a protected (so only accessable for
     * classes in the package net.minecraft.world.biome package and for
     * subclasses of BiomeBase.a). To get around this, we have to subclass
     * BiomeBase.a.
     */
    private static class BiomeBase_a extends BiomeBase.a
    {

        public BiomeBase_a(String name, BiomeConfig biomeConfig)
        {
            super(name);

            // Minecraft doesn't like temperatures between 0.1 and 0.2, so avoid
            // them: round them to either 0.1 or 0.2
            float adjustedTemperature = biomeConfig.biomeTemperature;
            if (adjustedTemperature >= 0.1 && adjustedTemperature <= 0.2)
            {
                if (adjustedTemperature >= 1.5)
                    adjustedTemperature = 0.2f;
                else
                    adjustedTemperature = 0.1f;
            }

            c(biomeConfig.biomeHeight);
            d(biomeConfig.biomeVolatility);
            a(adjustedTemperature);
            b(biomeConfig.biomeWetness);
            if (biomeConfig.biomeWetness <= 0.0001)
            {
                a(); // disableRain()
            }
            if (biomeConfig.biomeTemperature <= WorldStandardValues.SNOW_AND_ICE_MAX_TEMP)
            {
                b(); // enableSnowfall()
            }
        }
    }

    /**
     * Creates a CustomBiome instance. Minecraft automatically registers those
     * instances in the BiomeBase constructor. We don't want this for virtual
     * biomes (the shouldn't overwrite real biomes), so we restore the old
     * biome, unregistering the virtual biome.
     *
     * @param biomeConfig Settings of the biome
     * @param biomeIds    Ids of the biome.
     * @return The CustomBiome instance.
     */
    /*
    public static OTGBiomeBase createInstance(BiomeConfig biomeConfig, BiomeIds biomeIds)
    {

[02:55:02 INFO]: [OTG] CreateAndRegisterBiome1: ForestSakura 0 -1
[02:55:03 INFO]: [OTG] CreateAndRegisterBiome2: ForestSakura 0 -1
[02:55:03 INFO]: [OTG] createBiomeFor: ForestSakura 0 -1
[02:55:03 ERROR]: [OpenTerrainGenerator] Could not set generator for default world 'Biome Bundle': Plugin 'OpenTerrainGenerator v1.12.2 v7
java.lang.ArrayIndexOutOfBoundsException: -1
        at net.minecraft.server.v1_12_R1.RegistryID.a(SourceFile:107) ~[spigot-1.12.2.jar:git-Spigot-e8ded36-acbc348]
        at net.minecraft.server.v1_12_R1.RegistryMaterials.a(SourceFile:21) ~[spigot-1.12.2.jar:git-Spigot-e8ded36-acbc348]
        at com.pg85.otg.bukkit.OTGBiomeBase.createInstance(OTGBiomeBase.java:120) ~[?:?]
        at com.pg85.otg.bukkit.BukkitBiome.forCustomBiome(BukkitBiome.java:32) ~[?:?]
        at com.pg85.otg.bukkit.BukkitWorld.createBiomeFor(BukkitWorld.java:176) ~[?:?]
        at com.pg85.otg.bukkit.BukkitWorld.createBiomeFor(BukkitWorld.java:1639) ~[?:?]
        at com.pg85.otg.network.ServerConfigProvider.CreateAndRegisterBiome(ServerConfigProvider.java:677) ~[?:?]
        at com.pg85.otg.network.ServerConfigProvider.indexSettings(ServerConfigProvider.java:431) ~[?:?]
        at com.pg85.otg.network.ServerConfigProvider.loadBiomes(ServerConfigProvider.java:164) ~[?:?]
        at com.pg85.otg.network.ServerConfigProvider.loadSettings(ServerConfigProvider.java:87) ~[?:?]
        at com.pg85.otg.network.ServerConfigProvider.<init>(ServerConfigProvider.java:76) ~[?:?]
        at com.pg85.otg.bukkit.OTGPlugin.getDefaultWorldGenerator(OTGPlugin.java:149) ~[?:?]
        at org.bukkit.craftbukkit.v1_12_R1.CraftServer.getGenerator(CraftServer.java:1224) [spigot-1.12.2.jar:git-Spigot-e8ded36-acbc348]
        at net.minecraft.server.v1_12_R1.MinecraftServer.a(MinecraftServer.java:260) [spigot-1.12.2.jar:git-Spigot-e8ded36-acbc348]
        at net.minecraft.server.v1_12_R1.DedicatedServer.init(DedicatedServer.java:272) [spigot-1.12.2.jar:git-Spigot-e8ded36-acbc348]
        at net.minecraft.server.v1_12_R1.MinecraftServer.run(MinecraftServer.java:545) [spigot-1.12.2.jar:git-Spigot-e8ded36-acbc348]
        at java.lang.Thread.run(Unknown Source) [?:1.8.0_171]
[02:55:03 INFO]: -------- World Settings For [Biome Bundle] --------

vs Forge

[03:03:06] [Server thread/INFO] [OTG]: CreateAndRegisterBiome1: ForestSakura 0 -1
[03:03:06] [Server thread/INFO] [OTG]: CreateAndRegisterBiome2: ForestSakura 0 -1
[03:03:06] [Server thread/INFO] [OTG]: createBiomeFor: ForestSakura 0 -1
[03:03:06] [Server thread/INFO] [OTG]: ForestSakura, 40, 0, openterraingenerator:overworld_forestsakura


    	  	
        OTGBiomeBase customBiome = new OTGBiomeBase(biomeConfig, biomeIds.getOTGBiomeId());

        // Insert the biome in Minecraft's biome mapping
        String biomeNameWithoutSpaces = StringHelper.toComputerFriendlyName(biomeConfig.getName());
        MinecraftKey biomeKey = new MinecraftKey(PluginStandardValues.PLUGIN_NAME, biomeNameWithoutSpaces);
        int savedBiomeId = biomeIds.getSavedId();

        // We need to init array size because Mojang uses a strange custom
        // ArrayList. RegistryID arrays are not correctly (but randomly!) copied
        // when resized.
        if(BiomeBase.getBiome(MAX_OTG_BIOME_ID) == null) {
            BiomeBase.REGISTRY_ID.a(MAX_OTG_BIOME_ID,
                    new MinecraftKey(PluginStandardValues.PLUGIN_NAME, "null"),
                    new OTGBiomeBase(biomeConfig));
        }

        //if (biomeIds.isVirtual())
        if(biomeConfig.replaceToBiomeName != null && biomeConfig.replaceToBiomeName.length() > 0) // This biome uses ReplaceToBiomeName and should use the ReplaceToBiomeName biome's id.
        {
            // Virtual biomes hack: register, then let original biome overwrite
            // In this way, the id --> biome mapping returns the original biome,
            // and the biome --> id mapping returns savedBiomeId for both the
            // original and custom biome
            BiomeBase existingBiome = BiomeBase.getBiome(savedBiomeId);

            if (existingBiome == null)
            {
                // Original biome not yet registered. This is because it's a
                // custom biome that is loaded after this virtual biome, so it
                // will soon be registered
                BiomeBase.REGISTRY_ID.a(savedBiomeId, biomeKey, customBiome);
                OTG.log(LogMarker.INFO, ",{},{},{}", biomeConfig.getName(), savedBiomeId, biomeIds.getOTGBiomeId());
            } else {
                MinecraftKey existingBiomeKey = BiomeBase.REGISTRY_ID.b(existingBiome);
                BiomeBase.REGISTRY_ID.a(savedBiomeId, biomeKey, customBiome);
                BiomeBase.REGISTRY_ID.a(savedBiomeId, existingBiomeKey, existingBiome);

                // String existingBiomeName = existingBiome.getClass().getSimpleName();
                // if(existingBiome instanceof CustomBiome) {
                //     existingBiomeName = String.valueOf(((CustomBiome) existingBiome).generationId);
                // }
                OTG.log(LogMarker.INFO, ",{},{},{}", biomeConfig.getName(), savedBiomeId, biomeIds.getOTGBiomeId()); // existingBiomeName
            }
        } else {
            // Normal insertion       	
            BiomeBase.REGISTRY_ID.a(savedBiomeId, biomeKey, customBiome);

            OTG.log(LogMarker.INFO, ",{},{},{}", biomeConfig.getName(), savedBiomeId, biomeIds.getOTGBiomeId());
        }

        // Add biome to Bukkit enum if it's not there yet
        try {
            Biome.valueOf(biomeNameWithoutSpaces.toUpperCase());
        } catch (IllegalArgumentException e) {
            EnumHelper.addEnum(Biome.class, biomeNameWithoutSpaces.toUpperCase(), new Class[0], new Object[0]);
        }

        // Sanity check: check if biome was actually registered
        int registeredSavedId = WorldHelper.getSavedId(customBiome);
        if (registeredSavedId != savedBiomeId)
        {
            throw new AssertionError("Biome " + biomeConfig.getName() + " is not properly registered: got id " + registeredSavedId + ", should be " + savedBiomeId);
        }

        checkRegistry();

        return customBiome;
    }    
    */
    
    public static OTGBiomeBase createInstance(BiomeConfig biomeConfig, BiomeIds biomeIds, String worldName)
    {
        // We need to init array size because Mojang uses a strange custom
        // ArrayList. RegistryID arrays are not correctly (but randomly!) copied
        // when resized.
        if(BiomeBase.getBiome(MAX_OTG_BIOME_ID) == null) {
            BiomeBase.REGISTRY_ID.a(MAX_OTG_BIOME_ID,
                    new MinecraftKey(PluginStandardValues.PLUGIN_NAME, "null"),
                    new OTGBiomeBase(biomeConfig));
        }
    	
        String biomeNameWithoutSpaces = worldName.toLowerCase() + "_" + StringHelper.toComputerFriendlyName(biomeConfig.getName());
        MinecraftKey biomeKey = new MinecraftKey(PluginStandardValues.MOD_ID, biomeNameWithoutSpaces);
          
        OTGBiomeBase customBiome = new OTGBiomeBase(biomeConfig);

        if(biomeConfig.replaceToBiomeName != null && biomeConfig.replaceToBiomeName.length() > 0) // This biome uses ReplaceToBiomeName and should use the ReplaceToBiomeName biome's id.
        {
        	customBiome.otgBiomeId = biomeIds.getOTGBiomeId();
        	customBiome.savedId = biomeIds.getSavedId();
        	
            // Virtual biomes hack: register, then let original biome overwrite
            // In this way, the id --> biome mapping returns the original biome,
            // and the biome --> id mapping returns savedBiomeId for both the
            // original and custom biome
            BiomeBase existingBiome = BiomeBase.getBiome(customBiome.savedId);

            BiomeBase.REGISTRY_ID.a(customBiome.savedId, biomeKey, customBiome);
            
            if (existingBiome != null)
            {
                MinecraftKey existingBiomeKey = BiomeBase.REGISTRY_ID.b(existingBiome);
                BiomeBase.REGISTRY_ID.a(customBiome.savedId, existingBiomeKey, existingBiome);

                // String existingBiomeName = existingBiome.getClass().getSimpleName();
                // if(existingBiome instanceof CustomBiome) {
                //     existingBiomeName = String.valueOf(((CustomBiome) existingBiome).generationId);
                // }
            } else {
            	throw new RuntimeException("ARSCHLOCH");
            }
        }
        else if(biomeIds.getSavedId() > -1) 
        {
        	// This is a biome for an existing world, make sure it uses the same biome id as before.         
        	BiomeBase biomeAtId = BiomeBase.REGISTRY_ID.getId(biomeIds.getSavedId());
            if(biomeAtId != null)
            {
            	throw new RuntimeException("Tried to register biome " + biomeKey.toString() + " to a id " + biomeIds.getSavedId() + " but it is occupied by biome: " + biomeAtId.toString() + ". This can happen when using the CustomBiomes setting in the world config or when changing mod/biome configurations for previously created worlds. OTG 1.12.2 v7 and above use dynamic biome id's for new worlds, this avoids the problem completely.");
            }
            
        	customBiome.otgBiomeId = biomeIds.getOTGBiomeId();
        	customBiome.savedId = biomeIds.getSavedId(); 
        	
            // Normal insertion       	
            BiomeBase.REGISTRY_ID.a(customBiome.savedId, biomeKey, customBiome);
        	
        } else {

            // Normal insertion, get next free id and register id+resourcelocation

        	int newId = 0;
        	while(BiomeBase.REGISTRY_ID.getId(newId) != null)
        	{
        		if(newId == MAX_OTG_BIOME_ID)
        		{
        			throw new RuntimeException("Biome could not be registered, no free biome id's!");
        		}
        		newId++;
        	}
        	biomeIds.setSavedId(newId);
        	customBiome.otgBiomeId = biomeIds.getOTGBiomeId();
        	customBiome.savedId = newId; 
        	
            // Normal insertion       	
            BiomeBase.REGISTRY_ID.a(customBiome.savedId, biomeKey, customBiome);
        }
        
    	OTG.log(LogMarker.INFO, "{}, {}, {}, {}", biomeConfig.getName(), biomeIds.getSavedId(), biomeIds.getOTGBiomeId(), biomeKey.toString());

        // Add biome to Bukkit enum if it's not there yet
        try {
            Biome.valueOf(biomeNameWithoutSpaces.toUpperCase());
        } catch (IllegalArgumentException e) {
            EnumHelper.addEnum(Biome.class, biomeNameWithoutSpaces.toUpperCase(), new Class[0], new Object[0]);
        }

        // Sanity check: check if biome was actually registered
        int registeredSavedId = WorldHelper.getSavedId(customBiome);
        if (registeredSavedId != customBiome.savedId)
        {
            throw new AssertionError("Biome " + biomeConfig.getName() + " is not properly registered: got id " + registeredSavedId + ", should be " + customBiome.savedId);
        }

        checkRegistry();

        return customBiome;    
    }
    
    /*
	public static ForgeBiome getOrCreateBiome(BiomeConfig biomeConfig, BiomeIds biomeIds, String worldName, ConfigProvider configProvider)
	{
		Biome biome = null;
		
	    String biomeNameForRegistry = worldName.toLowerCase() + "_" + StringHelper.toComputerFriendlyName(biomeConfig.getName());
	    String resourceDomain = OTG.MOD_ID;
	    ResourceLocation registryKey = new ResourceLocation(resourceDomain, biomeNameForRegistry);
	
	    // Check if registered earlier
		Biome alreadyRegisteredBiome = ForgeRegistries.BIOMES.getValue(registryKey);
	    if (alreadyRegisteredBiome != null)
	    {        	
	    	// This can happen when an unloaded world is loaded, its biomes have already been registered
	    	biome = alreadyRegisteredBiome;
	    } else {
	
	        // No existing biome, create new one
	        OTGBiome customBiome = new OTGBiome(biomeConfig, registryKey);
	
	        ForgeEngine forgeEngine = ((ForgeEngine) OTG.getEngine());
	
	        //if (biomeIds.isVirtual())
	        if(biomeConfig.replaceToBiomeName != null && biomeConfig.replaceToBiomeName.length() > 0) // This biome uses ReplaceToBiomeName and should use the ReplaceToBiomeName biome's id.
	        {
	        	// Only register by resourcelocation.
	        	// TODO: Make sure this is enough for Forge 1.12+ <- It looks like the server may not send the biomes to the client if they are not added to the registry. TODO: Check if only virtual biomes have this problem.
	        	forgeEngine.registerForgeBiome(registryKey, customBiome);
	        	
	        	customBiome.otgBiomeId = biomeIds.getOTGBiomeId();
	        	customBiome.savedId = biomeIds.getSavedId();        	
	        }
	        else if(biomeIds.getSavedId() > -1) 
	        {
	        	// This is a biome for an existing world, make sure it uses the same biome id as before.         
	        	int newId = forgeEngine.registerForgeBiomeWithId(biomeIds.getSavedId(), registryKey, customBiome);       	
	        	customBiome.otgBiomeId = biomeIds.getOTGBiomeId();
	        	customBiome.savedId = newId;
	        	
	        } else {
	
	            // Normal insertion, get next free id and register id+resourcelocation
	
	        	// TODO: Make this prettier?
	        	int newId = forgeEngine.registerForgeBiomeWithId(registryKey, customBiome);
	        	biomeIds.setSavedId(newId);
	        	customBiome.otgBiomeId = biomeIds.getOTGBiomeId();
	        	customBiome.savedId = newId;       	        
	        }
	        
	    	OTG.log(LogMarker.INFO, "{}, {}, {}, {}", biomeConfig.getName(), biomeIds.getSavedId(), biomeIds.getOTGBiomeId(), registryKey.toString());
	
	        biome = customBiome;
	    }
	    
		// Always try to register biomes and create Biome Configs. Biomes with id's > 255 are registered
		// only for biome -> id queries, any (saved)id -> biome query will return the ReplaceToBiomeName biome.
	
	    Biome existingBiome = Biome.getBiome(biomeIds.getSavedId());
	
	    if (biomeIds.getSavedId() >= 256 || biomeIds.getSavedId() < 0)
	    {
	        throw new RuntimeException("Could not allocate the requested id " + biomeIds.getSavedId() + " for biome " + biomeConfig.getName() + ". All available id's under 256 have been allocated\n" + ". To proceed, adjust your WorldConfig or use the ReplaceToBiomeName feature to make the biome virtual.");
	    }
	
	    ForgeBiome forgeBiome = new ForgeBiome(biome, biomeConfig, biomeIds);
	
	    BiomeRegistryManager.registerBiomeInBiomeDictionary(biome, existingBiome, biomeConfig, configProvider);
	    
	    return forgeBiome;
	}
*/
    /**
     * Check if biome ID registry is well filled.
     */
    private static void checkRegistry() {
        for(int i = 0; i < 256; i++) {
            BiomeBase biome = getBiome(i);
            if(biome != null && biome instanceof OTGBiomeBase && ((OTGBiomeBase) biome).savedId != i) {
                throw new AssertionError("Biome ID #" + i + " returns custom biome #" + ((OTGBiomeBase) biome).savedId + " instead of its own.");
            }
        }
    }

    private OTGBiomeBase(BiomeConfig biomeConfig)
    {
        super(new BiomeBase_a(biomeConfig.getName(), biomeConfig));

        // Sanity check
        if (this.getHumidity() != biomeConfig.biomeWetness)
        {
            throw new AssertionError("Biome temperature mismatch");
        }

        this.q = ((BukkitMaterialData) biomeConfig.surfaceBlock).internalBlock();
        this.r = ((BukkitMaterialData) biomeConfig.groundBlock).internalBlock();

        // Mob spawning
        addMobs(this.t, biomeConfig.spawnMonsters);
        addMobs(this.u, biomeConfig.spawnCreatures);
        addMobs(this.v, biomeConfig.spawnWaterCreatures);
        addMobs(this.w, biomeConfig.spawnAmbientCreatures);
    }

    // Adds the mobs to the internal list.
    protected void addMobs(List<BiomeMeta> internalList, List<WeightedMobSpawnGroup> configList)
    {
        internalList.clear();
        internalList.addAll(MobSpawnGroupHelper.toMinecraftlist(configList));
    }
}
