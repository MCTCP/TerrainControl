package com.khorn.terraincontrol.forge.launch;

/**
 * Fields that contain information necessary for Forge to launch.
 */
final class LaunchEnvironment
{
    // Would there be a way to extract these from the build.gradle file?
    private static final String MINECRAFT_VERSION = "1.9";
    private static final String MCP_VERSION = "20160312";
    private final String minecraftGradleDir = System.getProperty("user.home") + "/.gradle/caches/minecraft";

    String getMcpFile()
    {
        return minecraftGradleDir + "/de/oceanlabs/mcp/mcp_snapshot/" + MCP_VERSION + "/srgs/srg-mcp.srg";
    }

    String getLwjglPath()
    {
        return minecraftGradleDir + "/net/minecraft/natives/" + MINECRAFT_VERSION;
    }

    String getAssetsPath()
    {
        return minecraftGradleDir + "/assets";
    }

    String getMinecraftVersion()
    {
        return MINECRAFT_VERSION;
    }
}
