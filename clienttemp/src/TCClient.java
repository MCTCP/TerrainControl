import com.khorn.terraincontrol.configuration.TCDefaultValues;
import net.minecraft.client.Minecraft;


import java.lang.reflect.Field;
import java.util.HashMap;

public class TCClient
{
    private static akv WorldProvider;

    public static boolean CheckWorld(akv worldProvider)
    {
        WorldProvider = worldProvider;
        adg netHandler = GetNetworkHandler();

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
            Field packetsMapF = abn.class.getDeclaredField("a");

            packetsMapF.setAccessible(true);

            HashMap packetsMap = (HashMap) packetsMapF.get(null);

            packetsMap.remove(dz.class);
            packetsMap.put(ChannelPacket.class, 250);

            abn.k.d(250);
            abn.k.a(250,ChannelPacket.class);


        } catch (NoSuchFieldException e)
        {
            e.printStackTrace();
        } catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }



    }

    public static void RegisterChanel(adg netHandler)
    {
        ChannelPacket packet = new ChannelPacket();
        packet.a = "REGISTER";
        byte[] data = TCDefaultValues.ChannelName.stringValue().getBytes();

        packet.c = data;
        packet.b = data.length;
        netHandler.c(packet);

    }

    public static void SendTCVersion(adg netHandler)
    {

        ChannelPacket packet = new ChannelPacket();
        packet.a = TCDefaultValues.ChannelName.stringValue();
        byte[] data = new byte[]{(byte)TCDefaultValues.ProtocolVersion.intValue()};

        packet.c = data;
        packet.b = data.length;
        netHandler.c(packet);
    }


    public static adg GetNetworkHandler()
    {

        try
        {
            Field minecraft = Minecraft.class.getDeclaredField("a");

            minecraft.setAccessible(true);

            Minecraft m = (Minecraft) minecraft.get(null);

            if (m.c instanceof rg)
            {

                Field handler = rg.class.getDeclaredField("l");

                handler.setAccessible(true);

                return (adg) handler.get(m.c);
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
