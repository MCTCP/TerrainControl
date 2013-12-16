package com.khorn.terraincontrol.generator.surfacegens;

import com.khorn.terraincontrol.DefaultMaterial;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.configuration.BiomeConfig;
import com.khorn.terraincontrol.generator.noise.NoiseGeneratorNewOctaves;
import com.khorn.terraincontrol.util.MathHelper;

import java.util.Arrays;
import java.util.Random;

public class MesaSurfaceGenerator implements SurfaceGenerator
{
    public static final String NAME_NORMAL = "Mesa";
    public static final String NAME_FOREST = "MesaForest";
    public static final String NAME_BRYCE = "MesaBryce";

    /**
     * Returns the mesa surface generator representing the setting value. If
     * no mes surface generator represents this setting value, null is
     * returned.
     * 
     * @param settingValue The setting value.
     * @return The mesa surface generator, or null if not found.
     */
    public static MesaSurfaceGenerator getFor(String settingValue)
    {
        if (NAME_NORMAL.equalsIgnoreCase(settingValue))
        {
            return new MesaSurfaceGenerator(false, false);
        }
        if (NAME_FOREST.equalsIgnoreCase(settingValue))
        {
            return new MesaSurfaceGenerator(false, true);
        }
        if (NAME_BRYCE.equalsIgnoreCase(settingValue))
        {
            return new MesaSurfaceGenerator(true, false);
        }
        return null;
    }

    private byte[] blockDataValuesArray;
    private boolean isForestMesa;
    private boolean isBryceMesa;
    private NoiseGeneratorNewOctaves noiseGenBryce1;
    private NoiseGeneratorNewOctaves noiseGenBryce2;
    private NoiseGeneratorNewOctaves noiseGenBlockData;

    private Random random;

    public MesaSurfaceGenerator(boolean mountainMesa, boolean forestMesa)
    {
        this.random = new Random();
        this.isBryceMesa = mountainMesa;
        this.isForestMesa = forestMesa;
    }

    private byte getBlockData(int i, int j, int k)
    {
        int l = (int) Math.round(this.noiseGenBlockData.a((double) i * 1.0D / 512.0D, (double) i * 1.0D / 512.0D) * 2.0D);

        return this.blockDataValuesArray[(j + l + 64) % 64];
    }

    private void initializeSmallByteArray(long worldSeed)
    {
        this.blockDataValuesArray = new byte[64];
        Arrays.fill(this.blockDataValuesArray, (byte) 16);
        Random random = new Random(worldSeed);

        this.noiseGenBlockData = new NoiseGeneratorNewOctaves(random, 1);

        for (int i = 0; i < 64; ++i)
        {
            i += random.nextInt(5) + 1;
            if (i < 64)
            {
                this.blockDataValuesArray[i] = 1;
            }
        }

        int maxValues = random.nextInt(4) + 2;
        for (int i = 0; i < maxValues; i++)
        {
            int oneTwoOrThree = random.nextInt(3) + 1;
            int numberBelow64 = random.nextInt(64);

            for (int j = 0; numberBelow64 + j < 64 && j < oneTwoOrThree; j++)
            {
                this.blockDataValuesArray[numberBelow64 + j] = 4;
            }
        }

        int numberFromTwoToFive = random.nextInt(4) + 2;
        for (int i = 0; i < numberFromTwoToFive; i++)
        {
            int twoThreeOrFour = random.nextInt(3) + 2;
            int numberBelow64 = random.nextInt(64);

            for (int j = 0; numberBelow64 + j < 64 && j < twoThreeOrFour; j++)
            {
                this.blockDataValuesArray[numberBelow64 + j] = 12;
            }
        }

        // Give another value
        numberFromTwoToFive = random.nextInt(4) + 2;
        for (int i = 0; i < numberFromTwoToFive; i++)
        {
            int oneTwoOrThree = random.nextInt(3) + 1;
            int numberBelow64 = random.nextInt(64);

            for (int j = 0; numberBelow64 + j < 64 && j < oneTwoOrThree; j++)
            {
                this.blockDataValuesArray[numberBelow64 + j] = 14;
            }
        }

        int numberFromThreeToFive = random.nextInt(3) + 3;
        int increasingNumber = 0;

        for (int i = 0; i < numberFromThreeToFive; i++)
        {
            increasingNumber += random.nextInt(16) + 4;

            for (int j = 0; increasingNumber + j < 64 && j < 1; j++)
            {
                this.blockDataValuesArray[increasingNumber + j] = 0;
                if (increasingNumber + j > 1 && random.nextBoolean())
                {
                    this.blockDataValuesArray[increasingNumber + j - 1] = 8;
                }

                if (increasingNumber + j < 63 && random.nextBoolean())
                {
                    this.blockDataValuesArray[increasingNumber + j + 1] = 8;
                }
            }
        }
    }

    @Override
    public void spawn(LocalWorld world, double noise, int x, int z)
    {
        random.setSeed(world.getSeed() ^ x ^ z);
        if (this.blockDataValuesArray == null)
        {
            this.initializeSmallByteArray(world.getSeed());
        }

        BiomeConfig biomeConfig = world.getSettings().biomeConfigs[world.getBiomeId(x, z)];
        int biomeSurfaceBlock = biomeConfig.surfaceBlock;
        int biomeGroundBlock = biomeConfig.groundBlock;
        // The following line prevents grass growing on dirt, but still allows
        // the user to change the ground block to something else without us
        // changing the block data
        int biomeGroundBlockData = biomeGroundBlock == DefaultMaterial.DIRT.id ? 1 : 0;

        // Bryce spike calculations
        double bryceHeight = 0.0D;
        if (this.isBryceMesa)
        {
            if (this.noiseGenBryce1 == null || this.noiseGenBryce2 == null)
            {
                Random newRandom = new Random(world.getSeed());

                this.noiseGenBryce1 = new NoiseGeneratorNewOctaves(newRandom, 4);
                this.noiseGenBryce2 = new NoiseGeneratorNewOctaves(newRandom, 1);
            }

            int k = (x & 0xFFFFFFF0) + (z & 0xF);
            int l = (z & 0xFFFFFFF0) + (x & 0xF);
            double bryceNoiseValue = Math.min(Math.abs(noise), this.noiseGenBryce1.a((double) k * 0.25D, (double) l * 0.25D));

            if (bryceNoiseValue > 0.0D)
            {
                double d3 = 0.001953125D;
                double d4 = Math.abs(this.noiseGenBryce2.a((double) k * d3, (double) l * d3));

                bryceHeight = bryceNoiseValue * bryceNoiseValue * 2.5D;
                double d5 = Math.ceil(d4 * 50.0D) + 14.0D;

                if (bryceHeight > d5)
                {
                    bryceHeight = d5;
                }

                bryceHeight += 64.0D;
            }
        }

        int currentGroundBlock = DefaultMaterial.STAINED_CLAY.id;
        int noisePlusRandomFactor = (int) (noise / 3.0D + 3.0D + random.nextDouble() * 0.25D);
        boolean cosNoiseIsLargerThanZero = MathHelper.cos((float) (noise / 3.0D * Math.PI)) > 0.0D;
        int j1 = -1;
        boolean belowSand = false;

        int maxHeight = world.getSolidHeight(x, z) - 1;
        int minHeight = Math.min(maxHeight - 6, 59);

        // Max height needs to be increased for the bryce spikes
        if (this.isBryceMesa)
        {
            maxHeight = Math.max((int) bryceHeight, maxHeight);
        }

        for (int y = maxHeight; y >= minHeight; y--)
        {
            int blockAtPosition = world.getTypeId(x, y, z);

            if (blockAtPosition == 0 && y < bryceHeight)
            {
                // Lie about the current block to generate the Bryce spikes
                blockAtPosition = DefaultMaterial.STONE.id;
            }

            if (blockAtPosition == 0)
            {
                j1 = -1;
            } else if (DefaultMaterial.getMaterial(blockAtPosition).isSolid())
            {
                byte blockData;

                if (j1 == -1)
                {
                    belowSand = false;
                    if (y >= 59 && y <= 64)
                    {
                        currentGroundBlock = DefaultMaterial.STAINED_CLAY.id;
                    }

                    j1 = noisePlusRandomFactor + Math.max(0, y - 63);
                    if (y >= 62)
                    {
                        if (this.isForestMesa && y > 86 + noisePlusRandomFactor * 2)
                        {
                            if (cosNoiseIsLargerThanZero)
                            {
                                world.setBlock(x, y, z, biomeGroundBlock, biomeGroundBlockData);
                            } else
                            {
                                world.setBlock(x, y, z, biomeSurfaceBlock, 0);
                            }
                        } else if (y > 66 + noisePlusRandomFactor)
                        {
                            blockData = 16;
                            if (y >= 64 && y <= 127)
                            {
                                if (!cosNoiseIsLargerThanZero)
                                {
                                    blockData = this.getBlockData(x, y, z);
                                }
                            } else
                            {
                                blockData = 1;
                            }

                            if (blockData < 16)
                            {
                                world.setBlock(x, y, z, DefaultMaterial.STAINED_CLAY.id, blockData);
                            } else
                            {
                                world.setBlock(x, y, z, DefaultMaterial.HARD_CLAY.id, 0);
                            }
                        } else
                        {
                            world.setBlock(x, y, z, DefaultMaterial.SAND.id, 1);
                            belowSand = true;
                        }
                    } else
                    {
                        if (currentGroundBlock == DefaultMaterial.STAINED_CLAY.id)
                        {
                            world.setBlock(x, y, z, currentGroundBlock, 1);
                        } else
                        {
                            world.setBlock(x, y, z, currentGroundBlock, 0);
                        }
                    }
                } else if (j1 > 0)
                {
                    --j1;
                    if (belowSand)
                    {
                        world.setBlock(x, y, z, DefaultMaterial.STAINED_CLAY.id, 1);
                    } else
                    {
                        blockData = this.getBlockData(x, y, z);
                        if (blockData < 16)
                        {
                            world.setBlock(x, y, z, DefaultMaterial.STAINED_CLAY.id, blockData);
                        } else
                        {
                            world.setBlock(x, y, z, DefaultMaterial.HARD_CLAY.id, 0);
                        }
                    }
                }
            }
        }

    }

    public String toString()
    {
        if (this.isForestMesa)
        {
            return NAME_FOREST;
        }
        if (this.isBryceMesa)
        {
            return NAME_BRYCE;
        }
        return NAME_NORMAL;
    }

}
