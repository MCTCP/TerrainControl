package com.khorn.terraincontrol.forge.launch;

/**
 * Class that launches the Minecraft Forge client. Intended to be used from
 * IDEs.
 *
 * <p> In most IDEs, you can simply select this class and press the run button,
 * and Minecraft will start.
 */
public final class TCLaunchForgeClient
{

    public static void main(String[] extraArgs)
    {
        LaunchEnvironment environment = new LaunchEnvironment();
        String[] requiredArgs = {"--version", environment.getMinecraftVersion(),
                "--tweakClass", net.minecraftforge.fml.common.launcher.FMLTweaker.class.getName(),
                "--assetIndex", environment.getMinecraftVersion(),
                "--assetsDir", environment.getAssetsPath(),
                "--accessToken", "foo",
                "--userProperties", "{}"};

        // Combine requiredArgs and extraArgs into a single array
        String[] allArgs = new String[requiredArgs.length + extraArgs.length];
        System.arraycopy(requiredArgs, 0, allArgs, 0, requiredArgs.length);
        System.arraycopy(extraArgs, 0, allArgs, requiredArgs.length, extraArgs.length);

        // Set some system properties
        System.setProperty("fml.ignoreInvalidMinecraftCertificates", "true");
        System.setProperty("org.lwjgl.librarypath", environment.getLwjglPath());
        System.setProperty("net.minecraftforge.gradle.GradleStart.srg.srg-mcp", environment.getMcpFile());

        // Launch
        net.minecraft.launchwrapper.Launch.main(allArgs);
    }
}
