package com.khorn.terraincontrol.configuration;

import com.khorn.terraincontrol.generator.resourcegens.IceSpikeGen.SpikeType;

import com.khorn.terraincontrol.DefaultBiome;
import com.khorn.terraincontrol.DefaultMaterial;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.configuration.BiomeConfig.RareBuildingType;
import com.khorn.terraincontrol.configuration.BiomeConfig.VillageType;
import com.khorn.terraincontrol.generator.resourcegens.*;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * A biome generator holds all <i>default</i> settings of a biome.
 * 
 */
public class DefaultBiomeSettings
{
    // Simple registry for the default settings
    @SuppressWarnings("unchecked")
    private static final Class<? extends DefaultBiomeSettings>[] defaultSettings = (Class<? extends DefaultBiomeSettings>[]) new Class<?>[256];

    /**
     * Gets the default settings for a biome. If no default settings are find
     * for this biome, some boring default settings will be returned.
     * 
     * @param biome The biome to get the defaults for.
     * @param worldHeight The height of the world (many default resources have
     *            values relative to this).
     * @return The default settings.
     */
    public static DefaultBiomeSettings getDefaultSettings(LocalBiome biome, int worldHeight)
    {
        DefaultBiomeSettings biomeDefaultSettings = null;
        try
        {
            // Try to get the default settings for a biome
            Class<? extends DefaultBiomeSettings> settingsClass = defaultSettings[biome.getId()];
            if (settingsClass != null)
            {
                biomeDefaultSettings = (DefaultBiomeSettings) settingsClass.getConstructors()[0].newInstance(biome, worldHeight);
            }
        } catch (Exception e)
        {
            TerrainControl.log(Level.SEVERE, "Cannot read default settings for biome {0}", new Object[] {biome.getName()});
            TerrainControl.printStackTrace(Level.SEVERE, e);
        }

        // Make sure that we always return something (for custom biomes)
        if (biomeDefaultSettings == null)
        {
            biomeDefaultSettings = new DefaultBiomeSettings(biome, worldHeight);
        }
        return biomeDefaultSettings;
    }

    /**
     * Registers the default settings for the biome with the given id.
     * 
     * @param biomeId The biome id.
     * @param clazz The class with the default settings.
     */
    public static void registerDefaultSettings(int biomeId, Class<? extends DefaultBiomeSettings> clazz)
    {
        defaultSettings[biomeId] = clazz;
    }

    // End of registry

    protected final LocalBiome minecraftBiome;
    protected final int worldHeight;

    public boolean defaultWaterLakes = true;
    public Object[] defaultTree; // Parameters for tree resource
    public int defaultDandelions = 2;
    public int defaultPoppies = 0;
    public int defaultBlueOrchids = 0;
    public int defaultTallFlowers = 0;
    public int defaultSunflowers = 0;
    public int defaultGrass = 10;
    public int defaultDoubleGrass = 0;
    public int defaultFerns = 0;
    public int defaultLargeFerns = 0;
    public int defaultDeadBush = 0;
    public int defaultMushroom = 0;
    public int defaultReed = 0;
    public int defaultCactus = 0;
    public int defaultClay = 1;
    public Object[] defaultWell; // Parameters for well resource
    public float defaultBiomeSurface = 0.1F;
    public float defaultBiomeVolatility = 0.3F;
    public int defaultSurfaceBlock = DefaultMaterial.GRASS.id;
    public int defaultGroundBlock = DefaultMaterial.DIRT.id;
    public float defaultBiomeTemperature = 0.5F;
    public float defaultBiomeWetness = 0.5F;
    public ArrayList<String> defaultIsle = new ArrayList<String>();
    public ArrayList<String> defaultBorder = new ArrayList<String>();
    public ArrayList<String> defaultNotBorderNear = new ArrayList<String>();
    public String defaultRiverBiome = DefaultBiome.RIVER.Name;
    public int defaultSize = 4;
    public int defaultRarity = 100;
    public String defaultColor = "0x000000";
    public int defaultWaterLily = 0;
    public String defaultWaterColorMultiplier = "0xFFFFFF";
    public String defaultGrassColor = "0xFFFFFF";
    public String defaultFoliageColor = "0xFFFFFF";
    public boolean defaultStrongholds = true;
    public VillageType defaultVillageType = VillageType.disabled;
    public RareBuildingType defaultRareBuildingType = RareBuildingType.disabled;
    public int defaultEmeraldOre = 0;
    public boolean defaultHasVines;
    public int defaultBoulder = 0;
    public Object[] defaultSurfaceSurfaceAndGroundControl = new Object[0];
    public boolean defaultIceSpikes;

    public DefaultBiomeSettings(LocalBiome minecraftBiome, int worldHeight)
    {
        this.minecraftBiome = minecraftBiome;
        this.worldHeight = worldHeight;

        // Some settings are provided by LocalBiome, which gets them from
        // Minecraft
        this.defaultBiomeSurface = this.minecraftBiome.getSurfaceHeight();
        this.defaultBiomeVolatility = this.minecraftBiome.getSurfaceVolatility();
        this.defaultSurfaceBlock = this.minecraftBiome.getSurfaceBlock();
        this.defaultGroundBlock = this.minecraftBiome.getGroundBlock();
        this.defaultBiomeTemperature = this.minecraftBiome.getTemperature();
        this.defaultBiomeWetness = this.minecraftBiome.getWetness();
    }

    /**
     * Creates the default resources.
     * 
     * @param config The biome config. Custom objects must already be loaded.
     * @return The default resources for this biome.
     */
    public List<Resource> createDefaultResources(BiomeConfig config)
    {
        List<Resource> resources = new ArrayList<Resource>(32);

        // Small water lakes
        if (this.defaultWaterLakes)
        {
            resources.add(Resource.createResource(config, SmallLakeGen.class, DefaultMaterial.WATER.id, TCDefaultValues.SmallLakeWaterFrequency.intValue(), TCDefaultValues.SmallLakeWaterRarity.intValue(), TCDefaultValues.SmallLakeMinAltitude.intValue(), TCDefaultValues.SmallLakeMaxAltitude.intValue()));
        }

        // Small lava lakes
        resources.add(Resource.createResource(config, SmallLakeGen.class, DefaultMaterial.LAVA.id, TCDefaultValues.SmallLakeLavaFrequency.intValue(), TCDefaultValues.SmallLakeLavaRarity.intValue(), TCDefaultValues.SmallLakeMinAltitude.intValue(), TCDefaultValues.SmallLakeMaxAltitude.intValue()));

        // Underground lakes
        resources.add(Resource.createResource(config, UndergroundLakeGen.class, TCDefaultValues.undergroundLakeMinSize.intValue(), TCDefaultValues.undergroundLakeMaxSize.intValue(), TCDefaultValues.undergroundLakeFrequency.intValue(), TCDefaultValues.undergroundLakeRarity.intValue(), TCDefaultValues.undergroundLakeMinAltitude.intValue(), TCDefaultValues.undergroundLakeMaxAltitude.intValue()));

        // Dungeon
        resources.add(Resource.createResource(config, DungeonGen.class, TCDefaultValues.dungeonFrequency.intValue(), TCDefaultValues.dungeonRarity.intValue(), TCDefaultValues.dungeonMinAltitude.intValue(), this.worldHeight));

        // Dirt
        resources.add(Resource.createResource(config, OreGen.class, DefaultMaterial.DIRT.id, TCDefaultValues.dirtDepositSize.intValue(), TCDefaultValues.dirtDepositFrequency.intValue(), TCDefaultValues.dirtDepositRarity.intValue(), TCDefaultValues.dirtDepositMinAltitude.intValue(), this.worldHeight, DefaultMaterial.STONE.id));

        // Gravel
        resources.add(Resource.createResource(config, OreGen.class, DefaultMaterial.GRAVEL.id, TCDefaultValues.gravelDepositSize.intValue(), TCDefaultValues.gravelDepositFrequency.intValue(), TCDefaultValues.gravelDepositRarity.intValue(), TCDefaultValues.gravelDepositMinAltitude.intValue(), this.worldHeight, DefaultMaterial.STONE.id));

        // Coal
        resources.add(Resource.createResource(config, OreGen.class, DefaultMaterial.COAL_ORE.id, TCDefaultValues.coalDepositSize.intValue(), TCDefaultValues.coalDepositFrequency.intValue(), TCDefaultValues.coalDepositRarity.intValue(), TCDefaultValues.coalDepositMinAltitude.intValue(), this.worldHeight, DefaultMaterial.STONE.id));

        // Iron
        resources.add(Resource.createResource(config, OreGen.class, DefaultMaterial.IRON_ORE.id, TCDefaultValues.ironDepositSize.intValue(), TCDefaultValues.ironDepositFrequency.intValue(), TCDefaultValues.ironDepositRarity.intValue(), TCDefaultValues.ironDepositMinAltitude.intValue(), this.worldHeight / 2, DefaultMaterial.STONE.id));

        // Gold
        resources.add(Resource.createResource(config, OreGen.class, DefaultMaterial.GOLD_ORE.id, TCDefaultValues.goldDepositSize.intValue(), TCDefaultValues.goldDepositFrequency.intValue(), TCDefaultValues.goldDepositRarity.intValue(), TCDefaultValues.goldDepositMinAltitude.intValue(), this.worldHeight / 4, DefaultMaterial.STONE.id));

        // Redstone
        resources.add(Resource.createResource(config, OreGen.class, DefaultMaterial.REDSTONE_ORE.id, TCDefaultValues.redstoneDepositSize.intValue(), TCDefaultValues.redstoneDepositFrequency.intValue(), TCDefaultValues.redstoneDepositRarity.intValue(), TCDefaultValues.redstoneDepositMinAltitude.intValue(), this.worldHeight / 8, DefaultMaterial.STONE.id));

        // Diamond
        resources.add(Resource.createResource(config, OreGen.class, DefaultMaterial.DIAMOND_ORE.id, TCDefaultValues.diamondDepositSize.intValue(), TCDefaultValues.diamondDepositFrequency.intValue(), TCDefaultValues.diamondDepositRarity.intValue(), TCDefaultValues.diamondDepositMinAltitude.intValue(), this.worldHeight / 8, DefaultMaterial.STONE.id));

        // Lapislazuli
        resources.add(Resource.createResource(config, OreGen.class, DefaultMaterial.LAPIS_ORE.id, TCDefaultValues.lapislazuliDepositSize.intValue(), TCDefaultValues.lapislazuliDepositFrequency.intValue(), TCDefaultValues.lapislazuliDepositRarity.intValue(), TCDefaultValues.lapislazuliDepositMinAltitude.intValue(), this.worldHeight / 8, DefaultMaterial.STONE.id));

        // Emerald ore
        if (defaultEmeraldOre > 0)
        {
            resources.add(Resource.createResource(config, OreGen.class, DefaultMaterial.EMERALD_ORE.id, TCDefaultValues.emeraldDepositSize.intValue(), this.defaultEmeraldOre, TCDefaultValues.emeraldDepositRarity.intValue(), TCDefaultValues.emeraldDepositMinAltitude.intValue(), this.worldHeight / 4, DefaultMaterial.STONE.id));
        }

        // Under water sand
        resources.add(Resource.createResource(config, UnderWaterOreGen.class, DefaultMaterial.SAND.id, TCDefaultValues.waterSandDepositSize.intValue(), TCDefaultValues.waterSandDepositFrequency.intValue(), TCDefaultValues.waterSandDepositRarity.intValue(), DefaultMaterial.DIRT.id, DefaultMaterial.GRASS.id));

        // Under water clay
        if (this.defaultClay > 0)
        {
            resources.add(Resource.createResource(config, UnderWaterOreGen.class, DefaultMaterial.CLAY.id, TCDefaultValues.waterClayDepositSize.intValue(), this.defaultClay, TCDefaultValues.waterClayDepositRarity.intValue(), DefaultMaterial.DIRT.id, DefaultMaterial.CLAY.id));

        }
        // Custom objects
        resources.add(Resource.createResource(config, CustomObjectGen.class, "UseWorld"));

        // Boulders
        if (this.defaultBoulder != 0)
        {
            resources.add(Resource.createResource(config, BoulderGen.class, DefaultMaterial.MOSSY_COBBLESTONE, defaultBoulder, TCDefaultValues.boulderDepositRarity.intValue(), TCDefaultValues.boulderDepositMinAltitude.intValue(), TCDefaultValues.boulderDepositMaxAltitude.intValue(), DefaultMaterial.GRASS, DefaultMaterial.DIRT, DefaultMaterial.STONE));
        }

        // Ice spikes
        if (this.defaultIceSpikes)
        {
            resources.add(Resource.createResource(config, IceSpikeGen.class, DefaultMaterial.PACKED_ICE, SpikeType.HugeSpike, 3, 1.66, TCDefaultValues.iceSpikeDepositMinHeight.intValue(), TCDefaultValues.iceSpikeDepositMaxHeight.intValue(), DefaultMaterial.ICE, DefaultMaterial.DIRT, DefaultMaterial.SNOW_BLOCK));
            resources.add(Resource.createResource(config, IceSpikeGen.class, DefaultMaterial.PACKED_ICE, SpikeType.SmallSpike, 3, 98.33, TCDefaultValues.iceSpikeDepositMinHeight.intValue(), TCDefaultValues.iceSpikeDepositMaxHeight.intValue(), DefaultMaterial.ICE, DefaultMaterial.DIRT, DefaultMaterial.SNOW_BLOCK));
            resources.add(Resource.createResource(config, IceSpikeGen.class, DefaultMaterial.PACKED_ICE, SpikeType.Basement, 2, 100, TCDefaultValues.iceSpikeDepositMinHeight.intValue(), TCDefaultValues.iceSpikeDepositMaxHeight.intValue(), DefaultMaterial.ICE, DefaultMaterial.DIRT, DefaultMaterial.SNOW_BLOCK));
        }

        // Trees
        if (this.defaultTree != null)
        {
            resources.add(Resource.createResource(config, TreeGen.class, this.defaultTree));
        }

        if (this.defaultWaterLily > 0)
        {
            resources.add(Resource.createResource(config, AboveWaterGen.class, DefaultMaterial.WATER_LILY, this.defaultWaterLily, 100));
        }

        if (this.defaultPoppies > 0)
        {
            // Poppy
            resources.add(Resource.createResource(config, PlantGen.class, PlantType.Poppy, this.defaultPoppies, TCDefaultValues.roseDepositRarity.intValue(), TCDefaultValues.roseDepositMinAltitude.intValue(), this.worldHeight, DefaultMaterial.GRASS, DefaultMaterial.DIRT));
        }

        if (this.defaultBlueOrchids > 0)
        {
            // Blue orchid
            resources.add(Resource.createResource(config, PlantGen.class, PlantType.BlueOrchid, this.defaultBlueOrchids, TCDefaultValues.blueOrchidDepositRarity.intValue(), TCDefaultValues.blueOrchidDepositMinAltitude.intValue(), this.worldHeight, DefaultMaterial.GRASS, DefaultMaterial.DIRT));
        }

        if (this.defaultDandelions > 0)
        {
            // Dandelion
            resources.add(Resource.createResource(config, PlantGen.class, PlantType.Dandelion, this.defaultDandelions, TCDefaultValues.flowerDepositRarity.intValue(), TCDefaultValues.flowerDepositMinAltitude.intValue(), this.worldHeight, DefaultMaterial.GRASS, DefaultMaterial.DIRT));
        }

        if (this.defaultTallFlowers > 0)
        {
            // Lilac
            resources.add(Resource.createResource(config, PlantGen.class, PlantType.Lilac, this.defaultTallFlowers, TCDefaultValues.flowerDepositRarity.intValue(), TCDefaultValues.flowerDepositMinAltitude.intValue(), this.worldHeight, DefaultMaterial.GRASS, DefaultMaterial.DIRT));

            // Rose bush
            resources.add(Resource.createResource(config, PlantGen.class, PlantType.RoseBush, this.defaultTallFlowers, TCDefaultValues.flowerDepositRarity.intValue(), TCDefaultValues.flowerDepositMinAltitude.intValue(), this.worldHeight, DefaultMaterial.GRASS, DefaultMaterial.DIRT));

            // Peony
            resources.add(Resource.createResource(config, PlantGen.class, PlantType.Peony, this.defaultTallFlowers, TCDefaultValues.flowerDepositRarity.intValue(), TCDefaultValues.flowerDepositMinAltitude.intValue(), this.worldHeight, DefaultMaterial.GRASS, DefaultMaterial.DIRT));
        }

        if (this.defaultSunflowers > 0)
        {
            // Sunflowers
            resources.add(Resource.createResource(config, PlantGen.class, PlantType.Sunflower, this.defaultSunflowers, TCDefaultValues.flowerDepositRarity.intValue(), TCDefaultValues.flowerDepositMinAltitude.intValue(), this.worldHeight, DefaultMaterial.GRASS, DefaultMaterial.DIRT));
        }

        if (this.defaultMushroom > 0)
        {
            // Red mushroom
            resources.add(Resource.createResource(config, PlantGen.class, PlantType.RedMushroom, this.defaultMushroom, TCDefaultValues.redMushroomDepositRarity.intValue(), TCDefaultValues.redMushroomDepositMinAltitude.intValue(), this.worldHeight, defaultSurfaceBlock, DefaultMaterial.DIRT.id));

            // Brown mushroom
            resources.add(Resource.createResource(config, PlantGen.class, PlantType.BrownMushroom, this.defaultMushroom, TCDefaultValues.brownMushroomDepositRarity.intValue(), TCDefaultValues.brownMushroomDepositMinAltitude.intValue(), this.worldHeight, defaultSurfaceBlock, DefaultMaterial.DIRT.id));
        }

        if (this.defaultFerns > 0)
        {
            // Ferns
            resources.add(Resource.createResource(config, GrassGen.class, PlantType.Fern, "", this.defaultFerns, TCDefaultValues.longGrassDepositRarity.intValue(), DefaultMaterial.GRASS.id, DefaultMaterial.DIRT.id));
        }

        if (this.defaultDoubleGrass > 0)
        {
            // Double tall grass
            resources.add(Resource.createResource(config, GrassGen.class, PlantType.DoubleTallgrass, "", this.defaultDoubleGrass, TCDefaultValues.longGrassDepositRarity.intValue(), DefaultMaterial.GRASS.id, DefaultMaterial.DIRT.id));
        }

        if (this.defaultGrass > 0)
        {
            // Tall grass
            resources.add(Resource.createResource(config, GrassGen.class, PlantType.Tallgrass, "", this.defaultGrass, TCDefaultValues.longGrassDepositRarity.intValue(), DefaultMaterial.GRASS.id, DefaultMaterial.DIRT.id));
        }

        if (this.defaultLargeFerns > 0)
        {
            // Large ferns
            resources.add(Resource.createResource(config, PlantGen.class, PlantType.LargeFern, this.defaultLargeFerns, 90, 30, this.worldHeight, DefaultMaterial.GRASS, DefaultMaterial.DIRT));
        }

        if (this.defaultDeadBush > 0)
        {
            // Dead Bush
            resources.add(Resource.createResource(config, GrassGen.class, PlantType.DeadBush, 0, this.defaultDeadBush, TCDefaultValues.deadBushDepositRarity.intValue(), DefaultMaterial.SAND, DefaultMaterial.HARD_CLAY, DefaultMaterial.STAINED_CLAY, DefaultMaterial.DIRT));
        }

        // Pumpkin
        resources.add(Resource.createResource(config, PlantGen.class, DefaultMaterial.PUMPKIN, TCDefaultValues.pumpkinDepositFrequency.intValue(), TCDefaultValues.pumpkinDepositRarity.intValue(), TCDefaultValues.pumpkinDepositMinAltitude.intValue(), this.worldHeight, DefaultMaterial.GRASS.id));

        if (this.defaultReed > 0)
        {
            // Reed
            resources.add(Resource.createResource(config, ReedGen.class, DefaultMaterial.SUGAR_CANE_BLOCK, this.defaultReed, TCDefaultValues.reedDepositRarity.intValue(), TCDefaultValues.reedDepositMinAltitude.intValue(), this.worldHeight, DefaultMaterial.GRASS.id, DefaultMaterial.DIRT.id, DefaultMaterial.SAND.id));
        }

        if (this.defaultCactus > 0)
        {
            // Cactus
            resources.add(Resource.createResource(config, CactusGen.class, DefaultMaterial.CACTUS, this.defaultCactus, TCDefaultValues.cactusDepositRarity.intValue(), TCDefaultValues.cactusDepositMinAltitude.intValue(), this.worldHeight, DefaultMaterial.SAND.id));
        }
        if (this.defaultHasVines)
        {
            resources.add(Resource.createResource(config, VinesGen.class, TCDefaultValues.vinesFrequency.intValue(), TCDefaultValues.vinesRarity.intValue(), TCDefaultValues.vinesMinAltitude.intValue(), this.worldHeight, DefaultMaterial.VINE.id));
        }

        // Water source
        resources.add(Resource.createResource(config, LiquidGen.class, DefaultMaterial.WATER.id, TCDefaultValues.waterSourceDepositFrequency.intValue(), TCDefaultValues.waterSourceDepositRarity.intValue(), TCDefaultValues.waterSourceDepositMinAltitude.intValue(), this.worldHeight, DefaultMaterial.STONE.id));

        // Lava source
        resources.add(Resource.createResource(config, LiquidGen.class, DefaultMaterial.LAVA.id, TCDefaultValues.lavaSourceDepositFrequency.intValue(), TCDefaultValues.lavaSourceDepositRarity.intValue(), TCDefaultValues.lavaSourceDepositMinAltitude.intValue(), this.worldHeight, DefaultMaterial.STONE.id));

        // Desert wells
        if (this.defaultWell != null)
        {
            resources.add(Resource.createResource(config, WellGen.class, this.defaultWell));
        }

        return resources;
    }
}
