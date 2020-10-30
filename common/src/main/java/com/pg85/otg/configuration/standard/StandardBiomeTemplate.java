package com.pg85.otg.configuration.standard;

import static com.pg85.otg.configuration.standard.BiomeStandardValues.*;

import com.pg85.otg.common.materials.LocalMaterialData;
import com.pg85.otg.common.materials.LocalMaterials;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.configuration.biome.BiomeConfig.MineshaftType;
import com.pg85.otg.configuration.biome.BiomeConfig.RareBuildingType;
import com.pg85.otg.configuration.biome.BiomeConfig.VillageType;
import com.pg85.otg.configuration.biome.settings.WeightedMobSpawnGroup;
import com.pg85.otg.generator.resource.*;
import com.pg85.otg.generator.resource.IceSpikeGen.SpikeType;
import com.pg85.otg.generator.terrain.TerrainShapeBase;
import com.pg85.otg.util.materials.MaterialSet;
import com.pg85.otg.util.minecraft.defaults.DefaultBiome;
import com.pg85.otg.util.minecraft.defaults.EntityNames;

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
    public LocalMaterialData defaultSurfaceBlock = LocalMaterials.GRASS;
    public LocalMaterialData defaultGroundBlock = LocalMaterials.DIRT;
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
    public boolean defaultWoodlandMansions = false;
    public boolean defaultNetherFortressEnabled = false;
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
    public String defaultBiomeDictId = "";
    public String defaultInheritMobsBiomeName = "";
    public String defaultReplaceToBiomeName = "";
    public boolean inheritSaplingResource = false;
    
    public List<WeightedMobSpawnGroup> defaultCreatures = Arrays.asList(
            new WeightedMobSpawnGroup(EntityNames.SHEEP, 12, 4, 4),
            new WeightedMobSpawnGroup(EntityNames.PIG, 10, 4, 4),
            new WeightedMobSpawnGroup(EntityNames.CHICKEN, 10, 4, 4),
            new WeightedMobSpawnGroup(EntityNames.COW, 8, 4, 4));
    public List<WeightedMobSpawnGroup> defaultMonsters = Arrays.asList(
            new WeightedMobSpawnGroup(EntityNames.SPIDER, 100, 4, 4),
            new WeightedMobSpawnGroup(EntityNames.ZOMBIE, 100, 4, 4),
            new WeightedMobSpawnGroup(EntityNames.SKELETON, 100, 4, 4),
            new WeightedMobSpawnGroup(EntityNames.CREEPER, 100, 4, 4),
            new WeightedMobSpawnGroup(EntityNames.SLIME, 100, 4, 4),
            new WeightedMobSpawnGroup(EntityNames.ENDERMAN, 10, 1, 4),
            new WeightedMobSpawnGroup(EntityNames.WITCH, 5, 1, 1));
    public List<WeightedMobSpawnGroup> defaultAmbientCreatures = Collections.singletonList(
            new WeightedMobSpawnGroup(EntityNames.BAT, 10, 8, 8));
    public List<WeightedMobSpawnGroup> defaultWaterCreatures = Collections.singletonList(
            new WeightedMobSpawnGroup(EntityNames.SQUID, 10, 4, 4));

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
        	resources.add(Resource.createResource(config, SmallLakeGen.class, LocalMaterials.WATER, SmallLakeWaterFrequency,
                SmallLakeWaterRarity, SmallLakeMinAltitude, SmallLakeMaxAltitude));
        }

        // Small lava lakes
        resources.add(Resource.createResource(config, SmallLakeGen.class, LocalMaterials.LAVA, SmallLakeLavaFrequency,
            SmallLakeLavaRarity, SmallLakeMinAltitude, SmallLakeMaxAltitude));

        // Small underground lava lakes
        resources.add(Resource.createResource(config, SmallLakeGen.class, LocalMaterials.LAVA, SmallLakeLavaFrequency2,
            SmallLakeLavaRarity2, SmallLakeMinAltitude2, SmallLakeMaxAltitude2));
        
        // Underground lakes
        resources.add(Resource
            .createResource(config, UndergroundLakeGen.class, UndergroundLakeMinSize, UndergroundLakeMaxSize, UndergroundLakeFrequency,
                UndergroundLakeRarity, UndergroundLakeMinAltitude, UndergroundLakeMaxAltitude));

        // Dungeon
        resources.add(Resource.createResource(config, DungeonGen.class, DungeonFrequency, DungeonRarity, DungeonMinAltitude,
            this.worldHeight));
        
        // Fossil
        if (defaultFossilRarity > 0)
        {
            resources.add(Resource.createResource(config, FossilGen.class, defaultFossilRarity));
        }

        // Dirt
        resources.add(Resource.createResource(config, OreGen.class, LocalMaterials.DIRT, DirtDepositSize, DirtDepositFrequency,
            DirtDepositRarity, DirtDepositMinAltitude, DirtDepositMaxAltitude, LocalMaterials.STONE));

        // Gravel
        resources.add(Resource.createResource(config, OreGen.class, LocalMaterials.GRAVEL, GravelDepositSize, GravelDepositFrequency,
            GravelDepositRarity, GravelDepositMinAltitude, GravelDepositMaxAltitude, LocalMaterials.STONE));

        // Granite
        resources.add(Resource.createResource(config, OreGen.class, LocalMaterials.STONE + ":1", GraniteDepositSize,
            GraniteDepositFrequency, GraniteDepositRarity, GraniteDepositMinAltitude,
            GraniteDepositMaxAltitude, LocalMaterials.STONE));

        // Diorite
        resources.add(Resource.createResource(config, OreGen.class, LocalMaterials.STONE + ":3", DioriteDepositSize,
            DioriteDepositFrequency, DioriteDepositRarity, DioriteDepositMinAltitude,
            DioriteDepositMaxAltitude, LocalMaterials.STONE));

        // Andesite
        resources.add(Resource.createResource(config, OreGen.class, LocalMaterials.STONE + ":5", AndesiteDepositSize,
            AndesiteDepositFrequency, AndesiteDepositRarity, AndesiteDepositMinAltitude,
            AndesiteDepositMaxAltitude, LocalMaterials.STONE));

        // Coal
        resources.add(Resource.createResource(config, OreGen.class, LocalMaterials.COAL_ORE, CoalDepositSize, CoalDepositFrequency,
            CoalDepositRarity, CoalDepositMinAltitude, CoalDepositMaxAltitude, LocalMaterials.STONE));

        // Iron
        resources.add(Resource.createResource(config, OreGen.class, LocalMaterials.IRON_ORE, IronDepositSize, IronDepositFrequency,
            IronDepositRarity, IronDepositMinAltitude, IronDepositMaxAltitude, LocalMaterials.STONE));

        // Gold
        resources.add(Resource.createResource(config, OreGen.class, LocalMaterials.GOLD_ORE, GoldDepositSize, GoldDepositFrequency,
            GoldDepositRarity, GoldDepositMinAltitude, GoldDepositMaxAltitude, LocalMaterials.STONE));

        // Redstone
        resources.add(Resource.createResource(config, OreGen.class, LocalMaterials.REDSTONE_ORE, RedstoneDepositSize,
            RedstoneDepositFrequency, RedstoneDepositRarity, RedstoneDepositMinAltitude,
            RedstoneDepositMaxAltitude, LocalMaterials.STONE));

        // Diamond
        resources.add(Resource.createResource(config, OreGen.class, LocalMaterials.DIAMOND_ORE, DiamondDepositSize,
            DiamondDepositFrequency, DiamondDepositRarity, DiamondDepositMinAltitude,
            DiamondDepositMaxAltitude, LocalMaterials.STONE));

        // Lapislazuli
        resources.add(Resource.createResource(config, OreGen.class, LocalMaterials.LAPIS_ORE, LapislazuliDepositSize,
            LapislazuliDepositFrequency, LapislazuliDepositRarity, LapislazuliDepositMinAltitude,
            LapislazuliDepositMaxAltitude, LocalMaterials.STONE));

        // Emerald ore
        if (defaultEmeraldOre > 0)
        {
        	resources.add(Resource.createResource(config, OreGen.class, LocalMaterials.EMERALD_ORE, EmeraldDepositSize,
                this.defaultEmeraldOre,
                EmeraldDepositRarity, EmeraldDepositMinAltitude, EmeraldDepositMaxAltitude, LocalMaterials.STONE));
        }

        // Under water sand
        if (defaultWaterSand > 0)
        {
        	resources.add(Resource.createResource(config, UnderWaterOreGen.class, LocalMaterials.SAND, WaterSandDepositSize,
	            defaultWaterSand,
	            WaterSandDepositRarity, LocalMaterials.DIRT, LocalMaterials.GRASS));
        }

        // Under water clay
        resources.add(Resource.createResource(config, UnderWaterOreGen.class, LocalMaterials.CLAY, WaterClayDepositSize,
            WaterClayDepositFrequency,
            WaterClayDepositRarity, LocalMaterials.DIRT, LocalMaterials.CLAY));

        // Under water gravel
        if (defaultWaterGravel > 0)
        {
        	resources.add(Resource.createResource(config, BoulderGen.class, LocalMaterials.MOSSY_COBBLESTONE, defaultBoulder,
                defaultWaterGravel,
                BoulderDepositMinAltitude, BoulderDepositMaxAltitude, LocalMaterials.GRASS, LocalMaterials.DIRT,
                LocalMaterials.STONE));
        }

        // Custom objects
        resources.add(Resource.createResource(config, CustomObjectGen.class, "UseWorld"));

        // Boulder
        if (this.defaultBoulder != 0)
        {
        	 resources.add(Resource.createResource(config, BoulderGen.class, LocalMaterials.MOSSY_COBBLESTONE, defaultBoulder,
                BoulderDepositRarity,
                BoulderDepositMinAltitude, BoulderDepositMaxAltitude, LocalMaterials.GRASS, LocalMaterials.DIRT,
                LocalMaterials.STONE));
        }

        // Ice spikes
        if (this.defaultIceSpikes)
        {
        	resources.add(Resource.createResource(config, IceSpikeGen.class, LocalMaterials.PACKED_ICE, SpikeType.HugeSpike, 3, 1.66,
                IceSpikeDepositMinHeight,
                IceSpikeDepositMaxHeight, LocalMaterials.ICE, LocalMaterials.DIRT, LocalMaterials.SNOW_BLOCK));
        	resources.add(Resource.createResource(config, IceSpikeGen.class, LocalMaterials.PACKED_ICE, SpikeType.SmallSpike, 3, 98.33,
                IceSpikeDepositMinHeight,
                IceSpikeDepositMaxHeight, LocalMaterials.ICE, LocalMaterials.DIRT, LocalMaterials.SNOW_BLOCK));
        	resources.add(Resource.createResource(config, IceSpikeGen.class, LocalMaterials.PACKED_ICE, SpikeType.Basement, 2, 100,
                IceSpikeDepositMinHeight,
                IceSpikeDepositMaxHeight, LocalMaterials.ICE, LocalMaterials.DIRT, LocalMaterials.SNOW_BLOCK));
        }

        // Melons (need to be spawned before trees)
        if (this.defaultMelons > 0)
        {
            resources.add(Resource.createResource(config, PlantGen.class, LocalMaterials.MELON_BLOCK, this.defaultMelons,
                FlowerDepositRarity, FlowerDepositMinAltitude, this.worldHeight, LocalMaterials.GRASS, LocalMaterials.DIRT));
        }

        // Melons (need to be spawned before trees)
        if (this.defaultSwampPatches > 0)
        {
        	resources.add(Resource.createResource(config, SurfacePatchGen.class, LocalMaterials.STATIONARY_WATER, LocalMaterials.WATER_LILY,
                62, 62, MaterialSet.SOLID_MATERIALS));
        }

        // Trees
        if (this.defaultTree != null)
        {
            resources.add(Resource.createResource(config, TreeGen.class, this.defaultTree));
        }

        if (this.defaultWaterLily > 0)
        {
        	resources.add(Resource.createResource(config, AboveWaterGen.class, LocalMaterials.WATER_LILY, this.defaultWaterLily, 100));
        }

        if (this.defaultPoppies > 0)
        {
            // Poppy
            resources.add(Resource.createResource(config, PlantGen.class, PlantType.Poppy, this.defaultPoppies, RoseDepositRarity,
        		RoseDepositMinAltitude, this.worldHeight, LocalMaterials.GRASS, LocalMaterials.DIRT));
        }

        if (this.defaultBlueOrchids > 0)
        {
            // Blue orchid
            resources.add(Resource.createResource(config, PlantGen.class, PlantType.BlueOrchid, this.defaultBlueOrchids,
        		BlueOrchidDepositRarity, BlueOrchidDepositMinAltitude, this.worldHeight, LocalMaterials.GRASS, LocalMaterials.DIRT));
        }

        if (this.defaultDandelions > 0)
        {
            // Dandelion
            resources.add(Resource.createResource(config, PlantGen.class, PlantType.Dandelion, this.defaultDandelions, FlowerDepositRarity,
        		FlowerDepositMinAltitude, this.worldHeight, LocalMaterials.GRASS, LocalMaterials.DIRT));
        }

        if (this.defaultTallFlowers > 0)
        {
            // Lilac
            resources.add(Resource.createResource(config, PlantGen.class, PlantType.Lilac, this.defaultTallFlowers, FlowerDepositRarity,
        		FlowerDepositMinAltitude, this.worldHeight, LocalMaterials.GRASS, LocalMaterials.DIRT));

            // Rose bush
            resources.add(Resource.createResource(config, PlantGen.class, PlantType.RoseBush, this.defaultTallFlowers, FlowerDepositRarity,
        		FlowerDepositMinAltitude, this.worldHeight, LocalMaterials.GRASS, LocalMaterials.DIRT));

            // Peony
            resources.add(Resource.createResource(config, PlantGen.class, PlantType.Peony, this.defaultTallFlowers, FlowerDepositRarity,
        		FlowerDepositMinAltitude, this.worldHeight, LocalMaterials.GRASS, LocalMaterials.DIRT));
        }

        if (this.defaultSunflowers > 0)
        {
            // Sunflower
            resources.add(Resource.createResource(config, PlantGen.class, PlantType.Sunflower, this.defaultSunflowers, FlowerDepositRarity,
        		FlowerDepositMinAltitude, this.worldHeight, LocalMaterials.GRASS, LocalMaterials.DIRT));
        }

        if (this.defaultTulips > 0)
        {
            // Tulip
            resources.add(Resource.createResource(config, PlantGen.class, PlantType.OrangeTulip, this.defaultTulips, TulipDepositRarity,
        		FlowerDepositMinAltitude, this.worldHeight, LocalMaterials.GRASS, LocalMaterials.DIRT));
            resources.add(Resource.createResource(config, PlantGen.class, PlantType.RedTulip, this.defaultTulips, TulipDepositRarity,
                FlowerDepositMinAltitude,
                this.worldHeight, LocalMaterials.GRASS, LocalMaterials.DIRT));
            resources.add(Resource.createResource(config, PlantGen.class, PlantType.WhiteTulip, this.defaultTulips, TulipDepositRarity,
        		FlowerDepositMinAltitude, this.worldHeight, LocalMaterials.GRASS, LocalMaterials.DIRT));
            resources.add(Resource.createResource(config, PlantGen.class, PlantType.PinkTulip, this.defaultTulips, TulipDepositRarity,
        		FlowerDepositMinAltitude, this.worldHeight, LocalMaterials.GRASS, LocalMaterials.DIRT));
        }

        if (this.defaultAzureBluets > 0)
        {
            // Azure bluet
            resources.add(Resource.createResource(config, PlantGen.class, PlantType.AzureBluet, this.defaultDandelions,
        		FlowerDepositRarity, FlowerDepositMinAltitude, this.worldHeight, LocalMaterials.GRASS, LocalMaterials.DIRT));
        }

        if (this.defaultAlliums > 0)
        {
            // Allium
            resources.add(Resource.createResource(config, PlantGen.class, PlantType.Allium, this.defaultDandelions, FlowerDepositRarity,
        		FlowerDepositMinAltitude, this.worldHeight, LocalMaterials.GRASS, LocalMaterials.DIRT));

        }

        if (this.defaultOxeyeDaisies > 0)
        {
            // Oxeye Daisy
            resources.add(Resource.createResource(config, PlantGen.class, PlantType.OxeyeDaisy, this.defaultDandelions,
        		FlowerDepositRarity, FlowerDepositMinAltitude, this.worldHeight, LocalMaterials.GRASS, LocalMaterials.DIRT));
        }

        if (this.defaultMushroom > 0)
        {
            // Red mushroom
            resources.add(Resource.createResource(config, PlantGen.class, PlantType.RedMushroom, this.defaultMushroom,
        		RedMushroomDepositRarity, RedMushroomDepositMinAltitude, this.worldHeight, this.defaultSurfaceBlock, LocalMaterials.DIRT));

            // Brown mushroom
            resources.add(Resource.createResource(config, PlantGen.class, PlantType.BrownMushroom, this.defaultMushroom,
                BrownMushroomDepositRarity, BrownMushroomDepositMinAltitude,
                this.worldHeight, defaultSurfaceBlock, LocalMaterials.DIRT));
        }

        if (this.defaultFerns > 0)
        {
            // Ferns
            resources.add(Resource.createResource(config, GrassGen.class, PlantType.Fern, GrassGen.GroupOption.NotGrouped,
        		this.defaultFerns, LongGrassDepositRarity, LocalMaterials.GRASS, LocalMaterials.DIRT));
        }

        if (this.defaultDoubleGrass > 0)
        {
            // Double tall grass
            if (this.defaultDoubleGrassIsGrouped)
            {
                resources.add(Resource.createResource(config, GrassGen.class, PlantType.DoubleTallgrass, GrassGen.GroupOption.Grouped, this.defaultDoubleGrass,
            		DoubleGrassGroupedDepositRarity, LocalMaterials.GRASS, LocalMaterials.DIRT));
            } else {
                resources.add(Resource.createResource(config, GrassGen.class, PlantType.DoubleTallgrass, GrassGen.GroupOption.NotGrouped, this.defaultDoubleGrass,
            		DoubleGrassDepositRarity, LocalMaterials.GRASS, LocalMaterials.DIRT));
            }
        }

        if (this.defaultGrass > 0)
        {
            // Tall grass
            if (this.defaultGrassIsGrouped)
            {
                resources.add(Resource.createResource(config, GrassGen.class, PlantType.Tallgrass, GrassGen.GroupOption.Grouped,
            		this.defaultGrass, LongGrassGroupedDepositRarity, LocalMaterials.GRASS, LocalMaterials.DIRT));
            } else {
                resources.add(Resource.createResource(config, GrassGen.class, PlantType.Tallgrass, GrassGen.GroupOption.NotGrouped,
            		this.defaultGrass, LongGrassDepositRarity, LocalMaterials.GRASS, LocalMaterials.DIRT));
            }
        }

        if (this.defaultLargeFerns > 0)
        {
            // Large ferns
        	resources.add(Resource.createResource(config, PlantGen.class, PlantType.LargeFern, this.defaultLargeFerns, 90, 30, this.worldHeight, LocalMaterials.GRASS, LocalMaterials.DIRT));
        }

        if (this.defaultDeadBush > 0)
        {
            // Dead Bush
            resources.add(Resource.createResource(config, GrassGen.class, PlantType.DeadBush, 0, this.defaultDeadBush,
                DeadBushDepositRarity, LocalMaterials.SAND, LocalMaterials.TERRACOTTA,
                LocalMaterials.STAINED_CLAY, LocalMaterials.DIRT));
        }

        // Pumpkin
        resources.add(Resource.createResource(config, PlantGen.class, LocalMaterials.PUMPKIN, PumpkinDepositFrequency,
            PumpkinDepositRarity, PumpkinDepositMinAltitude, this.worldHeight, LocalMaterials.GRASS));

        if (this.defaultReed > 0)
        {
            // Reed
        	resources.add(Resource.createResource(config, ReedGen.class, LocalMaterials.SUGAR_CANE_BLOCK, this.defaultReed,
                ReedDepositRarity, ReedDepositMinAltitude, this.worldHeight,
                LocalMaterials.GRASS, LocalMaterials.DIRT, LocalMaterials.SAND));
        }

        if (this.defaultCactus > 0)
        {
            // Cactus
            resources.add(Resource.createResource(config, CactusGen.class, LocalMaterials.CACTUS, this.defaultCactus, CactusDepositRarity,
                CactusDepositMinAltitude, this.worldHeight, LocalMaterials.SAND));
        }
        if (this.defaultHasVines)
        {
            resources.add(Resource.createResource(config, VinesGen.class, VinesFrequency, VinesRarity, VinesMinAltitude, this.worldHeight,
        		LocalMaterials.VINE));
        }

        // Water source
        resources.add(Resource.createResource(config, LiquidGen.class, LocalMaterials.WATER, WaterSourceDepositFrequency,
            WaterSourceDepositRarity, WaterSourceDepositMinAltitude, this.worldHeight, LocalMaterials.STONE));

        // Lava source
        resources.add(Resource.createResource(config, LiquidGen.class, LocalMaterials.LAVA, LavaSourceDepositFrequency,
            LavaSourceDepositRarity, LavaSourceDepositMinAltitude, this.worldHeight, LocalMaterials.STONE));

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
