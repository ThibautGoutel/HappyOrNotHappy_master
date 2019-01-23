package com.example.thibautgoutel.HappyOrNotHappy.Notification_receiver;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.example.thibautgoutel.HappyOrNotHappy.Activity_for_notification.HappyReceiver;
import com.example.thibautgoutel.HappyOrNotHappy.Activity_for_notification.NeutralReceiver;
import com.example.thibautgoutel.HappyOrNotHappy.Activity_for_notification.NotHappyReceiver;
import com.example.thibautgoutel.HappyOrNotHappy.Activity_for_notification.VeryHappyReceiver;
import com.example.thibautgoutel.HappyOrNotHappy.Activity_for_notification.VeryNotHappyReceiver;
import com.example.thibautgoutel.HappyOrNotHappy.Base_de_donnee.MyBDD;
import com.example.thibautgoutel.HappyOrNotHappy.Main.MainActivity;
import com.example.thibautgoutel.HappyOrNotHappy.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

import static android.content.Context.ALARM_SERVICE;

public class AlarmReceiver extends BroadcastReceiver
{
    Context ct;
    Intent it;

    //Parametre de notification
    private Notification notifyPlayer;
    public static int notificationId = 100;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Intent background = new Intent(context, SensorService.class);
        context.startService(background);

        ct = context;
        it = intent;

        int intervalle = Integer.parseInt(readData("intervalle"));

        showCustomNotification(context, intent);

        if(intent.getBooleanExtra("service",false)) {

            Log.d("Intervalle = ", String.valueOf(intervalle));

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
            intent = new Intent(context, AlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 234324243, intent, 0);

            // Set the alarm to start at 7:45 AM
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, 7);
            calendar.set(Calendar.MINUTE, 45);
            calendar.add(Calendar.DAY_OF_YEAR, 1);

            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_HALF_DAY, pendingIntent);
        }
    }

    private void setRemoteViews(RemoteViews remoteViews) {

        //Creation des Intents pour chaques humeurs différente en y incluant les variable à passer depuis le MainActivity vers les activité des humeurs

        Intent veryHappyReceiver = new Intent(ct, VeryHappyReceiver.class);
        veryHappyReceiver.putExtra("mood", ct.getString(R.string.very_happy));
        PendingIntent pendingVeryHappyIntent = PendingIntent.getActivity(ct, 0, veryHappyReceiver, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent happyReceiver = new Intent(ct, HappyReceiver.class);
        happyReceiver.putExtra("mood", ct.getString(R.string.happy));
        PendingIntent pendingHappyIntent = PendingIntent.getActivity(ct, 0, happyReceiver, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent neutralReceiver = new Intent(ct, NeutralReceiver.class);
        neutralReceiver.putExtra("mood", ct.getString(R.string.neutral));
        PendingIntent pendingNeutralIntent = PendingIntent.getActivity(ct, 0, neutralReceiver, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent notHappyReceiver = new Intent(ct, NotHappyReceiver.class);
        notHappyReceiver.putExtra("mood", ct.getString(R.string.not_happy));
        PendingIntent pendingNotHapyIntent = PendingIntent.getActivity(ct, 0, notHappyReceiver, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent veryNotHappyReceiver = new Intent(ct, VeryNotHappyReceiver.class);
        veryNotHappyReceiver.putExtra("mood", ct.getString(R.string.very_not_happy));
        PendingIntent pendingVeryNotHapyIntent = PendingIntent.getActivity(ct, 0, veryNotHappyReceiver, PendingIntent.FLAG_UPDATE_CURRENT);

        //Creation des listener sur les images en les reliant au Intentes créer auparavant
        remoteViews.setOnClickPendingIntent(R.id.very_not_happy, pendingVeryNotHapyIntent);
        remoteViews.setOnClickPendingIntent(R.id.not_happy, pendingNotHapyIntent);
        remoteViews.setOnClickPendingIntent(R.id.neutral, pendingNeutralIntent);
        remoteViews.setOnClickPendingIntent(R.id.happy, pendingHappyIntent);
        remoteViews.setOnClickPendingIntent(R.id.very_happy, pendingVeryHappyIntent);
    }

    public static int getNotificationId() {
        return notificationId;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void showCustomNotification(Context context, Intent intent) {

        //Initialisation de la notification
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "channel-01";
        String channelName = "Channel Name";
        int importance = NotificationManager.IMPORTANCE_HIGH;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance);
            notificationManager.createNotificationChannel(mChannel);
        }

        //Association du layout perso à la notification
        RemoteViews mContentView = new RemoteViews(ct.getPackageName(), R.layout.custom_push);

        PendingIntent pendingIntent = PendingIntent.getActivity(ct, 0, intent, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, channelId);

        //Creation de la notification
        notifyPlayer = mBuilder.setSmallIcon(R.drawable.happy)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
        notifyPlayer.contentView = mContentView;

        //Creation des listeners sur les notifications
        setRemoteViews(mContentView);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntent(intent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        mBuilder.setContentIntent(resultPendingIntent);

        //Demarage de la notification
        notificationManager.notify(notificationId, notifyPlayer);
    }

    public String readData(String file)
    {
        String textFromFile = "";
        // Gets the file from the primary external storage space of the
        // current application.
        File testFile = new File(ct.getExternalFilesDir(null), file + ".txt");
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