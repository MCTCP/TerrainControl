package com.pg85.otg.forge.dimensions;

import javax.annotation.Nullable;

import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.forge.OTGPlugin;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
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
    	// Inherit NBT data from overworld, then override dimension-specific data.
    	NBTTagCompound nbttagcompound = this.delegate.cloneNBTCompound(nbt);
        updateTagCompound(nbttagcompound);
        return nbttagcompound;
    }

    private void updateTagCompound(NBTTagCompound nbt)
    {    	
        nbt.setLong("RandomSeed", this.getSeed());
        nbt.setString("generatorName", this.getTerrainType().getName());
        nbt.setInteger("generatorVersion", this.getTerrainType().getVersion());
        nbt.setString("generatorOptions", this.getGeneratorOptions());
        nbt.setInteger("GameType", this.getGameType().getID());
        nbt.setBoolean("MapFeatures", this.isMapFeaturesEnabled());
        nbt.setInteger("SpawnX", this.getSpawnX());
        nbt.setInteger("SpawnY", this.getSpawnY());
        nbt.setInteger("SpawnZ", this.getSpawnZ());
        nbt.setLong("Time", this.getWorldTotalTime());
        nbt.setLong("DayTime", this.getWorldTime());
        nbt.setLong("SizeOnDisk", this.getSizeOnDisk());
        nbt.setLong("LastPlayed", MinecraftServer.getCurrentTimeMillis());
        nbt.setString("LevelName", this.getWorldName());
        nbt.setInteger("version", this.getSaveVersion());
        nbt.setInteger("clearWeatherTime", this.getCleanWeatherTime());
        nbt.setInteger("rainTime", this.getRainTime());
        nbt.setBoolean("raining", this.isRaining());
        nbt.setInteger("thunderTime", this.getThunderTime());
        nbt.setBoolean("thundering", this.isThundering());
        nbt.setBoolean("hardcore", this.isHardcoreModeEnabled());
        nbt.setBoolean("allowCommands", this.areCommandsAllowed());
        nbt.setBoolean("initialized", this.isInitialized());
        nbt.setDouble("BorderCenterX", this.getBorderCenterX());
        nbt.setDouble("BorderCenterZ", this.getBorderCenterZ());
        nbt.setDouble("BorderSize", this.getBorderSize());
        nbt.setLong("BorderSizeLerpTime", this.getBorderLerpTime());
        nbt.setDouble("BorderSafeZone", this.getBorderSafeZone());
        nbt.setDouble("BorderDamagePerBlock", this.getBorderDamagePerBlock());
        nbt.setDouble("BorderSizeLerpTarget", this.getBorderLerpTarget());
        nbt.setDouble("BorderWarningBlocks", (double)this.getBorderWarningDistance());
        nbt.setDouble("BorderWarningTime", (double)this.getBorderWarningTime());

        if (this.getDifficulty() != null)
        {
            nbt.setByte("Difficulty", (byte)this.getDifficulty().getId());
        }

        nbt.setBoolean("DifficultyLocked", this.isDifficultyLocked());
        nbt.setTag("GameRules", this.getGameRulesInstance().writeToNBT());
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
        return this.otgDimWorldInfo.getWorldTotalTime();
    }

    /**
     * Get current world time
     */
    public long getWorldTime()
    {
        return this.otgDimWorldInfo.getWorldTime();
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
        return this.otgDimWorldInfo.isThundering();
    }

    /**
     * Returns the number of ticks until next thunderbolt.
     */
    public int getThunderTime()
    {
        return this.otgDimWorldInfo.getThunderTime();
    }

    /**
     * Returns true if it is raining, false otherwise.
     */
    public boolean isRaining()
    {
        return this.otgDimWorldInfo.isRaining();
    }

    /**
     * Return the number of ticks until rain.
     */
    public int getRainTime()
    {
        return this.otgDimWorldInfo.getRainTime();
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
    	this.otgDimWorldInfo.setSpawnX(x);
    }

    /**
     * Sets the y spawn position
     */
    @SideOnly(Side.CLIENT)
    public void setSpawnY(int y)
    {
    	this.otgDimWorldInfo.setSpawnY(y);
    }

    public void setWorldTotalTime(long time)
    {
    	this.otgDimWorldInfo.setWorldTotalTime(time);
    }

    /**
     * Set the z spawn position to the passed in value
     */
    @SideOnly(Side.CLIENT)
    public void setSpawnZ(int z)
    {
    	this.otgDimWorldInfo.setSpawnZ(z);
    }

    /**
     * Set current world time
     */
    public void setWorldTime(long time)
    {
    	this.otgDimWorldInfo.setWorldTime(time);
    }

    public void setSpawn(BlockPos spawnPoint)
    {
    	this.otgDimWorldInfo.setSpawn(spawnPoint);
    }

    public void setWorldName(String worldName)
    {
    	this.otgDimWorldInfo.setWorldName(worldName);
    }

    /**
     * Sets the save version of the world
     */
    public void setSaveVersion(int version)
    {
    	this.delegate.setSaveVersion(version); // TODO: Is this necessary?
    }

    /**
     * Sets whether it is thundering or not.
     */
    public void setThundering(boolean thunderingIn)
    {
    	this.otgDimWorldInfo.setThundering(thunderingIn);
    }

    /**
     * Defines the number of ticks until next thunderbolt.
     */
    public void setThunderTime(int time)
    {
    	this.otgDimWorldInfo.setThunderTime(time);
    }

    /**
     * Sets whether it is raining or not.
     */
    public void setRaining(boolean isRaining)
    {
    	this.otgDimWorldInfo.setRaining(isRaining);
    }

    /**
     * Sets the number of ticks until rain.
     */
    public void setRainTime(int time)
    {
    	this.otgDimWorldInfo.setRainTime(time);
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
        return PluginStandardValues.PLUGIN_NAME;
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
    	this.otgDimWorldInfo.setAllowCommands(allow);
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
    	this.delegate.setServerInitialized(initializedIn);
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
    	this.otgDimWorldInfo.setDifficulty(newDifficulty);
    }

    public boolean isDifficultyLocked()
    {
        return this.otgDimWorldInfo.isDifficultyLocked();
    }

    public void setDifficultyLocked(boolean locked)
    {
    	this.otgDimWorldInfo.setDifficultyLocked(locked);
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