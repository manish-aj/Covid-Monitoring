package com.manish.covidMonitor;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

public class DataBaseHelper extends SQLiteOpenHelper {
    public static String DatabaseName;
    public static final String TABLE_NAME = "covid_monitor";
    SharedPreferences sharedPreferences;

    public DataBaseHelper(@Nullable Context context, @Nullable String databaseName) {
        super(context, databaseName, null, 1);
        this.DatabaseName = databaseName;
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table "+  TABLE_NAME+
                "(timestamp BIGINT PRIMARY KEY, heart_rate INTEGER, respiratory_rate INTEGER, nausea INTEGER," +
                " headache INTEGER, diarrhea INTEGER," + "soar_throat INTEGER, " +
                "fever INTEGER, muscle_ache INTEGER, loss_of_smell_or_taste INTEGER, " +
                "cough INTEGER, shortness_of_breath INTEGER, feeling_tired INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS monitor_data");
        onCreate(sqLiteDatabase);
    }

    public boolean insertData(long timestamp, int heartRate, int respiratoryRate, HashMap<String, Integer> symptoms){
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("timestamp", timestamp);
        contentValues.put("heart_rate", heartRate);
        contentValues.put("respiratory_rate", respiratoryRate);
        if (symptoms != null) {
            for(Map.Entry<String, Integer> entry : symptoms.entrySet()){
                contentValues.put(entry.getKey(),entry.getValue());
            }
        }

        database.insert(TABLE_NAME, "0", contentValues);
        return true;
    }

    public boolean insertOrUpdateData(long timestamp, int heartRate, int respiratoryRate, HashMap<String, Integer> symptoms){
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("timestamp", timestamp);

        if(heartRate!=0)
            contentValues.put("heart_rate", heartRate);
        if(respiratoryRate!=0)
            contentValues.put("respiratory_rate", respiratoryRate);
        if (symptoms != null) {
            for(Map.Entry<String, Integer> entry : symptoms.entrySet()){
                contentValues.put(entry.getKey(),entry.getValue());
            }
        }

        if(rowExists(timestamp)){
            database.update(TABLE_NAME, contentValues, "timestamp=?", new String[]{String.valueOf(timestamp)});
        }
        else{
            database.insert(TABLE_NAME, "0", contentValues);
        }

        return true;
    }

    public boolean updateSigns(long timestamp, int heartRate, int respiratoryRate){
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("heart_rate", heartRate);
        contentValues.put("respiratory_rate", respiratoryRate);
        database.update(TABLE_NAME, contentValues, "timestamp=?", new String[]{String.valueOf(timestamp)});
        return true;
    }

    public boolean updateSymptoms(long timestamp, HashMap<String, Integer> symptoms){
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        for(Map.Entry<String, Integer> entry : symptoms.entrySet()){
            contentValues.put(entry.getKey(),entry.getValue());
        }

        database.update(TABLE_NAME, contentValues, "timestamp=?", new String[]{String.valueOf(timestamp)});
        return true;
    }

    public boolean rowExists(long timestamp){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor response = db.rawQuery("SELECT * FROM "+ TABLE_NAME + " WHERE timestamp="+timestamp,null);
        if(response.isAfterLast()){
            return false;
        }
        return true;
    }

    public String getData(){
        SQLiteDatabase db = this.getReadableDatabase();
        String tableRow = "";
        Cursor response = db.rawQuery("SELECT * FROM "+ TABLE_NAME, null);

        for(response.moveToFirst(); !response.isAfterLast(); response.moveToNext()) {
            for (int i = 0; i < response.getColumnCount(); i++) {
                tableRow += response.getColumnName(i) + " : " + response.getString(i)+"; ";
            }
            tableRow+="\n";
        }
        return tableRow;
    }

    public int numberOfRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, TABLE_NAME);
        return numRows;
    }
}