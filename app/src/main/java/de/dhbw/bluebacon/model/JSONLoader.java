package de.dhbw.bluebacon.model;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;

import de.dhbw.bluebacon.MainActivity;
import de.dhbw.bluebacon.R;

/**
 * Loader for data loading
 */
public class JSONLoader extends AsyncTask<String, Void, Void> {

    protected Context context;
    public static final String SERVER_URL = "http://example.com";
    public static final String LOG_TAG = "DHBW JSONLoader";
    private boolean success;
    private final boolean try_discovery;

    public JSONLoader(Context context){
        this.context = context;
        this.success = false;
        this.try_discovery = true;
    }

    public JSONLoader(Context context, boolean try_discovery){
        this.context = context;
        this.success = false;
        this.try_discovery = try_discovery;
    }

    @Override
    protected Void doInBackground(String... params){
        String url = null;
        if(params.length > 0){
            url = params[0];
        }
        String result = getJSON(url);
        try {
            Tuple<BeaconData[], Machine[]> parsed = parseJSON(result);
            save(parsed.getX(), parsed.getY());
            success = true;
        } catch(JSONException e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void params) {
        ((MainActivity)context).getBlueBaconManager().loadMachines();
        boolean preferRemoteServer = ((MainActivity)context).prefs.getBoolean(MainActivity.PrefKeys.SERVER_LOCATION_PRIORITY.toString(), true);
        if(success){
            Log.i(LOG_TAG, "Success.");
            ((MainActivity)context).progressHide();
            Toast.makeText(context, context.getString(R.string.update_success), Toast.LENGTH_LONG).show();
        } else {
            // if we prefer the remote server and couldn't contact it, try local server discovery now.
            // don't do it though if we prefer the remote server, which failed, then discovered a local server,
            // which nevertheless failed later in the HTTP stage (this would possibly create an infinite loop)
            if(preferRemoteServer && try_discovery){
                Log.i(LOG_TAG, "Could not contact remote server, trying to discover local server...");
                ((MainActivity)context).progressShow(context.getString(R.string.discovering_server));
                new DiscoveryListener(context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                new DiscoveryBroadcaster(context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                Log.e(LOG_TAG, "No local and/or remote servers could be reached.");
                Toast.makeText(context, context.getString(R.string.no_server_found), Toast.LENGTH_LONG).show();
                ((MainActivity)context).progressHide();
            }
        }
    }

    /**
     * Get JSON without params
     * @param url Address to load beacons from
     */
    private String getJSON(String url) {
        String urlString = url == null ? SERVER_URL : url;
        Log.i(LOG_TAG, "Trying " + urlString + " ...");
        URLRequest request = new URLRequest(urlString);
        try{
            request.exec();
            if(request.getHTTPStatus() == URLRequest.HTTP_OK){
                return request.getResult();
            } else {
                Log.e(LOG_TAG, "Server returned status code != " + URLRequest.HTTP_OK + ".");
                return "";
            }
        } catch(ConnectException e){
            Log.e(LOG_TAG, "Could not connect to: '" + url + "'");
            e.printStackTrace();
        } catch(IOException e){
            e.printStackTrace();
        }

        return "";
    }

    private Tuple<BeaconData[], Machine[]> parseJSON(String json) throws JSONException {
        ArrayList<BeaconData> beacons = new ArrayList<>();
        ArrayList<Machine> machines = new ArrayList<>();

        JSONObject jo = new JSONObject(json);
        JSONArray machinesJSON = jo.getJSONArray("machines");
        JSONArray beaconsJSON = jo.getJSONArray("beacons");

        for(int i = 0; i < beaconsJSON.length(); i++){
            beacons.add(new BeaconData(
                    beaconsJSON.getJSONObject(i).getString("UUID"),
                    beaconsJSON.getJSONObject(i).getInt("Major"),
                    beaconsJSON.getJSONObject(i).getInt("Minor"),
                    beaconsJSON.getJSONObject(i).getDouble("PositionX"),
                    beaconsJSON.getJSONObject(i).getDouble("PositionY"),
                    beaconsJSON.getJSONObject(i).getInt("MachineID")
            ));
        }
        for(int i = 0; i < machinesJSON.length(); i++){
            machines.add(new Machine(
                    machinesJSON.getJSONObject(i).getInt("MachineID"),
                    machinesJSON.getJSONObject(i).getString("Name"),
                    machinesJSON.getJSONObject(i).getString("Description"),
                    machinesJSON.getJSONObject(i).getString("Maintenancestatus"),
                    machinesJSON.getJSONObject(i).getString("Productionstatus")
            ));
        }

        BeaconData[] beaconsResult = new BeaconData[beacons.size()];
        Machine[] machinesResult = new Machine[machines.size()];
        return new Tuple<>(beacons.toArray(beaconsResult), machines.toArray(machinesResult));
    }

    @SuppressWarnings("PMD.UseVarargs")
    private void save(BeaconData[] beacons, Machine[] machines){
        ((MainActivity)context).getBeaconDB().clearBeacons();
        ((MainActivity)context).getBeaconDB().clearMachines();
        ((MainActivity)context).getBeaconDB().saveBeacons(beacons);
        ((MainActivity)context).getBeaconDB().saveMachines(machines);
    }

}
