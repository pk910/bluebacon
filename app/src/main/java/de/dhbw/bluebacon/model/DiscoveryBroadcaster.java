package de.dhbw.bluebacon.model;


import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.security.SecureRandom;
import java.util.Arrays;

import de.dhbw.bluebacon.BuildConfig;

public class DiscoveryBroadcaster extends AsyncTask<Void, Void, Void> {

    public static final int UDP_PORT = 9996;
    public static final int SCAN_VIA_BROADCAST = 0;
    public static final String LOG_TAG = "DHBW Broadcaster";

    private static byte[] last_random_bytes = new byte[32];

    private final Context context;

    public DiscoveryBroadcaster(Context context){
        this.context = context;
    }

    @Override
    protected Void doInBackground(Void... params){
        // sleep a little bit to make sure we're actually listening when we send the broadcast packet
        try {
            Thread.sleep(100);
        } catch (InterruptedException e){
            e.printStackTrace();
        }

        sendUdpBroadcast();

        return null;
    }

    public void sendUdp(InetAddress toAddr, boolean so_broadcast){
        SecureRandom random = new SecureRandom();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        random.nextBytes(DiscoveryBroadcaster.last_random_bytes);

        try {
            //Open a random port to send the package
            DatagramSocket socket = new DatagramSocket();
            socket.setBroadcast(so_broadcast);

            outputStream.write(DiscoveryBroadcaster.last_random_bytes);

            byte sendData[] = outputStream.toByteArray();

            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, toAddr, UDP_PORT);
            socket.send(sendPacket);
            if(BuildConfig.DEBUG) {
                Log.i(LOG_TAG, getClass() + " packet sent to: " + toAddr.getHostAddress());
            }
            socket.close();
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getClass() + ": " + e.getMessage());
        }
    }

    public void sendUdp(InetAddress toAddr){
        sendUdp(toAddr, false);
    }

    public void sendUdpBroadcast() {
        try {
            sendUdp(getBroadcastAddress(), true);
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException: " + e.getMessage());
        }

    }

    InetAddress getBroadcastAddress() throws IOException {
        WifiManager wifi = (WifiManager) this.context.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        // handle null somehow

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        //Log.v("DHBW", String.format("ipAddr: %d, netmask: %d, broadcast: %d", dhcp.ipAddress, dhcp.netmask, broadcast));
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++) {
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        }
        return InetAddress.getByAddress(quads);
    }

    public static byte[] getLastRandomBytes(){
        return Arrays.copyOf(DiscoveryBroadcaster.last_random_bytes, DiscoveryBroadcaster.last_random_bytes.length);
    }
}
