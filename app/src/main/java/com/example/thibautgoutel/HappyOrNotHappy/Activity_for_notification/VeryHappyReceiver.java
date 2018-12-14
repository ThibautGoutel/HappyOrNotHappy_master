package com.example.thibautgoutel.HappyOrNotHappy.Activity_for_notification;

import android.app.NotificationManager;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.thibautgoutel.HappyOrNotHappy.Base_de_donnee.CSVWriter;
import com.example.thibautgoutel.HappyOrNotHappy.Base_de_donnee.MOOD;
import com.example.thibautgoutel.HappyOrNotHappy.Base_de_donnee.MyBDD;
import com.example.thibautgoutel.HappyOrNotHappy.Base_de_donnee.Send;
import com.example.thibautgoutel.HappyOrNotHappy.Notification_receiver.AlarmReceiver;
import com.example.thibautgoutel.HappyOrNotHappy.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class VeryHappyReceiver extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.receiver_layout);

        //Initialisation des variables transmisent depuis le MainActivité
        String mood_name = getIntent().getStringExtra("mood");  //Humeur
        String id_user = readData("id_user"); //Identificateur de l'utilisateur

        //Creation de la base de donnée
        MyBDD database = new MyBDD(this);

        //Creation d'une humeur à partir de l'id de l'utilisateur et de son humeur
        MOOD mood = new MOOD(mood_name, id_user.substring(7));

        //Ajout de l'humeur dans la base de donnée
        database.addMood(mood);

        //Envoi de l'humeur dans la base de donnée du serveur
        Send objSend = new Send();
        objSend.setMyBDD(database);
        objSend.execute("");

        //TODO// A enlever
        //Exporter la base de donnée en fichier CSV sur le téléphone
        exportDB(database, 4);

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
                    textFromFile += "\n";
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