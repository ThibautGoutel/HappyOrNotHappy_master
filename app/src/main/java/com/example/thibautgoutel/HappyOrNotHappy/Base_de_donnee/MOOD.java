package com.example.thibautgoutel.HappyOrNotHappy.Base_de_donnee;

import java.util.Calendar;

public class MOOD
{
    ////////////////////ARGUMENTS/////////////////////////
    private int id;
    private String Mood;
    private String heure;
    private String user;


    ////////////////////Constructeurs/////////////////////////
    public MOOD(String id_user)
    {
        this.id=0;
        this.Mood = "";
        this.heure= Calendar.getInstance().getTime().toString();
        this.user= id_user;
    }

    public MOOD(String mood_, String id_user) {
        this.id = 0;
        this.Mood = mood_;
        this.heure = Calendar.getInstance().getTime().toString();
        this.user = id_user;
    }



        ////////////////////Méthodes/////////////////////////
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMood() {
        return Mood;
    }

    public void setMood(String mood) {
        Mood = mood;
    }

    public String getHeure() {
        return heure;
    }

    public void setHeure(String heure) {
        this.heure = heure;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }














}
