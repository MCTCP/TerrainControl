package com.pg85.otg.util.minecraft;

public class LegacyRegistry {
    /*
    * I've created this class to bridge 1.16 Registry() keywords
    * In 1.18, they renamed many of the Registry() resource locations
    * This class is not intended as a perfect conversion,
    * but we can do our best.
    * If something looks completely off and there is a better way to
    * do this, please DM me on Discord.
    * - Frank
     */
    public static String convertLegacyResourceLocation(String old) {
        old = old.replace("minecraft:", "");
        switch (old) {
            case "glowstone":
                return "minecraft:glowstone_extra";
            case "warped_fungi":
                return "minecraft:warped_fungus";
            case "spring_delta":
                return "minecraft:delta";
            case "crimson_fungi":
                return "crimson_fungus";
            case "birch_other":
                return "minecraft:trees_birch_and_oak";
            case "seagrass_swamp":
            case "seagrass_deep_warm":
            case "seagrass_warm":
            case "seagrass_cold":
            case "seagrass_river":
                return "minecraft:seagrass_simple";
            case "patch_berry_sparse":
                return "minecraft:patch_berry_bush";
            case "bamboo_light":
                return "minecraft:bamboo_no_podzol";
            case "plain_vegetation":
                return "minecraft:trees_plains";
            case "kelp_warm":
            case "kelp_cold":
                return "minecraft:kelp";
            case "forest_flower_trees":
                return "minecraft:trees_flower_forest";
            case "bamboo":
                return "minecraft:bamboo_some_podzol";
            default:
                return null;
        }
    }
}
