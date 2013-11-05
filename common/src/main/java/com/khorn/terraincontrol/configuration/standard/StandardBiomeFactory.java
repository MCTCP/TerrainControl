package com.khorn.terraincontrol.configuration.standard;

import com.khorn.terraincontrol.generator.resource.TreeGen;
import com.khorn.terraincontrol.generator.resource.OreGen;
import com.khorn.terraincontrol.generator.resource.WellGen;
import com.khorn.terraincontrol.generator.resource.GrassGen;
import com.khorn.terraincontrol.generator.resource.PlantGen;
import com.khorn.terraincontrol.generator.resource.CactusGen;
import com.khorn.terraincontrol.generator.resource.VinesGen;
import com.khorn.terraincontrol.generator.resource.ReedGen;
import com.khorn.terraincontrol.generator.resource.UnderWaterOreGen;
import com.khorn.terraincontrol.generator.resource.UndergroundLakeGen;
import com.khorn.terraincontrol.generator.resource.Resource;
import com.khorn.terraincontrol.generator.resource.SmallLakeGen;
import com.khorn.terraincontrol.generator.resource.DungeonGen;
import com.khorn.terraincontrol.generator.resource.LiquidGen;
import com.khorn.terraincontrol.generator.resource.AboveWaterGen;
import com.khorn.terraincontrol.generator.resource.CustomObjectGen;
import com.khorn.terraincontrol.configuration.BiomeConfigFile;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.DefaultBiome;
import com.khorn.terraincontrol.DefaultMaterial;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.configuration.BiomeConfigFile.RareBuildingType;
import com.khorn.terraincontrol.configuration.BiomeConfigFile.VillageType;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * A biome generator holds all <i>default</i> settings of a biome.
 * 
 */
public class StandardBiomeFactory
{
    // Simple registry for the default settings
    @SuppressWarnings("unchecked")
    private static final Class<? extends StandardBiomeFactory>[] defaultSettings = (Class<? extends StandardBiomeFactory>[]) new Class<?>[1024];

    /**
     * Gets the default settings for a biome. If no default settings are find
     * for this biome, some boring default settings will be returned.
     * 
     * @param biome The biome to get the defaults for.
     * @param worldHeight The height of the world (many default resources have
     *            values relative to this).
     * @return The default settings.
     */
    public static StandardBiomeFactory getDefaultSettings(LocalBiome biome, int worldHeight)
    {
        StandardBiomeFactory biomeDefaultSettings = null;
        try
        {
            // Try to get the default settings for a biome
            Class<? extends StandardBiomeFactory> settingsClass = defaultSettings[biome.getId()];
            if (settingsClass != null)
            {
                biomeDefaultSettings = (StandardBiomeFactory) settingsClass.getConstructors()[0].newInstance(biome, worldHeight);
            }
        } catch (Exception e)
        {
            TerrainControl.log(Level.SEVERE, "Cannot read default settings for biome {0}", new Object[] {biome.getName()});
            TerrainControl.printStackTrace(Level.SEVERE, e);
        }

        // Make sure that we always return something (for custom biomes)
        if (biomeDefaultSettings == null)
        {
            biomeDefaultSettings = new StandardBiomeFactory(biome, worldHeight);
        }
        return biomeDefaultSettings;
    }

    /**
     * Registers the default settings for the biome with the given id.
     * 
     * @param biomeId The biome id.
     * @param clazz The class with the default settings.
     */
    public static void registerDefaultSettings(int biomeId, Class<? extends StandardBiomeFactory> clazz)
    {
        defaultSettings[biomeId] = clazz;
    }

    // End of registry

    protected final LocalBiome minecraftBiome;
    protected final int worldHeight;

    public String defaultExtends = "";
    public boolean defaultWaterLakes = true;
    public Object[] defaultTree; // Parameters for tree resource
    public int defaultFlowers = 2;
    public int defaultGrass = 10;
    public int defaultDeadBrush = 0;
    public int defaultMushroom = 0;
    public int defaultReed = 0;
    public int defaultCactus = 0;
    public int defaultClay = 1;
    public Object[] defaultWell; // Parameters for well resource
    public float defaultBiomeSurface = 0.1F;
    public float defaultBiomeVolatility = 0.3F;
    public byte defaultSurfaceBlock = (byte) DefaultMaterial.GRASS.id;
    public byte defaultGroundBlock = (byte) DefaultMaterial.DIRT.id;
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
    private boolean defaultHasVines;

    public StandardBiomeFactory(LocalBiome minecraftBiome, int worldHeight)
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
    public List<Resource> createDefaultResources(BiomeConfigFile config)
    {
        List<Resource> resources = new ArrayList<Resource>(32);

        // Small water lakes
        if (this.defaultWaterLakes)
        {
            resources.add(Resource.createResource(config, SmallLakeGen.class, DefaultMaterial.WATER.id, BiomeStandardValues.SmallLakeWaterFrequency.intValue(), BiomeStandardValues.SmallLakeWaterRarity.intValue(), BiomeStandardValues.SmallLakeMinAltitude.intValue(), BiomeStandardValues.SmallLakeMaxAltitude.intValue()));
        }

        // Small lava lakes
        resources.add(Resource.createResource(config, SmallLakeGen.class, DefaultMaterial.LAVA.id, BiomeStandardValues.SmallLakeLavaFrequency.intValue(), BiomeStandardValues.SmallLakeLavaRarity.intValue(), BiomeStandardValues.SmallLakeMinAltitude.intValue(), BiomeStandardValues.SmallLakeMaxAltitude.intValue()));

        // Underground lakes
        resources.add(Resource.createResource(config, UndergroundLakeGen.class, BiomeStandardValues.undergroundLakeMinSize.intValue(), BiomeStandardValues.undergroundLakeMaxSize.intValue(), BiomeStandardValues.undergroundLakeFrequency.intValue(), BiomeStandardValues.undergroundLakeRarity.intValue(), BiomeStandardValues.undergroundLakeMinAltitude.intValue(), BiomeStandardValues.undergroundLakeMaxAltitude.intValue()));

        // Dungeon
        resources.add(Resource.createResource(config, DungeonGen.class, BiomeStandardValues.dungeonFrequency.intValue(), BiomeStandardValues.dungeonRarity.intValue(), BiomeStandardValues.dungeonMinAltitude.intValue(), this.worldHeight));

        // Dirt
        resources.add(Resource.createResource(config, OreGen.class, DefaultMaterial.DIRT.id, BiomeStandardValues.dirtDepositSize.intValue(), BiomeStandardValues.dirtDepositFrequency.intValue(), BiomeStandardValues.dirtDepositRarity.intValue(), BiomeStandardValues.dirtDepositMinAltitude.intValue(), this.worldHeight, DefaultMaterial.STONE.id));

        // Gravel
        resources.add(Resource.createResource(config, OreGen.class, DefaultMaterial.GRAVEL.id, BiomeStandardValues.gravelDepositSize.intValue(), BiomeStandardValues.gravelDepositFrequency.intValue(), BiomeStandardValues.gravelDepositRarity.intValue(), BiomeStandardValues.gravelDepositMinAltitude.intValue(), this.worldHeight, DefaultMaterial.STONE.id));

        // Coal
        resources.add(Resource.createResource(config, OreGen.class, DefaultMaterial.COAL_ORE.id, BiomeStandardValues.coalDepositSize.intValue(), BiomeStandardValues.coalDepositFrequency.intValue(), BiomeStandardValues.coalDepositRarity.intValue(), BiomeStandardValues.coalDepositMinAltitude.intValue(), this.worldHeight, DefaultMaterial.STONE.id));

        // Iron
        resources.add(Resource.createResource(config, OreGen.class, DefaultMaterial.IRON_ORE.id, BiomeStandardValues.ironDepositSize.intValue(), BiomeStandardValues.ironDepositFrequency.intValue(), BiomeStandardValues.ironDepositRarity.intValue(), BiomeStandardValues.ironDepositMinAltitude.intValue(), this.worldHeight / 2, DefaultMaterial.STONE.id));

        // Gold
        resources.add(Resource.createResource(config, OreGen.class, DefaultMaterial.GOLD_ORE.id, BiomeStandardValues.goldDepositSize.intValue(), BiomeStandardValues.goldDepositFrequency.intValue(), BiomeStandardValues.goldDepositRarity.intValue(), BiomeStandardValues.goldDepositMinAltitude.intValue(), this.worldHeight / 4, DefaultMaterial.STONE.id));

        // Redstone
        resources.add(Resource.createResource(config, OreGen.class, DefaultMaterial.REDSTONE_ORE.id, BiomeStandardValues.redstoneDepositSize.intValue(), BiomeStandardValues.redstoneDepositFrequency.intValue(), BiomeStandardValues.redstoneDepositRarity.intValue(), BiomeStandardValues.redstoneDepositMinAltitude.intValue(), this.worldHeight / 8, DefaultMaterial.STONE.id));

        // Diamond
        resources.add(Resource.createResource(config, OreGen.class, DefaultMaterial.DIAMOND_ORE.id, BiomeStandardValues.diamondDepositSize.intValue(), BiomeStandardValues.diamondDepositFrequency.intValue(), BiomeStandardValues.diamondDepositRarity.intValue(), BiomeStandardValues.diamondDepositMinAltitude.intValue(), this.worldHeight / 8, DefaultMaterial.STONE.id));

        // Lapislazuli
        resources.add(Resource.createResource(config, OreGen.class, DefaultMaterial.LAPIS_ORE.id, BiomeStandardValues.lapislazuliDepositSize.intValue(), BiomeStandardValues.lapislazuliDepositFrequency.intValue(), BiomeStandardValues.lapislazuliDepositRarity.intValue(), BiomeStandardValues.lapislazuliDepositMinAltitude.intValue(), this.worldHeight / 8, DefaultMaterial.STONE.id));

        // Emerald ore
        if (defaultEmeraldOre > 0)
        {
            resources.add(Resource.createResource(config, OreGen.class, DefaultMaterial.EMERALD_ORE.id, BiomeStandardValues.emeraldDepositSize.intValue(), this.defaultEmeraldOre, BiomeStandardValues.emeraldDepositRarity.intValue(), BiomeStandardValues.emeraldDepositMinAltitude.intValue(), this.worldHeight / 4, DefaultMaterial.STONE.id));
        }

        // Under water sand
        resources.add(Resource.createResource(config, UnderWaterOreGen.class, DefaultMaterial.SAND.id, BiomeStandardValues.waterSandDepositSize.intValue(), BiomeStandardValues.waterSandDepositFrequency.intValue(), BiomeStandardValues.waterSandDepositRarity.intValue(), DefaultMaterial.DIRT.id, DefaultMaterial.GRASS.id));

        // Under water clay
        if (this.defaultClay > 0)
        {
            resources.add(Resource.createResource(config, UnderWaterOreGen.class, DefaultMaterial.CLAY.id, BiomeStandardValues.waterClayDepositSize.intValue(), this.defaultClay, BiomeStandardValues.waterClayDepositRarity.intValue(), DefaultMaterial.DIRT.id, DefaultMaterial.CLAY.id));

        }
        // Custom objects
        resources.add(Resource.createResource(config, CustomObjectGen.class, "UseWorld"));

        // Trees
        if (this.defaultTree != null)
        {
            resources.add(Resource.createResource(config, TreeGen.class, this.defaultTree));
        }

        if (this.defaultWaterLily > 0)
        {
            resources.add(Resource.createResource(config, AboveWaterGen.class, DefaultMaterial.WATER_LILY.id, this.defaultWaterLily, 100));
        }

        if (this.defaultFlowers > 0)
        {
            // Red flower
            resources.add(Resource.createResource(config, PlantGen.class, DefaultMaterial.RED_ROSE.id, this.defaultFlowers, BiomeStandardValues.roseDepositRarity.intValue(), BiomeStandardValues.roseDepositMinAltitude.intValue(), this.worldHeight, DefaultMaterial.GRASS.id, DefaultMaterial.DIRT.id, DefaultMaterial.SOIL.id));

            // Yellow flower
            resources.add(Resource.createResource(config, PlantGen.class, DefaultMaterial.YELLOW_FLOWER.id, this.defaultFlowers, BiomeStandardValues.flowerDepositRarity.intValue(), BiomeStandardValues.flowerDepositMinAltitude.intValue(), this.worldHeight, DefaultMaterial.GRASS.id, DefaultMaterial.DIRT.id, DefaultMaterial.SOIL.id));
        }

        if (this.defaultMushroom > 0)
        {
            // Red mushroom
            resources.add(Resource.createResource(config, PlantGen.class, DefaultMaterial.RED_MUSHROOM.id, this.defaultMushroom, BiomeStandardValues.redMushroomDepositRarity.intValue(), BiomeStandardValues.redMushroomDepositMinAltitude.intValue(), this.worldHeight, defaultSurfaceBlock, DefaultMaterial.DIRT.id));

            // Brown mushroom
            resources.add(Resource.createResource(config, PlantGen.class, DefaultMaterial.BROWN_MUSHROOM.id, this.defaultMushroom, BiomeStandardValues.brownMushroomDepositRarity.intValue(), BiomeStandardValues.brownMushroomDepositMinAltitude.intValue(), this.worldHeight, defaultSurfaceBlock, DefaultMaterial.DIRT.id));
        }

        if (this.defaultGrass > 0)
        {
            // Grass
            resources.add(Resource.createResource(config, GrassGen.class, DefaultMaterial.LONG_GRASS.id, 1, this.defaultGrass, BiomeStandardValues.longGrassDepositRarity.intValue(), DefaultMaterial.GRASS.id, DefaultMaterial.DIRT.id));
        }

        if (this.defaultDeadBrush > 0)
        {
            // Dead Bush
            resources.add(Resource.createResource(config, GrassGen.class, DefaultMaterial.DEAD_BUSH.id, 0, this.defaultDeadBrush, BiomeStandardValues.deadBushDepositRarity.intValue(), DefaultMaterial.SAND.id));
        }

        // Pumpkin
        resources.add(Resource.createResource(config, PlantGen.class, DefaultMaterial.PUMPKIN.id, BiomeStandardValues.pumpkinDepositFrequency.intValue(), BiomeStandardValues.pumpkinDepositRarity.intValue(), BiomeStandardValues.pumpkinDepositMinAltitude.intValue(), this.worldHeight, DefaultMaterial.GRASS.id));

        if (this.defaultReed > 0)
        {
            // Reed
            resources.add(Resource.createResource(config, ReedGen.class, DefaultMaterial.SUGAR_CANE_BLOCK.id, this.defaultReed, BiomeStandardValues.reedDepositRarity.intValue(), BiomeStandardValues.reedDepositMinAltitude.intValue(), this.worldHeight, DefaultMaterial.GRASS.id, DefaultMaterial.DIRT.id, DefaultMaterial.SAND.id));
        }

        if (this.defaultCactus > 0)
        {
            // Cactus
            resources.add(Resource.createResource(config, CactusGen.class, DefaultMaterial.CACTUS.id, this.defaultCactus, BiomeStandardValues.cactusDepositRarity.intValue(), BiomeStandardValues.cactusDepositMinAltitude.intValue(), this.worldHeight, DefaultMaterial.SAND.id));
        }
        if (this.defaultHasVines)
        {
            resources.add(Resource.createResource(config, VinesGen.class, BiomeStandardValues.vinesFrequency.intValue(), BiomeStandardValues.vinesRarity.intValue(), BiomeStandardValues.vinesMinAltitude.intValue(), this.worldHeight, DefaultMaterial.VINE.id));
        }

        // Water source
        resources.add(Resource.createResource(config, LiquidGen.class, DefaultMaterial.WATER.id, BiomeStandardValues.waterSourceDepositFrequency.intValue(), BiomeStandardValues.waterSourceDepositRarity.intValue(), BiomeStandardValues.waterSourceDepositMinAltitude.intValue(), this.worldHeight, DefaultMaterial.STONE.id));

        // Lava source
        resources.add(Resource.createResource(config, LiquidGen.class, DefaultMaterial.LAVA.id, BiomeStandardValues.lavaSourceDepositFrequency.intValue(), BiomeStandardValues.lavaSourceDepositRarity.intValue(), BiomeStandardValues.lavaSourceDepositMinAltitude.intValue(), this.worldHeight, DefaultMaterial.STONE.id));

        // Desert wells
        if (this.defaultWell != null)
        {
            resources.add(Resource.createResource(config, WellGen.class, this.defaultWell));
        }

        return resources;
    }
}
