package com.example.thibautgoutel.HappyOrNotHappy.Main;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.thibautgoutel.HappyOrNotHappy.Notification_receiver.AlarmReceiver;
import com.example.thibautgoutel.HappyOrNotHappy.R;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.FileInputStream;
import java.util.Calendar;

// TUTORIALS http://www.androidhive.info/2012/01/android-login-and-registration-with-php-mysql-and-sqlite/

public class MainActivity extends AppCompatActivity {

    private PendingIntent pendingIntent;
    private AlarmManager alarmManager;
    private static int intervalle = 6 * 60 * 60 * 1000;
    private static String id_user;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.RECEIVE_BOOT_COMPLETED,
                        Manifest.permission.WAKE_LOCK,
                        Manifest.permission.ACCESS_WIFI_STATE
                },
                1);

        //Creation de l'intervalle en SharedPreference pour qu'il soit accessible de chaques classe
        SharedPreferences settings = getSharedPreferences( "PrivateSettings" , Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = settings.edit();
        edit.putInt("intervalle", intervalle);
        edit.apply();

        //Creation de l'alarme
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        //Synchronisation du bouton pour activé ou desactivé la notification
        synchroNotification();

        //Synchronisation de la variable globale correspondant à l'id de l'utilisateur
        synchroIdUser();

        //Création de l'intent pour prévoir la notification et passage des variables
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("id_user", id_user);
        intent.putExtra("intervalle", intervalle);
        intent.putExtra("verif",true);
        intent.putExtra("service",false);
        pendingIntent = PendingIntent.getBroadcast( this.getApplicationContext(), 234324243, intent, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void synchroNotification()
    {
        ToggleButton toggleButton = findViewById(R.id.toggleButton);

        SharedPreferences settings = getSharedPreferences( "PrivateSettings" , Context.MODE_PRIVATE);
        String notif = settings.getString("notification", "error");

        if(notif.equals("error") || notif.equals("desactive"))
        {
            toggleButton.setChecked(false);
        }
        else if(notif.equals("active"))
        {
            toggleButton.setChecked(true);
        }
        else Log.e("MainActivity", "Erreur de preference");
    }

    public void synchroIdUser()
    {
        SharedPreferences settings = getSharedPreferences( "PrivateSettings" , Context.MODE_PRIVATE);
        String id = settings.getString("id_user", "error");

        TextView textView3 = findViewById(R.id.textView3);
        textView3.setText("id user : " + id);

        if(id.equals("error"))
        {
            Calendar calendar = Calendar.getInstance();
            id_user = String.valueOf(calendar.getTimeInMillis());

            SharedPreferences.Editor edit = settings.edit();
            edit.putString("id_user", id_user);
            edit.apply();
        }
        else
        {
            id_user = id;
        }
    }

    public static String getId_user()
    {
        return id_user;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void OnToggleClicked(View view)
    {
        long time_real;
        if (((ToggleButton) view).isChecked())
        {
            //Reception de l'intervalle afficher
            EditText editIntervalle = findViewById(R.id.EditIntervalle);
            if(!editIntervalle.getText().toString().equals(""))
            {
                intervalle = Integer.parseInt(editIntervalle.getText().toString()) * 60 * 1000;
            }

            //Creation de l'intervalle en SharedPreference pour qu'il soit accessible de chaques classe
            SharedPreferences settings = getSharedPreferences( "PrivateSettings" , Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = settings.edit();
            edit.putInt("intervalle", intervalle);
            edit.apply();

            edit.putString("notification", "active");
            edit.apply();

            Toast.makeText(MainActivity.this, "ALARM ON", Toast.LENGTH_SHORT).show();

            Calendar calendar = Calendar.getInstance();
            time_real = calendar.getTimeInMillis();

            Log.d("Intervalle = ", String.valueOf(intervalle));

            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, time_real, intervalle, pendingIntent);
        }
        else
        {
            SharedPreferences settings = getSharedPreferences( "PrivateSettings" , Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = settings.edit();
            edit.putString("notification", "desactive");
            edit.apply();

            Toast.makeText(MainActivity.this, "ALARM OFF", Toast.LENGTH_SHORT).show();

            //Création de l'intent pour prévoir la notification et passage des variables
            Intent intent = new Intent(this, AlarmReceiver.class);
            intent.putExtra("verif",false);
            pendingIntent = PendingIntent.getBroadcast( this.getApplicationContext(), 234324243, intent, 0);

            alarmManager.cancel(pendingIntent);
        }
    }
}