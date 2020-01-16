package com.pg85.otg.forge.dimensions;

import net.minecraft.network.play.server.SPacketWorldBorder;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.village.VillageCollection;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.border.IBorderListener;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;

public class OTGWorldServerMulti extends WorldServer
{
    private final WorldServer delegate;
    private IBorderListener borderListener;
    
    // Used when creating OTG dims
    OTGWorldServerMulti(MinecraftServer server, ISaveHandler saveHandlerIn, int dimensionId, WorldServer delegate, Profiler profilerIn, WorldInfo otgDimWorldInfo)
    {
        super(server, saveHandlerIn, new OTGDerivedWorldInfo(delegate.getWorldInfo(), otgDimWorldInfo), dimensionId, profilerIn);
        this.delegate = delegate;
        this.borderListener = new IBorderListener()
        {
            public void onSizeChanged(WorldBorder border, double newSize)
            {
            	server.getPlayerList().sendPacketToAllPlayersInDimension(new SPacketWorldBorder(border, SPacketWorldBorder.Action.SET_SIZE), dimensionId);
            }
            public void onTransitionStarted(WorldBorder border, double oldSize, double newSize, long time)
            {
            	server.getPlayerList().sendPacketToAllPlayersInDimension(new SPacketWorldBorder(border, SPacketWorldBorder.Action.LERP_SIZE), dimensionId);
            }
            public void onCenterChanged(WorldBorder border, double x, double z)
            {
            	server.getPlayerList().sendPacketToAllPlayersInDimension(new SPacketWorldBorder(border, SPacketWorldBorder.Action.SET_CENTER), dimensionId);
            }
            public void onWarningTimeChanged(WorldBorder border, int newTime)
            {
            	server.getPlayerList().sendPacketToAllPlayersInDimension(new SPacketWorldBorder(border, SPacketWorldBorder.Action.SET_WARNING_TIME), dimensionId);
            }
            public void onWarningDistanceChanged(WorldBorder border, int newDistance)
            {
            	server.getPlayerList().sendPacketToAllPlayersInDimension(new SPacketWorldBorder(border, SPacketWorldBorder.Action.SET_WARNING_BLOCKS), dimensionId);
            }
            public void onDamageAmountChanged(WorldBorder border, double newAmount)
            {
            }
            public void onDamageBufferChanged(WorldBorder border, double newSize)
            {
            }
        };
        getWorldBorder().addListener(this.borderListener);
    }

    /**
     * Saves the chunks to disk.
     */
    protected void saveLevel() throws MinecraftException
    {
        this.perWorldStorage.saveAllData();
    }

    public World init()
    {
    	if(this.delegate != null)
    	{
	        this.mapStorage = this.delegate.getMapStorage();
	        this.worldScoreboard = this.delegate.getScoreboard();
	        this.lootTable = this.delegate.getLootTableManager();
	        this.advancementManager = this.delegate.getAdvancementManager();
	        String s = VillageCollection.fileNameForProvider(this.provider);
	        VillageCollection villagecollection = (VillageCollection)this.perWorldStorage.getOrLoadData(VillageCollection.class, s);

	        if (villagecollection == null)
	        {
	            this.villageCollection = new VillageCollection(this);
	            this.perWorldStorage.setData(s, this.villageCollection);
	        } else {
	            this.villageCollection = villagecollection;
	            this.villageCollection.setWorldsForAll(this);
	        }

	        this.initCapabilities();
    	} else {
    		super.init();
    	}
        return this;
    }

    /**
     * Syncs all changes to disk and wait for completion.
     */
    @Override
    public void flush()
    {
        super.flush();
        if(this.delegate != null)
        {
        	this.delegate.getWorldBorder().removeListener(this.borderListener); // Unlink ourselves, to prevent world leak.
        }
    }

    /**
     * Called during saving of a world to give children worlds a chance to save additional data. Only used to save
     * WorldProviderEnd's data in Vanilla.
     */
    public void saveAdditionalData()
    {
        this.provider.onWorldSave();
    }
}
