package com.Khorn.PTMBukkit;

import net.minecraft.server.BiomeBase;
import net.minecraft.server.Block;
import net.minecraft.server.NoiseGeneratorOctaves;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import java.util.*;

@SuppressWarnings({"PointlessArithmeticExpression"})
class ChunkProviderPTM extends ChunkGenerator
{
    private Random rnd;
    private NoiseGeneratorOctaves k;
    private NoiseGeneratorOctaves l;
    private NoiseGeneratorOctaves m;
    private NoiseGeneratorOctaves n;
    private NoiseGeneratorOctaves o;
    private NoiseGeneratorOctaves a;
    private NoiseGeneratorOctaves b;

    private World localWorld;
    private double[] q;
    private double[] r = new double[256];
    private double[] s = new double[256];
    private double[] t = new double[256];
    private Settings WorldSettings;
    private MapGenCavesPTM CaveGen;
    private BiomeBase[] BiomeArray;
    private double[] d;
    private double[] e;
    private double[] f;
    private double[] g;
    private double[] h;

    private boolean isInit = false;
    private ArrayList<BlockPopulator> populatorList;

    public ChunkProviderPTM(Settings worker)
    {
        this.WorldSettings = worker;
        populatorList = new ArrayList<BlockPopulator>();
        populatorList.add(new ObjectSpawner(this.WorldSettings));


    }

    public void Init(World wrld)
    {

        this.localWorld = wrld;

        if (!this.WorldSettings.isInit)
        {
            ((CraftWorld) this.localWorld).getHandle().worldProvider.b = new BiomeManagerPTM(((CraftWorld) this.localWorld).getHandle(), this.WorldSettings);
            this.WorldSettings.isInit = true;
        }

        this.rnd = new Random(wrld.getSeed());

        this.k = new NoiseGeneratorOctaves(this.rnd, 16);
        this.l = new NoiseGeneratorOctaves(this.rnd, 16);
        this.m = new NoiseGeneratorOctaves(this.rnd, 8);
        this.n = new NoiseGeneratorOctaves(this.rnd, 4);
        this.o = new NoiseGeneratorOctaves(this.rnd, 4);

        this.a = new NoiseGeneratorOctaves(this.rnd, 10);
        this.b = new NoiseGeneratorOctaves(this.rnd, 16);


        this.CaveGen = new MapGenCavesPTM(this.WorldSettings);
        this.isInit = true;

    }

    void generateTerrain(int paramInt1, int paramInt2, byte[] paramArrayOfByte, double[] paramArrayOfDouble)
    {
        int i1 = 4;
        int i2 = this.WorldSettings.getWaterLevel();

        int i3 = i1 + 1;
        int i4 = 17;
        int i5 = i1 + 1;
        this.q = a(this.q, paramInt1 * i1, 0, paramInt2 * i1, i3, i4, i5);

        for (int i6 = 0; i6 < i1; i6++)
            for (int i7 = 0; i7 < i1; i7++)
                for (int i8 = 0; i8 < 16; i8++)
                {
                    double d1 = 0.125D;
                    double d2 = this.q[(((i6 + 0) * i5 + (i7 + 0)) * i4 + (i8 + 0))];
                    double d3 = this.q[(((i6 + 0) * i5 + (i7 + 1)) * i4 + (i8 + 0))];
                    double d4 = this.q[(((i6 + 1) * i5 + (i7 + 0)) * i4 + (i8 + 0))];
                    double d5 = this.q[(((i6 + 1) * i5 + (i7 + 1)) * i4 + (i8 + 0))];

                    double d6 = (this.q[(((i6 + 0) * i5 + (i7 + 0)) * i4 + (i8 + 1))] - d2) * d1;
                    double d7 = (this.q[(((i6 + 0) * i5 + (i7 + 1)) * i4 + (i8 + 1))] - d3) * d1;
                    double d8 = (this.q[(((i6 + 1) * i5 + (i7 + 0)) * i4 + (i8 + 1))] - d4) * d1;
                    double d9 = (this.q[(((i6 + 1) * i5 + (i7 + 1)) * i4 + (i8 + 1))] - d5) * d1;

                    for (int i9 = 0; i9 < 8; i9++)
                    {
                        double d10 = 0.25D;

                        double d11 = d2;
                        double d12 = d3;
                        double d13 = (d4 - d2) * d10;
                        double d14 = (d5 - d3) * d10;

                        for (int i10 = 0; i10 < 4; i10++)
                        {
                            int i11 = i10 + i6 * 4 << 11 | 0 + i7 * 4 << 7 | i8 * 8 + i9;
                            int i12 = 128;
                            double d15 = 0.25D;

                            double d16 = d11;
                            double d17 = (d12 - d11) * d15;
                            for (int i13 = 0; i13 < 4; i13++)
                            {
                                double d18 = paramArrayOfDouble[((i6 * 4 + i10) * 16 + (i7 * 4 + i13))];
                                int i14 = 0;
                                if (i8 * 8 + i9 < i2)
                                {
                                    if ((d18 < this.WorldSettings.iceThreshold) && (i8 * 8 + i9 >= i2 - 1))
                                        i14 = Block.ICE.id;
                                    else
                                    {
                                        i14 = Block.STATIONARY_WATER.id;
                                    }
                                }
                                if (d16 > 0.0D)
                                {
                                    i14 = Block.STONE.id;
                                }

                                paramArrayOfByte[i11] = (byte) i14;
                                i11 += i12;
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

    void replaceBlocksForBiome(int paramInt1, int paramInt2, byte[] paramArrayOfByte, BiomeBase[] paramArrayOfBiomeBase)
    {
        if (!this.WorldSettings.oldGen)
        {
            int i1 = this.WorldSettings.getWaterLevel();
            double d1 = 0.03125D;

            this.r = this.n.a(this.r, paramInt1 * 16, paramInt2 * 16, 0.0D, 16, 16, 1, d1, d1, 1.0D);
            this.s = this.n.a(this.s, paramInt1 * 16, 109.0134D, paramInt2 * 16, 16, 1, 16, d1, 1.0D, d1);
            this.t = this.o.a(this.t, paramInt1 * 16, paramInt2 * 16, 0.0D, 16, 16, 1, d1 * 2.0D, d1 * 2.0D, d1 * 2.0D);

            for (int i2 = 0; i2 < 16; i2++)
                for (int i3 = 0; i3 < 16; i3++)
                {
                    BiomeBase localBiomeBase = paramArrayOfBiomeBase[(i2 + i3 * 16)];
                    int i4 = this.r[(i2 + i3 * 16)] + this.rnd.nextDouble() * 0.2D > 0.0D ? 1 : 0;
                    int i5 = this.s[(i2 + i3 * 16)] + this.rnd.nextDouble() * 0.2D > 3.0D ? 1 : 0;
                    int i6 = (int) (this.t[(i2 + i3 * 16)] / 3.0D + 3.0D + this.rnd.nextDouble() * 0.25D);

                    int i7 = -1;

                    byte i8 = localBiomeBase.p;
                    byte i9 = localBiomeBase.q;

                    for (int i10 = 127; i10 >= 0; i10--)
                    {
                        int i11 = (i3 * 16 + i2) * 128 + i10;

                        if ((i10 <= 0 + this.rnd.nextInt(5)) && (WorldSettings.createadminium(i10)))
                        {
                            paramArrayOfByte[i11] = this.WorldSettings.getadminium();
                        } else
                        {
                            int i12 = paramArrayOfByte[i11];

                            if (i12 == 0)
                                i7 = -1;
                            else if (i12 == Block.STONE.id)
                                if (i7 == -1)
                                {
                                    if (i6 <= 0)
                                    {
                                        i8 = 0;
                                        i9 = (byte) Block.STONE.id;
                                    } else if ((i10 >= i1 - 4) && (i10 <= i1 + 1))
                                    {
                                        i8 = localBiomeBase.p;
                                        i9 = localBiomeBase.q;
                                        if (i5 != 0)
                                            i8 = 0;
                                        if (i5 != 0)
                                            i9 = (byte) Block.GRAVEL.id;
                                        if (i4 != 0)
                                            i8 = (byte) Block.SAND.id;
                                        if (i4 != 0)
                                            i9 = (byte) Block.SAND.id;
                                    }

                                    if ((i10 < i1) && (i8 == 0))
                                        i8 = (byte) Block.STATIONARY_WATER.id;

                                    i7 = i6;
                                    if (i10 >= i1 - 1)
                                        paramArrayOfByte[i11] = i8;
                                    else
                                        paramArrayOfByte[i11] = i9;
                                } else if (i7 > 0)
                                {
                                    i7--;
                                    paramArrayOfByte[i11] = i9;
                                    if ((i7 == 0) && (i9 == Block.SAND.id))
                                    {
                                        i7 = this.rnd.nextInt(4);
                                        i9 = (byte) Block.SANDSTONE.id;
                                    }
                                }
                        }
                    }
                }
        } else
        {
            int i1 = this.WorldSettings.getWaterLevel();

            double d1 = 0.03125D;
            this.r = this.n.a(this.r, paramInt1 * 16, paramInt2 * 16, 0.0D, 16, 16, 1, d1, d1, 1.0D);
            this.s = this.n.a(this.s, paramInt2 * 16, 109.0134D, paramInt1 * 16, 16, 1, 16, d1, 1.0D, d1);
            this.t = this.o.a(this.t, paramInt1 * 16, paramInt2 * 16, 0.0D, 16, 16, 1, d1 * 2.0D, d1 * 2.0D, d1 * 2.0D);

            for (int i2 = 0; i2 < 16; i2++)
                for (int i3 = 0; i3 < 16; i3++)
                {
                    BiomeBase localBiomeBase = paramArrayOfBiomeBase[(i2 * 16 + i3)];
                    int i4 = this.r[(i2 * 16 + i3)] + this.rnd.nextDouble() * 0.2D > 0.0D ? 1 : 0;
                    int i5 = this.s[(i2 * 16 + i3)] + this.rnd.nextDouble() * 0.2D > 3.0D ? 1 : 0;
                    int i6 = (int) (this.t[(i2 * 16 + i3)] / 3.0D + 3.0D + this.rnd.nextDouble() * 0.25D);

                    int i7 = -1;

                    byte i8 = localBiomeBase.p;
                    byte i9 = localBiomeBase.q;

                    for (int i10 = 127; i10 >= 0; i10--)
                    {
                        int i11 = (i2 * 16 + i3) * 128 + i10;

                        if ((i10 <= 0 + this.rnd.nextInt(5)) && (this.WorldSettings.createadminium(i10)))
                        {
                            paramArrayOfByte[i11] = this.WorldSettings.getadminium();
                        } else
                        {
                            int i12 = paramArrayOfByte[i11];

                            if (i12 == 0)
                                i7 = -1;
                            else if (i12 == Block.STONE.id)
                                if (i7 == -1)
                                {
                                    if (i6 <= 0)
                                    {
                                        i8 = 0;
                                        i9 = (byte) Block.STONE.id;
                                    } else if ((i10 >= i1 - 4) && (i10 <= i1 + 1))
                                    {
                                        i8 = localBiomeBase.p;
                                        i9 = localBiomeBase.q;
                                        if (i5 != 0)
                                            i8 = 0;
                                        if (i5 != 0)
                                            i9 = (byte) Block.GRAVEL.id;
                                        if (i4 != 0)
                                            i8 = (byte) Block.SAND.id;
                                        if (i4 != 0)
                                            i9 = (byte) Block.SAND.id;
                                    }

                                    if ((i10 < i1) && (i8 == 0))
                                        i8 = (byte) Block.STATIONARY_WATER.id;

                                    i7 = i6;
                                    if (i10 >= i1 - 1)
                                        paramArrayOfByte[i11] = i8;
                                    else
                                        paramArrayOfByte[i11] = i9;
                                } else if (i7 > 0)
                                {
                                    i7--;
                                    paramArrayOfByte[i11] = i9;
                                    if ((i7 == 0) && (i9 == Block.SAND.id))
                                    {
                                        i7 = this.rnd.nextInt(4);
                                        i9 = (byte) Block.SANDSTONE.id;
                                    }
                                }
                        }
                    }
                }
        }
    }


    private double[] a(double[] paramArrayOfDouble, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
    {
        if (paramArrayOfDouble == null)
        {
            paramArrayOfDouble = new double[paramInt4 * paramInt5 * paramInt6];
        }

        double d1 = 684.41200000000003D * this.WorldSettings.getFractureHorizontal();
        double d2 = 684.41200000000003D * this.WorldSettings.getFractureVertical();

        double[] arrayOfDouble1 = ((CraftWorld) this.localWorld).getHandle().getWorldChunkManager().temperature;
        double[] arrayOfDouble2 = ((CraftWorld) this.localWorld).getHandle().getWorldChunkManager().rain;
        this.g = this.a.a(this.g, paramInt1, paramInt3, paramInt4, paramInt6, 1.121D, 1.121D, 0.5D);
        this.h = this.b.a(this.h, paramInt1, paramInt3, paramInt4, paramInt6, 200.0D, 200.0D, 0.5D);

        this.d = this.m.a(this.d, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6, d1 / 80.0D, d2 / 160.0D, d1 / 80.0D);
        this.e = this.k.a(this.e, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6, d1, d2, d1);
        this.f = this.l.a(this.f, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6, d1, d2, d1);

        int i1 = 0;
        int i2 = 0;

        int i3 = 16 / paramInt4;
        for (int i4 = 0; i4 < paramInt4; i4++)
        {
            int i5 = i4 * i3 + i3 / 2;

            for (int i6 = 0; i6 < paramInt6; i6++)
            {
                int i7 = i6 * i3 + i3 / 2;
                double d3 = arrayOfDouble1[(i5 * 16 + i7)];
                double d4 = arrayOfDouble2[(i5 * 16 + i7)] * d3;
                double d5 = 1.0D - d4;
                d5 *= d5;
                d5 *= d5;
                d5 = 1.0D - d5;

                double d6 = (this.g[i2] + 256.0D) / 512.0D;
                d6 *= d5;
                if (d6 > 1.0D)
                    d6 = 1.0D;

                double d7 = this.h[i2] / 8000.0D;
                if (d7 < 0.0D)
                    d7 = -d7 * 0.3D;
                d7 = d7 * 3.0D - 2.0D;

                if (d7 < 0.0D)
                {
                    d7 /= 2.0D;
                    if (d7 < -1.0D)
                        d7 = -1.0D;
                    d7 -= this.WorldSettings.getMaxAverageDepth();
                    d7 /= 1.4D;
                    d7 /= 2.0D;
                    d6 = 0.0D;
                } else
                {
                    if (d7 > 1.0D)
                        d7 = 1.0D;
                    d7 += this.WorldSettings.getMaxAverageHeight();
                    d7 /= 8.0D;
                }

                if (d6 < 0.0D)
                    d6 = 0.0D;
                d6 += 0.5D;
                d7 = d7 * paramInt5 / 16.0D;

                double d8 = paramInt5 / 2.0D + d7 * 4.0D;

                i2++;

                for (int i8 = 0; i8 < paramInt5; i8++)
                {
                    double d9;

                    double d10 = (i8 - d8) * 12.0D / d6;
                    if (d10 < 0.0D)
                        d10 *= 4.0D;

                    double d11 = this.e[i1] / 512.0D * this.WorldSettings.getVolatility1();
                    double d12 = this.f[i1] / 512.0D * this.WorldSettings.getVolatility2();

                    double d13 = (this.d[i1] / 10.0D + 1.0D) / 2.0D;
                    if (d13 < this.WorldSettings.getVolatilityWeight1())
                        d9 = d11;
                    else if (d13 > this.WorldSettings.getVolatilityWeight2())
                        d9 = d12;
                    else
                        d9 = d11 + (d12 - d11) * d13;
                    d9 -= d10;

                    if (i8 > paramInt5 - 4)
                    {
                        double d14 = (i8 - (paramInt5 - 4)) / 3.0F;
                        d9 = d9 * (1.0D - d14) + -10.0D * d14;
                    }

                    paramArrayOfDouble[i1] = d9;
                    i1++;
                }
            }
        }
        return paramArrayOfDouble;
    }


    public void processChunkBlocks(byte[] blocks, BiomeBase[] biomes)
    {

        for (int x = 0; x < BiomeTerrainValues.xLimit.intValue(); x++)
            for (int z = 0; z < BiomeTerrainValues.zLimit.intValue(); z++)
            {
                BiomeBase currentBiome = biomes[(x * BiomeTerrainValues.zLimit.intValue() + z)];
                for (int y = 0; y < BiomeTerrainValues.yLimit.intValue(); y++)
                {
                    int block = y + z * BiomeTerrainValues.yLimit.intValue() + x * BiomeTerrainValues.yLimit.intValue() * BiomeTerrainValues.zLimit.intValue();
                    if (block >= BiomeTerrainValues.maxChunkBlockValue.intValue() - 1)
                        continue;
                    if ((this.WorldSettings.removeSurfaceDirtFromDesert) && (currentBiome == BiomeBase.DESERT) && ((blocks[block] == Block.GRASS.id) || (blocks[block] == Block.DIRT.id)) && (blocks[(block + 1)] == 0)) // next
                        // block
                        // is
                        // air
                        blocks[block] = (byte) Block.SAND.id;
                    if ((this.WorldSettings.desertDirt) && (currentBiome == BiomeBase.DESERT) && (this.WorldSettings.desertDirtFrequency > 0) && (this.rnd.nextInt(this.WorldSettings.desertDirtFrequency * BiomeTerrainValues.xLimit.intValue() * BiomeTerrainValues.zLimit.intValue()) == 0) && (blocks[block] == Block.SAND.id) && (blocks[(block + 1)] == 0)) // next
                        // block
                        // is
                        // air
                        blocks[block] = (byte) Block.DIRT.id;
                    if ((this.WorldSettings.waterlessDeserts) && (currentBiome == BiomeBase.DESERT) && ((blocks[block] == Block.STATIONARY_WATER.id) || (blocks[block] == Block.ICE.id)))
                        blocks[block] = (byte) Block.SAND.id;
                    if (((this.WorldSettings.muddySwamps) || (this.WorldSettings.claySwamps)) && (currentBiome == BiomeBase.SWAMPLAND) && ((blocks[block] == Block.SAND.id) || (blocks[block] == Block.DIRT.id) || (blocks[block] == Block.SAND.id)))
                        createSwamps(blocks, block);
                    if ((this.WorldSettings.removeSurfaceStone) && (blocks[block] == Block.STONE.id) && (blocks[(block + 1)] == 0)) // next
                        // block
                        // is
                        // air
                        blocks[block] = findNearestStoneReplacement(blocks, block, currentBiome);

                }
            }
    }



    private byte findNearestStoneReplacement(byte[] blocks, int block, BiomeBase currentBiome)
    {
        for (int w = 1; w < 16; w++)
            for (int y = 0; y <= 2; y++)
                for (int x = -1 * w; x <= w; x++)
                    for (int z = -1 * w; z <= w; z++)
                    {
                        int newBlock = block + (y == 2 ? -1 : y) + z * BiomeTerrainValues.yLimit.intValue() + x * BiomeTerrainValues.yLimit.intValue() * BiomeTerrainValues.zLimit.intValue();
                        if ((newBlock < 0) || (newBlock > BiomeTerrainValues.maxChunkBlockValue.intValue() - 1))
                            continue;
                        if ((blocks[newBlock] == Block.GRASS.id) || (blocks[newBlock] == Block.DIRT.id) || (blocks[newBlock] == Block.SAND.id) || (blocks[newBlock] == Block.GRAVEL.id) || (blocks[newBlock] == Block.CLAY.id))
                            return blocks[newBlock];
                    }
        return (byte) ((currentBiome == BiomeBase.DESERT) || (currentBiome == BiomeBase.ICE_DESERT) ? Block.SAND.id : Block.GRASS.id);
    }




    private void createSwamps(byte[] blocks, int block)
    {
        int swampSize = this.WorldSettings.swampSize < 0 ? 0 : this.WorldSettings.swampSize > 15 ? 15 : this.WorldSettings.swampSize;
        int Swamptype = (this.WorldSettings.muddySwamps) ? Block.SOUL_SAND.id : Block.CLAY.id;

        if (this.WorldSettings.muddySwamps && this.WorldSettings.claySwamps)
        {
            Swamptype = (this.rnd.nextBoolean()) ? Block.SOUL_SAND.id : Block.CLAY.id;
        }

        if (blocks[(block + 1)] == Block.STATIONARY_WATER.id)
        {

            blocks[block] = (byte) Swamptype;
            return;
        }

        for (int x = swampSize * -1; x < swampSize + 1; x++)
            for (int z = swampSize * -1; z < swampSize + 1; z++)
            {
                int newBlock = block + z * BiomeTerrainValues.yLimit.intValue() + x * BiomeTerrainValues.yLimit.intValue() * BiomeTerrainValues.zLimit.intValue();
                if ((newBlock < 0) || (newBlock > BiomeTerrainValues.maxChunkBlockValue.intValue() - 1))
                    continue;
                if (blocks[newBlock] != Block.STATIONARY_WATER.id)
                    continue;
                blocks[block] = (byte) Swamptype;
                return;
            }
    }





    @Override
    public byte[] generate(World world, Random random, int x, int z)
    {

        if (!this.isInit)
            this.Init(world);

        this.rnd.setSeed(x * 341873128712L + z * 132897987541L);

        byte[] arrayOfByte = new byte[32768];

        this.BiomeArray = ((CraftWorld) this.localWorld).getHandle().getWorldChunkManager().a(this.BiomeArray, x * 16, z * 16, 16, 16);
        double[] TempArray = ((CraftWorld) this.localWorld).getHandle().getWorldChunkManager().temperature; // was .a

        generateTerrain(x, z, arrayOfByte, TempArray);
        replaceBlocksForBiome(x, z, arrayOfByte, this.BiomeArray);

        this.CaveGen.a(this.localWorld, x, z, arrayOfByte);

        this.processChunkBlocks(arrayOfByte, this.BiomeArray);   // Todo need optimise.

        return arrayOfByte;

    }

    @Override
    public boolean canSpawn(World world, int x, int z)
    {
        int i = ((CraftWorld) world).getHandle().a(x, z);
        return i != 0 && Block.byId[i].material.isSolid();

    }

    @Override
    public List<BlockPopulator> getDefaultPopulators(World world)
    {
        return populatorList;
    }
}