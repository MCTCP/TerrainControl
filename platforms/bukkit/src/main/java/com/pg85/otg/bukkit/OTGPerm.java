package com.pg85.otg.bukkit;

public enum OTGPerm
{
	CMD_TP("cmd.tp"),
    CMD_BIOME("cmd.biome"),
    CMD_CHECK("cmd.check"),
    CMD_HELP("cmd.help"),
    CMD_LIST("cmd.list"),
    CMD_MAP("cmd.map"),
    CMD_RELOAD("cmd.reload"),
    CMD_SPAWN("cmd.spawn"), 
    CMD_LOOKUP("cmd.lookup"), 
    CMD_EXPORT("cmd.export");

    public final String node;

    OTGPerm(final String permissionNode)
    {
        this.node = "otg." + permissionNode;
    }
}
