package com.khorn.terraincontrol.forge.feature;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class RandomPlantStateGenerator {

    static boolean debug = false;

    public static IBlockState randomCrop(World worldIn, BlockPos blockPos, Random random) {
        String plantName = "";
        int plantAge = 0;
        int plant = random.nextInt(64); // Total Crops

        if (plant == 0) {
            plantName = "almura:crop/agave";
            plantAge = 5;
        } else if (plant == 1) {
            plantName = "almura:crop/alfalfa";
            plantAge = 3;
        } else if (plant == 2) {
            plantName = "almura:crop/bambooshoot";
            plantAge = 4;
        } else if (plant == 3) {
            plantName = "almura:crop/barley";
            plantAge = 3;
        } else if (plant == 4) {
            plantName = "almura:crop/basil";
            plantAge = 4;
        } else if (plant == 5) {
            plantName = "almura:crop/bellpepper";
            plantAge = 4;
        } else if (plant == 6) {
            plantName = "almura:crop/blackberry";
            plantAge = 4;
        } else if (plant == 7) {
            plantName = "almura:crop/bellpepper";
            plantAge = 4;
        } else if (plant == 8) {
            plantName = "almura:crop/blackroot";
            plantAge = 5;
        } else if (plant == 9) {
            plantName = "almura:crop/blueberry";
            plantAge = 4;
        } else if (plant == 10) {
            plantName = "almura:crop/broccoli";
            plantAge = 4;
        } else if (plant == 11) {
            plantName = "almura:crop/butterbean";
            plantAge = 4;
        } else if (plant == 12) {
            plantName = "almura:crop/cabbage";
            plantAge = 4;
        } else if (plant == 13) {
            plantName = "almura:crop/celery";
            plantAge = 4;
        } else if (plant == 14) {
            plantName = "almura:crop/chilipepper";
            plantAge = 4;
        } else if (plant == 15) {
            plantName = "almura:crop/chive";
            plantAge = 5;
        } else if (plant == 16) {
            plantName = "almura:crop/cilantro";
            plantAge = 5;
        } else if (plant == 17) {
            plantName = "almura:crop/clove";
            plantAge = 5;
        } else if (plant == 18) {
            plantName = "almura:crop/coffee_bean";
            plantAge = 4;
        } else if (plant == 19) {
            plantName = "almura:crop/concord_grape";
            plantAge = 3;
        } else if (plant == 20) {
            plantName = "almura:crop/corn";
            plantAge = 4;
        } else if (plant == 21) {
            plantName = "almura:crop/cotton";
            plantAge = 4;
        } else if (plant == 22) {
            plantName = "almura:crop/cranberry";
            plantAge = 4;
        } else if (plant == 23) {
            plantName = "almura:crop/cucumber";
            plantAge = 4;
        } else if (plant == 24) {
            plantName = "almura:crop/cumin";
            plantAge = 5;
        } else if (plant == 25) {
            plantName = "almura:crop/daikon";
            plantAge = 4;
        } else if (plant == 26) {
            plantName = "almura:crop/dill";
            plantAge = 5;
        } else if (plant == 27) {
            plantName = "almura:crop/eggplant";
            plantAge = 4;
        } else if (plant == 28) {
            plantName = "almura:crop/fargreen";
            plantAge = 4;
        } else if (plant == 29) {
            plantName = "almura:crop/garlic";
            plantAge = 4;
        } else if (plant == 30) {
            plantName = "almura:crop/honeydew";
            plantAge = 4;
        } else if (plant == 31) {
            plantName = "almura:crop/hop";
            plantAge = 5;
        } else if (plant == 32) {
            plantName = "almura:crop/laurel";
            plantAge = 5;
        } else if (plant == 33) {
            plantName = "almura:crop/leek";
            plantAge = 4;
        } else if (plant == 34) {
            plantName = "almura:crop/lettuce";
            plantAge = 4;
        } else if (plant == 35) {
            plantName = "almura:crop/lotusroot";
            plantAge = 4;
        } else if (plant == 36) {
            plantName = "almura:crop/luffa";
            plantAge = 4;
        } else if (plant == 37) {
            plantName = "almura:crop/mint";
            plantAge = 5;
        } else if (plant == 38) {
            plantName = "almura:crop/oat";
            plantAge = 3;
        } else if (plant == 39) {
            plantName = "almura:crop/olive";
            plantAge = 4;
        } else if (plant == 40) {
            plantName = "almura:crop/onion";
            plantAge = 4;
        } else if (plant == 41) {
            plantName = "almura:crop/oregano";
            plantAge = 5;
        } else if (plant == 42) {
            plantName = "almura:crop/parsley";
            plantAge = 5;
        } else if (plant == 43) {
            plantName = "almura:crop/pea";
            plantAge = 5;
        } else if (plant == 44) {
            plantName = "almura:crop/peanut";
            plantAge = 3;
        } else if (plant == 45) {
            plantName = "almura:crop/peppercorn";
            plantAge = 5;
        } else if (plant == 46) {
            plantName = "almura:crop/pineapple";
            plantAge = 3;
        } else if (plant == 47) {
            plantName = "almura:crop/radish";
            plantAge = 4;
        } else if (plant == 48) {
            plantName = "almura:crop/raspberry";
            plantAge = 4;
        } else if (plant == 49) {
            plantName = "almura:crop/rice";
            plantAge = 3;
        } else if (plant == 50) {
            plantName = "almura:crop/rosemary";
            plantAge = 5;
        } else if (plant == 51) {
            plantName = "almura:crop/rye";
            plantAge = 4;
        } else if (plant == 52) {
            plantName = "almura:crop/sorghum";
            plantAge = 5;
        } else if (plant == 53) {
            plantName = "almura:crop/soybean";
            plantAge = 4;
        } else if (plant == 54) {
            plantName = "almura:crop/spinach";
            plantAge = 4;
        } else if (plant == 55) {
            plantName = "almura:crop/strawberry";
            plantAge = 4;
        } else if (plant == 56) {
            plantName = "almura:crop/sugarbeet";
            plantAge = 5;
        } else if (plant == 57) {
            plantName = "almura:crop/sweetpepper";
            plantAge = 4;
        } else if (plant == 58) {
            plantName = "almura:crop/sweetpotato";
            plantAge = 4;
        } else if (plant == 59) {
            plantName = "almura:crop/tarragon";
            plantAge = 5;
        } else if (plant == 60) {
            plantName = "almura:crop/thyme";
            plantAge = 5;
        } else if (plant == 61) {
            plantName = "almura:crop/tobacco";
            plantAge = 4;
        } else if (plant == 62) {
            plantName = "almura:crop/tomato";
            plantAge = 4;
        } else if (plant == 63) {
            plantName = "almura:crop/turnip";
            plantAge = 4;
        } else if (plant == 64) {
            plantName = "almura:crop/white_grape";
            plantAge = 3;
        }

        if (debug) {
            System.out.println("RandomPlantStateGenerator - Crop: " + plantName + " at: " + blockPos);
        }

        final Block block = Block.getBlockFromName(plantName);
        if (block != null) {
            final IBlockState state = block.getStateFromMeta(plantAge);
            return state;
        }
        return null;
    }
    public static IBlockState randomFlower(World worldIn, BlockPos blockPos, Random random) {
        String plantName = "";
        int plantAge = 0;
        int plant = random.nextInt(77); // Total Flowers
        if (plant == 0) {
            plantName = "almura:horizontal/flower/amaryllis";
        } else if (plant == 1) {
            plantName = "almura:horizontal/flower/bladeshrub";
        } else if (plant == 2) {
            plantName = "almura:horizontal/flower/bleedingheart";
        } else if (plant == 3) {
            plantName = "almura:horizontal/flower/bluebells";
        } else if (plant == 4) {
            plantName = "almura:horizontal/flower/bonsai";
        } else if (plant == 5) {
            plantName = "almura:horizontal/flower/bud";
        } else if (plant == 6) {
            plantName = "almura:horizontal/flower/carnation_blue";
        } else if (plant == 7) {
            plantName = "almura:horizontal/flower/carnation_pink";
        } else if (plant == 8) {
            plantName = "almura:horizontal/flower/carnation_purple";
        } else if (plant == 9) {
            plantName = "almura:horizontal/flower/carnation_red";
        } else if (plant == 10) {
            plantName = "almura:horizontal/flower/carnation_yellow";
        } else if (plant == 11) {
            plantName = "almura:horizontal/flower/chrysanthemum";
        } else if (plant == 12) {
            plantName = "almura:horizontal/flower/cosmos";
        } else if (plant == 13) {
            plantName = "almura:horizontal/flower/cyprus";
        } else if (plant == 14) {
            plantName = "almura:horizontal/flower/daffodil";
        } else if (plant == 15) {
            plantName = "almura:horizontal/flower/fairystool";
        } else if (plant == 16) {
            plantName = "almura:horizontal/flower/firehazel";
        } else if (plant == 17) {
            plantName = "almura:horizontal/flower/foxglove";
        } else if (plant == 18) {
            plantName = "almura:horizontal/flower/frostshroom";
        } else if (plant == 19) {
            plantName = "almura:horizontal/flower/frozengrass";
        } else if (plant == 20) {
            plantName = "almura:horizontal/flower/fuzzybrush";
        } else if (plant == 21) {
            plantName = "almura:horizontal/flower/geranium";
        } else if (plant == 22) {
            plantName = "almura:horizontal/flower/goldenlactarius";
        } else if (plant == 23) {
            plantName = "almura:horizontal/flower/goldenrod";
        } else if (plant == 24) {
            plantName = "almura:horizontal/flower/grandrussala";
        } else if (plant == 25) {
            plantName = "almura:horizontal/flower/grayboletus";
        } else if (plant == 26) {
            plantName = "almura:horizontal/flower/heartleaf";
        } else if (plant == 27) {
            plantName = "almura:horizontal/flower/iris";
        } else if (plant == 28) {
            plantName = "almura:horizontal/flower/kaiseragaric";
        } else if (plant == 29) {
            plantName = "almura:horizontal/flower/kingstrumpet";
        } else if (plant == 30) {
            plantName = "almura:horizontal/flower/leafyswamproot";
        } else if (plant == 31) {
            plantName = "almura:horizontal/flower/lily";
        } else if (plant == 32) {
            plantName = "almura:horizontal/flower/mandragora";
        } else if (plant == 33) {
            plantName = "almura:horizontal/flower/meadowparasol";
        } else if (plant == 34) {
            plantName = "almura:horizontal/flower/moneytree";
        } else if (plant == 35) {
            plantName = "almura:horizontal/flower/mountainrose";
        } else if (plant == 36) {
            plantName = "almura:horizontal/flower/mushboom";
        } else if (plant == 37) {
            plantName = "almura:horizontal/flower/nightshade";
        } else if (plant == 38) {
            plantName = "almura:horizontal/flower/orchid";
        } else if (plant == 39) {
            plantName = "almura:horizontal/flower/palm";
        } else if (plant == 40) {
            plantName = "almura:horizontal/flower/pampas_grass";
        } else if (plant == 41) {
            plantName = "almura:horizontal/flower/poppy";
        } else if (plant == 42) {
            plantName = "almura:horizontal/flower/rafflesia";
        } else if (plant == 43) {
            plantName = "almura:horizontal/flower/redbud";
        } else if (plant == 44) {
            plantName = "almura:horizontal/flower/rosebush";
        } else if (plant == 45) {
            plantName = "almura:horizontal/flower/roundedtopiary";
        } else if (plant == 46) {
            plantName = "almura:horizontal/flower/shitake";
        } else if (plant == 47) {
            plantName = "almura:horizontal/flower/shore_grass";
        } else if (plant == 48) {
            plantName = "almura:horizontal/flower/smallfern";
        } else if (plant == 49) {
            plantName = "almura:horizontal/flower/snowdrops";
        } else if (plant == 50) {
            plantName = "almura:horizontal/flower/snowfern";
        } else if (plant == 51) {
            plantName = "almura:horizontal/flower/stoneshroom";
        } else if (plant == 52) {
            plantName = "almura:horizontal/flower/sunflower";
        } else if (plant == 53) {
            plantName = "almura:horizontal/flower/sunnyside";
        } else if (plant == 54) {
            plantName = "almura:horizontal/flower/tallfern";
        } else if (plant == 55) {
            plantName = "almura:horizontal/flower/tallplainsgrass";
        } else if (plant == 56) {
            plantName = "almura:horizontal/flower/thistle";
        } else if (plant == 57) {
            plantName = "almura:horizontal/flower/timidpincussion";
        } else if (plant == 58) {
            plantName = "almura:horizontal/flower/tropicalgrass";
        } else if (plant == 59) {
            plantName = "almura:horizontal/flower/tulip";
        } else if (plant == 60) {
            plantName = "almura:horizontal/flower/violets";
        } else if (plant == 61) {
            plantName = "almura:horizontal/flower/whitecaps";
        } else if (plant == 62) {
            plantName = "almura:horizontal/flower/wildflower_blue";
        } else if (plant == 63) {
            plantName = "almura:horizontal/flower/wildflower_lightblue";
        } else if (plant == 64) {
            plantName = "almura:horizontal/flower/wildflower_pink";
        } else if (plant == 65) {
            plantName = "almura:horizontal/flower/wildflower_purple";
        } else if (plant == 66) {
            plantName = "almura:horizontal/flower/wildflower_red";
        } else if (plant == 67) {
            plantName = "almura:horizontal/flower/wildflower_white";
        } else if (plant == 68) {
            plantName = "almura:horizontal/flower/wildflower_yellow";
        } else if (plant == 69) {
            plantName = "almura:horizontal/flower/witherymorel";
        } else if (plant == 70) {
            plantName = "almura:horizontal/flower/woodears";
        } else if (plant == 71) {
            plantName = "almura:horizontal/flower/zenset";
        } else if (plant == 72) {
            plantName = "almura:horizontal/flower/flower_bonsai";
        } else if (plant == 73) {
            plantName = "almura:horizontal/flower/flower_daisy";
        } else if (plant == 74) {
            plantName = "almura:horizontal/flower/flower_hydrangea";
        } else if (plant == 75) {
            plantName = "almura:horizontal/flower/flower_lotus";
        } else if (plant == 76) {
            plantName = "almura:horizontal/flower/flower_peony";
        } else if (plant == 77) {
            plantName = "almura:horizontal/flower/flower_whiterose";
        }

        if (debug) {
            System.out.println("RandomPlantStateGenerator - Flower: " + plantName + " at: " + blockPos);
        }

        final Block block = Block.getBlockFromName(plantName);
        if (block != null) {
            final IBlockState state = block.getStateFromMeta(plantAge);
            return state;
        }

        return null;
    }

    public static IBlockState randomDesertPlant(World worldIn, BlockPos blockPos, Random random) {
        String plantName = "";
        int plantAge = 0;
        int plant = random.nextInt(10);
        if (plant == 0) {
            plantName = "almura:horizontal/flower/barrelcatcus";
        } else if (plant == 1) {
            plantName = "almura:horizontal/flower/hedgehogcactus";
        } else if (plant == 2) {
            plantName = "almura:horizontal/flower/pricklypear";
        } else if (plant == 3) {
            plantName = "almura:horizontal/flower/saguaro";
        } else if (plant == 4) {
            plantName = "almura:horizontal/flower/cactus_brown";
        } else if (plant == 5) {
            plantName = "almura:horizontal/flower/cactus_flower";
        } else if (plant == 6) {
            plantName = "almura:horizontal/flower/cactus_green";
        } else if (plant == 7) {
            plantName = "almura:horizontal/flower/cactus_greensml";
        } else if (plant == 8) {
            plantName = "almura:horizontal/flower/cactus_greensplit";
        } else if (plant == 9) {
            plantName = "almura:horizontal/flower/cactus_olivesplit";
        } else if (plant == 10) {
            plantName = "almura:horizontal/flower/flower_cactus";
        }


        if (debug) {
            System.out.println("RandomPlantStateGenerator - Desert Plant: " + plantName + " at: " + blockPos);
        }

        final Block block = Block.getBlockFromName(plantName);
        if (block != null) {
            final IBlockState state = block.getStateFromMeta(plantAge);
            return state;
        }

        return null;
    }

    public static IBlockState randomIceFlower(World worldIn, BlockPos blockPos, Random random) {
        String plantName = "";
        int plantAge = 0;
        int plant = random.nextInt(1);
        if (plant == 0 || plant == 1) {
            plantName = "almura:horizontal/flower/icerose";
        }

        if (debug) {
            System.out.println("RandomPlantStateGenerator - Ice Flower: " + plantName + " at: " + blockPos);
        }

        final Block block = Block.getBlockFromName(plantName);
        if (block != null) {
            final IBlockState state = block.getStateFromMeta(plantAge);
            return state;
        }

        return null;
    }
}
