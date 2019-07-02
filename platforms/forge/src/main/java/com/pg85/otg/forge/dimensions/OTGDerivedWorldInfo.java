package com.pg85.otg.forge.dimensions;

import javax.annotation.Nullable;

import com.pg85.otg.forge.OTGPlugin;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldType;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class OTGDerivedWorldInfo extends WorldInfo
{
    /** Instance of WorldInfo. */
    private WorldInfo delegate;
    private WorldInfo otgDimWorldInfo;

    OTGDerivedWorldInfo(WorldInfo delegateWorldInfo, WorldInfo otgDimWorldInfo)
    {
        this.delegate = delegateWorldInfo;
        this.otgDimWorldInfo = otgDimWorldInfo;
    }

    /**
     * Creates a new NBTTagCompound for the world, with the given NBTTag as the "Player"
     */
    public NBTTagCompound cloneNBTCompound(@Nullable NBTTagCompound nbt)
    {
        return this.delegate.cloneNBTCompound(nbt);
    }

    /**
     * Returns the seed of current world.
     */
    public long getSeed()
    {
        return this.otgDimWorldInfo.getSeed();
    }

    /**
     * Returns the x spawn position
     */
    public int getSpawnX()
    {
        return this.otgDimWorldInfo.getSpawnX();
    }

    /**
     * Return the Y axis spawning point of the player.
     */
    public int getSpawnY()
    {
        return this.otgDimWorldInfo.getSpawnY();
    }

    /**
     * Returns the z spawn position
     */
    public int getSpawnZ()
    {
        return this.otgDimWorldInfo.getSpawnZ();
    }

    public long getWorldTotalTime()
    {
        return this.delegate.getWorldTotalTime();
    }

    /**
     * Get current world time
     */
    public long getWorldTime()
    {
        return this.delegate.getWorldTime();
    }

    @SideOnly(Side.CLIENT)
    public long getSizeOnDisk()
    {
        return this.delegate.getSizeOnDisk();
    }

    /**
     * Returns the player's NBTTagCompound to be loaded
     */
    public NBTTagCompound getPlayerNBTTagCompound()
    {
        return this.delegate.getPlayerNBTTagCompound();
    }

    /**
     * Get current world name
     */
    public String getWorldName()
    {
        return this.otgDimWorldInfo.getWorldName();
    }

    /**
     * Returns the save version of this world
     */
    public int getSaveVersion()
    {
        return this.delegate.getSaveVersion();
    }

    /**
     * Return the last time the player was in this world.
     */
    @SideOnly(Side.CLIENT)
    public long getLastTimePlayed()
    {
        return this.delegate.getLastTimePlayed();
    }

    /**
     * Returns true if it is thundering, false otherwise.
     */
    public boolean isThundering()
    {
        return this.delegate.isThundering();
    }

    /**
     * Returns the number of ticks until next thunderbolt.
     */
    public int getThunderTime()
    {
        return this.delegate.getThunderTime();
    }

    /**
     * Returns true if it is raining, false otherwise.
     */
    public boolean isRaining()
    {
        return this.delegate.isRaining();
    }

    /**
     * Return the number of ticks until rain.
     */
    public int getRainTime()
    {
        return this.delegate.getRainTime();
    }

    /**
     * Gets the GameType.
     */
    public GameType getGameType()
    {
        return this.otgDimWorldInfo.getGameType();
    }

    /**
     * Set the x spawn position to the passed in value
     */
    @SideOnly(Side.CLIENT)
    public void setSpawnX(int x)
    {
    	
    }

    /**
     * Sets the y spawn position
     */
    @SideOnly(Side.CLIENT)
    public void setSpawnY(int y)
    {
    	
    }

    public void setWorldTotalTime(long time)
    {
    	
    }

    /**
     * Set the z spawn position to the passed in value
     */
    @SideOnly(Side.CLIENT)
    public void setSpawnZ(int z)
    {
    	
    }

    /**
     * Set current world time
     */
    public void setWorldTime(long time)
    {
    	
    }

    public void setSpawn(BlockPos spawnPoint)
    {
    	
    }

    public void setWorldName(String worldName)
    {
    	
    }

    /**
     * Sets the save version of the world
     */
    public void setSaveVersion(int version)
    {
    	
    }

    /**
     * Sets whether it is thundering or not.
     */
    public void setThundering(boolean thunderingIn)
    {
    	
    }

    /**
     * Defines the number of ticks until next thunderbolt.
     */
    public void setThunderTime(int time)
    {
    	
    }

    /**
     * Sets whether it is raining or not.
     */
    public void setRaining(boolean isRaining)
    {
    	
    }

    /**
     * Sets the number of ticks until rain.
     */
    public void setRainTime(int time)
    {
    	
    }

    /**
     * Get whether the map features (e.g. strongholds) generation is enabled or disabled.
     */
    public boolean isMapFeaturesEnabled()
    {
        return this.otgDimWorldInfo.isMapFeaturesEnabled();
    }
    
    /**
     * Sets the GameType.
     */
    public void setGameType(GameType type)
    {
        this.otgDimWorldInfo.setGameType(type);
    }    

    /**
     * Returns true if hardcore mode is enabled, otherwise false
     */
    public boolean isHardcoreModeEnabled()
    {
        return this.otgDimWorldInfo.isHardcoreModeEnabled();
    }

    public WorldType getTerrainType()
    {
        return OTGPlugin.OtgWorldType;
    }
    
    public String getGeneratorOptions()
    {
        return "OpenTerrainGenerator";
    }

    /**
     * Returns true if commands are allowed on this World.
     */
    public boolean areCommandsAllowed()
    {
        return this.otgDimWorldInfo.areCommandsAllowed();
    }

    public void setAllowCommands(boolean allow)
    {
    	
    }

    /**
     * Returns true if the World is initialized.
     */
    public boolean isInitialized()
    {
        return this.delegate.isInitialized();
    }

    /**
     * Sets the initialization status of the World.
     */
    public void setServerInitialized(boolean initializedIn)
    {
    	
    }

    /**
     * Gets the GameRules class Instance.
     */
    public GameRules getGameRulesInstance()
    {
        return this.otgDimWorldInfo.getGameRulesInstance();
    }

    public EnumDifficulty getDifficulty()
    {
        return this.otgDimWorldInfo.getDifficulty();
    }

    public void setDifficulty(EnumDifficulty newDifficulty)
    {
    	
    }

    public boolean isDifficultyLocked()
    {
        return this.otgDimWorldInfo.isDifficultyLocked();
    }

    public void setDifficultyLocked(boolean locked)
    {
    	
    }

    @Deprecated
    public void setDimensionData(DimensionType dimensionIn, NBTTagCompound compound)
    {
        this.delegate.setDimensionData(dimensionIn, compound);
    }

    @Deprecated
    public NBTTagCompound getDimensionData(DimensionType dimensionIn)
    {
        return this.delegate.getDimensionData(dimensionIn);
    }

    public void setDimensionData(int dimensionID, NBTTagCompound compound)
    {
        this.delegate.setDimensionData(dimensionID, compound);
    }

    public NBTTagCompound getDimensionData(int dimensionID)
    {
        return this.delegate.getDimensionData(dimensionID);
    }
}