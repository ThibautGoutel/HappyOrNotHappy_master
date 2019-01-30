package com.example.thibautgoutel.HappyOrNotHappy.Main;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private PendingIntent pendingIntent;
    private AlarmManager alarmManager;
    //L'intervalle n'est plus utilisé mais je ne l'ai pas supprimé pour pouvoir le réutiliser au cas ou
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

        String intervalle_string = readData("intervalle");
        if (intervalle_string.equals("error"))
        {
            writeData("intervalle", String.valueOf(intervalle));
            EditText editIntervalle = findViewById(R.id.EditIntervalle);
            editIntervalle.setHint(String.valueOf(intervalle / 60 / 1000));
        }
        else
        {
            EditText editIntervalle = findViewById(R.id.EditIntervalle);
            editIntervalle.setHint(String.valueOf(Integer.parseInt(intervalle_string) / 60 / 1000));
        }

        Intent it = getIntent();

        //Creation de l'alarme
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        //Synchronisation du bouton pour activé ou desactivé la notification
        synchroNotification();

        //Synchronisation de la variable globale correspondant à l'id de l'utilisateur
        synchroIdUser();

        //Création de l'intent pour prévoir la notification et passage des variables
        Intent intent = new Intent(this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast( this.getApplicationContext(), 234324243, intent, 0);

        if(it.getBooleanExtra("reboot", false) && readData("notification").equals("active"))
        {
            intervalle = Integer.parseInt(readData("intervalle"));

            // Set the alarm to start at 7:45 AM
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, 7);
            calendar.set(Calendar.MINUTE, 45);
            calendar.add(Calendar.DAY_OF_YEAR, 1);

            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_HALF_DAY, pendingIntent);

            finish();
        }
        else
        {
            //Creation de l'intervalle en SharedPreference pour qu'il soit accessible de chaques classe
            writeData("intervalle", String.valueOf(intervalle));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void synchroNotification()
    {
        ToggleButton toggleButton = findViewById(R.id.toggleButton);

        String notif = readData("notification");

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

    RadioButton m;
    RadioButton f;
    Button save;
    Spinner year;
    AlertDialog alertDialog;
    View dialogView;

    public void synchroIdUser()
    {
        String id = readData("id_user");



        if(id.equals("error"))
        {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
// ...Irrelevant code for customizing the buttons and title
            LayoutInflater inflater = this.getLayoutInflater();
            dialogView = inflater.inflate(R.layout.alert_dialog, null);
            dialogBuilder.setView(dialogView);

            m = dialogView.findViewById(R.id.male);
            f = dialogView.findViewById(R.id.female);
            year = dialogView.findViewById(R.id.year);
            save = dialogView.findViewById(R.id.save);

            save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String genre;
                    String year_selected;

                    if(m.isSelected())
                    {
                        genre = "m";
                    }
                    else if (f.isSelected())
                    {
                        genre = "f";
                    }
                    else
                    {
                        alertDialog.show();
                        return;
                    }

                    year_selected = (String) year.getSelectedItem();

                    Calendar calendar = Calendar.getInstance();
                    id_user = String.valueOf(calendar.getTimeInMillis());

                    writeData("id_user", id_user + genre + year_selected);
                }
            });

            ArrayList<String> listYear = new ArrayList<String>();

            for (int i = Calendar.getInstance().get(Calendar.YEAR); i > Calendar.getInstance().get(Calendar.YEAR) - 100; i--)
            {
                listYear.add(String.valueOf(i));
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listYear);

            year.setAdapter(adapter);

            alertDialog = dialogBuilder.create();
            alertDialog.show();
        }
        else
        {
            id_user = id;
        }

        TextView textView3 = findViewById(R.id.textView3);
        textView3.setText("id user : " + id_user);
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
            writeData("intervalle", String.valueOf(intervalle));

            writeData("notification", "active");

            Toast.makeText(MainActivity.this, "ALARM ON", Toast.LENGTH_SHORT).show();

            // Set the alarm to start at 7:45 AM
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, 7);
            calendar.set(Calendar.MINUTE, 45);
            calendar.add(Calendar.DAY_OF_YEAR, 1);

            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_HALF_DAY, pendingIntent);
        }
        else
        {
            writeData("notification", "desactive");

            Toast.makeText(MainActivity.this, "ALARM OFF", Toast.LENGTH_SHORT).show();

            //Création de l'intent pour prévoir la notification et passage des variables
            Intent intent = new Intent(this, AlarmReceiver.class);
            pendingIntent = PendingIntent.getBroadcast( this.getApplicationContext(), 234324243, intent, 0);

            alarmManager.cancel(pendingIntent);
        }
    }

    public void writeData(String file,String data)
    {
        try {
            // Creates a file in the primary external storage space of the
            // current application.
            // If the file does not exists, it is created.
            File testFile = new File(this.getExternalFilesDir(null), file + ".txt");
            if (!testFile.exists())
                testFile.createNewFile();

            // Adds a line to the file
            BufferedWriter writer = new BufferedWriter(new FileWriter(testFile, false));
            writer.write(data);
            writer.close();
            // Refresh the data so it can seen when the device is plugged in a
            // computer. You may have to unplug and replug the device to see the
            // latest changes. This is not necessary if the user should not modify
            // the files.
        } catch (IOException e) {
            Log.e("ReadWriteFile", "Unable to write to the " + file + ".txt file.");
        }
    }

    public String readData(String file)
    {
        String textFromFile = "";
        // Gets the file from the primary external storage space of the
        // current application.
        File testFile = new File(this.getExternalFilesDir(null), file + ".txt");
        if (testFile != null) {
            BufferedReader reader;
            try {
                reader = new BufferedReader(new FileReader(testFile));
                String line;

                while ((line = reader.readLine()) != null) {
                    textFromFile += line.toString();
                }
                reader.close();
            } catch (Exception e) {
                Log.e("ReadWriteFile", "Unable to read the " + file + ".txt file.");
                return "error";
            }
        }
        return textFromFile;
    }
}