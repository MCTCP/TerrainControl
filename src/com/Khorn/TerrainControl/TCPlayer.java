package com.Khorn.TerrainControl;

import com.Khorn.TerrainControl.CustomObjects.CustomObject;
import org.bukkit.entity.Player;


public class TCPlayer
{
    public Player BukkitPlayer;
    public boolean hasObjectToSpawn;
    public CustomObject object;

    public  TCPlayer(Player player)
    {
        this.BukkitPlayer = player;
    }
}
