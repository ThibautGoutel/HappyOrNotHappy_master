package com.example.thibautgoutel.HappyOrNotHappy.Base_de_donnee;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class Send extends AsyncTask<String,String,String>
{
    private static final String DB_URL = "jdbc:mysql://162.38.134.172/wastedtime";
    private static final String USER = "wastedtime";
    private static final String PASS = "Proj92!emi";

    @Override
    protected void onPreExecute() {}

    @Override
    protected String doInBackground(String... strings)
    {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
            if (conn == null) {
                //TODO//On a pas de connection internet ou l'envoi n'a pas marché
            } else {
                String query = "INSERT INTO HappyOrNotHappy(NAME, MOOD, DATE) VALUES('" + strings[0] + "' , '" + strings[1] + "' , '"  + strings[2] + "')";  //text
                Statement stmt = conn.createStatement();
                stmt.execute(query);
            }
            conn.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return strings[0];
    }

    @Override
    protected void onPostExecute(String msg)
    {
    }
}