package com.example.thibautgoutel.HappyOrNotHappy.Notification_receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.thibautgoutel.HappyOrNotHappy.Activity_for_notification.HappyReceiver;
import com.example.thibautgoutel.HappyOrNotHappy.Main.MainActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class BootCompletedReceiver extends BroadcastReceiver {

    Context ct;
    Intent it;

    @Override
    public void onReceive(Context context, Intent arg1) {
        ct = context;
        it = arg1;

        if(readData("notification").equals("active")) {
            ct.startActivity(new Intent(context, MainActivity.class).putExtra("reboot",true));
        }

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