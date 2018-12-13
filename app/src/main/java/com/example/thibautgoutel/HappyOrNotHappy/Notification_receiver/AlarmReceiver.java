package com.example.thibautgoutel.HappyOrNotHappy.Notification_receiver;

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
import com.example.thibautgoutel.HappyOrNotHappy.R;

import java.util.Calendar;

import static android.content.Context.ALARM_SERVICE;

public class AlarmReceiver extends BroadcastReceiver
{
    Context ct;
    Intent it;

    String[] list_reasons = {"Pas de raisons", "Raisons personnelles", "Raisons professionnelles"};

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

        int intervalle = it.getIntExtra("intervalle",-1);

        if(intent.getBooleanExtra("verif",false)) {
            showCustomNotification(context, intent);
        }

        if(intent.getBooleanExtra("service",false)) {
            Calendar calendar = Calendar.getInstance();
            long time_real = calendar.getTimeInMillis();

            Log.d("Intervalle = ", String.valueOf(intervalle));

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
            intent = new Intent(context, AlarmReceiver.class);
            intent.putExtra("id_user", intent.getStringExtra("id_user"));
            intent.putExtra("intervalle", intervalle);
            intent.putExtra("verif", true);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 234324243, intent, 0);
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, time_real, intervalle, pendingIntent);
        }

        if(intent.getAction() != null) {
            if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
                Calendar calendar = Calendar.getInstance();
                long time_real = calendar.getTimeInMillis();

                synchroIdUser();

                //Creation de l'intervalle en SharedPreference pour qu'il soit accessible de chaques classe
                SharedPreferences settings = ct.getSharedPreferences( "PrivateSettings" , Context.MODE_PRIVATE);
                intervalle = settings.getInt("intervalle", 6*60*1000);

                Log.d("ERROR", String.valueOf(intervalle));

                AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
                intent = new Intent(context, AlarmReceiver.class);
                intent.putExtra("id_user", intent.getStringExtra("id_user"));
                intent.putExtra("intervalle", intervalle);
                intent.putExtra("verif", true);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 234324243, intent, 0);
                alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, time_real, intervalle, pendingIntent);

                background = new Intent(context, SensorService.class);
                context.startService(background);
            }
        }
    }

    private void setRemoteViews(RemoteViews remoteViews) {

        //Creation des Intents pour chaques humeurs différente en y incluant les variable à passer depuis le MainActivity vers les activité des humeurs

        Intent veryHappyReceiver = new Intent(ct, VeryHappyReceiver.class);
        veryHappyReceiver.putExtra("mood", ct.getString(R.string.very_happy));
        veryHappyReceiver.putExtra("id_user", it.getStringExtra("id_user"));
        veryHappyReceiver.putExtra("intervalle", it.getIntExtra("intervalle",-1));
        PendingIntent pendingVeryHappyIntent = PendingIntent.getActivity(ct, 0, veryHappyReceiver, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent happyReceiver = new Intent(ct, HappyReceiver.class);
        happyReceiver.putExtra("mood", ct.getString(R.string.happy));
        happyReceiver.putExtra("id_user", it.getStringExtra("id_user"));
        happyReceiver.putExtra("intervalle", it.getIntExtra("intervalle",-1));
        PendingIntent pendingHappyIntent = PendingIntent.getActivity(ct, 0, happyReceiver, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent neutralReceiver = new Intent(ct, NeutralReceiver.class);
        neutralReceiver.putExtra("mood", ct.getString(R.string.neutral));
        neutralReceiver.putExtra("id_user", it.getStringExtra("id_user"));
        neutralReceiver.putExtra("intervalle", it.getIntExtra("intervalle",-1));
        PendingIntent pendingNeutralIntent = PendingIntent.getActivity(ct, 0, neutralReceiver, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent notHappyReceiver = new Intent(ct, NotHappyReceiver.class);
        notHappyReceiver.putExtra("mood", ct.getString(R.string.not_happy));
        notHappyReceiver.putExtra("id_user", it.getStringExtra("id_user"));
        notHappyReceiver.putExtra("intervalle", it.getIntExtra("intervalle",-1));
        PendingIntent pendingNotHapyIntent = PendingIntent.getActivity(ct, 0, notHappyReceiver, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent veryNotHappyReceiver = new Intent(ct, VeryNotHappyReceiver.class);
        veryNotHappyReceiver.putExtra("mood", ct.getString(R.string.very_not_happy));
        veryNotHappyReceiver.putExtra("id_user", it.getStringExtra("id_user"));
        veryNotHappyReceiver.putExtra("intervalle", it.getIntExtra("intervalle",-1));
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

    public void synchroIdUser()
    {
        SharedPreferences settings = ct.getSharedPreferences( "PrivateSettings" , Context.MODE_PRIVATE);
        String id = settings.getString("id_user", "error");

        String id_user;

        if(id.equals("error"))
        {
            MyBDD myBDD = new MyBDD(ct);
            id_user = String.valueOf(myBDD.getMood(0).getId());

            SharedPreferences.Editor edit = settings.edit();
            edit.putString("id_user", id_user);
            edit.apply();
        }
        else
        {
            id_user = id;

            SharedPreferences.Editor edit = settings.edit();
            edit.putString("id_user", id_user);
            edit.apply();
        }
    }
}