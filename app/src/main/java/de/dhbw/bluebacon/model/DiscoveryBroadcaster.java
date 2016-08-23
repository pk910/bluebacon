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
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import de.dhbw.bluebacon.BuildConfig;

public class DiscoveryBroadcaster extends AsyncTask<Void, Void, Void> {

    public static final int UDP_PORT = 9996;
    public static final String LOG_TAG = "DHBW Broadcaster";

    private static byte[] last_random_bytes = new byte[32];

    private final Context context;
    private AtomicBoolean gotOwnDatagram;

    public DiscoveryBroadcaster(Context context, AtomicBoolean gotOwnDatagram){
        this.context = context;
        this.gotOwnDatagram = gotOwnDatagram;
    }

    @Override
    protected Void doInBackground(Void... params){
        // gotOwnDatagram gets changed by DiscoveryListener. if true, we can be sure we're listening
        do{
            try {
                Thread.sleep(50);
            } catch (InterruptedException e){
                e.printStackTrace();
                return null;
            }
            try {
                sendUdp(InetAddress.getLocalHost(), false, new byte[]{});
            } catch(UnknownHostException e){
                Log.e(LOG_TAG, "Could not resolve localhost");
                return null;
            } catch(IOException e){
                Log.e(LOG_TAG, "Could not send datagram to localhost");
                return null;
            }
        } while(!this.gotOwnDatagram.get());

        Log.i(LOG_TAG, "Socket is listening, we can send the broadcast now ...");
        sendDiscoveryBroadcast();

        return null;
    }

    public void sendDiscovery(InetAddress toAddr, boolean so_broadcast){
        SecureRandom random = new SecureRandom();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        random.nextBytes(DiscoveryBroadcaster.last_random_bytes);

        try {
            outputStream.write(DiscoveryBroadcaster.last_random_bytes);
            byte sendData[] = outputStream.toByteArray();

            sendUdp(toAddr, so_broadcast, sendData);
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getClass() + ": " + e.getMessage());
        }
    }

    public void sendDiscovery(InetAddress toAddr){
        sendDiscovery(toAddr, false);
    }

    public void sendDiscoveryBroadcast() {
        try {
            sendDiscovery(getBroadcastAddress(), true);
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException: " + e.getMessage());
        }

    }

    public InetAddress getBroadcastAddress() throws IOException {
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

    private void sendUdp(InetAddress toAddr, boolean so_broadcast, byte[] sendData) throws IOException {
        //Open a random port to send the package
        DatagramSocket socket = new DatagramSocket();
        socket.setBroadcast(so_broadcast);

        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, toAddr, UDP_PORT);
        socket.send(sendPacket);
        if(BuildConfig.DEBUG) {
            Log.i(LOG_TAG, getClass() + " packet sent to: " + toAddr.getHostAddress());
        }
        socket.close();
    }
}
