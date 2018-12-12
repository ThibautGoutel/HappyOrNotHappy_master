package com.example.thibautgoutel.HappyOrNotHappy.Base_de_donnee;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class Send extends AsyncTask<String,String,String>
{
    private static final String DB_URL = "jdbc:mysql://162.38.134.172/wastedtime";
    private static final String USER = "wastedtime";
    private static final String PASS = "Proj92!emi";
    private static MyBDD myBDD;

    @Override
    protected void onPreExecute() {}

    public static void setMyBDD(MyBDD myBDD_)
    {
        myBDD = myBDD_;
    }

    @Override
    protected String doInBackground(String... strings)
    {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
            if (conn == null) {
                //TODO//On a pas de connection internet ou l'envoi n'a pas marché
                Log.d("ERROR", "Pas de connection internet");
            } else {
                List<MOOD> tab_mood = new ArrayList<>();
                tab_mood.addAll(myBDD.getMood());
                String query = "INSERT INTO HappyOrNotHappy(NAME, MOOD, DATE) VALUES";  //text
                for(int i = 0; i < tab_mood.size() - 1; i++)
                {
                    query += "('" + tab_mood.get(i).getUser() + "' , '" + tab_mood.get(i).getMood() + "' , '"  + tab_mood.get(i).getHeure() + "'),";  //text
                }
                query += "('" + tab_mood.get(tab_mood.size() - 1).getUser() + "' , '" + tab_mood.get(tab_mood.size() - 1).getMood() + "' , '"  + tab_mood.get(tab_mood.size() - 1).getHeure() + "');";  //text
                myBDD.deletAll();
                Log.d("ERROR", query);
                Statement stmt = conn.createStatement();
                stmt.execute(query);

            }
            assert conn != null;
            conn.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    protected void onPostExecute(String msg)
    {
    }
}