import com.khorn.terraincontrol.configuration.TCDefaultValues;
import com.khorn.terraincontrol.configuration.WorldConfig;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class mod_TerrainControl extends BaseMod
{
    public static TCPlugin Plugin;
    public static asu PacketHandler;

    @Override
    public String getVersion()
    {
        return "2.2.4";
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
    public void clientCustomPayload(asu clientHandler, ce packet)
    {
        if (!packet.a.equals(TCDefaultValues.ChannelName.stringValue()))
            return;

        atc world = ModLoader.getMinecraftInstance().e;
        SingleWorld TCWorld = new SingleWorld(world.A.j());
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
    public void serverCustomPayload(gy serverHandler, ce packet)
    {
        /*
        if (packet.c.length == 1)
        {
            if (packet.c[0] == TCDefaultValues.ProtocolVersion.intValue())
            {
                WorldConfig config = Plugin.TCWorld.getSettings();

                System.out.println("TerrainControl: client config requested for world " + config.WorldName);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                DataOutputStream stream = new DataOutputStream(outputStream);

                try
                {
                    config.Serialize(stream);
                    stream.flush();

                } catch (IOException e)
                {
                    e.printStackTrace();
                }

                byte[] data = outputStream.toByteArray();

                ce packetOut = new ce();
                packet.a = TCDefaultValues.ChannelName.stringValue();
                packet.c = data;
                packet.b = data.length;
                ModLoader.serverSendPacket(serverHandler, packetOut);

            }
        }  */
    }

    @Override
    public void clientConnect(asu handler)
    {
        PacketHandler = handler;
        SendTCPacker();
    }


    public static void NewWorldCreated(uo world)
    {
        if (world instanceof gq && !(world instanceof gk))
            SingleWorld.RestoreBiomes();
        if (world instanceof atc)
            SendTCPacker();

    }

    private static void SendTCPacker()
    {
        try
        {
            if( PacketHandler == null  || ModLoader.getPrivateValue(asu.class,PacketHandler,"g") == null)
                return;
        } catch (NoSuchFieldException e)
        {
            e.printStackTrace();
            return;
        }

        System.out.println("TerrainControl: client config request send to server");
        ce packet = new ce();
        packet.a = TCDefaultValues.ChannelName.stringValue();
        byte[] data = new byte[]{(byte) TCDefaultValues.ProtocolVersion.intValue()};

        packet.c = data;
        packet.b = data.length;
        ModLoader.clientSendPacket(packet);
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
