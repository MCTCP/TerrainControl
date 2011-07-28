package com.Khorn.PTMBukkit;

import net.minecraft.server.BiomeBase;
import net.minecraft.server.Block;
import net.minecraft.server.MathHelper;
import net.minecraft.server.World;
import sun.reflect.generics.tree.ReturnType;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

public class WorldWorker
{
    private String Name;
    public World localWorld;
    private BufferedWriter SettingsWriter;
    private HashMap<String, String> ReadedSettings = new HashMap<String, String>();
    public Random rand;
    // public BiomeBase currentBiome;
    // --Commented out by Inspection (17.07.11 1:49):String seedValue;
    private boolean oldGen;
    private double biomeSize;
    private double minMoisture;
    private double maxMoisture;
    private double minTemperature;
    private double maxTemperature;
    private double snowThreshold;
    private double iceThreshold;
    private boolean muddySwamps;
    private boolean claySwamps;
    private int swampSize;
    private boolean waterlessDeserts;
    private boolean desertDirt;
    private int desertDirtFrequency;
    private boolean removeSurfaceDirtFromDesert;

    private int caveRarity;
    private int caveFrequency;
    private int caveMinAltitude;
    private int caveMaxAltitude;
    private int individualCaveRarity;
    private int caveSystemFrequency;
    private int caveSystemPocketChance;
    private int caveSystemPocketMinSize;
    private int caveSystemPocketMaxSize;
    private boolean evenCaveDistribution;


    private int waterLevel;
    private double maxAverageHeight;
    private double maxAverageDepth;
    private double fractureHorizontal;
    private double fractureVertical;
    private double volatility1;
    private double volatility2;
    private double volatilityWeight1;
    private double volatilityWeight2;
    private boolean disableBedrock;
    private boolean flatBedrock;
    private boolean bedrockobsidian;

    private boolean removeSurfaceStone;
    private HashMap<Byte, Byte> replaceBlocks = new HashMap<Byte, Byte>();

    public ObjectSpawner Spawner;


    public String SettingsDir;

    private boolean isInit = false;


    public WorldWorker(String name,String settingsDir)
    {
        this.Name = name;
        this.SettingsDir = settingsDir;
        this.Spawner = new ObjectSpawner(this);

        ReadSettings();
        CorrectSettings();
        WriteSettings();
        this.Spawner.RegisterBOBPlugins();


        System.out.println("PhoenixTerrainMod Bukkit version 0.5 Loaded!");
    }

    public void InitWorld(World world, Random rnd)
    {
        if(isInit)
          return;

        this.localWorld = world;
        this.rand = rnd;

        world.worldProvider.b = new WorldChunkProviderPTM(world,this);


    }

    void CorrectSettings()
    {
        this.biomeSize = (this.biomeSize <= 0.0D ? 4.9E-324D : this.biomeSize);
        this.minMoisture = (this.minMoisture < 0.0D ? 0.0D : this.minMoisture > 1.0D ? 1.0D : this.minMoisture);
        this.minTemperature = (this.minTemperature < 0.0D ? 0.0D : this.minTemperature > 1.0D ? 1.0D : this.minTemperature);
        this.maxMoisture = (this.maxMoisture > 1.0D ? 1.0D : this.maxMoisture < this.minMoisture ? this.minMoisture : this.maxMoisture);
        this.maxTemperature = (this.maxTemperature > 1.0D ? 1.0D : this.maxTemperature < this.minTemperature ? this.minTemperature : this.maxTemperature);
        this.snowThreshold = (this.snowThreshold < 0.0D ? 0.0D : this.snowThreshold > 1.0D ? 1.0D : this.snowThreshold);
        this.iceThreshold = (this.iceThreshold < -1.0D ? -1.0D : this.iceThreshold > 1.0D ? 1.0D : this.iceThreshold);

        this.caveRarity = (this.caveRarity < 0 ? 0 : this.caveRarity > 100 ? 100 : this.caveRarity);
        this.caveFrequency = (this.caveFrequency < 0 ? 0 : this.caveFrequency);
        this.caveMinAltitude = (this.caveMinAltitude < 0 ? 0 : this.caveMinAltitude > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.caveMinAltitude);
        this.caveMaxAltitude = (this.caveMaxAltitude > BiomeTerrainValues.yLimit.intValue() ? BiomeTerrainValues.yLimit.intValue() : this.caveMaxAltitude <= this.caveMinAltitude ? this.caveMinAltitude + 1 : this.caveMaxAltitude);
        this.individualCaveRarity = (this.individualCaveRarity < 0 ? 0 : this.individualCaveRarity);
        this.caveSystemFrequency = (this.caveSystemFrequency < 0 ? 0 : this.caveSystemFrequency);
        this.caveSystemPocketChance = (this.caveSystemPocketChance < 0 ? 0 : this.caveSystemPocketChance > 100 ? 100 : this.caveSystemPocketChance);
        this.caveSystemPocketMinSize = (this.caveSystemPocketMinSize < 0 ? 0 : this.caveSystemPocketMinSize);
        this.caveSystemPocketMaxSize = (this.caveSystemPocketMaxSize <= this.caveSystemPocketMinSize ? this.caveSystemPocketMinSize + 1 : this.caveSystemPocketMaxSize);



        this.waterLevel = (this.waterLevel < 0 ? 0 : this.waterLevel > BiomeTerrainValues.yLimit.intValue() - 1 ? BiomeTerrainValues.yLimit.intValue() - 1 : this.waterLevel);

        this.Spawner.fixSettingsValues();
    }

    void ReadSettings()
    {
        BufferedReader SettingsReader = null;

        // f = new File(this.worldSaveFolder, BiomeTerrainValues.biomeTerrainSettingsName.stringValue());
        File f = new File(SettingsDir, BiomeTerrainValues.biomeTerrainSettingsName.stringValue());
        if (f.exists())
        {

            try
            {
                SettingsReader = new BufferedReader(new FileReader(f));
                String thisLine;
                while ((thisLine = SettingsReader.readLine()) != null)
                {
                    if (thisLine.toLowerCase().contains(":"))
                    {
                        String[] splitsettings = thisLine.split(":");
                        if (splitsettings.length == 2)
                            this.ReadedSettings.put(splitsettings[0].trim(), splitsettings[1].trim());
                    }
                }
            } catch (IOException e)
            {
                e.printStackTrace();

                if (SettingsReader != null)
                    try
                    {
                        SettingsReader.close();
                    } catch (IOException localIOException1)
                    {
                        localIOException1.printStackTrace();
                    }
            } finally
            {
                if (SettingsReader != null)
                    try
                    {
                        SettingsReader.close();
                    } catch (IOException localIOException2)
                    {
                        localIOException2.printStackTrace();
                    }
            }
        }
        ReadWorldSettings();
        this.Spawner.readSettings();

    }

    private void ReadWorldSettings()
    {
        this.oldGen = ReadModSettins(BiomeTerrainValues.oldGen.name(), BiomeTerrainValues.oldGen.booleanValue());
        this.biomeSize = ReadModSettins(BiomeTerrainValues.biomeSize.name(), BiomeTerrainValues.biomeSize.doubleValue());
        this.minMoisture = ReadModSettins(BiomeTerrainValues.minMoisture.name(), BiomeTerrainValues.minMoisture.doubleValue());
        this.maxMoisture = ReadModSettins(BiomeTerrainValues.maxMoisture.name(), BiomeTerrainValues.maxMoisture.doubleValue());
        this.minTemperature = ReadModSettins(BiomeTerrainValues.minTemperature.name(), BiomeTerrainValues.minTemperature.doubleValue());
        this.maxTemperature = ReadModSettins(BiomeTerrainValues.maxTemperature.name(), BiomeTerrainValues.maxTemperature.doubleValue());
        this.snowThreshold = ReadModSettins(BiomeTerrainValues.snowThreshold.name(), BiomeTerrainValues.snowThreshold.doubleValue());
        this.iceThreshold = ReadModSettins(BiomeTerrainValues.iceThreshold.name(), BiomeTerrainValues.iceThreshold.doubleValue());

        this.muddySwamps = ReadModSettins(BiomeTerrainValues.muddySwamps.name(), BiomeTerrainValues.muddySwamps.booleanValue());
        this.claySwamps = ReadModSettins(BiomeTerrainValues.claySwamps.name(), BiomeTerrainValues.claySwamps.booleanValue());
        this.swampSize = ReadModSettins(BiomeTerrainValues.swampSize.name(), BiomeTerrainValues.swampSize.intValue());

        this.waterlessDeserts = ReadModSettins(BiomeTerrainValues.waterlessDeserts.name(), BiomeTerrainValues.waterlessDeserts.booleanValue());
        this.removeSurfaceDirtFromDesert = ReadModSettins(BiomeTerrainValues.removeSurfaceDirtFromDesert.name(), BiomeTerrainValues.removeSurfaceDirtFromDesert.booleanValue());
        this.desertDirt = ReadModSettins(BiomeTerrainValues.desertDirt.name(), BiomeTerrainValues.desertDirt.booleanValue());
        this.desertDirtFrequency = ReadModSettins(BiomeTerrainValues.desertDirtFrequency.name(), BiomeTerrainValues.desertDirtFrequency.intValue());

        this.caveRarity = ReadModSettins(BiomeTerrainValues.caveRarity.name(), BiomeTerrainValues.caveRarity.intValue());
        this.caveFrequency = ReadModSettins(BiomeTerrainValues.caveFrequency.name(), BiomeTerrainValues.caveFrequency.intValue());
        this.caveMinAltitude = ReadModSettins(BiomeTerrainValues.caveMinAltitude.name(), BiomeTerrainValues.caveMinAltitude.intValue());
        this.caveMaxAltitude = ReadModSettins(BiomeTerrainValues.caveMaxAltitude.name(), BiomeTerrainValues.caveMaxAltitude.intValue());
        this.individualCaveRarity = ReadModSettins(BiomeTerrainValues.individualCaveRarity.name(), BiomeTerrainValues.individualCaveRarity.intValue());
        this.caveSystemFrequency = ReadModSettins(BiomeTerrainValues.caveSystemFrequency.name(), BiomeTerrainValues.caveSystemFrequency.intValue());
        this.caveSystemPocketChance = ReadModSettins(BiomeTerrainValues.caveSystemPocketChance.name(), BiomeTerrainValues.caveSystemPocketChance.intValue());
        this.caveSystemPocketMinSize = ReadModSettins(BiomeTerrainValues.caveSystemPocketMinSize.name(), BiomeTerrainValues.caveSystemPocketMinSize.intValue());
        this.caveSystemPocketMaxSize = ReadModSettins(BiomeTerrainValues.caveSystemPocketMaxSize.name(), BiomeTerrainValues.caveSystemPocketMaxSize.intValue());
        this.evenCaveDistribution = ReadModSettins(BiomeTerrainValues.evenCaveDistribution.name(), BiomeTerrainValues.evenCaveDistribution.booleanValue());




        this.waterLevel = ReadModSettins(BiomeTerrainValues.waterLevel.name(), BiomeTerrainValues.waterLevel.intValue());
        this.maxAverageHeight = ReadModSettins(BiomeTerrainValues.maxAverageHeight.name(), BiomeTerrainValues.maxAverageHeight.doubleValue());
        this.maxAverageDepth = ReadModSettins(BiomeTerrainValues.maxAverageDepth.name(), BiomeTerrainValues.maxAverageDepth.doubleValue());
        this.fractureHorizontal = ReadModSettins(BiomeTerrainValues.fractureHorizontal.name(), BiomeTerrainValues.fractureHorizontal.doubleValue());
        this.fractureVertical = ReadModSettins(BiomeTerrainValues.fractureVertical.name(), BiomeTerrainValues.fractureVertical.doubleValue());
        this.volatility1 = ReadModSettins(BiomeTerrainValues.volatility1.name(), BiomeTerrainValues.volatility1.doubleValue());
        this.volatility2 = ReadModSettins(BiomeTerrainValues.volatility2.name(), BiomeTerrainValues.volatility2.doubleValue());
        this.volatilityWeight1 = ReadModSettins(BiomeTerrainValues.volatilityWeight1.name(), BiomeTerrainValues.volatilityWeight1.doubleValue());
        this.volatilityWeight2 = ReadModSettins(BiomeTerrainValues.volatilityWeight2.name(), BiomeTerrainValues.volatilityWeight2.doubleValue());

        this.disableBedrock = ReadModSettins(BiomeTerrainValues.disableBedrock.name(), BiomeTerrainValues.disableBedrock.booleanValue());
        this.flatBedrock = ReadModSettins(BiomeTerrainValues.flatBedrock.name(), BiomeTerrainValues.flatBedrock.booleanValue());
        this.bedrockobsidian = ReadModSettins(BiomeTerrainValues.bedrockobsidian.name(), BiomeTerrainValues.bedrockobsidian.booleanValue());



        this.removeSurfaceStone = ReadModSettins(BiomeTerrainValues.removeSurfaceStone.name(), BiomeTerrainValues.removeSurfaceStone.booleanValue());

        this.ReadModReplaceSettings();


    }

    public String ReadModSettins(String settingsName, String defaultValue)
    {
        if (this.ReadedSettings.containsKey(settingsName))
        {
            return String.valueOf(this.ReadedSettings.get(settingsName));
        }
        return defaultValue;
    }

    public int ReadModSettins(String settingsName, int defaultValue)
    {
        if (this.ReadedSettings.containsKey(settingsName))
        {
            return Integer.valueOf(this.ReadedSettings.get(settingsName));
        }
        return defaultValue;
    }

    double ReadModSettins(String settingsName, double defaultValue)
    {
        if (this.ReadedSettings.containsKey(settingsName))
        {
            return Double.valueOf(this.ReadedSettings.get(settingsName));
        }
        return defaultValue;
    }

    public boolean ReadModSettins(String settingsName, boolean defaultValue)
    {
        if (this.ReadedSettings.containsKey(settingsName))
        {
            return Boolean.valueOf(this.ReadedSettings.get(settingsName));
        }
        return defaultValue;
    }

    private void ReadModReplaceSettings()
    {
        if (this.ReadedSettings.containsKey("ReplacedBlocks"))
        {
            if (this.ReadedSettings.get("ReplacedBlocks").trim().equals("") || !this.ReadedSettings.get("ReplacedBlocks").contains(","))
                return;
            String[] keys = this.ReadedSettings.get("ReplacedBlocks").split(",");
            try
            {
                for (String key : keys)
                {

                    String[] blocks = key.split("=");

                    this.replaceBlocks.put(Byte.valueOf(blocks[0]), Byte.valueOf(blocks[1]));

                }

            } catch (NumberFormatException e)
            {
                System.out.println("Wrong replace settings: '" + this.ReadedSettings.get("ReplacedBlocks") + "'");
            }

            return;
        }

        for (Entry<String, String> me : this.ReadedSettings.entrySet())
        {
            if (me.getKey().contains("replace"))
            {
                byte testkey = 0;
                byte testvalue = 0;

                try
                {
                    testkey = Byte.parseByte((me.getKey()).replace("replace", ""));
                    testvalue = Byte.parseByte(me.getValue());

                } catch (NumberFormatException e)
                {
                    try
                    {
                        testkey = BiomeTerrainValues.valueOf((me.getKey()).replace("replace", "")).byteValue();
                        testvalue = BiomeTerrainValues.valueOf(me.getValue()).byteValue();
                    } catch (IllegalArgumentException ex)
                    {
                        System.out.println("Wrong replace settings: '" + me.getKey() + ":" + me.getValue() + "'");
                    }

                }
                if (testkey == 0 && testvalue == 0)
                    System.out.println("Wrong replace settings: '" + me.getKey() + ":" + me.getValue() + "'");

                this.replaceBlocks.put(testkey, testvalue);
            }
        }

    }

    void WriteSettings()
    {
        //File f = new File(this.worldSaveFolder, BiomeTerrainValues.biomeTerrainSettingsName.stringValue());
        File f = new File(SettingsDir, BiomeTerrainValues.biomeTerrainSettingsName.stringValue());
        try
        {
            this.SettingsWriter = new BufferedWriter(new FileWriter(f, false));

            WriteWorldSettings();
            this.Spawner.writeSettings();
        } catch (IOException e)
        {
            e.printStackTrace();

            if (this.SettingsWriter != null)
                try
                {
                    this.SettingsWriter.close();
                } catch (IOException localIOException1)
                {
                    localIOException1.printStackTrace();
                }
        } finally
        {
            if (this.SettingsWriter != null)
                try
                {
                    this.SettingsWriter.close();
                } catch (IOException localIOException2)
                {
                    localIOException2.printStackTrace();
                }
        }
    }

    private void WriteWorldSettings() throws IOException
    {
        WriteModTitleSettings("Start Biome Variables :");
        WriteModTitleSettings("All Biome Variables");
        WriteModSettings(BiomeTerrainValues.oldGen.name(), this.oldGen);
        WriteModSettings(BiomeTerrainValues.biomeSize.name(), this.biomeSize);
        WriteModSettings(BiomeTerrainValues.minMoisture.name(), this.minMoisture);
        WriteModSettings(BiomeTerrainValues.maxMoisture.name(), this.maxMoisture);
        WriteModSettings(BiomeTerrainValues.minTemperature.name(), this.minTemperature);
        WriteModSettings(BiomeTerrainValues.maxTemperature.name(), this.maxTemperature);
        WriteModSettings(BiomeTerrainValues.snowThreshold.name(), this.snowThreshold);
        WriteModSettings(BiomeTerrainValues.iceThreshold.name(), this.iceThreshold);

        WriteModTitleSettings("Swamp Biome Variables");
        WriteModSettings(BiomeTerrainValues.muddySwamps.name(), this.muddySwamps);
        WriteModSettings(BiomeTerrainValues.claySwamps.name(), this.claySwamps);
        WriteModSettings(BiomeTerrainValues.swampSize.name(), this.swampSize);

        WriteModTitleSettings("Desert Biome Variables");
        WriteModSettings(BiomeTerrainValues.waterlessDeserts.name(), this.waterlessDeserts);
        WriteModSettings(BiomeTerrainValues.removeSurfaceDirtFromDesert.name(), this.removeSurfaceDirtFromDesert);
        WriteModSettings(BiomeTerrainValues.desertDirt.name(), this.desertDirt);
        WriteModSettings(BiomeTerrainValues.desertDirtFrequency.name(), this.desertDirtFrequency);

        WriteModTitleSettings("Start Underground Variables :");
        WriteModTitleSettings("Cave Variables");
        WriteModSettings(BiomeTerrainValues.caveRarity.name(), this.caveRarity);
        WriteModSettings(BiomeTerrainValues.caveFrequency.name(), this.caveFrequency);
        WriteModSettings(BiomeTerrainValues.caveMinAltitude.name(), this.caveMinAltitude);
        WriteModSettings(BiomeTerrainValues.caveMaxAltitude.name(), this.caveMaxAltitude);
        WriteModSettings(BiomeTerrainValues.individualCaveRarity.name(), this.individualCaveRarity);
        WriteModSettings(BiomeTerrainValues.caveSystemFrequency.name(), this.caveSystemFrequency);
        WriteModSettings(BiomeTerrainValues.caveSystemPocketChance.name(), this.caveSystemPocketChance);
        WriteModSettings(BiomeTerrainValues.caveSystemPocketMinSize.name(), this.caveSystemPocketMinSize);
        WriteModSettings(BiomeTerrainValues.caveSystemPocketMaxSize.name(), this.caveSystemPocketMaxSize);
        WriteModSettings(BiomeTerrainValues.evenCaveDistribution.name(), this.evenCaveDistribution);




        WriteModTitleSettings("Start Terrain Variables :");
        WriteModSettings(BiomeTerrainValues.waterLevel.name(), this.waterLevel);
        WriteModSettings(BiomeTerrainValues.maxAverageHeight.name(), this.maxAverageHeight);
        WriteModSettings(BiomeTerrainValues.maxAverageDepth.name(), this.maxAverageDepth);
        WriteModSettings(BiomeTerrainValues.fractureHorizontal.name(), this.fractureHorizontal);
        WriteModSettings(BiomeTerrainValues.fractureVertical.name(), this.fractureVertical);
        WriteModSettings(BiomeTerrainValues.volatility1.name(), this.volatility1);
        WriteModSettings(BiomeTerrainValues.volatility2.name(), this.volatility2);
        WriteModSettings(BiomeTerrainValues.volatilityWeight1.name(), this.volatilityWeight1);
        WriteModSettings(BiomeTerrainValues.volatilityWeight2.name(), this.volatilityWeight2);
        WriteModSettings(BiomeTerrainValues.disableBedrock.name(), this.disableBedrock);
        WriteModSettings(BiomeTerrainValues.flatBedrock.name(), this.flatBedrock);
        WriteModSettings(BiomeTerrainValues.bedrockobsidian.name(), this.bedrockobsidian);


        WriteModTitleSettings("Replace Variables");
        WriteModSettings(BiomeTerrainValues.removeSurfaceStone.name(), this.removeSurfaceStone);

        WriteModReplaceSettings();
    }

    public void WriteModSettings(String settingsName, int settingsValue) throws IOException
    {
        this.SettingsWriter.write(settingsName + ":" + Integer.toString(settingsValue));
        this.SettingsWriter.newLine();
    }

    void WriteModSettings(String settingsName, double settingsValue) throws IOException
    {
        this.SettingsWriter.write(settingsName + ":" + Double.toString(settingsValue));
        this.SettingsWriter.newLine();
    }

    public void WriteModSettings(String settingsName, boolean settingsValue) throws IOException
    {
        this.SettingsWriter.write(settingsName + ":" + Boolean.toString(settingsValue));
        this.SettingsWriter.newLine();
    }

    void WriteModSettings(String settingsName, String settingsValue) throws IOException
    {
        this.SettingsWriter.write(settingsName + ":" + settingsValue);
        this.SettingsWriter.newLine();
    }

    public void WriteModTitleSettings(String title) throws IOException
    {
        this.SettingsWriter.newLine();
        this.SettingsWriter.write("<" + title + ">");
        this.SettingsWriter.newLine();
    }

    private void WriteModReplaceSettings() throws IOException
    {

        if (this.replaceBlocks.size() == 0)
        {
            this.WriteModSettings("ReplacedBlocks", "None");
            return;
        }
        String output = "";
        Iterator<Entry<Byte, Byte>> i = this.replaceBlocks.entrySet().iterator();
        while (i.hasNext())
        {
            Map.Entry<Byte, Byte> me = i.next();

            output += me.getKey().toString() + "=" + me.getValue().toString();
            if (i.hasNext())
                output += ",";
        }
        this.WriteModSettings("ReplacedBlocks", output);
    }

    public void processChunkBlocks(byte[] blocks, BiomeBase[] biomes)
    {

        for (int x = 0; x < BiomeTerrainValues.xLimit.intValue(); x++)
            for (int z = 0; z < BiomeTerrainValues.zLimit.intValue(); z++)
            {
                BiomeBase currentBiome = biomes[(x * BiomeTerrainValues.zLimit.intValue() + z)];
                for (int y = 0; y < BiomeTerrainValues.yLimit.intValue(); y++)
                {
                    int block = y + z * BiomeTerrainValues.yLimit.intValue() + x * BiomeTerrainValues.yLimit.intValue() * BiomeTerrainValues.zLimit.intValue();
                    if (block >= BiomeTerrainValues.maxChunkBlockValue.intValue() - 1)
                        continue;
                    if ((this.removeSurfaceDirtFromDesert) && (currentBiome == BiomeBase.DESERT) && ((blocks[block] == Block.GRASS.id) || (blocks[block] == Block.DIRT.id)) && (blocks[(block + 1)] == 0)) // next
                        // block
                        // is
                        // air
                        blocks[block] = (byte) Block.SAND.id;
                    if ((this.desertDirt) && (currentBiome == BiomeBase.DESERT) && (this.desertDirtFrequency > 0) && (this.rand.nextInt(this.desertDirtFrequency * BiomeTerrainValues.xLimit.intValue() * BiomeTerrainValues.zLimit.intValue()) == 0) && (blocks[block] == Block.SAND.id) && (blocks[(block + 1)] == 0)) // next
                        // block
                        // is
                        // air
                        blocks[block] = (byte) Block.DIRT.id;
                    if ((this.waterlessDeserts) && (currentBiome == BiomeBase.DESERT) && ((blocks[block] == Block.STATIONARY_WATER.id) || (blocks[block] == Block.ICE.id)))
                        blocks[block] = (byte) Block.SAND.id;
                    if (((this.muddySwamps) || (this.claySwamps)) && (currentBiome == BiomeBase.SWAMPLAND) && ((blocks[block] == Block.SAND.id) || (blocks[block] == Block.DIRT.id) || (blocks[block] == Block.SAND.id)))
                        createSwamps(blocks, block);
                    if ((this.removeSurfaceStone) && (blocks[block] == Block.STONE.id) && (blocks[(block + 1)] == 0)) // next
                        // block
                        // is
                        // air
                        blocks[block] = findNearestStoneReplacement(blocks, block, currentBiome);

                }
            }
    }



    private byte findNearestStoneReplacement(byte[] blocks, int block, BiomeBase currentBiome)
    {
        for (int w = 1; w < 16; w++)
            for (int y = 0; y <= 2; y++)
                for (int x = -1 * w; x <= w; x++)
                    for (int z = -1 * w; z <= w; z++)
                    {
                        int newBlock = block + (y == 2 ? -1 : y) + z * BiomeTerrainValues.yLimit.intValue() + x * BiomeTerrainValues.yLimit.intValue() * BiomeTerrainValues.zLimit.intValue();
                        if ((newBlock < 0) || (newBlock > BiomeTerrainValues.maxChunkBlockValue.intValue() - 1))
                            continue;
                        if ((blocks[newBlock] == Block.GRASS.id) || (blocks[newBlock] == Block.DIRT.id) || (blocks[newBlock] == Block.SAND.id) || (blocks[newBlock] == Block.GRAVEL.id) || (blocks[newBlock] == Block.CLAY.id))
                            return blocks[newBlock];
                    }
        return (byte) ((currentBiome == BiomeBase.DESERT) || (currentBiome == BiomeBase.ICE_DESERT) ? Block.SAND.id : Block.GRASS.id);
    }


    public void ReplaceBlocks(int x, int z)
    {
        if (this.replaceBlocks.size() <= 0)
            return;

        byte[] blocks = this.localWorld.getChunkAt(x, z).b;

        for (int i = 0; i < blocks.length; i++)
        {
            if (this.replaceBlocks.containsKey(blocks[i]))
            {
                blocks[i] = this.replaceBlocks.get(blocks[i]);
            }
        }
    }

    private void createSwamps(byte[] blocks, int block)
    {
        int swampSize = this.swampSize < 0 ? 0 : this.swampSize > 15 ? 15 : this.swampSize;
        int Swamptype = (this.muddySwamps) ? Block.SOUL_SAND.id : Block.CLAY.id;

        if (this.muddySwamps && this.claySwamps)
        {
            Swamptype = (this.rand.nextBoolean()) ? Block.SOUL_SAND.id : Block.CLAY.id;
        }

        if (blocks[(block + 1)] == Block.STATIONARY_WATER.id)
        {

            blocks[block] = (byte) Swamptype;
            return;
        }

        for (int x = swampSize * -1; x < swampSize + 1; x++)
            for (int z = swampSize * -1; z < swampSize + 1; z++)
            {
                int newBlock = block + z * BiomeTerrainValues.yLimit.intValue() + x * BiomeTerrainValues.yLimit.intValue() * BiomeTerrainValues.zLimit.intValue();
                if ((newBlock < 0) || (newBlock > BiomeTerrainValues.maxChunkBlockValue.intValue() - 1))
                    continue;
                if (blocks[newBlock] != Block.STATIONARY_WATER.id)
                    continue;
                blocks[block] = (byte) Swamptype;
                return;
            }
    }




    public boolean getNotUseOldGen()
    {
        return !this.oldGen;
    }

    public double getBiomeSize()
    {
        return this.biomeSize;
    }

    public double getMinimumTemperature()
    {
        return this.minTemperature;
    }

    public double getMaximumTemperature()
    {
        return this.maxTemperature;
    }

    public double getMinimumMoisture()
    {
        return this.minMoisture;
    }

    public double getMaximumMoisture()
    {
        return this.maxMoisture;
    }

    public double getSnowThreshold()
    {
        return this.snowThreshold;
    }

    public double getIceThreshold()
    {
        return this.iceThreshold;
    }

    public int getCaveRarity()
    {
        return this.caveRarity;
    }

    public int getCaveFrequency()
    {
        return this.caveFrequency;
    }

    public int getCaveMinAltitude()
    {
        return this.caveMinAltitude;
    }

    public int getCaveMaxAltitude()
    {
        return this.caveMaxAltitude;
    }

    public int getIndividualCaveRarity()
    {
        return this.individualCaveRarity;
    }

    public int getCaveSystemFrequency()
    {
        return this.caveSystemFrequency;
    }

    public int getCaveSystemPocketChance()
    {
        return this.caveSystemPocketChance;
    }

    public int getCaveSystemPocketMinSize()
    {
        return this.caveSystemPocketMinSize;
    }

    public int getCaveSystemPocketMaxSize()
    {
        return this.caveSystemPocketMaxSize;
    }

    public boolean getEvenCaveDistribution()
    {
        return this.evenCaveDistribution;
    }



    public int getWaterLevel()
    {
        return this.waterLevel;
    }

    public double getMaxAverageHeight()
    {
        return this.maxAverageHeight;
    }

    public double getMaxAverageDepth()
    {
        return this.maxAverageDepth;
    }

    public double getFractureHorizontal()
    {
        return this.fractureHorizontal < 0.0D ? 1.0D / (Math.abs(this.fractureHorizontal) + 1.0D) : this.fractureHorizontal + 1.0D;
    }

    public double getFractureVertical()
    {
        return this.fractureVertical < 0.0D ? 1.0D / (Math.abs(this.fractureVertical) + 1.0D) : this.fractureVertical + 1.0D;
    }

    public double getVolatility1()
    {
        return this.volatility1 < 0.0D ? 1.0D / (Math.abs(this.volatility1) + 1.0D) : this.volatility1 + 1.0D;
    }

    public double getVolatility2()
    {
        return this.volatility2 < 0.0D ? 1.0D / (Math.abs(this.volatility2) + 1.0D) : this.volatility2 + 1.0D;
    }

    public double getVolatilityWeight1()
    {
        return (this.volatilityWeight1 - 0.5D) * 24.0D;
    }

    public double getVolatilityWeight2()
    {
        return (0.5D - this.volatilityWeight2) * 24.0D;
    }

    public boolean createadminium(int y)
    {
        return (!this.disableBedrock) && ((!this.flatBedrock) || ((this.flatBedrock) && (y == 0)));
    }

    public byte getadminium()
    {
        return (byte) (this.bedrockobsidian ? Block.OBSIDIAN.id : Block.BEDROCK.id);
    }



}