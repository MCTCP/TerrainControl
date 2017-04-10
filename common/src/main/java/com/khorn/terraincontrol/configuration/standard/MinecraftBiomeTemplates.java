package com.khorn.terraincontrol.configuration.standard;

import com.khorn.terraincontrol.configuration.BiomeConfig.MineshaftType;
import com.khorn.terraincontrol.configuration.BiomeConfig.RareBuildingType;
import com.khorn.terraincontrol.configuration.BiomeConfig.VillageType;
import com.khorn.terraincontrol.generator.surface.MesaSurfaceGenerator;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultBiome;
import com.khorn.terraincontrol.util.minecraftTypes.DefaultMaterial;
import com.khorn.terraincontrol.util.minecraftTypes.TreeType;

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
            this.isCustomBiome = false; // We're a vanilla biome
            
            // Some settings are provided by MojangSettings,
            // which gets them from Minecraft
            this.defaultBiomeSurface = this.mojangSettings.getSurfaceHeight();
            this.defaultBiomeVolatility = this.mojangSettings.getSurfaceVolatility();
            this.defaultSurfaceBlock = this.mojangSettings.getSurfaceBlock().toDefaultMaterial();
            this.defaultGroundBlock = this.mojangSettings.getGroundBlock().toDefaultMaterial();
            this.defaultBiomeTemperature = this.mojangSettings.getTemperature();
            this.defaultBiomeWetness = this.mojangSettings.getWetness();
                                   
            // Remove default mobs for vanilla biomes, these will be inherited
            // TODO: Check if this breaks Bukkit mob spawning
            
            this.defaultCreatures = new ArrayList();
            this.defaultMonsters = new ArrayList();
            this.defaultWaterCreatures = new ArrayList();
            this.defaultAmbientCreatures = new ArrayList();
            
            //this.defaultMonsters = this.mojangSettings.getMobSpawnGroup(MojangSettings.EntityCategory.MONSTER);
            //this.defaultCreatures = this.mojangSettings.getMobSpawnGroup(MojangSettings.EntityCategory.CREATURE);
            //this.defaultWaterCreatures = this.mojangSettings.getMobSpawnGroup(MojangSettings.EntityCategory.WATER_CREATURE);
            //this.defaultAmbientCreatures = this.mojangSettings.getMobSpawnGroup(MojangSettings.EntityCategory.AMBIENT_CREATURE);

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
            
            this.defaultInheritMobsBiomeName = "Ocean";
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
            
            this.defaultInheritMobsBiomeName = "Plains";
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
            this.defaultWell = new Object[] {DefaultMaterial.SANDSTONE, DefaultMaterial.STEP + ":1", DefaultMaterial.WATER, 1, 0.1, 2,
                    this.worldHeight, DefaultMaterial.SAND};
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
            
            this.defaultInheritMobsBiomeName = "Desert";
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
            this.defaultEmeraldOre = BiomeStandardValues.emeraldDepositFrequency;
            this.defaultTree = new Object[] {1, TreeType.Taiga2, 10, TreeType.BigTree, 1, TreeType.Tree, 9};
            this.defaultSurfaceSurfaceAndGroundControl = new Object[] {DefaultMaterial.GRASS, DefaultMaterial.DIRT, 1.0,
                    DefaultMaterial.STONE, DefaultMaterial.STONE, 10.0};
            
            this.defaultInheritMobsBiomeName = "Extreme Hills";            
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
            
            this.defaultInheritMobsBiomeName = "Forest";            
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
            
            this.defaultInheritMobsBiomeName = "Taiga";            
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
            
            this.defaultInheritMobsBiomeName = "Swampland";           
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
            
            this.defaultInheritMobsBiomeName = "River";            
            this.defaultBiomeDictId = "RIVER";
        }
    }

    public static class Hell extends MinecraftBiomeTemplate
    {
        public Hell(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);
            this.defaultColor = 0xFF0000;
            
            this.defaultInheritMobsBiomeName = "Hell";            
            this.defaultBiomeDictId = "HOT, DRY, NETHER";
        }
    }

    public static class Sky extends MinecraftBiomeTemplate
    {
        public Sky(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);
            this.defaultColor = 0x8080FF;
            
            this.defaultInheritMobsBiomeName = "Sky";            
            this.defaultBiomeDictId = "COLD, DRY, END";
        }
    }

    public static class FrozenOcean extends Ocean
    {
        public FrozenOcean(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);

            this.defaultColor = 0x9090A0;
            
            this.defaultInheritMobsBiomeName = "FrozenOcean";            
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
            
            this.defaultInheritMobsBiomeName = "FrozenRiver";            
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
            
            this.defaultInheritMobsBiomeName = "Ice Plains";            
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
            
            this.defaultInheritMobsBiomeName = "Ice Mountains";            
            this.defaultBiomeDictId = "COLD, SNOWY, MOUNTAIN";
        }
    }

    public static class MushroomIsland extends MinecraftBiomeTemplate
    {
        public MushroomIsland(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);

            this.defaultSurfaceBlock = DefaultMaterial.MYCEL;
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
            
            this.defaultInheritMobsBiomeName = "MushroomIsland";            
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
            
            this.defaultInheritMobsBiomeName = "MushroomIslandShore";            
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
            
            this.defaultInheritMobsBiomeName = "Beach";
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
            this.defaultWell = new Object[] {DefaultMaterial.SANDSTONE, DefaultMaterial.STEP + ":1", DefaultMaterial.WATER, 1, 0.1, 2,
                    this.worldHeight, DefaultMaterial.SAND};
            this.defaultVillageType = VillageType.sandstone;
            this.defaultRareBuildingType = RareBuildingType.desertPyramid;

            // Don't inherit border properties of the Desert biome
            this.clearDefaultBorder();
            
            this.defaultInheritMobsBiomeName = "DesertHills";            
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
            
            this.defaultInheritMobsBiomeName = "ForestHills";            
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
            
            this.defaultInheritMobsBiomeName = "TaigaHills";            
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
            
            this.defaultInheritMobsBiomeName = "Extreme Hills Edge";            
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
            
            this.defaultInheritMobsBiomeName = "Jungle";            
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
            
            this.defaultInheritMobsBiomeName = "JungleHills";            
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
            
            this.defaultInheritMobsBiomeName = "JungleEdge";            
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
            
            this.defaultInheritMobsBiomeName = "Deep Ocean";            
            this.defaultBiomeDictId = "OCEAN";
        }
    }

    public static class StoneBeach extends MinecraftBiomeTemplate
    {
        public StoneBeach(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);
            this.defaultColor = 0xA2A284;
            
            this.defaultInheritMobsBiomeName = "Stone Beach";            
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
            
            this.defaultInheritMobsBiomeName = "Cold Beach";            
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
            
            this.defaultInheritMobsBiomeName = "Birch Forest";            
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
            
            this.defaultInheritMobsBiomeName = "Birch Forest Hills";            
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
            
            this.defaultInheritMobsBiomeName = "Roofed Forest";            
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
            
            this.defaultInheritMobsBiomeName = "Cold Taiga";            
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
            
            this.defaultInheritMobsBiomeName = "Cold Taiga Hills";            
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
            this.defaultSurfaceSurfaceAndGroundControl = new Object[] {DefaultMaterial.DIRT + ":2", DefaultMaterial.DIRT, -0.95,
                    DefaultMaterial.DIRT + ":1", DefaultMaterial.DIRT, 1.75};
            
            this.defaultInheritMobsBiomeName = "Mega Taiga";            
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
            
            this.defaultInheritMobsBiomeName = "Mega Taiga Hills";            
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
            
            this.defaultInheritMobsBiomeName = "Extreme Hills+";
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
            
            this.defaultInheritMobsBiomeName = "Savanna";            
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
            
            this.defaultInheritMobsBiomeName = "Savanna Plateau";            
            this.defaultBiomeDictId = "HOT, SAVANNA, PLAINS, SPARSE";
        }
    }

    public static class Mesa extends MinecraftBiomeTemplate
    {
        public Mesa(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);
            this.defaultColor = 0xD94515;
            this.defaultSurfaceSurfaceAndGroundControl = new Object[] {MesaSurfaceGenerator.NAME_NORMAL};
            this.defaultDandelions = 0;
            this.defaultDeadBush = 7;
            this.defaultReed = 5;
            this.defaultCactus = 10;
            this.defaultGrass = 0;
            this.defaultMineshaftType = MineshaftType.mesa;
            
            this.defaultInheritMobsBiomeName = "Mesa";            
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
            this.defaultSurfaceSurfaceAndGroundControl = new Object[] {MesaSurfaceGenerator.NAME_FOREST};
            this.defaultTree = new Object[] {1, TreeType.Tree, 100};
            this.defaultGrass = 10;

            // Minecraft has chosen sand and stained clay as the surface and
            // ground blocks. It then places hardcoded grass and dirt.
            // Open Terrain Generator does it the other way round: it places
            // hardcoded sand and stained clay and lets the user change the
            // grass and dirt blocks.
            this.defaultSurfaceBlock = DefaultMaterial.GRASS;
            this.defaultGroundBlock = DefaultMaterial.DIRT;
            
            this.defaultInheritMobsBiomeName = "Mesa Plateau F";            
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
            
            this.defaultInheritMobsBiomeName = "Mesa Plateau";            
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
                        
            this.defaultInheritMobsBiomeName = "The Void";            
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
            
            this.defaultInheritMobsBiomeName = "Sunflower Plains";
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
            
            this.defaultInheritMobsBiomeName = "Desert M";
        }
    }

    public static class ExtremeHillsMountains extends ExtremeHills
    {
        public ExtremeHillsMountains(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);
            this.defaultColor = 0x525252;
            this.defaultSurfaceSurfaceAndGroundControl = new Object[] {DefaultMaterial.GRAVEL, DefaultMaterial.GRAVEL, -1.0,
                    DefaultMaterial.GRASS, DefaultMaterial.DIRT, 2.0, DefaultMaterial.GRAVEL, DefaultMaterial.GRAVEL, 10.0};
            this.defaultRarity = 10;
            
            this.defaultInheritMobsBiomeName = "Extreme Hills M";
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
            
            this.defaultInheritMobsBiomeName = "Flower Forest";
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
            
            this.defaultInheritMobsBiomeName = "Taiga M";
        }
    }

    public static class SwamplandMountains extends Swampland
    {
        public SwamplandMountains(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);
            this.defaultColor = 0x28D29F;
            this.defaultRarity = 10;
            
            this.defaultInheritMobsBiomeName = "Swampland M";
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
            
            this.defaultInheritMobsBiomeName = "Ice Plains Spikes";
        }
    }

    public static class JungleMountains extends Jungle
    {
        public JungleMountains(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);
            this.defaultColor = 0x4C7009;
            this.defaultRarity = 10;
            
            this.defaultInheritMobsBiomeName = "Jungle M";
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
            
            this.defaultInheritMobsBiomeName = "JungleEdge M";
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
            
            this.defaultInheritMobsBiomeName = "Birch Forest M";
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
            
            this.defaultInheritMobsBiomeName = "Birch Forest Hills M";
        }
    }

    public static class RoofedForestMountains extends RoofedForest
    {
        public RoofedForestMountains(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);
            this.defaultColor = 0x364416;
            this.defaultRarity = 10;
            
            this.defaultInheritMobsBiomeName = "Roofed Forest M";
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
            
            this.defaultInheritMobsBiomeName = "Cold Taiga M";
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
            
            this.defaultInheritMobsBiomeName = "Mega Spruce Taiga";
        }
    }

    public static class MegaSpruceTaigaHills extends MegaSpruceTaiga
    {
        public MegaSpruceTaigaHills(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);
            this.defaultColor = 0x475141;

            this.defaultIsle.add(DefaultBiome.MEGA_SPRUCE_TAIGA.Name);
            
            this.defaultInheritMobsBiomeName = "Mega Spruce Taiga Hills";
        }
    }

    public static class ExtremeHillsPlusMountains extends ExtremeHillsPlus
    {
        public ExtremeHillsPlusMountains(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);
            this.defaultColor = 0x466246;
            this.defaultRarity = 10;
            this.defaultSurfaceSurfaceAndGroundControl = new Object[] {DefaultMaterial.GRAVEL, DefaultMaterial.GRAVEL, -1.0,
                    DefaultMaterial.GRASS, DefaultMaterial.DIRT, 2.0, DefaultMaterial.GRAVEL, DefaultMaterial.GRAVEL, 10.0};
            // Override IsleInBiome: Extreme Hills of Extreme Hills+
            this.defaultIsle.clear();
            this.defaultIsle.add(DefaultBiome.EXTREME_HILLS_MOUNTAINS.Name);
            
            this.defaultInheritMobsBiomeName = "Extreme Hills+ M";
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
            this.defaultSurfaceSurfaceAndGroundControl = new Object[] {DefaultMaterial.GRASS, DefaultMaterial.DIRT, -0.5,
                    DefaultMaterial.DIRT + ":1", DefaultMaterial.DIRT, 1.75, DefaultMaterial.STONE, DefaultMaterial.STONE, 10};
            
            this.defaultInheritMobsBiomeName = "Savanna M";
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
            
            this.defaultInheritMobsBiomeName = "Savanna Plateau M";
        }
    }

    public static class MesaBryce extends Mesa
    {
        public MesaBryce(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);
            this.defaultColor = 0xE45627;
            this.defaultRarity = 10;
            this.defaultSurfaceSurfaceAndGroundControl = new Object[] {MesaSurfaceGenerator.NAME_BRYCE};
            this.defaultIsle.add(DefaultBiome.MESA.Name);
            this.defaultSizeWhenIsle = 5;
            this.defaultRarityWhenIsle = 90;
            
            this.defaultInheritMobsBiomeName = "Mesa (Bryce)";
        }
    }

    public static class MesaPlateauForestMountains extends MesaPlateauForest
    {
        public MesaPlateauForestMountains(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);
            this.defaultColor = 0xA68F5F;
            this.defaultRarityWhenIsle = 90;
            
            this.defaultInheritMobsBiomeName = "Mesa Plateau F M";
        }
    }

    public static class MesaPlateauMountains extends MesaPlateau
    {
        public MesaPlateauMountains(MojangSettings mojangSettings, int worldHeight)
        {
            super(mojangSettings, worldHeight);
            this.defaultColor = 0xB77F5C;
            this.defaultRarityWhenIsle = 90;
            
            this.defaultInheritMobsBiomeName = "Mesa Plateau M";
        }
    }

}
