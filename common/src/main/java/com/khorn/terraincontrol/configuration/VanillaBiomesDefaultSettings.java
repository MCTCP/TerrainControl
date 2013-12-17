package com.khorn.terraincontrol.configuration;

import com.khorn.terraincontrol.DefaultBiome;
import com.khorn.terraincontrol.DefaultMaterial;
import com.khorn.terraincontrol.LocalBiome;
import com.khorn.terraincontrol.configuration.BiomeConfig.RareBuildingType;
import com.khorn.terraincontrol.configuration.BiomeConfig.VillageType;
import com.khorn.terraincontrol.generator.resourcegens.TreeType;
import com.khorn.terraincontrol.generator.surfacegens.MesaSurfaceGenerator;

/**
 * Class to hold all default settings of all default biomes.
 * 
 * Because most vanilla biomes just have a few changes, it isn't needed to
 * give each their own class, a simple inner class is enough.
 */
public class VanillaBiomesDefaultSettings
{

    public static class Ocean extends DefaultBiomeSettings
    {
        public Ocean(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);

            this.defaultColor = "0x000070";
            this.defaultStrongholds = false;
            this.defaultRiverBiome = "";
            this.defaultTree = new Object[] {1, TreeType.BigTree, 1, TreeType.Tree, 9};
        }
    };

    public static class Plains extends DefaultBiomeSettings
    {
        public Plains(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);

            this.defaultDandelions = 4;
            this.defaultPoppies = 4;
            this.defaultGrass = 100;
            this.defaultColor = "0x8DB360";
            this.defaultStrongholds = false;
            this.defaultVillageType = VillageType.wood;
        }
    };

    public static class Desert extends DefaultBiomeSettings
    {
        public Desert(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);

            this.defaultWaterLakes = false;
            this.defaultDeadBush = 4;
            this.defaultGrass = 0;
            this.defaultReed = 10;
            this.defaultCactus = 10;
            this.defaultColor = "0xFA9418";
            this.defaultWell = new Object[] {DefaultMaterial.SANDSTONE, DefaultMaterial.STEP + ":1", DefaultMaterial.WATER, 1, 0.1, 2, this.worldHeight, DefaultMaterial.SAND};
            this.defaultVillageType = VillageType.sandstone;
            this.defaultRareBuildingType = RareBuildingType.desertPyramid;
        }
    };

    public static class ExtremeHills extends DefaultBiomeSettings
    {
        public ExtremeHills(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);

            this.defaultColor = "0x606060";
            this.defaultDandelions = 4;
            this.defaultEmeraldOre = TCDefaultValues.emeraldDepositFrequency.intValue();
            this.defaultTree = new Object[] {1, TreeType.Taiga2, 10, TreeType.BigTree, 1, TreeType.Tree, 9};
            this.defaultSurfaceSurfaceAndGroundControl = new Object[] {DefaultMaterial.GRASS, DefaultMaterial.DIRT, 1.0, DefaultMaterial.STONE, DefaultMaterial.STONE, 10.0};
        }
    };

    public static class Forest extends DefaultBiomeSettings
    {
        public Forest(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);

            this.defaultGrass = 30;
            this.defaultColor = "0x056621";
            this.defaultTree = new Object[] {10, TreeType.Birch, 20, TreeType.Tree, 100};
            this.defaultTallFlowers = 2;
            this.defaultPoppies = 4;
            this.defaultReed = 3;
            this.defaultMushroom = 1;
        }
    };

    public static class Taiga extends DefaultBiomeSettings
    {
        public Taiga(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);

            this.defaultGrass = 10;
            this.defaultColor = "0x0B6659";
            this.defaultTree = new Object[] {10, TreeType.Taiga1, 35, TreeType.Taiga2, 100};
        }
    };

    public static class Swampland extends DefaultBiomeSettings
    {
        public Swampland(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);

            this.defaultDandelions = 0;
            this.defaultMushroom = 16;
            this.defaultReed = 10;
            this.defaultClay = 1;
            this.defaultWaterLily = 1;
            this.defaultDandelions = 0;
            this.defaultBlueOrchids = 2;
            this.defaultColor = "0x07F9B2";
            this.defaultWaterColorMultiplier = "0xe0ffae";
            this.defaultGrassColor = "0x7E6E7E";
            this.defaultFoliageColor = "0x7E6E7E";
            this.defaultGrass = 30;
            this.defaultRareBuildingType = RareBuildingType.swampHut;
            this.defaultTree = new Object[] {2, TreeType.SwampTree, 100};
        }
    };

    public static class River extends DefaultBiomeSettings
    {
        public River(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);

            this.defaultSize = 8;
            this.defaultRarity = 95;
            this.defaultIsle.add(DefaultBiome.SWAMPLAND.Name);
            this.defaultColor = "0x0000FF";
            this.defaultStrongholds = false;
            this.defaultTree = new Object[] {1, TreeType.BigTree, 1, TreeType.Tree, 9};
        }
    };

    public static class Hell extends DefaultBiomeSettings
    {
        public Hell(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);
            this.defaultColor = "0xFF0000";
        }
    };

    public static class Sky extends DefaultBiomeSettings
    {
        public Sky(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);
            this.defaultColor = "0x8080FF";
        }
    };

    public static class FrozenOcean extends DefaultBiomeSettings
    {
        public FrozenOcean(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);

            this.defaultColor = "0x9090A0";
            this.defaultStrongholds = false;
            this.defaultRiverBiome = "";
        }
    };

    public static class FrozenRiver extends DefaultBiomeSettings
    {
        public FrozenRiver(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);

            this.defaultColor = "0xA0A0FF";
            this.defaultStrongholds = false;
        }
    };

    public static class IcePlains extends DefaultBiomeSettings
    {
        public IcePlains(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);

            this.defaultColor = "0xFFFFFF";
            this.defaultRiverBiome = DefaultBiome.FROZEN_RIVER.Name;
            this.defaultPoppies = 2;
        }
    };

    public static class IceMountains extends DefaultBiomeSettings
    {
        public IceMountains(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);

            this.defaultColor = "0xA0A0A0";
            this.defaultRiverBiome = DefaultBiome.FROZEN_RIVER.Name;
        }
    };

    public static class MushroomIsland extends DefaultBiomeSettings
    {
        public MushroomIsland(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);

            this.defaultSurfaceBlock = (byte) DefaultMaterial.MYCEL.id;
            this.defaultMushroom = 2;
            this.defaultGrass = 0;
            this.defaultDandelions = 0;
            this.defaultRarity = 1;
            this.defaultRiverBiome = "";
            this.defaultSize = 6;
            this.defaultIsle.add(DefaultBiome.OCEAN.Name);
            this.defaultColor = "0xFF00FF";
            this.defaultWaterLily = 1;
            this.defaultStrongholds = false;
            this.defaultTree = new Object[] {1, TreeType.HugeMushroom, 100};
        }
    };

    public static class MushroomIslandShore extends MushroomIsland
    {
        public MushroomIslandShore(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);

            this.defaultSize = 9;
            this.defaultBorder.add(DefaultBiome.MUSHROOM_ISLAND.Name);
            this.defaultColor = "0xA000FF";
            this.defaultTree = null; // No mushrooms on the shore
        }
    };

    public static class Beach extends DefaultBiomeSettings
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
            this.defaultColor = "0xFADE55";
            this.defaultStrongholds = false;
        }
    };

    public static class DesertHills extends Desert
    {
        public DesertHills(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);

            this.defaultWaterLakes = false;
            this.defaultSize = 6;
            this.defaultRarity = 97;
            this.defaultIsle.add(DefaultBiome.DESERT.Name);
            this.defaultDeadBush = 4;
            this.defaultGrass = 0;
            this.defaultReed = 50;
            this.defaultCactus = 10;
            this.defaultColor = "0xD25F12";
            this.defaultWell = new Object[] {DefaultMaterial.SANDSTONE, DefaultMaterial.STEP + ":1", DefaultMaterial.WATER, 1, 0.1, 2, this.worldHeight, DefaultMaterial.SAND};
            this.defaultVillageType = VillageType.sandstone;
            this.defaultRareBuildingType = RareBuildingType.desertPyramid;
        }
    };

    public static class ForestHills extends Forest
    {
        public ForestHills(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);

            this.defaultSize = 6;
            this.defaultRarity = 97;
            this.defaultIsle.add(DefaultBiome.FOREST.Name);
            this.defaultGrass = 15;
            this.defaultColor = "0x22551C";
        }
    };

    public static class TaigaHills extends Taiga
    {
        public TaigaHills(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);

            this.defaultSize = 6;
            this.defaultRarity = 97;
            this.defaultIsle.add(DefaultBiome.TAIGA.Name);
            this.defaultGrass = 10;
            this.defaultColor = "0x163933";
            this.defaultRiverBiome = DefaultBiome.FROZEN_RIVER.Name;
        }
    };

    public static class ExtremeHillsEdge extends ExtremeHills
    {
        public ExtremeHillsEdge(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);

            this.defaultSize = 8;
            this.defaultBorder.add(DefaultBiome.EXTREME_HILLS.Name);
            this.defaultColor = "0x72789A";
            this.defaultSurfaceSurfaceAndGroundControl = new Object[0];
        }
    };

    public static class Jungle extends DefaultBiomeSettings
    {
        public Jungle(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);

            this.defaultGrass = 25;
            this.defaultPoppies = 4;
            this.defaultDandelions = 4;
            this.defaultColor = "0x537B09";
            this.defaultRareBuildingType = RareBuildingType.jungleTemple;
            this.defaultTree = new Object[] {50, TreeType.BigTree, 10, TreeType.GroundBush, 50, TreeType.JungleTree, 35, TreeType.CocoaTree, 100};
        }
    };

    public static class JungleHills extends Jungle
    {
        public JungleHills(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);

            this.defaultColor = "0x2C4205";
            this.defaultIsle.add(DefaultBiome.JUNGLE.Name);
        }
    };

    public static class JungleEdge extends Jungle
    {
        public JungleEdge(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);
            this.defaultColor = "0x628B17";
        }
    }

    public static class DeepOcean extends Ocean
    {
        public DeepOcean(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);
            this.defaultColor = "0x000030";
        }
    }

    public static class StoneBeach extends Beach
    {
        public StoneBeach(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);
            this.defaultColor = "0xA2A284";
        }
    }

    public static class ColdBeach extends Beach
    {
        public ColdBeach(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);
            this.defaultColor = "0xFAF0C0";
        }
    }

    public static class BirchForest extends Forest
    {
        public BirchForest(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);
            this.defaultColor = "0x307444";
            this.defaultTree = new Object[] {10, TreeType.Birch, 80};
        }
    }

    public static class BirchForestHills extends BirchForest
    {
        public BirchForestHills(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);
            this.defaultColor = "0x1F5F32";
        }
    }

    public static class RoofedForest extends DefaultBiomeSettings
    {
        public RoofedForest(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);
            this.defaultColor = "0x40511A";
            this.defaultGrass = 15;
            this.defaultTree = new Object[] {20, TreeType.HugeMushroom, 3, TreeType.DarkOak, 66, TreeType.Birch, 20, TreeType.Tree, 100};
            this.defaultTallFlowers = 1;
        }
    }

    public static class ColdTaiga extends Taiga
    {
        public ColdTaiga(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);
            this.defaultColor = "0x31554A";
        }
    }

    public static class ColdTaigaHills extends ColdTaiga
    {
        public ColdTaigaHills(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);
            this.defaultColor = "0x243F36";
        }
    }

    public static class MegaTaiga extends DefaultBiomeSettings
    {
        public MegaTaiga(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);
            this.defaultColor = "0x596651";
            this.defaultTree = new Object[] {10, TreeType.HugeTaiga1, 33, TreeType.Taiga1, 33, TreeType.Taiga2, 100};
            this.defaultBoulder = 2;
            this.defaultGrass = 16;
            this.defaultFerns = 80;
            this.defaultMushroom = 8;
            this.defaultLargeFerns = 60;
            this.defaultSurfaceSurfaceAndGroundControl = new Object[] {DefaultMaterial.DIRT + ":2", DefaultMaterial.DIRT, -0.95, DefaultMaterial.DIRT + ":1", DefaultMaterial.DIRT, 1.75};
        }
    }

    public static class MegaTaigaHills extends MegaTaiga
    {
        public MegaTaigaHills(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);
            this.defaultColor = "0x454F3E";
        }
    }

    public static class ExtremeHillsPlus extends ExtremeHills
    {
        public ExtremeHillsPlus(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);
            this.defaultColor = "0x507050";
            this.defaultSurfaceSurfaceAndGroundControl = new Object[0];
            this.defaultTree = new Object[] {1, TreeType.Taiga2, 66, TreeType.BigTree, 10, TreeType.Tree, 100};
        }
    }

    public static class Savanna extends DefaultBiomeSettings
    {
        public Savanna(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);
            this.defaultColor = "0xBDB25F";
            this.defaultVillageType = VillageType.wood;
            this.defaultGrass = 200;
            this.defaultDoubleGrass = 4;
            this.defaultDandelions = 4;
            this.defaultTree = new Object[] {1, TreeType.Acacia, 80, TreeType.Tree, 100};
        }
    }

    public static class SavannaPlateau extends Savanna
    {
        public SavannaPlateau(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);
            this.defaultColor = "0xA79D64";
        }
    }

    public static class Mesa extends DefaultBiomeSettings
    {
        public Mesa(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);
            this.defaultColor = "0xD94515";
            this.defaultSurfaceSurfaceAndGroundControl = new Object[] {MesaSurfaceGenerator.NAME_NORMAL};
            this.defaultDandelions = 0;
            this.defaultDeadBush = 7;
            this.defaultReed = 5;
            this.defaultCactus = 10;
            this.defaultGrass = 0;
        }
    }

    public static class MesaPlateauForest extends MesaPlateau
    {
        public MesaPlateauForest(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);
            this.defaultColor = "0xB09765";
            this.defaultSurfaceSurfaceAndGroundControl = new Object[] {MesaSurfaceGenerator.NAME_FOREST};
            this.defaultTree = new Object[] {1, TreeType.Tree, 100};
            this.defaultGrass = 10;

            // Minecraft has chosen sand and stained clay as the surface and
            // ground blocks. It then places hardcoded grass and dirt.
            // Terrain Control does it the other way round: it places
            // hardcoded sand and stained clay and lets the user change the
            // grass and dirt blocks.
            this.defaultSurfaceBlock = DefaultMaterial.GRASS.id;
            this.defaultGroundBlock = DefaultMaterial.DIRT.id;
        }
    }

    public static class MesaPlateau extends Mesa
    {
        public MesaPlateau(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);
            this.defaultColor = "0xCA8C65";
        }
    }

    public static class SunflowerPlains extends Plains
    {
        public SunflowerPlains(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);
            this.defaultColor = "0xDEFF00";
            this.defaultSunflowers = 30;
        }
    }

    public static class DesertMountains extends Desert
    {
        public DesertMountains(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);
            this.defaultColor = "0xE58208";
            this.defaultWaterLakes = true;
        }
    }

    public static class ExtremeHillsMountains extends ExtremeHills
    {
        public ExtremeHillsMountains(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);
            this.defaultColor = "0x525252";
            this.defaultSurfaceSurfaceAndGroundControl = new Object[] {DefaultMaterial.GRAVEL, DefaultMaterial.GRAVEL, -1.0, DefaultMaterial.GRASS, DefaultMaterial.DIRT, 2.0, DefaultMaterial.GRAVEL, DefaultMaterial.GRAVEL, 10.0};
        }
    }

    public static class FlowerForest extends Forest
    {
        public FlowerForest(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);
            this.defaultColor = "0x2D8E49";
            this.defaultDandelions = 0;
        }
    }

    public static class TaigaMountains extends Taiga
    {
        public TaigaMountains(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);
            this.defaultColor = "0x0A5B4F";
        }
    }

    public static class SwamplandMountains extends Swampland
    {
        public SwamplandMountains(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);
            this.defaultColor = "0x28D29F";
        }
    }

    public static class IcePlainsSpikes extends IcePlains
    {
        public IcePlainsSpikes(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);
            this.defaultColor = "0x8CB4B4";
        }
    }

    public static class JungleMountains extends Jungle
    {
        public JungleMountains(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);
            this.defaultColor = "0x4C7009";
        }
    }

    public static class JungleEdgeMountains extends JungleEdge
    {
        public JungleEdgeMountains(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);
            this.defaultColor = "0x5A8015";
        }
    }

    public static class BirchForestMountains extends BirchForest
    {
        public BirchForestMountains(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);
            this.defaultColor = "0x4E6E58";
            this.defaultTree = new Object[] {10, TreeType.TallBirch, 80};
        }
    }

    public static class BirchForestHillsMountains extends BirchForestHills
    {
        public BirchForestHillsMountains(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);
            this.defaultColor = "0x1F502E";
            this.defaultTree = new Object[] {10, TreeType.TallBirch, 80};
        }
    }

    public static class RoofedForestMountains extends RoofedForest
    {
        public RoofedForestMountains(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);
            this.defaultColor = "0x364416";
        }
    }

    public static class ColdTaigaMountains extends ColdTaiga
    {
        public ColdTaigaMountains(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);
            this.defaultColor = "0x2E5046";
        }
    }

    public static class MegaSpruceTaiga extends MegaTaiga
    {
        public MegaSpruceTaiga(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);
            this.defaultColor = "0x598110";
            this.defaultTree = new Object[] {10, TreeType.HugeTaiga2, 8, TreeType.HugeTaiga1, 30, TreeType.Taiga1, 33, TreeType.Taiga2, 100};
        }
    }

    public static class MegaSpruceTaigaHills extends MegaSpruceTaiga
    {
        public MegaSpruceTaigaHills(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);
            this.defaultColor = "0x475141";
        }
    }

    public static class ExtremeHillsPlusMountains extends ExtremeHillsPlus
    {
        public ExtremeHillsPlusMountains(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);
            this.defaultColor = "0x466246";
            this.defaultSurfaceSurfaceAndGroundControl = new Object[] {DefaultMaterial.GRAVEL, DefaultMaterial.GRAVEL, -1.0, DefaultMaterial.GRASS, DefaultMaterial.DIRT, 2.0, DefaultMaterial.GRAVEL, DefaultMaterial.GRAVEL, 10.0};
        }
    }

    public static class SavannaMountains extends Savanna
    {
        public SavannaMountains(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);
            this.defaultColor = "0x5B8015";
            this.defaultGrass = 60;
            this.defaultDoubleGrass = 0;
            this.defaultSurfaceSurfaceAndGroundControl = new Object[] {DefaultMaterial.GRASS, DefaultMaterial.DIRT, -0.5, DefaultMaterial.DIRT + ":1", DefaultMaterial.DIRT, 1.75, DefaultMaterial.STONE, DefaultMaterial.STONE, 10};
        }
    }

    public static class SavannaPlateauMountains extends SavannaMountains
    {
        public SavannaPlateauMountains(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);
            this.defaultColor = "0x99905C";
        }
    }

    public static class MesaBryce extends Mesa
    {
        public MesaBryce(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);
            this.defaultColor = "0xE45627";
            this.defaultSurfaceSurfaceAndGroundControl = new Object[] {MesaSurfaceGenerator.NAME_BRYCE};
        }
    }

    public static class MesaPlateauForestMountains extends MesaPlateauForest
    {
        public MesaPlateauForestMountains(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);
            this.defaultColor = "0xA68F5F";
        }
    }

    public static class MesaPlateauMountains extends MesaPlateau
    {
        public MesaPlateauMountains(LocalBiome minecraftBiome, int worldHeight)
        {
            super(minecraftBiome, worldHeight);
            this.defaultColor = "0xB77F5C";
        }
    }

}
