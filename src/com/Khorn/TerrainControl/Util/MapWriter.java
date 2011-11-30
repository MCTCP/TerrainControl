package com.Khorn.TerrainControl.Util;


import com.sun.imageio.plugins.png.PNGImageWriter;
import com.sun.imageio.plugins.png.PNGImageWriterSpi;
import net.minecraft.server.BiomeBase;
import net.minecraft.server.World;

import javax.imageio.stream.FileCacheImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;

public class MapWriter
{


    private static int[] JpegColors = {0x3333FF, 0x999900, 0xFFCC33, 0x666600, 0x00FF00, 0x007700, 0x99cc66, 0x00CCCC, 0, 0, 0xFFFFFF, 0x66FFFF, 0xCCCCCC, 0xCC9966, 0xFF33cc, 0xff9999};
    private static BiomeBase[] BiomeBuffer;


    public static void GenerateMaps(World world,int height, int width)
    {
        try
        {

            float[] tempArray = new float[256];


            BufferedImage biomeImage = new BufferedImage(height * 16, width * 16, BufferedImage.TYPE_INT_RGB);
            BufferedImage tempImage = new BufferedImage(height * 16, width * 16, BufferedImage.TYPE_INT_RGB);
            for (int x = -height / 2; x < height / 2; x++)
                for (int z = -width / 2; z < width / 2; z++)
                {

                    BiomeBuffer = world.getWorldChunkManager().a(BiomeBuffer, x * 16, z * 16, 16, 16);
                    tempArray = world.getWorldChunkManager().getTemperatures(tempArray, x * 16, z * 16, 16, 16);
                    for (int x1 = 0; x1 < 16; x1++)
                        for (int z1 = 0; z1 < 16; z1++)
                        {
                            biomeImage.setRGB((x + height / 2) * 16 + x1, (z + width / 2) * 16 + z1, JpegColors[BiomeBuffer[x1 + 16 * z1].F]);

                            Color tempColor = Color.getHSBColor(0.7f - tempArray[x1 + 16 * z1]*0.7f , 0.9f, tempArray[x1 + 16 * z1] * 0.7f + 0.3f);


                            tempImage.setRGB((x + height / 2) * 16 + x1, (z + width / 2) * 16 + z1, tempColor.getRGB());

                        }
                }

            PNGImageWriter PngEncoder = new PNGImageWriter(new PNGImageWriterSpi());
            ImageOutputStream imageOutput = new FileCacheImageOutputStream(new FileOutputStream("biome.png", false), null);
            PngEncoder.setOutput(imageOutput);
            PngEncoder.write(biomeImage);

            imageOutput = new FileCacheImageOutputStream(new FileOutputStream("temp.png", false), null);
            PngEncoder.setOutput(imageOutput);
            PngEncoder.write(tempImage);

            PngEncoder.dispose();

        } catch (Exception e1)
        {
            e1.printStackTrace();
        }
    }
}
