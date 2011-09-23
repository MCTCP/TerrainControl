package com.Khorn.PTMBukkit;

import java.io.BufferedWriter;
import java.util.HashMap;

public class BiomeConfig
{
    private BufferedWriter SettingsWriter;
    private HashMap<String, String> ReadedSettings = new HashMap<String, String>();

    public HashMap<Integer, Byte> replaceBlocks = new HashMap<Integer, Byte>();
    public byte[] ReplaceBlocksMatrix = new byte[256];





}
