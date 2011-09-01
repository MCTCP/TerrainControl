package com.Khorn.PTMBukkit;

import com.Khorn.PTMBukkit.CustomObjects.CustomObject;
import org.bukkit.entity.Player;


public class PTMPlayer
{
    public Player BukkitPlayer;
    public boolean hasObjectToSpawn;
    public CustomObject object;

    public  PTMPlayer(Player player)
    {
        this.BukkitPlayer = player;
    }
}
