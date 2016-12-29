package com.khorn.terraincontrol.forge.launch;

/**
 * Fields that contain information necessary for Forge to launch.
 */
final class LaunchEnvironment
{
    // Would there be a way to extract these from the build.gradle file?
    private static final String MINECRAFT_VERSION = "1.11";
    private static final String MCP_VERSION = "20161129";
    private final String minecraftGradleDir = System.getProperty("user.home") + "/.gradle/caches/minecraft";

    String getMcpFile()
    {
        return this.minecraftGradleDir + "/de/oceanlabs/mcp/mcp_snapshot/" + MCP_VERSION + "/" + MINECRAFT_VERSION + "/srgs/srg-mcp.srg";
    }

    String getLwjglPath()
    {
        return this.minecraftGradleDir + "/net/minecraft/natives/" + MINECRAFT_VERSION;
    }

    String getAssetsPath()
    {
        return this.minecraftGradleDir + "/assets";
    }

    String getMinecraftVersion()
    {
        return MINECRAFT_VERSION;
    }
}
