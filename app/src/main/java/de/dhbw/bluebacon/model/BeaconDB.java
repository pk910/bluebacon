package de.dhbw.bluebacon.model;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

public class BeaconDB {
    public static final String LOG_TAG = "BeaconDB";
    public static final String dbFileName = "beacondb.sqlite";

    private File dbFile;
    private SQLiteDatabase sqlite_db;
    private final int codedSchemaVersion = 1;
    private Context context;


    public BeaconDB(Context context){
        this.context = context;
        openDB();

        if(this.sqlite_db.needUpgrade(codedSchemaVersion)){
            Log.i(LOG_TAG, String.format("DB schema version changed (%d -> %d)", this.sqlite_db.getVersion(), codedSchemaVersion));
            updateSchema();
        }
    }

    private void openDB(){
        this.dbFile = new File(context.getFilesDir().getAbsolutePath() + "/" + BeaconDB.dbFileName);
        this.sqlite_db = SQLiteDatabase.openOrCreateDatabase(this.dbFile, null);
    }

    private void updateSchema(){
        Log.i(LOG_TAG, "(Re)Populating sqlite database...");
        this.sqlite_db.close();
        if(this.dbFile.delete()){
            openDB();
            sqlite_db.execSQL("CREATE TABLE beacons (" +
                    "beaconid INTEGER PRIMARY KEY, " +
                    "uuid TEXT NOT NULL, " +
                    "major INTEGER NOT NULL, " +
                    "minor INTEGER NOT NULL, " +
                    "machineid INTEGER NOT NULL);");
            sqlite_db.execSQL("CREATE TABLE machines (" +
                    "machineid INTEGER PRIMARY KEY, " +
                    "machinename TEXT NOT NULL, " +
                    "description TEXT NOT NULL, " +
                    "maintenancestatus TEXT NOT NULL, " +
                    "productionstatus TEXT NOT NULL, " +
                    "added INTEGER NOT NULL);");

            sqlite_db.setVersion(codedSchemaVersion);
            Log.i(LOG_TAG, "(Re)Populating sqlite database: success.");
        } else {
            Log.i(LOG_TAG, "(Re)Populating sqlite database: old DB could not be deleted.");
        }
    }

    public void saveMachine(Machine machine){
        SQLiteStatement stmt = sqlite_db.compileStatement("INSERT INTO machines (" +
                "machinename, " +
                "description, " +
                "maintenancestatus, " +
                "productionstatus, " +
                "added) VALUES (?, ?, ?, ?, 0)");
        stmt.bindString(1, machine.getName());
        stmt.bindString(2, machine.getDescription());
        stmt.bindString(3, machine.getMaintenanceState());
        stmt.bindString(4, machine.getProductionState());

        stmt.execute();
        stmt.close();
    }

    public void saveMachines(Machine[] machines){
        for(Machine machine : machines){
            saveMachine(machine);
        }
    }

    public boolean deleteMachine(int machineId){
        boolean retVal = false;

        Cursor rs = sqlite_db.rawQuery("SELECT count(*) FROM machines WHERE machineid = ?;", new String[]{String.valueOf(machineId)});
        int count = 0;
        while(rs.moveToNext()){
            count = rs.getInt(0);
        }
        rs.close();
        if(count > 0){
            Log.v(LOG_TAG, "deleteMachine: id \"" + machineId + "\" found, deleting..");
            retVal = true;
        } else {
            Log.v(LOG_TAG, "deleteMachine: id \"" + machineId + "\" not found.");
        }

        sqlite_db.delete("machines", "machineid=?", new String[]{String.valueOf(machineId)});
        return retVal;
    }

    public void clearMachines(){
        sqlite_db.delete("machines", "1=1", null);
    }

    public Machine getMachine(int machineId){
        Cursor rs = sqlite_db.rawQuery("SELECT * FROM machines WHERE machineid = ?;", new String[]{String.valueOf(machineId)});
        if(rs.moveToNext()){
            return new Machine(
                    rs.getInt(1), // id
                    rs.getString(2), // name
                    rs.getString(3), // description
                    rs.getString(4), // maintenance status
                    rs.getString(5) // production status
            );
        }
        rs.close();
        return null;
    }

    public ArrayList<Machine> getMachines(){
        ArrayList<Machine> result = new ArrayList<>();
        Cursor rs = sqlite_db.rawQuery("SELECT machineid FROM machines;", null);
        while(rs.moveToNext()){
            result.add(getMachine(rs.getInt(0)));
        }
        rs.close();
        return result;
    }

    public void saveBeacon(BeaconData beacon){
        SQLiteStatement stmt = sqlite_db.compileStatement("INSERT INTO beacons (" +
                "uuid, " +
                "major, " +
                "minor, " +
                "posX, " +
                "posY, " +
                "machineid) VALUES (?, ?, ?, ?, ?, ?)");
        stmt.bindString(1, beacon.uuid);
        stmt.bindString(2, beacon.major);
        stmt.bindString(3, beacon.minor);
        stmt.bindDouble(4, beacon.posX);
        stmt.bindDouble(5, beacon.posY);
        stmt.bindLong(6, beacon.machineid < 1 ? 0 : beacon.machineid);

        stmt.execute();
        stmt.close();
    }

    public void saveBeacons(BeaconData[] beacons){
        for(BeaconData beacon : beacons){
            saveBeacon(beacon);
        }
    }

    public boolean deleteBeacon(int beaconId){
        boolean retVal = false;

        Cursor rs = sqlite_db.rawQuery("SELECT count(*) FROM beacons WHERE beaconid = ?;", new String[]{String.valueOf(beaconId)});
        int count = 0;
        while(rs.moveToNext()){
            count = rs.getInt(0);
        }
        rs.close();
        if(count > 0){
            Log.v(LOG_TAG, "deleteMachine: id \"" + beaconId + "\" found, deleting..");
            retVal = true;
        } else {
            Log.v(LOG_TAG, "deleteMachine: id \"" + beaconId + "\" not found.");
        }

        sqlite_db.delete("machines", "machineid=?", new String[]{String.valueOf(beaconId)});
        return retVal;
    }

    public void clearBeacons(){
        sqlite_db.delete("beacons", "1=1", null);
    }

    public BeaconData getBeacon(int beaconId){
        Cursor rs = sqlite_db.rawQuery("SELECT * FROM beacons WHERE beaconid = ?;", new String[]{String.valueOf(beaconId)});
        if(rs.moveToNext()){
            return new BeaconData(
                    rs.getString(2), // uuid
                    rs.getString(3), // majoir
                    rs.getString(4), // minor
                    rs.getDouble(5), // posX
                    rs.getDouble(6), // posY
                    rs.getInt(7) // machineID
            );
        }
        rs.close();
        return null;
    }

    public ArrayList<BeaconData> getBeacons(){
        ArrayList<BeaconData> result = new ArrayList<>();
        Cursor rs = sqlite_db.rawQuery("SELECT beaconid FROM beacons;", null);
        while(rs.moveToNext()){
            result.add(getBeacon(rs.getInt(0)));
        }
        rs.close();
        return result;
    }
}
