import com.khorn.terraincontrol.configuration.TCDefaultValues;
import net.minecraft.client.Minecraft;


import java.lang.reflect.Field;
import java.util.HashMap;

public class TCClient
{
    private static alb WorldProvider;

    public static boolean CheckWorld(alb worldProvider)
    {
        WorldProvider = worldProvider;
        adl netHandler = GetNetworkHandler();

        if (netHandler != null)
        {
            Replace250PacketToCustom();

            RegisterChanel(netHandler);
            SendTCVersion(netHandler);

        }

        return false;

    }

    public static void ReceiveConfig(ChannelPacket packet)
    {
        WorldProvider.InitTCBiomeManager(packet);
    }

    @SuppressWarnings("unchecked")
    public static void Replace250PacketToCustom()
    {
        try
        {
            Field packetsMapF = abs.class.getDeclaredField("a");

            packetsMapF.setAccessible(true);

            HashMap packetsMap = (HashMap) packetsMapF.get(null);

            packetsMap.remove(ee.class);
            packetsMap.put(ChannelPacket.class, 250);

            abs.k.d(250);
            abs.k.a(250,ChannelPacket.class);


        } catch (NoSuchFieldException e)
        {
            e.printStackTrace();
        } catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }



    }

    public static void RegisterChanel(adl netHandler)
    {
        ChannelPacket packet = new ChannelPacket();
        packet.a = "REGISTER";
        byte[] data = TCDefaultValues.ChannelName.stringValue().getBytes();

        packet.c = data;
        packet.b = data.length;
        netHandler.c(packet);

    }

    public static void SendTCVersion(adl netHandler)
    {

        ChannelPacket packet = new ChannelPacket();
        packet.a = TCDefaultValues.ChannelName.stringValue();
        byte[] data = new byte[]{(byte)TCDefaultValues.ProtocolVersion.intValue()};

        packet.c = data;
        packet.b = data.length;
        netHandler.c(packet);
    }


    public static adl GetNetworkHandler()
    {

        try
        {
            Field minecraft = Minecraft.class.getDeclaredField("a");

            minecraft.setAccessible(true);

            Minecraft m = (Minecraft) minecraft.get(null);

            if (m.c instanceof rk)
            {

                Field handler = rk.class.getDeclaredField("l");

                handler.setAccessible(true);

                return (adl) handler.get(m.c);
            }
            return null;


        } catch (NoSuchFieldException e)
        {
            e.printStackTrace();
        } catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
        return null;

    }


}
