package com.khorn.terraincontrol.configuration.standard;

import static com.khorn.terraincontrol.configuration.standard.BiomeStandardValues.*;

import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.BiomeConfig.MineshaftType;
import com.khorn.terraincontrol.configuration.BiomeConfig.RareBuildingType;
import com.khorn.terraincontrol.configuration.BiomeConfig.VillageType;
import com.khorn.terraincontrol.configuration.WeightedMobSpawnGroup;
import com.khorn.terraincontrol.generator.resource.*;
import com.khorn.terraincontrol.generator.resource.IceSpikeGen.SpikeType;
import com.khorn.terraincontrol.generator.terrain.TerrainShapeBase;
import com.khorn.terraincontrol.util.MaterialSet;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultBiome;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;
import com.khorn.terraincontrol.util.minecraftTypes.MobNames;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
    public int defaultMelons = 0;
    public int defaultWaterSand = 3;
    public int defaultWaterGravel = 1;
    public int defaultSwampPatches = 0;
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
    public int defaultSizeWhenIsle = 6;
    public int defaultSizeWhenBorder = 8;
    public int defaultRarity = 100;
    public int defaultRarityWhenIsle = 97;
    public int defaultColor = 0x000000;
    public int defaultWaterLily = 0;
    public int defaultWaterColorMultiplier = 0xFFFFFF;
    public int defaultGrassColor = 0xFFFFFF;
    public int defaultFoliageColor = 0xFFFFFF;
    public boolean defaultStrongholds = true;
    public boolean defaultOceanMonuments = false;
    public boolean defaultMansions = false;
    public VillageType defaultVillageType = VillageType.disabled;
    public RareBuildingType defaultRareBuildingType = RareBuildingType.disabled;
    public MineshaftType defaultMineshaftType = MineshaftType.normal;
    public int defaultEmeraldOre = 0;
    public boolean defaultHasVines;
    public int defaultBoulder = 0;
    public Object[] defaultSurfaceSurfaceAndGroundControl = new Object[0];
    public boolean defaultIceSpikes;
    public boolean defaultDisableBiomeHeight;
    public double[] defaultCustomHeightControl = new double[TerrainShapeBase.PIECES_PER_CHUNK_Y + 1];;
    public double defaultFossilRarity = 0;

    public List<WeightedMobSpawnGroup> defaultCreatures = Arrays.asList(
            new WeightedMobSpawnGroup(MobNames.SHEEP, 12, 4, 4),
            new WeightedMobSpawnGroup(MobNames.PIG, 10, 4, 4),
            new WeightedMobSpawnGroup(MobNames.CHICKEN, 10, 4, 4),
            new WeightedMobSpawnGroup(MobNames.COW, 8, 4, 4));
    public List<WeightedMobSpawnGroup> defaultMonsters = Arrays.asList(
            new WeightedMobSpawnGroup(MobNames.SPIDER, 100, 4, 4),
            new WeightedMobSpawnGroup(MobNames.ZOMBIE, 100, 4, 4),
            new WeightedMobSpawnGroup(MobNames.SKELETON, 100, 4, 4),
            new WeightedMobSpawnGroup(MobNames.CREEPER, 100, 4, 4),
            new WeightedMobSpawnGroup(MobNames.SLIME, 100, 4, 4),
            new WeightedMobSpawnGroup(MobNames.ENDERMAN, 10, 1, 4),
            new WeightedMobSpawnGroup(MobNames.WITCH, 5, 1, 1));
    public List<WeightedMobSpawnGroup> defaultAmbientCreatures = Collections.singletonList(
            new WeightedMobSpawnGroup(MobNames.BAT, 10, 8, 8));
    public List<WeightedMobSpawnGroup> defaultWaterCreatures = Collections.singletonList(
            new WeightedMobSpawnGroup(MobNames.SQUID, 10, 4, 4));

    public StandardBiomeTemplate(int worldHeight)
    {
        this.worldHeight = worldHeight;
    }

    /**
     * Creates the default resources.
     * 
     * @param config
     *            The biome config. Custom objects must already be loaded.
     * @return The default resources for this biome.
     */
    public List<Resource> createDefaultResources(BiomeConfig config)
    {
        List<Resource> resources = new ArrayList<Resource>(32);

        // Small water lakes
        if (this.defaultWaterLakes)
        {
            resources.add(Resource.createResource(config, SmallLakeGen.class, DefaultMaterial.WATER, SmallLakeWaterFrequency,
                    SmallLakeWaterRarity, SmallLakeMinAltitude, SmallLakeMaxAltitude));
        }

        // Small lava lakes
        resources.add(Resource.createResource(config, SmallLakeGen.class, DefaultMaterial.LAVA, SmallLakeLavaFrequency,
                SmallLakeLavaRarity, SmallLakeMinAltitude, SmallLakeMaxAltitude));

        // Underground lakes
        resources.add(Resource
                .createResource(config, UndergroundLakeGen.class, undergroundLakeMinSize, undergroundLakeMaxSize, undergroundLakeFrequency,
                        undergroundLakeRarity, undergroundLakeMinAltitude, undergroundLakeMaxAltitude));

        // Dungeon
        resources.add(Resource.createResource(config, DungeonGen.class, dungeonFrequency, dungeonRarity, dungeonMinAltitude,
                this.worldHeight));
        
        // Fossil
        if (defaultFossilRarity > 0)
        {
            resources.add(Resource.createResource(config, FossilGen.class, defaultFossilRarity));
        }

        // Dirt
        resources.add(Resource.createResource(config, OreGen.class, DefaultMaterial.DIRT, dirtDepositSize, dirtDepositFrequency,
                dirtDepositRarity, dirtDepositMinAltitude, dirtDepositMaxAltitude, DefaultMaterial.STONE));

        // Gravel
        resources.add(Resource.createResource(config, OreGen.class, DefaultMaterial.GRAVEL, gravelDepositSize, gravelDepositFrequency,
                gravelDepositRarity, gravelDepositMinAltitude, gravelDepositMaxAltitude, DefaultMaterial.STONE));

        // Granite
        resources.add(Resource.createResource(config, OreGen.class, DefaultMaterial.STONE + ":1", graniteDepositSize,
                graniteDepositFrequency, graniteDepositRarity, graniteDepositMinAltitude,
                graniteDepositMaxAltitude, DefaultMaterial.STONE));

        // Diorite
        resources.add(Resource.createResource(config, OreGen.class, DefaultMaterial.STONE + ":3", dioriteDepositSize,
                dioriteDepositFrequency, dioriteDepositRarity, dioriteDepositMinAltitude,
                dioriteDepositMaxAltitude, DefaultMaterial.STONE));

        // Andesite
        resources.add(Resource.createResource(config, OreGen.class, DefaultMaterial.STONE + ":5", andesiteDepositSize,
                andesiteDepositFrequency, andesiteDepositRarity, andesiteDepositMinAltitude,
                andesiteDepositMaxAltitude, DefaultMaterial.STONE));

        // Coal
        resources.add(Resource.createResource(config, OreGen.class, DefaultMaterial.COAL_ORE, coalDepositSize, coalDepositFrequency,
                coalDepositRarity, coalDepositMinAltitude, coalDepositMaxAltitude, DefaultMaterial.STONE));

        // Iron
        resources.add(Resource.createResource(config, OreGen.class, DefaultMaterial.IRON_ORE, ironDepositSize, ironDepositFrequency,
                ironDepositRarity, ironDepositMinAltitude, ironDepositMaxAltitude, DefaultMaterial.STONE));

        // Gold
        resources.add(Resource.createResource(config, OreGen.class, DefaultMaterial.GOLD_ORE, goldDepositSize, goldDepositFrequency,
                goldDepositRarity, goldDepositMinAltitude, goldDepositMaxAltitude, DefaultMaterial.STONE));

        // Redstone
        resources.add(Resource.createResource(config, OreGen.class, DefaultMaterial.REDSTONE_ORE, redstoneDepositSize,
                redstoneDepositFrequency, redstoneDepositRarity, redstoneDepositMinAltitude,
                redstoneDepositMaxAltitude, DefaultMaterial.STONE));

        // Diamond
        resources.add(Resource.createResource(config, OreGen.class, DefaultMaterial.DIAMOND_ORE, diamondDepositSize,
                diamondDepositFrequency, diamondDepositRarity, diamondDepositMinAltitude,
                diamondDepositMaxAltitude, DefaultMaterial.STONE));

        // Lapislazuli
        resources.add(Resource.createResource(config, OreGen.class, DefaultMaterial.LAPIS_ORE, lapislazuliDepositSize,
                lapislazuliDepositFrequency, lapislazuliDepositRarity, lapislazuliDepositMinAltitude,
                lapislazuliDepositMaxAltitude, DefaultMaterial.STONE));

        // Emerald ore
        if (defaultEmeraldOre > 0)
        {
            resources.add(Resource.createResource(config, OreGen.class, DefaultMaterial.EMERALD_ORE, emeraldDepositSize,
                    this.defaultEmeraldOre,
                    emeraldDepositRarity, emeraldDepositMinAltitude, emeraldDepositMaxAltitude, DefaultMaterial.STONE));
        }

        // Under water sand
        if (defaultWaterSand > 0)
        {
            resources.add(Resource.createResource(config, UnderWaterOreGen.class, DefaultMaterial.SAND, waterSandDepositSize,
                    defaultWaterSand,
                    waterSandDepositRarity, DefaultMaterial.DIRT, DefaultMaterial.GRASS));
        }

        // Under water clay
        resources.add(Resource.createResource(config, UnderWaterOreGen.class, DefaultMaterial.CLAY, waterClayDepositSize,
                waterClayDepositFrequency,
                waterClayDepositRarity, DefaultMaterial.DIRT, DefaultMaterial.CLAY));

        // Under water gravel
        if (defaultWaterGravel > 0)
        {
            resources.add(Resource.createResource(config, UnderWaterOreGen.class, DefaultMaterial.GRAVEL, waterGravelDepositSize,
                    defaultWaterGravel,
                    waterGravelDepositRarity, DefaultMaterial.DIRT, DefaultMaterial.GRASS));
        }

        // Custom objects
        resources.add(Resource.createResource(config, CustomObjectGen.class, "UseWorld"));

        // Boulder
        if (this.defaultBoulder != 0)
        {
            resources.add(Resource.createResource(config, BoulderGen.class, DefaultMaterial.MOSSY_COBBLESTONE, defaultBoulder,
                    boulderDepositRarity,
                    boulderDepositMinAltitude, boulderDepositMaxAltitude, DefaultMaterial.GRASS, DefaultMaterial.DIRT,
                    DefaultMaterial.STONE));
        }

        // Ice spikes
        if (this.defaultIceSpikes)
        {
            resources.add(Resource.createResource(config, IceSpikeGen.class, DefaultMaterial.PACKED_ICE, SpikeType.HugeSpike, 3, 1.66,
                    iceSpikeDepositMinHeight,
                    iceSpikeDepositMaxHeight, DefaultMaterial.ICE, DefaultMaterial.DIRT, DefaultMaterial.SNOW_BLOCK));
            resources.add(Resource.createResource(config, IceSpikeGen.class, DefaultMaterial.PACKED_ICE, SpikeType.SmallSpike, 3, 98.33,
                    iceSpikeDepositMinHeight,
                    iceSpikeDepositMaxHeight, DefaultMaterial.ICE, DefaultMaterial.DIRT, DefaultMaterial.SNOW_BLOCK));
            resources.add(Resource.createResource(config, IceSpikeGen.class, DefaultMaterial.PACKED_ICE, SpikeType.Basement, 2, 100,
                    iceSpikeDepositMinHeight,
                    iceSpikeDepositMaxHeight, DefaultMaterial.ICE, DefaultMaterial.DIRT, DefaultMaterial.SNOW_BLOCK));
        }

        // Melons (need to be spawned before trees)
        if (this.defaultMelons > 0)
        {
            resources.add(Resource.createResource(config, PlantGen.class, DefaultMaterial.MELON_BLOCK, this.defaultMelons,
                    flowerDepositRarity, flowerDepositMinAltitude, this.worldHeight, DefaultMaterial.GRASS, DefaultMaterial.DIRT));
        }

        // Melons (need to be spawned before trees)
        if (this.defaultSwampPatches > 0)
        {
            resources.add(Resource.createResource(config, SurfacePatchGen.class, DefaultMaterial.STATIONARY_WATER, DefaultMaterial.WATER_LILY,
                    62, 62, MaterialSet.SOLID_MATERIALS));
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
            resources.add(Resource.createResource(config, PlantGen.class, PlantType.Poppy, this.defaultPoppies, roseDepositRarity,
                    roseDepositMinAltitude, this.worldHeight, DefaultMaterial.GRASS, DefaultMaterial.DIRT));
        }

        if (this.defaultBlueOrchids > 0)
        {
            // Blue orchid
            resources.add(Resource.createResource(config, PlantGen.class, PlantType.BlueOrchid, this.defaultBlueOrchids,
                    blueOrchidDepositRarity, blueOrchidDepositMinAltitude, this.worldHeight, DefaultMaterial.GRASS, DefaultMaterial.DIRT));
        }

        if (this.defaultDandelions > 0)
        {
            // Dandelion
            resources.add(Resource.createResource(config, PlantGen.class, PlantType.Dandelion, this.defaultDandelions, flowerDepositRarity,
                    flowerDepositMinAltitude, this.worldHeight, DefaultMaterial.GRASS, DefaultMaterial.DIRT));
        }

        if (this.defaultTallFlowers > 0)
        {
            // Lilac
            resources.add(Resource.createResource(config, PlantGen.class, PlantType.Lilac, this.defaultTallFlowers, flowerDepositRarity,
                    flowerDepositMinAltitude, this.worldHeight, DefaultMaterial.GRASS, DefaultMaterial.DIRT));

            // Rose bush
            resources.add(Resource.createResource(config, PlantGen.class, PlantType.RoseBush, this.defaultTallFlowers, flowerDepositRarity,
                    flowerDepositMinAltitude, this.worldHeight, DefaultMaterial.GRASS, DefaultMaterial.DIRT));

            // Peony
            resources.add(Resource.createResource(config, PlantGen.class, PlantType.Peony, this.defaultTallFlowers, flowerDepositRarity,
                    flowerDepositMinAltitude, this.worldHeight, DefaultMaterial.GRASS, DefaultMaterial.DIRT));
        }

        if (this.defaultSunflowers > 0)
        {
            // Sunflower
            resources.add(Resource.createResource(config, PlantGen.class, PlantType.Sunflower, this.defaultSunflowers, flowerDepositRarity,
                    flowerDepositMinAltitude, this.worldHeight, DefaultMaterial.GRASS, DefaultMaterial.DIRT));
        }

        if (this.defaultTulips > 0)
        {
            // Tulip
            resources.add(Resource.createResource(config, PlantGen.class, PlantType.OrangeTulip, this.defaultTulips, tulipDepositRarity,
                    flowerDepositMinAltitude, this.worldHeight, DefaultMaterial.GRASS, DefaultMaterial.DIRT));
            resources.add(Resource.createResource(config, PlantGen.class, PlantType.RedTulip, this.defaultTulips, tulipDepositRarity,
                    flowerDepositMinAltitude,
                    this.worldHeight, DefaultMaterial.GRASS, DefaultMaterial.DIRT));
            resources.add(Resource.createResource(config, PlantGen.class, PlantType.WhiteTulip, this.defaultTulips, tulipDepositRarity,
                    flowerDepositMinAltitude, this.worldHeight, DefaultMaterial.GRASS, DefaultMaterial.DIRT));
            resources.add(Resource.createResource(config, PlantGen.class, PlantType.PinkTulip, this.defaultTulips, tulipDepositRarity,
                    flowerDepositMinAltitude, this.worldHeight, DefaultMaterial.GRASS, DefaultMaterial.DIRT));
        }

        if (this.defaultAzureBluets > 0)
        {
            // Azure bluet
            resources.add(Resource.createResource(config, PlantGen.class, PlantType.AzureBluet, this.defaultDandelions,
                    flowerDepositRarity, flowerDepositMinAltitude, this.worldHeight, DefaultMaterial.GRASS, DefaultMaterial.DIRT));

        }

        if (this.defaultAlliums > 0)
        {
            // Allium
            resources.add(Resource.createResource(config, PlantGen.class, PlantType.Allium, this.defaultDandelions, flowerDepositRarity,
                    flowerDepositMinAltitude, this.worldHeight, DefaultMaterial.GRASS, DefaultMaterial.DIRT));

        }

        if (this.defaultOxeyeDaisies > 0)
        {
            // Oxeye Daisy
            resources.add(Resource.createResource(config, PlantGen.class, PlantType.OxeyeDaisy, this.defaultDandelions,
                    flowerDepositRarity, flowerDepositMinAltitude, this.worldHeight, DefaultMaterial.GRASS, DefaultMaterial.DIRT));
        }

        if (this.defaultMushroom > 0)
        {
            // Red mushroom
            resources.add(Resource.createResource(config, PlantGen.class, PlantType.RedMushroom, this.defaultMushroom,
                    redMushroomDepositRarity, redMushroomDepositMinAltitude, this.worldHeight, defaultSurfaceBlock, DefaultMaterial.DIRT));

            // Brown mushroom
            resources.add(Resource.createResource(config, PlantGen.class, PlantType.BrownMushroom, this.defaultMushroom,
                    brownMushroomDepositRarity, brownMushroomDepositMinAltitude,
                    this.worldHeight, defaultSurfaceBlock, DefaultMaterial.DIRT));
        }

        if (this.defaultFerns > 0)
        {
            // Ferns
            resources.add(Resource.createResource(config, GrassGen.class, PlantType.Fern, GrassGen.GroupOption.NotGrouped,
                    this.defaultFerns, longGrassDepositRarity, DefaultMaterial.GRASS, DefaultMaterial.DIRT));
        }

        if (this.defaultDoubleGrass > 0)
        {
            // Double tall grass
            if (this.defaultDoubleGrassIsGrouped)
            {
                resources.add(Resource.createResource(config, GrassGen.class, PlantType.DoubleTallgrass, GrassGen.GroupOption.Grouped, this.defaultDoubleGrass,
                        doubleGrassGroupedDepositRarity, DefaultMaterial.GRASS, DefaultMaterial.DIRT));
            } else
            {
                resources.add(Resource.createResource(config, GrassGen.class, PlantType.DoubleTallgrass, GrassGen.GroupOption.NotGrouped, this.defaultDoubleGrass,
                        doubleGrassDepositRarity, DefaultMaterial.GRASS, DefaultMaterial.DIRT));
            }
        }

        if (this.defaultGrass > 0)
        {
            // Tall grass
            if (this.defaultGrassIsGrouped)
            {
                resources.add(Resource.createResource(config, GrassGen.class, PlantType.Tallgrass, GrassGen.GroupOption.Grouped,
                        this.defaultGrass, longGrassGroupedDepositRarity, DefaultMaterial.GRASS, DefaultMaterial.DIRT));
            } else
            {
                resources.add(Resource.createResource(config, GrassGen.class, PlantType.Tallgrass, GrassGen.GroupOption.NotGrouped,
                        this.defaultGrass, longGrassDepositRarity, DefaultMaterial.GRASS, DefaultMaterial.DIRT));
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
            resources.add(Resource.createResource(config, GrassGen.class, PlantType.DeadBush, 0, this.defaultDeadBush,
                    deadBushDepositRarity, DefaultMaterial.SAND, DefaultMaterial.HARD_CLAY,
                    DefaultMaterial.STAINED_CLAY, DefaultMaterial.DIRT));
        }

        // Pumpkin
        resources.add(Resource.createResource(config, PlantGen.class, DefaultMaterial.PUMPKIN, pumpkinDepositFrequency,
                pumpkinDepositRarity, pumpkinDepositMinAltitude, this.worldHeight, DefaultMaterial.GRASS));

        if (this.defaultReed > 0)
        {
            // Reed
            resources.add(Resource.createResource(config, ReedGen.class, DefaultMaterial.SUGAR_CANE_BLOCK, this.defaultReed,
                    reedDepositRarity, reedDepositMinAltitude, this.worldHeight,
                    DefaultMaterial.GRASS, DefaultMaterial.DIRT, DefaultMaterial.SAND));
        }

        if (this.defaultCactus > 0)
        {
            // Cactus
            resources.add(Resource.createResource(config, CactusGen.class, DefaultMaterial.CACTUS, this.defaultCactus, cactusDepositRarity,
                    cactusDepositMinAltitude, this.worldHeight, DefaultMaterial.SAND));
        }
        if (this.defaultHasVines)
        {
            resources.add(Resource.createResource(config, VinesGen.class, vinesFrequency, vinesRarity, vinesMinAltitude, this.worldHeight,
                    DefaultMaterial.VINE));
        }

        // Water source
        resources.add(Resource.createResource(config, LiquidGen.class, DefaultMaterial.WATER, waterSourceDepositFrequency,
                waterSourceDepositRarity, waterSourceDepositMinAltitude, this.worldHeight, DefaultMaterial.STONE));

        // Lava source
        resources.add(Resource.createResource(config, LiquidGen.class, DefaultMaterial.LAVA, lavaSourceDepositFrequency,
                lavaSourceDepositRarity, lavaSourceDepositMinAltitude, this.worldHeight, DefaultMaterial.STONE));

        // Desert wells
        if (this.defaultWell != null)
        {
            resources.add(Resource.createResource(config, WellGen.class, this.defaultWell));
        }

        // Sort resources according to their natural other
        // (Sorting the resources here is easier and less error prone than
        // keeping the order of this method in sync with the natural resource
        // order)
        Collections.sort(resources);
        return resources;
    }
}
