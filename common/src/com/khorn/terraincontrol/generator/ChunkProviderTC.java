package com.khorn.terraincontrol.generator;

import com.khorn.terraincontrol.DefaultMaterial;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.biomelayers.ArraysCacheManager;
import com.khorn.terraincontrol.biomelayers.layers.Layer;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.configuration.WorldConfig;
import com.khorn.terraincontrol.generator.terrainsgens.CanyonsGen;
import com.khorn.terraincontrol.generator.terrainsgens.CavesGen;
import com.khorn.terraincontrol.generator.terrainsgens.TerrainGenBase;
import com.khorn.terraincontrol.util.MathHelper;
import com.khorn.terraincontrol.util.NoiseGeneratorOctaves;

import java.util.Random;


@SuppressWarnings({"PointlessArithmeticExpression"})
public class ChunkProviderTC
{
    private Random rnd;
    private NoiseGeneratorOctaves o;
    private NoiseGeneratorOctaves p;
    private NoiseGeneratorOctaves q;
    private NoiseGeneratorOctaves r;
    private NoiseGeneratorOctaves a;
    private NoiseGeneratorOctaves b;
    private double[] u;
    private double[] v = new double[256];

    double[] g;
    double[] h;
    double[] i;
    double[] j;
    double[] k;
    float[] NearBiomeWeight;

    private static int ChunkMaxX = 16;
    private static int ChunkMaxZ = 16;


    private LocalWorld localWorld;
    private double VolatilityFactor;
    private double HeightFactor;

    private WorldConfig worldSettings;
    private TerrainGenBase CaveGen;


    private TerrainGenBase CanyonGen;

    private int[] BiomeArray;

    private int height;
    private int heightBits;
    private int heightBitsPlusFour;
    private int heightMinusOne;


    public ChunkProviderTC(WorldConfig config, LocalWorld world)
    {
        this.worldSettings = config;
        this.localWorld = world;
        this.height = world.getHeight();
        this.heightBits = world.getHeightBits();
        this.heightBitsPlusFour = this.heightBits + 4;
        this.heightMinusOne = this.height - 1;


        this.rnd = new Random(world.getSeed());

        this.o = new NoiseGeneratorOctaves(this.rnd, 16);
        this.p = new NoiseGeneratorOctaves(this.rnd, 16);
        this.q = new NoiseGeneratorOctaves(this.rnd, 8);
        this.r = new NoiseGeneratorOctaves(this.rnd, 4);

        this.a = new NoiseGeneratorOctaves(this.rnd, 10);
        this.b = new NoiseGeneratorOctaves(this.rnd, 16);


        this.CaveGen = new CavesGen(this.worldSettings, this.localWorld);
        this.CanyonGen = new CanyonsGen(this.worldSettings, this.localWorld);


        // Contains 2d array 5*5.  Maximum wight is in array center.
        this.NearBiomeWeight = new float[25];
        for (int x = -2; x <= 2; x++)
        {
            for (int z = -2; z <= 2; z++)
            {
                float f1 = 10.0F / MathHelper.sqrt((float) (x * x + z * z) + 0.2F);
                this.NearBiomeWeight[(x + 2 + (z + 2) * 5)] = f1;
            }
        }


    }

    private void generateTerrain(int chunkX, int chunkZ, byte[] paramArrayOfByte)
    {
        int i1 = 4;
        int i2 = this.height / 8;

        int noise_xSize = i1 + 1;
        int noise_ySize = this.height / 8 + 1;
        int noise_zSize = i1 + 1;
        ArraysCacheManager.NextRiver = true;
        if (this.worldSettings.biomeMode == TerrainControl.getBiomeModeManager().OLD_GENERATOR)
        {
            this.BiomeArray = this.localWorld.getBiomesUnZoomed(this.BiomeArray, chunkX * 16, chunkZ * 16, 16, 16);
        } else
            this.BiomeArray = this.localWorld.getBiomesUnZoomed(this.BiomeArray, chunkX * 4 - 2, chunkZ * 4 - 2, noise_xSize + 5, noise_zSize + 5);

        this.u = GenerateTerrainNoise(this.u, chunkX * i1, 0, chunkZ * i1, noise_xSize, noise_ySize, noise_zSize);

        this.BiomeArray = this.localWorld.getBiomes(this.BiomeArray, chunkX * 16, chunkZ * 16, ChunkMaxX, ChunkMaxZ);

        double d1 = 0.125D;
        double d10 = 0.25D;
        int z_step = 1 << this.heightBits;
        double d15 = 0.25D;

        for (int x = 0; x < i1; x++)
            for (int z = 0; z < i1; z++)
                for (int y = 0; y < i2; y++)
                {

                    double d2 = this.u[(((x + 0) * noise_zSize + (z + 0)) * noise_ySize + (y + 0))];
                    double d3 = this.u[(((x + 0) * noise_zSize + (z + 1)) * noise_ySize + (y + 0))];
                    double d4 = this.u[(((x + 1) * noise_zSize + (z + 0)) * noise_ySize + (y + 0))];
                    double d5 = this.u[(((x + 1) * noise_zSize + (z + 1)) * noise_ySize + (y + 0))];

                    double d6 = (this.u[(((x + 0) * noise_zSize + (z + 0)) * noise_ySize + (y + 1))] - d2) * d1;
                    double d7 = (this.u[(((x + 0) * noise_zSize + (z + 1)) * noise_ySize + (y + 1))] - d3) * d1;
                    double d8 = (this.u[(((x + 1) * noise_zSize + (z + 0)) * noise_ySize + (y + 1))] - d4) * d1;
                    double d9 = (this.u[(((x + 1) * noise_zSize + (z + 1)) * noise_ySize + (y + 1))] - d5) * d1;

                    for (int piece_y = 0; piece_y < 8; piece_y++)
                    {


                        double d11 = d2;
                        double d12 = d3;
                        double d13 = (d4 - d2) * d10;
                        double d14 = (d5 - d3) * d10;

                        for (int piece_x = 0; piece_x < 4; piece_x++)
                        {
                            int position = (piece_x + x * 4) << this.heightBitsPlusFour | (0 + z * 4) << this.heightBits | (y * 8 + piece_y);

                            double d16 = d11;
                            double d17 = (d12 - d11) * d15;
                            for (int piece_z = 0; piece_z < 4; piece_z++)
                            {
                                int biomeId = BiomeArray[(z * 4 + piece_z) * 16 + (piece_x + x * 4)];
                                int i15 = 0;
                                if (y * 8 + piece_y < this.worldSettings.biomeConfigs[biomeId].waterLevelMax && y * 8 + piece_y > this.worldSettings.biomeConfigs[biomeId].waterLevelMin)
                                {
                                    i15 = this.worldSettings.biomeConfigs[biomeId].waterBlock;
                                }

                                if (d16 > 0.0D)
                                {
                                    i15 = DefaultMaterial.STONE.id;
                                }

                                paramArrayOfByte[position] = (byte) i15;
                                position += z_step;
                                d16 += d17;
                            }
                            d11 += d13;
                            d12 += d14;
                        }

                        d2 += d6;
                        d3 += d7;
                        d4 += d8;
                        d5 += d9;
                    }
                }

    }

    boolean ReplaceForBiomeAndReturnWaterless(int paramInt1, int paramInt2, byte[] paramArrayOfByte)
    {

        int dryBlock = 256;

        double d1 = 0.03125D;
        this.v = this.r.Noise3D(this.v, paramInt1 * 16, paramInt2 * 16, 0, 16, 16, 1, d1 * 2.0D, d1 * 2.0D, d1 * 2.0D);
        float[] TemperatureArray = this.localWorld.getTemperatures(paramInt1 * 16, paramInt2 * 16, 16, 16);


        for (int x = 0; x < 16; x++)
        {
            for (int z = 0; z < 16; z++)
            {
                float temperature = TemperatureArray[(z + x * 16)];

                int biomeId = BiomeArray[(z + x * 16)];
                int stone_noise = (int) (this.v[(x + z * 16)] / 3.0D + 3.0D + this.rnd.nextDouble() * 0.25D);

                int i5 = -1;

                BiomeConfig biomeConfig = this.worldSettings.biomeConfigs[biomeId];
                int surfaceBlock = biomeConfig.SurfaceBlock;
                int groundBlock = biomeConfig.GroundBlock;
                int waterLevel = biomeConfig.waterLevelMax;

                if (this.worldSettings.ceilingBedrock)
                {
                    // Moved one block lower to fix lighting issues
                    paramArrayOfByte[(z * 16 + x) * this.height + this.heightMinusOne - 1] = (byte) this.worldSettings.bedrockBlock;
                }

                for (int y = this.heightMinusOne; y >= 0; y--)
                {
                    int i9 = (z * 16 + x) * this.height + y;

                    if (y < 5 && (worldSettings.createAdminium(y)) && y <= this.rnd.nextInt(5))
                    {
                        paramArrayOfByte[i9] = (byte) this.worldSettings.bedrockBlock;
                    } else
                    {
                        int i10 = paramArrayOfByte[i9];

                        if (i10 == 0)
                            i5 = -1;
                        else if (i10 == DefaultMaterial.STONE.id)
                            if (i5 == -1)
                            {
                                if (stone_noise <= 0 && !this.worldSettings.removeSurfaceStone)
                                {
                                    surfaceBlock = 0;
                                    groundBlock = (byte) DefaultMaterial.STONE.id;
                                } else if ((y >= waterLevel - 4) && (y <= waterLevel + 1))
                                {
                                    surfaceBlock = biomeConfig.SurfaceBlock;
                                    groundBlock = biomeConfig.GroundBlock;
                                }

                                if ((y < waterLevel) && (y > this.worldSettings.waterLevelMin) && (surfaceBlock == 0))
                                {
                                    if (temperature < 0.15F)
                                        surfaceBlock = (byte) biomeConfig.iceBlock;
                                    else
                                        surfaceBlock = (byte) biomeConfig.waterBlock;
                                }

                                i5 = stone_noise;
                                if (y >= waterLevel - 1)
                                    paramArrayOfByte[i9] = (byte) surfaceBlock;
                                else
                                    paramArrayOfByte[i9] = (byte) groundBlock;


                            } else if (i5 > 0)
                            {
                                i5--;
                                paramArrayOfByte[i9] = (byte) groundBlock;

                                if ((i5 == 0) && (groundBlock == DefaultMaterial.SAND.id))
                                {
                                    i5 = this.rnd.nextInt(4);
                                    groundBlock = (byte) DefaultMaterial.SANDSTONE.id;
                                }
                            }
                    }
                }
                if (paramArrayOfByte[(z * 16 + x) * this.height + biomeConfig.waterLevelMax] == biomeConfig.waterBlock)
                    dryBlock--;
            }
        }

        return dryBlock > 250;
    }


    private double[] GenerateTerrainNoise(double[] outArray, int xOffset, int yOffset, int zOffset, int max_X, int max_Y, int max_Z)
    {
        if (outArray == null)
        {
            outArray = new double[max_X * max_Y * max_Z];
        }


        double xzScale = 684.41200000000003D * this.worldSettings.getFractureHorizontal();
        double yScale = 684.41200000000003D * this.worldSettings.getFractureVertical();

        if (this.worldSettings.oldTerrainGenerator)
            this.j = this.a.Noise2D(this.j, xOffset, zOffset, max_X, max_Z, 1.121D, 1.121D);
        this.k = this.b.Noise2D(this.k, xOffset, zOffset, max_X, max_Z, 200.0D, 200.0D);

        this.g = this.q.Noise3D(this.g, xOffset, yOffset, zOffset, max_X, max_Y, max_Z, xzScale / 80.0D, yScale / 160.0D, xzScale / 80.0D);
        this.h = this.o.Noise3D(this.h, xOffset, yOffset, zOffset, max_X, max_Y, max_Z, xzScale, yScale, xzScale);
        this.i = this.p.Noise3D(this.i, xOffset, yOffset, zOffset, max_X, max_Y, max_Z, xzScale, yScale, xzScale);

        int i3D = 0;
        int i2D = 0;

        for (int x = 0; x < max_X; x++)
        {
            for (int z = 0; z < max_Z; z++)
            {

                int biomeId = (this.BiomeArray[(x + 2 + (z + 2) * (max_X + 5))] | Layer.RiverBits) ^ Layer.RiverBits ;
                BiomeConfig biomeConfig = this.worldSettings.biomeConfigs[biomeId];

                double noiseHeight = this.k[i2D] / 8000.0D;
                if (noiseHeight < 0.0D)
                    noiseHeight = -noiseHeight * 0.3D;
                noiseHeight = noiseHeight * 3.0D - 2.0D;

                if (noiseHeight < 0.0D)
                {
                    noiseHeight /= 2.0D;
                    if (noiseHeight < -1.0D)
                        noiseHeight = -1.0D;
                    noiseHeight -= biomeConfig.maxAverageDepth;
                    noiseHeight /= 1.4D;
                    noiseHeight /= 2.0D;
                } else
                {
                    if (noiseHeight > 1.0D)
                        noiseHeight = 1.0D;
                    noiseHeight += biomeConfig.maxAverageHeight;
                    noiseHeight /= 8.0D;
                }

                if (this.worldSettings.oldTerrainGenerator)
                    this.oldTerrainNoise(x, z, i2D, max_X, max_Y, noiseHeight);
                else
                    this.newTerrainNoise(x, z, max_X, max_Y, noiseHeight);


                i2D++;


                for (int y = 0; y < max_Y; y++)
                {
                    double d7;

                    double d8 = (HeightFactor - y) * 12.0D * 128.0D / this.height / VolatilityFactor;

                    if (d8 > 0.0D)
                        d8 *= 4.0D;

                    double d9 = this.h[i3D] / 512.0D * biomeConfig.volatility1;
                    double d10 = this.i[i3D] / 512.0D * biomeConfig.volatility2;

                    double d11 = (this.g[i3D] / 10.0D + 1.0D) / 2.0D;
                    if (d11 < biomeConfig.volatilityWeight1)
                        d7 = d9;
                    else if (d11 > biomeConfig.volatilityWeight2)
                        d7 = d10;
                    else
                        d7 = d9 + (d10 - d9) * d11;

                    if (!biomeConfig.disableNotchHeightControl)
                    {
                        d7 += d8;

                        if (y > max_Y - 4)
                        {
                            double d12 = (y - (max_Y - 4)) / 3.0F;
                            d7 = d7 * (1.0D - d12) + -10.0D * d12;
                        }

                    }
                    d7 += biomeConfig.heightMatrix[y];

                    outArray[i3D] = d7;
                    i3D++;
                }
            }
        }
        return outArray;

    }

    private void oldTerrainNoise(int x, int z, int i4, int max_X, int max_Y, double noiseHeight)
    {
        if (this.worldSettings.biomeMode == TerrainControl.getBiomeModeManager().OLD_GENERATOR)
        {
            this.VolatilityFactor = (1.0D - localWorld.getBiomeFactorForOldBM(z * 48 + 17 + x * 3));

        } else
        {
            int biomeId = this.BiomeArray[(x + 2 + (z + 2) * (max_X + 5))];
            this.VolatilityFactor = (1.0D - worldSettings.biomeConfigs[biomeId].BiomeTemperature * worldSettings.biomeConfigs[biomeId].BiomeWetness);
        }
        this.VolatilityFactor *= this.VolatilityFactor;
        this.VolatilityFactor = 1.0D - this.VolatilityFactor * this.VolatilityFactor;

        this.VolatilityFactor = (this.g[i4] + 256.0D) / 512.0D * this.VolatilityFactor;
        if (this.VolatilityFactor > 1.0D)
            this.VolatilityFactor = 1.0D;
        if (this.VolatilityFactor < 0.0D || noiseHeight < 0.0D)
            this.VolatilityFactor = 0.0D;

        this.VolatilityFactor += 0.5D;
        this.HeightFactor = max_Y * (2.0D + noiseHeight) / 4.0D;
    }

    private void newTerrainNoise(int x, int z, int max_X, int max_Y, double noiseHeight)
    {
        float volatilitySum = 0.0F;
        float heightSum = 0.0F;
        float biomeWeightSum = 0.0F;

        // TODO We may change that ???!!
        int lookRadius = 2;

        int biomeId = this.BiomeArray[(x + 2 + (z + 2) * (max_X + 5))];
        boolean isRiver = (biomeId & Layer.RiverBits) > 0;
        biomeId = (biomeId | Layer.RiverBits) ^ Layer.RiverBits;


        for (int nextX = -lookRadius; nextX <= lookRadius; nextX++)
        {
            for (int nextZ = -lookRadius; nextZ <= lookRadius; nextZ++)
            {
                int nextBiomeId = this.BiomeArray[(x + nextX + 2 + (z + nextZ + 2) * (max_X + 5))];


                BiomeConfig nextBiomeConfig = this.worldSettings.biomeConfigs[(nextBiomeId| Layer.RiverBits)^Layer.RiverBits];

                float nextBiomeHeight =  nextBiomeConfig.BiomeHeight -  (((nextBiomeId & Layer.RiverBits) > 0)? 1.0F:0.0F);

                float biomeWeight = this.NearBiomeWeight[(nextX + 2 + (nextZ + 2) * 5)] / (nextBiomeHeight + 2.0F);
                biomeWeight = Math.abs(biomeWeight);
                if (nextBiomeHeight > (this.worldSettings.biomeConfigs[biomeId].BiomeHeight - (isRiver? 1.0F: 0.0F)))
                {
                    biomeWeight /= 2.0F;
                }
                volatilitySum += nextBiomeConfig.BiomeVolatility * biomeWeight;
                heightSum += nextBiomeHeight * biomeWeight;
                biomeWeightSum += biomeWeight;
            }
        }
        volatilitySum /= biomeWeightSum;
        heightSum /= biomeWeightSum;

        volatilitySum = volatilitySum * 0.9F + 0.1F;
        heightSum = (heightSum * 4.0F - 1.0F) / 8.0F;

        this.VolatilityFactor = volatilitySum;
        this.HeightFactor = max_Y * (2.0D + heightSum + noiseHeight * 0.2D) / 4.0D;
    }


    public byte[] generate(int x, int z)
    {

        this.rnd.setSeed(x * 341873128712L + z * 132897987541L);

        byte[] arrayOfByte = new byte[ChunkMaxX * this.height * ChunkMaxZ];


        generateTerrain(x, z, arrayOfByte);

        //this.BiomeArray = this.localWorld.getBiomes(this.BiomeArray, x * 16, z * 16, ChunkMaxX, ChunkMaxZ);  now get it in generateTerrain

        boolean dry = ReplaceForBiomeAndReturnWaterless(x, z, arrayOfByte);

        this.CaveGen.a(x, z, arrayOfByte);
        this.CanyonGen.a(x, z, arrayOfByte);

        if (this.worldSettings.ModeTerrain == WorldConfig.TerrainMode.Normal || this.worldSettings.ModeTerrain == WorldConfig.TerrainMode.OldGenerator)
            this.localWorld.PrepareTerrainObjects(x, z, arrayOfByte, dry);

        if (this.worldSettings.isDeprecated)
            this.worldSettings = this.worldSettings.newSettings;

        return arrayOfByte;

    }

}
