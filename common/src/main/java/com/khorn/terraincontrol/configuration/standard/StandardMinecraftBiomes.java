package com.khorn.terraincontrol.configuration.standard;

import com.khorn.terraincontrol.DefaultBiome;
import com.khorn.terraincontrol.DefaultMaterial;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.configuration.BiomeConfigFile.RareBuildingType;
import com.khorn.terraincontrol.configuration.BiomeConfigFile.VillageType;
import com.khorn.terraincontrol.generator.resource.TreeType;

/**
 * Class to hold all default settings of all default biomes.
 * 
 * Because most vanilla biomes just have a few changes, it isn't needed to give
 * each their own class, a simple inner class is enough.
 */
public class StandardMinecraftBiomes
{

    public static class Ocean extends StandardBiomeFactory
    {
        public Ocean(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);

            this.defaultColor = "0x3333FF";
            this.defaultStrongholds = false;
            this.defaultRiverBiome = "";
            this.defaultTree = new Object[] {1, TreeType.BigTree, 1, TreeType.Tree, 9};
        }
    }

    public static class Plains extends StandardBiomeFactory
    {
        public Plains(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);

            this.defaultFlowers = 4;
            this.defaultGrass = 100;
            this.defaultColor = "0x999900";
            this.defaultStrongholds = false;
            this.defaultVillageType = VillageType.wood;
        }
    }

    public static class Desert extends StandardBiomeFactory
    {
        public Desert(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);

            this.defaultWaterLakes = false;
            this.defaultDeadBrush = 4;
            this.defaultGrass = 0;
            this.defaultReed = 10;
            this.defaultCactus = 10;
            this.defaultColor = "0xFFCC33";
            this.defaultWell = new Object[] {DefaultMaterial.SANDSTONE, DefaultMaterial.STEP + ":1", DefaultMaterial.WATER, 1, 0.1, 2, this.worldHeight, DefaultMaterial.SAND};
            this.defaultVillageType = VillageType.sandstone;
            this.defaultRareBuildingType = RareBuildingType.desertPyramid;
        }
    }

    public static class ExtremeHills extends StandardBiomeFactory
    {
        public ExtremeHills(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);

            this.defaultColor = "0x333300";
            this.defaultEmeraldOre = BiomeStandardValues.emeraldDepositFrequency.intValue();
            this.defaultTree = new Object[] {1, TreeType.BigTree, 1, TreeType.Tree, 9};
        }
    }

    public static class Forest extends StandardBiomeFactory
    {
        public Forest(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);

            this.defaultGrass = 15;
            this.defaultColor = "0x00FF00";
            this.defaultTree = new Object[] {10, TreeType.Forest, 20, TreeType.BigTree, 10, TreeType.Tree, 100};
        }
    }

    public static class Taiga extends StandardBiomeFactory
    {
        public Taiga(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);

            this.defaultGrass = 10;
            this.defaultColor = "0x007700";
            this.defaultTree = new Object[] {10, TreeType.Taiga1, 35, TreeType.Taiga2, 100};
        }
    }

    public static class Swampland extends StandardBiomeFactory
    {
        public Swampland(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);

            this.defaultFlowers = -999;
            this.defaultDeadBrush = 1;
            this.defaultMushroom = 8;
            this.defaultReed = 10;
            this.defaultClay = 1;
            this.defaultWaterLily = 1;
            this.defaultColor = "0x99CC66";
            this.defaultWaterColorMultiplier = "0xE0FFAE";
            this.defaultGrassColor = "0x7E6E7E";
            this.defaultFoliageColor = "0x7E6E7E";
            this.defaultRareBuildingType = RareBuildingType.swampHut;
            this.defaultTree = new Object[] {2, TreeType.SwampTree, 100};
        }
    }

    public static class River extends StandardBiomeFactory
    {
        public River(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);

            this.defaultSize = 8;
            this.defaultRarity = 95;
            this.defaultIsle.add(DefaultBiome.SWAMPLAND.Name);
            this.defaultColor = "0x00CCCC";
            this.defaultStrongholds = false;
            this.defaultTree = new Object[] {1, TreeType.BigTree, 1, TreeType.Tree, 9};
        }
    }

    public static class Hell extends StandardBiomeFactory
    {
        public Hell(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);
        }
    }

    public static class Sky extends StandardBiomeFactory
    {
        public Sky(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);
        }
    }

    public static class FrozenOcean extends StandardBiomeFactory
    {
        public FrozenOcean(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);

            this.defaultColor = "0xFFFFFF";
            this.defaultStrongholds = false;
            this.defaultRiverBiome = "";
        }
    }

    public static class FrozenRiver extends StandardBiomeFactory
    {
        public FrozenRiver(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);

            this.defaultColor = "0x66FFFF";
            this.defaultStrongholds = false;
        }
    }

    public static class IcePlains extends StandardBiomeFactory
    {
        public IcePlains(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);

            this.defaultColor = "0xCCCCCC";
            this.defaultRiverBiome = DefaultBiome.FROZEN_RIVER.Name;
        }
    }

    public static class IceMountains extends StandardBiomeFactory
    {
        public IceMountains(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);

            this.defaultColor = "0xCC9966";
            this.defaultRiverBiome = DefaultBiome.FROZEN_RIVER.Name;
        }
    }

    public static class MushroomIsland extends StandardBiomeFactory
    {
        public MushroomIsland(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);

            this.defaultSurfaceBlock = (byte) DefaultMaterial.MYCEL.id;
            this.defaultMushroom = 1;
            this.defaultGrass = 0;
            this.defaultFlowers = 0;
            this.defaultRarity = 1;
            this.defaultRiverBiome = "";
            this.defaultSize = 6;
            this.defaultIsle.add(DefaultBiome.OCEAN.Name);
            this.defaultColor = "0xFF33CC";
            this.defaultWaterLily = 1;
            this.defaultStrongholds = false;
            this.defaultTree = new Object[] {1, TreeType.HugeMushroom, 100};
        }
    }

    public static class MushroomIslandShore extends MushroomIsland
    {
        public MushroomIslandShore(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);

            this.defaultSize = 9;
            this.defaultBorder.add(DefaultBiome.MUSHROOM_ISLAND.Name);
            this.defaultColor = "0xFF9999";
            this.defaultTree = null; // No mushrooms on the shore
        }
    }

    public static class Beach extends StandardBiomeFactory
    {
        public Beach(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);

            this.defaultSize = 8;
            this.defaultBorder.add(DefaultBiome.OCEAN.Name);
            this.defaultNotBorderNear.add(DefaultBiome.RIVER.Name);
            this.defaultNotBorderNear.add(DefaultBiome.SWAMPLAND.Name);
            this.defaultNotBorderNear.add(DefaultBiome.EXTREME_HILLS.Name);
            this.defaultNotBorderNear.add(DefaultBiome.MUSHROOM_ISLAND.Name);
            this.defaultColor = "0xFFFF00";
            this.defaultStrongholds = false;
        }
    }

    public static class DesertHills extends Desert
    {
        public DesertHills(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);

            this.defaultWaterLakes = false;
            this.defaultSize = 6;
            this.defaultRarity = 97;
            this.defaultIsle.add(DefaultBiome.DESERT.Name);
            this.defaultDeadBrush = 4;
            this.defaultGrass = 0;
            this.defaultReed = 50;
            this.defaultCactus = 10;
            this.defaultColor = "0x996600";
            this.defaultWell = new Object[] {DefaultMaterial.SANDSTONE, DefaultMaterial.STEP + ":1", DefaultMaterial.WATER, 1, 0.1, 2, this.worldHeight, DefaultMaterial.SAND};
            this.defaultVillageType = VillageType.sandstone;
            this.defaultRareBuildingType = RareBuildingType.desertPyramid;
        }
    }

    public static class ForestHills extends Forest
    {
        public ForestHills(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);

            this.defaultSize = 6;
            this.defaultRarity = 97;
            this.defaultIsle.add(DefaultBiome.FOREST.Name);
            this.defaultGrass = 15;
            this.defaultColor = "0x009900";
        }
    }

    public static class TaigaHills extends Taiga
    {
        public TaigaHills(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);

            this.defaultSize = 6;
            this.defaultRarity = 97;
            this.defaultIsle.add(DefaultBiome.TAIGA.Name);
            this.defaultGrass = 10;
            this.defaultColor = "0x003300";
            this.defaultRiverBiome = DefaultBiome.FROZEN_RIVER.Name;
        }
    }

    public static class ExtremeHillsEdge extends ExtremeHills
    {
        public ExtremeHillsEdge(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);

            this.defaultSize = 8;
            this.defaultBorder.add(DefaultBiome.EXTREME_HILLS.Name);
            this.defaultColor = "0x666600";
        }
    }

    public static class Jungle extends StandardBiomeFactory
    {
        public Jungle(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);

            this.defaultGrass = 25;
            this.defaultFlowers = 4;
            this.defaultColor = "0xCC6600";
            this.defaultRareBuildingType = RareBuildingType.jungleTemple;
            this.defaultTree = new Object[] {50, TreeType.BigTree, 10, TreeType.GroundBush, 50, TreeType.JungleTree, 35, TreeType.CocoaTree, 100};
        }
    }

    public static class JungleHills extends Jungle
    {
        public JungleHills(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);

            this.defaultColor = "0x663300";
            this.defaultIsle.add(DefaultBiome.JUNGLE.Name);
        }
    }
}
