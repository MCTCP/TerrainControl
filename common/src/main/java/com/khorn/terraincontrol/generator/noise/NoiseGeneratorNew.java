package com.khorn.terraincontrol.generator.noise;

import java.util.Random;

public class NoiseGeneratorNew {

    private static int[][] e = new int[][] { { 1, 1, 0}, { -1, 1, 0}, { 1, -1, 0}, { -1, -1, 0}, { 1, 0, 1}, { -1, 0, 1}, { 1, 0, -1}, { -1, 0, -1}, { 0, 1, 1}, { 0, -1, 1}, { 0, 1, -1}, { 0, -1, -1}};
    public static final double a = Math.sqrt(3.0D);
    private int[] f;
    public double b;
    public double c;
    public double d;
    private static final double g = 0.5D * (a - 1.0D);
    private static final double h = (3.0D - a) / 6.0D;

    public NoiseGeneratorNew() {
        this(new Random());
    }

    public NoiseGeneratorNew(Random random) {
        this.f = new int[512];
        this.b = random.nextDouble() * 256.0D;
        this.c = random.nextDouble() * 256.0D;
        this.d = random.nextDouble() * 256.0D;

        int i;

        for (i = 0; i < 256; this.f[i] = i++) {
            ;
        }

        for (i = 0; i < 256; ++i) {
            int j = random.nextInt(256 - i) + i;
            int k = this.f[i];

            this.f[i] = this.f[j];
            this.f[j] = k;
            this.f[i + 256] = this.f[i];
        }
    }

    private static int a(double d0) {
        return d0 > 0.0D ? (int) d0 : (int) d0 - 1;
    }

    private static double a(int[] aint, double d0, double d1) {
        return (double) aint[0] * d0 + (double) aint[1] * d1;
    }

    public double a(double d0, double d1) {
        double d2 = 0.5D * (a - 1.0D);
        double d3 = (d0 + d1) * d2;
        int i = a(d0 + d3);
        int j = a(d1 + d3);
        double d4 = (3.0D - a) / 6.0D;
        double d5 = (double) (i + j) * d4;
        double d6 = (double) i - d5;
        double d7 = (double) j - d5;
        double d8 = d0 - d6;
        double d9 = d1 - d7;
        byte b0;
        byte b1;

        if (d8 > d9) {
            b0 = 1;
            b1 = 0;
        } else {
            b0 = 0;
            b1 = 1;
        }

        double d10 = d8 - (double) b0 + d4;
        double d11 = d9 - (double) b1 + d4;
        double d12 = d8 - 1.0D + 2.0D * d4;
        double d13 = d9 - 1.0D + 2.0D * d4;
        int k = i & 255;
        int l = j & 255;
        int i1 = this.f[k + this.f[l]] % 12;
        int j1 = this.f[k + b0 + this.f[l + b1]] % 12;
        int k1 = this.f[k + 1 + this.f[l + 1]] % 12;
        double d14 = 0.5D - d8 * d8 - d9 * d9;
        double d15;

        if (d14 < 0.0D) {
            d15 = 0.0D;
        } else {
            d14 *= d14;
            d15 = d14 * d14 * a(e[i1], d8, d9);
        }

        double d16 = 0.5D - d10 * d10 - d11 * d11;
        double d17;

        if (d16 < 0.0D) {
            d17 = 0.0D;
        } else {
            d16 *= d16;
            d17 = d16 * d16 * a(e[j1], d10, d11);
        }

        double d18 = 0.5D - d12 * d12 - d13 * d13;
        double d19;

        if (d18 < 0.0D) {
            d19 = 0.0D;
        } else {
            d18 *= d18;
            d19 = d18 * d18 * a(e[k1], d12, d13);
        }

        return 70.0D * (d15 + d17 + d19);
    }

    public void a(double[] adouble, double d0, double d1, int i, int j, double d2, double d3, double d4) {
        int k = 0;

        for (int l = 0; l < j; ++l) {
            double d5 = (d1 + (double) l) * d3 + this.c;

            for (int i1 = 0; i1 < i; ++i1) {
                double d6 = (d0 + (double) i1) * d2 + this.b;
                double d7 = (d6 + d5) * g;
                int j1 = a(d6 + d7);
                int k1 = a(d5 + d7);
                double d8 = (double) (j1 + k1) * h;
                double d9 = (double) j1 - d8;
                double d10 = (double) k1 - d8;
                double d11 = d6 - d9;
                double d12 = d5 - d10;
                byte b0;
                byte b1;

                if (d11 > d12) {
                    b0 = 1;
                    b1 = 0;
                } else {
                    b0 = 0;
                    b1 = 1;
                }

                double d13 = d11 - (double) b0 + h;
                double d14 = d12 - (double) b1 + h;
                double d15 = d11 - 1.0D + 2.0D * h;
                double d16 = d12 - 1.0D + 2.0D * h;
                int l1 = j1 & 255;
                int i2 = k1 & 255;
                int j2 = this.f[l1 + this.f[i2]] % 12;
                int k2 = this.f[l1 + b0 + this.f[i2 + b1]] % 12;
                int l2 = this.f[l1 + 1 + this.f[i2 + 1]] % 12;
                double d17 = 0.5D - d11 * d11 - d12 * d12;
                double d18;

                if (d17 < 0.0D) {
                    d18 = 0.0D;
                } else {
                    d17 *= d17;
                    d18 = d17 * d17 * a(e[j2], d11, d12);
                }

                double d19 = 0.5D - d13 * d13 - d14 * d14;
                double d20;

                if (d19 < 0.0D) {
                    d20 = 0.0D;
                } else {
                    d19 *= d19;
                    d20 = d19 * d19 * a(e[k2], d13, d14);
                }

                double d21 = 0.5D - d15 * d15 - d16 * d16;
                double d22;

                if (d21 < 0.0D) {
                    d22 = 0.0D;
                } else {
                    d21 *= d21;
                    d22 = d21 * d21 * a(e[l2], d15, d16);
                }

                int i3 = k++;

                adouble[i3] += 70.0D * (d18 + d20 + d22) * d4;
            }
        }
    }
}