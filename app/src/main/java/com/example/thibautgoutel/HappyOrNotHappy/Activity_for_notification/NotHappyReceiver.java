package com.example.thibautgoutel.HappyOrNotHappy.Activity_for_notification;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.thibautgoutel.HappyOrNotHappy.Base_de_donnee.CSVWriter;
import com.example.thibautgoutel.HappyOrNotHappy.Base_de_donnee.MOOD;
import com.example.thibautgoutel.HappyOrNotHappy.Base_de_donnee.MyBDD;
import com.example.thibautgoutel.HappyOrNotHappy.Base_de_donnee.Send;
import com.example.thibautgoutel.HappyOrNotHappy.Notification_receiver.AlarmReceiver;
import com.example.thibautgoutel.HappyOrNotHappy.Notification_receiver.BackgroundService;
import com.example.thibautgoutel.HappyOrNotHappy.R;

import java.io.File;
import java.io.FileWriter;
import java.util.Calendar;

public class NotHappyReceiver extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.receiver_layout);

        //Cet Intent permet le fonctionnement en arrière plan sur certains téléphones
        Intent background = new Intent(this, BackgroundService.class);
        this.startService(background);

        //Initialisation des variables transmisent depuis le MainActivité
        String mood_name = getIntent().getStringExtra("mood");  //Humeur
        String id_user = getIntent().getStringExtra("id_user"); //Identificateur de l'utilisateur
        int intervalle = getIntent().getIntExtra("intervalle", -1); //Intervalle d'heure avant la prochaine notification

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        intervalle = settings.getInt("intervalle", -1);

        //Creation de la base de donnée
        MyBDD database = new MyBDD(this);

        //Creation d'une humeur à partir de l'id de l'utilisateur et de son humeur
        MOOD mood = new MOOD(mood_name, id_user);

        String[] tab = new String[3];
        tab[0] = id_user;
        tab[1] = mood_name;
        tab[2] = Calendar.getInstance().getTime().toString();

        //Ajout de l'humeur dans la base de donnée
        Send objSend = new Send();
        objSend.execute(tab);

        database.addMood(mood);

        //TODO//////////////////Verification de la connection internet/////
        //TODO/////////////////Si il y a connection => Ajout de la nouvelle partie dans le fichier csv///
        //Exportation de la base de donnée dans un fichier .csv (4 colonnes à exporter : id, id_user, time, moodd
        exportDB(database, 4);

        //Recuperation de l'heure actuel en milliseconde
        Calendar calendar = Calendar.getInstance();
        long time_real = calendar.getTimeInMillis();

        //Creation d'un Intente vers AlarmReceiver
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 234324243, intent, 0);

        //Planification d'une nouvelle notification avec l'intervalle voulu
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        //alarmManager.set(AlarmManager.RTC_WAKEUP, time_real + intervalle, pendingIntent);

        //Fermeture de la notification
        NotificationManager notifManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notifManager.cancel(AlarmReceiver.getNotificationId());

        //Attente de 700 milliseconde avant la fermeture de l'activité
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                finish();
            }
        }, 700);   //700 milliseconds
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void exportDB(MyBDD database, int number_column) {

        // Creation du fichier csv dans la mémmoire interne du téléphone
        File[] SDCARD = this.getExternalFilesDirs("");
        File mFile = new File(SDCARD[0] + "/" + "HappyOrNotHappy"); //SDCARD[0] => memoire interne, SDCARD[1] => memoire externe
        mFile.mkdir();
        File file = new File(mFile, "doc.csv");

        try {
            file.createNewFile();

            //Lecture de la database avec un curseur
            CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
            SQLiteDatabase db = database.getReadableDatabase();
            Cursor curCSV = db.rawQuery("SELECT * FROM " + database.getTableName(), null);
            csvWrite.writeNext(curCSV.getColumnNames());
            while (curCSV.moveToNext()) {
                //Colonnes a exporter
                String[] arrStr = new String[number_column];
                for (int i = 0; i < number_column; i++) {
                    arrStr[i] = curCSV.getString(i);
                }

                //Ecriture dans le fichier
                csvWrite.writeNext(arrStr);

            }
            csvWrite.close();
        } catch (Exception sqlEx) {
            Log.e("MainActivity", sqlEx.getMessage(), sqlEx);
        }
    }
}