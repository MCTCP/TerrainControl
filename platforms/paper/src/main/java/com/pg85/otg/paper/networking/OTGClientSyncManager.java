package com.pg85.otg.paper.networking;

import java.util.HashMap;
import java.util.Map;

public class OTGClientSyncManager
{
	private static final Map<String, BiomeSettingSyncWrapper> syncedData = new HashMap<>();

	public static Map<String, BiomeSettingSyncWrapper> getSyncedData()
	{
		return syncedData;
	}
}
