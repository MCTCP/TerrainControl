package com.pg85.otg.gen.gen;

import com.pg85.otg.gen.noise.DoublePerlinNoiseSampler;
import com.pg85.otg.util.helpers.MathHelper;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterials;

import java.util.Random;

public final class OreVeinGenerator
{
    private final Random random;
    private final long seed;
    private final DoublePerlinNoiseSampler veininessNoiseSource;
    private final DoublePerlinNoiseSampler veinANoiseSource;
    private final DoublePerlinNoiseSampler veinBNoiseSource;
    private final DoublePerlinNoiseSampler gapNoise;

    public OreVeinGenerator(long seed) {
        this.random = new Random(seed);
        this.seed = seed;
        this.veininessNoiseSource = DoublePerlinNoiseSampler.create(new Random(random.nextLong()), -8, 1.0D);
        this.veinANoiseSource = DoublePerlinNoiseSampler.create(new Random(random.nextLong()), -7, 1.0D);
        this.veinBNoiseSource = DoublePerlinNoiseSampler.create(new Random(random.nextLong()), -7, 1.0D);
        this.gapNoise = DoublePerlinNoiseSampler.create(new Random(random.nextLong()), -5, 1.0D);
    }

    public OreVeinData getForChunk(int chunkX, int chunkZ) {
        setBaseStoneSeed(this.random, this.seed, chunkX * 16, 0, chunkZ * 16);

        return new OreVeinData(
                fill(chunkX, chunkZ, this.veininessNoiseSource, 1.5),
                fill(chunkX, chunkZ, this.veinANoiseSource, 4.0),
                fill(chunkX, chunkZ, this.veinBNoiseSource, 4.0)
        );
    }

    private double[][][] fill(int chunkX, int chunkZ, DoublePerlinNoiseSampler sampler, double scale) {
        double[][][] buffer = new double[5][5][33];

        for (int x = 0; x < 5; x++)
        {
            for (int z = 0; z < 5; z++)
            {
                fillNoiseColumn(buffer[x][z], chunkX * 4 + x, chunkZ * 4 + z, sampler, scale, 0, 33);
            }
        }

        return buffer;
    }

    private void fillNoiseColumn(double[] buffer, int x, int z, DoublePerlinNoiseSampler sampler, double scale, int minY, int noiseSizeY) {
        for(int i = 0; i < noiseSizeY; ++i) {
            int j = i + minY;
            int k = x * 4;
            int l = j * 8;
            int m = z * 4;
            double d;
            // TODO: correct y value controls for l
            if (l >= 0 && l <= 72) {
                d = sampler.sample((double)k * scale, (double)l * scale, (double)m * scale);
            } else {
                d = 0.0D;
            }

            buffer[i] = d;
        }

    }

    private VeinType getVeinType(double oreFrequencyNoise, int y) {
        VeinType veinType = oreFrequencyNoise > 0.0D ? VeinType.COPPER : VeinType.IRON;
        int i = veinType.maxY - y;
        int j = y - veinType.minY;
        if (j >= 0 && i >= 0) {
            int k = Math.min(i, j);
            double d = MathHelper.clampedMap((double)k, 0.0D, 20.0D, -0.2D, 0.0D);
            return Math.abs(oreFrequencyNoise) + d < 0.5D ? null : veinType;
        } else {
            return null;
        }
    }

    private boolean isVein(double firstOrePlacementNoise, double secondOrePlacementNoise) {
        double d = Math.abs(1.0D * firstOrePlacementNoise) - (double)0.08F;
        double e = Math.abs(1.0D * secondOrePlacementNoise) - (double)0.08F;
        return Math.max(d, e) < 0.0D;
    }

    private LocalMaterialData oreVeinify(Random random, int x, int y, int z, double veininess, double veinA, double veinB) {
        LocalMaterialData blockState = null;
        VeinType veinType = this.getVeinType(veininess, y);

        if (veinType == null) {
            return blockState;
        } else if (random.nextFloat() > 0.7F) {
            return blockState;
        } else if (this.isVein(veinA, veinB)) {
            double d = MathHelper.clampedMap(Math.abs(veininess), 0.5D, (double)0.6F, (double)0.1F, (double)0.3F);
            if ((double)random.nextFloat() < d && this.gapNoise.sample((double)x, (double)y, (double)z) > (double)-0.3F) {
                return random.nextFloat() < 0.02F ? veinType.rawOreBlock : veinType.ore;
            } else {
                return veinType.filler;
            }
        } else {
            return blockState;
        }
    }

    public LocalMaterialData getMaterial(int realX, int realY, int realZ, int x, int y, int z, double xLerp, double yLerp, double zLerp, OreVeinData data) {
        double veininess = lerp(x, y, z, xLerp, yLerp, zLerp, data.veininess());
        double veinA = lerp(x, y, z, xLerp, yLerp, zLerp, data.veinA());
        double veinB = lerp(x, y, z, xLerp, yLerp, zLerp, data.veinB());

        return oreVeinify(this.random, realX, realY, realZ, veininess, veinA, veinB);
    }

    private long setBaseStoneSeed(Random random, long worldSeed, int x, int y, int z) {
        random.setSeed(worldSeed);
        long l = random.nextLong();
        long m = random.nextLong();
        long n = random.nextLong();
        long o = (long)x * l ^ (long)y * m ^ (long)z * n ^ worldSeed;
        random.setSeed(o);

        return o;
    }
    
    private double lerp(int x, int y, int z, double xLerp, double yLerp, double zLerp, double[][][] data) {
        // Lower samples
        double x0z0y0 = data[x][z][y];
        double x0z1y0 = data[x][z + 1][y];
        double x1z0y0 = data[x + 1][z][y];
        double x1z1y0 = data[x + 1][z + 1][y];
        // Upper samples
        double x0z0y1 = data[x][z][y + 1];
        double x0z1y1 = data[x][z + 1][y + 1];
        double x1z0y1 = data[x + 1][z][y + 1];
        double x1z1y1 = data[x + 1][z + 1][y + 1];

        return MathHelper.lerp3(
                yLerp, xLerp, zLerp,
                x0z0y0, x0z0y1, x1z0y0, x1z0y1,
                x0z1y0, x0z1y1, x1z1y0, x1z1y1
        );
    }

    private enum VeinType {
        COPPER(LocalMaterials.COPPER_ORE, LocalMaterials.RAW_COPPER_BLOCK, LocalMaterials.GRANITE, 36, 72),
        IRON(LocalMaterials.IRON_ORE, LocalMaterials.RAW_IRON_BLOCK, LocalMaterials.TUFF, 0, 32);

        private final LocalMaterialData ore;
        private final LocalMaterialData rawOreBlock;
        private final LocalMaterialData filler;
        private final int minY;
        private final int maxY;

        VeinType(LocalMaterialData ore, LocalMaterialData rawBlock, LocalMaterialData stone, int minY, int maxY) {
            this.ore = ore;
            this.rawOreBlock = rawBlock;
            this.filler = stone;
            this.minY = minY;
            this.maxY = maxY;
        }
    }
}
