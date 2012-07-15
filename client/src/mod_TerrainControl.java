import com.khorn.terraincontrol.configuration.TCDefaultValues;
import com.khorn.terraincontrol.configuration.WorldConfig;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class mod_TerrainControl extends BaseMod
{
    public static TCPlugin Plugin;
    private static boolean ServerConnected = false;
    private static adl NetworkHandler;

    @Override
    public String getVersion()
    {
        return "2.2";
    }

    @Override
    public String getName()
    {
        return "TerrainControl";
    }

    @Override
    public void load()
    {
        ModLoader.addLocalization("generator.TerrainControl", "TerrainControl");
        Plugin = new TCPlugin(4, "TerrainControl");

        ModLoader.registerPacketChannel(this, TCDefaultValues.ChannelName.stringValue());
    }

    @Override
    public void receiveCustomPacket(ee packet)
    {
        if (!packet.a.equals(TCDefaultValues.ChannelName.stringValue()))
            return;

        xd world = ModLoader.getMinecraftInstance().f;
        SingleWorld TCWorld = new SingleWorld(world.x.j());
        try
        {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(packet.c);
            DataInputStream stream = new DataInputStream(inputStream);
            WorldConfig config = new WorldConfig(stream, TCWorld);

            TCWorld.setSettings(config);
            TCWorld.InitM(world);

            //this.c = new BiomeManager(this.world);
            System.out.println("TerrainControl: config received");

        } catch (IOException e1)
        {
            e1.printStackTrace();
        }
    }

    @Override
    public void serverConnect(adl handler)
    {
        ServerConnected = true;
        NetworkHandler = handler;
        SendTCPacker();

    }

    @Override
    public void serverDisconnect()
    {
        ServerConnected = false;
        NetworkHandler = null;
    }

    public static void NewWorldCreated()
    {
        SingleWorld.RestoreBiomes();
        if (ServerConnected)
            SendTCPacker();

    }

    private static void SendTCPacker()
    {
        ee packet = new ee();
        packet.a = TCDefaultValues.ChannelName.stringValue();
        byte[] data = new byte[]{(byte) TCDefaultValues.ProtocolVersion.intValue()};

        packet.c = data;
        packet.b = data.length;
        NetworkHandler.c(packet);
    }

    /*
    Method from ChunkProvider for fog.

     public bo b(float paramFloat1, float paramFloat2) {
        float f1 = gk.b(paramFloat1 * 3.141593F * 2.0F) * 2.0F + 0.5F;
        if (f1 < 0.0F) f1 = 0.0F;
        if (f1 > 1.0F) f1 = 1.0F;

        if (this.world == null)
        {

            float f2 = 0.7529412F;    //r
            float f3 = 0.8470588F;    //g
            float f4 = 1.0F;          //b

            f2 *= (f1 * 0.94F + 0.06F);
            f3 *= (f1 * 0.94F + 0.06F);
            f4 *= (f1 * 0.91F + 0.09F);
            return bo.b(f2, f3, f4);
        } else
        {

            WorldConfig config = this.world.getSettings();

            float red = (config.WorldFogR*f1 + config.WorldNightFogR*(1-f1));
            float green = (config.WorldFogG*f1 + config.WorldNightFogG*(1-f1));
            float blue = (config.WorldFogB*f1 + config.WorldNightFogB*(1-f1));

            return bo.b(red, green, blue);

        }
    }
     */
}
