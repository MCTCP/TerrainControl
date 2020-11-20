package com.pg85.otg.config.minecraft;

import com.pg85.otg.config.standard.BiomeStandardValues;
import com.pg85.otg.config.standard.MojangSettings;
import com.pg85.otg.config.standard.StandardBiomeTemplate;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.util.biome.WeightedMobSpawnGroup;
import com.pg85.otg.util.biome.SettingsEnums.MineshaftType;
import com.pg85.otg.util.biome.SettingsEnums.RareBuildingType;
import com.pg85.otg.util.biome.SettingsEnums.VillageType;
import com.pg85.otg.util.materials.LocalMaterials;
import com.pg85.otg.util.minecraft.TreeType;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Class to hold all default settings of all default biomes.
 *
 * Because most vanilla biomes just have a few changes, it isn't needed to
 * give each their own top-level class, a simple inner class is enough.
 */
public class MinecraftBiomeTemplates
{
    public abstract static class MinecraftBiomeTemplate extends StandardBiomeTemplate
    {
        protected final MojangSettings mojangSettings;

        // For each biome's vanilla settings see net.minecraft.world.biome.Biome
        public MinecraftBiomeTemplate(MojangSettings mojangSettings, int worldHeight)
        {
            super(worldHeight);
            this.mojangSettings = mojangSettings;

            // Some settings are provided by MojangSettings,
            // which gets them from Minecraft
            this.defaultBiomeSurface = this.mojangSettings.getSurfaceHeight();
            this.defaultBiomeVolatility = this.mojangSettings.getSurfaceVolatility();
            this.defaultSurfaceBlock = this.mojangSettings.getSurfaceBlock();
            this.defaultGroundBlock = this.mojangSettings.getGroundBlock();
            this.defaultBiomeTemperature = this.mojangSettings.getTemperature();
            this.defaultBiomeWetness = this.mojangSettings.getWetness();

            // Don't load default mobs for vanilla biomes, they will be inherited
            this.defaultCreatures = new ArrayList<WeightedMobSpawnGroup>();
            this.defaultMonsters = new ArrayList<WeightedMobSpawnGroup>();
            this.defaultWaterCreatures = new ArrayList<WeightedMobSpawnGroup>();
            this.defaultAmbientCreatures = new ArrayList<WeightedMobSpawnGroup>();
        }

        protected void clearDefaultBorder()
        {
            this.defaultBorder.clear();
            this.defaultNotBorderNear.clear();
            this.defaultSizeWhenBorder = 8;
        }
    }

    public static class Ocean extends MinecraftBiomeTemplate
    {
        public Ocean(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);

            this.defaultColor = 0x000070;
            this.defaultStrongholds = false;
            this.defaultRiverBiome = "";
            this.defaultTree = new Object[] {1, TreeType.BigTree, 1, TreeType.Tree, 9};
            this.defaultOceanMonuments = true;

            this.defaultReplaceToBiomeName = "minecraft:ocean";
            this.defaultInheritMobsBiomeName = "minecraft:ocean";
            this.defaultBiomeDictId = "OCEAN";
        }
    }

    public static class Plains extends MinecraftBiomeTemplate
    {
        public Plains(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);

            this.defaultDandelions = 3;
            this.defaultPoppies = 1;
            this.defaultAzureBluets = 1;
            this.defaultOxeyeDaisies = 1;
            this.defaultTulips = 3;
            this.defaultGrass = 100;
            this.defaultColor = 0x8DB360;
            this.defaultStrongholds = false;
            this.defaultVillageType = VillageType.wood;
            this.defaultDoubleGrass = 10;
            this.defaultDoubleGrassIsGrouped = true;
            this.defaultReed = 5;

            this.defaultReplaceToBiomeName = "minecraft:plains";
            this.defaultInheritMobsBiomeName = "minecraft:plains";
            this.defaultBiomeDictId = "PLAINS";
        }
    }

    public static class Desert extends MinecraftBiomeTemplate
    {
        public Desert(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);

            this.defaultWaterLakes = false;
            this.defaultDeadBush = 4;
            this.defaultGrass = 0;
            this.defaultReed = 10;
            this.defaultCactus = 10;
            this.defaultColor = 0xFA9418;
            this.defaultWell = new Object[] { LocalMaterials.SANDSTONE, LocalMaterials.STONE_SLAB + ":1", LocalMaterials.WATER, 1, 0.1, 2,
                    this.worldHeight, LocalMaterials.SAND};
            this.defaultVillageType = VillageType.sandstone;
            this.defaultRareBuildingType = RareBuildingType.desertPyramid;
            this.defaultBorder.add(DefaultBiome.MESA.Name);
            this.defaultNotBorderNear.add(DefaultBiome.OCEAN.Name);
            this.defaultNotBorderNear.add(DefaultBiome.MESA_PLATEAU.Name);
            this.defaultNotBorderNear.add(DefaultBiome.MESA_PLATEAU_FOREST.Name);
            this.defaultNotBorderNear.add(DefaultBiome.MESA_PLATEAU_MOUNTAINS.Name);
            this.defaultNotBorderNear.add(DefaultBiome.MESA_PLATEAU_FOREST_MOUNTAINS.Name);
            this.defaultNotBorderNear.add(DefaultBiome.MESA_BRYCE.Name);
            this.defaultFossilRarity = 1.156; // 1/64 chance of spawning

            this.defaultReplaceToBiomeName = "minecraft:desert";
            this.defaultInheritMobsBiomeName = "minecraft:desert";
            this.defaultBiomeDictId = "HOT, DRY, SANDY";
        }
    }

    public static class ExtremeHills extends MinecraftBiomeTemplate
    {
        public ExtremeHills(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);

            this.defaultColor = 0x606060;
            this.defaultDandelions = 4;
            this.defaultEmeraldOre = BiomeStandardValues.EmeraldDepositFrequency;
            this.defaultTree = new Object[] {1, TreeType.Taiga2, 10, TreeType.BigTree, 1, TreeType.Tree, 9};
            this.defaultSurfaceSurfaceAndGroundControl = new Object[] { LocalMaterials.GRASS, LocalMaterials.DIRT, 1.0,
            		LocalMaterials.STONE, LocalMaterials.STONE, 10.0};

            this.defaultReplaceToBiomeName = "minecraft:extreme_hills";
            this.defaultInheritMobsBiomeName = "minecraft:extreme_hills";
            this.defaultBiomeDictId = "MOUNTAIN, HILLS";
        }
    }

    public static class Forest extends MinecraftBiomeTemplate
    {
        public Forest(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);

            this.defaultRarityWhenIsle = 96;
            this.defaultIsle.add(DefaultBiome.PLAINS.Name);
            this.defaultGrass = 30;
            this.defaultColor = 0x056621;
            this.defaultTree = new Object[] {10, TreeType.Birch, 20, TreeType.Tree, 100};
            this.defaultTallFlowers = 2;
            this.defaultPoppies = 4;
            this.defaultReed = 3;
            this.defaultMushroom = 1;

            this.defaultReplaceToBiomeName = "minecraft:forest";
            this.defaultInheritMobsBiomeName = "minecraft:forest";
            this.defaultBiomeDictId = "FOREST";
        }
    }

    public static class Taiga extends MinecraftBiomeTemplate
    {
        public Taiga(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);

            this.defaultGrass = 10;
            this.defaultColor = 0x0B6659;
            this.defaultTree = new Object[] {10, TreeType.Taiga1, 35, TreeType.Taiga2, 100};

            // Place taiga on the border of Mega Taiga
            this.defaultBorder.add(DefaultBiome.MEGA_TAIGA.Name);
            this.defaultNotBorderNear.add(DefaultBiome.MEGA_SPRUCE_TAIGA.Name);
            this.defaultNotBorderNear.add(DefaultBiome.MEGA_TAIGA_HILLS.Name);
            this.defaultNotBorderNear.add(DefaultBiome.MEGA_SPRUCE_TAIGA_HILLS.Name);
            this.defaultSizeWhenBorder = 6;

            this.defaultVillageType = VillageType.taiga;

            this.defaultReplaceToBiomeName = "minecraft:taiga";
            this.defaultInheritMobsBiomeName = "minecraft:taiga";
            this.defaultBiomeDictId = "COLD, CONIFEROUS, FOREST";
        }
    }

    public static class Swampland extends MinecraftBiomeTemplate
    {
        public Swampland(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);

            this.defaultDandelions = 0;
            this.defaultMushroom = 8;
            this.defaultReed = 10;
            this.defaultWaterSand = 0;
            this.defaultWaterGravel = 0;
            this.defaultWaterLily = 4;
            this.defaultSwampPatches = 1;
            this.defaultDandelions = 0;
            this.defaultBlueOrchids = 2;

            this.defaultColor = 0x07F9B2;
            this.defaultWaterColorMultiplier = 0xe0ffae;
            this.defaultGrassColor = 0x7E6E7E;
            this.defaultFoliageColor = 0x7E6E7E;

            this.defaultGrass = 30;
            this.defaultRareBuildingType = RareBuildingType.swampHut;
            this.defaultTree = new Object[] {2, TreeType.SwampTree, 100};
            this.defaultStrongholds = false;
            this.defaultFossilRarity = 1.156; // 1/64 chance of spawning

            this.defaultReplaceToBiomeName = "minecraft:swampland";
            this.defaultInheritMobsBiomeName = "minecraft:swampland";
            this.defaultBiomeDictId = "WET, SWAMP";
        }
    }

    public static class River extends MinecraftBiomeTemplate
    {
        public River(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);

            this.defaultSize = 8;
            this.defaultRarity = 95;
            this.defaultColor = 0x0000FF;
            this.defaultStrongholds = false;
            this.defaultTree = new Object[] {1, TreeType.BigTree, 1, TreeType.Tree, 9};
            this.defaultOceanMonuments = true;

            this.defaultReplaceToBiomeName = "minecraft:river";
            this.defaultInheritMobsBiomeName = "minecraft:river";
            this.defaultBiomeDictId = "RIVER";
        }
    }

    public static class Hell extends MinecraftBiomeTemplate
    {
        public Hell(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);
            this.defaultColor = 0xFF0000;

            this.defaultReplaceToBiomeName = "minecraft:hell";
            this.defaultInheritMobsBiomeName = "minecraft:hell";
            this.defaultBiomeDictId = "HOT, DRY, NETHER";
            this.defaultNetherFortressEnabled = true;
        }
    }

    public static class Sky extends MinecraftBiomeTemplate
    {
        public Sky(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);
            this.defaultColor = 0x8080FF;

            this.defaultReplaceToBiomeName = "minecraft:sky";
            this.defaultInheritMobsBiomeName = "minecraft:sky";
            this.defaultBiomeDictId = "COLD, DRY, END";
        }
    }

    public static class FrozenOcean extends Ocean
    {
        public FrozenOcean(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);

            this.defaultColor = 0x9090A0;

            this.defaultReplaceToBiomeName = "minecraft:frozen_ocean";
            this.defaultInheritMobsBiomeName = "minecraft:frozen_ocean";
            this.defaultBiomeDictId = "COLD, OCEAN, SNOWY";
        }
    }

    public static class FrozenRiver extends MinecraftBiomeTemplate
    {
        public FrozenRiver(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);

            this.defaultColor = 0xA0A0FF;
            this.defaultStrongholds = false;
            this.defaultOceanMonuments = true;

            this.defaultReplaceToBiomeName = "minecraft:frozen_river";
            this.defaultInheritMobsBiomeName = "minecraft:frozen_river";
            this.defaultBiomeDictId = "COLD, RIVER, SNOWY";
        }
    }

    public static class IcePlains extends MinecraftBiomeTemplate
    {
        public IcePlains(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);

            this.defaultColor = 0xFFFFFF;
            this.defaultRiverBiome = DefaultBiome.FROZEN_RIVER.Name;
            this.defaultPoppies = 3;
            this.defaultGrass = 20;
            this.defaultGrassIsGrouped = true;
            this.defaultTree = new Object[] {1, TreeType.Taiga2, 15};
            this.defaultRareBuildingType = RareBuildingType.igloo;

            this.defaultReplaceToBiomeName = "minecraft:ice_flats";
            this.defaultInheritMobsBiomeName = "minecraft:ice_flats";
            this.defaultBiomeDictId = "COLD, SNOWY, WASTELAND";
        }
    }

    public static class IceMountains extends MinecraftBiomeTemplate
    {
        public IceMountains(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);

            this.defaultColor = 0xA0A0A0;
            this.defaultSizeWhenIsle = 6;
            this.defaultRarityWhenIsle = 97;
            this.defaultIsle.add(DefaultBiome.ICE_PLAINS.Name);
            this.defaultRiverBiome = DefaultBiome.FROZEN_RIVER.Name;

            this.defaultReplaceToBiomeName = "minecraft:ice_mountains";
            this.defaultInheritMobsBiomeName = "minecraft:ice_mountains";
            this.defaultBiomeDictId = "COLD, SNOWY, MOUNTAIN";
        }
    }

    public static class MushroomIsland extends MinecraftBiomeTemplate
    {
        public MushroomIsland(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);

            this.defaultSurfaceBlock = LocalMaterials.MYCELIUM;
            this.defaultMushroom = 2;
            this.defaultGrass = 0;
            this.defaultDandelions = 0;
            this.defaultRarityWhenIsle = 1;
            this.defaultRiverBiome = "";
            this.defaultSizeWhenIsle = 6;
            this.defaultIsle.add(DefaultBiome.OCEAN.Name);
            this.defaultIsle.add(DefaultBiome.DEEP_OCEAN.Name);
            this.defaultColor = 0xFF00FF;
            this.defaultWaterLily = 1;
            this.defaultStrongholds = false;
            this.defaultTree = new Object[] {1, TreeType.HugeMushroom, 100};

            this.defaultReplaceToBiomeName = "minecraft:mushroom_island";
            this.defaultInheritMobsBiomeName = "minecraft:mushroom_island";
            this.defaultBiomeDictId = "MUSHROOM";
        }
    }

    public static class MushroomIslandShore extends MushroomIsland
    {
        public MushroomIslandShore(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);

            this.defaultSizeWhenBorder = 9;
            this.defaultBorder.add(DefaultBiome.MUSHROOM_ISLAND.Name);
            this.defaultColor = 0xA000FF;
            this.defaultTree = null; // No mushrooms on the shore

            this.defaultReplaceToBiomeName = "minecraft:mushroom_island_shore";
            this.defaultInheritMobsBiomeName = "minecraft:mushroom_island_shore";
            this.defaultBiomeDictId = "MUSHROOM, BEACH";
        }
    }

    public static class Beach extends MinecraftBiomeTemplate
    {
        public Beach(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);

            this.defaultSizeWhenBorder = 8;
            this.defaultBorder.add(DefaultBiome.OCEAN.Name);
            this.defaultNotBorderNear.add(DefaultBiome.RIVER.Name);
            this.defaultNotBorderNear.add(DefaultBiome.SWAMPLAND.Name);
            this.defaultNotBorderNear.add(DefaultBiome.EXTREME_HILLS.Name);
            this.defaultNotBorderNear.add(DefaultBiome.ICE_PLAINS_SPIKES.Name);
            this.defaultNotBorderNear.add(DefaultBiome.ICE_PLAINS.Name);
            this.defaultNotBorderNear.add(DefaultBiome.COLD_TAIGA.Name);
            this.defaultNotBorderNear.add(DefaultBiome.COLD_TAIGA_HILLS.Name);
            this.defaultNotBorderNear.add(DefaultBiome.COLD_TAIGA_MOUNTAINS.Name);
            this.defaultNotBorderNear.add(DefaultBiome.ICE_MOUNTAINS.Name);
            this.defaultNotBorderNear.add(DefaultBiome.MUSHROOM_ISLAND.Name);
            this.defaultNotBorderNear.add(DefaultBiome.DEEP_OCEAN.Name);
            this.defaultNotBorderNear.add(DefaultBiome.COLD_BEACH.Name);
            this.defaultNotBorderNear.add(DefaultBiome.STONE_BEACH.Name);
            this.defaultNotBorderNear.add(DefaultBiome.MESA.Name);
            this.defaultColor = 0xFADE55;
            this.defaultStrongholds = false;

            this.defaultReplaceToBiomeName = "minecraft:beaches";
            this.defaultInheritMobsBiomeName = "minecraft:beaches";
            this.defaultBiomeDictId = "BEACH";
        }
    }

    public static class DesertHills extends Desert
    {
        public DesertHills(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);

            this.defaultWaterLakes = false;
            this.defaultSizeWhenIsle = 6;
            this.defaultRarityWhenIsle = 97;
            this.defaultIsle.add(DefaultBiome.DESERT.Name);
            this.defaultDeadBush = 4;
            this.defaultGrass = 0;
            this.defaultReed = 50;
            this.defaultCactus = 10;
            this.defaultColor = 0xD25F12;
            this.defaultWell = new Object[] { LocalMaterials.SANDSTONE, LocalMaterials.STONE_SLAB + ":1", LocalMaterials.WATER, 1, 0.1, 2,
                    this.worldHeight, LocalMaterials.SAND};
            this.defaultVillageType = VillageType.sandstone;
            this.defaultRareBuildingType = RareBuildingType.desertPyramid;

            // Don't inherit border properties of the Desert biome
            this.clearDefaultBorder();

            this.defaultReplaceToBiomeName = "minecraft:desert_hills";
            this.defaultInheritMobsBiomeName = "minecraft:desert_hills";
            this.defaultBiomeDictId = "HOT, DRY, SANDY, HILLS";
        }
    }

    public static class ForestHills extends Forest
    {
        public ForestHills(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);

            this.defaultSizeWhenIsle = 6;
            this.defaultRarityWhenIsle = 97;
            this.defaultIsle.add(DefaultBiome.FOREST.Name);
            this.defaultGrass = 15;
            this.defaultColor = 0x22551C;

            this.defaultReplaceToBiomeName = "minecraft:forest_hills";
            this.defaultInheritMobsBiomeName = "minecraft:forest_hills";
            this.defaultBiomeDictId = "FOREST, HILLS";
        }
    }

    public static class TaigaHills extends Taiga
    {
        public TaigaHills(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);
            this.clearDefaultBorder();

            this.defaultSizeWhenIsle = 6;
            this.defaultRarityWhenIsle = 97;
            this.defaultIsle.add(DefaultBiome.TAIGA.Name);
            this.defaultGrass = 10;
            this.defaultColor = 0x163933;
            this.defaultRiverBiome = DefaultBiome.FROZEN_RIVER.Name;

            this.defaultReplaceToBiomeName = "minecraft:taiga_hills";
            this.defaultInheritMobsBiomeName = "minecraft:taiga_hills";
            this.defaultBiomeDictId = "COLD, CONIFEROUS, FOREST, HILLS";
        }
    }

    public static class ExtremeHillsEdge extends ExtremeHills
    {
        public ExtremeHillsEdge(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);

            this.defaultSizeWhenBorder = 8;
            this.defaultBorder.add(DefaultBiome.EXTREME_HILLS.Name);
            this.defaultNotBorderNear.add(DefaultBiome.EXTREME_HILLS_PLUS.Name);
            this.defaultColor = 0x72789A;
            this.defaultSurfaceSurfaceAndGroundControl = new Object[0];

            this.defaultReplaceToBiomeName = "minecraft:smaller_extreme_hills";
            this.defaultInheritMobsBiomeName = "minecraft:smaller_extreme_hills";
            this.defaultBiomeDictId = "MOUNTAIN";
        }
    }

    public static class Jungle extends MinecraftBiomeTemplate
    {
        public Jungle(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);

            this.defaultGrass = 60;
            this.defaultFerns = 20;
            this.defaultPoppies = 4;
            this.defaultDandelions = 4;
            this.defaultColor = 0x537B09;
            this.defaultRareBuildingType = RareBuildingType.jungleTemple;
            this.defaultTree = new Object[] {50, TreeType.BigTree, 10, TreeType.GroundBush, 50, TreeType.JungleTree, 35,
                    TreeType.CocoaTree, 100};
            this.defaultMelons = 1;

            this.defaultReplaceToBiomeName = "minecraft:jungle";
            this.defaultInheritMobsBiomeName = "minecraft:jungle";
            this.defaultBiomeDictId = "HOT, WET, DENSE, JUNGLE";
        }
    }

    public static class JungleHills extends Jungle
    {
        public JungleHills(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);

            this.defaultColor = 0x2C4205;
            this.defaultIsle.add(DefaultBiome.JUNGLE.Name);

            this.defaultReplaceToBiomeName = "minecraft:jungle_hills";
            this.defaultInheritMobsBiomeName = "minecraft:jungle_hills";
            this.defaultBiomeDictId = "HOT, WET, DENSE, JUNGLE, HILLS";
        }
    }

    public static class JungleEdge extends Jungle
    {
        public JungleEdge(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);
            this.defaultColor = 0x628B17;
            this.defaultSizeWhenBorder = 8;
            this.defaultBorder.add(DefaultBiome.JUNGLE.Name);
            
            this.defaultReplaceToBiomeName = "minecraft:jungle_edge";
            this.defaultInheritMobsBiomeName = "minecraft:jungle_edge";
            this.defaultBiomeDictId = "HOT, WET, JUNGLE, FOREST";
        }
    }

    public static class DeepOcean extends Ocean
    {
        public DeepOcean(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);
            this.defaultColor = 0x000030;
            this.defaultIsle.add(DefaultBiome.OCEAN.Name);
            this.defaultSizeWhenIsle = 4;
            this.defaultRarityWhenIsle = 100;

            this.defaultReplaceToBiomeName = "minecraft:deep_ocean";
            this.defaultInheritMobsBiomeName = "minecraft:deep_ocean";
            this.defaultBiomeDictId = "OCEAN";
        }
    }

    public static class StoneBeach extends MinecraftBiomeTemplate
    {
        public StoneBeach(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);
            this.defaultColor = 0xA2A284;

            this.defaultReplaceToBiomeName = "minecraft:stone_beach";
            this.defaultInheritMobsBiomeName = "minecraft:stone_beach";
            this.defaultBiomeDictId = "BEACH";
        }
    }

    public static class ColdBeach extends MinecraftBiomeTemplate
    {
        public ColdBeach(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);
            this.defaultColor = 0xFAF0C0;
            this.defaultStrongholds = false;

            this.defaultReplaceToBiomeName = "minecraft:cold_beach";
            this.defaultInheritMobsBiomeName = "minecraft:cold_beach";
            this.defaultBiomeDictId = "COLD, BEACH, SNOWY";
        }
    }

    public static class BirchForest extends Forest
    {
        public BirchForest(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);
            this.defaultColor = 0x307444;
            this.defaultTree = new Object[] {10, TreeType.Birch, 80};
            // Forest spawns as an isle in Plains, BirchForest shouldn't
            this.defaultIsle.clear();

            this.defaultReplaceToBiomeName = "minecraft:birch_forest";
            this.defaultInheritMobsBiomeName = "minecraft:birch_forest";
            this.defaultBiomeDictId = "FOREST";
        }
    }

    public static class BirchForestHills extends BirchForest
    {
        public BirchForestHills(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);
            this.defaultColor = 0x1F5F32;
            this.defaultIsle.add(DefaultBiome.BIRCH_FOREST.Name);
            this.defaultRarityWhenIsle = 97;

            this.defaultReplaceToBiomeName = "minecraft:birch_forest_hills";
            this.defaultInheritMobsBiomeName = "minecraft:birch_forest_hills";
            this.defaultBiomeDictId = "FOREST, HILLS";
        }
    }

    public static class RoofedForest extends MinecraftBiomeTemplate
    {
        public RoofedForest(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);
            this.defaultColor = 0x40511A;
            this.defaultGrass = 15;
            this.defaultTree = new Object[] {20, TreeType.HugeMushroom, 3, TreeType.DarkOak, 66, TreeType.Birch, 20, TreeType.Tree, 100};
            this.defaultTallFlowers = 1;
            this.defaultPoppies = 4;

            this.defaultWoodlandMansions = true;

            this.defaultReplaceToBiomeName = "minecraft:roofed_forest";
            this.defaultInheritMobsBiomeName = "minecraft:roofed_forest";
            this.defaultBiomeDictId = "SPOOKY, DENSE, FOREST";
        }
    }

    public static class ColdTaiga extends Taiga
    {
        public ColdTaiga(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);
            this.clearDefaultBorder();

            this.defaultColor = 0x31554A;
            this.defaultRarity = 35;
            this.defaultRareBuildingType = RareBuildingType.igloo;

            this.defaultReplaceToBiomeName = "minecraft:taiga_cold";
            this.defaultInheritMobsBiomeName = "minecraft:taiga_cold";
            this.defaultBiomeDictId = "COLD, CONIFEROUS, FOREST, SNOWY";
        }
    }

    public static class ColdTaigaHills extends ColdTaiga
    {
        public ColdTaigaHills(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);

            this.defaultColor = 0x243F36;
            this.defaultSizeWhenIsle = 6;
            this.defaultRarityWhenIsle = 97;
            this.defaultIsle.add(DefaultBiome.COLD_TAIGA.Name);
            this.defaultRareBuildingType = RareBuildingType.disabled;

            this.defaultReplaceToBiomeName = "minecraft:taiga_cold_hills";
            this.defaultInheritMobsBiomeName = "minecraft:taiga_cold_hills";
            this.defaultBiomeDictId = "COLD, CONIFEROUS, FOREST, SNOWY, HILLS";
        }
    }

    public static class MegaTaiga extends MinecraftBiomeTemplate
    {
        public MegaTaiga(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);
            this.defaultColor = 0x596651;
            this.defaultTree = new Object[] {10, TreeType.HugeTaiga1, 33, TreeType.Taiga1, 33, TreeType.Taiga2, 100};
            this.defaultBoulder = 2;
            this.defaultGrass = 16;
            this.defaultFerns = 80;
            this.defaultMushroom = 8;
            this.defaultLargeFerns = 60;
            this.defaultSurfaceSurfaceAndGroundControl = new Object[] { LocalMaterials.DIRT + ":2", LocalMaterials.DIRT, -0.95,
            		LocalMaterials.DIRT + ":1", LocalMaterials.DIRT, 1.75};

            this.defaultVillageType = VillageType.taiga;

            this.defaultReplaceToBiomeName = "minecraft:redwood_taiga";
            this.defaultInheritMobsBiomeName = "minecraft:redwood_taiga";
            this.defaultBiomeDictId = "COLD, CONIFEROUS, FOREST";
        }
    }

    public static class MegaTaigaHills extends MegaTaiga
    {
        public MegaTaigaHills(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);
            this.defaultColor = 0x454F3E;
            this.defaultSize = 6;
            this.defaultRarityWhenIsle = 97;
            this.defaultIsle.add(DefaultBiome.MEGA_TAIGA.Name);

            this.defaultReplaceToBiomeName = "minecraft:redwood_taiga_hills";
            this.defaultInheritMobsBiomeName = "minecraft:redwood_taiga_hills";
            this.defaultBiomeDictId = "COLD, CONIFEROUS, FOREST, HILLS";
        }
    }

    // Actually called EXTREME_HILLS_WITH_TREES in this version of MC
    public static class ExtremeHillsPlus extends ExtremeHills
    {
        public ExtremeHillsPlus(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);
            this.defaultColor = 0x507050;
            this.defaultSurfaceSurfaceAndGroundControl = new Object[0];
            this.defaultTree = new Object[] {1, TreeType.Taiga2, 66, TreeType.BigTree, 10, TreeType.Tree, 100};
            this.defaultIsle.add(DefaultBiome.EXTREME_HILLS.Name);
            this.defaultRarityWhenIsle = 97;

            this.defaultReplaceToBiomeName = "minecraft:extreme_hills_with_trees";
            this.defaultInheritMobsBiomeName = "minecraft:extreme_hills_with_trees";
            this.defaultBiomeDictId = "MOUNTAIN, FOREST, SPARSE";
        }
    }

    public static class Savanna extends MinecraftBiomeTemplate
    {
        public Savanna(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);
            this.defaultColor = 0xBDB25F;
            this.defaultVillageType = VillageType.wood;
            this.defaultGrass = 200;
            this.defaultDoubleGrass = 4;
            this.defaultDandelions = 4;
            this.defaultTree = new Object[] {1, TreeType.Acacia, 80, TreeType.Tree, 100};

            this.defaultVillageType = VillageType.savanna;

            this.defaultReplaceToBiomeName = "minecraft:savanna";
            this.defaultInheritMobsBiomeName = "minecraft:savanna";
            this.defaultBiomeDictId = "HOT, SAVANNA, PLAINS, SPARSE";
        }
    }

    public static class SavannaPlateau extends Savanna
    {
        public SavannaPlateau(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);
            this.defaultColor = 0xA79D64;
            this.defaultSizeWhenIsle = 6;
            this.defaultRarityWhenIsle = 97;
            this.defaultIsle.add(DefaultBiome.SAVANNA.Name);

            this.defaultReplaceToBiomeName = "minecraft:savanna_rock";
            this.defaultInheritMobsBiomeName = "minecraft:savanna_rock";
            this.defaultBiomeDictId = "HOT, SAVANNA, PLAINS, SPARSE";
        }
    }

    public static class Mesa extends MinecraftBiomeTemplate
    {
        public Mesa(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);
            this.defaultColor = 0xD94515;
            this.defaultSurfaceSurfaceAndGroundControl = new Object[] {Constants.MESA_NAME_NORMAL};
            this.defaultDandelions = 0;
            this.defaultDeadBush = 7;
            this.defaultReed = 5;
            this.defaultCactus = 10;
            this.defaultGrass = 0;
            this.defaultMineshaftType = MineshaftType.mesa;

            this.defaultReplaceToBiomeName = "minecraft:mesa";
            this.defaultInheritMobsBiomeName = "minecraft:mesa";
            this.defaultBiomeDictId = "MESA, SANDY";
        }
    }

    // Actually called MESA_ROCK in this version of MC
    public static class MesaPlateauForest extends MesaPlateau
    {
        public MesaPlateauForest(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);
            this.defaultColor = 0xB09765;
            this.defaultSurfaceSurfaceAndGroundControl = new Object[] {Constants.MESA_NAME_FOREST};
            this.defaultTree = new Object[] {1, TreeType.Tree, 100};
            this.defaultGrass = 10;

            // Minecraft has chosen sand and stained clay as the surface and
            // ground blocks. It then places hardcoded grass and dirt.
            // Open Terrain Generator does it the other way round: it places
            // hardcoded sand and stained clay and lets the user change the
            // grass and dirt blocks.
            this.defaultSurfaceBlock = LocalMaterials.GRASS;
            this.defaultGroundBlock = LocalMaterials.DIRT;

            this.defaultReplaceToBiomeName = "minecraft:mesa_rock";
            this.defaultInheritMobsBiomeName = "minecraft:mesa_rock";
            this.defaultBiomeDictId = "MESA, SPARSE, SANDY";
        }
    }

    // Actually called MESA_CLEAR_ROCK in this version of MC
    public static class MesaPlateau extends Mesa
    {
        public MesaPlateau(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);
            this.defaultColor = 0xCA8C65;
            this.defaultIsle.add(DefaultBiome.MESA.Name);
            this.defaultRarityWhenIsle = 99;

            this.defaultReplaceToBiomeName = "minecraft:mesa_clear_rock";
            this.defaultInheritMobsBiomeName = "minecraft:mesa_clear_rock";
            this.defaultBiomeDictId = "MESA, SANDY";
        }
    }

    public static class TheVoid extends MinecraftBiomeTemplate
    {
        public TheVoid(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);
            this.defaultColor = 0xB6D0FF;
            this.defaultDisableBiomeHeight = true;
            this.defaultStrongholds = false;
            Arrays.fill(this.defaultCustomHeightControl, -100);

            this.defaultReplaceToBiomeName = "minecraft:void";
            this.defaultInheritMobsBiomeName = "minecraft:void";
            this.defaultBiomeDictId = ""; // TODO: Should this be END?
        }
    }

    public static class SunflowerPlains extends Plains
    {
        public SunflowerPlains(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);
            this.defaultColor = 0xDEFF00;
            this.defaultSunflowers = 30;
            this.defaultRarity = 10;

            this.defaultReplaceToBiomeName = "minecraft:mutated_plains";
            this.defaultInheritMobsBiomeName = "minecraft:mutated_plains";
        }
    }

    public static class DesertMountains extends Desert
    {
        public DesertMountains(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);
            this.defaultColor = 0xE58208;
            this.defaultWaterLakes = true;
            this.defaultRarity = 10;

            // Don't inherit border properties of the Desert biome
            this.clearDefaultBorder();

            this.defaultReplaceToBiomeName = "minecraft:mutated_desert";
            this.defaultInheritMobsBiomeName = "minecraft:mutated_desert";
        }
    }

    public static class ExtremeHillsMountains extends ExtremeHills
    {
        public ExtremeHillsMountains(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);
            this.defaultColor = 0x525252;
            this.defaultSurfaceSurfaceAndGroundControl = new Object[] { LocalMaterials.GRAVEL, LocalMaterials.GRAVEL, -1.0,
            		LocalMaterials.GRASS, LocalMaterials.DIRT, 2.0, LocalMaterials.GRAVEL, LocalMaterials.GRAVEL, 10.0};
            this.defaultRarity = 10;

            this.defaultReplaceToBiomeName = "minecraft:mutated_extreme_hills";
            this.defaultInheritMobsBiomeName = "minecraft:mutated_extreme_hills";
        }
    }

    public static class FlowerForest extends Forest
    {
        public FlowerForest(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);
            int flowerMultiplier = 20;

            this.defaultColor = 0x2D8E49;
            this.defaultRarity = 10;

            this.defaultDandelions = 0;
            this.defaultTallFlowers = 6;
            this.defaultPoppies = flowerMultiplier * 2;
            this.defaultAlliums = flowerMultiplier;
            this.defaultAzureBluets = flowerMultiplier;
            // Four different tulip colors, so each tulip now
            // generates as much as the other flowers
            this.defaultTulips = flowerMultiplier * 4;
            this.defaultOxeyeDaisies = flowerMultiplier;

            this.defaultReplaceToBiomeName = "minecraft:mutated_forest";
            this.defaultInheritMobsBiomeName = "minecraft:mutated_forest";
        }
    }

    public static class TaigaMountains extends Taiga
    {
        public TaigaMountains(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);
            this.clearDefaultBorder();

            this.defaultColor = 0x0A5B4F;
            this.defaultRarity = 10;

            this.defaultReplaceToBiomeName = "minecraft:mutated_taiga";
            this.defaultInheritMobsBiomeName = "minecraft:mutated_taiga";
        }
    }

    public static class SwamplandMountains extends Swampland
    {
        public SwamplandMountains(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);
            this.defaultColor = 0x28D29F;
            this.defaultRarity = 10;

            this.defaultReplaceToBiomeName = "minecraft:mutated_swampland";
            this.defaultInheritMobsBiomeName = "minecraft:mutated_swampland";
        }
    }

    public static class IcePlainsSpikes extends IcePlains
    {
        public IcePlainsSpikes(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);
            this.defaultColor = 0x8CB4B4;
            this.defaultRarity = 10;
            this.defaultTree = null;
            this.defaultDandelions = 0;
            this.defaultGrass = 0;
            this.defaultIceSpikes = true;
            this.defaultRareBuildingType = RareBuildingType.disabled;

            this.defaultReplaceToBiomeName = "minecraft:mutated_ice_flats";
            this.defaultInheritMobsBiomeName = "minecraft:mutated_ice_flats";
        }
    }

    public static class JungleMountains extends Jungle
    {
        public JungleMountains(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);
            this.defaultColor = 0x4C7009;
            this.defaultRarity = 10;

            this.defaultReplaceToBiomeName = "minecraft:mutated_jungle";
            this.defaultInheritMobsBiomeName = "minecraft:mutated_jungle";
        }
    }

    public static class JungleEdgeMountains extends JungleMountains
    {
        public JungleEdgeMountains(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);
            this.defaultColor = 0x5A8015;
            this.defaultSizeWhenBorder = 8;
            this.defaultBorder.add(DefaultBiome.JUNGLE_MOUNTAINS.Name);

            this.defaultReplaceToBiomeName = "minecraft:mutated_jungle_edge";
            this.defaultInheritMobsBiomeName = "minecraft:mutated_jungle_edge";
        }
    }

    public static class BirchForestMountains extends BirchForest
    {
        public BirchForestMountains(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);
            this.defaultColor = 0x4E6E58;
            this.defaultRarity = 10;
            this.defaultTree = new Object[] {10, TreeType.TallBirch, 80};

            this.defaultReplaceToBiomeName = "minecraft:mutated_birch_forest";
            this.defaultInheritMobsBiomeName = "minecraft:mutated_birch_forest";
        }
    }

    public static class BirchForestHillsMountains extends BirchForestHills
    {
        public BirchForestHillsMountains(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);
            this.defaultColor = 0x1F502E;
            this.defaultTree = new Object[] {10, TreeType.TallBirch, 80};
            this.defaultRarityWhenIsle = 97;
            this.defaultIsle.clear();
            this.defaultIsle.add(DefaultBiome.BIRCH_FOREST_MOUNTAINS.Name);

            this.defaultReplaceToBiomeName = "minecraft:mutated_birch_forest_hills";
            this.defaultInheritMobsBiomeName = "minecraft:mutated_birch_forest_hills";
        }
    }

    public static class RoofedForestMountains extends RoofedForest
    {
        public RoofedForestMountains(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);
            this.defaultColor = 0x364416;
            this.defaultRarity = 10;

            this.defaultReplaceToBiomeName = "minecraft:mutated_roofed_forest";
            this.defaultInheritMobsBiomeName = "minecraft:mutated_roofed_forest";
        }
    }

    public static class ColdTaigaMountains extends ColdTaiga
    {
        public ColdTaigaMountains(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);
            this.defaultColor = 0x2E5046;
            this.defaultRarity = 10;
            this.defaultRareBuildingType = RareBuildingType.disabled;

            this.defaultReplaceToBiomeName = "minecraft:mutated_taiga_cold";
            this.defaultInheritMobsBiomeName = "minecraft:mutated_taiga_cold";
        }
    }

    public static class MegaSpruceTaiga extends MegaTaiga
    {
        public MegaSpruceTaiga(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);
            this.defaultColor = 0x818E79;
            this.defaultRarity = 10;
            this.defaultTree = new Object[] {10, TreeType.HugeTaiga2, 8, TreeType.HugeTaiga1, 30, TreeType.Taiga1, 33, TreeType.Taiga2, 100};

            this.defaultReplaceToBiomeName = "minecraft:mutated_redwood_taiga";
            this.defaultInheritMobsBiomeName = "minecraft:mutated_redwood_taiga";
        }
    }

    public static class MegaSpruceTaigaHills extends MegaSpruceTaiga
    {
        public MegaSpruceTaigaHills(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);
            this.defaultColor = 0x475141;

            this.defaultIsle.add(DefaultBiome.MEGA_SPRUCE_TAIGA.Name);

            this.defaultReplaceToBiomeName = "minecraft:mutated_redwood_taiga_hills";
            this.defaultInheritMobsBiomeName = "minecraft:mutated_redwood_taiga_hills";
        }
    }

    public static class ExtremeHillsPlusMountains extends ExtremeHillsPlus
    {
        public ExtremeHillsPlusMountains(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);
            this.defaultColor = 0x466246;
            this.defaultRarity = 10;
            this.defaultSurfaceSurfaceAndGroundControl = new Object[] { LocalMaterials.GRASS, LocalMaterials.DIRT, -0.5,
        		LocalMaterials.DIRT + ":1", LocalMaterials.DIRT, 1.75, LocalMaterials.STONE, LocalMaterials.STONE, 10};
            // Override IsleInBiome: Extreme Hills of Extreme Hills+
            this.defaultIsle.clear();
            this.defaultIsle.add(DefaultBiome.EXTREME_HILLS_MOUNTAINS.Name);

            this.defaultReplaceToBiomeName = "minecraft:mutated_extreme_hills_with_trees";
            this.defaultInheritMobsBiomeName = "minecraft:mutated_extreme_hills_with_trees";
        }
    }

    public static class SavannaMountains extends Savanna
    {
        public SavannaMountains(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);
            this.defaultColor = 0x5B8015;
            this.defaultRarity = 10;
            this.defaultGrass = 60;
            this.defaultDoubleGrass = 0;
            this.defaultSurfaceSurfaceAndGroundControl = new Object[] {LocalMaterials.GRASS, LocalMaterials.DIRT, -0.5,
        		LocalMaterials.DIRT + ":1", LocalMaterials.DIRT, 1.75, LocalMaterials.STONE, LocalMaterials.STONE, 10};

            this.defaultReplaceToBiomeName = "minecraft:mutated_savanna";
            this.defaultInheritMobsBiomeName = "minecraft:mutated_savanna";
        }
    }

    public static class SavannaPlateauMountains extends SavannaMountains
    {
        public SavannaPlateauMountains(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);
            this.defaultColor = 0x99905C;
            this.defaultSizeWhenIsle = 6;
            this.defaultRarityWhenIsle = 97;
            this.defaultIsle.add(DefaultBiome.SAVANNA_MOUNTAINS.Name);

            this.defaultReplaceToBiomeName = "minecraft:mutated_savanna_rock";
            this.defaultInheritMobsBiomeName = "minecraft:mutated_savanna_rock";
        }
    }

    public static class MesaBryce extends Mesa
    {
        public MesaBryce(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);
            this.defaultColor = 0xE45627;
            this.defaultRarity = 10;
            this.defaultSurfaceSurfaceAndGroundControl = new Object[] {Constants.MESA_NAME_BRYCE};
            this.defaultIsle.add(DefaultBiome.MESA.Name);
            this.defaultSizeWhenIsle = 5;
            this.defaultRarityWhenIsle = 90;

            this.defaultReplaceToBiomeName = "minecraft:mutated_mesa";
            this.defaultInheritMobsBiomeName = "minecraft:mutated_mesa";
        }
    }

    public static class MesaPlateauForestMountains extends MesaPlateauForest
    {
        public MesaPlateauForestMountains(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);
            this.defaultColor = 0xA68F5F;
            this.defaultRarityWhenIsle = 90;

            this.defaultReplaceToBiomeName = "minecraft:mutated_mesa_rock";
            this.defaultInheritMobsBiomeName = "minecraft:mutated_mesa_rock";
        }
    }

    public static class MesaPlateauMountains extends MesaPlateau
    {
        public MesaPlateauMountains(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);
            this.defaultColor = 0xB77F5C;
            this.defaultRarityWhenIsle = 90;

            this.defaultReplaceToBiomeName = "minecraft:mutated_mesa_clear_rock";
            this.defaultInheritMobsBiomeName = "minecraft:mutated_mesa_clear_rock";
        }
    }
}
