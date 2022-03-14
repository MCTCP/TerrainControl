package com.pg85.otg.gen.resource;

import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.gen.noise.DoublePerlinNoiseSampler;
import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IWorldGenRegion;
import com.pg85.otg.util.OTGDirection;
import com.pg85.otg.util.Vec3i;
import com.pg85.otg.util.helpers.MathHelper;
import com.pg85.otg.util.materials.LocalMaterials;
import com.pg85.otg.util.materials.MaterialProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GeodeResource extends FrequencyResourceBase
{
    public GeodeResource(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) throws InvalidConfigException
    {
        super(biomeConfig, args, logger, materialReader);
        this.frequency = readInt(args.get(0), 1, 500);
        this.rarity = readRarity(args.get(1));
        // TODO: make everything configurable
    }

    @Override
    public void spawn(IWorldGenRegion world, Random random, int x, int z)
    {
        int y = random.nextInt(25) + 20;
        Vec3i pos = new Vec3i(x, y, z);
        int points = 3 + random.nextInt(2);
        double offset = points / 6.0;

        double fillingThreshold = 1.0 / Math.sqrt(1.7);
        double innerThreshold = 1.0 / Math.sqrt(2.2 + offset);
        double middleThreshold = 1.0 / Math.sqrt(3.2 + offset);
        double outerThreshold = 1.0 / Math.sqrt(4.2 + offset);
        double crackThreshold = 1.0 / Math.sqrt(2.0 + random.nextDouble() / 2.0D + (points > 3 ? offset : 0.0D));

        DoublePerlinNoiseSampler sampler = DoublePerlinNoiseSampler.create(random, -4, 1.0);

        List<Sphere> spheres = new ArrayList<>();
        for (int i = 0; i < points; i++)
        {
            int x1 = 4 + random.nextInt(3);
            int y1 = 4 + random.nextInt(3);
            int z1 = 4 + random.nextInt(3);

            spheres.add(new Sphere(pos.add(x1, y1, z1), 1 + random.nextInt(2)));
        }

        List<Vec3i> crackPoints = new ArrayList<>();

        if (random.nextInt(20) > 0)
        {
            int r = random.nextInt(4);
            int s = points * 2 + 1;
            if (r == 0)
            {
                crackPoints.add(pos.add(s, 7, 0));
                crackPoints.add(pos.add(s, 5, 0));
                crackPoints.add(pos.add(s, 1, 0));
            } else if (r == 1)
            {
                crackPoints.add(pos.add(0, 7, s));
                crackPoints.add(pos.add(0, 5, s));
                crackPoints.add(pos.add(0, 1, s));
            } else if (r == 2)
            {
                crackPoints.add(pos.add(s, 7, s));
                crackPoints.add(pos.add(s, 5, s));
                crackPoints.add(pos.add(s, 1, s));
            } else
            {
                crackPoints.add(pos.add(0, 7, 0));
                crackPoints.add(pos.add(0, 5, 0));
                crackPoints.add(pos.add(0, 1, 0));
            }
        }

        List<Vec3i> clusters = new ArrayList<>();

        for (int x1 = -16; x1 <= 16; x1++)
        {
            for (int z1 = -16; z1 <= 16; z1++)
            {
                for (int y1 = -16; y1 <= 16; y1++)
                {
                    int worldX = x + x1;
                    int worldY = y + y1;
                    int worldZ = z + z1;

                    double noise = sampler.sample(worldX, worldY, worldZ) * 0.05;

                    double density = 0;
                    double crackDensity = 0;

                    for (Sphere sphere : spheres)
                    {
                        int dx = (worldX - sphere.pos.x());
                        int dy = (worldY - sphere.pos.y());
                        int dz = (worldZ - sphere.pos.z());

                        density += MathHelper.fastInverseSqrt(dx * dx + dy * dy + dz * dz + sphere.value) + noise;
                    }

                    for (Vec3i sphere : crackPoints)
                    {
                        int dx = (worldX - sphere.x());
                        int dy = (worldY - sphere.y());
                        int dz = (worldZ - sphere.z());

                        crackDensity += MathHelper.fastInverseSqrt(dx * dx + dy * dy + dz * dz + 2.0) + noise;
                    }

                    if (!(density < outerThreshold))
                    {
                        if ((crackDensity >= crackThreshold && density < fillingThreshold) || density >= fillingThreshold)
                        {
                            world.setBlock(worldX, worldY, worldZ, LocalMaterials.AIR);
                        } else if (density >= innerThreshold)
                        {
                            if (random.nextInt(12) == 0)
                            {
                                world.setBlock(worldX, worldY, worldZ, LocalMaterials.BUDDING_AMETHYST);
                                clusters.add(new Vec3i(worldX, worldY, worldZ));
                            } else
                            {
                                world.setBlock(worldX, worldY, worldZ, LocalMaterials.AMETHYST_BLOCK);
                            }
                        } else if (density >= middleThreshold)
                        {
                            world.setBlock(worldX, worldY, worldZ, LocalMaterials.CALCITE);
                        } else if (density >= outerThreshold)
                        {
                            world.setBlock(worldX, worldY, worldZ, LocalMaterials.SMOOTH_BASALT);
                        }
                    }
                }
            }
        }

        for (Vec3i cluster : clusters)
        {
            if (random.nextInt(2) == 0)
            {
                placeRandomCluster(world, cluster, OTGDirection.UP, random);
                placeRandomCluster(world, cluster, OTGDirection.DOWN, random);
                placeRandomCluster(world, cluster, OTGDirection.NORTH, random);
                placeRandomCluster(world, cluster, OTGDirection.SOUTH, random);
                placeRandomCluster(world, cluster, OTGDirection.EAST, random);
                placeRandomCluster(world, cluster, OTGDirection.WEST, random);
            }
        }
    }

    private void placeRandomCluster(IWorldGenRegion world, Vec3i vec, OTGDirection dir, Random random)
    {
        int x = vec.x() + dir.getX();
        int y = vec.y() + dir.getY();
        int z = vec.z() + dir.getZ();

        if (world.getMaterial(x, y, z) == LocalMaterials.AIR)
        {
            int type = random.nextInt(4);
            switch (type)
            {
                case 0 -> world.setBlock(x, y, z, LocalMaterials.SMALL_AMETHYST_BUD.withProperty(MaterialProperties.DIRECTION, dir));
                case 1 -> world.setBlock(x, y, z, LocalMaterials.MEDIUM_AMETHYST_BUD.withProperty(MaterialProperties.DIRECTION, dir));
                case 2 -> world.setBlock(x, y, z, LocalMaterials.LARGE_AMETHYST_BUD.withProperty(MaterialProperties.DIRECTION, dir));
                case 3 -> world.setBlock(x, y, z, LocalMaterials.AMETHYST_CLUSTER.withProperty(MaterialProperties.DIRECTION, dir));
            }
        }
    }

    private static record Sphere(Vec3i pos, int value)
    {
    }

    @Override
    public String toString()
    {
        return "Geode(" + this.frequency + "," + this.rarity + ")";
    }
}
