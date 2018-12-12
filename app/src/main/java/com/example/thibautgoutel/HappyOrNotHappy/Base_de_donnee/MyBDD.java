package com.example.thibautgoutel.HappyOrNotHappy.Base_de_donnee;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.thibautgoutel.HappyOrNotHappy.Main.MainActivity;

import java.util.ArrayList;
import java.util.List;

public class MyBDD extends SQLiteOpenHelper {

    //Nom de la base de donnée
    private static final String DATABASE_NAME = "HappyOrNotHappy";

    //Version de la base de donnée
    private static final int DATABASE_VERSION = 1;

    //Nom de la table associé à la base de donnée
    private static final String TABLE_Application = "mood";

    public static String getTableName() {
        return TABLE_Application;
    }

    //Nom de chaque colonne de la base de données
    private static final String ID = "ID";
    private static final String ID_USER = "id_user";
    private static final String Mood = "Mood";
    private static final String Hour = "heure";

    // Commande sql pour la création de la base de donnée
    private static final String DATABASE_CREATE_APPLICATIONS = "create table "
            + TABLE_Application + "(" +
            ID + " integer primary key autoincrement, " +
            ID_USER + " TEXT, " +
            Mood + " TEXT, " +
            Hour + " TEXT);";

    //Contructeur
    public MyBDD(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        //Creation de la base de donnée
        database.execSQL(DATABASE_CREATE_APPLICATIONS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MyBDD.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_Application);
        onCreate(db);
    }

    //Ajout d'une humeur à la base de donnée
    public void addMood(MOOD mood){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ID_USER, mood.getUser());
        values.put(Mood, mood.getMood());
        values.put(Hour, mood.getHeure());
        db.insert(TABLE_Application, null, values);
        db.close();
    }

    //Suppression d'une humeur de la base de donnée
    public void deletMood(MOOD mood) {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] whereArgs = {mood.getId()+""};
        db.delete(TABLE_Application, ID + " = ?", whereArgs);
        db.close();
    }

    //Suppression de toute la base de donnée
    public void deletAll() {
        String countQuery = "DELETE FROM " + TABLE_Application;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        db.close();
    }

    //Renvoit une humeur de la base de donnée
    public MOOD getMood(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        MOOD mood = new MOOD(MainActivity.getId_user());
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_Application + " WHERE " + ID + "=" + id, null);
        if (c.moveToFirst()){
            mood.setId(c.getInt(c.getColumnIndex(ID)));
            mood.setMood(c.getString(c.getColumnIndex(Mood)));
            mood.setHeure(c.getString(c.getColumnIndex(Hour)));
            mood.setUser(c.getString(c.getColumnIndex(ID_USER)));
        }
        return mood;
    }

    //Renvoit de toutes les humeurs de la base de donnée
    public List<MOOD> getMood() {
        List<MOOD> moodList = new ArrayList<MOOD>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_Application, null);
        if (c.moveToFirst()) {
            do {
                MOOD mood = new MOOD(MainActivity.getId_user());
                mood.setId(c.getInt(c.getColumnIndex(ID)));
                mood.setMood(c.getString(c.getColumnIndex(Mood)));
                mood.setHeure(c.getString(c.getColumnIndex(Hour)));
                mood.setUser(c.getString(c.getColumnIndex(ID_USER)));
                moodList.add(mood);
            } while (c.moveToNext());
        }
        db.close();
        return moodList;
    }

    //Renvois le nombre d'humeurs de la base de donnée
    public int getMoodCount() {
        String countQuery = "SELECT * FROM " + TABLE_Application;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        return cursor.getCount();
    }
}