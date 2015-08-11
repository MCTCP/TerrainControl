package com.khorn.terraincontrol.forge.launch;

/**
 * Class that launches the Minecraft Forge client. Intended to be used from
 * IDEs.
 *
 * <p>
 * In most IDEs, you can simply select this class and press the run button, and
 * Minecraft will start.
 */
public final class TCLaunchForgeClient
{

    static final String MINECRAFT_VERSION = "1.8";

    public static void main(String[] extraArgs)
    {
        String homeDir = System.getProperty("user.home");
        String[] requiredArgs = {"--version", MINECRAFT_VERSION, "--tweakClass", net.minecraftforge.fml.common.launcher.FMLTweaker.class.getName(), "--assetIndex", MINECRAFT_VERSION, "--assetsDir", homeDir + "/.gradle/caches/minecraft/assets", "--accessToken", "foo", "--userProperties", "[]"};

        // Combine requiredArgs and extraArgs into a single array
        String[] allArgs = new String[requiredArgs.length + extraArgs.length];
        System.arraycopy(requiredArgs, 0, allArgs, 0, requiredArgs.length);
        System.arraycopy(extraArgs, 0, allArgs, requiredArgs.length, extraArgs.length);

        // Set some system properties
        System.setProperty("fml.ignoreInvalidMinecraftCertificates", "true");
        System.setProperty("org.lwjgl.librarypath", homeDir + "/.gradle/caches/minecraft/net/minecraft/minecraft_natives/" + MINECRAFT_VERSION);

        // Launch
        net.minecraft.launchwrapper.Launch.main(allArgs);
    }
}
