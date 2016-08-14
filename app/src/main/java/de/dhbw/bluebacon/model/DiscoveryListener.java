package de.dhbw.bluebacon.model;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import de.dhbw.bluebacon.BuildConfig;
import de.dhbw.bluebacon.MainActivity;
import de.dhbw.bluebacon.R;

public class DiscoveryListener extends AsyncTask<Void, Void, String> {

    public static final String LOG_TAG = "DHBW DiscoveryListener";
    public static final int SOCKET_TIMEOUT_MILLIS = 2000;

    Context context;
    DatagramSocket socket;
    private final String HMAC_SECRET = "eFqqDnFNeLLJ";

    public DiscoveryListener(Context context){
        this.context = context;
    }

    @Override
    protected String doInBackground(Void... params){
        return listen();
    }

    @Override
    protected void onPostExecute(String hostname) {
        if(hostname == null){
            Log.i(LOG_TAG, "UDP discovery: we got no answer");
            boolean preferRemoteServer = ((MainActivity)context).prefs.getBoolean(MainActivity.PrefKeys.SERVER_LOCATION_PRIORITY.toString(), true);
            if(preferRemoteServer){
                Log.e(LOG_TAG, "No local and/or remote servers could be reached.");
                ((MainActivity)context).progressHide();
                Toast.makeText(context, context.getString(R.string.no_server_found), Toast.LENGTH_LONG).show();
            } else {
                // use JSONLoader within new thread and don't try to discover a local server again
                Log.i(LOG_TAG, "No local server found, trying remote server...");
                new JSONLoader(context).execute();
            }

        } else {
            Log.i(LOG_TAG, "UDP discovery: got answer from: " + hostname);
            ((MainActivity)context).prefs.edit().putString(MainActivity.PrefKeys.SERVER_ADDR.toString(), hostname).commit();
            Toast.makeText(context, context.getString(R.string.found_local_server), Toast.LENGTH_LONG).show();
            // we have found our server and can contact it via JSONLoader now
            new JSONLoader(context, false).execute(hostname);
        }
    }

    public String listen(){
        WifiManager wifi = (WifiManager) this.context.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        String ipaddr = Formatter.formatIpAddress(dhcp.ipAddress);
        try {
            //Keep a socket open to listen to all UDP traffic that is destined for this port
            socket = new DatagramSocket(DiscoveryBroadcaster.UDP_PORT, InetAddress.getByName("0.0.0.0"));
            socket.setBroadcast(true);
            socket.setSoTimeout(SOCKET_TIMEOUT_MILLIS);

            Log.i(LOG_TAG, "Ready to receive packet");

            //Receive a packet
            byte[] recvBuf = new byte[100];
            DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);

            do{
                socket.receive(packet);
                // ignore our own packets and wait for a next one
                // could use a more elegant way to compare ip addresses..
            } while(packet.getAddress().getHostAddress().equals(ipaddr));

            //Packet received
            if(BuildConfig.DEBUG){
                Log.i(LOG_TAG, "Packet with " + packet.getData().length + " bytes data received from: " + packet.getAddress().getHostAddress());
            }

            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret = new SecretKeySpec(HMAC_SECRET.getBytes(),"HmacSHA256");
            mac.init(secret);
            byte[] digest = mac.doFinal(DiscoveryBroadcaster.last_random_bytes);
            if(!Arrays.equals(packet.getData(), digest)){
                Log.i(LOG_TAG, "Got invalid identifier");
                return null;
            } else if(!packet.getAddress().isSiteLocalAddress()) {
                Log.i(LOG_TAG, "Packet did not come from local address!");
                return null;
            }
            return packet.getAddress().getHostName();

        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            if(BuildConfig.DEBUG && e.getMessage() != null) {
                Log.e(LOG_TAG, e.getMessage());
            }
        } finally {
            socket.close();
        }
        return null;
    }

    public static int packIpv4Addr(byte[] bytes){
        int val = 0;
        for (int i = 0; i < bytes.length; i++) {
            val <<= 8;
            val |= bytes[i] & 0xff;
        }
        return val;
    }

    public static byte[] unpackIpv4Addr(int bytes){
        return new byte[] {
                (byte)((bytes >>> 24) & 0xff),
                (byte)((bytes >>> 16) & 0xff),
                (byte)((bytes >>>  8) & 0xff),
                (byte)((bytes       ) & 0xff)
        };
    }

}
