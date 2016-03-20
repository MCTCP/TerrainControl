package com.khorn.terraincontrol.forge.launch;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

/**
 * Class that launches the Minecraft Forge server. Intended to be used from
 * IDEs.
 *
 * <p>
 * In most IDEs, you can simply select this class and press the run button, and
 * the Minecraft server will start.
 */
public final class TCLaunchForgeServer
{

    public static void main(String[] extraArgs) throws IOException
    {
        LaunchEnvironment environment = new LaunchEnvironment();
        // Set some system properties
        System.setProperty("fml.ignoreInvalidMinecraftCertificates", "true");
        System.setProperty("net.minecraftforge.gradle.GradleStart.srg.srg-mcp", environment.getMcpFile());

        // Add nogui arg
        String[] allArgs = Arrays.copyOf(extraArgs, extraArgs.length + 1);
        allArgs[allArgs.length - 1] = "nogui";

        // Agree with eula (anyone interested in developing TerrainControl has
        // already agreed countless times before)
        File eula = new File("eula.txt");
        if (!eula.exists())
        {
            eula.createNewFile();
            FileWriter writer = new FileWriter(eula);
            writer.write("eula=true");
            writer.close();
        }

        // Launch
        net.minecraftforge.fml.relauncher.ServerLaunchWrapper.main(allArgs);
    }
}
