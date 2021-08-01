package com.pg85.otg.fabric;

import net.fabricmc.api.ModInitializer;

public class OTGPlugin implements ModInitializer {
	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		System.out.println("OpenTerrainGenerator-Fabric reporting in... it's dark in here.");
	}
}
