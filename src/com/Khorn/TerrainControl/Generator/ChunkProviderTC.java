package com.Khorn.TerrainControl.Generator;

import com.Khorn.TerrainControl.BiomeManager.BiomeManagerOld;
import com.Khorn.TerrainControl.Configuration.WorldConfig;
import com.Khorn.TerrainControl.Generator.TerrainsGens.CanyonsGen;
import com.Khorn.TerrainControl.Generator.TerrainsGens.CavesGen;
import com.Khorn.TerrainControl.Generator.TerrainsGens.TerrainGenBase;
import com.Khorn.TerrainControl.TCDefaultValues;
import net.minecraft.server.*;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import java.util.*;


public class ChunkProviderTC extends ChunkGenerator
{
    private Random rnd;
    private NoiseGeneratorOctaves o;
    private NoiseGeneratorOctaves p;
    private NoiseGeneratorOctaves q;
    private NoiseGeneratorOctaves r;
    public NoiseGeneratorOctaves a;
    public NoiseGeneratorOctaves b;
    public NoiseGeneratorOctaves c;
    private double[] u;
    private double[] v = new double[256];

    double[] g;
    double[] h;
    double[] i;
    double[] j;
    double[] k;
    float[] l;

    private static int ChunkMaxX = 16;
    private static int ChunkMaxZ = 16;


    private World localWorld;
    private BiomeManagerOld old_wcm;
    private double biomeFactor;
    private double biomeFactor2;

    private WorldConfig worldSettings;
    private TerrainGenBase CaveGen;


    public WorldGenStronghold strongholdGen = new WorldGenStronghold();

    public WorldGenVillage VillageGen = new WorldGenVillage(0);
    public WorldGenMineshaft MineshaftGen = new WorldGenMineshaft();

    private TerrainGenBase CanyonGen;

    private BiomeBase[] BiomeArray;


    private ArrayList<BlockPopulator> populatorList;

    public ChunkProviderTC(WorldConfig config)
    {
        this.worldSettings = config;
        this.worldSettings.ChunkProvider = this;
        populatorList = new ArrayList<BlockPopulator>();
        if (this.worldSettings.ModeTerrain == WorldConfig.TerrainMode.Normal || this.worldSettings.ModeTerrain == WorldConfig.TerrainMode.OldGenerator)
            populatorList.add(new ObjectSpawner(this.worldSettings));


    }

    public void Init(org.bukkit.World world)
    {

        this.localWorld = ((CraftWorld) world).getHandle();

        this.rnd = new Random(world.getSeed());

        this.o = new NoiseGeneratorOctaves(this.rnd, 16);
        this.p = new NoiseGeneratorOctaves(this.rnd, 16);
        this.q = new NoiseGeneratorOctaves(this.rnd, 8);
        this.r = new NoiseGeneratorOctaves(this.rnd, 4);

        this.a = new NoiseGeneratorOctaves(this.rnd, 10);
        this.b = new NoiseGeneratorOctaves(this.rnd, 16);

        this.c = new NoiseGeneratorOctaves(this.rnd, 8);
        if (this.worldSettings.ModeBiome == WorldConfig.BiomeMode.OldGenerator)
            old_wcm = (BiomeManagerOld) this.localWorld.getWorldChunkManager();


        this.CaveGen = new CavesGen(this.worldSettings, this.localWorld);
        this.CanyonGen = new CanyonsGen(this.worldSettings, this.localWorld);

        this.l = new float[25];
        for (int i1 = -2; i1 <= 2; i1++)
        {
            for (int i2 = -2; i2 <= 2; i2++)
            {
                float f1 = 10.0F / MathHelper.c(i1 * i1 + i2 * i2 + 0.2F);
                this.l[(i1 + 2 + (i2 + 2) * 5)] = f1;
            }
        }


    }

    private void generateTerrain(int paramInt1, int paramInt2, byte[] paramArrayOfByte)
    {
        int i1 = 4;
        int i2 = this.localWorld.height / 8;

        int i4 = i1 + 1;
        int i5 = this.localWorld.height / 8 + 1;
        int i6 = i1 + 1;
        if (this.worldSettings.ModeBiome == WorldConfig.BiomeMode.OldGenerator)
        {
            this.BiomeArray = this.localWorld.getWorldChunkManager().getBiomes(this.BiomeArray, paramInt1 * 16, paramInt2 * 16, 16, 16);
        } else
            this.BiomeArray = this.localWorld.getWorldChunkManager().getBiomes(this.BiomeArray, paramInt1 * 4 - 2, paramInt2 * 4 - 2, i4 + 5, i6 + 5);

        this.u = a(this.u, paramInt1 * i1, 0, paramInt2 * i1, i4, i5, i6);

        for (int x = 0; x < i1; x++)
            for (int z = 0; z < i1; z++)
                for (int y = 0; y < i2; y++)
                {
                    double d1 = 0.125D;
                    double d2 = this.u[(((x + 0) * i6 + (z + 0)) * i5 + (y + 0))];
                    double d3 = this.u[(((x + 0) * i6 + (z + 1)) * i5 + (y + 0))];
                    double d4 = this.u[(((x + 1) * i6 + (z + 0)) * i5 + (y + 0))];
                    double d5 = this.u[(((x + 1) * i6 + (z + 1)) * i5 + (y + 0))];

                    double d6 = (this.u[(((x + 0) * i6 + (z + 0)) * i5 + (y + 1))] - d2) * d1;
                    double d7 = (this.u[(((x + 0) * i6 + (z + 1)) * i5 + (y + 1))] - d3) * d1;
                    double d8 = (this.u[(((x + 1) * i6 + (z + 0)) * i5 + (y + 1))] - d4) * d1;
                    double d9 = (this.u[(((x + 1) * i6 + (z + 1)) * i5 + (y + 1))] - d5) * d1;

                    for (int piece_y = 0; piece_y < 8; piece_y++)
                    {
                        double d10 = 0.25D;

                        double d11 = d2;
                        double d12 = d3;
                        double d13 = (d4 - d2) * d10;
                        double d14 = (d5 - d3) * d10;

                        for (int i11 = 0; i11 < 4; i11++)
                        {
                            int position = i11 + x * 4 << this.localWorld.heightBitsPlusFour | 0 + z * 4 << this.localWorld.heightBits | y * 8 + piece_y;
                            int step = 1 << this.localWorld.heightBits;
                            double d15 = 0.25D;

                            double d16 = d11;
                            double d17 = (d12 - d11) * d15;
                            for (int i14 = 0; i14 < 4; i14++)
                            {
                                // TODO Add ice replace

                                int i15 = 0;
                                if (y * 8 + piece_y < this.worldSettings.waterLevelMax && y * 8 + piece_y > this.worldSettings.waterLevelMin)
                                {
                                    i15 = this.worldSettings.waterBlock;
                                }

                                if (d16 > 0.0D)
                                {
                                    i15 = Block.STONE.id;
                                }

                                paramArrayOfByte[position] = (byte) i15;
                                position += step;
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

    boolean ReplaceForBiomeAndReturnWaterless(int paramInt1, int paramInt2, byte[] paramArrayOfByte, BiomeBase[] paramArrayOfBiomeBase)
    {
        int waterLevel = this.worldSettings.waterLevelMax;
        int dryBlock = 256;

        double d1 = 0.03125D;
        this.v = this.r.a(this.v, paramInt1 * 16, paramInt2 * 16, 0, 16, 16, 1, d1 * 2.0D, d1 * 2.0D, d1 * 2.0D);
        float[] arrayOfFloat = this.localWorld.getWorldChunkManager().a(paramInt1 * 16, paramInt2 * 16, 16, 16);


        for (int x = 0; x < 16; x++)
            for (int z = 0; z < 16; z++)
            {
                float f1 = arrayOfFloat[(z + x * 16)];

                BiomeBase localBiomeBase = paramArrayOfBiomeBase[(z + x * 16)];
                int i4 = (int) (this.v[(x + z * 16)] / 3.0D + 3.0D + this.rnd.nextDouble() * 0.25D);

                int i5 = -1;

                int i6 = this.worldSettings.biomeConfigs[localBiomeBase.K].SurfaceBlock;
                int i7 = this.worldSettings.biomeConfigs[localBiomeBase.K].GroundBlock;

                if (this.worldSettings.ceilingBedrock)
                    paramArrayOfByte[(z * 16 + x) * this.localWorld.height + this.localWorld.heightMinusOne] = (byte) this.worldSettings.bedrockBlock;

                for (int y = this.localWorld.heightMinusOne; y >= 0; y--)
                {
                    int i9 = (z * 16 + x) * this.localWorld.height + y;

                    if (y < 5 && (worldSettings.createAdminium(y)) && y <= this.rnd.nextInt(5))
                    {
                        paramArrayOfByte[i9] = (byte) this.worldSettings.bedrockBlock;
                    } else
                    {
                        int i10 = paramArrayOfByte[i9];

                        if (i10 == 0)
                            i5 = -1;
                        else if (i10 == Block.STONE.id)
                            if (i5 == -1)
                            {
                                if (i4 <= 0)
                                {
                                    i6 = 0;
                                    i7 = (byte) Block.STONE.id;
                                } else if ((y >= waterLevel - 4) && (y <= waterLevel + 1))
                                {
                                    i6 = this.worldSettings.biomeConfigs[localBiomeBase.K].SurfaceBlock;
                                    i7 = this.worldSettings.biomeConfigs[localBiomeBase.K].GroundBlock;
                                }

                                if ((y < waterLevel) && ( y > this.worldSettings.waterLevelMin) && (i6 == 0))
                                {
                                    if (f1 < 0.15F)
                                        i6 = (byte) this.worldSettings.iceBlock;
                                    else
                                        i6 = (byte) this.worldSettings.waterBlock;
                                }

                                i5 = i4;
                                if (y >= waterLevel - 1)
                                    paramArrayOfByte[i9] = (byte) i6;
                                else
                                    paramArrayOfByte[i9] = (byte) i7;

                                if (localBiomeBase == BiomeBase.DESERT)
                                {
                                    if ((this.worldSettings.desertDirt) && (this.worldSettings.desertDirtFrequency > 0) && (this.rnd.nextInt(this.worldSettings.desertDirtFrequency * ChunkMaxX * ChunkMaxZ) == 0) && (paramArrayOfByte[i9] == Block.SAND.id))
                                        paramArrayOfByte[i9] = (byte) Block.DIRT.id;

                                    if ((this.worldSettings.waterlessDeserts) && ((paramArrayOfByte[i9] == Block.STATIONARY_WATER.id) || (paramArrayOfByte[i9] == Block.ICE.id)))
                                        paramArrayOfByte[i9] = (byte) Block.SAND.id;
                                }

                                if (((this.worldSettings.muddySwamps) || (this.worldSettings.claySwamps)) && (localBiomeBase == BiomeBase.SWAMPLAND) && ((paramArrayOfByte[i9] == Block.SAND.id) || (paramArrayOfByte[i9] == Block.DIRT.id) || (paramArrayOfByte[i9] == Block.SAND.id)))
                                    createSwamps(paramArrayOfByte, i9);

                                if ((this.worldSettings.removeSurfaceStone) && (paramArrayOfByte[i9] == Block.STONE.id))
                                    paramArrayOfByte[i9] = (byte) ((localBiomeBase == BiomeBase.DESERT) ? Block.SAND.id : Block.GRASS.id);


                            } else if (i5 > 0)
                            {
                                i5--;
                                paramArrayOfByte[i9] = (byte) i7;

                                if ((i5 == 0) && (i7 == Block.SAND.id))
                                {
                                    i5 = this.rnd.nextInt(4);
                                    i7 = (byte) Block.SANDSTONE.id;
                                }
                            }
                    }
                }
                if (paramArrayOfByte[(z * 16 + x) * 128 + this.worldSettings.waterLevelMax] == this.worldSettings.waterBlock)
                    dryBlock--;


            }

        return dryBlock > 250;
    }


    private double[] a(double[] paramArrayOfDouble, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
    {
        if (paramArrayOfDouble == null)
        {
            paramArrayOfDouble = new double[paramInt4 * paramInt5 * paramInt6];
        }


        double d1 = 684.41200000000003D * this.worldSettings.getFractureHorizontal();
        double d2 = 684.41200000000003D * this.worldSettings.getFractureVertical();

        if (this.worldSettings.oldTerrainGenerator)
            this.j = this.a.a(this.j, paramInt1, paramInt3, paramInt4, paramInt6, 1.121D, 1.121D, 0.5D);
        this.k = this.b.a(this.k, paramInt1, paramInt3, paramInt4, paramInt6, 200.0D, 200.0D, 0.5D);

        this.g = this.q.a(this.g, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6, d1 / 80.0D, d2 / 160.0D, d1 / 80.0D);
        this.h = this.o.a(this.h, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6, d1, d2, d1);
        this.i = this.p.a(this.i, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6, d1, d2, d1);

        int i3 = 0;
        int i4 = 0;

        for (int x = 0; x < paramInt4; x++)
        {
            for (int z = 0; z < paramInt6; z++)
            {


                double d3 = this.k[i4] / 8000.0D;
                if (d3 < 0.0D)
                    d3 = -d3 * 0.3D;
                d3 = d3 * 3.0D - 2.0D;

                if (d3 < 0.0D)
                {
                    d3 /= 2.0D;
                    if (d3 < -1.0D)
                        d3 = -1.0D;
                    d3 -= this.worldSettings.maxAverageDepth;
                    d3 /= 1.4D;
                    d3 /= 2.0D;
                } else
                {
                    if (d3 > 1.0D)
                        d3 = 1.0D;
                    d3 += this.worldSettings.maxAverageHeight;
                    d3 /= 8.0D;
                }

                if (this.worldSettings.oldTerrainGenerator)
                    this.oldTerrainNoise(x, z, i4, paramInt4, paramInt5, d3);
                else
                    this.newTerrainNoise(x, z, paramInt4, paramInt5, d3);


                i4++;


                for (int i10 = 0; i10 < paramInt5; i10++)
                {
                    double d7;

                    double d8 = (i10 - biomeFactor2) * 12.0D * 128.0D / this.localWorld.height / biomeFactor;

                    if (d8 < 0.0D)
                        d8 *= 4.0D;

                    double d9 = this.h[i3] / 512.0D * this.worldSettings.getVolatility1();
                    double d10 = this.i[i3] / 512.0D * this.worldSettings.getVolatility2();

                    double d11 = (this.g[i3] / 10.0D + 1.0D) / 2.0D;
                    if (d11 < this.worldSettings.getVolatilityWeight1())
                        d7 = d9;
                    else if (d11 > this.worldSettings.getVolatilityWeight2())
                        d7 = d10;
                    else
                        d7 = d9 + (d10 - d9) * d11;

                    if (!this.worldSettings.disableNotchHeightControl)
                    {
                        d7 -= d8;

                        if (i10 > paramInt5 - 4)
                        {
                            double d12 = (i10 - (paramInt5 - 4)) / 3.0F;
                            d7 = d7 * (1.0D - d12) + -10.0D * d12;
                        }

                    }
                    d7 += this.worldSettings.heightMatrix[i10];

                    paramArrayOfDouble[i3] = d7;
                    i3++;
                }
            }
        }
        return paramArrayOfDouble;

    }

    private void oldTerrainNoise(int x, int z, int i4, int paramInt4, int paramInt5, double d3)
    {
        if (this.worldSettings.ModeBiome == WorldConfig.BiomeMode.OldGenerator)
        {
            this.biomeFactor = (1.0D - old_wcm.old_temperature[z * 48 + 17 + x * 3] * old_wcm.old_rain[z * 48 + 17 + x * 3]);

        } else
        {
            BiomeBase localBiomeBase1 = this.BiomeArray[(x + 2 + (z + 2) * (paramInt4 + 5))];
            this.biomeFactor = (1.0D - localBiomeBase1.y * localBiomeBase1.z);
        }
        this.biomeFactor *= this.biomeFactor;
        this.biomeFactor = 1.0D - this.biomeFactor * this.biomeFactor;

        this.biomeFactor = (this.g[i4] + 256.0D) / 512.0D * this.biomeFactor;
        if (this.biomeFactor > 1.0D)
            this.biomeFactor = 1.0D;
        if (this.biomeFactor < 0.0D || d3 < 0.0D)
            this.biomeFactor = 0.0D;

        this.biomeFactor += 0.5D;

        d3 = d3 * paramInt5 / 16.0D;

        this.biomeFactor2 = paramInt5 / 2.0D + d3 * 4.0D;
    }

    private void newTerrainNoise(int x, int z, int paramInt4, int paramInt5, double d3)
    {
        float f2 = 0.0F;
        float f3 = 0.0F;
        float f4 = 0.0F;

        int i7 = 2;

        BiomeBase localBiomeBase1 = this.BiomeArray[(x + 2 + (z + 2) * (paramInt4 + 5))];
        for (int i8 = -i7; i8 <= i7; i8++)
        {
            for (int i9 = -i7; i9 <= i7; i9++)
            {
                BiomeBase localBiomeBase2 = this.BiomeArray[(x + i8 + 2 + (z + i9 + 2) * (paramInt4 + 5))];
                float f5 = this.l[(i8 + 2 + (i9 + 2) * 5)] / (this.worldSettings.biomeConfigs[localBiomeBase2.K].BiomeHeight + 2.0F);
                if (this.worldSettings.biomeConfigs[localBiomeBase2.K].BiomeHeight > this.worldSettings.biomeConfigs[localBiomeBase1.K].BiomeHeight)
                {
                    f5 /= 2.0F;
                }
                f2 += this.worldSettings.biomeConfigs[localBiomeBase2.K].BiomeVolatility * f5;
                f3 += this.worldSettings.biomeConfigs[localBiomeBase2.K].BiomeHeight * f5;
                f4 += f5;
            }
        }
        f2 /= f4;
        f3 /= f4;

        f2 = f2 * 0.9F + 0.1F;
        f3 = (f3 * 4.0F - 1.0F) / 8.0F;

        double d4 = f3;
        this.biomeFactor = f2;

        d4 += d3 * 0.2D;
        d4 = d4 * paramInt5 / 16.0D;

        this.biomeFactor2 = paramInt5 / 2.0D + d4 * 4.0D;
    }


    private void createSwamps(byte[] blocks, int block)
    {
        int swampSize = this.worldSettings.swampSize < 0 ? 0 : this.worldSettings.swampSize > 15 ? 15 : this.worldSettings.swampSize;
        int Swamptype = (this.worldSettings.muddySwamps) ? Block.SOUL_SAND.id : Block.CLAY.id;

        if (this.worldSettings.muddySwamps && this.worldSettings.claySwamps)
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
                int newBlock = block + z * this.localWorld.height + x * this.localWorld.height * ChunkMaxZ;
                if ((newBlock < 0) || (newBlock > TCDefaultValues.maxChunkBlockValue.intValue() - 1))
                    continue;
                if (blocks[newBlock] != Block.STATIONARY_WATER.id)
                    continue;
                blocks[block] = (byte) Swamptype;
                return;
            }
    }


    @Override
    public byte[] generate(org.bukkit.World world, Random random, int x, int z)
    {

        this.rnd.setSeed(x * 341873128712L + z * 132897987541L);

        byte[] arrayOfByte = new byte[ChunkMaxX * this.localWorld.height * ChunkMaxZ];


        generateTerrain(x, z, arrayOfByte);

        this.BiomeArray = this.localWorld.getWorldChunkManager().getBiomeBlock(this.BiomeArray, x * 16, z * 16, ChunkMaxX, ChunkMaxZ);
        boolean dry = ReplaceForBiomeAndReturnWaterless(x, z, arrayOfByte, this.BiomeArray);

        this.CaveGen.a(x, z, arrayOfByte);
        this.CanyonGen.a(x, z, arrayOfByte);

        if (this.worldSettings.StrongholdsEnabled)
            this.strongholdGen.a(null, this.localWorld, x, z, arrayOfByte);

        if (this.worldSettings.MineshaftsEnabled)
            this.MineshaftGen.a(null, this.localWorld, x, z, arrayOfByte);
        if (this.worldSettings.VillagesEnabled && dry)
            this.VillageGen.a(null, this.localWorld, x, z, arrayOfByte);


        if (this.worldSettings.isDeprecated)
            this.worldSettings = this.worldSettings.newSettings;

        return arrayOfByte;

    }

    @Override
    public boolean canSpawn(org.bukkit.World world, int x, int z)
    {
        this.worldSettings.plugin.WorldInit(world);

        int i = ((CraftWorld) world).getHandle().a(x, z);
        return i != 0 && Block.byId[i].material.isSolid();

    }

    @Override
    public List<BlockPopulator> getDefaultPopulators(org.bukkit.World world)
    {
        return populatorList;
    }

}
