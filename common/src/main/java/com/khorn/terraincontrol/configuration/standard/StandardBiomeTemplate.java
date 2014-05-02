package com.khorn.terraincontrol.configuration.standard;

import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.BiomeConfig.RareBuildingType;
import com.khorn.terraincontrol.configuration.BiomeConfig.VillageType;
import com.khorn.terraincontrol.generator.resource.*;
import com.khorn.terraincontrol.generator.resource.IceSpikeGen.SpikeType;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultBiome;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;

import java.util.ArrayList;
import java.util.List;

/**
 * A biome generator holds all <i>default</i> settings of a biome.
 * 
 */
public class StandardBiomeTemplate
{
    protected final int worldHeight;

    public boolean isCustomBiome = true;

    public String defaultExtends = "";
    public boolean defaultWaterLakes = true;
    public Object[] defaultTree; // Parameters for tree resource
    public int defaultDandelions = 2;
    public int defaultPoppies = 0;
    public int defaultBlueOrchids = 0;
    public int defaultTallFlowers = 0;
    public int defaultSunflowers = 0;
    public int defaultTulips = 0;
    public int defaultAzureBluets = 0;
    public int defaultOxeyeDaisies = 0;
    public int defaultAlliums = 0;
    public int defaultGrass = 10;
    public boolean defaultGrassIsGrouped = false;
    public int defaultDoubleGrass = 0;
    public boolean defaultDoubleGrassIsGrouped = false;
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
    public DefaultMaterial defaultSurfaceBlock = DefaultMaterial.GRASS;
    public DefaultMaterial defaultGroundBlock = DefaultMaterial.DIRT;
    public float defaultBiomeTemperature = 0.5F;
    public float defaultBiomeWetness = 0.5F;
    public ArrayList<String> defaultIsle = new ArrayList<String>();
    public ArrayList<String> defaultBorder = new ArrayList<String>();
    public ArrayList<String> defaultNotBorderNear = new ArrayList<String>();
    public String defaultRiverBiome = DefaultBiome.RIVER.Name;
    public int defaultSize = 4;
    public int defaultRarity = 100;
    public int defaultColor = 0x000000;
    public int defaultWaterLily = 0;
    public int defaultWaterColorMultiplier = 0xFFFFFF;
    public int defaultGrassColor = 0xFFFFFF;
    public int defaultFoliageColor = 0xFFFFFF;
    public boolean defaultStrongholds = true;
    public VillageType defaultVillageType = VillageType.disabled;
    public RareBuildingType defaultRareBuildingType = RareBuildingType.disabled;
    public int defaultEmeraldOre = 0;
    public boolean defaultHasVines;
    public int defaultBoulder = 0;
    public Object[] defaultSurfaceSurfaceAndGroundControl = new Object[0];
    public boolean defaultIceSpikes;

    public StandardBiomeTemplate(int worldHeight)
    {
        this.worldHeight = worldHeight;
    }

    /**
     * Creates the default resources.
     * 
     * @param config The biome config. Custom objects must already be loaded.
     * @return The default resources for this biome.
     */
    public List<Resource> createDefaultResources(BiomeConfig config)
    {
        int worldOreHeight = 128; // Should we make this dynamic for 8-bits scale worlds?
        List<Resource> resources = new ArrayList<Resource>(32);

        // Small water lakes
        if (this.defaultWaterLakes)
        {
            resources.add(Resource.createResource(config, SmallLakeGen.class, DefaultMaterial.WATER, BiomeStandardValues.SmallLakeWaterFrequency, BiomeStandardValues.SmallLakeWaterRarity, BiomeStandardValues.SmallLakeMinAltitude, BiomeStandardValues.SmallLakeMaxAltitude));
        }

        // Small lava lakes
        resources.add(Resource.createResource(config, SmallLakeGen.class, DefaultMaterial.LAVA, BiomeStandardValues.SmallLakeLavaFrequency, BiomeStandardValues.SmallLakeLavaRarity, BiomeStandardValues.SmallLakeMinAltitude, BiomeStandardValues.SmallLakeMaxAltitude));

        // Underground lakes
        resources.add(Resource.createResource(config, UndergroundLakeGen.class, BiomeStandardValues.undergroundLakeMinSize, BiomeStandardValues.undergroundLakeMaxSize, BiomeStandardValues.undergroundLakeFrequency, BiomeStandardValues.undergroundLakeRarity, BiomeStandardValues.undergroundLakeMinAltitude, BiomeStandardValues.undergroundLakeMaxAltitude));

        // Dungeon
        resources.add(Resource.createResource(config, DungeonGen.class, BiomeStandardValues.dungeonFrequency, BiomeStandardValues.dungeonRarity, BiomeStandardValues.dungeonMinAltitude, this.worldHeight));

        // Dirt
        resources.add(Resource.createResource(config, OreGen.class, DefaultMaterial.DIRT, BiomeStandardValues.dirtDepositSize, BiomeStandardValues.dirtDepositFrequency, BiomeStandardValues.dirtDepositRarity, BiomeStandardValues.dirtDepositMinAltitude, this.worldHeight, DefaultMaterial.STONE));

        // Gravel
        resources.add(Resource.createResource(config, OreGen.class, DefaultMaterial.GRAVEL, BiomeStandardValues.gravelDepositSize, BiomeStandardValues.gravelDepositFrequency, BiomeStandardValues.gravelDepositRarity, BiomeStandardValues.gravelDepositMinAltitude, this.worldHeight, DefaultMaterial.STONE));

        // Coal
        resources.add(Resource.createResource(config, OreGen.class, DefaultMaterial.COAL_ORE, BiomeStandardValues.coalDepositSize, BiomeStandardValues.coalDepositFrequency, BiomeStandardValues.coalDepositRarity, BiomeStandardValues.coalDepositMinAltitude, worldOreHeight, DefaultMaterial.STONE));

        // Iron
        resources.add(Resource.createResource(config, OreGen.class, DefaultMaterial.IRON_ORE, BiomeStandardValues.ironDepositSize, BiomeStandardValues.ironDepositFrequency, BiomeStandardValues.ironDepositRarity, BiomeStandardValues.ironDepositMinAltitude, worldOreHeight / 2, DefaultMaterial.STONE));

        // Gold
        resources.add(Resource.createResource(config, OreGen.class, DefaultMaterial.GOLD_ORE, BiomeStandardValues.goldDepositSize, BiomeStandardValues.goldDepositFrequency, BiomeStandardValues.goldDepositRarity, BiomeStandardValues.goldDepositMinAltitude, worldOreHeight / 4, DefaultMaterial.STONE));

        // Redstone
        resources.add(Resource.createResource(config, OreGen.class, DefaultMaterial.REDSTONE_ORE, BiomeStandardValues.redstoneDepositSize, BiomeStandardValues.redstoneDepositFrequency, BiomeStandardValues.redstoneDepositRarity, BiomeStandardValues.redstoneDepositMinAltitude, worldOreHeight / 8, DefaultMaterial.STONE));

        // Diamond
        resources.add(Resource.createResource(config, OreGen.class, DefaultMaterial.DIAMOND_ORE, BiomeStandardValues.diamondDepositSize, BiomeStandardValues.diamondDepositFrequency, BiomeStandardValues.diamondDepositRarity, BiomeStandardValues.diamondDepositMinAltitude, worldOreHeight / 8, DefaultMaterial.STONE));

        // Lapislazuli
        resources.add(Resource.createResource(config, OreGen.class, DefaultMaterial.LAPIS_ORE, BiomeStandardValues.lapislazuliDepositSize, BiomeStandardValues.lapislazuliDepositFrequency, BiomeStandardValues.lapislazuliDepositRarity, BiomeStandardValues.lapislazuliDepositMinAltitude, worldOreHeight / 8, DefaultMaterial.STONE));

        // Emerald ore
        if (defaultEmeraldOre > 0)
        {
            resources.add(Resource.createResource(config, OreGen.class, DefaultMaterial.EMERALD_ORE, BiomeStandardValues.emeraldDepositSize, this.defaultEmeraldOre, BiomeStandardValues.emeraldDepositRarity, BiomeStandardValues.emeraldDepositMinAltitude, worldOreHeight / 4, DefaultMaterial.STONE));
        }

        // Under water sand
        resources.add(Resource.createResource(config, UnderWaterOreGen.class, DefaultMaterial.SAND, BiomeStandardValues.waterSandDepositSize, BiomeStandardValues.waterSandDepositFrequency, BiomeStandardValues.waterSandDepositRarity, DefaultMaterial.DIRT, DefaultMaterial.GRASS));

        // Under water clay
        if (this.defaultClay > 0)
        {
            resources.add(Resource.createResource(config, UnderWaterOreGen.class, DefaultMaterial.CLAY, BiomeStandardValues.waterClayDepositSize, this.defaultClay, BiomeStandardValues.waterClayDepositRarity, DefaultMaterial.DIRT, DefaultMaterial.CLAY));

        }
        // Custom objects
        resources.add(Resource.createResource(config, CustomObjectGen.class, "UseWorld"));

        // Boulders
        if (this.defaultBoulder != 0)
        {
            resources.add(Resource.createResource(config, BoulderGen.class, DefaultMaterial.MOSSY_COBBLESTONE, defaultBoulder, BiomeStandardValues.boulderDepositRarity, BiomeStandardValues.boulderDepositMinAltitude, BiomeStandardValues.boulderDepositMaxAltitude, DefaultMaterial.GRASS, DefaultMaterial.DIRT, DefaultMaterial.STONE));
        }

        // Ice spikes
        if (this.defaultIceSpikes)
        {
            resources.add(Resource.createResource(config, IceSpikeGen.class, DefaultMaterial.PACKED_ICE, SpikeType.HugeSpike, 3, 1.66, BiomeStandardValues.iceSpikeDepositMinHeight, BiomeStandardValues.iceSpikeDepositMaxHeight, DefaultMaterial.ICE, DefaultMaterial.DIRT, DefaultMaterial.SNOW_BLOCK));
            resources.add(Resource.createResource(config, IceSpikeGen.class, DefaultMaterial.PACKED_ICE, SpikeType.SmallSpike, 3, 98.33, BiomeStandardValues.iceSpikeDepositMinHeight, BiomeStandardValues.iceSpikeDepositMaxHeight, DefaultMaterial.ICE, DefaultMaterial.DIRT, DefaultMaterial.SNOW_BLOCK));
            resources.add(Resource.createResource(config, IceSpikeGen.class, DefaultMaterial.PACKED_ICE, SpikeType.Basement, 2, 100, BiomeStandardValues.iceSpikeDepositMinHeight, BiomeStandardValues.iceSpikeDepositMaxHeight, DefaultMaterial.ICE, DefaultMaterial.DIRT, DefaultMaterial.SNOW_BLOCK));
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
            resources.add(Resource.createResource(config, PlantGen.class, PlantType.Poppy, this.defaultPoppies, BiomeStandardValues.roseDepositRarity, BiomeStandardValues.roseDepositMinAltitude, this.worldHeight, DefaultMaterial.GRASS, DefaultMaterial.DIRT));
        }

        if (this.defaultBlueOrchids > 0)
        {
            // Blue orchid
            resources.add(Resource.createResource(config, PlantGen.class, PlantType.BlueOrchid, this.defaultBlueOrchids, BiomeStandardValues.blueOrchidDepositRarity, BiomeStandardValues.blueOrchidDepositMinAltitude, this.worldHeight, DefaultMaterial.GRASS, DefaultMaterial.DIRT));
        }

        if (this.defaultDandelions > 0)
        {
            // Dandelion
            resources.add(Resource.createResource(config, PlantGen.class, PlantType.Dandelion, this.defaultDandelions, BiomeStandardValues.flowerDepositRarity, BiomeStandardValues.flowerDepositMinAltitude, this.worldHeight, DefaultMaterial.GRASS, DefaultMaterial.DIRT));
        }

        if (this.defaultTallFlowers > 0)
        {
            // Lilac
            resources.add(Resource.createResource(config, PlantGen.class, PlantType.Lilac, this.defaultTallFlowers, BiomeStandardValues.flowerDepositRarity, BiomeStandardValues.flowerDepositMinAltitude, this.worldHeight, DefaultMaterial.GRASS, DefaultMaterial.DIRT));

            // Rose bush
            resources.add(Resource.createResource(config, PlantGen.class, PlantType.RoseBush, this.defaultTallFlowers, BiomeStandardValues.flowerDepositRarity, BiomeStandardValues.flowerDepositMinAltitude, this.worldHeight, DefaultMaterial.GRASS, DefaultMaterial.DIRT));

            // Peony
            resources.add(Resource.createResource(config, PlantGen.class, PlantType.Peony, this.defaultTallFlowers, BiomeStandardValues.flowerDepositRarity, BiomeStandardValues.flowerDepositMinAltitude, this.worldHeight, DefaultMaterial.GRASS, DefaultMaterial.DIRT));
        }

        if (this.defaultSunflowers > 0)
        {
            // Sunflower
            resources.add(Resource.createResource(config, PlantGen.class, PlantType.Sunflower, this.defaultSunflowers, BiomeStandardValues.flowerDepositRarity, BiomeStandardValues.flowerDepositMinAltitude, this.worldHeight, DefaultMaterial.GRASS, DefaultMaterial.DIRT));
        }
        
        if (this.defaultTulips > 0)
        {
            // Tulip
            resources.add(Resource.createResource(config, PlantGen.class, PlantType.OrangeTulip, this.defaultTulips, BiomeStandardValues.tulipDepositRarity, BiomeStandardValues.flowerDepositMinAltitude, this.worldHeight, DefaultMaterial.GRASS, DefaultMaterial.DIRT));
            resources.add(Resource.createResource(config, PlantGen.class, PlantType.RedTulip, this.defaultTulips, BiomeStandardValues.tulipDepositRarity, BiomeStandardValues.flowerDepositMinAltitude, this.worldHeight, DefaultMaterial.GRASS, DefaultMaterial.DIRT));
            resources.add(Resource.createResource(config, PlantGen.class, PlantType.WhiteTulip, this.defaultTulips, BiomeStandardValues.tulipDepositRarity, BiomeStandardValues.flowerDepositMinAltitude, this.worldHeight, DefaultMaterial.GRASS, DefaultMaterial.DIRT));
            resources.add(Resource.createResource(config, PlantGen.class, PlantType.PinkTulip, this.defaultTulips, BiomeStandardValues.tulipDepositRarity, BiomeStandardValues.flowerDepositMinAltitude, this.worldHeight, DefaultMaterial.GRASS, DefaultMaterial.DIRT));
        }
        
        if (this.defaultAzureBluets > 0)
        {
            // Azure bluet
            resources.add(Resource.createResource(config, PlantGen.class, PlantType.AzureBluet, this.defaultDandelions, BiomeStandardValues.flowerDepositRarity, BiomeStandardValues.flowerDepositMinAltitude, this.worldHeight, DefaultMaterial.GRASS, DefaultMaterial.DIRT));
        
        }
        
        if (this.defaultAlliums > 0)
        {
            // Allium
            resources.add(Resource.createResource(config, PlantGen.class, PlantType.Allium, this.defaultDandelions, BiomeStandardValues.flowerDepositRarity, BiomeStandardValues.flowerDepositMinAltitude, this.worldHeight, DefaultMaterial.GRASS, DefaultMaterial.DIRT));
        
        }
        
        if (this.defaultOxeyeDaisies > 0)
        {
            // Oxeye Daisy
            resources.add(Resource.createResource(config, PlantGen.class, PlantType.OxeyeDaisy, this.defaultDandelions, BiomeStandardValues.flowerDepositRarity, BiomeStandardValues.flowerDepositMinAltitude, this.worldHeight, DefaultMaterial.GRASS, DefaultMaterial.DIRT));
        }

        if (this.defaultMushroom > 0)
        {
            // Red mushroom
            resources.add(Resource.createResource(config, PlantGen.class, PlantType.RedMushroom, this.defaultMushroom, BiomeStandardValues.redMushroomDepositRarity, BiomeStandardValues.redMushroomDepositMinAltitude, this.worldHeight, defaultSurfaceBlock, DefaultMaterial.DIRT));

            // Brown mushroom
            resources.add(Resource.createResource(config, PlantGen.class, PlantType.BrownMushroom, this.defaultMushroom, BiomeStandardValues.brownMushroomDepositRarity, BiomeStandardValues.brownMushroomDepositMinAltitude, this.worldHeight, defaultSurfaceBlock, DefaultMaterial.DIRT));
        }

        if (this.defaultFerns > 0)
        {
            // Ferns
            resources.add(Resource.createResource(config, GrassGen.class, PlantType.Fern, GrassGen.GroupOption.NotGrouped, this.defaultFerns, BiomeStandardValues.longGrassDepositRarity, DefaultMaterial.GRASS, DefaultMaterial.DIRT));
        }

        if (this.defaultDoubleGrass > 0)
        {
            // Double tall grass
            if (this.defaultDoubleGrassIsGrouped)
            {
                resources.add(Resource.createResource(config, GrassGen.class, PlantType.DoubleTallgrass, GrassGen.GroupOption.Grouped, this.defaultDoubleGrass, BiomeStandardValues.doubleGrassGroupedDepositRarity, DefaultMaterial.GRASS, DefaultMaterial.DIRT));
            } else
            {
                resources.add(Resource.createResource(config, GrassGen.class, PlantType.DoubleTallgrass, GrassGen.GroupOption.NotGrouped, this.defaultDoubleGrass, BiomeStandardValues.doubleGrassDepositRarity, DefaultMaterial.GRASS, DefaultMaterial.DIRT));
            }
        }

        if (this.defaultGrass > 0)
        {
            // Tall grass
            if (this.defaultGrassIsGrouped)
            {
                resources.add(Resource.createResource(config, GrassGen.class, PlantType.Tallgrass, GrassGen.GroupOption.Grouped, this.defaultGrass, BiomeStandardValues.longGrassGroupedDepositRarity, DefaultMaterial.GRASS, DefaultMaterial.DIRT));
            } else
            {
                resources.add(Resource.createResource(config, GrassGen.class, PlantType.Tallgrass, GrassGen.GroupOption.NotGrouped, this.defaultGrass, BiomeStandardValues.longGrassDepositRarity, DefaultMaterial.GRASS, DefaultMaterial.DIRT));
            }
        }

        if (this.defaultLargeFerns > 0)
        {
            // Large ferns
            resources.add(Resource.createResource(config, PlantGen.class, PlantType.LargeFern, this.defaultLargeFerns, 90, 30, this.worldHeight, DefaultMaterial.GRASS, DefaultMaterial.DIRT));
        }

        if (this.defaultDeadBush > 0)
        {
            // Dead Bush
            resources.add(Resource.createResource(config, GrassGen.class, PlantType.DeadBush, 0, this.defaultDeadBush, BiomeStandardValues.deadBushDepositRarity, DefaultMaterial.SAND, DefaultMaterial.HARD_CLAY, DefaultMaterial.STAINED_CLAY, DefaultMaterial.DIRT));
        }

        // Pumpkin
        resources.add(Resource.createResource(config, PlantGen.class, DefaultMaterial.PUMPKIN, BiomeStandardValues.pumpkinDepositFrequency, BiomeStandardValues.pumpkinDepositRarity, BiomeStandardValues.pumpkinDepositMinAltitude, this.worldHeight, DefaultMaterial.GRASS));

        if (this.defaultReed > 0)
        {
            // Reed
            resources.add(Resource.createResource(config, ReedGen.class, DefaultMaterial.SUGAR_CANE_BLOCK, this.defaultReed, BiomeStandardValues.reedDepositRarity, BiomeStandardValues.reedDepositMinAltitude, this.worldHeight, DefaultMaterial.GRASS, DefaultMaterial.DIRT, DefaultMaterial.SAND));
        }

        if (this.defaultCactus > 0)
        {
            // Cactus
            resources.add(Resource.createResource(config, CactusGen.class, DefaultMaterial.CACTUS, this.defaultCactus, BiomeStandardValues.cactusDepositRarity, BiomeStandardValues.cactusDepositMinAltitude, this.worldHeight, DefaultMaterial.SAND));
        }
        if (this.defaultHasVines)
        {
            resources.add(Resource.createResource(config, VinesGen.class, BiomeStandardValues.vinesFrequency, BiomeStandardValues.vinesRarity, BiomeStandardValues.vinesMinAltitude, this.worldHeight, DefaultMaterial.VINE));
        }

        // Water source
        resources.add(Resource.createResource(config, LiquidGen.class, DefaultMaterial.WATER, BiomeStandardValues.waterSourceDepositFrequency, BiomeStandardValues.waterSourceDepositRarity, BiomeStandardValues.waterSourceDepositMinAltitude, this.worldHeight, DefaultMaterial.STONE));

        // Lava source
        resources.add(Resource.createResource(config, LiquidGen.class, DefaultMaterial.LAVA, BiomeStandardValues.lavaSourceDepositFrequency, BiomeStandardValues.lavaSourceDepositRarity, BiomeStandardValues.lavaSourceDepositMinAltitude, this.worldHeight, DefaultMaterial.STONE));

        // Desert wells
        if (this.defaultWell != null)
        {
            resources.add(Resource.createResource(config, WellGen.class, this.defaultWell));
        }

        return resources;
    }
}
