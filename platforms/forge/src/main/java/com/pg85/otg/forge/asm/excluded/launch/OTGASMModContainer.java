package com.pg85.otg.forge.asm.excluded.launch;

import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.ModMetadata;

import com.google.common.eventbus.EventBus;
import net.minecraftforge.fml.common.LoadController;

public class OTGASMModContainer extends DummyModContainer
{
	public OTGASMModContainer()
	{
		super(new ModMetadata());
		ModMetadata meta = getMetadata();
		meta.modId = "otgcore";
		meta.name = "OTG Core";
		meta.description = "Allows missing OTG biomes in the registry when a client connects to the server. OTG sends and registers the missing biomes on the client before the player joins the world. Also makes sure the correct biome is returned when querying the registry by id. Also allows gravity settings per world/dimension.";
		meta.version = "1.12.2 - v8.3_r1";
		List<String> authorList = new ArrayList<String>();
		authorList.add("PG85");
		meta.authorList = authorList;
	}

    @Override
    public boolean registerBus(EventBus bus, LoadController controller)
    {
    	bus.register(this);
        return true;
    }
}