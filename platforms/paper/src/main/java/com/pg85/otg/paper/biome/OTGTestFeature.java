package com.pg85.otg.paper.biome;

import com.mojang.serialization.Codec;
import com.pg85.otg.core.OTG;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.CountConfiguration;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.RarityFilter;

import java.util.Random;

public class OTGTestFeature extends Feature<CountConfiguration> {

    public static final Feature<CountConfiguration> FEATURE = Registry.register(Registry.FEATURE, "otg_test", new OTGTestFeature(CountConfiguration.CODEC));
    public static final ConfiguredFeature<?, ?> CONFIGURED = Registry
            .register(
                    BuiltinRegistries.CONFIGURED_FEATURE,
                    "otg_test",
                    FEATURE.configured(new CountConfiguration(20)));
    public static final PlacedFeature PLACED = Registry
            .register(
                    BuiltinRegistries.PLACED_FEATURE,
                    "otg_test",
                    CONFIGURED.placed(RarityFilter.onAverageOnceEvery(16), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_TOP_SOLID, BiomeFilter.biome()));

    public OTGTestFeature(Codec<CountConfiguration> configCodec) {
        super(configCodec);
    }

    /*
    Code modified from SeaPickleFeature
    */
    @Override
    public boolean place(FeaturePlaceContext<CountConfiguration> context) {
        int i = 0;
        Random random = context.random();
        WorldGenLevel worldGenLevel = context.level();
        BlockPos blockPos = context.origin();
        int j = context.config().count().sample(random);
        OTG.getEngine().getLogger().log(LogLevel.INFO, LogCategory.MAIN, "OTG Test Resource is being placed");
        for(int k = 0; k < j; ++k) {
            int l = random.nextInt(8) - random.nextInt(8);
            int m = random.nextInt(8) - random.nextInt(8);
            int n = worldGenLevel.getHeight(Heightmap.Types.OCEAN_FLOOR, blockPos.getX() + l, blockPos.getZ() + m);
            BlockPos blockPos2 = new BlockPos(blockPos.getX() + l, n, blockPos.getZ() + m);
            BlockState blockState = Blocks.SEA_PICKLE.defaultBlockState().setValue(SeaPickleBlock.PICKLES, Integer.valueOf(random.nextInt(4) + 1));
            if (worldGenLevel.getBlockState(blockPos2).is(Blocks.WATER) && blockState.canSurvive(worldGenLevel, blockPos2)) {
                worldGenLevel.setBlock(blockPos2, blockState, 2);
                ++i;
            }
        }

        return i > 0;
    }
}

